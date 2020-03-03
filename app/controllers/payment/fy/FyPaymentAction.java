package controllers.payment.fy;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import models.t_bids;
import models.t_bills;
import models.t_invests;
import models.t_pay_pro_city;
import models.t_user_bank_accounts;
import models.t_users;
import payment.PaymentInterface;
import payment.fy.impl.FyPaymentImpl;
import payment.fy.util.FyConstants;
import payment.fy.util.FyPaymentUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import business.Bid;
import business.Bill;
import business.Debt;
import business.Invest;
import business.User;

import com.shove.Convert;

import constants.Constants;
import constants.Constants.RechargeType;
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
 * 富友资金托管接口封装实现类
 * @author xiaoqi
 *
 */
public class FyPaymentAction extends PaymentBaseAction implements PaymentInterface{

	@Override
	public Map<String, Object> advance(ErrorInfo error, int client, Object... obj) {
		
		
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
		
		//垫付接口调用
		String pMerBillNo = "D" + t_bill.mer_bill_no; //D打头为垫付，正常还款无前缀, P打头为垫付还款	
		
		//垫付查询参数
		List<Map<String, String>> pDetailsList = FyPaymentImpl.queryAdvance(bill_id, error);					
		for(int i=0; i < pDetailsList.size(); i++){
			
			Map<String, String> map = pDetailsList.get(i);
			String out_cust_no = map.get("out_cust_no");
			String in_cust_no = map.get("in_cust_no");
			double amount = Convert.strToDouble(map.get("amt"), 0);
			String orderno  = map.get("mchnt_txn_ssn");
			String rem = map.get("rem");
			
			try {
				//垫付接口调用
				FyPaymentImpl.transferBmu(error, out_cust_no, in_cust_no, amount, orderno, rem, PayType.ADVANCE.name());
				if(error.code < 0){			
					//出现错误，跳出本地业务执行，提示到本金垫付页面
					flash.error(error.msg);				
					if(CompensateType.COMPENSATE == type){	
						
						PayableBillManager.overdueUnpaidBills(); //本金垫付
					}else{
						ReceivableBillManager.overdueBills(); //线下还款		
					}				
				}
			} catch (Exception e) {
				
				flash.error(error.msg);				
				if(CompensateType.COMPENSATE == type){	
					
					PayableBillManager.overdueUnpaidBills(); //本金垫付
				}else{
					ReceivableBillManager.overdueBills(); //线下还款		
				}
				e.printStackTrace();
			}
		}				
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
			bill.merBillNo = pMerBillNo;
			bill.principalAdvancePayment(supervisorId, bill_id, error);
			flash.error(error.msg);
			PayableBillManager.overdueUnpaidBills();
		}
		
		//线下还款
		if(CompensateType.OFFLINE_REPAYMENT == type){			
			Bill  bill = new Bill();
			bill.merBillNo = pMerBillNo;
			bill.principalAdvancePayment(supervisorId, bill_id, error);			
			flash.error(error.msg);		
			ReceivableBillManager.overdueBills();
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
		//垫付查询参数
		Map<String, String> map = FyPaymentImpl.queryAdvanceRepayment(bill.id, userId, error);
		
		try {
			String out_cust_no = map.get("out_cust_no");
			String in_cust_no = map.get("in_cust_no");
			double amount = Convert.strToDouble(map.get("amt"), 0.00);
			String orderno = map.get("mchnt_txn_ssn");
			String rem = map.get("rem");	
			
			//垫付还款接口调用
			FyPaymentImpl.transferBmu(error, out_cust_no, in_cust_no, amount, orderno, rem, PayType.ADVANCE_REPAYMENT.name());
			if(error.code < 0){		
				
				payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if(error.code < 0){		
				
				payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
				return null;
			}
			return null;
		}
		//垫付还款逻辑
		bill.repayment(userId, error);	
		if(error.code < 0){					
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return null;
		}
		error.code = 1;
		error.msg = "还款成功!";
		payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
		return null;
	}

	@Override
	public Map<String, Object> applyCredit(ErrorInfo error, int client, Object... obj) {
		
		//暂无此功能
		return null;
	}

	@Override
	public Map<String, Object> applyVIP(ErrorInfo error, int client, Object... obj) {
		//暂无此功能
		return null;
	}

	@Override
	public Map<String, Object> autoInvest(ErrorInfo error, int client, Object... obj) {
		
		//查询获取，接口所需要的参数
		t_bids bid = (t_bids) obj[0]; //投资的标的
		
		User user = (User) obj[1]; //投资人
		
		User borrower = new User();
		borrower.id = bid.user_id;
		
		double pTrdAmt = Convert.strToDouble(obj[2].toString(), 0.00); //投资金额
		String orderno = FyPaymentUtil.createBillNo();
		Map<String,String> xmlMap = null;
		try {
			xmlMap = FyPaymentImpl.preAuth(error, user.ipsAcctNo, borrower.ipsAcctNo, pTrdAmt, orderno, "自动投资", PayType.INVEST.name());
			if(error.code < 0){
				return null;
			}
		} catch (Exception e) {
			if(error.code < 0){
				return null;
			}
		}	
		Bid bids = new Bid();
		bids.id = bid.id;
		//计算投资时分摊到投资人身上的借款管理费及投标奖励
		Map<String, Double> map = Bid.queryAwardAndBidFee(bids, pTrdAmt, error);
		double award = map.get("award");
		double bid_fee = map.get("bid_fee") ;	
		t_users invester = t_users.findById(user.id);		
		Invest.doInvest(invester, bid.id, pTrdAmt, orderno, 0, Constants.CLIENT_PC, award, bid_fee,null, error);	
		
		if(error.code < 0){
			return null;
		}		
		t_invests t_invest = t_invests.find(" mer_bill_no = ?", orderno).first();
		t_invest.ips_bill_no = xmlMap.get("contract_no");
		t_invest.mer_bill_no = FyPaymentUtil.createBillNo();
		t_invest.save();
		return null;
	}

	@Override
	public Map<String, Object> autoInvestSignature(ErrorInfo error, int client, Object... obj) {
		// 富友自动投标无需签约，故空实现
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
		List<LinkedHashMap<String, String>> list = FyPaymentImpl.queryRepaymentData(bill);		
		
		for(int i=0; i< list.size(); i++){
			
			LinkedHashMap<String, String> map = list.get(i);
			String type = map.get("type");
			map.remove("type");
			String out_cust_no = map.get("out_cust_no");
			String in_cust_no = map.get("in_cust_no");
			double amt = Convert.strToDouble(map.get("amt"), 0.00);
			String rem = map.get("rem");
			String mchnt_txn_ssn = map.get("mchnt_txn_ssn");
			try {
				//还款
				if("R".equals(type) && amt > 0){
					
					FyPaymentImpl.transferBu(error, out_cust_no, in_cust_no, amt, mchnt_txn_ssn, rem, PayType.REPAYMENT.name());
					if(error.code < 0){
						return null;
					}
				}
				//理财管理费
				if("RM".equals(type) && amt > 0){
					
					FyPaymentImpl.transferBmu(error, out_cust_no, in_cust_no, amt, mchnt_txn_ssn, rem, PayType.REPAYMENT.name());
					if(error.code < 0){
						return null;
					}
				}
			} catch (Exception e) {
				
				e.printStackTrace();
				if(error.code < 0){
					return null;
				}
			}
			
		}
		//处理本地业务逻辑
		bill.repayment(borrower.id, error);
		if(error.code < 0){
			return null;
		}
		error.code = 1;
		error.msg = "还款成功!";
		return null;
	}

	@Override
	public Map<String, Object> autoRepaymentSignature(ErrorInfo error, int client, Object... obj) {
		//富友接口无需签约，故空实现
		return null;
	}

	@Override
	public Map<String, Object> bidAuditFail(ErrorInfo error, int client, Object... obj) {
		
		Bid bid = new Bid();
		bid.id = (Long) obj[0];
		String typeStr = (String) obj[1];		
		//初始化借款人
		User borrower = new User();
		borrower.id = bid.userId;
		
		//调用解冻保证金接口
		try {
			if(bid.bail > 0 && bid.ipsStatus != 3){				
				FyPaymentImpl.unFreeze(error, borrower.ipsAcctNo, bid.bail, FyPaymentUtil.createBillNo("BUF", bid.bidNo), "解冻保证金", PayType.UNFREEZE.name());
				//将标的修改成，保证金已解冻状态
				JPAUtil.executeUpdate(error, " update t_bids d set d.ips_status = ? where d.id = ? ", 3, bid.id);
			}
			if(error.code < 0){
				return null;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			if(error.code < 0){
				return null;
			}
		}
		//解冻投资人投标金额
		List<t_invests> list = t_invests.find(" bid_id = ?", bid.id).fetch();
		for(int i =0; i < list.size(); i++){
			
			t_invests invest = list.get(i);
			//初始化投资人
			User invester = new User();
			invester.id = invest.user_id;
			String orderno = FyPaymentUtil.createBillNo("BF", invest.mer_bill_no);
			try {
				FyPaymentImpl.preAuthCancel(error, invester.ipsAcctNo, borrower.ipsAcctNo, invest.amount, invest.ips_bill_no, orderno, "标的审核不通过", PayType.BID_AUDIT_FAIL.name());				
				if(error.code < 0){
					return null;
				}
			} catch (Exception e) {
				if(error.code < 0){
					return null;
				}
				e.printStackTrace();
				
			}
		}
			
		if(error.code < 0){
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
				return null;
			}
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
		//调用解冻保证金接口
		try {
			if(bid.bail > 0 && bid.ipsStatus != 3){				
				FyPaymentImpl.unFreeze(error, borrower.ipsAcctNo, bid.bail, FyPaymentUtil.createBillNo("BUF", bid.bidNo), "解冻保证金", PayType.UNFREEZE.name());
				//将标的修改成，保证金已解冻状态
				JPAUtil.executeUpdate(error, " update t_bids d set d.ips_status = ? where d.id = ? ", 3, bid.id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(error.code < 0){
			return null;
		}		
		List<t_invests> investList = t_invests.find(" bid_id = ? ", bid.id).fetch();
		
		for(int i = 0; i < investList.size(); i++){			
			t_invests invest = investList.get(i);
			//初始化投资人
			User user = new User();
			user.id = invest.user_id;			
			try {
				//调用放款接口
				FyPaymentImpl.bidAuditSucc(error, user.ipsAcctNo, borrower.ipsAcctNo, invest.amount, invest.ips_bill_no, FyPaymentUtil.createBillNo("BS", invest.mer_bill_no), "满标放款", PayType.BID_AUDIT_SUCC.name());
				
				if(error.code < 0 ){
					return null;
				}
				//发放投标奖励
				if(invest.award > 0){
					
					FyPaymentImpl.transferBu(error, borrower.ipsAcctNo, user.ipsAcctNo, invest.award, FyPaymentUtil.createBillNo("BA", invest.mer_bill_no), "投标奖励", PayType.AWARD.name());
				}
				
				if(error.code < 0 ){
					return null;
				}
				
				//借款管理费
				if(invest.bid_fee > 0){
					
					FyPaymentImpl.transferBmu(error, borrower.ipsAcctNo, FyConstants.mchnt_name, invest.bid_fee, FyPaymentUtil.createBillNo("BM", invest.mer_bill_no), "借款管理费", PayType.BID_FEE.name());
				}				
				if(error.code < 0 ){
					return null;
				}
			} catch (Exception e) {			
				e.printStackTrace();
				if(error.code < 0 ){
					return null;
				}
			}
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
	public Map<String, Object> bidCreate(ErrorInfo error, int client, Object... obj) {
		
		
		//标的信息
		t_bids tbid = (t_bids) obj[0];
		//pc端/app/微信/手机网站	
		//标的业务类
		Bid bid = (Bid) obj[1];
		//初始化借款人
		User user = new User();
		user.id = tbid.user_id;
		String merBillNo = FyPaymentUtil.createBillNo();
		try {
			FyPaymentImpl.freeze(error, user.ipsAcctNo, bid.bail, merBillNo, "冻结保证金", PayType.USRFREEZE.name());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "返回标的列表页面"));
		}
		tbid.mer_bill_no = merBillNo; //流水号
		tbid.ips_bill_no = merBillNo; //ips流水号, 用于解冻保证金
		tbid.bid_no = FyPaymentUtil.createBillNo(); //标的号
		
		bid.afterCreateBid(tbid, merBillNo, client, -1, error);
		if(error.code < 0 ){
			
			//机构合作标，去后台提示
			if(tbid.agency_id > 0){		
				flash.error(error.msg);	
				BidAgencyAction.agencyBidList(0);
			}
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
		return null;
	}

	@Override
	public Map<String, Object> bidDataAudit(ErrorInfo error, int client, Object... obj) {
		//无需此功能
		return null;
	}

	@Override
	public Map<String, Object> debtorTransfer(ErrorInfo error, int client, Object... obj) {
		
		Long debtId = (Long) obj[0];
		String dealPassword = (String) obj[1];
		Map<String, Object> map = Debt.queryTransferInfo(debtId, error);
		
		//初始化转让人
		Long fromUserId = (Long) map.get("fromUserId");
		User invester = new User();
		invester.id = fromUserId;
		
		//初始化受让人
		Long toUserId = (Long) map.get("toUserId");
		User transfer = new User();
		transfer.id = toUserId;		
		String pCreMerBillNo = map.get("pCreMerBillNo").toString(); //登记债权人时提交的订单号
		double pPayAmt = Convert.strToDouble(map.get("pPayAmt").toString(), 0.00);//支付金额
		
		double pFromFee = Convert.strToDouble(map.get("managefee").toString(), 0.00) ;//转让方手续费
		
		
		try {
			//债权转让
			if(pPayAmt > 0){
				
				FyPaymentImpl.transferBu(error, transfer.ipsAcctNo, invester.ipsAcctNo, pPayAmt, FyPaymentUtil.createBillNo("D", pCreMerBillNo), "债权转让", PayType.DEBTOR_TRANSFER.name());
				
				if(error.code < 0 ){
					
					payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "返回标的列表页面"));
					return null;
				}
			}
			//债权转让手续费
			if(pFromFee > 0){
				
				FyPaymentImpl.transferBmu(error, invester.ipsAcctNo, FyConstants.mchnt_name, pFromFee, FyPaymentUtil.createBillNo("DM", pCreMerBillNo), "债权转让手续费", PayType.DEBTOR_TRANSFER.name());
				if(error.code < 0 ){
					payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "返回标的列表页面"));
					return null;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}			
		//处理本地业务逻辑
		Debt.dealDebtTransfer(FyPaymentUtil.createBillNo("D", pCreMerBillNo), debtId, dealPassword, false, error);
		if(error.code < 0 ){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "返回标的列表页面"));
			return null;
		}
		error.code = 1;
		error.msg = "债权转让成功!";
		payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "返回标的列表页面"));
		return null;
	}

	@Override
	public Map<String, Object> debtorTransferConfirm(ErrorInfo error, int client, LinkedList<Map<String, String>> pDetails, String pBidNo, String parentOrderno, String debtId, String dealpwd) {
		// 富友接口，只要一次成交，故债权转让成交接口为空实现
		return null;
	}

	@Override
	public Map<String, Object> grantCps(ErrorInfo error, int client, Object... obj) {
		// TODO Auto-generated method stub
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
	public Map<String, Object> invest(ErrorInfo error, int client, Object... obj) {
		
		//查询获取，接口所需要的参数
		t_bids bid = (t_bids) obj[0]; //投资的标的
		
		User user = (User) obj[1]; //投资人
		
		User borrower = new User();
		borrower.id = bid.user_id;
		
		double pTrdAmt = Convert.strToDouble(obj[2].toString(), 0.00); //投资金额
		String orderno = FyPaymentUtil.createBillNo();
		Map<String,String> xmlMap = null;
		try {
			xmlMap = FyPaymentImpl.preAuth(error, user.ipsAcctNo, borrower.ipsAcctNo, pTrdAmt, orderno, "普通投资", PayType.INVEST.name());
			if(error.code < 0){
				payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/invest/investHome", "我要理财页面!"));
				return null;
			}
		} catch (Exception e) {
			if(error.code < 0){
				payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/invest/investHome", "我要理财页面!"));
				return null;
			}
		}
		
		Bid bids = new Bid();
		bids.id = bid.id;
		//计算投资时分摊到投资人身上的借款管理费及投标奖励
		Map<String, Double> map = Bid.queryAwardAndBidFee(bids, pTrdAmt, error);
		double award = map.get("award");
		double bid_fee = map.get("bid_fee") ;	
		t_users invester = t_users.findById(user.id);		
		Invest.doInvest(invester, bid.id, pTrdAmt, orderno, 0, Constants.CLIENT_PC, award, bid_fee,null, error);
		
		
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/invest/investHome", "投资页面!"));
			return null;
		}		
		t_invests t_invest = t_invests.find(" mer_bill_no = ?", orderno).first();
		t_invest.ips_bill_no = xmlMap.get("contract_no");
		t_invest.mer_bill_no = FyPaymentUtil.createBillNo();
		t_invest.save();		
		error.code = 1;
		error.msg = "投资成功!";
		payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/dealRecord", "交易记录页面!"));
		return null;
	}

	@Override
	public Map<String, Object> merWithdrawal(ErrorInfo error, int client, Object... obj) {
		//富友接口，无此功能，富友商户提现，登陆富友官网进行提现即可
		return null;
	}

	@Override
	public Map<String, Object> merchantRecharge(ErrorInfo error, int client, Object... obj) {
		//富友接口，无此功能，富友商户充值，登陆富友官网进行充值即可
		return null;
	}

	@Override
	public Map<String, Object> queryAmount(ErrorInfo error, int client, Object... obj) {
		
		User user = (User) obj[0];
		Map<String, Object> dataMap = new HashMap<String, Object>();
		Map<String, String> map;
		try {
			map = FyPaymentImpl.queryAmount(error, user.getIpsAcctNo(), "余额查询");
			dataMap.put("pBlance", map.get("ca_balance").toString());
			dataMap.put("pFreeze", map.get("cf_balance").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataMap;
	}

	@Override
	public Map<String, Object> queryAmountByMerchant(ErrorInfo error, int client, Object... obj) {
		//富友接口，无需此功能，故空实现
		return null;
	}

	@Override
	public List<Map<String, Object>> queryBanks(ErrorInfo error, int client, Object... obj) {
		// 富友接口，提现银行卡，即为开户时设置的银行卡，故无需此接口，此处空实现
		return null;
	}

	@Override
	public Map<String, Object> recharge(ErrorInfo error, int client, Object... obj) {
		
		User user = User.currUser();
		//充值金额
		String pTrdAmt = obj[0].toString();
		//生成流水号
		String payNumber = FyPaymentUtil.createBillNo();
		//生成充值记录
		User.sequence(RechargeType.Normal, payNumber, new Double(pTrdAmt), Constants.GATEWAY_RECHARGE, Constants.CLIENT_PC, error);
		
		Map<String,String> paramMap =  FyPaymentImpl.recharge(error, user.ipsAcctNo, new Double(pTrdAmt), payNumber);
		
		
		String html = FyPaymentUtil.createHtml(paramMap, FyConstants.post_url + FyConstants.recharge);
		paramMap.put("payNumber", payNumber);
		paramMap.put("amount", pTrdAmt);		
		paramMap.put("out_cust_no", user.ipsAcctNo);		
		FyPaymentUtil.printRequestData(paramMap, "充值提交参数", PayType.RECHARGE.name(), true);
		renderHtml(html);
		return null;
	}

	@Override
	public Map<String, Object> register(ErrorInfo error, int client, Object... obj) {
		
		
		User user = (User) obj[0];
		List banks = t_user_bank_accounts.find(" user_id = ? ", user.id).fetch();
		if(banks == null || banks.size() == 0){
			
			checkFyBank();
		}		
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
		List<LinkedHashMap<String, String>> list = FyPaymentImpl.queryRepaymentData(bill);		
		
		for(int i=0; i< list.size(); i++){
			
			LinkedHashMap<String, String> map = list.get(i);
			String type = map.get("type");
			map.remove("type");
			String out_cust_no = map.get("out_cust_no");
			String in_cust_no = map.get("in_cust_no");
			double amt = Convert.strToDouble(map.get("amt"), 0.00);
			String rem = map.get("rem");
			String mchnt_txn_ssn = map.get("mchnt_txn_ssn");
			try {
				//还款
				if("R".equals(type) && amt > 0){
					
					FyPaymentImpl.transferBu(error, out_cust_no, in_cust_no, amt, mchnt_txn_ssn, rem, PayType.REPAYMENT.name());
					if(error.code < 0){
						payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/invest/investHome", "投资页面!"));
						return null;
					}
				}
				//理财管理费
				if("RM".equals(type) && amt > 0){
					
					FyPaymentImpl.transferBmu(error, out_cust_no, in_cust_no, amt, mchnt_txn_ssn, rem, PayType.REPAYMENT.name());
					if(error.code < 0){
						payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/invest/investHome", "投资页面!"));
						return null;
					}
				}
			} catch (Exception e) {
				
				e.printStackTrace();
				if(error.code < 0){
					payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/invest/investHome", "投资页面!"));
					return null;
				}
			}
			
		}
		//处理本地业务逻辑
		bill.repayment(borrower.id, error);
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/invest/investHome", "投资页面!"));
			return null;
		}
		error.code = 1;
		error.msg = "还款成功!";
		payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/invest/investHome", "投资页面!"));
		return null;
	}

	@Override
	public Map<String, Object> userBindCard(ErrorInfo error, int client, Object... obj) {
		//富友接口，银行卡绑定，在开户时已经绑定，无需重新绑定，故空实现
		return null;
	}

	@Override
	public Map<String, Object> withdraw(ErrorInfo error, int client, Object... obj) {
		
		User user = User.currUser();
		String withdrawalId = String.valueOf(obj[0]);
		double pTrdAmt = (Double)obj[1];
		Map<String,String> paramMap = FyPaymentImpl.withdraw(error, user.ipsAcctNo, pTrdAmt, FyPaymentUtil.createBillNo());
				
		String html = FyPaymentUtil.createHtml(paramMap, FyConstants.post_url + FyConstants.withdraw);
		paramMap.put("userId", user.id+"");
		paramMap.put("withdrawalId", withdrawalId);
		paramMap.put("out_cust_no", user.ipsAcctNo);
		FyPaymentUtil.printRequestData(paramMap, "提现提交参数", PayType.WITHDRAW.name(), true);
		renderHtml(html);
		return null;
	}
	
	public static void findByCity(String prov_num){
		
		List<Map<String, Object>>  list = t_pay_pro_city.find(" prov_num = ? ", prov_num).fetch();
		renderJSON(list);
	}
	/**
	 * 开户
	 * @param prov_num 省代码
	 * @param city_num 城市代码
	 * @param bank_type 开户行代码
	 * @param bank_name 开户行名称
	 * @param bank_num 开户行卡号
	 * @throws Exception 
	 */
	public static void addBank(String prov_num, String city_num, String bank_type, String bank_name, String bank_num) throws Exception{
		
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		
		Map<String, String> paramMap = FyPaymentImpl.register(error, user.realityName, user.idNumber, user.mobile, user.email, city_num, bank_type, bank_name, bank_num);		
		if(error.code < 0 ){		
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/home", "登陆页面"));
			return;
		}
		user.updateIpsAcctNo(user.id, user.mobile, error);
		
		approve();
		
		return;
	}

	@Override
	public Map<String, Object> loginAccount(ErrorInfo error, int client, Object... obj) {
		//富友接口，直接登录官网进行登录，故空实现
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
