package controllers.front.account;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.t_bids;
import models.t_bill_invests;
import models.t_content_news;
import models.t_statistic_bill_invest;
import models.t_user_automatic_invest_options;
import models.v_bid_attention;
import models.v_bill_invest;
import models.v_bill_invest_detail;
import models.v_debt_auction_records;
import models.v_debt_user_receive_transfers_management;
import models.v_debt_user_transfer_management;
import models.v_front_all_debts;
import models.v_invest_records;
import models.v_receiving_invest_bids;
import models.v_user_account_statistics;
import models.v_user_attention_invest_transfers;
import models.v_user_blacklist;
import models.v_user_success_invest_bids;
import models.v_user_waiting_full_invest_bids;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.lang.StringUtils;

import payment.PaymentProxy;
import play.mvc.With;
import utils.CnUpperCaser;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;
import utils.evi.util.SceneHelper;
import annotation.DebtCheck;
import annotation.InactiveUserCheck;
import annotation.IpsAccountCheck;
import annotation.SubmitCheck;
import annotation.SubmitOnly;
import bean.QualityBid;
import business.Agency;
import business.BackstageSet;
import business.Bid;
import business.BillInvests;
import business.CreditLevel;
import business.Debt;
import business.Invest;
import business.News;
import business.Optimization.BidOZ;
import business.Optimization.InvestOZ;
import business.User;

import com.alibaba.fastjson.JSON;
import com.shove.Convert;

import constants.Constants;
import constants.IPSConstants;
import constants.IPSConstants.IpsCheckStatus;
import controllers.BaseController;
import controllers.DebtTransferCheck;
import controllers.SubmitRepeat;
import controllers.interceptor.AccountInterceptor;
import controllers.supervisor.bidManager.BidPlatformAction;

/**
 * 
 * @author cp
 *
 */
@With({AccountInterceptor.class, SubmitRepeat.class, DebtTransferCheck.class,})
public class InvestAccount extends BaseController {

	//---------------------------------------理财子账户------------------------------------------
	
	/**
	 * 我的理财子账户
	 */
	public static void investAccount(){
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		v_user_account_statistics accountStatistics = User.queryAccountStatistics(user.id, error);
		int size = Constants.QUALITY_BID_COUNT;
		/* 优质标推荐 */
		List<QualityBid> qualityBids= BidOZ.queryQualityBid(size, error);
		/* 优质债权推荐 */
		List<v_front_all_debts> qualityDebts= Debt.queryQualityDebtTransfers(error);
		
		/* 最新借款中满标倒计时提醒 */
		List<t_bids> fundraiseingBid = Bid.queryFundraiseingBid(user.id, error);
		
		if(null == fundraiseingBid)
			render(Constants.ERROR_PAGE_PATH_FRONT);
		

		List<t_content_news> news = News.queryNewForFront(Constants.NewsTypeId.MONEY_TIPS, 3, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String content = News.queryContent(Constants.NewsTypeId.VIP_AGREEMENT, error);
		
		renderArgs.put("childId", "child_14");
		renderArgs.put("labId", "lab_4");
		render(user, accountStatistics, qualityBids, qualityDebts, fundraiseingBid, news, backstageSet, content);
	}
	
	
	/**
	 * 优质标推荐
	 */
	public static void queryQualityBid() {
		ErrorInfo error = new ErrorInfo();
		int size = Constants.QUALITY_BID_COUNT;
		List<QualityBid> qualityBids= BidOZ.queryQualityBid(size, error);
		
		render(qualityBids);
	}
	
	/**
	 * 优质债权推荐
	 */
	@DebtCheck(1)
	public static void queryQualityDebt() {
		
		ErrorInfo error = new ErrorInfo();
		List<v_front_all_debts> qualityDebts= Debt.queryQualityDebtTransfers(error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		     
		render(qualityDebts);
	}
	
	/**
	 * 我的理财账单
	 */
	public static void investBills(int payType, int isOverType, int keyType, String key, int currPage){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		long userId= user.id;
		PageBean<v_bill_invest> page = BillInvests.queryMyInvestBills(payType, isOverType, keyType, key, currPage, userId, error);
		
		renderArgs.put("childId", "child_15");
		renderArgs.put("labId", "lab_4");
		render(page);
	}
	
	/**
	 * 下载数据(我的理财账单)
	 */
	public static void exportInvestBills() {
		ErrorInfo error = new ErrorInfo();
		
    	List<v_bill_invest> bills = BillInvests.queryMyAllInvestBills(error);
    	
    	if (error.code < 0) {
			renderText("下载数据失败");
		}
    	
    	JsonConfig jsonConfig = new JsonConfig();
    	jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
    	JSONArray arrBills = JSONArray.fromObject(bills, jsonConfig);
    	
    	for (Object obj : arrBills) {
			JSONObject bill = (JSONObject)obj;
			int status = bill.getInt("status");
			bill.put("is_overdue", (status == 0 || status == -1) ? "未逾期" : "逾期");
			bill.put("status", (status == -1 || status == -2) ? "未收款" : "已收款");
		}
    	
    	File file = ExcelUtils.export(
    			"我的理财账单", 
    			arrBills,
				new String[] {"账单标题", "本期应收款金额", "是否逾期", "状态", "到期还款时间", "实际还款时间"}, 
				new String[] {"title", "income_amounts", "is_overdue", "status", "repayment_time", "real_repayment_time"});
    	
    	renderBinary(file, "我的理财账单.xls");
    }
	
	/**
	 * 从理财收款标跳转到借款账单详情
	 * @param sign
	 */
	public static void toLoanBill(String sign){
		
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
		
		investBillDetails(id, 1);
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
		
		investBillDetails(id, 1);
	}
	
	/**
	 * 账单详情
	 * @param billInvestId
	 */
	public static void investBillDetails(String investId,int currPage){
		ErrorInfo error = new ErrorInfo();
		int pageSize = Constants.FIVE;
		long id = Security.checkSign(investId, Constants.BILL_ID_SIGN, 3600, error);
		
		User user = User.currUser();
		
		v_bill_invest_detail investDetail = BillInvests.queryMyInvestBillDetails(id, user.id, error);
		PageBean<t_bill_invests> page = BillInvests.queryMyInvestBillReceivables(investDetail.bid_id,investDetail.user_id, investDetail.invest_id, currPage, pageSize, error);
		BackstageSet backSet = BackstageSet.getCurrentBackstageSet();
		
		Map<String,Object> result = Invest.findInvest(investDetail.invest_id);
		render(investDetail, backSet, page,result);
	}
	
	public static void investBillDetailsPact(long id){
		Map<String,Object> result = Invest.findInvest(id);
		render(result);
	}
	
	public static void investBillDetailsCert(long id){
		Map<String,Object> result = Invest.findInvestForId(id);
		String cert = (String) result.get("certificateUrl");
		String url = SceneHelper.getViewCertificateInfoUrl(cert,(String) result.get("idNumber"));
		redirect(url);
	}
	
	/**
	 *进入 投标记录页面
	 */
	public static void investRecord(){
		
		ErrorInfo error = new ErrorInfo();
		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String paramter = params.get("paramter");
		String type = params.get("type");
		
		
		User user = User.currUser();
		PageBean<v_invest_records> page = Invest.queryUserInvestRecords(user.id, currPage,pageSize, type, paramter,error);
		
		renderArgs.put("childId", "child_16");
		renderArgs.put("labId", "lab_4");
		render(page);
		
	}
	
	/**
	 * 查询投标记录
	 * @param pageNum
	 * @param pageSize
	 * @param type
	 * @param paramter
	 */
	public static void queryInvestRecords(){
		
		ErrorInfo error = new ErrorInfo();
		String currPage = params.get("curPage");
		String pageSize = params.get("pageSize");
		String paramter = params.get("paramter");
		String type = params.get("type");
		
		
		User user = User.currUser();
		PageBean<v_invest_records> page = Invest.queryUserInvestRecords(user.id, currPage,pageSize, type, paramter,error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		render(page);
	}
	
	/**
	 * 受让债权管理
	 */
	@DebtCheck(1)
	public static void receivedDebts(int pageNum,int pageSize,String type,String paramter) {
		
		User user = User.currUser();
		
		int currPage = pageNum;
		if (params.get("currPage") != null) {
			currPage = Integer.parseInt(params.get("currPage"));
		}
		
		PageBean<v_debt_user_receive_transfers_management> page = Debt.queryUserAllReceivedDebtTransfersByConditions(user.id, type, paramter,  currPage,pageSize);
		
		if(null == page){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		render(page);
	}

	/**
	 * 进入债权转让管理页面
	 */
	@DebtCheck(1)
	public static void myDebts(int code,String msg) {
                
    	    renderArgs.put("childId", "child_20");
    	    renderArgs.put("labId", "lab_4");
		render(code,msg);
	}
	
	/**
	 * 债权转让管理分页
	 * @param pagNum
	 */
	@DebtCheck(1)
	public static void transferDebts(int pageNum,int pageSize,String type,String status,String paramter){
		
		User user = User.currUser();
		int currPage = pageNum;
		
		if (params.get("currPage") != null) {
			currPage = Integer.parseInt(params.get("currPage"));
		}
		
		PageBean<v_debt_user_transfer_management> page = Debt.queryUserAllDebtTransfersByConditions( user.id, type, paramter, status, currPage,pageSize);
		
		if(null == page){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		 render(page);
		
	}
	
	/**
	 * 受让债权管理详情页面
	 * @param debtId 债权ID
	 * @param type 转让类型
	 * @param status 转让状态
	 */
	@DebtCheck(1)
	public static void receiveDebtDetails(String sign,int type,int status, int code, String msg){
		
		
		if(type == 1 ){//定向转让
			directionalDebt(sign, code, msg);
		}
		if(type == 2 && status == 1 ){//竞价成功
			auctionSuccess(sign);
		}
		if(type == 2 && status == 0 ){//竞价竞拍中
			auctionBidding(sign);
		}
		if(type == 2 && status == 3 ){//竞价竞拍等待确认
			auctionWaitConcirm(sign);
		}
		if(status == -1){
			auctionFailure(sign);
		}
	}
	
	/**
	 * 受让债权管理竞价成功债权详情页面
	 * @param debtId
	 */
	@DebtCheck(1)
	public static void auctionSuccess(String sign){
		
		ErrorInfo error = new ErrorInfo();
		
		long viewId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		v_debt_user_receive_transfers_management debt = Debt.details(viewId);
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debt.transer_id;
		
		render(debt,debtBussiness);
	}
	
	/**
	 * 受让债权管理审核不通过债权详情页面
	 * @param debtId
	 */
	@DebtCheck(1)
	public static void auctionFailure(String sign){
		
		ErrorInfo error = new ErrorInfo();
		
		long viewId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		v_debt_user_receive_transfers_management debt = Debt.details(viewId);
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debt.transer_id;
		
		render(debt,debtBussiness);
	}
	
	/**
	 * 受让债权管理竞价竞拍中债权详情页面
	 * @param debtId
	 */
	@DebtCheck(1)
	public static void auctionBidding(String  sign){
		ErrorInfo error = new ErrorInfo();
		
		long viewId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		User user = User.currUser();
		
		v_debt_user_receive_transfers_management debt = Debt.details(viewId);
		
		long userId = debt.user_id;
		long transferId = debt.transer_id;
		//目前我的竞拍出价
		Double offerPrice = Debt.getMyAuctionPrice(transferId, userId, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debt.transer_id;
		
		renderArgs.put("offerPrice", offerPrice);
		renderArgs.put("user", user);
		render(debt,debtBussiness);
		
	}
	
	/**
	 * 受让债权管理竞价竞拍中债权详情页面
	 * @param debtId
	 */
	@SubmitCheck
	@DebtCheck(1)
	public static void auctionWaitConcirm(String  sign){
		
		
		
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		
		long viewId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		v_debt_user_receive_transfers_management debt = Debt.details(viewId);
		
		long userId = debt.user_id;
		long transferId = debt.transer_id;
		//目前我的竞拍出价
		Double offerPrice = Debt.getMyAuctionPrice(transferId, userId, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debt.transer_id;
		
		renderArgs.put("offerPrice", offerPrice);
		renderArgs.put("user", user);
		render(debt,debtBussiness);
		
	}
	
	/**
	 * 加价竞拍
	 * @param debtId
	 */
	@DebtCheck(1)
	public static void increaseAuction(long debtId,String offerPriceStr,String dealpwdStr) {

		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		JSONObject json = new JSONObject();
		
		if( StringUtils.isBlank(offerPriceStr)){
			error.msg = "出价不能为空";
			error.code = -1;
			json.put("msg", error.msg);
			renderJSON(json);
		}
		
		boolean b=offerPriceStr.matches("^[1-9][0-9]*$");
		
    	if(!b){
    		error.msg = "对不起！只能输入正整数!";
    		error.code = -1;
    		json.put("msg", error.msg);
    		renderJSON(json);
    	} 
		
		int offerPrice = Integer.parseInt(offerPriceStr);
	    Debt.auctionDebt(user.id, offerPrice, debtId,dealpwdStr, Constants.CLIENT_PC, error);
		
		json.put("msg", error.msg);
		renderJSON(json);
	}

	/**
	 * 受让债权管理定向转让债权详情页面
	 * @param debtId
	 */
	@SubmitCheck
	@DebtCheck(1)
	public static void directionalDebt(String sign, int code, String msg){
		
		//钱不够，提示跳转充值页面
		if (code == Constants.BALANCE_NOT_ENOUGH) {
			flash.put("redirectToRechage", msg);
		}
		
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		
		long viewId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		v_debt_user_receive_transfers_management debt = Debt.details(viewId);
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debt.transer_id;
		
		render(debt,debtBussiness,user);
		
	}
	
	/**
	 * 转让债权管理详情页面
	 * @param debtId
	 * @param status
	 */
	@DebtCheck(1)
	public static void debtDetails(String sign,int status) {
		
		
		
		if (status == 0) {
			debtDetailsAuditing(sign);//审核中
		}
		
		if (status == 1 || status == 2 || status == 4) {
			debtDetailsTransfering(sign);//转让中
		}
		
		if(status == 3){
			debtDetailsSuccess( sign);//已成功
		}
		
		if(status == -1){
			debtDetailsNoPass( sign);//未通过
		}
		
		if(status == -2 || status == -3 || status == -5){
			debtDetailsFailure( sign);//失败
		}
	}
	
	/**
	 * 转让债权管理已成功的详情页面
	 * @param debtId
	 */
	@DebtCheck(1)
	public static void debtDetailsSuccess(String sign){
		
		ErrorInfo error = new ErrorInfo();
		
		long viewDebtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		v_debt_user_transfer_management debt = Debt.transferDetails(viewDebtId,error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		} 
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = viewDebtId;
		
		render(debt,debtBussiness);
		
	}
	
	/**
	 * 转让债权管理转让中的详情页面
	 * @param debtId
	 */
	@DebtCheck(1)
	public static void debtDetailsTransfering(String sign){
		
		ErrorInfo error = new ErrorInfo();
		
		long viewDebtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		v_debt_user_transfer_management debt = Debt.transferDetails(viewDebtId,error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		} 
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = viewDebtId;
		
		render(debt,debtBussiness);
	}
	
	
	
	
	/**
	 * 转让债权管理审核中的详情页面
	 * @param debtId
	 */
	@DebtCheck(1)
	public static void debtDetailsAuditing(String sign){
		
		ErrorInfo error = new ErrorInfo();
		
		long viewDebtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || viewDebtId <= 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		v_debt_user_transfer_management debt = Debt.transferDetails(viewDebtId,error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		} 
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = viewDebtId;
		
		render(debt,debtBussiness);
	}
	
	
	
	
	/**
	 * 转让债权管理不通过的详情页面
	 * @param debtId
	 */
	@DebtCheck(1)
	public static void debtDetailsNoPass(String sign){
		
		ErrorInfo error = new ErrorInfo();
		
		long viewDebtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		v_debt_user_transfer_management debt = Debt.transferDetails(viewDebtId,error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		} 
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = viewDebtId;
		
		render(debt,debtBussiness);
	}
	
	
	
	/**
	 * 转让债权管理失败的详情页面
	 * @param debtId
	 */
	@DebtCheck(1)
	public static void debtDetailsFailure(String sign){
		
		ErrorInfo error = new ErrorInfo();
		
		long viewDebtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		v_debt_user_transfer_management debt = Debt.transferDetails(viewDebtId,error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		} 
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = viewDebtId;
		
		render(debt,debtBussiness);
	}
	
	
	
	/**
	 * 成交债权
	 * @param debtId
	 */
	@SubmitOnly
	@DebtCheck(1)
	public static void transact(){
		
		ErrorInfo error = new ErrorInfo();
		
		String sign = params.get("sign");
		String dealpwd = params.get("dealpwd");
		long debtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}

		Integer status = Debt.queryStatusById(debtId);

		if(status != Constants.DIRECTIONAL_MODE && status != Constants.AUCTION_MODE && status != Constants.WAIT_CONFIRM){
			myDebts(-1, "当前债权已失败，操作无效。");
		}	
		if (Constants.IPS_ENABLE) {
			String pMerBillNo = User.createBillNo();
			Map<String, Object> map = Debt.queryTransferInfo(debtId, error);
			
			if (error.code < 0) {
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			
			Debt.updateMerBillNo(debtId, pMerBillNo, error);
			
			if (error.code < 0) {
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}		
			if (error.code < 0) {
				return;
			}			
			PaymentProxy.getInstance().debtorTransfer(error, Constants.PC, debtId, dealpwd);
			
			myDebts(error.code,error.msg);
		}
		
		Debt.dealDebtTransfer(null, debtId, dealpwd, false, error);
		
		myDebts(error.code,error.msg);
	}
	
	/**
	 * 债权用户初步成交债权，之后等待竞拍方确认成交
	 * @param sign
	 */
	@DebtCheck(1)
	public static void firstDealDebt(String sign){
		
		ErrorInfo error = new ErrorInfo();
		
		long debtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		Debt.firstDealDebt(debtId, error);
		JSONObject json = new JSONObject();
		
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 进入等待满标的理财标页面
	 */
	public static void loaningInvestBid(){
		
		String type = params.get("type");
		int pageSize = params.get("pageSize") == null ? Constants.PAGE_SIZE : Convert.strToInt(params.get("pageSize"), Constants.PAGE_SIZE);
		String paramter = params.get("paramter");
		int currPage = params.get("currPage") == null ? 1 : Convert.strToInt(params.get("currPage"), 1);
		
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_waiting_full_invest_bids> page = Invest.queryUserWaitFullBids(user.id, type, paramter, currPage, pageSize,error);
		
		renderArgs.put("childId", "child_17");
		renderArgs.put("labId", "lab_4");
		render(page);
	}
	
	/**
	 * 待放款的理财标
	 */
	public static void readyReleaseBid() {
		String type = params.get("type");
		int pageSize = params.get("pageSize") == null ? Constants.PAGE_SIZE : Convert.strToInt(params.get("pageSize"), Constants.PAGE_SIZE);
		String paramter = params.get("paramter");
		int currPage = params.get("currPage") == null ? 1 : Convert.strToInt(params.get("currPage"), 1);
		
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_waiting_full_invest_bids> page = Invest.queryUserReadyReleaseBid(user.id, type, paramter, currPage, pageSize,error);
		
		renderArgs.put("childId", "child_41");
		renderArgs.put("labId", "lab_4");
		render(page);
	}
	
	/**
	 * 收款中的理财标
	 * @param result
	 */
	public static void repayingInvestBid(int code,String msg){
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		int currPage = 1;

		if (StringUtils.isNotBlank(params.get("currPage"))) {
			currPage = Integer.parseInt(params.get("currPage"));
		}
		int pageSize = Constants.TEN;
		
		String type = params.get("type");
		String keyWords = params.get("keyWords");
		PageBean<v_receiving_invest_bids> page = Invest.queryUserAllReceivingInvestBids(user.id, type, keyWords, currPage, pageSize,error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		renderArgs.put("childId", "child_18");
		renderArgs.put("labId", "lab_4");
		render(code,msg,page);
	}
	
	/**
	 * 收款中的理财标
	 * @param pageNum
	 */
	public static void queryUserAllReceivingInvestBids(int pageNum,int pageSize,String type,String paramter){
		
		ErrorInfo error = new ErrorInfo();
		int currPage = pageNum;

		if (params.get("currPage") != null) {
			currPage = Integer.parseInt(params.get("currPage"));
		}
		User user = User.currUser();
		
		PageBean<v_receiving_invest_bids> page = Invest.queryUserAllReceivingInvestBids(user.id, type, paramter, currPage, pageSize,error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		render(page);
	}
	
	
	/**
	 * 收款中的理财标账单
	 * @param investId
	 */
	public static void billDetails(String sign){
		
		ErrorInfo error = new ErrorInfo();
		
		long investId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		Long billId = Invest.queryBillByInvestId(investId, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		User user  = User.currUser();
		
		v_bill_invest_detail investDetail = BillInvests.queryMyInvestBillDetails(billId, user.id, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		BackstageSet backSet = BackstageSet.getCurrentBackstageSet();
		
		render(investDetail, backSet);
	}
	
	
	
	/**
	 * 债权转让
	 * @param investId
	 */
	@DebtCheck(1)
	public static void debtTransfer(String sign){
		
		ErrorInfo error = new ErrorInfo();
		long investId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		Invest invest = new Invest();
		invest.id = investId;
		
		//目前，债权转让只支持一次转让。
		if(Invest.isSecondCretansfer(investId)){
			error.msg = "不支持二次债权转让!";
			error.code = -1;
			InvestAccount.repayingInvestBid(error.code,error.msg);
		}

		
		Map<String,String> map = Debt.debtBidReceiveSituation(investId);//债权转让借款标资金情况
		render(invest,map);
	}
	
	
	
	/**
	 * 接收定向债权转让
	 */
	@SubmitOnly
	@DebtCheck(1)
	@IpsAccountCheck
	@InactiveUserCheck
	public static void acceptDebts(){
		ErrorInfo error = new ErrorInfo();
		String sign = params.get("sign");
		String dealpwd = params.get("dealpwd");
		long debtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		Integer status = Debt.queryStatusById(debtId);

		if(status != Constants.DIRECTIONAL_MODE && status != Constants.AUCTION_MODE && status != Constants.WAIT_CONFIRM){
			myDebts(-1, "当前债权已失败，操作无效。");
		}
		
		if (!Debt.isEnoughToDealDebt(debtId, error)) {
			
			long detDetailId = Debt.queryDebtDetailIdByDebtIdAndUserId(debtId, User.currUser().id);
			int type = Debt.queryTypeById(debtId);
			//钱不够，返回受让债权详请页面,并提示是否跳转到充值页面;
			receiveDebtDetails(Security.addSign(detDetailId, Constants.BID_ID_SIGN), type, status, error.code, error.msg);
		}
		
		if (Constants.IPS_ENABLE) {
			String pMerBillNo = User.createBillNo();						
			if (error.code < 0) {
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			
			Debt.updateMerBillNo(debtId, pMerBillNo, error);
			
			if (error.code < 0) {
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			
			/*IpsDetail.setIpsInfo(Long.parseLong(pMerBillNo), "{\"debtId\":\""+debtId+"\"}", error);
//			Cache.add(pMerBillNo, debtId, IPSConstants.CACHE_TIME);
			
			long bidUser = (Long) map.get("bidUser");
			long fromUserId = (Long) map.get("fromUserId"); 
			long toUserId = (Long) map.get("toUserId");
			String bidNo = (String) map.get("bidNo"); 
			String pCreMerBillNo = (String) map.get("pCreMerBillNo");
			double managefee = (Double) map.get("managefee");
			double pCretAmt = (Double) map.get("pCretAmt");
			double interest = (Double) map.get("interest");
			double pCretAmt2 = (Double) map.get("pCretAmt2");
			double pPayAmt = (Double) map.get("pPayAmt");
			String orderDate =  (String) map.get("orderDate");
			String printAmt = (String) map.get("printAmt");
			
			IpsDetail detail = new IpsDetail();
			detail.merBillNo = pMerBillNo;
			detail.userName = User.queryUserNameById(toUserId, error);
			detail.time = new Date();
			detail.type = IPSOperation.REGISTER_CRETANSFER;
			detail.status = Status.FAIL;
			detail.create(error);
			
			if (error.code < 0) {
				return;
			}*/
			//定向转让调用托管接口
			PaymentProxy.getInstance().debtorTransfer(error, Constants.PC, debtId, dealpwd);
			
			myDebts(error.code,error.msg);
		}
		
		
		Debt.dealDebtTransfer(null, debtId, dealpwd, false, error);
		
		myDebts(error.code,error.msg);
		
	}
	
	
	
	/**
	 * 拒收定向转让债权
	 */
	@DebtCheck(1)
	public static void notAccept(String sign){
		
		ErrorInfo error = new ErrorInfo();
		
		long debtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		
		Debt.refuseAccept(debtId, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 添加黑名单
	 * @param bidId
	 * @param reason
	 */
	public static void addBlack(long bidId,  String reason){
		
		ErrorInfo error = new ErrorInfo();
		User currUser = User.currUser();
		User user = new User();
		user.id = currUser.id;

		JSONObject json = new JSONObject();
		
		user.addBlacklist(bidId, reason, error);
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 进入已完成的理财标页面
	 */
	public static void successInvestBid(int currPage, int pageSize, String type, String keyWords){
		ErrorInfo error = new ErrorInfo();

		User user = User.currUser();
		PageBean<v_user_success_invest_bids> page = Invest.queryUserSuccessInvestBids(user.id, type, keyWords, currPage == 0 ? 1 : currPage, pageSize == 0 ? Constants.PAGE_SIZE : pageSize, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		renderArgs.put("childId", "child_19");
		renderArgs.put("labId", "lab_4");
		render(page);
	}
	
	/**
	 * 我的黑名单
	 */
	public static void blackList(String key, int currPage){
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		
		if (currPage <= 0) {
			currPage = 1;
		}
		int pageSize = Constants.PAGE_SIZE ;
		PageBean<v_user_blacklist> page = User.queryBlacklist(user.id, key, currPage,pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT, user);
		}
		
		renderArgs.put("childId", "child_21");
		renderArgs.put("labId", "lab_4");
		render(user, page);
	}
	
	/**
	 * 删除黑名单
	 * @param blacklistId
	 */
	public static void removeBlacklist(long blacklistId) {
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		user.deleteBlacklist(user.id, blacklistId, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 理财情况查询
	 */
	public static void investStatistics(int year, int month, int orderType, int currPage){
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		
		PageBean<t_statistic_bill_invest> page = InvestOZ.queryUserInvestStatistics(user.id, 
				year, month, orderType, currPage, error);
		
		if(error.code < 0) {
			render(user, Constants.PAGE_SIZE);
		}
		
		renderArgs.put("childId", "child_22");
		renderArgs.put("labId", "lab_4");
		render(user, page);
	}
	
	/**
	 * 投标机器人
	 * @param result
	 */
	public static void auditmaticInvest(int result,String msg) {
		
		ErrorInfo error = new ErrorInfo();
      
		User user = User.currUser();
		
		double balance = Invest.getUserBalance(user.id);// 个人可用余额
		
		t_user_automatic_invest_options robot = Invest.getUserRobot(user.id,error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		
         List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		renderArgs.put("childId", "child_23");
		renderArgs.put("labId", "lab_4");
		render(balance, robot, creditLevels, result,msg);
	}
	
	
	/**
	 * 关闭投标机器人
	 * @param robotId
	 */
	public static void closeRobot(long robotId) {
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		
		Invest.closeRobot(user.id ,robotId, error);
		
		auditmaticInvest(error.code, error.msg);
	}
	
	/**
	 * 设置投标机器人
	 */
	public static void saveOrUpdateRobot(int validType, int validDate, double minAmount, double maxAmount){
		
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		
		String msg = "";
		String bidAmount = params.get("bidAmount");
		String rateStart = params.get("rateStart");
		String rateEnd = params.get("rateEnd");
		String deadlineStart = params.get("deadlineStart");
		String deadlineEnd = params.get("deadlineEnd");
		String creditStart = params.get("creditStart");
		String creditEnd = params.get("creditEnd");
		String remandAmount = params.get("remandAmount");
		String [] borrow = params.getAll("loanType");
		
		if(StringUtils.isBlank(bidAmount) || StringUtils.isBlank(rateStart) || StringUtils.isBlank(rateEnd) || borrow.length <= 0){
			msg = "请正确设置各种参数！";
			auditmaticInvest(-1,msg);
		}
		
		double amount = Double.parseDouble(bidAmount);
		
		if(amount > Constants.AUTO_INVEST || amount < 0){
			msg = "投标金额有误!";
			auditmaticInvest(-1,msg);
		}
		
		if(minAmount > Constants.AUTO_INVEST || minAmount < 0){
			msg = "借款额度下限有误!";
			auditmaticInvest(-1,msg);
		}
		
		if(maxAmount > Constants.AUTO_INVEST || maxAmount < 0){
			msg = "借款额度上限有误!";
			auditmaticInvest(-1,msg);
		}

		if (validDate <= 0) {
			msg = "请选择有效期";
			auditmaticInvest(-1,msg);
		}
		
		if (minAmount < IPSConstants.MIN_AMOUNT) {
			msg = "借款额度必须大于"+IPSConstants.MIN_AMOUNT;
			auditmaticInvest(-1,msg);
		}
		
		if (minAmount > maxAmount) {
			msg = "最高借款额度不能小于最低借款额度";
			auditmaticInvest(-1,msg);
		}
		
		boolean b=bidAmount.matches("^[1-9][0-9]*$");
    	if(!b){
    		msg = "对不起！投标金额只能输入正整数!";
			auditmaticInvest(-1,msg);
    	} 
		
    	
    	if(!NumberUtil.isNumericDouble(rateStart)){
    		msg = "对不起！设置的最低利率必须是数字!";
			auditmaticInvest(-1,msg);
    	}
    	
    	if(!NumberUtil.isNumericDouble(rateEnd)){
    		msg = "对不起！设置的最高利率必须是数字!";
			auditmaticInvest(-1,msg);
    	}
    	
    	if(!NumberUtil.isNumeric(remandAmount)){
    		msg = "对不起！设置的保留金额必须是数字!";
			auditmaticInvest(-1,msg);
    	}
    	
		String str = "";
		for(int i = 0;i < borrow.length;i++){
			str += borrow[i]+",";
		}
		
		String borrowWay = str.substring(0, str.length()-1);
		
		int result = Invest.saveOrUpdateRobot(user.id, validType, validDate, minAmount, maxAmount, bidAmount, 
				 rateStart, rateEnd, deadlineStart, deadlineEnd, creditStart, creditEnd, remandAmount, borrowWay, error);
		 
		if (Constants.IPS_ENABLE && error.code >= 0) {
				
			//托管投资签约， 业务逻辑在回调方法中
			Map<String, String> map = new HashMap<String, String>();
			map.put("userId", user.id+"");
			map.put("validType", validType+"");
			map.put("validDate", validDate+"");
			map.put("minAmount", minAmount+"");
			map.put("maxAmount", maxAmount+"");
			map.put("bidAmount", bidAmount);
			map.put("rateStart", rateStart);
			map.put("rateEnd", rateEnd);
			map.put("deadlineStart", deadlineStart);
			map.put("deadlineEnd", deadlineEnd);
			map.put("creditStart", creditStart);
			map.put("creditEnd", creditEnd);
			map.put("remandAmount", remandAmount);
			map.put("borrowWay", borrowWay);
			
			PaymentProxy.getInstance().autoInvestSignature(error, Constants.PC, map);
			
			auditmaticInvest(-1,error.msg);
			
		}	 

		msg = error.msg;
		auditmaticInvest(result,msg);
	
	}
	
	/**
	 * 进入我的收藏页面
	 */
	public static void attentionBid() {
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		PageBean<v_bid_attention> pageBean = new PageBean<v_bid_attention>();
		pageBean.page = Bid.queryAttentionBid(pageBean, error, BidPlatformAction.getParameter(pageBean, user.id + ""));
                
		renderArgs.put("childId", "child_24");
		renderArgs.put("labId", "lab_4");
		render(pageBean);
	}
	
	
	/**
	 * 我的收藏————债权
	 * @param pageNum
	 */
	@DebtCheck(1)
	public static void attentionDebts(int pageNum,int pageSize,String type,String paramter){
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_attention_invest_transfers> page = new PageBean<v_user_attention_invest_transfers>();

		int currPage = pageNum;
		if (params.get("currPage") != null) {
			currPage = Integer.parseInt(params.get("currPage"));
		}
		User user = User.currUser();
		page = Debt.queryUserAttentionDebtTransfers(user.id, currPage, type, paramter, pageSize,error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		render(page);
	}
	
	//资产负债表
	public static void getBalance(){
		render();
	}
	
	//收款纪录
	public static void receivedRecord(){
		render();
	}
	
	/**
	 * ajax分页查询债权竞拍记录
	 * @param debtId
	 */
	public static void ajaxViewAuctionRecords(int pageNum, String sign){
		
		
		ErrorInfo error = new ErrorInfo();
		

		long debtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		 
		int currPage = pageNum;
		
		int pageSize = Constants.TEN;
		if (params.get("currPage") != null) {
			currPage = Integer.parseInt(params.get("currPage"));
		}
		PageBean<v_debt_auction_records> page = Invest.viewAuctionRecords(currPage,pageSize, debtId, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		render(page);
	}
	
	/**
	 * 居间服务协议
	 * @param bidId
	 */
	public static void repayingInvestBidBrokerageProtoco(String sign){
		
		ErrorInfo error = new ErrorInfo();
		
		long bidId=Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		//投资人
		User useri= User.currUser();
		
		//标的信息
		Bid b = new Bid();
		b.id = bidId;

		//借款人信息
		User userb = new User();
		userb.id = b.userId;

		//编号(QBBXX+满表时间的年月日时分秒+标的id)
		String dategs = DateUtil.dateToString1(b.realInvestExpireTime);
		
		
		
		//期限
		String dates = "";
		if(b.periodUnit == -1){
			dates = b.period + " 年";
		}else if(b.periodUnit == 0){
			dates = b.period + " 个月";
		}else if(b.periodUnit == 1){
			dates = b.period + " 天";
		}
		
		
		
		//编号(QBBXX+满表时间的年月日时分秒+标的id)
		//String date = DateUtil.simple2(b.realInvestExpireTime);
		//String noid = "QBBXX"+date+b.id;
		BackstageSet backstageSet=BackstageSet.getCurrentBackstageSet();
		String noid=backstageSet.loanNumber+bidId;
		
		render(noid,useri,b,userb,dates,dategs);
	}

	/**
	 * 借款协议
	 * @param bidId
	 */
	public static void loanAgreement(String sign){
		
		ErrorInfo error = new ErrorInfo();
		
		long bidId=Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId == 0){
			return;
		}
		
		//投资人
		User useri= User.currUser();
		
		//标的信息
		Bid b = new Bid();
		b.id = bidId;
	
		if(b.repayment == null){
			return;
		}
		//借款人信息
		User userb = new User();
		userb.id = b.userId;
		
		//投资信息
		Invest invest = new Invest();
		invest.bid = b;
		
		//投资还款表
		BillInvests bi = new BillInvests();
		
		List<t_bill_invests> tbilist = bi.queryMyInvestBillReceivablesBid(bidId, useri.id, error);
		
		int dateDay = 0;
		DecimalFormat df = new DecimalFormat();
	    df.applyPattern("###.00");
		if(b.repayment.id == 1){//按月还款、等额本息 
			for (t_bill_invests tBillInvests : tbilist) {
				tBillInvests.dxreceive_amount = new CnUpperCaser(df.format(tBillInvests.receive_amount)).getCnString();
			}
			t_bill_invests tbi = tbilist.get(0);
			dateDay = DateUtil.getDay(tbi.receive_time);
		}else if(b.repayment.id == 2){//按月付息、到期还本 
			for (t_bill_invests tBillInvests : tbilist) {
				tBillInvests.dxreceive_amount = new CnUpperCaser(df.format(tBillInvests.receive_amount)).getCnString();
			}
			t_bill_invests tbi = tbilist.get(0);
			dateDay = DateUtil.getDay(tbi.receive_time);
		}else if(b.repayment.id == 3){//一次性还款 
			for (t_bill_invests tBillInvests : tbilist) {
				tBillInvests.dxreceive_amount = new CnUpperCaser(df.format(tBillInvests.receive_amount)).getCnString();
			}
			t_bill_invests tbi = tbilist.get(0);
			dateDay = DateUtil.getDay(tbi.receive_time);
		}
	
		//数字转大写
		String dxMoney = new CnUpperCaser(df.format(invest.bid.amount)).getCnString();
		//b.period
		
		
		//期限
		String dates = "";
		if(b.periodUnit == -1){
			dates = b.period + " 年";
		}else if(b.periodUnit == 0){
			dates = b.period + " 个月";
		}else if(b.periodUnit == 1){
			dates = b.period + " 天";
		}
		
		
		
		//编号(QBBXX+满表时间的年月日时分秒+标的id)
		/*String date = DateUtil.simple2(b.realInvestExpireTime);
		String noid = "QBBXX"+date+b.id;*/
		BackstageSet backstageSet=BackstageSet.getCurrentBackstageSet();
		String noid=backstageSet.loanNumber+bidId;
		
		render(noid,useri,b,userb,invest,dxMoney,dates,tbilist,dateDay);
	}

	/**
	 * 居间服务协议
	 * @param bidId
	 */
	public static void letterOfGuarantee(String sign){
		
		ErrorInfo error = new ErrorInfo();
		
		long bidId=Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		//投资人
		User useri= User.currUser();
		
		//标的信息
		Bid b = new Bid();
		b.id = bidId;
	
		//借款人信息
		User userb = new User();
		userb.id = b.userId;
	
	
		//投资信息
		Invest invest = new Invest();
		invest.bid = b;
		
		//编号(QBBXX+满表时间的年月日时分秒+标的id)
		String dategs = DateUtil.dateToString1(b.realInvestExpireTime);
	
		//数字转大写
		DecimalFormat df = new DecimalFormat();
	    df.applyPattern("###.00");
		String dxMoney = new CnUpperCaser(df.format(invest.bid.amount)).getCnString();
		
		
		
		//期限
		String dates = "";
		if(b.periodUnit == -1){
			dates = b.period + " 年";
		}else if(b.periodUnit == 0){
			dates = b.period + " 个月";
		}else if(b.periodUnit == 1){
			dates = b.period + " 日";
		}
		
		
		
		//编号(QBBXX+满表时间的年月日时分秒+标的id)
		//String date = DateUtil.simple2(b.realInvestExpireTime);
		//String noid = "QBBXX"+date+b.id;
		BackstageSet backstageSet=BackstageSet.getCurrentBackstageSet();
		String noid=backstageSet.loanNumber+bidId;
		
		
		render(noid,useri,b,userb,dates,dategs,dxMoney);
	}
	
	/**
	 *担保函
	 * @param bidId
	 */
	public static void guarantee(String sign){
		
		ErrorInfo error = new ErrorInfo();
		
		long bidId=Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		//投资人
		User useri= User.currUser();
		
		//标的信息
		Bid b = new Bid();
		b.id = bidId;
	
		//借款人信息
		User userb = new User();
		userb.id = b.userId;
	
	
		//投资信息
		Invest invest = new Invest();
		invest.bid = b;
		
		//编号(QBBXX+满表时间的年月日时分秒+标的id)
		String dategs = DateUtil.dateToString1(b.realInvestExpireTime);
	
		//数字转大写
		DecimalFormat df = new DecimalFormat();
	    df.applyPattern("###.00");
		String dxMoney = new CnUpperCaser(df.format(invest.bid.amount)).getCnString();
	
	
		Agency age = b.agency;
		//期限
		String dates = "";
		if(b.periodUnit == -1){
			dates = b.period + " 年";
		}else if(b.periodUnit == 0){
			dates = b.period + " 个月";
		}else if(b.periodUnit == 1){
			dates = b.period + " 日";
		}
		
		
		
		//编号(QBBXX+满表时间的年月日时分秒+标的id)
		//String date = DateUtil.simple2(b.realInvestExpireTime);
		//String noid = "QBBXX"+date+b.id;
		BackstageSet backstageSet=BackstageSet.getCurrentBackstageSet();
		String noid=backstageSet.loanNumber+bidId;
		
		if(StringUtils.isBlank(age.imageFilenames)){
			age.imageFilenames = "/public/images/default.png";
		}
		
		 render(noid,useri,b,userb,dates,dategs,dxMoney,age);
	}
	

	/**
	 * 取消关注债权
	 */
	@DebtCheck(1)
	public static void canaleDebt(long attentionId){
		
		ErrorInfo error = new ErrorInfo();
		
		Debt.canaleDebt(attentionId, error);
		
		JSONObject json = new JSONObject();
		
		json.put("error", error);
		renderJSON(json);
	}
	
	
	/**
	 * 取消关注借款标
	 * @param userId
	 * @param bidId
	 */
	public static void cancleBid(long userId,long bidId){
		ErrorInfo error = new ErrorInfo();
		Debt.cancleBid(bidId, userId, error);
		
		JSONObject json = new JSONObject();
		
		json.put("error", error);
		renderJSON(json);
		
	}
	
	/**
	 * 借款合同
	 */
	public static void investPact(String sign, int type){
		
		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
			
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		String pact = Invest.queryPact(id);
		
		render(pact, type);
	}
	
	/**
	 * 居间服务协议
	 */
	public static void intermediaryAgreement(String sign, int type){
		
		ErrorInfo error = new ErrorInfo();
			
		long id = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
			
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		String intermediaryAgreement = Invest.queryIntermediaryAgreement(id);
		
		render(intermediaryAgreement, type);
	}
	
	/**
	 * 保障涵
	 */
	public static void guaranteeBid(String sign, int type){
		
		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
			
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		String guaranteeBid = Invest.queryGuaranteeBid(id);
		
		render(guaranteeBid, type);
	}
	
	/**
	 * 转让协议
	 */
	public static void deptPact(String sign){
		

		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
			
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		String pact = Debt.queryPact(id,error);
		
		render(pact);
	}
	
	/**
	 * 查询理财人的理财合同，居间服务协议以及保证函
	 * @param sign
	 */
	public static void queryPactTemplates(String sign){
		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if (id < 1) {
			renderJSON(error);
		}
		
		JSONObject json = new JSONObject();
		json.put("pact", Invest.queryPact(id));
		json.put("intermediaryAgreement", Invest.queryIntermediaryAgreement(id));
		json.put("guaranteeBid", Invest.queryGuaranteeBid(id));
		
		renderJSON(json);
	}
}