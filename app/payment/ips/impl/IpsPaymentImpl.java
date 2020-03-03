package payment.ips.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import models.t_bids;
import models.t_bills;
import models.t_invests;
import models.t_users;

import org.apache.commons.lang.StringUtils;
import org.json.XML;

import payment.PaymentInterface;
import payment.PaymentBaseService;
import payment.ips.service.IpsPaymentService;
import payment.ips.util.IpsConstants;
import payment.ips.util.IpsPaymentUtil;
import play.Logger;
import play.cache.Cache;
import utils.ErrorInfo;
import utils.JPAUtil;
import business.Bid;
import business.Bill;
import business.Debt;
import business.OverBorrow;
import business.User;
import business.Vip;

import com.google.gson.Gson;
import com.shove.Convert;

import constants.Constants;
import constants.Constants.RechargeType;
import constants.IPSConstants;
import constants.IPSConstants.CompensateType;
import constants.IPSConstants.IpsCheckStatus;
import constants.PayType;
import controllers.front.account.AccountHome;
import controllers.payment.ips.IpsPaymentReqAction;
import controllers.supervisor.bidManager.BidPlatformAction;
import controllers.supervisor.financeManager.PayableBillManager;
import controllers.supervisor.financeManager.ReceivableBillManager;

/**
 * 环讯资金托管接口封装实现类
 * @author xiaoqi
 *
 */
public class IpsPaymentImpl implements PaymentInterface{
	
	private IpsPaymentService ipsPaymentService = new IpsPaymentService();

	public static payment.ips.util.XmlUtil xmlUtil = new payment.ips.util.XmlUtil();
	public static Gson gson = new Gson();
	
	@Override
	public Map<String, Object> register(ErrorInfo error, int client, Object... obj) {
		
		User user = (User) obj[0];
		
		//开户前进行认证
		int status = user.getIpsStatus();
		if(IpsCheckStatus.MOBILE != status){
			IpsPaymentReqAction.approve();
		}

		//参数组装
		LinkedHashMap<String, String> paramMap = ipsPaymentService.register(error, user.idNumber, user.realityName, user.mobile, user.email);
		
		String xml = IpsPaymentUtil.parseMapToXml(paramMap);
		String html = IpsPaymentUtil.createHtml(xml, "CreateNewIpsAcct.aspx", IpsConstants.terraceNoOne, "argMerCode", "arg3DesXmlPara", "argSign");
		
		//提交入库参数, 用于回调
		paramMap.put("userId", String.valueOf(user.getId())); //回调需要参数:用户id
		ipsPaymentService.printRequestData(paramMap, "开户提交参数", PayType.REGISTER);
		
		IpsPaymentReqAction.getInstance().submitForm(html,client);
		
		return null;	
	}
	
	@Override
	public Map<String, Object> advance(ErrorInfo error, int client,Object... obj) {
		
		//初始化标的信息
		Long bidId = (Long) obj[0];		
		t_bids bid = t_bids.findById(bidId);
		
		//账单id
		Long bill_id = (Long) obj[1];
		t_bills t_bill = t_bills.findById(bill_id);

		//类型
		Integer type = (Integer) obj[2];
		
		//操作员id
		Long supervisorId = (Long) obj[3];
		
		//回调方法需求的参数
		LinkedHashMap<String, String> dataMap = new LinkedHashMap<String, String>();
		
		dataMap.put("type", type+""); //垫付类型，线下还款/本金垫付
		dataMap.put("supervisorId", supervisorId+"");//操作人员id
		dataMap.put("bill_id", bill_id+"");//账单id
		
		//该标的，进行过担保登记，则无需调用担保登记接口，直接垫付即可
		if(!Bid.queryIsRegisterGuarantor(bid.id)){
			
			LinkedHashMap<String, String> paramMap = ipsPaymentService.advance(bid.bid_no, bid.amount * 1.5);
			
			String xml = IpsPaymentUtil.parseMapToXml(paramMap);
			String html = IpsPaymentUtil.createHtml(xml, "registerGuarantor.aspx", IpsConstants.terraceNoOne, "pMerCode", "p3DesXmlPara", "pSign");
			
			paramMap.putAll(dataMap);
			
			ipsPaymentService.printRequestData(paramMap, "本金垫付(登记担保方)提交参数", PayType.ADVANCE);
			
			IpsPaymentReqAction.getInstance().submitForm(html,client);
		}
		
		//垫付接口调用
		String pMerBillNo = "D" + t_bill.mer_bill_no; //D打头为垫付，正常还款无前缀, P打头为垫付还款	
		
		//垫付查询参数
		List<Map<String, String>> pDetailsList = ipsPaymentService.queryAdvance(bill_id, error);
					
		ipsPaymentService.advanceConfirm(pDetailsList, bid.bid_no, pMerBillNo, dataMap, error);		
		
		if(error.code < 0){
			
			return null;
		}
		
		//本金垫付业务
		if(CompensateType.COMPENSATE == type){
			
			Bill  bill = new Bill();
			bill.merBillNo = pMerBillNo;
			bill.principalAdvancePayment(supervisorId, bill_id, error);

			if(error.code < 0){
				
				return null;
			}
			
			error.code = 1;
			error.msg = "本金垫付成功!";
			
			return null;

		}
		
		//线下收款
		if(CompensateType.OFFLINE_REPAYMENT == type){			
			Bill  bill = new Bill();
			bill.merBillNo = pMerBillNo;
			
			// TODO 此处应该调用线下收款逻辑
			bill.principalAdvancePayment(supervisorId, bill_id, error);			

			if(error.code < 0){
				
				return null;
			}
			
			error.code = 1;
			error.msg = "线下收款成功!";
			
			return null;
		}
		
		return null;
	}
	
	@Override
	public Map<String, Object> offlineRepayment(ErrorInfo error, int client, Object... obj) {
		return null;
	}
	
	
	@Override
	public Map<String, Object> advanceRepayment(ErrorInfo error, int client, Object... obj) {
		
		//初始化账单
		Bill bill = (Bill) obj[0];
		
		//初始化借款人id
		Long userId = (Long) obj[1];	
		t_users user = t_users.findById(userId);
		//初始化标的信息	
		t_bids bid = t_bids.findById(bill.bid.id);
		
		//垫付查询参数
		List<Map<String, String>> pDetailsList = ipsPaymentService.queryAdvanceRepayment(bill.id, userId, error);
		
		String pMerBillNo = "P" + bill.merBillNo; //D打头为垫付，正常还款无前缀, P打头为垫付还款	
		
		//回调方法需求的参数
		LinkedHashMap<String, String> dataMap = new LinkedHashMap<String, String>();
		dataMap.put("bill_id", bill.id+"");//账单id
		dataMap.put("userId", userId+"");//借款人id
		dataMap.put("pIpsAcctNo", user.ips_acct_no); //借款人ips账号

		ipsPaymentService.AdvanceRepayment(pDetailsList, bid.bid_no, pMerBillNo, dataMap, error);

		if(error.code < 0){		
			
			return null;
		}
		
		//垫付还款逻辑
		bill.repayment(userId, error);	
		if(error.code < 0){	
			
			return null;
		}
		
		error.code = 1;
		error.msg = "垫付还款成功!";
		
		return null;
	}

	@Override
	public Map<String, Object> applyCredit(ErrorInfo error, int client, Object... obj) {
		
		Map<String,Object> map = (Map<String, Object>) obj[0];
		
		//组装回调需要的参数
		long userId = Convert.strToLong(map.get("userId").toString(), -1);
		double fees = 0.00;
		int amount = Convert.strToInt(map.get("amount").toString(), 0);
		String reason = map.get("reason").toString();
		List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
		
		//调用申请信用额度的业务逻辑处理方法
		OverBorrow.returnApplyFor(userId, fees, amount, reason, auditItems, error);
		if(!(error.code < 0)){
			error.code = 1;
			error.msg = "申请信用额度成功！";
		}		
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("error", error);
		return returnMap;
	}

	@Override
	public Map<String, Object> applyVIP(ErrorInfo error, int client, Object... obj) {
		
		Map<String, Object> map = (Map<String, Object>) obj[0];
		Map<String, Object> returnMap = new HashMap<String, Object>();
		User user = new User();
		
		user.id = Convert.strToLong(map.get("userId").toString(), -1);
		int serviceTime = Convert.strToInt(map.get("serviceTime").toString(), 0);
		double fee = 0.00;		
		error = Vip.returnRenewal(user.id, serviceTime, client, fee);		
		returnMap.put("error", error);
		return returnMap;
	}

	

	@Override
	public Map<String, Object> bidAuditFail(ErrorInfo error, int client, Object... obj) {
		
		Bid bid = new Bid();
		bid.id = (Long) obj[0];
		String typeStr = (String) obj[1];
		Map<String, String> dataMap = IpsPaymentUtil.parseStringMapToObjectMap(IpsPaymentUtil.queryMmmDataByOrderNum(bid.bidNo));
		dataMap.put("bidId", bid.id+"");
		dataMap.put("typeStr", typeStr);
		
		ipsPaymentService.bidAuditFail(dataMap, error);
		
		if(error.code < 0){
			
			return null;
		}
		
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
		
		return null;
	}

	@Override
	public Map<String, Object> bidAuditSucc(ErrorInfo error, int client, Object... obj) {
		
		//初始化参数
		Bid bid = (Bid) obj[0];
		//初始化借款人
		User borrower = new User();
		borrower.id = bid.userId;	
		String parentOrderno = ipsPaymentService.createBillNo();//父流水号，管控满标审核与满标解冻借款人保证金原子性
		
		//调用解冻保证金接口
		if(bid.bail > 0 && bid.ipsStatus != 3){				
			try {
				ipsPaymentService.unFreeze(bid.bidNo, bid.bail, "1", borrower.idNumber, borrower.realityName, borrower.ipsAcctNo, parentOrderno, bid.id, error);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(error.code < 0){
				
				return null;
			}
			
			//将标的修改成，保证金已解冻状态
			JPAUtil.executeUpdate(error, " update t_bids d set d.ips_status = ? where d.id = ? ", 3, bid.id);
			
			if(error.code < 0){
				
				return null;
			}
		}
		
		List<t_invests> investList = t_invests.find(" bid_id = ? ", bid.id).fetch();
		List<Map<String, String>> pDetails = new LinkedList< Map<String, String>>();
		for(int i = 0; i < investList.size(); i++){			
			t_invests invest = investList.get(i);
			//初始化投资人
			User user = new User();
			user.id = invest.user_id;
			
			//组装放款列表
			Map<String, String> pDetailsMap = new HashMap<String, String>();
			pDetailsMap.put("pOriMerBillNo", invest.mer_bill_no);
			pDetailsMap.put("pTrdAmt", invest.amount+"");
			pDetailsMap.put("pFAcctType", "1");
			pDetailsMap.put("pFIpsAcctNo", user.ipsAcctNo);
			pDetailsMap.put("pFTrdFee", "0.00");
			pDetailsMap.put("pTAcctType", "1");
			pDetailsMap.put("pTIpsAcctNo", borrower.ipsAcctNo);
			pDetailsMap.put("pTTrdFee", String.format("%.2f", invest.bid_fee));
			pDetails.add(pDetailsMap);
		}	
		
		try {
			//调用放款接口
			ipsPaymentService.bidAuditSucc(pDetails, bid.bidNo, borrower.ipsAcctNo, parentOrderno, bid.id,  error);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(error.code < 0 ){
			
			return null;
		}
		
		//处理回调逻辑
		Map<String, Double> map = Bid.queryBidAwardAndBidFee(bid, error);
		bid.serviceFees = map.get("bid_fee");
		
		bid.eaitLoanToRepayment(error);
		return null;
	}

	@Override
	public Map<String, Object> bidDataAudit(ErrorInfo error, int client, Object... obj) {
		return null;
	}

	@Override
	public Map<String, Object> debtorTransfer(ErrorInfo error, int client, Object... obj) {
		
		Long debtId = (Long) obj[0];
		String dealPassword = (String) obj[1];
		Map<String, Object> map = Debt.queryTransferInfo(debtId, error);
		
		String pBidNo  = map.get("bidNo").toString(); //标的号
		
		//初始化转让人
		Long fromUserId = (Long) map.get("fromUserId");
		User invester = new User();
		invester.id = fromUserId;
		
		//初始化受让人
		Long toUserId = (Long) map.get("toUserId");
		User transfer = new User();
		transfer.id = toUserId;		
		String pCreMerBillNo = map.get("pCreMerBillNo").toString(); //登记债权人时提交的订单号
		
		double pCretAmt = Convert.strToDouble(map.get("pCretAmt").toString(), 0.00);//债权面额
		
		double pPayAmt = Convert.strToDouble(map.get("pPayAmt").toString(), 0.00);//支付金额
		
		double pFromFee = Convert.strToDouble(map.get("managefee").toString(), 0.00) ;//转让方手续费
		
		LinkedHashMap<String, String> xmlMap = ipsPaymentService.registerCretansfer(pBidNo, invester.realityName, invester.ipsAcctNo, invester.idNumber, transfer.realityName, transfer.ipsAcctNo, transfer.idNumber, pCreMerBillNo, pCretAmt, pPayAmt, pFromFee);
	
		String xml = IpsPaymentUtil.parseMapToXml(xmlMap);		
		String html =IpsPaymentUtil.createHtml(xml, "registerCretansfer.aspx", IpsConstants.terraceNoOne, "pMerCode", "p3DesXmlPara", "pSign");

		xmlMap.put("userId", transfer.id+"");
		xmlMap.put("pMerBillNo", xmlMap.get("pMerBillNo")); //流水号
		xmlMap.put("pIpsAcctNo", transfer.ipsAcctNo); //受让人ips账号
		xmlMap.put("dealPassword", dealPassword); //交易密码
		xmlMap.put("debtId", debtId+""); //债权转让id
		
		//组装债权转让成交参数, 供登记债权转让回调用
		Map<String, String> pDetailsMap = new HashMap<String, String>();
		pDetailsMap.put("pTrdAmt", pPayAmt+"");
		pDetailsMap.put("pFAcctType", "1");
		pDetailsMap.put("pFIpsAcctNo", transfer.ipsAcctNo);
		pDetailsMap.put("pTTrdFee", String.format("%.2f", pFromFee));
		pDetailsMap.put("pTAcctType", "1");
		pDetailsMap.put("pTIpsAcctNo", invester.ipsAcctNo);
		pDetailsMap.put("pFTrdFee", "0.00");
		
		xmlMap.put("pDetails", gson.toJson(pDetailsMap));
		
		ipsPaymentService.printRequestData(xmlMap, "债权转让提交参数", PayType.DEBTOR_TRANSFER);	
		
		IpsPaymentReqAction.getInstance().submitForm(html,client);
		return null;
	}
	
	@Override
	public Map<String, Object> debtorTransferConfirm(ErrorInfo error, int client, LinkedList< Map<String, String>> pDetails, String pBidNo, String parentOrderno, String debtId, String dealpwd){
		
		Map<String, String> dataMap = null;
		try {
			 dataMap = ipsPaymentService.auctingDebtConfirm( pDetails, pBidNo, parentOrderno, debtId,  dealpwd, error);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return IpsPaymentUtil.parseObjectMapToStringMap(dataMap);
	}

	@Override
	public Map<String, Object> invest(ErrorInfo error, int client, Object... obj) {
		
		//查询获取，接口所需要的参数
		t_bids bid = (t_bids) obj[0]; //投资的标的
		
		User user = (User) obj[1]; //投资人
		
		double pTrdAmt = Convert.strToDouble(obj[2].toString(), 0.00); //投资金额
		
		LinkedHashMap<String, String> xmlMap = ipsPaymentService.normalInvest(bid.bid_no, pTrdAmt, user.idNumber, user.realityName, user.ipsAcctNo);	
		
		String xml = IpsPaymentUtil.parseMapToXml(xmlMap);		
		String html =IpsPaymentUtil.createHtml(xml, "registerCreditor.aspx", IpsConstants.terraceNoOne, "pMerCode", "p3DesXmlPara", "pSign");
	
		xmlMap.put("bidId", bid.id+"");
		xmlMap.put("investAmount", pTrdAmt+"");
		xmlMap.put("userId", user.id+"");
		xmlMap.put("userId", user.id+"");
		xmlMap.put("pMerBillNo", xmlMap.get("pMerBillNo"));
		xmlMap.put("pIpsAcctNo", user.ipsAcctNo);
		
		ipsPaymentService.printRequestData(xmlMap, "普通投资提交参数", PayType.INVEST);		
		
		IpsPaymentReqAction.getInstance().submitForm(html,client);	
		return null;
	}

	@Override
	public Map<String, Object> autoInvest(ErrorInfo error, int client, Object... obj) {
		
		//查询获取，接口所需要的参数
		t_bids bid = (t_bids) obj[0]; //投资的标的		
		User user = new User();
		user.setId(Long.parseLong(obj[1].toString())) ; //投资人		
		double pTrdAmt = Convert.strToDouble(obj[2].toString(), 0.00); //投资金额
		t_users u =  t_users.findById(user.id);
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		xmlMap.put("bidId", bid.id+"");
		xmlMap.put("investAmount", pTrdAmt+"");
		xmlMap.put("userId", user.id+"");
		xmlMap.put("userId", user.id+"");
		xmlMap.put("pIpsAcctNo", user.ipsAcctNo);
		
		try {
			ipsPaymentService.autoInvest(bid.bid_no, pTrdAmt, user.idNumber, user.realityName, user.ipsAcctNo, u.ips_bid_auth_no, xmlMap, error);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public Map<String, Object> queryAmount(ErrorInfo error, int client, Object... obj) {
		
		User user = (User) obj[0];
		LinkedHashMap<String, String> dataMap = null;			
		try {
			LinkedHashMap<String, String> xmlMap = ipsPaymentService.queryAmount(user.ipsAcctNo);	
			IpsPaymentUtil.printData(xmlMap, "余额查询提交参数", PayType.QUERY_AMOUNT);
			
			String xml = IpsPaymentUtil.sendWebService(IpsConstants.soap_url+ IpsConstants.QueryForAccBalance, IpsConstants.QueryForAccBalance, xmlMap, "argMerCode", "argIpsAccount", "argSign");
			xmlUtil.SetDocument(xml);
			xml = xmlUtil.getNodeValue("QueryForAccBalanceResult");
			dataMap = IpsPaymentUtil.parseNoEncryptXmlToJson(xml);
			IpsPaymentUtil.printData(dataMap, "余额查询回调参数", PayType.QUERY_AMOUNT);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("余额查询时，%s", e.getMessage());
			
			error.code = -1;
			error.msg = "余额查询异常";
			
			return null;
		}
		
		Map<String, Object> map = IpsPaymentUtil.parseObjectMapToStringMap(dataMap);
		map.put("pBlance", map.get("pBalance").toString());
		map.put("pFreeze", map.get("pLock").toString());
		
		return map;
	}

	@Override
	public List<Map<String, Object>> queryBanks(ErrorInfo error, int client, Object... obj) {
		
		List<Map<String, Object>> banks = (List<Map<String, Object>>) Cache.get("banks");
		if(banks != null){			
			return banks;
		}
		
		try {
			banks = ipsPaymentService.queeryBankList();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("获取银行列表时，%s", e.getMessage());
			
			error.code = -1;
			error.msg = "获取银行列表异常";
			
			return null;
		}
		
		Cache.set("banks", banks);
		return banks;
	}

	@Override
	public Map<String, Object> recharge(ErrorInfo error, int client, Object... obj) {
		
		User user = User.currUser();
		//银行卡编码
		String pTrdBnkCode = obj[1].toString();
		//充值金额
		String pTrdAmt = obj[0].toString();
		//生成流水号
		String payNumber = ipsPaymentService.createBillNo();
		//生成充值记录
		User.sequence(RechargeType.Normal, payNumber, new Double(pTrdAmt), Constants.GATEWAY_RECHARGE, Constants.CLIENT_PC, error);
		
		//组装环讯托管，充值所需要的参数
		LinkedHashMap<String, String> paramMap = ipsPaymentService.recharge(error, user.idNumber, user.realityName, user.ipsAcctNo, pTrdAmt, pTrdBnkCode);	
		
		String xml = IpsPaymentUtil.parseMapToXml(paramMap);
		String html = IpsPaymentUtil.createHtml(xml, "doDpTrade.aspx", IpsConstants.terraceNoOne, "pMerCode", "p3DesXmlPara", "pSign");
		
		//提交入库参数, 用于回调
		paramMap.put("userId", String.valueOf(user.getId())); //回调需要参数:用户id
		paramMap.put("amount", pTrdAmt); //回调需要参数:用户id
		paramMap.put("amount", pTrdAmt);
		paramMap.put("payNumber", payNumber);//支付流水号
		paramMap.put("pIpsAcctNo", user.ipsAcctNo);
		
		ipsPaymentService.printRequestData(paramMap, "充值提交参数", PayType.RECHARGE);
		
		IpsPaymentReqAction.getInstance().submitForm(html,client);
		return null;
	}

	@Override
	public Map<String, Object> repayment(ErrorInfo error, int client, Object... obj) {
		
		//初始化账单
		Bill bill = (Bill) obj[0];		
		//标的初始化
		Bid bid = new Bid();
		bid.id = bill.bidId;
		
		//初始化借款人
		User borrower = new User();
		borrower.id = bid.userId;		
		LinkedHashMap<String, String> paramMap = ipsPaymentService.queryRepaymentData(bill, false);	
		
		String xml = IpsPaymentUtil.parseMapToXml(paramMap);
		String html = IpsPaymentUtil.createHtml(xml, "RepaymentNewTrade.aspx", IpsConstants.terraceNoOne, "pMerCode", "p3DesXmlPara","pSign");

		paramMap.put("billId", bill.id+"");
		paramMap.put("borrowerId", borrower.id+"");
		paramMap.put("pIpsAcctNo", borrower.ipsAcctNo);
		
		ipsPaymentService.printRequestData(paramMap, "还款提交参数", PayType.REPAYMENT);
		
		IpsPaymentReqAction.getInstance().submitForm(html,client);
		return null;
	}
	
	@Override
	public Map<String, Object> autoRepayment(ErrorInfo error, int client, Object... obj) {
		
		//初始化账单
		Bill bill = (Bill) obj[0];		
		//标的初始化
		Bid bid = new Bid();
		bid.id = bill.bidId;		
		//初始化借款人
		User borrower = new User();
		borrower.id = bid.userId;	
		
		LinkedHashMap<String, String> paramMap = ipsPaymentService.queryRepaymentData(bill, true);		
		
		String xml = IpsPaymentUtil.parseMapToXml(paramMap);
		
		paramMap.put("billId", bill.id+"");
		paramMap.put("borrowerId", borrower.id+"");
		paramMap.put("pIpsAcctNo", borrower.ipsAcctNo);
		
		ipsPaymentService.printRequestData(paramMap, "还款提交参数", PayType.REPAYMENT);
		
		String str3DesXmlPana = xml;
		str3DesXmlPana = com.ips.security.utility.IpsCrypto.triDesEncrypt(xml, IpsConstants.des_key, IpsConstants.des_iv);
		str3DesXmlPana = str3DesXmlPana.replaceAll("\r", "");
		str3DesXmlPana = str3DesXmlPana.replaceAll("\n", "");
		String strSign = com.ips.security.utility.IpsCrypto.md5Sign(IpsConstants.terraceNoOne + str3DesXmlPana + IpsConstants.cert_md5);		
		HashMap<String, String> dataMap = new HashMap<String, String>(); 
		dataMap.put("pMerCode", IpsConstants.terraceNoOne);
		dataMap.put("p3DesXmlPara", str3DesXmlPana);
		dataMap.put("pSign", strSign);		
		String data = IpsPaymentUtil.postMethod(IpsConstants.POST_URL + "RepaymentNewTrade.aspx", dataMap, "UTF-8");
		
		data = data.split("</form>")[0] + "</form>";
		org.json.JSONObject jsonObj;
		try {
			jsonObj = XML.toJSONObject(data);
			org.json.JSONObject form = (org.json.JSONObject) jsonObj.get("form");
			org.json.JSONArray inputs = (org.json.JSONArray) form.get("input");		
			LinkedHashMap<String, String> returnMap = new LinkedHashMap<String, String>();
			for(int i=0; i < inputs.length(); i++){
				org.json.JSONObject json = (org.json.JSONObject) inputs.get(i);
				String name = json.getString("name");
				String value = json.getString("value");
				returnMap.put(name, value);
			}
			
			Map<String, String> p3DesXmlPara = null;
			
			if (dataMap.get("p3DesXmlPara") != null && !"".equals(dataMap.get("p3DesXmlPara"))) {
				p3DesXmlPara = IpsPaymentUtil.parseXmlToJson(dataMap.get("p3DesXmlPara"));
			}
			IpsPaymentUtil.printData(returnMap, "自动还款回调参数", PayType.REPAYMENT);
			
			IpsPaymentUtil.checkSign(returnMap, "自动还款", p3DesXmlPara, PayType.REPAYMENT.name(), error);	
			
			if(error.code < 0){
				return null;
			}
			bill.repayment(borrower.id, error);			
			if(error.code < 0){
				return null;
			}	 
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return null;
	}

	@Override
	public Map<String, Object> withdraw(ErrorInfo error, int client, Object... obj) {
		
		
		User user = User.currUser();
		String withdrawalId = String.valueOf(obj[0]);
		double pTrdAmt = (Double)obj[1];
		double pMerFee = User.withdrawalFee(pTrdAmt);
		
		LinkedHashMap<String, String> paramMap = ipsPaymentService.withdraw(user.idNumber, user.realityName, user.ipsAcctNo, pTrdAmt, pMerFee);
				
		String xml = IpsPaymentUtil.parseMapToXml(paramMap);
		
		String html = IpsPaymentUtil.createHtml(xml, "doDwTrade.aspx", IpsConstants.terraceNoOne, "pMerCode", "p3DesXmlPara", "pSign");
	
		paramMap.put("withdrawalId", withdrawalId);
		paramMap.put("pMerFee", pMerFee+"");
		paramMap.put("userId", user.id+"");
		
		ipsPaymentService.printRequestData(paramMap, "提现提交参数", PayType.WITHDRAW);
		IpsPaymentReqAction.getInstance().submitForm(html,client);
		return null;
	}

	@Override
	public Map<String, Object> bidCreate(ErrorInfo error, int client, Object... obj) {
		
		//标的信息
		t_bids tbid = (t_bids) obj[0];
		//pc端/app/微信/手机网站	
		//标的业务类
		Bid bid = (Bid) obj[1];
		User user = new User();
		user.id = tbid.user_id;
		LinkedHashMap<String, String> paramMap = ipsPaymentService.bidCreate(tbid.amount, tbid.bail, 1, tbid.service_fees, user.idNumber, user.realityName, user.getIpsAcctNo());
		
		String xml = IpsPaymentUtil.parseMapToXml(paramMap);
		
		String html = "";
		try {
			html = IpsPaymentUtil.createHtml(xml, "registerSubject.aspx", IpsConstants.terraceNoOne, "pMerCode", "p3DesXmlPara", "pSign");
		} catch (Exception e) {
			e.printStackTrace();
		}		
		paramMap.put("tbid", gson.toJson(tbid));
		paramMap.put("client", client+"");
		paramMap.put("pIpsAcctNo", user.ipsAcctNo);
		ipsPaymentService.printRequestData(paramMap, "标的发布提交参数", PayType.BIDCREATE);
		IpsPaymentReqAction.getInstance().submitForm(html,client);
		return null;
	}

	@Override
	public Map<String, Object> userBindCard(ErrorInfo error, int client, Object... obj) {
		
		//环讯托管，无需此接口，故空实现;
		return null;
	}

	@Override
	public Map<String, Object> autoRepaymentSignature(ErrorInfo error, int client, Object... obj) {
		
		User user = User.currUser();
		LinkedHashMap<String, String> paramMap = ipsPaymentService.autoRepaymentSignature(user.idNumber, user.realityName, user.ipsAcctNo);
		
		String xml = IpsPaymentUtil.parseMapToXml(paramMap);
		String html = IpsPaymentUtil.createHtml(xml, "RepaymentSigning.aspx", IpsConstants.terraceNoOne, "pMerCode", "p3DesXmlPara", "pSign");

		paramMap.put("userId", user.id+"");
		paramMap.put("pIpsAcctNo", user.ipsAcctNo);
		ipsPaymentService.printRequestData(paramMap, "自动还款签约提交参数", PayType.AUTO_REPAYMENT_SIGNATURE);		
		
		IpsPaymentReqAction.getInstance().submitForm(html,client);
		return null;
	}

	@Override
	public Map<String, Object> autoInvestSignature(ErrorInfo error, int client, Object... obj) {
		
		Map<String, String> map = (Map<String, String>) obj[0];
		User user = User.currUser();
		String authNo = User.queryIpsBidAuthNo(user.id, error); 
		if(StringUtils.isBlank(authNo)) {			
			
			LinkedHashMap<String, String> paramMap = ipsPaymentService.autoInvestSignature(user.idNumber, user.realityName, user.ipsAcctNo);
			
			String xml = IpsPaymentUtil.parseMapToXml(paramMap);
			String html = IpsPaymentUtil.createHtml(xml, "AutoNewSigning.aspx", IpsConstants.terraceNoOne, "pMerCode", "p3DesXmlPara", "pSign");

			paramMap.put("userId", user.id+"");
			paramMap.put("pIpsAcctNo", user.ipsAcctNo);
			paramMap.putAll(map);
			
			ipsPaymentService.printRequestData(paramMap, "自动投标签约提交参数", PayType.AUTO_INVEST_SIGNATURE);
			IpsPaymentReqAction.getInstance().submitForm(html,client);
		}	
		return null;
	}

	@Override
	public Map<String, Object> grantCps(ErrorInfo error, int client, Object... obj) {
		
		 Long userCpsIncomeId = (Long) obj[1];
		 //修改cps记录为处理中状态，防止项目运行久了之后，每次查询cps发放记录过多
		 User.updateIncomeStatus(userCpsIncomeId, 2, error);
		 
		//环讯不支持cps故空实现类
		return null;
	}
	
	@Override
	public Map<String, Object> grantInvitation(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Map<String, Object> agentRecharge(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> merchantRecharge(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> merWithdrawal(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> queryAmountByMerchant(ErrorInfo error,
			int client, Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> loginAccount(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Map<String, Object> queryLog(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> queryBindedBankCard(ErrorInfo error, int client,
			Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}

}
