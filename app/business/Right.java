package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import utils.ErrorInfo;
import models.t_right_actions;
import models.t_right_groups;
import models.t_right_types;
import models.t_rights;

/**
 * 权限
 * @author lzp
 * @version 6.0
 * @created 2014-4-7 上午9:43:24
 */

public class Right implements Serializable{

	public long id;
	private long _id = -1;
	
	public long typeId;
	public String name;
	public String code;
	public String description;

	public void setId(long id) {
		t_rights right = null;
		
		try {
			right = t_rights.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			
			return;
		}
		
		if (null == right) {
			this._id = -1;
			
			return;
		}
		
		setInfomation(right);
	}
	
	public long getId() {
		return _id;
	}

	/**
	 * 填充数据
	 * @param right
	 */
	private void setInfomation(t_rights right){
		if (null == right) {
			this._id = -1;
			
			return;
		}
		
		this._id = right.id;
		this.typeId = right.type_id;
		this.name = right.name;
		this.code = right.code;
		this.description = right.description;
	}
	
	/**
	 * 查询RightIds通过action
	 * @param action
	 * @param error
	 * @return
	 */
	public static List<Integer> queryRightIdByAction(String action, ErrorInfo error) {
		error.clear();
		
		if (StringUtils.isBlank(action)) {
			error.code = -1;
			error.msg = "action不能为空";
			
			return null;
		}
		
		List<Integer> rightIds = null;
		
		try {
			rightIds = t_right_actions.find("select right_id from t_right_actions where action = ?", action).fetch();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		error.code = 0;
		
		return rightIds;
	}
	
	/**
	 * 查询权限地图
	 * @param error
	 * @return
	 */
	public static List<Map<String, Object>> queryRightMap(ErrorInfo error) {
		error.clear();
		
		List<Map<String, Object>> rightMapList = new ArrayList<Map<String,Object>>();
		List<t_right_types> types = Right.queryAllRightTypes(error);
		
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
	 * 查询所有的权限模块
	 * @param error
	 * @return
	 */
	public static List<t_right_types> queryAllRightTypes(ErrorInfo error) {
		error.clear();
		
		List<t_right_types> types = null;
		
		try {
			types = t_right_types.findAll();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		error.code = 0;
		
		return types;
	}
	
	/**
	 * 查询某个权限模块下的所有权限
	 * @param type
	 * @return
	 */
	public static List<t_rights> queryRightsOfType(long type, ErrorInfo error) {
		error.clear();
		
		String sql = "select r from t_rights as r where r.type_id = ?";
    	List<t_rights> list = null;
    	
    	try {
			list = t_rights.find(sql, (long)type).fetch();
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
	 * 通过action查询rightId
	 * @param action
	 * @param error
	 * @return
	 */
	public static List<Integer> querySameLevelRightIdByAction(String action, ErrorInfo error) {
		error.clear();
		
		if (StringUtils.isBlank(action)) {
			error.code = -1;
			error.msg = "action不能为空";
			
			return null;
		}
		
		List<Integer> rightIds = null;
		
		try {
			System.out.println(action);
		Long levelId= (Long)t_right_actions.find("select id from t_right_actions where action = ?", action).fetch(1).get(0);
		levelId = levelId / 1000 * 1000;
		
		rightIds =t_right_actions.find("select right_id from t_right_actions where id >= ? and id < ? group by right_id", levelId,levelId + 1000).fetch();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			return null;
		}
		error.code = 0;
		return rightIds;
	}

	/**
	 * 查询管理员所在权限组，所拥有的权限地图
	 * @param supervisorId
	 * @param groups 
	 * @param error
	 * @return
	 */
	public static List<Map<String, Object>> queryRightMapForSupervisorId(long supervisorId, List<t_right_groups> groups, ErrorInfo error) {
		error.clear();
		
		List<Map<String, Object>> rightMapList = new ArrayList<Map<String,Object>>();
		Map<Long,t_right_types> map = new HashMap<Long,t_right_types>();
		for (t_right_groups t_right_groups : groups) {
			List<t_right_types> types = RightGroup.queryRightTypes(t_right_groups.getId(), error);
			for (t_right_types t_right_types : types) {
				map.put(t_right_types.getId(), t_right_types);
			}
		}
		
		for (Long typeId : map.keySet()) {
			List<t_rights> rightList = Right.queryRightsOfTypeAndSupervisor(typeId,supervisorId, error);
			if(rightList.size() == 0)
				continue;
			Map<String, Object> rightMap = new HashMap<String, Object>();
			rightMap.put("type", map.get(typeId));
			rightMap.put("rights", rightList);
			rightMapList.add(rightMap);
		}
		
		error.code = 0;
		
		return rightMapList;
	}

	/**
	 * 按管理员ID和模块ID查询权限
	 */
	private static List<t_rights> queryRightsOfTypeAndSupervisor(Long typeId, long supervisorId, ErrorInfo error) {
		String sql ="select r from t_rights as r where id in ( select right_id from t_rights_of_group where group_id in ( select group_id from t_right_groups_of_supervisor where supervisor_id = ? ) ) and type_id = ?";
		return t_rights.find(sql, supervisorId, typeId).fetch();
	}

}
