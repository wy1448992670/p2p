package utils;

import java.util.Date;
import java.util.List;
import constants.Constants;
import constants.OptionKeys;
import models.t_message_sms_templates;
import models.t_system_mobile_sms_sending;
import models.t_system_options;
import models.t_users;
import play.Logger;

public class MobileUtil {

	/**
	 * 发送普通短信
	 * @param email
	 * @param title
	 * @param content
	 * @param info
	 * @return
	 */
	public static int sendMoblie(String mobile,String content, ErrorInfo info) {
		
		t_system_mobile_sms_sending mobileSending = new t_system_mobile_sms_sending();
		
		mobileSending.time = new Date();
		mobileSending.mobile = mobile;
		mobileSending.body = content;
		
		try{
			mobileSending.save();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("发送短信:"+e.getMessage());
			info.code = -2;
			info.msg = "发送短信出现错误";
			
			return info.code;
		}
		
		info.msg = "发送短信成功！";
		
		return 0;
	}
	
	/**
	 * 通过用户的手机找回用户名
	 * @param telephone 用户的手机
	 * @param info
	 * @return
	 */
	public static int mobileFindUserName(String telephone, ErrorInfo error) {
		error.clear();
		
		String sql = "select name from t_users where mobile = ?";
		String optionSql = "select _value from t_system_options where _key = ? or _key = ? order by id";
		String name = null;
		List<String> values  = null;
		t_message_sms_templates template = null;
		try{
			name = t_users.find(sql, telephone).first();
			values = t_system_options.find(optionSql, OptionKeys.PLATFORM_TELEPHONE, OptionKeys.PLATFORM_NAME).fetch();
			template = t_message_sms_templates.find("scenarios = ?", Constants.FIND_USERNAME).first();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("通过用户的手机找回用户名时:"+e.getMessage());
			error.code = -1;
			error.msg = "模板或用户名查询出现错误";
			
			return error.code;
			
		}
		
		if(name == null) {
			error.code = -2;
			error.msg = "手机号码不存在";
			
			return error.code;
		}
		
		if(values == null || values.size() == 0) {
			error.code = -3;
			error.msg = "系统设置不完善";
			
			return error.code;
		}
		
		if(template == null) {
			error.code = -4;
			error.msg = "模板不存在";
			
			return error.code;
		}
		
		String content = template.content;
		content = content.replace(Constants.EMAIL_NAME, name);
		content = content.replace(Constants.EMAIL_EMAIL, telephone);
		content = content.replace(Constants.EMAIL_TELEPHONE, values.get(0));
		content = content.replace(Constants.EMAIL_PLATFORM, values.get(1));
		content = content.replace(Constants.EMAIL_TIME, new Date().toString());
		
		t_system_mobile_sms_sending teleSending = new t_system_mobile_sms_sending();
		teleSending.time = new Date();
		teleSending.mobile = telephone;
		teleSending.body = content;
		
		try{
			teleSending.save();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("通过用户的手机号码找回用户名:"+e.getMessage());
			error.code = -5;
			error.msg = "找回用户名保存出现错误";
			
			return error.code;
			
		}
		
		return 0;
	}
	
	public static void findUserNameByMobile(String telephone, ErrorInfo error) {
		error.clear();
		
		error.msg = "短信发送成功,请注意查收";
	}
}
