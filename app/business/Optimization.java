package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_debt_bill_invest;
import models.t_statistic_bill_invest;
import models.v_user_audit_item_stats;

import org.apache.commons.lang.StringUtils;

import bean.AgencyBid;
import bean.FullBidsApp;
import bean.QualityBid;
import play.Logger;
import play.db.jpa.JPA;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import constants.Constants;
import constants.SQLTempletes;

/**
 * 优化类，所有优化，冗余都写在这个里面
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-12-25 上午10:08:08
 */
public class Optimization {

	/**
	 * 用户这块
	 * @author bsr
	 * @version 6.0
	 * @created 2015-1-10 上午10:19:58
	 */
	public static class UserOZ implements Serializable{
		public long userId; // 用户ID
        public int auditingCount; // 待审核借款标
        public int repaymentingCount; // 还款中借款标
        public int receivableInvestBidsCount; // 收款中理财标
        public int untreatedBillsCount; // 未处理借款账单
        public int untreatedInvestBillsCount; // 未处理理财账单
        public int overdueBillsCount; // 逾期借款账单
        public int lackSuditItemCount; // 须上传的审核资料
        
        public double user_account;  //账户总额  
        public double freeze;  //冻结金额
        public double user_account2;  //平台账户可用余额（资金托管模式）
        public double invest_amount;  //投标总额
        public int invest_count;  //投标数量
        public double receive_amount;  //应收账款
        public double bid_amount;  //借款总额
        public int bid_count;  //借款标数量
        public double repayment_amount;  //应还账款
        
        // v.8.02 wangyun
        public double receive_corpus;//待收本金
        
        public UserOZ(){}
        
        public UserOZ(long userId){
        	this.userId = userId;
        	
        	String sql = "select new map(t.balance as balance, t.freeze as freeze, t.balance2 as balance2) from t_users t where t.id = ?";
        	List<Map<?, ?>> maps = null;
        	
        	EntityManager em = JPA.em();
        	Query query = em.createQuery(sql).setParameter(1, this.userId);
        	
        	try {
				maps = query.getResultList();
			} catch (Exception e) { 
				Logger.error("查询用户信息时：" + e.getMessage());
			}
        	
        	if (maps != null && maps.size() > 0) {
        		
        		Map<?, ?> map = maps.get(0);
        		
        		if (map.size() > 2) {        			
					this.freeze = (Double) map.get("freeze");
					this.user_account = (Double) map.get("balance") + this.freeze;
					this.user_account2 = (Double) map.get("balance2");
				}
			}
        }

		public int getAuditingCount() {
            String sql = "SELECT count(1)  FROM t_bids WHERE user_id = ? AND status = 0";
            List<Object> count = null;
            
            try {
                Query query = JPA.em().createNativeQuery(sql).setParameter(1, this.userId);
                count = query.getResultList();
            } catch (Exception e) {
                Logger.error("" + e.getMessage());
                
                return 0;
            }
             
            if(null == count || count.size() == 0){
                
                return 0;
            }
            
            return Integer.parseInt(count.get(0).toString());
        }
        
		public double getReceive_corpus(){
			if (this.receive_corpus == 0) {
				String sql = "select  SUM(a.receive_corpus) from t_bill_invests  a  where   a.`status` IN (?,?,?) AND a.user_id = ? ";
				String sql_ = "SELECT SUM(a.receive_corpus) FROM t_debt_bill_invest a WHERE a.`status` IN (?, ?) AND  a.user_id = ? ";
				double record = 0;
				double record_ = 0;
				double debt_record = 0;
				
				EntityManager em = JPA.em();
				
				Query query = em.createNativeQuery(sql).setParameter(1, Constants.NO_RECEIVABLES).setParameter(2, Constants.OVERDUE_NORECEIVABLES).setParameter(3, Constants.FOR_DEBT_TRANSFERING).setParameter(4, this.userId);
				Query query_ = em.createNativeQuery(sql_).setParameter(1, Constants.NO_RECEIVABLES).setParameter(2, Constants.OVERDUE_NORECEIVABLES).setParameter(3, this.userId);
				try {
					record = query.getResultList().get(0) == null ? 0 : Double.parseDouble(query.getResultList().get(0).toString());
					record_ = query_.getResultList().get(0) == null ? 0 : Double.parseDouble(query_.getResultList().get(0).toString());
				} catch (Exception e) {
					Logger.error("查询用户应收账款时：" + e.getMessage());
					
					return 0d;
				}
				
				this.receive_corpus = record + record_-debt_record ;
			}
			
			return this.receive_corpus;
		}
		
        public int getRepaymentingCount() {
            String sql = "select count(1) from t_bids where user_id = ? and status in(4,14)";  //状态14：本金垫付还款中，但借款人未还款
            List<Object> count = null;
            
            try {
                Query query = JPA.em().createNativeQuery(sql).setParameter(1, this.userId);
                count = query.getResultList();
            } catch (Exception e) {
                Logger.error("" + e.getMessage());
                
                return 0;
            }
            
            if (null == count || count.size() == 0) {
                
                return 0;
            }
            
            return Integer.parseInt(count.get(0).toString());
        }
        
        public int getReceivableInvestBidsCount() {
            String sql = "SELECT count(b.id) FROM (t_bids b LEFT JOIN t_invests c ON (b.id = c.bid_id) ) WHERE ( (c.user_id = ?) AND (c.transfer_status IN (0, 1) ) AND (b.status = 4) )";
            List<Object> count = null;
            
            try {
                Query query = JPA.em().createNativeQuery(sql).setParameter(1, this.userId);
                count = query.getResultList();
            } catch (Exception e) {
                Logger.error("" + e.getMessage());
                
                return 0;
            }
            
            if (null == count || count.size() == 0) {
                
                return 0;
            }
            
            return Integer.parseInt(count.get(0).toString());
        }
        
        public int getUntreatedBillsCount() {
            String sql = "SELECT count(b.id) FROM ( t_bills b LEFT JOIN t_bids d ON ( (b.bid_id = d.id) ) ) WHERE (  (d.user_id = ?) AND (b.status = -(1) ) )";
            List<Object> count = null;
            
            try {
                Query query = JPA.em().createNativeQuery(sql).setParameter(1, this.userId);
                count = query.getResultList();
            } catch (Exception e) {
                Logger.error("" + e.getMessage());
                
                return 0;
            }
            
            if (null == count || count.size() == 0) {
                
                return 0;
            }
            
            return Integer.parseInt(count.get(0).toString());
        }
        
        public int getUntreatedInvestBillsCount() {
            String sql = "SELECT count(b.id) FROM t_bill_invests b WHERE ( (b.status IN (-1 ,-2 ,-5 ,-6)) AND (b.user_id = ?) )";
            List<Object> count = null;
            
            try {
                Query query = JPA.em().createNativeQuery(sql).setParameter(1, this.userId);
                count = query.getResultList();
            } catch (Exception e) {
                Logger.error("" + e.getMessage());
                
                return 0;
            }
            
            if (null == count || count.size() == 0) {
                
                return 0;
            }
            
            return Integer.parseInt(count.get(0).toString());
        
        }
        
        public int getOverdueBillsCount() {
            String sql = "SELECT count(b.id) FROM ( t_bills b LEFT JOIN t_bids c ON (b.bid_id = c.id)) WHERE ( ( b.overdue_mark IN (-(1) ,-(2) ,-(3)) ) AND (c.user_id = ?) )";
            List<Object> count = null;
            
            try {
                Query query = JPA.em().createNativeQuery(sql).setParameter(1, this.userId);
                count = query.getResultList();
            } catch (Exception e) {
                Logger.error("" + e.getMessage());
                
                return 0;
            }
            
            if (null == count || count.size() == 0) {
                
                return 0;
            }
            
            return Integer.parseInt(count.get(0).toString());
        }
       
        public int getLackSuditItemCount() {
            String sql = "SELECT count(b.id) FROM t_user_audit_items b WHERE   (   (b.user_id = ?) AND (b.status = 0) )";
            List<Object> count = null;
            
            try {
                Query query = JPA.em().createNativeQuery(sql).setParameter(1, this.userId);
                count = query.getResultList();
            } catch (Exception e) {
                Logger.error("" + e.getMessage());
                
                return 0;
            }
            
            if (null == count || count.size() == 0) {
                
                return 0;
            }
            
            return Integer.parseInt(count.get(0).toString());
        }

        
        
        
		public Double getUser_account() {
			return user_account;
		}

		public Double getFreeze() {
			return freeze;
		}

		public Double getUser_account2() {
			return user_account2;
		}

		public Double getInvest_amount() {
			if(0 == this.invest_amount){
				String sql = "SELECT SUM(a.amount) AS invest_amount FROM t_invests a,t_bids b WHERE a.bid_id = b.id AND b.`status` IN (?, ?, ?, ?, ?, ?) AND a.user_id = ?";
				Object record = null;
				
				EntityManager em = JPA.em();
				Query query = em.createNativeQuery(sql).setParameter(1, Constants.BID_ADVANCE_LOAN).setParameter(2, Constants.BID_FUNDRAISE).setParameter(3, Constants.BID_EAIT_LOAN).setParameter(4, Constants.BID_REPAYMENT).setParameter(5, Constants.BID_REPAYMENTS).setParameter(6, Constants.BID_COMPENSATE_REPAYMENT).setParameter(7, this.userId);
				
				try {
					record = query.getResultList().get(0);
				} catch (Exception e) {
					Logger.error("查询用户投资总额时：" + e.getMessage());
					
					return 0d;
				}
				
				this.invest_amount = null == record ? 0 : Double.parseDouble(record.toString());
			}
			
			return this.invest_amount;
		}

		public Integer getInvest_count() {
			if (0 == this.invest_count) {
				String sql = "SELECT COUNT(DISTINCT(a.bid_id)) AS invest_count FROM t_invests a,t_bids b WHERE a.bid_id = b.id AND b.`status` IN (?, ?, ?, ?, ?, ?) AND a.user_id = ? ";
				Object record = null;
				
				EntityManager em = JPA.em();
				Query query = em.createNativeQuery(sql).setParameter(1, Constants.BID_ADVANCE_LOAN).setParameter(2, Constants.BID_FUNDRAISE).setParameter(3, Constants.BID_EAIT_LOAN).setParameter(4, Constants.BID_REPAYMENT).setParameter(5, Constants.BID_REPAYMENTS).setParameter(6, Constants.BID_COMPENSATE_REPAYMENT).setParameter(7, this.userId);
				
				try {
					record = query.getResultList().get(0);
				} catch (Exception e) {
					Logger.error("查询用户投标数量时：" + e.getMessage());
					
					return 0;
				}
				
				this.invest_count = null == record ? 0 : Integer.parseInt(record.toString());
			}
		
			return this.invest_count;
		}

		public Double getReceive_amount() {
			if (this.receive_amount == 0) {
				String sql = "SELECT SUM(a.receive_corpus + a.receive_interest + a.receive_increase_interest) FROM t_bill_invests a WHERE a.`status` IN (?, ?) AND  a.user_id = ? ";
				String sql_ = "SELECT SUM(a.receive_corpus + a.receive_interest) as debt_receive_money FROM t_debt_bill_invest a WHERE a.`status` IN (?, ?) AND  a.user_id = ? ";
				double record = 0;
				double record_ = 0;
				
				EntityManager em = JPA.em();
				
				Query query = em.createNativeQuery(sql).setParameter(1, Constants.NO_RECEIVABLES).setParameter(2, Constants.OVERDUE_NORECEIVABLES).setParameter(3, this.userId);
				Query query_ = em.createNativeQuery(sql_).setParameter(1, Constants.NO_RECEIVABLES).setParameter(2, Constants.OVERDUE_NORECEIVABLES).setParameter(3, this.userId);
				 
				try {
					record = query.getResultList().get(0) == null ? 0 : Double.parseDouble(query.getResultList().get(0).toString());
					record_ = query_.getResultList().get(0) == null ? 0 : Double.parseDouble(query_.getResultList().get(0).toString());
					
				} catch (Exception e) {
					Logger.error("查询用户应收账款时：" + e.getMessage());
					
					return 0d;
				}
				
				this.receive_amount = record + record_ ;
			}
			
			return this.receive_amount;
		}

		public Double getBid_amount() {
			if (0 == this.bid_amount) {
				String sql = "SELECT SUM(a.amount) FROM t_bids a WHERE a.`status` IN (?, ?, ?) AND a.user_id = ?";
				Object record = null;
				
				EntityManager em = JPA.em();
				Query query = em.createNativeQuery(sql).setParameter(1, Constants.BID_REPAYMENT).setParameter(2, Constants.BID_REPAYMENTS).setParameter(3, Constants.BID_COMPENSATE_REPAYMENT).setParameter(4, this.userId);
				
				try {
					record = query.getResultList().get(0);
				} catch (Exception e) {
					Logger.error("查询用户借款总额时：" + e.getMessage());
					
					return 0d;
				}
				
				this.bid_amount = record == null ? 0 : Double.parseDouble(record.toString());
			}
			
			return this.bid_amount;
		}

		public Integer getBid_count() {
			if (0 == this.bid_count) {
				String sql = "SELECT COUNT(1) FROM t_bids a WHERE a.`status` >= ? AND a.user_id = ?";
				Object record = null;
				
				EntityManager em = JPA.em();
				Query query = em.createNativeQuery(sql).setParameter(1, Constants.BID_AUDIT).setParameter(2, this.userId);
				
				try {
					record = query.getResultList().get(0);
				} catch (Exception e) {
					Logger.error("查询用户借款标数量时：" + e.getMessage());
					
					return 0;
				}
				
				this.bid_count = record == null ? 0 : Integer.parseInt(record.toString());
			}
			
			return this.bid_count;
		}

		public Double getRepayment_amount() {
			if (0 == this.repayment_amount) {
				String sql = "SELECT SUM(a.repayment_corpus + a.repayment_interest) FROM t_bills a LEFT JOIN t_bids b ON a.bid_id = b.id WHERE a.`status` IN (?, ?) AND b.user_id = ? ";
				Object record = null;
				
				EntityManager em = JPA.em();
				Query query = em.createNativeQuery(sql).setParameter(1, Constants.NO_REPAYMENT).setParameter(2, Constants.ADVANCE_PRINCIIPAL_REPAYMENT).setParameter(3, this.userId);
				
				try {
					record = query.getResultList().get(0);
				} catch (Exception e) {
					Logger.error("查询用户应还账款时：" + e.getMessage());
					
					return 0d;
				}
				
				this.repayment_amount = record == null ? 0 : Double.parseDouble(record.toString());
			}
			
			return this.repayment_amount;
		}
	}
	
	/**
	 * 资料这块
	 * @author bsr
	 * @version 6.0
	 * @created 2015-1-10 上午10:46:29
	 */
	public static class AuditItemOZ {

		/**
		 * 创建记录
		 * 
		 * @param userId
		 */
		public void create(long userId) {
			if(userId < 1) {
				return ;
			}
			
			String sql = "insert into t_statistic_user_audit_items(user_id) value(?)";
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, userId);

			try {
				query.executeUpdate();
			} catch (Exception e) {
				Logger.error("创建会员审核资料管理冗余字段标失败" + e.getMessage());
			}
		}

		/**
		 * 更新资料数据
		 */
		public static void createItemStatistic(long userId){
			String sql = "select tmp1.count c1, tmp2.count c2, tmp3.count c3, tmp4.count c4 from "
					+ "(select count(distinct audit_item_id) as count from t_user_audit_items where user_id = ? and status not in(?, ?)) tmp1,"
					+ "(select count(distinct audit_item_id) as count from t_user_audit_items where user_id = ? and status = ?) tmp2,"
					+ "(select count(distinct audit_item_id) as count from t_user_audit_items where user_id = ? and status = ?) tmp3,"
					+ "(select count(distinct audit_item_id) as count from t_user_audit_items where user_id = ? and status = ?) tmp4";
			
			List<Object[]> items = null;
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, userId);
			query.setParameter(2, Constants.UNCOMMITTED);
			query.setParameter(3, Constants.UPLOAD);
			query.setParameter(4, userId);
			query.setParameter(5, Constants.AUDITED);
			query.setParameter(6, userId);
			query.setParameter(7, Constants.NOT_PASS);
			query.setParameter(8, userId);
			query.setParameter(9, Constants.AUDITING);
			
			try {
				items = query.getResultList(); // 资料总数
			} catch (Exception e) {
				Logger.info("查询 资料总数时，%s", e.getMessage());
				return ;
			}
			
			if(null == items || items.get(0) == null) {
				return ;
			}
			
			int sumCount = 0;
			int auditedCount = 0;
			int notPassCount = 0;
			int auditingCount = 0;
			
			for (Object[] item : items) {
				sumCount = item[0] == null ? 0 : Integer.parseInt(item[0].toString());
				auditedCount = item[1] == null ? 0 : Integer.parseInt(item[1].toString());
				notPassCount = item[2] == null ? 0 : Integer.parseInt(item[2].toString());
				auditingCount = item[3] == null ? 0 : Integer.parseInt(item[3].toString());
				
				break ;
			}
			
			sql = "update t_statistic_user_audit_items set sum_count = ?, audited_count = ?, not_pass_count = ?, auditing_count = ? where user_id = ?";
			query = em.createNativeQuery(sql);
			query.setParameter(1, sumCount);
			query.setParameter(2, auditedCount);
			query.setParameter(3, notPassCount);
			query.setParameter(4, auditingCount);
			query.setParameter(5, userId);
			
			try {
				query.executeUpdate();
			} catch (Exception e) {
				Logger.error("更新资料数据失败" + e.getMessage());
			}
		}
		
		/**
		 * 平台借款标管理->会员借款资料审核管理
		 */
		public static PageBean<v_user_audit_item_stats> queryStatistic(
				int currPage, int pageSize, int condition, String keyword,
				int orderIndex, int orderStatus) {
			currPage = currPage == 0 ? 1 : currPage;
			pageSize = pageSize == 0 ? 10 : pageSize;

			StringBuffer conditions = new StringBuffer(" where 1 = 1");
			List<Object> values = new ArrayList<Object>();
			Map<String, Object> conditionmap = new HashMap<String, Object>();

			switch (condition) {
			/* 名称搜索 */
			case Constants.ITEM_SEARCH_NAME:
				conditions.append(" AND a.name LIKE ?");
				values.add("%" + keyword + "%");

				break;

			/* 邮箱搜索 */
			case Constants.ITEM_SEARCH_EMAIL:
				conditions.append(" AND a.email LIKE ?");
				values.add("%" + keyword + "%");

				break;

			/* 全部搜索 */
			case Constants.SEARCH_ALL:

				if (StringUtils.isBlank(keyword))
					break;

				conditions.append(" AND (a.name LIKE ? OR a.email LIKE ?)");
				values.add("%" + keyword + "%");
				values.add("%" + keyword + "%");

				break;
			}

			conditionmap.put("condition", condition);
			conditionmap.put("keyword", keyword);
			conditionmap.put("orderIndex", orderIndex);

			PageBean<v_user_audit_item_stats> page = new PageBean<v_user_audit_item_stats>();
			page.currPage = currPage;
			page.pageSize = pageSize;
			page.conditions = conditionmap;

			StringBuffer sql = new StringBuffer();
			sql.append("SELECT count(1) FROM t_users a LEFT JOIN t_statistic_user_audit_items b ON a.id = b.user_id");
			sql.append(conditions);

			List<Object> count = null;
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString());
			int len = values.size();
			if (len > 0)
				for (int i = 0; i < len; i++)
					query.setParameter((i + 1), values.get(i));

			try {
				count = query.getResultList();
			} catch (Exception e) {
				Logger.error("会员借款资料审核管理:->" + e.getMessage());

				return null;
			}

			if (null == count || count.size() == 0)
				return page;

			page.totalCount = Integer.parseInt(count.get(0).toString());

			conditions.append(Constants.ITEMS_SEARCH_ORDER[orderIndex]);

			/* 升降序 */
			if (orderIndex > 0) {

				if (orderStatus == 1)
					conditions.append("ASC");
				else
					conditions.append("DESC");

				/* 保存当前索引值 + 升降值 */
				conditionmap.put("orderStatus", orderStatus);
			}

			sql = new StringBuffer();
			sql.append(SQLTempletes.V_USER_AUDIT_ITEM_STATS);
			sql.append(conditions);

			query = em.createNativeQuery(sql.toString(),
					v_user_audit_item_stats.class);
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			List<v_user_audit_item_stats> lists = null;
			if (len > 0)
				for (int i = 0; i < len; i++)
					query.setParameter((i + 1), values.get(i));

			try {
				lists = query.getResultList();
			} catch (Exception e) {
				Logger.error("会员借款资料审核管理:->" + e.getMessage());

				return null;
			}

			page.page = lists;

			return page;
		}
	}
	
	/**
	 * 标这块
	 * @author bsr
	 * @version 6.0
	 * @created 2015-1-10 上午10:19:58
	 */
	public static class BidOZ {
    	/**
    	 * 前台->我的账户->优质借款标
    	 * @param size
    	 * @param error
    	 * @return
    	 */
    	public static List<QualityBid> queryQualityBid(int size, ErrorInfo error){
    		String sql = "SELECT `b`.`id` AS `id`,`b`.`user_id` AS `user_id`,`b`.`image_filename` AS `bid_image_filename`,`p`.`small_image_filename` AS `small_image_filename`,`b`.`title` AS `title`,`b`.`loan_schedule` AS `loan_schedule`,`b`.`apr` AS `apr`,`b`.`amount` AS `amount`,`b`.`period_unit` AS `period_unit`,`b`.`period` AS `period`,`b`.`has_invested_amount` AS `has_invested_amount`,`r`.`name` AS `repayment_type_name` FROM ((`t_bids` `b` LEFT JOIN `t_products` `p` ON ((`b`.`product_id` = `p`.`id`))) LEFT JOIN `t_dict_bid_repayment_types` `r` ON (( `r`.`id` = `b`.`repayment_type_id`))) WHERE b.is_quality = 1 AND b.amount > b.has_invested_amount AND b. STATUS IN (?, ?) ORDER BY `b`.`id` DESC";
     		
    		EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql, QualityBid.class);
			query.setParameter(1, Constants.BID_ADVANCE_LOAN);
			query.setParameter(2, Constants.BID_FUNDRAISE);
			query.setMaxResults(size);
			
    		try {
				return query.getResultList(); 
			} catch (Exception e) {
				Logger.error("标->优质标列表:" + e.getMessage());
				error.msg = "加载优质标列表失败!";
				
				return null;
			}
    	}
    	
    	/**
    	 * 后台合作机构列表
    	 * @param pageBean
    	 * @param error
    	 * @param str
    	 * @return
    	 */
    	public static List<AgencyBid> queryAgencyBids(int noPage, PageBean<AgencyBid> pageBean, ErrorInfo error , String ... str){
			String[] BID_SEARCH = {" AND (`u`.`name` LIKE ? OR concat(`e`.`_value`,cast(`b`.`id` AS CHAR charset utf8)) LIKE ?)",
					" AND concat(`e`.`_value`,cast(`b`.`id` AS CHAR charset utf8)) LIKE ?", "", " AND `u`.`name` LIKE ?" };
    		
    		Map<String, Object> conditionMap = new HashMap<String, Object>();
    		List<Object> valuesCount = new ArrayList<Object>();
    		List<Object> values = new ArrayList<Object>();
    		
    		
    		StringBuffer sqlCount = new StringBuffer();
    		StringBuffer sql = new StringBuffer();
    		
    		sqlCount.append("SELECT COUNT(1) FROM ((`t_bids` `b`LEFT JOIN `t_users` `u` ON ((`u`.`id` = `b`.`user_id`))) JOIN `t_system_options` `e`) WHERE (`e`.`_key` = 'loan_number') AND is_agency = 1");
    		sql.append("SELECT concat(`e`.`_value`,cast(`b`.`id` AS CHAR charset utf8)) AS `bid_no`,`b`.`id` AS `id`,`u`.`id` AS `user_id`,`u`.`name` AS `user_name`,`p`.`small_image_filename` AS `small_image_filename`,`b`.`title` AS `title`,`b`.`loan_schedule` AS `loan_schedule`,`b`.`apr` AS `apr`,`b`.`amount` AS `amount`,`b`.`period_unit` AS `period_unit`,`b`.`period` AS `period`,`b`.`status` AS `status`,`a`.`name` AS `agency_name`,`b`.`time` AS `time`,c.order_sort AS `order_sort`,c.image_filename AS credit_level_image_filename,`f_user_audit_item` (`u`.`id`, `b`.`mark`, 2) AS `user_item_count_true`,(SELECT count(`pail`.`id`) AS `product_item_count` FROM `t_product_audit_items_log` `pail` WHERE ((`pail`.`mark` = `b`.`mark`) AND (`pail`.`type` = 1))) AS `product_item_count` FROM ((((((`t_bids` `b` LEFT JOIN `t_products` `p` ON ((`b`.`product_id` = `p`.`id`))) LEFT JOIN `t_users` `u` ON ((`u`.`id` = `b`.`user_id`))) LEFT JOIN t_credit_levels c ON ((u.credit_level_id = c.id))) LEFT JOIN `t_agencies` `a` ON ((`b`.`agency_id` = `a`.`id`))) LEFT JOIN `t_dict_bid_repayment_types` `r` ON ((`r`.`id` = `b`.`repayment_type_id`))) JOIN `t_system_options` `e`) WHERE (`e`.`_key` = 'loan_number') AND `b`.is_agency = 1");
    		
    		/* 条件 */
    		if (str.length > 1 && NumberUtil.isNumericInt(str[1])) {
				int c = Integer.parseInt(str[1]);
				
				sqlCount.append(BID_SEARCH[c]);
				sql.append(BID_SEARCH[c]);
				
				if (0 == c) {
					valuesCount.add("%" + str[2] + "%");
					valuesCount.add("%" + str[2] + "%");
					values.add("%" + str[2] + "%");
					values.add("%" + str[2] + "%");
				}else{
					valuesCount.add("%" + str[2] +"%");
					values.add("%" + str[2] +"%");
				}
				
				conditionMap.put("condition", str[1]);
				conditionMap.put("keyword", str[2]);
			}
    		
    		/* 开始时间 */
    		if (str.length > 3 && StringUtils.isNotBlank(str[3])) {
    			sqlCount.append(" AND `b`.`time` >= ?");
    			sql.append(" AND `b`.`time` >= ?");
    			valuesCount.add(DateUtil.strDateToStartDate(str[3]));
				values.add(DateUtil.strDateToStartDate(str[3]));
				conditionMap.put("startDate", str[3]);
			}
    		
    		/* 结束时间 */
    		if (str.length > 4 && StringUtils.isNotBlank(str[4])) {
    			sqlCount.append(" AND `b`.`time` <= ?");
    			sql.append(" AND `b`.`time` <= ?");
    			valuesCount.add(DateUtil.strDateToEndDate(str[4]));
				values.add(DateUtil.strDateToEndDate(str[4]));
				conditionMap.put("endDate", str[4]);
			}
    		
    		/* 排序 */
    		if (str.length > 5 && NumberUtil.isNumericInt(str[5])) {
				int _order = Integer.parseInt(str[5]);
				sql.append(Constants.BID_SEARCH_ORDER[_order]);
				
				conditionMap.put("orderIndex", str[5]);
    		
				/* 升降序  */
				if (str.length > 6 && NumberUtil.isNumericInt(str[6]) && _order > 0) {
					if (Integer.parseInt(str[6]) == 1) {
						sql.append("ASC");
					}else{
						sql.append("DESC");
					}
			
					/* 保存当前索引值 + 升降值 */
					conditionMap.put("orderStatus", str[6]);
				}
    		}
    		
    		 /* 第一次让它为ID降序搜索 */ 
    		if(StringUtils.isBlank(str[5])) 
    			sql.append(Constants.BID_SEARCH_ORDER[0]); 

    		pageBean.conditions = conditionMap;
    		
    		EntityManager em = JPA.em();
    		Query query = em.createNativeQuery(sqlCount.toString());
    		
    		for (int i = 1; i <= values.size(); i++) {
				query.setParameter(i, valuesCount.get(i - 1));
			}
    		
    		List<?> list = null;
    		
    		try {
				list = query.getResultList();
			} catch (Exception e) {
				Logger.error("借款标管理->合作机构标列表,查询总记录时：" + e.getMessage());
				
				return null;
			}
    		
    		int count = -1;
    		
    		count = list == null ? 0 :Integer.parseInt(list.get(0).toString());
    		
    		if (count < 1) {
				return new ArrayList<AgencyBid>();
			}
    		
    		pageBean.totalCount = count;
    		
    		query = em.createNativeQuery(sql.toString(), AgencyBid.class);
    		
    		for (int i = 1; i <= values.size(); i++) {
				query.setParameter(i, values.get(i - 1));
			}
    		
    		if(noPage != Constants.NO_PAGE){
    			query.setFirstResult((pageBean.currPage - 1) * pageBean.pageSize);
    			query.setMaxResults(pageBean.pageSize);
    		}
    		
    		try {
				return query.getResultList();
			} catch (Exception e) {
				Logger.error("借款标管理->合作机构标列表：" + e.getMessage());
				
				return null;
			}
    	}
    	
    	/**
    	 * APP前台查询3个满表中的借款标
    	 * @param error
    	 * @return
    	 */
    	public static List<FullBidsApp> queryFullBid(ErrorInfo error){
    		error.clear();
    		error.code = -1;
    		
    		List<FullBidsApp> fullBids = new ArrayList<FullBidsApp>();
    		String sql = "SELECT a.id AS id,a.image_filename AS bid_image_filename,a.has_invested_amount AS has_invested_amount,a.apr AS apr,COUNT(1) AS num FROM t_bids a,t_invests b  WHERE a.id = b.bid_id AND a.`status` IN (?,?) AND a.amount = a.has_invested_amount AND b.transfer_status = ? GROUP BY a.id DESC";
    		
    		EntityManager em = JPA.em();
    		Query query = em.createNativeQuery(sql, FullBidsApp.class).setParameter(1, Constants.BID_ADVANCE_LOAN).setParameter(2, Constants.BID_FUNDRAISE).setParameter(3, Constants.INVEST_NORMAL);
    		query.setMaxResults(Constants.HOME_SHOW_AMOUNT_APP);
    		
    		try {
				fullBids = query.getResultList();
			} catch (Exception e) {
				Logger.error("查询满标中的借款标时：" + e.getMessage());
				error.code = -1;
				error.msg = "查询满标中的借款标时有误！";
				
				return null;
			}
    		
    		error.code = 0;
    		
    		return fullBids;
    	}
	}
	
	/**
	 * 理财这块
	 * @author bsr
	 * @version 6.0
	 * @created 2015-1-10 上午10:19:58
	 */
	public static class InvestOZ {
		/**
         * 理财情况统计表
         * @param userId
         * @param year
         * @param month
         * @param orderType
         * @param currPage
         * @param error
         * @return
         */
        public static PageBean<t_statistic_bill_invest> queryUserInvestStatistics(long userId, int year,int month,int orderType,int currPage,ErrorInfo error){
            error.clear();
            
            Map<String, Object> conditionMap = new HashMap<String, Object>();
            List<Object> values = new ArrayList<Object>();
            
            conditionMap.put("year", year);
            conditionMap.put("month", month);
            conditionMap.put("orderType", orderType);
            
            StringBuffer conditions = new StringBuffer("1=1 and user_id = ?");
            
            values.add(userId);
            
            if (year != 0) {
                conditions.append("and year = ? ");
                values.add(year);
            }
            
            if (month != 0) {
                conditions.append("and month = ? ");
                values.add(month);
            }
            
            if (orderType != 0) {
                conditions.append(Constants.INVEST_STATISTICS[orderType]);
            }
            
            List<t_statistic_bill_invest> bills = new ArrayList<t_statistic_bill_invest>();
            int count = 0;
            
            try {
                count = (int)t_statistic_bill_invest.count(conditions.toString(), values.toArray());
                bills = t_statistic_bill_invest.find(conditions.toString(), values.toArray()).fetch(currPage, Constants.PAGE_SIZE);
            } catch (Exception e) {
                Logger.info("查询理财情况统计表时：" + e.getMessage());
                error.code = -1;
                error.msg = "查询理财情况统计表时出现异常";
                
                return null;
            }
            
            PageBean<t_statistic_bill_invest> page = new PageBean<t_statistic_bill_invest>();
            
            page.pageSize = Constants.PAGE_SIZE;
            page.currPage = currPage;
            page.page = bills;
            page.totalCount = count;
            page.conditions = conditionMap;
            
            return page;
        }
        
        /**
         * 添加每月统计
         * @param error
         * @return
         */
        public static void add(){
            Calendar cal = Calendar.getInstance();
            
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            
            String sql = "insert into t_statistic_bill_invest(invest_id, user_id, year, month, invest_count, average_loan_amount, average_invest_period, average_invest_amount, invest_fee_back, invest_amount) " +
            		"(select `b`.`id` AS `id`,`b`.`user_id` AS `user_id`,year(`b`.`time`) AS `year`,month(`b`.`time`) AS `month`,(select count(`t`.`id`) from `t_invests` `t` where ((`t`.`user_id` = `b`.`user_id`) and (month(`t`.`time`) = month(`b`.`time`)) and (year(`t`.`time`) = year(`b`.`time`)))) AS `invest_count`,(select round((sum(`t1`.`amount`) / count(`t1`.`id`)),2) AS `ROUND((SUM(t1.amount) / COUNT(t1.id)),2)` from (`t_bids` `t1` join `t_invests` `t2` on((`t1`.`id` = `t2`.`bid_id`))) where ((`t2`.`user_id` = `b`.`user_id`) and (date_format(`t2`.`time`,'%y%m') = date_format(`b`.`time`,'%y%m')))) AS `average_loan_amount`,(select round((sum(`t1`.`period`) / count(`t1`.`id`)),0) AS `ROUND((SUM(t1.period) / COUNT(t1.id)),0)` from (`t_bids` `t1` join `t_invests` `t2` on((`t1`.`id` = `t2`.`bid_id`))) where ((`t2`.`user_id` = `b`.`user_id`) and (date_format(`t2`.`time`,'%y%m') = date_format(`b`.`time`,'%y%m')))) AS `average_invest_period`,(select round((sum(`t2`.`amount`) / count(`t2`.`id`)),0) AS `jj` from `t_invests` `t2` where ((`t2`.`user_id` = `b`.`user_id`) and (date_format(`t2`.`time`,'%y%m') = date_format(`b`.`time`,'%y%m')))) AS `average_invest_amount`,(select round((sum(`t1`.`apr`) / count(`t1`.`id`)),0) AS `ROUND((SUM(t1.apr) / COUNT(t1.id)),0)` from (`t_bids` `t1` join `t_invests` `t2` on((`t1`.`id` = `t2`.`bid_id`))) where ((`t2`.`user_id` = `b`.`user_id`) and (date_format(`t2`.`time`,'%y%m') = date_format(`b`.`time`,'%y%m')))) AS `invest_fee_back`,(select sum(`t`.`amount`) from `t_invests` `t` where ((`t`.`user_id` = `b`.`user_id`) and (month(`t`.`time`) = month(`b`.`time`)) and (year(`t`.`time`) = year(`b`.`time`)))) AS `invest_amount` from (`t_invests` `b` join `t_bids` `a` on((`b`.`bid_id` = `a`.`id`))) where (`a`.`status` in (4,5)) and YEAR(b.time) = ? and MONTH(b.time) = ? group by `b`.`user_id`)";
            Query query = JPA.em().createNativeQuery(sql);
            query.setParameter(1, year);
            query.setParameter(2, month);
            
            try {
                query.executeUpdate();
            } catch (Exception e) {
                Logger.error("添加理财子账户理财情况统计表时：" + e.getMessage());
            }
        }
	}
	
	/**
	 * 账单这块
	 * @author bsr
	 * @version 6.0
	 * @created 2015-1-10 上午10:19:58
	 */
	public static class BillOZ {
		
		/**
		 * 后台->我的会员账单
		 */
		public static final String [] C_TYPE = {"and (concat(`f`.`_value`,cast(`b`.`id` AS CHAR charset utf8)) like ? or name like ? or concat(`e`.`_value`,cast(`a`.`id` AS CHAR charset utf8)) like ?) ","and concat(`f`.`_value`,cast(`b`.`id` AS CHAR charset utf8)) like ? ","and name like ? ","and concat(`e`.`_value`,cast(`a`.`id` AS CHAR charset utf8)) like ? "};
	}
}