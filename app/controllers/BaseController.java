package controllers;

import play.Logger;
import play.Play;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http.Request;
import utils.ErrorInfo;
import business.User;

import com.shove.web.security.InjectionInterceptor;

import constants.Constants;
public class BaseController extends Controller {

	@Before(unless={"front.account.LoginAndRegisterAction.setCode","Application.images","front.account.LoginAndRegisterAction.getImg","ymd.FileUploadController.uploadImage","ymd.BackFileUploadController.getOperatorAuthResultCallBack","ymd.BackFileUploadController.getOperatorAuthCallBack"})
	protected static void injectionInterceptor() throws Exception {
		Request request = Request.current.get();
		Logger.debug("正在执行ation请求命令："+request.action);
		
		new InjectionInterceptor().run();
		
		//更新用户凭证时间为30分钟
		User.setCurrUser(User.currUser());
		
	}
	
	/**
	 * 获取当前请求根路径
	 * 
	 * @return
	 */
	public static String getBaseURL() {
		String baseURL = Constants.BASE_URL;

		Request req = Request.current();
		if (req != null) {
			baseURL = req.getBase()
					+ Play.configuration.getProperty("http.path") + "/";
		}

		return baseURL;
	}

	/**
	 * 跳转错误提示页面
	 */
	public static void payErrorInfo(ErrorInfo error){
		render(error);
	}

}
