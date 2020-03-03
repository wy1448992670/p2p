package utils.pay.bank.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import utils.Base64;

/**
 * Created by chenyuehua on 2014/11/15.
 * 各种算法工具类，方便使用
 */
@SuppressWarnings("restriction")
public abstract class JRAlgorithmUtils {
	private static final String DES = "DESede";
	private static final String TRANSFORMATION = "DESede/ECB/PKCS5Padding";
	
    /**
     * 使用3Des进行加密，
     *
     * @param plainText
     * @param charset
     * @return
     */
    public static byte[] des3Encrypt(String plainText, String key, String charset) {

        try {
            SecretKey secretKey = new SecretKeySpec(key.getBytes(charset), DES);
/*
            KeyGenerator kg = KeyGenerator.getInstance("DESede");
            kg.init(new SecureRandom(key.getBytes(charset)));
            SecretKey secretKey = kg.generateKey();
*/
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            return cipher.doFinal(plainText.getBytes(charset));

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 使用3DES进行解密
     *
     * @param cipherText
     * @param key
     * @param charset
     * @return
     */
    public static String des3Decrypt(byte[] cipherText, String key, String charset) {
        try {
            SecretKey secretKey = new SecretKeySpec(key.getBytes(charset), DES);

/*
            KeyGenerator kg = KeyGenerator.getInstance("DESede");
            // kg.init(56);
            kg.init(new SecureRandom(key.getBytes(charset)));
            SecretKey secretKey = kg.generateKey();
*/

            Cipher c1 = Cipher.getInstance(TRANSFORMATION);
            c1.init(Cipher.DECRYPT_MODE, secretKey);    //初始化为解密模式
            byte[] bytes = c1.doFinal(cipherText);
            return new String(bytes, charset);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 使用Base64进行编码
     *
     * @param input
     * @param charset
     * @return
     */
    public static String base64Encode(String input, String charset) {
        try {
            return base64Encode(input.getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * base64解码
     *
     * @param input
     * @return
     */
    public static String base64Encode(byte[] input) {
        //return BASE64Util.encode(input);
    	return new Base64().encode(input);
    }

    /**
     * 使用Base64进行解码
     *
     * @param input
     * @return
     */
    public static byte[] base64Decode(String input) {
        try {
            //byte[] bytes = BASE64Util.decode(input);
            byte[] bytes = new Base64().decode(input);
            return bytes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 按url编码方式对给定字符串进行编码
     *
     * @param input
     * @param charset
     * @return
     */
    public static String urlEncode(String input, String charset) {
        try {
            return URLEncoder.encode(input, charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param input
     * @param charset
     * @return
     */
    public static String urlDecode(String input, String charset) {
        try {
            return URLDecoder.decode(input, charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    public static void main(String[] args) {

    }


}
