package controllers.interceptor;

import java.util.List;

import models.t_dict_ad_citys;
import models.t_dict_ad_provinces;
import models.t_dict_cars;
import models.t_dict_educations;
import models.t_dict_houses;
import models.t_dict_maritals;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.cache.Cache;
import play.mvc.Before;
import play.mvc.Controller;
import annotation.InactiveUserCheck;
import annotation.IpsAccountCheck;
import annotation.LoginCheck;
import annotation.RealNameCheck;
import business.Product;
import business.User;
import constants.Constants;
import controllers.front.account.BasicInformation;
import controllers.front.account.CheckAction;
import controllers.front.account.LoginAndRegisterAction;
import controllers.front.bid.BidAction;

/**
 * 用户状态拦截器
 * 		包括：未登陆、托管未开户、账户未激活、未实名认证
 *
 * @author hys
 * @createDate  2015年9月9日 下午6:24:57
 *
 */
public class UserStatusInterceptor extends Controller{
	
	/**
	 * 登陆拦截器
	 */
	@Before
	static void checkLogin(){
		
		LoginCheck checkAction = getActionAnnotation(LoginCheck.class);
		
		if(checkAction == null){   //不拦截
			
			return;
		}
		
		User user = User.currUser();
		
		if(user == null){  //未登陆
			if(checkAction.value()){  //ajax
				
				renderText(LoginCheck.TOKEN);
			}
			
			LoginAndRegisterAction.login();
		}
	}
	
	/**
	 * 资金托管账户开户拦截
	 */
	@Before
	static void checkIpsAccount(){
		if(!Constants.IPS_ENABLE){  //非资金托管不拦截
			
			return;
		}
		
		IpsAccountCheck checkAction = getActionAnnotation(IpsAccountCheck.class);
		
		if(checkAction == null){  //不拦截
			
			return;
		}
		
		User user = User.currUser();
		
		if(user == null){  //未登录
			if(checkAction.value()){  //ajax
				
				renderText(LoginCheck.TOKEN);
			}
			
			LoginAndRegisterAction.login();
		}
		
		if (StringUtils.isBlank(user.ipsAcctNo)) {  //未开户
			if(checkAction != null && checkAction.value()){  
				
				renderText(IpsAccountCheck.TOKEN);;  //ajax请求
			}
			
			CheckAction.trustAccount();  //非ajax
		}
		
	}
	
	/**
	 * 用户未激活拦截器
	 */
	@Before
	static void checkInactiveUser(){
		if(Constants.IPS_ENABLE){  //资金托管不拦截
			
			return;
		}

		InactiveUserCheck checkAction = getActionAnnotation(InactiveUserCheck.class);
		
		if(checkAction == null){  //不拦截
			
			return;
		}
		
		User user = User.currUser();
		
		if(user == null){  //未登录
			
			if(checkAction.value()){
				renderText(LoginCheck.TOKEN);
			}
			
			LoginAndRegisterAction.login();
		}
		
		if (!(user.isEmailVerified || user.isMobileVerified)) {  //用户未激活
			
			if(checkAction.value()){
				
				renderText(InactiveUserCheck.TOKEN);
			}
			
			CheckAction.inactiveUser();
		}
	}
	
	/**
	 * 用户未实名认证拦截器
	 */
	@Before
	static void checkRealName(){

		RealNameCheck checkAction = getActionAnnotation(RealNameCheck.class);
		
		Cache.set("referUrl", "invest");
		
		if(checkAction == null){  //不拦截
			
			return;
		}
		
		User user = User.currUser();
		
		if(user == null){  //未登录
			
			if(checkAction.value()){
				renderText(LoginCheck.TOKEN);
			}
			
			LoginAndRegisterAction.login();
		}
		
		if (StringUtils.isBlank(user.realityName)) {  //未实名认证
			
			if(checkAction.value()){
				
				renderText(RealNameCheck.TOKEN);
			}
			
			BasicInformation.certification();
		}
	}
	
	
	/**
	 * 用户未完善资料拦截器
	 */
	@Before(only = {"front.bid.BidAction.applyNow","front.bid.BidAction.createBid"})
	static void checkBaseInfo(){
		
		User user = User.currUser();
		
		if(user == null){
			
			LoginAndRegisterAction.login();
		}
		
		user.id = User.currUser().id; // 及时在抓取一次	
		
		if(user.isAddBaseInfo){
			
			return ;
		}
		
		String _status = params.get("status");
		String _productId = params.get("productId");
		
		if(StringUtils.isBlank(_productId) || StringUtils.isBlank(_status))
			render(Constants.ERROR_PAGE_PATH_FRONT); 
		
		long productId = 0;
		int status = 0;
		
		/* 无法转换，跳转至首页 */
		try {
			productId = Long.parseLong(_productId);
			status = Integer.parseInt(_status);
		} catch (Exception e) {
			BidAction.index(productId, 0, 1);
		}
		
		/* 如果是合作机构标及其未启动 */
		Boolean falg = Product.isAgency(productId);
		
		if(null == falg || falg){
			BidAction.index(productId, 0, 1);
		}

		switch (status) {
		/* 首页申请 */
		case Constants.APPLY_NOW_INDEX:
			BidAction.index(productId, Constants.NOT_ADDBASEINFO, status);
			break;
		/* 详情申请 */	
		case Constants.APPLY_NOW_DETAIL:
			BidAction.detail(productId, Constants.NOT_ADDBASEINFO, status);
			break;
		default:
			BidAction.index(productId, 0, 1);
			break;
		}
	}
}
