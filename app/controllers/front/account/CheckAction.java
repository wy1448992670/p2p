package controllers.front.account;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import payment.PaymentProxy;
import payment.hf.util.HfConstants;
import utils.EmailUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.RegexUtils;
import utils.SMSUtil;
import business.BackstageSet;
import business.TemplateEmail;
import business.User;
import constants.Constants;
import constants.IPSConstants.IpsCheckStatus;
import controllers.BaseController;

/**
 * 安全认证
 *
 * @author hys
 * @createDate  2015年9月11日 上午8:42:12
 *
 */
public class CheckAction extends BaseController {
	
	/**
	 * 账户激活
	 */
	public static void inactiveUser(){
		User user = User.currUser();
		
		if(user == null){
			LoginAndRegisterAction.login();
		}
		
		render(user);
	}
	
	/**
	 * 资金托管账户开户页面
	 */
	public static void trustAccount() {
		if(!Constants.IPS_ENABLE){
			AccountHome.home();
		}

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
	 * 托管开户
	 */
	public static void createAcct() {		
		ErrorInfo error = new ErrorInfo();
		PaymentProxy.getInstance().register(error, Constants.PC, User.currUser());
		
		flash.error(error.msg);
		CheckAction.trustAccount();
	}

}
