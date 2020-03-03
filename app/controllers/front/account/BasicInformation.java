package controllers.front.account;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.shove.Xml;

import annotation.AddCheck;
import annotation.Check;
import bean.AuthReviewBean;
import business.AuthReq;
import business.AuthResp;
import business.BackstageSet;
import business.CompanyUserAuthReviewBusiness;
import business.DictBanksDate;
import business.News;
import business.Score;
import business.SecretQuestion;
import business.TemplateEmail;
import business.User;
import business.UserActions;
import business.UserBankAccounts;
import business.UserCitys;
import business.Vip;
import constants.Constants;
import constants.Templets;
import controllers.BaseController;
import controllers.DSecurity;
import controllers.front.invest.InvestAction;
import controllers.interceptor.AccountInterceptor;
import models.t_dict_ad_citys;
import models.t_dict_ad_provinces;
import models.t_dict_cars;
import models.t_dict_educations;
import models.t_dict_houses;
import models.t_dict_maritals;
import models.t_user_auth_review;
import models.t_user_city;
import models.t_user_vip_records;
import models.t_users;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import play.Logger;
import play.cache.Cache;
import play.libs.Codec;
import play.mvc.With;
import utils.AuthenticationUtil;
import utils.Base64;
import utils.CharUtil;
import utils.CompressStringUtil;
import utils.DateUtil;
import utils.EmailUtil;
import utils.ErrorInfo;
import utils.IDCardValidate;
import utils.IDNumberUtil;
import utils.RegexUtils;
import utils.Security;

@With({AccountInterceptor.class,DSecurity.class})
public class BasicInformation extends BaseController {

	//-------------------------------基本资料-------------------------
	
	/**
	 * 基本信息
	 */
	public static void basicInformation(){
		User user = User.currUser();
		user.id = User.currUser().id;
		String randomId = Codec.UUID(); 
		
		ErrorInfo error = new ErrorInfo();
		
		if(error.code < 0) {
			render(user, Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		List<t_dict_cars> cars = (List<t_dict_cars>) Cache.get("cars");
		List<t_dict_ad_provinces> provinces = (List<t_dict_ad_provinces>) Cache.get("provinces");
		List<t_dict_educations> educations = (List<t_dict_educations>) Cache.get("educations");
		List<t_dict_houses> houses = (List<t_dict_houses>) Cache.get("houses");
		List<t_dict_maritals> maritals = (List<t_dict_maritals>) Cache.get("maritals");
		t_user_auth_review entity = CompanyUserAuthReviewBusiness.findNotResetedAuthReviewRecord(user.id);
		AuthReviewBean authReview = null;
		if(entity != null) {
			authReview = new AuthReviewBean();
			CompanyUserAuthReviewBusiness.copyProperties(entity, authReview);
		}
		user.authReview = authReview;
		
		List<t_dict_ad_citys> cityList = null;
		if(flash.get("province") != null) {
			cityList = User.queryCity(Integer.parseInt(flash.get("province")));
		}else {
			cityList = User.queryCity(user.provinceId);
		}
		
		List<t_user_vip_records> vipRecords = Vip.queryVipRecord(user.id, error);
		
		if(error.code < 0) {
			render(user, Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String content = News.queryContent(Constants.NewsTypeId.VIP_AGREEMENT, error);
		boolean ipsEnable = constants.Constants.IPS_ENABLE;
		boolean checkMsgCode = Constants.CHECK_PIC_CODE;
		
		renderArgs.put("childId", "child_30");
		renderArgs.put("labId", "lab_6");
 		render(user,cars,provinces,educations,houses,maritals,cityList,vipRecords,backstageSet,content,ipsEnable,randomId,checkMsgCode);
	}
	
	/**
	 * 根据省获得市联动
	 */
	public static void getCity(long provinceId){
		List<t_dict_ad_citys> cityList = User.queryCity(provinceId);
		JSONArray json = JSONArray.fromObject(cityList);
//		json.put("cityList", cityList);
		renderJSON(json);
	}
	
	/**
	 * 保存基本信息
	 */
	public static void saveInformation( int sex, int age, int city, int province,int education, int marital, int car, int house){
		ErrorInfo error = new ErrorInfo();
		
		String realityName = params.get("realityName");
		
		String idNumber = params.get("idNumber");
		
		flash.put("sex", sex);
 		flash.put("age", age);
		flash.put("city", city);
		flash.put("province", province);
		flash.put("education", education);
		flash.put("marital", marital);
		flash.put("car", car);
		flash.put("house", house);
		flash.put("realityName", realityName);
		flash.put("idNumber", idNumber);
		
		User newUser = new User();
		newUser.id = User.currUser().id;
		if(StringUtils.isBlank(newUser.realityName) || StringUtils.isBlank(newUser.idNumber)) {
			if(StringUtils.isBlank(realityName)){
				
				flash.error("真实姓名不能为空！");
				
				basicInformation();
				
			}
			
			if(StringUtils.isBlank(idNumber)){
				flash.error("身份证号码不能为空！");
				
				basicInformation();
			}
			
			if (!CharUtil.isChinese(realityName)) {
				flash.error("真实姓名必须是中文！");
				basicInformation();
			}
			
			if(!"".equals(IDCardValidate.chekIdCard(0, idNumber))) {
				flash.error("请输入正确的身份证号！");
				basicInformation();
			}
			
			if(!IDNumberUtil.check18(idNumber)){
				flash.error("未满18周岁");
				basicInformation();
			}
		}
		
		newUser.setSex(sex);
		newUser.age = age;
		newUser.cityId = city;
		newUser.educationId = education;
		newUser.maritalId = marital;
		newUser.carId = car;
		newUser.houseId = house;
		
		if(StringUtils.isBlank(newUser.idNumber)){
			User.isIDNumberExist(idNumber, idNumber, newUser.financeType, error);
			
			if(error.code < 0) {
				flash.error("此身份证已开户，请重新输入！");
				basicInformation();
			}
		}
		boolean flag = false;
		
		if(StringUtils.isBlank(newUser.realityName) || StringUtils.isBlank(newUser.idNumber)) {
			
			newUser.realityName = realityName;
			newUser.idNumber=idNumber;
			
			String refID = Codec.UUID();
			new AuthReq().create(refID, User.currUser().id, 1);
			//实名认证
			String res = AuthenticationUtil.requestForIdNo(realityName, idNumber, error);
			
			if(error.code < 0) {
				flash.error(error.msg);
				
				//添加错误记录
				UserActions.insertAction(newUser.id, 1,error.msg, error);
				
				basicInformation();
			}
			
			Map<String, Object> result = new HashMap<String, Object>();
			if (StringUtils.isNotBlank(res)) {
				result = Xml.extractSimpleXMLResultMap(res);
				new AuthResp().create(refID, Long.parseLong((String)result.get("status")), (String)result.get("errorCode"), (String)result.get("errorMessage"));
			}
			if ("1".equals((String)result.get("status"))){
				String returnVal = (String)result.get("returnValue");
				String resXml = CompressStringUtil.decompress(new Base64().decode(returnVal));
				Logger.info(resXml);
				Map<String, Object> tt = AuthenticationUtil.extractMultiXMLResult(resXml, 0);
				if (!"1".equals((String)tt.get("result"))){
					flash.error("姓名与身份证号码不一致");
					
					//添加错误记录
					UserActions.insertAction(newUser.id, 1,"姓名与身份证号码不一致", error);
					
					basicInformation();
				}
			}else{
				flash.error("身份认证系统出错");
				//添加错误记录
				UserActions.insertAction(newUser.id, 1,"身份认证系统出错", error);
				
				basicInformation();
			}
			flag = true;
		}
		
		newUser.edit(error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			
			basicInformation();
		}else{
			if(flag){
				//实名认证送积分
				try {
					Score.sendScore(newUser.id,Score.AUTH ,newUser.realityName, error);
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 实名成功后添加省市信息
				try {
					//实名认证后添加地址
					//截取身份证前6位
					String province_id = idNumber.substring(0, 2);
					String city_id = idNumber.substring(0, 4);
					province_id = province_id + "0000"; //补全区 ，6位数
					city_id = city_id + "00"; //补全区 ，6位数
					
					t_user_city userCity = new t_user_city();
					userCity.city_id = city_id;
					userCity.province_id = province_id;
					userCity.user_id = newUser.id;
					UserCitys.addUserCity(error, userCity);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		flash.success(error.msg);

		basicInformation();
	}
	
	/**
	 * 弹出重置安保问题的页面
	 */
	public static void setSafeQuestionModify(){
		
		render();
	}
	
	/**
	 * vip详情
	 */
	public static void vipDetail(){
		BackstageSet options = BackstageSet.getCurrentBackstageSet();
		
		JSONObject json = new JSONObject();
		json.put("test", options);

		renderJSON(options);
	}
	
	/**
	 * 设置安全问题
	 */
	public static void setSafeQuestion(){
		User user = User.currUser();
		Logger.info("设置安全问题："+user.isSecretSet);
		List<SecretQuestion> questions = SecretQuestion.queryUserQuestion();
		renderArgs.put("childId", "child_32");
		renderArgs.put("labId", "lab_6");
		render(user,questions);
	}
	
	/**
	 * 校验安全问题
	 */
	@AddCheck(Constants.IS_AJAX)
	public static void verifySafeQuestion(String questionName1, String questionName2, 
			String questionName3) {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		String randomId = Codec.UUID();
		user.verifySafeQuestion(questionName1, questionName2, questionName3, error);
		
		JSONObject json = new JSONObject();
		 
		json.put("randomId", randomId);
		json.put("encryString", flash.get("encryString"));
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 保存安全问题
	 */
	@Check(Constants.VERIFY_SAFE_QUESTION)
	public static void saveSafeQuestion(String encryString, long secretQuestion1, 
			long secretQuestion2, long secretQuestion3, String answer1, 
			String answer2, String answer3){
		if( secretQuestion1 == 0 || 
			secretQuestion2 == 0 || 
			secretQuestion3 == 0 ||
			StringUtils.isBlank(answer1) ||
			StringUtils.isBlank(answer2) ||
			StringUtils.isBlank(answer3) ||
			secretQuestion1 == secretQuestion2 ||
			secretQuestion1 == secretQuestion3 ||
			secretQuestion2 == secretQuestion3 ||
			answer1.length() > 50 ||
			answer2.length() > 50 ||
			answer3.length() > 50 
		
		){
			flash.error("答案不能为空，且长度需在1~50之间!");
			
			setSafeQuestion();
		}
		
		User user = new User();
		user.id = User.currUser().id;
		ErrorInfo error = new ErrorInfo();
		
		user.secretQuestionId1 = secretQuestion1;
		user.secretQuestionId2 = secretQuestion2;
		user.secretQuestionId3 = secretQuestion3;
		user.answer1 = answer1;
		user.answer2 = answer2;
		user.answer3 = answer3;
		
		user.updateSecretQuestion(true, error);
		
		flash.error(error.msg);
		
		String fromPage = params.get("fromPage");
		
		if (StringUtils.isBlank(fromPage)) {
			setSafeQuestion();
		}
		
		if (fromPage.equals("modifyEmail")) {
			modifyEmail();
		}
		
		if (fromPage.equals("modifyPassword")) {
			modifyPassword();
		}
		
		if (fromPage.equals("modifyMobile")) {
			modifyMobile();
		}
		
		setSafeQuestion();
	}
	
	//得到安全问题
	public static void getSafeQuestion(){
		render();
	}
	
	/**
	 * 通过邮箱重置安全问题
	 */
	public static void resetSafeQuestion(){
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		TemplateEmail tEmail = new TemplateEmail();
		tEmail.id = 4;

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String sign = Security.addSign(user.id, Constants.SECRET_QUESTION);
		String url = Constants.RESET_QUESTION_EMAIL + sign;

		String content = tEmail.content;

		content = content.replace("<p","<div");
		content = content.replace("</p>","</div>");
		content = content.replace(Constants.EMAIL_NAME, user.name);
		content = content.replace(Constants.EMAIL_LOGIN, "<a href = "+Constants.LOGIN+">登录</a>");
		content = content.replace(Constants.EMAIL_TELEPHONE, backstageSet.companyTelephone);
		content = content.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
		content = content.replace(Constants.EMAIL_URL, "<a href = "+url+">"+url+"</a>");
		content = content.replace(Constants.EMAIL_TIME, DateUtil.dateToString(new Date()));

		System.out.println(content);
		
		TemplateEmail.sendEmail(0, user.email, tEmail.title, content, error);

		if (error.code < 0) {
			flash.error(error.msg);
			resetSafeQuestion();
		}
		
//		EmailUtil.emailResetSecretQuestion(user.name, user.email, error);
//		String emailUrl = EmailUtil.emailUrl(user.email);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		json.put("emailUrl", EmailUtil.emailUrl(user.email));
		
		renderJSON(json);
	}
	
	/**
	 * 重置安全问题页面
	 */
	public static void resetQuestion(String sign){
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(sign, Constants.SECRET_QUESTION, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			LoginAndRegisterAction.login();
		}
		
		String name = User.queryUserNameById(id, error);
		
		List<SecretQuestion> questions = SecretQuestion.queryUserQuestion();
		
		render(loginOrRegister, name, sign, questions);
	}
		
	/**
	 * 通过邮件重置后，保存安全问题答案
	 */
	public static void saveSafeQuestionByEmail(String sign, long secretQuestion1, 
			long secretQuestion2, long secretQuestion3, String answer1, 
			String answer2, String answer3){
		ErrorInfo error = new ErrorInfo();
		
		if( secretQuestion1 == 0 || 
			secretQuestion2 == 0 || 
			secretQuestion3 == 0 ||
			StringUtils.isBlank(answer1) ||
			StringUtils.isBlank(answer2) ||
			StringUtils.isBlank(answer3) ||
			secretQuestion1 == secretQuestion2 ||
			secretQuestion1 == secretQuestion3 ||
			secretQuestion2 == secretQuestion3 ||
			answer1.length() > 50 ||
			answer2.length() > 50 ||
			answer3.length() > 50  
			){
			error.code = -1;
			error.msg = "请填写正确的问题和答案!";
			
			flash.error(error.msg);
			
			AccountHome.home();
		}
		
		long id = Security.checkSign(sign, Constants.SECRET_QUESTION, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			LoginAndRegisterAction.login();
		}
		
		User user = new User();
		user.id = id;
		
		user.secretQuestionId1 = secretQuestion1;
		user.secretQuestionId2 = secretQuestion2;
		user.secretQuestionId3 = secretQuestion3;
		user.answer1 = answer1;
		user.answer2 = answer2;
		user.answer3 = answer3;
		
		user.updateSecretQuestion(false, error);
		
		flash.error(error.msg);
		
		AccountHome.home();
	}
	
	/**
	 * 激活邮箱
	 */
	public static void activeEmail() {
		ErrorInfo error = new ErrorInfo();
		JSONArray json = new JSONArray();
		
		User user = User.currUser();
		
		if(user.isEmailVerified) {
			error.code = -1;
			error.msg = "你的邮箱已激活，无需再次激活";
		}
		
		TemplateEmail.activeEmail(user, error);
		
		json.add(error);
		json.add(EmailUtil.emailUrl(user.email));
		
		renderJSON(json);
	}
	
	/**
	 * 保存邮箱
	 */
	public static void saveEmail(String email) {
		ErrorInfo error = new ErrorInfo();
		User user = new User();
		user.id = User.currUser().id;
		JSONObject json = new JSONObject();
		/**
		 * 1.新邮箱和旧邮箱不相等时候，需要检查是否已经被使用。
		 * 2.同一个邮箱并且已经激活过了，无需再次激活。
		 * */
		if (email != null && !email.equals(user.email)) {
			user.isEmailExist(email, user.email, error);
			if (error.code != 0) {
				json.put("error", error);
				renderJSON(json);
			} else {
				user.email = email;
				user.editEmail(error);
				if (error.code < 0) {
					json.put("error", error);
					renderJSON(json);
				}
			}
		} else if (email != null && email.equals(user.email)) {
			if (user.isEmailVerified) {
				error.code = -1;
				error.msg = "你的邮箱已激活，无需再次激活";
				json.put("error", error);
				renderJSON(json);
			}
		}
		TemplateEmail.activeEmail(user, error);
		String emailUrl = EmailUtil.emailUrl(email);

		if (error.code < 0) {
			error.code = -11;
		}
		json.put("error", error);
		json.put("emailUrl", emailUrl);
		error.code = 0;
		renderJSON(json);
	}
	
	/**
	 * 修改邮箱
	 */
	public static void modifyEmail() {
		User user = User.currUser();
		
		/*if(!user.isSecretSet) {
			flash.error("您还没有设置安全问题，为了保障您的安全，请先设置安全问题");
			flash.put("fromPage", "modifyEmail");
			setSafeQuestion();
		}*/
		renderArgs.put("childId", "child_33");
		renderArgs.put("labId", "lab_6");
		render(user);
	}
	
	/**
	 * 绑定邮箱（回答安全问题）
	 */
	public static void bindEmail(String answer1, String answer2, String answer3) {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		user.verifySafeQuestion(answer1, answer2, answer3, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			modifyEmail();
		}
 		
		render(user);
	}
	
	/**
	 * 修改密码
	 */
	public static void modifyPassword(){
		User user = User.currUser();
		
		/*if(!user.isSecretSet) {
			flash.error("您还没有设置安全问题，为了保障您的安全，请先设置安全问题");
			flash.put("fromPage", "modifyPassword");
			setSafeQuestion();
		}*/
		renderArgs.put("childId", "child_31");
		renderArgs.put("labId", "lab_6");
		render(user);
	}
	
	/**
	 * 保存密码
	 * @param oldPassword
	 * @param newPassword1
	 * @param newPassword2
	 */
	//@Check({Constants.VERIFY_SAFE_QUESTION,Constants.IS_AJAX})
	public static void savePassword(String oldPassword, String newPassword1, 
			String newPassword2, String encryString){
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		if(oldPassword.equalsIgnoreCase(newPassword1)){
			JSONObject json = new JSONObject();
			json.put("error", "新密码与原密码一样，请重新输入");
			renderJSON(json);
		}
		
		user.editPassword(oldPassword, newPassword1, newPassword2, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 保存支付密码
	 * @param oldPassword
	 * @param newPassword1
	 * @param newPassword2
	 */
	//@Check(Constants.VERIFY_SAFE_QUESTION)
	public static void editPayPassword(String oldPayPassword, String newPayPassword1, 
			String newPayPassword2, String encryString){
		
		if(oldPayPassword.equalsIgnoreCase(newPayPassword1)){
			JSONObject json = new JSONObject();
			json.put("error", "新密码与原密码一样，请重新输入");
			renderJSON(json);
		}
		
		User user = new User();
		user.id = User.currUser().id;
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		user.editPayPassword(oldPayPassword, newPayPassword1, newPayPassword2, error);
		
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 添加支付密码
	 * @param oldPassword
	 * @param newPassword1
	 * @param newPassword2
	 */
	//@Check(Constants.VERIFY_SAFE_QUESTION)
	public static void savePayPassword(String newPayPassword1, String newPayPassword2, 
			String encryString){
		User user = new User();
		user.id = User.currUser().id;
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		user.addPayPassword(true, newPayPassword1, newPayPassword2, error);
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 重置支付密码
	 * @param randomID 系统生成码
	 * @param code 输入图片验证码
	 * @param teleCode  手机验证码
	 * @param newPayPassword1
	 * @param newPayPassword2
	 */
	@Check(Constants.VERIFY_SAFE_QUESTION)
	public static void resetPayPassword(String randomID,String code,String teleCode,String newPayPassword1, 
			String newPayPassword2, String encryString) {
		User user = new User();
		user.id = User.currUser().id;
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
        if (Constants.CHECK_PIC_CODE) {
			if (StringUtils.isBlank(code)) {
				
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
	        if (!codec.equalsIgnoreCase(code)) {
				
	        	error.code = -1;
	        	error.msg = "图形验证码错误";
	        	
	        	json.put("error", error);
	        	
	        	renderJSON(json);
			}
  		}
		
		if(Constants.CHECK_MSG_CODE) {
			String cCode = String.valueOf(Cache.get(user.mobile));
			
			if(cCode == null) {
				error.code = -1;
				error.msg = "验证码已失效，请重新点击发送验证码";
				
				return;
			}
			
			if(!teleCode.equals(cCode)) {
				error.code = -1;
				error.msg = "手机验证错误";
				
				return;
			}
		}
		
		user.addPayPassword(false, newPayPassword1, newPayPassword2, error);
		
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 修改手机
	 */
	public static void modifyMobile(){
        User user = User.currUser();
		
		/*if(!user.isSecretSet) {
			flash.error("您还没有设置安全问题，为了保障您的安全，请先设置安全问题");
			flash.put("fromPage", "modifyMobile");
			setSafeQuestion();
		}*/
		
		renderArgs.put("childId", "child_34");
		renderArgs.put("labId", "lab_6");
		render(user);
	}
	
	public static void addSafeBankCard(){
		
		User user = User.currUser();
		long userId = user.id;
		
		ErrorInfo error = new ErrorInfo();
		
		List<UserBankAccounts> userBanks = UserBankAccounts.queryUserAllBankAccount(userId);
		
		Map<String,String> bankCodeNameTable = DictBanksDate.bankCodeNameTable;
		Map<String,String> provinceCodeNameTable = DictBanksDate.provinceCodeNameTable;
		
		renderArgs.put("childId", "child_80");
		renderArgs.put("labId", "lab_6");
		
		render(user,userBanks,bankCodeNameTable,provinceCodeNameTable);
	}
	
	/**
	 * 实名认证页面跳转
	 */
	public static void certification(){
        User user = User.currUser();
        if (user.idNumber!=null&&!"".equals(user.idNumber)) {
        	user.idNumber = user.idNumber.substring(0, 6)+"**********"+user.idNumber.substring(14);
		}
        
        String val = params.get("investAmount");
		Logger.info(val+"---------------");
		
        renderArgs.put("childId", "child_27");
		renderArgs.put("labId", "lab_5");
		
		String referUrl = Cache.get("referUrl")+"";
		
		if(StringUtils.isNotBlank(referUrl)){
			
			Cache.delete("referUrl");
		}
		
		renderArgs.put("referUrl", referUrl);
		
		
		render(user);
	}
	
	/**
	 * 实名认证信息提交
	 */
	public static void doCertification(String realName, String idNumber) {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		flash.put("realName", realName);
		flash.put("idNumber", idNumber);

		if (StringUtils.isBlank(realName)) {
			flash.put("nameError", "真实姓名不能为空");
			certification();
		}

		if (StringUtils.isBlank(idNumber)) {
			flash.put("idNoError", "身份证不能为空");
			certification();
		}

		if (!CharUtil.isChinese(realName)) {
			flash.put("nameError", "真实姓名必须是中文");
			certification();
		}
		
		if(!"".equals(IDCardValidate.chekIdCard(0, idNumber))) {
			flash.put("idNoError", "请输入正确的身份证号");
			certification();
		}
		
		User.isIDNumberExist(idNumber, null,1, error);
		
		if(error.code < 0) {
			flash.put("idNoError", "此身份证已开户，请重新输入");
			certification();
		}
		
		user.updateCertification(realName, idNumber,user.id, error);

		if (error.code < 0) {
			flash.put(error.code == -1 ? "nameError" : "idNoError", error.msg);
		}
		
		String referUrl = params.get("referUrl");
		
		if("recharge".equals(referUrl)){
			
			FundsManage.recharge();
			
		}else if("invest".equals(referUrl)){
			
			InvestAction.investHome();
		}else{
			certification();
		}
		
	}
	
	/**
	 * 保存手机号码
	 * @param code
	 * @param mobile
	 */
	public static void saveMobile(String code, String mobile) {
		ErrorInfo error = new ErrorInfo();
		User user = new User();
		user.id = User.currUser().id;
		
		user.mobile = mobile;
		user.editMobile(code, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	} 
	
	/**
	 * 绑定手机
	 */
	public static void bindMobile(String answer1, String answer2, String answer3) {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		String randomId = Codec.UUID();
		boolean checkMsgCode = Constants.CHECK_PIC_CODE;
		
		/*user.verifySafeQuestion(answer1, answer2, answer3, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			modifyMobile();
		}*/
		renderArgs.put("childId", "child_34");
		renderArgs.put("labId", "lab_6");
		render(user, randomId, checkMsgCode);
	}
	
	/**
	 * 保存重置密码
	 */
	public static void savePayPasswordByEmail(String sign, String password, String confirmPassword) {
		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(sign, Constants.PASSWORD, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			LoginAndRegisterAction.login();
		}
		
		User user = new User();
		user.id = id;
		user.updatePayPasswordByEmail(password, confirmPassword, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			resetDelPassword(sign);
		}
		
		flash.error(error.msg);
		
		modifyPassword();
	}
	
	public static void resetDelPassword(String sign){
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(sign, Constants.PASSWORD, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			modifyPassword();
		}
		
		String name = User.queryUserNameById(id, error);
		
		render(loginOrRegister, name,sign);
	}

	/**
	 * 发送重置交易密码邮件
	 */
	public static void resetPayPasswordByEmail(String email) {
		ErrorInfo error = new ErrorInfo();

		flash.put("email", email);


		if (StringUtils.isBlank(email)) {
			flash.error("请输入邮箱地址");
			modifyPassword();
		}

		if (!RegexUtils.isEmail(email)) {
			flash.error("请输入正确的邮箱地址");
			modifyPassword();
		}

		

		User.isEmailExist(email, null, error);

		if (error.code != -2) {
			flash.error("对不起，该邮箱没有注册");
			modifyPassword();
		}
		
		t_users user = User.queryUserByEmail(email, error);

		if (error.code < 0) {
			flash.error(error.msg);
			modifyPassword();
		}
		
		TemplateEmail tEmail = new TemplateEmail();
		tEmail.id = Templets.E_FIND_DELPWD_EMAL;

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String sign = Security.addSign(user.id, Constants.PASSWORD);
		String url = Constants.RESET_PAY_PASSWORD_EMAIL + sign;

		String content = tEmail.content;

		content = content.replace("<p","<div");
		content = content.replace("</p>","</div>");
		content = content.replace(Constants.EMAIL_NAME, user.name);
		content = content.replace(Constants.EMAIL_LOGIN, "<a href = "+Constants.LOGIN+">登录</a>");
		content = content.replace(Constants.EMAIL_TELEPHONE, backstageSet.companyTelephone);
		content = content.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
		content = content.replace(Constants.EMAIL_URL, "<a href = "+url+">"+url+"</a>");
		content = content.replace(Constants.EMAIL_TIME, DateUtil.dateToString(new Date()));

		TemplateEmail.sendEmail(0, email, tEmail.title, content, error);

		if (error.code < 0) {
			flash.error(error.msg);
			modifyPassword();
		}
		

		flash.put("email", "");
		flash.put("code", "");
		flash.error("邮件发送成功");
		flash.put("emailUrl", EmailUtil.emailUrl(email));
		modifyPassword();
	}
	
}
