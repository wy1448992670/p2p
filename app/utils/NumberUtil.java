package utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import business.User;

public class NumberUtil {

	public static Pattern patternInt = Pattern.compile("(^[+-]?([0-9]|([1-9][0-9]*)))");
//	public static Pattern patternInt = Pattern.compile("^[+-]?[0-9]+$");
	
	public static Pattern patternDouble = Pattern.compile("^[+-]?(([1-9]\\d*\\.?\\d+)|(0{1}\\.\\d+)|0{1})");//判断是否为小数
//	public static Pattern patternDouble = Pattern.compile("\\d+\\.\\d+$|-\\d+\\.\\d+$");//判断是否为小数

	public static boolean isNumeric(String str) { 
		if(StringUtils.isBlank(str)) {
			return false;
		}
		
		for (int i = str.length();--i>=0;){
			if (!Character.isDigit(str.charAt(i))){
				return false;    
				}   
		}   return true;  
	} 
	
	/**
	 * 判断是否是个整数（int,long等）
	 * @param str
	 * @return
	 */
	public static boolean isNumericInt(String str) {
		if(str == null) {
			return false;
		}
		
		return patternInt.matcher(str).matches();
	}
	
	/**
	 * 判断是否是个小数（double,float等）
	 * @param str
	 * @return
	 */
	public static boolean isNumericDouble(String str) {
		if(str == null) {
			return false;
		}
		
		return patternDouble.matcher(str).matches()||isNumericInt(str);
	}
	
	public static boolean isBoolean(String str) {
		if(str == null) {
			return false;
		}
		
		return str.equals("true") || str.equals("false");
	}
	
	public static boolean isDate(String str) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			format.parse(str);
		} catch (ParseException e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 生成订单编号
	 * @return
	 */
	public static String getBillNo(int type) {
		java.util.Date currentTime = new java.util.Date();//得到当前系统时间 
		java.text.SimpleDateFormat formatter2 = new java.text.SimpleDateFormat("yyyyMMddHHmmss"); 
		Random random = new Random(); 
		String billno = type+"X"+User.currUser().id+formatter2.format(currentTime) + random.nextInt(9) + random.nextInt(9) + random.nextInt(9) + random.nextInt(9) + random.nextInt(9) + random.nextInt(9);
		
		return billno;
	}
	
	/**
	 * 生成订单编号
	 * @return
	 */
	public static String getBillNo(String uid) {
		java.util.Date currentTime = new java.util.Date();//得到当前系统时间 
		java.text.SimpleDateFormat formatter2 = new java.text.SimpleDateFormat("yyyyMMddHHmmss"); 
		Random random = new Random(); 
		String billno = uid+formatter2.format(currentTime) + random.nextInt(9) + random.nextInt(9) + random.nextInt(9) + random.nextInt(9) + random.nextInt(9) + random.nextInt(9);
		
		return billno;
	}

	/**
	 * 把int类型转换成long类型
	 */
	public static long getLongVal(int value) {
		return Long.parseLong(value + "");
	}
	
	/**
	 * 金额格式化
	 */
	public static String amountFormat(double amount) {
		DecimalFormat myformat = new DecimalFormat();
		myformat.applyPattern("##,###.00");
		
		return myformat.format(amount);
	}

	/**
	 * 指定位数随机数数字 字母
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param length
	 * @return
	 * @author: zj
	 */
	public static String getRandomCharAndNumr(Integer length) {
	    String str = "";
	    Random random = new Random();
	    for (int i = 0; i < length; i++) {
	        boolean b = random.nextBoolean();
	        if (b) { // 字符串
	             // int choice = random.nextBoolean() ? 65 : 97; 取得65大写字母还是97小写字母
	             str += (char) (97 + random.nextInt(26));// 取得大写字母
	         } else { // 数字
	             str += String.valueOf(random.nextInt(10));
	         }
	     }
	    return str;
	}
	
	/**
	 * 根据传入的数字，得到定长为5的字符串，前面位数不够补0
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param num
	 * @return
	 * @author: zj
	 */
	public static String getNumberByZero(int num) {
		DecimalFormat df = new DecimalFormat("00000");
		String str2 = df.format(num);
		return str2;
	}
	
	public static void main(String[] args) {
		System.out.println(getNumberByZero(0));
	}
}
