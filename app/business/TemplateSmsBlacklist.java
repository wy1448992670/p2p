package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

import constants.SQLTempletes;
import models.v_sms_blacklist;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.PageBean;
import utils.QueryUtil;

public class TemplateSmsBlacklist implements Serializable {
	/**
	 * 查询短信黑名单模板
	 * 
	 * @param currPage
	 * @param pageSize
	 * @param mobile
	 * @param error
	 * @return
	 */
	public static PageBean<v_sms_blacklist> querySmsBlacklist(int currPage, int pageSize, String mobile,
			ErrorInfo error) {
		currPage = currPage < 1? 1: currPage;
		pageSize = pageSize < 1? 10: pageSize;
		
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(SQLTempletes.V_SMS_BLACKLIST);
		if (StringUtils.isNotBlank(mobile)) {
			sql.append(" and t.mobile = ?");
			params.add(mobile);
		}
		sql.append(" order by t.sms_blacklist_dt desc");

		int count = 0;
		List<v_sms_blacklist> page = new ArrayList<v_sms_blacklist>();;
		try {
			EntityManager em = JPA.em();
			Logger.info("sql:"+sql.toString());
			Query query = em.createNativeQuery(sql.toString(), v_sms_blacklist.class);
			for(int n = 0; n < params.size(); n++){
				query.setParameter(n + 1, params.get(n));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			page = query.getResultList();
			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询短信黑名单模板时" + e.getMessage());
			error.code = -1;
			error.msg = "查询短信黑名单模板失败";

			return null;
		}

		Map<String, Object> paramsMap = new HashMap<String, Object>();
		if (StringUtils.isNotBlank(mobile)) {
			paramsMap.put("mobile", mobile);
		}

		PageBean<v_sms_blacklist> bean = new PageBean<v_sms_blacklist>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.page = page;
		bean.totalCount = count;
		bean.conditions = paramsMap;

		error.code = 0;

		return bean;
	}
	
	/**
	 * 更新短信黑名单用户状态
	 * @param userId
	 * @param isBlacklist
	 * @return
	 */
	public static int addOrUpdateSmsBlacklist(long userId, int isBlacklist) {
		String sql = "UPDATE t_users SET is_cps_blacklist = ?, cps_blacklist_dt = ? WHERE id = ? and (is_cps_blacklist = ? or is_cps_blacklist is null)";
		return JPAUtil.executeUpdate(new ErrorInfo(), sql, isBlacklist, new Date(), userId, isBlacklist == 0 ? 1 : 0);
	}
	
	/**
	 * 判断手机号是否在短信黑名单中
	 * @return
	 */
	public static boolean isMobileInSmsBlacklist(String Mobile){
		String sql = "select 1 from t_users where is_sms_blacklist = 1 and mobile = ?";
		List resultList = JPAUtil.createNativeQuery(sql, Mobile).getResultList();
		return resultList.size() > 0? true: false;
	}
	
}
