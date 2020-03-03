package controllers.supervisor.userManager;

import constants.Constants;
import constants.Templets;
import controllers.front.account.AccountHome;
import controllers.supervisor.SupervisorController;
import net.sf.json.JSONObject;
import models.v_user_report_list;
import models.v_user_reported_info;
import business.StationLetter;
import business.Supervisor;
import business.TemplateEmail;
import business.User;
import utils.ErrorInfo;
import utils.PageBean;
import utils.SMSUtil;
import utils.Security;

/**
 * 
 * 类名:ReportedUser
 * 功能:被举报会员管理
 */

public class ReportedUser extends SupervisorController {

	
	//详情
	public static void reportedUser(){
		String name = params.get("name");
		String email = params.get("email");
		String beginTime = params.get("beginTime");
		String endTime = params.get("endTime");
		String orderType = params.get("orderType");
		String key = params.get("key");
		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		
		ErrorInfo error = new ErrorInfo(); 
		PageBean<v_user_reported_info> page = User.queryReportedUserBySupervisor(name, email, beginTime, endTime, key, orderType, 
				currPage, pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}
	
	/**
	 * 举报会员列表
	 */
	public static void reportUsers(String sign, String reportedName) {
		ErrorInfo error = new ErrorInfo();
		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		
		long reportedUserId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			flash.error(error.msg);
			reportedUser();
		}
		
		PageBean<v_user_report_list> page = User.queryReportUserBySupervisor(1L, reportedUserId, currPage, pageSize, error);
		
		render(page , sign, reportedName);
	}
	
	/**
	 * 添加黑名单
	 * @param userId
	 */
	public static void addBlacklist(String sign, String reason) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			json.put("error", error);
			renderJSON(json);
		}
		
		User.addBlacklistBySupervisor(userId, reason, error);
		
		json.put("error", error);
		
		renderJSON(json);
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
			
			reportedUser();
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
	 * 模拟登录
	 */
	public static void simulateLogin(String sign){
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			flash.error(error.msg);
			reportedUser();
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
