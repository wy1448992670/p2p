package utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.SignatureException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import constants.Constants;
import play.Logger;
import play.mvc.Http.Header;
import play.mvc.Http.Request;

public class GopayUtils {
	
	public static String input_charset = "GBK";
	
	/**
	 * 获取国付宝服务器时间 用于时间戳
	 * @return 格式YYYYMMDDHHMMSS
	 */
	public static String getGopayServerTime() {
		HttpClient httpClient = new HttpClient();
		httpClient.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
		httpClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, 10000); 
		GetMethod getMethod = new GetMethod(Constants.GO_SERVER_TIME_URL);
		getMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"GBK");  
		// 执行getMethod
		int statusCode = 0;
		try {
			statusCode = httpClient.executeMethod(getMethod);			
			if (statusCode == HttpStatus.SC_OK){
				String respString = StringUtils.trim((new String(getMethod.getResponseBody(),"GBK")));
				return respString;
			}			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			getMethod.releaseConnection();
		}
		return null;
	}
	
	/**
     * Convenience method to get the IP Address from client.
     * 
     * @param request the current request
     * @return IP to application
     */
    public static String getIpAddr(Request request) { 
    	if (request == null) return "";
    	
    	Header header = request.headers.get("X-Forwarded-For");
        String ip =  header == null ? null : header.value(); 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
//            ip = request.getHeader("Proxy-Client-IP");
        	header = request.headers.get("Proxy-Client-IP");
        	ip =  header == null ? null : header.value();
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
//            ip = request.getHeader("WL-Proxy-Client-IP");
        	header = request.headers.get("Proxy-Client-IP");
        	ip =  header == null ? null : header.value();
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
//            ip = request.getHeader("HTTP_CLIENT_IP"); 
        	header = request.headers.get("Proxy-Client-IP");
        	ip =  header == null ? null : header.value();
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
//            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        	header = request.headers.get("Proxy-Client-IP");
        	ip =  header == null ? null : header.value();
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.remoteAddress;
        } 
        return ip; 
    } 
    
    /**
     * 对字符串进行MD5签名
     * 
     * @param text
     *            明文
     * 
     * @return 密文
     */
    public static String md5(String text) {
        return DigestUtils.md5Hex(getContentBytes(text, input_charset));
    }
    
    /**
     * 对字符串进行SHA签名
     * 
     * @param text
     *            明文
     * 
     * @return 密文
     */
    public static String sha(String text) {
        return DigestUtils.shaHex(getContentBytes(text, input_charset));
    }

    /**
     * @param content
     * @param charset
     * @return
     * @throws SignatureException
     * @throws UnsupportedEncodingException 
     */
    private static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }

        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }
    
    public static boolean validateSign(String version, String tranCode, String merchantID, String merOrderNum,
    		String tranAmt, String feeAmt, String tranDateTime, String frontMerUrl, String backgroundMerUrl,
    		String orderId, String gopayOutOrderId, String tranIP, String respCode,String VerficationCode, String signValue) {
    	StringBuffer plain = new StringBuffer();
    	plain.append("version=[");
    	plain.append(version);
    	plain.append("]tranCode=[");
    	plain.append(tranCode);
    	plain.append("]merchantID=[");
    	plain.append(merchantID);
    	plain.append("]merOrderNum=[");
    	plain.append(merOrderNum);
    	plain.append("]tranAmt=[");
    	plain.append(tranAmt);
    	plain.append("]feeAmt=[]");
    	//plain.append(feeAmt);
    	plain.append("tranDateTime=[");
    	plain.append(tranDateTime);
    	plain.append("]frontMerUrl=[");
    	plain.append(frontMerUrl);
    	plain.append("]backgroundMerUrl=[");
    	plain.append(backgroundMerUrl);
    	plain.append("]orderId=[");
    	plain.append(orderId);
    	plain.append("]gopayOutOrderId=[");
    	plain.append(gopayOutOrderId);
    	plain.append("]tranIP=[");
    	plain.append(tranIP);
    	plain.append("]respCode=[");
    	//plain.append(respCode);
    	plain.append("]gopayServerTime=[]VerficationCode=[");
    	plain.append(VerficationCode);
    	plain.append("]");
    	String sign = GopayUtils.md5(plain.toString());
    	
    	Logger.info("正在校验.....");
    	
    	return sign.equals(signValue);
    }
    
    public static boolean validateQuerySign(String tranCode,String merchantID,String merOrderNum,String tranAmt,String feeAmt
			,String currencyType,String merURL,String tranDateTime,String customerEMail,String virCardNo,String virCardNoIn
			,String tranIP,String msgExt,String respCode,String orgtranDateTime,String orgOrderNum
			,String orgtranAmt,String orgTxnType,String orgTxnStat,String authID,String isLocked,String VerficationCode, String signValue) {
    	StringBuffer plain = new StringBuffer();
    	plain.append("tranCode=[");
    	plain.append(tranCode);
    	plain.append("]merchantID=[");
    	plain.append(merchantID);
    	plain.append("]merOrderNum=[");
    	plain.append(merOrderNum);
    	plain.append("]tranAmt=[");
    	plain.append(tranAmt);
    	plain.append("]ticketAmt=[]");
    	plain.append("tranDateTime=[");
    	plain.append(tranDateTime);
    	plain.append("]currencyType=[");
    	plain.append(currencyType);
    	plain.append("]merURL=[");
    	plain.append(merURL);
    	plain.append("]customerEMail=[");
    	plain.append("]authID=[]orgOrderNum=[");
    	plain.append("]orgtranDateTime=[");
    	plain.append("]orgtranAmt=[]orgTxnType=[");
    	plain.append("]orgTxnStat=[]msgExt=[");
    	plain.append("]virCardNo=[]virCardNoIn=[");
    	plain.append(virCardNoIn);
    	plain.append("]tranIP=[");
    	plain.append(tranIP);
    	plain.append("]isLocked=[");
    	plain.append("]feeAmt=[0]respCode=[");
    	plain.append("]VerficationCode=[");
    	plain.append(VerficationCode);
    	plain.append("]");
    	String sign = GopayUtils.md5(plain.toString());
    	
    	Logger.info("正在校验.....");
    	
    	return sign.equals(signValue);
    }
}
