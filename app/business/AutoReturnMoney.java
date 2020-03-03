package business;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import constants.Constants;
import constants.IPSConstants.IPSDealStatus;
import exception.ApiStatusUnknownException;
import exception.BalanceIsEnoughException;
import exception.BusinessException;
import models.t_bids;
import models.t_bills;
import models.t_debt_bill_invest;
import models.t_debt_transfer;
import models.t_enum_map;
import models.t_user_bank_accounts;
import models.t_user_recharge_details;
import models.t_users;
import play.Logger;
import play.db.jpa.JPA;
import utils.Arith;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.baofoo.AgreementBanks;
import utils.baofoo.business.SinglePay;
import utils.baofoo.util.Log;

/**
 * 自动回款
 * @author zqq
 */
public class AutoReturnMoney {
	
	/**
	 * 自动回款方法 autoReturnMoneyForBill
	 * 1.还款:repayment
	 * 2.如果资金不足 支付账单 payForBill
	 * 		1.预支付:perPayForBillWithAccount
	 * 		2.直接协议支付:directProtocolPay
	 * 		3.确认支付:充值 recharge
	 * 3.还款:repayment
	 * @param billId
	 * @throws Exception
	 */
	static public boolean autoReturnMoneyForBill(long billId) throws Exception {
		
		//是否还款成功
		Boolean isRepaymentSuccess=false;
		try {
			Logger.info("第一次还款开始 billId:"+billId);
			JPAUtil.transactionBegin();
			//true还款成功,false账户资金不足
			isRepaymentSuccess=AutoReturnMoney.repaymentMain(billId);
			if(isRepaymentSuccess) {
				Logger.info("第一次还款成功 billId:"+billId);
				Logger.info("JPAUtil.transactionCommit();");
				JPAUtil.transactionCommit();
				return true;
			}else {
				Logger.info("第一次还款失败 billId:"+billId+" 资金不足");
				Logger.info("JPA.em().getTransaction().rollback();");
				JPA.em().getTransaction().rollback();
			}
		}catch (Exception e) {
			//非资金不足造成的还款失败
			Logger.error("billId:"+billId+" 非资金不足造成的还款失败,账单自动回款终止");
			e.printStackTrace();
			Logger.info("JPA.em().getTransaction().rollback();");
			JPA.em().getTransaction().rollback();
			throw e;
		}
		
		//账单支付是否成功
		Boolean isPayForBillSuccess=false;
		Boolean balanceIsEnough=false;
		try {
			JPAUtil.transactionBegin();
			//true账单支付成功,false账单支付不成功
			isPayForBillSuccess=AutoReturnMoney.payForBill(billId);
			if(isPayForBillSuccess) {
				Logger.info("billId:"+billId+" 账单支付成功");
			}else {
				Logger.info("billId:"+billId+" 账单支付不成功");
			}
			
		} catch(BalanceIsEnoughException e) {
			//账户资金足够引起的账单支付异常
			Logger.info("billId:"+billId+" 账单支付异常 "+e.getMessage());
			balanceIsEnough=true;
		} catch (Exception e1) {
			Logger.error("billId:"+billId+"账单支付异常,账单自动回款终止");
			e1.printStackTrace();
			throw e1;
		}
		
		if(isPayForBillSuccess || balanceIsEnough) {
			JPAUtil.transactionBegin();
			try {
				//true还款成功,false账户资金不足
				Logger.info("第二次还款开始 billId:"+billId);
				isRepaymentSuccess=repaymentMain(billId);
				if(isRepaymentSuccess) {
					Logger.info("第二次还款成功 billId:"+billId);
					Logger.info("JPAUtil.transactionCommit();");
					JPAUtil.transactionCommit();
					return true;
				}else {
					Logger.info("第二次还款失败 billId:"+billId+" 资金不足");
					Logger.info("JPA.em().getTransaction().rollback();");
					JPA.em().getTransaction().rollback();
					return false;
				}
			} catch (Exception e) {
				//非资金不足造成的还款失败
				Logger.error("billId:"+billId+"非资金不足造成的还款失败,账单自动回款终止");
				e.printStackTrace();
				Logger.info("JPA.em().getTransaction().rollback();");
				JPA.em().getTransaction().rollback();
				throw e;
			}
		}else {
			return false;
		}
		
	}
	
	/**
	 * 2019-03-27 zqq
	 * 后台还款整合 
	 * 还款成功返回true,资金不足返回false
	 * 其他抛出异常
	 * @param billId
	 * @throws Exception
	 */
	static public boolean repaymentMain(long billId) throws Exception {
		ErrorInfo error=new ErrorInfo();
		
		t_bills t_bill= t_bills.findById(billId);
		t_bids t_bid=t_bids.findById(t_bill.bid_id);
		t_users t_user=t_users.findById(t_bid.user_id);
		JPA.em().refresh(t_user,LockModeType.PESSIMISTIC_WRITE);
		JPA.em().refresh(t_bid,LockModeType.PESSIMISTIC_WRITE);
		JPA.em().refresh(t_bill,LockModeType.PESSIMISTIC_WRITE);
		/**
		 * 投资人开排他锁会造成死锁,投资人需要开乐观锁.
		 * 原系统没有考虑同步性问题,接口也没有设计,代码混在一起,不好处理.
		 * 现有代码,同二投资人同时出现在两笔回款账单中,会死锁
		 */
		
		Bill bill = new Bill();
		bill.setId(billId);
		
		if(bill.ipsStatus == IPSDealStatus.REPAYMENT_HANDING){  //还款处理中
			throw new BusinessException("还款处理中，请勿重复操作!");
		}
		
		if(bill.ipsStatus == IPSDealStatus.OFFLINEREPAYMENT_HANDING){  //线下收款处理中
			throw new BusinessException("线下收款处理中，不能还款!");
		}
		
		if(bill.ipsStatus == IPSDealStatus.COMPENSATE_HANDING){  //本金垫付处理中
			throw new BusinessException("本金垫付处理中，不能还款!");
		}
		/* 2014-12-29 限制还款需要从第一期逐步开始还款 */
		if(Bill.checkPeriod(bill.bidId, bill.periods) > 0){
			throw new BusinessException("请您从第一期逐次还款!");
		}
		
		/*本金垫付还款,本金垫付还款,*/
		if (Constants.IPS_ENABLE && bill.status == Constants.ADVANCE_PRINCIIPAL_REPAYMENT) {
			throw new BusinessException("本金垫付还款失败!");
		}else if(Constants.IPS_ENABLE) {
			throw new BusinessException("资金托管还款失败!");
		}
		
		t_debt_transfer transfer = t_debt_transfer.find(" bid_id = ? ", bill.bidId).first();
		List<t_debt_bill_invest> debtBill = new ArrayList<>();
		if(transfer != null ){
			debtBill = t_debt_bill_invest.find(" debt_id = ? ", transfer.id).fetch();
			Logger.info("该标的有债权转让，执行债权转让还款逻辑! 账单信息: " + debtBill);
			bill.repaymentV1(bill.user.id, error);
		}else {
			//普通网关模式，还款业务逻辑
			bill.repayment(bill.user.id, error);
		}
		if (error.code == Constants.BALANCE_NOT_ENOUGH){
			return false;
			//throw new BalanceNotEnoughException(error.msg);
		}else if(error.code<0) {
			throw new Exception("还款失败:"+error.msg);
		}else if(JPA.em().getTransaction().getRollbackOnly()) {
			throw new Exception("还款失败:JPA.em().getTransaction().getRollbackOnly() 但未返回error_code");
		}
		return true;
	}

	/**
	 * 2019-03-28 zqq
	 * 账单自动回款
	 * 为账单还款自动充值,针对账单的[应还金额]的不足额度自动充值,充值成功或者已满足[应还金额],返回true
	 * 查询用户的协议卡,循环银行卡,调用代扣功能,如果充值成功,返回true,都支付失败返回false,未知报异常
	 * @param billId
	 * @return
	 * @throws Exception
	 */
	static public boolean payForBill(long billId) throws BalanceIsEnoughException,Exception {
		
		Logger.info("支付账单:"+billId);
		ErrorInfo error=new ErrorInfo();
		
		t_bills bill= t_bills.findById(billId);
		t_bids bid=t_bids.findById(bill.bid_id);
		t_users user=t_users.findById(bid.user_id);

		//账户余额
		double balance=Bill.queryBalance(user.id,error);
		if(error.code<0) {
			throw new Exception("支付账单失败:"+error.msg);
		}
		//账单应收
		double receivable = Arith.add(bill.repayment_corpus,
							Arith.add(bill.repayment_interest,
								Arith.add(bill.overdue_fine,
								bill.service_amount.doubleValue())
							)
						);// 总共要还款的金额
		
		//余额大于应收
		if(balance>=receivable) {
			//return true;
			throw new BalanceIsEnoughException("用户资金足够,不需要再支付账单");
		}
		//充值中的金额
		double unfinishedRecharge=AutoReturnMoney.queryUnfinishedRecharge(bill);
		/**
		 * 算上充值中的金额,余额大于应收,
		 * 此时不能继续代扣,也不能还款
		 */
		if(Arith.add(unfinishedRecharge,balance)>=receivable) {
			throw new Exception("支付账单失败:有充值未完成,不能继续支付");
		}
		
		//不足的账单应收账款
		//应收-账户余额-充值中金额
		//double insufficientReceivable=Arith.sub(Arith.sub(receivable, balance),unfinishedRecharge);
		
		List<t_user_bank_accounts> banks = UserBankAccounts.queryMoreById(user.id);
		for(t_user_bank_accounts bankAccount:banks) {
			//没有银行号
			if(bankAccount.bank_code==null){
				continue;
			}
			//非协议绑卡银行
			if(!AgreementBanks.isAvailable(bankAccount.bank_code)) {
				continue;
			}
			//无协议号
			if(StringUtils.isBlank(bankAccount.protocol_no)) {
				continue;
			}
			
			//支付单
			t_user_recharge_details perPayDetail=null;
			try {
				//预支付
				Logger.info("预支付开始 billId:"+billId);
				JPAUtil.transactionBegin();
				perPayDetail=perPayForBillWithAccount(billId,bankAccount);
				Logger.info("预支付成功 billId:"+billId);
				Logger.info("JPAUtil.transactionCommit();");
				JPAUtil.transactionCommit();
				
			}catch (BalanceIsEnoughException e) {
				//用户资金足够
				Logger.error("预支付异常,用户资金足够 billId:"+billId);
				Logger.info("JPA.em().getTransaction().rollback()");
				JPA.em().getTransaction().rollback();
				//return true;
				throw e;
			} catch (Exception e) {
				//预支付异常,回滚
				Logger.error("预支付异常,billId:"+billId+" "+e.getMessage());
				e.printStackTrace();
				Logger.info("JPA.em().getTransaction().rollback()");
				JPA.em().getTransaction().rollback();
				//TOTHINK 预支付未成功,是否终止,如果终止,抛异常
			}
			
			JPAUtil.transactionBegin();
			//预支付单未生成,换下一张卡
			if(perPayDetail==null || perPayDetail.id==null) {
				Logger.info("预支付单未生成,换下一张卡");
				continue;
			}
			
			//是否协议支付成功
			boolean isProtocolPaySuccess=false;
			try {
				//直接协议支付
				Logger.info("直接协议支付,billId:"+billId);
				isProtocolPaySuccess=AutoReturnMoney.directProtocolPay(perPayDetail);
				if(isProtocolPaySuccess) {
					Logger.info("直接协议支付成功,billId:"+billId);
				}else {
					Logger.info("直接协议支付失败,billId:"+billId);
				}
			} catch (ApiStatusUnknownException e) {
				//接口状态超时,返回值不匹配等
				throw e;
			} catch (Exception e) {
				//接口秘钥,签名,传输等异常
				throw e;
			}
			
			if(isProtocolPaySuccess) {
				JPAUtil.transactionBegin();
				try {
					//充值
					Logger.info("充值开始,billId:"+billId);
					User.recharge(perPayDetail.pay_number, perPayDetail.amount, error);
					if(error.code<0) {
						throw new Exception(error.msg);
					}
					if(JPA.em().getTransaction().getRollbackOnly()) {
						throw new Exception("充值失败:JPA.em().getTransaction().getRollbackOnly() 但未返回error_code");
					}
					Logger.info("充值成功,billId:"+billId);
					Logger.info("JPAUtil.transactionCommit();");
					JPAUtil.transactionCommit();
					return true;
				} catch (Exception e) {
					Logger.error("充值异常,billId:"+billId+",支付成功后,充值异常");
					e.printStackTrace();
					Logger.error("JPA.em().getTransaction().rollback();");
					JPA.em().getTransaction().rollback();
					throw e;
				}
				
			}else {
				try {
					JPAUtil.transactionBegin();
					perPayDetail.merge();
					perPayDetail.completed_time=new Date();
					perPayDetail.save();
					JPAUtil.transactionCommit();
				} catch (Exception e) {
					Logger.error("充值异常,billId:"+billId+",支付失败后,完结协议支付单失败");
					e.printStackTrace();
					Logger.error("JPA.em().getTransaction().rollback();");
					JPA.em().getTransaction().rollback();
					throw e;
				}
				//协议支付失败,换卡再付
				continue;
			}
			
			
		}
		
		return false;
	}
	/**
	 * 预协议支付
	 * 先存储需要调用的数据
	 * 使用指定的银行卡,针对还款账单,预支付
	 * 生成预支单成功,返回预支付单,否则返回null
	 * @param billId
	 * @param account
	 * @return
	 * @throws Exception
	 */
	private static t_user_recharge_details perPayForBillWithAccount(long billId,t_user_bank_accounts account) 
			throws BalanceIsEnoughException,Exception {
		
		Logger.info("为账单还款自动充值:"+billId);
		ErrorInfo error=new ErrorInfo();
		t_bills bill= t_bills.findById(billId);
		t_bids bid=t_bids.findById(bill.bid_id);
		t_users user=t_users.findById(bid.user_id);
		JPA.em().refresh(user,LockModeType.PESSIMISTIC_WRITE);
		JPA.em().refresh(bid,LockModeType.PESSIMISTIC_WRITE);
		JPA.em().refresh(bill,LockModeType.PESSIMISTIC_WRITE);
		//账户余额
		double balance=Bill.queryBalance(user.id,error);
		if(error.code<0) {
			throw new Exception("后台代扣失败:"+error.msg);
		}
		//账单应收
		double receivable = Arith.add(bill.repayment_corpus,
							Arith.add(bill.repayment_interest,
								Arith.add(bill.overdue_fine,
								bill.service_amount.doubleValue())
							)
						);// 总共要还款的金额
		
		//余额大于应收
		if(balance>=receivable) {
			throw new BalanceIsEnoughException("后台代扣失败:用户账户金额已足够");
		}
		//充值中的金额
		double unfinishedRecharge=AutoReturnMoney.queryUnfinishedRecharge(bill);
		/**
		 * 算上充值中的金额,余额大于应收,
		 * 此时不能继续代扣,也不能还款
		 */
		if(Arith.add(unfinishedRecharge,balance)>=receivable) {
			throw new Exception("后台代扣失败:有充值未完成,不能继续代扣");
		}
		
		//不足的账单应收账款
		//应收-账户余额-充值中金额
		double insufficientReceivable=Arith.sub(Arith.sub(receivable, balance),unfinishedRecharge);
		
		/*
		//非协议绑卡银行
		if(!AgreementBanks.isAvailable(account.bank_code)) {
			throw new UnimportanceBusinessException("非协议绑卡银行");
		}
		//无协议号
		if(StringUtils.isBlank(account.protocol_no)) {
			throw new UnimportanceBusinessException("无协议号");
		}
		*/
		//--------------------------------------------------------用户id		交易金额					账号			recharge_for_type	recharge_for_id	gateway:-4 宝付-协议支付	1.接口充值 2.后台充值	1.pc 2.app 3.WECHAT 4.other
		t_user_recharge_details userRechargeDetail=User.sequence(user.id,insufficientReceivable,account.account
				,t_enum_map.getEnumNameMapByTypeName("t_user_recharge_details.recharge_for_type").get("t_bill.id").enum_code
				,bill.id, -4, Constants.GATEWAY_RECHARGE,Constants.CLIENT_BACKSTAGE);
		if(JPA.em().getTransaction().getRollbackOnly()) {
			throw new Exception("预协议支付失败:JPA.em().getTransaction().getRollbackOnly() 但未抛出异常");
		}
		return userRechargeDetail;
	}
	
	
	/**
	 * 直接协议支付方法
	 * 支付成功返回 true
	 * 失败返回 false
	 * 延时抛异常
	 * @param bankAccount
	 * @param userRechargeDetail
	 * @param receivable
	 * @return
	 * @throws Exception
	 */
	public static boolean directProtocolPay(t_user_recharge_details userRechargeDetail) throws Exception {
		//验证支付人使用的卡开通了协议支付
		//卡号,用户能对上,且有效且有协议号
		List<t_user_bank_accounts> userBanAccountVerify = t_user_bank_accounts
				.find(" user_id = ? and account = ? and protocol_no is not null and is_valid = 1 ", userRechargeDetail.user_id,userRechargeDetail.bank_card_no).fetch();
		if(userBanAccountVerify.size()<1) {
			return false;
		}
		/**
		 * 用户分离时,一个老用户可能产生 借款人id 投资人id
		 * 协议号一样,调用宝付需要协议绑定时的user_id验证
		 *
		 * 同一银行卡号,如果协议绑两次,会有两个协议号,只有最后一次的绑定数据有用
		 * 调用宝付需要使用最后一次协议号和用户id
		 * 
		 * 不同的用户可以绑定同一张卡(要求理财类型不同或主体性质不同)
		 */
		t_user_bank_accounts userBankAccount = t_user_bank_accounts
				.find(" account = ? and protocol_no is not null and is_valid = 1 order by time desc,id asc ",userRechargeDetail.bank_card_no).first();
		
		Map<String, String> payResult = SinglePay.execute(userBankAccount.protocol_no, BigDecimal.valueOf(userRechargeDetail.amount), userRechargeDetail.pay_number);
		//支付成功更新订单及用户余额信息
		if(payResult.get("resp_code").toString().equals("S")){
			Double succ_amt = new BigDecimal(payResult.get("succ_amt")).divide(new BigDecimal("100")).doubleValue();
			String trans_id = payResult.get("trans_id");
			if(!succ_amt.equals(userRechargeDetail.amount)) {
				throw new ApiStatusUnknownException("接口返回充值金额与调用金额不匹配");
			}
			if(!trans_id.equals(userRechargeDetail.pay_number)) {
				throw new ApiStatusUnknownException("接口返回商户订单号与调用商户订单号不匹配");
			}
			Log.Write("支付成功！[trans_id:"+trans_id+"]");
			
			return true;
			
		} else if(payResult.get("resp_code").toString().equals("I")){	
			Log.Write("处理中！");
			throw new ApiStatusUnknownException("交易处理中");
			
		} else if(payResult.get("resp_code").toString().equals("F")){
			Log.Write("支付失败！ " + payResult.get("biz_resp_msg"));
			return false;
		} else {
			throw new ApiStatusUnknownException(payResult.get("biz_resp_msg").toString());
		}
		
	}

	/**
	 * 查询用户为账单还款充值中的金额
	 * @param error
	 * @return
	 */
	public static double queryUnfinishedRecharge(t_bills bill) {
		Double unfinishedRecharge = null;
		t_bids bid=t_bids.findById(bill.bid_id);
		try {
			unfinishedRecharge = t_bids.find("select SUM(amount) from t_user_recharge_details "
					+ " WHERE payment_gateway_id = -4 and is_completed = 0 and completed_time is null "//宝付协议支付,未完成
					+ " and recharge_for_type=? and recharge_for_id = ? and user_id=? "
					,t_enum_map.getEnumNameMapByTypeName("t_user_recharge_details.recharge_for_type").get("t_bill.id").enum_code
					,bill.id
					,bid.user_id).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			throw e;
		}
		
		return null == unfinishedRecharge ? 0 : unfinishedRecharge;
	}
	
	/**
	 * 查询还款日是今天和明天的账单
	 * @return
	 */
	public static List<BigInteger> findBillId() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date todayMorning = calendar.getTime();
		calendar.add(Calendar.DATE,3);
		Date afterYomorrowMorning=calendar.getTime();
		Logger.info("todayMorning:"+todayMorning+" afterYomorrowMorning:"+afterYomorrowMorning);
		/**
		 * 2019-04-22 新增 企业及个体工商户不自动还款
		 */
		String sql = "select bill.id from t_bills bill "
				+ " ,t_bids bid,t_users users "
				+ " where bill.bid_id=bid.id and bid.user_id=users.id and users.user_type=1 "
				+ " and bill.status = -1 "
				+ " and bill.repayment_time>=? "
				+ " and bill.repayment_time<? order by bill.id ";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, todayMorning);
		query.setParameter(2, afterYomorrowMorning);
		List<BigInteger> list = query.getResultList();
		return list;
	}
	/**
	 * 查询还款日是今天和明天的账单
	 * @return
	 */
	public static List<t_bills> findBill() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		Date todayMorning = calendar.getTime();
		calendar.add(Calendar.DATE,3);
		Date afterYomorrowMorning=calendar.getTime();
		Logger.info("todayMorning:"+todayMorning+" afterYomorrowMorning:"+afterYomorrowMorning);

		return t_bills.find("from t_bills bill "
				+ " where bill.status = -1 "
				+ " and bill.repayment_time>=? "
				+ " and bill.repayment_time<? "
				,todayMorning
				,afterYomorrowMorning).fetch();
		
	}
}
