package controllers.wechat.service;

import java.util.Date;
import java.util.Map;

import models.t_users;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.shove.security.Encrypt;

import play.cache.Cache;
import play.mvc.Scope.Session;
import utils.CaptchaUtil;
import utils.DateUtil;
import utils.EmailUtil;
import utils.ErrorInfo;
import utils.RegexUtils;
import utils.SMSUtil;
import utils.Security;
import utils.WeChatUtil;
import business.BackstageSet;
import business.TemplateEmail;
import business.User;
import constants.Constants;
import controllers.BaseController;
import controllers.wechat.account.WechatAccountHome;

/**
 * 注册和登录相关
 * 
 * @author Administrator
 *
 */
public class RegistAndLogin extends BaseController {

	/**
	 * 跳转到登录界面
	 */
	public static void login() {
		Object nameObj = Cache.get("name" + Session.current().getId());
		Object passwordObj = Cache.get("password" + Session.current().getId());
		if (nameObj != null && passwordObj != null) {

			if (StringUtils.isBlank(flash.get("error"))) {
				String name = Encrypt.decrypt3DES(nameObj + "",
						Constants.ENCRYPTION_KEY) + "";
				String password = Encrypt.decrypt3DES(passwordObj + "",
						Constants.ENCRYPTION_KEY) + "";
				flash.put("name", name);
				flash.put("password", password);
			}

			flash.put("checkbox", "on");
		}

		if (request.headers.get("referer") != null) {
			String url = request.headers.get("referer").value();

			render(url);
		}

		render();
	}

	/**
	 * 登录操作
	 */
	public static void logining() {
		BackstageSet currBackstageSet = BackstageSet.getCurrentBackstageSet();
		Map<String, java.util.List<business.BottomLinks>> bottomLinks = business.BottomLinks
				.currentBottomlinks();
		if (null != currBackstageSet) {
			Cache.delete("backstageSet");
		}

		if (null != bottomLinks) {
			Cache.delete("bottomlinks");
		}

		ErrorInfo error = new ErrorInfo();

		String url = request.headers.get("referer").value();
		String userName = params.get("name");
		String password = params.get("password");
		String code = params.get("code");
		String randomID = params.get("randomID");

		flash.put("name", userName);
		flash.put("code", code);

		if (StringUtils.isBlank(userName)) {
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

		
		if(!code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) {
			flash.error("验证码错误");
			redirect(url);
		}
	

		User user = new User();
		user.name = userName;

		if (user.id < 0) {
			flash.error("用户名不存在或者密码错误");
			redirect(url);
		}

		if (user.login(password, false, Constants.CLIENT_WECHAT, error) < 0) {
			flash.error(error.msg);
			redirect(url);
		}
		/**
		 * 资金托管(PC端) if (Constants.LOGIN.equalsIgnoreCase(url)) {
		 * 
		 * if(Constants.IPS_ENABLE && (user.getIpsStatus() !=
		 * IpsCheckStatus.IPS)){ CheckAction.approve(); }
		 * 
		 * }
		 * 
		 * redirect(url);
		 */
		if ("on".equals(params.get("checkbox"))) {
			/*
			 * 记住密码
			 */
			String key = Session.current().getId();
			Object nameObj = Cache.get("name" + Session.current().getId());
			Object passwordObj = Cache.get("password"
					+ Session.current().getId());
			if (nameObj != null && passwordObj != null) {
				String nameStr = Encrypt.decrypt3DES(nameObj + "",
						Constants.ENCRYPTION_KEY) + "";
				String passwordStr = Encrypt.decrypt3DES(passwordObj + "",
						Constants.ENCRYPTION_KEY) + "";
				if (!(userName.equals(nameStr))
						|| !(password.equals(passwordStr))) {
					Cache.delete("name" + key);
					Cache.delete("password" + key);
					Cache.set("name" + key, Encrypt.encrypt3DES(userName,
							Constants.ENCRYPTION_KEY), "48h");
					Cache.set("password" + key, Encrypt.encrypt3DES(password,
							Constants.ENCRYPTION_KEY), "48h");
				}
			} else {
				Cache.set(
						"name" + key,
						Encrypt.encrypt3DES(userName, Constants.ENCRYPTION_KEY),
						"48h");
				Cache.set(
						"password" + key,
						Encrypt.encrypt3DES(password, Constants.ENCRYPTION_KEY),
						"48h");
			}
		} else {
			/*
			 * 取消记住密码
			 */
			String key = Session.current().getId();
			Cache.delete("name" + key);
			Cache.delete("password" + key);
		}

		/*
		 * 得到登录页面中传过来的url值,
		 */
		String url2 = params.get("url");
		if (!StringUtils.isBlank(url2)
				&& !url2.contains("/wechat/registAndLogin/login")
				&& !url2.contains("/wechat/registAndLogin/logining")
				&& !url2.contains("/wechat/registAndLogin/register")
				&& !url2.contains("/wechat/registAndLogin/registering")
				&& !url2.contains("/wechat/registAndLogin/forgetPassword")
				&& !url2.contains("/wechat/registAndLogin/forgetPasswording")
				&& !url2.contains("/wechat/registAndLogin/forgetPasswordByEmail")
				&& !url2.contains("/wechat/registAndLogin/forgetPasswordByEmailing")
				&& !url2.contains("/wechat/home/baseInfo")
				&& !url2.contains("/wechat/registAndLogin/unBoundUser")
				&& !url2.contains("/wechat/registAndLogin/unBoundUsering")
				&& !url2.contains("/wechat/registAndLogin/bindUser")
				&& !url2.contains("/wechat/registAndLogin/bindUsering")) {

			redirect(url2);
		}

		
		WechatAccountHome.accountInfo();

	}

	/**
	 * 跳转到注册页面
	 */
	public static void register() {
		/**
		 * pc端 String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		 * 
		 * ErrorInfo error = new ErrorInfo(); String content =
		 * News.queryContent(Constants.NewsTypeId.REGISTER_AGREEMENT2, error);
		 */

		render();
	}

	/**
	 * 验证注册
	 * 
	 * @param t_users
	 */
	public static void registering() {
		// Bad authenticity token
		/*
		 * PC checkAuthenticity();
		 */
		ErrorInfo error = new ErrorInfo();

		String name = params.get("userName");
		String password = params.get("password");
		String telephone = params.get("telephone");
		String yanzhengma = params.get("yanzhengma");
		
		String randomID = (String) Cache.get(params.get("randomID"));
		String code = params.get("code");

		flash.put("userName", name);
		flash.put("telephone", telephone);
		flash.put("yanzhengma", yanzhengma);
		flash.put("code", code);
		
		if (StringUtils.isBlank(name)) {
			flash.error("请填写用户名");

			register();
		}

		if (StringUtils.isBlank(password)) {
			flash.error("请输入密码");

			register();
		}

		if (StringUtils.isBlank(code)) {
			flash.error("请输入图形验证码");
			
			register();
		}
		
		if (StringUtils.isBlank(telephone)) {
			flash.error("请输入手机号码");

			register();
		}

		if (StringUtils.isBlank(yanzhengma)) {
			flash.error("请输入手机验证码");

			register();
		}

		if (!RegexUtils.isValidUsername(name)) {
			flash.error("请填写符合要求的用户名");

			register();
		}

		if (!RegexUtils.isValidPassword(password)) {
			flash.error("请填写符合要求的密码");

			register();
		}

		if (!RegexUtils.isMobileNum(telephone)) {
			flash.error("请填写符合要求的手机");

			register();
		}
	
			
		if (!code.equalsIgnoreCase(randomID)) {
			flash.error("图形验证码输入有误");
			
			register();
		}


		/**
		 * 是否校验手机验证码
		 */
		if (Constants.CHECK_MSG_CODE) {
			Object cCode1 = Cache.get(telephone);

			if (cCode1 == null) {
				flash.error("手机验证码已失效，请重新点击发送验证码");

				register();
			}

			if (!cCode1.toString().equals(yanzhengma)) {
				flash.error("手机验证错误");

				register();
			}
		}
		
		User.isNameExist(name, error);

		if (error.code < 0) {
			flash.error(error.msg);

			register();
		}

		User.isMobileExist(telephone, null, error);

		if (error.code < 0) {
			flash.error(error.msg);

			register();
		}
		User user = new User();

		user.time = new Date();
		user.name = name;
		user.password = password;
		user.mobile = telephone;
		/*
		 * 手机绑定
		 */
		user.isMobileVerified = true;
		user.register(Constants.CLIENT_WECHAT, error);

		if (error.code < 0) {
			flash.error(error.msg);

			register();
		}

		/*
		 * 注册成功后跳转到登录界面
		 */

		/*
		 * 激活用户邮箱
		 */
		user.activeEmail(error);

		if (error.code < 0) {
			flash.error(error.msg);

			register();
		}
		flash.clear();
		flash.error("注册成功");
		flash.put("name", name);

		login();
	}

	/**
	 * 注册跳转到成功页面
	 */
	public static void registerSuccess() {
		User user = User.currUser();
		if (user == null) {
			login();
		}

		// if (Constants.IPS_ENABLE) {
		// CheckAction.approve();
		// }
		//
		// if (user.isEmailVerified) {
		// WechatAccountHome.home();
		// }
		//
		// String loginOrRegister = Constants.LOGIN_AREAL_FLAG;

		// render(loginOrRegister);

		login();
	}

	/**
	 * 通过手机重置密码
	 */
	public static void forgetPassword() {
		/*
		 * String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		 */
		render();
	}

	/**
	 * 保存更改的密码
	 * 
	 * @param mobile
	 * @param code
	 * @param password
	 * @param confirmPassword
	 */
	public static void forgetPasswording(String mobile, String code,
			String password, String confirmPassword,int  financeType) {
		ErrorInfo error = new ErrorInfo();
		
		String code2 = params.get("code2");
		String randomID = params.get("randomID");
		
		User.updatePasswordByMobile(mobile, code, password, confirmPassword, randomID, code2, 
				error,  financeType);

		if (error.code < 0) {
			flash.put("mobile", mobile);
			flash.put("code", code);
			flash.put("code2", code2);
			flash.error(error.msg);

			forgetPassword();
		}

		flash.error(error.msg);

		login();
	}

	/**
	 * 通过邮箱重置密码
	 */
	public static void forgetPasswordByEmail() {
		/*
		 * String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		 */
		render();
	}

	/**
	 * 发送重置密码邮件
	 * 
	 * @param email
	 * @param code
	 * @param randomID
	 */
	public static void forgetPasswordByEmailing(String email, String code,
			String randomID) {
		ErrorInfo error = new ErrorInfo();

		flash.put("email", email);

		if (StringUtils.isBlank(code)) {
			flash.error("请输入验证码");
			forgetPasswordByEmail();
		}

		if (StringUtils.isBlank(email)) {
			flash.error("请输入邮箱地址");
			forgetPasswordByEmail();
		}

		if (!RegexUtils.isEmail(email)) {
			flash.error("请输入正确的邮箱地址");
			forgetPasswordByEmail();
		}

		if (code != null && !code.equalsIgnoreCase(Cache.get(randomID).toString())) {
			flash.error("验证码错误");
			forgetPasswordByEmail();
		}

		User.isEmailExist(email, null, error);

		if (error.code != -2) {
			flash.error("对不起，该邮箱没有注册");
			forgetPasswordByEmail();
		}

		t_users user = User.queryUserByEmail(email, error);

		if (error.code < 0) {
			flash.error(error.msg);
			forgetPasswordByEmail();
		}

		TemplateEmail tEmail = new TemplateEmail();
		tEmail.id = 3;

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String sign = Security.addSign(user.id, Constants.PASSWORD);
		String url = Constants.RESET_PASSWORD_EMAIL + sign;

		String content = tEmail.content;

		content = content.replace("<p", "<div");
		content = content.replace("</p>", "</div>");
		content = content.replace(Constants.EMAIL_NAME, user.name);
		content = content.replace(Constants.EMAIL_LOGIN, "<a href = "
				+ Constants.LOGIN + ">登录</a>");
		content = content.replace(Constants.EMAIL_TELEPHONE,
				backstageSet.companyTelephone);
		content = content.replace(Constants.EMAIL_PLATFORM,
				backstageSet.platformName);
		content = content.replace(Constants.EMAIL_URL, "<a href = " + url + ">"
				+ url + "</a>");
		content = content.replace(Constants.EMAIL_TIME,
				DateUtil.dateToString(new Date()));

		TemplateEmail.sendEmail(0, email, tEmail.title, content, error);

		if (error.code < 0) {
			flash.error(error.msg);
			forgetPasswordByEmail();
		}

		/*
		 * EmailUtil.emailFindUserName(email, error);
		 * 
		 * if (error.code < 0) { flash.error("邮件发送失败，请重新发送");
		 * resetPasswordByEmail(); }
		 */

		flash.put("email", "");
		flash.put("code", "");
		flash.error("邮件发送成功，请查收邮件");
		flash.put("emailUrl", EmailUtil.emailUrl(email));

		login();
	}

	/**
	 * 绑定用户账号
	 * 
	 * @param openId
	 */
	public static void bindUser(String openId) {

		render(openId);
	}

	/**
	 * 绑定用户
	 * 
	 * @param account
	 * @param password
	 * @param openId
	 */
	public static void bindUsering(String name, String password, String code,
			String randomID, String openId) {
		ErrorInfo error = new ErrorInfo();
		// 查看openId是否过期
		Object time = Cache.get("wechat_time" + Session.current().getId());
		if (null == time) {
			flash.error("您的页面过期，重新绑定");

			bindUser(openId);
		}

		// 对openId进行解密
		String openIdDecrypt3DE = WeChatUtil.decrypt3DESOpenId(openId);

		if (null == openIdDecrypt3DE) {
			flash.error("您的页面过期，重新绑定");

			bindUser(openId);
		}

		if (StringUtils.isBlank(name)) {
			flash.error("账号不能为空");

			bindUser(openId);
		}

		if (StringUtils.isBlank(password)) {
			flash.error("密码不能为空");

			bindUser(openId);
		}

		if (!code.equalsIgnoreCase(Cache.get(randomID).toString())) {
			flash.error("验证码错误");

			bindUser(openId);
		}

		// 对解密后的openId进行判断，向前台传的是加密openId，向后台传的是解密后的openId
		if (StringUtils.isBlank(openIdDecrypt3DE)) {
			flash.error("openId不能为空");

			bindUser(openId);
		}

		User.bindUser(name, password, openIdDecrypt3DE, error);

		/**
		 * 失败,则继续绑定
		 */
		if (error.code < 0) {
			flash.error(error.msg);

			bindUser(openId);
		}

		/**
		 * 绑定成功后，帮助用户直接登录，查询用户资料，直接跳转到账户中心
		 */
		User user = new User();
		user.name = name;
		user.login(password, false, Constants.CLIENT_WECHAT, error);

		if (error.code < 0) {
			flash.error(error.msg);

			login();
		}
		BackstageSet.getCurrentBackstageSet();
		WechatAccountHome.accountInfo();
	}

	/**
	 * 解绑用户账号
	 * 
	 * @param openId
	 */
	public static void unBoundUser(String openId) {

		render(openId);
	}

	/**
	 * 解绑绑定用户
	 * 
	 * @param account
	 * @param password
	 * @param openId
	 */
	public static void unBoundUsering(String name, String password,
			String code, String randomID, String openId) {
		ErrorInfo error = new ErrorInfo();
		// 查看openId是否过期
		Object time = Cache.get("wechat_time" + Session.current().getId());
		if (null == time) {
			flash.error("您的页面过期，重新绑定");

			unBoundUser(openId);
		}

		// 对openId进行解密
		String openIdDecrypt3DE = WeChatUtil.decrypt3DESOpenId(openId);

		if (null == openIdDecrypt3DE) {
			flash.error("您的页面过期，重新绑定");

			unBoundUser(openId);
		}

		if (StringUtils.isBlank(name)) {
			flash.error("账号不能为空");

			unBoundUser(openId);
		}

		if (StringUtils.isBlank(password)) {
			flash.error("密码不能为空");

			unBoundUser(openId);
		}

		if (!code.equalsIgnoreCase(Cache.get(randomID).toString())) {
			flash.error("验证码错误");

			unBoundUser(openId);
		}

		// 对解密后的openId进行判断，向前台传的是加密openId，向后台传的是解密后的openId
		if (StringUtils.isBlank(openIdDecrypt3DE)) {
			flash.error("openId不能为空");

			unBoundUser(openId);
		}

		User.unBoundUser(name, password, openIdDecrypt3DE, error);

		/**
		 * 失败,则继续绑定
		 */
		if (error.code < 0) {
			flash.error(error.msg);

			unBoundUser(openId);
		}

		/**
		 * 解绑成功后，直接跳到登录界面
		 */
		flash.error(error.msg);
		RegistAndLogin.login();
	}

	/**
	 * 验证手机号码是否已存在
	 * 
	 * @param name
	 */
	public static void hasMobileExist(String telephone) {
		ErrorInfo error = new ErrorInfo();

		User.isMobileExist(telephone, null, error);

		JSONObject json = new JSONObject();
		json.put("result", error);

		renderJSON(json.toString());
	}

	/**
	 * 发送手机校验码
	 * 
	 * @param code
	 */
	public static void verifyMobile(String mobile, String captcha, String randomID) {
		ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();
        
        if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请输入手机号码";

            json.put("error", error);

            renderJSON(json);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请输入正确的手机号码";

            json.put("error", error);

            renderJSON(json);
        }
        
		if (StringUtils.isBlank(captcha)) {
			
			error.code = -1;
			error.msg = "请输入图形验证码";
			
			json.put("error", error);
			
			renderJSON(json);
		}
		
		if (StringUtils.isBlank(randomID)) {
			
        	error.code = -1;
        	error.msg = "请刷新图形验证码";
        	
        	json.put("error", error);
        	
        	renderJSON(json);
		}
        
        String codec = (String) Cache.get(randomID);
        if (!codec.equalsIgnoreCase(captcha)) {
			
        	error.code = -1;
        	error.msg = "图形验证码错误";
        	
        	json.put("error", error);
        	
        	renderJSON(json);
		}
  		  
        

        User user = User.currUser();

        if (user == null || StringUtils.isBlank(user.mobile) || !user.mobile.equals(mobile)) {
            User.isMobileExist(mobile, null, error);

            if (error.code != -2) {

                json.put("error", error);

                renderJSON(json);
            }
        }

        SMSUtil.sendCode(mobile, error);

        json.put("error", error);

        renderJSON(json);
	}
}

