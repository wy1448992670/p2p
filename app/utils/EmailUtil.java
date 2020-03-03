package utils;

import java.util.Date;
import java.util.List;
import business.DealDetail;
import business.Supervisor;
import play.Logger;
import constants.Constants;
import constants.OptionKeys;
import constants.SupervisorEvent;
import models.t_message_email_templates;
import models.t_system_email_sending;
import models.t_system_options;
import models.t_users;

public class EmailUtil {
	
	/**
	 * 通过用户的邮箱找回用户名
	 * @param email 用户的邮箱
	 * @param info
	 * @return
	 */
	public static int emailFindUserName(String email, ErrorInfo info) {
		info.clear();
		
		String sql = "select name from t_users where email = ?";
		String optionSql = "select _value from t_system_options where _key = ? or _key = ? order by id";
		String name = null;
		List<String> values  = null;
		t_message_email_templates template = null;
		try{
			name = t_users.find(sql, email).first();
			values = t_system_options.find(optionSql, OptionKeys.PLATFORM_TELEPHONE, OptionKeys.PLATFORM_NAME).fetch();
			template = t_message_email_templates.find("scenarios = ?", Constants.FIND_USERNAME).first();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("通过用户的邮箱找回用户名时:"+e.getMessage());
			info.code = -1;
			info.msg = "模板或用户名查询出现错误";
			
			return info.code;
			
		}
		
		if(name == null) {
			info.code = -2;
			info.msg = "邮箱不存在";
			
			return info.code;
		}
		
		if(values == null || values.size() == 0) {
			info.code = -3;
			info.msg = "系统设置不完善";
			
			return info.code;
		}
		
		if(template == null) {
			info.code = -4;
			info.msg = "通过邮件寻找用户名的邮件模板不存在";
			
			return info.code;
		}
		
		String content = template.content;
		content = content.replace(Constants.EMAIL_NAME, name);
		content = content.replace(Constants.EMAIL_EMAIL, email);
		content = content.replace(Constants.EMAIL_TELEPHONE, values.get(0));
		content = content.replace(Constants.EMAIL_PLATFORM, values.get(1));
		content = content.replace(Constants.EMAIL_TIME, new Date().toString());
		
		t_system_email_sending emailSending = new t_system_email_sending();
		emailSending.time = new Date();
		emailSending.email = email;
		emailSending.title = template.title;
		emailSending.body = content;
		
		try{
			emailSending.save();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("通过用户的邮箱找回用户名:"+e.getMessage());
			info.code = -5;
			info.msg = "找回用户名邮件保存出现错误";
			
			return info.code;
			
		}
		
		return 0;
	}
	
	/**
	 * 激活用户
	 * @param userName
	 * @param email
	 * @param info
	 * @return
	 */
	public static int emailActivateUse(String userName, String email, ErrorInfo info) {
		
		String optionSql = "select _value from t_system_options where _key = ? or _key =? order by id";
		List<String> values  = null;
		t_message_email_templates template = null;
		try{
			values = t_system_options.find(optionSql, OptionKeys.PLATFORM_TELEPHONE, OptionKeys.PLATFORM_NAME).fetch();
			template = t_message_email_templates.find("scenarios = ?", Constants.FIND_USERNAME).first();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("激活用户:"+e.getMessage());
			info.code = -1;
			info.msg = "系统参数或邮件模板查询出现错误";
			
			return info.code;
		}
		
		String content = template.content;
		content = content.replace(Constants.EMAIL_NAME, userName);
		content = content.replace(Constants.EMAIL_EMAIL, email);
		content = content.replace(Constants.EMAIL_TELEPHONE, values.get(0));
		content = content.replace(Constants.EMAIL_PLATFORM, values.get(1));
		content = content.replace(Constants.EMAIL_URL, "url");
		content = content.replace(Constants.EMAIL_TIME, new Date().toString());
		
		t_system_email_sending emailSending = new t_system_email_sending();
		emailSending.time = new Date();
		emailSending.email = email;
		emailSending.title = template.title;
		emailSending.body = content;
		
		try{
			emailSending.save();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("激活用户:"+e.getMessage());
			info.code = -2;
			info.msg = "注册激活邮件保存出现错误";
			
			return info.code;
		}
		
		return 0;
		
	}
	
	/**
	 * 找回密码（根据邮件中的链接重置用户密码）
	 * @param userName
	 * @param email
	 * @param info
	 * @return
	 */
	public static int emailFindUserPassword(String userName, String email, ErrorInfo info) {
		info.clear();
		
		String optionSql = "select _value from t_system_options where _key = ? or _key =? order by id";
		List<String> values  = null;
		t_message_email_templates template = null;
		
		try{
			values = t_system_options.find(optionSql, OptionKeys.PLATFORM_TELEPHONE, OptionKeys.PLATFORM_NAME).fetch();
			template = t_message_email_templates.find("scenarios = ?", Constants.RESET_PASSWORD).first();
		} catch(Exception e) {
			e.printStackTrace();
			info.code = -1;
			info.msg = "系统参数或邮件模板查询出现错误";
			
			return info.code;
		}
		
		String content = template.content;
		content = content.replace(Constants.EMAIL_NAME, userName);
		content = content.replace(Constants.EMAIL_TELEPHONE, values.get(0));
		content = content.replace(Constants.EMAIL_PLATFORM, values.get(1));
		content = content.replace(Constants.EMAIL_URL, "url");
		content = content.replace(Constants.EMAIL_TIME, new Date().toString());
		
		t_system_email_sending emailSending = new t_system_email_sending();
		emailSending.time = new Date();
		emailSending.email = email;
		emailSending.title = template.title;
		emailSending.body = content;
		
		
		try{
			emailSending.save();
		}catch(Exception e) {
			e.printStackTrace();
			info.code = -2;
			info.msg = "找回密码邮件保存出现错误";
			
			return info.code;
		}
		
		info.msg = "重置密码邮件发送成功！";
		
		return 0;
	}
	
	/**
	 * 重置安全问题
	 * @param userName
	 * @param email
	 * @param info
	 * @return
	 */
	public static int emailResetSecretQuestion(String userName, String email, ErrorInfo info) {
		info.clear();
		
		String optionSql = "select _value from t_system_options where _key = ? or _key =? order by id";
		List<String> values  = null;
		t_message_email_templates template = null;
		
		try{
			values = t_system_options.find(optionSql, OptionKeys.PLATFORM_TELEPHONE, OptionKeys.PLATFORM_NAME).fetch();
			template = t_message_email_templates.find("scenarios = ?", Constants.RESET_SECRET_QUESTION).first();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("重置安全问题:"+e.getMessage());
			info.code = -1;
			info.msg = "重置安全问题出现错误";
			
			return info.code;
		}
		
		String content = template.content;
		content = content.replace(Constants.EMAIL_NAME, userName);
		content = content.replace(Constants.EMAIL_TELEPHONE, values.get(0));
		content = content.replace(Constants.EMAIL_PLATFORM, values.get(1));
		content = content.replace(Constants.EMAIL_URL, "url");
		content = content.replace(Constants.EMAIL_TIME, new Date().toString());
		
		t_system_email_sending emailSending = new t_system_email_sending();
		emailSending.time = new Date();
		emailSending.email = email;
		emailSending.title = template.title;
		emailSending.body = content;
		
		
		try{
			emailSending.save();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("重置安全问题:"+e.getMessage());
			info.code = -2;
			info.msg = "重置安全问题保存出现错误";
			
			return info.code;
		}
		
		info.msg = "重置安全问题邮件发送成功！";
		
		return 0;
	}
	
	/**
	 * 发送普通邮件
	 * @param type 2  重置密邮件 1 普通邮件 3催收邮件
	 * @param email
	 * @param title
	 * @param content
	 * @param error
	 * @return
	 */
	public static int sendEmail(int type, String email, String title, String content, ErrorInfo error) {
		
		t_system_email_sending emailSending = new t_system_email_sending();
		
		emailSending.time = new Date();
		emailSending.email = email;
		emailSending.title = title;
		emailSending.body = content;
		
		try{
			emailSending.save();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("发送邮件时:"+e.getMessage());
			error.code = -2;
			error.msg = "邮件发送失败";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, type == 1 ? SupervisorEvent.RESET_PASSWORD :SupervisorEvent.SEND_EMAIL, 
				type == 1 ? "发送重置用户密码邮件" : "发送邮件", error);
		
		error.msg = "邮件发送成功！";
		
		return 0;
	}
	
	/**
	 * 根据邮箱
	 * @param email
	 * @return
	 */
	public static String emailUrl(String email) {
		String domain = email.substring(email.indexOf('@') + 1);
		
		return "http://mail." + domain + '/';
	}
}
