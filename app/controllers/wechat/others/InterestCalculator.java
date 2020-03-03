package controllers.wechat.others;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import utils.Arith;
import utils.ErrorInfo;
import business.AuditItem;
import business.BackstageSet;
import business.Bill;
import business.News;
import business.Product;
import business.Bid.Repayment;
import constants.OptionKeys;
import controllers.BaseController;

public class InterestCalculator extends BaseController{
	/**
	 * 信用计算器
	 */
	public static void wealthToolkitCreditCalculator(){
		ErrorInfo error = new ErrorInfo();
		
		List<AuditItem> auditItems = AuditItem.queryAuditItems(error);
		
		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); // 得到积分对应的借款额度值
		double amountKey = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value);
		
		render(auditItems, amountKey);
	}
	
	/**
	 * 还款计算器
	 */
	public static void wealthToolkitRepaymentCalculator(){
		ErrorInfo error = new ErrorInfo();
		
		List<Repayment> rtypes = Repayment.queryRepaymentType(null, error); // 还款类型
		
		render(rtypes);
	}
	
	/**
	 * 还款明细(异步)
	 */
	public static void repaymentCalculate(double amount, double apr, int period, int periodUnit, int repaymentType){
		List<Map<String, Object>> payList = null;
		
		payList = Bill.repaymentCalculate(amount, apr, period, periodUnit, repaymentType);
		
		render(payList);
	}
	
	/**
	 * 净值计算器
	 */
	public static void wealthToolkitNetValueCalculator(){
		ErrorInfo error = new ErrorInfo();
		
		double bailScale = Product.queryNetValueBailScale(error); // 得到净值产品的保证金比例
		
		render(bailScale);
	}
	
	/**
	 * 利率计算器
	 */
	public static void wealthToolkitAPRCalculator(){
		ErrorInfo error = new ErrorInfo();
		
		List<Repayment> rtypes = Repayment.queryRepaymentType(null, error); // 还款类型
		
		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); // 得到积分对应的借款额度值
		double serviceFee = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value);

		render(rtypes, serviceFee);
	}
	
	/**
	 * 利率计算器,计算年华收益、总利益(异步)
	 */
	public static void aprCalculator(double amount, double apr,int repaymentType,double award,int rperiod){
		ErrorInfo error = new ErrorInfo();
		DecimalFormat df = new DecimalFormat("#.00");
		
		double managementRate = BackstageSet.getCurrentBackstageSet().investmentFee / 100;//系统管理费费率
		double earning = 0;
		
		if(repaymentType == 1){/* 按月还款、等额本息 */
			double monRate = apr / 12;// 月利率
			int monTime = rperiod;
			double val1 = amount * monRate * Math.pow((1 + monRate), monTime);
			double val2 = Math.pow((1 + monRate), monTime) - 1;
			double monRepay = val1 / val2;// 每月偿还金额
			
			/**
			 * 年化收益
			 */
			 earning = Arith.excelRate((amount - award),
					Double.parseDouble(df.format(monRepay)), monTime, 200, 15)*12*100;
			 earning = Double.parseDouble(df.format(earning)+"");
		}
		
		if(repaymentType == 2 || repaymentType == 3){ /* 按月付息、一次还款   */
			double monRate = apr / 12;// 月利率
			int monTime = rperiod;// * 12;借款期限填月
			double borrowSum = Double.parseDouble(df.format(amount));
			double monRepay = Double.parseDouble(df.format(borrowSum * monRate));// 每月偿还金额
			double allSum = Double.parseDouble(df.format((monRepay * monTime)))
					+ borrowSum;// 还款本息总额
			 earning = Arith.rateTotal(allSum,
					(borrowSum - award), monTime)*100;
			 earning = Double.parseDouble(df.format(earning)+"");
		}
		
		
		JSONObject obj = new JSONObject();
		obj.put("managementRate", managementRate < 0 ? 0 : managementRate); 
		obj.put("earning", earning); 
		
		renderJSON(obj);
	}
	
	/**
	 * 服务手续费
	 */
	public static void wealthToolkitServiceFee(){
		ErrorInfo error = new ErrorInfo();
		String content = News.queryContent(-1011L, error);
		flash.error(error.msg);
		
		renderText(content);
	}
}
