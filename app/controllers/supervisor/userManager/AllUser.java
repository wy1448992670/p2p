package controllers.supervisor.userManager;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.StringUtil;

import play.Logger;
import play.cache.Cache;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;

import com.shove.Convert;

import constants.AuthReviewEnum;
import constants.Constants;
import constants.PactTypeEnum;
import constants.SupervisorEvent;
import constants.Templets;
import constants.UserTypeEnum;
import controllers.front.account.AccountHome;
import controllers.supervisor.SupervisorController;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import business.BackstageSet;
import business.CompanyUserAuthReviewBusiness;
import business.DataSafety;
import business.DealDetail;
import business.DictBanksDate;
import business.Invest;
import business.NewPact;
import business.Pact;
import business.StationLetter;
import business.Supervisor;
import business.TemplateEmail;
import business.User;
import business.UserAuthReview;
import business.UserBankAccounts;
import business.UserCitys;
import models.t_dict_ad_citys;
import models.t_dict_ad_provinces;
import models.t_dict_banks_col;
import models.t_dict_cars;
import models.t_dict_educations;
import models.t_dict_houses;
import models.t_dict_maritals;
import models.t_new_city;
import models.t_new_province;
import models.t_user_auth_review;
import models.t_user_bank_accounts;
import models.t_user_city;
import models.t_users;
import models.v_user_info;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.NumberUtil;
import utils.PageBean;
import utils.RegexUtils;
import utils.SMSUtil;
import utils.Security;
import utils.baofoo.business.ABolishBind;
import utils.ymsms.SMS;

/**
 * 
 * 类名:AllUser 功能:全部会员列表
 */

public class AllUser extends SupervisorController {

	/**
	 * 所有会员列表
	 */
	public static void allUser() {
		String name = params.get("name");
		String beginTime = params.get("beginTime");
		String endTime = params.get("endTime");
		String beginLoginTime = params.get("beginLoginTime");
		String endLoginTime = params.get("endLoginTime");
		String orderType = params.get("orderType");
		String key = params.get("key");
		String curPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String isBank = params.get("isBank");
		String isRealname = params.get("isRealname");
		String recommend_user_name = params.get("recommend_user_name");
		String risk_result = params.get("risk_result");
		String is_first_invest = params.get("is_first_invest");
		String user_type = params.get("user_type");
		String finance_type = params.get("finance_type");

		int isExport = Convert.strToInt(params.get("isExport"), 0);

		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_info> page = User.queryUserBySupervisorV2(name, null, beginTime, endTime, beginLoginTime,
				endLoginTime, key, orderType, curPage, isExport == Constants.IS_EXPORT ? "999999" : pageSize, error,
				isBank, isRealname, recommend_user_name, risk_result, is_first_invest,user_type,finance_type);

		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			List<v_user_info> list = page.page;

			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);

			for (Object obj : arrList) {
				JSONObject bill = (JSONObject) obj;

				bill.put("name", bill.getString("name"));
				bill.put("reality_name", bill.getString("reality_name"));
				bill.put("mobile", bill.getString("mobile"));
				bill.put("recommend_user_name", bill.getString("recommend_user_name"));
				bill.put("register_time", bill.getString("register_time"));
				bill.put("invest_amount", bill.getString("invest_amount"));
				bill.put("bank", bill.getInt("bank") > 0 ? "是" : "否");
				bill.put("user_amount", bill.getString("user_amount"));

			}

			// 用户名、真实姓名、手机号

			File file = ExcelUtils.export("用户列表", arrList,
					new String[] {"用户名", "真实姓名", "手机号", "推荐人用户名","推荐人手机号", "注册时间","累计投资金额","是否绑卡","累计投标数量","可用余额"},
					new String[] {"name", "reality_name", "mobile", "recommend_user_name","recommend_user_mobile", "register_time","invest_amount","bank","invest_count","user_amount"});
			
			renderBinary(file, "用户列表" + ".xls");
		}

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(page);
	}

	/**
	 * 详情
	 *
	 * @param id
	 */
	@SuppressWarnings("unchecked")
	public static void detail(String sign) {
		ErrorInfo error = new ErrorInfo();

		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if (error.code < 0) {
			flash.error(error.msg);

			allUser();
		}

		User user = new User();
		user.id = id;
		user.hasBids = User.isHasBids(id);
		int userViewType=0;
		if(user.user_type==0){
			String paramUserType=params.get("userType");
			if(NumberUtil.isNumericInt(paramUserType)){
				userViewType=Integer.parseInt(paramUserType);
			}else{
				userViewType=1;
			}
		}else{
			userViewType=user.user_type;
		}
		t_user_bank_accounts banks = UserBankAccounts.queryById(user.id);
		// 多张银行卡实现
		List<t_user_bank_accounts> banks_ = UserBankAccounts.queryMoreById(user.id);
		//double profit = Invest.getProfit(id);

		//加载用户户籍所在省市数据
		t_user_city userCity=t_user_city.find("user_id=? ", id).first();
		List<t_new_province> newProvinces =User.queryAllNewProvince();
		List<t_new_city> newCitys = null;
		if(userCity != null){
			newCitys = User.queryNewCity(userCity.province_id);
		}else{
			newCitys = new ArrayList<t_new_city>();
		}

		//加载用户居住地所在省市数据  user.cityId
		List<t_dict_ad_citys> citys = new ArrayList<t_dict_ad_citys>();
		t_dict_ad_citys adCity = t_dict_ad_citys.findById(Long.valueOf(user.cityId));
		if(adCity != null) {
			citys = User.queryCity(adCity.province_id);
		}
		List<t_dict_ad_provinces> provinces = (List<t_dict_ad_provinces>) Cache.get("provinces");
		
		//--------------------------------------------enums-----------------------------------------------
		Map<String, String> bankCodeNameTable = DictBanksDate.bankCodeNameTable;
		List<t_dict_cars> cars = (List<t_dict_cars>) Cache.get("cars");
		List<t_dict_educations> educations = (List<t_dict_educations>) Cache.get("educations");
		List<t_dict_houses> houses = (List<t_dict_houses>) Cache.get("houses");
		List<t_dict_maritals> maritals = (List<t_dict_maritals>) Cache.get("maritals");
		
		render(user,banks,banks_,userViewType
				,userCity,newProvinces,newCitys
				,adCity,provinces,citys
				,bankCodeNameTable,cars,educations,houses,maritals);

	}

	/**
	 * 解绑
	 *
	 * @param id
	 */
	public static void deleteUserBank(long userId, long accountId) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();

		t_user_bank_accounts bank_accounts = t_user_bank_accounts.findById(accountId);
		if (StringUtils.isNotBlank(bank_accounts.protocol_no)) {
			try {
				ABolishBind.execute(bank_accounts.protocol_no, userId);
				UserBankAccounts.deleteUserBankAccount(userId, accountId, error);
			} catch (Exception e) {
				json.put("error", e.getMessage());
				renderJSON(json);
			}
		} else {
			UserBankAccounts.deleteUserBankAccount(userId, accountId, error);
		}

		if (error.code < 0) {
			json.put("error", error);
			renderJSON(json);
		}

		json.put("error", error);
		renderJSON(json);

	}

	/**
	 * 解绑
	 *
	 * @param id
	 */
	public static void modifyMobile(long userId, String mobile, int flag) {
		if (flag == 1) {
			String reality_name = params.get("reality_name");
			String id_number = params.get("id_number");
			modifyRealityName(userId, reality_name, id_number,0);
	/*		try {
				if (User.checkPact2(userId, PactTypeEnum.QZSQ.getCode())) {
					// 修改手机号电子签章自动签署授权协议(投资人，出借人)
					NewPact.createPact(userId, null, PactTypeEnum.QZSQ.getCode(), 2);
					Logger.info(userId + "=====修改手机号电子签章自动签署授权协议(投资人，出借人)完成========");
					//  修改手机号电子签章自动签署授权协议(上标成功，借款人)
					NewPact.createPact(userId, null, PactTypeEnum.QZSQ.getCode(), 2);
					Logger.info(userId + "=====修改手机号电子签章自动签署授权协议(上标成功，借款人)完成========");
				}
				if (User.checkPact2(userId, PactTypeEnum.CJFWXY.getCode())) {
					//  修改手机号出借人服务协议（投资成功，出借人）
					NewPact.createPact(userId, null, PactTypeEnum.CJFWXY.getCode(), 2);
					Logger.info(userId + "=====修改手机号出借人服务协议（投资成功，出借人）完成========");
					//  修改手机号借款人服务协议(上标成功，借款人)
					NewPact.createPact(userId, null, PactTypeEnum.JKFWXY.getCode(), 2);
					Logger.info(userId + "=====修改手机号借款人服务协议(上标成功，借款人)完成========");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}*/
			return;
		}
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		User.updateMobile1(userId, mobile, error);
		if (error.code < 0) {
			json.put("error", error);
			renderJSON(json);
		}

		json.put("error", error);
	/*	try {
			if (User.checkPact2(userId, PactTypeEnum.QZSQ.getCode())) {
				//  修改手机号电子签章自动签署授权协议(投资人，出借人)
				NewPact.createPact(userId, null, PactTypeEnum.QZSQ.getCode(), 2);
				Logger.info(userId + "=====修改手机号电子签章自动签署授权协议(投资人，出借人)完成========");
				//  修改手机号电子签章自动签署授权协议(上标成功，借款人)
				NewPact.createPact(userId, null, PactTypeEnum.QZSQ.getCode(), 2);
				Logger.info(userId + "=====修改手机号电子签章自动签署授权协议(上标成功，借款人)完成========");
			}
			if (User.checkPact2(userId, PactTypeEnum.QZSQ.getCode())) {
				//  修改手机号出借人服务协议（投资成功，出借人）
				NewPact.createPact(userId, null, PactTypeEnum.CJFWXY.getCode(), 2);
				Logger.info(userId + "=====修改手机号出借人服务协议（投资成功，出借人）完成========");
				//  修改手机号借款人服务协议(上标成功，借款人)
				NewPact.createPact(userId, null, PactTypeEnum.JKFWXY.getCode(), 2);
				Logger.info(userId + "=====修改手机号借款人服务协议(上标成功，借款人)完成========");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		renderJSON(json);

	}

	/**
	 * 修改实名
	 *
	 * @param id
	 */
	public static void modifyRealityName(long userId, String reality_name, String id_number,int userType) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		t_users user = t_users.findById(userId);
		if(user == null) {
			error.code = -1;
			error.msg = "用户不存在或已删除！";
			renderJSON(json);
		}
		if((user.user_type == null || user.user_type < 1) && userType > 0) {
			User.updateUserType(userId, userType);
		}
		User.updateRealityName(userId, reality_name, id_number, error);
		if (error.code < 0) {
			json.put("error", error);
			renderJSON(json);
		}

		json.put("error", error);
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.LOCK_USER, user.name + "修改实名",
				error);
		renderJSON(json);

	}

	/**
	 * 站内信
	 */
	public static void stationLetter(String sign, String content, String title) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();

		if (content.length() > 1000) {
			error.code = -1;
			error.msg = "内容超出字数范围";
			json.put("error", error);
			renderJSON(json);
		}

		long receiverUserId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if (error.code < 0) {
			json.put("error", error);
			renderJSON(json);
		}

		StationLetter message = new StationLetter();

		message.senderSupervisorId = Supervisor.currSupervisor().id;
		message.receiverUserId = receiverUserId;
		message.title = title;
		message.content = content;

		message.sendToUserBySupervisor(error);

		json.put("error", error);

		renderJSON(json);
	}

	/**
	 * 邮件
	 */
	public static void email(String email, String content) {
		ErrorInfo error = new ErrorInfo();
		TemplateEmail.sendEmail(1, email, null, Templets.replaceAllHTML(content), error);

		JSONObject json = new JSONObject();
		json.put("error", error);

		renderJSON(json);
	}

	/**
	 * 发信息
	 *
	 * @param mobile
	 * @param content
	 */
	public static void sendMsg(String mobile, String content) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();

		if (StringUtils.isBlank(mobile)) {
			error.code = -1;
			error.msg = "请选择正确的手机号码!";
			json.put("error", error);
		}

		SMSUtil.sendSMS(mobile, content, error, SMS.SEND_TYPE_MARKETING);
		json.put("error", error);

		renderJSON(json);
	}

	/**
	 * 重置密码
	 */
	public static void resetPassword(String userName, String email) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		if (StringUtils.isBlank(userName) || StringUtils.isBlank(email)) {
			error.code = -1;
			error.msg = "参数传入有误";
			json.put("error", error);

			renderJSON(json);
		}

		User.isEmailExist(email, null, error);

		if (error.code != -2) {
			error.code = -1;
			error.msg = "对不起，该邮箱没有注册";
			json.put("error", error);

			renderJSON(json);
		}

		t_users user = User.queryUserByEmail(email, error);

		TemplateEmail tEmail = new TemplateEmail();
		tEmail.id = 3;

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String sign = Security.addSign(user.id, Constants.PASSWORD);
		String url = Constants.RESET_PASSWORD_EMAIL + sign;

		String content = tEmail.content;

		content = content.replace(Constants.EMAIL_NAME, user.name);
		content = content.replace(Constants.EMAIL_TELEPHONE, backstageSet.companyTelephone);
		content = content.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
		content = content.replace(Constants.EMAIL_URL, "<a href = " + url + ">点击此处重置密码</a>");
		content = content.replace(Constants.EMAIL_TIME, DateUtil.dateToString(new Date()));

		TemplateEmail.sendEmail(2, email, tEmail.title, content, error);

		json.put("error", error);

		renderJSON(json);
	}

	/**
	 * 编辑用户信息弹框
	 *
	 * @param sign
	 */
	public static void editUserInfoWin(String sign) {
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if (error.code < 0) {
			renderJSON(error);
		}

		Map<String, Object> user = User.queryIpsInfo(error, userId);

		if (error.code < 0) {
			renderJSON(error);
		}

		// 资金托管模式下，如果已成功开通自己托管账户，不允许修改
		if (user.containsKey("ips_acct_no") && user.get("ips_acct_no") != null
				&& StringUtils.isNotBlank(user.get("ips_acct_no").toString())) {

			error.code = -1;
			error.msg = "资金托管账户已开通成功，不能修改！";

			renderJSON(error);
		}

		render(user, sign);
	}

	/**
	 * 编辑用户信息
	 *
	 * @param sign
	 */
	public static void editUserInfo(String sign, String realityName, String idNumber, String email, String mobile) {
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if (error.code < 0) {
			renderJSON(error);
		}
		User user = new User();
		user.id = userId;
		
		User.updateIpsInfo(error, userId, realityName, idNumber, email, mobile,user.financeType);

		renderJSON(error);
	}
	
	/**
	 *  编辑用户信息
	 *
	 * @param sign
	 */
	public static void editUserBaseinfo() {
		
		long userId = Integer.parseInt(params.get("userId"));
//		int userType = Integer.parseInt(params.get("userType"));
		Integer sex = StringUtils.isNotBlank(params.get("sex"))?Integer.parseInt(params.get("sex")):0;
		String education= params.get("education");
		String car= params.get("car");
		String cityId= params.get("cityId");
		Long adCityId = StringUtils.isNotBlank(params.get("adCityId"))?Long.valueOf(params.get("adCityId")):0;
		String house = params.get("house");
		String marital = params.get("marital");
		Double creditAmount = StringUtils.isNotBlank(params.get("creditAmount"))?Double.parseDouble(params.get("creditAmount")):0;
		String email = params.get("email");
		String lawSuit = params.get("lawSuit");
		String creditReport = params.get("creditReport");
		String birthday = params.get("birthday");
		String legal_person = params.get("legal_person");
		Long industry = StringUtils.isNotBlank(params.get("industry"))?Long.valueOf(params.get("industry")):0;
		BigDecimal reg_capital = StringUtils.isNotBlank(params.get("reg_capital"))? new BigDecimal(params.get("reg_capital")):BigDecimal.ZERO ;
		String business_scope = params.get("business_scope");
		String income_debt_info = params.get("income_debt_info");
		String asset_info = params.get("asset_info");
		String other_finance_info = params.get("other_finance_info");
		String other_info = params.get("other_info");
		
		ErrorInfo error = new ErrorInfo();
		t_users dbUser = t_users.findById(userId);
		if(dbUser == null) {
			error.code = -1;
			error.msg = "用户不存在或已删除!";
			renderJSON(error);
		}
		User.validUserBaseinfo(userId,dbUser.user_type,sex,education,car,cityId,adCityId,house,marital,creditAmount,email,lawSuit,creditReport,error);
		if(error.code < 0) {
			renderJSON(error);
		}
		
		User.editUserBaseinfo(error,userId,dbUser.user_type,sex,education,car,cityId,adCityId,house,marital,creditAmount,email,lawSuit,creditReport
				,birthday,legal_person,industry,reg_capital,business_scope,income_debt_info,asset_info,other_finance_info,other_info);

	/*	try {
			if (User.checkPact2(userId, PactTypeEnum.QZSQ.getCode())) {
				// 修改用户基本电子签章自动签署授权协议(投资人，出借人)
				NewPact.createPact(userId, null, PactTypeEnum.QZSQ.getCode(), 2);
				Logger.info(userId + "===== 修改用户基本信息电子签章自动签署授权协议(投资人，出借人)完成========");
				// 电子签章自动签署授权协议(上标成功，借款人)
				NewPact.createPact(userId, null, PactTypeEnum.QZSQ.getCode(), 2);
				Logger.info(userId + "=====修改用户基本信息电子签章自动签署授权协议(上标成功，借款人)完成========");
			}
			if (User.checkPact2(userId, PactTypeEnum.QZSQ.getCode())) {
				// 出借人服务协议（投资成功，出借人）
				NewPact.createPact(userId, null, PactTypeEnum.CJFWXY.getCode(), 2);
				Logger.info(userId + "=====修改用户基本信息出借人服务协议（投资成功，出借人）完成========");
				// 借款人服务协议(上标成功，借款人)
				NewPact.createPact(userId, null, PactTypeEnum.JKFWXY.getCode(), 2);
				Logger.info(userId + "=====修改用户基本信息借款人服务协议(上标成功，借款人)完成========");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
*/

		renderJSON(error);
	}
	
	/**
	 *  添加用户信息
	 *
	 * @param sign
	 */
	public static void addUserBaseinfo() {
		/**
		 * long userId,int userType,int sex,String education,String car,String cityId,long adCityId,String house,
			String marital,double creditAmount,String email,String lawSuit,String creditReport,
			String birthday,String legal_person,Long industry,BigDecimal reg_capital,String business_scope,
			String income_debt_info,String asset_info,String other_finance_info,String other_info
		 */
		Long userId =  StringUtils.isNotBlank(params.get("userId"))?Long.parseLong(params.get("userId")):0;
		Integer userType = Integer.parseInt(params.get("userType"));
		Integer sex = StringUtils.isNotBlank(params.get("sex"))?Integer.parseInt(params.get("sex")):0;
		String education= params.get("education");
		String car= params.get("car");
		String cityId= params.get("cityId");
		Long adCityId = StringUtils.isNotBlank(params.get("adCityId"))?Long.valueOf(params.get("adCityId")):0;
		String house = params.get("house");
		String marital = params.get("marital");
		Double creditAmount = StringUtils.isNotBlank(params.get("creditAmount"))?Double.parseDouble(params.get("creditAmount")):0L;
		String email = params.get("email");
		String lawSuit = params.get("lawSuit");
		String creditReport = params.get("creditReport");
		String birthday = params.get("birthday");
		String legal_person = params.get("legal_person");
		Long industry = StringUtils.isNotBlank(params.get("industry"))?Long.valueOf(params.get("industry")):0L;
		BigDecimal reg_capital = StringUtils.isNotBlank(params.get("reg_capital"))? new BigDecimal(params.get("reg_capital")):BigDecimal.ZERO ;
		String business_scope = params.get("business_scope");
		String income_debt_info = params.get("income_debt_info");
		String asset_info = params.get("asset_info");
		String other_finance_info = params.get("other_finance_info");
		String other_info = params.get("other_info");
		
		ErrorInfo error = new ErrorInfo();
		User.validUserBaseinfo(userId,userType,sex,education,car,cityId,adCityId,house,marital,creditAmount,email,lawSuit,creditReport,error);
		if(error.code < 0) {
			renderJSON(error);
		}
		
		User.editUserBaseinfo(error,userId,userType,sex,education,car,cityId,adCityId,house,marital,creditAmount,email,lawSuit,creditReport
				,birthday,legal_person,industry,reg_capital,business_scope,income_debt_info,asset_info,other_finance_info,other_info);

		renderJSON(error);
	}
	/**
	 * 添加用户信息
	 */
	public static void addUser() {
		ErrorInfo error = new ErrorInfo();
		long userId = Long.valueOf(request.params.get("userId"));
		t_users user = t_users.findById(userId);
		if(user == null) {
			error.code = -1;
			error.msg = "用户不存在或已删除！";
			renderJSON(error);
		}
		int userType = Integer.valueOf(request.params.get("userType"));
		if((user.user_type == null || user.user_type.compareTo(0) == 0) && userType == 0) {
			error.code = -1;
			error.msg = "请选择主体性质！";
			renderJSON(error);
		}
		UserTypeEnum userTypeEnum = UserTypeEnum.getEnumByCode(userType);
		String realityName = request.params.get("realityName");
		if(StringUtils.isBlank(realityName)) {
			String errMsg = null;
			switch (userTypeEnum) {
			case PERSONAL:
				errMsg = "姓名不能为空！";
				break;
			case COMPANY:
			case INDIVIDUAL:
				errMsg = "企业名称不能为空！";
				break;
			default:
				errMsg = "未知的主体性质！";
				break;
			}
			error.code = -1;
			error.msg = errMsg;
			renderJSON(error);
		}
		String idNumber = request.params.get("idNumber");
		if(StringUtils.isBlank(idNumber)) {
			String errMsg = null;
			switch (userTypeEnum) {
			case PERSONAL:
				errMsg = "身份证号不能为空！";
				break;
			case COMPANY:
			case INDIVIDUAL:
				errMsg = "统一社会信用代码不能为空！";
				break;
			default:
				errMsg = "未知的主体性质！";
				break;
			}
			error.code = -1;
			error.msg = errMsg;
			renderJSON(error);
		}
		String bankName = request.params.get("bankName");
		if(StringUtils.isBlank(bankName)) {
			error.code = -1;
			error.msg = "开户行不能为空！";
			renderJSON(error);
		}
		String bankNo = request.params.get("bankNo");
		if(StringUtils.isBlank(bankNo)) {
			String errMsg = null;
			switch (userTypeEnum) {
			case PERSONAL:
				errMsg = "银行卡号不能为空！";
				break;
			case COMPANY:
			case INDIVIDUAL:
				errMsg = "对公账户不能为空！";
				break;
			default:
				errMsg = "未知的主体性质！";
				break;
			}
			error.code = -1;
			error.msg = errMsg;
			renderJSON(error);
		}
		String bankMobile = request.params.get("bankMobile");
		if(UserTypeEnum.PERSONAL.equals(userTypeEnum) && StringUtils.isBlank(bankMobile)) {
			error.code = -1;
			error.msg = "银行预留手机号不能为空！";
			renderJSON(error);
		}
		String bankAccount = request.params.get("bankAccount");
		if(UserTypeEnum.INDIVIDUAL.equals(userTypeEnum) && StringUtils.isBlank(bankAccount)) {
			error.code = -1;
			error.msg = "持卡人姓名不能为空！";
			renderJSON(error);
		}
		String cityId = request.params.get("cityId");
		if(StringUtils.isBlank(cityId)) {
			error.code = -1;
			error.msg = "所在地不能为空！";
			renderJSON(error);
		}
		if(StringUtils.isBlank(request.params.get("creditAmount"))) {
			error.code = -1;
			error.msg = "授信金额不能为空！";
			renderJSON(error);
		}
		BigDecimal creditAmount = new BigDecimal(request.params.get("creditAmount"));
		if(BigDecimal.ZERO.compareTo(creditAmount) > 0) {
			error.code = -1;
			error.msg = "授信金额不能为负数！";
			renderJSON(error);
		}
		
		try {
			String sql = "update t_users set reality_name = ?,id_number = ?,user_type = ?,credit_amount = ?,is_bank = 1 where id = ?";
			Query query = JpaHelper.execute(sql, realityName,idNumber,userType,creditAmount,userId);
			query.executeUpdate();
			
			if(StringUtils.isBlank(user.reality_name) && (UserTypeEnum.COMPANY.equals(userTypeEnum) || UserTypeEnum.INDIVIDUAL.equals(userTypeEnum))) {
				t_user_auth_review dbAuthReview = CompanyUserAuthReviewBusiness.findNotResetedAuthReviewRecord(userId);
				if(dbAuthReview != null) {
					dbAuthReview.status = AuthReviewEnum.PASSED.getCode();
					dbAuthReview.update_time = new Date();
					dbAuthReview.update_by = Supervisor.currSupervisor().id;
					dbAuthReview.save();
				}
			}
			
			t_dict_banks_col dbBank = t_dict_banks_col.find("bank_name = ?", bankName).first();
			t_user_bank_accounts bank = new t_user_bank_accounts();
			bank.user_id = userId;
			bank.bank_name = bankName;
			bank.bank_code = dbBank == null? null: String.valueOf(dbBank.bank_code);
			bank.account = bankNo;
			bank.mobile = bankMobile;
			if(UserTypeEnum.INDIVIDUAL.equals(userTypeEnum)) {
				bank.account_name = bankAccount;
			}else {
				bank.account_name = realityName;
			}
			bank.is_valid = Boolean.TRUE;
			bank.save();
			
			t_user_city userCity = t_user_city.find("user_id = ?", userId).first();
			if(userCity == null){
				userCity = new t_user_city();
			}
			userCity.city_id = cityId;
			t_new_city newCity = t_new_city.find("city_id = ?", cityId).first();
			userCity.province_id = newCity.father;
			userCity.user_id = userId;
			UserCitys.addUserCity(error, userCity);
		} catch (Exception e) {
			error.code = -1;
			error.msg = "操作失败！";
			JPA.setRollbackOnly();
			e.printStackTrace();
		}
		
		renderJSON(error);
	}
	
	/**
	 * 重置用户基本信息
	 * @param userId
	 */
	public static void resetUserBaseinfo(long userId) {
		ErrorInfo error = new ErrorInfo();
		boolean isHasBids = User.isHasBids(userId);;
		if(isHasBids) {
			error.code = -1;
			error.msg = "该用户已发布过标的，不能重置！";
			renderJSON(error);
		}
		User.resetUser(userId, error);
		
		renderJSON(error);
	}
	
	/**
	 * 模拟登录
	 */
	public static void simulateLogin(String sign) {
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if (error.code < 0) {
			flash.error(error.msg);
			allUser();
		}

		User user = new User();

		user.id = id;
		user.simulateLogin = user.encrypt();
		user.setCurrUser(user);
		AccountHome.home();
	}

	/**
	 * 锁定用户
	 */
	public static void lockUser(String sign) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if (error.code < 0) {
			json.put("error", error);
			renderJSON(json);
		}

		User.lockUser(id, error);

		json.put("error", error);

		renderJSON(json);
	}

	/**
	 * 更新签名
	 *
	 * @param id
	 */
	public static void changeSign(String sign) {
		ErrorInfo error = new ErrorInfo();

		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if (error.code < 0) {
			renderJSON(error);
		}

		DataSafety data = new DataSafety();
		data.updateSignWithLock(id, error);

		if (error.code == 0) {
			error.msg = "更新用户签名成功！";
		}
		renderJSON(error);
	}

	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年8月30日
	 * @description 绑定银行卡
	 * @param userId
	 * @param reality_name
	 * @param id_number
	 */
	public static void bindBank(long userId, String bankCode, String bankNo, String bankMobile,String bankRealName) {
		ErrorInfo error = new ErrorInfo();
		JSONObject jsonMap = new JSONObject();
		// User.updateRealityName(userId, reality_name,id_number, error);

		if (error.code < 0) {
			jsonMap.put("error", error);
			renderJSON(jsonMap);
		}

		if (StringUtils.isBlank(bankCode)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "银行不能为空");
			renderJSON(jsonMap);
		}
		int addBankCode = Integer.valueOf(bankCode);
		String bankName = DictBanksDate.queryBankByCode(addBankCode);
		if (StringUtils.isBlank(bankNo)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "银行卡账号有误");

			renderJSON(jsonMap);
		}
		t_users user = t_users.findById(userId);
		if (user == null) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "用户信息错误");
			
			renderJSON(jsonMap);
		}
		if(user.user_type ==  null) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "用户主体性质未知");
			
			renderJSON(jsonMap);
		}
		if(user.user_type.intValue() == UserTypeEnum.PERSONAL.getCode()) {
			if (StringUtils.isBlank(bankMobile)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "预留手机号不能为空");
				
				renderJSON(jsonMap);
			}
			
			if (!RegexUtils.isMobileNum(bankMobile)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "预留手机号格式错误");
				
				renderJSON(jsonMap);
			}
		}
		if(user.user_type.intValue() == UserTypeEnum.INDIVIDUAL.getCode()) {
			if (StringUtils.isBlank(bankRealName)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "银行预留姓名不能为空");
				
				renderJSON(jsonMap);
			}
		}

		String addAccountName = user.reality_name;
		if (StringUtils.isBlank(addAccountName)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "真实姓名不能为空");

			renderJSON(jsonMap);
		}

		boolean flag = new UserBankAccounts().isReuseBank(userId, bankNo, user.finance_type, "");

		if (flag) {

			jsonMap.put("error", "-3");
			jsonMap.put("msg", "该银行账户已存在，请重新输入!");

			renderJSON(jsonMap);
		}
		UserBankAccounts bankUser = new UserBankAccounts();

		bankUser.userId = userId;
		bankUser.bankName = bankName;
		bankUser.bankCode = addBankCode + "";
		// bankUser.provinceCode = addProviceCode;
		// bankUser.cityCode = addCityCode;
		// bankUser.branchBankName = addBranchBankName;
		// bankUser.province = provice;
		// bankUser.city = city;
		bankUser.account = bankNo;
		bankUser.accountName = user.user_type.intValue() == UserTypeEnum.INDIVIDUAL.getCode()? bankRealName: addAccountName;
		bankUser.mobile = user.user_type.intValue() == UserTypeEnum.PERSONAL.getCode()? bankMobile: "";
		bankUser.isValid = Boolean.TRUE;
		bankUser.isValidMobile = user.user_type.intValue() == UserTypeEnum.PERSONAL.getCode()? Boolean.TRUE: Boolean.FALSE;
		bankUser.isSign = false;
		bankUser.addUserBankAccount(error, false);
		if (error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
		} else {
			jsonMap.put("error", "-1");
			jsonMap.put("msg", "添加成功");
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.LOCK_USER, user.name + "绑定银行卡",
					error);
		}

		jsonMap.put("error", error);
		renderJSON(jsonMap);

	}


	/**
	 * 企业会员列表
	 *
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @author: zj
	 */
	public static void companyUser() {
		String mobile = params.get("mobile");
		String curPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		Integer authStatus = null;
		if (null != params.get("status")) {
			authStatus = Integer.parseInt(params.get("status"));
		}

		ErrorInfo error = new ErrorInfo();
		PageBean<UserAuthReview> page = UserAuthReview.selectCompanyUserList(mobile, authStatus, curPage, pageSize,
				error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(page);
	}
	/**
	 * 企业会员列表
	 *
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @author: zj
	 */
	public static void listCompanyUser() {
		String mobile = params.get("mobile");
		String curPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		Integer authStatus = null;
		if (null != params.get("status")) {
			authStatus = Integer.parseInt(params.get("status"));
		}
		
		ErrorInfo error = new ErrorInfo();
		PageBean<UserAuthReview> page = UserAuthReview.selectCompanyUserListView(mobile, authStatus, curPage, pageSize,
				error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(page);
	}

	/**
	 * 审核企业用户认证
	 *
	 * @param id
	 */
	public static void auditCompanyUser(long id, int status) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		try {
			UserAuthReview.saveUserBankAccountInfo(id, status);
			/*	t_user_auth_review t_user_auth_review =UserAuthReview.getTuserAuthReview(id);
			if (User.checkPact2(t_user_auth_review.user_id, PactTypeEnum.QZSQ.getCode())) {
				// 电子签章自动签署授权协议(投资人，出借人)
				NewPact.createPact(t_user_auth_review.user_id, null, PactTypeEnum.QZSQ.getCode(), 2);
				Logger.info(t_user_auth_review.user_id + "=====企业审核通过时电子签章自动签署授权协议(投资人，出借人)完成========");
				// 电子签章自动签署授权协议(上标成功，借款人)
				NewPact.createPact(t_user_auth_review.user_id, null, PactTypeEnum.QZSQ.getCode(), 2);
				Logger.info(t_user_auth_review.user_id + "=====企业审核通过时电子签章自动签署授权协议(上标成功，借款人)完成========");
			}
			if (User.checkPact2(t_user_auth_review.user_id, PactTypeEnum.QZSQ.getCode())) {
				// 出借人服务协议（投资成功，出借人）
				NewPact.createPact(t_user_auth_review.user_id, null, PactTypeEnum.CJFWXY.getCode(), 2);
				Logger.info(t_user_auth_review.user_id + "=====企业审核通过时出借人服务协议（投资成功，出借人）完成========");
				// 借款人服务协议(上标成功，借款人)
				NewPact.createPact(t_user_auth_review.user_id, null, PactTypeEnum.JKFWXY.getCode(), 2);
				Logger.info(t_user_auth_review.user_id + "=====企业审核通过时借款人服务协议(上标成功，借款人)完成========");
			}*/
		} catch (Exception e) {
			json.put("error", e.getMessage());
			json.put("code", -1);
			renderJSON(json);
			e.printStackTrace();
		}
		if (error.code < 0) {
			json.put("error", error);
			json.put("code", -1);
			renderJSON(json);
		}
		json.put("error", error);
		json.put("code", 1);
		renderJSON(json);

	}
	
	@SuppressWarnings("unchecked")
	public static void userExclusiveContent(long userId,int userType) {
		User user = new User();
		user.id = userId;
		
		List<t_dict_cars> cars = (List<t_dict_cars>) Cache.get("cars"); 
		List<t_dict_educations> educations = (List<t_dict_educations>) Cache.get("educations");
		List<t_dict_houses> houses = (List<t_dict_houses>) Cache.get("houses");
		List<t_dict_maritals> maritals = (List<t_dict_maritals>) Cache.get("maritals");
		if(user.user_type == 0 && userType != 0) {
			user.user_type = userType;
		}
		
		render(user,cars,educations,houses,maritals);
	}
}