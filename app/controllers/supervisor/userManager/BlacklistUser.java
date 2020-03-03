package controllers.supervisor.userManager;

import constants.Constants;
import controllers.front.account.AccountHome;
import controllers.supervisor.SupervisorController;
import net.sf.json.JSONObject;
import models.v_user_blacklist_info;
import business.User;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;

/**
 * 
 * 类名:BlacklistUser
 * 功能:黑名单会员列表
 */

public class BlacklistUser extends SupervisorController {

	//详情
	public static void blacklistUser(){
		String name = params.get("name");
		String email = params.get("email");
		String beginTime = params.get("beginTime");
		String endTime = params.get("endTime");
		String orderType = params.get("orderType");
		String key = params.get("key");
		String curPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		
		ErrorInfo error = new ErrorInfo(); 
		PageBean<v_user_blacklist_info> page = User.queryBlacklistUserBySupervisor(name, email, beginTime, endTime, key, orderType, 
				curPage, pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}
	
	/**
	 * 解除黑名单
	 */
	public static void removeBlacklist(String sign){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			json.put("error", error);
			renderJSON(json);
		}
		User.editBlacklist(userId, error);
		
		
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
			blacklistUser();
		}
		
		User user = new User();
		
		user.id = id;
		user.simulateLogin = user.encrypt();
		user.setCurrUser(user);
		AccountHome.home();
	}
}
