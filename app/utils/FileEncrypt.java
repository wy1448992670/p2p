package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * 文件加解密
 * @author lzp
 * @version 6.0
 * @created 2014-7-18
 */
public class FileEncrypt {
	
	/**
	 * 加密文件
	 * @param srcFileName
	 * @param destFileName
	 * @param key
	 */
	public static boolean encrypt(String srcFileName, String destFileName, String key) {
		/* 截取大于16位的加密串 */
		if(null != key && key.length() > 16){
			key = key.substring(0, 16);
		}
		
		File file = new File(srcFileName);
		
		if (!file.exists()) {
			return false;
		}
		
		if (srcFileName.equals(destFileName)) {
			return encrypt(srcFileName, key);
		}
		
		try {
			File inFile = new File(srcFileName);
			File outFile = new File(destFileName);

			InputStream is = new FileInputStream(inFile);
			OutputStream os = new FileOutputStream(outFile);

			byte[] input = new byte[53];

			while (is.read(input) > 0) {
				byte[] output = AES.encrypt(input, key);
				os.write(output, 0, output.length);
				input = new byte[53];
			}

			os.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
			
			return false;
		}
		
		return true;
	}

	/**
	 * 解密文件
	 * @param srcFileName
	 * @param destFileName
	 * @param key
	 */
	public static boolean decrypt(String srcFileName, String destFileName, String key) {
		/* 截取大于16位的加密串 */
		if(null != key && key.length() > 16){
			key = key.substring(0, 16);
		}
		
		File file = new File(srcFileName);
		
		if (!file.exists()) {
			return false;
		}
		
		try {
			File inFile = new File(srcFileName);
			File outFile = new File(destFileName);

			InputStream is = new FileInputStream(inFile);
			OutputStream os = new FileOutputStream(outFile);

			byte[] input = new byte[64];

			while (is.read(input) > 0) {
				byte[] output = AES.decrypt(input, key);
				os.write(output, 0, output.length);
				input = new byte[64];
			}

			os.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * 加密文件
	 * @param fileName
	 * @param key
	 * @return
	 */
	public static boolean encrypt(String fileName, String key) {
		/* 截取大于16位的加密串 */
		if(null != key && key.length() > 16){
			key = key.substring(0, 16);
		}
		
		File file = new File(fileName);
		
		if (!file.exists()) {
			return false;
		}
		
		String encryptFileName = file.getParent() + "/" + UUID.randomUUID().toString();
		
		if (!encrypt(fileName, encryptFileName, key)) {
			return false;
		}
		
		if (!file.delete()) {
			return false;
		}
		
		File encryptFile = new File(encryptFileName);
		
		if (!encryptFile.renameTo(new File(fileName))) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 解密文件
	 * @param fileName
	 * @param key
	 * @return
	 */
	public static boolean decrypt(String fileName, String key) {
		/* 截取大于16位的加密串 */
		if(null != key && key.length() > 16){
			key = key.substring(0, 16);
		}
		
		File file = new File(fileName);
		
		if (!file.exists()) {
			return false;
		}
		
		String decryptFileName = file.getParent() + "/" + UUID.randomUUID().toString();
		
		if (!decrypt(fileName, decryptFileName, key)) {
			return false;
		}
		
		if (!file.delete()) {
			return false;
		}
		
		File decryptFile = new File(decryptFileName);
		
		if (!decryptFile.renameTo(new File(fileName))) {
			return false;
		}
		
		return true;
	}
	
	public static void main(String args[]) throws Exception {
		String key = "GDgLwwdK270Qj1w4";
		//FileEncrypt.encrypt("/Users/md005/Desktop/1.txt", "/Users/md005/Desktop/2.txt", key);
		//FileEncrypt.decrypt("/Users/md005/Desktop/2.txt", "/Users/md005/Desktop/3.txt", key);
//		encrypt("/Users/md005/Desktop/1.txt", key);
		decrypt("/Users/md005/Desktop/1.txt", key);
	}
}