package gateway.tonglian;

import gateway.tonglian.base.OperatorType;
import gateway.tonglian.base.PropertyConfig;
import gateway.tonglian.base.TPayType;
import gateway.tonglian.bean.PayResult;
import gateway.tonglian.bean.TOrder;
import gateway.tonglian.utils.TLPayUtil;

import java.math.BigDecimal;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang.NullArgumentException;

import play.Logger;
import utils.ErrorInfo;
import business.AgentPayment;
import constants.Constants;
import controllers.BaseController;

/**
 * 通连支付（普通网关）
 *
 * @author hys
 * @createDate  2015年7月10日 下午6:53:15
 *
 */
public class TonglianPayment {

	/** 订单 */
	public TOrder order = null;

	/** 支付交易类型 */
	public String operatorType = null;

	/**
	 * 网银支付，组装网银支付需要的参数
	 * @param pMerBillNo
	 * @param bankCode
	 * @param money
	 * @param operatorType
	 */
	public TonglianPayment(String pMerBillNo, String bankCode, double money) {
		
		//网银支付
		this.operatorType = OperatorType.CYBERBANK_PAY;
		
		BigDecimal bigDecimal = new BigDecimal(money * 100);// 转成分
		String amount = bigDecimal.toString();
		int index = amount.indexOf(".");
		if (index > 0) {
			Logger.info("充值最小单位为分");
		}
		
		// 订单
		order = new TOrder();
		order.orderAmount = String.valueOf(amount);
		order.issuerId = bankCode.toLowerCase();
		order.orderNo = pMerBillNo;

		//网银支付
		order.payType = TPayType.SAVE_PAY;// 支付类型,1个人储蓄卡网银支付
		order.ext1 = PropertyConfig.VERSION_EXT1_INIT;
		
	}

	/**
	 * 组装接口需要的基本参数
	 */
	public void getBaseParam() {
		order.version = PropertyConfig.VERSION;// 版本号
		order.inputCharset = PropertyConfig.INPUTCHARSET_UTF8;// 编码字符集，默认UTF-8
		order.merchantId = PropertyConfig.MERCHANTID;// 商户号
		order.orderDatetime = TLPayUtil.getCurrentDateTimeStr();// 时间戳

		order.action = PropertyConfig.BANKGATEWAY_URL;// 接口地址
		order.pickupUrl = BaseController.getBaseURL() + PropertyConfig.PICKUPURL;// 同步请求url
		order.receiveUrl = BaseController.getBaseURL() + PropertyConfig.RECEIVEURL;// 异步请求url
		order.signType = PropertyConfig.SIGNTYPE;// 0表示订单上送和交易结果通知都使用MD5进行签名
		order.orderCurrency = PropertyConfig.ORDERCURRENCY;// 订单金额币种类型,0和156代表人民币
	}

	/**
	 * 查询卡BIN
	 */
	public TOrder pay() {
		
		parmValidate();

		getBaseParam();

		// 加密
		MD5Ensecret();

		return order;

	}
	
	/**
	 * 支付接口同步/异步回调业务
	 * 
	 * @param isSyn 是否同步回调，true：同步，false：异步
	 * @param paramMap
	 * @param error
	 */
	public static void payCallBack(boolean isSyn, Map<String, String> paramMap, ErrorInfo error) {
		error.clear();
		
		String jsonStr = JSONObject.fromObject(paramMap).toString();
		/** 验签 */
		if (!TLPayUtil.checkSign(jsonStr, PropertyConfig.MD5_KEY, PayResult.checkedSignKey)) {
			Logger.info("[%s]--[订单:%s]支付结果通知验签失败", PropertyConfig.name, paramMap.get("orderNo"));
			
			error.code = -1;
			error.msg = "验签失败";
			return;
		}

		String result = paramMap.get("payResult");
		if (!PropertyConfig.ASYN_NOTIFY_SUCCESS_FLAG.equals(result)) {
			Logger.info("[%s]--[订单:%s]通联支付失败", PropertyConfig.name, paramMap.get("orderNo"));

			error.code = -2;
			error.msg = "通联支付失败";
			
			return;
		}
		
		//异步或本地测试环境，执行用户支付成功后的逻辑
		if(!isSyn || Constants.IS_LOCALHOST){
			
			/** 必要字段 */
			String orderNo = paramMap.get("orderNo");
			double amount = Double.parseDouble(paramMap.get("orderAmount")) / 100;  //将分换算成元
			
			//更新请求参数日志表，status = 2 ，成功。防重复
			TLPayUtil.updateRequestLog(orderNo, error);
			
			AgentPayment ap = new AgentPayment(Long.parseLong(orderNo), error);
			if(error.code < 0){
				
				return;
			}
			
			ap.paySuccess(amount, error);
		}
	}

	/**
	 * 加密，签名
	 */
	private void MD5Ensecret() {
		// 加签名
		String sign = TLPayUtil.addSign(JSONObject.fromObject(order), PropertyConfig.MD5_KEY, TOrder.checkedKeyArray);
		order.signMsg = sign;
	}

	/**
	 * @param str
	 * @return
	 */
	public String log(String str) {
		return "[" + PropertyConfig.name + "]：" + str;
	}

	/**
	 * 传值为空检查
	 */
	public void parmValidate() {
		if (order == null || operatorType == null) {
			throw new NullArgumentException(log("传值为空，请检查"));
		}
	}
}
