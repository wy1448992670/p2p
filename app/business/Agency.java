package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_agencies;
import models.v_agencies;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import constants.Constants;
import constants.SupervisorEvent;

/**
 * 合作机构标
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-3-23 下午04:32:20
 */
public class Agency implements Serializable{

	public long id;
	private long _id;
	public String name;
	public Date time;
	public long creditLevel ;
	public String introduction;
	public boolean isUse;
	public String id_number;
	public String imageFilenames;
	
	/**
	 * 获取ID
	 */
	public long getId() {
		return _id;
	}

	/**
	 * 填充自己
	 */
	public void setId(long id) {
		 t_agencies tagency = null;
		 
		try {
			tagency = t_agencies.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("合作机构标->标填充自己:" + e.getMessage());
			this._id = -1;
			
			return;
		}

		if (null == tagency) {
			this._id = -1;
			
			return;
		}
		
		this._id = tagency.id;
		this.name = tagency.name;
		this.time= tagency.time;
		this.creditLevel= tagency.credit_level;
		this.introduction= tagency.introduction;
		this.isUse= tagency.is_use;
		this.id_number = tagency.id_number;
		this.imageFilenames = tagency.imageFilenames;
	}
	
	/**
	 * 合作机构标,机构列表
	 * @param error 信息值
	 * @return List<Agency>
	 */
	public static List<Agency> queryAgencys(ErrorInfo error) {
		error.clear();
		
		List<t_agencies> tagencies = null;
		List<Agency> agencys = new ArrayList<Agency>();

		String hql = "select new t_agencies(id, name) from t_agencies where is_use =? order by time desc";

		try {
			tagencies = t_agencies.find(hql, Constants.ENABLE).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("合作机构->布合作机构标,机构列表:" +  e.getMessage());
			error.msg = error.FRIEND_INFO + "加载合作机构失败!";
			
			return null;
		}

		if (null == tagencies) {
			return agencys;
		}
		
		Agency agency = null;
		
		for (t_agencies tagency : tagencies) {
			agency = new Agency();

			agency._id = tagency.id;
			agency.name = tagency.name;

			agencys.add(agency);
		}
		
		error.code = 0;
		
		return agencys;
	}
	
	/**
	 * 检查用户是否存在
	 * @param name 名称
	 * @return true : 存在,false : 不存在
	 */
	public static boolean checkName(String name){
		String hql = "select name from t_agencies where name = ?";
		
		try {
			name = t_agencies.find(hql, name.trim()).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->根据name查询name:" + e.getMessage());
			
			return true;
		}
		
		if(null == name) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 检查机构的营业执照号是否为存在
	 * @param idNumber 营业执照号
	 * @return true : 存在,false : 不存在
	 */
	public static boolean checkIdNumber(String idNumber){
	    String sql = "select id_number from t_agencies where id_number = ?";
	    
	    try {
            idNumber = t_agencies.find(sql, idNumber.trim()).first();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("资料->根据id_number查询id_number", e.getMessage());
            
            return true;
        }
	    
	    if (null == idNumber) {
            return false;
        }
	    
	    return true;
	}
	
	/**
	 * 添加合作机构
	 */
	public void createAgency(ErrorInfo error) {
		t_agencies agency = new t_agencies();
		agency.time = new Date(); // 当前时间
		agency.name = this.name;
		agency.credit_level = this.creditLevel;
		agency.introduction = this.introduction;
		agency.id_number = this.id_number;
	    agency.imageFilenames = this.imageFilenames; // 借款图片
		agency.is_use = Constants.ENABLE;  //默认为启动

		try {
			agency.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("合作机构->添加合作机构:" +  e.getMessage());
			error.msg = "添加失败!";
			this._id = -1;
			
			return;
		}

		if(agency.id < 0){
			error.msg = "添加失败!";
			
			return;
		}
		
		/* 添加事件 */
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.CREATE_AGENCY, "添加合作机构", error);
		
		if(error.code < 0){
			JPA.setRollbackOnly();
			error.msg = "添加失败!";
			
			return;
		}
		
		error.code = 0;
	}

	/**
	 * 改变合作机构状态(正常/暂停)
	 * @param aid 机构ID
	 * @param isUse 正常/暂停
	 * @param error 信息值
	 */
	public static void editStatus(long aid, boolean isUse, ErrorInfo error) {
		error.clear();

		String hql = "update t_agencies set is_use=? where id=?";
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, isUse);
		query.setParameter(2, aid);
		
		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("合作机构->正常/暂停:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "设置失败!";

			return;
		}

		if(rows == 0){
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "设置失败!";
			
			return;
		}
		
		/* 添加事件 */
		if(isUse)
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ENABLE_AGENCY, "启用合作机构", error);
		else
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.NOT_ENABLE_AGENCY, "暂停合作机构", error);
		if(error.code < 0){
			JPA.setRollbackOnly();
			error.msg = "设置失败!";
			
			return;
		}
		
		error.code = 0;
	}
	
	/**--------------------------------------------------视图查询-----------------------------------------------------------------*/
	
	/**
	 * 合作结构列表
	 * @param pageBean 分页对象
	 * @param error 信息值
	 * @return List<v_agencies>
	 */
	public static List<v_agencies> queryAgencies(PageBean<v_agencies> pageBean, ErrorInfo error, String condition, String keyword){
		error.clear();
		
		int count = -1;
		StringBuffer conditions = new StringBuffer(" where e._key = 'agencies_number'"); 
		List<Object> values = new ArrayList<Object>(); 
		Map<String, Object> conditionmap = new HashMap<String, Object>(); 
		
		/* 组装条件 */
		if (NumberUtil.isNumericInt(condition)) {
			
			switch (Integer.parseInt(condition)) {
			/* 编号搜索 */
			case Constants.AGENCY_SEARCH_ID:
				conditions.append(" AND concat(e._value,cast(t.id AS CHAR charset utf8)) LIKE ?");
				values.add("%" + keyword + "%");

				break;

			/* 名称搜索 */
			case Constants.AGENCY_SEARCH_NAME:
				conditions.append(" AND t.name LIKE ?");
				values.add("%" + keyword + "%");

				break;
			
			/* 全部搜索 */
			case Constants.SEARCH_ALL:

				if (StringUtils.isBlank(keyword))
					break;

				conditions.append(" AND (concat(e._value,cast(t.id AS CHAR charset utf8)) LIKE ? OR t.name LIKE ?)");
				values.add("%" + keyword + "%");
				values.add("%" + keyword + "%");

				break;
			}
			
			conditionmap.put("condition", condition);
			conditionmap.put("keyword", keyword);
		}
			
		pageBean.conditions = conditionmap;
		
		StringBuffer sql = new StringBuffer("select count(t.id) from t_agencies t join t_system_options e");
		sql.append(conditions);
		
		EntityManager em = JPA.em();
        Query query = em.createNativeQuery(sql.toString());

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }

        List<?> list = null;
		
		try {
			list = query.getResultList();
		} catch (Exception e) {
			Logger.error("合作机构->合作结构列表,查询总记录数:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "加载合作结构列表失败!";
		}
		
		count = list == null ? 0 : Integer.parseInt(list.get(0).toString());
		
		if(count < 1)
			return new ArrayList<v_agencies>();
			
		pageBean.totalCount = count;
		
		sql = new StringBuffer("select concat(e._value,cast(t.id AS CHAR charset utf8)) as no, t.id as id, t.name as name, t.credit_level as credit_level,t.bid_count as bid_count, t.bid_avg_apr as bid_avg_apr, t.success_bid_count as success_bid_count,t.overdue_bid_count as overdue_bid_count"
				+ ",t.bad_bid_count as bad_bid_count, t.time as time, (to_days(now())- to_days(t.time)) as cooperation_length, t.is_use as is_use from t_agencies t join t_system_options e ");
		sql.append(conditions);
		sql.append(" order by t.id desc");

		query = em.createNativeQuery(sql.toString(), v_agencies.class);

        for(int n = 1; n <= values.size(); n++){
            query.setParameter(n, values.get(n-1));
        }
	        
        query.setFirstResult((pageBean.currPage - 1) * pageBean.pageSize);
        query.setMaxResults(pageBean.pageSize);
		
		try {
			List<v_agencies> result =  query.getResultList();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标->合作结构列表:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "加载合作结构列表失败!";
			
			return null;
		}
	}
	
	public static void updateData(){
		try {
			String sql = "update t_agencies t set t.bid_count = (SELECT count(b.id) FROM t_bids b WHERE (b.agency_id = t.id)), t.bid_avg_apr = (SELECT avg(b.apr) FROM t_bids b WHERE	(b.agency_id = t.id)), t.success_bid_count = (SELECT	count(b.id)	FROM t_bids b	WHERE ((b.agency_id = t.id) AND (b.status = 4))), t.overdue_bid_count = (SELECT	count(b.id) FROM t_bids b	JOIN t_bills bl WHERE ((b.agency_id = t.id) AND (bl.bid_id = b.id) AND (bl.overdue_mark IN(-1 ,-2)))), t.bad_bid_count = (SELECT count(b.id) FROM (t_bids b	JOIN t_bills bl) WHERE ((b.agency_id = t.id) AND (bl.bid_id = b.id)	AND (bl.overdue_mark = -3)))";		
			Query query = JPA.em().createQuery(sql);
			query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			return;
		}
	}
}
