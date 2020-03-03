package reports;

import java.util.Calendar;
import java.util.List;

import javax.persistence.Query;

import models.t_dict_audit_items;
import models.t_statistic_audit_items;
import models.t_users;
import play.Logger;
import play.db.jpa.JPA;
import utils.Arith;
import utils.ErrorInfo;

import com.shove.Convert;

import constants.Constants;
import constants.OptionKeys;

/**
 * 审核科目库统计分析表
 * @author lzp
 * @version 6.0
 * @created 2014-7-16
 */
public class StatisticAuditItems {
	/**
	 * 周期性执行
	 * @param error
	 * @return
	 */
	public static int executeUpdate(ErrorInfo error) {
		error.clear();
		
		List<t_dict_audit_items> items = null;
		
		try {
			items = t_dict_audit_items.findAll();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		if (items == null) {
			return error.code;
		}
		try {
			t_statistic_audit_items.deleteAll();
			for (int i = 0; i < items.size(); i++) {
				t_dict_audit_items item = null;
				
				item = (t_dict_audit_items)items.get(i);
				
				add(item, error);
				
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
	private static int add(t_dict_audit_items item, ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int itemId = item.id.intValue();
		
		t_statistic_audit_items entity = new t_statistic_audit_items();
		entity.audit_item_id = itemId;
		entity.year = year;
		entity.month = month;
		entity.no = OptionKeys.getvalue(OptionKeys.AUDIT_ITEM_NUMBER, new ErrorInfo())+itemId;
		entity.name = item.name;
		entity.credit_score = (int)item.credit_score;
		
		//科目收入
		entity.audit_fee = queryAuditFee(error, year, month, itemId);
		entity.borrow_user_num = queryBorrowUserNum(error);
		
		//提交会员数
		entity.submit_user_num = querySubmitUserNum(itemId, error);
		entity.submit_per = entity.borrow_user_num==0 ? 0 : Arith.div(entity.submit_user_num, entity.borrow_user_num, 2);
		
		//通过审核数
		entity.audit_pass_num = queryAuditPassNum(itemId, error);
		
		//通过占比
		entity.pass_per = entity.submit_user_num==0 ? 0 : Arith.div(entity.audit_pass_num, entity.submit_user_num, 2);
		
		entity.relate_product_num = queryRelateProductNum(itemId, error);
		
		//关联逾期借款标数量
		entity.relate_overdue_bid_num = queryRelateOverdueBidNum(itemId, year, month, error);
		//关联关联坏账借款标数量
		entity.relate_bad_bid_num = queryRelateBadBidNum(itemId, year, month, error);
		
		entity.risk_control_ranking = queryRiskControlRanking(itemId, year, month, error);

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
	private static int update(t_dict_audit_items item, ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int itemId = item.id.intValue();
		
		t_statistic_audit_items entity = null;
		
		try {
			entity = t_statistic_audit_items.find("audit_item_id = ? and year = ? and month = ?", itemId, year, month).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		if (entity == null) {
			error.code = -1;
			error.msg = "本月借款资料统计不存在";
			
			return error.code;
		}
		
		entity.audit_item_id = itemId;
		entity.year = cal.get(Calendar.YEAR);
		entity.month = cal.get(Calendar.MONTH) + 1;
		entity.no = OptionKeys.getvalue(OptionKeys.AUDIT_ITEM_NUMBER, new ErrorInfo())+itemId;
		entity.name = item.name;
		entity.credit_score = item.credit_score;
		entity.audit_fee = queryAuditFee(error, year, month, itemId);
		entity.borrow_user_num = queryBorrowUserNum(error);
		entity.submit_user_num = querySubmitUserNum(itemId, error);
		entity.submit_per = entity.borrow_user_num==0 ? 0 : Arith.div(entity.submit_user_num, entity.borrow_user_num, 2);
		entity.audit_pass_num = queryAuditPassNum(itemId, error);
		entity.pass_per = entity.submit_user_num==0 ? 0 : Arith.div(queryAuditPassMonthNum(itemId, error), entity.submit_user_num, 2);
		entity.relate_product_num = queryRelateProductNum(itemId, error);
		entity.relate_overdue_bid_num = queryRelateOverdueBidNum(itemId, year, month, error);
		entity.relate_bad_bid_num = queryRelateBadBidNum(itemId, year, month, error);
		entity.risk_control_ranking = queryRiskControlRanking(itemId, year, month, error);

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
	private static boolean isAdd(int itemId, ErrorInfo error) {
		error.clear();
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int count = 0;
		
		try {
			count = (int)t_statistic_audit_items.count("audit_item_id = ? and year = ? and month = ?", itemId, year, month);
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
	
	public static double queryAuditFee(ErrorInfo error, int year, int month, int itemId) {
		error.clear();
		double auditFee = 0.0;
		Object obj = null;
		
		//String sql = "SELECT sum(t.a) FROM (SELECT b.audit_fee AS a FROM t_user_audit_items a LEFT JOIN t_dict_audit_items c ON a.audit_item_id = c.id LEFT JOIN t_dict_audit_items_log b ON a.mark = b.mark WHERE YEAR (audit_time) = ? AND MONTH (audit_time) = ? AND c.id = ? AND a.`status` IN (?, ?) GROUP BY a.user_id) t";
		String sql = "SELECT sum(t.a) FROM (SELECT b.audit_fee AS a FROM t_user_audit_items a LEFT JOIN t_dict_audit_items c ON a.audit_item_id = c.id LEFT JOIN t_dict_audit_items_log b ON a.mark = b.mark WHERE c.id = ? AND a.`status` IN (?, ?) GROUP BY a.user_id) t";
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, itemId).setParameter(2, Constants.AUDITING).setParameter(3, Constants.AUDITED);
		
		try {
			obj = query.getSingleResult();
		} catch (Exception e) {
			Logger.error("查询本月审核资料费用时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询本月审核资料费用时异常！";
			
			return 0.0;
		}
		
		if (null != obj) {
			auditFee = Double.parseDouble(obj.toString());
		}
		
		error.code = 0;
		
		return auditFee;
	}
	
	/**
	 * 查询借款会员数
	 * @param error
	 * @return
	 */
	public static int queryBorrowUserNum(ErrorInfo error) {
		error.clear();
		int count = 0;
		
		try {
			count = (int) t_users.count("master_identity = 1");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询借款会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询借款会员数出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return count;
	}
	
	/**
	 * 查询提交会员数
	 * @param auditItemId
	 * @param error
	 * @return
	 */
	public static int querySubmitUserNum(int auditItemId, ErrorInfo error) {
		error.clear();
		String sql = "select count(distinct user_id) from t_user_audit_items where audit_item_id = ? and status <> 0";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, auditItemId);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询提交会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询提交会员数出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询本月提交并且是本月审核通过数
	 * @param auditItemId
	 * @param error
	 * @return
	 */
	public static int queryAuditPassMonthNum(int auditItemId, ErrorInfo error) {
		error.clear();
		String sql = "select count(distinct user_id) from t_user_audit_items where audit_item_id = ? and status = ? and date_format(audit_time,'%Y%m') = date_format(curdate(),'%Y%m') AND date_format(time,'%Y%m') = date_format(curdate(),'%Y%m')";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, auditItemId);
		query.setParameter(2, Constants.AUDITED);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询审核通过数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询审核通过数出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询审核通过数
	 * @param auditItemId
	 * @param error
	 * @return
	 */
	public static int queryAuditPassNum(int auditItemId, ErrorInfo error) {
		error.clear();
		String sql = "select count(distinct user_id) from t_user_audit_items where audit_item_id = ? and status = ? ";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, auditItemId);
		query.setParameter(2, Constants.AUDITED);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询审核通过数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询审核通过数出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询关联借款标产品数量
	 * @param auditItemId
	 * @param error
	 * @return
	 */
	public static int queryRelateProductNum(int auditItemId, ErrorInfo error) {
		error.clear();
		String sql = "SELECT count(DISTINCT product_id) FROM t_product_audit_items WHERE audit_item_id = ?";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, auditItemId);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询关联借款标产品数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询关联借款标产品数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询关联逾期借款标数量
	 * @param auditItemId
	 * @param error
	 * @return
	 */
	public static int queryRelateOverdueBidNum(int auditItemId, int year, int month, ErrorInfo error) {
		error.clear();
		//String sql = "SELECT COUNT(a.id) FROM t_bids a WHERE a.mark IN (SELECT mark FROM t_product_audit_items_log b WHERE b.audit_item_id = ?) AND a.id IN (SELECT DISTINCT(c.bid_id) FROM t_bills c WHERE YEAR(c.mark_overdue_time) = ? AND MONTH(c.mark_overdue_time) = ? AND c.overdue_mark IN (?, ?, ?))";
		String sql = "SELECT COUNT(a.id) FROM t_bids a WHERE a.mark IN (SELECT mark FROM t_product_audit_items_log b WHERE b.audit_item_id = ?) AND a.id IN (SELECT DISTINCT(c.bid_id) FROM t_bills c WHERE  c.overdue_mark IN (?, ?, ?))";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, auditItemId);
		query.setParameter(2, Constants.BILL_NORMAL_OVERDUE);
		query.setParameter(3, Constants.BILL_OVERDUE);
		query.setParameter(4, Constants.BILL_BAD_DEBTS);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询关联逾期借款标数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询关联逾期借款标数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询关联坏账借款标数量
	 * @param auditItemId
	 * @param error
	 * @return
	 */
	public static int queryRelateBadBidNum(int auditItemId, int year, int month, ErrorInfo error) {
		error.clear();
		String sql = "SELECT COUNT(a.id) FROM t_bids a WHERE a.mark IN (SELECT b.mark FROM t_product_audit_items_log b WHERE b.audit_item_id = ?) AND a.id IN (SELECT DISTINCT(c.bid_id) FROM t_bills c WHERE c.overdue_mark = ? )";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, auditItemId);
		query.setParameter(2, Constants.BILL_BAD_DEBTS);
		Object obj = null;
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询关联坏账借款标数量数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询关联坏账借款标数量出现异常！";
			
			return 0;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询风控有效性排名
	 * @param auditItemId
	 * @param error
	 * @return
	 */
	public static int queryRiskControlRanking(int auditItemId, int year, int month, ErrorInfo error) {
		error.clear();
		//String sql = "SELECT COUNT(*) + ? FROM ((SELECT m.auditItrmId AS auditItrmId,IFNULL(m.auditCount,0) AS auditCount FROM ((SELECT b.audit_item_id AS auditItrmId,COUNT(b.id) AS auditCount FROM t_bids a,t_product_audit_items_log b WHERE a.mark = b.mark AND a.id IN (SELECT DISTINCT(c.bid_id) FROM t_bills c WHERE YEAR (c.mark_overdue_time) = ? AND MONTH (c.mark_overdue_time) = ? AND c.overdue_mark IN (?, ?, ?)) GROUP BY b.audit_item_id ORDER BY auditCount) m RIGHT JOIN t_dict_audit_items n ON m.auditItrmId = n.id))) t1 WHERE t1.auditCount < (SELECT t2.auditCount FROM (SELECT b.audit_item_id AS auditItrmId,COUNT(b.id) AS auditCount FROM t_bids a,t_product_audit_items_log b WHERE a.mark = b.mark AND a.id IN (SELECT DISTINCT(c.bid_id) FROM t_bills c WHERE YEAR (c.mark_overdue_time) = ? AND MONTH (c.mark_overdue_time) = ? AND c.overdue_mark IN (?, ?, ?)) GROUP BY b.audit_item_id ORDER BY auditCount) t2 WHERE t2.auditItrmId = ?)";
		String sql = "SELECT COUNT(*) + ? FROM ((SELECT m.auditItrmId AS auditItrmId,IFNULL(m.auditCount,0) AS auditCount FROM ((SELECT b.audit_item_id AS auditItrmId,COUNT(b.id) AS auditCount FROM t_bids a,t_product_audit_items_log b WHERE a.mark = b.mark AND a.id IN (SELECT DISTINCT(c.bid_id) FROM t_bills c WHERE  c.overdue_mark IN (?, ?, ?)) GROUP BY b.audit_item_id ORDER BY auditCount) m RIGHT JOIN t_dict_audit_items n ON m.auditItrmId = n.id))) t1 WHERE t1.auditCount < (SELECT t2.auditCount FROM (SELECT b.audit_item_id AS auditItrmId,COUNT(b.id) AS auditCount FROM t_bids a,t_product_audit_items_log b WHERE a.mark = b.mark AND a.id IN (SELECT DISTINCT(c.bid_id) FROM t_bills c WHERE  c.overdue_mark IN (?, ?, ?)) GROUP BY b.audit_item_id ORDER BY auditCount) t2 WHERE t2.auditItrmId = ?)";
 		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, Constants.ONE);
		//query.setParameter(2, year);
		//query.setParameter(3, month);
		query.setParameter(2, Constants.BILL_NORMAL_OVERDUE);
		query.setParameter(3, Constants.BILL_OVERDUE);
		query.setParameter(4, Constants.BILL_BAD_DEBTS);
		//query.setParameter(7, year);
		//query.setParameter(8, month);
		query.setParameter(5, Constants.BILL_NORMAL_OVERDUE);
		query.setParameter(6, Constants.BILL_OVERDUE);
		query.setParameter(7, Constants.BILL_BAD_DEBTS);
		query.setParameter(8, auditItemId);
		
		Object obj;
		
		try {
			obj = query.getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询关联坏账借款标数量数量时："+e.getMessage());
			error.code = -1;
			error.msg = "查询关联坏账借款标数量出现异常！";
			
			return 0;
		}
		
		if (null == obj) {
			
			return 0; 
		}
		
		return Integer.parseInt(obj.toString());
	}
}
