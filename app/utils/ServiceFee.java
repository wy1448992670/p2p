package utils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import business.BackstageSet;
import business.Bill;
import constants.Constants;
import constants.OptionKeys;

/**
 * 服务费
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-13 上午11:59:34
 */
public class ServiceFee {

	/**
	 * 借款管理费
	 * 
	 * @param amount 金额
	 * @param period 期限
	 * @param unit 期限单位
	 * @param error 信息值
	 * @return 借款管理费
	 */
	public static double loanServiceFee(double amount, int period, int unit, ErrorInfo error) {
	    BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();

        double num1 = backstageSet.borrowFee;
        double num2 = backstageSet.borrowFeeMonth;
        double num3 = backstageSet.borrowFeeRate;
        double num4 = backstageSet.borrowFeeDay;
        
        switch (unit) {
		case Constants.DAY:
			if (0 >= num4) return 0.0;
			
			break;
		case Constants.YEAR:
		case Constants.MONTH:
			if ((0 == num1) && (0 == num3)) return 0.0;
			
			break;
		default:
			
			break;
		}
        
	    /* 换算月份 */
		if(unit == Constants.YEAR){
			period *= 12;
		}else if(unit == Constants.DAY){
		    
	    /** 公式：按借款本金 * ?% /360 * 借款天数 **/
			num4 = new BigDecimal(num4).compareTo(new BigDecimal(Constants.BORROW_MANAGE_MAXRATE))>0?Constants.BORROW_MANAGE_MAXRATE:num4;	
			num4 = Arith.div(num4, 100, 10);
			
			return Arith.mul(Arith.div(Arith.mul(amount, num4), Constants.DAY_INTEREST, 10), period);
		}

		/** 公式：按借款本金 * ? % +本金*（期数 - ?个月）* ? % **/

		num1 = Arith.div(num1, 100, 10);
		double div = Arith.mul(amount, num1); // 本金 *?

		// 如果达到给定的额外收费期数
		if (period > num2) {
			num3 = Arith.div(num3, 100, 10);
			div = Arith.add(Arith.mul(Arith.mul(amount, (period - num2)), num3), div);
		}
		
		double borrowManageFeeFiftyPercent = Arith.mul(amount, Arith.div(Constants.BORROW_MANAGE_MAXRATE, 100, 10));
		div = new BigDecimal(div).compareTo(new BigDecimal(borrowManageFeeFiftyPercent))>0 ? borrowManageFeeFiftyPercent : div;
		
		if (div < 0) {
			error.code = -5;
			error.msg = error.FRIEND_INFO + "借款管理费有误!" + error.PROCESS_INFO;
			
			return -5;
		}

		return div;
	}
	
	/**
	 * 理财管理费
	 * @param amount 金额
	 * @param apr 年利率
	 * @param error 信息值
	 * @return 理财管理费
	 */
	@Deprecated
	public static double investServiceFee(double amount, double apr, ErrorInfo error) { 
		/* 得到理财管理费基准值 */
		String strfee = OptionKeys.getvalue(OptionKeys.INVESTMENT_FEE, error);

		if (null == strfee)  return -1;
		
		double fee = Double.parseDouble(strfee);
		
		fee = Arith.mul(Arith.mul(amount, Arith.div(apr, 100, 20)), Arith.div(fee, 100, 20));
		
		if(fee <= 0)
			return 0;
		
		return fee;
	}
	
	
	/**
	 * 计算理财CPS推广奖励（该方法暂时没有被用到）
	 * @param amount
	 * @param apr
	 * @param periodUnit
	 * @param period
	 * @param error
	 * @return
	 */
	public static double investServiceManageFee(double amount, double apr, int periodUnit,int period, long repaymentid, ErrorInfo error) { 
		
		double interest = ServiceFee.interestCompute(amount, apr, periodUnit, period, (int)repaymentid);
			//Bid.getInterest(period, periodUnit, apr, amount);
		double managementRate = BackstageSet.getCurrentBackstageSet().investmentFee;
 		
 		if(managementRate != 0){
 			managementRate = managementRate / 100;
		}
		
		
		double investManageFee = Arith.mul(interest, managementRate);
		
		if(investManageFee <= 0)
			return 0;
		
		return investManageFee;
	}
	/**
	 * 提现管理费
	 * @param amount
	 * @return
	 */
	public static double withdrawalFee(double amount) {
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		double withdrawFee = amount - backstageSet.withdrawFee;
		
		if(withdrawFee <= 0) {
			return 0 + backstageSet.withdrawBase;
		}
		
		return Arith.round(withdrawFee*backstageSet.withdrawRate/100, 2) + backstageSet.withdrawBase;
	}
	
	/**
	 * 计算可提现的最大金额（针对外扣）
	 * @param amount 余额
	 * @return
	 */
	public static double maxWithdralAmount(double amount){
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		double rate = backstageSet.withdrawRate;
		double fee = backstageSet.withdrawFee;
		double base = backstageSet.withdrawBase;

		//用户余额小于或等于基本手续费时，可提现金额为0
		if(amount <= base){
			return 0;
		}
		
		//提现手续费等于基本手续费时
		if (amount - fee <= 0) {
			return amount - base;
		}
		
		return Arith.divDown(100 * amount + rate * fee - 100 * base, 100 + rate, 2);
	}
	
	/**
	 * 利息计算
	 * @param amount 金额
	 * @param apr 年利率
	 * @param unit 期限单位(-1 年标 0月标 1天标)
	 * * @param period 期限
	 * @param repayment 还款方式
	 */
	public static double interestCompute(double amount, double apr, int unit, int period, int repayment){
		if(0 == amount || 
			apr < 0 || 
			apr > 100 || 
			0 == period || 
			repayment < 1 || 
			repayment > 3 || 
			unit < -1 || 
			unit > 1)
			return 0;
			
		double monthRate = Double.valueOf(apr * 0.01)/12.0;//通过年利率得到月利率
		double interest = 0;
		
		if(unit == Constants.DAY){//秒还还款和天标的总利息
			interest = Arith.div(Arith.mul(Arith.mul(amount, monthRate), period), 30, 2);//天标的总利息
			
		}else{
			if(unit == Constants.YEAR){
				period = period * 12;
				
			}
			
			//等额本息还款（否则一次还款或先息后本）
			if(repayment == Constants.PAID_MONTH_EQUAL_PRINCIPAL_INTEREST){
				double monPay = Double.valueOf(Arith.mul(amount, monthRate) * Math.pow((1 + monthRate), period))/ 
				Double.valueOf(Math.pow((1 + monthRate), period) - 1);//每个月要还的本金和利息
				interest = Arith.sub(Arith.mul(monPay, period), amount);
				
			}else{
				interest = Arith.round(Arith.mul(Arith.mul(amount, monthRate), period), 2);
				
			}
		}
		
		return Arith.round(interest, 2);
	}
	
	/**
	 * 还款截止日期
	 * @param unit
	 * @param period
	 * @param repayment
	 * @return
	 */
	public static String repayTime(int unit, int period, int repayment){
		String payTime = null;

		if (unit == Constants.DAY) {
			String date = DateUtil.dateToString1(Bill.add(new Date(),Calendar.DAY_OF_MONTH, period));
			String[] sub = date.split("-");
			String year = sub[0];
			String month = sub[1];
			String day = sub[2];

			payTime = year + "年" + month + "月" + day + "日";

		} else {
			if (Constants.ONCE_REPAYMENT == repayment) {
				String date = DateUtil.dateToString1(Bill.add(new Date(), unit == Constants.YEAR ? Calendar.YEAR : Calendar.MONTH, period));
				String[] sub = date.split("-");
				String year = sub[0];
				String month = sub[1];
				String day = sub[2];

				payTime = year + "年" + month + "月" + day + "日";
				
			} else {
				Calendar date = Calendar.getInstance();
				int day = date.get(Calendar.DAY_OF_MONTH);
				payTime = "每月" + day + "号";
				
			}

		}

		return payTime;
	}
}
