package controllers.payment.fy;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.json.JSONException;

import payment.fy.util.FyPaymentUtil;
import payment.ips.util.IpsPaymentUtil;
import utils.ErrorInfo;
import business.User;

import com.shove.Convert;

import constants.PayType;
import controllers.payment.PaymentBaseAction;

public class FyPaymentCallBackAction extends PaymentBaseAction{

	/**
	 * 充值回调
	 * @throws JSONException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void returnRecharge() throws Exception{
		
		FyPaymentUtil.printData(params.allSimple(), "充值回调参数", PayType.RECHARGE.name(), true);
		
		Map<String, String> parseXml = params.allSimple();
		ErrorInfo error = new ErrorInfo();
		
		FyPaymentUtil.checkFormSign(error, params.allSimple(), "充值", true, "amt", "login_id", "mchnt_cd", "mchnt_txn_ssn", "rem", "resp_code");
		if(error.code < 0){			
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}
		Map<String, Object> dataMap = FyPaymentUtil.queryMmmDataByOrderNum(parseXml.get("mchnt_txn_ssn"));
		
		User.recharge(dataMap.get("payNumber").toString(), Double.parseDouble(dataMap.get("amount").toString()), error);
		
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}
		payErrorInfo(ErrorInfo.createError(error, 1, "充值成功!", "/front/account/dealRecord", "交易记录页面!"));
	}
	
	/**
	 * 提现回调
	 * @throws Exception
	 */
	public static void returnWithdraw() throws Exception{ 
		
		FyPaymentUtil.printData(params.allSimple(), "提现回调参数", PayType.WITHDRAW.name(), true);
		
		Map<String, String> parseXml = params.allSimple();
		ErrorInfo error = new ErrorInfo();
		FyPaymentUtil.checkFormSign(error, params.allSimple(), "提现", true, "amt", "login_id", "mchnt_cd", "mchnt_txn_ssn", "resp_code");		
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}
		String orderNum = parseXml.get("mchnt_txn_ssn");
		Map<String, Object> dataMap = IpsPaymentUtil.queryMmmDataByOrderNum(orderNum);	
		
		//处理本地业务逻辑	
		Long userId = Convert.strToLong(dataMap.get("userId").toString(), -1);
		Long withdrawalId = Convert.strToLong(dataMap.get("withdrawalId").toString(), -1);
		double serviceFee  = Convert.strToDouble("pMerFee", 0.00);
		
		//调用提现方法
		User.withdrawalNotice(userId, serviceFee, withdrawalId, "1", false, true, error);
		
		if(error.code < 0){
			payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
			return;
		}
		error.code = 1;
		error.msg = "提现成功!";
		payErrorInfo(ErrorInfo.createError(error, error.code, error.msg, "/front/account/recharge", "充值页面"));
	}
}
