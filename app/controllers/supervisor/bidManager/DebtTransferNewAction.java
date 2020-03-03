package controllers.supervisor.bidManager;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import constants.BidPreFixEnum;
import constants.Constants;
import constants.SQLTempletes;
import controllers.DebtTransferCheck;
import controllers.supervisor.SupervisorController;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import models.t_debt_transfer;
import models.v_bid_not_through;
import models.v_debt_auction_records;
import models.v_debt_auditing_transfers;
import models.v_debt_no_pass_transfers;
import models.v_debt_transfer;
import models.v_debt_transfer_failure;
import models.v_debt_transfering;
import models.v_debt_transfers_success;
import annotation.DebtCheck;
import business.Bid;
import business.Debt;
import business.DebtNew;
import business.Product;
import business.Supervisor;
import business.User;
import business.UserAuditItem;
import play.db.jpa.JPA;
import play.mvc.With;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.NumberUtil;
import utils.PageBean;
import utils.QueryUtil;
import utils.Security;

/**
 * 债权转让管理
 * 
 * @ClassName DebtTransferNewAction
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年7月18日 上午9:37:00
 * @version 1.0.0
 */
@With(DebtTransferCheck.class)
public class DebtTransferNewAction extends SupervisorController {

	/**
	 * 待审核债权转让标
	 */
	@DebtCheck(2)
	public static void debtTransferPending() {

		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");

		String startDateStr = params.get("startDateStr");
		String endDateStr = params.get("endDateStr");
		String orderType = params.get("orderType");
		String typeStr = params.get("typeStr");
		String keyWords = params.get("keyWords");

		PageBean<v_debt_auditing_transfers> page = Debt.queryAllAuditingTransfers(typeStr, startDateStr, endDateStr,
				keyWords, orderType, currPageStr, pageSizeStr);

		if (page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		render(page);
	}

	/**
	 * 转让中债权转让标
	 */
	@DebtCheck(2)
	public static void debtIsTransfer() {

		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");

		String orderType = params.get("orderType");
		String typeStr = params.get("typeStr");
		String keyWords = params.get("keyWords");

		PageBean<v_debt_transfering> page = Debt.queryAllTransferingDebts(typeStr, keyWords, orderType, currPageStr,
				pageSizeStr);

		render(page);
	}

	/**
	 * 转让中债权转让标（新）
	 */
	@DebtCheck(2)
	public static void debtIsTransferNew() {

		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		String startDateStr = params.get("startDateStr");
		String endDateStr = params.get("endDateStr");

		String orderType = params.get("orderTypeStr");
		String typeStr = params.get("typeStr");
		String keyWords = params.get("keyWords");

		PageBean<v_debt_transfer> page = DebtNew.queryAllTransferingDebtsNew(typeStr, keyWords, orderType, currPageStr,
				pageSizeStr, startDateStr, endDateStr);

		render(page);
	}



	/**
	 * 成功的债权转让标（新）
	 */
	@DebtCheck(2)
	public static void successDebtTransferNew() {

		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");

		String startDateStr = params.get("startDateStr");
		String endDateStr = params.get("endDateStr");
		String orderType = params.get("orderTypeStr");
		String typeStr = params.get("typeStr");
		String keyWords = params.get("keyWords");

		PageBean<v_debt_transfer> page = DebtNew.queryAllSuccessedDebtsNew(typeStr, startDateStr, endDateStr, keyWords,
				orderType, currPageStr, pageSizeStr);

		if (page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		render(page);
	}

	/**
	 * 未通过的转让债权标（新）
	 */
	@DebtCheck(2)
	public static void nopassAssignedClaimsNew() {

		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		String startDateStr = params.get("startDateStr");
		String endDateStr = params.get("endDateStr");
		String orderType = params.get("orderTypeStr");
		String typeStr = params.get("typeStr");
		String keyWords = params.get("keyWords");

		PageBean<v_debt_transfer> page = DebtNew.queryAllNopassDebtsNew(typeStr, startDateStr, endDateStr, keyWords,
				orderType, currPageStr, pageSizeStr);

		if (page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		render(page);
	}

	/**
	 * 失败的债权转让标
	 */
	@DebtCheck(2)
	public static void failedDebtTransferNew() {

		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		String startDateStr = params.get("startDateStr");
		String endDateStr = params.get("endDateStr");
		String orderType = params.get("orderTypeStr");
		String typeStr = params.get("typeStr");
		String keyWords = params.get("keyWords");

		PageBean<v_debt_transfer> page = DebtNew.queryAllFailureDebtsNew(typeStr, startDateStr, endDateStr, keyWords,
				orderType, currPageStr, pageSizeStr);

		if (page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		render(page);
	}



	

	/**
	 * 待审核债权转让标(新)
	 */
	@DebtCheck(2)
	public static void debtTransferPendingNew() {

		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");

		String startDateStr = params.get("startDateStr");
		String endDateStr = params.get("endDateStr");
		String orderType = params.get("orderTypeStr");
		String typeStr = params.get("typeStr");
		String keyWords = params.get("keyWords");

		PageBean<v_debt_transfer> page = DebtNew.queryAllAuditingTransfersNew(typeStr, startDateStr, endDateStr,
				keyWords, orderType, currPageStr, pageSizeStr);

		if (page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		render(page);
	}

	/**
	 * 待审核转让标详情
	 */
	@DebtCheck(2)
	public static void auditTransferPendingDetailsNew(Integer id) {

		DebtNew debt_transfer = DebtNew.debtTransferDetail(id);
		if (debt_transfer == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		render(debt_transfer);
	}

	/**
	 * 转让中转让标详情
	 */
	@DebtCheck(2)
	public static void debtIsTransferDetailNew(Integer id) {

		DebtNew debt_transfer = DebtNew.debtTransferingDetail(id);
		if (debt_transfer == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		render(debt_transfer);
	}

	/**
	 * 成功转让标详情
	 */
	@DebtCheck(2)
	public static void successDebtIsTransferDetailNew(Integer id) {

		DebtNew debt_transfer = DebtNew.debtTransferingDetail(id);
		if (debt_transfer == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		render(debt_transfer);
	}

	/**
	 * 未通过转让标详情
	 */
	@DebtCheck(2)
	public static void noPassDebtIsTransferDetailNew(Integer id) {

		DebtNew debt_transfer = DebtNew.debtTransferingDetail(id);
		if (debt_transfer == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		render(debt_transfer);
	}
	/**
	 * 失败的转让标详情
	 */
	@DebtCheck(2)
	public static void failedDebtIsTransferDetailNew(Integer id) {
		
		DebtNew debt_transfer = DebtNew.debtTransferingDetail(id);
		if (debt_transfer == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		render(debt_transfer);
	}
	
	/**
	 * 转让中的债权转让标->撤标
	 * 退还投资金额
	 * 退还服务费
	 */
	public static void transferingToNotThrough(String sign,String reason){
		//checkAuthenticity();
		ErrorInfo error = new ErrorInfo();
		long debtTransferId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 1 ){
			flash.error(error.msg); 
			//debtIsTransferNew();
			error.code = -1;
			error.msg = "数据传输失败！";
			renderJSON(error);
		}
		Date actionDate=new Date();
		DebtNew debtNew = new DebtNew(actionDate,error);
		
		//撤标
		debtNew.transferingToNotThrough(debtTransferId,reason);
		if(error.code < 1){
			error.code = -1;
		}
		flash.error(error.msg);
		renderJSON(error);
		//debtIsTransferNew();
		
	}
}
