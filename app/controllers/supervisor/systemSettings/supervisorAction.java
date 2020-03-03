package controllers.supervisor.systemSettings;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.shove.JSONUtils;

import business.NewCity;
import business.NewProvince;
import business.Right;
import business.RightGroup;
import business.Supervisor;
import business.SupervisorCitys;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import models.t_agencies;
import models.t_dict_bid_repayment_types;
import models.t_dict_cars;
import models.t_dict_educations;
import models.t_dict_houses;
import models.t_dict_loan_purposes;
import models.t_dict_maritals;
import models.t_dict_relation;
import models.t_industries;
import models.t_new_city;
import models.t_new_province;
import models.t_right_groups;
import models.t_supervisor_citys;
import models.v_right_groups;
import models.v_supervisors;
import models.core.t_new_product;
import models.file.t_file_dict;
import models.file.t_file_relation_dict;
import models.risk.t_risk_manage_type;
import play.Logger;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;

/**
 * 管理员管理
 * @author lzp
 * @version 6.0
 * @created 2014-5-28
 */
public class supervisorAction extends SupervisorController {
	/**
	 * 管理员列表(首页)
	 * @param currPage
	 * @param pageSize
	 * @param keyword
	 */
	public static void list(int currPage, int pageSize, String keyword) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_supervisors> pageBean = 
				Supervisor.querySupervisors(currPage, pageSize, keyword, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(pageBean);
	}
	
	/**
	 * 添加管理员初始化
	 */
	public static void createSupervisorInit() {
		render();
	}
	
	/**
	 * 添加管理员
	 */
	public static void createSupervisor(int level, String groupIds, String realityName, String mobile1,  String email) {
		ErrorInfo error = new ErrorInfo();
		
		
		Supervisor supervisor = new Supervisor();
		supervisor.password = Constants.SUPERVISOR_INITIAL_PASSWORD;
		supervisor.level = level;
		supervisor.realityName = realityName;
		supervisor.mobile1 = mobile1;
		supervisor.email = email;
		supervisor.isAllowLogin = true;
		supervisor.isErased = false;
		supervisor.create(error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		if (StringUtils.isBlank(groupIds)) {
			renderJSON(error);
		}
		
		supervisor.editGroups(groupIds, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		error.msg = "添加管理员成功";
		
		renderJSON(error);
	}
	
	/**
	 * 设置管理员的权限组初始化
	 * @param currPage
	 * @param pageSize
	 */
	public static void selectGroupsOfSupervisorInit(int currPage, int pageSize) {
		ErrorInfo error = new ErrorInfo();
		
		PageBean<v_right_groups> pageBean = RightGroup.queryRightGroups(currPage, pageSize, null, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render(pageBean);
	}
	
	/**
	 * 设置管理员的权限组
	 * @param sign
	 * @param groupIds
	 */
	public static void setGroupsOfSupervisor(String sign, String groupIds) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		Supervisor supervisor = new Supervisor();
		supervisor.id = supervisorId;
		supervisor.editGroups(groupIds, error);
		
		renderJSON(error);
	}
	
	/**
	 * 编辑管理员初始化
	 */
	public static void editSupervisorInit(String sign) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		Supervisor supervisor = new Supervisor();
		supervisor.id = supervisorId;
		
		String groupIds = Supervisor.queryGroupIds(supervisorId, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render(supervisor, groupIds);
	}
	
	/**
	 * 编辑管理员
	 */
	public static void editSupervisor(String sign, int level, String realityName, int sex,
			String birthday, String mobile1, String email) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		Supervisor supervisor = new Supervisor();
		supervisor.id = supervisorId;
		supervisor.level = level;
		supervisor.realityName = realityName;
		supervisor.mobile1 = mobile1;
		supervisor.email = email;
		supervisor.edit(error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		renderJSON(error);
	}

	/**
	 * 查看详情
	 * @param id
	 */
	public static void detail(String sign) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}

		v_supervisors supervisor = Supervisor.detail(supervisorId, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render(supervisor);
	}

	/**
	 * 设置权限初始化
	 * @param sign
	 */
	public static void setRightsInit(String sign) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		List<t_right_groups> groups = Supervisor.queryGroups(supervisorId, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		List<Map<String, Object>> rightMapList = Right.queryRightMapForSupervisorId(supervisorId, groups, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		String rightIds = Supervisor.queryRightIds(supervisorId, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		String groupName = "";
		String groupDescription = "";
		
		for (t_right_groups group : groups) {
			groupName += group.name+",";
			groupDescription += group.description+",";
		}
		
		if (groups.size() > 0) {
			groupName = groupName.substring(0, groupName.length()-1);
			groupDescription = groupDescription.substring(0, groupDescription.length()-1);
		}
		
		render(groupName, groupDescription, sign, rightMapList, rightIds);
	}
	
	/**
	 * 设置权限
	 * @param sign
	 * @param rightIds
	 */
	public static void setRights(String sign, String rightIds) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		new Supervisor(supervisorId).editRights(rightIds, error);
		
		renderJSON(error);
	}
	
	/**
	 * 锁定/启用
	 * @param sign
	 * @param isAllowLogin
	 */
	public static void enable(String sign, boolean isAllowLogin) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		new Supervisor(supervisorId).enable(isAllowLogin, error);

		renderJSON(error);
	}
	
	/**
	 * 删除管理员
	 * @param sign
	 */
	public static void deleteSupervisor(String sign) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		Supervisor.delete(supervisorId, error);
		
		renderJSON(error);
	}
	
	/**
	 * 重置管理员密码
	 */
	public static void resetPassword(String sign, int currPage, int pageSize) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			flash.error(error.msg);
			
			list(currPage, pageSize, "");
		}
		
		Supervisor.resetPassword(supervisorId, error);
		flash.error(error.msg);
		
		list(currPage, pageSize, "");
	}
	
	public static void userAddrManageList(int currPage, int pageSize, String keyword){
		ErrorInfo error = new ErrorInfo();
		PageBean<v_supervisors> pageBean = Supervisor.querySupervisors(currPage, pageSize, keyword, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(pageBean);
	}
	
	public static void editCitysInit(String sign){
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		//查看管理员详细信息
		v_supervisors supervisor = Supervisor.detail(supervisorId, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		render(supervisor);
	}
	
	
	public static void getProvinceList(){
		ErrorInfo error = new ErrorInfo();
	
		List<t_new_province> province = NewProvince.getProvinceList();//省
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		 
		renderJSON(JSONUtils.toJSONString(province));
	}
	
	public static void getCityList(String province_id){
		ErrorInfo error = new ErrorInfo();
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		List<t_new_city> city = NewCity.getCityList(province_id);//市
		
		renderJSON(JSONUtils.toJSONString(city));
	}
	
	/**
	 * 查询管理员可见城市列表
	 * @param sign
	 */
	public static void supervisorCitysList(String sign, int currPage){
		ErrorInfo error = new ErrorInfo();
		//String currPage = params.get("currPage"); // 当前页
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		v_supervisors supervisor = Supervisor.detail(supervisorId, error);
		PageBean<t_supervisor_citys> pageBean = new PageBean<>();
		
		pageBean.currPage = NumberUtil.isNumericInt(String.valueOf(currPage))? currPage: 1;
		pageBean.pageSize =  Constants.FIVE; // 分页行数
		
		pageBean = SupervisorCitys.getCityList(supervisorId, error,pageBean);
		render(pageBean, supervisor, sign);
	}
	
	/**
	 * 添加管理员可见城市列表
	 * @param sign
	 * @param province_id
	 * @param city_id
	 * @param city
	 * @param province
	 */
	public static void addCitys(String sign, String province_id, String city_id,String city, String province){
		ErrorInfo error = new ErrorInfo();
		
		
		
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		
		if(StringUtils.isEmpty(city_id)){ //若城市为空，则添加该省下所有的城市列表
			List<t_new_city> all_citys  = NewCity.getCityList(province_id);
		    if(all_citys.size() != 0){
		    	for (int i = 0; i < all_citys.size(); i++) {
					t_supervisor_citys citys = new t_supervisor_citys();
					citys.province_id = province_id;
					citys.province = province;
					citys.create_time =  new Date() ;
					citys.update_time = new Date();
					citys.supervisor_id = supervisorId;
					citys.city_id = all_citys.get(i).city_id;
					citys.city = all_citys.get(i).city;
					SupervisorCitys.addSupervisorCitys(error, citys, supervisorId);
					Logger.info("error.code: " + error.code);
					if(error.code == -2){
						continue;
					}
				}
		    } else { //澳门，香港及台湾
		    	t_supervisor_citys citys = new t_supervisor_citys();
				citys.province_id = province_id;
				citys.province = province;
				citys.create_time =  new Date() ;
				citys.update_time = new Date();
				citys.supervisor_id = supervisorId;
				citys.city_id = city_id;
				citys.city = city;
				SupervisorCitys.addSupervisorCitys(error, citys, supervisorId);
		    }
			
			// 添加一个省的城市时，不需要提示已存在的地区
			if(error.code == -2){ 
				error.code = 0;
				error.msg = "添加成功";
			}
			
			renderJSON(error);
			
		} else {
			t_supervisor_citys citys = new t_supervisor_citys();
			citys.province_id = province_id;
			citys.province = province;
			citys.create_time =  new Date() ;
			citys.update_time = new Date();
			citys.supervisor_id = supervisorId;
			citys.city_id = city_id;
			citys.city = city; 
			SupervisorCitys.addSupervisorCitys(error, citys, supervisorId);
			renderJSON(error);
		}
		
	}
	
	/**
	 * 单个删除 可见列表
	 * @param sign
	 * @param city_id
	 */
	public static void deleteSupervisorCitys(String sign, String city_id){
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
	 
		SupervisorCitys.deleteSupervisorCitys(error, city_id, supervisorId);
		renderJSON(error);
	}
	
	
	/**
	 * 批量删除可见列表
	 * @param sign
	 * @param city_ids
	 */
	public static void deleteAllCitys(String sign){
		ErrorInfo error = new ErrorInfo();
		//Logger.info("批量删除管理员可见城市ids："+ city_ids);
		
		//String[] city_id = city_ids.split(",");
		
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		/*for (int i = 0; i < city_id.length; i++) {
			String id = city_id[i];
			SupervisorCitys.deleteSupervisorCitys(error, id, supervisorId);
		}*/
		SupervisorCitys.deleteAllCitys(error, supervisorId);
		renderJSON(error);
	}
	
	/**
	 * 重置enumModel
	 */
	public static void enumModelInit(){
		System.out.println("enumModelInit");
		//EnumModel
		new t_agencies().init();
		new t_dict_bid_repayment_types().init();
		new t_dict_cars().init();
		new t_dict_educations().init();
		new t_dict_houses().init();
		new t_dict_loan_purposes().init();
		new t_dict_maritals().init();
		new t_industries().init();
		new t_file_dict().init();
		new t_file_relation_dict().init();
		new t_risk_manage_type().init();
		new t_dict_relation().init();
		
		//EnumCodeModel
		new t_new_product().init();
		flash.error("操作成功");
		
		//跳转到系统设置第一页
		SecuritySettingAction.UKeyList(0,0,null);
	}
}
