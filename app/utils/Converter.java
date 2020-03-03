package utils;

import org.apache.commons.lang.StringUtils;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class Converter {
	
	public static void main(String[] args) {
//		String strjson = new XMLSerializer().readFromFile(new File("/Users/md005/Desktop/1.xml")).toString();
//		System.out.println(strjson);
//		
//		XMLSerializer xmlSerializer = new XMLSerializer();  
//        xmlSerializer.setTypeHintsEnabled(false); 
//        xmlSerializer.setRootName("pReq");
//        xmlSerializer.setElementName("pRow");
//        String strxml = xmlSerializer.write(JSONSerializer.toJSON(strjson));
//        System.out.println(strxml);
		
//		<pOriMerBillNo>string</pOriMerBillNo>
//        <pTrdAmt>string</pTrdAmt>
//        <pFAcctType>string</pFAcctType>
//        <pFIpsAcctNo>string</pFIpsAcctNo>
//        <pFTrdFee>string</pFTrdFee>
//        <pTAcctType>string</pTAcctType>
//        <pTIpsAcctNo>string</pTIpsAcctNo>
//<pTTrdFee>string</pTTrdFee>
		JSONObject json = (JSONObject) xmlToObj("<?xml version=\"1.0\" encoding=\"utf-8\"?><pReq><pStatus>9999</pStatus><pMerBillNo>1#1#1410252884476</pMerBillNo><pIdentNo>410621198406155011</pIdentNo><pRealName>环迅</pRealName><pIpsAcctNo></pIpsAcctNo><pIpsAcctDate></pIpsAcctDate><pMemo1>http://172.16.6.171:9000/IPSAction/IPSCallBack;http://172.16.6.171:9000/IPSAction/IPSCallBack;1;2;1;hx</pMemo1><pMemo2>pMemo2</pMemo2><pMemo3>pMemo3</pMemo3></pReq>");
		System.out.println(json);
		
		String xml = jsonToXml(json.toString(), "pReq", null, null, null);
		
		System.out.println(xml);
		
		System.out.println(xmlToObj(xml));
		
	}
	
	/**
	 * xml字符串转json字符串
	 * @param xml
	 * @return
	 */
	public static String xmlToJson(String xml){  
        return new XMLSerializer().read(xml).toString();  
    }
	
	/**
	 * json字符串转xml字符串
	 * @param json
	 * @return
	 */
    public static String jsonToXml(String json){  
        XMLSerializer xmlSerializer = new XMLSerializer();  
        xmlSerializer.setTypeHintsEnabled(false);      
        
        return xmlSerializer.write(JSONSerializer.toJSON(json));  
    }
    
	/**
	 * json字符串转xml字符串
	 * @param json
	 * @param rootName
	 * @param elementName
	 * @param objectName
	 * @param arrayName
	 * @return
	 */
    public static String jsonToXml(String json, String rootName, String elementName, String objectName, String arrayName){  
        XMLSerializer xmlSerializer = new XMLSerializer();  
        xmlSerializer.setTypeHintsEnabled(false);
        
        if (StringUtils.isNotBlank(rootName)) {
        	xmlSerializer.setRootName(rootName);
		}
        
        if (StringUtils.isNotBlank(elementName)) {
        	xmlSerializer.setElementName(elementName);
		}
        
        if (StringUtils.isNotBlank(objectName)) {
        	xmlSerializer.setObjectName(objectName);
        }
        
        if (StringUtils.isNotBlank(arrayName)) {
        	xmlSerializer.setArrayName(arrayName);
        }
        
        return xmlSerializer.write(JSONSerializer.toJSON(json));  
    }  
    
	/**
	 * xml字符串转json对象/数组
	 * @param xml
	 * @return
	 */
	public static JSON xmlToObj(String xml) {
		return new XMLSerializer().read(xml);  
	}
}
