package reports;

import java.util.Calendar;

import javax.persistence.Query;

import models.t_statistic_borrow;
import models.t_users;
import play.Logger;
import play.db.jpa.JPA;
import utils.Arith;
import utils.ErrorInfo;

import com.shove.Convert;

/**
 * 借款情况统计分析表
 * @author lzp
 * @version 6.0
 * @created 2014-7-16
 */
public class StatisticBorrow {
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
	 * 添加本月统计数据
	 * @param error
	 * @return
	 */
	private static int add(ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		t_statistic_borrow entity = new t_statistic_borrow();
		entity.year = cal.get(Calendar.YEAR);
		entity.month = cal.get(Calendar.MONTH) + 1;
		
		//累计借款总额
		entity.total_borrow_amount = queryTotalBorrowAmount(error);
		entity.total_borrow_amount_pc = queryTotalBorrowAmountPC(error);
		entity.total_borrow_amount_app = queryTotalBorrowAmountAPP(error);
		entity.total_borrow_amount_wechat = queryTotalBorrowAmountWechat(error);
		
		//本月借款总额
		entity.this_month_borrow_amount = queryThisMonthBorrowAmount(error);
		entity.this_month_borrow_amount_pc = queryThisMonthBorrowAmountPC(error);
		entity.this_month_borrow_amount_app = queryThisMonthBorrowAmountAPP(error);
		entity.this_month_borrow_amount_wechat = queryThisMonthBorrowAmountWechat(error);
		
		entity.total_borrow_user_num = queryTotalBorrowUserNum(error);
		entity.total_borrow_user_num_pc = queryTotalBorrowUserNumPC(error);
		entity.total_borrow_user_num_app = queryTotalBorrowUserNumAPP(error);
		entity.total_borrow_user_num_wechat = queryTotalBorrowUserNumWechat(error);
		entity.new_borrow_user_num = queryNewBorrowUserNum(error);
		entity.new_borrow_user_num_pc = queryNewBorrowUserNumPC(error);
		entity.new_borrow_user_num_app = queryNewBorrowUserNumAPP(error);
		entity.new_borrow_user_num_wechat = queryNewBorrowUserNumWechat(error);
		entity.finished_borrow_amount = queryFinishedBorrowAmount(error);
		entity.finished_borrow_amount_pc = queryFinishedBorrowAmountPC(error);
		entity.finished_borrow_amount_app = queryFinishedBorrowAmountAPP(error);
		entity.finished_borrow_amount_wechat = queryFinishedBorrowAmountWechat(error);
		
		//还款中的借款总额
		entity.repaying_borrow_amount = queryRepayingBorrowAmount(error);
		
		entity.released_bids_num_pc = queryReleasedBidsNumPC(error);
		entity.released_bids_num_app = queryReleasedBidsNumAPP(error);
		entity.released_bids_num_wechat = queryReleasedBidsNumWechat(error);
		
		//已成功借款标数量
		entity.released_bids_num = queryReleasedBidsNum(error);
		
		//已成功借款总额
		entity.released_borrow_amount = queryReleasedBorrowAmount(error);
		
		entity.average_annual_rate_pc = queryAverageAnnualRatePC(error);
		entity.average_annual_rate_app = queryAverageAnnualRateAPP(error);
		entity.average_annual_rate_wechat = queryAverageAnnualRateWechat(error);
		
		//平均年利率
		entity.average_annual_rate = queryAverageAnnualRate(error);
		
		//平均借款金额
		entity.average_borrow_amount = queryAverageBorrowAmount(error);
		
		entity.average_borrow_amount_pc = queryAverageBorrowAmountPC(error);
		entity.average_borrow_amount_app = queryAverageBorrowAmountAPP(error);
		entity.average_borrow_amount_wechat = queryAverageBorrowAmountWechat(error);
		
		//逾期账单数量
		entity.overdue_bids_num = queryOverduedBidsNum(error);
		//逾期账单总额
		entity.overdue_amount = queryOverdueAmount(error);
		//逾期总额占比
		entity.overdue_per = entity.repaying_borrow_amount == 0 ? 0 : Arith.div(entity.overdue_amount, entity.repaying_borrow_amount, 2);
		
		//坏账借款标数量
		entity.bad_bids_num = queryBadBidsNum(error);
		//坏账总额
		entity.bad_bill_amount = queryBadBillAmount(error);
		
		//坏账总额占比
		entity.bad_bill_amount_per = entity.repaying_borrow_amount == 0 ? 0 : Arith.div(entity.bad_bill_amount, entity.repaying_borrow_amount, 2);

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
	 * 更新本月统计数据
	 * @param error
	 * @return
	 */
	private static int update(ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		t_statistic_borrow entity = null;
		
		try {
			entity = t_statistic_borrow.find("year = ? and month = ?", year, month).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		if (entity == null) {
			error.code = -1;
			error.msg = "本月借款情况统计不存在";
			
			return error.code;
		}
		
		entity.year = year;
		entity.month = month;
		entity.total_borrow_amount = queryTotalBorrowAmount(error);
		entity.total_borrow_amount_pc = queryTotalBorrowAmountPC(error);
		entity.total_borrow_amount_app = queryTotalBorrowAmountAPP(error);
		entity.total_borrow_amount_wechat = queryTotalBorrowAmountWechat(error);
		entity.this_month_borrow_amount = queryThisMonthBorrowAmount(error);
		entity.this_month_borrow_amount_pc = queryThisMonthBorrowAmountPC(error);
		entity.this_month_borrow_amount_app = queryThisMonthBorrowAmountAPP(error);
		entity.this_month_borrow_amount_wechat = queryThisMonthBorrowAmountWechat(error);
		entity.total_borrow_user_num = queryTotalBorrowUserNum(error);
		entity.total_borrow_user_num_pc = queryTotalBorrowUserNumPC(error);
		entity.total_borrow_user_num_app = queryTotalBorrowUserNumAPP(error);
		entity.total_borrow_user_num_wechat = queryTotalBorrowUserNumWechat(error);
		entity.new_borrow_user_num = queryNewBorrowUserNum(error);
		entity.new_borrow_user_num_pc = queryNewBorrowUserNumPC(error);
		entity.new_borrow_user_num_app = queryNewBorrowUserNumAPP(error);
		entity.new_borrow_user_num_wechat = queryNewBorrowUserNumWechat(error);
		entity.finished_borrow_amount = queryFinishedBorrowAmount(error);
		entity.finished_borrow_amount_pc = queryFinishedBorrowAmountPC(error);
		entity.finished_borrow_amount_app = queryFinishedBorrowAmountAPP(error);
		entity.finished_borrow_amount_wechat = queryFinishedBorrowAmountWechat(error);
		entity.repaying_borrow_amount = queryRepayingBorrowAmount(error);
		entity.released_bids_num_pc = queryReleasedBidsNumPC(error);
		entity.released_bids_num_app = queryReleasedBidsNumAPP(error);
		entity.released_bids_num_wechat = queryReleasedBidsNumWechat(error);
		entity.released_bids_num = queryReleasedBidsNum(error);
		entity.released_borrow_amount = queryReleasedBorrowAmount(error);
		entity.average_annual_rate_pc = queryAverageAnnualRatePC(error);
		entity.average_annual_rate_app = queryAverageAnnualRateAPP(error);
		entity.average_annual_rate_wechat = queryAverageAnnualRateWechat(error);
		entity.average_annual_rate = queryAverageAnnualRate(error);
		entity.average_borrow_amount = queryAverageBorrowAmount(error);
		entity.average_borrow_amount_pc = queryAverageBorrowAmountPC(error);
		entity.average_borrow_amount_app = queryAverageBorrowAmountAPP(error);
		entity.average_borrow_amount_wechat = queryAverageBorrowAmountWechat(error);
		entity.overdue_bids_num = queryOverduedBidsNum(error);
		entity.overdue_amount = queryOverdueAmount(error);
		entity.overdue_per = entity.this_month_borrow_amount == 0 ? 0 : entity.overdue_amount / entity.this_month_borrow_amount;
		entity.bad_bids_num = queryBadBidsNum(error);
		entity.bad_bill_amount = queryBadBillAmount(error);
		entity.bad_bill_amount_per = entity.this_month_borrow_amount == 0 ? 0 : entity.bad_bill_amount / entity.this_month_borrow_amount;

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
	 * 是否添加了本月数据
	 * @return
	 */
	private static boolean isAdd(ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int count = 0;
		
		try {
			count = (int)t_statistic_borrow.count("year = ? and month = ?", year, month);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return false;
		}
		
		error.code = 0;
		
		return (count > 0);
	}
	
	/**
	 * 查询累计借款总额
	 * @param error
	 * @return
	 */
	public static double queryTotalBorrowAmount(ErrorInfo error) {
		error.clear();
		String sql = "select sum(amount) from t_bids where status in (4, 5, 14)";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询累计借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询累计借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * PC查询累计借款总额
	 * @param error
	 * @return
	 */
	public static double queryTotalBorrowAmountPC(ErrorInfo error) {
		error.clear();
		String sql = "select sum(amount) from t_bids where status in (4, 5, 14) and client = 1";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询累计借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询累计借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * APP查询累计借款总额
	 * @param error
	 * @return
	 */
	public static double queryTotalBorrowAmountAPP(ErrorInfo error) {
		error.clear();
		String sql = "select sum(amount) from t_bids where status in (4, 5, 14) and client = 2";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询累计借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询累计借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * wechat查询累计借款总额
	 * @param error
	 * @return
	 */
	public static double queryTotalBorrowAmountWechat(ErrorInfo error) {
		error.clear();
		String sql = "select sum(amount) from t_bids where status in (4, 5, 14) and client = 3";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询累计借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询累计借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询本月借款总额
	 * @param error
	 * @return
	 */
	public static double queryThisMonthBorrowAmount(ErrorInfo error) {
		error.clear();
		String sql = "select sum(`t_bids`.`amount`) AS `sum(amount)` from `t_bids` where status in (4, 5, 14) and (date_format(`t_bids`.`time`,'%Y%m') = date_format(curdate(),'%Y%m'))";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询本月借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询本月借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * PC查询本月借款总额
	 * @param error
	 * @return
	 */
	public static double queryThisMonthBorrowAmountPC(ErrorInfo error) {
		error.clear();
		String sql = "select sum(`t_bids`.`amount`) AS `sum(amount)` from `t_bids` where status in (4, 5, 14) and (date_format(`t_bids`.`time`,'%Y%m') = date_format(curdate(),'%Y%m') AND client = 1)";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询本月借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询本月借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * APP查询本月借款总额
	 * @param error
	 * @return
	 */
	public static double queryThisMonthBorrowAmountAPP(ErrorInfo error) {
		error.clear();
		String sql = "select sum(`t_bids`.`amount`) AS `sum(amount)` from `t_bids` where status in (4, 5, 14) and (date_format(`t_bids`.`time`,'%Y%m') = date_format(curdate(),'%Y%m') AND client = 2)";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询本月借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询本月借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * wechat查询本月借款总额
	 * @param error
	 * @return
	 */
	public static double queryThisMonthBorrowAmountWechat(ErrorInfo error) {
		error.clear();
		String sql = "select sum(`t_bids`.`amount`) AS `sum(amount)` from `t_bids` where status in (4, 5, 14) and (date_format(`t_bids`.`time`,'%Y%m') = date_format(curdate(),'%Y%m') and client = 3)";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询本月借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询本月借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询累计借款会员数
	 * @param error
	 * @return
	 */
	public static int queryTotalBorrowUserNum(ErrorInfo error) {
		error.clear();
		int count = 0;
		
		try {
			count = (int) t_users.count("master_identity = 1 or master_identity = 3");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询累计借款会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询累计借款会员数出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return count;
	}
	
	/**
	 * PC查询累计借款会员数
	 * @param error
	 * @return
	 */
	public static int queryTotalBorrowUserNumPC(ErrorInfo error) {
		error.clear();
		int count = 0;
		
		try {
			count = (int) t_users.count("client = 1 and master_identity = 1 or master_identity = 3");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询累计借款会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询累计借款会员数出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return count;
	}
	
	/**
	 * APP查询累计借款会员数
	 * @param error
	 * @return
	 */
	public static int queryTotalBorrowUserNumAPP(ErrorInfo error) {
		error.clear();
		int count = 0;
		
		try {
			count = (int) t_users.count("client = 2 and master_identity = 1 or master_identity = 3");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询累计借款会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询累计借款会员数出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return count;
	}
	
	/**
	 * 	wechat查询累计借款会员数
	 * @param error
	 * @return
	 */
	public static int queryTotalBorrowUserNumWechat(ErrorInfo error) {
		error.clear();
		int count = 0;
		
		try {
			count = (int) t_users.count("client = 3 and master_identity = 1 or master_identity = 3");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询累计借款会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询累计借款会员数出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return count;
	}
	
	/**
	 * 查询新增借款会员数
	 * @param error
	 * @return
	 */
	public static int queryNewBorrowUserNum(ErrorInfo error) {
		error.clear();
		String sql = "select count(*) from t_users where (date_format(master_time_loan,'%Y%m') = date_format(curdate(),'%Y%m')) or (master_time_loan = null and master_identity = 3 and date_format(master_time_complex,'%Y%m') = date_format(curdate(),'%Y%m'))";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询新增借款会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询新增借款会员数出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询PC新增借款会员数
	 * @param error
	 * @return
	 */
	public static int queryNewBorrowUserNumPC(ErrorInfo error) {
		error.clear();
		String sql = "select count(*) from t_users where master_client = 1 and (date_format(master_time_loan,'%Y%m') = date_format(curdate(),'%Y%m')) or (master_time_loan = null and master_identity = 3 and date_format(master_time_complex,'%Y%m') = date_format(curdate(),'%Y%m'))";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询新增借款会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询新增借款会员数出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询APP新增借款会员数
	 * @param error
	 * @return
	 */
	public static int queryNewBorrowUserNumAPP(ErrorInfo error) {
		error.clear();
		String sql = "select count(*) from t_users where master_client = 2 and (date_format(master_time_loan,'%Y%m') = date_format(curdate(),'%Y%m')) or (master_time_loan = null and master_identity = 3 and date_format(master_time_complex,'%Y%m') = date_format(curdate(),'%Y%m'))";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询新增借款会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询新增借款会员数出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询wechat新增借款会员数
	 * @param error
	 * @return
	 */
	public static int queryNewBorrowUserNumWechat(ErrorInfo error) {
		error.clear();
		String sql = "select count(*) from t_users where master_client = 3 and (date_format(master_time_loan,'%Y%m') = date_format(curdate(),'%Y%m')) or (master_time_loan = null and master_identity = 3 and date_format(master_time_complex,'%Y%m') = date_format(curdate(),'%Y%m'))";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询新增借款会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询新增借款会员数出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询已完成借款总额
	 * @param error
	 * @return
	 */
	public static double queryFinishedBorrowAmount(ErrorInfo error) {
		error.clear();
		String sql = "select sum(amount) from t_bids where ((status = 5) and (date_format(time,'%Y%m') = date_format(curdate(),'%Y%m')))";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已完成借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已完成借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询PC已完成借款总额
	 * @param error
	 * @return
	 */
	public static double queryFinishedBorrowAmountPC(ErrorInfo error) {
		error.clear();
		String sql = "select sum(amount) from t_bids where ((status = 5) and (date_format(time,'%Y%m') = date_format(curdate(),'%Y%m')) and client = 1)";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已完成借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已完成借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询APP已完成借款总额
	 * @param error
	 * @return
	 */
	public static double queryFinishedBorrowAmountAPP(ErrorInfo error) {
		error.clear();
		String sql = "select sum(amount) from t_bids where ((status = 5) and (date_format(time,'%Y%m') = date_format(curdate(),'%Y%m')) and client = 2)";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已完成借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已完成借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * wechat查询已完成借款总额
	 * @param error
	 * @return
	 */
	public static double queryFinishedBorrowAmountWechat(ErrorInfo error) {
		error.clear();
		String sql = "select sum(amount) from t_bids where ((status = 5) and (date_format(time,'%Y%m') = date_format(curdate(),'%Y%m')) and client = 3)";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已完成借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已完成借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询还款中的借款总额
	 * @param error
	 * @return
	 */
	public static double queryRepayingBorrowAmount(ErrorInfo error) {
		error.clear();
		String sql = "select sum(repayment_corpus+repayment_interest+overdue_fine) from t_bills where status in (-1, -2) and date_format(repayment_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询还款中的借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询还款中的借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	/**
	 * 查询还款中的借款总额
	 * @param error
	 * @return
	 */
	public static double queryAllRepayingBorrowAmount(ErrorInfo error) {
		error.clear();
		//String sql = "select sum(repayment_corpus+repayment_interest+overdue_fine) from t_bills where status in (-1, -2) ";
		String sql = "select sum(repayment_corpus) from t_bills where status in (-1, -2) ";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询还款中的借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询还款中的借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询已放款借款标数量
	 * @param error
	 * @return
	 */
	public static int queryReleasedBidsNum(ErrorInfo error) {
		error.clear();
		//String sql = "select count(*) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		String sql = "select count(*) from t_bids where status in (4, 5, 14)";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已放款借款标数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已放款借款标数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询PC已放款借款标数量
	 * @param error
	 * @return
	 */
	public static int queryReleasedBidsNumPC(ErrorInfo error) {
		error.clear();
		String sql = "select count(*) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m') and client = 1";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已放款借款标数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已放款借款标数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询APP已放款借款标数量
	 * @param error
	 * @return
	 */
	public static int queryReleasedBidsNumAPP(ErrorInfo error) {
		error.clear();
		String sql = "select count(*) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m') and client = 2";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已放款借款标数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已放款借款标数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * wechat查询已放款借款标数量
	 * @param error
	 * @return
	 */
	public static int queryReleasedBidsNumWechat(ErrorInfo error) {
		error.clear();
		String sql = "select count(*) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m') and client = 3";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已放款借款标数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已放款借款标数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询已放款借款总额
	 * @param error
	 * @return
	 */
	public static double queryReleasedBorrowAmount(ErrorInfo error) {
		error.clear();
		String sql = "select sum(amount) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已放款借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已放款借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 平均年利率(基于已放款借款标数量来算)
	 * @param error
	 * @return
	 */
	public static double queryAverageAnnualRate(ErrorInfo error) {
		error.clear();
		String sql = "select avg(apr) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已放款借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已放款借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * PC平均年利率(基于已放款借款标数量来算)
	 * @param error
	 * @return
	 */
	public static double queryAverageAnnualRatePC(ErrorInfo error) {
		error.clear();
		String sql = "select avg(apr) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m') and client = 1";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已放款借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已放款借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * APP平均年利率(基于已放款借款标数量来算)
	 * @param error
	 * @return
	 */
	public static double queryAverageAnnualRateAPP(ErrorInfo error) {
		error.clear();
		String sql = "select avg(apr) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m') and client = 2";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已放款借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已放款借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * wechat平均年利率(基于已放款借款标数量来算)
	 * @param error
	 * @return
	 */
	public static double queryAverageAnnualRateWechat(ErrorInfo error) {
		error.clear();
		String sql = "select avg(apr) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m') and client = 3";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已放款借款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已放款借款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 均借款金额(基于已放款借款标数量来算)
	 * @param error
	 * @return
	 */
	public static double queryAverageBorrowAmount(ErrorInfo error) {
		error.clear();
		String sql = "select avg(amount) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询均借款金额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询均借款金额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * PC均借款金额(基于已放款借款标数量来算)
	 * @param error
	 * @return
	 */
	public static double queryAverageBorrowAmountPC(ErrorInfo error) {
		error.clear();
		String sql = "select avg(amount) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m') and client = 1";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询均借款金额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询均借款金额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * APP均借款金额(基于已放款借款标数量来算)
	 * @param error
	 * @return
	 */
	public static double queryAverageBorrowAmountAPP(ErrorInfo error) {
		error.clear();
		String sql = "select avg(amount) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m') and client = 2";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询均借款金额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询均借款金额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * wechat均借款金额(基于已放款借款标数量来算)
	 * @param error
	 * @return
	 */
	public static double queryAverageBorrowAmountWechat(ErrorInfo error) {
		error.clear();
		String sql = "select avg(amount) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m') and client = 3";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询均借款金额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询均借款金额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询逾期借款标数量
	 * @param error
	 * @return
	 */
	public static int queryOverduedBidsNum(ErrorInfo error) {
		error.clear();
		String sql = "select count(distinct bid_id) from t_bills where status in (-1,-2) and overdue_mark in (-1,-2,-3)";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询逾期借款标数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询逾期借款标数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询逾期总额
	 * @param error
	 * @return
	 */
	public static double queryOverdueAmount(ErrorInfo error) {
		error.clear();
		String sql = "select sum(repayment_corpus+real_repayment_interest+overdue_fine) from t_bills where status in (-1,-2) and overdue_mark in (-1,-2,-3) and date_format(repayment_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询逾期总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询逾期总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询坏账借款标数量
	 * @param error
	 * @return
	 */
	public static int queryBadBidsNum(ErrorInfo error) {
		error.clear();
		String sql = "select count(distinct bid_id) from t_bills where status in (-1,-2) and overdue_mark = -3";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询坏账借款标数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询坏账借款标数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询坏账总额
	 * @param error
	 * @return
	 */
	public static double queryBadBillAmount(ErrorInfo error) {
		error.clear();
		String sql = "select sum(repayment_corpus+real_repayment_interest+overdue_fine) from t_bills where status in (-1,-2) and overdue_mark = -3 and date_format(repayment_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询坏账总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询坏账总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询某一个月的借款总额
	 * @param year
	 * @param month
	 * @param error
	 * @return
	 */
	public static Double queryTotalBorrowAmount(int year, int month, ErrorInfo error){
		error.clear();
		
		Double amount = null;
		try {
			amount = t_statistic_borrow.find("select t.total_borrow_amount from t_statistic_borrow t where t.year = ? and t.month = ?", year,month).first();
		} catch (Exception e) {
			Logger.error("查询某一个月的借款总额时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询某一个月的借款总额时有误！";
			
			return 0d;
		}
		
		//每个月的第一天没有记录，此时需要查上月的记录（这里只需要查最后一条记录即可）
		if(amount == null){
			try {
				amount = t_statistic_borrow.find("select total_borrow_amount from t_statistic_borrow order by year desc, month desc").first();
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
	
}
