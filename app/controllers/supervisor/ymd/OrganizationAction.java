package controllers.supervisor.ymd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.gson.Gson;
import com.shove.gateway.weixin.gongzhong.vo.user.UserInfo;

import business.User;
import business.UserBankAccounts;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import dao.ymd.OrganizationDao;
import models.t_dict_ad_citys;
import models.t_dict_ad_provinces;
import models.t_new_city;
import models.t_new_province;
import models.t_user_bank_accounts;
import models.t_user_city;
import models.t_users;
import models.t_users_info;
import models.v_organization;
import models.core.t_interest_rate;
import models.core.t_org_project;
import models.core.t_organization;
import models.core.t_person;
import models.file.t_file_relation_dict;
import net.sf.json.JSONObject;
import play.cache.Cache;
import services.file.FileService;
import services.ymd.OrganizationService;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 医院机构Action
 * @author Sxy 2018-12-25
 *
 */
public class OrganizationAction extends SupervisorController {
	
	/**
	 * 机构列表详情
	 * @throws Exception 
	 */
	public static void organizationList() throws Exception {
		
		ErrorInfo error = new ErrorInfo();

		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数
		String org_name = params.get("org_name"); // 商户名
		String contacts_name = params.get("contacts_name"); // 联系人姓名
		String contacts_phone = params.get("contacts_phone"); // 联系人电话
		String bd_name = params.get("bd_name"); //对应BD
		String is_use = params.get("is_use"); //是否启用
		
		Boolean isUse = null;
		if("1".equals(is_use)){
			isUse = true;
		}else if("0".equals(is_use)){
			isUse = false;
		}
		
		PageBean<v_organization> page = OrganizationDao.getOrganizationList(org_name, contacts_name, contacts_phone,
				bd_name, currPage, pageSize, isUse, error);

		render(page,org_name,contacts_name,contacts_phone);
	}
	
	/**
	 * 启用合作机构
	 * @throws Exception 
	 */
	public static void enableOrganization(long aid) throws Exception{
		ErrorInfo error = new ErrorInfo();
	    OrganizationDao.editStatus(aid, Constants.ENABLE, error);
		flash.error(error.msg);
	    
		organizationList();
	}
	
	/**
	 * 暂停合作机构
	 * @throws Exception 
	 */
	public static void notEnableOrganization(long aid) throws Exception{
		ErrorInfo error = new ErrorInfo();
		OrganizationDao.editStatus(aid, Constants.NOT_ENABLE, error);
		flash.error(error.msg);
	    
		organizationList();
	}
	
	/**
	 * 机构列表添加页面
	 *//*
	public static void addOrganization(Long orgId) {
		
		//注册地
		List<t_new_province> newProvinces = User.queryAllNewProvince();
		//暂住地
		List<t_dict_ad_provinces> provinces = (List<t_dict_ad_provinces>) Cache.get("provinces");
		
		
		//7.users.user_type in (2,3)企业用户资料
		//FileService.getFileDictByRelationType(7);
		List<t_file_relation_dict> userFileList=new ArrayList<t_file_relation_dict>();
		// 8.orgnization合作机构资料 
		List<t_file_relation_dict> orgnizationFileList=FileService.getFileDictByRelationType(8);
		
		
		ErrorInfo error = new ErrorInfo();
		t_organization organization=null;
		t_users_info userInfo=null;
		t_users user=null;
		//注册地地址
		t_user_city registAddr=null;
		//注册地兄弟市
		List<t_new_city> newCitys = new ArrayList<t_new_city>();
		
		//暂住地市
		t_dict_ad_citys businessAddr =null;
		//暂住地兄弟市
		List<t_dict_ad_citys> citys =new ArrayList<t_dict_ad_citys>();
		
		//法人,联系人,备用 联系人
		t_person legalPerson = null;
		t_person firstContacts =  null;
		t_person secondContacts =  null;
		t_person bdPerson =  null;
		
		//银行账户
		t_user_bank_accounts userBank =null;
		
		//机构项目
		List<t_org_project> orgProjectList=new ArrayList<t_org_project>();
		if(orgId!=null) {
			organization= t_organization.findById(orgId);
			user = t_users.findById(organization.user_id);
			userInfo = t_users_info.find(" user_id = ? ", user.id).first();
			
			//注册地地址
			registAddr = t_user_city.find(" user_id = ? ", user.id).first();
			//注册地兄弟市
			if(registAddr != null){
				newCitys = User.queryNewCity(registAddr.province_id);
			}
			
			//暂住地市
			businessAddr = t_dict_ad_citys.findById(Long.valueOf(user.city_id));
			//暂住地兄弟市
			if(businessAddr != null) {
				citys = User.queryCity(businessAddr.province_id);
			}
			
			//法人,联系人,备用 联系人
			legalPerson = t_person.findById(userInfo.legal_person_id);
			firstContacts = t_person.findById(userInfo.first_contacts_id);
			secondContacts = t_person.findById(userInfo.second_contacts_id);
			bdPerson = t_person.findById(organization.bd_person_id);
			
			//银行账户
			userBank = UserBankAccounts.queryById(user.id);
			
			//机构项目
			orgProjectList = OrganizationService.getOrgProject(orgId);
		}

		
		
		
		//render(user,userInfo,registAddr,newProvinces,newCitys,businessAddr,provinces,citys,legalPerson,
		//		firstContacts,secondContacts,bdPerson,userBank,orgProjectList);
		
		
		render(user,userInfo,organization,userBank,
				userFileList,orgnizationFileList,orgProjectList,
				newProvinces,newCitys,registAddr,//注册地
				provinces,citys,businessAddr,//暂住地
				legalPerson,firstContacts,secondContacts,bdPerson
				);
		
		
	}*/
	
	/**
	 * 添加机构基础信息
	 * @param user
	 * @param userInfo
	 * @param registAddr
	 * @param legalPerson
	 * @param firstContacts
	 * @param secondContacts
	 * @param userBank
	 * @param bdPerson
	 * @throws Exception
	 * @author Sxy
	 */
	public static void createOrganization(t_users user, t_users_info userInfo, t_user_city registAddr, t_person legalPerson,
			t_person firstContacts, t_person secondContacts, t_user_bank_accounts userBank, t_person bdPerson) throws Exception {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		String businessCity = params.get("businessCity");
		
		if(StringUtils.isBlank(user.reality_name)){
			json.put("msg", "机构注册名不能为空");
			renderJSON(json);
			organizationDetail(null);
		}
		if(StringUtils.isBlank(userInfo.brand_name)){
			json.put("msg", "机构商家名不能为空");
			renderJSON(json);
			organizationDetail(null);
		}
		if(StringUtils.isBlank(registAddr.province_id)||StringUtils.isBlank(registAddr.city_id)){
			json.put("msg", "机构注册地址不能为空");
			renderJSON(json);
			organizationDetail(null);
		}
		if(StringUtils.isBlank(user.postcode)){
			json.put("msg", "邮政编码");
			renderJSON(json);
			organizationDetail(null);
		}
		if(user.birthday == null){
			json.put("msg", "机构经营起始日不能为空");
			renderJSON(json);
			organizationDetail(null);
		}
		if(StringUtils.isBlank(userInfo.business_scope)){
			json.put("msg", "机构经营范围不能为空");
			renderJSON(json);
			organizationDetail(null);
		}
		if(StringUtils.isBlank(user.id_number)){
			json.put("msg", "统一社会信用代码不能为空");
			renderJSON(json);
			organizationDetail(null);
		}
		if(StringUtils.isBlank(userBank.account)){
			json.put("msg", "结算账号不能为空");
			renderJSON(json);
			organizationDetail(null);
		}
		if(StringUtils.isBlank(userBank.bank_name)){
			json.put("msg", "开户银行不能为空");
			renderJSON(json);
			organizationDetail(null);
		}
		if(user.id==null) {
			OrganizationDao.saveOrganization(user,userInfo,registAddr,businessCity,
					legalPerson,firstContacts,secondContacts,userBank,bdPerson,error);
		}else {
			OrganizationDao.editOrganizationInfo(user,userInfo,registAddr,businessCity,
					legalPerson,firstContacts,secondContacts,userBank,bdPerson,error);
		}
		
		
		t_organization organization = t_organization.find(" user_id = ?", user.id).first();
		
		json.put("msg", "添加成功");
		json.put("userId", user.id);
		json.put("orgId", organization.id);
		
		renderJSON(json);
		
	}
	
	/**
	 * 机构列表详情页面
	 * 
	 * @author Sxy
	 */
	public static void organizationDetail(Long orgId) {

		// 注册地
		List<t_new_province> newProvinces = User.queryAllNewProvince();
		// 暂住地
		List<t_dict_ad_provinces> provinces = (List<t_dict_ad_provinces>) Cache.get("provinces");

		// 7.users.user_type in (2,3)企业用户资料
		// FileService.getFileDictByRelationType(7);
		List<t_file_relation_dict> userFileRelationDictList = new ArrayList<t_file_relation_dict>();
		// 8.orgnization合作机构资料
		List<t_file_relation_dict> orgnizationFileRelationDictList = FileService.getFileDictByRelationType(8);

		ErrorInfo error = new ErrorInfo();
		t_organization organization = null;
		t_users_info userInfo = null;
		t_users user = null;
		// 注册地地址
		t_user_city registAddr = null;
		// 注册地兄弟市
		List<t_new_city> newCitys = new ArrayList<t_new_city>();

		// 暂住地市
		t_dict_ad_citys businessAddr = null;
		// 暂住地兄弟市
		List<t_dict_ad_citys> citys = new ArrayList<t_dict_ad_citys>();

		// 法人,联系人,备用 联系人
		t_person legalPerson = null;
		t_person firstContacts = null;
		t_person secondContacts = null;
		t_person bdPerson = null;

		// 银行账户
		t_user_bank_accounts userBank = null;
		
		//商家利率
		List<t_interest_rate> interestRateList = new ArrayList<t_interest_rate>();

		// 机构项目
		List<t_org_project> orgProjectList = new ArrayList<t_org_project>();
		if (orgId != null) {
			organization = t_organization.findById(orgId);
			user = t_users.findById(organization.user_id);
			userInfo = t_users_info.find(" user_id = ? ", user.id).first();

			// 注册地地址
			registAddr = t_user_city.find(" user_id = ? ", user.id).first();
			// 注册地兄弟市
			if (registAddr != null) {
				newCitys = User.queryNewCity(registAddr.province_id);
			}

			// 暂住地市
			businessAddr = t_dict_ad_citys.findById(Long.valueOf(user.city_id));
			// 暂住地兄弟市
			if (businessAddr != null) {
				citys = User.queryCity(businessAddr.province_id);
			}

			// 法人,联系人,备用 联系人
			legalPerson = t_person.findById(userInfo.legal_person_id);
			firstContacts = t_person.findById(userInfo.first_contacts_id);
			secondContacts = t_person.findById(userInfo.second_contacts_id);
			bdPerson = t_person.findById(organization.bd_person_id);

			// 银行账户
			userBank = UserBankAccounts.queryById(user.id);
			
			//商家利率
			interestRateList = t_interest_rate.find(" org_id = ? ", orgId).fetch();

			// 机构项目
			orgProjectList = OrganizationService.getOrgProject(orgId);
			// 7.users.user_type in (2,3)企业用户资料
			//userFileRelationDictList =  FileService.getFileUpdateByRelation(7,user.id);
			// 8.orgnization合作机构资料
			orgnizationFileRelationDictList = FileService.getFileUpdateByRelation(8,organization.id);
		}
		//System.out.println(new Gson().toJson(orgnizationFileRelationDictList));
		render(user,userInfo,organization,userBank,
				userFileRelationDictList,orgnizationFileRelationDictList,orgProjectList,interestRateList,
				newProvinces,newCitys,registAddr,//注册地
				provinces,citys,businessAddr,//暂住地
				legalPerson,firstContacts,secondContacts,bdPerson
				);
	}
	
	/**
	 * 编辑机构信息
	 * 
	 * @author Sxy
	 * @throws Exception 
	 */
	public static void editOrganizationInfo(t_users user, t_users_info userInfo, t_user_city registAddr, t_person legalPerson,
			t_person firstContacts, t_person secondContacts, t_user_bank_accounts userBank, t_person bdPerson) throws Exception{
		
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		String businessCity = params.get("businessCity");
		
		if(StringUtils.isBlank(user.reality_name)){
			json.put("msg", "机构注册名不能为空");
			renderJSON(json);
			organizationDetail(null);
		}
		if(StringUtils.isBlank(userInfo.brand_name)){
			json.put("msg", "机构商家名不能为空");
			renderJSON(json);
			organizationDetail(null);
		}
		if(StringUtils.isBlank(registAddr.province_id)||StringUtils.isBlank(registAddr.city_id)){
			json.put("msg", "机构注册地址不能为空");
			renderJSON(json);
			organizationDetail(null);
		}
		if(StringUtils.isBlank(user.postcode)){
			json.put("msg", "邮政编码");
			renderJSON(json);
			organizationDetail(null);
		}
		if(user.birthday == null){
			json.put("msg", "机构经营起始日不能为空");
			renderJSON(json);
			organizationDetail(null);
		}
		if(StringUtils.isBlank(userInfo.business_scope)){
			json.put("msg", "机构经营范围不能为空");
			renderJSON(json);
			organizationDetail(null);
		}
		if(StringUtils.isBlank(user.id_number)){
			json.put("msg", "统一社会信用代码不能为空");
			renderJSON(json);
			organizationDetail(null);
		}
		if(StringUtils.isBlank(userBank.account)){
			json.put("msg", "结算账号不能为空");
			renderJSON(json);
			organizationDetail(null);
		}
		if(StringUtils.isBlank(userBank.bank_name)){
			json.put("msg", "开户银行不能为空");
			renderJSON(json);
			organizationDetail(null);
		}
		if(user.id==null || user.id==0L) {
			OrganizationDao.saveOrganization(user,userInfo,registAddr,businessCity,
					legalPerson,firstContacts,secondContacts,userBank,bdPerson,error);
		}else {
			OrganizationDao.editOrganizationInfo(user,userInfo,registAddr,businessCity,
					legalPerson,firstContacts,secondContacts,userBank,bdPerson,error);
		}
		t_organization organization = t_organization.find(" user_id = ?", user.id).first();
		
		json.put("msg", "添加成功");
		json.put("userId", user.id);
		json.put("orgId", organization.id);
		
		renderJSON(json);
		
	}
	
	/**
	 * 添加机构项目
	 * 
	 * @author Sxy
	 * @throws Exception 
	 */
	public static void createOrgProject() throws Exception{
		
		ErrorInfo error = new ErrorInfo();
		
		long userId = Long.parseLong(params.get("userId"));
		//System.out.println("userId "+userId);
		t_organization organization = t_organization.find(" user_id = ? ", userId).first();
		//System.out.println("orgId "+organization.id);
		
		String projects = params.get("projects");
		//System.out.println("projects "+projects);
		
		OrganizationDao.saveOrgProject(organization.id,projects,error);
		
		organizationList();
		
	}
	
	/**
	 * 删除机构项目
	 * 
	 * @author Sxy
	 */
	public static void deleteProject(){
		
		ErrorInfo error = new ErrorInfo();
		
		String firstProjectIdStr = params.get("firstProjectId");
		String secondProjectIdStr = params.get("secondProjectId");
		
		OrganizationDao.deleteProject(firstProjectIdStr,secondProjectIdStr,error);
		
	}
	
	/**
	 * 添加商家利率
	 * 
	 * @author Sxy
	 */
	public static void createInterestRate(){
		
		ErrorInfo error = new ErrorInfo();
		
		long orgId = Long.parseLong(params.get("orgId"));
		String interestRate = params.get("interestRate");
		System.out.println(orgId);
		
		OrganizationDao.saveInterestRate(orgId, interestRate, error);
		
	}
	
}
