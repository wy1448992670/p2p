package controllers.supervisor.billCollectionManager;

import java.io.File;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.lang.StringUtils;

import constants.Constants;
import controllers.supervisor.SupervisorController;
import business.Bid;
import business.Product;
import business.Supervisor;
import business.User;
import models.t_bids;
import models.v_bid_assigned;
import models.v_bill_department_month_maturity;
import models.v_supervisors;
import models.v_user_loan_info_bad_d;
import models.v_user_loan_info_bill;
import models.v_user_loan_info_bill_d;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.PageBean;
import utils.Security;

/**
 * 
 * 类名:loanUsers
 * 功能:借款会员管理
 */

public class LoanUsers extends SupervisorController {

	//我的会员账单-----借款会员管理
	public static void loanUserManager(){
        
		ErrorInfo error = new ErrorInfo();
		
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		String type = params.get("type");
		String startDate = params.get("startDate");
		String endDate = params.get("endDate");
		String orderType = params.get("orderType");
		String name = params.get("keywords");
		
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_user_loan_info_bill> page = new PageBean<v_user_loan_info_bill>();
		page = User.queryUserInfoBill(supervisor.id, type, startDate, endDate, name, orderType, currPageStr, pageSizeStr, error);
		
		render(page);
	}

	
	//借款标详情
	public static void bidDetail(long bidId, String light, int flag){
			Bid bid = new Bid();
			bid.bidDetail = true;
			bid.upNextFlag = flag;
			bid.id = bidId;
			
			render(bid, light, flag);
	}
	
	//借款标详情
	public static void bidDetailDept(long bidId){
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.id = bidId;
		
		renderTemplate("/supervisor/billCollectionManager/LoanUsers/bidDetail",bid);
	}
	
	//我的会员账单-----借款会员管理--借款标目录
	public static void userBidDetail(String sign,int type,String keywords,String status,int pageNum,int pageSize, String light){
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			renderJSON(error);
		}
		
		Supervisor supervisor = Supervisor.currSupervisor();
		PageBean<t_bids> page = Bid.queryUserInfoBillDetail( pageNum, pageSize,userId, type, supervisor.id, keywords, status, error);
		renderArgs.put("sign", sign);
		renderArgs.put("type", type);
		renderArgs.put("keywords", keywords);
		renderArgs.put("status", status);
		
		render(page, light);
	}
	
	
	/**
	 * 部门账单管理----已分配的借款会员管理
	 */
	public static void deptLoanUserManager(int isExport){
		
        ErrorInfo error = new ErrorInfo();
		
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		String type = params.get("type");
		String startDate = params.get("startDate");
		String endDate = params.get("endDate");
		String orderType = params.get("orderType");
		String name = params.get("keywords");
		
		
		PageBean<v_user_loan_info_bill_d> page = new PageBean<v_user_loan_info_bill_d>();
		page = User.queryUserInfoBillD(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, type, startDate, endDate, name, orderType, currPageStr, pageSizeStr, error);
		
		if(isExport == Constants.IS_EXPORT){
			
			List<v_user_loan_info_bill_d> list = page.page;
			 
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bill = (JSONObject)obj;
				
				String status = "";
				int bad_bid_count = bill.getInt("bad_bid_count");
				int overdue_bill_count = bill.getInt("overdue_bill_count");
				if(bad_bid_count > 0 && overdue_bill_count <= 0){
					status = "坏账";
				}
				if(bad_bid_count <= 0 && overdue_bill_count > 0){
					status = "逾期";
				}
				if(bad_bid_count <= 0 && overdue_bill_count <= 0){
					status = "正常";
				}
				
				String creditLevel = User.queryCreditLevelByImage(bill.getString("credit_level_image_filename"));
				
				bill.put("credit_level_image_filename", creditLevel);
				bill.put("status", status);
			}
			
			File file = ExcelUtils.export("已分配的借款会员",
			arrList,
			new String[] {
			"会员名", "注册时间", "信用等级", "借款标数量", "累计借款总额",
			"投标数量", "累计投标总额", "借款中的借款标数量", "还款中的借款标数量",
			"逾期账单数量", "坏账借款标数量", "账户余额", "账单状态",
			"最后登录时间","客服"},
			new String[] {"name", "register_time", "credit_level_image_filename",
			"bid_count", "bid_amount", "invest_count", "invest_amount",
			"bid_loaning_amount", "bid_repayment_amount",
			"overdue_bill_count", "bad_bid_count", "user_amount", "status",
			"last_login_time", "supervisor_name"});
			   
			renderBinary(file, "已分配的借款会员.xls");
		}
		
		render(page);
	}
	
	/**
	 * 部门账单管理----已分配的借款标管理
	 */
	public static void deptLoanBidManager(int isExport){
		
        ErrorInfo error = new ErrorInfo();
		
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		String type = params.get("type");
		String startDate = params.get("startDate");
		String endDate = params.get("endDate");
		String _orderType = params.get("orderType");
		String orderType = _orderType == null ? "0" : _orderType;
		String name = params.get("keywords");
		
		
		PageBean<v_bid_assigned> page = new PageBean<v_bid_assigned>();
		page = User.queryBidInfoBillD(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, type, startDate, endDate, name, orderType, currPageStr, pageSizeStr, error);
		
		if(isExport == Constants.IS_EXPORT){
			
			List<v_bid_assigned> list = page.page;
			 
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bid = (JSONObject)obj;
				
				String showPeriod = "";
				int period = bid.getInt("period");
				int period_unit = bid.getInt("period_unit");
				if(period_unit == -1){
					showPeriod = period + "[年 ]";
				}else if(period_unit == 1){
					showPeriod = period + "[日]";
				}else{		
					showPeriod = period + "[月]";
				}
				
				String productName = Product.queryProductNameByImage(bid.getString("small_image_filename"));
				String creditLevel = User.queryCreditLevelByImage(bid.getString("credit_level_image_filename"));
				
				bid.put("period", showPeriod);
				bid.put("small_image_filename", productName);
				bid.put("credit_level_image_filename", creditLevel);
				bid.put("apr", String.format("%.1f", bid.getDouble("apr")));
			}
			
			File file = ExcelUtils.export("已分配的借款标管理",
			arrList,
			new String[] {
			"借款标编号", "借款人", "信用等级", "借款标金额", "借款标类型", "年利率",
			"借款期限", "放款时间", "本息合计", "客服"},
			new String[] { "bid_no", "user_name", "credit_level_image_filename",
			"amount", "small_image_filename", "apr", "period",
			"audit_time", "capital_interest_sum", "supervisor_name",});
			   
			renderBinary(file, "已分配的借款标.xls");
		}
		
		render(page);
	}
	
	/**
	 * 部门账单管理账单-----借款会员管理--借款标目录
	 * @param userId
	 * @param keywords
	 * @param status
	 * @param pageNum
	 * @param pageSize
	 */
	public static void deptLoanUserBidDetail(String sign,String keywords,String status,int pageNum,int pageSize,String light){
		
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			renderJSON(error);
		}
		
		PageBean<t_bids> page = Bid.queryDeptUserInfoBillDetail(pageNum, pageSize, userId, keywords, status, error);
		renderArgs.put("sign", sign);
		
		render(page, light);
	}
	
	/**
	 * 查询所有管理员
	 */
	public static void queryAllSupervisors(int currPage, int pageSize, String keyword, String userId, String type, String bidId){
		ErrorInfo error = new ErrorInfo();
		PageBean<v_supervisors> page = new PageBean<v_supervisors>();
		page = Supervisor.queryCustomers(currPage, pageSize, 0, 0, keyword, error);
		
		if(error.code < 0){
			renderJSON(error);
		}
		
		render(page,userId,type,bidId);
	}
	
	/**
	 * 部门账单管理----坏账会员管理
	 */
	public static void deptBadLoanUserManager(int isExport){
		
        ErrorInfo error = new ErrorInfo();
		
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		String type = params.get("type");
		String startDate = params.get("startDate");
		String endDate = params.get("endDate");
		String orderType = params.get("orderType");
		String name = params.get("keywords");
		
		
		PageBean<v_user_loan_info_bad_d> page = new PageBean<v_user_loan_info_bad_d>();
		page = User.queryUserInfoBadD(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, type, startDate, endDate, name, orderType, currPageStr, pageSizeStr, error);
		
		if(isExport == Constants.IS_EXPORT){
			
			List<v_user_loan_info_bad_d> list = page.page;
			
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bill = (JSONObject)obj;
				
				String status = "";
				int bad_bid_count = bill.getInt("bad_bid_count");
				int overdue_bill_count = bill.getInt("overdue_bill_count");
				if(bad_bid_count > 0 && overdue_bill_count <= 0){
					status = "坏账";
				}
				if(bad_bid_count <= 0 && overdue_bill_count > 0){
					status = "逾期";
				}
				if(bad_bid_count <= 0 && overdue_bill_count <= 0){
					status = "正常";
				}
				
				String creditLevel = User.queryCreditLevelByImage(bill.getString("credit_level_image_filename"));
				
				bill.put("credit_level_image_filename", creditLevel);
				bill.put("status", status);
			}
			
			File file = ExcelUtils.export("坏账会员管理",
			arrList,
			new String[] {
			"会员名", "注册时间", "信用等级", "借款标数量", "累计借款总额",
			"投标数量", "累计投标总额", "还款中的借款标数量",
			"逾期账单数量", "坏账借款标数量", "坏账金额", "账户余额",
			"最后登录时间","客服"},
			new String[] {"name", "register_time", "credit_level_image_filename",
			"bid_count", "bid_amount", "invest_count", "invest_amount",
			"bid_loaning_amount", "overdue_bill_count",
			"bad_bid_count", "bid_repayment_amount", "user_amount", 
			"last_login_time", "supervisor_name"});
			   
			renderBinary(file, "坏账会员.xls");
		}
		
		render(page);
	}
	
	/**
	 * 部门账单管理账单-----坏账会员管理--借款标目录
	 * @param userId
	 * @param keywords
	 * @param status
	 * @param pageNum
	 * @param pageSize
	 */
	public static void deptUserBidDetail(String sign,String keywords,String status,int pageNum,int pageSize){
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			renderJSON(error);
		}
		
		PageBean<t_bids> page = Bid.queryDeptUserInfoBillDetail(pageNum, pageSize, userId, keywords, status, error);
		renderArgs.put("sign", sign);
		
		render(page);
	}

}
