package utils.pay.bank.domain;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import utils.Base64;
import utils.pay.bank.util.FastJsonUtils;
import utils.pay.bank.util.SHA256Util;

public abstract class AbstractBase{

	protected static final String charset = "UTF-8";
	protected static final String version = "1.0";

	protected static String getSignature(String version, String charset,
			String data, String privateKey) throws Exception {
		String localSign = new String(
				SHA256Util.encrypt(version + charset + data + privateKey));
		return localSign;
	}

	protected static String makeEncodeRequestJson(String requestJson,
			String des3key) throws Exception {
		//String reqData = BASE64Util.encode(requestJson.getBytes("UTF-8"));
		String reqData = new Base64().encode(requestJson.getBytes("UTF-8"));
		return reqData;
	}

	protected static String decodeResponseJson(String responseJson)
			throws Exception {
		Map<String, String> map = FastJsonUtils.toBean(responseJson,
				HashMap.class);
		String responseData = map.get("data");
		//byte[] decodeBase64 = BASE64Util.decode(responseData);
		byte[] decodeBase64 = new Base64().decode(responseData);
		String strRS = new String(decodeBase64, "UTF-8");
		return strRS;
	}

	protected static String streamPost(String url, String param) {
		// HashMap类型的对象传输
		HttpClient client = null;
		PostMethod postMethod = null;
		try {
			client = new HttpClient();
			postMethod = new PostMethod(url);
			postMethod.setRequestBody(param);
			client.executeMethod(postMethod);
			String result = URLDecoder.decode(postMethod.getResponseBodyAsString(), "UTF-8");
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (postMethod != null) {
				postMethod.releaseConnection();
			}
		}

	}
}
