package utils;

import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import constants.Constants;
import play.cache.Cache;
import play.libs.Codec;
import play.libs.Images;
import play.libs.Images.Captcha;

public class CaptchaUtil {

   /**
    * 生成验证码
    */
	public static String setCaptcha(){
		String randomID = Codec.UUID();
	    return randomID;
	}

	/**
	 * 生成验证码图片
	 * @param id
	 * @return
	 */
   public static Captcha CaptchaImage(String id){
	    if(StringUtils.isBlank(id)){  //id不允许为空
//	    	id = UUID.randomUUID().toString();
	    	return null;
	    }
	    
	    String cachecode = (String)Cache.get(id);
	    if(!StringUtils.isEmpty(cachecode)){
	    	return null;  //同一id只能生成一次验证码
	    }
	    
	    Images.Captcha captcha = Images.captcha();
	    
		String code = captcha.setBackground("#DEF1F8", "#DEF1F8").getText("#0056A0", 4); // 从做到右背景颜色变化
		Cache.set(id, code, "10mn");
		return captcha;
   }
 
   public static String getCode(String id) {
	   
	   String code = (String) Cache.get(id);
	   Cache.delete(id);
	   
	   return code;
   }

	/**
	 * 生成UUID,放Cache中
	 * 
	 * @return UUID
	 */
	public static String getUUID() {
		String uuid = UUID.randomUUID().toString();
		Cache.set(uuid, uuid, Constants.CACHE_TIME);

		return uuid;
	}
	
	/**
	 * check Cache UUID
	 */
	public static boolean checkUUID(String key) {
		if(StringUtils.isBlank(key))
			return false;
			
		Object obj = Cache.get(key);
		
		try {
			Cache.delete(key);
		} catch (Exception e) {
			return false;
		}
		
		if(null == obj)
			return false;
		
		return true;
	}
}
