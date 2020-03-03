package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.xfire.client.Client;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.shove.Xml;

import play.Logger;

public class AuthenticationUtil {
	
	private static final String TEST_URL = "http://www.pycredit.com:9001/services/WebServiceSingleQuery?wsdl";   //测试地址
	private static final String FORMAL_URL = "http://www.pycredit.com:8001/services/WebServiceSingleQuery?wsdl";   //正式地址
    private static final String USERNAME = "zyywsquery01"; //测试地址用户名zyywsquery
    private static final String PASSWORD = "4JurNE0oXw514U9PWI5SCQ=="; //测试地址密码wRK7pYbwzfz373OhBpgiGQ==
    
    public static String requestForIdNo(String username, String idNo, ErrorInfo error) {
    	
    	Object[] results = {};
        //得到子报告结果
        try {
        	Client client = new Client(new URL(FORMAL_URL));
        	String request = generateXMLforIdNo(username, idNo);
        	Logger.info("个人姓名身份证认证请求：" + request);
			results = client.invoke("queryReport",new Object[]{USERNAME, PASSWORD ,request,"xml"});
		} catch (Exception e) {
			Logger.error("认证个人身份信息时异常，%s", e.getMessage());
			error.code = -1;
			error.msg = "认证个人身份信息时异常" ;
		}
        String res = null;
		try {
			res = (String)results[0];
		} catch (Exception e) {
		}
        //Logger.info("个人姓名身份证认证响应：" + res);
    	return res;
    }
    
    public static String requestForBankInfo(String username, String idNo, String bankAccount, String mobile, ErrorInfo error) {
    	
    	Object[] results = {};
        //得到子报告结果
        try {
        	Client client = new Client(new URL(FORMAL_URL));
        	String request = generateXMLforBankInfo(username, idNo, bankAccount, mobile);
        	Logger.info("银行卡认证请求：" + request);
			results = client.invoke("queryReport",new Object[]{USERNAME, PASSWORD , request,"xml"});
		} catch (Exception e) {
			Logger.error("认证银行卡信息时异常，%s", e.getMessage());
			error.code = -1;
			error.msg = "认证银行卡信息时异常" ;
		}
        String res = null;
		try {
			res = (String)results[0];
		} catch (Exception e) {
		}
        Logger.info("个人姓名身份证认证响应：" + res);
    	return res;
    }

	public static Map<String, Object> extractMultiXMLResult(String xml, int type){
		//String sectionNodeName = (type == 0) ? "policeCheck2Info" : "personBankCheckInfo";
		String sectionNodeName = (type == 0) ? "policeCheckInfo" : "personBankCheckInfo";
	    Document document = null;
	    try{
	      document = DocumentHelper.parseText(xml);
	    }
	    catch (DocumentException ex){
	      return null;
	    }
	    Map<String, Object> result = new HashMap<String, Object>();
	    Element root = document.getRootElement();
	    Element element = root.element("cisReport").element(sectionNodeName);
	    String treatResult = element.attributeValue("treatResult");
	    result.put("treatResult", treatResult);
	    if("1".equals(treatResult)){
	    	element = element.element("item");
	    	Iterator<?> it = element.elementIterator();
	    	while(it.hasNext()) {
	    		Element ele = (Element)it.next();
	    		if(!"condition".equals(ele.getName())){
	    			result.put(ele.getName(), ele.getText());
	    		}else{
	    			Iterator bankInfo = ele.elementIterator();
	    			while(bankInfo.hasNext()){
	    				Element info = (Element)bankInfo.next();
	    				if("accountBankName".equals(info.getName())){
	    					result.put(info.getName(), info.getText());
	    				}
	    			}
	    		}
	    	}
	    }
	    return result;
	  } 
	
	public static void main(String[] args) {
		System.out.println(requestForIdNo("蓝燕", "360730198601270626", new ErrorInfo()));
		//System.out.println(AuthenticationV2Util.requestForIdNo("蓝燕", "3607301986012706268"));
		/*Object [] results = {};
		try {
			Client client = new Client(new URL("http://www.pycredit.com:8001/services/WebServiceSingleQuery?wsdl"));
			
			String requestXml = "<?xml version=\"1.0\" encoding=\"GBK\"?><conditions><condition queryType=\"25160\"><item><name>subreportIDs</name><value>10604</value></item><item><name>refID</name><value></value></item><item><name>name</name><value>储兵兵</value></item><item><name>queryReasonID</name><value>101</value></item><item><name>documentNo</name><value>421302199209177675</value></item></condition></conditions>";
            //得到子报告结果
            results = client.invoke("queryReport",new Object[]{"zyywsquery01","4JurNE0oXw514U9PWI5SCQ==",requestXml,"xml"});
            System.out.println((String)results[0]);
            Map<String, Object> map = Xml.extractSimpleXMLResultMap((String)results[0]);
            String res = (String) map.get("returnValue");
            System.out.println(CompressStringUtil.decompress(new Base64().decode(res)));
            Map<String, Object> tt = AuthenticationUtil.extractMultiXMLResult(CompressStringUtil.decompress(new Base64().decode(res)), 0);
            byte[] photo = new Base64().decode((String)tt.get("photo"));
            File file = new File("d:\\chenqiao.jpg");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(photo);
            fos.close();
		} catch (Exception e) {
			// TODO: handle exception
		}*/
		//String xx = "UEsDBBQACAAIAK6NukgAAAAAAAAAAAAAAAALAAAAcmVwb3J0cy54bWx9U09vEkEcPWPid5jMqU2KzCwsSrPQRKymMdYE6gdYdgfYdJnBmVmUfpuS3jwYayJqjSY1aLUxUBWaGP8cPBhjvHoxMc7ObsECkQP7m7ez773f+81YK7cbPmgRLjxG8xCfQxAQ6jDXo7U8vHLxKgQrhbNnLMcTJdJkXAoAKrZcZ3loIJxFppHFJlK/rAlBQD25bjdIHo4Gbz+8f/HmwfHrd4P+50d3Dp9CIILKdV6zlcrRoNvZfQbBzYDw9g1B+NqlPNxqt28JjcQviiygUlmCgBOHeC2y4YXUJ7IAm8tmbjltwFP+1ObwETLOGqwEnu+uUnfClERmcsKVjqVLxBaMhhwYYe08It9oN4kIwSzKLOGMgVRakhNblogIfGUWLaHp7eW6qop2U0YBb3de/ex+OuiPdhY0zSIwtjsPfx0cdr/1h3tfjz8Oev3OguZeBGHr1dCGY6PceRM7yYrjZiCo26LcFpI0VjlnPA8lDwgEnrjM2RZRKlXbFyTMJWHFUVLXCw2IEEtYnvpUVwmLqnkVwj8rpcsIdSK/hXt7u9+P7g6/DIbPf1upEzTa07L9gBTu77/sWamo1typMfmMjMucoEGoOj3/FRvtPPnzuLf/Y75gxsBpZOBczkAXEMaGiefLW6nZ1q0m8z2HFOvE2TTWaJWdnlU02fTUBItMyHjmU9NOx2s9hSJz9Zky1AUK19eIEHZNQTTw/WgWk2R091F04yCsSTxzm/zntd7OtYuClYoLDTbrTDKFRU8dQ6yqsKnmdSDq5jNa8sRmWdpybiTRMZ8XyZwLMNM9jKRnZDQ8vrWnV6LwF1BLBwipzJm4ZgIAAJcEAABQSwECFAAUAAgACACujbpIqcyZuGYCAACXBAAACwAAAAAAAAAAAAAAAAAAAAAAcmVwb3J0cy54bWxQSwUGAAAAAAEAAQA5AAAAnwIAAAAA";
		//System.out.println(CompressStringUtil.decompress(xx.getBytes()));
	}
	
	/**
	 * 生成请求XML字符串
	 */
	private static String generateReqXMLStr(Map<String, Object> by_param_kvs, String queryType) {
		StringBuffer req_xml = new StringBuffer();
		req_xml.append("<?xml version=\"1.0\" encoding=\"GBK\"?>");
		req_xml.append("<conditions>");
		req_xml.append("<condition queryType=\"").append(queryType).append("\">");
		req_xml.append(generateReqXMLNodeStr(by_param_kvs));
		req_xml.append("</condition>");
		req_xml.append("</conditions>");
		//Logger.info("信息认证:"+req_xml.toString());
		return req_xml.toString();
	}
	
	/**
	 * 生成请求XML节点字符串
	 */
	private static String generateReqXMLNodeStr(Map<String, Object> by_param_kvs) {
		StringBuffer req_xml = new StringBuffer();
		for (Entry<String, Object> entry : by_param_kvs.entrySet()) {
			req_xml.append("<item>");
			String key = entry.getKey();
			Object val = entry.getValue();
			req_xml.append("<name>").append(key).append("</name>");
			req_xml.append("<value>").append(val).append("</value>");
			req_xml.append("</item>");
		}
		return req_xml.toString();
	}
	
	public static String generateXMLforIdNo(String username, String idNo) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("name", username);
		map.put("documentNo", idNo);
		map.put("subreportIDs", "10602");
		map.put("queryReasonID", "101");
		map.put("refID", "");
		return generateReqXMLStr(map, "25160");
	}
	
	public static String generateXMLforBankInfo(String username, String idNo, String bankAccount, String mobile) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("name", username);
		map.put("documentNo", idNo);
		map.put("accountNo", bankAccount);
		map.put("openBankNo", "");
		map.put("mobile", mobile);
		map.put("subreportIDs", "14506");
		map.put("queryReasonID", "101");
		map.put("refID", "");
		return generateReqXMLStr(map, "25173");
	}
}
