package payment.fy.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import models.t_mmm_data;
import models.t_return_data;
import models.t_sequences;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import payment.ips.util.IpsConstants;
import payment.ips.util.IpsPaymentUtil;
import play.Logger;
import utils.ErrorInfo;
import utils.JPAUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ips.security.utility.IpsCrypto;
import com.shove.Convert;
import constants.Constants;
import constants.PayType;

/**
 * 环讯相关工具类（用于日志打印/保存，接口参数通用组参，回调签名统一校验等）
 * @author xiaoqi
 *
 */
public class FyPaymentUtil {

	public static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
	public static final Gson gson = new Gson();
	
	public static Log log = LogFactory.getLog(FyPaymentUtil.class);

	/**
	 * 将xml转化成map
	 * @param data
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> parseXmlToMap(String data, ErrorInfo error) throws Exception{		
		if(data==null||data==""){
			error.code = -1;
			error.msg = "连接超时";
			return null;
		}
		Map<String, String> dataMap = new HashMap<String, String>();
		JSONObject jsonObj = XML.toJSONObject(data);
		JSONObject ap = jsonObj.getJSONObject("ap");
		JSONObject plain = ap.getJSONObject("plain");
		java.util.Iterator<String> iterator = plain.keys();
	
		while (iterator.hasNext()) {
			String key = iterator.next();
			if (plain.get(key).getClass().isAssignableFrom(String.class)) {
				dataMap.put(key, plain.get(key).toString());
			} else {
				dataMap.put(key, "");
			}
		}
		
		String str = data.substring(data.indexOf("<plain>"), data.indexOf("</plain>")+8);
		String signature = data.substring(data.indexOf("<signature>")+11, data.indexOf("</signature>"));
		dataMap.put("signature", signature.toString());
		dataMap.put("plain", str.toString());
		return dataMap;
		
	}
	
	/**
	 * 签名校验
	 * @param error
	 * @param paramMap 返回参数map
	 * @param desc 描述
	 * @param desc 接口类型
	 * @param is_check_heavy 是否需要防止重单; 需要:true; 不需要:false
	 * @throws Exception
	 */
	public static void checkSign(ErrorInfo error, Map<String, String> paramMap, String desc, String type, boolean is_check_heavy) throws Exception  {
		if(paramMap == null){
			
			error.code = -1;
			error.msg = "连接超时";
			return;
		}
		boolean flag = FyPaymentUtil.verifySign(paramMap.get("plain"), paramMap.get("signature"));
		if(flag == false){
			
			error.code = -2;
			error.msg = desc+"签名失败";
			return;
		}
		//满标放款
		if(type.equals(PayType.BID_AUDIT_SUCC.name())){
			
			if(!paramMap.get("resp_code").equals("0000") && !paramMap.get("resp_code").equals("3122")){ //3122状态码:原授权交易已全部完成
				
				error.code = -3;
				error.msg = FyPaymentUtil.getErrorMsg(paramMap.get("resp_code"));
				return;
			}
		}
		//借款管理费
		if(type.equals(PayType.BID_FEE.name())){
			
			if(!paramMap.get("resp_code").equals("0000") && !paramMap.get("resp_code").equals("5345")){ //5345状态码:流水号重复，未处理成功时，第三方不会记录该流水号已使用
				
				error.code = -3;
				error.msg = FyPaymentUtil.getErrorMsg(paramMap.get("resp_code"));
				return;
			}
			
		}
		//债权转让
		if(type.equals(PayType.DEBTOR_TRANSFER.name())){			
			if(!paramMap.get("resp_code").equals("0000") && !paramMap.get("resp_code").equals("5345")){ //5345状态码:流水号重复，未处理成功时，第三方不会记录该流水号已使用
				
				error.code = -3;
				error.msg = FyPaymentUtil.getErrorMsg(paramMap.get("resp_code"));
				return;
			}
			
		}
		
		//还款
		if(type.equals(PayType.REPAYMENT.name())){
			
			if(!paramMap.get("resp_code").equals("0000") && !paramMap.get("resp_code").equals("5345")){ //5345状态码:流水号重复，未处理成功时，第三方不会记录该流水号已使用
				
				error.code = -3;
				error.msg = FyPaymentUtil.getErrorMsg(paramMap.get("resp_code"));
				return;
			}
			
		}
		
		//其他
		if(!type.equals(PayType.BID_AUDIT_SUCC.name()) && !type.equals(PayType.BID_FEE.name()) && !type.equals(PayType.REPAYMENT.name()) && !type.equals(PayType.DEBTOR_TRANSFER.name())){
			
			if(!paramMap.get("resp_code").equals("0000")){
				
				error.code = -3;
				error.msg = FyPaymentUtil.getErrorMsg(paramMap.get("resp_code"));
				return;
			}
			
		}
		
		String sql = "update t_mmm_data m set m.status = 2 where m.orderNum = ? and m.status = 1";
		
		String orderNo = paramMap.get("mchnt_txn_ssn");
		int count  = JPAUtil.executeUpdate(error, sql, orderNo);
		
		if(is_check_heavy){
			
			if(count == 0){			
				error.code = -4;
				error.msg = desc + " :处理成功";	
				return;
			}
		}
		
		error.code = 1;
		error.msg = desc + "成功!";	
		return;
	}
	/**
	 * 校验表单返回通知校验
	 * @param error
	 * @param paramMap 返回参数map
	 * @param desc 描述
	 * @param is_check_heavy 是否需要防止重单; 需要:true; 不需要:false
	 * @param keys 待签名key值，按顺序来
	 * @throws Exception
	 */
	public static void checkFormSign(ErrorInfo error, Map<String, String> paramMap, String desc, boolean is_check_heavy, String... keys) throws Exception  {
		if(paramMap == null){
			
			error.code = -1;
			error.msg = "连接超时";
			return;
		}
		String sign = "";
		for(int i = 0; i< keys.length; i++){
			sign = sign + paramMap.get(keys[i]) + "|";
		}
		sign = sign.substring(0, sign.length() - 1);
		boolean flag = FyPaymentUtil.verifySign(sign, paramMap.get("signature"));
		if(flag == false){
			
			error.code = -2;
			error.msg = desc+"签名失败";
			return;
		}
		if(!paramMap.get("resp_code").equals("0000")){
			
			error.code = -3;
			error.msg = FyPaymentUtil.getErrorMsg(paramMap.get("resp_code"));
			return;
		}
		String sql = "update t_mmm_data m set m.status = 2 where m.orderNum = ? and m.status = 1";
		
		String orderNo = paramMap.get("mchnt_txn_ssn");
		int count  = JPAUtil.executeUpdate(error, sql, orderNo);		
		if(is_check_heavy){
			
			if(count == 0){			
				error.code = -4;
				error.msg = desc + " :处理成功";	
				return;
			}
		}
		
		error.code = 1;
		error.msg = desc + "成功!";	
		return;
	}
	/**
	 * 生成签名
	 * @param map
	 * @return
	 */
	public static String createSign(Map<String, String> map) {
		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.putAll(map);
		dataMap.remove("signature");
		dataMap.remove("body");
		String[] dataArray = new String[dataMap.size()];
		int i = 0;
		String keys = "";
		for (Map.Entry<String, String> entry : dataMap.entrySet()) {
			dataArray[i] = entry.getKey();
			i++;
			keys = keys +entry.getKey()+"|";
		}
		Arrays.sort(dataArray, String.CASE_INSENSITIVE_ORDER);

		StringBuffer sign = new StringBuffer("");
		for (int j = 0; j < dataArray.length; j++) {
			sign = sign.append(dataMap.get(dataArray[j]) + "|");
		}
		String signatureStr = FyPaymentUtil.sign(sign.substring(0, sign.length() - 1));
		return signatureStr;
	}

	

	/**
	 * init:初始化私钥
	 */
	public static void initPrivateKey() {
		try {
			if (FyConstants.privateKey == null) {
				FyConstants.privateKey = getPrivateKey(FyConstants.privateKeyPath);
			}
		} catch (Exception e) {
			System.out.println("SecurityUtils初始化失败" + e.getMessage());
			e.printStackTrace();
			System.out.println("密钥初始化失败");
		}
	}

	/**
	 * 初始化公钥
	 */
	public static void initPublicKey() {
		try {
			if (FyConstants.publicKey == null) {
				FyConstants.publicKey = getPublicKey(FyConstants.publicKeyPath);
			}
		} catch (Exception e) {
			System.out.println("SecurityUtils初始化失败" + e.getMessage());
			e.printStackTrace();
			System.out.println("密钥初始化失败");
		}
	}

	/**
	 * 对传入字符串进行签名
	 * 
	 * @param inputStr
	 * @return @
	 */
	public static String sign(String inputStr) {
		String result = null;
		try {
			if (FyConstants.privateKey == null) {
				// 初始化
				initPrivateKey();
			}
			byte[] tByte;
			Signature signature = Signature.getInstance("SHA1withRSA", "BC");
			signature.initSign(FyConstants.privateKey);
			signature.update(inputStr.getBytes("UTF-8"));
			tByte = signature.sign();
			result = Base64.encode(tByte);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("密钥初始化失败");
		}
		return result;
	}

	/**
	 * 对富友返回的数据进行验签
	 * @param src 返回数据明文
	 * @param signValue 返回数据签名
	 * @return
	 */
	public static boolean verifySign(String src, String signValue) {
		boolean bool = false;
		try {
			if (FyConstants.publicKey == null) {
				initPublicKey();
			}
			Signature signature = Signature.getInstance("SHA1withRSA", "BC");
			signature.initVerify(FyConstants.publicKey);
			signature.update(src.getBytes("UTF-8"));
			bool = signature.verify(Base64.decode(signValue));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("密钥初始化失败");
		}
		return bool;
	}

	private static PrivateKey getPrivateKey(String filePath) {
		String base64edKey = readFile(filePath);
		KeyFactory kf;
		PrivateKey privateKey = null;
		try {
			kf = KeyFactory.getInstance("RSA", "BC");
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64
					.decode(base64edKey));
			privateKey = kf.generatePrivate(keySpec);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("密钥初始化失败");
		}
		return privateKey;
	}

	private static PublicKey getPublicKey(String filePath) {
		String base64edKey = readFile(filePath);
		KeyFactory kf;
		PublicKey publickey = null;
		try {
			kf = KeyFactory.getInstance("RSA", "BC");
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64
					.decode(base64edKey));
			publickey = kf.generatePublic(keySpec);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("密钥初始化失败");
		}
		return publickey;
	}

	private static String readFile(String fileName) {
		try {
			File f = new File(fileName);
			FileInputStream in = new FileInputStream(f);
			int len = (int) f.length();

			byte[] data = new byte[len];
			int read = 0;
			while (read < len) {
				read += in.read(data, read, len - read);
			}
			in.close();
			return new String(data);
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * 生成流水号(最长20位)
	 * @param userId (不能为负，系统行为：0)
	 * @param operation
	 * @return
	 */
	public static String createBillNo() {
		t_sequences sequence = new t_sequences();
		sequence.save();		
		return format.format(new Date()) + sequence.id+"" ;
	}
	
	/**
	 * 带前缀的流水号
	 * @param pre (满标审核通过:BS; 满标审核不通过:BF; 标的借款管理费:BM; 标的投标奖励:BA; 标的解冻保证金:BUF; 债权转让:D; 本金垫付:A; 还款:R; 投资利息管理费:RM; 债权转让手续费:DM; 垫付还款:RA)备注:定义前缀的目的是，防止重复还款，重复转账
	 * @param orderNum
	 * @return
	 */
	public static String createBillNo(String pre, String orderNum) {
		
		return pre + orderNum;
	}
	
	/**
	 * 打印提交日志
	 * @param xmlMap 提交参数集合
	 * @param mark 日志头提示
	 * @param type 接口类型
	 * @param is_save_log 是否需要数据库日志
	 */
	@SuppressWarnings("unchecked")
	public static void printRequestData(Map<String, String> xmlMap, String mark, String type, boolean is_save_log){
		
		Logger.info("******************"+mark+"开始******************");
		for(Entry<String, String> entry : xmlMap.entrySet()){			
			Logger.info("***********"+entry.getKey() + "--" + entry.getValue());
		}
		Logger.info("******************"+mark+"结束******************");
		
		if(is_save_log){		
			JPAUtil.transactionBegin();
			t_mmm_data t_mmm_data = new t_mmm_data();
			t_mmm_data.mmmUserId = xmlMap.get("out_cust_no") == null ? "-1" : xmlMap.get("out_cust_no");
			t_mmm_data.orderNum = xmlMap.get("mchnt_txn_ssn");
			t_mmm_data.parent_orderNum = xmlMap.get("parentOrderno");
			t_mmm_data.op_time = new Date();
			t_mmm_data.msg = mark;
			t_mmm_data.data = gson.toJson(xmlMap);
			t_mmm_data.status = 1;
			t_mmm_data.type = type;		
			t_mmm_data.url = xmlMap.get("page_notify_url") == null ? "" : xmlMap.get("page_notify_url").toString();
			t_mmm_data.save();
			JPAUtil.transactionCommit();
		}
	}
	/**
	 * 打印回调日志信息
	 * @param paramMap 回调参数集合即params.all();
	 * @param desc 回调日志头信息
	 * @param type 接口类型
	 * @param is_save_log 是否需要数据库日志
	 */
	public static void printData(Map<String, String> paramMap, String desc, String type, boolean is_save_log){
		
		if(paramMap.containsKey("body")){
			paramMap.remove("body");
		}
		Gson gson = new Gson();
		Logger.info("**********************"+desc+"开始***************************");
		for(Entry<String, String> entry : paramMap.entrySet()){
			
			Logger.info("***********"+entry.getKey() + "--" + entry.getValue());
		}
		Logger.info("**********************"+desc+"结束***************************");
		
		if(is_save_log){
			
			JPAUtil.transactionBegin();			
			Map<String, Object> t_mmm_data = IpsPaymentUtil.queryMmmDataByOrderNum(paramMap.get("mchnt_txn_ssn"));		
			t_return_data t_return_data = new t_return_data();
			t_return_data.mmmUserId = t_mmm_data.get("mmmUserId") == null ? "" : t_mmm_data.get("mmmUserId").toString();
			t_return_data.orderNum = paramMap.get("mchnt_txn_ssn") ;
			t_return_data.parent_orderNum = paramMap.get("parentOrderno");
			t_return_data.op_time = new Date();
			t_return_data.type = type;
			t_return_data.data = gson.toJson(paramMap);				
			t_return_data.save();
			JPAUtil.transactionCommit();
		}
	}
	/**
	 * 获取错误信息
	 * @param code
	 * @return
	 */
	public static String getErrorMsg(String code){
		
		String msg = FyConstants.error.get(code);
		
		return msg == null ? "未知错误:" + code : msg;
		
	}
	
	/**
	 * 格式化金额,保留2位小数(将元转化为分)
	 * @param money 金额(单位元)
	 * @return 单位分
	 */
	public static String formatAmountToFen(double money){
		
		return String.format("%.0f", money * 100);
		
	}
	
	/**
	 * 格式化金额,保留2位小数(将分转化为元)
	 * @param money 金额(单位元)
	 * @return 单位分
	 */
	public static String formatAmountToYuan(double money){
		
		return String.format("%.2f", money / 100);
		
	}
	
	/**
	 * 构造表单信息
	 * @param maps 提交参数
	 * @param action 请求地址
	 * @return
	 */
	public static String createHtml(Map<String,String> maps, String action){
		StringBuffer buffer = new StringBuffer();
		buffer.append("<!DOCTYPE html>")
		.append("<html>")
		.append("<head>")
		.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />")
		.append("<title>Servlet AccountServlet</title>")
		.append("</head>")
		.append("<body>")
		.append("<h4>处理中...</h4>")
		.append("<form action="+action+" id=\"frm1\" method=\"post\">");
		for(Entry<String, String> entry : maps.entrySet()){
			buffer.append("<input type=\"hidden\" name="+entry.getKey()+" value="+entry.getValue()+" />");
		}
		buffer.append("</form>")
		.append("<script language=\"javascript\">")
		.append("document.getElementById(\"frm1\").submit();")
		.append("</script>")
		.append("</body>")
		.append("</html>");
		return buffer.toString();
	}
	/**
	 * 根据流水号查询提交参数
	 * @param orderNum
	 * @return
	 */
	public static Map<String, Object> queryMmmDataByOrderNum(String orderNum){
		
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> dataMap = JPAUtil.getMap(error, "select * from t_mmm_data m where m.orderNum = ?", orderNum);		
		return gson.fromJson(dataMap.get("data").toString(), new TypeToken<Map<String, String>>(){}.getType());
	}
}
