package reports;

import play.db.jpa.JPA;
import constants.Constants;
import models.t_statistic_platform_float;
import models.t_users;

/**
 * 平台浮存金数据统计
 * @author lwh
 *
 */
public class StatisticPlatformFloat {


	/**
	 * 查询所有用户的站岗资金，包括可用余额，提现处理中的冻结金额，投资未满标的冻结金额。
	 * @return
	 */
	public static Double queryZhangangZhijin(){

		Object balanceFloatSum = null;
		String sql = " select a.m1+b.m2+c.m3 money from (SELECT sum(u.balance) m1 from t_users u ) a,(select   IFNULL(sum(a.amount),0) m2 from t_user_withdrawals a INNER JOIN t_users b on a.user_id = b.id where a.`status`in(0,1) ) b,(SELECT IFNULL(sum(a.amount-a.red_amount),0) m3 from t_invests a INNER JOIN t_bids b on a.bid_id = b.id where b.`status` in(1,2,3)) c ";


		try {
			balanceFloatSum = JPA.em().createNativeQuery(sql).getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return balanceFloatSum==null?0:Double.parseDouble(balanceFloatSum.toString());
	}
	
	
	public static Double queryBalanceFloatsum(){
		
		Object balanceFloatSum = null;//账户可用余额浮存
		String sql = "select sum(balance) from t_users ";
		  
		if(Constants.IPS_ENABLE) {
			sql = "select sum(balance + balance2) from t_users ";
		} 
		
		try {
			balanceFloatSum = JPA.em().createNativeQuery(sql).getSingleResult();//账户可用余额浮存
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return balanceFloatSum==null?0:Double.parseDouble(balanceFloatSum.toString());
	}
	
	public static Double queryFreezeFloatsum(){
		Double freeze_float_sum = 0.0;//冻结资金浮存
		

		String sql = "select sum(freeze) from t_users ";

		try {
			freeze_float_sum = t_users.find(sql).first();// 冻结资金浮存
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (null == freeze_float_sum) {
			freeze_float_sum = 0.0;
		}
		
		return freeze_float_sum;
	}
	
	
	public static Long queryHasBalanceUseraccount(){
		Long has_balance_user_account = 0l;//有可用余额账户数量
		

		String sql = "select count(id) from t_users where balance > 0 ";

		if(Constants.IPS_ENABLE) {
			sql = "select count(id) from t_users where balance > 0 or balance2 > 0";
		}
		
		try {
			has_balance_user_account = t_users.find(sql).first();// 有可用余额账户数量
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (null == has_balance_user_account) {
			has_balance_user_account = 0l;
		}
		
		return has_balance_user_account;
	}
	
	
	public static Long queryHasBalancevipUseraccount(){
		Long has_balance_vip_user_account = 0l;//有可用余额的VIP账户数量
		

		String sql = "select count(id) from t_users where balance > 0 and vip_status = 1 ";

		if(Constants.IPS_ENABLE) {
			sql = "select count(id) from t_users where (balance > 0 or balance2 > 0) and vip_status = 1 ";
		}
		
		try {
			has_balance_vip_user_account = t_users.find(sql).first();// 有可用余额的VIP账户数量
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (null == has_balance_vip_user_account) {
			has_balance_vip_user_account = 0l;
		}
		
		return has_balance_vip_user_account;
	}
	
	
	public static Double queryVipBalancefloat(){
		Double vip_balance_float = 0.0;//VIP账户可用余额浮存
		

		String sql = "select sum(balance) from t_users where vip_status = 1 ";

		if(Constants.IPS_ENABLE) {
			sql = "select sum(balance + balance2) from t_users where vip_status = 1 ";
		}
		
		try {
			vip_balance_float = t_users.find(sql).first();// VIP账户可用余额浮存
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (null == vip_balance_float){
			vip_balance_float = 0.0;
		}
		
		return vip_balance_float;
	}
	
	
	//判断对象是否存在
	public static boolean judgeIsnew(int year,int month,int day){
		t_statistic_platform_float floa = null;
		
		try {
			floa = t_statistic_platform_float.find(" year = ? and month = ? and day = ?", year,month,day).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == floa){
			return true;
		}
		return false;
	}
	
	
	//获取对象
	public static t_statistic_platform_float getTarget(int year,int month,int day){
		t_statistic_platform_float floa = null;
		
		try {
			floa = t_statistic_platform_float.find(" year = ? and month = ? and day = ?", year,month,day).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return floa;
	}
}
