package controllers.supervisor.userManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import constants.DealType;
import constants.SQLTempletes;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.shove.Convert;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import constants.Constants;
import constants.Templets;
import controllers.front.account.AccountHome;
import controllers.supervisor.SupervisorController;
import models.t_user_details;
import models.t_users;
import models.v_user_invest_info;
import models.vo.MigrationInvestment;
import models.vo.MigrationInvestmentBill;
import models.vo.MigrationUser;
import business.BackstageSet;
import business.StationLetter;
import business.Supervisor;
import business.TemplateEmail;
import business.User;
import play.db.jpa.JPA;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.PageBean;
import utils.SMSUtil;
import utils.Security;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.ExcelUtils;

import javax.persistence.LockModeType;
import javax.persistence.Query;

/**
 * 
 * 类名:InvestUser
 * 功能:理财会员列表
 */

public class InvestUser extends SupervisorController {

	/**
	 * 理财会员
	 */
	public static void investUser(){
		String name = params.get("name");
		String email = params.get("email");
		String beginTime = params.get("beginTime");
		String endTime = params.get("endTime");
		String orderType = params.get("orderType");
		String key = params.get("key");
		String curPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		int isExport = Convert.strToInt(params.get("isExport"), 0);
		
		ErrorInfo error = new ErrorInfo(); 
		PageBean<v_user_invest_info> page = User.queryInvestUserBySupervisor(isExport == Constants.IS_EXPORT ? Constants.NO_PAGE : 0,name, email, beginTime, endTime, key, orderType, 
				curPage, pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			List<v_user_invest_info> list = page.page;

			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);

			for (Object obj : arrList) {
				JSONObject user = (JSONObject) obj;

				if("3".equals(user.getString("master_identity"))) {
					user.put("master_identity", "是");
				}else {
					user.put("master_identity", "否");
				}
			}

			File file = ExcelUtils.export("待收款借款账单列表", arrList,
					new String[] { "会员名", "注册时间", "充值金额", "投标数量","累计投标总额", "收款中理财标数量", "待收本金总额", "转让中债权标数量", "注册邮箱", "账户可用余额",
							"是否借过款"},
					new String[] { "name", "register_time", "recharge_amount", "invest_count", "invest_amount", "invest_receive_count", "receive_amount",
							"transfer_count", "email", "user_amount", "master_identity" });

			renderBinary(file, "理财会员列表" + ".xls");
		}
		
		render(page);
	}

	/**
	 * 详情
	 * @param id
	 */
	public static void migrateBenfuStep1(String sign){
		ErrorInfo error = new ErrorInfo();

		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if(error.code < 0) {
			flash.error(error.msg);

			investUser();
		}

		JSONObject json = new JSONObject();


		User user = new User();
		user.id = id;

		//判断用户是否有冻结资金，如果有，提示在途资金处理完后再来迁移
		t_users investUser = null;
		try {
			investUser = User.getUserByIdUserId(user.id);
		} catch (Exception e) {

		}
		if(investUser==null){
			json.put("code","-1");
			json.put("msg","参数有误！");

			renderJSON(json);
		}


		if(investUser.is_migration){
			renderJSON(json);
		}

		if(investUser.freeze>0){
			json.put("code","-1");
			json.put("msg","此用户有在途资金，请处理完后再迁移。");

			renderJSON(json);
		}

		//如果用户余额大于0，则发起一笔自动提现，并产生提现成功的资金明细
		if(investUser.balance>0){
			User.migrationAutoWithdraw(investUser.balance, investUser.getId(), DealType.CHARGE_AUTO_BALANCE_WITHDRAW, error);
			if(error.code < 0) {
				JPA.setRollbackOnly();

				json.put("code","-1");
				json.put("msg","此用户有在途资金，请处理完后再迁移。");

				renderJSON(json);
			}
		}

		investUser.refresh();

		//禁止登录
		investUser.is_allow_login = true;

		//修改用户为已迁移用户
		investUser.is_migration=true;
		investUser.migration_time = new Date();

		investUser.save();

		json.put("code","1");

		renderJSON(json);
	}
	
	/**
	 * 详情
	 * @param id
	 */
	public static void detail(String sign){
        ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			
			investUser();
		}
		
		User user = new User();
		user.id = id;

		render(user);
	}

	/**
	 * 站内信
	 */
	public static void stationLetter(String sign, String content, String title){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
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
	public static void email(String email, String content){
		ErrorInfo error = new ErrorInfo();
		TemplateEmail.sendEmail(1, email, null, Templets.replaceAllHTML(content), error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}

	/**
	 * 发信息
	 * @param mobile
	 * @param content
	 */
	public static void sendMsg(String mobile, String content){
		
		ErrorInfo error = new ErrorInfo();
		SMSUtil.sendSMS(mobile, content, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 重置密码
	 */
	public static void resetPassword(String userName, String email){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		if(StringUtils.isBlank(userName) || StringUtils.isBlank(email)) {
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
		content = content.replace(Constants.EMAIL_URL, "<a href = "+url+">点击此处重置密码</a>");
		content = content.replace(Constants.EMAIL_TIME, DateUtil.dateToString(new Date()));

		TemplateEmail.sendEmail(2, email, tEmail.title, content, error);
		
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 模拟登录
	 */
	public static void simulateLogin(String sign){
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			flash.error(error.msg);
			investUser();
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
	public static void lockUser(String sign){
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
	 * 迁移用户导出
	 * @throws Exception 
	 */
	public static void migrationExport(String sign) throws Exception{
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			investUser();
		}
		t_users user=t_users.findById(id);
		if(!user.is_migration || user.migration_time==null) {
			flash.error("用户未迁移锁定");
			investUser();
		}
		
		MigrationUser migrationUser=new MigrationUser();
		migrationUser.setId(user.id);
		migrationUser.setReality_name(user.reality_name);
		migrationUser.setId_number(user.id_number);
		migrationUser.setMobile(user.mobile);
		migrationUser.setMigration_time(new Date(user.migration_time.getTime()));//t_users的migration_time实例是Date的子类
		
		//迁移资金
		Query userDetailsQuery = JPA.em().createNativeQuery("select * from t_user_details where operation=331 and user_id=?",t_user_details.class);
		userDetailsQuery.setParameter(1, user.id);
		List<t_user_details> userDetailsList=userDetailsQuery.getResultList();
		if(userDetailsList.size()>1) {
			flash.error("用户转移资金有误");
			investUser();
		}else if(userDetailsList.size()==1){
			t_user_details userDetails=userDetailsList.get(0);
			migrationUser.setBalance(userDetails.amount);
		}
		
		//迁移投资
		//PageBeanForPlayJPA.getPageBeanBySQL
		StringBuffer investmentSql = new StringBuffer("");

		investmentSql.append("select * " + 
				"from migration_investment_vo " + 
				"where userId=? ");
		Query investmentQuery = JPA.em().createNativeQuery(investmentSql.toString(),MigrationInvestment.class);
		investmentQuery.setParameter(1, user.id);
		migrationUser.setInvestmentList(investmentQuery.getResultList());
		
		//迁移投资账单
		StringBuffer billSql = new StringBuffer("");

		billSql.append("select * " + 
				"from migration_investment_bill_vo " + 
				"where userId=? ");
		Query billQuery = JPA.em().createNativeQuery(billSql.toString(),MigrationInvestmentBill.class);
		billQuery.setParameter(1, user.id);
		
		migrationUser.setBillList(billQuery.getResultList());
		
		migrationUser.sign();
		String resultJson=new Gson().toJson(migrationUser);
		//MigrationUser migrationUserP= new Gson().fromJson(resultJson, MigrationUser.class);
		//System.out.println("验证结果:"+migrationUser.verify()+" "+migrationUserP.verify());
		
		ByteArrayInputStream resultInputStream=new ByteArrayInputStream(resultJson.getBytes("UTF-8"));
		renderBinary(resultInputStream,"用户迁移数据_"+user.reality_name+"_"+user.mobile+"_"+user.id+".mujson");
		
	}
}
