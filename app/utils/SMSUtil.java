package utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;

import business.BackstageSet;
import constants.Constants;
import play.Logger;
import play.Play;
import play.cache.Cache;
import utils.ymsms.SMS;

public class SMSUtil {
    
	public static final String url = Play.configuration.getProperty("jwURL", "");
	public static final String productid = Play.configuration.getProperty("jw_productID", ""); 
	
	
	/** 
	* @MethodName: sendSMS 
	* @Param: SMSUtil 
	* @Return: 
	* @Descb: 发送短信 
	* @Throws: 
	*/ 
	public static String sendSMS(String userName, String password, String content, 
			String phone) { 

		try{ 

			Map<String, String> map = new HashMap<String, String>(); 
			map.put("username", userName); 
			map.put("password", password); 
			map.put("mobile", phone);
			
			
			map.put("content", content); 
			map.put("productid", productid); 
			String data = MmmUtil.byPostMethodToHttpEntity(url, MmmUtil.putParams(map), "UTF-8"); 
			
			
			String status = data.split(",")[0]; 
			if(status.equals("1")){ 
				return "Success"; 
			} 
			new Exception(); 
		}catch (Exception e) { 
			
			return "Fail"; 
		};	
		return "Success"; 

	}
   
	/**
	 * 发送短信
	 * @param mobile
	 * @param content
	 * @param error
	 */
	public static void sendSMS(String mobile,String content, ErrorInfo error, Integer...sendType) {
		if(StringUtils.isBlank(content)) {
			error.code = -1;
			error.msg = "请输入短信内容";
			
			return;
		}
		
		BackstageSet backstageSet  = BackstageSet.getCurrentBackstageSet();
		
		//sendSMS(backstageSet.smsAccount, backstageSet.smsPassword, content, mobile);
		
		if(sendType != null && sendType.length > 0){
			//new SMS(SMS.SEND_TYPE_MARKETING).send(mobile, content);
			SMS.send(mobile, content, SMS.SEND_TYPE_MARKETING);
		}else{
			//new SMS().send(mobile, content);
			SMS.send(mobile, content, SMS.SEND_TYPE_SYSTEM);
		}
		
		error.msg = "短信发送成功";
	}
	
	/**
	 * 发送校验码
	 * @param mobile
	 * @param error
	 */
	public static void sendCode(String mobile, ErrorInfo error) {
		error.clear();
		
		BackstageSet backstageSet  = BackstageSet.getCurrentBackstageSet();
		
		String sms_code = Play.configuration.getProperty("sms_code", "");
		
		String randomCode = StringUtils.isNotBlank(sms_code) ? sms_code : RandomStringUtils.random(4, "1234567890");
//		int randomCode = (new Random()).nextInt(8999) + 1000;// 最大值位9999
//		int randomCode = 1111;// 最大值位9999
		String content = randomCode+"(动态验证码)。工作人员不会向您索要，请勿向任何人泄露";
		
		Logger.info(content);
		//sendSMS(backstageSet.smsAccount, backstageSet.smsPassword, content, mobile);
		//new SMS().send(mobile, content);
		
 		int sendCount = 0;
		try {
			sendCount = (int) Cache.get("sms_code_caogle_"+mobile);
		} catch (Exception e) {}
		
		if(sendCount > 6) {
			error.code = -999;
			error.msg = "发送太多次了, 请稍候重试!";
		}else {
			SMS.send(mobile, content, SMS.SEND_TYPE_SYSTEM);
			
			play.cache.Cache.set(mobile, randomCode + "", "4min");
			
			Cache.set("sms_code_caogle_"+mobile, ++sendCount, Constants.CACHE_TIME_MINUS_30);
			error.msg = "短信验证码发送成功";
		}

	}
	
	
	/**
	 * 发送校验码(新)   为了借款app 而造
	 * @param mobile
	 * @param error
	 */
	public static void sendCode(String mobile,int financeType ,ErrorInfo error) {
		error.clear();
		
		//BackstageSet backstageSet  = BackstageSet.getCurrentBackstageSet();
		
		String sms_code = Play.configuration.getProperty("sms_code", "");
		
		String randomCode = StringUtils.isNotBlank(sms_code) ? sms_code : RandomStringUtils.random(4, "1234567890");
//		int randomCode = (new Random()).nextInt(8999) + 1000;// 最大值位9999
//		int randomCode = 1111;// 最大值位9999
		String content = randomCode+"(动态验证码)。工作人员不会向您索要，请勿向任何人泄露";
		
		Logger.info(content);
		//sendSMS(backstageSet.smsAccount, backstageSet.smsPassword, content, mobile);
		//new SMS().send(mobile, content);
		
		int sendCount = 0;
		try {
			sendCount = (int) Cache.get("sms_code_caogle_"+financeType+"_"+mobile);
		} catch (Exception e) {}
		
		if(sendCount > 6) {
			error.code = -999;
			error.msg = "发送太多次了, 请稍候重试!";
		}else {
			SMS.send(mobile, content, SMS.SEND_TYPE_SYSTEM);
			
			play.cache.Cache.set(mobile+"_"+financeType, randomCode + "", "4min");
			
			Cache.set("sms_code_caogle_"+financeType+"_"+mobile, ++sendCount, Constants.CACHE_TIME_MINUS_30);
			error.msg = "短信验证码发送成功";
		}

	}
}
