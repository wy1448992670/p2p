package controllers.supervisor.billCollectionManager;

import java.io.File;
import java.util.Date;
import java.util.List;

import models.v_bill_department_haspayed;
import models.v_bill_department_month_maturity;
import models.v_bill_department_overdue;
import models.v_bill_detail;
import models.v_bill_detail_for_collection;
import models.v_bill_detail_for_mark_overdue;
import models.v_bill_haspayed;
import models.v_bill_month_maturity;
import models.v_bill_overdue;
import models.v_bill_repayment_record;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.lang.StringUtils;

import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;
import business.BackstageSet;
import business.Bid;
import business.Bill;
import business.StationLetter;
import business.Supervisor;
import business.TemplateEmail;
import constants.Constants;
import controllers.supervisor.SupervisorController;

/**
 * 
 * 类名:UserLoanBills
 * 功能:会员借款账单管理(我的会员和部门会员)
 */

public class UserLoanBills  extends SupervisorController{

//	public static void thisMonthMaturityBillLeft(){
//		thisMonthMaturityBills();
//	}
	
	/**
	 * 本月到期账单
	 */
	public static void thisMonthMaturityBills(){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		Supervisor supervisor = Supervisor.currSupervisor();
		
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bill_month_maturity> page = Bill.queryBillMonthMaturity(supervisor.id, yearStr, monthStr,typeStr, key, orderType, 
				currPageStr, pageSizeStr, error);
		
		render(page);
	}
	
	/**
	 * 本月到期账单(部门)
	 */
	public static void thisMonthMaturityBillDept(int isExport){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String kefuStr = params.get("kefuStr");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bill_department_month_maturity> page = Bill.queryBillDepartmentMonthMaturity(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, 1L, yearStr, monthStr,
				typeStr, key,kefuStr, orderType, currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		if(isExport == Constants.IS_EXPORT){
			
			List<v_bill_department_month_maturity> list = page.page;
			 
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bill = (JSONObject)obj;
				
				String overtime = "";
				long  overdue_time = bill.getLong("overdue_time");
				overtime = (overdue_time/86400) + "天" + (overdue_time%86400/3600) + "时" + (overdue_time%3600/60) + "分";
				
				String supervisorName = "暂无分配";
				if(StringUtils.isEmpty(bill.getString("supervisor_name2"))){
					if(StringUtils.isNotEmpty((bill.getString("supervisor_name")))){
						supervisorName = bill.getString("supervisor_name");
					}
				}else{
					supervisorName = bill.getString("supervisor_name2");
				}
				
				bill.put("overdue_time", overtime);
				bill.put("supervisor_name", supervisorName);
				bill.put("apr", String.format("%.1f", bill.getDouble("apr")) + "%");
			}
			
			File file = ExcelUtils.export("本月到期账单",
			arrList,
			new String[] {
			"账单编号", "借款人", "借款标编号", "借款金额",
			"年利率", "账单标题", "账单期数", "还款时间", "逾期时长", "逾期账单",
			"客服"},
			new String[] {"bill_no", "name", "bid_no",
			"amount", "apr", "title", "period",
			"repayment_time", "overdue_time",
			"overdue_count", "supervisor_name"});
			   
			renderBinary(file, "本月到期账单(部门).xls");
		}
		
		render(page);
	}
	
	/**
	 * 催收账单
	 */
	public static void queryCollection(String billIdStr) {
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		long id = Security.checkSign(billIdStr, Constants.BILL_ID_SIGN, 3600, error);
		
		v_bill_detail_for_collection collection = Bill.queryCollection(supervisor.id, id, error);
		
		if(collection == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(collection);
	}
	
	/**
	 * 催收账单
	 */
	public static void queryCollectionDept(String billId) {
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();

		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		v_bill_detail_for_collection collection = Bill.queryCollection(supervisor.id, id, error);
		
		if(collection == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		renderTemplate("", collection);
	}
	
	/**
	 * 借款标详情
	 * @param bidid
	 * @param type 账单类型 1 本月到期账单 2 逾期账单 3 已还款账单列表
	 */
	public static void detail(long bidid, int type, int falg) { 
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = falg;
		bid.id = bidid;
		
		render(bid, type, falg);
	}
	
	/**
	 * 借款标详情
	 */
	public static void detailDept(long bidid) { 
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.id = bidid;
		
		render(bid);
	}
	
	/**
	 * 标记账单逾期页面
	 */
	public static void queryOverdue(String billIdStr) {
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		long id = Security.checkSign(billIdStr, Constants.BILL_ID_SIGN, 3600, error);
		
		v_bill_detail_for_mark_overdue overdue = Bill.queryOverdue(supervisor.id, id, error);
		
		if(overdue == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(overdue);
	}
	
	/**
	 * 标记账单坏账页面
	 */
	public static void queryBadBills(String billIdStr) {
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		long id = Security.checkSign(billIdStr, Constants.BILL_ID_SIGN, 3600, error);
		
		v_bill_detail_for_mark_overdue overdue = Bill.queryOverdue(supervisor.id, id, error);
		
		if(overdue == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(overdue);
	}
	
//	/**
//	 * 逾期账单按钮
//	 */
//	public static void queryOverdueDept(String billIdStr) {
//		ErrorInfo error = new ErrorInfo();
//		Supervisor supervisor = Supervisor.currSupervisor();
//		
//		v_bill_detail_for_mark_overdue overdue = Bill.queryOverdue(supervisor.id, billIdStr, error);
//		
//		if(overdue == null) {
//			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
//		}
//		
//		render(overdue);
//	}
	
	/**
	 * 发送站内信催收
	 * @param userIdStr
	 * @param typeStr
	 * @param billIdStr
	 * @param title
	 * @param content
	 */
	public static void updateBillCollectionDeptByMessage(String userIdStr, String typeStr, String billIdStr,
			String title, String content) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		if(!NumberUtil.isNumericInt(userIdStr) || !NumberUtil.isNumericInt(typeStr)) {
 			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
 		}
		
		if(StringUtils.isBlank(title)) {
			error.msg = "标题不能为空";
			error.code = -1;
			json.put("error", error);
			renderJSON(json);
		}
		
		if(StringUtils.isBlank(content)) {
			error.msg = "内容不能为空";
			error.code = -1;
			json.put("error", error);
			renderJSON(json);
		}
		
		if(content.length() > 140) {
			error.msg = "内容不能超过140个字";
			error.code = -1;
			json.put("error", error);
			renderJSON(json);
		}
 		
		long id = Security.checkSign(billIdStr, Constants.BILL_ID_SIGN, 3600, error);
		
		long userId = Long.parseLong(userIdStr);
		
		StationLetter letter = new StationLetter();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		letter.senderSupervisorId = 1L;
		letter.receiverUserId = userId;
		letter.title = title;
		letter.content = content;
		letter.sendToUserBySupervisor(error);
		
		if(error.code >= 0){
		    Bill.updateBillCollection(supervisor.id,typeStr, id, error);
		}
		
		json.put("error", error);
		renderJSON(json);
	} 
	
	/**
	 * 发送邮件催收
	 * @param userIdStr
	 * @param typeStr
	 * @param billIdStr
	 * @param title
	 * @param content
	 */
	public static void updateBillCollectionDeptByEmail(String email, String typeStr, String billIdStr,
			String title, String content) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		if(!NumberUtil.isNumericInt(typeStr)) {
 			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
 		}
		
		if(StringUtils.isBlank(title)) {
			error.msg = "标题不能为空";
			error.code = -1;
			json.put("error", error);
			renderJSON(json);
		}
		
		if(StringUtils.isBlank(content)) {
			error.msg = "内容不能为空";
			error.code = -1;
			json.put("error", error);
			renderJSON(json);
		}
		
		TemplateEmail.sendEmail(3, email, title, content, error);
		
		long id = Security.checkSign(billIdStr, Constants.BILL_ID_SIGN, 3600, error);
		
		if(error.code >= 0){
		    Bill.updateBillCollection(supervisor.id,typeStr, id, error);
		    System.out.println(typeStr);
		}
		
		json.put("error", error);

		renderJSON(json);
	}
	
	/**
	 * 电话催收
	 * @param userIdStr
	 * @param typeStr
	 * @param billIdStr
	 * @param title
	 * @param content
	 */
	public static void updateBillCollectionDeptByMobile(String typeStr, String billIdStr) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		long id = Security.checkSign(billIdStr, Constants.BILL_ID_SIGN, 3600, error);
		
		Bill.updateBillCollection(supervisor.id,typeStr, id, error);
		
		json.put("error", error);

		renderJSON(json);
	}
	
	/**
	 * 发送站内信催收
	 * @param userIdStr
	 * @param typeStr
	 * @param billIdStr
	 * @param title
	 * @param content
	 */
	public static void updateBillCollectionByMessage(String userIdStr, String typeStr, String billIdStr,
			String title, String content) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		if(!NumberUtil.isNumericInt(userIdStr) || !NumberUtil.isNumericInt(typeStr)) {
 			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
 		}
		
		if(StringUtils.isBlank(title)) {
			error.msg = "标题不能为空";
			error.code = -1;
			json.put("error", error);
			renderJSON(json);
		}
		
		if(StringUtils.isBlank(content)) {
			error.msg = "内容不能为空";
			error.code = -1;
			json.put("error", error);
			renderJSON(json);
		}
		
		if(content.length() > 140) {
			error.msg = "内容不能超过140个字";
			error.code = -1;
			json.put("error", error);
			renderJSON(json);
		}
 		
		long id = Security.checkSign(billIdStr, Constants.BILL_ID_SIGN, 3600, error);
		
		long userId = Long.parseLong(userIdStr);
		
		StationLetter letter = new StationLetter();
		
		letter.senderSupervisorId = 1L;
		letter.receiverUserId = userId;
		letter.title = title;
		letter.content = content;
		letter.sendToUserBySupervisor(error);
		
		if(error.code >= 0){
		    Bill.updateBillCollection(supervisor.id,typeStr, id, error);
		}
		
		json.put("error", error);
		renderJSON(json);
	} 
	
	/**
	 * 发送邮件催收
	 * @param userIdStr
	 * @param typeStr
	 * @param billIdStr
	 * @param title
	 * @param content
	 */
	public static void updateBillCollectionByEmail(String email, String typeStr, String billIdStr,
			String title, String content) {
        ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		if(!NumberUtil.isNumericInt(typeStr)) {
 			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
 		}
		
		if(StringUtils.isBlank(title)) {
			error.msg = "标题不能为空";
			error.code = -1;
			json.put("error", error);
			renderJSON(json);
		}
		
		if(StringUtils.isBlank(content)) {
			error.msg = "内容不能为空";
			error.code = -1;
			json.put("error", error);
			renderJSON(json);
		}
		
		long id = Security.checkSign(billIdStr, Constants.BILL_ID_SIGN, 3600, error);
		
		TemplateEmail.sendEmail(3, email, title, content, error);
		
		if(error.code >= 0){
		    Bill.updateBillCollection(supervisor.id,typeStr, id, error);
		}
		
		json.put("error", error);

		renderJSON(json);
	}
	
	/**
	 * 电话催收
	 * @param userIdStr
	 * @param typeStr
	 * @param billIdStr
	 * @param title
	 * @param content
	 */
	public static void updateBillCollectionByMobile(String typeStr, String billIdStr) {
        ErrorInfo error = new ErrorInfo();
		
	    long id = Security.checkSign(billIdStr, Constants.BILL_ID_SIGN, 3600, error);
		
		
		JSONObject json = new JSONObject();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		Bill.updateBillCollection(supervisor.id,typeStr, id, error);
		
		json.put("error", error);

		renderJSON(json);
	}

	/**
	 * 逾期账单
	 */
	public static void overdueBills(){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_bill_overdue> page = Bill.queryBillOverdue(supervisor.id, yearStr, monthStr, typeStr, key, orderType, 
				currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}
	
	/**
	 * 逾期账单(部门)
	 */
	public static void overdueBillDept(int isExport){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String kefuStr = params.get("kefuStr");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_bill_department_overdue> page = Bill.queryBillDepartmentOverdue(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, supervisor.id, yearStr, monthStr,
				typeStr, key,kefuStr, orderType, currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		if(isExport == Constants.IS_EXPORT){
			
			List<v_bill_department_overdue> list = page.page;
			 
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bill = (JSONObject)obj;
				
				String supervisorName = "暂无分配";
				if(StringUtils.isEmpty(bill.getString("supervisor_name2"))){
					if(StringUtils.isNotEmpty((bill.getString("supervisor_name")))){
						supervisorName = bill.getString("supervisor_name");
					}
				}else{
					supervisorName = bill.getString("supervisor_name2");
				}

				bill.put("supervisor_name", supervisorName);
				bill.put("apr", String.format("%.1f", bill.getDouble("apr")) + "%");
			}
			
			File file = ExcelUtils.export("逾期账单(部门)",
			arrList,
			new String[] {
			"账单编号", "借款人", "借款标编号", "借款金额",
			"年利率", "账单标题", "账单期数", "还款时间", "逾期时长", "逾期账单",
			"客服"},
			new String[] {"bill_no", "name", "bid_no",
			"amount", "apr", "title", "period",
			"repayment_time", "overdue_time",
			"overdue_count", "supervisor_name"});
			   
			renderBinary(file, "逾期账单(部门).xls");
		}
		
		render(page);
	}
	
	/**
	 * 标记逾期
	 */
	public static void markOverdue(String billId) {
		ErrorInfo error = new ErrorInfo();
		
	    long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
	    
		JSONObject json = new JSONObject();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		Bill.markOverdue(supervisor.id, id, error);
		
		json.put("error", error);

		renderJSON(json);
	}
	
	/**
	 * 标记坏账
	 */
	public static void markBillBad(String billIdStr) {
		ErrorInfo error = new ErrorInfo();
			
	    long id = Security.checkSign(billIdStr, Constants.BILL_ID_SIGN, 3600, error);
	    
		JSONObject json = new JSONObject();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		Bill.markBad(supervisor.id, id, error);
		
		json.put("error", error);

		renderJSON(json);
	}

	/**
	 * 已还款账单
	 */
	public static void paidBills(){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bill_haspayed> page = Bill.queryBillHasPayed(Supervisor.currSupervisor().id, yearStr, monthStr, typeStr, key, orderType, 
				currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}
	
	/**
	 * 已还款账单(部门)
	 */
	public static void paidBillDept(int isExport){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String kefuStr = params.get("kefuStr");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_bill_department_haspayed> page = Bill.queryBillDepartmentHasPayed(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0,supervisor.id, yearStr, monthStr,
				typeStr, key,kefuStr, orderType, currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		if(isExport == Constants.IS_EXPORT){
			
			List<v_bill_department_haspayed> list = page.page;
			 
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bill = (JSONObject)obj;
				
				String supervisorName = "暂无分配";
				if(StringUtils.isEmpty(bill.getString("supervisor_name2"))){
					if(StringUtils.isNotEmpty((bill.getString("supervisor_name")))){
						supervisorName = bill.getString("supervisor_name");
					}
				}else{
					supervisorName = bill.getString("supervisor_name2");
				}

				bill.put("supervisor_name", supervisorName);
				bill.put("apr", String.format("%.1f", bill.getDouble("apr")) + "%");
			}
			
			File file = ExcelUtils.export("已还款账单(部门)",
			arrList,
			new String[] {
			"账单编号", "借款人", "借款标编号", "借款金额",
			"年利率", "账单标题", "账单期数", "还款时间", "逾期时长", "实际还款时间",
			"客服"},
			new String[] {"bill_no", "name", "bid_no",
			"amount", "apr", "title", "period",
			"repayment_time", "overdue_time",
			"real_repayment_time", "supervisor_name"});
			   
			renderBinary(file, "已还款账单(部门).xls");
		}
		
		render(page);
	}
	
	/**
	 * 账单详情
	 */
	public static void billDetail(String billId, int type, int currPage) { 
		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		v_bill_detail billDetail = Bill.queryBillDetails(id, error);
		PageBean<v_bill_repayment_record> page = Bill.queryBillReceivables(billDetail.bid_id, currPage, 0, error);
		BackstageSet backSet = BackstageSet.getCurrentBackstageSet();
		
		render(billDetail, page, backSet, type);
	}
	
	/**
	 * 部门账单详情
	 */
	public static void billDetailDept(String billId, int type) { 
        ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		int currPage = 1;
		String curPage = params.get("currPage");
		
		if(curPage != null) {
			currPage = Integer.parseInt(curPage);
		}
		
		v_bill_detail billDetail = Bill.queryBillDetails(id, error);
		PageBean<v_bill_repayment_record> page = Bill.queryBillReceivables(billDetail.bid_id, currPage, 0, error);
		
		render(billDetail,page, type);
	}
}
