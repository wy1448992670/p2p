package utils.tsign.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlgorithmHelper {
	private static Logger LOG = LoggerFactory.getLogger(AlgorithmHelper.class);

	/***
	 * 获取HMAC加密后的X-timevale-signature签名信息
	 * 
	 * @param data
	 *            加密前数据
	 * @param key
	 *            密钥
	 * @param algorithm
	 *            HmacMD5 HmacSHA1 HmacSHA256 HmacSHA384 HmacSHA512
	 * @param encoding
	 *            编码格式
	 * @return HMAC加密后16进制字符串
	 * @throws Exception
	 */
	public static String getXtimevaleSignature(String data, String key, String algorithm, String encoding) {
		Mac mac = null;
		try {
			mac = Mac.getInstance(algorithm);
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(encoding), algorithm);
			mac.init(secretKey);
			mac.update(data.getBytes(encoding));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			LOG.info("获取Signature签名信息异常：" + e.getMessage());
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			LOG.info("获取Signature签名信息异常：" + e.getMessage());
			return null;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			LOG.info("获取Signature签名信息异常：" + e.getMessage());
			return null;
		}
		return byte2hex(mac.doFinal());
	}

	/***
	 * 获取文件字节流的Base64加密字符串
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static String getFileByteString(String filePath) {
		Base64 b64 = new Base64();
		byte[] buffer = FileHelper.getBytes(filePath);
		return b64.encodeToString(buffer);
	}

	/***
	 * 将Base64字符串解密并保存为文件
	 * 
	 * @param encodeStr
	 * @param targetFilePath
	 * @throws Exception
	 */
	public static void saveFileByEncodeStr(String encodeStr, String targetFilePath) {
		Base64 b64 = new Base64();
		byte[] buffer = b64.decode(encodeStr);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(targetFilePath);
			fos.write(buffer);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			LOG.info("Base64字符串解密保存文件失败：" + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			LOG.info("Base64字符串解密保存文件失败：" + e.getMessage());
		}
	}

	/***
	 * Base64方法 加密字符串
	 * 
	 * @param str
	 * @return encodeStr 加密后字符串
	 */
	public static String encodeStr(String str) {
		byte[] bytes = str.getBytes();
		Base64 base64 = new Base64();
		bytes = base64.encode(bytes);
		String encodeStr = new String(bytes);
		return encodeStr;
	}

	/***
	 * Base64方法 解密字符串
	 * 
	 * @param encodeStr
	 * @return decodeStr 解密后字符串
	 */
	public static String decodeStr(String encodeStr) {
		byte[] bytes = encodeStr.getBytes();
		Base64 base64 = new Base64();
		bytes = base64.decode(bytes);
		String decodeStr = new String(bytes);
		return decodeStr;
	}

	/***
	 * 将byte[]转成16进制字符串
	 * 
	 * @param data
	 * 
	 * @return 16进制字符串
	 */
	public static String byte2hex(byte[] data) {
		StringBuilder hash = new StringBuilder();
		String stmp;
		for (int n = 0; data != null && n < data.length; n++) {
			stmp = Integer.toHexString(data[n] & 0XFF);
			if (stmp.length() == 1)
				hash.append('0');
			hash.append(stmp);
		}
		return hash.toString();
	}
}
