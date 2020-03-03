package business;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import constants.Constants;
import constants.SQLTempletes;
import constants.SupervisorEvent;
import play.Logger;
import play.db.DB;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import utils.ErrorInfo;
import utils.PageBean;
import utils.QueryUtil;
import models.*;

/**
 * 信用等级
 * @author lzp
 * @version 6.0
 * @created 2014-4-7 下午2:19:25
 */

public class CreditLevel implements Serializable{

	public long id;
	private long _id = -1;
	
	public Date time;
	public String name;
	public String imageFilename;
	public boolean isEnable;
	public boolean isAllowOverdue;
	public int minCreditScore;
	public int minAuditItems;
	public String suggest;
	public String mustItems;//1,2...
	public int order_sort;
	public boolean lazy;

	public void setId(long id) {
		t_credit_levels creditLevel = null;
		
		try {
			creditLevel = t_credit_levels.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			this._id = -1;
			
			return;
		}
		
		if (null == creditLevel) {
			this._id = -1;
			
			return;
		}
		
		if(lazy){
			this._id = creditLevel.id;
			this.imageFilename = creditLevel.image_filename;
			this.name = creditLevel.name;
			
			return;
		}
		
		setInfomation(creditLevel);
	}

	public long getId() {
		return _id;
	}

	/**
	 * 填充基本信息
	 * @param creditLevel
	 */
	private void setInfomation(t_credit_levels creditLevel) {
		if (null == creditLevel) {
			this._id = -1;
			
			return;
		}

		this._id = creditLevel.id;
		this.time = creditLevel.time;
		this.name = creditLevel.name;
		this.imageFilename = creditLevel.image_filename;
		this.isEnable = creditLevel.is_enable;
		this.isAllowOverdue = creditLevel.is_allow_overdue;
		this.minCreditScore = creditLevel.min_credit_score;
		this.minAuditItems = creditLevel.min_audit_items;
		this.suggest = creditLevel.suggest;
		this.mustItems = creditLevel.must_items;
	}
	
	/**
	 * 填充数据库实体
	 * @param cl
	 * @param error
	 * @return
	 */
	private int fillDBE(t_credit_levels cl, ErrorInfo error) {
		error.clear();
		
		if (StringUtils.isBlank(this.name)) {
			error.code = -1;
			error.msg = "名称不能为空";
			
			return error.code;
		}
		
		if (StringUtils.isBlank(this.imageFilename)) {
			error.code = -1;
			error.msg = "请上传等级图标";
			
			return error.code;
		}
		
		if (StringUtils.isBlank(this.suggest)) {
			error.code = -1;
			error.msg = "信贷建议不能为空";
			
			return error.code;
		}
		
		this.mustItems = (null == this.mustItems) ? "" : this.mustItems.replaceAll("\\s", "");
		
		cl.name = this.name;
		cl.image_filename = this.imageFilename;
		cl.is_enable = this.isEnable;
		cl.is_allow_overdue = this.isAllowOverdue;
		cl.min_audit_items = this.minAuditItems;
		cl.min_credit_score = this.minCreditScore;
		cl.suggest = this.suggest;
		cl.time = new Date();
		cl.must_items = this.mustItems;
		
		this.time = cl.time;
		
		try {
			cl.save();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		//更新信用等级的排序
		List<Object> creditList = new ArrayList<Object>();
		
        String sql = "select id from t_credit_levels order by min_credit_score desc";
		
		try {
			creditList = t_credit_levels.find(sql).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		for(int i = 0; i < creditList.size(); i++){
			Query query = JpaHelper.execute("update t_credit_levels set order_sort = ? where id = ?", i+1, creditList.get(i));
			int rows = 0;
			
			try {
				rows = query.executeUpdate();
			} catch(Exception e) {
				e.printStackTrace();
				Logger.info("更新信用等级排序时："+e.getMessage());
				error.code = -2;
				error.msg = "数据库异常，导致更新信用等级排序失败";
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			if(rows == 0) {
				JPA.setRollbackOnly();
				error.code = -1;
				error.msg = "数据更新失败";
				
				return error.code;
			}
		}
		
		error.code = 0;
		
		return 0;
	}

	/**
	 * 添加信用等级
	 * @param error
	 * @return
	 */
	public int create(ErrorInfo error) {
		error.clear();
		
		if (checkScoreUnique(error) < 0) {
			return error.code;
		}
		
		t_credit_levels cl = new t_credit_levels();
		
		if (fillDBE(cl, error) < 0) {
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.CREATE_CREDIT_LEVEL, "添加信用等级", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		//信用规则一旦改变就从新更改用户的信用等级
		/* 
		 * v.7.2.7 信用规则改变，不及时更新用户信用等级，每天凌晨统一更新
		updateAllUserCreditLevels(error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		*/
		
		_id = cl.id;
		error.code = 0;
		error.msg = "添加信用等级成功";
		
		return cl.id.intValue();
	}

	/**
	 * 修改信用等级
	 * @param error
	 * @return
	 */
	public int edit(ErrorInfo error) {
		error.clear();
		
		t_credit_levels cl = null;
		try {
			cl = t_credit_levels.findById(this.id);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			
			return error.code;
		}
		
		if (null == cl) {
			error.code = -2;
			error.msg = "出错啦";
			
			return error.code;
		}
		
		if (cl.min_credit_score != this.minCreditScore) {
			if (checkScoreUnique(error) < 0) {
				return error.code;
			}
		}
		
		if (fillDBE(cl, error) < 0) {
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.EDIT_CREDIT_LEVEL, "编辑信用等级", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		//信用规则一旦改变就从新更改用户的信用等级
		/* 
		 * v.7.2.7 信用规则改变，不及时更新用户信用等级，每天凌晨统一更新
		updateAllUserCreditLevels(error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		*/
		
		error.code = 0;
		error.msg = "编辑信用等级成功";
		
		return 0;
	}
	
	/**
	 * 检查信用积分唯一性
	 * @param error
	 * @return
	 */
	private int checkScoreUnique(ErrorInfo error) {
		error.clear();
		
		List<t_credit_levels> list = null;
		
		try {
			list = t_credit_levels.find("min_credit_score = ?", this.minCreditScore).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		if ((null != list) && (list.size() > 0)) {
			error.code = -2;
			error.msg = "已存在该信用积分的信用等级，请修改";

			return error.code;
		}
		
		error.code = 0;
		
		return 0;
	}

	/**
	 * 暂停或启用信用等级
	 * @param id
	 * @param isEnable
	 * @param error
	 * @return
	 */
	public static int enable(long id, boolean isEnable, ErrorInfo error) {
		error.clear();

		EntityManager em = JPA.em();
		Query query = em.createQuery("update t_credit_levels set is_enable = :isEnable where id = :id");
		query.setParameter("isEnable", isEnable);
		query.setParameter("id", id);
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		if (isEnable) {
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ENABLE_CREDIT_LEVEL, "启用信用等级", error);
		} else {
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DISABLE_CREDIT_LEVEL, "暂停信用等级", error);
		}
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		//信用规则一旦改变就从新更改用户的信用等级
		/* 
		 * v.7.2.7 信用规则改变，不及时更新用户信用等级，每天凌晨统一更新
		updateAllUserCreditLevels(error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		*/
		
		error.code = 0;
		
		return error.code;
	}

	/**
	 * 查询用户信用等级
	 * @param userId
	 * @param error
	 * @return
	 */
	public static CreditLevel queryUserCreditLevel(long userId, ErrorInfo error) {
		error.clear();

		User user = new User();
		user.id = userId;
		CreditLevel level = new CreditLevel();
		level.lazy = true;
		level.id = user.creditLevelId;
	
		return level;
	}
	
	/**
	 * 查询信用等级列表
	 * @param error
	 * @return
	 */
	public static List<v_credit_levels> queryCreditLevelList(ErrorInfo error) {
		error.clear();
		
		List<v_credit_levels> creditLevels = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_CREDIT_LEVELS);
		sql.append(" and  is_enable = 1");
		
		try {
			//creditLevels = v_credit_levels.findAll();
			 EntityManager em = JPA.em();
			 Query query = em.createNativeQuery(sql.toString(), v_credit_levels.class);
			 creditLevels = query.getResultList();
			    
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return null;
		}
		
		error.code = 0;

		return creditLevels;
	}
	
	/**
	 * 查询信用等级(分页、搜索)
	 * @param currPage
	 * @param pageSize
	 * @param keyword
	 * @param error
	 * @return
	 */
	public static PageBean<v_credit_levels> queryCreditLevels(int currPage, int pageSize, String keyword, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_CREDIT_LEVELS);
		
		List<Object> params = new ArrayList<Object>();
		
		if (StringUtils.isNotBlank(keyword)) {
			sql.append(" and name like ?");
			params.add("%" + keyword + "%");
		}

		int count = 0;
		List<v_credit_levels> page = null;

		try {
			sql.append("order by min_credit_score");
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_credit_levels.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            page = query.getResultList();
            
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		Map<String, Object> map = new HashMap<String, Object>();

		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}
		
		PageBean<v_credit_levels> bean = new PageBean<v_credit_levels>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.totalCount = count;
		bean.page = page;
		bean.conditions = map;
		
		error.code = 0;

		return bean;
	}

	/**
	 * 比较两个信用等级的大小
	 * @param id1
	 * @param id2
	 * @return 正数：第一个等级高、0：相等、负数：第二个等级高
	 */
	public static int compare(long id1, long id2, ErrorInfo error) {
		error.clear();
		
		t_credit_levels cl1 = null;
		t_credit_levels cl2 = null;
		
		try {
			cl1 = t_credit_levels.findById(id1);
			cl2 = t_credit_levels.findById(id2);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return error.code;
		}
		
		error.code = 0;
		
		if (cl1 == null) {
			return -1;
		}
		
		if (cl2 == null) {
			return 1;
		}
		
		return (cl1.min_credit_score - cl2.min_credit_score);
	}
	
	/**
	 * 用户是否达到了某个信用等级
	 * @param userId
	 * @param creditLevelId
	 * @param error
	 * @return 正：高， 0：相等， 负数：低
	 */
	public static int compareWith(long userId, long creditLevelId, ErrorInfo error) {
		error.clear();
		CreditLevel level = queryUserCreditLevel(userId, error);
		
		if (level == null) {
			return -1;
		}
		
		return compare(level.id, creditLevelId, error);
	}
	

	@Override
	public String toString() {
		return "CreditLevel [id=" + id + ", _id=" + _id + ", time=" + time + ", name=" + name
				+ ", imageFilename=" + imageFilename + ", isEnable=" + isEnable
				+ ", isAllowOverdue=" + isAllowOverdue + ", minCreditScore=" + minCreditScore
				+ ", minAuditItems=" + minAuditItems + ", suggest=" + suggest + ", mustItems="
				+ mustItems + "]";
	}

	/**
	 * 根据信用ID,返回信用图标
	 * @param id ID
	 * @param error 信息值
	 * @return 信用图标
	 */
	public static String queryImageFilename(long id, ErrorInfo error) {
		error.clear();
		
		String hql = "select image_filename from t_credit_levels where id=?";

		try {
			error.code = 0;
			
			return t_credit_levels.find(hql, id).first();
		} catch (Exception e) {
			Logger.error("根据ID返回图标 出现异常!" + e.getMessage());
			error.code = -1;
			error.msg = error.FRIEND_INFO + "信用图标加载失败!";
			
			return "";
		}
	}
	
	
	/**
	 * 得到所有等级ID以及对应图片
	 * @return
	 */
	public static List<CreditLevel> queryAllCreditLevels(ErrorInfo error){
		error.clear();
		
		List<t_credit_levels> tcreditLevels = null;
		List<CreditLevel> creditLevels = new ArrayList<CreditLevel>();
		
		String hql = "select new t_credit_levels(id,name,image_filename,order_sort) from t_credit_levels where is_enable = ? order by order_sort desc";
		
		try {
			tcreditLevels =  t_credit_levels.find(hql, Constants.ENABLE).fetch();
		} catch (Exception e) {
			Logger.error("信用等级->得到所有等级ID以及对应图片:" + e.getMessage());
			error.code = -1;
			error.msg = error.FRIEND_INFO + "信用图标加载失败!";
			
			return null;
		}
		
		if(null == tcreditLevels)
			return creditLevels;
		
		CreditLevel level = null;
		
		for (t_credit_levels leve : tcreditLevels) {
			level = new CreditLevel();
			
			level._id = leve.id;
			level.name = leve.name;
			level.imageFilename = leve.image_filename;
			level.order_sort = leve.order_sort;
			creditLevels.add(level);
		}
		
		error.code = 1;
		
		return creditLevels;
	}
	
	/**
	 * 获取所有信用名称/图片
	 * @param error
	 * @return
	 */
	public static List<CreditLevel> queryCreditName(ErrorInfo error) {
		error.clear();
		
		List<t_credit_levels> tcreditLevelc = null;
		List<CreditLevel> creditLevelc = new ArrayList<CreditLevel>();

		String hql = "select new t_credit_levels(c.id, c.name, c.image_filename)"
				+ " from t_credit_levels c where c.is_enable = ?";

		try {
			tcreditLevelc = t_credit_levels.find(hql, Constants.ENABLE).fetch();
		} catch (Exception e) {
			Logger.error("获取所有信用图标" + e.getMessage());
			error.code = -1;
			error.msg = "获取信用图标 有误!";
			
			return null;
		}

		CreditLevel creditLevel = null;

		for (t_credit_levels level : tcreditLevelc) {
			creditLevel = new CreditLevel();

			creditLevel._id = level.id;
			creditLevel.name = level.name;
			creditLevel.imageFilename = level.image_filename;

			creditLevelc.add(creditLevel);
		}
		
		error.code = 0;

		return creditLevelc;
	}
	
	/**
	 * 批量更新用户的信用等级
	 * @param error
	 */
	public static void updateAllUserCreditLevels(ErrorInfo error){
		error.clear();
		
		List<Long> users = t_users.find("select id from t_users").fetch();

		for (Long userId : users) {
			User.updateCreditLevel(userId, error);
		}
	}
}
