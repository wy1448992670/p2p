package payment.ips.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.t_mmm_data;
import models.t_return_data;
import models.t_sequences;

import org.apache.commons.httpclient.HttpStatus;
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
public class IpsPaymentUtil {

	public static final Gson gson = new Gson();
	/**
	 * 
	 * @param paramMap待签名参数map
	 * @param desc 接口描述
	 * @param desc 操作类型
	 * @throws Exception 
	 */
	public static ErrorInfo checkSign(Map<String, String> paramMap, String desc, Map<String, String> parseXml, String type, ErrorInfo error){
					
		if(paramMap == null){
			error.code = -1;
			error.msg = desc+"连接超时";	
			return error;
		}		
		//
		String signPlainText = paramMap.get("pMerCode") + paramMap.get("pErrCode") + paramMap.get("pErrMsg") + paramMap.get("p3DesXmlPara") + IpsConstants.cert_md5;
		//待签名串
		String localSign = com.ips.security.utility.IpsCrypto.md5Sign(signPlainText);		
		String pSign = paramMap.get("pSign");
		
		if(pSign == null || localSign == null){
			
			error.code = -2;
			error.msg = desc+"签名串不能为null";	
			return error;
		}
		
		if(pSign != null && localSign != null && !pSign.equals(localSign)){
			
			error.code = -3;
			error.msg = desc+"签名失败";	
			return error;
		}
		//注册
		returnValid(paramMap, type, PayType.REGISTER.name(), desc, error);
		if(error.code == -4){
			return error;
		}
		//提现
		returnValid(paramMap, type, PayType.WITHDRAW.name(), desc, error);
		if(error.code == -4){
			return error;
		}
		//充值
		returnValid(paramMap, type, PayType.RECHARGE.name(), desc, error);
		if(error.code == -4){
			return error;
		}
		//投资
		returnValid(paramMap, type, PayType.INVEST.name(), desc, error);
		if(error.code == -4){
			return error;
		}
		//解冻
		returnValid(paramMap, type, PayType.UNFREEZE.name(), desc, error);
		if(error.code == -4){
			return error;
		}
		//自动还款签约
		returnValid(paramMap, type, PayType.AUTO_REPAYMENT_SIGNATURE.name(), desc, error);
		if(error.code == -4){
			return error;
		}
		//自动投标签约
		returnValid(paramMap, type, PayType.AUTO_INVEST_SIGNATURE.name(), desc, error);
		if(error.code == -4){
			return error;
		}
		//登记债权转让
		returnValid(paramMap, type, PayType.DEBTOR_TRANSFER.name(), desc, error);
		if(error.code == -4){
			return error;
		}
		//债权转让确认
		returnValid(paramMap, type, PayType.DEBTOR_TRANSFER_CONFIRM.name(), desc, error);
		if(error.code == -4){
			return error;
		}
		//流标，MG02029F（标的信息不存在，按成功处理，执行本地逻辑撤销标的）MG02030F(标的已结束，按成功处理，走本地流程)
		if(type.equals(PayType.BID_AUDIT_FAIL.name())){
			
			if(!paramMap.get("pErrCode").equals("MG02505F") && !paramMap.get("pErrCode").equals("MG02029F") && !paramMap.get("pErrCode").equals("MG02030F") && !paramMap.get("pErrCode").equals("MG02503F")){
				
				error.code = -4;
				error.msg = desc + "操作失败," + paramMap.get("pErrMsg") + "!";	
				return error;
			}			
		}
		//标的审核通过
		if (type.equals(PayType.BID_AUDIT_SUCC.name())) {
			if(!paramMap.get("pErrCode").equals("MG02500F") && !paramMap.get("pErrCode").equals("MG00000F") && !paramMap.get("pErrCode").equals("MG00008F")){
				
				error.code = -4;
				error.msg = desc + "操作失败," + paramMap.get("pErrMsg") + "!";	
				return error;
			}
		}
		//标的新增
		if (type.equals(PayType.BIDCREATE.name())) {
			
			if(!paramMap.get("pErrCode").equals("MG02500F") && !paramMap.get("pErrCode").equals("MG02501F")){//MG02500F:标的新增，待短信验证；MG02501F：标的募集中，已回复短信，标的已生效；
							
				error.code = -4;
				error.msg = paramMap.get("pErrMsg") + "!";	
				return error;	
			}
			//线上环境，且为第三方返回还款处理中
			if(!Constants.IS_LOCALHOST && paramMap.get("pErrCode").equals("MG02500F")){ //此拦截是针对，环讯本地测试成功还款状态码，无法返回的问题
				
				error.code = -4;
				error.msg = "标的发布成功，请在30分钟内回复短信，标的即可生效!";	
				return error;
			}
			
		}
		//还款
		if (type.equals(PayType.REPAYMENT.name())) {
			
			if(!paramMap.get("pErrCode").equals("MG00000F") && !paramMap.get("pErrCode").equals("MG00008F")){
						
				error.code = -4;
				error.msg = paramMap.get("pErrMsg") + "!";	
				return error;
				
			}
			//线上环境，且为第三方返回还款处理中
			if(!Constants.IS_LOCALHOST && paramMap.get("pErrCode").equals("MG00008F")){ 
				
				error.code = -4;
				error.msg = "还款处理中，请耐心等待2-10分钟!";	
				return error;
			}
		}
		//登记担保方
		if (type.equals(PayType.ADVANCE.name())) {			
			if(!paramMap.get("pErrCode").equals("MG00000F") && !paramMap.get("pErrCode").equals("MG04003F")){ 
				//状态码MG04003F：重复担保（环讯针对同一个标的，同一个担保人，只能担保一次,这里将重复担保当做成功处理）
						
				error.code = -4;
				error.msg = paramMap.get("pErrMsg") + "!";	
				return error;			
			}
		}
		
		//逾期垫付成交
		if (type.equals(PayType.ADVANCE_CONFIRM.name())) {
			
			if(!paramMap.get("pErrCode").equals("MG00000F") && !paramMap.get("pErrCode").equals("MG00008F")){ 				
				error.code = -4;
				error.msg = paramMap.get("pErrMsg") + "!";	
				return error;			
			}
			
			//线上环境，且为第三方返回垫付处理中
			if(!Constants.IS_LOCALHOST && paramMap.get("pErrCode").equals("MG00008F")){ 
				
				error.code = -4;
				error.msg = "垫付处理中，请耐心等待2-10分钟!";	
				return error;
			}
		}		
		//逾期垫付还款
		if (type.equals(PayType.ADVANCE_REPAYMENT.name())) {
			
			if(!paramMap.get("pErrCode").equals("MG00000F") && !paramMap.get("pErrCode").equals("MG00008F")){ 				
				error.code = -4;
				error.msg = paramMap.get("pErrMsg") + "!";	
				return error;			
			}		
			//线上环境，且为第三方返回垫付处理中
			if(!Constants.IS_LOCALHOST && paramMap.get("pErrCode").equals("MG00008F")){ 
				
				error.code = -4;
				error.msg = "垫付处理中，请耐心等待2-10分钟!";	
				return error;
			}
		}		
		String sql = "update t_mmm_data m set m.status = 2 where m.orderNum = ? and m.status = 1";
		
		String orderNo = parseXml.get("pMemo1");
		int count  = JPAUtil.executeUpdate(error, sql, orderNo);
		if(count == 0){			
			error.code = -4;
			error.msg = desc + " :处理成功";	
			return error;
		}
		
		error.code = 1;
		error.msg = desc + "成功!";	
		return error;
	}
	
	/**
	 * 环迅通用校验
	 * @return ErrorInfo
	 */
	private static ErrorInfo returnValid(Map<String, String> paramMap, String type, String enumstr, String desc,
			ErrorInfo error) {
		if (type.equals(enumstr)) {
			if (paramMap.get("pErrCode").equals("MG00008F")) {// ips处理中

				error.code = -4;
				error.msg = desc + "ips处理中," + paramMap.get("pErrMsg") + "!";
				return error;
			}
			if (!paramMap.get("pErrCode").equals("MG00000F")) {

				error.code = -4;
				error.msg = desc + "操作失败," + paramMap.get("pErrMsg") + "!";
				return error;
			}
		}
		return error;
	}
	
	/**
	 * 校验提交到第三方参数是否有NULl/""空字符串
	 * @param parmas
	 * @throws Exception 
	 */
	public static void checkParams(Map<String, String> parmas) throws Exception{
		
		for(Entry<String, String> entry : parmas.entrySet()){			
			if(entry.getValue().isEmpty()){
				throw new Exception(entry.getKey() + "不能为NULL/空字符串");
			}
		}
	}
	
	/**
	 * 打印回调日志信息
	 * @param paramMap 回调参数集合即params.all();
	 * @param desc 回调日志头信息
	 * @param type 接口类型
	 * @param is_save_log 是否需要数据库日志
	 */
	public static void printData(Map<String, String> paramMap, String desc, PayType payType){
		
		if(paramMap.containsKey("body")){
			paramMap.remove("body");
		}
		Gson gson = new Gson();
		Logger.info("**********************"+desc+"开始***************************");
		Map<String, String> data = null;
		for(Entry<String, String> entry : paramMap.entrySet()){
			
			if(entry.getKey().equals("p3DesXmlPara")){
				try {
					data = IpsPaymentUtil.parseXmlToJson(entry.getValue());
					for(Entry<String, String> dataEntry : data.entrySet()){
						Logger.info("***********"+dataEntry.getKey() + "--" + dataEntry.getValue());
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}else{				
				Logger.info("***********"+entry.getKey() + "--" + entry.getValue());
			}
		}
		Logger.info("**********************"+desc+"结束***************************");
		
		if(payType.getIsSaveLog()){
			
			JPAUtil.transactionBegin();			
			Map<String, Object> t_mmm_data = IpsPaymentUtil.queryMmmDataByOrderNum(data.get("pMemo1"));		
			t_return_data t_return_data = new t_return_data();
			t_return_data.mmmUserId = t_mmm_data.get("mmmUserId") == null ? "" : t_mmm_data.get("mmmUserId").toString();
			t_return_data.orderNum = data.get("pMemo1") ;
			t_return_data.parent_orderNum = paramMap.get("parentOrderno");
			t_return_data.op_time = new Date();
			t_return_data.type = payType.name();
			t_return_data.data = gson.toJson(paramMap);				
			t_return_data.save();
			JPAUtil.transactionCommit();
		}
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
	
	/**
	 * 校验当前流水号的父流水号下，所有订单是否全部完成
	 * @param orderNum 流水号
	 */
	public static  void checkAllOrderIsComplete(String orderNum, ErrorInfo error){
		
		Map<String, Object> dataMap = JPAUtil.getMap(error, "select * from t_mmm_data m where m.orderNum = ?", orderNum);
		
		if(dataMap.get("parent_orderNum") == null){
			Logger.error("父流水号不能为null");
		}
		String parent_orderNum = dataMap.get("parent_orderNum").toString();	
		List<Map<String, Object>> list = JPAUtil.getList(error, "select * from t_mmm_data m where m.parent_orderNum = ? and m.status = 1", parent_orderNum);
		if(list == null){
			error.code = -1;
			error.msg = "订单未全部完成!";
			return;
		}
		if(list.size() > 0){
			error.code = -1;
			error.msg = "订单未全部完成!";
			return;
		}		
		error.code = 1;
		error.msg = "订单全部完成!";
		return;
	}
	
	/**
	 * 将map转换成xml
	 * 
	 * @param xmlMap
	 * @return
	 * @throws JSONException
	 * @throws IpayInterfaceException 
	 */
	public static String parseMapToXml(LinkedHashMap<String, String> xmlMap){

		String strxml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><pReq>";
		try {
			
			for (Map.Entry<String, String> entry : xmlMap.entrySet()) {
				
				String key = entry.getKey();
				String value = "";
				if (entry.getValue().getClass().isAssignableFrom(String.class)) {
					value = entry.getValue().toString();
				}
				if(value == null){
					new Exception("参数" + key + "不能为null!");
					
						throw new Exception("参数" + key + "不能为null!");
					
				}
				if(value.equals("")){				
					throw new Exception("参数" + key + "不能为null!");
				}
				strxml = strxml + "<" + key + ">" + value + "</" + key + ">";
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		strxml = strxml + "</pReq>";
		return strxml;
	}
	
	/**
	 * 将list转换成xml(仅用于还款pDetails还款明细的xml组装)
	 * 
	 * @param xmlMap
	 * @return
	 * @throws JSONException
	 * @throws IpayInterfaceException 
	 */
	public static String parseListToXml(List<LinkedHashMap<String, String>> list){

		StringBuffer sb = new StringBuffer("");		
		for(int i=0; i < list.size(); i++){
			LinkedHashMap<String, String> xmlMap = list.get(i);
			sb.append("<pRow>");
			  for(Map.Entry<String, String> entry : xmlMap.entrySet()){
				  sb.append("<"+entry.getKey()+">").append(entry.getValue()).append("</" + entry.getKey()+ ">");
			  }
			sb.append("</pRow>");
		}
		return sb.toString();
	}
	
	/**
	 * 获取格式日期
	 * @param format
	 */
	public static final String getFormatDate(String format){	
		return new SimpleDateFormat(format).format(new Date());
	}

	/**
	 * 将加密的xml转化成Map字符串
	 * 
	 * @param xmlStr
	 * @return
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 */
	public static LinkedHashMap<String, String> parseXmlToJson(String p3DesXmlPara)
			throws JSONException, UnsupportedEncodingException {		
		String xml = IpsCrypto.triDesDecrypt(p3DesXmlPara, IpsConstants.des_key,
				IpsConstants.des_iv);
		return parseNoEncryptXmlToJson(xml);
	}
	
	/**
	 * 将未加密的xml转化成Map字符串
	 * 
	 * @param xmlStr
	 * @return
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	public static LinkedHashMap<String, String> parseNoEncryptXmlToJson(String xml)
			throws JSONException, UnsupportedEncodingException {
		LinkedHashMap<String, String> jsonMap = new LinkedHashMap<String, String>();
		JSONObject jsonObj = XML.toJSONObject(xml);
		JSONObject pReq = jsonObj.getJSONObject("pReq");
		java.util.Iterator<String> iterator = pReq.keys();
		while (iterator.hasNext()) {
			String key = iterator.next();
			if (pReq.get(key).getClass().isAssignableFrom(String.class)) {
				jsonMap.put(key, pReq.get(key).toString());
			} else {
				jsonMap.put(key, "");
			}
			;
		}
		return jsonMap;
	}
	/**
	 * 将Map<String, String>转化为Map<String, Object>
	 * 
	 * @param xmlStr
	 * @return
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> parseObjectMapToStringMap(Map<String, String> map){
		Map<String, Object> dataMap =  new HashMap<String, Object>();
		for(Map.Entry<String, String> entry : map.entrySet()){
			dataMap.put(entry.getKey(), entry.getValue());
		}
		return dataMap;
	}
	
	/**
	 * 将Map<String, Object>转化为Map<String, String>
	 * 
	 * @param xmlStr
	 * @return
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> parseStringMapToObjectMap(Map<String, Object> map){
		Map<String, String> dataMap =  new HashMap<String, String>();
		for(Map.Entry<String, Object> entry : map.entrySet()){
			dataMap.put(entry.getKey(), entry.getValue().toString());
		}
		return dataMap;
	}
	
	/**
	 * 格式化金额,保留2位小数
	 * @param money 金额
	 * @return
	 */
	public static String formatAmount(String money){
		
		return String.format("%.2f", Convert.strToDouble(money, 0));
		
	}
	/**
	 * 发送数据
	 * 
	 * @param map
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String createHtml(String strxml, String url, String pMerCode, String htmlPMerCode, String htmlP3DesXmlPara, String htmlPSign){

		String str3DesXmlPana = strxml;
		str3DesXmlPana = com.ips.security.utility.IpsCrypto.triDesEncrypt(
				strxml, IpsConstants.des_key, IpsConstants.des_iv);
		str3DesXmlPana = str3DesXmlPana.replaceAll("\r", "");
		str3DesXmlPana = str3DesXmlPana.replaceAll("\n", "");
		String strSign = com.ips.security.utility.IpsCrypto.md5Sign(pMerCode
				+ str3DesXmlPana + IpsConstants.cert_md5);
		StringBuffer sb = new StringBuffer();

		sb.append("<!DOCTYPE html>");
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>Servlet AccountServlet</title>");
		sb.append("</head>");
		sb.append("<body>");
		sb.append("<form action=" + IpsConstants.POST_URL + url
				+ " id=\"frm1\" method=\"post\">");
		sb.append("<input type=\"hidden\" name=" + htmlPMerCode + " value="
				+ pMerCode + ">");
		sb.append("<input type=\"hidden\" name=" + htmlP3DesXmlPara + " value="
				+ str3DesXmlPana + ">");
		sb.append("<input type=\"hidden\" name=" + htmlPSign + " value="
				+ strSign + ">");
		sb.append("</form>");
		sb.append("<script language=\"javascript\">");
		sb.append("document.getElementById(\"frm1\").submit();");
		sb.append("</script>");
		sb.append("</body>");
		sb.append("</html>");
		Logger.info(sb.toString());
		return sb.toString();
	};
	/**
	 * webservice接口请求
	 * @param actionUrl 请求ACtionUrl 以银行列表为例,例如:http://tempuri.org/GetBankList
	 * @param action 请求接口, 以银行列表为例,例如:GetBankList
	 * @param xmlMap 参数集合
	 * @param merCodeSoap soapxml 属性，以银行列表为例,为字符串"argMerCode"
	 * @param desXmlParaSoap soapxml 属性, 以银行列表为例,没有这个属性填, 填"arg3DesXmlPara", 若有其属性，照着填
	 * @param SignSoap soapxml 属性 以银行列表为例 为字符串"argSign" 
	 * @throws JSONException 
	 * @throws UnsupportedEncodingException 
	 */
	public static  String sendWebService(String actionUrl, String action, LinkedHashMap<String, String> xmlMap, String merCodeSoap, String desXmlParaSoap, String SignSoap) throws Exception{
		
		String  strxml = "";
		if(xmlMap.size() > 0){
			strxml = IpsPaymentUtil.parseMapToXml(xmlMap);
		}
		strxml = com.ips.security.utility.IpsCrypto.triDesEncrypt(strxml, IpsConstants.des_key, IpsConstants.des_iv);
    	strxml = strxml.replaceAll("\r\n", "");
    	
    	String  strSign = com.ips.security.utility.IpsCrypto.md5Sign(IpsConstants.terraceNoOne + strxml + IpsConstants.cert_md5);
    	if (IpsConstants.GetBankList.equals(action)) {
    		strSign = com.ips.security.utility.IpsCrypto.md5Sign(IpsConstants.terraceNoOne + IpsConstants.cert_md5);
    	}
    	if (IpsConstants.QueryForAccBalance.equals(action)) {
    		strxml = xmlMap.get("argIpsAccount");
    		strSign = com.ips.security.utility.IpsCrypto.md5Sign(IpsConstants.terraceNoOne + xmlMap.get("argIpsAccount") + IpsConstants.cert_md5);
    	}
		String data = getSoapInputStream(actionUrl, action, strxml, strSign, merCodeSoap, desXmlParaSoap, SignSoap);
		return data;
	}
	/**
	 * webservice
	 * @param Url 请求地址
	 * @param argMerCode 商户号
	 * @param arg3DesXmlPara 参数xml
	 * @param argSign 签名
	 * @return
	 */
	private static String getSoapInputStream(String actionUrl, String action, String arg3DesXmlPara, String argSign, String merCodeSoap, String desXmlParaSoap, String signSoap) {  
        try {  
            String soap = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
            "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "<soap:Body>"+
            "  <"+action+" xmlns=\"http://tempuri.org/\">"+
            "    <"+merCodeSoap+">"+IpsConstants.terraceNoOne+"</"+merCodeSoap+">"+
            "    <"+desXmlParaSoap+">"+arg3DesXmlPara+"</"+desXmlParaSoap+">"+
            "    <"+signSoap+">"+argSign+"</"+signSoap+">"+
            "  </"+action+">"+
            "</soap:Body>"+
            "</soap:Envelope>"; 
            URL url = new URL(IpsConstants.WS_URL);  
            URLConnection conn = url.openConnection();  
            conn.setUseCaches(false);  
            conn.setDoInput(true);  
            conn.setDoOutput(true);  
            conn.setRequestProperty("Referer", "http://www.baidu.com/");
            conn.setRequestProperty("Content-Length", Integer.toString(soap  
                    .length()));  
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");  
            conn.setRequestProperty("SOAPAction", actionUrl);  
  
            OutputStream os = conn.getOutputStream();  
            OutputStreamWriter osw = new OutputStreamWriter(os, "utf-8");  
            osw.write(soap);
            osw.flush();  
            osw.close();  
           InputStream is = conn.getInputStream();
           ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int i = -1;
			
			while ((i = is.read()) != -1) {
				baos.write(i);
			}
		
			return baos.toString("utf-8");
  
        } catch (Exception e) {  
            e.printStackTrace();
            return null;  
        }  
    } 
	/**
	 * 组织httpClient参数
	 * 
	 * @param paramMap
	 * @return
	 */
	public static List<org.apache.http.NameValuePair> putParams(
			Map<String, String> paramMap) {
		List<org.apache.http.NameValuePair> params = new ArrayList<org.apache.http.NameValuePair>();

		for (Iterator<Entry<String, String>> iterator = paramMap.entrySet()
				.iterator(); iterator.hasNext();) {
			Entry<String, String> entry = iterator.next();

			params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		return params;
	}

	/**
	 * httpClient表单提交
	 * 
	 * @param url
	 * @param params
	 * @param urlEncoded
	 * @return
	 */
	public static String postMethod(String url, Map<String, String> map,
			String urlEncoded) {		
		return byPostMethodToHttpEntity(url, putParams(map), urlEncoded);
	}

	/**
	 * Post方式提交并且返回Entity字符�?
	 * 
	 * @param url
	 *            提交的url
	 * @param client
	 *            HttpClient
	 * @param post
	 *            HttpPost
	 * @param params
	 *            队列参数
	 * @param urlEncoded
	 *            url编码
	 * @return
	 */
	public static String byPostMethodToHttpEntity(String url,
			List<org.apache.http.NameValuePair> params, String urlEncoded) {

		HttpEntity entity = null;
		StringBuffer buff = new StringBuffer();
		// 创建线程安全的httpClient
		org.apache.http.client.HttpClient httpClient = new org.apache.http.impl.client.DefaultHttpClient(
				new ThreadSafeClientConnManager());
		HttpPost httpPost = new HttpPost(url);
		try {
			if (params != null) {
				org.apache.http.client.entity.UrlEncodedFormEntity uefEntity = new org.apache.http.client.entity.UrlEncodedFormEntity(
						params, urlEncoded);
				httpPost.setEntity(uefEntity);
			}
			HttpResponse response = httpClient.execute(httpPost);
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				entity = response.getEntity();
				buff.append(EntityUtils.toString(entity));
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			releaseSource(null, httpPost, httpClient);
		}
		return buff.toString();
	}

	/**
	 * 释放资源
	 * 
	 * @param httpGet
	 * @param httpPost
	 * @param httpClient
	 */
	public static void releaseSource(HttpGet httpGet, HttpPost httpPost,
			org.apache.http.client.HttpClient httpClient) {
		if (httpGet != null) {
			httpGet.abort();
		}
		if (httpPost != null) {
			httpPost.abort();
		}
		if (httpClient != null) {
			httpClient.getConnectionManager().shutdown();
		}
	}

}
