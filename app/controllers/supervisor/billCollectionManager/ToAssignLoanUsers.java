package controllers.supervisor.billCollectionManager;

import java.util.List;
import net.sf.json.JSONObject;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import models.v_supervisors;
import models.v_user_loan_user_unassigned;
import business.Bid;
import business.Product;
import business.Supervisor;
import business.User;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;

/**
 * 
 * 类名:ToAssignLoanUsers
 * 功能:待分配借款会员列表
 */

public class ToAssignLoanUsers extends SupervisorController {

	/**
	 * 待分配借款会员列表
	 */
	public static void toAssignUsers(int productId) {
		
		ErrorInfo error = new ErrorInfo();
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		String name= params.get("name");
		String startDate = params.get("startDate");
		String endDate = params.get("endDate");
		String orderType = params.get("orderType");
		
		List<Product> products = Product.queryProductNames(false,error);
		PageBean<v_user_loan_user_unassigned> page = User.queryUserUnassigned(name,startDate,endDate,productId,orderType,currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		render(page,products);
		
	}
	
	/**
	 * 借款会员管理分配
	 * @param currPage
	 * @param pageSize
	 * @param keyword
	 */
	public static void loanUserAssign(int currPage, int pageSize, String keyword, String bidId, String type) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_supervisors> page = Supervisor.queryCustomers(currPage, pageSize, 0, 0, keyword, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(page,bidId,type);
	}

	/**
	 * 分配单个借款标
	 */
	public static void assignBid( String typeStr,String tosSupervisorIdStr, String bidIdStr) {
		ErrorInfo error = new ErrorInfo();
		long toSupervisorId = Security.checkSign(tosSupervisorIdStr, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		long bidId = Security.checkSign(bidIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		Bid.assignBidToSupervisor(toSupervisorId, typeStr, bidId, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 分配用户所有的标
	 */
	public static void assignUser(String typeStr, String tosSupervisorIdStr, String bidIdStr) {
		ErrorInfo error = new ErrorInfo();
		long toSupervisorId = Security.checkSign(tosSupervisorIdStr, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		long bidId = Security.checkSign(bidIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		User.assignUser(toSupervisorId, typeStr, bidId, error);
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 重新分配用户所有的标
	 */
	public static void assignUserAgain(String typeStr, String tosSupervisorIdStr, String userIdStr) {
		ErrorInfo error = new ErrorInfo();
		long toSupervisorId = Security.checkSign(tosSupervisorIdStr, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		Supervisor supervisor = Supervisor.currSupervisor();
		
		User.assignUserAgain(supervisor.id, typeStr, toSupervisorId+"", userIdStr, error);
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 部门账单管理--借款会员管理
	 */
	public static void updateassignedUser(String tosSupervisorIdStr, String bidIdStr) {
		ErrorInfo error = new ErrorInfo();
		long toSupervisorId = Security.checkSign(tosSupervisorIdStr, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		long bidId = Security.checkSign(bidIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		int code = User.assignUser(toSupervisorId, "2", bidId, error);
		
		if(code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		renderJSON(error);
	}

	
}
