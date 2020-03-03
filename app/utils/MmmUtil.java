package utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


/**
 * @author l'
 * @ 2015年12月24日
 */
public class MmmUtil {
	
	
	/**
	 * 组织httpClient参数
	 * 
	 * @param paramMap
	 * @return
	 */
	public static List<NameValuePair> putParams(Map<String, String> paramMap) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (Iterator<Entry<String, String>> iterator = paramMap.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, String> entry = iterator.next();
			params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		return params;
	}

	/**
	 * httpClient表单提交
	 * 
	 * @param url
	 * @param params
	 * @param urlEncoded
	 * @return
	 * @throws Exception
	 */
	public static String postMethod(String url, Map<String, String> params, String urlEncoded) throws Exception {
		// 待请求参数数组
		return byPostMethodToHttpEntity(url, putParams(params), urlEncoded);
	}

	/**
	 * Post方式提交并且返回Entity字符串
	 * 
	 * @param url 提交的url
	 * @param client  HttpClient
	 * @param post HttpPost
	 * @param params 队列参数
	 * @param urlEncoded url编码
	 * @return
	 */
	public static String byPostMethodToHttpEntity(String url, List<NameValuePair> params, String urlEncoded) {

		HttpEntity entity = null;
		StringBuffer buff = new StringBuffer();
		// 创建线程安全的httpClient
		HttpClient httpClient = new DefaultHttpClient(
				new ThreadSafeClientConnManager());
		HttpPost httpPost = new HttpPost(url);
		try {
			if (params != null) {
				UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(params, urlEncoded);
				httpPost.setEntity(uefEntity);
			}
			HttpResponse response = httpClient.execute(httpPost);
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				entity = response.getEntity();
				buff.append(EntityUtils.toString(entity));
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			releaseSource(null, httpPost, httpClient);
		}
		return buff.toString();
	}

	/**
	 * 释放资源
	 * 
	 * @param httpGet
	 * @param httpPost
	 * @param httpClient
	 */
	public static void releaseSource(HttpGet httpGet, HttpPost httpPost,HttpClient httpClient) {
		if (httpGet != null) {
			httpGet.abort();
		}
		if (httpPost != null) {
			httpPost.abort();
		}
		if (httpClient != null) {
			httpClient.getConnectionManager().shutdown();
		}
	}

}
