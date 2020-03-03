package controllers;

import org.apache.commons.lang.StringUtils;

import play.mvc.After;
import play.mvc.Before;
import utils.ErrorInfo;
import utils.Security;
import annotation.MaliceFalsify;
import business.DataSafety;
import business.User;
import constants.Constants;

public class MaliceFalsifyCheck extends BaseController {

	private static DataSafety data = null; 
	
	/**
	 * 防篡改拦截
	 */
	@Before
	public static void check() {
		MaliceFalsify falsify = getActionAnnotation(MaliceFalsify.class);
		ErrorInfo error = new ErrorInfo();
		
		if (null != falsify) {
			long userId = 0;
			int type = falsify.type();
			boolean isAjax = falsify.isAjax();
			
			if(0 == type){
				
				if(isAjax)
					renderText("某个提示");
				
				render("某个提示页面");
			}
			
			if(type == Constants.FRONT){
				User user = User.currUser();
				
				if(null == user){
					
					if(isAjax)
						renderText("某个提示");
					
					render("某个提示页面");
				}
				
				userId  = user.id;
			}else if(type == Constants.ADMIN){
				String signUserId = params.get("signUserId"); // 获取提交过来的UserIdSign
				
				if(StringUtils.isBlank(signUserId)){
					
					if(isAjax)
						renderText("某个提示");
					
					flash.error("非法请求"); 
					render("某个提示页面");
				}
				
				userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
				
				if(userId < 1){

					if(isAjax)
						renderText("某个提示");
					
					flash.error(error.msg); 
					render("某个提示页面");
				}
			}
			
			data = new DataSafety();
			data.setId(userId);
			boolean sign = data.signCheck(error);

			if (error.code < 0) {

				if(isAjax)
					renderText("某个提示");
				
				flash.error(error.msg);
				render("某个提示页面");
			}

			if (!sign) {// 数据被异常改动
				flash.error("对不起！尊敬的用户，你的账户资金出现异常变动，请速联系管理员!");
				
				render("某个提示页面");;
			}
		}
	}

	/**
	 * 防篡改更新
	 */
	@After
	public static void update() {
		MaliceFalsify falsify = getActionAnnotation(MaliceFalsify.class);

		if (null != falsify) {
			ErrorInfo error = new ErrorInfo();
			
			if(data.id == 0){
				
				return;
			}
			
			data.updateSignWithLock(data.id, error);// 更新防篡改字段

			if (error.code < 0) {
				flash.error(error.msg);

				return;
			}
		}
	}
}
