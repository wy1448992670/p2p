package utils.pay.bank.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.lang.StringUtils;

public class SHA256Util{

	// 定义摘要算法为SHA-256
	private static final String SHA256 = "SHA-256";

	/**
	 * 对字符串进行摘要,摘要算法使用SHA-256
	 *
	 * @param bts
	 *            要加密的字符串的byte数组
	 * @return 16进制表示的大写字符串 长度一定是8的整数倍
	 */
	public static byte[] encrypt(byte[] bts) {
		MessageDigest md = null;
		byte[] result = null;
		try {
			md = MessageDigest.getInstance(SHA256);
			md.update(bts);
			result = md.digest();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		return result;
	}

	public static String encrypt(String strSrc) {
		return encrypt(strSrc, "UTF-8");
	}

	public static String encrypt(String strSrc, String charset) {

		byte[] bt = new byte[0];
		try {
			if (StringUtils.isEmpty(charset)) {
				charset = "UTF-8";
			}
			bt = strSrc.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			return null;
		}

		byte[] bts = encrypt(bt);

		return bytes2Hex(bts);

	}

	public static String bytes2Hex(byte[] bts) {
		String des = "";
		String tmp = null;
		for (int i = 0; i < bts.length; i++) {
			tmp = (Integer.toHexString(bts[i] & 0xFF));
			if (tmp.length() == 1) {
				des += "0";
			}
			des += tmp;
		}
		return des;
	}
}
