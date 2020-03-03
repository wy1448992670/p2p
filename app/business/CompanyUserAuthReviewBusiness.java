package business;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;

import bean.AuthReviewBean;
import constants.AuthReviewEnum;
import constants.UserTypeEnum;
import models.t_user_auth_review;
import play.Logger;
import utils.ErrorInfo;

public class CompanyUserAuthReviewBusiness implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 校验企业认证请求参数
	 * @param companyName
	 * @param creditCode
	 * @param bankName
	 * @param bankNo
	 * @param realName
	 * @param userType
	 * @param error
	 */
	public static void validParams(String companyName,String creditCode,String bankName,
			String bankNo, String realName, UserTypeEnum userType,ErrorInfo error) {
		if(error == null) {
			error = new ErrorInfo();
		}
		if(StringUtils.isBlank(companyName)){
			error.code = -1;
			error.msg = "请输入企业名称！";
		}
		if(companyName.length() > 30){
			error.code = -1;
			error.msg = "企业名称长度需在0~30之间！";
		}
		if(StringUtils.isBlank(creditCode)){
			error.code = -1;
			error.msg = "请输入统一社会信用代码！";
		}
		if(creditCode.length() > 25){
			error.code = -1;
			error.msg = "统一社会信用代码长度需在0~25之间！";
		}
		if(StringUtils.isBlank(bankName)){
			error.code = -1;
			error.msg = "请输入开户行！";
		}
		if(bankName.length() > 30){
			error.code = -1;
			error.msg = "开户行长度需在0~30之间！";
		}
		if(StringUtils.isBlank(bankNo)){
			error.code = -1;
			error.msg = "请输入企业对公账户！";
		}
		if(bankNo.length() > 25){
			error.code = -1;
			error.msg = UserTypeEnum.COMPANY.equals(userType)? "企业对公账户长度需在0~25之间！": "经营者银行账户长度需在0~25之间！";
		}
		if(!StringUtils.isNumeric(bankNo)){
			error.code = -1;
			error.msg = UserTypeEnum.COMPANY.equals(userType)? "企业对公账户输入错误！": "经营者银行账户输入错误！";
		}
		if(UserTypeEnum.INDIVIDUAL.equals(userType)) {
			if(StringUtils.isBlank(realName)){
				error.code = -1;
				error.msg = "请输入经营者银行账户真实姓名！";
			}
			if(realName.length() > 30){
				error.code = -1;
				error.msg = "经营者银行账户真实姓名长度需在0~30之间！";
			}
		}
	}
	
	/**
	 * 获取用户未重置过的申请记录
	 * @param userId
	 * @return
	 */
	public static t_user_auth_review findNotResetedAuthReviewRecord(long userId){
		return t_user_auth_review.find("user_id = ? and status != ? order by create_time desc limit 1", userId,AuthReviewEnum.RESET.getCode()).first();
	}
	
	/**
	 * 复制属性
	 * @param from
	 * @param to
	 */
	public static void copyProperties(t_user_auth_review from,AuthReviewBean to) {
		to.setId(from.id);
		to.setUser_id(from.user_id);
		to.setReal_name(from.real_name);
		to.setCompany_name(from.company_name);
		to.setCredit_code(from.credit_code);
		to.setBank_name(from.bank_name);
		to.setBank_no(from.bank_no);
		to.setStatus(from.status);
		to.setCreate_time(from.create_time);
		to.setUpdate_time(from.update_time);
		to.setUpdate_by(from.update_by);
	}
	
	public static String addOrUpdateAuthReview(String companyName,String creditCode,String bankName,
			String bankNo,String realName,int authType, Integer proviceId, Integer cityId,long userId,ErrorInfo errorInfo) {
		try {
			UserTypeEnum userType = authType == 2? UserTypeEnum.COMPANY: UserTypeEnum.INDIVIDUAL;
			validParams(companyName,creditCode,bankName,bankNo,realName,userType,errorInfo);
			if(errorInfo.code < 0) {
				return JSONObject.toJSONString(errorInfo);
			}
			User user = new User();
			user.id = userId;
			t_user_auth_review entity = CompanyUserAuthReviewBusiness.findNotResetedAuthReviewRecord(userId);
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
			//回写用户类型 和城市ID
			if(cityId != null && cityId != 0 ) {
				User.updateUserType(user.id, userType.getCode(), proviceId,cityId);
			} else {
				User.updateUserType(user.id, userType.getCode());
			}
			
			user.user_type = userType.getCode();
			User.setCurrUser(user);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("提交“企业认证”或“个体工商户认证”申请-系统异常！", e);
			errorInfo.code = -2;
			errorInfo.msg = "企业认证系统异常！";
			return JSONObject.toJSONString(errorInfo); 
		}
		
		errorInfo.code = 0;
		errorInfo.msg = "认证申请提交成功！";
		return JSONObject.toJSONString(errorInfo); 
	}
}
