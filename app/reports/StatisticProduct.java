package reports;

import java.util.Calendar;
import java.util.List;

import javax.persistence.Query;

import com.shove.Convert;

import models.t_products;
import models.t_statistic_product;
import play.Logger;
import play.db.jpa.JPA;
import utils.Arith;
import utils.ErrorInfo;

/**
 * 借款标产品销售情况分析表
 * @author lzp
 * @version 6.0
 * @created 2014-7-16
 */
public class StatisticProduct {
	/**
	 * 周期性执行
	 * @param error
	 * @return
	 */
	public static int executeUpdate(ErrorInfo error) {
		error.clear();
		
		List<t_products> products = null;
		
		try {
			products = t_products.findAll();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		if (products == null) {
			return error.code;
		}
		
		//每次插入新数据之前先删除之前的数据
		try {
			t_statistic_product.deleteAll();
			
			for (int i = 0; i < products.size(); i++) {
				t_products product = null;
				
				product = (t_products)products.get(i);
				add(product, error);
				
				if (error.code < 0) {
					return error.code;
				}
			}
		} catch (Exception e) {
			JPA.setRollbackOnly();
		}
		
		
		error.code = 0;
		
		return error.code;
	}
	
	/**
	 * 添加本月统计数据
	 * @param error
	 * @return
	 */
	private static int add(t_products product, ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		int productId = product.id.intValue();
		int releasedBidsTotalNum = queryReleasedBidsTotalNum(error);
		
		t_statistic_product entity = new t_statistic_product();
		entity.product_id = productId;
		entity.year = cal.get(Calendar.YEAR);
		entity.month = cal.get(Calendar.MONTH) + 1;
		entity.name = product.name;
		entity.released_bids_num = queryReleasedBidsNum(productId, error);
		entity.released_amount = queryReleasedAmount(productId, error);
		entity.average_bid_amount = queryAverageBidAmount(productId, error);
		entity.per = releasedBidsTotalNum==0 ? 0 : entity.released_bids_num / releasedBidsTotalNum;
		//逾期数量
		entity.overdue_num = queryOverdueNum(productId, error);
		
		//坏账数量
		entity.bad_bids_num = queryBadBidsNum(productId, error);
		
		
		//借款标数量
		entity.bids_num = queryBidsNum(productId, error);
		
		//投标会员数
		entity.invest_user_num = queryInvestUserNum(productId, error);
		
		//平均年利率
		entity.average_annual_rate = queryAverageAnnualRate(productId, error);
		//已经成功借款标数量
		entity.success_bids_num = querySuccessBidsNum(productId, error);
		//已成功借款标总额
		entity.success_bids_amount = querySuccessBidsAmount(productId, error);
		entity.manage_fee_amount = queryManageFeeAmount(productId, error);

		//逾期占比
		entity.overdue_per = entity.success_bids_num == 0 ? 0 : Arith.div(entity.overdue_num, entity.success_bids_num, 2);

		//坏账占比
		entity.bad_bids_per = entity.success_bids_num == 0 ? 0 : Arith.div(entity.bad_bids_num, entity.success_bids_num, 2);
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
	private static int update(t_products product, ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int productId = product.id.intValue();
		
		t_statistic_product entity = null;
		
		try {
			entity = t_statistic_product.find("product_id = ? and year = ? and month = ?", productId, year, month).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		if (entity == null) {
			error.code = -1;
			error.msg = "本月借款标产品统计不存在";
			
			return error.code;
		}
		
		int releasedBidsTotalNum = queryReleasedBidsTotalNum(error);
		
		entity.year = cal.get(Calendar.YEAR);
		entity.month = cal.get(Calendar.MONTH) + 1;
		entity.name = product.name;
		entity.released_bids_num = queryReleasedBidsNum(productId, error);
		entity.released_amount = queryReleasedAmount(productId, error);
		entity.average_bid_amount = queryAverageBidAmount(productId, error);
		entity.per = releasedBidsTotalNum==0 ? 0 : entity.released_bids_num / releasedBidsTotalNum;
		entity.overdue_num = queryOverdueNum(productId, error);
		entity.overdue_per = entity.released_bids_num==0 ? 0 : entity.overdue_num / (double) entity.released_bids_num;
		entity.bad_bids_num = queryBadBidsNum(productId, error);
		entity.bad_bids_per = entity.released_bids_num==0 ? 0 : entity.bad_bids_num / (double) entity.released_bids_num;
		entity.bids_num = queryBidsNum(productId, error);
		entity.invest_user_num = queryInvestUserNum(productId, error);
		entity.average_annual_rate = queryAverageAnnualRate(productId, error);
		entity.success_bids_num = querySuccessBidsNum(productId, error);
		entity.success_bids_amount = querySuccessBidsAmount(productId, error);
		entity.manage_fee_amount = queryManageFeeAmount(productId, error);

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
	private static boolean isAdd(int productId, ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int count = 0;
		
		try {
			count = (int)t_statistic_product.count("product_id = ? and year = ? and month = ?", productId, year, month);
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
	 * 查询已放款标总数量
	 * @param error
	 * @return
	 */
	public static int queryReleasedBidsTotalNum(ErrorInfo error) {
		error.clear();
		String sql = "select count(*) from t_bids where status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已放款标数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已放款标数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询已放款标数量
	 * @param error
	 * @return
	 */
	public static int queryReleasedBidsNum(int productId, ErrorInfo error) {
		error.clear();
		String sql = "select count(*) from t_bids where product_id = ? and status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, productId);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已放款标数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已放款标数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询已放款总额
	 * @param error
	 * @return
	 */
	public static double queryReleasedAmount(int productId, ErrorInfo error) {
		error.clear();
		String sql = "select sum(amount) from t_bids where product_id = ? and status in (4, 5, 14) ";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, productId);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已放款总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已放款总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询均标借款金额
	 * @param error
	 * @return
	 */
	public static double queryAverageBidAmount(int productId, ErrorInfo error) {
		error.clear();
		String sql = "select avg(amount) from t_bids where product_id = ? and status in (4, 5, 14)";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, productId);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询均标借款金额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询均标借款金额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询逾期标数量
	 * @param error
	 * @return
	 */
	public static int queryOverdueNum(int productId, ErrorInfo error) {
		error.clear();
		//String sql = "select count(*) from t_bids where product_id = ? and status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m') and id in (select bid_id from t_bills where overdue_mark <> 0)";
		String sql = "select count(DISTINCT(t.id))  from t_bids t LEFT JOIN t_bills a on (t.id= a.bid_id) where t.status = 4 and a.overdue_mark in(-1,-2,-3) and t.product_id = ?";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, productId);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询逾期数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询逾期数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询坏账数量
	 * @param error
	 * @return
	 */
	public static int queryBadBidsNum(int productId, ErrorInfo error) {
		error.clear();
		//String sql = "select count(*) from t_bids where product_id = ? and status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m') and id in (select bid_id from t_bills where overdue_mark = -3)";
		String sql = "select count(DISTINCT(t.id))  from t_bids t LEFT JOIN t_bills a on (t.id= a.bid_id) where t.status in(4,5) and a.overdue_mark =-3 and t.product_id=?";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, productId);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询坏账数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询坏账数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询借款标数量
	 * @param error
	 * @return
	 */
	public static int queryBidsNum(int productId, ErrorInfo error) {
		error.clear();
		//String sql = "select count(*) from t_bids where product_id = ? and status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		String sql = "select count(*) from t_bids where product_id = ? ";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, productId);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询借款标数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询借款标数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询投标会员数
	 * @param error
	 * @return
	 */
	public static int queryInvestUserNum(int productId, ErrorInfo error) {
		error.clear();
		//String sql = "select count(distinct user_id) from t_invests where bid_id in (select id from t_bids where product_id = ? and status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m'))";
		String sql = "select count(distinct user_id) from t_invests where bid_id in (select id from t_bids where product_id = ?)";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, productId);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询投标会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询投标会员数出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询平均年利率
	 * @param error
	 * @return
	 */
	public static double queryAverageAnnualRate(int productId, ErrorInfo error) {
		error.clear();
		//String sql = "select avg(apr) from t_bids where product_id = ? and status in (4, 5, 14) and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m')";
		String sql = "select avg(apr) from t_bids where product_id = ? ";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, productId);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询平均年利率时："+e.getMessage());
			error.code = -1;
			error.msg = "查询平均年利率出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询已成功借款标数量
	 * @param error
	 * @return
	 */
	public static int querySuccessBidsNum(int productId, ErrorInfo error) {
		error.clear();
		String sql = "select count(*) from t_bids where product_id = ? and status in (4,5,14)";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, productId);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已成功借款标数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询已成功借款标数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询已成功借款总额
	 * @param error
	 * @return
	 */
	public static double querySuccessBidsAmount(int productId, ErrorInfo error) {
		error.clear();
		String sql = "select sum(amount) from t_bids where product_id = ? and status in (4,5,14)";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, productId);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询平均年利率时："+e.getMessage());
			error.code = -1;
			error.msg = "查询平均年利率出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 查询管理费收入总额(借款管理费、理财管理费、债权转让管理费、逾期管理费这四项的总和)
	 * @param error
	 * @return
	 */
	public static double queryManageFeeAmount(int productId, ErrorInfo error) {
		error.clear();
//		String sql = "select sum(amount) from t_user_details where DATE_FORMAT(time, '%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') and" + 
//					" ((operation = 309 and relation_id in (select id from t_bids where product_id = :productId)) or" +
//					" (operation = 313 and (select bid_id from t_invests where id = relation_id) in (select id from t_bids where product_id = :productId)) or" + 
//					" (operation = 316 and (select i.bid_id from t_invest_transfers as it,t_invests as i where it.id = relation_id and i.id = it.invest_id) in (select id from t_bids where product_id = :productId)) or" +
//					" (operation = 317 and (select bid_id from t_bills where id = relation_id) in (select id from t_bids where product_id = :productId)))";
		String sql = "select sum(amount) from t_user_details where " + 
				" ((operation = 309 and relation_id in (select id from t_bids where product_id = :productId)) or" +
				" (operation = 313 and (select bid_id from t_invests where id = relation_id) in (select id from t_bids where product_id = :productId)) or" + 
				" (operation = 316 and (select i.bid_id from t_invest_transfers as it,t_invests as i where it.id = relation_id and i.id = it.invest_id) in (select id from t_bids where product_id = :productId)) or" +
				" (operation = 317 and (select bid_id from t_bills where id = relation_id) in (select id from t_bids where product_id = :productId)))";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter("productId", productId);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询管理费收入总额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询管理费收入总额出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToDouble(obj+"", 0);
	}
}
