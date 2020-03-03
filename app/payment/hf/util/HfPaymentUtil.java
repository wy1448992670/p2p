package payment.hf.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import play.libs.Codec;
import utils.ErrorInfo;
import utils.JPAUtil;
import chinapnr.SecureLink;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import constants.Constants;
import constants.PayType;

/**
 * 环讯相关工具类（用于日志打印/保存，接口参数通用组参，回调签名统一校验等）
 * @author xiaoqi
 *
 */
public class HfPaymentUtil {

	public static final Gson gson = new Gson();
	
	/**
	 * 签名,状态码，防重复处理校验
	 * @param paramMap 返回参数
	 * @param desc 页面提示描述
	 * @param type 接口类型
	 * @param error
	 * @return
	 * @throws Exception
	 */
	public static void checkSign(Map<String, String> paramMap, String desc, PayType payType , ErrorInfo error){
					
		//第一步:连接超时
		if(paramMap == null){
			error.code = -1;
			error.msg = desc+"连接超时";	
			return ;
		}	
		
		//第二步:签名判断
		String cmdId = paramMap.get("CmdId");
		
		//获取需要签名的字段
		String[] keys = HfConstants.getRespChkValueKeys(cmdId);
		
		StringBuffer buffer =new StringBuffer();
		for(String key : keys){

			if(StringUtils.isBlank(paramMap.get(key))) continue;

			try {
				buffer.append(java.net.URLDecoder.decode(paramMap.get(key).trim(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		System.out.println("签名原串--"+buffer.toString());
		//第三方签名串
		String chkValue = paramMap.get("ChkValue").trim();
		boolean flag;
		try {
			
			flag = SignUtils.verifyByRSA(buffer.toString(), chkValue);	
			if(!flag){			
				error.code = -2;
				error.msg = desc+"签名失败!";	
				return ;
			}
			
		} catch (Exception e) {
					
			e.printStackTrace();
		}
		//第三步:状态码判断;根据不同接口，不同处理	
		String respCode = paramMap.get("RespCode");
		if(!respCode.equals("000")){
			
			error.code = -3;
			try {
				error.msg = desc + URLDecoder.decode(paramMap.get("RespDesc"), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}	
			return ;
		}
		
		// 根据特殊的支付类型做特殊操作
		if(PayType.REPAYMENT == payType) {
			// 解析批量还款返回参数
			MsgFlag msgFlag = resolveBatchRepaymentResp(paramMap);
			if(!msgFlag.isSucess()) {
				error.code = -4;
				error.msg = msgFlag.getMsg();
				return;
			}
		}
		
		if(payType.getIsSaveLog()){
			//第四部，防止重单
			String sql = "update t_mmm_data m set m.status = 2 where m.orderNum = ? and m.status = 1";
			
			//所有流水号放在私有域
			String orderNum = paramMap.get("MerPriv");		
			int count  = JPAUtil.executeUpdate(error, sql, orderNum);
			
			if(count == 0){
				error.code = Constants.ALREADY_RUN;
				error.msg = desc + " :处理成功";	
				return ;
			}
		}
		error.code = 1;
		error.msg = desc + "成功!";
		return ;
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
	 * 获取格式日期
	 * @param format
	 */
	public static final String getFormatDate(String format){	
		return new SimpleDateFormat(format).format(new Date());
	}
	
	/**
	 * 获取格式日期
	 * @param format
	 */
	public static final String getFormatDate(String format, Date date){	
		return new SimpleDateFormat(format).format(date);
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
	public static String formatAmount(double money){
		
		return String.format("%.2f", money);
		
	}
	
	/**
	 * 格式化金额,保留2位小数,格式："##,##0.00"
	 * @param money 金额
	 * @return
	 */
	public static String formatAmount2(double money){
		DecimalFormat myformat = new DecimalFormat();
		myformat.applyPattern("##,##0.00");
		
		return myformat.format(money);
	}
	
	
	
	/**
	 * 生成签名
	 * @param parmas 待签名字符串
	 * @return
	 */
	public static String createSign(Map<String,String> parmas){

		//接口类型
		String cmdId = (String) parmas.get("CmdId");
		
		String[] chkKeys = HfConstants.getChkValueKeys(cmdId);
		StringBuffer buffer = new StringBuffer();
		SecureLink sl = new SecureLink();
		if(chkKeys!=null){
			
			//拼接签名原串
			for(String key : chkKeys){
				String value = (String) (parmas.get(key) == null ? "" : parmas.get(key));
				buffer.append(value);
			}	
			
			String value = buffer.toString();
			
			//批量还款接口传输参数需要加密
			if(HfConstants.CMD_BATCHREPAYMENT.equals(cmdId)){
				value = Codec.hexMD5(buffer.toString());
			}		
			
			try {
				sl.SignMsg(HfConstants.MERID, HfConstants.MER_PRI_KEY_PATH, value.getBytes("utf-8"));
			} catch (UnsupportedEncodingException e) {
				
				e.printStackTrace();
			}			
		}
		return sl.getChkValue();
	}
	
	/**
	 * 获取生成签名后的Map集合
	 * @param parmas
	 */
	public static LinkedHashMap<String,String> createSignMap(LinkedHashMap<String,String> parmas){
				
		String sign = HfPaymentUtil.createSign(parmas);		
		parmas.put("ChkValue", sign);
		return parmas;	
	}
	/**
	 * 生成html表单
	 * @param map
	 * @param url
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	public static String createFormHtml(Map<String,String> maps,String action){
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
	
	/**
	 * json To map
	 * @param json
	 * @return
	 */
	public static Map<String,String> jsonToMap(String json){
		JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
		return jsonToMap(jsonObj);
	}
	
	/**
	 * json To map
	 * @param json
	 * @return
	 */
	private static Map<String,String> jsonToMap(JsonObject json){
		Set<Entry<String, JsonElement>> set =json.entrySet();
		Map<String,String> maps = new HashMap<String, String>();
		for(Entry<String, JsonElement> entry : set){
			maps.put(entry.getKey(), (entry.getValue() instanceof JsonNull | entry.getValue() == null)? "":(entry.getValue() instanceof JsonArray)?entry.getValue().getAsJsonArray().toString():entry.getValue().getAsString());
		}
		return maps;
	}
	
	/**
	 * 将中文字符串进行URLDecoder编码
	 * @param params
	 * @return
	 */
	public static String URLEncoder(String params){
		String result = "";
		try {
			result =  java.net.URLEncoder.encode(params, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 将URLDecoder编码字符串转化成中文
	 * @param params
	 * @return
	 */
	public static String URLDecoder(String params){
		String result = "";
		try {
			result =  java.net.URLDecoder.decode(params, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	/**
	 * 解析批量还款请求
	 * @param result
	 * @return
	 */
	public static MsgFlag resolveBatchRepaymentResp(Map<String, String> paramMap){
		JsonArray array = null;
		//失败条数(排除351重复还款)
		int num = 0;  
		MsgFlag msgFlag = new MsgFlag(true, "还款成功");
		if(HfConstants.SUCESSCODE.equals(paramMap.get("RespCode"))){
			 //失败条数
			int failNum = Integer.valueOf(paramMap.get("FailNum"));
			if(failNum > 0){
				array = new JsonParser().parse(paramMap.get("ErrMsg")).getAsJsonArray();
				for(JsonElement ele : array){
					//重复还款
					if(HfConstants.REPAYMENT_ED.equals(ele.getAsJsonObject().get("ItemCode").getAsString())){
						
					}else{
						num ++;
					}
				}
			}
			if(num  > 0){
				msgFlag = new MsgFlag(false,"还款失败");
				return msgFlag;
			}
		}else{
			msgFlag = new MsgFlag(false,paramMap.get("RespDesc"));
			return msgFlag;
		}
		return msgFlag;
	}


}
