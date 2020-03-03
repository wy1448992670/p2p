package utils;

import java.io.Serializable;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Sheet implements Serializable{
	
	
	
	public String getTableCode(List<String> columnNames,List<String> fields,List<Object> data,String tableStyle,
			String thStyle,String trStyle,String tdStyle){
		
		StringBuffer code = new StringBuffer();
		
		code.append("<table " + tableStyle +"> <tr "+ trStyle +"> ");
		
		for(int i = 0;i < columnNames.size(); i++){
			code.append("<th "+ thStyle  +">" + columnNames.get(i) + "</th>");
		}
		code.append("</tr>");
		
		JSONArray arr = JSONArray.fromObject(data);
		
		for(int i = 0;i < arr.size(); i++){
			code.append("<tr>");
			code.append("<td "+tdStyle +">"+ (i+1) + "</td>");
			JSONObject obj = (JSONObject)arr.get(i);
			
			for(int j = 0;j < fields.size(); j++){
				code.append("<td "+ tdStyle+">"+ obj.get(fields.get(j)) +"</td>");
			}
			code.append("</tr>");
		}
		
		code.append("</table>");
		return code.toString();
	}
}
