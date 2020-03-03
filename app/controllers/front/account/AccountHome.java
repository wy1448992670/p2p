package controllers.front.account;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.t_bids;
import models.t_content_news;
import models.t_debt_bill_invest;
import models.t_debt_transfer;
import models.t_dict_audit_items;
import models.t_products;
import models.t_user_events;
import models.v_bid_auditing;
import models.v_bid_fundraiseing;
import models.v_bid_not_through;
import models.v_bid_release_funds;
import models.v_bid_repayment;
import models.v_bid_repaymenting;
import models.v_bid_wait_verify;
import models.v_bids;
import models.v_bill_detail;
import models.v_bill_loan;
import models.v_bill_recently_pending;
import models.v_bill_repayment_record;
import models.v_front_all_debts;
import models.v_invest_records;
import models.v_user_account_statistics;
import models.v_user_attention_info;
import models.v_user_audit_items;
import models.v_user_for_details;
import models.v_user_for_personal;
import models.core.t_new_product;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.lang.StringUtils;

import payment.PaymentProxy;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.mvc.With;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.FileUtil;
import utils.JsonDateValueProcessor;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;
import annotation.InactiveUserCheck;
import annotation.IpsAccountCheck;
import annotation.LoginCheck;
import annotation.SubmitCheck;
import annotation.SubmitOnly;
import bean.QualityBid;
import business.Agency;
import business.AuditItem;
import business.BackstageSet;
import business.Bid;
import business.Bill;
import business.BorrowApply;
import business.Debt;
import business.Invest;
import business.News;
import business.Optimization.BidOZ;
import business.Optimization.UserOZ;
import business.OverBorrow;
import business.Product;
import business.Score;
import business.StationLetter;
import business.User;
import business.UserAuditItem;
import business.Vip;
import business.Bid.Purpose;
import constants.Constants;
import constants.Constants.RechargeType;
import constants.FinanceTypeEnum;
import constants.IPSConstants;
import constants.ProductEnum;
import constants.UserTypeEnum;
import constants.IPSConstants.IPSDealStatus;
import controllers.BaseController;
import controllers.SubmitRepeat;
import controllers.interceptor.AccountInterceptor;

/**
 * 
 * @author cp
 *
 */
@With({AccountInterceptor.class,SubmitRepeat.class})
public class AccountHome extends BaseController {

	/**
	 * 我的账户的首页
	 */
	public static void home() {
		ErrorInfo error = new ErrorInfo();
		
		//避免缓存中的数据与数据库一致
		User user = new User();
		user.id = User.currUser().id;
		
		System.out.println(user.sign);
		
		if(StringUtils.isBlank(user.risk_result) && user.financeType == FinanceTypeEnum.INVEST.getCode()) {
			flash.error("is_risk", "您还没有完成风险测评");
		}
		
//		jsonMap.put("hasProtocolBank", Score.hasProtocolBank(userId));
		
		int unReadMsgCount = StationLetter.queryUserUnreadMsgsCount(user.id, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		List<t_content_news> news = News.queryNewForFront(Constants.NewsTypeId.BORROWING_TECHNIQUES, 3, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		String content = News.queryContent(Constants.NewsTypeId.VIP_AGREEMENT, error);
		
		if(error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_FRONT);
        }
		
		UserOZ warmPrompt = new UserOZ();
        warmPrompt.userId = user.id;
        
        BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
        
        //优质标推荐
        int size = Constants.QUALITY_BID_COUNT;
	List<QualityBid> qualityBids= BidOZ.queryQualityBid(size, error);
	
	//优质债权推荐
	List<v_front_all_debts> qualityDebts= Debt.queryQualityDebtTransfers(error);
        
	renderArgs.put("childId", "child_0");
	renderArgs.put("labId", "lab_1");
		render(user, warmPrompt,unReadMsgCount,news,backstageSet, content,qualityBids,qualityDebts);
	}
	
	/**
	 * 申请超额借款页面
	 */
	public static void applyForOverBorrowInit() {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		if (OverBorrow.haveAuditingOverBorrow(user.id, error) && 0 == error.code) {
			error.code = -1;
			error.msg = "您还有未审核的超额借款申请，不能再次申请";
			
			renderJSON(error);
		}
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render();
	}
	
	/**
	 * 申请超额借款
	 * @param amount
	 * @param reason
	 * @param jsonAuditItems
	 */
	public static void applyForOverBorrow(int amount, String reason, String jsonAuditItems) {
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		
		JSONArray jsonArray = JSONArray.fromObject(jsonAuditItems);
		List<Map<String,String>> auditItems = (List)jsonArray;
		
		new OverBorrow().applyFor(user, amount, reason, auditItems, error);
		
		if(error.code >= 0){
			error.msg = "您的超额借款申请已提交，请耐心等待审核结果。";
		}
		
		renderJSON(error);
	}
	
	/**
	 * 选择超额借款审核资料页面
	 */
	public static void selectAuditItemsInit() {
		User user = User.currUser();
		long userId = user.id;
		ErrorInfo error = new ErrorInfo();
		
		List<AuditItem> auditItems = UserAuditItem.queryAuditItemsOfOverBorrow(userId, error);
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render(auditItems);
	}
	
	/**
	 * 提交审核资料页面
	 */
	public static void submitAuditItemInit(String mark) {
		t_dict_audit_items auditItem = null;
		
		try {
			auditItem = t_dict_audit_items.find("mark = ?", mark).first();
		} catch (Exception e) {
			Logger.error("查询资料:" + e.getMessage());
			
			renderText("数据库异常");
		}
		 
		render(auditItem);
	}
	
	/**
	 * 申请超额借款页面ips
	 */
	public static void applyForOverBorrowWin() {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		if (OverBorrow.haveAuditingOverBorrow(user.id, error) && 0 == error.code) {
			error.code = -1;
			error.msg = "您还有未审核的超额借款申请，不能再次申请";			
			renderJSON(error);
		}		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render();
	}
	
	/**
	 * 选择超额借款审核资料页面ips
	 */
	public static void selectAuditItemsWin() {
		User user = User.currUser();
		long userId = user.id;
		ErrorInfo error = new ErrorInfo();
		
		List<AuditItem> auditItems = UserAuditItem.queryAuditItemsOfOverBorrow(userId, error);
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render(auditItems);
	}
	
	/**
	 * 提交审核资料页面ips
	 */
	public static void submitAuditItemWin(String mark) {
		t_dict_audit_items auditItem = null;
		
		try {
			auditItem = t_dict_audit_items.find("mark = ?", mark).first();
		} catch (Exception e) {
			Logger.error("查询资料:" + e.getMessage());
			
			renderText("数据库异常");
		}
		
		render(auditItem);
	}
	
	/**
	 * 优质标推荐
	 */
	public static void queryQualityBids() {
		ErrorInfo error = new ErrorInfo();
		int size = Constants.QUALITY_BID_COUNT;
		List<QualityBid> qualityBids= BidOZ.queryQualityBid(size, error);
		
		render(qualityBids);
	}
	
	/**
	 * 优质债权推荐
	 */
	public static void queryQualityDebts() {
		
		ErrorInfo error = new ErrorInfo();
		List<v_front_all_debts> qualityBids= Debt.queryQualityDebtTransfers(error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		     
		render(qualityBids);
	}
	
	/**
	 * 我关注的人
	 */
	public static void myAttentionUser(int currPage, int pageSize) {
		User user = User.currUser();
		
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_attention_info> page= User.queryAttentionUsers(user.id, currPage, pageSize, error);
		
		if(error.code < 0 ) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		renderArgs.put("childId", "child_1");
		renderArgs.put("labId", "lab_1");
		render(page,user);
	}
	
	/**
	 * 我关注的人--修改备注名跳转页面
	 */
	public static void attentionUserNote(long id){
		render(id);
	}
	
	/**
	 * 我关注的人--修改发送站内信跳转页面
	 */
	public static void attentionUsersStation(String sign, String userName){
		render(sign, userName);
	}
	
	/**
	 * 我关注的人(AJAX)
	 */
	public static void AttentionUsers(String sign, int currPage, int pageSize) {
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			renderJSON(error);
		}
		
		User user = User.currUser();
		
		if(userId < 0) {
			error.code = -1;
			error.msg = "传入参数有误";
			
			render(error);
		}
		
		if(!User.isAttentionUser(user.id, userId, error)) {
			render(error);
		}
		
		PageBean<v_user_attention_info> page= User.queryAttentionUsers(userId, currPage, pageSize, error);
		
		if(error.code < 0 ) {
			render(error);
		}
		
		render(page);
	}
	
	/**
	 * 借款列表(AJAX)
	 */
	public static void loanList(String sign) {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			render(error);
		}
		
		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		
		if(userId < 0) {
			error.code = -1;
			error.msg = "传入参数有误";
			
			render(error);
		}
		
		if(!User.isAttentionUser(user.id, userId, error)) {
			render(error);
		}
		
		PageBean<v_bids> page= Bid.queryBidByUser(userId, currPage, pageSize, error);
		
		if(error.code < 0 ) {
			render(error);
		}
		
		render(page);
	}
	
	/**
	 * 最新动态(AJAX)
	 */
	public static void currentEvents(String sign) {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			render(error);
		}
		
		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		
		if(userId < 0) {
			error.code = -1;
			error.msg = "传入参数有误";
			
			render(error);
		}
		
		if(!User.isAttentionUser(user.id, userId, error)) {
			render(error);
		}
		
		PageBean<t_user_events> page = User.queryUserEvnets(userId, error, currPage, pageSize);
		
		if(error.code < 0 ) {
			render(error);
		}
		
		render(page);
	}
	
	/**
	 * 投资记录(AJAX)
	 */
	public static void investRecords(String sign) {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			render(error);
		}
		
		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		
		if(userId <= 0) {
			error.code = -1;
			error.msg = "传入参数有误";
			
			render(error);
		}
		
		if(!User.isAttentionUser(user.id, userId, error)) {
			render(error);
		}
		
		PageBean<v_invest_records> page = Invest.queryInvestRecords(userId, currPage, pageSize, error);
		
		if(error.code < 0 ) {
			render(error);
		}
		
		render(page);
	}

	
	/**
	 * 修改备注名
	 */
	public static void setNoteName(long id, String noteName) {
		ErrorInfo error = new ErrorInfo();
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		User.updateAttentionUser(id, noteName, error);
		
		JSONObject json = new JSONObject();
		
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 上传图像
	 */
	public static void uploadPhoto(File imgFile) {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> fileInfo = FileUtil.uploadFile(imgFile, 1, error);
		
		if (error.code < 0) {
			JSONObject json = new JSONObject();
			json.put("error", error);
			
			renderText(json.toString());
		}
		
		String fileExt = (String) fileInfo.get("fileName");
		String filename = fileExt.substring(0, fileExt.lastIndexOf("."));
		
		User user = new User();
		user.id = User.currUser().id;
		user.photo = filename;
		user.editPhoto(error);
		
		if (error.code < 0) {
			JSONObject json = new JSONObject();
			json.put("error", error);
			
			renderText(json.toString());
		}
		
		JSONObject json = new JSONObject();
		json.put("filename", Constants.HTTP_PATH+filename);
		json.put("error", error);
		
		renderText(json.toString());
	}
	
	/**
	 * 关注用户
	 */
	@LoginCheck(true)
	@IpsAccountCheck(true)
	@InactiveUserCheck(true)
	public static void attentionUser(String sign) {
		
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		
		long attentionUserId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			renderJSON(error);
		}
		
		if(attentionUserId <= 0) {
			error.code = -1;
			error.msg = "传入数据有误";
			
			json.put("error", error.msg);
			
			renderJSON(json);
		}
		
		if(attentionUserId == user.id){
			
			error.msg = "您不能关注自己";
			json.put("error", error.msg);
			
			renderJSON(json);
		}
		
		User.attentionUser(user.id, attentionUserId, error);
		
		json.put("error", error.msg);
		
		renderJSON(json);
	}
	
	/**
	 * 取消关注用户
	 * @param id 关注表中的id主键
	 */
	public static void cancelAttentionUser(long id) {
		ErrorInfo error = new ErrorInfo();
		/*long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}*/
		
		JSONObject json = new JSONObject();
		
		if(id <= 0) {
			error.code = -1;
			error.msg = "传入数据有误";
			
			json.put("error", error.msg);
			
			renderJSON(json);
		}
		
		User.cancelAttentionUser(id, error);
		
		json.put("error", error);

		renderJSON(json);
	}
	
	/**
	 * 用户详情
	 * @param userId
	 */
	public static void userDetail(String sign) {
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			render(user, error);
		}
		
		if(userId <= 0) {
			error.code = -1;
			error.msg = "传入参数有误";
			
			render(user, error);
		}
		
		v_user_for_personal userInfo = User.queryUserInformation(userId, error);
		
		if(error.code < 0) {
			render(user, error);
		}
		
		render(user, userInfo);
	}
	
	/**
	 * 根据申请会员时间，计算所需要的钱
	 * @param time
	 */
	public static void vipMoney(String time) {
		int serviceTime = Integer.parseInt(time);
		Vip vip = new Vip();
		vip.serviceTime = serviceTime;
		
		ErrorInfo error = new ErrorInfo();
		double amount = vip.vipMoney(error);
		
		JSONObject json = new JSONObject();
		//json.put("error", error);
		json.put("amount", amount);
		
		renderJSON(json);
	}
	
	/**
	 * 申请会员
	 */
	@LoginCheck(true)
	@IpsAccountCheck(true)
	@InactiveUserCheck(true)
	public static void vipApply(Integer serviceTime, Integer type) {
		User user = new User();
		user.id = User.currUser().id;
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		if(serviceTime == null) {
			error.code = -1;
			error.msg = "请输入申请时间";
			
			json.put("error", error);
		}		
		if(user.vipStatus && (1 != type)) {
			error.code = -1;
			error.msg = "您已经是VIP了!";
			
			json.put("error", error);
			
			renderJSON(json);	
		}
		
		Vip vip = new Vip();
		vip.serviceTime = serviceTime;
		vip.renewal(user, Constants.CLIENT_PC, error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
		}else{
			error.code = 1;
			error.msg = "VIP申请成功!";
		}
		
		json.put("error", error);
		
		renderJSON(json);	
		
	}
	
	/**
	 * 发送站内信
	 * @param receiverUserId
	 * @param title
	 * @param content
	 */
	@LoginCheck(true)
	@IpsAccountCheck(true)
	@InactiveUserCheck(true)
	public static void sendMessage(String sign, String title, String content) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		if(StringUtils.isBlank(title)) {
			error.msg = "标题不能为空";
			json.put("error", error);
			renderJSON(json);
		}
		
		long receiverUserId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			error.msg = "非法请求";
			json.put("error", error);
			renderJSON(json);
		}
		
		User user = User.currUser();
		
		if(receiverUserId == user.id){
			error.msg = "您不能给自己发送站内信";
			json.put("error", error);
			renderJSON(json);
		}
		
		StationLetter message = new StationLetter();
		
		message.senderUserId = user.id;
		message.receiverUserId = receiverUserId;
		message.title = title;
		message.content = content;
		
		message.sendToUserByUser(error); 
		
		json.put("error", error);
		
		renderJSON(json);
	}
	
	
	
	/**
	 * 我的站内信
	 */
	public static void myMessages() {
		ErrorInfo error = new ErrorInfo();
		int unreadSystemMsgCount = StationLetter.queryUserUnreadSystemMsgsCount(1, error);
		int unreadInboxMsgCount = StationLetter.queryUserUnreadInboxMsgsCount(1, error);
		render(unreadSystemMsgCount,unreadInboxMsgCount);
	}
	
	//-----------------------------------借款子账户--------------------------------------
	
	/**
	 * 借款子账户首页
	 */
	public static void loanAccount(){
		User user = User.currUser();
		
		ErrorInfo error = new ErrorInfo();
		v_user_account_statistics accountStatistics = User.queryAccountStatistics(user.id, error);
		
		if(error.code < 0) 
			render(Constants.ERROR_PAGE_PATH_FRONT);
		
		List<t_content_news> news = News.queryNewForFront(Constants.NewsTypeId.BORROWING_TECHNIQUES, 3, error);
		
		if(error.code < 0) 
			render(Constants.ERROR_PAGE_PATH_FRONT);
		
		/* 最新借款中满标倒计时提醒 */
		List<t_bids> fundraiseingBid = Bid.queryFundraiseingBid(user.id, error);
		
		/* 账单提醒 */
		List<v_bill_recently_pending> recentlyRepayBills = Bill.queryRecentlyBills(error);
		
		if(null == fundraiseingBid)
			render(Constants.ERROR_PAGE_PATH_FRONT);
		
		/* 最新欠缺资料的借款标 */
		List<t_bids> toSubmitItemBid = Bid.queryToSubmitItemBid(user.id, error);
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String content = News.queryContent(Constants.NewsTypeId.VIP_AGREEMENT, error);
		
		if(null == toSubmitItemBid)
			render(Constants.ERROR_PAGE_PATH_FRONT);
		
		renderArgs.put("childId", "child_7");
		renderArgs.put("labId", "lab_3");
		render(user, accountStatistics, news, fundraiseingBid, toSubmitItemBid, recentlyRepayBills,backstageSet, content);
	}
	
	/**
	 * 我的借款账单
	 */

	public static void myLoanBills(int payType, int isOverType, int keyType, String key, int currPage){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		PageBean<v_bill_loan> page = Bill.queryMyLoanBills(user.id, payType, isOverType, keyType, key, currPage, 0, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		renderArgs.put("childId", "child_8");
		renderArgs.put("labId", "lab_3");
		render(page);
	}
	
	/**
	 * 下载数据(我的借款账单)
	 */
	public static void exportLoanBills() {
		ErrorInfo error = new ErrorInfo();
		
    	List<v_bill_loan> bills = Bill.queryMyAllLoanBills(error);
    	
    	if (error.code < 0) {
			renderText("下载数据失败");
		}
    	
    	JsonConfig jsonConfig = new JsonConfig();
    	jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
    	JSONArray arrBills = JSONArray.fromObject(bills, jsonConfig);
    	
    	for (Object obj : arrBills) {
			JSONObject bill = (JSONObject)obj;
			int isOverdue = bill.getInt("is_overdue");
			int status = bill.getInt("status");
			bill.put("is_overdue", (isOverdue == 0) ? "未逾期" : "逾期");
			bill.put("status", (status == -1 || status == -2) ? "未还款" : "已还款");
		}
    	
    	File file = ExcelUtils.export(
    			"我的借款账单", 
    			arrBills,
				new String[] {"账单标题", "本期需还款金额", "是否逾期", "还款状态", "到期还款时间", "实际还款时间"}, 
				new String[] {"title", "repayment_amount", "is_overdue", "status", "repayment_time", "real_repayment_time"});
    	
    	renderBinary(file, "我的借款账单.xls");
    }
	
	/**
	 * 从理财收款标跳转到借款账单详情
	 * @param sign
	 */
	public static void toInvestBill(String sign){
		
        ErrorInfo error = new ErrorInfo();
		
		long investId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		Long billId = Invest.queryBillByInvestId(investId, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		String id = Security.addSign(billId, Constants.BILL_ID_SIGN);
		
		loanBidDetails(id, 1);
	}
	
	/**
	 *理财收款标借款账单详情
	 */
	public static void loanBidDetails(String billId, int currPage) { 
		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		User user = User.currUser();
		
		v_bill_detail billDetail = Bill.queryBillDetails(id, user.id, error);
		
		PageBean<v_bill_repayment_record> page = Bill.queryBillReceivables(billDetail.bid_id, currPage, 0, error);
		BackstageSet backSet = BackstageSet.getCurrentBackstageSet();
		
		render(billDetail, page, backSet, user);
	}
	
	/**
	 * 我的借款账单详情
	 */
	@SubmitCheck
	public static void loanBillDetails(String billId, int currPage) { 
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
		
		PageBean<v_bill_repayment_record> page = Bill.queryBillReceivables(billDetail.bid_id, currPage, 0, error);
		BackstageSet backSet = BackstageSet.getCurrentBackstageSet();
		int checkPeriod = Bill.checkPeriod(billDetail.bid_id, billDetail.current_period);
		
		render(billDetail, page, backSet, user, isDealPassword, checkPeriod);
	}
	
	// 还款
	public static void repayment(long billId){
		
		
		render();
	}
	
	/**
	 * 确认还款
	 */
	@SubmitOnly
	public static void submitRepayment(String payPassword, double amount, String billId){
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		if(error.code < 0){
			flash.error(error.msg);
			loanBillDetails(billId, 1);
		}
		
		User user = User.currUser();
		
		//资金托管模式下，平台不设交易密码
		if(!Constants.IPS_ENABLE){
			boolean isDealPassword = false;
			
			try {
				isDealPassword = t_products.find("select is_deal_password from t_products where id = (select product_id from t_bids where id = (select bid_id from t_bills where id = ?))", id).first();
			} catch (Exception e) {
				Logger.error(e.getMessage());
				error.code = -1;
				error.msg = "还款失败";
				flash.error(error.msg);
				
				loanBillDetails(billId, 1);
			}
			
			if (isDealPassword) {
				int code = user.verifyPayPassword(payPassword, error);
				
				if(code < 0){
					flash.error(error.msg);
					loanBillDetails(billId, 1);
				}
			}
		}
		
		Bill bill = new Bill();
		bill.setId(id);
		
		if(bill.ipsStatus == IPSDealStatus.REPAYMENT_HANDING){  //还款处理中
			flash.error("还款处理中，请勿重复操作！");
			
			loanBillDetails(billId, 1);
		}
		
		if(bill.ipsStatus == IPSDealStatus.OFFLINEREPAYMENT_HANDING){  //线下收款处理中
			flash.error("线下收款处理中，不能还款！");
			
			loanBillDetails(billId, 1);
		}
		
		if(bill.ipsStatus == IPSDealStatus.COMPENSATE_HANDING){  //本金垫付处理中
			flash.error("本金垫付处理中，不能还款！");
			
			loanBillDetails(billId, 1);
		}
		
		/* 2014-12-29 限制还款需要从第一期逐步开始还款 */
		if(bill.checkPeriod(bill.bidId, bill.periods) > 0){
			flash.error("请您从第一期逐次还款!");
			
			loanBillDetails(billId, 1);
		}
		
		/*本金垫付还款*/
		if (Constants.IPS_ENABLE && bill.status == Constants.ADVANCE_PRINCIIPAL_REPAYMENT) {
			
			//垫付还款，本地业务逻辑，在垫付还款回调方法中
			PaymentProxy.getInstance().advanceRepayment(error, Constants.PC, bill, user.id);
			
			flash.error(error.msg);
			
			loanBillDetails(billId, 1);
			
			return;
		}
		
		/* 余额不足则提示跳转去充值页面，故此不能把错误值放入flash.error */
		if(error.code == Constants.BALANCE_NOT_ENOUGH) {
			flash.put("notEnough", -999);
			loanBillDetails(billId, 1);
		}
		
		if(error.code < 0){
			flash.error(error.msg);
			loanBillDetails(billId, 1);
		}
		
		if(Constants.IPS_ENABLE) {		
			
			//资金托管还款接口调用，本地业务逻辑处理，在托管回调方法中
			PaymentProxy.getInstance().repayment(error, Constants.PC, bill, user.id);
			
			flash.error(error.msg);
			loanBillDetails(billId, 1);
			
			return;
		}	

		t_debt_transfer transfer = t_debt_transfer.find(" bid_id = ? ", bill.bidId).first();
		List<t_debt_bill_invest> debtBill = new ArrayList<>();
		if(transfer != null ){
			debtBill = t_debt_bill_invest.find(" debt_id = ? ", transfer.id).fetch();
			Logger.info("该标的有债权转让，执行债权转让还款逻辑！ 账单信息: " + debtBill);
			bill.repaymentV1(user.id, error);
		}else {

			//普通网关模式，还款业务逻辑
			bill.repayment(user.id, error);
		}
		
		
		/*if(debtBill != null && debtBill.size() > 0){//存在债权转让账单，走债权还款
			
			
		} */
		
		
		
		
		if (error.code == Constants.BALANCE_NOT_ENOUGH){
			flash.put("notEnough", -999);
		}
		else{
			
			flash.error("还款成功");
		}
		
		loanBillDetails(billId, 1);
	}
	
	/**
	 * 待验证的借款标列表
	 */
	public static void waitVerifyBids(String currPage, String pageSize, String condition, String keyword){
		ErrorInfo error = new ErrorInfo();
		
		String userId = String.valueOf(User.currUser().id);
		
		PageBean<v_bid_wait_verify> pageBean = new PageBean<v_bid_wait_verify>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		
		pageBean.page = Bid.queryBidWaitVerify(pageBean, error, userId, condition, keyword, "", "", "");

		if (null == pageBean.page) 
			render(Constants.ERROR_PAGE_PATH_FRONT);  
		
		renderArgs.put("childId", "child_40");
		renderArgs.put("labId", "lab_3");
		render(pageBean);
	}
	
	/**
	 * 审核中的借款标列表
	 */
	public static void auditingLoanBids(String currPage, String pageSize, String condition, String keyword) {
		ErrorInfo error = new ErrorInfo();
		
		String userId = String.valueOf(User.currUser().id);
		
		PageBean<v_bid_auditing> pageBean = new PageBean<v_bid_auditing>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		Map<String, Object> map = new HashMap<String,Object>();
		
		map.put("userId", userId);
		map.put("condition", condition);
		map.put("keyword", keyword);
		
		pageBean.page = Bid.queryBidAuditing (pageBean, error, map);
		pageBean.conditions = map;

		if (null == pageBean.page) 
			render(Constants.ERROR_PAGE_PATH_FRONT);  
                
		renderArgs.put("childId", "child_9");
		renderArgs.put("labId", "lab_3");
		
		render(pageBean);
	}
	
	/**
	 * 等待满标的借款标列表
	 */
	public static void loaningBids(String currPage, String pageSize, String condition, String keyword){
		ErrorInfo error = new ErrorInfo();
		
		String userId = String.valueOf(User.currUser().id);
		
		PageBean<v_bid_fundraiseing> pageBean = new PageBean<v_bid_fundraiseing>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		pageBean.page = Bid.queryBidFundraiseing(pageBean, -1, error, userId, condition, keyword, "", "", "");
		
		if (null == pageBean.page)
			render(Constants.ERROR_PAGE_PATH_FRONT);  
		
		renderArgs.put("childId", "child_10");
		renderArgs.put("labId", "lab_3");
		render(pageBean);
	}
	
	/**
	 * 待放款的借款标列表
	 */
	public static void readyReleaseBid(String currPage, String pageSize, String condition, String keyword){
		ErrorInfo error = new ErrorInfo();
		
		String userId = String.valueOf(User.currUser().id);
		
		PageBean<v_bid_release_funds> pageBean = new PageBean<v_bid_release_funds>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		pageBean.page = Bid.queryReleaseFunds(0, pageBean, Constants.BID_EAIT_LOAN, error, userId, condition, keyword, "", "", "");
		
		if (null == pageBean.page)
			render(Constants.ERROR_PAGE_PATH_FRONT);  
		
		renderArgs.put("childId", "child_38");
		renderArgs.put("labId", "lab_3");
		render(pageBean);
	}
	
	/**
	 * 借款标详情
	 */
	public static void bidDetail(long bidId){
		ErrorInfo error = new ErrorInfo();
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.id = bidId;
		
		/* 如果非当前用户操作 */
		if(bid.userId  != User.currUser().id)
			render(Constants.ERROR_PAGE_PATH_FRONT); 
			
		Map<String, String> historySituationMap = User.historySituation(bid.userId, error);// 借款者历史记录情况
		List<UserAuditItem> uItems = UserAuditItem.queryUserAllAuditItem(bid.userId, bid.mark); // 用户正对产品上传的资料集合
		
		//List<UserBankAccounts> banks = null;
		//if(Constants.BID_ADVANCE_LOAN == bid.status || Constants.BID_FUNDRAISE == bid.status)
		//	banks = UserBankAccounts.queryUserAllBankAccount(User.currUser().id); // 用户银行卡列表

		render(bid, historySituationMap, uItems);
	}
	
	/**
	 * 投标记录
	 */
	public static void bidRecord(int currPage, long bidId) {
		if(0 == bidId) 
			render();
		
		ErrorInfo error = new ErrorInfo();
		PageBean<v_invest_records> pageBean = new PageBean<v_invest_records>();
		pageBean.currPage = currPage;
		pageBean.pageSize = Constants.PAGE_SIZE;
		pageBean.page = Invest.bidInvestRecord(pageBean, bidId, error);
		
		render(pageBean);
	}
	
	/**
	 * 查看资料
	 */
	public static void showitem(String mark){
		UserAuditItem item = new UserAuditItem();
		item.lazy = true;
		item.userId = User.currUser().id;
		item.mark = mark;
		
		render(item);
	}
	
	
	/**
	 * 审核中->撤销
	 */
	public static void auditToRepeal(String sign){
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			auditingLoanBids("", "", "", "");
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		
		if(bid.userId != User.currUser().id)
			return;
		
		bid.auditToRepeal(error);
		
		if(Constants.IPS_ENABLE && error.code >= 0) {
			//流标调用环迅托管接口, 处理业务逻辑在托管实现类回调方法中  error  client  标的id   流标方式  
			PaymentProxy.getInstance().bidAuditFail(error, Constants.PC, bidId, IPSConstants.BID_CANCEL);	
			flash.error(error.msg); 
			
			auditingLoanBids("", "", "", "");
		}
		
		flash.error(error.msg);
		
		auditingLoanBids("", "", "", "");
	}
	
	/**
	 * 提前借款->撤销
	 */
	public static void advanceLoanToRepeal(String sign){
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			loaningBids("", "", "", "");
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		
		if(bid.userId != User.currUser().id)
			return;
		
		bid.advanceLoanToRepeal(error);
		
		if(Constants.IPS_ENABLE && error.code >= 0) {
			//流标调用环迅托管接口, 处理业务逻辑在托管实现类回调方法中  error  client  标的id   流标方式  
			PaymentProxy.getInstance().bidAuditFail(error, Constants.PC, bidId, IPSConstants.BID_CANCEL_F);	

			flash.error(error.msg); 
			
			loaningBids("", "", "", "");
		}
		
		flash.error(error.msg);
		
		loaningBids("", "", "", "");
	}
	
	/**
	 * 借款中->撤销
	 */
	public static void fundraiseToRepeal(String sign){
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			loaningBids("", "", "", "");
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		
		if(bid.userId != User.currUser().id)
			return;
		
		bid.fundraiseToRepeal(error);
		
		if(Constants.IPS_ENABLE && error.code >= 0) {
			//流标调用环迅托管接口, 处理业务逻辑在托管实现类回调方法中  error  client  标的id   流标方式  
			PaymentProxy.getInstance().bidAuditFail(error, Constants.PC, bidId, IPSConstants.BID_CANCEL_N);	
			flash.error(error.msg); 
			
			loaningBids("", "", "", "");
		}
		
		flash.error(error.msg);
		
		loaningBids("", "", "", "");
	}
	
	/**
	 * 还款中的借款标列表
	 */
	public static void repaymentBids(){
		ErrorInfo error = new ErrorInfo();
		
		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String condition = params.get("condition");
		String keyword = params.get("keyword");
		String userid = User.currUser().id + "";
		
		PageBean<v_bid_repaymenting> pageBean = new PageBean<v_bid_repaymenting>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		pageBean.page = Bid.queryBidRepaymenting(0, pageBean, 0, error, userid, condition, keyword, "", "", "");
                
		renderArgs.put("childId", "child_11");
		renderArgs.put("labId", "lab_3");
		render(pageBean);
	}
	
	//提前还款
	public static void prepayment(){
		render();
	}
	
	//确认提前还款
	public static void submitPrepayment(){
		render();
	}
	
	/**
	 * 已成功的借款标列表
	 */
	public static void successBids(){
		ErrorInfo error = new ErrorInfo();

		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String condition = params.get("condition");
		String keyword = params.get("keyword");
		String userid = User.currUser().id + "";
		
		PageBean<v_bid_repayment> pageBean = new PageBean<v_bid_repayment>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		pageBean.page = Bid.queryBidRepayment(0, pageBean, 0, error, userid, condition, keyword, "", "", "");
                
		renderArgs.put("childId", "child_12");
		renderArgs.put("labId", "lab_3");
		render(pageBean);
	}
	
	/**
	 * 失败的借款标列表
	 */
	public static void failBids() {
		ErrorInfo error = new ErrorInfo();

		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String condition = params.get("condition");
		String keyword = params.get("keyword");
		String userid = User.currUser().id + "";
		
		PageBean<v_bid_not_through> pageBean = new PageBean<v_bid_not_through>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		pageBean.page = Bid.queryBidNotThrough(0, pageBean, error, userid, condition, keyword, "", "", "");
		
		renderArgs.put("childId", "child_39");
		renderArgs.put("labId", "lab_3");
		
		render(pageBean);
	}
	
	/**
	 * 审核资料认证
	 */
	@Deprecated
	public static void auditMaterials(String currPage, String pageSize,
			String status, String startDate, String endDate, String productId,
			String productType) {
		return ;
	}
	
	/**
	 * 审核资料认证
	 * @param currPage
	 * @param pageSize
	 * @param status
	 * @param startDate
	 * @param endDate
	 * @param productId
	 * @param productType
	 */
	public static void auditMaterialsIPS(String currPage, String pageSize,
			String status, String startDate, String endDate, String productId,
			String productType) {
		ErrorInfo error = new ErrorInfo();
		
		long userId = User.currUser().id;
		
		/* 产品列表 */
		List<Product> products = Product.queryProductNames(true, error);
		PageBean<v_user_audit_items> pageBean = UserAuditItem.queryUserAuditItem(currPage, pageSize, userId, error, status, startDate, endDate, productId, productType);
		
		renderArgs.put("childId", "child_13");
		renderArgs.put("labId", "lab_3");
		render(pageBean, products);
	}
	
	/**
	 * 查看审核资料
	 */
	public static void auditMaterialsSameItem(String mark){
		long userId = User.currUser().id;
		
		UserAuditItem item = new UserAuditItem();
		item.userId = userId;
		item.mark = mark;
		
		ErrorInfo error = new ErrorInfo();
		List<v_user_audit_items> items = UserAuditItem.querySameAuditItem(userId, item.auditItemId, error);
		
		if(null == items) {
			flash.error(error.msg);
			
			auditMaterials(null, null, null, null, null, null, null);
		}
		
		render(item, items);
	}
	
	/**
	 * 删除用户资料
	 */
	@Deprecated
	public static void deleteAuditItem(String sign){
		return ;
	}
	
	/**
	 * 提交资料(异步)
	 */  
	@Deprecated
	public static void createUserAuditItem(String sign, String signItemId, String items, double size){
		return ;
	}
	
	/**
	 * 上传资料(资金托管-异步)
	 */  
	public static void createUserAuditItemIPS(String sign, String signItemId, String items, double size){
		ErrorInfo error = new ErrorInfo();
		long userItemId = Security.checkSign(sign, Constants.USER_ITEM_ID_SIGN, Constants.VALID_TIME, error);
		
		if(userItemId < 1)
			renderJSON(error);
		
		long itemId = Security.checkSign(signItemId, Constants.ITEM_ID_SIGN, Constants.VALID_TIME, error);
		
		if(itemId < 1)
			renderJSON(error);
		
		if(StringUtils.isBlank(items)){
			error.msg = "数据有误!";
			
			renderJSON(error);
		}
		
		UserAuditItem item = new UserAuditItem();
		item.lazy = true;
		item.userId = User.currUser().id;
		item.id = userItemId;
		
		if(item.id < 1) {
			error.msg = "资料标示项已过期，请刷新页面!";
			
			renderJSON(error);
		}
		
		item.imageFileNames = items;
		item.createUserAuditItem(error);
		
		JSONObject json = new JSONObject();
		json.put("msg", error.msg);
		json.put("status", item.status);
		json.put("time", DateUtil.dateToString1(item.time));
		
		renderJSON(json);
	}
	
	/**
	 * 查询用户上传未付款的资料信息
	 */
	public static void queryUploadedItems() {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> map = UserAuditItem.queryUploadItems(User.currUser().id, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		if (map == null || (Long) map.get("count") == 0) {
			error.code = -1;
			error.msg = "请先上传资料再提交";
			
			renderJSON(error);
		}
		
		map.put("code", 0);
		
		renderJSON(map);
	}
	
	/**
	 * 提交已上传的资料
	 */
	public static void submitUploadedItems() {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> info = UserAuditItem.queryUploadItems(User.currUser().id, error);
		
		if (error.code < 0) {
			flash.error(error.msg);
			
			auditMaterialsIPS("1", "10", "", "", "", "", "");
		}
		
		/* 2014-11-18把单个提交修改为组提交 */
		double balance = 0;
		double fees = (Double) info.get("fees");
		User user = User.currUser();
		v_user_for_details details = user.balanceDetail;
		
		fees = 0;
		
		if(null == details) {
			flash.error("查询用户资金出现错误!");
			
			auditMaterialsIPS("1", "10", "", "", "", "", "");
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
			
			auditMaterialsIPS("1", "10", "", "", "", "", "");
		} else {
			UserAuditItem.submitUploadedItems(user.id, balance, error);
			flash.error(error.msg);
			
			auditMaterialsIPS("1", "10", "", "", "", "", "");
		}
		
		AccountHome.auditMaterialsIPS("1", "10", null, null, null, null, null);
	}
	
	/**
	 * 清空用户上传未付款的资料
	 */
	public static void clearUploadedItems() {
		ErrorInfo error = new ErrorInfo();
		UserAuditItem.clearUploadedItems(User.currUser().id, error);
		flash.error(error.msg);
		
		AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
	}
	
	//vip会员优先计划
	public static void vipFirst(){
		render();
	}
	
	/* 2014-11-15 */
	/**
	 * 借款合同
	 */
	public static void pact(String sign, int type){
		ErrorInfo error = new ErrorInfo();
		
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1)
			renderText(error.msg);
		
	    long userId = User.currUser().id;
		String pact = Bid.queryPact(bidId, userId);
		
		render(pact, type);
	}

	/**
	 * 居间服务协议
	 */
	public static void intermediaryAgreement(String sign, int type){
		ErrorInfo error = new ErrorInfo();
		
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1)
			renderText(error.msg);
		
	    long userId = User.currUser().id;
		String intermediaryAgreement = Bid.queryIntermediaryAgreement(bidId, userId);
		
		render(intermediaryAgreement, type);
	}
	
	/**
	 * 保障涵
	 */
	public static void guaranteeBid(String sign, int type){
		ErrorInfo error = new ErrorInfo();
		
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1)
			renderText(error.msg);
		
	    long userId = User.currUser().id;
		String guaranteeBid = Bid.queryGuaranteeBid(bidId, userId);
		
		render(guaranteeBid, type);
	}

	/**
	 * 查询某一个标的借款合同，居间服务协议，保证函
	 * @param sign
	 */
	public static void queryPactTemplates(String sign){
		ErrorInfo error = new ErrorInfo();
		
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if (bidId < 1) {
			renderJSON(error);
		}
		
		Bid bid = new Bid();
		bid.auditBidPact = true;
		bid.id = bidId;
		
		JSONObject json = new JSONObject();
		json.put("pact", bid.pact);
		json.put("intermediary_agreement", bid.intermediary_agreement);
		json.put("guarantee_bid", bid.guarantee_bid);
		renderJSON(json);
	}
	/**
	 * 借款申请接口
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public static void applyLoan() throws IOException{
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		if(user != null && user.financeType == FinanceTypeEnum.INVEST.getCode()) {
			user.removeCurrUser();
			LoginAndRegisterAction.loginLoan();
		}
		try {
			List<Agency> agency = Agency.queryAgencys(error);
			List<Purpose> purpose = Purpose.queryLoanPurpose(error, true);

			List<Map<String, Object>> userTypeList = new ArrayList<>();

			for (UserTypeEnum type : UserTypeEnum.values()) {
				Map<String, Object> map = new HashMap<>();
				map.put(String.valueOf(type.getCode()), type.getName());
				userTypeList.add(map);
			}

			List<Map<String, Object>> productTypeList = new ArrayList<>();

			/*
			for (ProductEnum type : ProductEnum.values()) {
				Map<String, Object> map = new HashMap<>();
				map.put(String.valueOf(type.getCode()), type.getName());
				productTypeList.add(map);
			}*/
			List<t_new_product> product_list=new t_new_product().fetchEnumList();
			for (t_new_product product:product_list) {
				if(product.borrow_app_can_use) {
					Map<String, Object> map = new HashMap<>();
					map.put(String.valueOf(product.id), product.name);
					productTypeList.add(map);
				}
			}

			//1. 借款人服务协议BORROWER_SERVICE_ID
			String service = News.queryContent(Constants.NewsTypeId.BORROWER_SERVICE_ID, error);
			service = service.replaceAll("\\n|\\r", "");
			// 2. //电子签章自动签署授权协议
			String authorization = News.queryContent(Constants.NewsTypeId.SIGNATURE_AUTHORIZATION_ID, error);
			if(authorization !=null) {
				authorization = authorization.replaceAll("\\n|\\r", "");
			}
		
			// 3.咨询与管理服务协议 
			String manage  = News.queryContent(Constants.NewsTypeId.CONSULT_MANAGE_ID, error);
			manage = manage.replaceAll("\\n|\\r", "");
			
			
			render(error, userTypeList, productTypeList, purpose, agency,service ,authorization,  manage);
		} catch (Exception e) {
			Logger.error("借款申请出错！" );
			e.printStackTrace();
			error.code = -1;
			error.msg = "借款申请出错！";
			render(error);
		}
	}

	/**
	 *    借款申请提交
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public static void borrowApplySubmit() throws IOException{
//		checkAuthenticity(); 
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		try {
			String userTypeId  = params.get("userTypeId");
			String agencyId = params.get("agencyId");
			String purposeId = params.get("purposeId");
			String productId = params.get("productId");
			String applyAmount = params.get("applyAmount");
			String period = params.get("period");
			
			if(StringUtils.isBlank(agencyId) ){
				renderText("申请地区不能为空");
			}
			if(StringUtils.isBlank(purposeId) ){
				renderText("借款用途不能为空");
			}
			if(StringUtils.isBlank(productId) ){
				renderText("产品类型不能为空");
			}
			if(StringUtils.isBlank(applyAmount) ){
				renderText("申请金额不能为空");
			}
			if(!NumberUtil.isNumeric(applyAmount)) {
				renderText("申请金额必须为数字类型");
			}
			if(StringUtils.isBlank(period) ){
				renderText("借款期限不能为空");
			}
 
			BorrowApply.addBorrowApply(user.id, Integer.parseInt(userTypeId), Long.parseLong(agencyId), Long.parseLong(purposeId),
					Double.parseDouble(applyAmount), Integer.parseInt(period), Long.parseLong(productId));
			error.code = 0;
			error.msg = "申请成功";
			renderJSON(error);
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error("借款申请出错！");
			e.printStackTrace();
		}
		renderJSON(error);
	}
}
