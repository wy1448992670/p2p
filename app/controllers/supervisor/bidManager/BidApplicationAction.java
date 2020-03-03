package controllers.supervisor.bidManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.StringUtil;

import com.alibaba.fastjson.JSONObject;

import business.BorrowApply;
import business.CompanyUserAuthReviewBusiness;
import business.DictBanksDate;
import business.Supervisor;
import business.User;
import business.UserBankAccounts;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import controllers.supervisor.ymd.CreditManageAction;
import models.t_borrow_apply;
import models.t_new_city;
import models.t_new_province;
import models.t_user_auth_review;
import models.t_user_bank_accounts;
import models.t_user_city;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;

public class BidApplicationAction extends SupervisorController{
	
	/**
	 * 借款申请尽调列表
	 * @throws Exception 
	 */
	public static void applicationCheckList() throws Exception{
		//1:审核列表,2:尽调
		Integer view=1;
		//=======================================查询参数=======================================
		String mobile=params.get("mobile");
		String borrow_no=params.get("borrow_no");
		String loan_property_id=params.get("loan_property_id");
		String the_area=params.get("apply_area");
		String apply_status=params.get("apply_status");
		
		String order_by_columns=params.get("order_by_columns");
		String asc_desc=params.get("asc_desc");
		//=======================================order_by=======================================
		if(StringUtil.isBlank(order_by_columns)){
			order_by_columns="apply.id";
		}
		if(StringUtil.isBlank(asc_desc)){
			asc_desc="desc";
		}
		//=======================================返回值=======================================
		PageBean<Map<String, Object>> pageBean=BorrowApply.applicationList(params);
		
		//=======================================基本数据=======================================
		//用户性质,借款性质
		//UserTypeEnum.getEnumList()
		//合作机构,对应地区
		//new t_agencies().getEnumList();
		//审核状态
		//t_enum_map.getEnumCodeMapByTypeName("t_borrow_apply.status");
		
		render(pageBean,view,//返回值
				mobile,borrow_no,loan_property_id,the_area,apply_status,order_by_columns,asc_desc);//查询参数
	}
	
	/**
	 * 借款申请尽调列表
	 * @throws Exception 
	 */
	public static void applicationDueDiligenceList() throws Exception{
		//1:审核列表,2:尽调
		Integer view=2;
		//=======================================查询参数=======================================
		String mobile=params.get("mobile");
		String borrow_no=params.get("borrow_no");
		String loan_property_id=params.get("loan_property_id");
		String the_area=params.get("apply_area");
		String apply_status=params.get("apply_status");
		
		String order_by_columns=params.get("order_by_columns");
		String asc_desc=params.get("asc_desc");
		//=======================================order_by=======================================
		if(StringUtil.isBlank(order_by_columns)){
			order_by_columns="apply.id";
		}
		if(StringUtil.isBlank(asc_desc)){
			asc_desc="desc";
		}
		//=======================================返回值=======================================
		params.put("is_due_diligence", "is_due_diligence");
		PageBean<Map<String, Object>> pageBean=BorrowApply.applicationList(params);
		
		//=======================================基本数据=======================================
		//用户性质,借款性质
		//UserTypeEnum.getEnumList()
		//合作机构,对应地区
		//new t_agencies().getEnumList();
		//审核状态
		//t_enum_map.getEnumCodeMapByTypeName("t_borrow_apply.status");
		
		render(pageBean,view,//返回值
				mobile,borrow_no,loan_property_id,the_area,apply_status,order_by_columns,asc_desc);//查询参数
	}
	
	/*
	#借款产品/借款申请审核视图
	GET		/supervisor/BidApplicationAction/applicationCheckView					supervisor.bidManager.BidApplicationAction.applicationCheckView
	#借款产品/借款申请详情视图
	GET		/supervisor/BidApplicationAction/applicationDetailView					supervisor.bidManager.BidApplicationAction.applicationDetailView
	#借款产品/借款申请审核视图
	GET		/supervisor/BidApplicationAction/applicationCheck						supervisor.bidManager.BidApplicationAction.applicationCheck
	*/
	/**
	 * 借款申请审核视图
	 * @throws Exception 
	 * GET		/supervisor/BidApplicationAction/applicationCheckView					supervisor.bidManager.BidApplicationAction.applicationCheckView
	 */
	public static void applicationCheckView(Long apply_id,Integer apply_status,Integer view) throws Exception{
		//view:1:审核列表,2:尽调
		//1:详情,2:新增,3:编辑
		Integer authority=1;
		try {
			//=======================================返回值=======================================
			//1:详情,2:新增(尽调),3:编辑(审核)
			if(apply_status==2){
				authority=2;
			}else{
				authority=3;
			}
			t_borrow_apply borrowApply=BorrowApply.applicationCheckView(apply_id,apply_status);
			
			User user=new User();
			user.id=borrowApply.user_id;
			
			JSONObject authReview = new JSONObject();
			authReview.put("user_type", user.user_type);
			authReview.put("creditAmount", user.creditAmount);
			if(user.realityName == null && (user.user_type == 2 || user.user_type == 3)) {
				t_user_auth_review dbAuthReview = CompanyUserAuthReviewBusiness.findNotResetedAuthReviewRecord(borrowApply.user_id);
				if(dbAuthReview != null) {
					authReview.put("realityName", dbAuthReview.company_name);
					authReview.put("idNumber", dbAuthReview.credit_code);
					authReview.put("bankName", dbAuthReview.bank_name);
					authReview.put("bankNo", dbAuthReview.bank_no);
					authReview.put("bankAccount", dbAuthReview.real_name);
				}
			} else {
				authReview.put("realityName", user.realityName);
				authReview.put("idNumber", user.idNumber);
				t_user_bank_accounts banks = UserBankAccounts.queryById(borrowApply.user_id);
				if(banks != null) {
					authReview.put("bankName", banks.bank_name);
					authReview.put("bankNo", banks.account);
					authReview.put("bankMobile", banks.mobile);
					authReview.put("bankAccount", banks.account_name);
				}
			}
			
			t_user_bank_accounts firstBank = UserBankAccounts.querValidBankyById(borrowApply.user_id);
			List<t_user_bank_accounts> userBanks = UserBankAccounts.queryMoreById(borrowApply.user_id);
			
			t_user_city userCity = t_user_city.find("user_id = ? ", borrowApply.user_id).first();
			List<t_new_province> newProvinces =User.queryAllNewProvince();
			List<t_new_city> newCitys = userCity == null? new ArrayList<t_new_city>(): User.queryNewCity(userCity.province_id);
			Map<String, String> bankNames = DictBanksDate.bankCodeNameTable;
			
			render(borrowApply,user,authority,view,authReview,firstBank,userBanks,userCity,newProvinces,newCitys,bankNames);
			//=======================================基本数据=======================================
			//用户性质,借款性质
			//UserTypeEnum.getEnumList()
			//合作机构,对应地区
			//new t_agencies().getEnumList();
			//审核状态
			//t_enum_map.getEnumCodeMapByTypeName("t_borrow_apply.status");
		} catch (Exception e) {
			e.printStackTrace();
			flash.error(e.getMessage());
			if( view == 2){
				applicationDueDiligenceList();
			}else{
				applicationCheckList();
			}
		}
	}

	/**
	 * 借款申请详情视图
	 * @throws Exception 
	 * GET		/supervisor/BidApplicationAction/applicationDetailView					supervisor.bidManager.BidApplicationAction.applicationDetailView
	 */
	public static void applicationDetailView(Long apply_id,Integer apply_status,Integer view) throws Exception{
		try {
			//=======================================返回值=======================================
			//1:详情,2:新增,3:编辑
			Integer authority=1;
			t_borrow_apply borrowApply=BorrowApply.applicationDetailView(apply_id,apply_status);
			User user=new User();
			user.id=borrowApply.user_id;
			
			JSONObject authReview = new JSONObject();
			authReview.put("user_type", user.user_type);
			authReview.put("creditAmount", user.creditAmount);
			if(user.realityName == null && (user.user_type == 2 || user.user_type == 3)) {
				t_user_auth_review dbAuthReview = CompanyUserAuthReviewBusiness.findNotResetedAuthReviewRecord(borrowApply.user_id);
				if(dbAuthReview != null) {
					authReview.put("realityName", dbAuthReview.company_name);
					authReview.put("idNumber", dbAuthReview.credit_code);
					authReview.put("bankName", dbAuthReview.bank_name);
					authReview.put("bankNo", dbAuthReview.bank_no);
					authReview.put("bankAccount", dbAuthReview.real_name);
				}
			} else {
				authReview.put("realityName", user.realityName);
				authReview.put("idNumber", user.idNumber);
				t_user_bank_accounts banks = UserBankAccounts.queryById(borrowApply.user_id);
				if(banks != null) {
					authReview.put("bankName", banks.bank_name);
					authReview.put("bankNo", banks.account);
					authReview.put("bankMobile", banks.mobile);
					authReview.put("bankAccount", banks.account_name);
				}
			}
			
			t_user_bank_accounts firstBank = UserBankAccounts.querValidBankyById(borrowApply.user_id);
			List<t_user_bank_accounts> userBanks = UserBankAccounts.queryMoreById(borrowApply.user_id);
			
			t_user_city userCity = t_user_city.find("user_id = ? ", borrowApply.user_id).first();
			List<t_new_province> newProvinces =User.queryAllNewProvince();
			List<t_new_city> newCitys = userCity == null? new ArrayList<t_new_city>(): User.queryNewCity(userCity.province_id);
			Map<String, String> bankNames = DictBanksDate.bankCodeNameTable;
			render(borrowApply,user,authority,authReview,firstBank,userBanks,userCity,newProvinces,newCitys,bankNames);
			//=======================================基本数据=======================================
			//用户性质,借款性质
			//UserTypeEnum.getEnumList()
			//合作机构,对应地区
			//new t_agencies().getEnumList();
			//审核状态
			//t_enum_map.getEnumCodeMapByTypeName("t_borrow_apply.status");
		} catch (Exception e) {
			e.printStackTrace();
			flash.error(e.getMessage());
			if( view == 2){
				applicationDueDiligenceList();
			}else{
				applicationCheckList();
			}
		}
	}
	/**
	 * 
	 * @param supervisorSign
	 * @param borrow_apply_id
	 * @param operation  1.check 2.noPass
	 * @throws Exception
	 */
	public static void YMDApplicationCheck(String supervisorSign,String borrow_apply_id,Integer operation) throws Exception {
		String operationStr="审核";
		if(operation==2) {
			operationStr="拒绝";
		}
		/* 有效表单验证  */
		checkAuthenticity();
		ErrorInfo error = new ErrorInfo();
		if(StringUtil.isBlank(borrow_apply_id) || !NumberUtil.isNumericInt(borrow_apply_id)) {
			flash.error("没有borrow_apply_id！");
			CreditManageAction.YMDBorrowApplyList();
		}
		
		long supervisorId = Security.checkSign(supervisorSign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		if(error.code < 1){
			
			Logger.error("借款申请操作失败1," + error.msg);
			flash.error("借款申请"+operationStr+"失败," + error.msg);
			CreditManageAction.YMDBorrowApplyInfo(borrow_apply_id);
		}
		Supervisor supervisor = Supervisor.currSupervisor();
		if(supervisor==null) {
			Logger.error("借款申请操作失败,管理员没有登录");
			flash.error("借款申请"+operationStr+"失败,管理员没有登录");
			CreditManageAction.YMDBorrowApplyInfo(borrow_apply_id);
		}
		if(Supervisor.currSupervisor().id!=supervisorId) {
			Logger.error("借款申请操作失败,审核人有误");
			flash.error("借款申请"+operationStr+"失败,审核人有误");
			CreditManageAction.YMDBorrowApplyInfo(borrow_apply_id);
		}
		
		t_borrow_apply borrowApply=BorrowApply.getModelByPessimisticWrite(Long.parseLong(borrow_apply_id));
		if(borrowApply==null) {
			Logger.error("借款申请操作失败2,没有对应的额度申请单:" + borrow_apply_id);
			flash.error("借款申请"+operationStr+"失败,没有对应的额度申请单!");
			CreditManageAction.YMDBorrowApplyInfo(borrow_apply_id);
		}
		
		String reason=params.get("reason");
		borrowApply.reason=reason;
		if(operation==2) {
			if(StringUtil.isBlank(borrowApply.reason)) {
				Logger.error("借款申请操作失败3,没有拒绝理由:" + borrow_apply_id);
				flash.error("借款申请"+operationStr+"失败,没有拒绝理由");
				CreditManageAction.YMDBorrowApplyInfo(borrow_apply_id);
			}
		}
		try {
			BorrowApply.checkYMD(borrowApply,operation,params);
			flash.error("借款申请"+operationStr+"成功" );
			CreditManageAction.YMDBorrowApplyInfo(borrow_apply_id);
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error(e.getMessage() +" "+ borrowApply.id);
			flash.error(e.getMessage());
			CreditManageAction.YMDBorrowApplyInfo(borrow_apply_id);
		}
		
	}
	/**
	 * YMD借款申请审核
	 * @param operation 1.check 2.noPass 3.sendBack
	 * @throws Exception 
	 * POST		/supervisor/BidApplicationAction/applicationCheck						supervisor.bidManager.BidApplicationAction.applicationCheck
	 */
	public static void applicationCheck(t_borrow_apply borrow_apply,Integer operation,Integer view) throws Exception{
		/* 有效表单验证  */
		checkAuthenticity();
		try {
			if(borrow_apply.id==null || borrow_apply.id==0){
				throw new Exception("错误的借款申请id");
			}
			if( borrow_apply.status==0){
				throw new Exception("错误的借款申请状态");
			}
			
			String approve_amount=params.get("approve_amount");
			if(!StringUtil.isBlank(approve_amount) ){
				if(!NumberUtil.isNumericDouble(approve_amount)){
					throw new Exception("错误的审批金额");
				}else{
					borrow_apply.approve_amount=new BigDecimal(approve_amount);
				}
			}
			
			//=======================================返回值=======================================
			t_borrow_apply borrowApply=BorrowApply.applicationCheck(borrow_apply,operation,params);
			
			//=======================================返回详情=======================================
			flash.error("操作成功");
			applicationDetailView(borrowApply.id,borrowApply.status,view);
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			flash.error(e.getMessage());
			applicationCheckView(borrow_apply.id,borrow_apply.status,view);
		}
	}
}
