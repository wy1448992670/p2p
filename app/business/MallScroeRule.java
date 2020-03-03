package business;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import constants.Constants;
import constants.MallConstants;
import models.t_mall_scroe_rule;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 积分商城 积分规则业务逻辑
 * 
 * @author yuy
 * @time 2015-10-13 17:17
 *
 */
public class MallScroeRule {

	/**
	 * 查询积分规则列表
	 * 
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static PageBean<t_mall_scroe_rule> queryMallScroeRuleByPage(int currPage, int pageSize, ErrorInfo error) {
		PageBean<t_mall_scroe_rule> page = new PageBean<t_mall_scroe_rule>();
		if (currPage == 0) {
			currPage = 1;
		}
		if (pageSize == 0) {
			pageSize = Constants.PAGE_SIZE;
		}

		StringBuffer sql_count = new StringBuffer("select count(*) from t_mall_scroe_rule where 1 = 1");
		StringBuffer sql_list = new StringBuffer("select new t_mall_scroe_rule(id, time, type, scroe, status) from t_mall_scroe_rule t where 1=1");
		List<Object> params = new ArrayList<Object>();
		page.currPage = currPage;
		page.pageSize = pageSize;
		sql_list.append(" order by id desc");
		int count = 0;
		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql_count.toString());

		for (int n = 1; n <= params.size(); n++) {
			query.setParameter(n, params.get(n - 1));
		}

		List<?> list = null;
		try {
			list = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询积分规则记录数时:" + e.getMessage());
			error.code = MallConstants.DML_ERROR_CODE;
			return page;
		}

		count = list == null ? 0 : Integer.parseInt(list.get(0).toString());
		if (count < 1) {
			return page;
		}

		List<t_mall_scroe_rule> mallScroeRuleList = new ArrayList<t_mall_scroe_rule>();
		try {
			query = em.createQuery(sql_list.toString(), t_mall_scroe_rule.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			mallScroeRuleList = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询积分规则列表时：" + e.getMessage());
			error.code = MallConstants.DML_ERROR_CODE;
		}
		page.totalCount = count;
		page.page = mallScroeRuleList;
		return page;
	}

	/**
	 * 查询积分规则信息
	 * 
	 * @param id
	 * @return
	 */
	public static t_mall_scroe_rule queryRuleDetailById(long id) {
		t_mall_scroe_rule rule = null;
		try {
			rule = t_mall_scroe_rule.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询积分规则信息时：" + e.getMessage());
		}
		return rule;
	}

	/**
	 * 保存积分规则
	 * 
	 * @param goods
	 * @return
	 */
	public static int saveRuleDetail(t_mall_scroe_rule rule) {
		if (rule == null)
			return MallConstants.COM_ERROR_CODE;
		// update
		if (rule.id != null) {
			rule = clone(rule);
		} else {// insert 检查是否重复
			int result = dataDuplicateCheck(rule.type);
			if (result < 0)
				return result;
		}

		try {
			rule.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("保存积分规则时：" + e.getMessage());
			return MallConstants.DML_ERROR_CODE;
		}
		return MallConstants.SUCCESS_CODE;
	}

	/**
	 * 暂停积分规则使用
	 * 
	 * @param id
	 * @return
	 */
	public static int stopRule(long id, int status) {
		t_mall_scroe_rule rule = queryRuleDetailById(id);
		if (rule == null)
			return MallConstants.COM_ERROR_CODE;
		try {
			rule.status = status;// 暂停/开启
			rule.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("暂停/开启积分规则使用时：" + e.getMessage());
			return MallConstants.DML_ERROR_CODE;
		}
		return MallConstants.SUCCESS_CODE;
	}

	/**
	 * 克隆
	 * 
	 * @param goods
	 * @return
	 */
	private static t_mall_scroe_rule clone(t_mall_scroe_rule rule) {
		if (rule == null)
			return null;
		if (rule.id == null)
			return rule;

		t_mall_scroe_rule rule_ = queryRuleDetailById(rule.id);
		rule_.scroe = rule.scroe;
		rule_.type = rule.type;
		return rule_;
	}

	/**
	 * 重复规则校验
	 * 
	 * @param rule
	 * @return
	 */
	public static int dataDuplicateCheck(int type) {
		String sql = "select count(*) from t_mall_scroe_rule where type = ?";
		long count = 0;
		try {
			count = t_mall_scroe_rule.find(sql, type).first();
		} catch (Exception e) {
			Logger.info(e.getMessage());
			e.printStackTrace();
			return MallConstants.DML_ERROR_CODE;
		}
		if (count > 0)
			return MallConstants.DATA_DUPL_CODE;
		return MallConstants.SUCCESS_CODE;
	}

	/**
	 * 根据类型查询积分规则
	 * 
	 * @param rule
	 * @return
	 */
	public static t_mall_scroe_rule queryRuleDetailByType(int type) {
		t_mall_scroe_rule rule = null;
		try {
			rule = t_mall_scroe_rule.find(" type = ? and status = ?", type, MallConstants.STATUS_ENABLE).first();
		} catch (Exception e) {
			Logger.info(e.getMessage());
			e.printStackTrace();
			return new t_mall_scroe_rule();
		}
		return rule;
	}

}
