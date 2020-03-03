package controllers.supervisor.financeManager;

import java.util.Map;

import payment.PaymentProxy;
import models.t_merchant_deal_details;
import utils.ErrorInfo;
import utils.PageBean;
import annotation.SubmitOnly;
import business.DealDetail;
import constants.Constants;
import controllers.supervisor.SupervisorController;

/**
 * 商户账户管理，目前只支持汇付资金托管
 *
 * @author hys
 * @createDate  2015年4月30日 上午11:53:21
 *
 */
public class MerchantAccountManager extends SupervisorController {
	
	/**
	 * 交易记录详情
	 * @param type
	 * @param startDate
	 * @param endDate
	 * @param currPage
	 */
	public static void dealDetails(int type, String startDate, String endDate, int currPage){
		
		PageBean<t_merchant_deal_details> page = DealDetail.queryMerDealDetails(type, startDate, endDate, currPage);
		
		if(null == page){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}

	/**
	 * 充值页面
	 */
	public static void merRecharge(){
		render();
	}
	
	/**
	 * 确认充值
	 */
	@SubmitOnly
	public static void submitRecharge(double amount){
		ErrorInfo error = new ErrorInfo();
		if (Constants.IPS_ENABLE) {
			
			if (amount == 0) {
				flash.error("请输入充值金额");

				merRecharge();
			}
			    	
			if (amount <= 0 || amount > Constants.MAX_VALUE) {
				flash.error("充值金额范围需在[0~" + Constants.MAX_VALUE + "]之间");
				merRecharge();
			}
			
			PaymentProxy.getInstance().merchantRecharge(error, Constants.PC, amount);
			
			flash.error(error.msg);
			
			merRecharge();
		}else{
			flash.error("非资金托管模式下不支持商户充值");
			merRecharge();
		}
	}
	
	/**
	 * 提现页面
	 */
	public static void merWithdrawal(){
		ErrorInfo error = new ErrorInfo();
		
		Map<String,Object> maps = PaymentProxy.getInstance().queryAmountByMerchant(error, Constants.PC, null);
		Double merBalance = Double.valueOf(maps.get("AvlBal")+"");
		render(merBalance);
	}
	
	/**
	 * 提交提现
	 * @param amount
	 */
	public static void submitWithdrawal(double amount){
		ErrorInfo error = new ErrorInfo();
		if (Constants.IPS_ENABLE) {
			
			if (amount == 0) {
				flash.error("请输入提现金额");

				merRecharge();
			}
			    	
			if (amount <= 0 || amount > Constants.MAX_VALUE) {
				flash.error("提现金额范围需在[0~" + Constants.MAX_VALUE + "]之间");
				merRecharge();
			}
			
			PaymentProxy.getInstance().merWithdrawal(error, Constants.PC, amount);
			
			flash.error(error.msg);
			merWithdrawal();
			
		}else{
			flash.error("非资金托管模式下不支持商户提现");
			merWithdrawal();
		}
	}
}
