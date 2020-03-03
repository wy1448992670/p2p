package utils;

import java.io.IOException;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JSONUtils {

	/** 
	* @Description: 将对象以json格式输出
	* @Author Yang Cheng
	* @Date: Feb 9, 2012 1:53:58 AM  
	* @param obj
	* @throws Exception  
	* @return void    
	 * @throws IOException 
	*/ 
	public static String printObject(Object obj) throws IOException {
		JSONObject jsObject =JSONObject.fromObject(obj);
		return jsObject.toString();
	}
	
	 /***
     * 将List对象序列化为JSON文本
     */
    public static <T> String toJSONString(List<T> list)
    {
        JSONArray jsonArray = JSONArray.fromObject(list);

        return jsonArray.toString();
    }
}
