package reports;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import constants.Constants;

import play.Logger;
import play.db.jpa.JPA;
import utils.Arith;
import utils.ErrorInfo;
import models.t_bids;
import models.t_bills;
import models.t_invest_transfers;
import models.t_invests;
import models.t_statistic_debt_situation;


/**
 * 债权转让情况数据统计表字段查询
 * @author lwh
 *
 */
public class StatisticDebt {
	
	 double average_debt_amount = 0 ;//转让债权均标金额
	 
	 double deal_percent = 0 ;//转让债权成交率
	 double transfer_percent = 0;//债权转让率
	
	 /**
	  * 查询某一个月的债权转让总金额
	  * @param year
	  * @param month
	  * @param error
	  * @return
	  */
	 public static Double queryTotalDebtAmount(int year, int month, ErrorInfo error){
		 error.clear();
		 
		 Double amount;
		 try {
			amount = t_statistic_debt_situation.find("select t.debt_amount_sum from t_statistic_debt_situation t where t.year = ? and t.month = ?", year, month).first();
		} catch (Exception e) {
			Logger.error("查询某一个月的债权转让总金额时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询某一个月的债权转让总金额时有误！";
			
			return 0d;
		}		 
		 
		 return amount == null ? 0 : amount; 
	 }
	 
	 public  static  long queryDebtAccount(ErrorInfo error){
		 error.clear();
		 
		 long debt_account = 0 ;//债权转让标总数量
		 String sql = "SELECT COUNT(DISTINCT a.bid_id) FROM t_invests a WHERE a.id IN (SELECT b.invest_id FROM t_invest_transfers b WHERE b.`status` = ?)";
		 
		 Query query = JPA.em().createNativeQuery(sql).setParameter(1, Constants.DEBT_SUCCESS);
		 Object obj;
		 
		 try {
			obj = query.getSingleResult()  ;//债权转让标总数量
		} catch (Exception e) {
			Logger.error("查询债权转让标总数量时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询债权转让标总数量时异常！";
			
			return 0;
		}
		
		error.code = 0;
		debt_account = obj == null ? 0 : Long.parseLong(obj.toString());
		
		return debt_account;
	 }
	 
	 public static Double queryDebtAmounSum(ErrorInfo error){
		 error.clear();
		 
		 Double debt_amount_sum = 0.0 ;//债权转让总金额
		 String sql = "select sum(a.debt_amount) from t_invest_transfers a where a.status = ?";
		 try {
			 debt_amount_sum = t_invest_transfers.find(sql, Constants.DEBT_SUCCESS).first() ;//债权转让总金额 
		} catch (Exception e) {
			Logger.error("查询债权转让总金额时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询债权转让总金额时异常！";
			
			return 0d;
		}
		
		error.code = 0;
		if(null == debt_amount_sum || debt_amount_sum == 0 || debt_amount_sum + "" == ""){
			debt_amount_sum = 0.0;
		}
		
		return debt_amount_sum ;
	 }
	 
	
	 
	 public static long queryIncreaseDebtAccount(ErrorInfo error){
		 error.clear();
		 
		 long increase_debt_account  = 0 ;//本月新增转让标数量
		 
		String sql = "SELECT COUNT(DISTINCT b.bid_id) FROM t_invests b WHERE b.id IN (select a.invest_id from t_invest_transfers a where DATE_FORMAT(a.time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') AND DATE_FORMAT(a.start_time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') AND a.`status` NOT IN (?, ?))";
		Object obj;
		
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, Constants.DEBT_NOPASS).setParameter(2, Constants.DEBT_AUDITING);
		
		try {
			obj = query.getSingleResult();// 本月新增转让标数量
		} catch (Exception e) {
			Logger.error("查询本月新增转让标数量时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询本月新增转让标数量时异常！";
			
			return 0;
		}
		
		error.code = 0;
		increase_debt_account = obj == null ? 0 : Long.parseLong(obj.toString()); 
		
		return increase_debt_account;
	 }
	 
	
	 public static Double queryIncreaseDebtAmountSum(ErrorInfo error){
		 error.clear();
		 
		 Double increase_debt_amount_sum = 0.0 ;//本月新增转让总额
		 Object obj;
		 
		String sql = "select SUM(a.debt_amount) from t_invest_transfers a where DATE_FORMAT(a.time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') AND DATE_FORMAT(a.start_time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') AND a.`status` NOT IN (?, ?)";
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, Constants.DEBT_NOPASS).setParameter(2, Constants.DEBT_AUDITING);
		
		try {
			obj = query.getSingleResult();// 本月新增转让总额
		} catch (Exception e) {
			Logger.error("查询本月新增转让总额时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询本月新增转让总额时异常！";
			
			return 0d;
		}

		error.code = 0;
		increase_debt_amount_sum = obj == null ? 0d : Double.parseDouble(obj.toString());
		
		return increase_debt_amount_sum ;
	 }
	 
	
	 public static Long queryHasOverdueDebt(ErrorInfo error){
		error.clear();
		
		Long has_overdue_debt = 0l ;//转让债权标含逾期数量
		Object obj;
		
		String sql = "SELECT COUNT(DISTINCT a.bid_id) FROM t_bills a WHERE a.overdue_mark <> ? AND DATE_FORMAT(a.mark_overdue_time,'%Y%m') <= DATE_FORMAT(CURDATE(),'%Y%m') AND a.bid_id IN (SELECT DISTINCT b.bid_id FROM t_invests b WHERE b.id IN (SELECT c.invest_id FROM t_invest_transfers c WHERE c.status = ?))";
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, Constants.BILL_NO_OVERDUE).setParameter(2, Constants.DEBT_SUCCESS);
		
		try {
			obj = query.getSingleResult();
		} catch (Exception e) {
			Logger.error("查询转让债权标含逾期数量时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询转让债权标含逾期数量时：异常！";
			
			return 0l;
		}
		
		error.code = 0;
		has_overdue_debt = obj == null ? 0l : Long.parseLong(obj.toString());
		
		return has_overdue_debt;
	 }
	 
	
	 public static long querySuccessDebtAmount(ErrorInfo error){
		error.clear();
	
		long success_debt_amount = 0;// 债权转让成功标数量
		Object obj;
		
		String sql = "SELECT COUNT(DISTINCT b.bid_id) FROM t_invests b WHERE b.id IN (SELECT a.invest_id FROM t_invest_transfers a WHERE a.status = ? AND DATE_FORMAT(a.transaction_time,'%y%m') = DATE_FORMAT(CURDATE(),'%y%m'))";
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, Constants.DEBT_SUCCESS);
		
		try {
			obj = query.getSingleResult();// 债权转让成功标数量
		} catch (Exception e) {
			Logger.error("查询债权转让成功标数量时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询债权转让成功标数量时异常！" ;
			
			return 0l;
		}
		
		error.code = 0;
		success_debt_amount = obj == null ? 0l : Long.parseLong(obj.toString());
		
		return success_debt_amount;
	 }
	 
	 /**
	  * 查询所有已放款的理财标
	  * @return
	  */
	 public static long querySuccessBidAccount(ErrorInfo error){
		 error.clear();
		 
		 long success_account = 0l;//已放款的理财标总数量
		 Object obj;
		 
		 String sql = "SELECT COUNT(1) FROM t_bids WHERE `status` IN (?, ?, ?)";
		 Query query = JPA.em().createNativeQuery(sql).setParameter(1, Constants.BID_REPAYMENT).setParameter(2, Constants.BID_REPAYMENTS).setParameter(3, Constants.BID_COMPENSATE_REPAYMENT);
		 
		 try {
			 obj = query.getSingleResult();
		} catch (Exception e) {
			Logger.error("查询已放款的理财标总数量时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询已放款的理财标总数量时异常！";
			
			return 0l;
		}
		 
		 error.code = 0;
		 success_account = obj == null ? 0l : Long.parseLong(obj.toString());
		 
		 return success_account;
	 }
	 
	 public static long queryAllInvests(){
		long temp = 0;
		
		String sql = "select count(*) from t_invests where DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') and transfer_status = 0";
		try {
			temp = t_invests.find(sql).first();
		} catch (Exception e) {
			e.printStackTrace();
			temp = 0;
		}
		
		return temp ;
	 }
	 
	 
	 /**
	  * 判断记录是否存在，不存在返回true,存在返回false
	  * @param year
	  * @param month
	  * @return
	  */
	 public static boolean judgeIsNew(int year,int month){
		 t_statistic_debt_situation statistic = null;
		 
		 try {
			 statistic = t_statistic_debt_situation.find("  year = ? and month = ?",year,month).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		if(null == statistic){
			return true;
		}
		 return false;
	 }
	 
	 
	//获取对象 
	public static t_statistic_debt_situation getTarget(int year, int month) {
		t_statistic_debt_situation statistic = null;

		try {
			statistic = t_statistic_debt_situation.find(
					"  year = ? and month = ?",year,month).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return statistic;
	}
	
	
	/**
	 * 数据统计--债权转让情况统计分析表
	 */
	public static void debtSituationStatistics(){
		
		 int year = 0 ;
		 int month = 0 ;
		 ErrorInfo error = new ErrorInfo();
		 
		 long debt_account = StatisticDebt.queryDebtAccount(error) ;//债权转让标总数量
		 Double debt_amount_sum = StatisticDebt.queryDebtAmounSum(error) ;//债权转让总金额
		 long increase_debt_account  = StatisticDebt.queryIncreaseDebtAccount(error) ;//本月新增转让标数量
		 Double increase_debt_amount_sum = StatisticDebt.queryIncreaseDebtAmountSum(error) ;//本月新增转让总额
		 long has_overdue_debt = StatisticDebt.queryHasOverdueDebt(error);//转让债权标含逾期数量
		 double overdue_percent = 0 ;//转让债权逾期占比
		 double average_debt_amount = 0 ;//转让债权均标金额
		 long success_debt_amount = StatisticDebt.querySuccessDebtAmount(error) ;//债权转让成功标数量
		 double deal_percent = 0 ;//本月转让债权成交率
		 double transfer_percent = 0;//本月债权转让率

		 
		 Calendar now = Calendar.getInstance(); 
		 year = now.get(Calendar.YEAR);  
		 month = now.get(Calendar.MONTH) + 1;
		 
		if(debt_account > 0){
			overdue_percent = Arith.div(has_overdue_debt, debt_account, 4)*100 ;//转让债权逾期占比
		}else{
			overdue_percent = 0;
		}
		
		if(debt_account > 0){
			average_debt_amount = Arith.div(debt_amount_sum, debt_account, 2) ;//转让债权均标金额
		}else{
			average_debt_amount = 0;
		}
		
		
		if(increase_debt_account > 0){
			 deal_percent = Arith.div(success_debt_amount, increase_debt_account, 4)*100 ;//本月转让债权成交率
		}else{
			deal_percent = 0;
		}
		
		long temp = StatisticDebt.querySuccessBidAccount(error);//已放款的理财标总数量
		
		if(temp > 0){
			transfer_percent = Arith.div(increase_debt_account, temp, 4)*100;//本月债权转让率
		}else{
			transfer_percent = 0;
		}
		
		//判断是否有记录存在
		boolean flag = StatisticDebt.judgeIsNew(year, month);
		
		if(flag){
			t_statistic_debt_situation  statistic =  new t_statistic_debt_situation();
			
			statistic.year = year ;
			statistic.month = month ;
			statistic.debt_account = debt_account ;//债权转让标总数量
			statistic.debt_amount_sum = debt_amount_sum ;//债权转让总金额
			statistic.increase_debt_account  = increase_debt_account ;//本月新增转让标数量
			statistic.increase_debt_amount_sum = increase_debt_amount_sum ;//本月新增转让总额
			statistic.has_overdue_debt = has_overdue_debt ;//转让债权标含逾期数量
			statistic.overdue_percent = overdue_percent ;//转让债权逾期占比
			statistic.average_debt_amount = average_debt_amount ;//转让债权均标金额
			statistic.success_debt_amount = success_debt_amount ;//债权转让成功标数量
			statistic.deal_percent = deal_percent ;//转让债权成交率
			statistic.transfer_percent = transfer_percent;//债权转让率
			
			try {
				statistic.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			t_statistic_debt_situation  statistic = StatisticDebt.getTarget(year, month);
			statistic.debt_account = debt_account ;//债权转让标总数量
			statistic.debt_amount_sum = debt_amount_sum ;//债权转让总金额
			statistic.increase_debt_account  = increase_debt_account ;//本月新增转让标数量
			statistic.increase_debt_amount_sum = increase_debt_amount_sum ;//本月新增转让总额
			statistic.has_overdue_debt = has_overdue_debt ;//转让债权标含逾期数量
			statistic.overdue_percent = overdue_percent ;//转让债权逾期占比
			statistic.average_debt_amount = average_debt_amount ;//转让债权均标金额
			statistic.success_debt_amount = success_debt_amount ;//债权转让成功标数量
			statistic.deal_percent = deal_percent ;//转让债权成交率
			statistic.transfer_percent = transfer_percent;//债权转让率
			
			try {
				statistic.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}
}
