package reports;

import java.util.Calendar;
import constants.DealType;
import play.Logger;
import models.t_bids;
import models.t_invests;
import models.t_statistic_cps;
import models.t_user_cps_income;
import models.t_user_recharge_details;
import models.t_users;

public class StatisticCPS {
	
	/**
	 * 查询当月CPS会员数
	 * @return
	 */
	public static Long queryCpscount(){
		Long cpscount = 0l;
		
		String sql = "select count(distinct recommend_user_id) from t_users where DATE_FORMAT(recommend_time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m')";
		
		try {
			cpscount = t_users.find(sql).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			
			return 0l;
		}
		if(null == cpscount){
			cpscount = 0l;
		}
		return cpscount;
	}
	
	
	/**
	 * 查询当月CPS注册会员数
	 * @return
	 */
	public static Long queryRecommendcount(){
		Long recommendcount = 0l;
		
		String sql = "select count(*) from t_users where DATE_FORMAT(recommend_time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m')";
		
		try {
			recommendcount = t_users.find(sql).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			
			return 0l;
		}
		
		if(null == recommendcount){
			recommendcount = 0l;
		}
		return recommendcount;
	}
	
	/**
	 * 查询当月推广充值会员数
	 * @return
	 */
	public static Long queryRechargecount(){
		Long recharge_count = 0l;
		String sql =  "select count(distinct user_id) from t_user_recharge_details where user_id in (select id from t_users where DATE_FORMAT(recommend_time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m'))";

		try {
			recharge_count = t_user_recharge_details.find(sql).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());

			return 0l;
		}

		if (null == recharge_count) {
			recharge_count = 0l;
		}
		
		return recharge_count;
	}
	
	/**
	 * 查询投资总额
	 * @return
	 */
	public static Double queryInvestAmount(){
		Double investAmount = 0.0;
		String sql =  "select sum(amount) from t_invests where user_id in (select id from t_users where DATE_FORMAT(recommend_time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m')) ";

		try {
			investAmount = t_invests.find(sql).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			return 0.0;
		}
		
		if(null == investAmount){
			investAmount = 0.0;
		}
		
		return investAmount;
	}
	
	/**
	 * 查询借款金额
	 * @return
	 */
	public static Double queryBidAmount(){
		Double investAmount = 0.0;
		
		String sql =  "select sum(amount) from t_bids where user_id in (select id from t_users where DATE_FORMAT(recommend_time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m'))";

		try {
			investAmount = t_bids.find(sql).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			return 0.0;
		}

		if (null == investAmount) {
			investAmount = 0.0;
		}

		return investAmount;
	}
	

	/**
	 * 查询管理费
	 * @return
	 */
	public static Double queryManageFee(){
		Double amount = 0.0;
		String sql =  "select sum(amount) from t_user_details where operation in (?,?,?,?,?,?,?,?) and user_id in (select id from t_users where DATE_FORMAT(recommend_time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m'))";

		try {
			amount = t_user_cps_income.find(sql, DealType.CHARGE_RECHARGE_FEE, DealType.CHARGE_WITHDRAWALT, DealType.CHARGE_VIP, DealType.CHARGE_AUDIT_ITEM
					, DealType.CHARGE_LOAN_SERVER_FEE, DealType.CHARGE_INVEST_FEE, DealType.CHARGE_DEBT_TRANSFER_MANAGEFEE, DealType.CHARGE_OVERDUE_FEE).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			
			return 0.0;
		}
		
		if(null == amount){
			amount = 0.0;
		}
		
		return amount;
	}
	
	/**
	 * 查询cps资金
	 * @return
	 */
	public static Double queryCpsAmount(int year,int month){
		Double amount = 0.0;
		
		String sql = "select sum(cps_reward) from t_user_cps_income where year = ? and month = ?";
		
		try {
			amount = t_user_cps_income.find(sql, year,month).first();
			
		} catch (Exception e) {
			Logger.error(e.getMessage());
			
			return 0.0;
		}
		
		if(null == amount){
			amount = 0.0;
		}
		
		return amount;
	}
	
	/**
	 * 插入记录
	 */
	public static void saveOrUpdateRecord(){
		

		Calendar cal=Calendar.getInstance();//使用日历类  
		    
		int year = cal.get(Calendar.YEAR);// 得到年
		int month = cal.get(Calendar.MONTH) + 1;// 得到月，因为从0开始的，所以要加1
		
		long cps_count = queryCpscount();
		long recommend_count = queryRecommendcount();
		long recharge_count = queryRechargecount();
		double invest_amount = queryInvestAmount();
		double bid_amount = queryBidAmount();
		double manage_fee = queryManageFee();
		double cps_amount = queryCpsAmount(year, month);
		
		t_statistic_cps cps = null;
		
		try {
			cps = t_statistic_cps.find(" year = ? and month = ? ", year,month).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
		
		if(null == cps){
			cps = new t_statistic_cps();
		}
		
		cps.year = year;
		cps.month = month;
		cps.cps_count = cps_count;
		cps.recommend_count = recommend_count;
		cps.recharge_count = recharge_count;
		cps.invest_amount = invest_amount;
		cps.bid_amount = bid_amount;
		cps.manage_fee = manage_fee;
		cps.cps_amount = cps_amount;
		
		try {
			cps.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
	}
}
