package dao.ymd;

import java.math.BigDecimal;
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
import org.jsoup.helper.StringUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import business.DealDetail;
import business.NewCity;
import business.NewProvince;
import business.Supervisor;
import business.User;
import business.UserBankAccounts;
import constants.Constants;
import constants.FinanceTypeEnum;
import constants.SQLTempletes;
import constants.SupervisorEvent;
import models.t_user_bank_accounts;
import models.t_user_city;
import models.t_users;
import models.t_users_info;
import models.v_organization;
import models.core.t_interest_rate;
import models.core.t_org_project;
import models.core.t_organization;
import models.core.t_person;
import play.Logger;
import play.db.jpa.GenericModel.JPAQuery;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;
import play.db.jpa.JPABase;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import utils.PageBeanForPlayJPA;
import utils.QueryUtil;

/**
 *  医院 机构信息 
 * @author MSI
 *
 */
public class OrganizationDao {
	
	/**
	 * 获取机构列表
	 */
	public static PageBean<v_organization> getOrganizationList(String org_name, String contacts_name, 
			String contacts_phone, String bd_name,String currPageStr, String pageSizeStr, Boolean isUse, ErrorInfo error ) throws Exception{
		error.clear();
		
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;
		
		if(!StringUtil.isBlank(currPageStr) && NumberUtil.isNumericInt(currPageStr)){
			currPage=Integer.parseInt(currPageStr)>0?Integer.parseInt(currPageStr):currPage;
		}
		if(!StringUtil.isBlank(pageSizeStr) && NumberUtil.isNumericInt(pageSizeStr)){
			pageSize=Integer.parseInt(pageSizeStr)>0?Integer.parseInt(pageSizeStr):pageSize;
		}
		
		String columns = " org.id,org.user_id,ui.brand_name as org_name,contacts.name as contacts_name,"
				+ "contacts.phone as contacts_phone,bd.name as bd_name,org.is_use,ifnull(file.real_path,'') as face_photo ";
		
		String table = " from t_users u "
				+ " inner join t_organization org on u.id = org.user_id "
				+ " left join t_users_info ui on u.id = ui.user_id "
				+ " left join t_person contacts on ui.first_contacts_id = contacts.id "
				+ " left join t_person bd on org.bd_person_id = bd.id "
				+ " left join (select file_relation.relation_id,min(file.real_path) real_path "
				+ "				from t_file_relation file_relation "
				+ " 			left join t_file file on file.id = file_relation.file_id "
				+ "				where file_relation.relation_type = 8 and file.file_dict_id = 14 "
				+ "				group by file_relation.relation_id )file on file.relation_id = org.id "
				+ " where 1 = 1 ";
		
		List<Object> paramsList = new ArrayList<Object>();
		String condition="";
		
		if(!StringUtil.isBlank(org_name)){
			condition += " and ui.brand_name like ? ";
			paramsList.add("%" +org_name+"%");
		}
		if(!StringUtil.isBlank(contacts_name)){
			condition += " and contacts.name like ? ";
			paramsList.add("%" + contacts_name +"%");
		}
		if(!StringUtil.isBlank(contacts_phone) ){
			condition += " and contacts.phone like ? ";
			paramsList.add("%"+contacts_phone+"%");
		}
		if(!StringUtil.isBlank(bd_name)){
			condition += " and bd.name like ? ";
			paramsList.add(bd_name);
		}
		if(isUse!=null){
			condition += " and org.is_use= ? ";
			paramsList.add(isUse?1:0);
		}
		
		String order_by = " order by id ";

		return PageBeanForPlayJPA.getPageBeanBySQL(new v_organization(), columns, table+condition, order_by, currPage, pageSize, paramsList.toArray());
	}
	
	/**
	 * 改变医院机构状态(正常/暂停)
	 * @param aid 机构ID
	 * @param isUse 正常/暂停
	 * @param error 信息值
	 */
	public static void editStatus(long aid, boolean isUse, ErrorInfo error) {
		error.clear();

		String hql = "update t_organization set is_use=? where id=?";
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, isUse);
		query.setParameter(2, aid);
		
		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("医院机构->正常/暂停:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "设置失败!";

			return;
		}

		if(rows == 0){
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "设置失败!";
			
			return;
		}
		
		/*// 添加事件 
		if(isUse)
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ENABLE_AGENCY, "启用合作机构", error);
		else
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.NOT_ENABLE_AGENCY, "暂停合作机构", error);
		if(error.code < 0){
			JPA.setRollbackOnly();
			error.msg = "设置失败!";
			
			return;
		}*/
		
		error.code = 0;
	}
	
	/**
	 * 保存机构基础信息
	 * @param user
	 * @param userInfo
	 * @param registAddr
	 * @param businessCity
	 * @param legalPerson
	 * @param firstContacts
	 * @param secondContacts
	 * @param userBank
	 * @param bdPerson
	 * @param error
	 * @author Sxy
	 */
	public static int saveOrganization(t_users user, t_users_info userInfo, t_user_city registAddr,String businessCity,
			t_person legalPerson, t_person firstContacts, t_person secondContacts, t_user_bank_accounts userBank,
			t_person bdPerson, ErrorInfo error){
		
		 try {
			 User orgUser = new User();

			 orgUser.time = new Date();
			 orgUser.name = "hzjg" + new Date().getTime();
			 orgUser.mobile = firstContacts.phone+"hzjg";
			 orgUser.financeType=FinanceTypeEnum.BORROW.getCode();
			 orgUser.register(Constants.CLIENT_APP, error);
			 
			//机构信息t_users
			 t_users users = orgUser.instance;
			 users.reality_name = user.reality_name;
			 users.city_id = Integer.parseInt(businessCity);
			 users.address = user.address;
			 users.postcode = user.postcode;
			 users.birthday = user.birthday;
			 users.id_number = user.id_number;
			 users.is_virtual_user = true; //是否是虚拟用户
			 users.user_type = 2; // 2为企业用户
			 users.save();
			
			 //机构信息t_users_info
			 t_users_info users_info = new t_users_info();
			 users_info.user_id = users.id;
			 users_info.brand_name = userInfo.brand_name;
			 users_info.reg_capital = userInfo.reg_capital;
			 users_info.business_scope = userInfo.business_scope;
			 users_info.save();
			
			 //机构信息t_user_city
			 t_user_city user_city = new t_user_city();
			 user_city.user_id = users.id;
			 user_city.province_id = registAddr.province_id;
			 user_city.province = NewProvince.getProvince(user_city.province_id);
			 user_city.city_id = registAddr.city_id;
			 user_city.city = NewCity.getCity(user_city.city_id);
			 user_city.address = registAddr.address;
			 user_city.save();
			 
			 //法人信息t_person
			 t_person legal_person = new t_person();
			 legal_person.name = legalPerson.name;
			 legal_person.id_card = legalPerson.id_card;
			 legal_person.save();
			
			 users_info.legal_person_id = legal_person.id;
			 users_info.save();
			 
			 //联系人信息t_person first_contacts
			 t_person first_contacts = new t_person();
			 first_contacts.name = firstContacts.name;
			 first_contacts.phone = firstContacts.phone;
			 first_contacts.email = firstContacts.email;
			 first_contacts.save();
			 
			 users_info.first_contacts_id = first_contacts.id;
			 users_info.save();
			 
			 //备用联系人信息t_person second_contacts
			 t_person second_contacts = new t_person();
			 second_contacts.name = secondContacts.name;
			 second_contacts.phone = secondContacts.phone;
			 second_contacts.save();
			 
			 users_info.second_contacts_id = second_contacts.id;
			 users_info.save();
			 
			 //机构银行账户t_user_bank_accounts
			 t_user_bank_accounts user_bank = new t_user_bank_accounts();
			 user_bank.user_id = users.id;
			 user_bank.account = userBank.account;
			 user_bank.bank_name = userBank.bank_name;
			 user_bank.save();
			 
			 //机构bd人员信息t_person bd_person
			 t_person bd_person = new t_person();
			 bd_person.name = bdPerson.name;
			 bd_person.phone = bdPerson.phone;
			 bd_person.save();
			 
			 //机构信息t_organization
			 t_organization organization = new t_organization();
			 organization.user_id = users.id;
			 organization.bd_person_id = bd_person.id;
			 organization.org_type = 1;
			 organization.is_use = true;
			 organization.save();
			 
			 user.id = users.id;
			 
			 error.code = 1;
			 error.msg = "添加机构成功！";
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			JPA.setRollbackOnly();
			e.printStackTrace();
			error.code = -1;
			error.msg = "添加机构失败！";
		}
		 
		 return error.code;
		 
	}
	
	/**
	 * 获取机构基础信息
	 * @param orgId
	 * @return
	 * @author Sxy
	 */
	public static List<Map<String,Object>> getOrganizationInfo(long orgId, ErrorInfo error){
		
		error.clear();
		
		String columns = " u.id,org.id as org_id,u.reality_name,u.id_number,u.address as business_address, "
				+ " u.postcode,u.birthday,u.city_id as business_city_id,ui.brand_name,ui.reg_capital, "
				+ " uc.province_id as regist_province_id,uc.city_id as regist_city_id,uc.address as regist_address, "
				+ " lp.name as legal_person_name,lp.id_card as legal_id_card,fcp.name as first_contacts_name, "
				+ " fcp.phone as first_contacts_phone,fcp.email as first_contacts_email,scp.name  as second_contacts_name, "
				+ " scp.phone as second_contacts_phone,uba.account,uba.bank_name,bdp.name as bd_person_name,bdp.phone as bd_person_phone ";
		
		String table = " from t_users u "
				+ " left join t_users_info ui on u.id = ui.user_id "
				+ " left join t_organization org on u.id = org.user_id "
				+ " left join t_user_city uc on u.id = uc.user_id "
				+ " left join t_person lp on ui.legal_person_id = lp.id "
				+ " left join t_person fcp on ui.first_contacts_id = fcp.id "
				+ " left join t_person scp on ui.second_contacts_id = scp.id "
				+ " left join t_user_bank_accounts uba on u.id = uba.user_id "
				+ " left join t_person bdp on org.bd_person_id = bdp.id "
				+ " where 1 = 1 ";
		
		String condition = " and org.id = ? ";
		
		String order_by = " order by org.id ";
		
		String sql = "select " + columns + table + condition + order_by;
		System.out.println(sql);
		
		List<Map<String, Object>> orgInfoList = null;
		
		try {
			Query query = JPA.em().createNativeQuery(sql);
			query.setParameter(1, orgId);
			query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			orgInfoList = query.getResultList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.error("查询机构基础信息时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询结构基础信息时出现异常！";
			
			return null;
		}
		
		return orgInfoList;
		
	}
	
	/**
	 * 编辑机构基础信息
	 * @param userId
	 * @param user
	 * @param userInfo
	 * @param registAddr
	 * @param businessCity
	 * @param legalPerson
	 * @param firstContacts
	 * @param secondContacts
	 * @param userBank
	 * @param bdPerson
	 * @param error
	 * @return
	 * @author Sxy
	 */
	public static int editOrganizationInfo( t_users user, t_users_info userInfo, t_user_city registAddr,String businessCity,
			t_person legalPerson, t_person firstContacts, t_person secondContacts, t_user_bank_accounts userBank,
			t_person bdPerson, ErrorInfo error){
		
		try {
			//机构信息t_users
			t_users users = t_users.findById(user.id);
			//System.out.println(users);
			//System.out.println(user.reality_name);
			users.reality_name = user.reality_name;
			users.city_id = Integer.parseInt(businessCity);
			users.address = user.address;
			users.postcode = user.postcode;
			users.birthday = user.birthday;
			users.id_number = user.id_number;
			users.mobile = firstContacts.phone;
			users.save();
			
			//机构信息t_users_info
			t_users_info users_info = t_users_info.find(" user_id = ? ", user.id).first();
			users_info.brand_name = userInfo.brand_name;
			users_info.reg_capital = userInfo.reg_capital;
			users_info.business_scope = userInfo.business_scope;
			users_info.save();
			
			//机构信息t_user_city
			t_user_city user_city = t_user_city.find(" user_id = ? ", user.id).first();
			user_city.province_id = registAddr.province_id;
			user_city.province = NewProvince.getProvince(user_city.province_id);
			user_city.city_id = registAddr.city_id;
			user_city.city = NewCity.getCity(user_city.city_id);
			user_city.address = registAddr.address;
			user_city.save();
			
			//法人信息t_person
			t_person legal_person = t_person.findById(users_info.legal_person_id);
			legal_person.name = legalPerson.name;
			legal_person.id_card = legalPerson.id_card;
			legal_person.save();
			 
			//联系人信息t_person first_contacts
			t_person first_contacts = t_person.findById(users_info.first_contacts_id);
			first_contacts.name = firstContacts.name;
			first_contacts.phone = firstContacts.phone;
			first_contacts.email = firstContacts.email;
			first_contacts.save();
			 
			//备用联系人信息t_person second_contacts
			t_person second_contacts = t_person.findById(users_info.second_contacts_id);
			second_contacts.name = secondContacts.name;
			second_contacts.phone = secondContacts.phone;
			second_contacts.save();
			 
			//机构银行账户t_user_bank_accounts
			t_user_bank_accounts user_bank = UserBankAccounts.queryById(user.id);
			user_bank.account = userBank.account;
			user_bank.bank_name = userBank.bank_name;
			user_bank.save();
			 
			//机构bd人员信息t_person bd_person
			t_organization organization = t_organization.find(" user_id = ? ", user.id).first();
			t_person bd_person = t_person.findById(organization.bd_person_id);
			bd_person.name = bdPerson.name;
			bd_person.phone = bdPerson.phone;
			bd_person.save();
			 
			error.code = 1;
			error.msg = "修改成功！";
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			JPA.setRollbackOnly();
			e.printStackTrace();
			error.code = -1;
			error.msg = "修改机构基础信息失败！";
		}
		
		return error.code;
		
	}
	
	/**
	 * 保存机构项目
	 * @param orgId
	 * @param projects
	 * @param error
	 * @return
	 * @author Sxy
	 */
	public static int saveOrgProject(long orgId, String projects, ErrorInfo error){
		
		try {
			
			List<HashMap> projectList = JSON.parseArray(projects, HashMap.class);
			for(int i=0;i<projectList.size();i++){
				if(projectList.get(i).get("isFirstAdd").equals("1")){
					t_org_project first_org_project = new t_org_project();
					first_org_project.org_id = orgId;
					first_org_project.p_id = -1;
					first_org_project.name = (String) projectList.get(i).get("firstName");
					first_org_project.is_use = true;
					first_org_project.save();
					
					List secondProjectList = (List) projectList.get(i).get("secondName");
					List isSecondAddList = (List) projectList.get(i).get("isSecondAdd");
					for(int j=0;j<secondProjectList.size();j++){
						if(isSecondAddList.get(j).equals("1")){
							t_org_project second_org_project = new t_org_project();
							second_org_project.org_id = orgId;
							second_org_project.p_id = Integer.parseInt(String.valueOf(first_org_project.id));
							second_org_project.name = (String) secondProjectList.get(j);
							second_org_project.is_use = true;
							second_org_project.save();
						}
					}
				}
				if(projectList.get(i).get("isFirstAdd").equals("0")){
					List secondProjectList = (List) projectList.get(i).get("secondName");
					List isSecondAddList = (List) projectList.get(i).get("isSecondAdd");
					for(int j=0;j<secondProjectList.size();j++){
						if(isSecondAddList.get(j).equals("1")){
							t_org_project second_org_project = new t_org_project();
							second_org_project.org_id = orgId;
							second_org_project.p_id = Integer.parseInt((String) projectList.get(i).get("firstProjectId"));
							second_org_project.name = (String) secondProjectList.get(j);
							second_org_project.is_use = true;
							second_org_project.save();
						}
					}
				}
			}
			
			error.code = 1;
			error.msg = "保存成功！";
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			JPA.setRollbackOnly();
			e.printStackTrace();
			error.code = -1;
			error.msg = "保存机构项目失败！";
		}
		
		return error.code;
		
	}
	
	/**
	 * 删除机构项目
	 * @param firstProjectIdStr
	 * @param secondProjectIdStr
	 * @param error
	 * @return
	 * @author Sxy
	 */
	public static int deleteProject(String firstProjectIdStr, String secondProjectIdStr, ErrorInfo error){
		error.clear();
		
		String sql = "delete t_org_project where id = ?";
		
		if(firstProjectIdStr!=null){
			
			long firstProjectId = Long.parseLong(firstProjectIdStr);
			
			int rows = 0;
			
			try {
				rows = JpaHelper.execute(sql, firstProjectId).executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				Logger.info("删除机构一级类目时：" + e.getMessage());
				error.code = -1;
				error.msg = "删除机构类目失败";
				
				return error.code;
			}
			
			if(rows == 0) {
				JPA.setRollbackOnly();
				error.code = -1;
				error.msg = "数据未更新";
				
				return error.code;
			}
			
			if (error.code < 0) {
				JPA.setRollbackOnly();

				return error.code;
			}
			
			List<t_org_project> secondProjectList = t_org_project.find(" p_id = ? ", (int)firstProjectId).fetch();
			if(secondProjectList!=null){
				for(int i=0;i<secondProjectList.size();i++){
					JpaHelper.execute(sql, secondProjectList.get(i).id).executeUpdate();
				}
			}
			
			error.code = 0;
			error.msg = "删除机构一级类目成功";
			
		}
		
		if(secondProjectIdStr!=null){
			
			try {
				List secondProjectIdStrList = JSON.parseArray(secondProjectIdStr);
				//System.out.println("里面的id数组:"+ secondProjectIdStrList);
				for(int i=0;i<secondProjectIdStrList.size();i++){
					long secondProjectId = Long.parseLong((String) secondProjectIdStrList.get(i));
					JpaHelper.execute(sql, secondProjectId).executeUpdate();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Logger.info("删除机构二级类目时：" + e.getMessage());
				error.code = -1;
				error.msg = "删除机构类目失败";
				
				return error.code;
			}
			
			error.code = 0;
			error.msg = "删除机构二级类目成功";
			
		}
		
		return error.code;
		
	}
	
	/**
	 * 保存利率
	 * @return
	 * @author Sxy
	 */
	public static int saveInterestRate(long orgId, String interestRate, ErrorInfo error){
		
		List<Object> interestRateList = JSON.parseArray(interestRate);
		System.out.println(interestRateList);
		List<t_interest_rate> findInterestRateList = t_interest_rate.find(" org_id = ? ", orgId).fetch();
		
		try {
			if(findInterestRateList.size()==0){
				for(int i=0;i<interestRateList.size();i++){
					t_interest_rate interest_rate = new t_interest_rate();
					interest_rate.org_id = orgId;
					interest_rate.product_id = 4;
					interest_rate.repayment_type_id = 1; //返款方式:1.按月还款,等额本息 2.按月付息,到期还本 3.一次性还款
					interest_rate.period_unit = 0; //借款期限单位-1: 年;0:月;1:日
					interest_rate.period = i+1;
					if(!interestRateList.get(i).equals("")){
						interest_rate.interest_rate = new BigDecimal((String)interestRateList.get(i));
					}
					interest_rate.save();
				}
			}else if (findInterestRateList.size()!=0) {
				for(int i=0;i<findInterestRateList.size();i++){
					if(!interestRateList.get(i).equals("")){
						findInterestRateList.get(i).interest_rate = new BigDecimal((String)interestRateList.get(i));
						findInterestRateList.get(i).save();
					}else{
						findInterestRateList.get(i).interest_rate = null;
						findInterestRateList.get(i).save();
					}
				}
			}
			
			
			error.code = 1;
			error.msg = "保存成功！";
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			JPA.setRollbackOnly();
			e.printStackTrace();
			error.code = -1;
			error.msg = "保存机构利率失败！";
		}
		
		
		
		return error.code;
		
	}
	
}
