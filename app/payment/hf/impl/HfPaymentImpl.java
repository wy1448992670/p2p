package payment.hf.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import models.t_bids;
import models.t_bill_invests;
import models.t_bills;
import models.t_invests;
import models.t_users;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import payment.PaymentInterface;
import payment.hf.service.HfPaymentCallBackService;
import payment.hf.service.HfPaymentService;
import payment.hf.util.CashEnum;
import payment.hf.util.HfConstants;
import payment.hf.util.HfPaymentUtil;
import payment.hf.util.UsrAcctPayEnum;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import business.Bid;
import business.Bill;
import business.BillInvests;
import business.Debt;
import business.Invest;
import business.User;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shove.Convert;

import constants.Constants;
import constants.Constants.RechargeType;
import constants.PayType;
import controllers.payment.hf.HfPaymentCallBackAction;
import controllers.payment.hf.HfPaymentReqAction;



/**
 * 环讯资金托管接口封装实现类
 * @author xiaoqi
 *
 */
public class HfPaymentImpl implements PaymentInterface {
	
	public HfPaymentService hfPaymentService = new HfPaymentService();
	public HfPaymentCallBackService hfPaymentCallBackService = new HfPaymentCallBackService();
	public Gson gson = new Gson();
	
	@Override
	public Map<String, Object> register(ErrorInfo error, int client, Object... obj) {

		User user = (User) obj[0];
		// 生成流水号
		String orderNo = hfPaymentService.createBillNo();
		// 参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.register(user.mobile,
				user.email, orderNo);

		// 生成表单
		String html = HfPaymentUtil.createFormHtml(paramMap, HfConstants.CHINAPNR_URL);

		// 提交入库参数, 用于回调
		paramMap.put("userId", String.valueOf(user.getId())); // 回调需要参数:用户id

		// 打印保存日志
		hfPaymentService.printRequestData(paramMap, "开户提交参数", PayType.REGISTER);

		// 表单提交
		HfPaymentReqAction.getInstance().submitForm(html, client);
		
		return null;
	}
	
	@Override
	public Map<String, Object> recharge(ErrorInfo error, int client, Object... obj) {
		
		User user = User.currUser();
		//银行卡编码
		String bankCode = String.valueOf(obj[1]);
		//充值金额
		double transAmt = (Double)obj[0];
		//生成流水号
		String payNumber = hfPaymentService.createBillNo();
		//生成充值记录
		User.sequence(RechargeType.Normal, payNumber, new Double(transAmt), Constants.GATEWAY_RECHARGE, Constants.CLIENT_PC, error);
		
		if(error.code < 0){
			
			return null;
		}
		
		//组装充值所需要的参数
		LinkedHashMap<String, String> paramMap = hfPaymentService.recharge(user.ipsAcctNo, payNumber, transAmt);
		
		//生成表单
		String html = HfPaymentUtil.createFormHtml(paramMap, HfConstants.CHINAPNR_URL);
		
		//提交入库参数, 用于回调
		paramMap.put("userId", String.valueOf(user.getId())); //回调需要参数:用户id
		paramMap.put("amount", transAmt+""); //回调需要参数:用户id
		paramMap.put("payNumber", payNumber);//支付流水号
		paramMap.put("UsrCustId", user.ipsAcctNo);
		
		//打印日志
		hfPaymentService.printRequestData(paramMap, "充值提交参数", PayType.RECHARGE);
		
		//提交表单
		HfPaymentReqAction.getInstance().submitForm(html, client);
		
		return null;
	}
	
	@Override
	public Map<String, Object> bidCreate(ErrorInfo error, int client, Object... obj) {
		
		//标的
		t_bids bid = (t_bids) obj[0];		
		
		//标的号，生成标的编号
		bid.bid_no = hfPaymentService.createBillNo();
		
		//借款人ips账号
		String borrCustId = User.queryIpsAcctNo(bid.user_id, error);
		
		//借款金额
		double borrTotAmt = bid.amount;
		
		//年利率
		String yearRate = HfPaymentUtil.formatAmount(bid.apr);
		
		//计算还款日期
		Date pubTime = bid.time;
		int period_unit = bid.period_unit;  //借款期限类型-1: 年;0:月;1:日;
		int period = bid.period;  //借款期限
		Date repaymentTime = null;
		switch (period_unit) {
		case -1:
			repaymentTime = DateUtil.dateAddYear(pubTime, period);
			break;
		case 0:
			repaymentTime = DateUtil.dateAddMonth(pubTime, period);
			break;
		case 1:
			repaymentTime = DateUtil.dateAddDay(pubTime, period);
			break;
		}
		//还款日期
		String retDate = DateUtil.simple(repaymentTime);
		
		//标的发布参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.bidCreate(bid.bid_no, borrCustId, borrTotAmt, yearRate, borrTotAmt, retDate, PayType.BIDCREATE.name());
		
		//组装回调需要参数
		LinkedHashMap<String, String> dataMap = new LinkedHashMap<String, String>();		
		dataMap.putAll(paramMap);
		dataMap.put("tbid", gson.toJson(bid));
		dataMap.put("client", client+"");
		dataMap.put("borrCustId", borrCustId);
		
		hfPaymentService.printRequestData(dataMap, "标的发布提交参数", PayType.BIDCREATE);
		
		String result  = HfPaymentUtil.postMethod(HfConstants.CHINAPNR_URL, paramMap, "UTF-8");
		
		//解析回调参数字符串
		Map<String,String> resultMap = HfPaymentUtil.jsonToMap(result);
		

		hfPaymentCallBackService.addBidInfo(resultMap, "标的发布及时响应", error);
		
		if(error.code < 0 && error.code != Constants.ALREADY_RUN){
			
			return null;
		}

		//标的发布成功，页面跳转，提示完善资料
		HfPaymentCallBackAction.getInstance().addBidInfoWS(error, bid);
		
		return null;
	}
	
	@Override
	public Map<String, Object> invest(ErrorInfo error, int client, Object... obj) {
		
		//投资人
		User user = (User) obj[1];
		//标的
		t_bids bid = (t_bids)obj[0];
		
		//流水号
		String ordId = hfPaymentService.createBillNo();
		
		//投资金额
		double transAmt = Double.parseDouble(obj[2]+"");
		
		//借款人第三方账号
		String borrowerCustId = User.queryIpsAcctNo(bid.user_id, error);
		
		if(error.code < 0){
			
			return null;
		}
		
		//借款人信息
		List<Map<String, String>> borrowerDetails = new LinkedList<Map<String, String>>();		
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("BorrowerCustId", borrowerCustId); //借款人第三方唯一标示
		map.put("BorrowerAmt", HfPaymentUtil.formatAmount(transAmt)+""); //借款金额
		map.put("BorrowerRate", HfConstants.BORROWERRATE);
		map.put("ProId", bid.bid_no); //标的号即项目号
		borrowerDetails.add(map);	
		
		//冻结订单号
		String freezeOrdId = hfPaymentService.createBillNo();
		
		//组装接口参数
		LinkedHashMap<String, String> paramMap = hfPaymentService.invest(ordId, transAmt, user.ipsAcctNo, gson.toJson(borrowerDetails), freezeOrdId);
		
		String html = HfPaymentUtil.createFormHtml(paramMap, HfConstants.CHINAPNR_URL);
		
		paramMap.put("bidId", bid.id + "");
		paramMap.put("transAmt", String.valueOf(transAmt));
		
		//打印日志
		hfPaymentService.printRequestData(paramMap, "手动投标提交参数", PayType.INVEST);
		
		return HfPaymentReqAction.getInstance().submitForm(html, client);
		
	}
	
	@Override
	public Map<String, Object> autoInvestSignature(ErrorInfo error, int client, Object... obj) {
		
		if(!HfConstants.TENDERPLANSUPPORT){
			error.code = -1;
			error.msg = "商户尚未与汇付天下签约自动投标功能";
			HfPaymentReqAction.payErrorInfo(error.code, error.msg, PayType.AUTO_INVEST_SIGNATURE);
		}
		
		Map<String,String> map = (Map<String, String>) obj[0];
		String platFormUserId = map.get("userId");
		String usrCustId= User.queryIpsAcctNo(Long.valueOf(platFormUserId), error);
		
		if(StringUtils.isBlank(usrCustId)){
			error.code = -2;
			error.msg = "您还未开通托管账户，无法签约";
			HfPaymentReqAction.payErrorInfo(error.code, error.msg, PayType.AUTO_INVEST_SIGNATURE);
		}
		
		String orderNo = hfPaymentService.createBillNo();
		
		Map<String,String> paramMap = hfPaymentService.autoInvestSignature(usrCustId, orderNo);
		
		//生成表单
		String html = HfPaymentUtil.createFormHtml(paramMap, HfConstants.CHINAPNR_URL);
		
		hfPaymentService.printRequestData(paramMap, "自动投标签约提交参数", PayType.AUTO_INVEST_SIGNATURE);	
		
		HfPaymentReqAction.getInstance().submitForm(html, client);
		
		return null;
	}
	
	@Override
	public Map<String, Object> autoInvest(ErrorInfo error, int client, Object... obj) {
		
		t_bids bid = (t_bids) obj[0];  //标的
		
		long userId = (Long) obj[1];  //用户id
		
		String ordId = hfPaymentService.createBillNo();  //订单号
		
		double transAmt = Convert.strToDouble(obj[2].toString(), 0.00);  //投资金额
		
		String usrCustId = User.queryIpsAcctNo(userId, error);  //投资人第三方账号
		
		//借款人信息
		String borrowerCustId = User.queryIpsAcctNo(bid.user_id, error);  //借款人第三方账号
		List<Map<String, String>> borrowerDetails = new LinkedList<Map<String, String>>();		
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("BorrowerCustId", borrowerCustId); //借款人第三方唯一标示
		map.put("BorrowerAmt", HfPaymentUtil.formatAmount(transAmt)+""); 
		map.put("BorrowerRate", HfConstants.BORROWERRATE);
		map.put("ProId", bid.bid_no); //标的号即项目号
		borrowerDetails.add(map);	
	
		//冻结订单号
		String freezeOrdId = hfPaymentService.createBillNo();
		
		//组装参数
		Map<String, String> paramMap = hfPaymentService.autoInvest(ordId, transAmt, usrCustId, gson.toJson(borrowerDetails), freezeOrdId);
		
		//回调所需参数
		LinkedHashMap<String, String> dateMap = new LinkedHashMap<String, String>();
		dateMap.putAll(paramMap);
		dateMap.put("bidId", bid.id + "");
		dateMap.put("transAmt", String.valueOf(transAmt));
		
		hfPaymentService.printRequestData(dateMap, "自动投标提交参数", PayType.AUTO_INVEST);
		
		String result = HfPaymentUtil.postMethod(HfConstants.CHINAPNR_URL, paramMap, "utf-8");
		
		Map<String,String> resultMap = HfPaymentUtil.jsonToMap(result);
		
		hfPaymentCallBackService.doInvest(resultMap, "自动投标投标及时响应", PayType.AUTO_INVEST, error);
		
		
		
		return null;
	}
	
	@Override
	public Map<String, Object> advance(ErrorInfo error, int client, Object... obj) {
		//后去借款信息
		Long bidId = (Long) obj[0];		
		t_bids bid = t_bids.findById(bidId);
		//获取账单信息
		Long bill_id = (Long) obj[1];
		t_bills t_bill = t_bills.findById(bill_id);
		
		//类型
		@SuppressWarnings("unused")
		Integer type = (Integer) obj[2];
		
		//操作员id
		Long supervisorId = (Long) obj[3];
		
		//投资利息管理费费率
		double managementRate = Bid.queryInvestRate(bid.id);		
 		if(managementRate != 0){
 			managementRate = managementRate / 100;
		}
 		
 		// 获取投资账单列表
 		List<Map<String, Object>> billInvestList = BillInvests.findBillInvestsByBidIdAndPeriods(t_bill.bid_id, t_bill.periods);
 		
		// 初始化遍历参数
		String orderNo = null;			// 订单号
		double transAmt = 0D;			// 交易金额
		String inCustId = null;			// 入账商户号
		String result = null;			// 返回消息
		int count = 0;					// 成功过技术
		// 遍历理财账单转账
		for(Map<String,Object> billInvestMap : billInvestList){
			// 生成订单号 = 前缀  + 用户id + 账单id + 投资id
			orderNo = hfPaymentService.createSpecialBillNo(PayType.ADVANCE, (Long) billInvestMap.get("user_id"), (Long) billInvestMap.get("id"), (Long) billInvestMap.get("investId"));
			//投资人收益
			double pInAmt = (Double)billInvestMap.get("receive_interest") + (Double)billInvestMap.get("receive_corpus") 
					+ (Double)billInvestMap.get("overdue_fine");
			//投资管理费
			double pInFee = BillInvests.getInvestManagerFee((Double)billInvestMap.get("receive_interest"), managementRate, (Long)billInvestMap.get("user_id"));
			// 入账商户号
			inCustId = User.queryIpsAcctNo((Long)billInvestMap.get("user_id"), error);
			// 交易金额
			transAmt = pInAmt - pInFee;
			// 参数组装
			LinkedHashMap<String, String> paramMap = hfPaymentService.doTransfer(orderNo, transAmt, inCustId);
		
			// 请求日志
			hfPaymentService.printRequestData(paramMap, "本金垫付提交参数", PayType.ADVANCE);	
			
			// 提交请求，获取返回参数
			result = HfPaymentUtil.postMethod(HfConstants.CHINAPNR_URL, paramMap, "UTF-8");
			
			//日志打印
			Map<String,String> resultMap = HfPaymentUtil.jsonToMap(result);
			
			hfPaymentService.printData(resultMap, "本金垫付回调参数", PayType.ADVANCE);
			
			// 判断返回码是否成功，成功则+1
			//签名，状态码，仿重单处理;
			HfPaymentUtil.checkSign(resultMap, "本金垫付回调参数", PayType.ADVANCE, error);
			if(error.code >= 0){
				hfPaymentCallBackService.modifyBillInvestMerBillId((Long)billInvestMap.get("id"), orderNo, error);
				// 操作成功，计数+1
				if(error.code >= 0) {
					count++;
				}
			}
		}
		
		// 判断全部转账是否有失败
		if(count != billInvestList.size()){
			error.code = -1;
			error.msg = "有" + (billInvestList.size() - count) + "笔失败";
			
			return null;
		}
		
		// 本金垫付业务逻辑
		hfPaymentCallBackService.advance(supervisorId, bill_id, error);
		
		return null;
	}
	
	@Override
	public Map<String, Object> offlineRepayment(ErrorInfo error, int client, Object... obj) {
		//获取账单信息
		Long bill_id = (Long) obj[0];
		t_bills t_bill = t_bills.findById(bill_id);
		
		//获取借款信息	
		t_bids bid = t_bids.findById(t_bill.bid_id);
		
		//类型,本金垫付或线下收款
		@SuppressWarnings("unused")
		Integer type = (Integer) obj[1];
		
		//操作员id
		Long supervisorId = (Long) obj[2];
		
		//投资利息管理费费率
		double managementRate = Bid.queryInvestRate(bid.id);		
		if(managementRate != 0){
			managementRate = managementRate / 100;
		}
		
		// 获取投资账单列表
		List<Map<String, Object>> billInvestList = BillInvests.findBillInvestsByBidIdAndPeriods(t_bill.bid_id, t_bill.periods);
		
		// 初始化遍历参数
		String orderNo = null;			// 订单号
		double transAmt = 0D;			// 交易金额
		String inCustId = null;			// 入账商户号
		String result = null;			// 返回消息
		int count = 0;					// 成功过技术
		// 遍历理财账单转账
		for(Map<String,Object> billInvestMap : billInvestList){
			// 生成订单号 = 前缀  + 用户id + 账单id + 投资id
			orderNo = hfPaymentService.createSpecialBillNo(PayType.OFFLINE_REPAYMENT, (Long) billInvestMap.get("user_id"), (Long) billInvestMap.get("id"), (Long) billInvestMap.get("investId"));
			//投资人收益
			double pInAmt = (Double)billInvestMap.get("receive_interest") + (Double)billInvestMap.get("receive_corpus") 
					+ (Double)billInvestMap.get("overdue_fine");
			//投资管理费
			double pInFee = BillInvests.getInvestManagerFee((Double)billInvestMap.get("receive_interest"), managementRate, (Long)billInvestMap.get("user_id"));
			// 入账商户号
			inCustId = User.queryIpsAcctNo((Long)billInvestMap.get("user_id"), error);
			// 交易金额
			transAmt = pInAmt - pInFee;
			// 参数组装
			LinkedHashMap<String, String> paramMap = hfPaymentService.doTransfer(orderNo, transAmt, inCustId);
			
			// 请求日志
			hfPaymentService.printRequestData(paramMap, "线下收款提交参数", PayType.OFFLINE_REPAYMENT);	
			
			// 提交请求，获取返回参数
			result = HfPaymentUtil.postMethod(HfConstants.CHINAPNR_URL, paramMap, "UTF-8");
			
			//日志打印
			Map<String,String> resultMap = HfPaymentUtil.jsonToMap(result);
			
			hfPaymentService.printData(resultMap, "线下收款回调参数", PayType.OFFLINE_REPAYMENT);
			
			// 判断返回码是否成功，成功则+1
			//签名，状态码，仿重单处理;
			HfPaymentUtil.checkSign(resultMap, "线下收款回调参数", PayType.OFFLINE_REPAYMENT, error);
			if(error.code >= 0){
				hfPaymentCallBackService.modifyBillInvestMerBillId((Long)billInvestMap.get("id"), orderNo, error);
				// 操作成功，计数+1
				if(error.code >= 0) {
					count++;
				}
			}
		}
		
		// 判断全部转账是否有失败
		if(count != billInvestList.size()){
			error.code = -1;
			error.msg = "有" + (billInvestList.size() - count) + "笔失败";
			
			return null;
		}
		
		// 线下收款业务逻辑
		hfPaymentCallBackService.offlineRepayment(supervisorId, bill_id, error);
		
		return null;
	}

	@Override
	public Map<String, Object> advanceRepayment(ErrorInfo error, int client, Object... obj) {
		//获取账单信息
		Bill bill = (Bill) obj[0];
		//获取借款人信息
		Long userId = (Long) obj[1];	
		t_users user = t_users.findById(userId);
		//获取交易金额信息	
		double transAmt = bill.repaymentCorpus + bill.realRepaymentInterest + bill.overdueFine;
		// 获取订单号
		String ordId = hfPaymentService.createBillNo();
		
		// 参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.usrAcctPay(ordId, user.ips_acct_no, transAmt);
		// 提交入库参数, 用于回调
		paramMap.put("PayType", PayType.ADVANCE_REPAYMENT.name());
		paramMap.put("UMerPriv",UsrAcctPayEnum.ADVANCEREPAYMENT.name());  //支付方式.回调可能多接口是同一个回调地址
		paramMap.put("UBill_id",bill.id+"");  //订单id
		
		// 生成表单
		String html = HfPaymentUtil.createFormHtml(paramMap, HfConstants.CHINAPNR_URL);
		
		// 打印请求参数
		hfPaymentService.printRequestData(paramMap, "垫付还款提交参数", PayType.ADVANCE_REPAYMENT);	
		
		// 提交请求页面
		return HfPaymentReqAction.getInstance().submitForm(html, client);
	}

	@Override
	public Map<String, Object> agentRecharge(ErrorInfo error, int client, Object... obj) {
		String ipsAcctNo = obj[0].toString();  //资金托管账号
		double amount = (Double)obj[1];  //转账金额
		String merOrderNo = obj[2].toString();  //转账流水号
		long agentOrderNo = Long.parseLong(obj[3].toString());  //用户支付流水号
		
		//参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.doTransfer(merOrderNo, amount, ipsAcctNo);
		
		paramMap.put("PayType", PayType.AGENTRECHARGE.name());  //转账类型
		paramMap.put("agentOrderNo", String.valueOf(agentOrderNo));  //转账类型
		
		hfPaymentService.printRequestData(paramMap, "代理充值提交参数", PayType.AGENTRECHARGE);	
		
		//ws请求汇付接口
		String result = HfPaymentUtil.postMethod(HfConstants.CHINAPNR_URL, paramMap, "utf-8");
		
		Map<String,String> resultMap = HfPaymentUtil.jsonToMap(result);
		
		if(Constants.IS_LOCALHOST){
			
			hfPaymentCallBackService.transfer(resultMap, "代理充值WS及时响应", error);
			
		}
		
		return null;
		
	}

	@Override
	public Map<String, Object> applyCredit(ErrorInfo error, int client, Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> applyVIP(ErrorInfo error, int client, Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Map<String, Object> autoRepayment(ErrorInfo error, int client, Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> autoRepaymentSignature(ErrorInfo error, int client, Object... obj) {
		
		return null;
	}

	@Override
	public Map<String, Object> bidAuditFail(ErrorInfo error, int client, Object... obj) {
		
		Bid bid = new Bid();
		bid.id = (Long) obj[0];
		List<t_invests> investList = Invest.queryInvestByBidId(bid.id);
		
		String type = obj[1].toString();
		
		String ordId = "";  //订单号
		String trxId = "";  //冻结订单号
		
		//借款人保证金解冻
		if(StringUtils.isNotBlank(bid.ipsBillNo)){
			ordId = hfPaymentService.createBillNo();
			trxId = bid.ipsBillNo;

			hfPaymentService.doUserUnFreeze(ordId, trxId, "流标解冻保证金请求", "流标解冻保证金回调", error);
			
			if(error.code < 0 && error.code != Constants.ALREADY_RUN){
				
				return null;
			}
		}
		
		//投资人资金解冻
		for(t_invests invest : investList){

			ordId = hfPaymentService.createBillNo();
			
			trxId = invest.ips_bill_no;
			
			hfPaymentService.doUserUnFreeze(ordId, trxId, "流标解冻投资金额请求", "流标解冻投资金额回调", error);
			
			if(error.code < 0 && error.code != Constants.ALREADY_RUN){
				
				return null;
			}
		}
		
		hfPaymentCallBackService.bidAuditFail(type, bid, error);
		
		return null;
	}

	@Override
	public Map<String, Object> bidAuditSucc(ErrorInfo error, int client, Object... obj) {
		
		//标的
		Bid bid = (Bid) obj[0];
		
		//调用解冻保证金接口
		if(bid.bail > 0 && bid.ipsStatus != 3){	
			String freezeTrxId = bid.ipsBillNo;
			String orderNo = hfPaymentService.createBillNo();
			hfPaymentService.doUserUnFreeze(orderNo, freezeTrxId, "放款解冻保证金请求", "放款解冻保证金回调", error);
			
			if(error.code < 0 && error.code != Constants.ALREADY_RUN){
				
				return null;
			}
			
			//将标的修改成，保证金已解冻状态
			JPAUtil.executeUpdate(error, " update t_bids d set d.ips_status = ? where d.id = ? ", 3, bid.id);
			
			if(error.code < 0){
				
				return null;
			}
			
			JPAUtil.transactionCommit();  //保证金已经解冻，下次重复点击时，就不用再解冻了
		}
		//投资记录列表
		List<t_invests> investList = t_invests.find(" bid_id = ? ", bid.id).fetch();
		
		//投资回调时间
		String subOrdDate = DateUtil.simple(new Date());
		//借款人第三方唯一标示
		String inCustId = User.queryIpsAcctNo(bid.userId, error);
		int i =0;
		
		for(t_invests invests : investList){
			
			//订单号
			String ordId = hfPaymentService.createBillNo();
			//投资人第三方唯一标示
			String outCustId = User.queryIpsAcctNo(invests.user_id, error);
			
			//投标流水号，即投标请求第三方的流水号
			String subOrdId = invests.mer_bill_no;
			
			//投资汇付返回的冻结订单号
			String freezeOrdId = invests.ips_bill_no;
			
			//解冻订单号
			String unFreezeOrdId = freezeOrdId + "1";
			
			String reqExt = "{\"ProId\":\""+bid.bidNo+"\"}";
			
			//手续费
			String fee = HfPaymentUtil.formatAmount(invests.bid_fee);
			
			//手续费子账号分配
			JsonArray divDetailAarray = new JsonArray();
			JsonObject divDetailsJson = new JsonObject();
			divDetailsJson.addProperty("DivCustId", HfConstants.MERCUSTID);
			divDetailsJson.addProperty("DivAcctId", HfConstants.SERVFEEACCTID);
			divDetailsJson.addProperty("DivAmt", fee);
			divDetailAarray.add(divDetailsJson);
			String divDetails = divDetailAarray.toString();
			
			
			LinkedHashMap<String, String> paramMaps = hfPaymentService.bidAuditSucc(ordId, outCustId, invests.amount, invests.bid_fee, subOrdId, subOrdDate, inCustId, unFreezeOrdId, freezeOrdId, divDetails, reqExt, bid.bidNo);
			
			// 请求日志
			hfPaymentService.printRequestData(paramMaps, "满标放款提交参数", PayType.LOANS);	
			
			// 提交请求，获取返回参数
			String result = HfPaymentUtil.postMethod(HfConstants.CHINAPNR_URL, paramMaps, "UTF-8");

			Map<String,String> resultMap = HfPaymentUtil.jsonToMap(result);
			
			hfPaymentService.printData(resultMap, "满标放款回调参数", PayType.LOANS);
			//TODO 希望用IO记录放款记录信息,防止数据库回滚.做IO数据库
			String respCode = resultMap.get("RespCode");
			
			if(HfConstants.SUCESSCODE.equals(respCode) || "345".equals(respCode) || "393".equals(respCode)){
				i = i+1;
			}
		}
		
		//执行放款
		if(i == investList.size()){
			Map<String, Double> map = Bid.queryBidAwardAndBidFee(bid, error);
			bid.serviceFees = map.get("bid_fee");
			bid.eaitLoanToRepayment(error);
		}else{
			error.code = -1;
			error.msg = "放款失败"; 
		}
		
		HfPaymentCallBackAction.getInstance().bidAuditSuccWS(error);
		
		return null;
	}

	@Override
	public Map<String, Object> bidDataAudit(ErrorInfo error, int client, Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> debtorTransfer(ErrorInfo error, int client, Object... obj) {
		Long debtId = (Long) obj[0];
		Map<String, Object> map = Debt.queryTransferInfo(debtId, error);
		t_bids bid = (t_bids) map.get("bid");
		
		String sellCustId = User.queryIpsAcctNo(Long.valueOf(map.get("fromUserId")+""), error);
		double cretAmt2 = Convert.strToDouble(map.get("pCretAmt2").toString(), 0.00);//转让金额（只算本金），债权转让金额不可超过可转让金额（即投资金额）
		double creditDealAmt = Convert.strToDouble(map.get("pPayAmt").toString(), 0.00);//成交金额
		double fee = Convert.strToDouble(map.get("managefee").toString(), 0.00) ;//转让方手续费
		String bidOrdId = String.valueOf(map.get("pCreMerBillNo"));
		String bidOrdDate = String.valueOf(map.get("orderDate"));
		String borrowerCustId = User.queryIpsAcctNo(bid.user_id, error);;
		double printAmt = Convert.strToDouble(map.get("printAmt").toString(), 0.00);  //已还金额(不包含线下收款和本金垫付)
		String proId = bid.bid_no;  //借款标编号
		
		
		/**
		 * CreditAmt：转让金额，债权转让转出的本金。
		 * 
		 * 
		 * BidDetails：
		 * 		BidOrdId:被转让的投标订单号。也就是投资流水号：map.get("pCreMerBillNo")
		 * 		BidOrdDate：被转让的投标订单日期。也就是投标时间：map.get("orderDate")
		 * 		BidCreditAmt：转让金额，BidDetails中的转让金额总和等于CreditAmt。债权面额：map.get("pCretAmt2")
		 * 
		 * BorrowerDetails：
		 * 		BorrowerCustId：借款人客户号，借款人的托管账号
		 * 		BorrowerCreditAmt：明细转让金额，BorrowerDetails中的明细转让金额的总和等于BidCreditAmt，债权面额：map.get("pCretAmt2")
		 * 		PrinAmt：已还金额。
		 * 		ProId：ProdId是项目标的唯一标识。借款标编号
		 */
		
		
		String bidDetails = "{\"BidDetails\":[{\"BidOrdId\":\""+bidOrdId+"\","
				+ "\"BidOrdDate\":\""+bidOrdDate+"\",\"BidCreditAmt\":\""+HfPaymentUtil.formatAmount(cretAmt2)+"\","
				+ "\"BorrowerDetails\":[{\"BorrowerCustId\":\""+borrowerCustId+"\","
						+ "\"BorrowerCreditAmt\":\""+HfPaymentUtil.formatAmount(cretAmt2)+"\",\"PrinAmt\":\""+ HfPaymentUtil.formatAmount(printAmt) +"\","
								+ "\"ProId\":\""+proId+"\"}]}]}";
		
		String buyCustId = User.queryIpsAcctNo(Long.valueOf(map.get("toUserId")+""), error);
		String ordId = hfPaymentService.createBillNo();
		
		//组装参数
		Map<String,String> maps = hfPaymentService.debtorTransfer(sellCustId, cretAmt2, creditDealAmt, bidDetails, fee, buyCustId, ordId);
		
		//表单提交页面
		String html = HfPaymentUtil.createFormHtml(maps, HfConstants.CHINAPNR_URL);
		
		//回调所需参数
		maps.put("debtId", String.valueOf(debtId)); //供回调使用
		maps.put("bidOrdId", bidOrdId); //供回调使用 - 用于将投资流水号更新至新记录
		
		hfPaymentService.printRequestData(maps, "债权转让提交参数", PayType.DEBTOR_TRANSFER);
		
		HfPaymentReqAction.getInstance().submitForm(html, client);
		return null;
	}

	@Override
	public Map<String, Object> debtorTransferConfirm(ErrorInfo error, int client,
			LinkedList<Map<String, String>> pDetails, String pBidNo, String parentOrderno, String debtId,
			String dealpwd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> grantCps(ErrorInfo error, int client, Object... obj) {
		
		double amount = (Double)obj[0];
		long userCpsIncomeId = (Long) obj[1];
		long userId = (Long)obj[2];
		
		// 入账用户商户号
		String inCustId = User.queryIpsAcctNo(userId, error);
		// 生成流水号
		String orderNo = hfPaymentService.createBillNo();
		// 参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.doTransfer(orderNo, amount, inCustId);
		// 提交入库参数, 用于回调
		paramMap.put("UUserCpsIncomeId", userCpsIncomeId+"");
		paramMap.put("UsrCustId", inCustId);
		paramMap.put("PayType", PayType.GRANTCPS.name());
		
		// 请求日志
		hfPaymentService.printRequestData(paramMap, "cps推广提交参数", PayType.GRANTCPS);	
		
		// 提交请求，获取返回参数
		String result = HfPaymentUtil.postMethod(HfConstants.CHINAPNR_URL, paramMap, "UTF-8");
		
		//日志打印
		Map<String,String> resultMap = HfPaymentUtil.jsonToMap(result);
		hfPaymentService.printData(resultMap, "cps推广回调参数", PayType.GRANTCPS);
		
		// 生产环境下，同步方法不走逻辑，只由异步走逻辑，但由于本地无法执行异步方法，为了方便测试，若配置为本地环境，添加如下代码走完逻辑
		if(Constants.IS_LOCALHOST){
			hfPaymentCallBackService.transfer(resultMap, "cps推广同步响应", error);
		}
		
		return null;
	}

	@Override
	public Map<String, Object> grantInvitation(ErrorInfo error, int client, Object... obj) {

		double amount = (Double)obj[0];
		long invitedIncomeId = (Long) obj[1];
		String inCustId = obj[2].toString();

		// 生成流水号
		String orderNo = hfPaymentService.createBillNo();
		// 参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.doTransfer(orderNo, amount, inCustId);
		// 提交入库参数, 用于回调
		paramMap.put("invitedIncomeId", invitedIncomeId + "");
		paramMap.put("UsrCustId", inCustId);
		paramMap.put("PayType", PayType.GRANT_INVITATION.name());
		
		// 请求日志
		hfPaymentService.printRequestData(paramMap, "佣金发放提交参数", PayType.GRANT_INVITATION);	
		
		// 提交请求，获取返回参数
		String result = HfPaymentUtil.postMethod(HfConstants.CHINAPNR_URL, paramMap, "UTF-8");
		
		//日志打印
		Map<String,String> resultMap = HfPaymentUtil.jsonToMap(result);
		hfPaymentService.printData(resultMap, "佣金发放回调参数", PayType.GRANT_INVITATION);
		
		// 生产环境下，同步方法不走逻辑，只由异步走逻辑，但由于本地无法执行异步方法，为了方便测试，若配置为本地环境，添加如下代码走完逻辑
		if(Constants.IS_LOCALHOST){
			hfPaymentCallBackService.transfer(resultMap, "佣金发放回调参数", error);
		}
		
		return null;
	}

	

	@Override
	public Map<String, Object> loginAccount(ErrorInfo error, int client, Object... obj) {
		
		User user = (User) obj[0];
		String usrCustId = user.ipsAcctNo;
		
		//参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.loginAccount(usrCustId);
		
		//生成表单
		String html = HfPaymentUtil.createFormHtml(paramMap, HfConstants.CHINAPNR_URL);
		
		hfPaymentService.printRequestData(paramMap, "用户登录提交参数", PayType.LOGIN_ACCOUNT);	
		
		return HfPaymentReqAction.getInstance().submitForm(html, client);
	}

	@Override
	public Map<String, Object> merWithdrawal(ErrorInfo error, int client, Object... obj) {
		// 生成流水号
		String orderNo = hfPaymentService.createBillNo();
		// 参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.merWithdrawal(orderNo, HfConstants.MERCUSTID,
				(Double) obj[0], 0.00);
		// 生成表单
		String html = HfPaymentUtil.createFormHtml(paramMap, HfConstants.CHINAPNR_URL);

		// 提交入库参数, 用于回调
		paramMap.put("cashType", CashEnum.MERCHANT + ""); // 回调需要参数:提现类型

		hfPaymentService.printRequestData(paramMap, "商户提现提交参数", PayType.WITHDRAW);

		return HfPaymentReqAction.getInstance().submitForm(html, client);
	}

	@Override
	public Map<String, Object> merchantRecharge(ErrorInfo error, int client, Object... obj) {
		// 生成流水号
		String orderNo = hfPaymentService.createBillNo();
		// 参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.merchantRecharge(orderNo, HfConstants.MERCUSTID,
				(Double) obj[0]);
		// 生成表单
		String html = HfPaymentUtil.createFormHtml(paramMap, HfConstants.CHINAPNR_URL);

		hfPaymentService.printRequestData(paramMap, "商户充值提交参数", PayType.AGENTRECHARGE);

		return HfPaymentReqAction.getInstance().submitForm(html, client);
	}

	@Override
	public Map<String, Object> queryAmount(ErrorInfo error, int client, Object... obj) {
		
		User user = (User) obj[0];
		
		if(StringUtils.isBlank(user.ipsAcctNo)){
			
			return null;
		}
		
		// 参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.queryAmount(user.ipsAcctNo);
		
		hfPaymentService.printRequestData(paramMap, "用户账户余额余额查询提交参数", PayType.QUERY_AMOUNT);
		
		// ws请求汇付接口
		String result = HfPaymentUtil.postMethod(HfConstants.CHINAPNR_URL, paramMap, "utf-8");

		Map<String, String> resultMap = HfPaymentUtil.jsonToMap(result);
		
		hfPaymentService.printData(resultMap, "用户账户余额余额查询及时响应", PayType.QUERY_AMOUNT);
		
		HfPaymentUtil.checkSign(resultMap, "用户账户余额余额查询", PayType.QUERY_AMOUNT, error);
		
		if(error.code < 0){
			
			return null;
		}
		
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put("pBlance", resultMap.get("AvlBal").replace(",", ""));  //用户可用余额
		maps.put("pFreeze", resultMap.get("FrzBal").replace(",", ""));  //用户冻结金额
		return maps;
	}

	@Override
	public Map<String, Object> queryAmountByMerchant(ErrorInfo error, int client, Object... obj) {
		// 参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.queryAmountByMerchant(HfConstants.MERCUSTID);
		// ws请求汇付接口
		String result = HfPaymentUtil.postMethod(HfConstants.CHINAPNR_URL, paramMap, "utf-8");

		Map<String, String> resultMap = HfPaymentUtil.jsonToMap(result);
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put("AcctBal", resultMap.get("AcctBal").replace(",", ""));
		maps.put("AvlBal", resultMap.get("AvlBal").replace(",", ""));
		maps.put("FrzBal", resultMap.get("FrzBal").replace(",", ""));
		maps.put("UsrCustId", resultMap.get("UsrCustId"));
		return maps;
	}
	
	/**
	 * 查询充值对账
	 * @param error
	 * @param client
	 * @param obj
	 * @return
	 */
	public Map<String, Object> querySaveReconciliation(ErrorInfo error, int client, Object... obj) {
		Date beginTime = (Date) obj[0];
		Date endTime = (Date) obj[1];
		int pageSize = Integer.parseInt(String.valueOf(obj[2]));
		int pageNum = Integer.parseInt(String.valueOf(obj[3]));
		// 参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.querySaveReconciliation(beginTime, endTime, pageSize,
				pageNum);
		// ws请求汇付接口
		String result = HfPaymentUtil.postMethod(HfConstants.CHINAPNR_URL, paramMap, "utf-8");

		Map<String, String> resultMap = HfPaymentUtil.jsonToMap(result);
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put("AcctBal", resultMap.get("AcctBal").replace(",", ""));
		maps.put("AvlBal", resultMap.get("AvlBal").replace(",", ""));
		maps.put("FrzBal", resultMap.get("FrzBal").replace(",", ""));
		maps.put("UsrCustId", resultMap.get("UsrCustId"));
		return maps;
	}

	@Override
	public List<Map<String, Object>> queryBanks(ErrorInfo error, int client, Object... obj) {
		return HfConstants.getBankList();
	}

	@Override
	public Map<String, Object> queryBindedBankCard(ErrorInfo error, int client, Object... obj) {
		String usrCustId = obj[0].toString();
		// 参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.queryBindedBankCard(usrCustId);
		
		hfPaymentService.printRequestData(paramMap, "查询用户绑定银行卡列表提交参数", PayType.QUERY_BINDED_CARDS);
		
		// ws请求汇付接口
		String result = HfPaymentUtil.postMethod(HfConstants.CHINAPNR_URL, paramMap, "utf-8");
		
		hfPaymentService.printData(HfPaymentUtil.jsonToMap(result), "查询用户绑定银行卡列表回调参数", PayType.QUERY_BINDED_CARDS);

		Map<String, Object> map = new HashMap<String, Object>();
		
		JSONObject json = JSONObject.fromObject(result);

		if (!"000".equals(json.getString("RespCode"))) {
			map.put("result", "");

			return map;
		}

		JSON cardList = (JSON) json.get("UsrCardInfolist");

		if (cardList == null) {
			map.put("result", "");

			return map;
		}

		if (cardList.getClass().isAssignableFrom(JSONArray.class)) {
			if (((JSONArray) cardList).size() == 0) {
				map.put("result", "");
			} else {
				map.put("result", (JSONArray) cardList);
			}

			return map;
		}

		map.put("result", "");
		return map;
	}

	@Override
	public Map<String, Object> queryLog(ErrorInfo error, int client, Object... obj) {
		// TODO Auto-generated method stub
		return null;
	}

	

	

	@Override
	public Map<String, Object> repayment(ErrorInfo error, int client, Object... obj) {
		//获取账单信息
		Bill bill = (Bill) obj[0];				
		// 获取借款标信息
		Bid bid = new Bid();
		bid.id = bill.bidId;
		// 获取借款人信息
		User borrower = new User();
		borrower.id = bid.userId;
		// 获取投资利息管理费费率
		double managementRate = Bid.queryInvestRate(bid.id);		
 		if(managementRate != 0){
 			managementRate = managementRate / 100;
		}
		
 		// 获取理财账单列表
 		List<t_bill_invests> list = t_bill_invests.find(" bid_id = ? and periods = ? and mer_bill_no is not null and status not in (0,-3,-7,-4) ", bill.bidId, bill.periods).fetch();
 		// 拼接接口所需的JSON格式理财账单列表
 		String inDetails = hfPaymentService.generateBatchRepaymentInDetails(list, managementRate);
 		// 获取出账商户号
 		String outCustId = User.queryIpsAcctNo(bid.userId, error);
 		// 获取订单号
		String batchId = hfPaymentService.createBillNo();
		// 获取操作时间
		String merOrdDate = DateUtil.simple(new Date());
 		// 获取借款标发布流水号
		String proId = bid.bidNo;
		
		// 参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.repayment(outCustId, batchId, merOrdDate, inDetails, proId);
		// 提交入库参数, 用于回调
		paramMap.put("UBillId", bill.id+"");
		paramMap.put("UUserId", bid.userId + "");
		paramMap.put("UsrCustId", outCustId);
		paramMap.put("OrdId", batchId);

		// 请求日志
		hfPaymentService.printRequestData(paramMap, "还款请求提交参数", PayType.REPAYMENT);	
		
		// 提交请求，获取返回参数
		String result = HfPaymentUtil.postMethod(HfConstants.CHINAPNR_URL, paramMap, "UTF-8");

		// 日志打印
		Map<String,String> resultMap = HfPaymentUtil.jsonToMap(result);
		hfPaymentService.printData(resultMap, "还款同步回调参数", PayType.REPAYMENT);
		
		// 调用回调控制层，由回调控制层跳转响应页面
		hfPaymentCallBackService.batchRepayment(resultMap, "批量还款同步回调", error);
		
		return null;
	}

	@Override
	public Map<String, Object> userBindCard(ErrorInfo error, int client, Object... obj) {
		
		User user = User.currUser();
		//生成流水号
		String orderNo = hfPaymentService.createBillNo();
		//参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.userBindCard(user.ipsAcctNo, orderNo);
			
		//生成表单
		String html = HfPaymentUtil.createFormHtml(paramMap, HfConstants.CHINAPNR_URL);
		
		//提交入库参数, 用于回调
		paramMap.put("userId", String.valueOf(user.getId())); //回调需要参数:用户id
		
		hfPaymentService.printRequestData(paramMap, "用户绑卡提交参数", PayType.USER_BIND_CARD);	
		
		HfPaymentReqAction.getInstance().submitForm(html, client);
		
		return null;
	}

	@Override
	public Map<String, Object> withdraw(ErrorInfo error, int client, Object... obj) {
		
		User user = User.currUser();
		String withdrawalId = String.valueOf(obj[0]);
		double tranAmount = (Double)obj[1];
		double serviceFee = User.withdrawalFee(tranAmount);
		
		//生成流水号
		String orderNo = hfPaymentService.createBillNo();
		
		//参数组装
		LinkedHashMap<String, String> paramMap = hfPaymentService.withdraw(user.ipsAcctNo, orderNo, tranAmount, serviceFee);
			
		//生成表单
		String html = HfPaymentUtil.createFormHtml(paramMap, HfConstants.CHINAPNR_URL);
		
		//提交入库参数, 用于回调
		paramMap.put("userId", String.valueOf(user.getId())); //回调需要参数:用户id
		paramMap.put("withdrawalId", withdrawalId);
		paramMap.put("tranAmount", tranAmount+"");
		paramMap.put("serviceFee", serviceFee+"");
		
		hfPaymentService.printRequestData(paramMap, "用户提现提交参数", PayType.WITHDRAW);	
		
		HfPaymentReqAction.getInstance().submitForm(html, client);
		
		return null;
	}

}
