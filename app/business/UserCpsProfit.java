/*
 * @(#)UserCpsProfit.java 2016年9月29日上午10:41:38
 * Copyright 2015 Sopell, Inc. All rights reserved.
 */
package business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.exception.ConstraintViolationException;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.shove.Convert;

import models.t_user_cps_profit;
import models.user_cps_profit;
import play.Logger;
import play.db.jpa.JPA;
import utils.JPAUtil;
import utils.PageBean;
import utils.PageBeanForPlayJPA;


 /**
 * @description.  cps收益分成
 *  
 * @modificationHistory.  
 * @author liulj 2016年9月29日上午10:41:38 TODO
 */

public class UserCpsProfit implements Serializable {

	public long id = -1;
	public long user_id;
	public String user_mobile;
	public long recommend_user_id;
	public long bid_id;
	public String bid_title;
	public int bid_is_loan;
	public long invest_id;
	public double invest_corpus;
	public double invest_interest;
	public Date invest_time;
	public double cps_reward;
	public double cps_rate;
	public Date ins_dt;
	
	public UserCpsProfit(){}
	
	public static UserCpsProfit saveUserCPSProfit(UserCpsProfit userCpsProfit, String log){
    	try {
    		t_user_cps_profit user_cps_profit = new t_user_cps_profit();
    		BeanUtils.copyProperties(user_cps_profit, userCpsProfit);
    		
    		long count = t_user_cps_profit.find("select count(id) from t_user_cps_profit where user_id = ? and recommend_user_id = ? and invest_id = ?", user_cps_profit.user_id, user_cps_profit.recommend_user_id, user_cps_profit.invest_id).first();
    		if(count == 0){
    			user_cps_profit = user_cps_profit.save();
    			Logger.info("新增CPS分成账单："+log);
    			return userCpsProfit;
    		}
    		
		} catch (Exception e) {
			JPA.setRollbackOnly();
			if(e instanceof PersistenceException && (e.getCause() instanceof ConstraintViolationException || e.getCause() instanceof MySQLIntegrityConstraintViolationException)){
				Logger.info("历史CPS分成账单： "+log);
			}else{
				e.printStackTrace();
				Logger.error("新增CPS分成账单："+log);
			}
		}
    	return null;
    }
	
	/**
     * 
     * @author liulj
     * @creationDate. 2016年9月30日 上午11:00:26 
     * @description.  查询用户CPS分成记录
     * 
     * @param recommendUserId
     * @return
     */
    public static PageBean<user_cps_profit> cpsUsersProfitOld(Long recommendUserId, int currPage, int pageSize){
    	String sql = " from t_user_cps_profit where recommend_user_id = ? and cps_reward > 0 order by id desc";
    	String listSql = "select new user_cps_profit(user_mobile, bid_title, cps_reward, ins_dt)".concat(sql);
    	String countSql = "select count(id)".concat(sql);
		
		List<user_cps_profit> list = null;
		int count = 0;
		try {
			EntityManager em = JPA.em();
            Query query = em.createQuery(listSql);
            query.setParameter(1, recommendUserId);
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            list = query.getResultList();
            
            Query queryCount = em.createQuery(countSql);
            queryCount.setParameter(1, recommendUserId);
    		count = Convert.strToInt(queryCount.getSingleResult().toString(),0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		PageBean<user_cps_profit> page = new PageBean<user_cps_profit>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.page = list;
		
		return page;
	}
    /**
     * 
     * @author zqq
     * @creationDate. 2018年12月05日
     * @description.  查询用户CPS分成记录
     * 
     * @param recommendUserId
     * @return
     * @throws Exception 
     */
    public static PageBean<Map<String, Object>> cpsUsersProfit(Long recommendUserId, int currPage, int pageSize) throws Exception{

    	PageBean<Map<String, Object>> page =PageBeanForPlayJPA.
    			getPageBeanMapBySQL(" user.name,user.reality_name,profit.invest_corpus,profit.invest_interest,profit.cps_reward " 
    					, " from t_user_cps_profit profit "
    					+ " inner join t_users user on  user.id=profit.user_id "
    					+ " where profit.recommend_user_id=? and profit.cps_reward>0 "
    					+ " and user.recommend_time >=ifnull(str_to_date((select _value from t_system_options where id=10060), '%Y-%m-%d'),now()) "
    					, " order by profit.id desc ", currPage, pageSize, recommendUserId);
    	BigDecimal  sum_profit=(BigDecimal)JPAUtil.createNativeQuery(" select ifnull(sum(profit.cps_reward),0) "
    			+ " from t_user_cps_profit profit "
    			+ " inner join t_users user on  user.id=profit.user_id "
    			+ " where profit.recommend_user_id=? "
    			+ " and user.recommend_time >=ifnull(str_to_date((select _value from t_system_options where id=10060), '%Y-%m-%d'),now()) ", recommendUserId).getSingleResult();
    	page.conditions=new HashMap<String,Object>();
    	page.conditions.put("sum_profit", sum_profit);
		return page;
	}
}
