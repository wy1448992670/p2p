package controllers.supervisor.ymd;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.StringUtil;

import com.shove.Convert;

import business.BorrowApply;
import business.LogCore;
import business.ReportUserInfo;
import business.Supervisor;
import business.User;
import business.UserAddressList;
import constants.Constants;
import constants.RiskReportIsValidStatusEnum;
import constants.RiskReportStatusEnum;
import controllers.supervisor.SupervisorController;
import models.t_borrow_apply;
import models.t_dict_ad_citys;
import models.t_enum_map;
import models.t_log;
import models.t_report_contact_detail;
import models.t_user_address_list;
import models.t_users;
import models.t_users_info;
import models.core.t_credit_apply;
import models.core.t_credit_increase_apply;
import models.core.t_org_project;
import models.file.t_file_relation;
import models.risk.t_risk_manage_score;
import models.risk.t_risk_report;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import play.Logger;
import play.db.jpa.JPA;
import services.RiskReportService;
import services.business.CreditApplyService;
import services.file.FileService;
import services.ymd.OrganizationService;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;

public class CreditManageAction extends SupervisorController {
	
	/**
	 * 额度申请 
	 * @throws Exception 
	 */
	public static void creditApplyInfo(String credit_apply_id) throws Exception {
		/*
		if(params==null) {
			params=request.params;
		}*/
		if(StringUtil.isBlank(credit_apply_id) || !NumberUtil.isNumericInt(credit_apply_id)) {
			flash.error("没有credit_apply_id！");
			creditApplyList();
		}
		t_credit_apply creditApply=t_credit_apply.findById(Long.parseLong(credit_apply_id));
		if(creditApply==null) {
			flash.error("没有creditApply！");
			creditApplyList();
		}
		User userBusiness=new User();
		userBusiness.id=creditApply.user_id;
		t_users user=userBusiness.instance;
		t_users_info userInfo=userBusiness.getUserInfo();
		
		//运营商报告
		t_risk_report t_risk_report = RiskReportService.getRiskReport(user.id, RiskReportStatusEnum.SUCCESS.getCode(),
						RiskReportIsValidStatusEnum.VALID.getCode());
		
		User consociationUserBusiness=new User();
		consociationUserBusiness.id=creditApply.consociation_user_id;
		t_users consociationUser=consociationUserBusiness.instance;
		t_users_info consociationUserInfo=consociationUserBusiness.getUserInfo();
		//t_organization organization=t_organization.find("user_id", consociationUser.id).first();
		
		List<t_org_project> projectList=OrganizationService.getOrgProjectByCreditApplyId(creditApply.id);
		List<t_risk_manage_score> scoreList=t_risk_manage_score.find("credit_apply_id=?", creditApply.id).fetch();
		
		//1.users身份证照片实名认证+3.users用户风控资料 -> 4.credit_apply
		List<t_file_relation> creditPictureList=FileService.getFileShowByRelation(4,creditApply.id);
		//List<t_file_relation> creditPictureList=FileService.getFileShowByRelation(3,user.id);
		//9.credit_apply第三方报告
		List<t_file_relation> reportList=FileService.getFileShowByRelation(9,creditApply.id);
		PageBean<t_user_address_list> userAddressPage = UserAddressList.getUserAddressListByUserId(user.id, "1", "10",null);
		List<t_log> creditApplyLogs=LogCore.getLog("t_credit_apply.status",creditApply.id);
		
		//加载用户居住地所在省市数据  user.cityId
		t_dict_ad_citys adCity = t_dict_ad_citys.findById(Long.valueOf(user.city_id));
		
		render(creditApply,user,userInfo,consociationUser,consociationUserInfo,
				t_risk_report,projectList,scoreList,creditPictureList,reportList,userAddressPage,creditApplyLogs,adCity);
	}
	/**
	 *提额申请
	 * @throws Exception 
	 */
	public static void increaseCreditApplyInfo(String increaseCreditId) throws Exception {
	 
		if(StringUtil.isBlank(increaseCreditId) || !NumberUtil.isNumericInt(increaseCreditId)) {
			flash.error("提额申请ID不能为空！");
			increaseCreditApplyList();
		}
		t_credit_increase_apply creditIncreaseApply = t_credit_increase_apply.findById(Long.parseLong(increaseCreditId));
		if(creditIncreaseApply==null) {
			flash.error("该笔提额申请记录不存在！ ");
			increaseCreditApplyList();
		}
		System.err.println(creditIncreaseApply.canAudit()+"=====================" +creditIncreaseApply.status);
		
		//额度申请记录
		t_credit_apply creditApply = t_credit_apply.findById(creditIncreaseApply.credit_apply_id);
		
		User userBusiness=new User();
		userBusiness.id = creditApply.user_id;
		t_users user = userBusiness.instance;
		t_users_info userInfo=userBusiness.getUserInfo();
		
		//运营商报告
		t_risk_report t_risk_report = RiskReportService.getRiskReport(user.id, RiskReportStatusEnum.SUCCESS.getCode(),
				RiskReportIsValidStatusEnum.VALID.getCode());
		
		User consociationUserBusiness = new User();
		consociationUserBusiness.id = creditApply.consociation_user_id;
		t_users consociationUser = consociationUserBusiness.instance;
		t_users_info consociationUserInfo=consociationUserBusiness.getUserInfo();
		//t_organization organization=t_organization.find("user_id", consociationUser.id).first();
		
		List<t_org_project> projectList=OrganizationService.getOrgProjectByCreditApplyId(creditApply.id);
		List<t_risk_manage_score> scoreList=t_risk_manage_score.find("credit_apply_id=?", creditApply.id).fetch();
		
		//1.users身份证照片实名认证+3.users用户风控资料 -> 4.credit_apply
		List<t_file_relation> creditPictureList=FileService.getFileShowByRelation(4,creditApply.id);
		//5.increase_credit_apply 补充资料
		List<t_file_relation> increaseCreditPictureList=FileService.getFileShowByRelation(5,creditIncreaseApply.id);
		//List<t_file_relation> creditPictureList=FileService.getFileShowByRelation(3,user.id);
		//9.credit_apply第三方报告
		List<t_file_relation> reportList=FileService.getFileShowByRelation(9,creditApply.id);
		PageBean<t_user_address_list> userAddressPage = UserAddressList.getUserAddressListByUserId(user.id, "1", "10",null);
		List<t_log> creditApplyLogs=LogCore.getLog("t_credit_increase_apply.status",creditIncreaseApply.id);
		
		//加载用户居住地所在省市数据  user.cityId
		t_dict_ad_citys adCity = t_dict_ad_citys.findById(Long.valueOf(user.city_id));
		
		render(creditIncreaseApply,creditApply,user,userInfo,consociationUser,consociationUserInfo,
				t_risk_report,projectList,scoreList,creditPictureList,increaseCreditPictureList,reportList,userAddressPage,creditApplyLogs,adCity);
 
	}
	/**
	 *借款申请审核
	 */
	public static void YMDBorrowApplyInfo(String borrow_apply_id) throws Exception{
		if(StringUtil.isBlank(borrow_apply_id) || !NumberUtil.isNumericInt(borrow_apply_id)) {
			flash.error("没有borrow_apply_id！");
			YMDBorrowApplyList();
		}
		t_borrow_apply borrowApply=t_borrow_apply.findById(Long.parseLong(borrow_apply_id));
		if(borrowApply==null) {
			flash.error("没有borrowApply！");
			YMDBorrowApplyList();
		}
		
		//借款人
		User userBusiness=new User();
		userBusiness.id=borrowApply.user_id;
		t_users user=userBusiness.instance;
		t_users_info userInfo=userBusiness.getUserInfo();
		
		//运营商报告
		t_risk_report t_risk_report = RiskReportService.getRiskReport(user.id, RiskReportStatusEnum.SUCCESS.getCode(),
						RiskReportIsValidStatusEnum.VALID.getCode());
				
		//协议代付收款人/合作机构
		User consociationUserBusiness=new User();
		consociationUserBusiness.id=borrowApply.consociation_user_id;
		t_users consociationUser=consociationUserBusiness.instance;
		t_users_info consociationUserInfo=consociationUserBusiness.getUserInfo();
		//t_organization organization=t_organization.find("user_id", consociationUser.id).first();
		
		//附件:6.borrow_apply
		List<t_file_relation> borrowPictureList=FileService.getFileShowByRelation(6,borrowApply.id);
		//List<t_file_relation> creditPictureList=FileService.getFileShowByRelation(3,user.id);
		
		//通讯录
		PageBean<t_user_address_list> userAddressPage = UserAddressList.getUserAddressListByUserId(user.id, "1", "10",null);
		List<t_log> creditApplyLogs=LogCore.getLog("t_borrow_apply.status",borrowApply.id);
		
		//加载用户居住地所在省市数据  user.cityId
		t_dict_ad_citys adCity = t_dict_ad_citys.findById(Long.valueOf(user.city_id));
		
		//额度申请单
		t_credit_apply creditApply=null;
		if(borrowApply.credit_apply_id!=null) {
			creditApply=t_credit_apply.findById(borrowApply.credit_apply_id);
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
		// 已使用额度
		BigDecimal useCredit=CreditApplyService.getUseCreditByCreditApplyId(creditApply.id);
		render(useCredit,borrowApply,creditApply,creditIncreaseApply,user,userInfo,consociationUser,consociationUserInfo,
				t_risk_report,projectList,scoreList,borrowPictureList,creditPictureList,reportList,userAddressPage,creditApplyLogs,adCity);
	}
	
	/**
	 * 额度申请列表
	 */
	public static void creditApplyList() {
		String apply_status=params.get("apply_status");
		String reality_name=params.get("reality_name");
		String user_idnumber=params.get("user_idnumber");
		String user_mobile=params.get("user_mobile");
		String apply_star_date=params.get("apply_star_date");
		String apply_end_date=params.get("apply_end_date");
		
		int isExport = Convert.strToInt(params.get("isExport"), 0);
		
		PageBean<Map<String, Object>> creditList=null;
		try {
			creditList=CreditApplyService.queryCreditList(params);
		} catch (Exception e) {
			e.printStackTrace();
			
			ErrorInfo error = new ErrorInfo();
			error.code = -1;
			error.msg = "额度申请列表查询失败！";
			// flash.error(error.msg); 
			// renderJSON(error);
		}
		
		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			List<Map<String, Object>> list = creditList.page;
			
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(java.sql.Timestamp.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject apply = (JSONObject)obj;
				
				apply.put("product_name", apply.get("product_name").equals(null)?"":apply.get("product_name"));
				apply.put("id_number", apply.get("id_number").equals(null)?"":apply.get("id_number"));
				apply.put("reality_name", apply.get("reality_name").equals(null)?"":apply.get("reality_name"));
				apply.put("mobile", apply.get("mobile").equals(null)?"":apply.get("mobile"));
				apply.put("create_time", apply.get("create_time").equals(null)?"":apply.get("create_time"));
				apply.put("apply_time", apply.get("apply_time").equals(null)?"":apply.get("apply_time"));
				apply.put("update_time", apply.get("update_time").equals(null)?"":apply.get("update_time"));
				apply.put("score_value", apply.get("score_value").equals(null)?"":apply.get("score_value"));
				apply.put("apply_credit_amount", apply.get("audit_credit_amount").equals(null)?"":apply.get("audit_credit_amount"));
				apply.put("audit_credit_amount", apply.get("audit_credit_amount").equals(null)?"":apply.get("audit_credit_amount"));
				apply.put("enum_name", apply.get("enum_name").equals(null)?"":apply.get("enum_name"));
			}
			
			//用户名、真实姓名、手机号
			
			File file = ExcelUtils.export("额度申请列表", arrList,
					new String[] {"对应产品", "身份证号", "客户姓名", "手机号", "注册时间","申请时间","处理时间","同盾分数","申请额度","审批额度 ","状态"}, 
					new String[] {"product_name", "id_number", "reality_name", "mobile","create_time", "apply_time","update_time","score_value","apply_credit_amount","audit_credit_amount","enum_name"});
			
			renderBinary(file, "额度申请列表" + ".xls");
		}	
			
		
		
		
		List<t_enum_map> statusList=t_enum_map.getEnumListByTypeName("t_credit_apply.status");
		for(int i=0;i<statusList.size();i++){
		    if(statusList.get(i).is_deleted) {
		    	statusList.remove(i);
		    	i--;
		    }
		}
		System.out.println(statusList);
		render(creditList,statusList,
				apply_status,reality_name,user_idnumber,user_mobile,apply_star_date,apply_end_date);
	}
	/**
	 *提额申请列表
	 */
	public static void increaseCreditApplyList() {
		String apply_status=params.get("apply_status");
		String reality_name=params.get("reality_name");
		String user_idnumber=params.get("user_idnumber");
		String user_mobile=params.get("user_mobile");
		String apply_star_date=params.get("apply_star_date");
		String apply_end_date=params.get("apply_end_date");
		PageBean<Map<String, Object>> increaseCreditList=null;
		try {
			increaseCreditList=CreditApplyService.queryIncreaseCreditList(params);
		} catch (Exception e) {
			e.printStackTrace();
			
			ErrorInfo error = new ErrorInfo();
			error.code = -1;
			error.msg = "提额申请列表查询失败！";
			flash.error(error.msg); 
			renderJSON(error);
		}
		
		List<t_enum_map> statusList=t_enum_map.getEnumListByTypeName("t_credit_increase_apply.status");
		for(int i=0;i<statusList.size();i++){
		    if(statusList.get(i).is_deleted) {
		    	statusList.remove(i);
		    	i--;
		    }
		}
		render(increaseCreditList,statusList,
				apply_status,reality_name,user_idnumber,user_mobile,apply_star_date,apply_end_date);
	}
	/**
	 * 亿美贷产品借款申请列表
	 */
	public static void YMDBorrowApplyList() {
		String apply_status=params.get("apply_status");
		String reality_name=params.get("reality_name");
		String user_idnumber=params.get("user_idnumber");
		String user_mobile=params.get("user_mobile");
		String apply_star_date=params.get("apply_star_date");
		String apply_end_date=params.get("apply_end_date");
		
		//亿美贷action参数
		params.put("is_ymd", "is_ymd");
		PageBean<Map<String, Object>> borrowApplyList=null;
		try {
			borrowApplyList=BorrowApply.applicationList(params);
		} catch (Exception e) {
			e.printStackTrace();
			
			ErrorInfo error = new ErrorInfo();
			error.code = -1;
			error.msg = "借款申请列表查询失败！";
			flash.error(error.msg); 
			renderJSON(error);
		}
		List<t_enum_map> statusList=t_enum_map.getEnumListByTypeName("t_borrow_apply.status");
		for(int i=0;i<statusList.size();i++){
		    if(statusList.get(i).is_deleted) {
		    	statusList.remove(i);
		    	i--;
		    }
		}
		render(borrowApplyList,statusList,
				apply_status,reality_name,user_idnumber,user_mobile,apply_star_date,apply_end_date);
	}
	
	/**
	 * 额度申请审核通过
	 * @param supervisor?.sign
	 * @throws Exception 
	 */
	public static void audit(String supervisorSign,String credit_apply_id) throws Exception {
		checkAuthenticity();
		ErrorInfo error = new ErrorInfo();
		if(StringUtil.isBlank(credit_apply_id) || !NumberUtil.isNumericInt(credit_apply_id)) {
			flash.error("没有credit_id！");
			creditApplyList();
		}
		long supervisorId = Security.checkSign(supervisorSign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		if(error.code < 1){
			Logger.error("额度申请审核失败1," + error.msg);
			flash.error("额度申请审核失败," + error.msg);
			creditApplyInfo(credit_apply_id);
		}
		Supervisor supervisor = Supervisor.currSupervisor();
		if(supervisor==null) {
			Logger.error("额度申请审核失败,管理员没有登录");
			flash.error("额度申请审核失败,管理员没有登录");
			creditApplyInfo(credit_apply_id);
		}
		if(Supervisor.currSupervisor().id!=supervisorId) {
			Logger.error("额度申请审核失败,审核人有误");
			flash.error("额度申请审核失败,审核人有误");
			creditApplyInfo(credit_apply_id);
		}
		
		t_credit_apply creditApply=CreditApplyService.getModelByPessimisticWrite(Long.parseLong(credit_apply_id));
		if(creditApply==null) {
			Logger.error("额度申请审核失败2,没有对应的额度申请单:" + credit_apply_id);
			flash.error("额度申请审核失败,没有对应的额度申请单!");
			creditApplyInfo(credit_apply_id);
		}
		
		String remark=params.get("remark");
		creditApply.remark=remark;
		String audit_credit_amount=params.get("audit_credit_amount");
		if(NumberUtil.isNumericDouble(audit_credit_amount)) {
			creditApply.audit_credit_amount=new BigDecimal(audit_credit_amount);
		}
		try {
			CreditApplyService.aduit(creditApply);
			flash.error("额度申请审核成功" );
			creditApplyInfo(credit_apply_id);
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error(e.getMessage() +" "+ creditApply.id);
			flash.error(e.getMessage());
			creditApplyInfo(credit_apply_id);
		}

	}
	/**
	 * 额度申请审核拒绝
	 * @param supervisor?.sign
	 * @throws Exception 
	 */
	public static void notThrough(String supervisorSign,String credit_apply_id) throws Exception {
		checkAuthenticity();
		ErrorInfo error = new ErrorInfo();
		if(StringUtil.isBlank(credit_apply_id) || !NumberUtil.isNumericInt(credit_apply_id)) {
			flash.error("没有credit_id！");
			creditApplyInfo(credit_apply_id);
		}
		long supervisorId = Security.checkSign(supervisorSign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		if(error.code < 1){
			Logger.error("额度申请拒绝失败1," + error.msg);
			flash.error("额度申请拒绝失败," + error.msg);
			creditApplyInfo(credit_apply_id);
		}
		Supervisor supervisor = Supervisor.currSupervisor();
		if(supervisor==null) {
			Logger.error("额度申请拒绝失败,管理员没有登录");
			flash.error("额度申请拒绝失败,管理员没有登录");
			creditApplyInfo(credit_apply_id);
		}
		if(Supervisor.currSupervisor().id!=supervisorId) {
			Logger.error("额度申请拒绝失败,审核人有误");
			flash.error("额度申请拒绝失败,审核人有误");
			creditApplyInfo(credit_apply_id);
		}
		
		t_credit_apply creditApply=CreditApplyService.getModelByPessimisticWrite(Long.parseLong(credit_apply_id));
		if(creditApply==null) {
			Logger.error("额度申请拒绝失败2,没有对应的额度申请单:" + credit_apply_id);
			flash.error("额度申请拒绝失败,没有对应的额度申请单!");
			creditApplyInfo(credit_apply_id);
		}
		
		String remark=params.get("remark");
		creditApply.remark=remark;
		
		try {
			CreditApplyService.notThrough(creditApply);
			flash.error("额度申请拒绝成功" );
			creditApplyInfo(credit_apply_id);
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error(e.getMessage() +" "+ credit_apply_id);
			flash.error(e.getMessage());
			creditApplyInfo(credit_apply_id);
		}

	}
	/**
	 * 冻结额度
	 * @param supervisor?.sign
	 * @throws Exception 
	 */
	public static void freeze(String supervisorSign,String credit_apply_id) throws Exception {
		checkAuthenticity();
		ErrorInfo error = new ErrorInfo();
		if(StringUtil.isBlank(credit_apply_id) || !NumberUtil.isNumericInt(credit_apply_id)) {
			flash.error("没有credit_id！");
			creditApplyInfo(credit_apply_id);
		}
		long supervisorId = Security.checkSign(supervisorSign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		if(error.code < 1){
			Logger.error("冻结额度失败1," + error.msg);
			flash.error("冻结额度失败," + error.msg);
			creditApplyInfo(credit_apply_id);
		}
		Supervisor supervisor = Supervisor.currSupervisor();
		if(supervisor==null) {
			Logger.error("冻结额度失败,管理员没有登录");
			flash.error("冻结额度失败,管理员没有登录");
			creditApplyInfo(credit_apply_id);
		}
		if(Supervisor.currSupervisor().id!=supervisorId) {
			Logger.error("冻结额度失败,审核人有误");
			flash.error("冻结额度失败,审核人有误");
			creditApplyInfo(credit_apply_id);
		}
		
		t_credit_apply creditApply=CreditApplyService.getModelByPessimisticWrite(Long.parseLong(credit_apply_id));
		if(creditApply==null) {
			Logger.error("冻结额度失败2,没有对应的额度申请单:" + credit_apply_id);
			flash.error("冻结额度失败,没有对应的额度申请单!");
			creditApplyInfo(credit_apply_id);
		}
		
		try {
			CreditApplyService.freeze(creditApply);
			flash.error("冻结额度成功" );
			creditApplyInfo(credit_apply_id);
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error(e.getMessage() +" "+ credit_apply_id);
			flash.error(e.getMessage());
			creditApplyInfo(credit_apply_id);
		}

	}
	
	/**
	 * 解冻额度
	 * @param supervisor?.sign
	 * @throws Exception 
	 */
	public static void unfreeze(String supervisorSign,String credit_apply_id) throws Exception {
		checkAuthenticity();
		ErrorInfo error = new ErrorInfo();
		if(StringUtil.isBlank(credit_apply_id) || !NumberUtil.isNumericInt(credit_apply_id)) {
			flash.error("没有credit_id！");
			creditApplyInfo(credit_apply_id);
		}
		long supervisorId = Security.checkSign(supervisorSign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		if(error.code < 1){
			Logger.error("解冻额度失败1," + error.msg);
			flash.error("解冻额度失败," + error.msg);
			creditApplyInfo(credit_apply_id);
		}
		Supervisor supervisor = Supervisor.currSupervisor();
		if(supervisor==null) {
			Logger.error("解冻额度失败,管理员没有登录");
			flash.error("解冻额度失败,管理员没有登录");
			creditApplyInfo(credit_apply_id);
		}
		if(Supervisor.currSupervisor().id!=supervisorId) {
			Logger.error("解冻额度失败,审核人有误");
			flash.error("解冻额度失败,审核人有误");
			creditApplyInfo(credit_apply_id);
		}
		
		t_credit_apply creditApply=CreditApplyService.getModelByPessimisticWrite(Long.parseLong(credit_apply_id));
		if(creditApply==null) {
			Logger.error("解冻额度失败2,没有对应的额度申请单:" + credit_apply_id);
			flash.error("解冻额度失败,没有对应的额度申请单!");
			creditApplyInfo(credit_apply_id);
		}
		
		try {
			CreditApplyService.unfreeze(creditApply);
			flash.error("解冻额度成功" );
			creditApplyInfo(credit_apply_id);
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error(e.getMessage() +" "+ credit_apply_id);
			flash.error(e.getMessage());
			creditApplyInfo(credit_apply_id);
		}

	}
	
	/**
	 * 关闭额度申请
	 * @param supervisor?.sign
	 * @throws Exception 
	 */
	public static void close(String supervisorSign,String credit_apply_id) throws Exception {
		checkAuthenticity();
		ErrorInfo error = new ErrorInfo();
		if(StringUtil.isBlank(credit_apply_id) || !NumberUtil.isNumericInt(credit_apply_id)) {
			flash.error("没有credit_id！");
			creditApplyInfo(credit_apply_id);
		}
		long supervisorId = Security.checkSign(supervisorSign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		if(error.code < 1){
			Logger.error("关闭额度失败1," + error.msg);
			flash.error("关闭额度失败," + error.msg);
			creditApplyInfo(credit_apply_id);
		}
		Supervisor supervisor = Supervisor.currSupervisor();
		if(supervisor==null) {
			Logger.error("关闭额度失败,管理员没有登录");
			flash.error("关闭额失败,管理员没有登录");
			creditApplyInfo(credit_apply_id);
		}
		if(Supervisor.currSupervisor().id!=supervisorId) {
			Logger.error("关闭额度失败,审核人有误");
			flash.error("关闭额度失败,审核人有误");
			creditApplyInfo(credit_apply_id);
		}
		
		t_credit_apply creditApply=CreditApplyService.getModelByPessimisticWrite(Long.parseLong(credit_apply_id));
		if(creditApply==null) {
			Logger.error("关闭额度失败2,没有对应的额度申请单:" + credit_apply_id);
			flash.error("关闭额度失败,没有对应的额度申请单!");
			creditApplyInfo(credit_apply_id);
		}
		
		try {
			CreditApplyService.close(creditApply);
			flash.error("关闭额度成功" );
			creditApplyInfo(credit_apply_id);
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error(e.getMessage() +" "+ credit_apply_id);
			flash.error(e.getMessage());
			creditApplyInfo(credit_apply_id);
		}

	}
	
	
	/**
	 * 提额申请审核
	 * @param supervisor?.sign
	 * @throws Exception 
	 */
	public static void auditIncreaseApply(String supervisorSign,String increase_credit_apply_id, String userId, String audit_status) throws Exception {
		checkAuthenticity();
		ErrorInfo error = new ErrorInfo();
		if(StringUtil.isBlank(increase_credit_apply_id) || !NumberUtil.isNumericInt(increase_credit_apply_id)) {
			flash.error("没有increase_credit_apply_id！");
			increaseCreditApplyList();
		}
		long supervisorId = Security.checkSign(supervisorSign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		if(error.code < 1){
			Logger.error("提额申请审核失败1," + error.msg);
			flash.error("提额申请审核失败," + error.msg);
			increaseCreditApplyInfo(increase_credit_apply_id);
		}
		Supervisor supervisor = Supervisor.currSupervisor();
		if(supervisor==null) {
			Logger.error("提额申请审核失败,管理员没有登录");
			flash.error("提额申请审核失败,管理员没有登录");
			increaseCreditApplyInfo(increase_credit_apply_id);
		}
		if(Supervisor.currSupervisor().id!=supervisorId) {
			Logger.error("提额申请审核失败,审核人有误");
			flash.error("提额申请审核失败,审核人有误");
			increaseCreditApplyInfo(increase_credit_apply_id);
		}
		if(StringUtils.isEmpty(audit_status)) {
			Logger.error("审核状态有误");
			flash.error("审核状态有误");
			increaseCreditApplyInfo(increase_credit_apply_id);
		}
		
		t_credit_increase_apply IncreaseCreditApply= t_credit_increase_apply.findById(Long.parseLong(increase_credit_apply_id));
		 
		
		if(IncreaseCreditApply==null) {
			Logger.error("提额申请审核失败2,没有对应的提额申请单:" + increase_credit_apply_id);
			flash.error("提额申请审核失败,没有对应的提额申请单!");
			increaseCreditApplyInfo(increase_credit_apply_id);
		}
		
		String remark=params.get("remark");
		IncreaseCreditApply.remark=remark;
		String audit_credit_amount=params.get("audit_credit_amount");
		if(NumberUtil.isNumericDouble(audit_credit_amount)) {
			IncreaseCreditApply.audit_credit_amount=new BigDecimal(audit_credit_amount);
		}
		try {
			CreditApplyService.aduitIncreaseApply(IncreaseCreditApply,audit_status);
			flash.error("提额申请审核成功" );
			increaseCreditApplyInfo(increase_credit_apply_id);
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error(e.getMessage() +" "+ IncreaseCreditApply.id);
			flash.error(e.getMessage());
			increaseCreditApplyInfo(increase_credit_apply_id);
		}

	}


	/**
	 * 运营商报告 用户通讯记录信息
	 * @throws Exception 
	 */
	public static void reportContactDetail() throws Exception {

		String userId = params.get("userId");

		ErrorInfo error = new ErrorInfo();
		
		if (StringUtils.isEmpty(userId)) {
			error.code = -1;
			error.msg = "用户身ID不能为空！";
			render(error);
		}

		// 通讯记录
		PageBean<Map<String, Object>> contactDetail = ReportUserInfo.getReportContactDetail(params);
		
		renderJSON(contactDetail);
	}
}
