package jobs;

import java.util.Date;
import java.util.List;

import business.AutoReturnMoney;
import business.ProtocolPay;
import business.User;
import constants.Constants;
import models.t_enum_map;
import models.t_user_recharge_details;
import play.Logger;
import play.db.jpa.JPA;
import play.jobs.On;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.baofoo.business.QueryOrder;

/**
 * 
 * @description.  存管交易记录处理-用户充值
 *  
 * @modificationHistory.  
 * @author liulj Oct 31, 201710:22:09 AM TODO
 */

@On("0/30 * * * * ?")
public class UserProtocolRechargeJob extends BaseJob {
	
	public void doJob() {
		if(!"1".equals(IS_JOB))return;
		System.out.println(String.format("[%s] 宝付协议交易记录处理-用户充值", DateUtil.dateToString(new Date())));
		userRechargeHandle();
	}
	
	/**
	 * 
	 * @author liulj
	 * @creationDate. Apr 28, 2018 11:19:21 AM 
	 * @description.  处理中的充值记录
	 *
	 */
	public void userRechargeHandle() {
		List<t_user_recharge_details> userRecharges = ProtocolPay.findRecharge4I();
		if(userRecharges != null && !userRecharges.isEmpty()) {
			for(t_user_recharge_details user_recharge_details : userRecharges) {
				JPAUtil.transactionBegin();
				//充值recharge是否成功
				Boolean isRechargeSuccess=false;
				try {
					String result = QueryOrder.execute(user_recharge_details.pay_number);
					System.out.println(result);
					
					if("S".equalsIgnoreCase(result)) {
						ErrorInfo error=new ErrorInfo();
						User.recharge(user_recharge_details.pay_number, user_recharge_details.amount, new ErrorInfo());
						if(error.code<0) {
							throw new Exception(error.msg);
						}
						if(JPA.em().getTransaction().getRollbackOnly()) {
							throw new Exception("充值失败:JPA.em().getTransaction().getRollbackOnly() 但未返回error_code");
						}
						isRechargeSuccess=true;
					}else if("F".equalsIgnoreCase(result)) {
						ProtocolPay.updateRecharge4F(user_recharge_details.pay_number);
					}
					JPAUtil.transactionCommit();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					JPA.em().getTransaction().rollback();
				}
				
				if(isRechargeSuccess
						&& user_recharge_details.client==Constants.CLIENT_BACKSTAGE
						&& user_recharge_details.recharge_for_type != null
						&& user_recharge_details.recharge_for_type.equals(t_enum_map.getEnumNameMapByTypeName("t_user_recharge_details.recharge_for_type").get("t_bill.id").enum_code)
						&& user_recharge_details.recharge_for_id != null) {
					//是否还款成功
					Boolean isRepaymentSuccess=false;
					try {
						Logger.info("协议支付查询接口,自动还款 billId:"+user_recharge_details.recharge_for_id);
						JPAUtil.transactionBegin();
						//true还款成功,false账户资金不足
						isRepaymentSuccess=AutoReturnMoney.repaymentMain(user_recharge_details.recharge_for_id);
						if(isRepaymentSuccess) {
							Logger.info("协议支付查询接口,自动还款成功 billId:"+user_recharge_details.recharge_for_id);
							JPAUtil.transactionCommit();
						}else {
							Logger.info("协议支付查询接口,自动还款失败 billId:"+user_recharge_details.recharge_for_id+" 资金不足");
							JPA.em().getTransaction().rollback();
						}
					}catch (Exception e) {
						//非资金不足造成的还款失败
						Logger.error("协议支付查询接口,自动还款 非资金不足造成的异常 billId:"+user_recharge_details.recharge_for_id);
						e.printStackTrace();
						JPA.em().getTransaction().rollback();
					}
				}
			}
		}
	}
}
