package controllers.supervisor.financeManager;

import java.io.File;
import java.util.Date;
import java.util.List;

import payment.PaymentProxy;
import models.t_bill_invests;
import models.v_bid_bad;
import models.v_bill_invest_detail;
import models.v_bill_invests_overdue_unpaid;
import models.v_bill_invests_paid;
import models.v_bill_invests_payables_statistics;
import models.v_bill_invests_pending_payment;
import models.v_bill_invests_principal_advances;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.PageBean;
import utils.Security;
import business.BackstageSet;
import business.Bid;
import business.Bill;
import business.BillInvests;
import business.Supervisor;
import constants.Constants;
import constants.IPSConstants.CompensateType;
import constants.IPSConstants.IPSDealStatus;
import controllers.supervisor.SupervisorController;
import controllers.supervisor.bidManager.BidPlatformAction;

/**
 * 
 * @ClassName:      PayableBillManager
 * @Description:	应付账单管理
 */

public class PayableBillManager extends SupervisorController {

	/**
	 * 待付款理财账单列表
	 */
	public static void toPayBills(){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_bill_invests_pending_payment> page = Bill.queryBillInvestPending(supervisor.id, yearStr, monthStr,
				typeStr, key, orderType, currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}

	/**
	 * 逾期未付理财账单列表
	 */
	public static void overdueUnpaidBills(){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_bill_invests_overdue_unpaid> page = Bill.queryBillOverdueUnpaid(supervisor.id, yearStr, monthStr,
				typeStr, key, orderType, currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}

	/**
	 * 已付款理财账单列表
	 */
	public static void paidBills(){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String paidType = params.get("paidType");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_bill_invests_paid> page = Bill.queryBillInvestPaid(supervisor.id, yearStr, monthStr,
				typeStr, key, paidType, orderType, currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}

	/**
	 * 本金垫付理财账单列表
	 */
	public static void principalAdvanceBills(){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_bill_invests_principal_advances> page = Bill.queryBillPrincipalAdvances(supervisor.id, yearStr, monthStr,
				typeStr, key, orderType, currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}

	/**
	 * 应付款理财账单统计表
	 */
	public static void payableBills(int isExport){
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_bill_invests_payables_statistics> page = Bill.queryBillInvestStatistics(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0,supervisor.id, yearStr, monthStr,
				 orderType, currPageStr, pageSizeStr, error);
		
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		if(isExport == Constants.IS_EXPORT){
			
			List<v_bill_invests_payables_statistics> list = page.page;
			 
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bill = (JSONObject)obj;			

				bill.put("ontime_complete_rate", String.format("%.1f", bill.getDouble("ontime_complete_rate")) + "%");
				bill.put("principal_advances_rate", String.format("%.1f", bill.getDouble("principal_advances_rate")) + "%");
				bill.put("nopaid_rate", String.format("%.1f", bill.getDouble("nopaid_rate")) + "%");
			}
			
			File file = ExcelUtils.export("应付款理财账单统计表",
			arrList,
			new String[] {
			"年", "月", "应付账单数", "应付金额", "实际已付账单数",
			"实际已付金额", "正常付款账单数", "正常付款率", "本金垫付账单数",
			"垫付总额", "垫付占比","未付账单数", "未付总额", "未付占比"},
			new String[] {"year", "month", "payables_bills",
			"payables_amount", "has_paid_bills", 
			"has_paid_amount", "normal_paid_bills",
			"ontime_complete_rate", "principal_advances_bills",
			"principal_advances_amount", "principal_advances_rate",
			"nopaid_bills", "nopaid_amount", "nopaid_rate"});
			   
			renderBinary(file, "应付款理财账单统计表.xls");
		}
		
		render(page);
	}

	/**
	 * 坏账借款标列表
	 */
	public static void badList(){
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_bad> pageBean = new PageBean<v_bid_bad>();
		pageBean.page = Bid.queryBidBad(0, pageBean, 0, error, BidPlatformAction.getParameter(pageBean, null));

		if (null == pageBean.page) 
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);  
		
		render(pageBean);
	}

	/**
	 * 账单详情
	 * @param billInvestId
	 */
	public static void investBillDetails(String billId, int type, int currPage){
		ErrorInfo error = new ErrorInfo();
		int pageSize = Constants.FIVE;
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		v_bill_invest_detail investDetail = BillInvests.queryMyInvestBillDetails(id, error);
		PageBean<t_bill_invests> page = BillInvests.queryMyInvestBillReceivables(investDetail.bid_id, investDetail.user_id, investDetail.invest_id, currPage,pageSize, error);
		BackstageSet backSet = BackstageSet.getCurrentBackstageSet();
		
		render(investDetail, backSet, page, type);
	}
	
	/**
	 * 待付款账单详情
	 * @param billInvestId
	 */
	public static void investBillForPay(String billId, int type, int status, int currPage){
		ErrorInfo error = new ErrorInfo();
		int pageSize = Constants.FIVE;
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		v_bill_invest_detail investDetail = BillInvests.queryMyInvestBillDetails(id, error);
		PageBean<t_bill_invests> page = BillInvests.queryMyInvestBillReceivables(investDetail.bid_id, investDetail.user_id, investDetail.invest_id, currPage,pageSize, error);
		BackstageSet backSet = BackstageSet.getCurrentBackstageSet();
		
		render(investDetail, backSet, page, type, status);
	}
	
	/**
	 * 对待付款理财账单付款
	 * @param billInvestId
	 */
	public static void payInvestBill(String investId){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		long id = Security.checkSign(investId, Constants.BILL_ID_SIGN, 3600, error);
		
		if(error.code < 0){
			json.put("error", error);
			renderJSON(json.toString());
			
		}
		
		Bill.investForPayment(id, error);
		
		if(error.code < 0){
			json.put("error", error);
			renderJSON(json.toString());
			
		}
		
		json.put("error", error);
		
		renderJSON(json.toString());
	}

	/**
	 * 借款标详情
	 */
	public static void bidDetail(long bidid, int type, int flag) { 
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = flag;
		bid.id = bidid;
		
		render(bid, type, flag);
	}
	
	//本金垫付
	public static void principalAdvance(int status, String billId, long bidId, int period){
		//国付宝支持线下收款
		if(!Constants.IS_OFFLINERECEIVE) {
			overdueUnpaidBills();
		}
		
		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);

		if (error.code < 0) {
			flash.error(error.msg);

			overdueUnpaidBills();
		}
		
		int ipsStatus = Bill.QueryIPSStatusByID(id);
		
		if(ipsStatus == IPSDealStatus.COMPENSATE_HANDING){  //本金垫付处理中
			flash.error("本金垫付处理中，请勿重复操作");
			
			overdueUnpaidBills();
		}
		
		if(ipsStatus == IPSDealStatus.REPAYMENT_HANDING){  //借款人还款中
			flash.error("借款人还款中，不能本金垫付");
			
			overdueUnpaidBills();
		}
		
		if(ipsStatus == IPSDealStatus.OFFLINEREPAYMENT_HANDING){  //线下收款处理中
			flash.error("线下收款处理中，不能本金垫付");
			
			overdueUnpaidBills();
		}

		if (Constants.IPS_ENABLE) {

			//资金托管(逾期垫付业务)，业务逻辑在回调方法里面
			PaymentProxy.getInstance().advance(error, Constants.PC, bidId, id, CompensateType.COMPENSATE, Supervisor.currSupervisor().getId());
	
			flash.error(error.msg);

			PayableBillManager.overdueUnpaidBills(); //本金垫付

		} else {
			
			Bill bill = new Bill();
			Supervisor supervisor = Supervisor.currSupervisor();
			bill.principalAdvancePayment(supervisor.id, id, error);
		}

		flash.error(error.msg);
		overdueUnpaidBills();
	}

}
