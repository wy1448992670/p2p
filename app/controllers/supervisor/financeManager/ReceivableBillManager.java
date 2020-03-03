package controllers.supervisor.financeManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.shove.Convert;

import payment.PaymentProxy;
import play.Logger;
import models.t_debt_bill_invest;
import models.t_debt_transfer;
import models.t_products;
import models.v_bid_repayment;
import models.v_bill_advance;
import models.v_bill_all;
import models.v_bill_detail;
import models.v_bill_has_received;
import models.v_bill_receiving;
import models.v_bill_receiving_overdue;
import models.v_bill_receviable_statistical;
import models.v_bill_repayment_record;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.PageBean;
import utils.Security;
import business.AutoReturnMoney;
import business.BackstageSet;
import business.Bid;
import business.Bill;
import business.Supervisor;
import business.User;
import business.UserBankAccounts;
import constants.Constants;
import constants.IPSConstants.CompensateType;
import constants.IPSConstants.IPSDealStatus;
import constants.IPSConstants.RegisterGuarantorType;
import controllers.supervisor.SupervisorController;
import controllers.supervisor.bidManager.BidPlatformAction;

/**
 * 
 * 类名:ReceivableBillManager 功能:应收账单管理
 */

public class ReceivableBillManager extends SupervisorController {

	/**
	 * 待收款借款账单列表
	 */
	public static void toReceiveBills() {
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		int isExport = Convert.strToInt(params.get("isExport"), 0);
		String rtStart = params.get("rtStart");
		String rtEnd = params.get("rtEnd");
		String tag = params.get("tag");
		String isDefer = params.get("isDefer");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();

		PageBean<v_bill_receiving> page = Bill.queryBillReceivingV2(supervisor.id, yearStr, monthStr, typeStr, key,
				orderType, currPageStr, pageSizeStr, isExport, error, rtStart, rtEnd,tag,isDefer);

		if (page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			List<v_bill_receiving> list = page.page;

			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);

			for (Object obj : arrList) {
				JSONObject bill = (JSONObject) obj;

				bill.put("apr", bill.get("apr") + "%");
				bill.put("overdue_time", bill.get("overdue_time") + "天");

				if (StringUtils.isBlank(bill.getString("supervisor_name"))
						&& StringUtils.isBlank(bill.getString("supervisor_name2"))) {
					bill.put("supervisor_name", "暂无分配");
				} else if (StringUtils.isNotBlank(bill.getString("supervisor_name"))
						|| StringUtils.isNotBlank(bill.getString("supervisor_name2"))) {
					bill.put("supervisor_name", bill.get("supervisor_name").toString() + bill.get("supervisor_name2"));
				} else {
					bill.put("supervisor_name", "暂无分配");
				}

			}

			File file = ExcelUtils.export("待收款借款账单列表", arrList,
					new String[] { "账单编号", "借款人", "真实姓名", "电话", "借款标编号", "借款金额", "年利率", "账单标题", "当前还款(元)", "账单期数",
							"还款时间", "逾期时长", "逾期账单", "客服" },
					new String[] { "bill_no", "name", "reality_name", "mobile", "bid_no", "amount", "apr", "title",
							"repayment_money", "period", "repayment_time", "overdue_time", "overdue_count",
							"supervisor_name" });

			renderBinary(file, "待收款借款账单列表" + ".xls");
		}

		// (待收款借款账单合计)
		double countAmount = Bill.countBillReceivingV2(yearStr, monthStr, typeStr, key, error, rtStart, rtEnd);
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
			// TODO
		}

		render(page, countAmount);

	}

	/**
	 * 逾期账单列表
	 */
	public static void overdueBills() {
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		String tag = params.get("tag");

		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();

		PageBean<v_bill_receiving_overdue> page = Bill.queryBillReceivingOverdue(supervisor.id, yearStr, monthStr,
				typeStr, key, orderType, currPageStr, pageSizeStr, error,tag);

		if (page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(page);
	}

	/**
	 * 已收款借款账单列表
	 */
	public static void receivedBills() {
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		String tag = params.get("tag");
		int isExport = Convert.strToInt(params.get("isExport"), 0);

		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();

		PageBean<v_bill_has_received> page = Bill.queryBillHasReceived(supervisor.id, yearStr, monthStr, typeStr, key,
				orderType, currPageStr, pageSizeStr, isExport, error,tag);
		if (page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			List<v_bill_has_received> list = page.page;

			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);

			for (Object obj : arrList) {
				JSONObject bill = (JSONObject) obj;

				bill.put("apr", bill.get("apr") + "%");
				bill.put("overdue_time", bill.get("overdue_time") + "天");

				if (StringUtils.isBlank(bill.getString("supervisor_name"))
						&& StringUtils.isBlank(bill.getString("supervisor_name2"))) {
					bill.put("supervisor_name", "暂无分配");
				} else if (StringUtils.isNotBlank(bill.getString("supervisor_name"))
						|| StringUtils.isNotBlank(bill.getString("supervisor_name2"))) {
					bill.put("supervisor_name", bill.get("supervisor_name").toString() + bill.get("supervisor_name2"));
				} else {
					bill.put("supervisor_name", "暂无分配");
				}

			}

			File file = ExcelUtils.export("已收款借款账单列表", arrList,
					new String[] { "账单编号", "借款人", "真实姓名", "借款标编号", "借款金额", "年利率", "本期本金", "利息", "服务费", "账单标题",  "账单期数",
							"还款时间","本期账单应还款","实际还款时间", "逾期时长",  "客服" },
					new String[] { "bill_no", "name", "reality_name","bid_no", "amount", "apr", "repayment_corpus", "repayment_interest", "service_amount", "title",
							"period","repayment_time", "current_pay_amount", "real_repayment_time","overdue_time", 
							"supervisor_name" });

			renderBinary(file, "已收款借款账单列表" + ".xls");
		}
		render(page);
	}

	/**
	 * 已完成借款标列表
	 */
	public static void repaymentList() {
		ErrorInfo error = new ErrorInfo();

		PageBean<v_bid_repayment> pageBean = new PageBean<v_bid_repayment>();
		pageBean.page = Bid.queryBidRepayment(0, pageBean, 0, error, BidPlatformAction.getParameter(pageBean, null));

		if (null == pageBean.page)
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);

		render(pageBean);
	}

	/**
	 * 应收款借款账单统计表
	 */
	public static void receivableBills(int isExport) {
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");

		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();

		PageBean<v_bill_receviable_statistical> page = Bill.queryBillReceivedStatical(
				isExport == Constants.IS_EXPORT ? Constants.NO_PAGE : 0, supervisor.id, yearStr, monthStr, orderType,
				currPageStr, pageSizeStr, error);

		if (page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		if (isExport == Constants.IS_EXPORT) {

			List<v_bill_receviable_statistical> list = page.page;

			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);

			for (Object obj : arrList) {
				JSONObject bill = (JSONObject) obj;

				bill.put("bills_timely_completion_rate",
						String.format("%.1f", bill.getDouble("bills_timely_completion_rate")) + "%");
				bill.put("bills_overdue_rate", String.format("%.1f", bill.getDouble("bills_overdue_rate")) + "%");
				bill.put("bills_completed_rate", String.format("%.1f", bill.getDouble("bills_completed_rate")) + "%");
				bill.put("uncollected_rate", String.format("%.1f", bill.getDouble("uncollected_rate")) + "%");
			}

			File file = ExcelUtils.export("应收款借款账单统计表", arrList,
					new String[] { "年", "月", "应收账单数", "应收金额", "关联借款标总额", "实际已收账单数", "实际已收金额", "应收账单按时完成率", "逾期账单数",
							"逾期占比", "总应收完成率", "未收逾期数量", "未收金额", "未收逾期占比" },
					new String[] { "year", "month", "bill_accounts", "amounts_receivable", "bids_amount",
							"bills_received", "amount_received", "bills_timely_completion_rate", "overdue_counts",
							"bills_overdue_rate", "bills_completed_rate", "bills_overdue_noreceive",
							"uncollected_amount", "uncollected_rate" });

			renderBinary(file, "应收款借款账单统计表.xls");
		}

		render(page);
	}

	/**
	 * 账单详情
	 */
	public static void billDetail(String billId, int type) {
		int currPage = 1;
		String curPage = params.get("currPage");

		if (curPage != null) {
			currPage = Integer.parseInt(curPage);
		}

		ErrorInfo error = new ErrorInfo();

		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);

		v_bill_detail billDetail = Bill.queryBillDetails(id, error);
		PageBean<v_bill_repayment_record> page = Bill.queryBillReceivables(billDetail.bid_id, currPage, 0, error);
		BackstageSet backSet = BackstageSet.getCurrentBackstageSet();

		render(billDetail, page, backSet, type);
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

	// 线下收款
	//type 1:逾期账单列表调用,2:代收款账单列表调用
	public static void offlineReceive(String billId, int type) {
		// 国付宝支持线下收款
		if (!Constants.IS_OFFLINERECEIVE) {
			overdueBills();
		}

		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);

		if (error.code < 0) {
			flash.error(error.msg);
			overdueBills();
		}

		Bill bill = new Bill();
		bill.id = id;

		if (bill.ipsStatus == IPSDealStatus.OFFLINEREPAYMENT_HANDING) { // 线下收款处理中
			flash.error("线下收款处理中，请勿重复操作！");

			if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
				toReceiveBills();
			} else {
				overdueBills();
			}
		}

		if (bill.ipsStatus == IPSDealStatus.REPAYMENT_HANDING) { // 借款人还款中
			flash.error("借款人还款中，不能线下收款");

			if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
				toReceiveBills();
			} else {
				overdueBills();
			}
		}

		if (bill.ipsStatus == IPSDealStatus.COMPENSATE_HANDING) { // 本金垫付处理中
			flash.error("本金垫付处理中，不能线下收款");

			if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
				toReceiveBills();
			} else {
				overdueBills();
			}
		}

		// 本金垫付后线下收款
		if (bill.status == Constants.ADVANCE_PRINCIIPAL_REPAYMENT) {
			bill.offlineReceive(error);

			flash.error(error.msg);

			if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
				toReceiveBills();
			} else {
				overdueBills();
			}
		}

		if (Constants.IPS_ENABLE) {

			// 线下收款
			PaymentProxy.getInstance().offlineRepayment(error, Constants.PC, id, CompensateType.OFFLINE_REPAYMENT,
					Supervisor.currSupervisor().getId());

			flash.error(error.msg);

			if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
				toReceiveBills();
			} else {
				overdueBills();
			}
			return;

		} else {
			bill.offlineCollection(Supervisor.currSupervisor().id, error);
		}

		flash.error(error.msg);

		if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
			toReceiveBills();
		} else {
			overdueBills();
		}
	}
	/**
	 *  全部账单
	 */
	public static void allBills() {
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		String tag = params.get("tag");
		int isExport = Convert.strToInt(params.get("isExport"), 0);
 
		String repayStartDate =  params.get("repayStartDate");
		String repayEndDate =  params.get("repayEndDate");
		String releaseStartDate =  params.get("releaseStartDate");
		String releaseEndDate =  params.get("releaseEndDate");
		String realRepayStartDate =  params.get("realRepayStartDate");
		String realRepayEndDate =  params.get("realRepayEndDate");
		
		String status = params.get("status");
		String overdue_status = params.get("overdue_status");
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();

		PageBean<v_bill_all> page = Bill.queryAllBill(supervisor.id, yearStr, monthStr, typeStr, key,
				orderType, currPageStr, pageSizeStr, isExport, error,tag,
				repayStartDate,repayEndDate,releaseStartDate,releaseEndDate,
				realRepayStartDate,realRepayEndDate,status,overdue_status);
 
		if (page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			List<v_bill_all> list = page.page;

			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);

			for (Object obj : arrList) {
				JSONObject bill = (JSONObject) obj;

				bill.put("apr", bill.get("apr") + "%");
				bill.put("overdue_time", bill.get("overdue_time") + "天");

				if (StringUtils.isBlank(bill.getString("supervisor_name"))
						&& StringUtils.isBlank(bill.getString("supervisor_name2"))) {
					bill.put("supervisor_name", "暂无分配");
				} else if (StringUtils.isNotBlank(bill.getString("supervisor_name"))
						|| StringUtils.isNotBlank(bill.getString("supervisor_name2"))) {
					bill.put("supervisor_name", bill.get("supervisor_name").toString() + bill.get("supervisor_name2"));
				} else {
					bill.put("supervisor_name", "暂无分配");
				}

			}

			File file = ExcelUtils.export("应收账单明细", arrList,
					new String[] { "账单编号", "项目类型","借款人", "真实姓名", "借款期限","借款标编号", "借款金额", "年利率","本期本金","利息","服务费", "账单标题",  "账单期数",
							"放款时间","还款时间","本期账单应还款","逾期时长", "实际还款时间","客服" },
					new String[] { "bill_no","tag", "name", "reality_name","period_","bid_no", "amount", "apr","repayment_corpus","repayment_interest",
							"service_amount","title","period", "release_time","repayment_time", "repayment_money","overdue_time", "real_repayment_time",
							"supervisor_name" });

			renderBinary(file, "应收账单明细" + ".xls");
		}
		render(page);
	}
	
	//公司垫付,未收垫付
	//type 1:逾期账单列表调用,2:代收款账单列表调用
	public static void paymentOnCompany(String billId, int type) {
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		if(error.code < 0){
			flash.error(error.msg);
			if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
				toReceiveBills();
			} else {
				overdueBills();
			}
		}
		
		Bill bill = new Bill();
		bill.setId(id);
		
		if(bill.ipsStatus == IPSDealStatus.REPAYMENT_HANDING){  //还款处理中
			flash.error("还款处理中，请勿重复操作！");
			
			if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
				toReceiveBills();
			} else {
				overdueBills();
			}
		}
		
		if(bill.ipsStatus == IPSDealStatus.OFFLINEREPAYMENT_HANDING){  //线下收款处理中
			flash.error("线下收款处理中，不能还款！");
			
			if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
				toReceiveBills();
			} else {
				overdueBills();
			}
		}
		
		if(bill.ipsStatus == IPSDealStatus.COMPENSATE_HANDING){  //本金垫付处理中
			flash.error("本金垫付处理中，不能还款！");
			
			if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
				toReceiveBills();
			} else {
				overdueBills();
			}
		}
		
		/* 2014-12-29 限制还款需要从第一期逐步开始还款 */
		if(bill.checkPeriod(bill.bidId, bill.periods) > 0){
			flash.error("请您从第一期逐次还款!");
			
			if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
				toReceiveBills();
			} else {
				overdueBills();
			}
		}
		
		/*本金垫付还款*/
		if (Constants.IPS_ENABLE && bill.status == Constants.ADVANCE_PRINCIIPAL_REPAYMENT) {
			
			//垫付还款，本地业务逻辑，在垫付还款回调方法中
			//PaymentProxy.getInstance().advanceRepayment(error, Constants.PC, bill, user.id);
			error.setWrongMsg("垫付不支持资金托管");
			flash.error(error.msg);
			
			if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
				toReceiveBills();
			} else {
				overdueBills();
			}
			
			return;
		}
		

		if(Constants.IPS_ENABLE) {		
			
			//资金托管还款接口调用，本地业务逻辑处理，在托管回调方法中
			//PaymentProxy.getInstance().repayment(error, Constants.PC, bill, user.id);
			error.setWrongMsg("垫付不支持资金托管");
			flash.error(error.msg);
			if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
				toReceiveBills();
			} else {
				overdueBills();
			}
			
			return;
		}	
		//公司垫付
		t_debt_transfer transfer = t_debt_transfer.find(" bid_id = ? ", bill.bidId).first();
		List<t_debt_bill_invest> debtBill = new ArrayList<>();
		if(transfer != null ){
			debtBill = t_debt_bill_invest.find(" debt_id = ? ", transfer.id).fetch();
			Logger.info("该标的有债权转让，执行债权转让还款逻辑！ 账单信息: " + debtBill);
			bill.repaymentV1(bill.user.id, true,error);
		}else {
			//普通网关模式，还款业务逻辑
			bill.repayment(bill.user.id,true, error);
		}
		
		if (error.code == Constants.BALANCE_NOT_ENOUGH){
			flash.put("notEnough", -999);
		}if(error.code<0) {
			flash.error(error.msg);
		}else{
			flash.error("公司垫付成功");
		}
		
		if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
			toReceiveBills();
		} else {
			overdueBills();
		}
	}
	
	/**
	 * 账单代扣
	 */
	//type 1:逾期账单列表调用,2:代收款账单列表调用
	public static void autoReturnMoney(String billId, int type) {
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		if(error.code < 0){
			flash.error(error.msg);
			if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
				toReceiveBills();
			} else {
				overdueBills();
			}
		}
		boolean isRepaymentSuccess=false;
		try {
			isRepaymentSuccess=AutoReturnMoney.autoReturnMoneyForBill(id);
			if(isRepaymentSuccess){
				flash.error("主动账单代扣成功");
			}else{
				flash.error("主动账单代扣失败");
			}
		} catch (Exception e) {
			Logger.error("billId:"+id+" 主动账单代扣异常");
			e.printStackTrace();
			flash.error(e.getMessage());
		}
		if (type == RegisterGuarantorType.OFFLINE_REPAYMENT) {
			toReceiveBills();
		} else {
			overdueBills();
		}
	}
	
	/**
	 * 垫付后账单代扣,偿还垫付金
	 */
	//type 1:逾期账单列表调用,2:代收款账单列表调用
	public static void autoReturnMoneyAfterPaymentOnCompany(String billId) {
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		
		if(error.code < 0){
			flash.error(error.msg);
			advanceBills();
		}
		boolean isRepaymentSuccess=false;
		try {
			isRepaymentSuccess=AutoReturnMoney.autoReturnMoneyForBill(id);
			if(isRepaymentSuccess){
				flash.error("垫付后账单代扣成功");
			}else{
				flash.error("垫付后账单代扣失败");
			}
		} catch (Exception e) {
			Logger.error("billId:"+id+" 垫付后账单代扣异常");
			e.printStackTrace();
			flash.error(e.getMessage());
		}
		advanceBills();
	}
	
	/**
	 * 垫付后线下还款,偿还垫付金
	 */
	//type 1:逾期账单列表调用,2:代收款账单列表调用
	public static void offlineReceiveAfterPaymentOnCompany(String billId) {
		// 国付宝支持线下收款
		if (!Constants.IS_OFFLINERECEIVE) {
			overdueBills();
		}

		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);

		if (error.code < 0) {
			flash.error(error.msg);
			overdueBills();
		}

		Bill bill = new Bill();
		bill.id = id;

		if (bill.ipsStatus == IPSDealStatus.OFFLINEREPAYMENT_HANDING) { // 线下收款处理中
			flash.error("线下收款处理中，请勿重复操作！");
			advanceBills();
		}

		if (bill.ipsStatus == IPSDealStatus.REPAYMENT_HANDING) { // 借款人还款中
			flash.error("借款人还款中，不能线下收款");
			advanceBills();
		}

		if (bill.ipsStatus == IPSDealStatus.COMPENSATE_HANDING) { // 本金垫付处理中
			flash.error("本金垫付处理中，不能线下收款");
			advanceBills();
		}

		// 本金垫付后线下收款
		if (bill.status == Constants.ADVANCE_PRINCIIPAL_REPAYMENT) {
			//bill.offlineReceive(error);
			flash.error("本金垫付账单，不能执行公司垫付后还款");
			advanceBills();
		}

		if (Constants.IPS_ENABLE) {
			// 线下收款
			//PaymentProxy.getInstance().offlineRepayment(error, Constants.PC, id, CompensateType.OFFLINE_REPAYMENT,Supervisor.currSupervisor().getId());
			//flash.error(error.msg);
			flash.error("资金托管模式，不能执行公司垫付后还款");
			advanceBills();
			return;
		}
		
		bill.offlineReceiveAfterPaymentOnCompany(Supervisor.currSupervisor().id, error);
		flash.error(error.msg);
		advanceBills();
	}
	
	/**
	 * 垫付账单列表
	 */
	public static void advanceBills() {
		String yearStr = params.get("yearStr");
		String monthStr = params.get("monthStr");
		String typeStr = params.get("typeStr");
		String key = params.get("key");
		String orderType = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		int isExport = Convert.strToInt(params.get("isExport"), 0);
		String rtStart = params.get("rtStart");
		String rtEnd = params.get("rtEnd");
		String tag = params.get("tag");
		String advanceStatusStr = params.get("advanceStatus");
		/*
		 * Integer advanceStatus = null; if(advanceStatusStr != null) { advanceStatus =
		 * Integer.parseInt(advanceStatusStr); }
		 */
		
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();

		PageBean<v_bill_advance> page = Bill.queryBillAdvance(supervisor.id, yearStr, monthStr, typeStr, key,
				orderType, currPageStr, pageSizeStr, isExport, error, rtStart, rtEnd,tag,advanceStatusStr);

		if (page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			List<v_bill_advance> list = page.page;

			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);

			for (Object obj : arrList) {
				JSONObject bill = (JSONObject) obj;

				bill.put("apr", bill.get("apr") + "%");
				bill.put("overdue_time", bill.get("overdue_time") + "天");

				if (StringUtils.isBlank(bill.getString("supervisor_name"))
						&& StringUtils.isBlank(bill.getString("supervisor_name2"))) {
					bill.put("supervisor_name", "暂无分配");
				} else if (StringUtils.isNotBlank(bill.getString("supervisor_name"))
						|| StringUtils.isNotBlank(bill.getString("supervisor_name2"))) {
					bill.put("supervisor_name", bill.get("supervisor_name").toString() + bill.get("supervisor_name2"));
				} else {
					bill.put("supervisor_name", "暂无分配");
				}

			}

			File file = ExcelUtils.export("垫付账单列表", arrList,
					new String[] { "账单编号", "借款人", "真实姓名", "借款电话", "借款标编号", "借款金额", "年利率", "本期本金", "利息", "服务费", "账单标题", "当前还款(元)", "账单期数",
							"还款时间", "逾期时长", "逾期账单", "客服" },
					new String[] { "bill_no", "name", "reality_name", "mobile", "bid_no", "amount", "apr", "repayment_corpus", "repayment_interest", "service_amount", "title",
							"repayment_money", "period", "repayment_time", "overdue_time", "overdue_count",
							"supervisor_name" });

			renderBinary(file, "垫付账单列表" + ".xls");
		}

		render(page);

	}
	
}
