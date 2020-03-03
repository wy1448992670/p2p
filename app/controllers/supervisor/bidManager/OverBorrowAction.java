package controllers.supervisor.bidManager;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import business.BackstageSet;
import business.OverBorrow;
import business.StationLetter;
import business.Supervisor;
import business.User;
import business.UserAuditItem;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import models.v_debt_transfer_failure;
import models.v_user_audit_items;
import models.v_user_over_borrows;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.PageBean;

/**
 * 超额借款管理
 * @author lzp
 * @version 6.0
 * @created 2014-6-18
 */
public class OverBorrowAction extends SupervisorController {
	/**
	 * 超额借款列表
	 */
	public static void overBorrows(int isExport, int currPage, int pageSize, int keywordType, String keyword, int orderType, String startDateStr, String endDateStr) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_over_borrows> pageBean = 
		OverBorrow.queryOverBorrows(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, currPage, pageSize, keywordType, keyword, orderType, startDateStr, endDateStr, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
	    
	    if(isExport == Constants.IS_EXPORT){
			
			List<v_user_over_borrows> list = pageBean.page;
			 
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject debt = (JSONObject)obj;
				
				String creditLevel = User.queryCreditLevelByImage(debt.getString("filename"));
				
				debt.put("filename", creditLevel);
			}
			
			File file = ExcelUtils.export("超额借款列表",
			arrList,
			new String[] {
			"借款人", "信用等级", "注册邮箱", "绑定手机",
			"标准信用额度", "超额申请金额", "通过额度", "申请时间", "已通过资料数",
			"补提交资料数","审核状态"},
			new String[] {"user_name", "filename",
			"user_email", "user_mobile", "user_credit_line", "amount",
			"pass_amount", "time",
			"passed_items_count", "appended_items_count",
			"status"});
			   
			renderBinary(file, "超额借款列表.xls");
		}
		
		render(pageBean);
	}

	/**
	 * 超额借款详情
	 */
	public static void overBorrowDetails(long id) {
		ErrorInfo error = new ErrorInfo();
		long preOverBorrowId = OverBorrow.queryPreOverBorrowId(id);
		long nextOverBorrowId = OverBorrow.queryNextOverBorrowId(id);
		long preOverBorrowCount = OverBorrow.queryPreOverBorrowCount(id);
		long laterOverBorrowCount = OverBorrow.queryLaterOverBorrowCount(id);
		List<v_user_audit_items> items = OverBorrow.queryAuditItems(id, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		List<v_user_over_borrows> historyOverBorrows = OverBorrow.queryHistoryOverBorrows(id, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		v_user_over_borrows auditingOverBorrow = OverBorrow.queryAuditingOverBorrow(id, error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long userId = OverBorrow.queryUserId(id, error);
		User user = new User();
		user.lazy = true;
		user.id = userId;
		
		Map<String ,String> creditRecord = User.debtUserhistorySituation(userId,error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		int passedAuditItemsCount = OverBorrow.queryPassedAuditItemsCount(id, error);
		int creditToMoney = (int) BackstageSet.getCurrentBackstageSet().creditToMoney;
		int suggestPassAmount =  creditToMoney * OverBorrow.queryCreditScore(id, error);
		
		String over_loan_no_through = Constants.OVER_LOAN_NO_THROUGH;
		
		render(user, preOverBorrowId, nextOverBorrowId, preOverBorrowCount, laterOverBorrowCount, creditRecord, items, 
				historyOverBorrows, auditingOverBorrow, passedAuditItemsCount, suggestPassAmount, over_loan_no_through);
	}
	
	/**
	 * 发送站内信
	 * @param receiverName
	 * @param content
	 */
	public static void sendMessage(String receiverName, String content){
		long supervisorId = Supervisor.currSupervisor().id;
		ErrorInfo error = new ErrorInfo();
		
		StationLetter message = new StationLetter();
		message.senderSupervisorId = supervisorId;
		message.receiverUserName = receiverName;
		message.content = content;
		message.sendToUserBySupervisor(error); 
		
		renderJSON(error);
	}
	
	
	
	/**
	 * 查看资料图片
	 */
	public static void showitem(long itemId){
		UserAuditItem item = new UserAuditItem();
		item.lazy = true;
		item.id = itemId;
		
		render(item);
	}
	
	/**
	 * 审核
	 * @param supervisorId
	 * @param overBorrowId
	 * @param status
	 * @param passAmount
	 * @param auditOpinion
	 */
	public static void audit(long overBorrowId, int status, int passAmount, String auditOpinion) {
		long supervisorId = Supervisor.currSupervisor().id;
		
		ErrorInfo error = new ErrorInfo();
		OverBorrow.audit(supervisorId, overBorrowId, status, passAmount, auditOpinion, error);
		
		renderJSON(error);
	}
}
