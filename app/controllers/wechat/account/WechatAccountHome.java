package controllers.wechat.account;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.t_content_advertisements;
import models.t_dict_ad_citys;
import models.t_dict_ad_provinces;
import models.t_dict_cars;
import models.t_dict_educations;
import models.t_dict_houses;
import models.t_dict_maritals;
import models.t_dict_payment_gateways;
import models.t_invests;
import models.t_products;
import models.t_user_vip_records;
import models.v_bid_auditing;
import models.v_bid_fundraiseing;
import models.v_bid_not_through;
import models.v_bid_release_funds;
import models.v_bid_repayment;
import models.v_bid_repaymenting;
import models.v_bill_detail;
import models.v_bill_invest;
import models.v_bill_invest_detail;
import models.v_bill_loan;
import models.v_debt_user_receive_transfers_management;
import models.v_debt_user_transfer_management;
import models.v_invest_records;
import models.v_messages_wechat_inbox;
import models.v_receiving_invest_bids;
import models.v_user_audit_items;
import models.v_user_details;
import models.v_user_for_details;
import models.v_user_success_invest_bids;
import models.v_user_waiting_full_invest_bids;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import payment.PaymentProxy;
import play.Logger;
import play.cache.Cache;
import play.mvc.With;
import utils.Arith;
import utils.CaptchaUtil;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;
import utils.ServiceFee;
import business.Ads;
import business.BackstageSet;
import business.Bid;
import business.Bill;
import business.BillInvests;
import business.Debt;
import business.Invest;
import business.News;
import business.Product;
import business.StationLetter;
import business.TemplateEmail;
import business.User;
import business.UserAuditItem;
import business.UserBankAccounts;
import business.Vip;
import business.Optimization.UserOZ;
import constants.Constants;
import constants.IPSConstants;
import constants.Constants.RechargeType;
import controllers.BaseController;
import controllers.interceptor.WeXinInterceptor;
import controllers.wechat.service.RegistAndLogin;

/**
 * 账户中心
 * @author zhs
 *
 */
@With({WeXinInterceptor.class})
public class WechatAccountHome extends BaseController {

	/**
	 *账户中心
	 */
	public static void accountInfo(){
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		if(user == null){
			RegistAndLogin.login();
		}
		
		int unreadSystemMsgCount = StationLetter.queryUserUnreadSystemMsgsCount(user.id, error);
		int unreadInboxMsgCount = StationLetter.queryUserUnreadInboxMsgsCount(user.id, error);
		
		int unReadCount = unreadSystemMsgCount + unreadInboxMsgCount;
		
		
		UserOZ accountInfo = new UserOZ(user.id);
		
		render(unReadCount, accountInfo, user);
	}
	
	/**
	 * 微信收件箱（含系统消息和站内信）
	 * 
	 * @param page
	 * @param pageSize
	 */
	public static void message(int currPage, int mark) {
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		int pageSize = Constants.WECHAT_PAGESIZE;
		
		if(user == null){
			RegistAndLogin.login();
		}
		
		PageBean<v_messages_wechat_inbox> pageBean = 
				StationLetter.queryWechatInboxMsgs(user.id, currPage, pageSize, "", 0, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		if(mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", pageBean);
			
			renderJSON(json);
		}
		
		render(pageBean);
	}
	
	/**
	 * 站内信详情
	 * 
	 * @param page
	 * @param pageSize
	 */
	public static void messageDetails(int index, String keyword, int readStatus) {
		ErrorInfo error = new ErrorInfo();
		long userId = User.currUser().id;
		
		String userName = User.currUser().name;
		PageBean<v_messages_wechat_inbox> pageBean = 
				StationLetter.queryWechatInboxMsgDetail(userId, index, keyword, readStatus, error);

		render(pageBean, userName);
	}
	
	/**
	 * 银行卡管理
	 */
	public static void bankManage(){
		User user = User.currUser();
		
		List<UserBankAccounts> banks = UserBankAccounts.queryUserAllBankAccount(user.id);
		
		render(banks);
	}
	
	/**
	 * 编辑银行卡
	 */
	public static void saveBank(long editAccountId, String editBankName, String editAccount, String editAccountName){
        ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		
		UserBankAccounts userAccount = new UserBankAccounts();
		
		userAccount.bankName = editBankName;
		userAccount.account = editAccount;
		userAccount.accountName = editAccountName;

		userAccount.editUserBankAccount(editAccountId, user.id, false, error);  //目前不支持提现银行卡支行信息
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 添加银行账号
	 */
	public static void addBank(String addBankName, String addAccount, String addAccountName){
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		UserBankAccounts bankUser =  new UserBankAccounts();
		
		bankUser.userId = user.id;
		bankUser.bankName = addBankName;
		bankUser.account = addAccount;
		bankUser.accountName = addAccountName;
		
		bankUser.addUserBankAccount(error, false);  //目前不支持提现银行卡支行信息
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 安全设置
	 */
	public static void securitySet(){
		User user = User.currUser();

        if(null == user){
			
			errorShow("");
		}
		
		render(user);
	}
	
	/**
	 * 保存手机号码
	 * @param code
	 * @param mobile
	 */
	public static void saveMobile(String code, String mobile, String code2, String randomID) {
		ErrorInfo error = new ErrorInfo();
		
		/**
		 * 是否需要验证图形验证码
		 */
		if (Constants.CHECK_PIC_CODE) {
			
			if (!code2.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) {
				flash.error("图形验证码输入有误");
				
				securitySet();
			}
		}
		
		User user = new User();
		user.id = User.currUser().id;
		
		user.mobile = mobile;
		user.editMobile(code, error);
		
		flash.error(error.msg);
		
		securitySet();
	}
	
	/**
	 * 添加支付密码
	 * @param payPasswrod
	 * @param confirmPayPasswrod
	 */
	public static void addPayPassword(String payPasswrod, String confirmPayPasswrod, 
			String encryString){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		user.addPayPassword(true, payPasswrod, confirmPayPasswrod, error);
		
		flash.error(error.msg);
		
		securitySet();
	}
	
	/**
	 * 保存支付密码
	 * @param oldPassword
	 * @param newPassword1
	 * @param newPassword2
	 */
	public static void savePayPassword(String oldPwd, String newPwd, 
			String confirmPwd, String encryString){
		ErrorInfo error = new ErrorInfo();
		
		if(oldPwd.equalsIgnoreCase(newPwd)){
			flash.error("新密码与原密码一样，请重新输入");
			securitySet();
		}
		
		User user = new User();
		user.id = User.currUser().id;
		user.editPayPassword(oldPwd, newPwd, confirmPwd, error);
		
		flash.error(error.msg);
		
		securitySet();
	}
	
	/**
	 * 保存邮箱
	 */
	public static void saveEmail(String email){
		ErrorInfo error = new ErrorInfo();
		
		User user = new User();
		user.id = User.currUser().id;
		user.email = email;
		
		if(user.editEmail(error) < 0) {
			flash.error(error.msg);
			securitySet();
		}
		
		TemplateEmail.activeEmail(user, error);
		
		flash.error(error.msg);
		
		securitySet();		
	}
	
	/**
	 * 保存密码
	 * @param password
	 * @param confirmPassword
	 */
	public static void savePassword(String password, 
			String confirmPassword, String encryString){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		User user = User.currUser();
		user.editWechatPassword(password, confirmPassword, error);
		
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 充值
	 */
	public static void recharge(){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		
		if(user.isActivityPush){
			
		}
		
//		if (Constants.IPS_ENABLE) {
//			
//			List<Map<String, Object>> bankList = null;
//			String version = BackstageSet.getCurrentBackstageSet().entrustVersion;
//			
//			if("1.0".equals(version)) {
//				bankList = Payment.getBankList(error);
//			}
//			
//			render("@front.account.FundsManage.rechargeIps",user, bankList, version);
//		}
		
		List<t_dict_payment_gateways> payType = user.gatewayForUse(error);
		
		render(user, payType);
	}
	
	/**
	 * 微信确认充值
	 */
	//@SubmitOnly
	public static void submitRecharge(int type, double money, int bankType){
		ErrorInfo error = new ErrorInfo();
		
		if (Constants.IPS_ENABLE) {
			String bankCode = params.get("bankCode");
			
			if (money <= 0 || money > Constants.MAX_VALUE) {
				flash.error("充值金额范围需在[0~" + Constants.MAX_VALUE + "]之间");
				recharge();
			}
			
			if ((StringUtils.isBlank(bankCode) || bankCode.equals("0")) && "1.0".equals(BackstageSet.getCurrentBackstageSet().entrustVersion)) {
				flash.error("请选择充值银行");
				recharge();
			}
			PaymentProxy.getInstance().recharge(error, Constants.WECHAT, bankCode, money);
			
			flash.error(error.msg);
			recharge();
		}
		
		flash.put("type", type);
		flash.put("money", money);
		flash.put("bankType",bankType);
		
		if(type<1 || type >2) {
			flash.error("请选择正确的充值方式");
			recharge();
		}
		
		if(money <= 0 || money > Constants.MAX_VALUE) {
			flash.error("充值金额范围需在[0~" + Constants.MAX_VALUE + "]之间");
			recharge();
		}
		
		BigDecimal moneyDecimal = new BigDecimal(money);
		
		if(moneyDecimal.compareTo(new BigDecimal("0.02")) < 0) {
			flash.error("请输入正确的充值金额");
			recharge();
		}
		
		if(type == 2) {
			Map<String, String> args = User.ipay(moneyDecimal, bankType, RechargeType.Normal, Constants.RECHARGE_WECHAT, Constants.CLIENT_WECHAT, error);
			
			if(error.code < 0) {
				flash.error(error.msg);
				recharge();
			}
			
			render("@front.account.FundsManage.submitRecharge",args);
		}
		
		if(type == 1) {
			Map<String, String> args = User.gpay(moneyDecimal, bankType, RechargeType.Normal, Constants.RECHARGE_WECHAT, Constants.CLIENT_WECHAT, error);
			
			if(error.code != 0) {
				flash.error(error.msg);
				recharge();
			}
			
			render("@front.account.FundsManage.submitRecharge2",args);
		}
		
	}
	
	/**
	 * 提现
	 */
	public static void withdraw(){
		User user = new User();
		user.id = User.currUser().id;
		ErrorInfo error = new ErrorInfo();
		
//		String type = params.get("type");
//		String currPage = params.get("currPage");
//		String pageSize = params.get("pageSize");
//		String beginTime = params.get("startDate");
//		String endTime = params.get("endDate");
		
		double amount = User.queryRechargeIn(user.id, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		double withdrawalAmount = user.balance - amount;
		
        if (!Constants.IS_WITHDRAWAL_INNER || (!Constants.IPS_ENABLE && Constants.WITHDRAWAL_PAY_TYPE == Constants.ONE)) {
			
			withdrawalAmount = ServiceFee.maxWithdralAmount(withdrawalAmount);
		}
		
		//最多提现金额上限
		double maxWithDrawalAmount = Constants.MAX_VALUE;
		
		if(withdrawalAmount < 0) {
			withdrawalAmount = 0;
		}
		
		List<UserBankAccounts> banks = UserBankAccounts.queryUserAllBankAccount(user.id);
		
		render(user, withdrawalAmount, maxWithDrawalAmount, banks);
	}
	
	/**
	 * 确认提现
	 */
	public static void submitWithdrawal(double amount, long bankId, String payPassword, int type, String ipsSelect){
		ErrorInfo error = new ErrorInfo();
		boolean flag = false;
		
		if(StringUtils.isNotBlank(ipsSelect) && ipsSelect.equals("1")) {
			flag = true;
		}
		if(amount <= 0) {
			flash.error("请输入提现金额");
			withdraw();
		}
		
		if(amount > Constants.MAX_VALUE) {
			flash.error("已超过最大充值金额" +Constants.MAX_VALUE+ "元");
			withdraw();
		}
		
		if (!(Constants.IPS_ENABLE && flag)) {
			if(StringUtils.isBlank(payPassword)) {
				flash.error("请输入交易密码");
				withdraw();
			}
			
			if(type !=1 && type != 2) {
				flash.error("传入参数有误");
				withdraw();
			}
			
			if(bankId <= 0) {
				flash.error("请选择提现银行");
				withdraw();
			}
		}
		
		User user = new User();
		user.id = User.currUser().id;
		
		long withdrawalId = user.withdrawal(amount, bankId, payPassword, type, flag, error);
		
		if(error.code < 0){
			flash.error(error.msg);
			withdraw();
		}
		
		if(Constants.IPS_ENABLE && flag) {
			if(error.code < 0) {
				flash.error(error.msg);
				withdraw();
			}
			
			PaymentProxy.getInstance().withdraw(error, Constants.WECHAT, withdrawalId, amount);
			
			flash.error(error.msg);
			withdraw();
		}
		
		if (error.code < 0) {
			flash.error(error.msg);
			withdraw();
		}
		
		flash.error(error.msg);
		
		accountInfo();
	}
	
	/**
	 * 交易记录
	 */
	public static void dealRecord(int type, String beginTime, String endTime, int currPage, int pageSize, int Mark){
		User user = User.currUser();
		PageBean<v_user_details> page = User.queryUserDetails(user.id, type, beginTime, endTime,currPage, pageSize);
		
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		
		render(page);
	}
	
	/**
	 * 我的借款标
	 */
	public static void myLoanBids(int condition, String keyWord, int currPage, int mark){
		switch(condition){
		case 0:
			auditingLoanBids(currPage, Constants.WECHAT_PAGESIZE, condition, keyWord, mark);
			break;
		case 1:
			loaningBids(currPage, Constants.WECHAT_PAGESIZE, condition, keyWord, mark);
			break;
		case 2:
			readyReleaseBid(currPage, Constants.WECHAT_PAGESIZE, condition, keyWord, mark);
			break;
		case 3:
			repaymentBids(currPage, Constants.WECHAT_PAGESIZE, condition, keyWord, mark);
			break;
		case 4:
			successBids(currPage, Constants.WECHAT_PAGESIZE, condition, keyWord, mark);
			break;
		case 5:
			failBids(currPage, Constants.WECHAT_PAGESIZE, condition, keyWord, mark);
			break;
		}
		
		render();
	}
	
	/**
	 * 审核中的借款标列表
	 */
	public static void auditingLoanBids(int currPage, int pageSize, int condition, String keyword, int mark) {
		ErrorInfo error = new ErrorInfo();
		String userId = String.valueOf(User.currUser().id);
		
		if(currPage <= 0){
			currPage = 1;
		}
		
		PageBean<v_bid_auditing> pageBean = new PageBean<v_bid_auditing>();
		pageBean.currPage = currPage;
		pageBean.pageSize = Constants.WECHAT_PAGESIZE;
		

		Map<String, Object> map = new HashMap<String,Object>();
		map.put("userId", userId);
		map.put("keyword", keyword);

		if(StringUtils.isBlank(keyword)){
    		map.put("condition", null);
        	pageBean.page = Bid.queryBidAuditing (pageBean, error, map);
        	
	    }else{
	    	map.put("condition", Constants.WECHAT_BID_SEARCH);
	    	pageBean.page = Bid.queryBidAuditing (pageBean, error, map);
	    }

		if (null == pageBean.page) 
			render(Constants.ERROR_PAGE_PATH_FRONT);  
		
		if(mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", pageBean);
			
			renderJSON(json);
		}

		render(pageBean, keyword, condition);
	}
	
	/**
	 * 等待满标的借款标列表
	 */
	public static void loaningBids(int currPage, int pageSize, int condition, String keyword, int mark){
		ErrorInfo error = new ErrorInfo();
		
		String userId = String.valueOf(User.currUser().id);
		
		if(currPage <= 0){
			currPage = 1;
		}
		
		PageBean<v_bid_fundraiseing> pageBean = new PageBean<v_bid_fundraiseing>();
		pageBean.currPage = currPage;
		pageBean.pageSize = Constants.WECHAT_PAGESIZE;
		
		if(StringUtils.isBlank(keyword)){
			pageBean.page = Bid.queryBidFundraiseing(pageBean, -1, error, userId, null, keyword, "", "", "");
			
		}else{
			pageBean.page = Bid.queryBidFundraiseing(pageBean, -1, error, userId, Constants.WECHAT_BID_SEARCH, keyword, "", "", "");
		}
		
		
		if (null == pageBean.page)
			render(Constants.ERROR_PAGE_PATH_FRONT);  
		
		if(mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", pageBean);
			
			renderJSON(json);
		}

		render(pageBean, keyword, condition);
	}
	
	/**
	 * 待放款的借款标列表
	 */
	public static void readyReleaseBid(int currPage, int pageSize, int condition, String keyword, int mark){
		ErrorInfo error = new ErrorInfo();
		
		String userId = String.valueOf(User.currUser().id);
		
		if(currPage <= 0){
			currPage = 1;
		}
		
		PageBean<v_bid_release_funds> pageBean = new PageBean<v_bid_release_funds>();
		pageBean.currPage = currPage;
		pageBean.pageSize = Constants.WECHAT_PAGESIZE;
		
        if(StringUtils.isBlank(keyword)){
        	pageBean.page = Bid.queryReleaseFunds(0, pageBean, Constants.BID_EAIT_LOAN, error, userId, null, keyword, "", "", "");
			
		}else{
			pageBean.page = Bid.queryReleaseFunds(0, pageBean, Constants.BID_EAIT_LOAN, error, userId, Constants.WECHAT_BID_SEARCH, keyword, "", "", "");
		}
		
		if (null == pageBean.page)
			errorShow(error.msg);  
		
		if(mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", pageBean);
			
			renderJSON(json);
		}

		render(pageBean, keyword);
	}
	
	/**
	 * 还款中的借款标列表
	 */
	public static void repaymentBids(int currPage, int pageSize, int condition, String keyword, int mark){
		ErrorInfo error = new ErrorInfo();
		
		String userid = User.currUser().id + "";
		
		if(currPage <= 0){
			currPage = 1;
		}
		
		PageBean<v_bid_repaymenting> pageBean = new PageBean<v_bid_repaymenting>();
		pageBean.currPage = currPage;
		pageBean.pageSize = Constants.WECHAT_PAGESIZE;
		
        if(StringUtils.isBlank(keyword)){
			pageBean.page = Bid.queryBidRepaymenting(0, pageBean, 0, error, userid, null, keyword, "", "", "");
			
		}else{
			pageBean.page = Bid.queryBidRepaymenting(0, pageBean, 0, error, userid, Constants.WECHAT_BID_SEARCH, keyword, "", "", "");
		}
		
		if(mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", pageBean);
			
			renderJSON(json);
		}

		render(pageBean, keyword, condition);
	}
	
	/**
	 * 已成功的借款标列表
	 */
	public static void successBids(int currPage, int pageSize, int condition, String keyword, int mark){
		ErrorInfo error = new ErrorInfo();

		String userid = User.currUser().id + "";
		
		if(currPage <= 0){
			currPage = 1;
		}
		
		PageBean<v_bid_repayment> pageBean = new PageBean<v_bid_repayment>();
		pageBean.currPage = currPage;
		pageBean.pageSize = Constants.WECHAT_PAGESIZE;
		
        if(StringUtils.isBlank(keyword)){
        	pageBean.page = Bid.queryBidRepayment(0, pageBean, 0, error, userid, null, keyword, "", "", "");
			
		}else{
			pageBean.page = Bid.queryBidRepayment(0, pageBean, 0, error, userid, Constants.WECHAT_BID_SEARCH, keyword, "", "", "");
		}
		
		pageBean.page = Bid.queryBidRepayment(0, pageBean, 0, error, userid, Constants.WECHAT_BID_SEARCH, keyword, "", "", "");
		
		if(mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", pageBean);
			
			renderJSON(json);
		}

		render(pageBean, keyword, condition);
	}
	
	/**
	 * 失败的借款标列表
	 */
	public static void failBids(int currPage, int pageSize, int condition, String keyword, int mark) {
		ErrorInfo error = new ErrorInfo();

		String userid = User.currUser().id + "";
		
		if(currPage <= 0){
			currPage = 1;
		}
		
		PageBean<v_bid_not_through> pageBean = new PageBean<v_bid_not_through>();
		pageBean.currPage = currPage;
		pageBean.pageSize = Constants.WECHAT_PAGESIZE;
		
        if(StringUtils.isBlank(keyword)){
        	pageBean.page = Bid.queryBidNotThrough(0, pageBean, error, userid, null, keyword, "", "", "");
		}else{
			pageBean.page = Bid.queryBidNotThrough(0, pageBean, error, userid, Constants.WECHAT_BID_SEARCH, keyword, "", "", "");
		}
		
		if(mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", pageBean);
			
			renderJSON(json);
		}
			
		render(pageBean, keyword, condition);
	}
	
	/**
	 * 我的借款标详情
	 */
	public static void myLoanBidDetails(String sign){
		ErrorInfo error = new ErrorInfo();
		
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.id = bidId;
		
		double principal = 0;

		if (error.code < 0) {
			flash.error(error.msg);
		}
		
		if(bid.status == Constants.BID_REPAYMENT || bid.status == Constants.BID_COMPENSATE_REPAYMENT || bid.status == Constants.BID_REPAYMENTS){
			
			principal = Bill.queryBidPrincipal(bidId, error);
		}else{
			
			principal = bid.amount + ServiceFee.interestCompute(bid.amount, bid.apr, bid.periodUnit, bid.period, (int)bid.repayment.id);
		}
		
		
		if(error.code < 0){
			flash.error(error.msg);
		}
		
		List<UserAuditItem> uItems = UserAuditItem.queryUserAllAuditItem(
				bid.userId, bid.mark); // 用户正对产品上传的资料集合

		if (error.code < 0) {
			flash.error(error.msg);
		}
		
		render(bid, uItems, principal);
	}
	
	/**
	 * 我的借款账单
	 */
	public static void myLoanBills(int payType, int isOverType, String key, int currPage, int mark){
        ErrorInfo error = new ErrorInfo();
        error.clear();
		
		User user = User.currUser();
		PageBean<v_bill_loan> page = Bill.queryMyLoanBills(user.id, payType, isOverType, Constants.WECHAT_BILL_SEARCH, key, currPage, 10, error);
		
		if(error.code < 0){
			errorShow(error.msg);
		}
		
		if(mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		
		render(page, key, payType, isOverType);
	}
	
	/**
	 * 我的借款账单详情
	 */
	public static void myLoanBillDetails(String billId){
        ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		boolean isDealPassword = false;
		
		try {
			isDealPassword = t_products.find("select is_deal_password from t_products where id = (select product_id from t_bids where id = (select bid_id from t_bills where id = ?))", id).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		User user = User.currUser();
		
		v_bill_detail billDetail = Bill.queryBillDetails(id, user.id, error);
		
		int checkPeriod = Bill.checkPeriod(billDetail.bid_id, billDetail.current_period);
		
		render(billDetail, user, isDealPassword, checkPeriod);
	}
	
	/**
	 * 确认还款
	 */
	public static void submitRepayment(String payPassword, double amount, String billId){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		if(error.code < 0){
			json.put("error", error);
			renderJSON(json);
		}
		
		User user = User.currUser();
		
		boolean isDealPassword = false;
		
		try {
			isDealPassword = t_products.find("select is_deal_password from t_products where id = (select product_id from t_bids where id = (select bid_id from t_bills where id = ?))", id).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "还款失败";
			flash.error(error.msg);
			
			json.put("error", error);
			renderJSON(json);
		}
		
		if (isDealPassword) {
			payPassword = payPassword.replaceAll(" ", "");
			
			int code = user.verifyPayPassword(payPassword, error);
			
			if(code < 0){
				json.put("error", error);
				renderJSON(json);
			}
		}
		
		Bill bill = new Bill();
		bill.setId(id);
		
		/* 2014-12-29 限制还款需要从第一期逐步开始还款 */
		if(bill.checkPeriod(bill.bidId, bill.periods) > 0){
			error.msg = "请您从第一期逐次还款!";
			json.put("error", error);
			renderJSON(json);
		}
		
		/*本金垫付还款*/
		if (Constants.IPS_ENABLE && bill.status == Constants.ADVANCE_PRINCIIPAL_REPAYMENT) {
			String pMerBillNo = Bill.getRepaymentBillNo(error, id);
			
			//本金垫付
			Map<String,String> args = null;//Payment.compensateRepayment(pMerBillNo, id, error);
			
			if(error.code == 100) {
				render("front/account/PaymentAction/loan.html" ,args);
			}
			
			json.put("error", error);
			renderJSON(json);
		}
		
		Map<String, List<Map<String, Object>>> mapList = bill.repayment(user.id, error);

		/* 余额不足则提示跳转去充值页面，故此不能把错误值放入flash.error */
		if(error.code == Constants.BALANCE_NOT_ENOUGH) {
			flash.put("notEnough", -999);
			myLoanBillDetails(billId);
		}
		
		if(error.code < 0){
			json.put("error", error);
			renderJSON(json);
		}
		
//		if(Constants.IPS_ENABLE) {
//			Map<String, String> args = Payment.repaymentNewTrade(mapList, id, error);
//			
//			if(error.code < 0){
//				flash.error(error.msg);
//				myLoanBillDetails(billId);
//			}
//			
//			render("@front.account.PaymentAction.repaymentNewTrade", args);
//		}
		
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 我的理财账单
	 */
	public static void myInvestBills(int payType, int isOverType, String key, int currPage, int mark){
        ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		long userId= user.id;
		PageBean<v_bill_invest> page = BillInvests.queryMyInvestBills(payType, isOverType, Constants.WECHAT_BILL_SEARCH, key, currPage, userId, error);
		
		if(mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		
		render(page, key, payType, isOverType);
	}
	
	/**
	 * 我的理财账单详情
	 */
	public static void myInvestBillDetails(String investId){
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(investId, Constants.BILL_ID_SIGN, 3600, error);
		
		User user = User.currUser();
		
		v_bill_invest_detail investDetail = BillInvests.queryMyInvestBillDetails(id, user.id, error);
		
		render(investDetail);
	}
	
	/**
	 * 上传认证资料
	 */
	public static void uploadAuthDatas(String currPage, String pageSize,
			String status, String startDate, String endDate, String productId,
			String productType, int mark){
        ErrorInfo error = new ErrorInfo();
		
		long userId = User.currUser().id;
		
		/* 产品列表 */
		List<Product> products = Product.queryProductNames(true, error);
		PageBean<v_user_audit_items> pageBean = UserAuditItem.queryUserAuditItem("1", "100", userId, error, status, startDate, endDate, productId, productType);
		
		if(mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", pageBean);
			
			renderJSON(json);
		}
		
		render(pageBean, products, productId, productType, status);
	}
	
	/**
	 * 清空用户上传未付款的资料
	 */
	public static void clearUploadedItems() {
		ErrorInfo error = new ErrorInfo();
		UserAuditItem.clearUploadedItems(User.currUser().id, error);
		flash.error(error.msg);
		
		uploadAuthDatas(null, null, null, null, null, null, null, 0);
	}
	
	/**
	 * 提交已上传的资料
	 */
	public static void submitUploadedItems() {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> info = UserAuditItem.queryUploadItems(User.currUser().id, error);
		
		if (error.code < 0) {
			flash.error(error.msg);
			
			uploadAuthDatas("1", "10", "", "", "", "", "", 0);
		}
		
		/* 2014-11-18把单个提交修改为组提交 */
		double balance = 0;
		double fees = (Double) info.get("fees");
		User user = User.currUser();
		v_user_for_details details = user.balanceDetail;
		
		if(null == details) {
			flash.error("查询用户资金出现错误!");
			
			uploadAuthDatas("1", "10", "", "", "", "", "", 0);
		}
		balance = details.user_amount;
		
		if(fees > balance){
			error.code = Constants.BALANCE_NOT_ENOUGH;
			error.msg = "对不起，您可用余额不足";
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("rechargeType", RechargeType.UploadItems);
			map.put("fee", fees);
			Cache.set("rechargePay" + user.id, map, IPSConstants.CACHE_TIME);
			flash.put("code", Constants.BALANCE_NOT_ENOUGH);
			flash.put("msg", "请支付资料审核费");
			
			uploadAuthDatas("1", "10", "", "", "", "", "", 0);
		} else {
			UserAuditItem.submitUploadedItems(user.id, balance, error);
			flash.error(error.msg);
			
			uploadAuthDatas("1", "10", "", "", "", "", "", 0);
		}
		uploadAuthDatas("1", "10", null, null, null, null, null, 0);
	}
	
	/**
	 * 我的理财标
	 */
	public static void myInvestBids(int condition, int currPage, String keyWords, int mark){
		if(currPage == 0){
			
			currPage = 1;
		}
		
		switch(condition){
		case 0:
			loaningInvestBid(condition, currPage, Constants.WECHAT_PAGESIZE, keyWords, mark);
			break;
		case 1:
			repayingInvestBid(condition, currPage, Constants.WECHAT_PAGESIZE, keyWords, mark);
			break;
		case 2:
			successInvestBid(condition, currPage, Constants.WECHAT_PAGESIZE, keyWords, mark);
			break;
		case 3:
			readyReleaseInvestBid(condition, currPage, Constants.WECHAT_PAGESIZE, keyWords, mark);
			break;
		}
		
		render();
	}
	
	/**
	 * 待放款的理财标
	 */
	public static void readyReleaseInvestBid(int condition, int currPage, int pageSize, String keyWords, int mark) {
		Bid bid = new Bid();
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		PageBean<v_user_waiting_full_invest_bids> page = Invest.queryUserReadyReleaseBid(user.id, Constants.WECHAT_INVEST_BID_SEARCH, keyWords, currPage == 0 ? 1 : currPage, pageSize == 0 ? Constants.PAGE_SIZE : pageSize,error);
		
		if(error.code < 0){
			errorShow(error.msg);
		}
		
		if(page.page.size() >= 1){
			bid.auditBid = true;
			bid.id = page.page.get(0).id;
		}
		
		if(mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		
		render(page, keyWords, bid, condition);
	}
	
	/**
	 *已完成的理财标页面
	 */
	public static void successInvestBid(int condition, int currPage, int pageSize, String keyWords, int mark){
		ErrorInfo error = new ErrorInfo();
		Bid bid = new Bid();
		User user = User.currUser();
		
		PageBean<v_user_success_invest_bids> page = Invest.queryUserSuccessInvestBids(user.id, Constants.WECHAT_INVEST_BID_SEARCH, keyWords, currPage == 0 ? 1 : currPage, pageSize == 0 ? Constants.PAGE_SIZE : pageSize, error);
		
		if(error.code < 0){
			errorShow(error.msg);
		}
		
		if(page.page.size() >= 1){
			bid.auditBid = true;
			bid.id = page.page.get(0).id;
		}
		
		if(mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		
		render(page, keyWords, bid, condition);
	}
	
	/**
	 * 收款中的理财标
	 * @param result
	 */
	public static void repayingInvestBid(int condition, int currPage, int pageSize, String keyWords, int mark){
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		Bid bid = new Bid();
		
		PageBean<v_receiving_invest_bids> page = Invest.queryUserAllReceivingInvestBids(user.id, Constants.WECHAT_INVEST_BID_SEARCH, keyWords, currPage, pageSize,error);
		
		if(error.code < 0){
			errorShow(error.msg);
		}
		
		if(page.page.size() >= 1){
			bid.auditBid = true;
			bid.id = page.page.get(0).id;
		}
		
		if(mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		
		boolean IsDebtFlag = constants.Constants.IS_DEBT_TWO;
		
		render(page, keyWords, bid, condition, IsDebtFlag);
	}
	
	/**
	 * 进入等待满标的理财标页面
	 */
	public static void loaningInvestBid(int condition, int currPage, int pageSize, String keyWords, int mark){
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		Bid bid = new Bid();
		
		PageBean<v_user_waiting_full_invest_bids> page = Invest.queryUserWaitFullBids(user.id, Constants.WECHAT_INVEST_BID_SEARCH, keyWords, currPage, pageSize,error);
		
		if(error.code < 0){
			errorShow(error.msg);
		}
		
		if(page.page.size() >= 1){
			bid.auditBid = true;
			bid.id = page.page.get(0).id;
		}
		
		if(mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		
		render(page, keyWords, bid, condition);
	}
	
	/**
	 * 我的理财标详情
	 */
	public static void myInvestBidDetails(long bidId){
		ErrorInfo error = new ErrorInfo();
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.id = bidId;

		/* 进入详情页面增加浏览次数 */
		Invest.updateReadCount(bidId, error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}

		Map<String,String> historySituationMap = User.historySituation(bid.userId,error);//借款者历史记录情况
		List<UserAuditItem> uItems = UserAuditItem.queryUserAllAuditItem(
				bid.userId, bid.mark); // 用户正对产品上传的资料集合

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		User user = User.currUser();

		/*
		 * 查询截止时间
		 */
		long endTime = 0;
		if (bid != null && bid.investExpireTime != null) {
			endTime = bid.investExpireTime.getTime();
		}
		
		/*
		 * 查找前10条投标记录
		 */
		PageBean<v_invest_records> pageBean = new PageBean<v_invest_records>();
		pageBean = Invest.queryBidInvestRecords(Constants.WECHAT_CURRPAGE,
				Constants.WECHAT_PAGESIZE, bidId, error);

		render(bid, uItems, user, endTime, pageBean, historySituationMap);
	}
	
	/**
	 * 债权转让管理
	 */
	public static void debeManage(int conditions, String message){
		switch(conditions){
		case 1:
			transferDebts(Constants.WECHAT_CURRPAGE, Constants.WECHAT_CURRPAGE, "0", "0", "", 0);
			break;
		case 2:
			receivedDebts(Constants.WECHAT_CURRPAGE, Constants.WECHAT_CURRPAGE, "0", "", 0, message);
			break;
		}
		
		render();
	}
	
	/**
	 * 债权转让
	 */
	public static void debeManageTransfer(String sign, double investAmount, double hasReceivedAmount, String title){
		
		double stillReceiveAmount = Arith.round(investAmount - hasReceivedAmount, 2);
		
		render(stillReceiveAmount, sign, title);
	}
	
	/**
	 * 确认转让债权
	 * @param investId
	 */
	public static void confirmTransfer(String sign, String specifiedUserName, String transferTitle, int period,
			String transferReason, String price, int type){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		long investId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			errorShow(error.msg);
		}
		
		t_invests invest = Invest.queryUserAndBid(investId);
		
		if(null == invest){
			errorShow(error.msg);
		}
		
		User user = User.currUser();
		
		double debtAmount = Debt.getDebtAmount(investId,error);
		
		boolean b = price.matches("^[1-9][0-9]*$");
    	if(!b){
    		error.msg = "对不起！转让底价只能输入正整数!";
			error.code = -10;
			json.put("error", error);
			
			renderJSON(json);
    	} 
    	
    	if(transferTitle != null && transferTitle.length() > 30) {
    		error.msg = "对不起！转让标题长度不能大于30!";
			error.code = -10;
			json.put("error", error);
			
			renderJSON(json);
    	}
    	
    	if(transferReason != null && transferReason.length() > 255) {
    		error.msg = "对不起！转让原因长度不能大于255!";
			error.code = -10;
			json.put("error", error);
			
			renderJSON(json);
    	}
    	
		double transferPrice = Double.parseDouble(price);
		
		if(type == Constants.DIRECTIONAL_MODE){//定向转让
			 specifiedUserName = params.get("specifiedUserName");
		}
		
		Debt.transferDebt(user.id,investId, transferTitle, transferReason, period, debtAmount, transferPrice, type, specifiedUserName, error);
		
		if(error.code < 0) {
			json.put("error", error);
			
			renderJSON(json);
    	}
		
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 债权转让管理
	 * @param pagNum
	 */
	public static void transferDebts(int currPage,int pageSize, String type, String status, String keyWords, int mark){
		User user = User.currUser();
		
		if (currPage <= 0) {
			currPage = 1;
		}
		
		pageSize = Constants.WECHAT_PAGESIZE;
		PageBean<v_debt_user_transfer_management> page = Debt.queryUserAllDebtTransfersByConditions( user.id, Constants.WECHAT_SEARCH_TYPE, keyWords, "", currPage,pageSize);
		
		if(null == page){
			errorShow("");
		}
		
		if(mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		
	    render(page, keyWords);
	}
	
	/**
	 * 受让债权管理
	 */
	public static void receivedDebts(int currPage,int pageSize,String type,String keyWords, int mark, String message) {
		User user = User.currUser();
		
		if (currPage <= 0) {
			currPage = 1;
		}
		
		pageSize = Constants.WECHAT_PAGESIZE;
		PageBean<v_debt_user_receive_transfers_management> page = Debt.queryUserAllReceivedDebtTransfersByConditions(user.id, Constants.WECHAT_SEARCH_TYPE, keyWords, currPage,pageSize);
		
		if(null == page){
			errorShow("");
		}
		
		if(mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		
		if(message != null){
			flash.error(message);
		}
		
		render(page, keyWords);
	}
	
	/**
	 * 债权转让详情入口
	 */
	public static void debeManageDetails(String sign,int status){
		if (status == 0) {
			debtDetailsAuditing(sign);  //审核中
		}
		
		if (status == 1 || status == 2 || status == 4) {
			debtDetailsTransfering(sign);  //转让中
		}
		
		if(status == 3){
			debtDetailsSuccess( sign);  //已成功
		}
		
		if(status == -1){
			debtDetailsNoPass( sign);  //未通过
		}
		
		if(status == -2 || status == -3 || status == -5){
			debtDetailsFailure( sign);  //失败
		}
		
		render();
	}
	
	/**
	 * 受让债权详情入口
	 */
	public static void debtReceiveDetails(String sign,int type,int status){
		if(type == 1 ){  //定向转让
			directionalDebt(sign);
		}
		if(type == 2 && status == 1 ){  //竞价成功
			auctionSuccess(sign);
		}
		if(type == 2 && status == 0 ){  //竞价竞拍中
			auctionBidding(sign);
		}
		if(type == 2 && status == 3 ){  //竞价竞拍等待确认
			auctionWaitConcirm(sign);
		}
		if(status == -1){  //审核不通过
			auctionFailure(sign);
		}
		
		render();
	}
	
	/**
	 * 接收定向债权转让
	 */
	public static void acceptDebts(String sign, String dealpwd){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
//		if (Constants.IPS_ENABLE && User.currUser().getIpsStatus() != IpsCheckStatus.IPS) {
//			CheckAction.approve();
//		}
		
		long debtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			json.put("error", error);
			renderJSON(json);
		}
		
//		if (Constants.IPS_ENABLE) {
//			String pMerBillNo = Payment.createBillNo(0, IPSOperation.REGISTER_CRETANSFER);
//			Map<String, Object> map = Debt.queryTransferInfo(debtId, error);
//			
//			if (error.code < 0) {
//				return;
//			}
//			
//			Debt.updateMerBillNo(debtId, pMerBillNo, error);
//			
//			if (error.code < 0) {
//				return;
//			}
//			
//			IpsDetail.setIpsInfo(Long.parseLong(pMerBillNo), "{\"debtId\":\""+debtId+"\"}", error);
////			Cache.add(pMerBillNo, debtId, IPSConstants.CACHE_TIME);
//			
//			long bidUser = (Long) map.get("bidUser");
//			long fromUserId = (Long) map.get("fromUserId"); 
//			long toUserId = (Long) map.get("toUserId");
//			String bidNo = (String) map.get("bidNo"); 
//			String pCreMerBillNo = (String) map.get("pCreMerBillNo");
//			double managefee = (Double) map.get("managefee");
//			double pCretAmt = (Double) map.get("pCretAmt");
//			double interest = (Double) map.get("interest");
//			double pCretAmt2 = (Double) map.get("pCretAmt2");
//			double pPayAmt = (Double) map.get("pPayAmt");
//			String orderDate =  (String) map.get("orderDate");
//			String printAmt = (String) map.get("printAmt");
//			
//			IpsDetail detail = new IpsDetail();
//			detail.merBillNo = pMerBillNo;
//			detail.userName = User.queryUserNameById(toUserId, error);
//			detail.time = new Date();
//			detail.type = IPSOperation.REGISTER_CRETANSFER;
//			detail.status = Status.FAIL;
//			detail.create(error);
//			
//			if (error.code < 0) {
//				return;
//			}
//			
//			Map<String, Object> args = Payment.registerCretansfer(pMerBillNo, bidUser, fromUserId, toUserId, bidNo, pCreMerBillNo, pCretAmt, pCretAmt2, interest, pPayAmt, managefee, orderDate, printAmt);
//
//			render("@front.account.PaymentAction.registerCretansfer", args);
//		}
		
		Debt.dealDebtTransfer(null, debtId,dealpwd,false, error);
		
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 拒收定向转让债权
	 */
	public static void notAccept(String refuseSign){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		long debtId = Security.checkSign(refuseSign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		Debt.refuseAccept(debtId, error);
		
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 成交债权
	 * @param debtId
	 */
	public static void transact(String sign, String dealpwd){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		long debtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			json.put("error", error);
			renderJSON(json);
		}
		
//		if (Constants.IPS_ENABLE) {
//			String pMerBillNo = Payment.createBillNo(0, IPSOperation.REGISTER_CRETANSFER);
//			Map<String, Object> map = Debt.queryTransferInfo(debtId, error);
//			
//			if(error.code < 0){
//				flash.error(error.msg);
//				auctionWaitConcirm(sign);
//			}
//			
//			Debt.updateMerBillNo(debtId, pMerBillNo, error);
//			
//			if(error.code < 0){
//				flash.error(error.msg);
//				auctionWaitConcirm(sign);
//			}
//			
//			IpsDetail.setIpsInfo(Long.parseLong(pMerBillNo), "{\"debtId\":\""+debtId+"\"}", error);
//			
//			long bidUser = (Long) map.get("bidUser");
//			long fromUserId = (Long) map.get("fromUserId"); 
//			long toUserId = (Long) map.get("toUserId");
//			String bidNo = (String) map.get("bidNo"); 
//			String pCreMerBillNo = (String) map.get("pCreMerBillNo");
//			double managefee = (Double) map.get("managefee");
//			double pCretAmt = (Double) map.get("pCretAmt");
//			double interest = (Double) map.get("interest");
//			double pCretAmt2 = (Double) map.get("pCretAmt2");
//			double pPayAmt = (Double) map.get("pPayAmt");
//			String orderDate = (String) map.get("orderDate");
//			String printAmt = (String) map.get("printAmt");
//			
//			IpsDetail detail = new IpsDetail();
//			detail.merBillNo = pMerBillNo;
//			detail.userName = User.queryUserNameById(toUserId, error);
//			detail.time = new Date();
//			detail.type = IPSOperation.REGISTER_CRETANSFER;
//			detail.status = Status.FAIL;
//			detail.create(error);
//			
//			if (error.code < 0) {
//				return;
//			}
//			
//			Map<String, Object> args = Payment.registerCretansfer(pMerBillNo, bidUser, fromUserId, toUserId, bidNo, pCreMerBillNo, pCretAmt, pCretAmt2, interest, pPayAmt, managefee, orderDate, printAmt);
//
//			render("@front.account.PaymentAction.registerCretansfer", args);
//		}
		
		Debt.dealDebtTransfer(null, debtId,dealpwd,false, error);
		
		if(error.code < 0){
			json.put("error", error);
			renderJSON(json);
		}
		
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 受让债权管理竞价竞拍中债权详情页面
	 * @param debtId
	 */
	public static void auctionBidding(String  sign){
		ErrorInfo error = new ErrorInfo();
		long viewId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		User user = User.currUser();
		
		if(error.code < 0){
			errorShow(error.msg);
		}
		
		v_debt_user_receive_transfers_management debt = Debt.details(viewId);
		
		long userId = debt.user_id;
		long transferId = debt.transer_id;
		//目前我的竞拍出价
		Double offerPrice = Debt.getMyAuctionPrice(transferId, userId, error);
		
		if(error.code < 0){
			errorShow(error.msg);
		}
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debt.transer_id;
		
		renderArgs.put("offerPrice", offerPrice);
		renderArgs.put("user", user);
		
		Invest invest = new Invest();
		invest.id = debtBussiness.investId;
		double investAmount = invest.amount;
		
		Bid bid = new Bid();
		bid.id = invest.bidId;
		String imageUrl = bid.product.smallImageFilename;
		
		render(debt,debtBussiness, investAmount, imageUrl);
	}
	
	/**
	 * 受让债权管理竞价竞拍中债权详情页面
	 * @param debtId
	 */
	public static void auctionWaitConcirm(String sign){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		long viewId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			errorShow(error.msg);
		}
		
		v_debt_user_receive_transfers_management debt = Debt.details(viewId);
		
		long userId = debt.user_id;
		long transferId = debt.transer_id;
		Double offerPrice = Debt.getMyAuctionPrice(transferId, userId, error);  //目前我的竞拍出价
		
		if(error.code < 0){
			errorShow(error.msg);
		}
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debt.transer_id;
		
		renderArgs.put("offerPrice", offerPrice);
		renderArgs.put("user", user);
		
		Invest invest = new Invest();
		invest.id = debtBussiness.investId;
		double investAmount = invest.amount;
		
		Bid bid = new Bid();
		bid.id = invest.bidId;
		String imageUrl = bid.product.smallImageFilename;
		
		boolean isHasPass = debtBussiness.invest.bid.product.isDealPassword;
		
		render(debt,debtBussiness, investAmount, imageUrl, user, isHasPass);
	}
	
	/**
	 * 受让债权管理审核不通过债权详情页面
	 * @param debtId
	 */
	public static void auctionFailure(String sign){
		ErrorInfo error = new ErrorInfo();
		long viewId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			errorShow(error.msg);
		}
		
		v_debt_user_receive_transfers_management debt = Debt.details(viewId);
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debt.transer_id;
		
		Invest invest = new Invest();
		invest.id = debtBussiness.investId;
		double investAmount = invest.amount;
		
		Bid bid = new Bid();
		bid.id = invest.bidId;
		String imageUrl = bid.product.smallImageFilename;
		
		render(debt,debtBussiness, investAmount, imageUrl);
	}
	
	/**
	 * 受让债权管理竞价成功债权详情页面
	 * @param debtId
	 */
	public static void auctionSuccess(String sign){
		ErrorInfo error = new ErrorInfo();
		long viewId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			errorShow(error.msg);
		}
		
		v_debt_user_receive_transfers_management debt = Debt.details(viewId);
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debt.transer_id;
		
		Invest invest = new Invest();
		invest.id = debtBussiness.investId;
		double investAmount = invest.amount;
		
		Bid bid = new Bid();
		bid.id = invest.bidId;
		String imageUrl = bid.product.smallImageFilename;
		
        User user= new User();
		
		if(debtBussiness.type == 1){
			
			user.id = debtBussiness.specifiedUserId;
		}else{
			
			user.id = debtBussiness.transactionUserId;
		}
		
		String specifiedName = user.name;
		
		render(debt,debtBussiness, investAmount, imageUrl, specifiedName);
	}
	
	/**
	 * 受让债权管理定向转让债权详情页面
	 * @param debtId
	 */
	public static void directionalDebt(String sign){
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		long viewId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			errorShow(error.msg);
		}
		v_debt_user_receive_transfers_management debt = Debt.details(viewId);
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debt.transer_id;
		
		Invest invest = new Invest();
		invest.id = debtBussiness.investId;
		double investAmount = invest.amount;
		
		Bid bid = new Bid();
		bid.id = invest.bidId;
		String imageUrl = bid.product.smallImageFilename;
		
		render(debt,debtBussiness, investAmount, imageUrl, user);
	}
	
	/**
	 * 转让债权管理审核中的 
	 * @param debtId
	 */
	public static void debtDetailsAuditing(String sign){
		ErrorInfo error = new ErrorInfo();
		
		long viewDebtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || viewDebtId <= 0){
			errorShow(error.msg);
		}
		
		v_debt_user_transfer_management debt = Debt.transferDetails(viewDebtId,error);
		
		if(error.code < 0){
			errorShow(error.msg);
		} 
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = viewDebtId;
		
		Invest invest = new Invest();
		invest.id = debtBussiness.investId;
		double investAmount = invest.amount;
		
		Bid bid = new Bid();
		bid.id = invest.bidId;
		String imageUrl = bid.product.smallImageFilename;
		
		render(debt, debtBussiness, investAmount, imageUrl);
	}
	
	/**
	 * 转让债权管理转让中的详情页面
	 * @param debtId
	 */
	public static void debtDetailsTransfering(String sign){
		ErrorInfo error = new ErrorInfo();
		
		long viewDebtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			errorShow(error.msg);
		}
		
		v_debt_user_transfer_management debt = Debt.transferDetails(viewDebtId,error);
		
		if(error.code < 0){
			errorShow(error.msg);
		} 
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = viewDebtId;
		
		Invest invest = new Invest();
		invest.id = debtBussiness.investId;
		double investAmount = invest.amount;
		
		Bid bid = new Bid();
		bid.id = invest.bidId;
		String imageUrl = bid.product.smallImageFilename;
		
		render(debt, debtBussiness, investAmount, imageUrl);
	}
	
	/**
	 * 转让债权管理已成功的详情页面
	 * @param debtId
	 */
	public static void debtDetailsSuccess(String sign){
		ErrorInfo error = new ErrorInfo();
		
		long viewDebtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			errorShow(error.msg);
		}
		
		v_debt_user_transfer_management debt = Debt.transferDetails(viewDebtId,error);
		
		if(error.code < 0){
			errorShow(error.msg);
		} 
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = viewDebtId;
		
		Invest invest = new Invest();
		invest.id = debtBussiness.investId;
		double investAmount = invest.amount;
		
        User user= new User();
		
		if(debtBussiness.type == 1){
			
			user.id = debtBussiness.specifiedUserId;
		}else{
			
			user.id = debtBussiness.transactionUserId;
		}
		
		String specifiedName = user.name;
		
		render(debt, debtBussiness, investAmount, specifiedName);
	}
	
	/**
	 * 转让债权管理失败的详情页面
	 * @param debtId
	 */
	public static void debtDetailsFailure(String sign){
		ErrorInfo error = new ErrorInfo();
		
		long viewDebtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			errorShow(error.msg);
		}
		
		v_debt_user_transfer_management debt = Debt.transferDetails(viewDebtId,error);
		
		if(error.code < 0){
			errorShow(error.msg);
		} 
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = viewDebtId;
		
		Invest invest = new Invest();
		invest.id = debtBussiness.investId;
		double investAmount = invest.amount;
		
		Bid bid = new Bid();
		bid.id = invest.bidId;
		String imageUrl = bid.product.smallImageFilename;
		
		render(debt, debtBussiness, investAmount, imageUrl);
	}
	
	/**
	 * 转让债权管理不通过的详情页面
	 * @param debtId
	 */
	public static void debtDetailsNoPass(String sign){
		ErrorInfo error = new ErrorInfo();
		
		long viewDebtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			errorShow(error.msg);
		}
		
		v_debt_user_transfer_management debt = Debt.transferDetails(viewDebtId,error);
		
		if(error.code < 0){
			errorShow(error.msg);
		} 
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = viewDebtId;
		
		Invest invest = new Invest();
		invest.id = debtBussiness.investId;
		double investAmount = invest.amount;
		
		Bid bid = new Bid();
		bid.id = invest.bidId;
		String imageUrl = bid.product.smallImageFilename;
		
		render(debt, debtBussiness, investAmount, imageUrl);
	}
	
	/**
	 * 债权用户初步成交债权，之后等待竞拍方确认成交
	 * @param sign
	 */
	public static void firstDealDebt(String sign){
		ErrorInfo error = new ErrorInfo();
		
		long debtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			flash.error(error.msg);
			debtDetailsTransfering(sign);
		}
		
		Debt.firstDealDebt(debtId, error);
		
		if(error.code < 0){
			flash.error(error.msg);
			debtDetailsTransfering(sign);
		}
		
		flash.error(error.msg);
		
		debeManage(1, "");
	}
	
	/**
	 * 完善资料 
	 */
	public static void baseInfo(){
		User user = User.currUser();
		user.id = User.currUser().id;

		ErrorInfo error = new ErrorInfo();
		
		List<t_dict_cars> cars = (List<t_dict_cars>) Cache.get("cars");
		List<t_dict_ad_provinces> provinces = (List<t_dict_ad_provinces>) Cache.get("provinces");
		List<t_dict_educations> educations = (List<t_dict_educations>) Cache.get("educations");
		List<t_dict_houses> houses = (List<t_dict_houses>) Cache.get("houses");
		List<t_dict_maritals> maritals = (List<t_dict_maritals>) Cache.get("maritals");
		
		List<t_dict_ad_citys> cityList = null;
		if(flash.get("province") != null) {
			cityList = User.queryCity(Integer.parseInt(flash.get("province")));
		}else {
			if(!user.isAddBaseInfo){
				if (null != provinces) {
					cityList = User.queryCity(provinces.get(0).id);
				}
			} else {
				cityList = User.queryCity(user.provinceId);
			}
		} 
		
		List<t_user_vip_records> vipRecords = Vip.queryVipRecord(user.id, error);
		
		if(error.code < 0) {
			errorShow(error.msg);
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String content = News.queryContent(Constants.NewsTypeId.VIP_AGREEMENT, error);
		
 		render(user,cars,provinces,educations,houses,maritals,cityList,vipRecords,backstageSet,content);
	}
	
	/**
	 * 保存基本信息
	 */
	public static void saveInformation(String realityName, int sex, int age, int city, int province,
			String idNumber, int education, int marital, int car, int house){
		
		ErrorInfo error = new ErrorInfo();
		
		User user = new User();
		user.id = User.currUser().id;

		user.editInfo(realityName, sex, age, city, idNumber, education, marital, car, house,user.financeType, error);
		
		if(error.code < 0) {
			flash.put("realityName", realityName);
			flash.put("sex", sex);
	 		flash.put("age", age);
			flash.put("city", city);
			flash.put("province", province);
			flash.put("idNumber", idNumber);
			flash.put("education", education);
			flash.put("marital", marital);
			flash.put("car", car);
			flash.put("house", house);
			
			flash.error(error.msg);
			
			baseInfo();
		}
		flash.success(error.msg);
		
		accountInfo();
	}
	
	/**
	 * 退出登录
	 */
	public static void loginOut(){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		user.logout(error);
		
		RegistAndLogin.login();
	}
	
	/**
	 * 首页
	 */
	public static void home() {
		ErrorInfo error = new ErrorInfo();
		long onlineUserNum = User.queryOnlineUserNum();
		long todayRegisterUserCount = User.queryTodayRegisterUserCount(error);
		
		List<t_content_advertisements> homeAds = Ads.queryAdsByLocation(Constants.HOME_PAGE_APP, error); // 广告条
		if (error.code < 0) {
			errorShow(error.msg);
		}
		
		long totalRegisterUserCount = User.queryTotalRegisterUserCount(error);
		
		if (error.code < 0) {
			errorShow(error.msg);
		}
		
		long todayBidCount = Bid.queryTodayBidCount(error);
		
		if (error.code < 0) {
			errorShow(error.msg);
		}
		
		long totalBidCount = Bid.queryTotalBidCount(error);
		
		if (error.code < 0) {
			errorShow(error.msg);
		}
		
		double totalBidDealAmount = Bid.queryTotalDealAmount(error);
		
		if (error.code < 0) {
			errorShow(error.msg);
		}
		
		long totalInvestCount = Invest.queryTotalInvestCount(error);
		
		if (error.code < 0) {
			errorShow(error.msg);
		}
		
		double totalInvestDealAmount = Invest.queryTotalDealAmount(error);
		
		if (error.code < 0) {
			errorShow(error.msg);
		}
		
		long totalNewsCount = News.queryTotalNewsCount(error);
		
		if (error.code < 0) {
			errorShow(error.msg);
		}
		
		render(onlineUserNum, todayRegisterUserCount, totalRegisterUserCount, todayBidCount, totalBidCount,totalBidDealAmount, totalInvestCount, totalInvestDealAmount, totalNewsCount, homeAds);
	}
	
	public static void errorShow(String errorMsg){
		if(null != null){
			flash.error(errorMsg);
		}
		
		render();
	}
	
	/**
	 * 判断用户状态并激活邮箱
	 */
	public static void checkUserStatusAndActiveEmail() {
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		
		JSONObject json = new JSONObject();
		
		/* 是否登录 */
		if(null == user) {
			json.put("status", Constants.NOT_LOGIN);
			json.put("code", "1");
			renderJSON(json);
		}
		user.id = user.id;
		/* 是否激活 */
		if(!user.isEmailVerified) {
			json.put("userName", user.name);
			json.put("email", user.email);
			json.put("status", Constants.NOT_EMAILVERIFIED);
			//未激活就发送邮箱
			TemplateEmail.activeEmail(user, error);
			json.put("code", error.code);
			json.put("msg", error.msg);
			renderJSON(json);
		}
		
		/* 是否完善基本资料 */
		if (!user.isAddBaseInfo) {
			json.put("status", Constants.NOT_ADDBASEINFO);
			json.put("code","1");
			renderJSON(json);
		}
		
		json.put("status", Constants.SUCCESS_STATUS);
		json.put("code","1");
		renderJSON(json);
	}
}
