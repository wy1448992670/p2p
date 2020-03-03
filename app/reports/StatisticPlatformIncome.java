package reports;

import models.t_statistic_platform_income;
import models.t_user_details;
import constants.DealType;


/**
 * 平台收入情况统计分析表
 * @author liuwenhui
 *
 */
public class StatisticPlatformIncome {
	
	 public static Double queryLoanManagefee(){
		 
		Double loan_manage_fee = 0.0;//借款管理费
		String sql = "select sum(amount) from t_user_details where operation = ?  and DATE_FORMAT(time, '%Y%m%d') = DATE_FORMAT(CURDATE(),'%Y%m%d')";

		try {
			loan_manage_fee = t_user_details.find(sql,
					DealType.CHARGE_LOAN_SERVER_FEE).first();// 借款管理费
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (null == loan_manage_fee || loan_manage_fee == 0) {
			loan_manage_fee = 0.0;
		}
		
		return loan_manage_fee;
	 }
	 
	 
	 
	 public static Double queryRechargeManagefee(){
		 Double recharge_manage_fee = 0.0;//充值手续费
		 String sql = "select sum(amount) from t_user_details where operation = ?  and DATE_FORMAT(time, '%Y%m%d') = DATE_FORMAT(CURDATE(),'%Y%m%d')";
		 try {
			 recharge_manage_fee = t_user_details.find(sql, DealType.CHARGE_RECHARGE_FEE).first();//充值手续费
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == recharge_manage_fee || recharge_manage_fee == 0){
			recharge_manage_fee = 0.0;
		}
		
		return recharge_manage_fee;
	 }
	
	 
	 public static Double queryWithdrawManagefee(){
		 Double withdraw_manage_fee = 0.0;//提现手续费
		 String sql = "select sum(amount) from t_user_details where operation = ?  and DATE_FORMAT(time, '%Y%m%d') = DATE_FORMAT(CURDATE(),'%Y%m%d')";
		 try {
			 withdraw_manage_fee = t_user_details.find(sql, DealType.CHARGE_WITHDRAWALT).first();//提现手续费
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == withdraw_manage_fee || withdraw_manage_fee == 0){
			withdraw_manage_fee = 0.0;
		}
		
		return withdraw_manage_fee;
	 }
	 
	 public static Double queryVipManagefee(){
		 Double vip_manage_fee = 0.0;//VIP会员费
		 String sql = "select sum(amount) from t_user_details where operation = ?  and DATE_FORMAT(time, '%Y%m%d') = DATE_FORMAT(CURDATE(),'%Y%m%d')";
		 
		 try {
			 vip_manage_fee = t_user_details.find(sql, DealType.CHARGE_VIP).first();//VIP会员费
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == vip_manage_fee || vip_manage_fee == 0){
			vip_manage_fee = 0.0;
		}
		
		return vip_manage_fee;
	 }
	 
	 
	 public static Double queryInvestManagefee(){
		 Double invest_manage_fee = 0.0;//理财管理费
		 String sql = "select sum(amount) from t_user_details where operation = ?  and DATE_FORMAT(time, '%Y%m%d') = DATE_FORMAT(CURDATE(),'%Y%m%d')";
		 
		 try {
			 invest_manage_fee = t_user_details.find(sql, DealType.CHARGE_INVEST_FEE).first();//理财管理费
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == invest_manage_fee || invest_manage_fee == 0){
			invest_manage_fee = 0.0;
		}
		
		return invest_manage_fee;
	 }
	
	 
	 public static Double queryDebtTransferManagefee(){
		 Double debt_transfer_manage_fee = 0.0;//债权转让管理费
		 String sql = "select sum(amount) from t_user_details where operation = ?  and DATE_FORMAT(time, '%Y%m%d') = DATE_FORMAT(CURDATE(),'%Y%m%d')";
		 
		 try {
			 debt_transfer_manage_fee = t_user_details.find(sql, DealType.CHARGE_DEBT_TRANSFER_MANAGEFEE).first();//债权转让管理费
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == debt_transfer_manage_fee || debt_transfer_manage_fee == 0){
			debt_transfer_manage_fee = 0.0;
		}
		return debt_transfer_manage_fee;
	 }
	 
	 
	 public static Double queryItemauditManagefee(){
		
		 Double item_audit_manage_fee = 0.0;//资料审核费
		 String sql = "select sum(amount) from t_user_details where operation = ?  and DATE_FORMAT(time, '%Y%m%d') = DATE_FORMAT(CURDATE(),'%Y%m%d')";
		 try {
			 item_audit_manage_fee = t_user_details.find(sql, DealType.CHARGE_AUDIT_ITEM).first();//资料审核费
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == item_audit_manage_fee || item_audit_manage_fee == 0){
			item_audit_manage_fee = 0.0;
		}
		return item_audit_manage_fee;
	 }
	
	 
	 public static Double queryOverdueManagefee(){
		 Double overdue_manage_fee = 0.0;//逾期管理费
		 String sql = "select sum(amount) from t_user_details where operation = ?  and DATE_FORMAT(time, '%Y%m%d') = DATE_FORMAT(CURDATE(),'%Y%m%d')";
		 
		try {
			overdue_manage_fee = t_user_details.find(sql,
					DealType.CHARGE_OVERDUE_FEE).first();// 逾期管理费
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (null == overdue_manage_fee || overdue_manage_fee == 0) {
			overdue_manage_fee = 0.0;
		}
		return overdue_manage_fee;
			
	 }
	
	 
	 //判断记录是否存在
	public static boolean judgeIsnew(int year,int month,int day){
		
		t_statistic_platform_income incom = null;
		try {
			 incom = t_statistic_platform_income.find(" year = ? and month = ? and day = ? ", year,month,day).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(null == incom){
			return true;
		}
		return false;
	}
	
	
	//获取对象
	public static t_statistic_platform_income getTarget(int year,int month,int day){
		
		t_statistic_platform_income incom = null ;
		try {
			 incom = t_statistic_platform_income.find(" year = ? and month = ? and day = ?", year,month,day).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return incom;
	}
	
}
