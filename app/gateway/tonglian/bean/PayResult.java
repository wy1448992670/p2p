package gateway.tonglian.bean;

import java.io.Serializable;

/**
 * 支付返回结果集
 * 
 * @author yuy
 * @date:2015-05-20 15:02
 * @version :1.0
 *
 */
public class PayResult implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 验签关键字，有顺序之分 */
	public static String[] checkedSignKey = new String[] { "merchantId", "version", "language", "signType", "payType", "paymentOrderId", "orderNo",
			"orderDatetime", "orderAmount", "payDatetime", "payAmount", "ext1", "payResult", "returnDatetime" };

	public String merchantId;// 商户号
	public String version;// 版本号
	public String language;// 1代表简体中文
	public String signType;// 签名类型
	public String payType;// 支付方式
	public String paymentOrderId;// 通联订单号
	public String orderNo;// 商户订单号
	public String orderDatetime;// 商户订单提交时间
	public String orderAmount;// 商户订单金额
	public String payDatetime;// 支付完成时间
	public String payAmount;// 订单实际支付金额
	public String payResult;// 处理结果
	public String returnDatetime;// 结果返回时间
	public String signMsg;// 签名字符串

	public String ext1;// 扩展字段1，与提交订单时的扩展字段1保持一致

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getSignType() {
		return signType;
	}

	public void setSignType(String signType) {
		this.signType = signType;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public String getPaymentOrderId() {
		return paymentOrderId;
	}

	public void setPaymentOrderId(String paymentOrderId) {
		this.paymentOrderId = paymentOrderId;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getOrderDatetime() {
		return orderDatetime;
	}

	public void setOrderDatetime(String orderDatetime) {
		this.orderDatetime = orderDatetime;
	}

	public String getOrderAmount() {
		return orderAmount;
	}

	public void setOrderAmount(String orderAmount) {
		this.orderAmount = orderAmount;
	}

	public String getPayDatetime() {
		return payDatetime;
	}

	public void setPayDatetime(String payDatetime) {
		this.payDatetime = payDatetime;
	}

	public String getPayAmount() {
		return payAmount;
	}

	public void setPayAmount(String payAmount) {
		this.payAmount = payAmount;
	}

	public String getPayResult() {
		return payResult;
	}

	public void setPayResult(String payResult) {
		this.payResult = payResult;
	}

	public String getReturnDatetime() {
		return returnDatetime;
	}

	public void setReturnDatetime(String returnDatetime) {
		this.returnDatetime = returnDatetime;
	}

	public String getSignMsg() {
		return signMsg;
	}

	public void setSignMsg(String signMsg) {
		this.signMsg = signMsg;
	}

}
