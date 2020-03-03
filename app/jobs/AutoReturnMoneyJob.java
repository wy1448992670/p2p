package jobs;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import business.AutoReturnMoney;
import models.t_bids;
import models.t_bills;
import play.Logger;
import play.jobs.On;
import utils.DateUtil;

/**
 * 
 * @description.  自动回款
 *  
 * @modificationHistory.  
 * @author zqq 2019-04-04
 */

//@On("0 0 9,12,16 * * ?")
//@Every("1min")
public class AutoReturnMoneyJob extends BaseJob {
	
	public void doJob() {
		if(!"1".equals(IS_JOB))return;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date todayMorning = calendar.getTime();
		calendar.add(Calendar.DATE,2);
		Date afterTomorrowMorning=calendar.getTime();
		
		System.out.println(String.format("[%s] 自动回款开始", DateUtil.dateToString(new Date())));
		
		List<BigInteger> billIdList=AutoReturnMoney.findBillId();
		for(BigInteger billId:billIdList) {
			System.out.println("autoReturnMoneyForBill begin:"+billId.longValue());
			t_bills t_bill= t_bills.findById(billId.longValue());
			t_bids t_bid=t_bids.findById(t_bill.bid_id);
			/*
				2019-05-24 邮件要求不自动回款
				 手机号：18663282566
				姓名：王建山
				身份证号：370403196903044816
				银行卡号：6217002170009543359
				
				2019-07-10 邮件要求不自动回款
				id:256
				 手机号：15066328815
				姓名：任欣
				身份证号：370403199006186113
				银行卡号：6222081605000409120
				
				2019-09-05 zzy18060317566 黄巧娟
				id:22045
			 */
			//不自动
			if(t_bid.user_id==25614L || t_bid.user_id==256L || t_bid.user_id==22045) {
				continue;
			}
			/*
				2019-07-05 邮件要求还款日前一天扣
				id:26068
				手机号：13863298388
				姓名：田永国
				身份证号：37040219631117053X
				银行卡号：6228481318424515679
			 */
			//还款日前一天扣
			if(t_bid.user_id==26068L) {
				//还款日在后天凌晨之前,即还款日是今天,明天
				//如果还款日不是今天,明天,不还
				if(!t_bill.repayment_time.before(afterTomorrowMorning)) {
					continue;
				}
			}
			
			try {
				AutoReturnMoney.autoReturnMoneyForBill(billId.longValue());
			} catch (Exception e) {
				Logger.error("billId:"+billId.longValue()+" 自动回款异常");
				e.printStackTrace();
			}
			System.out.println("autoReturnMoneyForBill end:"+billId.longValue());
		}
		
		/*
		List<t_bills> returnBills=AutoReturnMoney.findBill();
		for(t_bills bill:returnBills) {
			System.out.println("autoReturnMoneyForBill begin:"+bill.id);
			try {
				AutoReturnMoney.autoReturnMoneyForBill(bill.id);
			} catch (Exception e) {
				Logger.error("billId:"+bill.id+" 自动回款异常");
				e.printStackTrace();
			}
			System.out.println("autoReturnMoneyForBill end:"+bill.id);
		}*/
	}
	
	
	
}
