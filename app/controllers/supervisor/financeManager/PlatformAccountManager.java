package controllers.supervisor.financeManager;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.swing.JApplet;

import models.t_mmm_data;
import models.t_platform_detail_types;
import models.t_return_data;
import models.t_user_bank_accounts;
import models.t_user_recharge_details;
import models.t_users;
import models.v_mmm_return_data;
import models.v_platform_detail;
import models.v_user_bill;
import models.v_user_info;
import models.v_user_withdrawal_info;
import models.v_user_withdrawals;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.lang.StringUtils;

import payment.PaymentProxy;
import payment.ips.util.IpsConstants;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JPAUtil;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.NumberUtil;
import utils.PageBean;
import utils.QueryUtil;
import utils.RegexUtils;
import utils.ymsms.SMS;
import annotation.Check;
import business.AgentPayment;
import business.DealDetail;
import business.DictBanksDate;
import business.PaymentLog;
import business.Supervisor;
import business.User;
import business.UserBankAccounts;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ips.security.utility.IpsCrypto;
import com.shove.Convert;

import constants.Constants;
import constants.IPSConstants.AgentPayStatus;
import constants.SQLTempletes;
import controllers.supervisor.SupervisorController;
import cryptix.jce.provider.dh.Test;

/**
 * 
 * 类名:PlatformAccountManager 功能:平台账户管理
 */

public class PlatformAccountManager extends SupervisorController {

	public static final Gson gson = new Gson();
	/**
	 * 待审核提现列表
	 */
	//@Check(Constants.TRUST_FUNDS)
	public static void toReviewWithdraws() {
		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数
		String beginTime = params.get("startDate"); // 开始时间
		String endTime = params.get("endDate"); // 结束时间
		String key = params.get("key"); 
		String orderTye = params.get("orderType") ;
		int isCount = Convert.strToInt(params.get("isCount"), 0);
		
		ErrorInfo error = new ErrorInfo();
		
		PageBean<v_user_withdrawal_info> page = User
				.queryWithdrawalBySupervisor(1L, Constants.WITHDRAWAL_CHECK_PENDING, beginTime, endTime, key,
				orderTye, currPage, pageSize, error);
		
		BigDecimal countAmount = User.countWithdrawalWaitAudit(error, beginTime, endTime, key);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		if(isCount == 1){
			renderJSON(page.totalCount);
		}

		render(page,countAmount);
	}
	
	/**
	 * 下载待审核列表中的用户银行卡信息
	 */
	public static void downloadUserBankList(){
		List<Map<String, Object>> list = UserBankAccounts.queryUserBanksList();
		
		JsonConfig jsonConfig = new JsonConfig();
    	jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
    	jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
    	JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
    	
    	JSONArray newList = new JSONArray();
    	
    	if(arrList.size() > 0){
    		for (int i=0;i<arrList.size();i++) {
        		Object obj = arrList.get(i);
    			JSONObject userBank = (JSONObject)obj;
    			if(userBank == null ){
    				flash.error("待审核提现列表为空！");
    				
    				return;
    			}
    			
//    			String status = userBank.get("status")+"";
//    			if(status.equals("0")){
    				newList.add(obj);
//    			}
    		}
    		
    		File file = ExcelUtils.export("待审核提现用户银行卡信息表", newList,
    				new String[] { "收款方姓名", "收款方银行卡号", "开户行所在省", "开户行所在市", "开户行名称", "收款方银行名称","金额" }, 
    				new String[] { "name", "account", "province", "city", "branch", "bankname", "amount" });
    		
    		renderBinary(file, "待审核提现用户银行卡信息表" + ".xls");
    		
    	}else{
    		
    		File file = ExcelUtils.export("待审核提现用户银行卡信息表", newList,
    				new String[] { "收款方姓名", "收款方银行卡号", "开户行所在省", "开户行所在市", "开户行名称", "收款方银行名称","金额" }, 
    				new String[] { "name", "account", "province", "city", "branch", "bankname", "amount" });
    		
    		renderBinary(file, "待审核提现用户银行卡信息表" + ".xls");
    		
    	}
	}
	
	//@Check(Constants.TRUST_FUNDS)
	public static void toPayWithdraws() {
		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数
		String beginTime = params.get("startDate"); // 开始时间
		String endTime = params.get("endDate"); // 结束时间
		String key = params.get("key"); 
		String orderTye = params.get("orderType") ;
		
		ErrorInfo error = new ErrorInfo();

		PageBean<v_user_withdrawal_info> page = User
				.queryWithdrawalBySupervisor(1L, Constants.WITHDRAWAL_PAYING, beginTime, endTime, key,
				orderTye, currPage, pageSize, error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(page);
	}

	/**
	 * 已付款提现列表
	 */
	public static void paidWithdraws() {
		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数
		String beginTime = params.get("startDate"); // 开始时间
		String endTime = params.get("endDate"); // 结束时间
		String key = params.get("key"); 
		String orderTye = params.get("orderType") ;
		int isExport = Convert.strToInt(params.get("isExport"), 0);
		pageSize = isExport == 0 ? pageSize : "99999";
		
		ErrorInfo error = new ErrorInfo();
		
		PageBean<v_user_withdrawal_info> page = User
				.queryWithdrawalBySupervisor(2L, Constants.WITHDRAWAL_SPEND, beginTime, endTime, key,
				orderTye, currPage, pageSize, error);

		BigDecimal countAmount = User.countWithdrawalPass(error, beginTime, endTime, key);
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			List<v_user_withdrawal_info> list = page.page;
			
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bill = (JSONObject)obj;
				
				bill.put("name", bill.getString("name"));
				bill.put("reality_name", bill.getString("reality_name"));
				bill.put("amount", bill.getString("amount"));
				bill.put("time", bill.getString("time"));
				bill.put("pay_time", bill.getString("pay_time"));
			}
			
			//用户名、真实姓名、手机号
			
			File file = ExcelUtils.export("提现列表", arrList,
					new String[] {"用户名", "真实姓名", "提现金额", "申请时间","付款时间"}, 
					new String[] {"name", "reality_name", "amount", "time","pay_time"});
			
			renderBinary(file, "提现列表" + ".xls");
		}

		render(page, countAmount);
	}

	/**
	 * 未通过提现列表
	 */
	public static void noPassWithdraws() {
		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数
		String beginTime = params.get("startDate"); // 开始时间
		String endTime = params.get("endDate"); // 结束时间
		String key = params.get("key"); 
		String orderTye = params.get("orderType") ;
		ErrorInfo error = new ErrorInfo();

		PageBean<v_user_withdrawal_info> page = User
				.queryWithdrawalBySupervisor(1L, Constants.WITHDRAWAL_NOT_PASS, beginTime, endTime, key,
				orderTye, currPage, pageSize, error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(page);
	}
	
	/**
	 * 详情  
	 */
	public static void withdrawDetail(long withdrawalId) {
		ErrorInfo error = new ErrorInfo();
		
		long supervisorId = 1L;
		v_user_withdrawals withdrawal =  User.queryWithdrawalDetailBySupervisor(
				supervisorId, withdrawalId, error);

		JSONObject json = new JSONObject();

		json.put("error", error);
		json.put("withdrawal", withdrawal);

		renderJSON(json);
	}

	/**
	 * 提现审核通过
	 */
	//@Check(Constants.TRUST_FUNDS)
	public static void withdrawPass(long withdrawalId) {
		ErrorInfo error = new ErrorInfo();
		User.auditWithdrawalPass(1L, withdrawalId, error);

		flash.error(error.msg);

		toReviewWithdraws();
	}

	/**
	 * 不通过(提现申请审核)
	 */
	//@Check(Constants.TRUST_FUNDS)
	public static void withdrawReview(long withdrawalId) {
		
		if(0 == withdrawalId)
			return ;
		
		String reason = params.get("reason");
		
		String returnType = params.get("returnType");
		
		ErrorInfo error = new ErrorInfo();
		User.auditWithdrawalDispass(1L, withdrawalId, reason, false, error);

		flash.error(error.msg);
		
		if(returnType.equalsIgnoreCase("2")){//该判断跳转到待付款提现列表
			toPayWithdraws();
		}else{
	        toReviewWithdraws();
		}
	}

	// 模拟登录
	public static void simulateLogin() {
		render();
	}

	/**
	 * 付款通知初始化
	 */
	public static void payNotificationInit(long withdrawalId) {
		ErrorInfo error = new ErrorInfo();

		long supervisorId = 1L;
		v_user_withdrawals withdrawal = User.withdrawalDetail(supervisorId,
				withdrawalId, error);

		JSONObject json = new JSONObject();

		json.put("error", error);
		json.put("withdrawal", withdrawal);

		renderJSON(json);
	}

	/**
	 * 付款通知
	 */
	public static void payNotification(long userId, long withdrawalId, String type) {
		ErrorInfo error = new ErrorInfo();
		
		if (StringUtils.isBlank(type)) {
			error.code = -1;
			error.msg = "请选择付款通知方式";
			
			renderJSON(error);
		}
		
		User.withdrawalNoticeForNormalGateway(userId, 0, withdrawalId, type, error);  //普通网关提现

		renderJSON(error);
	}
	/**
	 * 
	 * @author xinsw 
	 * @creationDate 2017年8月16日
	 * @description 批量付款通知
	 */
	public static void batchPayNotification(String startDate,String endDate,String key){
		
		ErrorInfo error = new ErrorInfo();
		
		List<v_user_withdrawal_info> page = User.queryWithdrawalBySupervisor(1L, Constants.WITHDRAWAL_PAYING, 
				startDate, endDate, key,error);
		if(page != null && page.size() > 0){
			for(v_user_withdrawal_info ui : page){
				JPAUtil.transactionBegin();
				User.withdrawalNoticeForNormalGateway(ui.user_id, 0, ui.id, "1,2", error);  //普通网关提现
				try {
					JPAUtil.transactionCommit();
				} catch (Exception e) {
					Logger.error("批量通知时："+e.getMessage());
				}
			}
		}
		renderJSON(error);
	}
	
	/**
	 * 打印付款单
	 */
	public static void printPayBill(long withdrawalId) {
		ErrorInfo error = new ErrorInfo();
		
		v_user_withdrawals withdrawal = User.printPayBill(
				withdrawalId, error);

		JSONObject json = new JSONObject();

		String status = null;
		
		json.put("error", error);
		json.put("name", withdrawal.name);
		json.put("amount", withdrawal.amount);
		json.put("service_fee", withdrawal.service_fee);
		json.put("time", DateUtil.dateToString(withdrawal.time));
		json.put("audit_time", DateUtil.dateToString(withdrawal.audit_time));
		
		switch (withdrawal.status) {
			case Constants.WITHDRAWAL_CHECK_PENDING: status = "待审核"; break;
			case Constants.WITHDRAWAL_PAYING: status = "付款中"; break;
			case Constants.WITHDRAWAL_SPEND: status = "已付款"; break;
			case Constants.WITHDRAWAL_NOT_PASS: status = "未通过 "; break;
		}
		json.put("status", status);
		withdrawal.account_name = withdrawal.account_name == null ? "" : withdrawal.account_name;
		withdrawal.account = withdrawal.account == null ? "" : withdrawal.account;
		withdrawal.bank_name = withdrawal.bank_name == null ? "" : withdrawal.bank_name;
		json.put("bankInfo", "真实姓名：" + withdrawal.account_name 
					+ "<br/>账号：" + withdrawal.account 
					+ "<br/>开户行：" + withdrawal.bank_name
					+ "<br/>支行：" + withdrawal.branch_bank_name
					+ "<br/>支行所在地：" + withdrawal.province + " | " + withdrawal.city);
		
		renderJSON(json);
	}

	//未通过提现详情
	public static void noPassWithdrawDetail() {
		
	}

	/**
	 * 未通过提现原因
	 */
	public static void noPassWithdrawReason(long withdrawalId) {
		ErrorInfo error = new ErrorInfo();

		Object[] reason = User.withdrawalDispassReason(withdrawalId, error);

		JSONObject json = new JSONObject();

		json.put("error", error);
		json.put("name", null == reason[1] ? "" : reason[1]);
		json.put("reason", null == reason[0] ? "" : reason[0]);

		renderJSON(json);
	}
	
	/**
	 * 选择会员
	 */
	public static void forRecharge(String name, String currPage, String pageSize) {
		ErrorInfo error = new ErrorInfo();

		PageBean<v_user_info> page = User.queryUserBySupervisor(name, null,
				null, null, null, null, null, null, currPage,pageSize, error);
		
		JSONObject json = new JSONObject();

		json.put("error", error);
		json.put("page", page);

		renderJSON(json);
	}

	/**
	 *  手工充值页面
	 */
	@Check(Constants.TRUST_FUNDS)
	public static void manualRecharge() {
		render();
	}
	
	/**
	 * 通过名字查询用户
	 * @param name
	 */
	public static void queryUserByName(String name) {
		ErrorInfo error = new ErrorInfo();
		t_users user = User.queryUserByName(name, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		renderJSON(user);
	}
	
	/**
	 * 手工充值
	 * @param name
	 * @param amount
	 */
	@Check(Constants.TRUST_FUNDS)
	public static void rechargeByHand(String name, double amount) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Supervisor.currSupervisor().id;
		User.rechargeByHand(supervisorId, name, amount, error);
		
		renderJSON(error);
	}

	/**
	 * 充值记录
	 */
	public static void rechargeRecord(int isExport, int type, int status, String name, String startDate, String endDate, int currPage){
		PageBean<t_user_recharge_details> page = DealDetail.queryUserRechargeDetails(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0,type, status, name, startDate, endDate, currPage);
		
		Logger.info("充值类型:"+type);
		
		if(null == page){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		if(isExport == Constants.IS_EXPORT){
			
			List<t_user_recharge_details> list = page.page;
			 
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd hh:mm:ss"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject recharge = (JSONObject)obj;			

				int rechargeType = recharge.getInt("type");
				String rechargeTypeStr = "";
				if(rechargeType == 0){
					rechargeTypeStr = "在线充值(普通网关)";
				}else if(rechargeType == 1){
					rechargeTypeStr = "在线充值(资金托管)";
				}else{
					rechargeTypeStr = "手工充值";
				}
				
				int gatewayId = recharge.getInt("payment_gateway_id");
				String gateway = "";
				switch (gatewayId) {
				case 0:
					gateway = "手工";
					break;
				case 1:
					gateway = "国付宝";
					break;
				case 2:
					gateway = "环讯";
					break;
				case 3:
					gateway = "连连支付";
					break;
				case 4:
					gateway = "宝付";
					break;
				case 5:
					gateway = "易宝";
					break;
				default:
					break;
				}

				recharge.put("type", rechargeTypeStr);
				recharge.put("payment_gateway_id", gateway);
				recharge.put("is_completed", recharge.getBoolean("is_completed")?"成功":"失败");
			}
			
			File file = ExcelUtils.export("充值记录",
			arrList,
			new String[] {
			"用户名", "时间", "充值类型", "支付网关名称", "银行账号","支付号",
			"充值金额", "状态", "完成时间"},
			new String[] {"name", "time", "type",
			"payment_gateway_id","bank_card_no", "pay_number", 
			"amount", "is_completed",
			"completed_time"});
			   
			renderBinary(file, "充值记录.xls");
		}
		
		
		BigDecimal countAmount = User.countRecharge(new ErrorInfo(),status, type, name, startDate, endDate);
		
		render(page, countAmount);
	}
	
	/**
	 * 提现记录
	 */
	public static void withdrawRecords(int isExport, int currPage, int pageSize, String name, int status, String startDate, String endDate){
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_withdrawals> page = DealDetail.queryWithdrawRecords(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0,currPage, pageSize, name, status, startDate, endDate, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		if(isExport == Constants.IS_EXPORT){
			
			List<v_user_withdrawals> list = page.page;
			 
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd hh:mm"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject withdraw = (JSONObject)obj;			

				withdraw.put("amount", withdraw.getString("amount") + "元");
				withdraw.put("status", withdraw.getInt("status")==2?"成功":"失败");
			}
			
			File file = ExcelUtils.export("提现记录",
			arrList,
			new String[] {
			"用户名", "提现金额（元）", "申请时间", "付款时间", "状态"},
			new String[] {"name", "amount", "time",
			"pay_time", "status"});
			   
			renderBinary(file, "提现记录.xls");
		}
		
		render(page);
	}
	
	// 交易记录
	public static void transactionRecords(int type, int operation, int side,
			String beginTime, String endTime, String name, int currPage) {
        ErrorInfo error = new ErrorInfo();
		
		PageBean <v_platform_detail> page = DealDetail.platformDetail(0, type, operation, side, beginTime, endTime, name, currPage, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		List<t_platform_detail_types> allType = DealDetail.queryType(type, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		Map<String, Double> account = DealDetail.total(error);
		double income = account.get("income");
		double expense = account.get("expense");
		
		render(page, allType, income, expense);
	}

	// 本金保障账户管理
	// 本金保障principalProtection
	public static void ppAccountManagement() {
		render();
	}

	/**
	 * 本金保障账户概要
	 */
	public static void ppAccountInfo() {
		ErrorInfo error = new ErrorInfo();
		
		Map<String, Object> account = DealDetail.accountSummary(error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(account);
	}

	/**
	 *  添加保障金
	 */
	public static void addPrincipalProtection(double amount, String summary) {
		ErrorInfo error = new ErrorInfo();
		
		DealDetail.addCapital(amount, summary, error);
		
		if(error.code < 0){
			flash.error(error.msg);
			ppAccountInfo();
		}
		
		flash.success(error.msg);
		
		ppAccountInfo();
	}

	/**
	 *  保障金收支记录
	 */
	public static void principalProtectionRecords(int isExport, int type, int operation, int side,
			String beginTime, String endTime, String name, int currPage) {
		ErrorInfo error = new ErrorInfo();
		
		PageBean <v_platform_detail> page = DealDetail.platformDetail(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0,type, operation, side, beginTime, endTime, name, currPage, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		List<t_platform_detail_types> allType = DealDetail.queryType(type, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		Map<String, Double> total = DealDetail.total(error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		if(isExport == Constants.IS_EXPORT){
			
			List<v_platform_detail> list = page.page;
			 
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd hh:mm:ss"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject principal = (JSONObject)obj;			

				int amountType = principal.getInt("type");
				String in_amount = "";
				String out_amount = "";
				
				if(amountType == 1){
					in_amount = principal.getString("amount");
				}else{
					in_amount = "0.00";
				}
				
				if(amountType == 2){
					out_amount = principal.getString("amount");
				}else{
					out_amount = "0.00";
				}

				principal.put("in_amount", in_amount);
				principal.put("out_amount", out_amount);
			}
			
			File file = ExcelUtils.export("保障金收支记录",
			arrList,
			new String[] {
			"时间", "收入", "支出", "科目", "支付方式",
			"付款方", "收款方"},
			new String[] {"time", "in_amount", "out_amount", "name",
			"payment", "from_pay", 
			"to_receive"});
			   
			renderBinary(file, "保障金收支记录.xls");
		}
		
		render(page, allType, total);
	}
	
	/**
	 *  保障金收支记录
	 */
	public static void recordDetail(long id) {
		ErrorInfo error = new ErrorInfo();
		
		v_platform_detail detail = DealDetail.detail(id, error);
		
		renderJSON(detail);
	}
	
	/**
	 *  通过收入|支出来查找平台交易类型名称
	 */
	public static void queryDetailTypeNames(int type){
		ErrorInfo error = new ErrorInfo();
		
		List<t_platform_detail_types> types = DealDetail.queryType(type, error);

		renderJSON(types);
	}
	
	/**
	 * 支付掉单处理方法
	 * @param uid
	 * @param payNumber
	 */
	public static void offSingleDeal(String payNumber){
		
		ErrorInfo error = new ErrorInfo();
		String info = "";
		if(StringUtils.isBlank(payNumber)){
			info = "不存在的补单记录";
			render(info);
		}
		
		t_user_recharge_details user_recharge = null;
		user_recharge = t_user_recharge_details.find("pay_number = ? and is_completed = ?", payNumber, false).first();
		if(null == user_recharge){
			info = "不存在的补单记录";
			render(info);
		}
		
		if(user_recharge.payment_gateway_id == Constants.GO_GATEWAY){
			//国付宝掉单处理
			User.goffSingle(payNumber,user_recharge,error);
			
		}
		
        info =error.msg;
        render(info);
	}
	
	/**
	 * 环迅主动对账接口
	 * 需提供主动对账地址给环迅一定要严格区分http和https，按照实际的访问地址进行提交
	 * @param billno
	 * @param mercode
	 * @param Currency_type
	 * @param amount
	 * @param date
	 * @param succ
	 * @param msg
	 * @param attach
	 * @param ipsbillno
	 * @param retencodetype
	 * @param signature
	 */
	public static void ipsOffSingleDeal(String billno, String mercode, String Currency_type, String amount, String date, String succ,
			String msg, String attach, String ipsbillno, String retencodetype, String signature) {
		ErrorInfo error = new ErrorInfo();
		
		//返回订单加密的明文:billno+【订单编号】+currencytype+【币种】+amount+【订单金额】+date+【订单日期】+succ+【成功标志】+ipsbillno+【IPS订单编号】+retencodetype +【交易返回签名方式】+【商户内部证书】 
		String content="billno"+billno + "currencytype"+Currency_type+"amount"+amount+"date"+date+"succ"+succ+"ipsbillno"+ipsbillno+"retencodetype"+retencodetype;  //明文：订单编号+订单金额+订单日期+成功标志+IPS订单编号+币种

		boolean verify = false;

		//验证方式：16-md5withRSA  17-md5
		if(retencodetype.equals("16")) {
			cryptix.jce.provider.MD5WithRSA a=new cryptix.jce.provider.MD5WithRSA();
			a.verifysignature(content, signature, "D:\\software\\publickey.txt");

			//Md5withRSA验证返回代码含义
			//-99 未处理
			//-1 公钥路径错
			//-2 公钥路径为空
			//-3 读取公钥失败
			//-4 验证失败，格式错误
			//1： 验证失败
			//0: 成功
			if (a.getresult() == 0){
				verify = true;
			}	
		} else if(retencodetype.equals("17")) {
			User.validSign(content, signature, error);
			
			if(error.code == 0) {
				verify = true;
			}
		}
		String info = "ipscheckok";
		if(!verify) {
			renderText(info);
			return;
		}
		
		if (succ == null) {
			info = "交易失败";
			renderText(info);
			return;
		}
		
		if(!succ.equalsIgnoreCase("Y")) {
			info = "ipscheckok";
			Logger.info("hxinfo_order_fail==:%s", billno);
			renderText(info);
			return;
		} 
		
		t_user_recharge_details user_recharge = null;
		user_recharge = t_user_recharge_details.find("pay_number = ? and is_completed = ?", billno, false).first();
		if(null == user_recharge){
			renderText(info);
			return;
		}
		
		User.recharge(billno, Double.parseDouble(amount), error);
		 
		if(error.code < 0) {
			renderText(info);
			return;
		}
		
		Logger.info("hxinfo_ok==:%s", billno);
		renderText(info);
	}

	/**
	 * 平台对账查询
	 * @param name
	 */
	public static void queryForAccBalance(String name, int currPage, int pageSize){
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_bill> page = User.queryUserBlance(name, currPage, pageSize, error);
		render(page);
		
	}
	
	/**
	 * 查询日志列表
	 */
	public static void queryLogData(String name, int currPage, int pageSize){
		
		String beginTime = params.get("startDate"); // 开始时间
		String endTime = params.get("endDate"); // 结束时间
		String key = params.get("key"); 		//关键字
		String username = params.get("username");	//用户名
		String orderNum = params.get("orderNumm");	//订单号
		String status = params.get("status");		//状态
		String type = params.get("type");			//类型		
		ErrorInfo error = new ErrorInfo();		
		PageBean<v_mmm_return_data> page = PaymentLog.queryMMMBySupervisor(beginTime, endTime, key, currPage+"", pageSize+"", username, orderNum, status, type, error);
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(page);
	}
	
	/**
	 * 流水账号账单管理
	 */
	public static void remarkInfo() {
		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数
		String beginTime = params.get("startDate"); // 开始时间
		String endTime = params.get("endDate"); // 结束时间
		String key = params.get("key"); 		//关键字
		String username = params.get("username");	//用户名
		String orderNum = params.get("orderNumm");	//订单号
		String status = params.get("status");		//状态
		String type = params.get("type");			//类型		
		ErrorInfo error = new ErrorInfo();		
		PageBean<v_mmm_return_data> page = PaymentLog.queryMMMBySupervisor(beginTime, endTime, key, currPage, pageSize, username, orderNum, status, type, error);
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(page);
	}
	/**
	 * 补单
	 * @param orderNum
	 */
	public static void singleSupplement(long id){
		ErrorInfo error = new ErrorInfo();
		JSONObject json =  new JSONObject();
		
		v_mmm_return_data data = PaymentLog.findVmmmReturnDate(id, error);
		if(error.code<0){
			json.put("error", error);
			renderJSON(json);
		}
		String url = PaymentLog.lookForReturnUrl(data.orderNum, error);
		
		if(error.code<0){
			json.put("error", error);
			renderJSON(json);
		}
		Map<String,String> paramMap = PaymentLog.getReturnData(data.send_id, error);
		if(error.code<0){
			json.put("error", error);
			renderJSON(json);
		}
		paramMap.put("url", url);
		renderJSON(paramMap);
	}
	/**
	 * 后台--流水账单管理
	 * @param id
	 * @return
	 */
	public static PageBean<v_mmm_return_data> queryMMMBySupervisor( String beginTimeStr, String endTimeStr, String key,
			String currPageStr, String pageSizeStr,String username, String orderNum,  String status,String type, ErrorInfo error){
		error.clear();
 		Date beginTime = null;
 		Date endTime = null;
 		int currPage = Constants.ONE;
 		int pageSize = Constants.PAGE_SIZE;
 
 		Map<String,Object> conditionMap = new HashMap<String, Object>();
 		if(RegexUtils.isDate(beginTimeStr)) {
 			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
 			conditionMap.put("beginTime", beginTimeStr);
 		}
 		
 		if(RegexUtils.isDate(endTimeStr)) {
 			endTime = DateUtil.strDateToEndDate(endTimeStr);
 			conditionMap.put("endTime", endTimeStr);
 		}
 		
 		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.V_MMM_RETURN_DATA);
		
		List<Object> params = new ArrayList<Object>();
		
		if(beginTime != null) {
			sql.append(" and t_return_data.op_time >= ? ");
			params.add(beginTime);
		}
		
		if(endTime != null) {
			sql.append(" and t_return_data.op_time <= ? ");
			params.add(endTime);
		}
		
		if(StringUtils.isNotBlank(username)) {
			conditionMap.put("username", username);
			sql.append(" and t_users.name like ? ");
			params.add("%"+username.trim()+"%");
		}
		if (StringUtils.isNotBlank(orderNum)) {
			conditionMap.put("orderNum", orderNum);
			sql.append(" and t_return_data.orderNum like  ? ");
			params.add("%"+orderNum.trim()+"%");

		}
		if(StringUtils.isNotBlank(status)) {
			conditionMap.put("status", status);
			if(status.trim().equals("成") || status.trim().equals("成功"))
				status = "2";
			else if(status.trim().equals("失败") || status.trim().equals("失"))
				status = "1";
			sql.append(" and t_mmm_data.status like ?  ");
			params.add("%"+status.trim()+"%");
			
		}
		if(StringUtils.isNotBlank(type)) {
			conditionMap.put("type", type);
			sql.append(" and t_mmm_data.msg like ? ");
			params.add("%"+type.trim()+"%");
		}
		if(StringUtils.isNotBlank(key)) {
			conditionMap.put("key", key);
			if(key.trim().equals("成") || key.trim().equals("成功"))
				orderNum = "2";
			else if(key.trim().equals("失败") || key.trim().equals("失"))
				orderNum = "1";
			
			sql.append(" and t_users.name like ? or t_return_data.orderNum like ? or t_mmm_data.status like ? or t_mmm_data.msg like ? ");
			params.add("%"+key+"%");
			params.add("%"+key+"%");
			params.add("%"+key+"%");
			params.add("%"+key+"%");
			
		}
		
		sql.append(" order by t_mmm_data.op_time desc");
		
		List<v_mmm_return_data> withdrawals = new ArrayList<v_mmm_return_data>();
		int count = 0;
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_mmm_return_data.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            withdrawals = query.getResultList();
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询流水号账单时！："+e.getMessage());
			error.code = -1;
			error.msg = "查询流水号账单时！";
			
			return null;
		}
		
		
		
		PageBean<v_mmm_return_data> page = new PageBean<v_mmm_return_data>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		page.page = withdrawals;
		
		error.code = 0;
		return page;
	}
	
	/**
	 * 查找回调参数信息
	 * @param orderNum
	 */
	public static String lookRarkInfo(String orderNum, ErrorInfo error){
		error.clear();
		t_return_data data = null;
		long id = Convert.strToLong(orderNum, -1);
		try {
			data = t_return_data.find(" id = ? ", id).first();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "查询流水号账单时链接数据库失败";
			
			return null;
		}
		
		if(data == null){
			
			error.code = -1;
			error.msg = "查询流水号账单时传入的流水账单号有误";
			
			return null;
			
		}
		error.code = 0;
		error.msg = "查询流水号账单时信息成功!";
		return data.data;
	}
	
	/**
	 * 查找发送参数信息
	 * @param orderNum
	 */
	public static String lookRarkSendInfo(String orderNum, ErrorInfo error){
		error.clear();
		t_mmm_data data = null;
		
		try {
			data = t_mmm_data.find(" orderNum = ? ", orderNum).first();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "查询流水号账单时链接数据库失败";
			
			return null;
		}
		
		if(data == null){
			
			error.code = -1;
			error.msg = "查询流水号账单时传入的流水账单号有误";
			
			return null;
			
		}
		
		error.code = 0;
		error.msg = "查询流水号账单时信息成功!";
		return data.data;
	}
	
	
	
	
	/**
	 * 根据流水号查找异步回调地址
	 * @param orderNum
	 */
	public static String lookForReturnUrl(String id, ErrorInfo error){
		error.clear();
		String returnUrl = "";
		t_mmm_data data = null;
		
		try {
			data = t_mmm_data.find(" orderNum = ?", id).first();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "查询流水号账单时链接数据库失败";
			
			return null;
		}
		
		if(data == null){
			
			error.code = -1;
			error.msg = "查询流水号账单时传入的流水账单号有误";
			
			return null;
			
		}
		
		error.code = 0;
		error.msg = "查询流水号账单时信息成功!";
		
		Map<String, String> map = gson.fromJson(data.data, new TypeToken<Map<String, String>>(){}.getType());
		returnUrl = map.get("NotifyURL");
		return returnUrl;
	}
	
	
	/**
	 * 根据流水号查询回调参数
	 */
	public static Map<String,String>  getReturnData(long id,ErrorInfo error){
		error.clear();
		t_return_data data = null;
		
		try {
			data = t_return_data.findById(id);
		} catch (Exception e) {
			error.code = -1;
			error.msg = "查询流水号账单时链接数据库失败";
			
			return null;
		}
		
		if(data == null){
			
			error.code = -1;
			error.msg = "查询流水号账单时传入的流水账单号有误";
			
			return null;
			
		}
		
		Map<String, String> map = gson.fromJson(data.data, new TypeToken<Map<String, String>>(){}.getType());
		error.code = 0;
		error.msg = "查询流水号账单时信息成功!";

		return map;
		}
	
	/**
	 * 查询视图里面的单条记录
	 * @param id
	 */
	public static v_mmm_return_data findVmmmReturnDate(long id, ErrorInfo error){
		error.clear();
		v_mmm_return_data data = null;
		
		try {
			data = v_mmm_return_data.findById(id);
		} catch (Exception e) {
			error.code = -1;
			error.msg = "查询流水号账单时链接数据库失败";
			
			return null;
		}
		
		if(data == null){
			
			error.code = -1;
			error.msg = "查询流水号账单时传入的流水账单号有误";
			
			return null;
			
		}	
		error.code = 0;
		error.msg = "查询单条流水号账单时信息成功!";

		return data;
	}
	/**
	 * 通过流水号查找流水号账单信息
	 */
	public static void lookForRarkInfo(String orderNum){
		ErrorInfo error = new ErrorInfo();
		String dataStr = PaymentLog.lookRarkInfo(orderNum, error);
		if(error.code<0){
			renderJSON(error);
		}
		Map<String, String> map = gson.fromJson(dataStr, new TypeToken<Map<String, String>>(){}.getType());
		
		List<Map<String, String>> list = new LinkedList<Map<String, String>>();
		
		//环讯托管回调参数解密
		if(Constants.TRUST_FUNDS_TYPE.equals("HX")){
			for(Entry<String, String> entry: map.entrySet()) {			
				if("p3DesXmlPara".equals(entry.getKey())){				
					Map<String, String> page = new HashMap<String, String>();
					String xml = IpsCrypto.triDesDecrypt(map.get("p3DesXmlPara"), IpsConstants.des_key, IpsConstants.des_iv);
					page.put("value", java.net.URLDecoder.decode(xml));
					page.put("key", entry.getKey());
					list.add(page);
				}else{
					Map<String, String> page = new HashMap<String, String>();
					page.put("value", java.net.URLDecoder.decode(entry.getValue()));
					page.put("key", entry.getKey());
					list.add(page);
				}
			}
			
		}else if(Constants.TRUST_FUNDS_TYPE.equals("HF")){
			Map<String, String> page = null;
			for(Entry<String, String> entry : map.entrySet()){
				page = new HashMap<String, String>();
				page.put("key", entry.getKey());
				page.put("value", entry.getValue());
				list.add(page);
			} 
		}else if(Constants.TRUST_FUNDS_TYPE.equals("FY")){
			Map<String, String> page = null;
			for(Entry<String, String> entry : map.entrySet()){
				page = new HashMap<String, String>();
				page.put("key", entry.getKey());
				page.put("value", entry.getValue());
				list.add(page);
			} 
		}
		render(list);
		
	}
	
	/**
	 * 通过流水号查找发送参数信息
	 * @param orderNum
	 */
	public static void lookForSendRarkInfo(String orderNum){
		ErrorInfo error = new ErrorInfo();
		Map<String, String> map = PaymentLog.lookRarkSendInfo(orderNum, error);
		if(error.code<0){
			renderJSON(error);
		}
		List<Map<String, String>> list = new LinkedList<Map<String, String>>();
		for(Entry<String, String> entry: map.entrySet()) {							
			Map<String, String> page = new HashMap<String, String>();
			page.put("value", java.net.URLDecoder.decode(entry.getValue()));
			page.put("key", entry.getKey());
			list.add(page);
		}		
		render(list);
	}
	
	
	/**
	 * 闪电快充失败，重新进行转账
	 * 
	 * 注意：只有是商户账户余额不足导致闪电快充失败，才允许重新转账，其他情况通过补单处理。
	 * 
	 * @param agentRecharge
	 */
	public static void reTransfer(String agentOrderNo, int type, int status, String name, String startDate, String endDate, int currPage){
		ErrorInfo error = new ErrorInfo();
		
		AgentPayment ap = new AgentPayment(Long.parseLong(agentOrderNo), error);
		if(error.code < 0){
			flash.error(error.msg);
			
			rechargeRecord(0, type, status, name, startDate, endDate, currPage);
		}
		
		if(ap.status == AgentPayStatus.AGENT_SUBMIT){  //用户未成功支付充值金额
			flash.error("系统记录显示：用户未成功支付充值金额，不能在执行转账");
			
			rechargeRecord(0, type, status, name, startDate, endDate, currPage);
		}
		
		//查询转账日志
		Map<String,Object> result = PaymentProxy.getInstance().queryLog(error, Constants.PC, ap.merOrderNo);
		int code = Integer.parseInt(result.get("code").toString());
		String msg = result.get("msg").toString();
		
		if(code < 0){
			flash.error(msg);
			
			rechargeRecord(0, type, status, name, startDate, endDate, currPage);
		}
		
		if(code == 1){  //转账成功
			flash.error("托管账户已到账，不能在执行转账，请联系运维排查问题");
			
			rechargeRecord(0, type, status, name, startDate, endDate, currPage);
		}
		
		int extend1 = Integer.parseInt(result.get("extend1").toString());
		
		if(code == 0 && extend1 == 0){  //非商户余额不足导致转账失败，
			flash.error("非商户余额不足导致转账失败，不能在执行转账，请联系运维排查问题");
			
			rechargeRecord(0, type, status, name, startDate, endDate, currPage);
		}
		
		if(code == 0 && extend1 == 1){  //商户余额不足导致转账失败，重新转账
			
			ap.reTransfer(error);
			
			if(error.code < 0){
				flash.error(error.msg);
				
				rechargeRecord(0, type, status, name, startDate, endDate, currPage);
			}

			flash.error("转账成功");
			rechargeRecord(0, type, status, name, startDate, endDate, currPage);
		}
		
		flash.error("转账异常");
		rechargeRecord(0, type, status, name, startDate, endDate, currPage);
	}
	
	/**
	 * 查看所有客户添加银行账户列表
	 * @param currPage
	 * @param pageSize
	 * @param userName
	 * @param accountName
	 * @param accountNo
	 */
	public static void toAccountBankList(int currPage,int pageSize,String userName,String accountName,String accountNo){
		
		ErrorInfo error = new ErrorInfo();
		
		PageBean<t_user_bank_accounts> page = new PageBean<t_user_bank_accounts>();
		
		page = DealDetail.queryBankAccounts(0, currPage, pageSize, userName,accountName,accountNo,error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}
	
	
	/**
	 * 查看客户添加的银行卡账户信息
	 */
	public static void editAccountBankInfoInit(long accountId){
		
		ErrorInfo error = new ErrorInfo();
		
		if(accountId <= 0){
			
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		UserBankAccounts userBankAccounts = new UserBankAccounts();
		
		userBankAccounts.id = accountId;
		
		User user = new User();
		
		user.id = userBankAccounts.userId;
		
		Map<String,String> bankCodeNameTable  = new LinkedHashMap<String, String>();
		
		if(userBankAccounts.verified){
			
			for(int i = 0 ;i<Constants.BAOFU_TYPE.length ; i++){
				
				bankCodeNameTable.put(Constants.BAOFU_TYPE[i], Constants.BAOFU_BANK_NAME[i]);
			}
			
		}else{
			bankCodeNameTable = DictBanksDate.bankCodeNameTable;
		}
		
		Map<String,String> provinceCodeNameTable = DictBanksDate.provinceCodeNameTable;
		
		render(userBankAccounts,user,bankCodeNameTable,provinceCodeNameTable);
	}
	
	public  static void editBankAccount(){
		
		ErrorInfo error = new ErrorInfo();
		
		String accountIdStr = params.get("accountId");
		
		String accountName = params.get("accountName");
		
		String bankCode = params.get("bankCode");
		
		String account = params.get("account");
		
		String branchBankName = params.get("branchBankName");
		
		String provinceCode = params.get("provinceCode");
		
		String cityCode = params.get("cityCode");
		
		if(StringUtils.isBlank(accountIdStr) || "0".equals(accountIdStr)){
			
			error.code = -1;
			
			error.msg = "更新失败，数据有误！！";
			
			renderJSON(error);
		}
		
		if(StringUtils.isBlank(accountName)){
			
			error.code = -1;
			
			error.msg = "更新失败，开户名不能为空！！";
			
			renderJSON(error);
			
		}
		
		if(StringUtils.isBlank(bankCode)){
			
			error.code = -1;
			
			error.msg = "更新失败，银行不能为空！！";
			
			renderJSON(error);
			
		}
		
		if(StringUtils.isBlank(account)){
			
			error.code = -1;
			
			error.msg = "更新失败，银行账号不能为空！！";
			
			renderJSON(error);
			
		}
		
		if(!RegexUtils.isBankAccount(account)) {
            error.code = -1;
            error.msg = "银行账号格式错误，应该是16-22位数字！";
            renderJSON(error);
        }

		
		long accountId = Long.parseLong(accountIdStr);
		
		String bankName = "";
		
		String provinceName = "";
		
		String cityName = "";
		
		UserBankAccounts userBankAccounts = new UserBankAccounts();
		
		userBankAccounts.id = accountId;
		
		if(userBankAccounts.verified){
			
			for(int i = 0 ;i<Constants.BAOFU_TYPE.length ; i++){
				
				//bankCodeNameTable.put(Constants.BAOFU_TYPE[i], Constants.BAOFU_BANK_NAME[i]);
				if(Constants.BAOFU_TYPE[i].equals(bankCode)){
					bankName = Constants.BAOFU_BANK_NAME[i];
				}
			}
		}else{
			
			Map<String,String> bankCodeNameTable  = new LinkedHashMap<String, String>();
			
			bankCodeNameTable = DictBanksDate.bankCodeNameTable;
			
			bankName = bankCodeNameTable.get(bankCode);
		}
		
		if(StringUtils.isNotBlank(provinceCode)){
			
			Map<String,String> provinceCodeNameTable = DictBanksDate.provinceCodeNameTable;
			
			provinceName = provinceCodeNameTable.get(provinceCode);
			
			Map<String,String> cityMaps = DictBanksDate.queryCityCode2NameByProvinceCode(Integer.parseInt(provinceCode), error);
			
			cityName = cityMaps.get(cityCode);
			
			userBankAccounts.cityCode = Integer.parseInt(cityCode);
			
			userBankAccounts.provinceCode = Integer.parseInt(provinceCode);
			
		}
		
		userBankAccounts.account = account;
		
		userBankAccounts.accountName = accountName;
		
		userBankAccounts.bankCode = bankCode;
		
		userBankAccounts.bankName = bankName;
		
		userBankAccounts.branchBankName = branchBankName;
		
		userBankAccounts.city = cityName;
		
		userBankAccounts.province = provinceName;
		
		userBankAccounts.updateUserBankAccountForPlatform(error);
		
		renderJSON(error);
		
	}
	
	/**
	 * 通过省份code获取城市code-name
	 */
	public static void queryCityCode2NameByProvinceCode(int provinceCode){
		ErrorInfo error = new ErrorInfo();
		Map<String,String> cityMaps = DictBanksDate.queryCityCode2NameByProvinceCode(provinceCode, error);
		JSONArray array = buildJSONArrayByMaps(cityMaps);
		renderJSON(array);
	}
	
	/**
	 * Map集合组装公用JSONArray(name-value键值对)
	 * @param maps
	 * @return
	 */
	//普通key
	private static final String NORMAL_KEY = "name";
	
	//普通value
	private static final String NORMAL_VALUE = "value";
	
	private static JSONArray buildJSONArrayByMaps(Map<String,String> maps){
		Set<Entry<String, String>> set = maps.entrySet();
		JSONArray array = new JSONArray();
		for(Entry<String, String> entry : set){
			JSONObject obj = new JSONObject();
			obj.put(NORMAL_KEY,  entry.getKey());
			obj.put(NORMAL_VALUE, entry.getValue());
			array.add(obj);
		}
		return array;
	}

	public static void main(String[] args) {
		String[] phones = StringUtils.split("13868033674,18958056908,13173676465", ",");
		for(String phone : phones) {
			Logger.info("还款通知结算: "+phone+"; 内容:");
		}
	}
}
