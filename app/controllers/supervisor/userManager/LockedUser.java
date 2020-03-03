package controllers.supervisor.userManager;

import constants.Constants;
import controllers.supervisor.SupervisorController;
import net.sf.json.JSONObject;
import models.v_user_locked_info;
import business.User;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;

/**
 * 
 * 类名:LockedUser
 * 功能:已锁定会员列表
 */

public class LockedUser extends SupervisorController {

	/**
	 * 已锁定用户
	 */
	public static void lockedUser(){
		String name = params.get("name");
		String email = params.get("email");
		String beginTime = params.get("beginTime");
		String endTime = params.get("endTime");
		String orderType = params.get("orderType");
		String key = params.get("key");
		String curPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		
		ErrorInfo error = new ErrorInfo(); 
		PageBean<v_user_locked_info> page = User.queryLockedUserBySupervisor(name, email, beginTime, endTime, key, orderType, 
				curPage, pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}
	
	/**
	 * 开启
	 * @param id
	 */
	public static void openUser(String sign) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			json.put("error", error);
			
			renderJSON(json);
		}
		
		User.openUser(id, error);
		
		
		json.put("error", error);
		
		renderJSON(json);
	}
}
