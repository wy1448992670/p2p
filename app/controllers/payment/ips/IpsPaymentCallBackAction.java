package controllers.payment.ips;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Map;

import models.t_bids;
import models.t_bills;
import models.t_invests;
import models.t_users;

import org.json.JSONException;

import payment.PaymentProxy;
import payment.ips.service.IpsPaymentCallBackService;
import payment.ips.service.IpsPaymentService;
import payment.ips.util.IpsPaymentUtil;
import play.Logger;
import utils.ErrorInfo;
import utils.JPAUtil;
import business.Bid;
import business.Bill;
import business.Debt;
import business.Invest;
import business.User;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shove.Convert;

import constants.Constants;
import constants.IPSConstants;
import constants.IPSConstants.CompensateType;
import constants.PayType;
import controllers.front.account.AccountHome;
import controllers.front.account.CheckAction;
import controllers.payment.PaymentBaseAction;
import controllers.supervisor.bidManager.BidAgencyAction;
import controllers.supervisor.bidManager.BidPlatformAction;
import controllers.supervisor.financeManager.PayableBillManager;
import controllers.supervisor.financeManager.ReceivableBillManager;

/**
 * 环讯托管回调实现类
 * @author liuwenhui
 *
 */
public class IpsPaymentCallBackAction extends PaymentBaseAction{
	
	private static IpsPaymentCallBackService ipsPaymentCallBackService = new IpsPaymentCallBackService(); 
	private static IpsPaymentService ipsPaymentService = new IpsPaymentService(); 
	
	public static Gson gson = new Gson();
	
	/**
	 * 开户回调
	 * @throws JSONException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void returnRegister() throws Exception{
		
		Map<String, String> paramMap = ipsPaymentCallBackService.getRespParams(params);
		
		IpsPaymentUtil.printData(paramMap, "开户回调参数", PayType.REGISTER);
		
		Map<String, String> parseXml = IpsPaymentUtil.parseXmlToJson(params.get("p3DesXmlPara"));
		ErrorInfo error = new ErrorInfo();
		error = IpsPaymentUtil.checkSign(paramMap, "开户", parseXml, PayType.REGISTER.name(), error);
		
		if(error.code < 0){		
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/home", "登陆页面"));
			return;
		}
		Map<String, Object> dataMap = IpsPaymentUtil.queryMmmDataByOrderNum(parseXml.get("pMemo1"));
		User user = new User();
		long userId = Convert.strToLong(dataMap.get("userId").toString(),  -1);
		user.updateIpsAcctNo(userId, parseXml.get("pIpsAcctNo"), error);
		
		approve();
	}
	
	/**
	 * 充值回调
	 * @throws JSONException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void returnRecharge() throws Exception{
		
		Map<String, String> paramMap = ipsPaymentCallBackService.getRespParams(params);
		
		IpsPaymentUtil.printData(paramMap, "充值回调参数", PayType.RECHARGE);
		
		Map<String, String> parseXml = IpsPaymentUtil.parseXmlToJson(params.get("p3DesXmlPara"));
		ErrorInfo error = new ErrorInfo();
		error = IpsPaymentUtil.checkSign(paramMap, "充值", parseXml, PayType.REGISTER.name(), error);
		
		if(error.code < 0){			
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}
		Map<String, Object> dataMap = IpsPaymentUtil.queryMmmDataByOrderNum(parseXml.get("pMemo1"));
		
		User.recharge(dataMap.get("payNumber").toString(), Double.parseDouble(dataMap.get("amount").toString()), error);
		
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}
		payErrorInfo(ErrorInfo.createError(error, 1, "充值成功!", "/front/account/dealRecord", "交易记录页面!"));
	}
	/**
	 * 标的发布回调
	 * @return 
	 * @throws Exception 
	 */
	public static void returnBidCreate() throws Exception{
		
		Map<String, String> paramMap = ipsPaymentCallBackService.getRespParams(params);
		
		IpsPaymentUtil.printData(paramMap, "标的发布回调参数", PayType.BIDCREATE);
		
		Map<String, String> parseXml = IpsPaymentUtil.parseXmlToJson(params.get("p3DesXmlPara"));
		ErrorInfo error = new ErrorInfo();
		error = IpsPaymentUtil.checkSign(paramMap, "标的发布回调", parseXml, PayType.BIDCREATE.name(), error);
		
		Map<String, Object> dataMap = IpsPaymentUtil.queryMmmDataByOrderNum(parseXml.get("pMemo1"));
		t_bids tbid = gson.fromJson(dataMap.get("tbid").toString(), t_bids.class);
		
		if(error.code < 0){		
			
			//机构合作标，去后台提示
			if(tbid.agency_id > 0){		
				flash.error(error.msg);	
				BidAgencyAction.agencyBidList(0);
			}			
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}		
		//标的号
		String bidNo = parseXml.get("pBidNo");		
		//ips流水号
		String pIpsBillNo = parseXml.get("pIpsBillNo");		
		//流水号
		String pMerBillNo = parseXml.get("pMerBillNo");	
		
		tbid.mer_bill_no = pMerBillNo; //流水号
		tbid.ips_bill_no = pIpsBillNo; //ips流水号, 用于解冻保证金
		tbid.bid_no = bidNo; //标的号
		int client = Convert.strToInt(dataMap.get("client").toString(), -1);
		Bid bid = new Bid();
		bid.afterCreateBid(tbid, bidNo, client, -1, error);
		if(error.code < 0 ){	
			
			//机构合作标，去后台提示
			if(tbid.agency_id > 0){		
				flash.error(error.msg);	
				BidAgencyAction.agencyBidList(0);
			}
			System.out.println("4"+error.msg);
			//前台标的发布，在前台页面提示
			error.code = -1;
			error.msg = "标的发布失败!";
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "返回标的列表页面"));
		}
		
		//机构合作标，去后台提示
		if(tbid.agency_id > 0){	
			flash.error(error.msg);	
			BidAgencyAction.agencyBidList(0);
		}
		//前台标的发布，在前台页面提示
		error.code = 1;
		error.msg = "标的发布成功!";
		payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "返回标的列表页面"));
	}
	
	/**
	 * 投资回调
	 * @return 
	 * @throws Exception 
	 */
	public static void returnInvest() throws Exception{
		
		Map<String, String> paramMap = ipsPaymentCallBackService.getRespParams(params);
		
		IpsPaymentUtil.printData(paramMap, "投资回调参数", PayType.INVEST);
		Map<String, String> parseXml = IpsPaymentUtil.parseXmlToJson(params.get("p3DesXmlPara"));
		ErrorInfo error = new ErrorInfo();
		error = IpsPaymentUtil.checkSign(paramMap, "投资回调", parseXml, PayType.INVEST.name(), error);		
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/invest/investHome", "我要理财页面!"));
			return;
		}
		Map<String, Object> dataMap = IpsPaymentUtil.queryMmmDataByOrderNum(parseXml.get("pMemo1"));
		
		
		//组装回调需要参数
		Bid bids = new Bid();
		bids.id = Convert.strToLong(dataMap.get("bidId").toString(), -1);
		
		long bidId = Convert.strToLong(dataMap.get("bidId").toString(), -1);
		
		double investAmount = Convert.strToDouble(dataMap.get("investAmount").toString(), -1);
		
		String pMerBillNo = dataMap.get("pMerBillNo").toString();
		
		Long userId = Convert.strToLong(dataMap.get("userId").toString(), -1);
		
		t_users user = t_users.findById(userId);
		
		
		//计算投资时分摊到投资人身上的借款管理费及投标奖励
		Map<String, Double> map = Bid.queryAwardAndBidFee(bids, investAmount, error);
		double award = map.get("award");
		double bid_fee = map.get("bid_fee") ;	
		
		Invest.doInvest(user, bidId, investAmount, pMerBillNo, 0, Constants.CLIENT_PC, award, bid_fee,null, error);
		
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/invest/investHome", "投资页面!"));
			return;
		}
		
		t_invests t_invest = t_invests.find(" mer_bill_no = ?", pMerBillNo).first();
		t_invest.ips_bill_no = parseXml.get("pP2PBillNo");
		t_invest.save();
		
		error.code = 1;
		error.msg = "投资成功!";
		payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/dealRecord", "交易记录页面!"));
	}
	
	
	/**
	 * 满标放款回调
	 * @throws Exception
	 */
	public static void returnBidAuditSucc() throws Exception{
		
		Map<String, String> paramMap = ipsPaymentCallBackService.getRespParams(params);
		
		IpsPaymentUtil.printData(paramMap, "满标放款回调参数", PayType.BID_AUDIT_SUCC);
		Map<String, String> parseXml = IpsPaymentUtil.parseXmlToJson(params.get("p3DesXmlPara"));
		ErrorInfo error = new ErrorInfo();
		error = IpsPaymentUtil.checkSign(paramMap, "满标放款回调", parseXml, PayType.BID_AUDIT_SUCC.name(), error);		
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}
		String orderNum = parseXml.get("pMemo1");
		IpsPaymentUtil.checkAllOrderIsComplete(orderNum, error);
		if(error.code < 0){
			Logger.info("订单未全部完成!");
			return;
		}
		Map<String, Object> dataMap = IpsPaymentUtil.queryMmmDataByOrderNum(orderNum);		
		long bid_id = Convert.strToLong(dataMap.get("bid").toString(), -1L);
		Bid bid = new Bid();
		bid.id = bid_id;
		
		//处理本地业务逻辑
		Map<String, Double> map = Bid.queryBidAwardAndBidFee(bid, error);
		bid.serviceFees = map.get("bid_fee");
		bid.eaitLoanToRepayment(error);
	}
	
	/**
	 * 解冻保证金回调
	 * @throws Exception
	 */
	public static void returnUnFreeze() throws Exception{
		
		Map<String, String> paramMap = ipsPaymentCallBackService.getRespParams(params);
		
		IpsPaymentUtil.printData(paramMap, "解冻保证金回调参数", PayType.UNFREEZE);
		Map<String, String> parseXml = IpsPaymentUtil.parseXmlToJson(params.get("p3DesXmlPara"));
		ErrorInfo error = new ErrorInfo();
		error = IpsPaymentUtil.checkSign(paramMap, "解冻保证金回调", parseXml, PayType.UNFREEZE.name(), error);		
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}
		String orderNum = parseXml.get("pMemo1");
		IpsPaymentUtil.checkAllOrderIsComplete(orderNum, error);
		if(error.code < 0){
			Logger.info("订单未全部完成!");
			return;
		}
		Map<String, Object> dataMap = IpsPaymentUtil.queryMmmDataByOrderNum(orderNum);		
		long bid_id = Convert.strToLong(dataMap.get("bid").toString(), -1L);
		Bid bid = new Bid();
		bid.id = bid_id;
		
		//处理本地业务逻辑
		Map<String, Double> map = Bid.queryBidAwardAndBidFee(bid, error);
		bid.serviceFees = map.get("bid_fee");
		bid.eaitLoanToRepayment(error);
	}
	
	/**
	 * 还款回调
	 * @throws Exception
	 */
	public static void returnRepayment() throws Exception{
		
		Map<String, String> paramMap = ipsPaymentCallBackService.getRespParams(params);
		
		IpsPaymentUtil.printData(paramMap, "还款回调参数", PayType.REPAYMENT);
		Map<String, String> parseXml = IpsPaymentUtil.parseXmlToJson(params.get("p3DesXmlPara"));
		ErrorInfo error = new ErrorInfo();
		error = IpsPaymentUtil.checkSign(paramMap, "还款回调", parseXml, PayType.REPAYMENT.name(), error);		
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}
		//根据流水号，查询回调需要的参数
		String orderNum = parseXml.get("pMemo1");
		Map<String, Object> dataMap = IpsPaymentUtil.queryMmmDataByOrderNum(orderNum);	
		
		long billId = Convert.strToLong(dataMap.get("billId").toString(), -1L);
		
		//借款人id
		long borrowerId = Convert.strToLong(dataMap.get("borrowerId").toString(), -1L);
		
		//初始化账单
		Bill bill = new Bill();
		bill.setId(billId);
		
		//处理本地业务逻辑
		bill.repayment(borrowerId, error);
		if(error.code < 0 ){			
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}		
		error.code = 1;
		error.msg = "还款成功!";
		payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "投资页面"));
	}
	/**
	 * 自动还款签约
	 * @throws Exception
	 */
	public static void returnAutoRepaymentSignature() throws Exception{
		
		Map<String, String> paramMap = ipsPaymentCallBackService.getRespParams(params);
		
		IpsPaymentUtil.printData(paramMap, "自动还款签约回调参数", PayType.AUTO_REPAYMENT_SIGNATURE);
		Map<String, String> parseXml = IpsPaymentUtil.parseXmlToJson(params.get("p3DesXmlPara"));
		ErrorInfo error = new ErrorInfo();
		error = IpsPaymentUtil.checkSign(paramMap, "自动还款签约", parseXml, PayType.AUTO_REPAYMENT_SIGNATURE.name(), error);		
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/bidAction/index", "我要借款页面"));
			return;
		}
		String pIpsAuthNo = parseXml.get("pIpsAuthNo");
		String orderNum = parseXml.get("pMemo1");
		Map<String, Object> dataMap = IpsPaymentUtil.queryMmmDataByOrderNum(orderNum);	
		long userId = Convert.strToLong(dataMap.get("userId").toString(), -1L);
		JPAUtil.executeUpdate(error, " update t_users u set u.ips_repay_auth_no = ? where u.id = ? ", pIpsAuthNo, userId);
		
		error.code = 1;
		error.msg = "自动还款签约成功!";
		payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/bidAction/index", "秒还标发布页面"));
		return;
	}
	
	/**
	 * 自动还款签约
	 * @throws Exception
	 */
	public static void returnAutoInvestSignature() throws Exception{
		
		Map<String, String> paramMap = ipsPaymentCallBackService.getRespParams(params);
		
		IpsPaymentUtil.printData(paramMap, "自动投标签约回调参数", PayType.AUTO_INVEST_SIGNATURE);
		Map<String, String> parseXml = IpsPaymentUtil.parseXmlToJson(params.get("p3DesXmlPara"));
		ErrorInfo error = new ErrorInfo();
		error = IpsPaymentUtil.checkSign(paramMap, "自动投标签约", parseXml, PayType.AUTO_INVEST_SIGNATURE.name(), error);		
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}
		String pIpsAuthNo = parseXml.get("pIpsAuthNo");
		String orderNum = parseXml.get("pMemo1");
		Map<String, Object> dataMap = IpsPaymentUtil.queryMmmDataByOrderNum(orderNum);	
		long userId = Convert.strToLong(dataMap.get("userId").toString(), -1L);
		JPAUtil.executeUpdate(error, " update t_users u set u.ips_bid_auth_no = ? where u.id = ? ", pIpsAuthNo, userId);
		
		int validType = Convert.strToInt(dataMap.get("validType").toString(), -1);
		int validDate = Convert.strToInt(dataMap.get("validDate").toString(), -1);
		double minAmount = Convert.strToDouble(dataMap.get("minAmount").toString(), -1);
		double maxAmount = Convert.strToDouble(dataMap.get("maxAmount").toString(), -1);
		
		String bidAmount = dataMap.get("bidAmount").toString();
		String rateStart = dataMap.get("rateStart").toString();
		String rateEnd = dataMap.get("rateEnd").toString();
		String deadlineStart = dataMap.get("deadlineStart").toString();
		
		String deadlineEnd = dataMap.get("deadlineEnd").toString();
		String creditStart = dataMap.get("creditStart").toString();
		String creditEnd = dataMap.get("creditEnd").toString();
		String borrowWay = dataMap.get("borrowWay").toString();
		String remandAmount = dataMap.get("remandAmount").toString();
	
		int result = Invest.saveOrUpdateRobot(userId, validType, validDate, minAmount, maxAmount, bidAmount, 
				 rateStart, rateEnd, deadlineStart, deadlineEnd, creditStart, creditEnd, remandAmount, borrowWay, error);
		
		if(error.code < 0 ){
			
			error.code = -1;
			error.msg = "自动投标签约失败!";
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
		}
		
		error.code = 1;
		error.msg = "自动投标签约成功!";
		payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
		return;
	}
	
	/**
	 * 登记债权转让回调
	 * @throws JSONException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void returnDebtorTransfer() throws Exception{
		
		Map<String, String> paramMap = ipsPaymentCallBackService.getRespParams(params);
		
		IpsPaymentUtil.printData(paramMap, "登记债权转让回调参数", PayType.DEBTOR_TRANSFER);
		Map<String, String> parseXml = IpsPaymentUtil.parseXmlToJson(params.get("p3DesXmlPara"));
		ErrorInfo error = new ErrorInfo();
		error = IpsPaymentUtil.checkSign(paramMap, "登记债权转让", parseXml, PayType.DEBTOR_TRANSFER.name(), error);		
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}
		String orderNum = parseXml.get("pMemo1");
		Map<String, Object> dataMap = IpsPaymentUtil.queryMmmDataByOrderNum(orderNum);
		
		Map<String, String> pDetails = gson.fromJson(dataMap.get("pDetails").toString(), new TypeToken<Map<String, String>>(){}.getType());
		pDetails.put("pOriMerBillNo", parseXml.get("pMerBillNo"));
		
		String pBidNo = dataMap.get("pBidNo").toString();
		String parentOrderno = ipsPaymentService.createBillNo();
		
		LinkedList<Map<String, String>> pDetailsList = new LinkedList<Map<String, String>>();
		
		pDetailsList.add(pDetails);
		
		Long debtId = Convert.strToLong(dataMap.get("debtId").toString(), -1);
		String dealpwd = dataMap.get("dealPassword") == null ? "" : dataMap.get("dealPassword").toString();
		
		Map<String,Object> transmap = PaymentProxy.getInstance().debtorTransferConfirm(error, Constants.PC, pDetailsList, pBidNo, parentOrderno, debtId+"", dealpwd);
		
		if(error.code < 0 ){		
			error.code = -1;
			error.msg = "债权转让失败!";
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
		}
		String transMerbill ="";
		if(transmap!=null && transmap.get("pMerBillNo")!=null){
			transMerbill = transmap.get("pMerBillNo").toString();
		}
		
		//处理本地业务逻辑
		Debt.dealDebtTransfer(transMerbill, debtId, dealpwd, false, error);
		
		if(error.code < 0 ){		
			error.code = -1;
			error.msg = "债权转让失败!";
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
		}
		
		error.code = -1;
		error.msg = "债权转让成功!";
		payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
	}
	
	/**
	 * 债权转让成交回调
	 * @throws Exception
	 */
	public static void returnDebtorTransferConfirm() throws Exception{ 
		
		Map<String, String> paramMap = ipsPaymentCallBackService.getRespParams(params);
		
		IpsPaymentUtil.printData(paramMap, "债权转让成交回调参数", PayType.DEBTOR_TRANSFER_CONFIRM);
		Map<String, String> parseXml = IpsPaymentUtil.parseXmlToJson(params.get("p3DesXmlPara"));
		ErrorInfo error = new ErrorInfo();
		error = IpsPaymentUtil.checkSign(paramMap, "债权转让成交", parseXml, PayType.DEBTOR_TRANSFER_CONFIRM.name(), error);		
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}
		String orderNum = parseXml.get("pMemo1");
		Map<String, Object> dataMap = IpsPaymentUtil.queryMmmDataByOrderNum(orderNum);
		
		Long debtId = Convert.strToLong(dataMap.get("debtId").toString(), -1);
		String dealpwd = dataMap.get("dealPassword") == null ? "" : dataMap.get("dealPassword").toString();		
		//处理本地业务逻辑	
		Debt.dealDebtTransfer(parseXml.get("pMerBillNo"), debtId, dealpwd, false, error);		
		if(error.code < 0 ){		
			error.code = -1;
			error.msg = "债权转让失败!";
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
		}
		
		error.code = -1;
		error.msg = "债权转让成功!";
		payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
		
	}
	
	/**
	 * 满标审核不通过
	 * @throws Exception
	 */
	public static void returnBidAuditFail() throws Exception{ 
		
		Map<String, String> paramMap = ipsPaymentCallBackService.getRespParams(params);
		
		IpsPaymentUtil.printData(paramMap, "满标审核不通回调参数", PayType.BID_AUDIT_FAIL);
		Map<String, String> parseXml = IpsPaymentUtil.parseXmlToJson(params.get("p3DesXmlPara"));
		ErrorInfo error = new ErrorInfo();
		error = IpsPaymentUtil.checkSign(paramMap, "满标审核不通过", parseXml, PayType.BID_AUDIT_FAIL.name(), error);		
		
		// 处理本地业务逻辑
		String orderNum = parseXml.get("pMemo1");
		Map<String, Object> dataMap = IpsPaymentUtil.queryMmmDataByOrderNum(orderNum);	
	
		long bidId = Long.parseLong(dataMap.get("bidId").toString());
		String typeStr = dataMap.get("typeStr").toString();
		
		if(error.code < 0){   // 流标失败的情况，跳转到对应的页面
			if(typeStr.equals(IPSConstants.BID_CANCEL_B) || typeStr.equals(IPSConstants.BID_CANCEL_I) ) {    // 1、提前借款->借款中不通过      4、筹款中->借款中不通过
				flash.error(error.msg);
				BidPlatformAction.fundraiseingList();  
			} else if(typeStr.equals(IPSConstants.BID_CANCEL_F) || typeStr.equals(IPSConstants.BID_CANCEL_N)) {   // 3、提前借款->撤销    6、 筹款中->撤销
				flash.error(error.msg);
				AccountHome.loaningBids("", "", "", "");
			} else if(typeStr.equals(IPSConstants.BID_CANCEL_M)) {          // 7、满标->放款不通过
				flash.error(error.msg);
				BidPlatformAction.fullList();
			} else if(typeStr.equals(IPSConstants.BID_CANCEL_S)) {          // 8、审核中->审核不通过
				flash.error(error.msg);
				BidPlatformAction.auditingList();   
			} else if(typeStr.equals(IPSConstants.BID_CANCEL)) {          // 9、审核中->撤销
				flash.error(error.msg);
				AccountHome.auditingLoanBids("", "", "", "");
			} else {													// 所有自动流标
				return ;
			}
		}
		Bid bid = new Bid();
		bid.id = bidId;
		// 流标成功，判断是那种流标方式，走相应的业务逻辑
		// 判断是那种流标方式
		if (typeStr.equals(IPSConstants.BID_CANCEL_B)) { // 提前借款->借款中不通过
			bid.advanceLoanToPeviewNotThroughBC(error);  
		} else if(typeStr.equals(IPSConstants.BID_ADVANCE_LOAN)) {   // 提前借款-->流标
			bid.advanceLoanToFlowBC(error);  
		} else if(typeStr.equals(IPSConstants.BID_CANCEL_F)) {   // 提前借款->撤销
			bid.advanceLoanToRepealBC(error);  
		} else if(typeStr.equals(IPSConstants.BID_CANCEL_I)) {   // 筹款中->借款中不通过
			bid.fundraiseToPeviewNotThroughBC(error);  
		} else if(typeStr.equals(IPSConstants.BID_FUNDRAISE)) {   // 筹款中->流标
			bid.fundraiseToFlowBC(error);  
		} else if(typeStr.equals(IPSConstants.BID_CANCEL_N)) {   // 筹款中->撤销
			bid.fundraiseToRepealBC(error); 
		}  else if(typeStr.equals(IPSConstants.BID_CANCEL_M)) {   // 满标->放款不通过
			bid.fundraiseToLoanNotThroughBC(error); 
		} else if(typeStr.equals(IPSConstants.BID_CANCEL_S)) {   // 审核中->审核不通过
			bid.auditToNotThroughBC(error); 
		} else if(typeStr.equals(IPSConstants.BID_CANCEL)) {   // 审核中->撤销
			bid.auditToRepealBC(error); 
		}  
		
		return ;
	}
	
	/**
	 * 提现回调
	 * @throws Exception
	 */
	public static void returnWithdraw() throws Exception{ 
		
		Map<String, String> paramMap = ipsPaymentCallBackService.getRespParams(params);
		
		IpsPaymentUtil.printData(paramMap, "提现回调参数", PayType.WITHDRAW);
		Map<String, String> parseXml = IpsPaymentUtil.parseXmlToJson(params.get("p3DesXmlPara"));
		ErrorInfo error = new ErrorInfo();
		error = IpsPaymentUtil.checkSign(paramMap, "提现", parseXml, PayType.WITHDRAW.name(), error);		
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}
		String orderNum = parseXml.get("pMemo1");
		Map<String, Object> dataMap = IpsPaymentUtil.queryMmmDataByOrderNum(orderNum);	
		
		//处理本地业务逻辑	
		Long userId = Convert.strToLong(dataMap.get("userId").toString(), -1);
		Long withdrawalId = Convert.strToLong(dataMap.get("withdrawalId").toString(), -1);
		double serviceFee  = Convert.strToDouble(dataMap.get("pMerFee").toString(), 0.00);
		
		//调用提现方法
		User.withdrawalNotice(userId, serviceFee, withdrawalId, "1", true, true, error);
		
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}
		error.code = 1;
		error.msg = "提现成功!";
		payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
	}
	
	/**
	 * 登记担保人(逾期垫付)
	 * @throws JSONException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void returnAdvance() throws Exception{
		
		Map<String, String> paramMap = ipsPaymentCallBackService.getRespParams(params);
		
		IpsPaymentUtil.printData(paramMap, "登记担保人回调参数", PayType.ADVANCE);
		Map<String, String> parseXml = IpsPaymentUtil.parseXmlToJson(params.get("p3DesXmlPara"));
		ErrorInfo error = new ErrorInfo();
		error = IpsPaymentUtil.checkSign(paramMap, "登记担保人", parseXml, PayType.ADVANCE.name(), error);	
		
		String orderNum = parseXml.get("pMemo1");
		Map<String, Object> dataMap = IpsPaymentUtil.queryMmmDataByOrderNum(orderNum);		
		int type = Convert.strToInt(dataMap.get("type").toString(), -1);
		long bill_id = Convert.strToLong(dataMap.get("bill_id").toString(), -1);
		
		if(error.code < 0){
			
			//本金垫付业务
			if(CompensateType.COMPENSATE == type){
				flash.error(error.msg);		
				PayableBillManager.overdueUnpaidBills();
			}			
			//线下还款业务
			if(CompensateType.OFFLINE_REPAYMENT == type){	
				ReceivableBillManager.overdueBills();
			}
			flash.error(error.msg);
			PayableBillManager.overdueUnpaidBills();
			return;
		}	
		//初始化账单
		t_bills t_bill = t_bills.findById(bill_id);
		
		//初始化标的
		t_bids bid = t_bids.findById(t_bill.bid_id);
		bid.is_register_guarantor = true;
		bid.save();
		
		//本金垫付业务
		if(CompensateType.COMPENSATE == type){
			flash.error(error.msg);		
			PayableBillManager.overdueUnpaidBills();
		}
		
		//线下还款业务
		if(CompensateType.OFFLINE_REPAYMENT == type){	
			ReceivableBillManager.overdueBills();
		}		
	}
	
	 /**
	  * 逾期垫付回调
	 * @throws JSONException 
	 * @throws UnsupportedEncodingException 
	  */
	public static void returnAdvanceConfirm() throws Exception{
		
		Map<String, String> paramMap = ipsPaymentCallBackService.getRespParams(params);
		
		IpsPaymentUtil.printData(paramMap, "本金垫付回调参数", PayType.ADVANCE_CONFIRM);
		Map<String, String> parseXml = IpsPaymentUtil.parseXmlToJson(params.get("p3DesXmlPara"));
		ErrorInfo error = new ErrorInfo();
		error = IpsPaymentUtil.checkSign(paramMap, "本金垫付", parseXml, PayType.ADVANCE_CONFIRM.name(), error);	
		
		String orderNum = parseXml.get("pMemo1");
		Map<String, Object> dataMap = IpsPaymentUtil.queryMmmDataByOrderNum(orderNum);		
		int type = Convert.strToInt(dataMap.get("type").toString(), -1);
		long bill_id = Convert.strToLong(dataMap.get("bill_id").toString(), -1);
		long supervisorId = Convert.strToLong(dataMap.get("supervisorId").toString(), -1);//操作人员id
		
		//初始化账单
		t_bills t_bill = t_bills.findById(bill_id);
		
		if(error.code < 0){			
			//出现错误，跳出本地业务执行，提示到本金垫付页面
			flash.error(error.msg);				
			if(CompensateType.COMPENSATE == type){	
				
				PayableBillManager.overdueUnpaidBills(); //本金垫付
			}else{
				ReceivableBillManager.overdueBills(); //线下还款		
			}				
		}	
		//本金垫付业务
		if(CompensateType.COMPENSATE == type){
			
			Bill  bill = new Bill();
			bill.merBillNo = parseXml.get("pMerBillNo");
			bill.principalAdvancePayment(supervisorId, bill_id, error);		
			flash.error(error.msg);
			PayableBillManager.overdueUnpaidBills();
		}
		
		//线下还款
		if(CompensateType.OFFLINE_REPAYMENT == type){			
			Bill  bill = new Bill();
			bill.merBillNo = parseXml.get("pMerBillNo");
			bill.principalAdvancePayment(supervisorId, bill_id, error);
			flash.error(error.msg);		
			ReceivableBillManager.overdueBills();
		}
		
	}
	
	 /**
	  * 逾期垫付还款回调
	 * @throws JSONException 
	 * @throws UnsupportedEncodingException 
	  */
	public static void returnAdvanceRepayment() throws Exception{
		
		Map<String, String> paramMap = ipsPaymentCallBackService.getRespParams(params);
		
		IpsPaymentUtil.printData(paramMap, "垫付还款参数", PayType.ADVANCE_REPAYMENT);
		Map<String, String> parseXml = IpsPaymentUtil.parseXmlToJson(params.get("p3DesXmlPara"));
		ErrorInfo error = new ErrorInfo();
		IpsPaymentUtil.checkSign(paramMap, "垫付还款", parseXml, PayType.ADVANCE_REPAYMENT.name(), error);	
		
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}
		String orderNum = parseXml.get("pMemo1");
		Map<String, Object> dataMap = IpsPaymentUtil.queryMmmDataByOrderNum(orderNum);		

		long bill_id = Convert.strToLong(dataMap.get("bill_id").toString(), -1);
		
		Bill bill = new Bill();
		bill.id = bill_id;
		
		//垫付还款逻辑
		bill.repayment(bill.user.id, error);
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}
		error.code = 1;
		error.msg = "还款成功!";
		payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
		return;
		
	}
	
	
	
}
