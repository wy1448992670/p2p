package controllers.supervisor.ymd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import business.BorrowApply;
import business.DictBanksDate;
import business.LogCore;
import business.User;
import business.UserAddressList;
import business.UserBankAccounts;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import models.t_borrow_apply;
import models.t_dict_ad_citys;
import models.t_dict_ad_provinces;
import models.t_dict_cars;
import models.t_dict_educations;
import models.t_dict_houses;
import models.t_dict_maritals;
import models.t_enum_map;
import models.t_log;
import models.t_new_city;
import models.t_new_province;
import models.t_user_address_list;
import models.t_user_bank_accounts;
import models.t_user_city;
import models.v_user_info;
import models.core.t_credit_apply;
import models.core.t_credit_increase_apply;
import models.core.t_org_project;
import models.file.t_file_relation;
import models.risk.t_risk_manage_score;
import play.cache.Cache;
import services.business.CreditApplyService;
import services.file.FileService;
import services.ymd.OrganizationService;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;

public class UserManageAction extends SupervisorController {
	
	/**
	 * 亿美贷借款人列表
	 */
	public static void borrowerInfoList() {
		String reality_name = params.get("reality_name");
		String credit_star_date = params.get("credit_star_date");
		String credit_end_date = params.get("credit_end_date");
		String mobile = params.get("mobile");
		String id_number = params.get("id_number");
		ErrorInfo error = new ErrorInfo();
		 
		PageBean<Map<String, Object>> page = null;
		try {
			page = BorrowApply.borrowerList(params);
		} catch (Exception e) {
			e.printStackTrace(); 
			error = new ErrorInfo();
			error.code = -1;
			error.msg = "亿美贷借款人列表查询失败！";
	 
			render(error);
		}
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
	 
		render(page, reality_name, mobile, id_number, credit_star_date, credit_end_date);
	}
	
	/**
	 * 亿美贷借款人详情
	 * @throws Exception 
	 */
	public static void borrowerInfo(Long borrow_apply_id) throws Exception {
		ErrorInfo error = new ErrorInfo();

	/*	long id = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if (error.code < 0) {
			flash.error(error.msg);
			
		}*/
		t_borrow_apply apply = t_borrow_apply.findById(borrow_apply_id);
		if(apply == null) {
			error.code = -1;
			error.msg = "申请借款记录为空！";
			render(error);
		}
		User user = new User();
		user.id = apply.user_id;
		user.hasBids = User.isHasBids(apply.user_id);
		int userViewType=0;
		if(user.user_type==0){
			String paramUserType=params.get("userType");
			if(NumberUtil.isNumericInt(paramUserType)){
				userViewType=Integer.parseInt(paramUserType);
			}else{
				userViewType=1;
			}
		}else{
			userViewType=user.user_type;
		}
		t_user_bank_accounts banks = UserBankAccounts.queryById(user.id);
		// 多张银行卡实现
		List<t_user_bank_accounts> banks_ = UserBankAccounts.queryMoreById(user.id);
		//double profit = Invest.getProfit(id);

		//加载用户户籍所在省市数据
		t_user_city userCity=t_user_city.find("user_id=? ", apply.user_id).first();
		List<t_new_province> newProvinces =User.queryAllNewProvince();
		List<t_new_city> newCitys = null;
		if(userCity != null){
			newCitys = User.queryNewCity(userCity.province_id);
		}else{
			newCitys = new ArrayList<t_new_city>();
		}

		//加载用户居住地所在省市数据  user.cityId
		List<t_dict_ad_citys> citys = new ArrayList<t_dict_ad_citys>();
		t_dict_ad_citys adCity = t_dict_ad_citys.findById(Long.valueOf(user.cityId));
		if(adCity != null) {
			citys = User.queryCity(adCity.province_id);
		}
		List<t_dict_ad_provinces> provinces = (List<t_dict_ad_provinces>) Cache.get("provinces");
		
		//--------------------------------------------enums-----------------------------------------------
		Map<String, String> bankCodeNameTable = DictBanksDate.bankCodeNameTable;
		List<t_dict_cars> cars = (List<t_dict_cars>) Cache.get("cars");
		List<t_dict_educations> educations = (List<t_dict_educations>) Cache.get("educations");
		List<t_dict_houses> houses = (List<t_dict_houses>) Cache.get("houses");
		List<t_dict_maritals> maritals = (List<t_dict_maritals>) Cache.get("maritals");
		
		//------------------------------*******第三方报告********--------------------
		
		
		//------------------------------*******用户图片信息********--------------------
		List<t_log> creditApplyLogs=LogCore.getLog("t_borrow_apply.status",borrow_apply_id);
	 
		//额度申请单
		t_credit_apply creditApply=null;
		if(apply.credit_apply_id!=null) {
			creditApply=t_credit_apply.findById(apply.credit_apply_id);
		}
		List<t_org_project> projectList=null;
		List<t_risk_manage_score> scoreList=null;
		List<t_file_relation> creditPictureList=null;
		List<t_file_relation> reportList=null;
		t_credit_increase_apply creditIncreaseApply=null;
		if(creditApply!=null) {
			projectList=OrganizationService.getOrgProjectByCreditApplyId(creditApply.id);
			scoreList=t_risk_manage_score.find("credit_apply_id=?", creditApply.id).fetch();
			//附件:1.users身份证照片实名认证+3.users用户风控资料 +5.increase_credit_apply-> 4.credit_apply
			creditPictureList=FileService.getFileShowByRelation(4,creditApply.id);
			
			//附件:9.credit_apply第三方报告
			reportList=FileService.getFileShowByRelation(9,creditApply.id);
			//提额申请,该额度申请最后一笔提额记录
			creditIncreaseApply=CreditApplyService.getCreditIncreaseApplyByCreditApplyId(creditApply.id);
			if(creditIncreaseApply!=null && !creditIncreaseApply.isPass()) {
				creditIncreaseApply=null;
			}
		}
		
		//-------------------------------*******预审结果***********-------------------
		PageBean<t_user_address_list> userAddressPage = UserAddressList.getUserAddressListByUserId(user.id, "1", "10",null);
		render(user,banks,banks_,userViewType
				,userCity,newProvinces,newCitys
				,adCity,provinces,citys
				,bankCodeNameTable,cars,educations,houses,maritals,creditApply,userAddressPage,reportList,creditPictureList,scoreList,projectList,creditApplyLogs);
	}
	
}
