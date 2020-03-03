package reports;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import business.DealDetail;

import com.shove.Convert;

import constants.DealType;
import models.t_bills;
import models.t_platform_details;
import models.t_statistic_security;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;

/**
 * 本金保障统计分析表
 * @author zhs
 * @version 6.0
 * @created 2014-7-18
 *
 */
public class StatisticSecurity {

	/**
	 * 周期性执行
	 * @param error
	 * @return
	 */
	public static int executeUpdate(ErrorInfo error) {
		error.clear();
		boolean isAdd = isAdd(error);
		
		if (error.code < 0) {
			return error.code;
		}
		
		if (isAdd) {
			update(error);
		} else {
			add(error);
		}
		
		error.code = 0;
		return error.code;
	}
	
	/**
	 * 添加本日统计数据
	 * @param error
	 * @return
	 */
	private static int add(ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		t_statistic_security entity = new t_statistic_security();
		
		entity.year = cal.get(Calendar.YEAR);
		entity.month = cal.get(Calendar.MONTH) + 1;
		entity.day = cal.get(Calendar.DAY_OF_MONTH);
		entity.balance = queryBalance(error);
		entity.pay = queryPay(error);
		entity.advance_acount = queryAdvanceAcount(error);
		entity.max_advance_amount = queryMaxAdvanceAmount(error);
		entity.min_advance_amount = queryMinAdvanceAmount(error);
		entity.recharge_amount = queryRechargeAmount(error);
		entity.income_amount = queryIncomeAmount(error);
		entity.loan_amount = queryLoanAmount(error);
		entity.bad_debt_amount = queryBadDebtAmount(error);
		entity.bad_debt_income_rate = queryBadDebtIncomeRate(error);
		entity.bad_debt_guarantee_rate = queryBadDebtGuaranteeRate(error);
		entity.bad_loan_rate = queryBadLoanRate(error);

		try {
			entity.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		error.code = 0;
		return error.code;
	}
	
	/**
	 * 更新本日统计数据
	 * @param error
	 * @return
	 */
	private static int update(ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		t_statistic_security entity = null;
		
		try {
			entity = t_statistic_security.find("year = ? and month = ? and day = ?", year, month, day).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		if (entity == null) {
			error.code = -1;
			error.msg = "本日本金保障统计分析表统计不存在";
			
			return error.code;
		}
		
		entity.year = cal.get(Calendar.YEAR);
		entity.month = cal.get(Calendar.MONTH) + 1;
		entity.day = cal.get(Calendar.DAY_OF_MONTH);
		entity.balance = queryBalance(error);
		entity.pay = queryPay(error);
		entity.advance_acount = queryAdvanceAcount(error);
		entity.max_advance_amount = queryMaxAdvanceAmount(error);
		entity.min_advance_amount = queryMinAdvanceAmount(error);
		entity.recharge_amount = queryRechargeAmount(error);
		entity.income_amount = queryIncomeAmount(error);
		entity.loan_amount = queryLoanAmount(error);
		entity.bad_debt_amount = queryBadDebtAmount(error);
		entity.bad_debt_income_rate = queryBadDebtIncomeRate(error);
		entity.bad_debt_guarantee_rate = queryBadDebtGuaranteeRate(error);
		entity.bad_loan_rate = queryBadLoanRate(error);

		try {
			entity.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		error.code = 0;
		return error.code;
	}
	
	/**
	 * 是否添加了本日数据
	 * @return
	 */
	private static boolean isAdd(ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		
		int count = 0;
		
		try {
			count = (int)t_statistic_security.count("year = ? and month = ? and day = ?", year, month, day);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return false;
		}
		
		return (count > 0);
	}
	
	/**
	 * 本金保障余额
	 * @return
	 */
	public static double queryBalance(ErrorInfo error){
		error.clear();
		Map<String, Object> account = DealDetail.accountSummary(error);
		
		return Double.parseDouble(account.get("balance").toString());
	}	
	/**
	 * 本金保障支出
	 * @return
	 */
	public static double queryPay(ErrorInfo error){
		error.clear();
		String sql = "select sum(amount) from t_platform_details where type = 2";
		
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		if(query.getResultList().size() == 0){
			return 0;
		}
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询本金保障支出时："+e.getMessage());
			error.code = -1;
			error.msg = "查询本金保障支出出现异常！";
			
			return 0;
		}
		
		return (obj == null) ? 0 : Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 垫付账单笔数
	 * @return
	 */
	public static int queryAdvanceAcount(ErrorInfo error){
		error.clear();
		
		Map<String, Object> account = DealDetail.accountSummary(error);
		return (Integer)account.get("advance");
	}
	
	/**
	 * 最高垫付金额
	 * @return
	 */
	public static double queryMaxAdvanceAmount(ErrorInfo error){
		error.clear();
		Double amount;
		String sql = "select MAX(amount) from t_platform_details as a where a.operation = ?";
		
		try {
			amount = t_platform_details.find(sql, DealType.ADVANCE_FEE).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询最高垫付金额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询最高垫付金额出现异常！";
			
			return error.code;
		}
		
		return (amount == null) ? 0 : Convert.strToDouble(amount+"", 0);
	}
	
	/**
	 * 最低垫付金额
	 * @return
	 */
	public static double queryMinAdvanceAmount(ErrorInfo error){
		error.clear();
		Double amount;
		String sql = "select MIN(amount) from t_platform_details as a where a.operation = ?";
		
		try {
			amount = t_platform_details.find(sql, DealType.ADVANCE_FEE).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询最低垫付金额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询最低垫付金额出现异常！";
			
			return error.code;
		}
		
		return (amount == null) ? 0 : Convert.strToDouble(amount+"", 0);
	}
	
	/**
	 * 本金保障总投入
	 * @return
	 */
	public static double queryRechargeAmount(ErrorInfo error){
		error.clear();
		Double amount;
		String sql = "select sum(a.amount) from t_platform_details as a where a.operation = ?";
		
		try {
			amount = t_platform_details.find(sql, DealType.ADD_CAPITAL).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询本金保障总投入时："+e.getMessage());
			error.code = -1;
			error.msg = "查询本金保障总投入异常！";
			
			return error.code;
		}
		
		return (amount == null) ? 0 : Convert.strToDouble(amount+"", 0);
	}
	
	/**
	 * 平台总收入
	 * @return
	 */
	public static double queryIncomeAmount(ErrorInfo error){
		error.clear();
		String sql = "select sum(amount) from t_platform_details where type = 1";
		
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		if(query.getResultList().size() == 0){
			return 0;
		}
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询平台总收入时："+e.getMessage());
			error.code = -1;
			error.msg = "查询平台总收入出现异常！";
			
			return 0;
		}
		
		return (obj == null) ? 0 : Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 平台总借款额
	 * @return
	 */
	public static double queryLoanAmount(ErrorInfo error){
		error.clear();
		String sql = "select sum(amount) from t_bids where status in(4,5)";
		
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		if(query.getResultList().size() == 0){
			return 0;
		}
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询平台总借款额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询平台总借款额出现异常！";
			
			return 0;
		}
		
		return (obj == null) ? 0 : Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 坏账总额
	 * @return
	 */
	public static double queryBadDebtAmount(ErrorInfo error){
		error.clear();
		String sql = "select sum(repayment_corpus + repayment_interest + overdue_fine) from t_bills where status = -1 and overdue_mark = -3";
		
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		if(query.getResultList().size() == 0){
			return 0;
		}
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询坏账总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询坏账总额出现异常！";
			
			return 0;
		}
		
		return (obj == null) ? 0 : Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 坏账收入占比(坏账金额/平台总收入)
	 * @return
	 */
	public static double queryBadDebtIncomeRate(ErrorInfo error){
		error.clear();
		String sql = "SELECT round((SELECT SUM(a.repayment_corpus + a.repayment_interest + a.overdue_fine)" +
				" FROM t_bills as a WHERE a.status = -1 AND a.overdue_mark = -3)/SUM(b.amount)*100,2) " +
				"FROM t_platform_details AS b where b.type = 1";
		
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		if(query.getResultList().size() == 0){
			return 0;
		}
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询平台总借款额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询平台总借款额出现异常！";
			
			return 0;
		}
		
		return (obj == null) ? 0 : Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 坏账保障金占比（坏账金额/本金保障支出）
	 * @return
	 */
	public static double queryBadDebtGuaranteeRate(ErrorInfo error){
		error.clear();
		String sql = "SELECT round((SELECT SUM(a.repayment_corpus + a.repayment_interest + a.overdue_fine)" +
				" FROM t_bills as a WHERE a.status = -1 AND a.overdue_mark = -3)/SUM(b.amount)*100,2) " +
				"FROM t_platform_details AS b where b.type = 2";
		
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		if(query.getResultList().size() == 0){
			return 0;
		}
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询坏账保障金占比时："+e.getMessage());
			error.code = -1;
			error.msg = "查询坏账保障金占比出现异常！";
			
			return 0;
		}
		
		return (obj == null) ? 0 : Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 坏账借款占比（坏账金额/平台总借款额）
	 * @return
	 */
	public static double queryBadLoanRate(ErrorInfo error){
		error.clear();
		String sql = "SELECT round((SELECT SUM(a.repayment_corpus + a.repayment_interest + a.overdue_fine)" +
				" FROM t_bills as a WHERE a.status = -1 AND a.overdue_mark = -3)/SUM(b.amount)*100,2)" +
				" FROM t_bids AS b where b.`status` in(4,5)";
		
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		if(query.getResultList().size() == 0){
			return 0;
		}
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询坏账借款占比时："+e.getMessage());
			error.code = -1;
			error.msg = "查询坏账借款占比出现异常！";
			
			return 0;
		}
		
		return (obj == null) ? 0 : Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 数据统计
	 * @param error
	 * @return
	 */
	public static Map<String,Object> statisticAmount(ErrorInfo error){
		error.clear();
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		String sql = "select new Map(sum(recharge_amount) as rechargeAmount, sum(balance) as balance, sum(pay) as pay," +
				"  sum(advance_acount) as advanceAcount) from t_statistic_security order by id";
		
		try {
			map = t_statistic_security.find(sql).first();//获取投资用户的余额
		 } catch(Exception e) {
				e.printStackTrace();
				Logger.info("统计本金保障数据时："+e.getMessage());
				error.code = -1;
				error.msg = "数据库异常，导致统计本金保障数据失败";
				
				return null;
			}
		List<Object[]> date;
		String sql2="SELECT year,month,day FROM `t_statistic_security` ORDER BY id DESC ";
		Query query=JPA.em().createNativeQuery(sql2);
		try {
			date = query.getResultList();
		} catch (Exception e) {
			Logger.error("查询保障本金统计最新数据时间时:" + e.getMessage());			
			
			return null;
		}
		
		if( null == date || date.size()==0 || date.get(0) == null ){
			
			return null;
			
		}
		map.put("year", date.get(0)[0].toString());
		map.put("month", date.get(0)[1].toString());
		map.put("day", date.get(0)[2].toString());
		
		return map;
	}
}
