package controllers.supervisor.dataStatistics;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
import models.t_statistic_platform_float;
import models.t_statistic_platform_income;
import models.t_statistic_recharge;
import models.t_statistic_security;
import models.t_statistic_withdraw;
import business.StatisticalReport;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import reports.StatisticRecharge;
import reports.StatisticSecurity;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.PageBean;

/**
 * 财务数据统计分析
 * 
 * @author bsr
 * 
 */
public class FinancialStatisticsAction extends SupervisorController {
	
	public static List<Object> getYears(){
		Calendar cal=Calendar.getInstance();//使用日历类  
	    List<Object> years = new ArrayList<Object>();
		int yearTemp = cal.get(Calendar.YEAR);// 得到年
		
		for(int i=0;i<5;i++){
			years.add(yearTemp-i);
		}
		
		return years;
	}
	/**
	 * 充值统计
	 */
	public static void rechargeStatistic(int currPage, int pageSize, int year, int month,int day,
			String startDateStr,String endDateStr,int order, int isExport) {
        ErrorInfo error = new ErrorInfo();
        
        List<Object> years = getYears();
		
		PageBean<t_statistic_recharge> page = StatisticalReport.queryRecharge(currPage, pageSize, year, 
				month, day, startDateStr, endDateStr, order, isExport, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			new FinancialStatisticsAction().exportExcel(page.page, "充值统计",
					new String[] { "年", "月", "日", "充值总额", "充值笔数", "充值会员数",
						"新增充值会员数", "人均充值金额", "平均每笔充值金额", "最高充值金额",
						"最低充值金额", }, 
					new String[] { "year", "month", "day",
						"recharge_amount", "recharge_count",
						"recharge_menber", "new_recharge_menber",
						"average_recharge", "average_each_recharge",
						"max_recharge_amount", "min_recharge_amount" });
		}
		
		double totalAmount = StatisticRecharge.totalRecharge(error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		String date = DateUtil.dateToString1(new Date());
		
		render(page, totalAmount, date,years);
	}

	/**
	 * 提现统计
	 */
	public static void withdrawalStatistic(int currPage, int pageSize, int year, int month,int day,
			String startDateStr,String endDateStr,int order, int isExport) {
		
		//java.text.DecimalFormat df=new java.text.DecimalFormat("0.00");//保留2位小数
		ErrorInfo error = new ErrorInfo();
		 List<Object> years = getYears();
		PageBean<t_statistic_withdraw> page = StatisticalReport.queryWIthdraw(currPage, pageSize, year, 
				month, day, startDateStr, endDateStr, order, isExport, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			new FinancialStatisticsAction().exportExcel(page.page, "提现统计",
					new String[] { "年", "月", "日", "付款笔数", "付款总额",
						"申请提现笔数(含付款中)", "申请提现总额", "均申请提现金额", "最高申请提现金额",
						"最低申请提现金额" }, 
					new String[] { "year", "month",
						"day", "payment_number", "payment_sum",
						"apply_withdraw_account", "apply_withdraw_sum",
						"average_withdraw_amount", "max_withdraw_amount",
						"min_withdraw_amount" });
		}
		
		Double amount = 0d;
		StringBuffer date = new StringBuffer();
		
		List<Object[]> records = StatisticalReport.queryPaymentSum();
		
		if (null != records && records.size() > 0) {
			
			for (int i = 0; i < records.size(); i++) {
				
				if (i == 0) {
					date.append("" + records.get(i)[0].toString() + "-" + records.get(i)[1].toString() + "-" + records.get(i)[2].toString());
				}
				
				amount += Double.parseDouble(records.get(i)[3].toString());
			}	
		}
		
		render(page, amount, date, years);
	}

	/**
	 * 平台收入统计
	 */
	public static void incomeStatistic(int currPage, int pageSize, int year, int month,int day,
			String startDateStr,String endDateStr,int order, int isExport) {
        
		ErrorInfo error = new ErrorInfo();
		//java.text.DecimalFormat df=new java.text.DecimalFormat("0.00");//保留2位小数
		 List<Object> years = getYears();
		PageBean<t_statistic_platform_income> page = StatisticalReport.queryIncome(currPage, pageSize, year, 
				month, day, startDateStr, endDateStr, order, isExport, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			new FinancialStatisticsAction().exportExcel(page.page, "平台收入统计表",
					new String[] { "年", "月", "日", "收入总额", "借款管理费", "充值手续费",
							"提现手续费", "VIP会员费", "理财管理费", "债权转让管理费", "资料审核费" }, 
					new String[] { "year", "month", "day",
							"income_sum", "loan_manage_fee",
							"recharge_manage_fee", "withdraw_manage_fee",
							"vip_manage_fee", "invest_manage_fee",
							"debt_transfer_manage_fee", "item_audit_manage_fee" });
		}
		
		Double amount = 0d;
		StringBuffer date = new StringBuffer();
		List<Object[]> records = StatisticalReport.queryPlatformAllIncomeAndTime();
		
		if (null != records && records.size() > 0) {
			
			for (int i = 0; i < records.size(); i++) {
				
				if (i == 0) {
					date.append("" + records.get(i)[0].toString() + "-" + records.get(i)[1].toString() + "-" + records.get(i)[2].toString());
				}
				
				amount += Double.parseDouble(records.get(i)[3].toString()); 
			}
		}
		
		render(page, amount, date, years);
	}

	/**
	 * 平台浮存金统计
	 */
	public static void floatAurum(int currPage, int pageSize, int year, int month,int day,
			String startDateStr,String endDateStr,int order, int isExport) {
		
         ErrorInfo error = new ErrorInfo();
         List<Object> years = getYears();
		PageBean<t_statistic_platform_float> page = StatisticalReport.queryFloat(currPage, pageSize, year, 
				month, day, startDateStr, endDateStr, order, isExport, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		/* 导出excel */
		if(isExport == Constants.IS_EXPORT) {
			new FinancialStatisticsAction().exportExcel(page.page, "平台浮存金统计",
					new String[] { "年", "月", "日", "账户可用余额浮存", "冻结资金浮存",
							"浮存金总额", "有可用余额账户数量", "均账户余额", "有可用余额的VIP账户数量",
							"VIP账户可用余额浮存", "VIP均账户余额" }, 
					new String[] { "year", "month", "day", "balance_float_sum",
							"freeze_float_sum", "float_sum",
							"has_balance_user_account", "average_balance",
							"has_balance_vip_user_account",
							"vip_balance_float", "average_vip_balance" });
		}
		
		Map<String,Object> map = StatisticalReport.queryFloatParamter();
		String date = DateUtil.dateToString1(new Date());
		render(page,map,date,years);
	}

	/**
	 * 保障本金统计
	 */
	public static void guaranteeStatistic(int currPage, int pageSize, int year, int month,int day,
			String startDateStr,String endDateStr,int order, int isExport) {
        ErrorInfo error = new ErrorInfo();
        List<Object> years = getYears();
		PageBean<t_statistic_security> page = StatisticalReport.querySecurity(currPage, pageSize, year, 
				month, day, startDateStr, endDateStr, order, isExport, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		/* 导出excel */
		if(isExport == Constants.IS_EXPORT) {
			new FinancialStatisticsAction().exportExcel(page.page, "本金保障统计",
					new String[] { "年", "月", "日", "本金保障余额", "本金保障支出", "垫付账单笔数",
							"最高垫付金额", "最低垫付金额", "本金保障总投入", "平台总收入", "平台总借款额",
							"坏账总额", "坏账收入占比", "坏账保障金占比", "坏账借款占比" },
					new String[] { "year", "month", "day", "balance", "pay",
							"advance_acount", "max_advance_amount",
							"min_advance_amount", "recharge_amount",
							"income_amount", "loan_amount", "bad_debt_amount",
							"bad_debt_income_rate", "bad_debt_guarantee_rate",
							"bad_loan_rate" });
		}
		
		Map<String,Object> map = StatisticSecurity.statisticAmount(error);
		String date = DateUtil.dateToString1(new Date());
		
		if (null != page.page && 0 != page.page.size()){
			t_statistic_security v = page.page.get(0);
			map.put("rechargeAmount", v.recharge_amount);
			map.put("advanceAcount", v.advance_acount);
			map.put("pay", v.pay);
			map.put("balance", v.balance);
		}else{
			map.put("rechargeAmount", 0.00);
			map.put("advanceAcount", 0);
			map.put("pay", 0.00);
			map.put("balance", 0.00);
		}
		
		render(page, map, date,years);
	}
	
	/**
	 * 导出Excel表格
	 * @param list 数据集合
	 * @param name 导出表格名称
	 * @param arr1 必要参数1
	 * @param arr2 必要参数2
	 * @param key 需要转换的key标示
	 */
	public void exportExcel(List<?> list, String name, String [] arr1, String [] arr2) {
		JsonConfig jsonConfig = new JsonConfig();
    	jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
    	jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
    	JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
    	  
		File file = ExcelUtils.export(name, arrList, arr1, arr2);

		renderBinary(file, name + ".xls");
	} 
}
