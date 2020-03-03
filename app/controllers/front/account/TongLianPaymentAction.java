package controllers.front.account;

import gateway.tonglian.TonglianPayment;
import gateway.tonglian.base.PropertyConfig;
import gateway.tonglian.bean.TOrder;
import gateway.tonglian.service.TonglPayService;
import gateway.tonglian.utils.TLPayUtil;

import java.util.Map;

import play.Logger;
import utils.ErrorInfo;
import business.User;
import constants.Constants;
import constants.PayType;
import controllers.BaseController;

/**
 * 通联支付
 *
 * @author hys
 * @createDate  2015年7月8日 下午7:49:49
 *
 */
public class TongLianPaymentAction extends BaseController {

	/**
	 * 网银支付
	 */
	public static void cyberbankPay(String pMerBillNo,String bankCode, double money, String mmmUserId) {

		TOrder order = new TonglianPayment(pMerBillNo, bankCode, money).pay();
		
		TLPayUtil.saveRequestLog(mmmUserId, "闪电快充支付提交参数", PayType.AGENTRECHARGE.toString(), order);

		render("@front.account.TongLianPaymentAction.TlNetJump", order);
	
	}
	
	/**
	 * 支付同步回调
	 */
	public static void synCallBack() {
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		if (user == null) {
			flash.error("由于你停留时间过长，请重新登录查看支付结果");
			FundsManage.recharge();
		}

		Logger.info("[%s-同步回调]--开始", PropertyConfig.name);

		//获取回调参数
		Map<String, String> paramMap = TonglPayService.parseToMap(params);
		if (paramMap == null || paramMap.size() == 0) {
			Logger.info("[%s-同步回调]--[订单:%s]支付结果参数为空", PropertyConfig.name, paramMap.get("orderNo"));
			flash.error("充值失败，请联系客服");
			FundsManage.recharge();
		}
		
		TLPayUtil.saveCallBackLog(paramMap);
		
		TonglianPayment.payCallBack(true, paramMap, error);
		
		if(error.code < 0 && error.code != Constants.ALREADY_RUN){
			flash.error("充值失败，请联系客服");
			FundsManage.recharge();
		}

		Logger.info("[%s-同步回调]--[订单:%s]充值成功，结束", PropertyConfig.name, paramMap.get("orderNo"));
		
		flash.error("充值成功");
		FundsManage.recharge();
	}

	/**
	 * 支付异步回调
	 */
	public static void asynCallBack() {
		ErrorInfo error = new ErrorInfo();
		Logger.info("[%s-异步回调]--开始", PropertyConfig.name);

		/** 解析 */
		Map<String, String> paramMap = TonglPayService.parseToMap(params);
		String orderNo = paramMap.get("orderNo");
		if (paramMap == null || paramMap.size() == 0) {
			Logger.info("[%s-异步回调]--[订单:%s]支付结果参数为空", PropertyConfig.name, orderNo);
			return;
		}

		TLPayUtil.saveCallBackLog(paramMap);
		
		TonglianPayment.payCallBack(false, paramMap, error);
		
		if(error.code < 0 ){
			
			return;
		}

		Logger.info("[%s-异步回调]--[订单:%s]充值成功，结束", PropertyConfig.name, orderNo);
	}

}
