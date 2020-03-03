package controllers.payment;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import annotation.LoginCheck;
import business.BackstageSet;
import business.TemplateEmail;
import business.User;
import payment.hf.util.HfConstants;
import play.Play;
import utils.EmailUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.RegexUtils;
import utils.SMSUtil;
import constants.Constants;
import constants.PayType;
import constants.IPSConstants.IpsCheckStatus;
import controllers.BaseController;
import controllers.front.account.LoginAndRegisterAction;

/**
 * 资金托管，Action基类
 *
 * @author hys
 * @createDate  2015年8月29日 上午9:24:05
 *
 */
public class PaymentBaseAction extends BaseController{
	
	/**
	 * 表单自动提交，请求第三方
	 * 
	 * @param html
	 */
	public Map<String, Object> submitForm(String html, int client){
		renderHtml(html);
		return null;
	}

	/**
	 * 跳转提示页面
	 * 		注意：此提示页面只使用与前台，后台业务请自己编写提示页面
	 */
	public static void payErrorInfo(int code, String msg, PayType payType){
		
		if(StringUtils.isBlank(payType.getSuccessTip())){
			
			throw new RuntimeException("此交易类型不支持：跳转错误提示页面");
		}
		
		String returnUrl;
		String urlDesc;
		
		if(code >= 0 || code == Constants.ALREADY_RUN){
			msg = payType.getSuccessTip();
			returnUrl = payType.getSuccessUrl();
			urlDesc = payType.getSuccessUrlDesc();
		}else{
			returnUrl = StringUtils.isBlank(payType.getFailedUrl())?payType.getSuccessUrl():payType.getFailedUrl();
			urlDesc = StringUtils.isBlank(payType.getFailedUrl())?payType.getSuccessUrlDesc():payType.getFailedUrlDesc();
		}

		String httpPath = Play.configuration.getProperty("http.path");
		
		if(StringUtils.isNotBlank(httpPath)){
			returnUrl = httpPath + returnUrl;
		}
		
		ErrorInfo error = new ErrorInfo();
		
		error.code = code;
		error.msg = msg;
		error.returnUrl = returnUrl;
		error.returnMsg = urlDesc;
		
		render(error);
	}
	
	/**
	 * 开户认证
	 */
	public static void approve() {
		User user = User.currUser();
		
		if(user == null){
			LoginAndRegisterAction.login();
		}
		
		String src = Constants.HTTP_PATH;

		String payType = Constants.TRUST_FUNDS_TYPE;
		
		if("HX".equalsIgnoreCase(payType)){
			src += Constants.IPS_LOGO_IMAGE;
		}
		else if("HF".equalsIgnoreCase(payType)){
			src += HfConstants.PNR_LOGO_IMAGE;
		}
		
		else{
			src += Constants.DEFAULT_SPAY_IMAGE;
		}

		render(src);
	}
	

	/**
	 * ips认证(弹框,ajax)
	 */
	public static void check() {
		User user = User.currUser();

		int status = user.getIpsStatus();

		switch (status) {
		case IpsCheckStatus.NONE:
			if (StringUtils.isNotBlank(user.mobile)
					&& user.isMobileVerified == true
					&& StringUtils.isBlank(user.email)) {
				checkEmailSet();
			}
			checkEmail();
			break;
		case IpsCheckStatus.EMAIL:
			checkEmailSuccess();
			break;
		case IpsCheckStatus.REAL_NAME:
			checkMobile();
			break;
		case IpsCheckStatus.MOBILE:
			createIpsAcct();
			break;
		case IpsCheckStatus.IPS:
			checkSuccess();
			break;
		default:
			break;
		}
	}
	
	/**
	 * 邮箱认证
	 */
	public static void checkEmail() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.NONE) {
			check();
		}

		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		TemplateEmail.activeEmail(user, error);
		String email = user.email;
		String emailUrl = EmailUtil.emailUrl(email);

		BackstageSet set = BackstageSet.getCurrentBackstageSet();
		String phone = set.platformTelephone; // 电话号码
		String qq1 = set.companyQQ1; // QQ1
		String qq2 = set.companyQQ2; // QQ2

		render(email, emailUrl, phone, qq1, qq2);
	}

	/**
	 * 发送激活邮件
	 */
	public static void sendActiveEmail() {
		ErrorInfo error = new ErrorInfo();

		if (User.currUser().getIpsStatus() != IpsCheckStatus.NONE) {
			error.code = -1;
			error.msg = "非法请求";

			renderJSON(error);
		}

		User user = User.currUser();
		TemplateEmail.activeEmail(user, error);

		if (error.code >= 0) {
			error.msg = "激活邮件发送成功！";
		}

		renderJSON(error);
	}

	/**
	 * 邮箱认证成功
	 */
	public static void checkEmailSuccess() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.EMAIL) {
			check();
		}

		BackstageSet set = BackstageSet.getCurrentBackstageSet();
		String phone = set.platformTelephone; // 电话号码
		String qq1 = set.companyQQ1; // QQ1
		String qq2 = set.companyQQ2; // QQ2

		render(phone, qq1, qq2);
	}

	/**
	 * 实名认证页面
	 */
	public static void checkRealName() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.EMAIL) {
			check();
		}

		BackstageSet set = BackstageSet.getCurrentBackstageSet();
		String phone = set.companyTelephone; // 电话号码
		String qq1 = set.companyQQ1; // QQ1
		String qq2 = set.companyQQ2; // QQ2

		render(phone, qq1, qq2);
	}

	/**
	 * 邮箱注册
	 * 
	 * @param email
	 */
	public static void doCheckEmailSet(String email) {
		User user = User.currUser();
		if (null == user) {
			LoginAndRegisterAction.login();
		}

		if (user.getIpsStatus() != IpsCheckStatus.NONE) {
			check();
		}

		ErrorInfo error = new ErrorInfo();

		if (StringUtils.isBlank(email) && !RegexUtils.isEmail(email)) {
			error.code = -1;
			error.msg = "邮箱格式有误！";
			flash.error(error.msg);

			checkEmailSet();
		}

		if (error.code < 0) {
			flash.error(error.msg);

			checkEmailSet();
		}

		user.checkEmail(email, error);

		if (0 > error.code) {
			flash.error(error.msg);

			checkEmailSet();
		}

		user.email = email;
		User.setCurrUser(user);

		check();
	}

	/**
	 * 实名认证
	 */
	public static void doCheckRealName(String realName, String idNumber) {
		User user = User.currUser();
		if (user.getIpsStatus() != IpsCheckStatus.EMAIL) {
			check();
		}

		flash.put("realName", realName);
		flash.put("idNumber", idNumber);

		if (StringUtils.isBlank(realName)) {
			flash.put("nameError", "真实姓名不能为空");

			checkRealName();
		}

		if (StringUtils.isBlank(idNumber)) {
			flash.put("idNoError", "身份证不能为空");
			checkRealName();
		}

		ErrorInfo error = new ErrorInfo();
		user.updateCertification(realName, idNumber, user.id,error);

		if (error.code < 0) {
			flash.put(error.code == -1 ? "nameError" : "idNoError", error.msg);
			checkRealName();
		}

		checkMobile();
	}
	
	/**
	 * 注册邮箱（手机注册）
	 */
	public static void checkEmailSet() {
		render();
	}

	/**
	 * 手机认证页面
	 */
	public static void checkMobile() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.REAL_NAME) {
			check();
		}

		BackstageSet set = BackstageSet.getCurrentBackstageSet();
		String companyName = set.companyName; // 公司名称
		String phone = set.platformTelephone; // 电话号码
		String qq1 = set.companyQQ1; // QQ1
		String qq2 = set.companyQQ2; // QQ2

		render(companyName, phone, qq1, qq2);
	}

	/**
	 * 发送短信验证码
	 * 
	 * @param mobile
	 */
	public static void sendCode(String mobile) {
		ErrorInfo error = new ErrorInfo();
		flash.put("mobile", mobile);

		if (StringUtils.isBlank(mobile)) {
			flash.error("手机号码不能为空");
		} else if (!RegexUtils.isMobileNum(mobile)) {
			flash.error("请输入正确的手机号码");
		} else {

			SMSUtil.sendCode(mobile, error);

			if (error.code < 0) {
				flash.error(error.msg);
			}

			flash.put("isSending", true);
		}
		checkMobile();
	}

	/**
	 * 手机认证
	 * 
	 * @param mobile
	 * @param code
	 */
	public static void doCheckMobile(String mobile, String code) {
		User user = User.currUser();
		if (user.getIpsStatus() != IpsCheckStatus.REAL_NAME) {
			check();
		}

		flash.put("mobile", mobile);
		flash.put("code", code);

		if (StringUtils.isBlank(mobile)) {
			flash.error("手机号不能为空");

			checkMobile();
		}

		if (StringUtils.isBlank(code)) {
			flash.error("验证码不能为空");

			checkMobile();
		}

		ErrorInfo error = new ErrorInfo();
		user.checkMoible(mobile, code, error);

		if (error.code < 0) {
			flash.error(error.msg);

			checkMobile();
		}

		createIpsAcct();
	}

	/**
	 * 资金托管开户
	 */
	public static void createIpsAcct() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.MOBILE) {
			check();
		}

		BackstageSet set = BackstageSet.getCurrentBackstageSet();
		String phone = set.platformTelephone; // 电话号码
		String qq1 = set.companyQQ1; // QQ1
		String qq2 = set.companyQQ2; // QQ2

		render(phone, qq1, qq2);
	}

	/**
	 * 认证成功
	 */
	public static void checkSuccess() {
		if (User.currUser().getIpsStatus() != IpsCheckStatus.IPS) {
			check();
		}

		render();
	}
	
	/**
	 * 富友绑定银行卡
	 */
	public static void checkFyBank() {
		
		List<Map<String, Object>>  list = JPAUtil.getList(new ErrorInfo(), " select * from t_pay_pro_city group by prov_num");
		render(list);
	}
}
