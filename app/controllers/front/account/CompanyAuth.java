package controllers.front.account;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;

import bean.AuthReviewBean;
import business.CompanyUserAuthReviewBusiness;
import business.User;
import constants.AuthReviewEnum;
import constants.UserTypeEnum;
import controllers.BaseController;
import controllers.SubmitRepeat;
import controllers.interceptor.AccountInterceptor;
import models.t_user_auth_review;
import play.Logger;
import play.mvc.With;
import utils.ErrorInfo;

@With({ AccountInterceptor.class, SubmitRepeat.class, })
public class CompanyAuth extends BaseController {
	
	/**
	 * 企业用户认证页面
	 */
	public static void auth() {
		renderArgs.put("childId", "child_52");
        renderArgs.put("labId", "lab_9");
        
		//避免缓存中的数据与数据库一致
		User user = new User();
		user.id = User.currUser().id;
		t_user_auth_review entity = CompanyUserAuthReviewBusiness.findNotResetedAuthReviewRecord(user.id);
		if(entity != null) {
			AuthReviewBean authReview = new AuthReviewBean();
			CompanyUserAuthReviewBusiness.copyProperties(entity, authReview);
			user.authReview = authReview;
		}
		
		render(user);
	}
	
	/**
	 * 个体工商户认证页面
	 */
	public static void individualAuth() {
		renderArgs.put("childId", "child_53");
        renderArgs.put("labId", "lab_10");
        
		//避免缓存中的数据与数据库一致
		User user = new User();
		user.id = User.currUser().id;
		t_user_auth_review entity = CompanyUserAuthReviewBusiness.findNotResetedAuthReviewRecord(user.id);
		if(entity != null) {
			AuthReviewBean authReview = new AuthReviewBean();
			CompanyUserAuthReviewBusiness.copyProperties(entity, authReview);
			user.authReview = authReview;
		}
		
		render(user);
	}
	
	/**
	 * 提交“企业认证”或“个体工商户认证”申请
	 * @param companyName
	 * @param creditCode
	 * @param bankName
	 * @param bankNo
	 * @param authType  0:：企业认证      1：个体工商户认证
	 */
	public static void addOrUpdateAuthReview(String companyName,String creditCode,String bankName,
			String bankNo,String realName,int authType ) {
		ErrorInfo errorInfo = new ErrorInfo();
		try {
			UserTypeEnum userType = authType == 0? UserTypeEnum.COMPANY: UserTypeEnum.INDIVIDUAL;
			CompanyUserAuthReviewBusiness.validParams(companyName,creditCode,bankName,bankNo,realName,userType,errorInfo);
			if(errorInfo.code < 0) {
				renderJSON(JSONObject.toJSON(errorInfo));
			}
			User user = User.currUser();
			t_user_auth_review entity = CompanyUserAuthReviewBusiness.findNotResetedAuthReviewRecord(user.id);
			if(entity == null) {
				entity = new t_user_auth_review();
				entity.user_id = user.id;
				entity.create_time = new Date();
			}
			entity.real_name = authType == 0? null: realName;
			entity.company_name = companyName;
			entity.credit_code = creditCode;
			entity.bank_name = bankName;
			entity.bank_no = bankNo;
			entity.status = AuthReviewEnum.UNREVIEW.getCode();
			entity = entity.save();
			//回写用户类型  
			User.updateUserType(user.id, userType.getCode());
			
			user.user_type = userType.getCode();
			User.setCurrUser(user);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("controllers.front.account.CompanyAuth.addOrUpdateAuthReview()：提交“企业认证”或“个体工商户认证”申请-系统异常！", e);
			errorInfo.code = -2;
			errorInfo.msg = "系统异常！";
			renderJSON(JSONObject.toJSON(errorInfo));
		}
		
		errorInfo.code = 0;
		errorInfo.msg = "认证申请提交成功！";
		renderJSON(JSONObject.toJSON(errorInfo));
	}
}
