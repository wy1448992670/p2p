package controllers.supervisor.bidManager;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.t_bills;
import models.t_user_audit_items;
import models.t_user_report_users;
import models.v_bid_auditing;
import models.v_bid_bad;
import models.v_bid_fundraiseing;
import models.v_bid_not_through;
import models.v_bid_overdue;
import models.v_bid_repayment;
import models.v_bid_repaymenting;
import models.v_bill_detail;
import models.v_bill_loan;
import models.v_bill_repayment_record;
import models.v_invest_records;
import models.v_user_audit_items;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;

import payment.PaymentProxy;
import play.Logger;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JPAUtil;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;
import business.Bid;
import business.BidImages;
import business.BidQuestions;
import business.Bill;
import business.Invest;
import business.Product;
import business.ProductAuditItem;
import business.StationLetter;
import business.Supervisor;
import business.User;
import business.UserAuditItem;
import constants.Constants;
import constants.IPSConstants;
import constants.IPSConstants.IPSDealStatus;
import controllers.supervisor.SupervisorController;
import controllers.supervisor.financeManager.LoanManager;

/**
 * 平台借款标 Action
 *
 * @author bsr
 * @version 6.0
 * @created 2014-4-25 上午08:43:32
 */
public class BidPlatformAction extends SupervisorController {

	/**
	 * 获取 参数值
	 * @param pageBean 当前模板PageBean
	 * @return String [] 参数值
	 */
	public static String [] getParameter(PageBean pageBean, String userId){
		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数
		String condition = params.get("condition"); // 条件
		String keyword = params.get("keyword"); // 关键词
		String startDate = params.get("startDate"); // 开始时间
		String endDate = params.get("endDate"); // 结束时间
		String orderIndex = params.get("orderIndex"); // 排序索引
		String orderStatus = params.get("orderStatus"); // 升降标示

		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;

		/* ""/null:标示非用户ID查询  */
		return new String[]{userId, condition, keyword, startDate, endDate, orderIndex, orderStatus};
	}

	/**
	 * 审核中的借款标列表
	 */
	public static void auditingList() {
		ErrorInfo error = new ErrorInfo();
		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数

		PageBean<v_bid_auditing> pageBean = new PageBean<v_bid_auditing>();

		Map<String, Object> map = getSearchCondition();

		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;

		pageBean.page = Bid.queryBidAuditing(pageBean, error, map);
		pageBean.conditions = map;
		render(pageBean);
	}

	/**
	 * 查询条件放进map中
	 * @return
	 */
	public static Map<String, Object> getSearchCondition(){

		String condition = params.get("condition"); // 条件
		String keyword = params.get("keyword"); // 关键词
		String startDate = params.get("startDate"); // 开始时间
		String endDate = params.get("endDate"); // 结束时间
		String orderIndex = params.get("orderIndex"); // 排序索引
		String orderStatus = params.get("orderStatus"); // 升降标示


		Map<String, Object> map = new HashMap<String,Object>();
		map.put("condition", condition);
		map.put("keyword", keyword);
		map.put("startDate", startDate);
		map.put("endDate", endDate);
		map.put("orderIndex", StringUtils.isEmpty(orderIndex) ? 0 : orderIndex);
		map.put("orderStatus", StringUtils.isEmpty(orderStatus) ? 1 :orderStatus);

		return map;
	}

	/**
	 *借款中的借款标列表
	 */
	public static void fundraiseingList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_fundraiseing> pageBean = new PageBean<v_bid_fundraiseing>();
		pageBean.page = Bid.queryBidFundraiseing(pageBean, Constants.V_FUNDRAISEING, error, getParameter(pageBean, null));

		render(pageBean);
	}
	/**
	 *借款中的借款标列表
	 */
	public static void ratesList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_fundraiseing> pageBean = new PageBean<v_bid_fundraiseing>();
		pageBean.page = Bid.queryBidFundraiseing(pageBean, Constants.V_FUNDRAISEING, error, getParameter(pageBean, null));

		render(pageBean);
	}

	/**
	 *满标待放款
	 */
	public static void fullList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_fundraiseing> pageBean = new PageBean<v_bid_fundraiseing>();
		pageBean.page = Bid.queryBidFundraiseing(pageBean, Constants.V_FULL, error, getParameter(pageBean, null));

		render(pageBean);
	}

	/**
	 *还款中的借款标列表
	 */
	public static void repaymentingList(int isExport) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_repaymenting> pageBean = new PageBean<v_bid_repaymenting>();
		pageBean.page = Bid.queryBidRepaymenting(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, pageBean, 0, error, getParameter(pageBean, null));

		if(isExport == Constants.IS_EXPORT){

			List<v_bid_repaymenting> list = pageBean.page;

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

			File file = ExcelUtils.export("还款中的借款标列表",
			arrList,
			new String[] {
			"编号", "标题", "借款人", "信用等级", "借款标类型", "借款标金额[￥]", "年利率",
			"借款期限", "放款时间", "本息合计", "还款期限", "还款方式", "已还期数",
			"逾期账单"},
			new String[] { "bid_no", "title", "user_name",
			"credit_level_image_filename", "small_image_filename", "amount", "apr",
			"period", "audit_time", "capital_interest_sum",
			"period", "repayment_type_name", "repayment_count",
			"overdue_count"});

			renderBinary(file, "还款中的借款标列表.xls");
		}

		render(pageBean);
	}

	/**
	 *逾期的借款标
	 */
	public static void overdueList(int isExport) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_overdue> pageBean = new PageBean<v_bid_overdue>();
		pageBean.page = Bid.queryBidOverdue(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, pageBean, error, getParameter(pageBean, null));

		if(isExport == Constants.IS_EXPORT){

			List<v_bid_overdue> list = pageBean.page;

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

			File file = ExcelUtils.export("逾期的借款标",
			arrList,
			new String[] {
			"编号", "标题", "借款人", "信用等级", "借款标类型", "借款标金额[￥]", "年利率",
			"借款期限", "放款时间", "本息合计", "已还期数",
			"逾期账单数量", "开始逾期时间"},
			new String[] { "bid_no", "title", "user_name",
			"credit_level_image_filename", "small_image_filename", "amount", "apr",
			"period", "audit_time", "capital_interest_sum",
			"repayment_count", "overdue_count", "mark_overdue_time"});

			renderBinary(file, "逾期的借款标.xls");
		}

		render(pageBean);
	}

	/**
	 *已完成的借款标列表的搜索
	 */
	public static void repaymentList(int isExport) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_repayment> pageBean = new PageBean<v_bid_repayment>();
		pageBean.page = Bid.queryBidRepayment(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, pageBean, 0, error, getParameter(pageBean, null));

		if(isExport == Constants.IS_EXPORT){

			List<v_bid_repayment> list = pageBean.page;

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

			File file = ExcelUtils.export("已完成的借款标列表",
			arrList,
			new String[] {
			"编号", "标题", "借款人", "信用等级", "借款标类型", "借款标金额[￥]", "年利率",
			"借款期限", "放款时间", "已还期数",
			"最后还款时间", "逾期账单数量"},
			new String[] { "bid_no", "title", "user_name",
			"credit_level_image_filename", "small_image_filename", "amount", "apr",
			"period", "audit_time",
			"repayment_count", "last_repay_time", "overdue_count"});

			renderBinary(file, "已完成的借款标列表.xls");
		}



		render(pageBean);
	}

	/**
	 *未通过的借标列表款
	 */
	public static void notThroughList(int isExport) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_not_through> pageBean = new PageBean<v_bid_not_through>();
		pageBean.page = Bid.queryBidNotThrough(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, pageBean, error, getParameter(pageBean, null));

		if(isExport == Constants.IS_EXPORT){

			List<v_bid_not_through> list = pageBean.page;

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

				DecimalFormat df = new DecimalFormat("#0.0");
				double percent = 0.0;
				int productItem = bid.getInt("product_item_count");
				int userItem = bid.getInt("user_item_count_true");
				if(productItem == 0 || userItem / productItem >= 1){
					percent = 100.0;
				}else{
					percent = (userItem * 100.0 ) / productItem;
				}
				String auditStatus = df.format(percent) + "%";

				String strStatus = "";
				int status = bid.getInt("status");
				switch (status) {
				case -1:
					strStatus = "审核不通过";
					break;
				case -2:
					strStatus = "借款中不通过";
					break;
				case -3:
					strStatus = "放款不通过";
					break;
				case -4:
					strStatus = "流标";
					break;
				case -5:
					strStatus = "撤销";
					break;
				case -10:
					strStatus = "未验证";
					break;
				default:
					break;
				}

				bid.put("period", showPeriod);
				bid.put("small_image_filename", productName);
				bid.put("credit_level_image_filename", creditLevel);
				bid.put("audit_status", auditStatus);  //审核进度
				bid.put("status", strStatus);
				bid.put("apr", String.format("%.1f", bid.getDouble("apr")));
			}

			File file = ExcelUtils.export("未通过的借标列表",
			arrList,
			new String[] {
			"编号", "标题", "借款人", "信用等级", "借款标类型", "借款标金额[￥]", "年利率",
			"借款期限", "申请时间", "失败时间", "须审核科目", "已提交科目", "审核进度",
			"当前状态"},
			new String[] { "bid_no", "title", "user_name",
			"credit_level_image_filename", "small_image_filename", "amount", "apr",
			"period", "time","audit_time", "product_item_count",
			"user_item_count_true", "audit_status", "status"});

			renderBinary(file, "未通过的借标列表.xls");
		}

		render(pageBean);
	}

	/**
	 *坏账借款标列表
	 */
	public static void badList(int isExport) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_bad> pageBean = new PageBean<v_bid_bad>();
		pageBean.page = Bid.queryBidBad(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, pageBean, 0, error, getParameter(pageBean, null));

		if(isExport == Constants.IS_EXPORT){

			List<v_bid_bad> list = pageBean.page;

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

			File file = ExcelUtils.export("坏账借款标列表",
			arrList,
			new String[] {
			"编号", "标题", "借款人", "信用等级", "借款标类型", "借款标金额[￥]", "年利率",
			"借款期限", "放款时间", "已还期数",
			"最后还款时间", "逾期账单数", "逾期时长", "坏账操作时间"},
			new String[] { "bid_no", "title", "user_name",
			"credit_level_image_filename", "small_image_filename", "amount", "apr",
			"period", "audit_time","repayment_count",
			"last_repay_time", "overdue_count", "overdue_length", "mark_bad_time"});

			renderBinary(file, "坏账借款标列表.xls");
		}

		render(pageBean);
	}

	/**
	 * 审核中的借款标详情
	 */
	public static void auditingDetail(long bidId){

		if(0 == bidId) render();

		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = Constants.BID_SHZ;
		bid.id = bidId;

		if(bid.ipsStatus == IPSDealStatus.BID_END_HANDING){  //标的撤销中，不能进行审核操作
			flash.error("标的撤销中，不能进行审核操作");

			auditingList();
		}

		render(bid);
	}

	/**
	 * 借款中的借款标详情
	 */
	public static void fundraiseingDetail(long bidId){

		if(0 == bidId) render();

		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = Constants.BID_JKZ;
		bid.id = bidId;
		List<BidImages> bidImagesList=new  ArrayList<>();
		try {
			bidImagesList=BidImages.getBidImagesByBidId(bidId);
		} catch (Exception e) {
			Logger.info("查询标关联图片信息出错");
			e.printStackTrace();
		}
		if(bid.ipsStatus == IPSDealStatus.BID_END_HANDING){  //标的撤销中，不能进行审核操作
			flash.error("标的撤销中，不能进行审核操作");

			fundraiseingList();
		}
		render(bid,bidImagesList);
	}

	/**
	 * 满标的借款标详情
	 */
	public static void fullDetail(long bidId){

		if(0 == bidId) render();

		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = Constants.BID_MBZ;
		bid.id = bidId;

		if(bid.ipsStatus == IPSDealStatus.BID_END_HANDING){  //标的撤销中，不能进行审核操作
			flash.error("标的撤销中，不能进行审核操作");

			fullList();
		}

		render(bid);
	}

	/**
	 * 借款成功(还款中的、已完成的、逾期的、坏账的借款标详情)的标详情
	 */

	public static void loanSucceedDetail(long bidId, int type, int falg){
		if(0 == bidId) render();

		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = falg;
		bid.id = bidId;

		render(bid, type, falg);
	}

	/**
	 * 借款失败,初核不通过、借款中不通过、流标、撤销、放款审核不通过详情
	 */
	public static void notThroughDetail(long bidId){

		if(0 == bidId) render();

		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = Constants.BID_SBZ;
		bid.id = bidId;

		render(bid);
	}

	/*public static void userItemsList(int currPage, String signUserId, long productId, int status) {
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if(userId < 1)
			renderText(error.msg);

		PageBean<v_user_audit_items> pageBean = UserAuditItem.queryUserAuditItem(currPage + "", null, userId, error, null, null, null, productId + "", null);
		Product product = new Product();
		product.bidDetail = true;
		product.id = productId;

		render(pageBean, product, status);
	}*/

	/**
	 * 资料列表
	 */
	public static void userItemsList(String signUserId, long productId, int status, String mark) {
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if(userId < 1)
			renderText(error.msg);
		System.out.println("super:"+userId+"-----"+signUserId);
		List<v_user_audit_items> items = UserAuditItem.queryUserAuditItem(userId, mark, error);
		System.out.println(JSON.toJSONString(items, true));
		List<ProductAuditItem> requiredAuditItem = ProductAuditItem.queryAuditByProductMark(mark, false, Constants.NEED_AUDIT);

		render(items, requiredAuditItem, status);
	}

	/**
	 * 查询借款标的所有提问记录异步分页方法
	 */
	public static void bidQuestion(int currPage, long bidId){

		if(0 == bidId) render();

		ErrorInfo error = new ErrorInfo();
		PageBean<BidQuestions> pageBean = BidQuestions.queryQuestion(currPage, 5, bidId, "", Constants.SEARCH_ALL, -1, error);

		render(pageBean, error);
	}


	/**
	 * 删除提问
	 * @param ids
	 */
	public static void deleteBidQuestion(String ids){
		ErrorInfo error = new ErrorInfo();

		if(null == ids || ids.length() == 0)
			renderText("数据有误!");

		String arr[] = ids.split(",");

		for (String str : arr) {
			BidQuestions.delete(Long.parseLong(str), error);

			if (error.code < 0)
				break;
		}

		renderText(error.msg);
	}


	/**
	 * 投标记录
	 */
	public static void bidRecord(int currPage, long bidId) {

		if(0 == bidId) render();

		ErrorInfo error = new ErrorInfo();
		PageBean<v_invest_records> pageBean = new PageBean<v_invest_records>();
		pageBean.currPage = currPage;
		pageBean.page = Invest.bidInvestRecord(pageBean, bidId, error);

		render(pageBean);
	}

	/**
	 * 历史记录
	 */
	public static void historyDetail(Date time, String signUserId) {
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if(userId < 1)
			renderText(error.msg);

		Map<String, String> historySituationMap = User.historySituation(userId,error);// 借款者历史记录情况

		render(time, historySituationMap);
	}

	/**
	 * 举报记录
	 */
	public static void reportRecord(int currPage, String signUserId){
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if(userId < 1)
			renderText(error.msg);

		PageBean<t_user_report_users> pageBean = new PageBean<t_user_report_users>();
		pageBean.currPage = currPage;
		pageBean.page = User.queryBidRecordByUser(pageBean, userId, error);

		render(pageBean);
	}

	/**
	 * 还款情况
	 */
	public static void repaymentSituation(int currPage, long bidId){

		if(0 == bidId) render();

		ErrorInfo error = new ErrorInfo();
		PageBean<v_bill_loan> pageBean = new PageBean<v_bill_loan>();
		pageBean.currPage = currPage;
		pageBean.page = Bill.queryMyLoanBills(pageBean, -1, bidId, error);

		render(pageBean);
	}
	/**
	 * 秒还标后台还款
	 * @param bidId
	 */
	public static void serondPayment(long bidId){

		ErrorInfo error = new ErrorInfo();
		t_bills t_bill = (t_bills) t_bills.find(" bid_id = ?", bidId).fetch().get(0);
		Bill bill = new Bill();
		bill.setId(t_bill.id);

		//资金托管秒还标还款
		PaymentProxy.getInstance().autoRepayment(error, Constants.PC, bill);
		flash.error(error.msg);
		LoanManager.alreadyReleaseList(0);
	}
	/**
	 * 还款情况详情
	 */
	public static void repaymentSituationDetail(int currPage, long billId) {
		ErrorInfo error = new ErrorInfo();

		User user = User.currUser();

		v_bill_detail billDetail = Bill.queryBillDetails(billId, user.id, error);
		PageBean<v_bill_repayment_record> page = Bill.queryBillReceivables(billDetail.bid_id, currPage, 0, error);

		render(billDetail, page);
	}

	/**
	 * 管理员给用户发送站内信
	 */
	public static void sendMessages(String signUserId, String title, String content) {
		/* 解密userId */
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if(userId < 1)
			renderText(error.msg);

		if (StringUtils.isBlank(title) || StringUtils.isBlank(content) || content.length() > 1000)
			renderText("数据有误!");

		StationLetter letter = new StationLetter();
		letter.senderSupervisorId = Supervisor.currSupervisor().id;
		letter.receiverUserId = userId;
		letter.title = title;
		letter.content = content;
		letter.sendToUserBySupervisor(error);

		renderText(error.msg);
	}

	/**
	 * 设置优质标
	 */
	public static void siteQuality(long bidId, boolean status) {

		if(0 == bidId) renderText("设置出错!");

		ErrorInfo error = new ErrorInfo();
		Bid.editQuality(bidId, status, error);

		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json);
	}

	/**
	 * 设置"火"标
	 */
	public static void siteHot(long bidId, boolean status) {

		if(0 == bidId) renderText("设置出错!");

		ErrorInfo error = new ErrorInfo();
		Bid.editHot(bidId, status, error);

		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json);
	}

	/**
	 *  审核中->提前借款
	 */
	public static void auditToadvanceLoan(String sign) {
		/* 解密BidId */
		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);

		if(bidId < 1){
			flash.error(error.msg);

			auditingList();
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人

		bid.auditToadvanceLoan(error);
		flash.error(error.msg);

		auditingList();
	}

	/**
	 *  审核中->借款中
	 */
	public static void auditToFundraise(String sign) {
		checkAuthenticity();

		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);

		if(bidId < 1){
			flash.error(error.msg);

			auditingDetail(bidId);
		}

		String suggest = params.get("suggest");

		if(StringUtils.isBlank(suggest)){
			flash.error("数据有误!");

			auditingDetail(bidId);
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.auditSuggest = suggest; // 审核意见
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人

		bid.auditToFundraise(error);
		flash.error(error.msg);

		//审核成功则跳到列表页面，失败停在当前页面
		if (error.code == 1){
			auditingList();
		}else{
			auditingDetail(bidId);
		}

	}

	/**
	 * 提前借款->借款中
	 */
	public static void advanceLoanToFundraise(String sign) {
		checkAuthenticity();

		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);

		if(bidId < 1){
			flash.error(error.msg);

			auditingList();
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;

		String suggest = params.get("suggest");

/*		if(StringUtils.isBlank(suggest)){
			flash.error("数据有误!"); 审核失败,请确定资料是否提交完毕或通过审核!

			if(bid.hasInvestedAmount == bid.amount)
				fullList();

			fundraiseingList();
		}*/

	//	bid.auditSuggest = suggest; // 审核意见
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人

		bid.advanceLoanToFundraise(error);
		flash.error(error.msg);

		if(bid.hasInvestedAmount == bid.amount)
			fullList();

		fundraiseingList();
	}

	/**
	 * 审核中->审核不通过
	 */
	public static void auditToNotThrough(String sign) {
		checkAuthenticity();

		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);

		if(bidId < 1){
			flash.error(error.msg);

			auditingList();
		}

		String suggest = params.get("suggest");

		if(StringUtils.isBlank(suggest)){
			flash.error("数据有误!");

			auditingList();
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.auditSuggest = suggest; // 审核意见
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人

		bid.auditToNotThrough(error);

		if(Constants.IPS_ENABLE && error.code >= 0) {
			//流标调用环迅托管接口, 处理业务逻辑在托管实现类回调方法中  error  client  标的id   流标方式
			PaymentProxy.getInstance().bidAuditFail(error, Constants.PC, bidId, IPSConstants.BID_CANCEL_S);
			flash.error(error.msg);

			auditingList();
		}

		flash.error(error.msg);

		auditingList();
	}

	/**
	 * 提前借款->借款中不通过
	 */
	public static void advanceLoanToPeviewNotThrough(String sign) {
		checkAuthenticity();

		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);

		if(bidId < 1){
			flash.error(error.msg);

			auditingList();
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;

/*		String suggest = params.get("suggest");

		if(StringUtils.isBlank(suggest)){
			flash.error("数据有误!");

			if(bid.hasInvestedAmount == bid.amount)
				fullList();

			fundraiseingList();
		}*/

	//	bid.auditSuggest = suggest; // 审核意见
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人

		bid.advanceLoanToPeviewNotThrough(error);

		if(Constants.IPS_ENABLE) {
			//流标调用环迅托管接口, 处理业务逻辑在托管实现类回调方法中  error  client  标的id   流标方式
			PaymentProxy.getInstance().bidAuditFail(error, Constants.PC, bidId, IPSConstants.BID_CANCEL_B);
			flash.error(error.msg);

			fundraiseingList();

		}

		flash.error(error.msg);

		if(bid.hasInvestedAmount == bid.amount)
			fullList();

		fundraiseingList();
	}

	/**
	 *  借款中->借款中不通过
	 */
	public static void fundraiseToPeviewNotThrough(String sign) {
		checkAuthenticity();

		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);

		if(bidId < 1){
			flash.error(error.msg);

			auditingList();
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人

		bid.fundraiseToPeviewNotThrough(error);

		if(Constants.IPS_ENABLE && error.code >= 0) {
			//流标调用环迅托管接口, 处理业务逻辑在托管实现类回调方法中  error  client  标的id   流标方式
			PaymentProxy.getInstance().bidAuditFail(error, Constants.PC, bidId, IPSConstants.BID_CANCEL_I);
			flash.error(error.msg);

			fundraiseingList();
		}

		flash.error(error.msg);

		fundraiseingList();
	}

	/**
	 * 满标->待放款
	 */
	public static void fundraiseToEaitLoan(String sign) {
		checkAuthenticity();

		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);

		if(bidId < 1){
			flash.error(error.msg);

			auditingList();
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人

		bid.fundraiseToEaitLoan(error);
		flash.error(error.msg);

		fullList();
	}

	/**
	 *  满标->放款不通过
	 */
	public static void fundraiseToLoanNotThrough(String sign) {
		checkAuthenticity();

		ErrorInfo error = new ErrorInfo();
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);

		if(bidId < 1){
			flash.error(error.msg);

			auditingList();
		}

		Bid bid = new Bid();
		bid.auditBid = true;
		bid.id = bidId;
		bid.allocationSupervisorId = Supervisor.currSupervisor().id; // 审核人

		bid.fundraiseToLoanNotThrough(error);

		if(Constants.IPS_ENABLE && error.code >= 0) {

			//流标调用环迅托管接口, 处理业务逻辑在托管实现类回调方法中  error  client  标的id   流标方式
			PaymentProxy.getInstance().bidAuditFail(error, Constants.PC, bidId, IPSConstants.BID_CANCEL_M);
			flash.error(error.msg);

			fullList();
		}

		flash.error(error.msg);

		fullList();
	}

	/**
	 * 上传资料
	 */
	public static void createUserAuditItem(long userId, long userItemId, String items,int uploadType){
		ErrorInfo error = new ErrorInfo();

		if(StringUtils.isBlank(items)){
			error.msg = "数据有误!";

			renderJSON(error);
		}

		UserAuditItem item = new UserAuditItem();
		item.lazy = true;
		item.userId = userId;
		item.id = userItemId;
		item.isSupervisorCreate = true;

		if(item.id < 1) {
			error.msg = "资料标示项已过期，请刷新页面!";

			renderJSON(error);
		}
		String mark =item.mark;
		//该审核资料所有的文件集合
		List<t_user_audit_items> itemAll = UserAuditItem.queryUserAuditItemAll(userId, mark, error);

		if(itemAll!=null && itemAll.size()>=1){
			int  itlengh = itemAll.size();
			//已上传照片集合
			List<t_user_audit_items> clearItems = UserAuditItem.queryUserAuditItemByCondition(userId,mark,uploadType, error);
			//打码照片集合
			int result=-1;
			//如果是第一次上传
			if(itlengh==1){
				if(StringUtils.isNotBlank(itemAll.get(0).image_file_name)){
					//删除该用户的上传资料
					if(clearItems!=null && clearItems.size()>0){
						for(t_user_audit_items item1 : clearItems){
							result=t_user_audit_items.delete(" id=? ", item1.id);
//							result=UserAuditItem.deleteAuditItemByCondition(userId,item.mark,uploadType, error);
							if(result<0){
								renderJSON(error);
							}
						}
					}
				}
			} else{
				//删除该用户的上传资料
				if(clearItems!=null && clearItems.size()>0){
					for(t_user_audit_items item1 : clearItems){
						result=t_user_audit_items.delete(" id=? ", item1.id);
//						result=UserAuditItem.deleteAuditItemByCondition(userId,item.mark,uploadType, error);
						if(result<0){
							renderJSON(error);
						}
					}

				}

			}
		}

		item.statusTyep = uploadType;//后台上传类型区分
		item.imageFileNames = items;
		item.createUserAuditItem(error);

		JSONObject json = new JSONObject();
		json.put("msg", error.msg);
		json.put("status", item.status);
		json.put("time", DateUtil.dateToString1(item.time));

		renderJSON(json);
	}
}
