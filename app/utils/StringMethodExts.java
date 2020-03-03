package utils;

import java.util.regex.Pattern;

import play.templates.JavaExtensions;

/**
 * String对象的模板方法扩展集
 * <br><b>作者 : </b>chenqiao
 */
public class StringMethodExts extends JavaExtensions {
	
	public static String safeBankAccount(String bankAccount) {
	    return Pattern.compile("(\\w+)(\\w{4})").matcher(bankAccount).replaceAll("********$2");
	}
	
	
}
