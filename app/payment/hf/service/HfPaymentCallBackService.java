package payment.hf.service;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

import models.t_bids;
import models.t_users;

import org.json.JSONException;

import payment.PaymentProxy;
import payment.hf.util.HfConstants;
import payment.hf.util.HfPaymentUtil;
import payment.hf.util.UsrAcctPayEnum;
import payment.ips.util.IpsPaymentUtil;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.JPAUtil;
import business.AgentPayment;
import business.Bid;
import business.Bill;
import business.DealDetail;
import business.Debt;
import business.Invest;
import business.User;

import com.google.gson.Gson;
import com.shove.Convert;

import constants.Constants;
import constants.IPSConstants;
import constants.PayType;
import controllers.supervisor.financeManager.ReceivableBillManager;

/**
 * 汇付接口回调，业务类
 * @author liuwenhui
 *
 */
public class HfPaymentCallBackService extends HfPaymentService{
	
	public Gson gson = new Gson();
	
	/**
	 * 开户回调
	 * @param resultMap
	 * @param desc
	 * @param error
	 */
	public void userRegister(Map<String, String> resultMap, String desc, ErrorInfo error){
		
		//日志打印
		this.printData(resultMap, desc, PayType.REGISTER);
		
		//签名，状态码，仿重单处理;
		HfPaymentUtil.checkSign(resultMap, desc, PayType.REGISTER, error);
		
		if(error.code < 0){
			
			return;
		}
		
		Map<String, String> dataMap = this.queryRequestData(resultMap.get("MerPriv"),error);
		
		User user = new User();
		long userId = Convert.strToLong(dataMap.get("userId").toString(),  -1);
		user.id = userId;
		
		user.updateTrustAcctount(userId, resultMap.get("UsrCustId"),resultMap.get("UsrName"), 
				resultMap.get("IdNo"),resultMap.get("UsrMp"),resultMap.get("UsrEmail"), error);
	}
	
	/**
	 * 充值回调
	 * @throws JSONException 
	 * @throws UnsupportedEncodingException 
	 */
	public void netSave(Map<String, String> paramMap, ErrorInfo error){
		
		this.printData(paramMap, "充值回调参数", PayType.RECHARGE);
		
		//签名，状态码，仿重单处理;
		HfPaymentUtil.checkSign(paramMap, "充值", PayType.RECHARGE, error);
		if(error.code < 0){
			return;
		}
		Map<String, String> dataMap = this.queryRequestData(paramMap.get("MerPriv"), error);		
		User.recharge(dataMap.get("payNumber").toString(), Double.parseDouble(dataMap.get("amount").toString()), error);
	}
	
	/**
	 * 标的信息录入业务
	 * @param paramMap
	 * @param desc
	 * @param error
	 */
	public void addBidInfo(Map<String, String> resultMap, String desc, ErrorInfo error) {
		
		this.printData(resultMap, desc, PayType.BIDCREATE);
		
		HfPaymentUtil.checkSign(resultMap, desc, PayType.BIDCREATE, error);
		
		if(error.code < 0){
			
			return ;
		}
		
		//获取请求参数
		Map<String, String> dataMap = this.queryRequestData(resultMap.get("MerPriv"), error);
		t_bids bid = gson.fromJson(dataMap.get("tbid"), t_bids.class);
		String borrCustId = dataMap.get("borrCustId");
		int client = Integer.parseInt(dataMap.get("client"));
		
		//无需保证金
		if(bid.bail <= 0){
			//业务逻辑
			new Bid().afterCreateBid(bid, bid.bid_no, client, -1, error);
			
			
			return ;  //标的发布结束
		}
		
		JPAUtil.transactionCommit();  //标的信息录入业务执行完毕，事务提交
		
		/** 冻结保证金开始  */
		
		//冻结保证金参数组装
		String freezeOrdId = this.createBillNo();
		LinkedHashMap<String, String> paramMap = this.freezeBailAmount(freezeOrdId, borrCustId, bid.bail, error);
		
		//组装回调需要参数
		LinkedHashMap<String, String> reqDataMap = new LinkedHashMap<String, String>();		
		reqDataMap.putAll(paramMap);
		reqDataMap.put("tbid", gson.toJson(bid));
		reqDataMap.put("client", client+"");
		
		this.printRequestData(reqDataMap, "冻结保证金提交参数", PayType.USRFREEZE);
		
		String freezeResult  = HfPaymentUtil.postMethod(HfConstants.CHINAPNR_URL, paramMap, "UTF-8");
		
		//解析冻结保证金额回调参数字符串
		Map<String,String> freezeReturnMap = HfPaymentUtil.jsonToMap(freezeResult);
		
		this.freezeBailAmount(freezeReturnMap, "冻结保证金及时响应", error);
	}

	/**
	 * 投标回调业务逻辑，（包含：主动投标、自动投标）
	 * 
	 * @param paramMap
	 * @param desc
	 * @param payType
	 * @param error
	 */
	public void doInvest(Map<String, String> paramMap, String desc, PayType payType, ErrorInfo error){
		error.clear();
		
		//日志打印
		this.printData(paramMap, desc, payType);
		
		//签名，状态码，仿重单处理;
		HfPaymentUtil.checkSign(paramMap, desc, payType, error);
		if(error.code < 0){
			
			return ;
		}
		
		String orderNo = paramMap.get("MerPriv");
		Map<String, String> maps = this.queryRequestData(orderNo, error);
		long bidId = Convert.strToLong(maps.get("bidId") + "", 0);

		String freezeTrxId = paramMap.get("FreezeTrxId");
		
		t_bids tbid = new t_bids();
		tbid.id = bidId;
		t_users user = User.queryUserByIpsAcctNo(String.valueOf(paramMap.get("UsrCustId")));
		
		double investTotal = Double.valueOf(String.valueOf(paramMap.get("TransAmt")));
		
		double pFee = tbid.service_fees;
		int client =  Constants.PC;
		Bid bid = new Bid();
		bid.id = tbid.id;
		Map<String, Double> map = Bid.queryAwardAndBidFee(bid, investTotal, error);
		double award = map.get("award");
		double bid_fee = map.get("bid_fee");
		
		//调用投标业务
		Invest.doInvest(user, bidId, investTotal, orderNo, pFee, client, award, bid_fee,null, error);	
		
		if(error.code < 0){
			if(error.code == Constants.OVERBIDAMOUNT){  //超标处理
				this.doUserUnFreeze(orderNo, freezeTrxId, "投标解冻请求", "投标解冻回调", error);
				
				if(error.code >= 0 || error.code != Constants.ALREADY_RUN) {  //解冻成功
					error.code = -1;
					error.msg = "投标失败,本次投资已超额,投资资金已解冻.";
				}
				
				return;
			}
			
			return;
		}
		
		Invest.modifyInvestIpsBillNo(orderNo,freezeTrxId,error);
		
		return;
		
	}
	
	/**
	 * 自动投标计划回调业务
	 * @param resultMap
	 * @param error
	 */
	public void autoInvestSignature(Map<String, String> resultMap, String desc,  ErrorInfo error) {
		error.clear();
		
		this.printData(resultMap, desc, PayType.AUTO_INVEST_SIGNATURE);
		
		//签名，状态码，仿重单处理;
		HfPaymentUtil.checkSign(resultMap, desc, PayType.AUTO_INVEST_SIGNATURE, error);
		
		if(error.code < 0){
			
			return ;
		}
	
		String ipsAccountNo = resultMap.get("UsrCustId");  //汇付无需签约号，托管账户号当作签约号
		
		long userId = User.queryUserByIpsAcctNo(ipsAccountNo).id;
		
		User user = new User();
		user.ipsBidAuthNo = ipsAccountNo;
		user.updateIpsBidAuthNo(userId, error);
	}
	
	/**
	 * 用户绑卡回调
	 * @param paramMap
	 * @param error
	 */
	public void freezeBailAmount(Map<String, String> resultMap, String desc, ErrorInfo error) {
		this.printData(resultMap, desc, PayType.USRFREEZE);
		
		HfPaymentUtil.checkSign(resultMap, desc, PayType.USRFREEZE, error);
		
		if(error.code < 0 ){
			
			return ;
		}	
		
		Map<String, String> dataMap = this.queryRequestData(resultMap.get("MerPriv"), error);
		t_bids bid = gson.fromJson(dataMap.get("tbid"), t_bids.class);
		int client = Integer.parseInt(dataMap.get("client"));

		bid.ips_bill_no = resultMap.get("TrxId"); //冻结标识号-第三方唯一标识,用于后期解冻
		bid.mer_bill_no = resultMap.get("OrdId"); //冻结保证金提交流水号
		
		//业务逻辑
		new Bid().afterCreateBid(bid, bid.bid_no, client, -1, error);
		
	}
	
	/**
	 * 用户绑卡回调
	 * @param paramMap
	 * @param error
	 */
	public void userBindCard(Map<String, String> paramMap, ErrorInfo error){
		
		//日志打印
		this.printData(paramMap, "用户绑卡回调参数", PayType.USER_BIND_CARD);
		
		//签名，状态码，仿重单处理;
		HfPaymentUtil.checkSign(paramMap, "用户绑卡", PayType.USER_BIND_CARD, error);

		if(error.code < 0){
			return;
		}
	}
	
	/**
	 * 提现回调
	 * @param paramMap
	 * @param error
	 */
	public void withdraw(Map<String, String> paramMap, String desc, ErrorInfo error){
		
		//日志打印
		this.printData(paramMap, desc, PayType.WITHDRAW);
		
		//签名，状态码，仿重单处理;
		HfPaymentUtil.checkSign(paramMap, desc, PayType.WITHDRAW, error);
		if(error.code < 0){
			return;
		}
		
		String orderNo = paramMap.get("MerPriv");
		
		double serviceFee = Convert.strToDouble(paramMap.get("ServFee") + "", 0.00);  //商户收取用户的服务费
		@SuppressWarnings("unused")
		double feeAmt = Convert.strToDouble(paramMap.get("FeeAmt") + "", 0.00);  //手续费金额（汇付收），商户垫付
		
		Map<String, String> dataMap = this.queryRequestData(orderNo, error);
		long withdrawalId = Convert.strToLong(dataMap.get("withdrawalId"), -1);
		long userId = Convert.strToLong(dataMap.get("userId") + "", -1);
		
		boolean chargeMode = false;
		Map<String,String> returnDate = this.queryReturnData(orderNo, error);
		String realTransAmt = returnDate.get("RealTransAmt");  //实际到账金额为可选参数，第一次回调有该参数，后面几次可能没有，所以从数据库中查询
		String rtransAmt= returnDate.get("TransAmt");
		if(realTransAmt.equals(rtransAmt)){  //用于处理内扣外扣之分
			chargeMode = true;
		}
		
		User.withdrawalNotice(userId, serviceFee, withdrawalId, "2", chargeMode, true, error);
	}
	
	/**
	 * 提现回调-商户
	 * @param paramMap
	 * @param error
	 */
	public void merWithdrawal(Map<String, String> paramMap, ErrorInfo error) {
		error.clear();

		// 日志打印
		this.printData(paramMap, "提现同步回调-商户", PayType.WITHDRAW);

		// 签名，状态码，仿重单处理;
		HfPaymentUtil.checkSign(paramMap, "提现-商户", PayType.WITHDRAW, error);
		if (error.code < 0) {
			return;
		}
		String orderNo = paramMap.get("MerPriv");
		Map<String, String> dataMap = this.queryRequestData(orderNo, error);
		double feeAmt = Double.parseDouble(String.valueOf(paramMap.get("FeeAmt")));
		double transAmt = Double.parseDouble(String.valueOf(dataMap.get("TransAmt")));; // 真实提现金额,汇付采取先外口,后内扣
		Map<String,Object> resutlMap = PaymentProxy.getInstance().queryAmountByMerchant(error, Constants.PC);
		
		double amountCount = Double.valueOf(String.valueOf(resutlMap.get("AcctBal")));
		DealDetail.addMerChantDetail(transAmt, feeAmt, 2, amountCount, Long.valueOf(orderNo), error);
	}
	
	/**
	 * 商户充值回调
	 * @throws JSONException 
	 * @throws UnsupportedEncodingException 
	 */
	public void merchantRecharge(Map<String, String> paramMap, ErrorInfo error){
		error.clear();
		
		this.printData(paramMap, "商户充值回调参数", PayType.RECHARGE);
		
		//签名，状态码，仿重单处理;
		HfPaymentUtil.checkSign(paramMap, "商户充值回调", PayType.RECHARGE, error);
		if(error.code < 0){
			return;
		}
		String orderNo = paramMap.get("OrdId");
		double feeAmt = Double.parseDouble(String.valueOf(paramMap.get("FeeAmt")));
		double transAmt = Double.parseDouble(String.valueOf(paramMap.get("TransAmt"))); // 真实提现金额,汇付采取先外口,后内扣
		
		Map<String,Object> resutlMap = PaymentProxy.getInstance().queryAmountByMerchant(error, Constants.PC);
		
		double amountCount = Double.valueOf(String.valueOf(resutlMap.get("AcctBal")));
		DealDetail.addMerChantDetail(transAmt, feeAmt, 1, amountCount, Long.valueOf(orderNo), error);
	}
	
	

	

	/**
	 * 转账（商户用），回调业务
	 * @param respParams
	 * @param desc
	 * @param error
	 */
	public void transfer(Map<String, String> paramMap, String desc, ErrorInfo error) {
		error.clear();
		
		Map<String, String> maps = this.queryRequestData(paramMap.get("OrdId"), error);
		
		PayType payType = PayType.valueOf(maps.get("PayType"));
		
		this.printData(paramMap, desc, payType);
		
		//签名，状态码，仿重单处理;
		HfPaymentUtil.checkSign(paramMap, desc, payType, error);
		if(error.code < 0){
			
			return;
		}
		
		switch (payType) {
		
		/* -- 异步不处理，由于订单号唯一，若同步有执行失败，由P2P重复点击本金垫付执行逻辑
		case ADVANCE:
			doAdvance(paramMap, error);
			break;
		*/
		
		case GRANTCPS:
			doGrantCps(paramMap, error);
			break;
			
		case GRANT_INVITATION:
			doGrantInvitation(paramMap, error);
			break;
			
		case AGENTRECHARGE:
			doAgentRecharge(paramMap, error);
			break;
		}
		
	}
	
	/**
	 * 本金垫付业务逻辑
	 * @param params
	 * @return
	 
	private static void doAdvance(Map<String,String> params, ErrorInfo error){
		error.clear();
		
		String orderNum = params.get("OrdId");
		/*error = checkDuplicate(error, "本金垫付", orderNum,params);
		if(error.code < 0){
			return error;
		}/
		Map<String,Object> maps = ChinaPnrUtil.queryMmmDataByOrderNum(orderNum);
		int uType = (Integer)maps.get("UType");
		long billInvestId = Long.valueOf(maps.get("UBillInvestMerBillId")+"");
		ChinaPnrUtil.modifyBillInvestMerBillId(billInvestId, orderNum, error);
		return ;
	}
	*/

	/**
	 * cps推广业务逻辑
	 * @param params
	 * @return
	 */
	private void doGrantCps(Map<String,String> params, ErrorInfo error){
		error.clear();
		
		String orderNum = params.get("MerPriv");

		Map<String,String> maps = this.queryRequestData(orderNum, error);
		long userCpsIncomeId = Long.valueOf(maps.get("UUserCpsIncomeId"));
		User.updateIncomeStatus(userCpsIncomeId, 2, error);
		if(error.code < 0){
			Logger.error("###修改推广费的状态错误");
			JPA.setRollbackOnly();
		}
		double amount = Double.valueOf(maps.get("TransAmt")+"");
		long userId = Long.valueOf(User.queryUserId(maps.get("InCustId")+"", error));
		User.updateUserBanlance(error, amount, userCpsIncomeId, userId);
		if(error.code < 0){
			Logger.error("###发放推广记录修改用户账户资金");
			JPA.setRollbackOnly();
		}
		return ;
	}
	
	/**
	 * 佣金发放业务逻辑
	 * @param params
	 * @return
	 */
	private void doGrantInvitation(Map<String,String> params, ErrorInfo error){
		error.clear();
		
		String orderNum = params.get("MerPriv");
	
		Map<String,String> maps = this.queryRequestData(orderNum, error);
		long invitedIncomeId = Long.valueOf(maps.get("invitedIncomeId"));
		double amount = Double.valueOf(maps.get("TransAmt"));
		long userId = Long.valueOf(User.queryUserId(maps.get("InCustId"), error));
		
		User.addBanlanceForInvitation(error, amount, invitedIncomeId, userId);
		if(error.code < 0){
			Logger.error("###发放佣金，修改用户账户资金时，%s", error.msg);
			JPA.setRollbackOnly();
		}
		
		return ;
	}
	
	/**
	 * 代理充值-商户转账给用户-逻辑
	 * @param params
	 * @return
	 */
	private void doAgentRecharge(Map<String,String> resultMap, ErrorInfo error){
		error.clear();
		
		String orderNum = resultMap.get("MerPriv");
		
		Map<String,String> maps = this.queryRequestData(orderNum,error);
		double amount = Double.valueOf(maps.get("TransAmt"));
		long agentOrderNo = Long.valueOf(maps.get("agentOrderNo"));
		
		
		AgentPayment ap = new AgentPayment(agentOrderNo, error);
		if(error.code < 0){
			
			return ;
		}
		
		ap.transferSuccess(amount, error);
		
		return ;
	}
	
	
	
	/**
	 * 更新理财人账单流水号-用于本金垫付
	 * @param id			理财账单ID
	 * @param mer_bill_no	订单流水号
	 * @param error
	 */
	public void modifyBillInvestMerBillId(long id,String mer_bill_no,ErrorInfo error){
		error.clear();
		
		try{
			JPAUtil.transactionBegin();			
			Logger.info("modifyBillInvestMerBillId -> id[%s]mer_bill_no[%s]", id,mer_bill_no);
			String sql = "update t_bill_invests set mer_bill_no = ? where id = ?";
			int row = JPA.em().createNativeQuery(sql).setParameter(1, mer_bill_no).setParameter(2, id).executeUpdate();
			if(row <1){
				error.code = -1;
				error.msg = "更新理财人账单流水号-用于本金垫付异常";
				Logger.error("###10###更新理财人账单流水号-用于本金垫付时影响行数:%s", row);
			}
			JPAUtil.transactionCommit();
		}catch(Exception e){
			error.code = -1;
			error.msg = "更新理财人账单流水号-用于本金垫付异常";
			Logger.error("###10###更新理财人账单流水号-用于本金垫付时:%s", e.getMessage());
		}
	}
	
	/**
	 * 本金垫付业务操作
	 * @param supervisorId	管理员ID
	 * @param bill_id		借款账单ID
	 * @param pMerBillNo	订单流水号
	 * @param error	
	 */
	public void advance(long supervisorId,long bill_id, ErrorInfo error) {
		error.clear();
		
		new Bill().principalAdvancePayment(supervisorId, bill_id, error);
	}
	
	/**
	 * 线下收款业务操作
	 * @param supervisorId	管理员ID
	 * @param bill_id		借款账单ID
	 * @param pMerBillNo	订单流水号
	 * @param error	
	 */
	public void offlineRepayment(long supervisorId,long bill_id, ErrorInfo error) {
		error.clear();
		
		Bill bill = new Bill();	
		bill.id = bill_id;
		bill.offlineCollection(supervisorId, error);
	}
	
	/**
	 * 垫付还款
	 * @param params
	 * @return
	 */
	public void advanceRepayment(Map<String,String> params, ErrorInfo error){
		error.clear();
		
		String orderNum = params.get("MerPriv");
		
		Map<String,String> maps = this.queryRequestData(orderNum, error);
		
		long billId = Long.valueOf(maps.get("UBill_id"));
		Bill bill = new Bill();
		bill.id = billId;
		
		//垫付还款逻辑
		bill.repayment(bill.user.id, error);
	}
	
	/**
	 * 用户支付回调业务
	 * @param respParams
	 * @param desc
	 * @param error
	 */
	public void usrAcctPay(Map<String, String> paramMap, String desc, ErrorInfo error) {
		error.clear();
		
		// 获取请求参数
		Map<String, String> maps = this.queryRequestData(paramMap.get("OrdId"), error);
		// 获取用户支付类型
		UsrAcctPayEnum usrAcctPayEnum = UsrAcctPayEnum.valueOf(maps.get("UMerPriv"));
		// 获取操作类型
		PayType payType = PayType.valueOf(maps.get("PayType").toString());
		
		// 打印返回参数
		this.printData(paramMap, desc, payType);
		
		//签名，状态码，仿重单处理;
		HfPaymentUtil.checkSign(paramMap, desc, payType, error);
		if(error.code < 0){
			
			return;
		}
		
		switch (usrAcctPayEnum) {
		case ADVANCEREPAYMENT:
			advanceRepayment(paramMap, error);
			break;

		case APPLYVIP:
			break;
		}
		
	}
	
	/**
	 * 债权转让回调业务
	 * @param respParams
	 * @param desc
	 * @param error
	 */
	public void doDebtTransfe(Map<String, String> paramMap, String desc, ErrorInfo error) {
		error.clear();
		
		// 日志打印
		this.printData(paramMap, desc, PayType.DEBTOR_TRANSFER);

		// 签名，状态码，仿重单处理;
		HfPaymentUtil.checkSign(paramMap, desc, PayType.DEBTOR_TRANSFER, error);
		if (error.code < 0) {
			return;
		}
		
		String orderNo = paramMap.get("MerPriv");
		Map<String,String> maps = this.queryRequestData(orderNo, error);
		String dealpwd = null;
		long debtId = Long.valueOf(maps.get("debtId"));
		Debt.dealDebtTransfer(orderNo, debtId, dealpwd, false, error);	
	}
	
	/**
	 * 批量还款，回调业务
	 * @param respParams
	 * @param desc
	 * @param error
	 */
	public void batchRepayment(Map<String, String> paramMap, String desc, ErrorInfo error) {
		error.clear();
		
		// 获取请求参数
		Map<String, String> maps = this.queryRequestData(paramMap.get("BatchId"), error);
		long billId = Long.valueOf(maps.get("UBillId").toString());
		long userId = Long.valueOf(maps.get("UUserId").toString());
		
		// 打印返回参数
		this.printData(paramMap, desc, PayType.REPAYMENT);
		
		//签名，状态码，仿重单处理;
		HfPaymentUtil.checkSign(paramMap, desc, PayType.REPAYMENT, error);
		if(error.code < 0){
			
			return;
		}
		
		// 还款逻辑
		Bill bill = new Bill();
		bill.id = billId;
		bill.repayment(userId, error);
		
		if(error.code >= 0) {
			error.msg = "还款成功！";
		}
	}

	/**
	 * 标的审核失败，撤标，流标，回调业务逻辑
	 * 
	 * @param type
	 * @param bid
	 * @param error
	 */
	public void bidAuditFail(String type, Bid bid, ErrorInfo error) {
		// 判断是那种流标方式
		if (type.equals(IPSConstants.BID_CANCEL_B)) { // 提前借款->借款中不通过
			bid.advanceLoanToPeviewNotThroughBC(error);  
		} else if(type.equals(IPSConstants.BID_ADVANCE_LOAN)) {   // 提前借款-->流标
			bid.advanceLoanToFlowBC(error);  
		} else if(type.equals(IPSConstants.BID_CANCEL_F)) {   // 提前借款->撤销
			bid.advanceLoanToRepealBC(error);  
		} else if(type.equals(IPSConstants.BID_CANCEL_I)) {   // 筹款中->借款中不通过
			bid.fundraiseToPeviewNotThroughBC(error);  
		} else if(type.equals(IPSConstants.BID_FUNDRAISE)) {   // 筹款中->流标
			bid.fundraiseToFlowBC(error);  
		} else if(type.equals(IPSConstants.BID_CANCEL_N)) {   // 筹款中->撤销
			bid.fundraiseToRepealBC(error); 
		}  else if(type.equals(IPSConstants.BID_CANCEL_M)) {   // 满标->放款不通过
			bid.fundraiseToLoanNotThroughBC(error); 
		} else if(type.equals(IPSConstants.BID_CANCEL_S)) {   // 审核中->审核不通过
			bid.auditToNotThroughBC(error); 
		} else if(type.equals(IPSConstants.BID_CANCEL)) {   // 审核中->撤销
			bid.auditToRepealBC(error); 
		}  
	}

}
