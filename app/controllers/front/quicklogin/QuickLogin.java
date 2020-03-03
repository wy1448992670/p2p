package controllers.front.quicklogin;

import java.util.Date;
import org.apache.commons.lang.StringUtils;
import net.sf.json.JSONObject;
import business.User;
import constants.Constants;
import controllers.BaseController;
import controllers.front.account.AccountHome;
import play.cache.Cache;
import utils.CaptchaUtil;
import utils.ErrorInfo;
import utils.RegexUtils;
import utils.Security;

public class QuickLogin extends BaseController{

	public static void quickLogin(){
		Object jsonObject = params.get("obj");
		if(null == jsonObject){
			jsonObject = Cache.get("obj_"+session.getId());
			Cache.delete("obj_"+session.getId());
		}
		JSONObject obj = JSONObject.fromObject(jsonObject);
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		render(loginOrRegister,obj);
	}
	
	public static void quickLogining(){
		ErrorInfo error = new ErrorInfo();

		String name = params.get("name");
		String url = request.headers.get("referer").value();
		String password = params.get("password");
		String code = params.get("code");
		String randomID = params.get("randomID");
		String bindKey = params.get("bindKey");
		String bindVal = params.get("bindVal");
		Object obj = params.get("obj");
		Cache.add("obj_"+session.getId(), obj);
		flash.put("name", name);
		flash.put("code", code);
		
		if (StringUtils.isBlank(name)) {
			flash.error("请输入用户名");
			redirect(url);
		}

		if (StringUtils.isBlank(password)) {
			flash.error("请输入密码");
			redirect(url);
		}

		if (StringUtils.isBlank(code)) {
			flash.error("请输入验证码");
			redirect(url);

		}

		if (StringUtils.isBlank(randomID)) {
			flash.error("请刷新验证码");
			redirect(url);
		}

		//QQ/微博绑定唯一标示
		long identify = 0;
		if (!StringUtils.isBlank(bindKey)) {
			identify = Security.checkSign(bindKey, Constants.BASE_URL, Constants.VALID_TIME, error);
			if(error.code < 0){
				flash.error("授权地址错误");
				redirect(url);
			}

		}
		
		User user = new User();
		user.name = name;

		if (user.id < 0) {
			flash.error("该用户名不存在");
			redirect(url);
		}
		
		if (!code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) {
			flash.error("验证码错误");
			redirect(url);
		}

		if (user.login(password,false, Constants.CLIENT_PC, error) < 0) {
			flash.error(error.msg);
			redirect(url);
		}

		//绑定QQ/微博账户
		if(identify == Constants.IDENTIFIED_QQ){
			user.qqKey = bindVal;
			if (user.bindingQQ(error) < 0) {
				flash.error(error.msg);
				redirect(url);
			}
		}else if(identify == Constants.IDENTIFIED_WB){
			user.weiboKey = bindVal;
			if (user.bindingWEIBO(error) < 0) {
				flash.error(error.msg);
				redirect(url);
			}
		}
		
		if (Constants.QUICK_LOGIN.equalsIgnoreCase(url)) {
			AccountHome.home();
		}

		redirect(url);
	}
	
	public static void quickRegist(){
		Object jsonObject = params.get("obj");
		if(null == jsonObject){
			jsonObject = Cache.get("obj_"+session.getId());
			Cache.delete("obj_"+session.getId());
		}
		JSONObject obj = JSONObject.fromObject(jsonObject);
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		render(loginOrRegister,obj);
	}
	
	public static void quickRegisting(){
		checkAuthenticity();
		ErrorInfo error = new ErrorInfo();

		String name = params.get("userName");
		String email = params.get("email");
		String password = params.get("password");
		String confirmPassword = params.get("confirmPassword");
		String randomID = (String) Cache.get(params.get("randomID"));
		String code = params.get("code");
		String recommendUserName = params.get("recommended");
		String bindKey = params.get("bindKey");
		String bindVal = params.get("bindVal");
		Object obj = params.get("obj");
		Cache.add("obj_"+session.getId(), obj);
		flash.put("userName", name);
		flash.put("email", email);
		flash.put("password", password);
		flash.put("confirmPassword", confirmPassword);
		flash.put("recommendUserName", recommendUserName);
		flash.put("code", code);
		
		if (StringUtils.isBlank(name)) {
			flash.error("请填写用户名");
			quickRegist();
		}

		if (StringUtils.isBlank(password)) {
			flash.error("请输入密码");
			quickRegist();
		}

		if (StringUtils.isBlank(confirmPassword)) {
			flash.error("请输入确认密码");
			quickRegist();
		}

		if (StringUtils.isBlank(code)) {
			flash.error("请输入验证码");
			quickRegist();
		}

		if (!RegexUtils.isValidUsername(name)) {
			flash.error("请填写符合要求的用户名");
			quickRegist();
		}

		if (!RegexUtils.isEmail(email)) {
			flash.error("请填写正确的邮箱地址");
			quickRegist();
		}

		if (!RegexUtils.isValidPassword(password)) {
			flash.error("请填写符合要求的密码");
			quickRegist();
		}

		if (!password.equals(confirmPassword)) {
			flash.error("两次输入密码不一致");
			quickRegist();
		}

		if (!code.equalsIgnoreCase(randomID)) {
			flash.error("验证码输入有误");
			quickRegist();
		}

		User.isNameExist(name, error);

		if (error.code < 0) {
			flash.error(error.msg);
			quickRegist();
		}

		User.isNameExist(recommendUserName, error);

		if (error.code == 0 || error.code == -10) {
			flash.error("对不起，该用户不存在，不能成为推荐人");
			quickRegist();
		}

		User.isEmailExist(email, null, error);

		if (error.code < 0) {
			flash.error(error.msg);
			quickRegist();
		}

		//QQ/微博绑定唯一标示
		long identify = 0;
		if (!StringUtils.isBlank(bindKey)) {
			identify = Security.checkSign(bindKey, Constants.BASE_URL, Constants.VALID_TIME, error);
			if(error.code < 0){
				flash.error("授权地址错误");
				quickRegist();
			}
		}
		
		
		User user = new User();

		user.time = new Date();
		user.name = name;
		user.password = password;
		user.email = email;
		user.recommendUserName = recommendUserName;
		
		//绑定QQ/微博账户
		if(identify == Constants.IDENTIFIED_QQ){
			boolean bindFlag = User.isBindedQQ(bindVal, error);
            if(bindFlag){
            	flash.error("QQ号已授权");
				quickRegist();
            }
			user.qqKey = bindVal;
		}else if(identify == Constants.IDENTIFIED_WB){
			boolean bindFlag = User.isBindedWEIBO(bindVal, error);
            if(bindFlag){
            	flash.error("微博账号已授权");
				quickRegist();
            }
			user.weiboKey = bindVal;
		}
		
		user.register(Constants.CLIENT_OTHERS, error);

		if (error.code < 0) {
			flash.error(error.msg);
			quickRegist();
		}

		AccountHome.home();
	}
}
