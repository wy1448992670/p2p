package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang.StringUtils;
import com.shove.Convert;
import constants.SQLTempletes;
import constants.SupervisorEvent;
import constants.Constants.SystemSupervisorGroup;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.PageBean;
import utils.QueryUtil;
import models.*;

public class RightGroup implements Serializable{

	public long id;
	private long _id = -1;

	public String name;
	public String description;

	/**
	 * 逗号分隔的权限模块id
	 */
	public String rightModules;
	private String _rightModules;

	public void setId(long id) {
		t_right_groups rg = null;

		try {
			rg = t_right_groups.findById(id);
		} catch (Exception e) {
			this._id = -1;
			Logger.error(e.getMessage());
			e.printStackTrace();

			return;
		}

		if (null == rg) {
			_id = -1;
			Logger.error("权限组不存在");

			return;
		}

		setInfomation(rg);
	}

	public long getId() {
		return _id;
	}
	
	/**
	 * 填充数据
	 * @param rg
	 */
	private void setInfomation(t_right_groups rg) {
		if (rg == null) {
			this._id = -1;
			
			return;
		}

		this._id = rg.id;
		this.name = rg.name;
		this.description = rg.description;
		this.rightModules = rg.right_modules;
	}

	/**
	 * 填充数据库实体(添加和编辑共用)
	 * @param rg
	 * @param isEditing
	 * @param error
	 * @return
	 */
	private int fillDBE(t_right_groups rg, boolean isEditing, ErrorInfo error) {
		error.clear();
		
		if (StringUtils.isBlank(this.name)) {
			error.code = -1;
			error.msg = "权限组名称不能为空";

			return error.code;
		}
		
		if (StringUtils.isBlank(this.description)) {
			error.code = -1;
			error.msg = "权限组描述不能为空";

			return error.code;
		}
		
		t_right_groups group = RightGroup.queryRightGroupByName(name, error);
		
		if (group != null) {
			if (!isEditing) {
				error.code = -1;
				error.msg = "已存在名称为"+this.name+"的权限组";

				return error.code;
			}
			
			if (group.id != this.id) {
				error.code = -1;
				error.msg = "已存在名称为"+this.name+"的权限组";

				return error.code;
			}
		}

		rg.name = this.name;
		rg.description = this.description;
		rg.right_modules = this.rightModules;

		try {
			rg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;

		return 0;
	}
	
	public String getRightModules() {
		return this._rightModules;
	}

	/**
	 * 设置权限模块
	 * @param rightModules
	 * @param error
	 * @return
	 */
	public int setRightModules(String rightModules) {
		ErrorInfo error = new ErrorInfo();
		
		if (SystemSupervisorGroup.ID == this.id) {
			error.code = -1;
			error.msg = "不能设置超级管理员组的权限";
			
			return error.code;
		}
		
		if (null == rightModules) {
			rightModules = "";
		}

		this._rightModules = rightModules.replaceAll("\\s", "");
		
		if (this.id < 0) {
			return 0;
		}
		
		String sqlUpdate = "update t_right_groups set right_modules = :right_modules where id = :id";
		Query queryUpdate = JPA.em().createQuery(sqlUpdate).setParameter("right_modules", this._rightModules).setParameter("id", this.id);

		String sqlDelete = "";
		Query queryDelete = null;
		int rows = 0;
		
		if (this._rightModules.equals("")) {
			sqlDelete = "delete from t_rights_of_group as rog where rog.group_id = :groupId";
			queryDelete = JPA.em().createQuery(sqlDelete).setParameter("groupId", this.id);
		} else {
			String[] ids = StringUtils.split(this._rightModules, ",");
			StringBuffer conditions = new StringBuffer("(");
			
			for(int i=0;i<ids.length;i++) {
				conditions.append("?,");
			}
			
			conditions.replace(conditions.length()-1, conditions.length(), ")");
			sqlDelete = "delete from t_rights_of_group as rog where rog.group_id = ? and rog.right_id in (select r.id from t_rights as r where r.type_id not in " + conditions.toString() + ")";
			queryDelete = JPA.em().createQuery(sqlDelete).setParameter(1, this.id);
			
			for(int i=0;i<ids.length;i++) {
				queryDelete.setParameter(i+2, Convert.strToLong(ids[i], -1));
			}
		}
		
		try {
			rows = queryUpdate.executeUpdate();
			queryDelete.executeUpdate();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		if (edit(error) < 0) {
			return error.code;
		}

		return 0;
	}
	
	/**
	 * 设置权限组的权限
	 * @param groupId
	 * @param RightIds
	 * @param error
	 * @return
	 */
	public static int setRights(long groupId, String rightIds, ErrorInfo error) {
		error.clear();
		
		if (SystemSupervisorGroup.ID == groupId) {
			error.code = -1;
			error.msg = "不能设置超级管理员组的权限";
			
			return error.code;
		}
		
		String sql = "delete from t_rights_of_group as rog where rog.group_id = :groupId";
		Query query = JPA.em().createQuery(sql);
		query.setParameter("groupId", groupId);

		try {
			query.executeUpdate();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		if (StringUtils.isBlank(rightIds)) {
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.GRANT_TO_RIGHT_GROUP, "给权限组分配权限", error);
			
			if (error.code < 0) {
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			error.code = 0;
			error.msg = "设置权限组权限成功";
			
			return error.code;
		}
		
		rightIds = rightIds.replaceAll("\\s", "");
		String[] arrRights = rightIds.split(",");
		
		for (String rightId : arrRights) {
			t_rights_of_group rog = new t_rights_of_group();
			rog.group_id = groupId;
			rog.right_id = Long.parseLong(rightId);

			try {
				rog.save();
			} catch (Exception e) {
				Logger.error(e.getMessage());
				e.printStackTrace();
				error.code = -1;
				error.msg = "数据库异常";
				JPA.setRollbackOnly();

				return error.code;
			}
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.GRANT_TO_RIGHT_GROUP, "给权限组分配权限", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "设置权限组权限成功";
		
		return error.code;
	}
	
	/**
	 * 设置权限组名单
	 * @param groupId
	 * @param supervisorRoster
	 * @return
	 */
	public static int setGroupRoster(long groupId, String supervisorRoster, ErrorInfo error) {
		error.clear();
		
//		/**
//		 * 系统管理员必须属于超级管理员组
//		 */
//		if (SystemSupervisorGroup.ID == groupId) {
//			if (!RegexUtils.contains(supervisorRoster, RegexUtils.getCommaSparatedRegex(SystemSupervisor.NAME))) {
//				error.code = -1;
//				error.msg = "系统管理员必须属于超级管理员组";
//				
//				return error.code;
//			}
//		}
		
		String sql = "delete from t_right_groups_of_supervisor where group_id = :groupId";
		Query query = JPA.em().createQuery(sql);
		query.setParameter("groupId", groupId);

		try {
			query.executeUpdate();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		if (StringUtils.isBlank(supervisorRoster)) {
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.EDIT_RIGTH_GROUP_ROSTER, "编辑权限组名单", error);
			
			if (error.code < 0) {
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			error.code = 0;
			error.msg = "设置权限组名单成功";
			
			return error.code;
		}
		
		supervisorRoster = supervisorRoster.replaceAll("\\s", "");
		String[] arrRoster = supervisorRoster.split(",");
		
		for (String supervisorName : arrRoster) {
			addSupervisor(groupId, supervisorName, error);
		}
		
		if (error.code < 0) {
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.EDIT_RIGTH_GROUP_ROSTER, "编辑权限组名单", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}

		error.code = 0;
		error.msg = "设置权限组名单成功";
		
		return error.code;
	}

	/**
	 * 添加权限组
	 * @param error
	 * @return
	 */
	public long create(ErrorInfo error) {
		error.clear();

		t_right_groups rg = new t_right_groups();

		if (fillDBE(rg, false, error) < 0) {
			return error.code;
		}
		
		_id = rg.id;
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.CREATE_RIGHT_GROUP, "添加权限组", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "添加权限组成功";

		return _id;
	}

	/**
	 * 编辑权限组
	 * @param error
	 * @return
	 */
	public int edit(ErrorInfo error) {
		error.clear();
		t_right_groups rg = null;

		try {
			rg = t_right_groups.findById(this.id);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		if (null == rg) {
			error.code = -1;
			error.msg = "权限组不存在";

			return error.code;
		}

		if (fillDBE(rg, true, error) < 0) {
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.EDIT_RIGHT_GROUP, "编辑权限组", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "编辑权限组成功";

		return 0;
	}
	
	/**
	 * 添加管理员到权限组
	 * @param groupId
	 * @param supervisorId
	 */
	public static int addSupervisor(long groupId, String supervisorName, ErrorInfo error) {
		error.clear();
		
		if (StringUtils.isBlank(supervisorName)) {
			error.code = -1;
			error.msg = "管理员不存在";
			
			return error.code;
		}
		
		long supervisorId = Supervisor.queryIdByName(supervisorName, error);
		
		if (error.code < 0) {
			return error.code;
		}
		
		if (supervisorId < 1) {
			error.code = -1;
			error.msg = "管理员不存在";
			
			return error.code;
		}
		
		t_right_groups_of_supervisor gos = null;
		
		try {
			gos = t_right_groups_of_supervisor.find("supervisor_id = ? and group_id = ?", supervisorId, groupId).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			
			return error.code;
		}
		
		if (null != gos) {
			error.code = 0;
			error.msg = "管理员已存在于权限组";
			
			return error.code;
		}
		
		gos = new t_right_groups_of_supervisor();
		gos.group_id = groupId;
		gos.supervisor_id = supervisorId;

		try {
			gos.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;
		error.msg = "添加管理员到权限组成功";
		
		return error.code;
	}

	/**
	 * 删除权限组
	 * @param groupId
	 * @param error
	 * @return
	 */
	public static int delete(long groupId, ErrorInfo error) {
		error.clear();
		
		if (1 == groupId) {
			error.code = -1;
			error.msg = "超级管理员组不能删除";
			
			return error.code;
		}

		t_right_groups rg = null;

		try {
			rg = t_right_groups.findById(groupId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		if (null == rg) {
			error.code = -1;
			error.msg = "权限组不存在";

			return error.code;
		}
		
		try {
			t_right_groups_of_supervisor.delete("group_id = ?", groupId);
			t_rights_of_group.delete("group_id = ?", groupId);
			rg.delete();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DELETE_RIGHT_GROUP, "删除权限组", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "删除权限组成功";

		return 0;
	}
	
	/**
	 * 分页查询权限组
	 * @param currPage
	 * @param pageSize
	 * @param keyword
	 * @param error
	 * @return
	 */
	public static PageBean<v_right_groups> queryRightGroups(int currPage, int pageSize, String keyword, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_RIGHT_GROUPS);
		
		List<Object> params = new ArrayList<Object>();
		
		if (StringUtils.isNotBlank(keyword)) {
			sql.append(" and (name like ?) ");
			params.add("%" + keyword + "%");
		}

		int count = 0;
		List<v_right_groups> page = null;

		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_right_groups.class);
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
		
		PageBean<v_right_groups> bean = new PageBean<v_right_groups>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.totalCount = count;
		bean.page = page;
		bean.conditions = map;

		error.code = 0;
		
		return bean;
	}
	
	/**
	 * 根据id查找权限组
	 * @param id
	 * @param error
	 * @return
	 */
	public static t_right_groups queryRightGroupById(long groupId, ErrorInfo error) {
		error.clear();
		t_right_groups rightGroup = null;
		
		try {
			rightGroup = t_right_groups.findById(groupId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		error.code = 0;
		error.msg = "查找权限组成功";
		
		return rightGroup;
	}
	
	/**
	 * 根据name查找权限组
	 * @param name
	 * @param error
	 * @return
	 */
	public static t_right_groups queryRightGroupByName(String name, ErrorInfo error) {
		error.clear();
		t_right_groups rightGroup = null;
		
		try {
			rightGroup = t_right_groups.find("name=?", name).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		error.code = 0;
		error.msg = "查找权限组成功";
		
		return rightGroup;
	}
	
	/**
	 * 查询所有的权限模块
	 * @param error
	 * @return
	 */
	public static List<t_right_types> queryRightTypes(long groupId, ErrorInfo error) {
		error.clear();
		
		String sql = "select * from t_right_types where find_in_set(id, (select right_modules from t_right_groups where id = :groupId))";
		Query query = null;

		try {
			query = JPA.em().createNativeQuery(sql, t_right_types.class);
			query.setParameter("groupId", groupId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		List<t_right_types> types = query.getResultList();
		error.code = 0;
		
		return types;
	}
	
	/**
	 * 查询权限地图
	 * @param error
	 * @return
	 */
	public static List<Map<String, Object>> queryRightMap(long groupId, ErrorInfo error) {
		error.clear();
		
		List<Map<String, Object>> rightMapList = new ArrayList<Map<String,Object>>();
		List<t_right_types> types = RightGroup.queryRightTypes(groupId, error);
		
		for (t_right_types type : types) {
			List<t_rights> rightList = Right.queryRightsOfType(type.id, error);
			Map<String, Object> rightMap = new HashMap<String, Object>();
			rightMap.put("type", type);
			rightMap.put("rights", rightList);
			rightMapList.add(rightMap);
		}
		
		error.code = 0;
		
		return rightMapList;
	}
	
	/**
	 * 查询权限组权限ids
	 * @param groupId
	 * @param error
	 * @return
	 */
	public static List<Long> queryRightIds(long groupId, ErrorInfo error) {
		error.clear();

		String sql = "select right_id from t_rights_of_group where group_id = ?)";
		List<Long> list = null;

		try {
			list = t_rights_of_supervisor.find(sql, groupId).fetch();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		error.code = 0;

		return list;
	}
	
	/**
	 * 查询管理员名单
	 * @param groupId
	 * @return
	 */
	public static String querySupervisorRoster(long groupId, ErrorInfo error) {
		error.clear();

		String sql = "select name from t_supervisors where id in (select supervisor_id from t_right_groups_of_supervisor where group_id = :groupId)";
		Query query = null;

		try {
			query = JPA.em().createNativeQuery(sql);
			query.setParameter("groupId", groupId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		List<String> roster = query.getResultList();
		error.code = 0;
		
		if (null == roster) {
			return null;
		}
		
		return StringUtils.join(roster, ",");
	}

}
