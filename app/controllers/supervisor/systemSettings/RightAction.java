package controllers.supervisor.systemSettings;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import controllers.supervisor.SupervisorController;
import models.t_right_groups;
import models.t_right_types;
import models.t_supervisors;
import models.v_right_groups;
import models.v_supervisors;
import business.Right;
import business.RightGroup;
import business.Supervisor;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 权限
 * @author md005
 */
public class RightAction extends SupervisorController {
	/**
	 * 权限地图(权限地图首页)
	 */
	public static void rightsMap(){
		ErrorInfo error = new ErrorInfo();
		List<Map<String, Object>> rightMapList = Right.queryRightMap(error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render(rightMapList);
	}
	
	/**
	 * 权限管理
	 * @param currPage
	 * @param pageSize
	 * @param keyword
	 */
	public static void rightsManage(int currPage, int pageSize, String keyword){
		ErrorInfo error = new ErrorInfo();
		PageBean<v_right_groups> pageBean = RightGroup.queryRightGroups(currPage, pageSize, keyword, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render(pageBean);
	}
	
	/**
	 * 添加权限组初始化
	 */
	public static void addRightGroupInit() {
		ErrorInfo error = new ErrorInfo();
		List<t_right_types> rightTypes = Right.queryAllRightTypes(error); 
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render(rightTypes);
	}
	
	/**
	 * 添加权限组
	 * @param name
	 * @param description
	 * @param rightModules
	 */
	public static void addRightGroup(String name, String description, String rightModules) {
		ErrorInfo error = new ErrorInfo();
		
		RightGroup rightGroup = new RightGroup();
		rightGroup.name = name;
		rightGroup.description = description;
		rightGroup.rightModules = rightModules;
		rightGroup.create(error);

		renderJSON(error);
	}
	
	/**
	 * 编辑权限组初始化
	 * @param groupId
	 */
	public static void editRightGroupInit(long groupId) {
		ErrorInfo error = new ErrorInfo();
		t_right_groups group = RightGroup.queryRightGroupById(groupId, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		List<t_right_types> rightTypes = Right.queryAllRightTypes(error); 
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render(group, rightTypes);
	}
	
	/**
	 * 编辑权限组
	 * @param groupId
	 * @param name
	 * @param description
	 * @param rightModules
	 */
	public static void editRightGroup(long groupId, String name, String description, String rightModules) {
		ErrorInfo error = new ErrorInfo();
		
		RightGroup rightGroup = new RightGroup();
		rightGroup.id = groupId;
		rightGroup.name = name;
		rightGroup.description = description;
		rightGroup.rightModules = rightModules;
		rightGroup.edit(error);
		
		renderJSON(error);
	}
	
	/**
	 * 设置权限组权限初始化
	 * @param groupId
	 */
	public static void setGroupRightsInit(long groupId) {
		ErrorInfo error = new ErrorInfo();
		t_right_groups group = RightGroup.queryRightGroupById(groupId, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		List<Map<String, Object>> rightMapList = RightGroup.queryRightMap(groupId, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		List<Long> list = RightGroup.queryRightIds(groupId, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		String rightIds = StringUtils.join(list, ",");
		
		render(group, rightMapList, rightIds);
	}
	
	/**
	 * 设置权限组权限
	 * @param groupId
	 * @param rightIds
	 */
	public static void setGroupRights(long groupId, String rightIds) {
		ErrorInfo error = new ErrorInfo();
		RightGroup.setRights(groupId, rightIds, error);
		
		renderJSON(error);
	}
	
	/**
	 * 编辑权限组名单初始化
	 * @param groupId
	 */
	public static void setGroupRosterInit(long groupId) {
		ErrorInfo error = new ErrorInfo();
		t_right_groups group = RightGroup.queryRightGroupById(groupId, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		String groupRoster = RightGroup.querySupervisorRoster(groupId, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render(group, groupRoster);
	}
	
	/**
	 * 编辑权限组名单
	 * @param groupId
	 * @param groupRoster
	 */
	public static void setGroupRoster(long groupId, String groupRoster) {
		ErrorInfo error = new ErrorInfo();
		RightGroup.setGroupRoster(groupId, groupRoster, error);
		
		renderJSON(error);
	}
	
	/**
	 * 查询管理员通过账号
	 * @param name
	 */
	public static void querySupervisorByName(String name) {
		ErrorInfo error = new ErrorInfo();
		t_supervisors supervisor = Supervisor.querySupervisorByName(name, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		renderJSON(supervisor);
	}
	
	/**
	 * 选择管理员初始化
	 * @param currPage
	 * @param pageSize
	 * @param keyword
	 */
	public static void selectSupervisorInit(int currPage, int pageSize, String keyword) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_supervisors> pageBean = 
				Supervisor.querySupervisors(currPage, pageSize, keyword, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}

		render(pageBean);
	}
	
	/**
	 * 选择管理员名单初始化
	 * @param currPage
	 * @param pageSize
	 * @param keyword
	 */
	public static void selectGroupRosterInit(int currPage, int pageSize, String keyword) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_supervisors> pageBean = 
				Supervisor.querySupervisors(currPage, pageSize, keyword, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}

		render(pageBean);
	}
	
	/**
	 * 添加管理员到权限组
	 * @param groupId
	 * @param supervisorName
	 */
	public static void addSupervisor(long groupId, String supervisorName) {
		ErrorInfo error = new ErrorInfo();
		RightGroup.addSupervisor(groupId, supervisorName, error);

		renderJSON(error);
	}
	
	/**
	 * 删除权限组
	 * @param id
	 */
	public static void deleteRightGroup(long groupId) {
		ErrorInfo error = new ErrorInfo();
		RightGroup.delete(groupId, error);

		renderJSON(error);
	}
	
}
