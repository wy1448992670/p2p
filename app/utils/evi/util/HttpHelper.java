package utils.evi.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/***
 * 
 * @Description: HTTP辅助类
 * @Team: 公有云技术支持小组
 * @Author: 天云小生
 * @Date: 2018年01月14日
 */
public class HttpHelper {
	private static Logger LOG = LoggerFactory.getLogger(HttpHelper.class);

	/***
	 * POST请求的头部信息
	 * 
	 * @param projectId
	 * @param signature
	 * @param algorithm
	 * @param ContentType
	 * @param encoding
	 * @return
	 */
	public static LinkedHashMap<String, String> getPOSTHeaders(String projectId, String signature, String algorithm,
			String ContentType, String encoding) {
		LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>();
		headers.put("X-timevale-project-id", projectId);
		headers.put("X-timevale-signature", signature);
		headers.put("X-signature-algorithm", algorithm);
		headers.put("X-timevale-mode", "package");
		headers.put("Content-Type", ContentType);
		headers.put("Charset", encoding);
		return headers;
	}

	/***
	 * PUT请求的头部信息
	 * 
	 * @param filePath
	 * @param ContentMD5
	 * @param ContentType
	 * @param encoding
	 * @return
	 */
	public static LinkedHashMap<String, String> getPUTHeaders(String ContentMD5, String ContentType, String encoding) {
		LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>();
		headers.put("Content-MD5", ContentMD5);
		headers.put("Content-Type", ContentType);
		headers.put("Charset", encoding);
		return headers;
	}

	/***
	 * 向指定URL发送POST方法的请求
	 * 
	 * @param apiUrl
	 * @param data
	 * @param projectId
	 * @param signature
	 * @param encoding
	 * @return
	 */
	public static JSONObject sendPOST(String apiUrl, String data, LinkedHashMap<String, String> headers,
			String encoding) {
		StringBuffer strBuffer = null;
		JSONObject obj = null;
		try {
			// 建立连接
			URL url = new URL(apiUrl);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			// 需要输出
			httpURLConnection.setDoOutput(true);
			// 需要输入
			httpURLConnection.setDoInput(true);
			// 不允许缓存
			httpURLConnection.setUseCaches(false);

			httpURLConnection.setRequestMethod("POST");
			// 设置Headers
			if (null != headers) {
				for (String key : headers.keySet()) {
					httpURLConnection.setRequestProperty(key, headers.get(key));
				}
			}

			// 连接会话
			httpURLConnection.connect();
			// 建立输入流，向指向的URL传入参数
			DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
			// 设置请求参数
			dos.write(data.getBytes(encoding));
			dos.flush();
			dos.close();
			// 获得响应状态
			int http_StatusCode = httpURLConnection.getResponseCode();
			String http_ResponseMessage = httpURLConnection.getResponseMessage();
			obj = new JSONObject();
			if (HttpURLConnection.HTTP_OK == http_StatusCode) {
				strBuffer = new StringBuffer();
				String readLine = new String();
				BufferedReader responseReader = new BufferedReader(
						new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
				while ((readLine = responseReader.readLine()) != null) {
					strBuffer.append(readLine);
				}
				responseReader.close();
				LOG.info("http_StatusCode = " + http_StatusCode + " request_Parameter = " + data);
				obj = JSONHelper.toJSONObject(strBuffer.toString());
			} else {
				obj.put("errCode", http_StatusCode);
				obj.put("msg", "请求失败！失败原因：" + http_ResponseMessage);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return obj;
	}

	/***
	 * 向指定URL发送PUT方法的请求
	 * 
	 * @return
	 */
	public static JSONObject sendPUT(String evId, String fileUploadUrl, String filePath,
			LinkedHashMap<String, String> headers) {
		StringBuffer strBuffer = null;
		JSONObject obj = null;
		try {
			// 建立连接
			URL url = new URL(fileUploadUrl);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			// 需要输出
			httpURLConnection.setDoOutput(true);
			// 需要输入
			httpURLConnection.setDoInput(true);
			// 不允许缓存
			httpURLConnection.setUseCaches(false);

			httpURLConnection.setRequestMethod("PUT");
			// 设置Headers
			if (null != headers) {
				for (String key : headers.keySet()) {
					httpURLConnection.setRequestProperty(key, headers.get(key));
				}
			}
			// 连接会话
			httpURLConnection.connect();
			// 建立输入流，向指向的URL传入参数
			DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
			// 设置请求参数
			dos.write(FileHelper.getBytes(filePath));
			dos.flush();
			dos.close();
			// 获得响应状态
			int http_StatusCode = httpURLConnection.getResponseCode();
			String http_ResponseMessage = httpURLConnection.getResponseMessage();
			obj = new JSONObject();
			if (HttpURLConnection.HTTP_OK == http_StatusCode) {
				strBuffer = new StringBuffer();
				String readLine = new String();
				BufferedReader responseReader = new BufferedReader(
						new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
				while ((readLine = responseReader.readLine()) != null) {
					strBuffer.append(readLine);
				}
				responseReader.close();
				LOG.info("存证编号 = " + evId + " 上传成功！http_StatusCode = " + http_StatusCode);
				obj.put("errCode", http_StatusCode);
				obj.put("msg", "存证编号 = " + evId + " 的待保全文件上传成功！");
			} else {
				obj.put("errCode", http_StatusCode);
				obj.put("msg", "存证编号 = " + evId + " 的待保全文件上传失败！失败原因：" + http_ResponseMessage);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return obj;
	}
}
