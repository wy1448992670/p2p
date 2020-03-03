package payment.fy.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import models.t_bill_invests;
import models.t_bills;
import models.t_invests;
import models.t_users;

import org.json.XML;

import payment.HttpUtil;
import payment.PaymentBaseService;
import payment.fy.util.FyConstants;
import payment.fy.util.FyPaymentUtil;
import utils.ErrorInfo;
import business.Bid;
import business.Bill;
import business.BillInvests;
import business.User;
import constants.Constants;
import constants.PayType;
import controllers.BaseController;

public class FyPaymentImpl {

	/**
	 * 注册开户接口
	 * @param cust_nm 客户姓名
	 * @param certif_id 身份证号
	 * @param mobile_no 手机号
	 * @param email 邮箱地
	 * @param city_id 开户行地区代码
	 * @param parent_bank_id 开户行行别
	 * @param bank_nm 开户行支行名称
	 * @param capAcntNo 户名
	 * @param rem 备注
	 * @return
	 * @throws Exception 
	 */
	public  static Map<String, String> register(ErrorInfo error, String cust_nm, String certif_id, String mobile_no, String email, String city_id, String parent_bank_id, String bank_nm, String capAcntNo) throws Exception{
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		
		String orderno = FyPaymentUtil.createBillNo();
		
		xmlMap.put("mchnt_cd", FyConstants.mchnt_cd);//企业代码
		xmlMap.put("mchnt_txn_ssn", orderno);//流水号
		xmlMap.put("capAcntNm", "");//流水号	
		xmlMap.put("password", "");//提现密码
		xmlMap.put("lpassword", "");//登录密码
		xmlMap.put("cust_nm", cust_nm);//客户姓名
		xmlMap.put("certif_id", certif_id);//身份证号
		xmlMap.put("mobile_no", mobile_no);//手机号
		xmlMap.put("email", email);//邮箱
		xmlMap.put("city_id", city_id);//开户行地区代码
		xmlMap.put("parent_bank_id", parent_bank_id);//开户行行别
		xmlMap.put("bank_nm", bank_nm);//开户行支行名称
		xmlMap.put("capAcntNo", capAcntNo);//提现银行卡账号
		xmlMap.put("rem", "开户");//备注
		xmlMap.put("signature", FyPaymentUtil.createSign(xmlMap));
						
		LinkedHashMap<String, String> temp = new LinkedHashMap<String, String>();
		temp.putAll(xmlMap);
		temp.put("out_cust_no", mobile_no);
		
		FyPaymentUtil.printRequestData(xmlMap, "开户提交参数", PayType.REGISTER.name(), true);
		
		String data = HttpUtil.postMethod(FyConstants.post_url + FyConstants.register, xmlMap, "UTF-8");
		
		Map<String, String> dataMap = FyPaymentUtil.parseXmlToMap(data, error);
		
		FyPaymentUtil.printData(dataMap, "开户回调参数", PayType.REGISTER.name(), true);
		
		FyPaymentUtil.checkSign(error, dataMap, "开户", PayType.REGISTER.name(), true);	
		
		if(error.code < 0){
			return null;
		}
		return dataMap;		
	}
	/**
	 * 充值
	 * @param error
	 * @param pIpsAcctNo 第三方唯一标示
	 * @param pTrdAmt 充值金额
	 * @param pTrdAmt 充值流水号
	 * @return
	 */
	public static LinkedHashMap<String, String> recharge(ErrorInfo error, String pIpsAcctNo, double pTrdAmt, String pMerBillNo){
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		xmlMap.put("mchnt_cd", FyConstants.mchnt_cd);
		xmlMap.put("mchnt_txn_ssn", pMerBillNo);
		xmlMap.put("amt", FyPaymentUtil.formatAmountToFen(pTrdAmt));
		xmlMap.put("login_id", pIpsAcctNo);
		xmlMap.put("page_notify_url", BaseController.getBaseURL()+ "payment/fy/returnRecharge");
		xmlMap.put("back_notify_url", BaseController.getBaseURL()+ "payment/fy/returnRecharge");
		xmlMap.put("signature", FyPaymentUtil.createSign(xmlMap));
		return xmlMap;
	}
	
	/**
	 * 提现
	 * @param error
	 * @param pIpsAcctNo 第三方唯一标示
	 * @param pTrdAmt 充值金额
	 * @param pTrdAmt 充值流水号
	 * @return
	 */
	public static LinkedHashMap<String, String> withdraw(ErrorInfo error, String pIpsAcctNo, double amt, String pMerBillNo){
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		xmlMap.put("mchnt_cd", FyConstants.mchnt_cd);
		xmlMap.put("mchnt_txn_ssn", pMerBillNo);
		xmlMap.put("login_id", pIpsAcctNo);
		xmlMap.put("amt", FyPaymentUtil.formatAmountToFen(amt));
		xmlMap.put("page_notify_url", BaseController.getBaseURL()+ "payment/fy/returnWithdraw");
		xmlMap.put("back_notify_url", BaseController.getBaseURL()+ "payment/fy/returnWithdraw");
		xmlMap.put("signature", FyPaymentUtil.createSign(xmlMap));
		return xmlMap;
	}
	
	/**
	 * 用户信息修改接口
	 * @param cust_nm 客户姓名
	 * @param certif_id 身份证号
	 * @param mobile_no 手机号
	 * @param email 邮箱地
	 * @param city_id 开户行地区代码
	 * @param parent_bank_id 开户行行别
	 * @param bank_nm 开户行支行名称
	 * @return
	 * @throws Exception 
	 */
	public  static Map<String, String> modifyUserInf(ErrorInfo error, String cust_nm, String certif_id, String mobile_no, String email, String city_id, String parent_bank_id, String bank_nm, String capAcntNo) throws Exception{
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		String orderno = FyPaymentUtil.createBillNo();
		xmlMap.put("mchnt_cd", FyConstants.mchnt_cd);//企业代码
		xmlMap.put("mchnt_txn_ssn", orderno);//流水号
		xmlMap.put("certif_id", certif_id);//身份证号
		xmlMap.put("mobile_no", mobile_no);//手机号
		xmlMap.put("email", email);//邮箱
		xmlMap.put("city_id", city_id);//开户行地区代码
		xmlMap.put("parent_bank_id", parent_bank_id);//开户行行别
		xmlMap.put("bank_nm", bank_nm);//开户行支行名称
		xmlMap.put("capAcntNo", capAcntNo);//提现银行卡账号
		xmlMap.put("signature", FyPaymentUtil.createSign(xmlMap));
		
		FyPaymentUtil.printRequestData(xmlMap, "用户信息修改提交参数", PayType.REGISTER.name(), true);
				
		String data = HttpUtil.postMethod(FyConstants.post_url+FyConstants.modifyUserInf, xmlMap, "UTF-8");
		Map<String, String> dataMap = FyPaymentUtil.parseXmlToMap(data, error);
	
		FyPaymentUtil.printData(dataMap, "用户信息修改回调参数", PayType.REGISTER.name(), true);
		
		FyPaymentUtil.checkSign(error, dataMap, "用户信息修改", PayType.RECHARGE.name(), true);			
		if(error.code < 0){
			return null;
		}		
		return dataMap;		
	}
	
	
	/**
	 * 预授权接口(投标，自动投标)
	 * @param error
	 * @param out_cust_no 出账账户
	 * @param in_cust_no 入账账户
	 * @param amount 金额
	 * @param orderno 流水号
	 * @param rem 备注
	 * @param type 接口类型
	 * @return
	 * @throws Exception
	 */
	public static Map<String,String> preAuth(ErrorInfo error, String out_cust_no, String in_cust_no, double amount, String orderno, String rem, String type) throws Exception{
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		

		xmlMap.put("mchnt_cd", FyConstants.mchnt_cd);//企业代码
		xmlMap.put("mchnt_txn_ssn", orderno);//流水号
		xmlMap.put("out_cust_no", out_cust_no);//出账账户
		xmlMap.put("in_cust_no", in_cust_no);//进账账户
		xmlMap.put("amt", FyPaymentUtil.formatAmountToFen(amount));//预授权金额
		xmlMap.put("rem", rem);//备注
		xmlMap.put("signature", FyPaymentUtil.createSign(xmlMap));
		
		FyPaymentUtil.printRequestData(xmlMap, rem+"提交参数", type, true);
		
		String data = HttpUtil.postMethod(FyConstants.post_url + FyConstants.preAuth, xmlMap, "UTF-8");
		Map<String, String> dataMap = FyPaymentUtil.parseXmlToMap(data, error);
		
		FyPaymentUtil.printData(dataMap, rem+"回调参数", type, true);
		
		FyPaymentUtil.checkSign(error, dataMap, rem, type, true);			

		if(error.code < 0){
			return null;
		}
		
		return dataMap;
		
	}
		
	/**
	 * 预授权撤销接口
	 * @param error
	 * @param out_cust_no 投资人第三方唯一标示
	 * @param in_cust_no 借款人第三方唯一标示
	 * @param amount 解冻金额
	 * @param contract_no 合同号（投资时返回的第三方流水号）
	 * @param orderno 本次请求的流水号
	 * @param rem 备注
	 * @param type 接口类型
	 * @return
	 * @throws Exception
	 */
	public static Map<String,String> preAuthCancel(ErrorInfo error, String out_cust_no,String in_cust_no,double amount,String contract_no,String orderno,String rem, String type) throws Exception{
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();

		xmlMap.put("mchnt_cd", FyConstants.mchnt_cd);//企业代码
		xmlMap.put("mchnt_txn_ssn", orderno);//流水号
		xmlMap.put("out_cust_no", out_cust_no);//出账账户
		xmlMap.put("in_cust_no", in_cust_no);//进账账户
		xmlMap.put("contract_no", contract_no);//预授权合同号
		xmlMap.put("rem", rem);//备注
		xmlMap.put("signature", FyPaymentUtil.createSign(xmlMap));
		FyPaymentUtil.printRequestData(xmlMap, rem+"提交参数", type, false);
	
		String data = HttpUtil.postMethod(FyConstants.post_url+FyConstants.preAuthCancel, xmlMap, "UTF-8");
		
		Map<String, String> dataMap = FyPaymentUtil.parseXmlToMap(data, error);
		FyPaymentUtil.printData(dataMap, rem+"回调参数", type, false);
		
		FyPaymentUtil.checkSign(error, dataMap, rem, type, false);			

		if(error.code < 0){
			return null;
		}
		
		return dataMap;
		
	}
	
		
	/**
	 * 满标放款
	 * @param error
	 * @param out_cust_no 投资人唯一标示
	 * @param in_cust_no 借款人唯一标示
	 * @param amount 借款金额
	 * @param contract_no 投资时返回的流水号
	 * @param orderno 流水号 (BS+投资人流水号)
	 * @param rem 备注
	 * @param type 接口类型
	 * @return
	 * @throws Exception
	 */
	public static Map<String,String> bidAuditSucc (ErrorInfo error, String out_cust_no, String in_cust_no, double amount, String contract_no, String orderno, String rem, String type) throws Exception{
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();

		xmlMap.put("mchnt_cd", FyConstants.mchnt_cd);//企业代码
		xmlMap.put("mchnt_txn_ssn", orderno);//流水号
		xmlMap.put("out_cust_no", out_cust_no);//出账账户
		xmlMap.put("in_cust_no", in_cust_no);//进账账户
		xmlMap.put("contract_no", contract_no);//预授权合同号
		xmlMap.put("amt", FyPaymentUtil.formatAmountToFen(amount));//转账金额
		xmlMap.put("rem", rem);//备注
		xmlMap.put("signature", FyPaymentUtil.createSign(xmlMap));
		
		FyPaymentUtil.printRequestData(xmlMap, rem+"提交参数", type, false);
		
		String data = HttpUtil.postMethod(FyConstants.post_url+FyConstants.transferBu, xmlMap, "UTF-8");
		Map<String, String> dataMap = FyPaymentUtil.parseXmlToMap(data, error);
		
		FyPaymentUtil.printData(dataMap, rem+"回调参数", type, false);
		
		FyPaymentUtil.checkSign(error, dataMap, rem, type, false);			

		if(error.code < 0){
			return null;
		}
		return dataMap;
		
	}
	
	/**
	 * 余额查询
	 * @param error
	 * @param cust_no 第三方唯一标示
	 * @param rem 备注
	 * @return
	 * @throws Exception
	 */
	public static Map<String,String> queryAmount (ErrorInfo error, String cust_no, String rem) throws Exception{
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		Date dt = new Date();
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String mchnt_txn_dt = df.format(dt);
		xmlMap.put("mchnt_cd", FyConstants.mchnt_cd);//企业代码
		xmlMap.put("mchnt_txn_ssn", FyPaymentUtil.createBillNo());//流水号
		xmlMap.put("mchnt_txn_dt", mchnt_txn_dt);//交易日期
		xmlMap.put("cust_no", cust_no);//待查询的登录帐户
		xmlMap.put("signature", FyPaymentUtil.createSign(xmlMap));
		
		FyPaymentUtil.printRequestData(xmlMap, rem+"提交参数", PayType.QUERY_AMOUNT.name(), false);
		
		String data = HttpUtil.postMethod(FyConstants.post_url+FyConstants.balanceAction, xmlMap, "UTF-8");
		
		Map<String, String> dataMap = FyPaymentUtil.parseXmlToMap(data, error);
		
		FyPaymentUtil.printData(dataMap, rem+"回调参数", PayType.QUERY_AMOUNT.name(), false);
		
		FyPaymentUtil.checkSign(error, dataMap, rem, PayType.QUERY_AMOUNT.name(), false);	
		
		if(error.code < 0){
			return null;
		}
		org.json.JSONObject plain = XML.toJSONObject(dataMap.get("plain"));
		org.json.JSONObject plain1 = plain.getJSONObject("plain");
		org.json.JSONObject results = plain1.getJSONObject("results");
		org.json.JSONObject result = results.getJSONObject("result");
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("ca_balance", FyPaymentUtil.formatAmountToYuan(new Double(result.getString("ca_balance"))));
		map.put("cf_balance", FyPaymentUtil.formatAmountToYuan(new Double(result.getString("cf_balance"))));
		return map;
	}
	
	/**
	 * 划拨(个人与个人) (应用:还款，投标奖励)
	 * @param out_cust_no 出账账户
	 * @param in_cust_no 进账账户
	 * @param amount  预授权金额
	 * @return
	 * @throws Exception
	 */
	public static Map<String,String> transferBu (ErrorInfo error, String out_cust_no,String in_cust_no,double amount,String orderno, String rem, String type) throws Exception{
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		xmlMap.put("mchnt_cd", FyConstants.mchnt_cd);//企业代码
		xmlMap.put("mchnt_txn_ssn", orderno);//流水号
		xmlMap.put("out_cust_no", out_cust_no);//出账账户
		xmlMap.put("in_cust_no", in_cust_no);//进账账户
		xmlMap.put("contract_no", "");//预授权合同号
		xmlMap.put("amt", FyPaymentUtil.formatAmountToFen(amount));//划拨金额
		xmlMap.put("rem", rem);//备注
		xmlMap.put("signature", FyPaymentUtil.createSign(xmlMap));
		
		FyPaymentUtil.printRequestData(xmlMap, rem+"提交参数", type, false);

		String data = HttpUtil.postMethod(FyConstants.post_url+FyConstants.transferBu, xmlMap, "UTF-8");
		
		Map<String, String> dataMap = FyPaymentUtil.parseXmlToMap(data, error);
		
		FyPaymentUtil.printData(dataMap, rem+"回调参数", type, false);
		
		FyPaymentUtil.checkSign(error, dataMap, rem, type, false);			

		if(error.code < 0){
			return null;
		}
		
		return dataMap;	
	}
	
	/**
	 * 划拨(商户与个人) (接款管理费，理财管理费，cps奖励发放等各项杂费及商户的各项奖励)
	 * @param out_cust_no 出账账户
	 * @param in_cust_no 进账账户
	 * @param amount  预授权金额
	 * @param contract_no 预授权合同号
	 * @return
	 * @throws Exception
	 */
	public static Map<String,String> transferBmu (ErrorInfo error, String out_cust_no,String in_cust_no,double amount,String orderno, String rem, String type) throws Exception{
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();

		xmlMap.put("mchnt_cd", FyConstants.mchnt_cd);//企业代码
		xmlMap.put("mchnt_txn_ssn", orderno);//流水号
		xmlMap.put("out_cust_no", out_cust_no);//出账账户
		xmlMap.put("in_cust_no", in_cust_no);//进账账户
		xmlMap.put("contract_no", "");//预授权合同号
		xmlMap.put("amt", FyPaymentUtil.formatAmountToFen(amount));//划拨金额
		xmlMap.put("rem", rem);//备注
		xmlMap.put("signature", FyPaymentUtil.createSign(xmlMap));
		
		FyPaymentUtil.printRequestData(xmlMap, rem+"提交参数", type, false);

		String data = HttpUtil.postMethod(FyConstants.post_url+FyConstants.transferBmu, xmlMap, "UTF-8");
		
		Map<String, String> dataMap = FyPaymentUtil.parseXmlToMap(data, error);
		
		FyPaymentUtil.printData(dataMap, rem+"回调参数", type, false);
		
		FyPaymentUtil.checkSign(error, dataMap, rem, type, false);			

		if(error.code < 0){
			return null;
		}
		
		return dataMap;	
	}
	
	/**
	 * 冻结
	 * @param error
	 * @param cust_no 借款人ips账户
	 * @param amount 冻结金额
	 * @param orderno 冻结流水号
	 * @param rem 冻结保证金
	 * @param type 接口类型
	 * @return
	 * @throws Exception
	 */
	public static Map<String,String> freeze(ErrorInfo error, String cust_no, double amount, String orderno, String rem, String type) throws Exception{
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		
		xmlMap.put("mchnt_cd", FyConstants.mchnt_cd);
		xmlMap.put("mchnt_txn_ssn", orderno);
		xmlMap.put("cust_no", cust_no);
		xmlMap.put("amt", FyPaymentUtil.formatAmountToFen(amount));
		xmlMap.put("rem", rem);
		xmlMap.put("signature", FyPaymentUtil.createSign(xmlMap));
		
		
		LinkedHashMap<String, String> temp = new LinkedHashMap<String, String>();
		temp.putAll(xmlMap);
		temp.put("out_cust_no", cust_no);
		FyPaymentUtil.printRequestData(temp, rem+"提交参数", type, true);
		
		String data = HttpUtil.postMethod(FyConstants.post_url + FyConstants.freeze, xmlMap, "UTF-8");
		Map<String, String> dataMap = FyPaymentUtil.parseXmlToMap(data, error);
		
		FyPaymentUtil.printData(dataMap, rem+"回调参数", type, true);
		
		FyPaymentUtil.checkSign(error, dataMap, rem, type, true);			

		if(error.code < 0){
			return null;
		}
		
		return dataMap;
		
	}
	/**
	 * 解冻保证金
	 * @param error
	 * @param cust_no 借款人ips账户
	 * @param amount 冻结金额
	 * @param orderno 冻结流水号
	 * @param rem 冻结保证金
	 * @param type 接口类型
	 * @return
	 * @throws Exception
	 */
	public static Map<String,String> unFreeze(ErrorInfo error, String cust_no, double amount, String orderno, String rem, String type) throws Exception{
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		
		xmlMap.put("mchnt_cd", FyConstants.mchnt_cd);
		xmlMap.put("mchnt_txn_ssn", orderno);
		xmlMap.put("cust_no", cust_no);
		xmlMap.put("amt", FyPaymentUtil.formatAmountToFen(amount));
		xmlMap.put("rem", rem);
		xmlMap.put("signature", FyPaymentUtil.createSign(xmlMap));
		
		
		LinkedHashMap<String, String> temp = new LinkedHashMap<String, String>();
		temp.putAll(xmlMap);
		temp.put("out_cust_no", cust_no);
		FyPaymentUtil.printRequestData(temp, rem+"提交参数", type, true);
		
		String data = HttpUtil.postMethod(FyConstants.post_url + FyConstants.unFreeze, xmlMap, "UTF-8");
		Map<String, String> dataMap = FyPaymentUtil.parseXmlToMap(data, error);
		
		FyPaymentUtil.printData(dataMap, rem+"回调参数", type, true);
		
		FyPaymentUtil.checkSign(error, dataMap, rem, type, true);			

		if(error.code < 0){
			return null;
		}
		
		return dataMap;
		
	}
	
	/**
	 * 查询还款提交第三方需要的参数
	 * @param bill
	 * @return 
	 */
	public static List<LinkedHashMap<String, String>> queryRepaymentData(Bill bill){
		
		t_bills t_bill = t_bills.findById(bill.id);
		
		//标的初始化
		Bid bid = new Bid();
		bid.id = bill.bidId;
		
		//初始化借款人
		User borrower = new User();
		borrower.id = bid.userId;
		t_users u = t_users.findById(borrower.id);
		
		double pOutAmt = 0.00; //还款总金额=本金 + 利息 +罚息
		double pOutFee = 0.00; //投资管理费总和
		
		//投资利息管理费费率
		double managementRate = Bid.queryInvestRate(bid.id);		
 		if(managementRate != 0){
 			managementRate = managementRate / 100;
		}
		//投资列表
		List<t_bill_invests> list = t_bill_invests.find(" bid_id = ? and periods = ? and status not in (0,-3,-7,-4) ", bill.bidId, t_bill.periods).fetch();
		
		List<LinkedHashMap<String, String>> pDetails = new LinkedList<LinkedHashMap<String, String>>();
		for(int i = 0; i< list.size(); i++){
			
			t_bill_invests invest = list.get(i);
			//投资人收益
			double pInAmt = invest.receive_interest + invest.receive_corpus + invest.overdue_fine;
			pOutAmt = pOutAmt + pInAmt;
			
			double pOutInfoFee = BillInvests.getInvestManagerFee(invest.receive_interest, managementRate, invest.user_id);  //投资管理费
			pOutFee = pOutFee + pOutInfoFee;
			
			//初始化投资人
			User invester = new User();
			invester.id = invest.user_id;
			
			//还款参数
			LinkedHashMap<String, String> repaymentMap = new LinkedHashMap<String, String>();		
			repaymentMap.put("mchnt_cd", FyConstants.mchnt_cd);//企业代码
			repaymentMap.put("mchnt_txn_ssn", FyPaymentUtil.createBillNo("R", t_bill.mer_bill_no + invest.id));//流水号
			repaymentMap.put("out_cust_no", borrower.ipsAcctNo);//出账账户
			repaymentMap.put("in_cust_no", invester.ipsAcctNo);//进账账户
			repaymentMap.put("contract_no", "");//预授权合同号
			repaymentMap.put("amt", pInAmt+"");//划拨金额
			repaymentMap.put("rem", "还款");//备注
			repaymentMap.put("type", "R");
			
			
			//理财管理费参数
			LinkedHashMap<String, String> rMap = new LinkedHashMap<String, String>();		
			rMap.put("mchnt_cd", FyConstants.mchnt_cd);//企业代码
			rMap.put("mchnt_txn_ssn", FyPaymentUtil.createBillNo("RM", t_bill.mer_bill_no + invest.id));//流水号
			rMap.put("out_cust_no", invester.ipsAcctNo);//出账账户
			rMap.put("in_cust_no", FyConstants.mchnt_name);//进账账户
			rMap.put("contract_no", "");//预授权合同号
			rMap.put("amt", pOutInfoFee+"");//划拨金额
			rMap.put("rem", "投资管理费");//备注
			rMap.put("type", "RM");
			pDetails.add(repaymentMap);
			pDetails.add(rMap);
		}
		return pDetails;
	}
	
	/**
	 * 垫付查询
	 * @param billId 账单bill_id
	 * @return
	 */
	public static List<Map<String, String>> queryAdvance(Long billId, ErrorInfo error){
		
		//查询需要垫付的账单
		t_bills bill = t_bills.findById(billId);
				
		double investmentRate = Bid.queryInvestRate(bill.bid_id);
 		
 		if(investmentRate != 0){
 			investmentRate = investmentRate /100;
 		}
 		
		String sql = "select new Map(invest.mer_bill_no as mer_bill_no, invest.id as id, invest.invest_id as investId, invest.receive_corpus as receive_corpus,invest.receive_interest as " + "receive_interest, invest.overdue_fine as overdue_fine, invest.user_id as user_id, invest.overdue_fine) "
				+ "from t_bill_invests as invest where invest.bid_id = ? and invest.periods = ? and invest.status not in (?,?,?,?)";

		List<Map<String, Object>> investList = t_bill_invests.find(sql, bill.bid_id, bill.periods, Constants.FOR_DEBT_MARK, Constants.NORMAL_RECEIVABLES, Constants.ADVANCE_PRINCIIPAL_RECEIVABLES, Constants.OVERDUE_RECEIVABLES).fetch();
		
		List<Map<String, String>> pDetails = new LinkedList< Map<String, String>>();
		
		for (Map<String, Object> map : investList) {

			double receiveInterest = (Double) map.get("receive_interest");// 本期的投资利息
			double receiveCorpus = (Double) map.get("receive_corpus"); //本期投资本金
			double receiveFees = (Double) map.get("overdue_fine"); //本期投资罚息
		
			//支付给投资人的本金+利息+罚息
			double pTrdAmt = receiveInterest + receiveCorpus + receiveFees;
			
			//初始化投资记录
			long investId  = (Long) map.get("investId");			
			t_invests invest = t_invests.findById(investId);
						
			//初始化投资人
			User investUser = new User();
			investUser.id = invest.user_id;
			
			//初始化投资利息管理费
			double investManageFee = BillInvests.getInvestManagerFee(receiveInterest, investmentRate, invest.user_id);// 投资管理费
			//垫付列表
			LinkedHashMap<String, String> rMap = new LinkedHashMap<String, String>();		
			rMap.put("mchnt_cd", FyConstants.mchnt_cd);//企业代码
			rMap.put("mchnt_txn_ssn", FyPaymentUtil.createBillNo("A", invest.mer_bill_no + bill.periods));//流水号
			rMap.put("out_cust_no", FyConstants.mchnt_name);//出账账户
			rMap.put("in_cust_no", investUser.ipsAcctNo);//进账账户
			rMap.put("contract_no", "");//预授权合同号
			rMap.put("amt", (pTrdAmt - investManageFee) +"");//划拨金额,
			rMap.put("rem", "垫付");//备注
			pDetails.add(rMap);
		}
		return pDetails;
	}
	
	/**
	 * 垫付还款查询
	 * @param billId 账单bill_id
	 * @param userId 借款人id
	 * @return
	 */
	public static Map<String, String> queryAdvanceRepayment(Long billId, long userId, ErrorInfo error){
		
		//1.查询本期借款账单的账单数据
		String sql = "select new Map(user.ips_acct_no as ips_acct_no,user.mobile as mobile,bid.id as bid_id,bid.bid_no as bid_no,bill.overdue_mark as overdue_mark, bill.repayment_corpus as " +
				"repayment_corpus, bill.repayment_interest as repayment_interest, bill.overdue_fine as overdue_fine," +
				" bill.status as status, bill.periods as period) from t_bills as bill,t_bids as bid, t_users as user where bill.bid_id = bid.id and bid.user_id = user.id and bill.id = ?";
		System.out.println(sql+"--"+billId);
		Map<String, Object> result = t_bills.find(sql, billId).first();
		Double repaymentCorpus = (Double)result.get("repayment_corpus"); //本金
		Double repaymentInterest = (Double)result.get("repayment_interest"); //利息
		Double repayOverdueFine = (Double)result.get("overdue_fine"); //罚息
		
		double pTrdAmt = repaymentCorpus + repaymentInterest + repayOverdueFine; //还款总额
		t_bills bill = t_bills.findById(billId);
		User user = new User();
		user.id = userId;
	
		//垫付列表
		LinkedHashMap<String, String> rMap = new LinkedHashMap<String, String>();		
		rMap.put("mchnt_cd", FyConstants.mchnt_cd);//企业代码
		rMap.put("mchnt_txn_ssn", FyPaymentUtil.createBillNo("RA", bill.mer_bill_no));//流水号
		rMap.put("out_cust_no", user.ipsAcctNo);//出账账户
		rMap.put("in_cust_no", FyConstants.mchnt_name);//进账账户
		rMap.put("contract_no", "");//预授权合同号
		rMap.put("amt", pTrdAmt+"");//划拨金额,
		rMap.put("rem", "垫付还款");//备注
		
		return rMap;
	}
	
	
}
