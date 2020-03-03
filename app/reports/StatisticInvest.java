package reports;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.persistence.Query;

import play.Logger;
import play.db.jpa.JPA;

import com.shove.Convert;

import constants.Constants;
import utils.Arith;
import utils.ErrorInfo;
import models.t_invest_transfer_details;
import models.t_invests;
import models.t_statistic_borrow;
import models.t_statistic_financial_situation;
import models.t_statistic_platform_float;
import models.t_statistic_platform_income;
import models.t_statistic_recharge;
import models.t_statistic_withdraw;
import models.t_users;


/**
 * 平台理财情况统计分析表
 * @author lwh
 *
 */
public class StatisticInvest {
	
	/**
	 * 查询某一个月的投资总额
	 * @param year
	 * @param month
	 * @param error
	 * @return
	 */
	public static Double queryTotalInvestAmount(int year, int month, ErrorInfo error){
		error.clear();
		
		Double amount;
		try {
			amount = t_statistic_financial_situation.find("select t.invest_accoumt from t_statistic_financial_situation t where t.year = ? and t.month = ?", year, month).first();
		} catch (Exception e) {
			Logger.error("查询某一个月的投资总额时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询某一个月的投资总额时有误！";
			
			return 0d;
		}
		
		
		//每个月的第一天没有记录，此时需要查上月的记录（这里只需要查最后一条记录即可）
		if(amount == null){
			try {
				amount = t_statistic_borrow.find("select invest_accoumt from t_statistic_financial_situation order by year desc, month desc").first();
			} catch (Exception e) {
				Logger.error("查询某一个月的借款总额时：" + e.getMessage());
				
				error.code = -1;
				error.msg = "查询某一个月的借款总额时有误！";
				
				return 0d;
			}
		}
		
		error.code = 0;
		
		return amount == null ? 0 : amount;
	}
	
	public static Double queryInvestAccoumt(){
		Double invest_accoumt = 0.0;//累计理财投标总额
		String sql = "select sum(amount) from t_invests where transfer_status = 0 and bid_id in (select id from t_bids where status > 0)";
		
		try {
			invest_accoumt = t_invests.find(sql).first();//累计理财投标总额
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == invest_accoumt || invest_accoumt == 0){
			invest_accoumt = 0.0;
		}
		return invest_accoumt;
	}
	
	public static Double queryInvestAccoumtPC(){
		Double invest_accoumt = 0.0;//累计理财投标总额
		String sql = "select sum(amount) from t_invests where transfer_status = 0 and bid_id in (select id from t_bids where status > 0) and client = 1";
		
		try {
			invest_accoumt = t_invests.find(sql).first();//累计理财投标总额
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == invest_accoumt || invest_accoumt == 0){
			invest_accoumt = 0.0;
		}
		return invest_accoumt;
	}
	
	public static Double queryInvestAccoumtAPP(){
		Double invest_accoumt = 0.0;//累计理财投标总额
		String sql = "select sum(amount) from t_invests where transfer_status = 0 and bid_id in (select id from t_bids where status > 0) and client = 2";
		
		try {
			invest_accoumt = t_invests.find(sql).first();//累计理财投标总额
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == invest_accoumt || invest_accoumt == 0){
			invest_accoumt = 0.0;
		}
		return invest_accoumt;
	}
	
	public static Double queryInvestAccoumtWechat(){
		Double invest_accoumt = 0.0;//累计理财投标总额
		String sql = "select sum(amount) from t_invests where transfer_status = 0 and bid_id in (select id from t_bids where status > 0) and client = 3";
		
		try {
			invest_accoumt = t_invests.find(sql).first();//累计理财投标总额
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == invest_accoumt || invest_accoumt == 0){
			invest_accoumt = 0.0;
		}
		return invest_accoumt;
	}
	
	
	public static Double queryIncreaseInvestAccount(){
		Double increase_invest_account = 0.0;//本月新增理财总额
		
		String sql = "select sum(amount) from t_invests where DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') and transfer_status = 0 and bid_id in (select id from t_bids where status > 0)";
		try {
			increase_invest_account = t_invests.find(sql).first();//本月新增理财总额
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == increase_invest_account || increase_invest_account == 0){
			increase_invest_account = 0.0;
		}
		return increase_invest_account;
	}
	
	public static Double queryIncreaseInvestAccountPC(){
		Double increase_invest_account = 0.0;//本月新增理财总额
		
		String sql = "select sum(amount) from t_invests where DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') and transfer_status = 0 and bid_id in (select id from t_bids where status > 0) and client = 1";
		try {
			increase_invest_account = t_invests.find(sql).first();//本月新增理财总额
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == increase_invest_account || increase_invest_account == 0){
			increase_invest_account = 0.0;
		}
		return increase_invest_account;
	}
	
	public static Double queryIncreaseInvestAccountAPP(){
		Double increase_invest_account = 0.0;//本月新增理财总额
		
		String sql = "select sum(amount) from t_invests where DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') and transfer_status = 0 and bid_id in (select id from t_bids where status > 0) and client = 2";
		try {
			increase_invest_account = t_invests.find(sql).first();//本月新增理财总额
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == increase_invest_account || increase_invest_account == 0){
			increase_invest_account = 0.0;
		}
		return increase_invest_account;
	}
	
	public static Double queryIncreaseInvestAccountWechat(){
		Double increase_invest_account = 0.0;//本月新增理财总额
		
		String sql = "select sum(amount) from t_invests where DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') and transfer_status = 0 and bid_id in (select id from t_bids where status > 0 ) and client = 3";
		try {
			increase_invest_account = t_invests.find(sql).first();//本月新增理财总额
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == increase_invest_account || increase_invest_account == 0){
			increase_invest_account = 0.0;
		}
		return increase_invest_account;
	}
	
	
	public static long queryInvestUseraccount(){
		long invest_user_account = 0;//累计理财会员数
		String sql = "select count(*) from t_users where master_identity in (2,3)";

		try {
			invest_user_account = t_users.find(sql).first();// 累计理财会员数
		} catch (Exception e) {
			e.printStackTrace();
		}
		return invest_user_account;
		
	}
	
	public static long queryInvestUseraccountPC(){
		long invest_user_account = 0;//累计理财会员数
		String sql = "select count(*) from t_users where master_identity in (2,3) and master_client = 1";

		try {
			invest_user_account = t_users.find(sql).first();// 累计理财会员数
		} catch (Exception e) {
			e.printStackTrace();
		}
		return invest_user_account;
		
	}
	
	public static long queryInvestUseraccountAPP(){
		long invest_user_account = 0;//累计理财会员数
		String sql = "select count(*) from t_users where master_identity in (2,3) and master_client = 2";

		try {
			invest_user_account = t_users.find(sql).first();// 累计理财会员数
		} catch (Exception e) {
			e.printStackTrace();
		}
		return invest_user_account;
		
	}
	
	public static long queryInvestUseraccountWechat(){
		long invest_user_account = 0;//累计理财会员数
		String sql = "select count(*) from t_users where master_identity in (2,3) and master_client = 3";

		try {
			invest_user_account = t_users.find(sql).first();// 累计理财会员数
		} catch (Exception e) {
			e.printStackTrace();
		}
		return invest_user_account;
		
	}
	
	public static long queryIncreaseInvestUseraccount(){
		long increase_invest_user_account = 0;//新增理财会员数
		
		String sql = "SELECT count(*) FROM t_users WHERE DATE_FORMAT(master_time_invest, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m') or (master_time_invest = null and master_identity = 3 and date_format(master_time_complex,'%Y%m') = date_format(curdate(),'%Y%m'))";

		try {
			increase_invest_user_account = t_users.find(sql).first();// 新增理财会员数
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return increase_invest_user_account;
	}
	
	public static long queryIncreaseInvestUseraccountPC(){
		long increase_invest_user_account = 0;//新增理财会员数
		
		String sql = "SELECT count(*) FROM t_users WHERE master_client = 1 and DATE_FORMAT(master_time_invest, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m') or (master_time_invest = null and master_identity = 3 and date_format(master_time_complex,'%Y%m') = date_format(curdate(),'%Y%m'))";

		try {
			increase_invest_user_account = t_users.find(sql).first();// 新增理财会员数
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return increase_invest_user_account;
	}
	
	public static long queryIncreaseInvestUseraccountAPP(){
		long increase_invest_user_account = 0;//新增理财会员数
		
		String sql = "SELECT count(*) FROM t_users WHERE master_client = 2 and DATE_FORMAT(master_time_invest, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m') or (master_time_invest = null and master_identity = 3 and date_format(master_time_complex,'%Y%m') = date_format(curdate(),'%Y%m'))";

		try {
			increase_invest_user_account = t_users.find(sql).first();// 新增理财会员数
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return increase_invest_user_account;
	}

	public static long queryIncreaseInvestUseraccountWechat(){
		long increase_invest_user_account = 0;//新增理财会员数
		
		String sql = "SELECT count(*) FROM t_users WHERE master_client = 3 and DATE_FORMAT(master_time_invest, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m') or (master_time_invest = null and master_identity = 3 and date_format(master_time_complex,'%Y%m') = date_format(curdate(),'%Y%m'))";

		try {
			increase_invest_user_account = t_users.find(sql).first();// 新增理财会员数
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return increase_invest_user_account;
	}
	
	/**
	 * 本月新增会员中的理财会员数
	 * @return
	 */
	public static long queryInvestUserCountOfNewUser(){
		long increase_invest_user_account = 0;//本月新增会员中的理财会员数
		
		String sql = "SELECT count(*) FROM t_users WHERE DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m') and DATE_FORMAT(master_time_invest, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m') or (master_time_invest = null and master_identity = 3 and date_format(master_time_complex,'%Y%m') = date_format(curdate(),'%Y%m'))";

		try {
			increase_invest_user_account = t_users.find(sql).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return increase_invest_user_account;
	}
	
	/**
	 * 查询本月投标人数
	 * @return
	 */
	public static long queryMonthInvestUserCount(){
		String sql = "select count(distinct user_id) from t_invests where transfer_status = 0 and bid_id in (select id from t_bids where status > 0) and DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m')";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询逾期借款标数量时："+e.getMessage());
			
			return 0;
		}
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询PC本月投标人数
	 * @return
	 */
	public static long queryMonthInvestUserCountPC(){
		String sql = "select count(distinct user_id) from t_invests where transfer_status = 0 and bid_id in (select id from t_bids where status > 0) and DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') AND client = 1";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询逾期借款标数量时："+e.getMessage());
			
			return 0;
		}
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询APP本月投标人数
	 * @return
	 */
	public static long queryMonthInvestUserCountAPP(){
		String sql = "select count(distinct user_id) from t_invests where transfer_status = 0 and bid_id in (select id from t_bids where status > 0) and DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') AND client = 2";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询逾期借款标数量时："+e.getMessage());
			
			return 0;
		}
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * wechat查询本月投标人数
	 * @return
	 */
	public static long queryMonthInvestUserCountWechat(){
		String sql = "select count(distinct user_id) from t_invests where transfer_status = 0 and bid_id in (select id from t_bids where status > 0) and DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') AND client = 3";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询逾期借款标数量时："+e.getMessage());
			
			return 0;
		}
		
		return Convert.strToInt(obj+"", 0);
	}
	

	public static Double queryIncreaseInvestUserblance(){
		
		Double temp = 0.0;
		
		String sql = "select sum(balance) FROM t_users WHERE DATE_FORMAT(master_time_invest, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m') or (master_time_invest = null and master_identity = 3 and date_format(master_time_complex,'%Y%m') = date_format(curdate(),'%Y%m'))";
		
		if(Constants.IPS_ENABLE) {
			sql = "select sum(balance + balance2) FROM t_users WHERE DATE_FORMAT(master_time_invest, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m') or (master_time_invest = null and master_identity = 3 and date_format(master_time_complex,'%Y%m') = date_format(curdate(),'%Y%m'))";
		} 
		
		try {
			temp = t_users.find(sql).first();//新增理财会员总余额
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == temp || temp == 0){
			temp = 0.0;
		}
		
		return temp;
	}
	
	
	
	public static long queryAllUser(){
		long temp = 0;
		
		String sql = "select count(*) from t_users where DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m')";
		try {
			temp = t_users.find(sql).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return temp;
	}
	
	
	
	public static Double queryBidCount(){
		Double temp = 0.0;
		
		String sql = "select sum(amount) from t_invests where DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') ";

		try {
			temp = t_invests.find(sql).first();// 借款标总额
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null ==temp || temp == 0){
			temp = 0.0;
		}
		
		return temp;
	}
	
	
	public static long queryInvestCount(){
		long investCount = 0;
		String sql = "select count(*) from t_invests where DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m')";

		try {
			investCount = t_invests.find(sql).first();// 投标数
		} catch (Exception e) {
			e.printStackTrace();
		}
		return investCount;
	}
	
	public static long queryDebtAccount(){
		
		long debtAccount = 0;
		String sql = "select count(*) from t_invest_transfer_details where DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m')";
		
		try {
			debtAccount = t_invest_transfer_details.find(sql).first();// 该月所有投资的债权标总和
		} catch (Exception e) {
			e.printStackTrace();
		}

		return debtAccount;
	}
	
	
	
	public static long queryDebtNum(){
		long debtNum = 0;
		String sql = "select COUNT(DISTINCT user_id) from t_invest_transfer_details where DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') ";
		try {
			debtNum = t_invest_transfer_details.find(sql).first();//该月 所有投资债权标的 人数 总和
		} catch (Exception e) {
			e.printStackTrace();
		}
		return debtNum;
	}
	
	
	//判断记录是否存在
	public static boolean judgeIsnew(int year,int month){
		t_statistic_financial_situation invest = null;
		
		try {
			invest = t_statistic_financial_situation.find(" year = ? and month = ?", year,month).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == invest){
			return true;
	    }
		return false;
	}
	
	
	public static t_statistic_financial_situation getTarget(int year,int month){
		t_statistic_financial_situation invest = null;

		try {
			invest = t_statistic_financial_situation.find(
					" year = ? and month = ?", year, month).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return invest;
	}
	 
	
	
	/**
	 * 平台收入统计表
	 */
	public static void platformIncomeStatistic(){
		
		
         Calendar cal=Calendar.getInstance();//使用日历类  
	    
	     int year = cal.get(Calendar.YEAR);//得到年  
	     int month = cal.get(Calendar.MONTH)+1;//得到月，因为从0开始的，所以要加1  
	     int day = cal.get(Calendar.DAY_OF_MONTH);//得到天
		 Double loan_manage_fee = StatisticPlatformIncome.queryLoanManagefee();//借款管理费
		 Double recharge_manage_fee = StatisticPlatformIncome.queryRechargeManagefee();//充值手续费
		 Double withdraw_manage_fee = StatisticPlatformIncome.queryWithdrawManagefee();//提现手续费
		 Double vip_manage_fee = StatisticPlatformIncome.queryVipManagefee();//VIP会员费
		 Double invest_manage_fee = StatisticPlatformIncome.queryInvestManagefee();//理财管理费
		 Double debt_transfer_manage_fee = StatisticPlatformIncome.queryDebtTransferManagefee();//债权转让管理费
		 Double overdue_manage_fee = StatisticPlatformIncome.queryOverdueManagefee();//逾期管理费
		 Double item_audit_manage_fee = StatisticPlatformIncome.queryItemauditManagefee();//资料审核费
		 double income_sum = loan_manage_fee + recharge_manage_fee
				+ withdraw_manage_fee + vip_manage_fee + invest_manage_fee
				+ debt_transfer_manage_fee + overdue_manage_fee
				+ item_audit_manage_fee;//平台总收入
		
		 boolean flag = StatisticPlatformIncome.judgeIsnew(year, month, day);
		 
		 if(flag){
			 t_statistic_platform_income statistic = new t_statistic_platform_income();
				
			 statistic.year = year; //得到年  
			 statistic.month = month;//得到月，因为从0开始的，所以要加1  
			 statistic.day = day;//得到天
			 statistic.income_sum = income_sum;//收入总额
			 statistic.loan_manage_fee = loan_manage_fee;//借款管理费
			 statistic.recharge_manage_fee = recharge_manage_fee;//充值手续费
			 statistic.withdraw_manage_fee = withdraw_manage_fee;//提现手续费
			 statistic.vip_manage_fee = vip_manage_fee;//VIP会员费
			 statistic.invest_manage_fee = invest_manage_fee;//理财管理费
			 statistic.debt_transfer_manage_fee = debt_transfer_manage_fee;//债权转让管理费
			 statistic.overdue_manage_fee = overdue_manage_fee;//逾期管理费
			 statistic.item_audit_manage_fee = item_audit_manage_fee;//资料审核费
			 
			 try {
				 statistic.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		 }else{
			 t_statistic_platform_income statistic = StatisticPlatformIncome.getTarget(year, month, day);
			 statistic.income_sum = income_sum;//收入总额
			 statistic.loan_manage_fee = loan_manage_fee;//借款管理费
			 statistic.recharge_manage_fee = recharge_manage_fee;//充值手续费
			 statistic.withdraw_manage_fee = withdraw_manage_fee;//提现手续费
			 statistic.vip_manage_fee = vip_manage_fee;//VIP会员费
			 statistic.invest_manage_fee = invest_manage_fee;//理财管理费
			 statistic.debt_transfer_manage_fee = debt_transfer_manage_fee;//债权转让管理费
			 statistic.overdue_manage_fee = overdue_manage_fee;//逾期管理费
			 statistic.item_audit_manage_fee = item_audit_manage_fee;//资料审核费
			 
			 try {
				 statistic.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		 }
		
	}
	
	/**
	 * 系统提现
	 */
	public static void platformWithdrawStatistic(){
		
		Calendar cal=Calendar.getInstance();//使用日历类  
		    
		int year = cal.get(Calendar.YEAR);// 得到年
		int month = cal.get(Calendar.MONTH) + 1;// 得到月，因为从0开始的，所以要加1
		int day = cal.get(Calendar.DAY_OF_MONTH);// 得到天
		Long payment_number = StatisticPlatformWithdraw.queryPaymentNumber();//付款笔数
		Double payment_sum = StatisticPlatformWithdraw.queryPaymentSum();//付款总额
		Long apply_withdraw_account = StatisticPlatformWithdraw.queryApplyWithdraw();//申请提现笔数（含付款中）
		Double apply_withdraw_sum = StatisticPlatformWithdraw.queryApplyWithdrawSum();//申请提现总额
		Double average_withdraw_amount = 0.0;//均申请提现金额
		Double max_withdraw_amount = StatisticPlatformWithdraw.queryMaxAmount();//最高申请提现金额
		Double min_withdraw_amount = StatisticPlatformWithdraw.queryMinAmount();//最低申请提现金额
		
		if(apply_withdraw_account > 0){
			average_withdraw_amount = Arith.div(apply_withdraw_sum, apply_withdraw_account, 2);//均申请提现金额
		}else{
			average_withdraw_amount = 0.0;
		}
		
		boolean flag = StatisticPlatformWithdraw.judgeIsNew(year,month,day);
		
		if(flag){
			t_statistic_withdraw statistic = new t_statistic_withdraw();
			statistic.year = year;
			statistic.month = month;// 得到月，因为从0开始的，所以要加1
			statistic.day = day;// 得到天
			statistic.payment_number = payment_number;//付款笔数
			statistic.payment_sum = payment_sum;//付款总额
			statistic.apply_withdraw_account = apply_withdraw_account;//申请提现笔数（含付款中）
			statistic.apply_withdraw_sum = apply_withdraw_sum;//申请提现总额
			statistic.average_withdraw_amount = average_withdraw_amount;//均申请提现金额
			statistic.max_withdraw_amount = max_withdraw_amount;//最高申请提现金额
			statistic.min_withdraw_amount = min_withdraw_amount;//最低申请提现金额
			
			try {
				statistic.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			t_statistic_withdraw statistic = StatisticPlatformWithdraw.getTarget(year, month,day);
			
			statistic.payment_number = payment_number;//付款笔数
			statistic.payment_sum = payment_sum;//付款总额
			statistic.apply_withdraw_account = apply_withdraw_account;//申请提现笔数（含付款中）
			statistic.apply_withdraw_sum = apply_withdraw_sum;//申请提现总额
			statistic.average_withdraw_amount = average_withdraw_amount;//均申请提现金额
			statistic.max_withdraw_amount = max_withdraw_amount;//最高申请提现金额
			statistic.min_withdraw_amount = min_withdraw_amount;//最低申请提现金额
			
			try {
				statistic.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	
	/**
	 * 平台浮存金统计
	 */
	public static void platformFloatstatistics(){
		
	    Calendar cal=Calendar.getInstance();//使用日历类  
	    
	    int year=cal.get(Calendar.YEAR);//得到年  
	    int month=cal.get(Calendar.MONTH)+1;//得到月，因为从0开始的，所以要加1  
	    int day=cal.get(Calendar.DAY_OF_MONTH);//得到天
		Double balance_float_sum = StatisticPlatformFloat.queryBalanceFloatsum();//账户可用余额浮存
		Double freeze_float_sum = StatisticPlatformFloat.queryFreezeFloatsum();//冻结资金浮存
		Double float_sum = 0.0;//浮存金总额
		Long has_balance_user_account = StatisticPlatformFloat.queryHasBalanceUseraccount();//有可用余额账户数量
		double average_balance = 0;//均账户余额
		Long has_balance_vip_user_account = StatisticPlatformFloat.queryHasBalancevipUseraccount();//有可用余额的VIP账户数量
		Double vip_balance_float = StatisticPlatformFloat.queryVipBalancefloat();//VIP账户可用余额浮存
		double average_vip_balance = 0.0;//VIP均账户余额
		  
		float_sum = freeze_float_sum + balance_float_sum;//浮存金总额
		
		if(has_balance_user_account > 0){
			average_balance = Arith.div(balance_float_sum, has_balance_user_account, 2);//均账户余额
		}else{
			average_balance = 0;
		}
		
		if(has_balance_vip_user_account > 0){
			 average_vip_balance = Arith.div(vip_balance_float, has_balance_vip_user_account, 2);//VIP均账户余额
		}else{
			average_vip_balance = 0;
		}
		
		boolean flag = StatisticPlatformFloat.judgeIsnew(year, month, day);
		
		if(flag){
			t_statistic_platform_float statistic = new t_statistic_platform_float();
			   statistic.year = year;//得到年  
			   statistic.month = month;//得到月，因为从0开始的，所以要加1  
			   statistic.day = day;//得到天
			   statistic.balance_float_sum = balance_float_sum;//账户可用余额浮存
			   statistic.freeze_float_sum = freeze_float_sum;//冻结资金浮存
			   statistic.float_sum = float_sum;//浮存金总额
			   statistic.has_balance_user_account = has_balance_user_account;//有可用余额账户数量
			   statistic.average_balance = average_balance;//均账户余额
			   statistic.has_balance_vip_user_account = has_balance_vip_user_account;//有可用余额的VIP账户数量
			   statistic.vip_balance_float = vip_balance_float;//VIP账户可用余额浮存
			   statistic.average_vip_balance = average_vip_balance;//VIP均账户余额
			   
			try {
				statistic.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			t_statistic_platform_float statistic = StatisticPlatformFloat.getTarget(year, month, day);
			   
			   statistic.balance_float_sum = balance_float_sum;//账户可用余额浮存
			   statistic.freeze_float_sum = freeze_float_sum;//冻结资金浮存
			   statistic.float_sum = float_sum;//浮存金总额
			   statistic.has_balance_user_account = has_balance_user_account;//有可用余额账户数量
			   statistic.average_balance = average_balance;//均账户余额
			   statistic.has_balance_vip_user_account = has_balance_vip_user_account;//有可用余额的VIP账户数量
			   statistic.vip_balance_float = vip_balance_float;//VIP账户可用余额浮存
			   statistic.average_vip_balance = average_vip_balance;//VIP均账户余额
			   
				try {
					statistic.save();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}
	
	
	
	/**
	 * 理财情况统计表
	 */
	public static void investSituationStatistic(){
		Calendar now = Calendar.getInstance(); 
		int year = now.get(Calendar.YEAR);  
		int month = now.get(Calendar.MONTH) + 1;
		Double invest_accoumt = StatisticInvest.queryInvestAccoumt();//累计理财投标总额
		Double invest_accoumt_pc = StatisticInvest.queryInvestAccoumtPC();//累计理财投标总额
		Double invest_accoumt_app = StatisticInvest.queryInvestAccoumtAPP();//累计理财投标总额
		Double invest_accoumt_wechat = StatisticInvest.queryInvestAccoumtWechat();//累计理财投标总额
		Double increase_invest_account = StatisticInvest.queryIncreaseInvestAccount();//本月新增理财总额
		Double increase_invest_account_pc = StatisticInvest.queryIncreaseInvestAccountPC();//pc本月新增理财总额
		Double increase_invest_account_app = StatisticInvest.queryIncreaseInvestAccountAPP();//APP本月新增理财总额
		Double increase_invest_account_wechat = StatisticInvest.queryIncreaseInvestAccountWechat();//WECHAT本月新增理财总额
		long monthInvestUserCount = StatisticInvest.queryMonthInvestUserCount();//本月投标人数
		long monthInvestUserCountPC = StatisticInvest.queryMonthInvestUserCountPC();//PC本月投标人数
		long monthInvestUserCountAPP = StatisticInvest.queryMonthInvestUserCountAPP();//APP本月投标人数
		long monthInvestUserCountWechat = StatisticInvest.queryMonthInvestUserCountWechat();//Wechat本月投标人数
		long invest_user_account = StatisticInvest.queryInvestUseraccount();//累计理财会员数
		long invest_user_account_pc = StatisticInvest.queryInvestUseraccountPC();//PC累计理财会员数
		long invest_user_account_app = StatisticInvest.queryInvestUseraccountAPP();//app累计理财会员数
		long invest_user_account_wechat = StatisticInvest.queryInvestUseraccountWechat();//wechat累计理财会员数
		long increase_invest_user_account = StatisticInvest.queryIncreaseInvestUseraccount();//新增理财会员数
		//人均理财金额 = 投标总额/理财会员数
		double per_capita_invest_amount = invest_user_account == 0 ? 0 : Arith.div(invest_accoumt, invest_user_account, 2);//人均理财金额
		double per_bid_average_invest_amount = 0;//每标人均投标金额（去掉，无法统计）
		double per_capita_invest_debt = 0;//人均投资债权标数量（无意义，去掉）
		long increase_invest_user_account_pc = StatisticInvest.queryIncreaseInvestUseraccountPC();//PC新增理财会员数
		long increase_invest_user_account_app = StatisticInvest.queryIncreaseInvestUseraccountAPP();//APP新增理财会员数
		long increase_invest_user_account_wechat = StatisticInvest.queryIncreaseInvestUseraccountWechat();//WECHAT新增理财会员数
		
		double per_capita_invest_amount_pc = 0;
		double per_capita_invest_amount_app = 0;
		double per_capita_invest_amount_wechat = 0;
		if(monthInvestUserCountPC > 0){
			per_capita_invest_amount_pc = monthInvestUserCount == 0 ? 0 : Arith.div(increase_invest_account_pc, monthInvestUserCountPC, 2);//人均理财金额
		}
		
		if(monthInvestUserCountAPP > 0){
			per_capita_invest_amount_app = monthInvestUserCount == 0 ? 0 : Arith.div(increase_invest_account_app, monthInvestUserCountAPP, 2);//人均理财金额
		}
		
		if(monthInvestUserCountWechat > 0){
			per_capita_invest_amount_wechat = monthInvestUserCount == 0 ? 0 : Arith.div(increase_invest_account_wechat, monthInvestUserCountWechat, 2);//人均理财金额
		}

		double per_capita_balance = 0;//新增人均账户余额
		double invest_user_conversion = 0;//理财会员转化率
		
		Double temp = StatisticInvest.queryIncreaseInvestUserblance();//新增理财会员总余额
		
		Double balance_float_sum = StatisticPlatformFloat.queryBalanceFloatsum();//账户可用余额浮存
		Double freeze_float_sum = StatisticPlatformFloat.queryFreezeFloatsum();//冻结资金浮存
		Double float_sum = 0.0;//浮存金总额
		float_sum = freeze_float_sum + balance_float_sum;//浮存金总额
		long users = queryAllUsers();//所有会员人数 
		
		if(users > 0){
			 per_capita_balance = Arith.div(float_sum, users, 2);//人均账户余额
		}else{
			per_capita_balance = 0;
		}
		
		long t = StatisticInvest.queryAllUser();//所有新增会员
		
		//会员转换率=新增理财会员数/(会员总数-累计理财会员数)
		if((users -invest_user_account) > 0){
			invest_user_conversion = Arith.div(increase_invest_user_account, (users -invest_user_account), 4)*100;//理财会员转化率
		}else{
			invest_user_conversion = 0;
		}
		
		Double bidCount = StatisticInvest.queryBidCount();//// 借款标总额
		
		
		long investCount = StatisticInvest.queryInvestCount();// 投标数
		
		if(investCount > 0){
			per_bid_average_invest_amount = Arith.div(bidCount, investCount, 2);//每标人均投标金额
		}else{
			per_bid_average_invest_amount = 0;
		}
		
		long debtAccount = StatisticInvest.queryDebtAccount();// 该月所有投资的债权标总和
		long debtNum = StatisticInvest.queryDebtNum();//该月 所有投资债权标的 人数 总和
		
		if(debtNum > 0){
			per_capita_invest_debt = Arith.div(debtAccount, debtNum, 2);//人均投资债权标数量
		}else{
			per_capita_invest_debt = 0;
		}
		
		boolean flag = StatisticInvest.judgeIsnew(year, month);
		
		if(flag){
			t_statistic_financial_situation statistic = new t_statistic_financial_situation();
			statistic.year = year;
			statistic.month = month;
			statistic.invest_accoumt = invest_accoumt;//累计理财投标总额
			statistic.invest_accoumt_pc = invest_accoumt_pc;//pc累计理财投标总额
			statistic.invest_accoumt_app = invest_accoumt_app;//app累计理财投标总额
			statistic.invest_accoumt_wechat = invest_accoumt_wechat;//wechat累计理财投标总额
			statistic.increase_invest_account = increase_invest_account;//本月新增理财总额
			statistic.increase_invest_account_pc = increase_invest_account_pc;//_pc本月新增理财总额
			statistic.increase_invest_account_app = increase_invest_account_app;//_app本月新增理财总额
			statistic.increase_invest_account_wechat = increase_invest_account_wechat;//_wechat本月新增理财总额
			statistic.invest_user_account = invest_user_account;//累计理财会员数
			statistic.invest_user_account_pc = invest_user_account_pc;//累计理财会员数
			statistic.invest_user_account_app = invest_user_account_app;//累计理财会员数
			statistic.invest_user_account_wechat = invest_user_account_wechat;//累计理财会员数
			statistic.increase_invest_user_account = increase_invest_user_account;//新增理财会员数
			statistic.increase_invest_user_account_pc = increase_invest_user_account_pc;//pc新增理财会员数
			statistic.increase_invest_user_account_app = increase_invest_user_account_app;//app新增理财会员数
			statistic.increase_invest_user_account_wechat = increase_invest_user_account_wechat;//wechat新增理财会员数
			statistic.per_capita_invest_amount = per_capita_invest_amount;//人均理财金额
			statistic.per_capita_invest_amount_pc = per_capita_invest_amount_pc;//人均理财金额
			statistic.per_capita_invest_amount_app = per_capita_invest_amount_app;//人均理财金额
			statistic.per_capita_invest_amount_wechat = per_capita_invest_amount_wechat;//人均理财金额
			statistic.per_bid_average_invest_amount = per_bid_average_invest_amount;//每标人均投标金额
			statistic.per_capita_invest_debt = per_capita_invest_debt;//人均投资债权标数量
			statistic.per_capita_balance = per_capita_balance;//新增人均账户余额
			statistic.invest_user_conversion = invest_user_conversion;//理财会员转化率
			
			try {
				statistic.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			t_statistic_financial_situation statistic = StatisticInvest.getTarget(year, month);
			
			statistic.invest_accoumt = invest_accoumt;//累计理财投标总额
			statistic.invest_accoumt_pc = invest_accoumt_pc;//pc累计理财投标总额
			statistic.invest_accoumt_app = invest_accoumt_app;//app累计理财投标总额
			statistic.invest_accoumt_wechat = invest_accoumt_wechat;//wechat累计理财投标总额
			statistic.increase_invest_account = increase_invest_account;//本月新增理财总额
			statistic.increase_invest_account_pc = increase_invest_account_pc;//_pc本月新增理财总额
			statistic.increase_invest_account_app = increase_invest_account_app;//_app本月新增理财总额
			statistic.increase_invest_account_wechat = increase_invest_account_wechat;//_wechat本月新增理财总额
			statistic.invest_user_account = invest_user_account;//累计理财会员数
			statistic.invest_user_account_pc = invest_user_account_pc;//累计理财会员数
			statistic.invest_user_account_app = invest_user_account_app;//累计理财会员数
			statistic.invest_user_account_wechat = invest_user_account_wechat;//累计理财会员数
			statistic.increase_invest_user_account = increase_invest_user_account;//新增理财会员数
			statistic.increase_invest_user_account_pc = increase_invest_user_account_pc;//pc新增理财会员数
			statistic.increase_invest_user_account_app = increase_invest_user_account_app;//app新增理财会员数
			statistic.increase_invest_user_account_wechat = increase_invest_user_account_wechat;//wechat新增理财会员数
			statistic.per_capita_invest_amount = per_capita_invest_amount;//人均理财金额
			statistic.per_capita_invest_amount_pc = per_capita_invest_amount_pc;//人均理财金额
			statistic.per_capita_invest_amount_app = per_capita_invest_amount_app;//人均理财金额
			statistic.per_capita_invest_amount_wechat = per_capita_invest_amount_wechat;//人均理财金额
			statistic.per_bid_average_invest_amount = per_bid_average_invest_amount;//每标人均投标金额
			statistic.per_capita_invest_debt = per_capita_invest_debt;//人均投资债权标数量
			statistic.per_capita_balance = per_capita_balance;//新增人均账户余额
			statistic.invest_user_conversion = invest_user_conversion;//理财会员转化率
			
			try {
				statistic.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 查询目前年月的所有会员数目
	 * @return
	 */
	public static long queryAllUsers(){
		long temp = 0;
		
		String sql = "select count(*) from t_users ";
		try {
			temp = t_users.find(sql).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return temp;
	}
	
	/**
	 * Echarts获取理财金额数据
	 * 
	 */
	public static double totalInvestByDate(ErrorInfo error,String startTime,String endTime,int type){
		error.clear();
		
		String sql = null;
		int hour = 0;
		
		if (type==Constants.YESTERDAY) {
			sql = "SELECT SUM(amount) FROM t_invests WHERE transfers_id NOT IN(SELECT id FROM t_invest_transfers WHERE status = 3 ) AND TO_DAYS(NOW()) - TO_DAYS(time) = 1 AND HOUR(time) <= ?";
			if (endTime.contains(":")) {
				hour = Integer.parseInt(endTime.substring(0, endTime.indexOf(":")));
			}
			if(0 == hour){
				hour = 24;
			}
		}else{
			sql="SELECT SUM(amount) FROM t_invests WHERE transfers_id NOT IN(SELECT id FROM t_invest_transfers WHERE status = 3 ) AND time>= ? AND time<= ?";
		}
		
		Double totalAmount = null;
		
		try {
			if (type==Constants.YESTERDAY) {
				totalAmount = t_statistic_recharge.find(sql,hour).first();
			}else{
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				totalAmount = t_statistic_recharge.find(sql,format.parse(startTime),format.parse(endTime)).first();
			}
		 } catch(Exception e) {
				e.printStackTrace();
				Logger.info("统计充值总额时："+e.getMessage());
				error.code = -1;
				error.msg = "数据库异常，导致统计充值总额失败";
				
				return error.code;
			}
		
		return (totalAmount == null) ? 0 : Convert.strToDouble(totalAmount+"", 0);
	}
	
}
