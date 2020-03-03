/*
 * @(#)StringUtils.java 2011-2-26上午12:28:15
 * Copyright 2011 HelloPY.com
 */
package utils;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 字符转码
 * @author liulj
 * 2015年3月26日下午1:10:12
 */
public class CharsetUtil {
	private static Logger LOG = LoggerFactory.getLogger(CharsetUtil.class);
	public static final String CHARSET_ISO="ISO-8859-1";
	public static final String CHARSET_GB2312="GB2312";
	public static final String CHARSET_GBK="GBK";
	public static final String CHARSET_UTF8="UTF-8";
	public static final String CHARSET_BIG5="big5";

	public static String getCharset(String str) {
		if(str==null)return "";
		try {
			if (str.equals(new String(str.getBytes(CHARSET_ISO), CHARSET_ISO))) {
				return CHARSET_ISO;
			}else if(str.equals(new String(str.getBytes(CHARSET_GB2312), CHARSET_GB2312))){
				return CHARSET_GB2312;
			}else if(str.equals(new String(str.getBytes(CHARSET_GBK), CHARSET_GBK))){
				return CHARSET_GBK;
			}else if(str.equals(new String(str.getBytes(CHARSET_BIG5), CHARSET_BIG5))){
				return CHARSET_BIG5;
			}else if(str.equals(new String(str.getBytes(CHARSET_UTF8), CHARSET_UTF8))){
				return CHARSET_UTF8;
			}else{
				return "";
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			LOG.error("CharsetUtil/getEncoding->Error:"+e.getMessage());
			return "";
		}
	}
	

	public static String charsetConverter(String str) {
		if(StringUtils.isEmpty(str)){
			return "";
		}
		try {
			String step1_str=new String(str.getBytes(CHARSET_ISO),CHARSET_UTF8);
			String step1_encod=getCharset(step1_str);
			if(step1_encod.equals(CHARSET_GBK)||step1_encod.equals(CHARSET_GB2312)){
				return new String(str.getBytes(CHARSET_ISO),CHARSET_UTF8);
			}else if(step1_encod.equals(CHARSET_UTF8)){
				String step2_str=new String(str.getBytes(CHARSET_ISO),CHARSET_GBK);
				String step2_cencod=getCharset(step2_str);
				if(step2_cencod.equals(CHARSET_UTF8)){
					step2_str=new String(str.getBytes(CHARSET_ISO),CHARSET_UTF8);
				}
				return step2_str;
			}else {
				return str;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			LOG.error("CharsetUtil/charsetConverter->Error:"+e.getMessage());
			return str;
		}
	}

	public static String URLEncode(String decodeStr){
		if(StringUtils.isEmpty(decodeStr))return "";
		try {
			return URLEncoder.encode(decodeStr, CHARSET_UTF8);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			LOG.error("CharsetUtil/URLEncode->Error:"+e.getMessage());
			return decodeStr;
		}
	}
	
	public static String URLDecode(String encodeStr){
		if(StringUtils.isEmpty(encodeStr))return "";
		try {
			return URLDecoder.decode(encodeStr, CHARSET_UTF8);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			LOG.error("CharsetUtil/URLDecode->Error:"+e.getMessage());
			return encodeStr;
		}
	}
}