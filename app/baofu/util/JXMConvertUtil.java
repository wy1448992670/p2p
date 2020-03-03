/**
 * 
 */
/**
 * @author Administrator
 *
 */
package baofu.util;
import java.util.HashMap;
import java.util.Iterator;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;


// 将json字符串转换成jsonObject  
// 遍历jsonObject数据，添加到Map对象  
public class JXMConvertUtil{	
	
	public static String XmlConvertJson(String XMLString){
		XMLSerializer xmlSerializer = new XMLSerializer();  
	    String xmltojson = xmlSerializer.read(XMLString).toString();  
		return xmltojson;
	}
	
	@SuppressWarnings("rawtypes")
	public static HashMap<String, String> JsonConvertHashMap(Object object)  
	   {  
	       HashMap<String, String> RMap = new HashMap<String, String>();  
	       // 将json字符串转换成jsonObject  
	       JSONObject jsonObject = JSONObject.fromObject(object);  
	       Iterator it = jsonObject.keys();  
	       // 遍历jsonObject数据，添加到Map对象  
	       while (it.hasNext()){  
	           String key = String.valueOf(it.next());  
	           String value = (String) jsonObject.get(key);  
	           RMap.put(key, value);  
	       }  
	       return RMap;
	   }  


}