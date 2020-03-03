package controllers.supervisor.dataStatistics;

import java.util.List;

import models.*;
import annotation.DebtCheck;
import business.StatisticalReport;
import constants.Constants;
import controllers.DebtTransferCheck;
import controllers.supervisor.SupervisorController;
import play.mvc.With;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 运营数据统计分析
 * 
 * @author bsr
 * 
 */
@With(DebtTransferCheck.class)
public class OperationStatisticsAction extends SupervisorController {

	/**
	 * 会员数据统计
	 */
	public static void userStatistic(int currPage, int pageSize, int year, int month, int day,
			String startDateStr,String endDateStr,int order, int isExport) {
		ErrorInfo error = new ErrorInfo();
		 List<Object> years = FinancialStatisticsAction.getYears();
		PageBean<t_statistic_member> page =StatisticalReport.queryMember(currPage, pageSize, year, 
				month, day, startDateStr, endDateStr, order, isExport, error); 
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			new FinancialStatisticsAction().exportExcel(page.page, "会员数据统计",
					new String[] { "年", "月", "日", "新增会员数", "新增充值会员数",
							"新增会员充值占比", "新增VIP会员数", "累计会员数",
							"会员活跃度（登录会员个数/累计会员数）", "借款会员数", "理财会员数", "复合会员数",
							"VIP会员数" }, 
					new String[] { "year", "month", "day",
							"new_member", "new_recharge_member",
							"new_member_recharge_rate", "new_vip_count",
							"member_count", "member_activity",
							"borrow_member_count", "invest_member_count",
							"composite_member", "vip_count" });
		}

		render(page,years);
	}
	
	/**
	 * 会员数据统计分析对比
	 */
	public static void userStatisticData(long id) {
		ErrorInfo error = new ErrorInfo();
		
		t_statistic_member member = new t_statistic_member();
		member = StatisticalReport.queryDateForMember(id, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(member);
	}

	/**
	 * 借款情况统计
	 */
	public static void loanStatistic(int currPage, int pageSize, int year, int month, int orderType, int isExport) {
		ErrorInfo error = new ErrorInfo();
		 List<Object> years = FinancialStatisticsAction.getYears();
		PageBean<t_statistic_borrow> page = 
				StatisticalReport.queryBorrows(currPage, pageSize, year, month, orderType, isExport, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
				
		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			new FinancialStatisticsAction().exportExcel(page.page, "借款情况统计",
					new String[] { "年", "月", "累计借款总额", "本月借款总额", "累计借款会员数",
							"新增借款会员数", "还款中的借款总额", "已成功借款标数量", "已成功借款总额",
							"平均年利率", "均借款金额", "逾期借款标数量", "逾期总额", "逾期总额占比",
							"坏账借款标数量", "坏账总额", "坏账总额占比" }, new String[] {
							"year", "month", "total_borrow_amount",
							"this_month_borrow_amount",
							"total_borrow_user_num", "new_borrow_user_num",
							"repaying_borrow_amount", "released_bids_num",
							"released_borrow_amount", "average_annual_rate",
							"average_borrow_amount", "overdue_bids_num",
							"overdue_amount", "overdue_per", "bad_bids_num",
							"bad_bill_amount", "bad_bill_amount_per" });
		}

		render(page,years);
	}
	
	/**
	 * 借款数据统计分析对比
	 */
	public static void loanStatisticData(long id) {
        ErrorInfo error = new ErrorInfo();
		
        t_statistic_borrow loans = new t_statistic_borrow();
		loans = StatisticalReport.queryDateForLoan(id, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(loans);
	}

	/**
	 * 理财情况统计
	 */
	public static void investorsStatistic(int currPage, int pageSize, int year, int month,
			String startDateStr,String endDateStr,int order, int isExport) {
		
		ErrorInfo error = new ErrorInfo();
		 List<Object> years = FinancialStatisticsAction.getYears();
		PageBean<t_statistic_financial_situation> page =StatisticalReport.queryInvest(currPage, pageSize, year, month, startDateStr, endDateStr, order, isExport, error); 
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
				
		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			new FinancialStatisticsAction().exportExcel(page.page, "理财情况统计",
					new String[] { "年", "月", "累计理财投标总额", "本月新增理财总额", "累计理财会员数",
							"新增理财会员数", "人均理财金额", "人均账户余额", "理财会员转化率" },
					new String[] { "year", "month",
							"invest_accoumt",
							"increase_invest_account",
							"invest_user_account",
							"increase_invest_user_account",
							"per_capita_invest_amount",
							"per_capita_balance",
							"invest_user_conversion" });
		}
		
		render(page,years);
	}
	
	/**
	 * 理财数据统计分析对比
	 */
	public static void investorsStatisticData(long id) {
        ErrorInfo error = new ErrorInfo();
		
        t_statistic_financial_situation invests = new t_statistic_financial_situation();
        invests = StatisticalReport.queryDateForInvest(id, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(invests);
	}

	/**
	 * 借款标销量情况统计
	 */
	public static void loanBidStatistic(int currPage, int pageSize, int year, int month, int keywordType, String keyword, int orderType, int isExport) {
		ErrorInfo error = new ErrorInfo();
		PageBean<t_statistic_product> page = 
				StatisticalReport.queryProducts(currPage, pageSize, year, month, keywordType, keyword, orderType, isExport, error);
		 List<Object> years = FinancialStatisticsAction.getYears();
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			new FinancialStatisticsAction().exportExcel(page.page, "借款标销售情况分析表",
					new String[] { "年", "月", "借款标类型", "已成功借款标数量", "已借款总额",
							"均标借款金额", "逾期数量", "逾期数量占比", "坏账数量", "坏账数量占比",
							"借款标数量", "投标会员数", "平均年利率", "管理费收入总额" },
					new String[] { "year", "month", "name",
							"released_bids_num", "released_amount",
							"average_bid_amount", "overdue_num", "overdue_per",
							"bad_bids_num", "bad_bids_per", "bids_num",
							"invest_user_num", "average_annual_rate",
							"manage_fee_amount" });
		}
		
		render(page,years);
	}

	/**
	 * 债权转让情况统计
	 */
	@DebtCheck(2)
	public static void debtStatistic(int currPage, int pageSize, int year, int month,
			String startDateStr,String endDateStr,int order, int isExport) {
		ErrorInfo error = new ErrorInfo();
		 List<Object> years = FinancialStatisticsAction.getYears();
		PageBean<t_statistic_debt_situation> page =StatisticalReport.queryDebt(currPage, pageSize, year, month, startDateStr, endDateStr, order, isExport, error); 
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			new FinancialStatisticsAction().exportExcel(page.page, "债权转让情况统计分析表",
					new String[] { "年", "月", "债权转让标总数量", "债权转让总金额",
							"本月新增转让标数量", "本月新增转让总额", "转让债权标含逾期数量", "转让债权逾期占比",
							"转让债权均标金额", "本月债权转让成功标数量", "本月转让债权成交率", "本月债权转让率" },
					new String[] { "year", "month", "debt_account",
							"debt_amount_sum", "increase_debt_account",
							"increase_debt_amount_sum", "has_overdue_debt",
							"overdue_percent", "average_debt_amount",
							"success_debt_amount", "deal_percent",
							"transfer_percent" });
		}
		
		render(page,years);
	}

	/**
	 * 审核科目库统计
	 */
	public static void auditItemsStatistic(int currPage, int pageSize, int year, int month, int keywordType, String keyword, int orderType, int isExport) {
		ErrorInfo error = new ErrorInfo();
		 List<Object> years = FinancialStatisticsAction.getYears();
		PageBean<t_statistic_audit_items> page = 
				StatisticalReport.queryAuditItems(currPage, pageSize, year, month, keywordType, keyword, orderType, isExport, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
			
		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			new FinancialStatisticsAction().exportExcel(page.page, "审核科目统计分析表",
					new String[] { "年", "月", "科目编号", "审核科目", "信用积分", "审核费用",
							"提交会员数", "审核通过数", "通过占比", "关联借款标类型数量", "关联逾期借款标数量",
							"关联坏账借款标数量", "风控有效性排名" }, new String[] { "year",
							"month", "no", "name", "credit_score", "audit_fee",
							"submit_user_num", "audit_pass_num", "pass_per",
							"relate_product_num", "relate_overdue_bid_num",
							"relate_bad_bid_num", "risk_control_ranking" });
		}

		render(page,years);
	}


	public static void dataDisclosureStatistic(int currPage, int pageSize, String beginTime,String endTime,int order, int isExport) {


		ErrorInfo error = new ErrorInfo();
		List<Object> years = FinancialStatisticsAction.getYears();
		PageBean<t_statistic_data_disclosure> page =StatisticalReport.queryDataDisclosure(currPage, pageSize, beginTime, endTime,  order, isExport, error);

		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			new FinancialStatisticsAction().exportExcel(page.page, "信披数据统计",
					new String[] {
							"日期",
							"每日存量",
							"待还本金",
							"待还利息",
							"充值总额",
							"提现总额",
							"资金净流入",
							"投资总额",
							"站岗资金",
							"历史注册",
							"新增注册",
							"借款人数",
							"新增借款人数",
							"投资人数",
							"新增投资人数",
							"上标总额",
							"新增上标总额",
							"上标总数",
							"新增上标总数",
							"还款总额",
							"逾期金额",
							"累计逾期金额",
							"逾期笔数",
							"累计逾期笔数" },
					new String[] { 	"the_date",
							"no_repay_money",
							"no_repay_corpus",
							"no_repay_interest",
							"day_recharge_money",
							"day_withdrawals_money",
							"day_in_money",
							"day_invest_money",
							"available_balance",
							"total_registe_users",
							"day_registe_users",
							"borrowers",
							"day_new_borrowers",
							"investers",
							"day_new_investers",
							"product_total_money",
							"day_new_product_money",
							"product_total_count",
							"day_new_product_count",
							"day_repay_money",
							"norepay_overdue_money",
							"total_overdue_money",
							"norepay_overdua_count",
							"tatal_overdua_count" });
		}

		render(page,years);
	}
}
