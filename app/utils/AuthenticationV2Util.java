package utils;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import play.Logger;
import cn.id5.gboss.businesses.validator.service.app.QueryValidatorServices;
import cn.id5.gboss.businesses.validator.service.app.QueryValidatorServicesProxy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @description.  国政通四要素验证
 *  
 * @modificationHistory.  
 * @author liulj 2016年9月11日上午9:36:06 TODO
 */
public class AuthenticationV2Util {
	
    private static final String URL = "http://gboss.id5.cn/services/QueryValidatorServices?wsdl"; //地址
    private static final String USERNAME = "zyytzservice"; //用户名
    private static final String PASSWORD = "zyytzservice_3$BPEm9P"; //密码
    
    private static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";
    
    private static final String DATASOURCE_ID = "1A020201"; //数据类型：姓名，省份证
    private static final String DATASOURCE_ID_BANK = "3Y010101";//数据类型：姓名，银行卡号，身份证号，手机号码
    
    
    public static JSONObject requestForIdNo(String username, String idNo) {
    	
    	JSONObject result = null;
        //得到子报告结果
        try {
			result = querySingle(String.format("%s,%s", username, idNo), DATASOURCE_ID);
		} catch (Exception e) {
			Logger.error("认证个人身份信息时异常，%s", e.getMessage());
		}
        //Logger.info("个人姓名身份证认证响应：" + res);
    	return result;
    }
    
    public static JSONObject requestForBankInfo(String username, String idNo, String bankAccount, String mobile) {
    	
    	JSONObject result = null;
        //得到子报告结果
        try {
        	result = querySingle(String.format("%s,%s,%s", username, bankAccount, idNo, mobile), DATASOURCE_ID_BANK);
		} catch (Exception e) {
			Logger.error("认证银行卡信息时异常，%s", e.getMessage());
		}
        //Logger.info("个人姓名身份证认证响应：" + res);
    	return result;
    }
    
	public static String encode(String data) throws Exception {
		String key = "12345678";
		DESKeySpec dks = new DESKeySpec(key.getBytes());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		// key的长度不能够小于8位字节
		Key secretKey = keyFactory.generateSecret(dks);
		Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
		IvParameterSpec iv = new IvParameterSpec("12345678".getBytes());// 向量
		AlgorithmParameterSpec paramSpec = iv;
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
		byte[] bytes = cipher.doFinal(data.getBytes("GB2312"));
		return new Base64().encode(bytes);
		//return com.sun.org.apache.xml.internal.security.utils.Base64.encode(bytes);
	}
	
	public static String decode(byte[] data) throws Exception {
		try {
			//data = com.sun.org.apache.xml.internal.security.utils.Base64.decode(data);
			String key = "12345678";
			data = new Base64().decode(new String(data)); 
			DESKeySpec dks = new DESKeySpec(key.getBytes());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			// key 的长度不能够小于 8 位字节
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
			IvParameterSpec iv = new IvParameterSpec("12345678".getBytes());
			AlgorithmParameterSpec paramSpec = iv;
			cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
			return new String(cipher.doFinal(data), "GB2312");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	}
	
	public static JSONObject querySingle(String param, String datasource) throws Exception{
		// 姓名，银行卡号，身份证号，手机号码 
		QueryValidatorServicesProxy proxy = new QueryValidatorServicesProxy(); 
		proxy.setEndpoint(URL); 
		QueryValidatorServices service =  proxy.getQueryValidatorServices(); 
		System.setProperty("javax.net.ssl.trustStore", "CheckID.keystore");
		String resultXML = service.querySingle(encode(USERNAME), encode(PASSWORD), encode(datasource), encode(param));
		resultXML = decode(resultXML.getBytes());
		System.out.println(resultXML);
		return extractMultiXMLResult(resultXML);
	}
	
	public static void main(String[] args) throws Exception {
		//System.out.println(new String(encode("中国人")) +"----qMeKyoDWvsE=");
		//System.out.println(new String(decode("qMeKyoDWvsE=".getBytes())));
	   // requestForIdNo("张君卿", "370826198907200068", new ErrorInfo());
	    requestForBankInfo("徐万玲", "210522198708045644", "6212261001019561759", "");
		
		/*QueryValidatorServicesProxy proxy = new QueryValidatorServicesProxy(); 
		proxy.setEndpoint("http://gboss.id5.cn/services/QueryValidatorServices?wsdl"); 
		QueryValidatorServices service =  proxy.getQueryValidatorServices(); 
		String userName = "username";//用户名 
		String password = "password";//密码
		System.setProperty("javax.net.ssl.trustStore", "CheckID.keystore");
		String resultXML = ""; String datasource = "1A020201";//数据类型
		//单条 
		String param = "刘丽萍,210204196501015829";
		//输入参数 
		resultXML = service.querySingle(userName, password, datasource, param); 
		//批量
		String params = " 王茜,150202198302101248; 吴晨晨,36252519821201061x; 王 鹏,110108197412255477"; 
		resultXML = service.queryBatch(userName, password, datasource, params);*/
	    
	   /* String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+	"<data>"
					+	  "<message>"
					+	    "<status>-9917</status>"
					+	    "<value>您无权查询数据（未绑定IP）</value>"
					+	  "</message>"
					+	  "<acctnoInfos>"
					+	    "<acctnoInfo inputXm=\"姓名\" inputAcctno=\"bank\" inputZjhm=\"身份证\" inputTelephone=\"mobile\">"
					+	       	"<wybs desc=\"唯一标识\">4028894156e5645701571c633a5b2766</wybs>"
					+	       	"<code desc=\"调用结果\">-999</code>"
					+	  		"<message desc=\"调用结果描述\">数据格式错误或其它错误</message>"
					+	  	"</acctnoInfo>"
					+	  "</acctnoInfos>"
					+	"</data>";
	    System.out.println(extractMultiXMLResult(xml));*/
	}
	
	public static JSONObject extractMultiXMLResult(String xml) throws Exception{
		JSONObject result = new JSONObject();
		
	    Document document = DocumentHelper.parseText(xml);
	    Element root = document.getRootElement();
	   	    
	    JSONObject message = new JSONObject();
	    message.put("status", root.element("message").element("status").getTextTrim());
	    message.put("value", root.element("message").element("value").getTextTrim());
	    result.put("message", message);
	    
	    if(root.element("acctnoInfos") != null){
	    	JSONObject acctnoInfo = new JSONObject();
	    	Element acctnoInfoElement = root.element("acctnoInfos").element("acctnoInfo");
	    	acctnoInfo.put("wybs", acctnoInfoElement.element("wybs").getTextTrim());
	    	acctnoInfo.put("code", acctnoInfoElement.element("code").getTextTrim());
	    	acctnoInfo.put("message", acctnoInfoElement.element("message").getTextTrim());
	    	result.put("acctnoInfo", acctnoInfo);
	    }
	    
	    if(root.element("policeCheckInfos") != null){
	    	JSONObject policeCheckInfo = new JSONObject();
	    	Element policeCheckInfoElement = root.element("policeCheckInfos").element("policeCheckInfo");
	    	policeCheckInfo.put("compStatus", policeCheckInfoElement.element("compStatus").getTextTrim());
	    	policeCheckInfo.put("compResult", policeCheckInfoElement.element("compResult").getTextTrim());
	    	result.put("policeCheckInfo", policeCheckInfo);
	    }
	    
	    System.out.println(JSON.toJSONString(result, true));
	    return result;
	  }
}
