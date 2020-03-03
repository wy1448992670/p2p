package utils;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import business.User;

import com.shove.security.Encrypt;

import constants.Constants;

public class Security {

	/**
	 * 加密（当前用户id，请求的方法method,当前时间,加密的key）
	 * @param method
	 * @return
	 */
	public static String encrypt(String action) {
		long id = User.currUser().id;
		return Encrypt.encrypt3DES(id+","+action+","+DateUtil.dateToString(new Date())+","+
		Constants.ENCRYPTION_KEY, Constants.ENCRYPTION_KEY);
	}
	
	/**
	 * 是否是合法请求
	 * @param encryString
	 * @param method
	 * @return true合法 false不合法
	 */
	public static boolean isValidRequest(String action, String encryString, ErrorInfo error) {
		error.clear();
		
		if(StringUtils.isBlank(action)) {
			error.code = -1;
			error.msg = "请求方法有误";
			
			return false;
		}
		
		if (StringUtils.isBlank(encryString)) {
			error.code = -1;
			error.msg = "无效请求";
			
			return false;
		}
		
		encryString = Encrypt.decrypt3DES(encryString, Constants.ENCRYPTION_KEY);
		
		if (StringUtils.isBlank(encryString)) {
			error.code = -1;
			error.msg = "无效请求";
			
			return false;
		}
		
		String[] decryArray = encryString.split(",");
		
		if(decryArray.length != 4) {
			error.code = -1;
			error.msg = "无效请求";
			
			return false;
		}
		
		if(!decryArray[1].equals(action)) {
			error.code = -1;
			error.msg = "无效请求";
			
			return false;
		}
		
		if(!decryArray[3].equals(Constants.ENCRYPTION_KEY)) {
			error.code = -1;
			error.msg = "无效请求";
			
			return false;
		}
		
		Date validTime = DateUtil.strToDate(decryArray[2]);
		
		if(validTime == null) {
			error.code = -1;
			error.msg = "无效请求";
			
			return false;
		}
		
		if(!DateUtil.inValidTime(validTime, Constants.VALID_TIME)) {
			error.code = -1;
			error.msg = "安全请求时间已过期，请重新请求";
			
			return false;
		}
		
		User user = User.currUser();
		
		String idStr = decryArray[0];
		
		if(!NumberUtil.isNumericInt(idStr)) {
			error.code = -1;
			error.msg = "无效请求";
			
			return false;
		}
		
		if(user.id != Long.parseLong(idStr)) {
			error.code = -1;
			error.msg = "无效请求";
			
			return false;
		}
		
		error.code = 1;
		return true;
	}
	
	public static String addSign(long id, String action) {
		String des=com.shove.security.Encrypt.encrypt3DES(id+","+action+","+DateUtil.dateToString(new Date()), 
				Constants.ENCRYPTION_KEY);
		String md5=com.shove.security.Encrypt.MD5(des+Constants.ENCRYPTION_KEY);
		String sign=des+md5.substring(0, 8);
		return sign;
	}
	
	public static long checkSign(String sign, String action,int validLength, ErrorInfo error) {
		if(StringUtils.isBlank(sign) || sign.length() < 8) {
			error.code = -1;
			error.msg = "无效请求";
			
			return error.code;
		}
		
		String des = sign.substring(0, sign.length()-8);
		String key = sign.substring(sign.length()-8);
		String md5 = com.shove.security.Encrypt.MD5(des + Constants.ENCRYPTION_KEY);
		
		if(!key.equals(md5.substring(0, 8))) {
			error.code = -1;
			error.msg = "无效请求";
			
			return error.code;
		}
		
		String[] decryArray = Encrypt.decrypt3DES(des, Constants.ENCRYPTION_KEY).split(",");
		
		if(decryArray.length != 3) {
			error.code = -1;
			error.msg = "无效请求";
			
			return error.code;
		}
		
		if(!decryArray[1].equals(action)) {
			error.code = -1;
			error.msg = "无效请求";
			
			return error.code;
		}
		
		Date validTime = DateUtil.strToDate(decryArray[2]);
		
		if(validTime == null) {
			error.code = -1;
			error.msg = "无效请求";
			
			return error.code;
		}
		
		if(!DateUtil.inValidTime(validTime, validLength)) {
			error.code = -1;
			error.msg = "请求时间已过期，请重新请求";
			
			return error.code;
		}
		
		if(!NumberUtil.isNumericInt(decryArray[0])) {
			error.code = -1;
			error.msg = "无效请求";
			
			return error.code;
		}
		
		error.code = 1;
		Logger.info("checkSign: " + Long.parseLong(decryArray[0]));
		return Long.parseLong(decryArray[0]);
	}
	
	public static String encryCookie(String  sessionId , String action) {
		String val = sessionId + "#" + action + Constants.BASE_URL;
		String sign=com.shove.security.Encrypt.MD5(val+Constants.ENCRYPTION_KEY).toUpperCase();

		return sign; 
	}
	
	public static long decryCookie(String encryCookie,String action, ErrorInfo error) {
		error.clear();
		
		if(StringUtils.isBlank(encryCookie)) {
			error.code = -1;
			error.msg = "cookie为空";
			
			return error.code;
		}
		
		String des = encryCookie.substring(0, encryCookie.length()-8);
		String key = encryCookie.substring(encryCookie.length()-8);
		String md5 = com.shove.security.Encrypt.MD5(des + Constants.ENCRYPTION_KEY).toUpperCase();
		
		if(!key.equals(md5.substring(0, 8))) {
			error.code = -1;
			error.msg = "cookie信息有误";
			
			return error.code;
		}

		des = Encrypt.decrypt3DES(des, Constants.ENCRYPTION_KEY);

		if (StringUtils.isBlank(des)) {
			error.code = -2;
			error.msg = "cookie信息有误";
			
			return error.code;
		}
		String [] cookie = des.split("#");
		
		if(!NumberUtil.isNumericInt(cookie[0])) {
			error.code = -3;
			error.msg = "cookie信息有误";
			
			return error.code;
		}
		
		if(!DataUtil.getIp().equals(cookie[2])){
			error.code = -4;
			error.msg = "cookie信息有误";
			
			return error.code;
		}
		
		//校验用户使用的浏览器
		/*String userAgent = Request.current().headers.get("user-agent").toString();

		if(!userAgent.equals(cookie[3])){
			error.code = -5;
			error.msg = "cookie信息有误";
			
			return error.code;
		}*/
		
		String encryAction = action + Constants.BASE_URL;
		if(!encryAction.equals(cookie[4])){
			error.code = -6;
			error.msg = "cookie信息有误";
			
			return error.code;
		}
		
		error.code = 1;
		return Long.parseLong(cookie[0]);
	}
	
}
