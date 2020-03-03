package controllers.supervisor.userManager;

import java.util.Date;
import org.apache.commons.lang.StringUtils;
import net.sf.json.JSONObject;
import constants.Constants;
import constants.Templets;
import controllers.front.account.AccountHome;
import controllers.supervisor.SupervisorController;
import models.t_users;
import models.v_user_complex_info;
import business.BackstageSet;
import business.StationLetter;
import business.Supervisor;
import business.TemplateEmail;
import business.User;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.PageBean;
import utils.SMSUtil;
import utils.Security;

/**
 * 
 * 类名:ComplexUser
 * 功能:复合会员列表
 */

public class ComplexUser extends SupervisorController {

	/**
	 * 复合会员
	 */
	public static void complexUser(){
		String name = params.get("name");
		String email = params.get("email");
		String beginTime = params.get("beginTime");
		String endTime = params.get("endTime");
		String orderType = params.get("orderType");
		String key = params.get("key");
		String curPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		
		ErrorInfo error = new ErrorInfo(); 
		PageBean<v_user_complex_info> page = User.queryComplexUserBySupervisor(name, email, beginTime, endTime, key, orderType, 
				curPage, pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}
	
	/**
	 * 详情
	 * @param id
	 */
	public static void detail(String sign){
        ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			
			complexUser();
		}
		
		User user = new User();
		user.id = id;

		render(user);
	}

	/**
	 * 站内信
	 */
	public static void stationLetter(String sign, String content, String title){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		long receiverUserId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			json.put("error", error);
			renderJSON(json);
		}
		
		StationLetter message = new StationLetter();
		
		message.senderSupervisorId = Supervisor.currSupervisor().id;
		message.receiverUserId = receiverUserId;
		message.title = title;
		message.content = content;
		
		message.sendToUserBySupervisor(error); 
		
		
		json.put("error", error);
		
		renderJSON(json);
	}

	
	/**
	 * 邮件
	 */
	public static void email(String email, String content){
		ErrorInfo error = new ErrorInfo();
		TemplateEmail.sendEmail(1, email, null, Templets.replaceAllHTML(content), error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}

	/**
	 * 发信息
	 * @param mobile
	 * @param content
	 */
	public static void sendMsg(String mobile, String content){
		
		ErrorInfo error = new ErrorInfo();
		SMSUtil.sendSMS(mobile, content, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 重置密码
	 */
	public static void resetPassword(String userName, String email){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		if(StringUtils.isBlank(userName) || StringUtils.isBlank(email)) {
			error.code = -1;
			error.msg = "参数传入有误";
			json.put("error", error);
			
			renderJSON(json);
		}
		
		User.isEmailExist(email, null, error);

		if (error.code != -2) {
			error.code = -1;
			error.msg = "对不起，该邮箱没有注册";
			json.put("error", error);
			
			renderJSON(json);
		}
		
		t_users user = User.queryUserByEmail(email, error);
		
		TemplateEmail tEmail = new TemplateEmail();
		tEmail.id = 3;

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String sign = Security.addSign(user.id, Constants.PASSWORD);
		String url = Constants.RESET_PASSWORD_EMAIL + sign;

		String content = tEmail.content;

		content = content.replace(Constants.EMAIL_NAME, user.name);
		content = content.replace(Constants.EMAIL_TELEPHONE, backstageSet.companyTelephone);
		content = content.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
		content = content.replace(Constants.EMAIL_URL, "<a href = "+url+">点击此处重置密码</a>");
		content = content.replace(Constants.EMAIL_TIME, DateUtil.dateToString(new Date()));

		TemplateEmail.sendEmail(2, email, tEmail.title, content, error);
		
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 模拟登录
	 */
	public static void simulateLogin(String sign){
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			flash.error(error.msg);
			complexUser();
		}
		
		User user = new User();
		
		user.id = id;
		user.simulateLogin = user.encrypt();
		user.setCurrUser(user);
		AccountHome.home();
	}
	
	/**
	 * 锁定用户
	 */
	public static void lockUser(String sign){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			json.put("error", error);
			renderJSON(json);
		}
		
		User.lockUser(id, error);
		
		json.put("error", error);
		
		renderJSON(json);
	}

}
