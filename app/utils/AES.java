package utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES
 * @author lzp
 * @version 6.0
 * @created 2014-7-18
 */
public class AES {

	private static final String AES = "AES";

	/**
	 * 加密
	 * @param src
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] src, String key) throws Exception {
		Cipher cipher = Cipher.getInstance(AES);
		SecretKeySpec securekey = new SecretKeySpec(key.getBytes(), AES);
		cipher.init(Cipher.ENCRYPT_MODE, securekey);// 设置密钥和加密形式
		
		return cipher.doFinal(src);
	}

	/**
	 * 解密
	 * @param src
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] src, String key) throws Exception {
		Cipher cipher = Cipher.getInstance(AES);
		SecretKeySpec securekey = new SecretKeySpec(key.getBytes(), AES);// 设置加密Key
		cipher.init(Cipher.DECRYPT_MODE, securekey);// 设置密钥和解密形式
		
		return cipher.doFinal(src);
	}
	
	/**
	 * 加密
	 * @param data
	 * @return
	 */
	public final static String encrypt(String src, String key) {
		try {
			return byte2hex(encrypt(src.getBytes(), key));
		} catch (Exception e) {
		}
		
		return null;
	}

	/**
	 * 解密
	 * @param data
	 * @return
	 */
	public final static String decrypt(String src, String key) {
		try {
			return new String(decrypt(hex2byte(src.getBytes()), key));
		} catch (Exception e) {
		}
		
		return null;
	}

	/**
	 * byte数组转十六进制字符串
	 * @param b
	 * @return
	 */
	public static String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
		}
		
		return hs.toUpperCase();
	}

	/**
	 * 十六进制字符串转byte数组
	 * @param b
	 * @return
	 */
	public static byte[] hex2byte(byte[] b) {
		if ((b.length % 2) != 0)
			throw new IllegalArgumentException("长度不是偶数");
		
		byte[] b2 = new byte[b.length / 2];
		
		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		
		return b2;
	}

}
