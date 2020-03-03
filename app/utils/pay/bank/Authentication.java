/*
 * @(#)Authentication.java 2016年9月19日上午10:22:35
 * Copyright 2015 Sopell, Inc. All rights reserved.
 */
package utils.pay.bank;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import play.Logger;
import utils.Base64;
import utils.pay.bank.domain.Constants;
import utils.pay.bank.test.BankTest;
import utils.pay.bank.util.FastJsonUtils;
import utils.pay.bank.util.JRAlgorithmUtils;
import utils.pay.bank.util.SHA256Util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


 /**
 * @description.  银行卡四要素验证
 *  
 * @modificationHistory.  
 * @author liulj 2016年9月19日上午10:22:35 TODO
 */

public class Authentication {

	private static Log log = LogFactory.getLog(BankTest.class);
    private static final String  token_url = "https://dc.jdpay.com/certification/bankelem/getToken.do";
    private static final String    msg_url = "https://dc.jdpay.com/certification/card/authentication.do";
    private static final String verify_url = "https://dc.jdpay.com/certification/card/confirm.do";
    //private static final String  query_url = "";
    private static final String DES3KEY = "mgcB84Z84GkP9VaqTtRLtBrA";
    //private static final String notifyURL="";
    
    private static final String ACCOUNT_ID = "zhongyiyuntouzi";
    private static final String ACCOUNT_TYPE="2";
    
	public static String getToken() throws Exception {
		// 1.基础请求数据
		JSONObject json = new JSONObject();
		json.put("merchantCode", Constants.merchantCode);
		json.put("version", Constants.version);
		json.put("accountId", ACCOUNT_ID);
		json.put("accountType", ACCOUNT_TYPE);
		json.put("notifyURL", "");// 可为空
		json.put("extParam", "{}");// 可为空
		// System.out.println(123456);

		// 2.构造base64编码的请求数据
		String encodeRequestData = makeEncodeRequestJson(json.toJSONString(), DES3KEY);
		//Logger.info("encodeRequestData---" + encodeRequestData);
		// 3.生成签名数据
		String sign = getSignature(Constants.version, Constants.charset, encodeRequestData, DES3KEY);
		JSONObject param = new JSONObject();
		param.put("version", Constants.version);
		param.put("checkSign", sign);
		param.put("data", encodeRequestData);
		param.put("charset", Constants.charset);

		// 4.进行urlEncode
		String reqStr = JRAlgorithmUtils.urlEncode(param.toJSONString(), "UTF-8");
		//Logger.info("reqStr---" + reqStr);
		// 5.发送请求
		String feedback = streamPost(token_url, reqStr);
		//Logger.info("feedback---" + feedback);
		// 6.数据解码
		String responseData = decodeResponseJson(feedback);
		//Logger.info("responseData---" + responseData);
		// 7. 对token进行urlEncode
		Map<String, String> map = new HashMap<String, String>();
		map = (Map<String, String>) JSON.parseObject(responseData, Map.class);
		String urlToken = URLEncoder.encode(map.get("token"), Constants.charset);
		Logger.info("urlToken---" + urlToken);
		// return urlToken;
		Logger.info("jd+++++++++++++++++token---" + JSON.toJSONString(map));
		return map.get("token");
	}
    
	public static JSONObject getMsg(String token, JSONObject bankInfo) throws Exception {
		JSONObject result = new JSONObject();
		
		Logger.info("用户信息："+bankInfo);
		
		// 1.基础请求数据
		JSONObject json = bankInfo;
		// 第一步获得的token
		json.put("token", token);
		// 2.构造base64编码的请求数据
		String encodeRequestData = makeEncodeRequestJson(json.toJSONString(), DES3KEY);
		//Logger.info("encodeRequestData---" + encodeRequestData);
		// 3.生成签名数据
		String sign = getSignature(Constants.version, Constants.charset, encodeRequestData, DES3KEY);
		JSONObject param = new JSONObject();
		param.put("version", Constants.version);
		param.put("checkSign", sign);
		param.put("data", encodeRequestData);
		param.put("charset", Constants.charset);
		// 4.进行urlEncode
		String reqStr = JRAlgorithmUtils.urlEncode(param.toJSONString(), "UTF-8");
		//Logger.info("reqStr---" + reqStr);
		// 5.发送请求
		String feedback = streamPost(msg_url, reqStr);
		//Logger.info("feedback---" + feedback);
		
		result = JSON.parseObject(feedback);
		if(result.getString("resultCode").equals("SUCC")){
			
			// 6.数据解码
			String responseData = decodeResponseJson(feedback);
			//Logger.info("responseData---" + responseData);
			result = JSON.parseObject(responseData);
		}
		
		
		
		/*// 7. 对token进行urlEncode
		Map<String, String> map = new HashMap<String, String>();
		map = (Map<String, String>) JSON.parseObject(responseData, Map.class);
		String verfyCode = map.get("verfyCode");
		Logger.info("verfyCode---" + verfyCode);*/
		Logger.info("jd+++++++++++++++++msg---" + JSON.toJSONString(result));
		return result;
	}
	
	public static JSONObject confirm(String token, String verfyCode, JSONObject bankInfo) throws Exception {
		JSONObject result = new JSONObject();
		
    	//1.基础请求数据
    	JSONObject json = bankInfo;
    	
    	json.put("token", token);
    	//第二步获得的短信验证码
    	json.put("verfyCode", verfyCode);
    	
        //2.构造base64编码的请求数据
        String encodeRequestData = makeEncodeRequestJson(json.toJSONString() , DES3KEY );
        //Logger.info("encodeRequestData---"+encodeRequestData);
        //3.生成签名数据
        String sign=getSignature(Constants.version, Constants.charset, encodeRequestData, DES3KEY);
        JSONObject param = new JSONObject();
        param.put("version", Constants.version);
        param.put("checkSign", sign);
        param.put("data", encodeRequestData);
        param.put("charset", Constants.charset);
        //4.进行urlEncode
        String reqStr=JRAlgorithmUtils.urlEncode(param.toJSONString(),"UTF-8");
       // Logger.info("reqStr---"+reqStr);
        //5.发送请求
        String feedback = streamPost(verify_url , reqStr);
       // Logger.info("feedback---" + feedback);
        
        result = JSON.parseObject(feedback);
		if(result.getString("resultCode").equals("SUCC")){
			
			// 6.数据解码
			String responseData = decodeResponseJson(feedback);
			//Logger.info("responseData---" + responseData);
			result = JSON.parseObject(responseData);
		}
        
       /* //6.数据解码
        String responseData=decodeResponseJson( feedback );
        Logger.info("responseData---" + responseData );
        //7. 对token进行urlEncode
        Map<String,String> map=new HashMap<String,String>();
        map=(Map<String,String>)JSON.parseObject(responseData, Map.class);
        String resultCode=map.get("resultCode");
        String resultInfo=map.get("resultInfo");
        Logger.info("resultCode---"+resultCode+" resultInfo---"+resultInfo);*/
		Logger.info("jd+++++++++++++++++verfy---" + JSON.toJSONString(result));
        return result;
    }
	
	private static String getSignature(String version, String charset, String data, String privateKey) throws Exception {
		String localSign = new String(SHA256Util.encrypt(version + charset + data + privateKey));
		return localSign;
	}

	private static String makeEncodeRequestJson(String requestJson, String des3key) throws Exception {
		//String reqData = BASE64Util.encode(requestJson.getBytes("UTF-8"));
		String reqData = new Base64().encode(requestJson.getBytes("UTF-8"));
		return reqData;
	}

	private static String decodeResponseJson(String responseJson) throws Exception {
		Map<String, String> map = FastJsonUtils.toBean(responseJson, HashMap.class);
		String responseData = map.get("data");
		//byte[] decodeBase64 = BASE64Util.decode(responseData);
		byte[] decodeBase64 = new Base64().decode(responseData);
		String strRS = new String(decodeBase64, "UTF-8");
		return strRS;
	}

	private static String streamPost(String url, String param) {
		// HashMap类型的对象传输
		HttpClient client = null;
		PostMethod postMethod = null;
		try {
			client = new HttpClient();
			postMethod = new PostMethod(url);
			postMethod.setRequestBody(param);
			client.executeMethod(postMethod);
			String result = JRAlgorithmUtils.urlDecode(postMethod.getResponseBodyAsString(), "UTF-8");
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (postMethod != null) {
				postMethod.releaseConnection();
			}
		}

	}
    
    public static void main(String[] args) {
    	JSONObject bankInfo = new JSONObject();
    	bankInfo.put("realName", "刘朗君");
    	bankInfo.put("idNumber", "421125198810235257");
    	bankInfo.put("cardPan", "6222021202014658935");
    	bankInfo.put("cardType", "D");
    	bankInfo.put("bankCode", "ICBC");
    	bankInfo.put("mobile", "18758265108");
    	
    	// {"charset":"UTF-8","checkSign":"","data":"","resultCode":"BEPV_001","resultInfo":"真实姓名不合法","version":"1.0"}
    	try {
    		/*String token = getToken();
    		System.err.println("token: "+token);
    		
			JSONObject msg = getMsg(token, bankInfo);
			System.err.println("msg: "+msg);*/
			
			/*JSONObject confirm = confirm("SbdzQR2ja0cltoFtZm5W1ycRqVt5BbvrEhPFxueVvLc=", "159387", bankInfo);
			System.out.println("confirm: "+confirm);*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	String str = "SbdzQR2ja0cltoFtZm5W19OzgeQIpqoYmvdX+BhrI+4=";
    	System.out.println(new Base64().encode(str.getBytes()));
    	System.out.println(new String(new Base64().decode("U2JkelFSMmphMGNsdG9GdFptNVcxOU96Z2VRSXBxb1ltdmRYK0JockkrND0=")));
	}
}
