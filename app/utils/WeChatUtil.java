package utils;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;

import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import net.sf.json.JSONObject;

import play.Logger;
import play.cache.Cache;
import play.libs.Encrypt;
import play.mvc.Scope.Session;
import services.WeChatX509TrustManagerService;

import constants.Constants;

/**
 * 微信工具类
 * @author fefrg
 *
 */
public class WeChatUtil {
	
	/**
	 * 发起https请求并获取结果
	 * 
	 * @param requestUrl 请求地址
	 * @param requestMethod 请求方式（GET、POST）
	 * @param outputStr 提交的数据
	 * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
	 */
	public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) {
		JSONObject jsonObject = null;
		StringBuffer buffer = new StringBuffer();
		try {
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = { new WeChatX509TrustManagerService() };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();

			URL url = new URL(requestUrl);
			HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
			httpUrlConn.setSSLSocketFactory(ssf);

			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod(requestMethod);

			if ("GET".equalsIgnoreCase(requestMethod))
				httpUrlConn.connect();

			// 当有数据需要提交时
			if (null != outputStr) {
				OutputStream outputStream = httpUrlConn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}

			// 将返回的输入流转换成字符串
			InputStream inputStream = httpUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			bufferedReader.close();
			inputStreamReader.close();
			// 释放资源
			inputStream.close();
			inputStream = null;
			httpUrlConn.disconnect();
			jsonObject = JSONObject.fromObject(buffer.toString());
		} catch (ConnectException ce) {
			Logger.error("Weixin server connection timed out.");
		} catch (Exception e) {
			Logger.error("https request error:{}", e);
		}
		return jsonObject;
	}
	
	public static String urlEncodeUTF8(String source) {
		String result = source;
		try {
			result = URLEncoder.encode(source,"utf-8");
		} catch (UnsupportedEncodingException e) {
			Logger.error(e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * 对openId进行加密
	 * @param openId
	 * @return
	 */
	public static String encryptOpenId(String openId) {
		long time = System.currentTimeMillis();
		//openId有效期为1个小时
		Cache.set("wechat_time" + Session.current().getId(), time, "60mn");
		String openId3DES = Constants.TOKEN + "," + time + "," + openId;
		openId3DES = Encrypt.encrypt3DES(openId3DES, Constants.ENCRYPTION_KEY);
		return openId3DES;
	}
	
	/**
	 * 对openId进行解密
	 */
	public static String decrypt3DESOpenId(String open3DESId) {
		Object time = Cache.get("wechat_time" + Session.current().getId());
		if(null == time) {
			return null;
			
		}
		
		String[] strs = Encrypt.decrypt3DES(open3DESId, Constants.ENCRYPTION_KEY).split(",");
		String openId = strs[2];
		
		return openId;
	}
	
	public static int getByteSize(String content) {  
	    int size = 0;  
	    if (null != content) {  
	        try {  
	            // 汉字采用utf-8编码时占3个字节  
	            size = content.getBytes("utf-8").length;  
	        } catch (UnsupportedEncodingException e) {  
	            Logger.error(e.getMessage());
	        }  
	    }  
	    return size;  
	}  
	
	public static long getCurrentTime() {
		
		return System.currentTimeMillis();
	}
}
