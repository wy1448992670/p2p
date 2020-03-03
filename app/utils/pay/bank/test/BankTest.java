package utils.pay.bank.test;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import utils.Base64;
import utils.pay.bank.domain.Constants;
import utils.pay.bank.util.FastJsonUtils;
import utils.pay.bank.util.JRAlgorithmUtils;
import utils.pay.bank.util.SHA256Util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;



/**
 * 
 * @Author liheng
 * @Create In 2016年1月19日
 */
public class BankTest {

	private static Log log = LogFactory.getLog(BankTest.class);
    private static final String  token_url = "http://111.202.58.61:8102/bankelem-web/bankelem/getToken.do";
    private static final String    msg_url = "http://111.202.58.61:8102/bankelem-web/card/authentication.do";
    private static final String verify_url = "http://111.202.58.61:8102/bankelem-web/card/confirm.do";
    private static final String  query_url = "";
    private static final String DES3KEY = "012345678901234567890123";
    private static final String notifyURL="";

    
    @Test
    public  String getToken() throws Exception {
    	//1.基础请求数据
    	JSONObject json = new JSONObject();
    	json.put("merchantCode", Constants.merchantCode);
    	json.put("version", Constants.version);
    	json.put("accountId", "zhongyiyuntouzi");
    	json.put("accountType", "2");
        json.put("notifyURL", "");//可为空
        json.put("extParam", "{}");//可为空
//        System.out.println(123456);
         
        //2.构造base64编码的请求数据
        String encodeRequestData = makeEncodeRequestJson(json.toJSONString() , DES3KEY );
        log.info("encodeRequestData---"+encodeRequestData);
        //3.生成签名数据
        String sign=getSignature(Constants.version, Constants.charset, encodeRequestData, DES3KEY);
        JSONObject param = new JSONObject();
        param.put("version", Constants.version);
        param.put("checkSign", sign);
        param.put("data", encodeRequestData);
        param.put("charset", Constants.charset);
        
        
        
        //4.进行urlEncode
        String reqStr=JRAlgorithmUtils.urlEncode(param.toJSONString(),"UTF-8");
        log.info("reqStr---"+reqStr);
        //5.发送请求
        String feedback = streamPost(token_url , reqStr);
        log.info("feedback---" + feedback);
        //6.数据解码
        String responseData=decodeResponseJson( feedback );
        log.info("responseData---" + responseData );
        //7. 对token进行urlEncode
        Map<String,String> map=new HashMap<String,String>();
        map=(Map<String,String>)JSON.parseObject(responseData, Map.class);
        String urlToken=URLEncoder.encode(map.get("token"),Constants.charset);
        log.info("urlToken---"+urlToken);
       // return urlToken;
        return map.get("token");
    }
    
    
    private JSONObject getJson() throws Exception{
    	//1.基础请求数据
    	JSONObject json = new JSONObject();
//      	json.put("token", token);
      	
//		json.put("realName", "xxx");
//		json.put("idNumber", "xxxx");
//		json.put("cardPan", "xxx");
//		json.put("cardType", "C");
//		json.put("bankCode", "PAB");
//		json.put("mobile", "xxx");
//		json.put("cVV2", "");
//		json.put("certType", "1");
//		json.put("dsType", "1");
		
	        json.put("realName", "");
	        json.put("idNumber", "");
	        json.put("cardPan", "");
	        json.put("cardType", "");
	        json.put("bankCode", "");
	        json.put("mobile", "");
	        json.put("cVV2", "");
	        json.put("certType", "1");
	        json.put("dsType", "1");
		
		return json;
    }
    
    public String getMsg(String token, JSONObject bankInfo) throws Exception {
    	//1.基础请求数据
    	JSONObject json = getJson();
    	json.putAll(bankInfo);
    	/*json.put("realName", "刘朗君");
    	json.put("idNumber", "421125198810235257");
    	json.put("cardPan", "6222021202014658935");
    	json.put("cardType", "D");
    	json.put("bankCode", "ICBC");
    	json.put("mobile", "18758265108");*/
    	//第一步获得的token
    	//String tokenTemp="";
    	json.put("token", token);
        //2.构造base64编码的请求数据
        String encodeRequestData = makeEncodeRequestJson(json.toJSONString() , DES3KEY );
        log.info("encodeRequestData---"+encodeRequestData);
        //3.生成签名数据
        String sign=getSignature(Constants.version, Constants.charset, encodeRequestData, DES3KEY);
        JSONObject param = new JSONObject();
        param.put("version", Constants.version);
        param.put("checkSign", sign);
        param.put("data", encodeRequestData);
        param.put("charset", Constants.charset);
        //4.进行urlEncode
        String reqStr=JRAlgorithmUtils.urlEncode(param.toJSONString(),"UTF-8");
        log.info("reqStr---"+reqStr);
        //5.发送请求
        String feedback = streamPost(msg_url , reqStr);
        log.info("feedback---" + feedback);
        //6.数据解码
        String responseData=decodeResponseJson( feedback );
        log.info("responseData---" + responseData );
        //7. 对token进行urlEncode
        Map<String,String> map=new HashMap<String,String>();
        map=(Map<String,String>)JSON.parseObject(responseData, Map.class);
        String verfyCode=map.get("verfyCode");
        log.info("verfyCode---"+verfyCode);
        return verfyCode;
    }
    
    
    @Test
    public void testConfirm(String token, String verfyCode, JSONObject bankInfo) throws Exception {
    	//1.基础请求数据
    	JSONObject json = getJson();
    	json.putAll(bankInfo);
    	//第一步获得的token
    	//String tempToken="";
    	//第二步获得的短信验证码
    	//String verfyCode="";
    	json.put("token", token);
    	json.put("verfyCode", verfyCode);
        //2.构造base64编码的请求数据
        String encodeRequestData = makeEncodeRequestJson(json.toJSONString() , DES3KEY );
        log.info("encodeRequestData---"+encodeRequestData);
        //3.生成签名数据
        String sign=getSignature(Constants.version, Constants.charset, encodeRequestData, DES3KEY);
        JSONObject param = new JSONObject();
        param.put("version", Constants.version);
        param.put("checkSign", sign);
        param.put("data", encodeRequestData);
        param.put("charset", Constants.charset);
        //4.进行urlEncode
        String reqStr=JRAlgorithmUtils.urlEncode(param.toJSONString(),"UTF-8");
        log.info("reqStr---"+reqStr);
        //5.发送请求
        String feedback = streamPost(verify_url , reqStr);
        log.info("feedback---" + feedback);
        //6.数据解码
        String responseData=decodeResponseJson( feedback );
        log.info("responseData---" + responseData );
        //7. 对token进行urlEncode
        Map<String,String> map=new HashMap<String,String>();
        map=(Map<String,String>)JSON.parseObject(responseData, Map.class);
        String resultCode=map.get("resultCode");
        String resultInfo=map.get("resultInfo");
        log.info("resultCode---"+resultCode+" resultInfo---"+resultInfo);
    }
    
    
    @Test
//    public void testQuery() throws Exception {
//    	//1.基础请求数据
//    	JSONObject json =new JSONObject();
//    	json.put("idNumber", "");
//    	json.put("nameCn", "");
//    	json.put("merchantCode", Constants.merchantCode);
//        //2.构造base64编码的请求数据
//        String encodeRequestData = makeEncodeRequestJson(json.toJSONString() , DES3KEY );
//        log.info("encodeRequestData---"+encodeRequestData);
//        //3.生成签名数据
//        String sign=getSignature(Constants.version, Constants.charset, encodeRequestData, DES3KEY);
//        JSONObject param = new JSONObject();
//        param.put("version", Constants.version);
//        param.put("checkSign", sign);
//        param.put("data", encodeRequestData);
//        param.put("charset", Constants.charset);
//        //4.进行urlEncode
//        String reqStr=JRAlgorithmUtils.urlEncode(param.toJSONString(),"UTF-8");
//        log.info("reqStr---"+reqStr);
//        //5.发送请求
//        String feedback = streamPost(query_url , reqStr);
//        log.info("feedback---" + feedback);
//        //6.数据解码
//        String responseData=decodeResponseJson( feedback );
//        log.info("responseData---" + responseData );
//        //7. 对token进行urlEncode
//        Map<String,String> map=new HashMap<String,String>();
//        map=(Map<String,String>)JSON.parseObject(responseData, Map.class);
//        String resultCode=map.get("resultCode");
//        String resultInfo=map.get("resultInfo");
//        log.info("resultCode---"+resultCode+" resultInfo---"+resultInfo);
//    }
//    
    
    
    private String getSignature(String version,String charset,String data,String privateKey) throws Exception {
        String localSign = new String(SHA256Util.encrypt(version + charset+ data + privateKey));
        return localSign;
    }

    private String makeEncodeRequestJson( String requestJson , String des3key ) throws Exception {
        //String reqData = BASE64Util.encode(requestJson.getBytes("UTF-8"));
        String reqData = new Base64().encode(requestJson.getBytes("UTF-8"));
        return reqData;
    }

    private String decodeResponseJson( String responseJson ) throws Exception {
        Map<String,String> map  = FastJsonUtils.toBean( responseJson , HashMap.class );
        String responseData = map.get( "data" );
        //byte[] decodeBase64 = BASE64Util.decode(responseData);
        byte[] decodeBase64 = new Base64().decode(responseData);
        String strRS=new String(decodeBase64,"UTF-8");
        return strRS;
    }
    
    private String streamPost(String url, String param){
    	//HashMap类型的对象传输
    	HttpClient client=null;
    	 PostMethod postMethod = null;
	  try {
		  client=new HttpClient();
		  postMethod = new PostMethod(url);  
		  postMethod.setRequestBody(param);
		  client.executeMethod(postMethod);
		  String result =JRAlgorithmUtils.urlDecode(postMethod.getResponseBodyAsString(),"UTF-8");
		  return result;
	} catch (Exception e) {
		e.printStackTrace();
		return null;
	} finally{
		if(postMethod!=null){
			postMethod.releaseConnection();
		}
	}
    	 
    }
    
    
    public static void main(String[] args) throws Exception {
    	
    	JSONObject bankInfo = new JSONObject();
    	bankInfo.put("realName", "刘朗君");
    	bankInfo.put("idNumber", "421125198810235257");
    	bankInfo.put("cardPan", "6222021202014658935");
    	bankInfo.put("cardType", "D");
    	bankInfo.put("bankCode", "ICBC");
    	bankInfo.put("mobile", "18758265108");
    	
    	BankTest bankTestP=new BankTest();
    	
    	//测试获取token
    	String tokenTemp=bankTestP.getToken();
    	System.out.println("tokenTemp---"+tokenTemp);
    	
    	//bankTestP.getToken();
    	//SbdzQR2ja0fi2tYbk1UDC/99BVE7zIPkwL3T7knUf48=
    	//测试获得验证码
    	bankTestP.getMsg(tokenTemp, bankInfo);
    	//确认接口
    	bankTestP.testConfirm(tokenTemp, "825310", bankInfo);
    	//测试查询接口
//    	bankTestP.testQuery();
    	
    	
    	
		
	}

    
}
