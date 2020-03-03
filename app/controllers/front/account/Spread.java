package controllers.front.account;

import models.t_user_cps_income;
import models.v_user_cps_user_count;
import models.v_user_cps_users;
import play.mvc.With;
import utils.ErrorInfo;
import utils.PageBean;
import business.BackstageSet;
import business.User;
import constants.Constants;
import controllers.BaseController;
import controllers.interceptor.AccountInterceptor;

@With({AccountInterceptor.class})
public class Spread extends BaseController {

	//-------------------------------GPS推广-------------------------
	//我的GPS链接
	public static void spreadLink(){
		User user = User.currUser();
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
                renderArgs.put("childId", "child_35");
                renderArgs.put("labId", "lab_7");
		render(user, backstageSet);
	}
	
	//我成功推广的会员
	public static void spreadUser(){
		User user = User.currUser();
		long userId = user.id;
		
		String type = params.get("type");
		String key = params.get("key");
		String year = params.get("year");
		String month = params.get("month");
		String currPage = params.get("currPage");
		String pageSize = params.get("currSize");
		
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_cps_users> page = User.queryCpsSpreadUsers(userId ,type, key, 
				year, month, currPage, pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		v_user_cps_user_count cpsCount = User.queryCpsCount(userId, error);
		
		/*查询用户所有的CPS收入*/
		double totalCpsIncome = User.queryTotalCpsIncome(userId);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}

                renderArgs.put("childId", "child_36");
                renderArgs.put("labId", "lab_7");
		render(user, page, cpsCount, totalCpsIncome);
	}
	
	//推广会员详情
	public static void userDetail(){
		render();
	}
	
	/**
	 * 我的推广会员收入
	 */
	public static void spreadIncome(){
		User user = User.currUser();
		long userId = user.id;
		
		String year = params.get("year");
		String month = params.get("month");
		String currPage = params.get("currPage");
		String pageSize = params.get("currSize");
		
		ErrorInfo error = new ErrorInfo();
		
		PageBean<t_user_cps_income> page = User.queryCpsSpreadIncome(userId, 
				year,month,currPage,pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		v_user_cps_user_count cpsCount = User.queryCpsCount(userId, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		double totalCpsIncome = User.queryTotalCpsIncome(userId);
		
		renderArgs.put("childId", "child_37");
		renderArgs.put("labId", "lab_7");
		render(user, page, cpsCount, totalCpsIncome);
	}
	
	/**
	 * 推广收入明细
	 */
	public static void incomeDetail(){
		User user = User.currUser();
		long userId = user.id;
		
		String type = params.get("type");
		String key = params.get("key");
		String year = params.get("year");
		String month = params.get("month");
		String currPage = params.get("currPage");
		String pageSize = params.get("currSize");
		
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_cps_users> page = User.queryCpsSpreadUsers(userId ,type, key, 
				year, month, currPage, pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		render(user, page, year, month);
	}
	
	
}
