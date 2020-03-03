package controllers.supervisor.userManager;

import constants.Constants;
import controllers.supervisor.SupervisorController;
import net.sf.json.JSONObject;
import models.v_user_unverified_info;
import business.User;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;

/**
 * 
 * 类名:InactiveUser
 * 功能:未激活会员列表
 */

public class InactiveUser extends SupervisorController {

	//详情
	public static void inactiveUser(){
		String name = params.get("name");
		String email = params.get("email");
		String beginTime = params.get("beginTime");
		String endTime = params.get("endTime");
		String orderType = params.get("orderType");
		String key = params.get("key");
		String curPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		
		ErrorInfo error = new ErrorInfo(); 
		PageBean<v_user_unverified_info> page = User.queryUnverifiedUserBySupervisor(name, email, beginTime, endTime, key, orderType, 
				curPage, pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}
	
	/**
	 * 手动激活
	 * @param id
	 */
	public static void activeUser(String sign) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			json.put("error", error);
			renderJSON(error);
		}		
		
		User.activeUserBySupervisor(id, error);
		
		json.put("error", error);
		
		renderJSON(json);
	}
}
