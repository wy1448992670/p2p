package gateway.tonglian.bean;

import gateway.tonglian.base.PropertyConfig;

import java.io.Serializable;

/**
 * 商户订单信息(全集)
 * 
 * @author yuy
 * @date:2015-05-20 15:02
 * @version :1.0
 *
 */
public class TOrder implements Serializable {
	private static final long serialVersionUID = 1L;

	public static String[] checkedKeyArray = new String[] { "inputCharset", "pickupUrl", "receiveUrl", "version", "signType", "merchantId",
			"orderNo", "orderAmount", "orderCurrency", "orderDatetime", "ext1", "payType", "issuerId" };

	public String inputCharset;// 参数字符编码集
	public String action;  //接口地址
	public String pickupUrl;// 同步返回url
	public String receiveUrl;// 异步通知url
	public String version;// 版本号
	public String signType;// 0表示订单上送和交易结果通知都使用MD5进行签名
	public String merchantId;// 商户号
	public String orderNo;// 订单号
	public String orderAmount;// 订单金额
	public String orderCurrency;// 订单金额币种类型,0和156代表人民币
	public String orderDatetime;// 下单时间
	public String payType;// 支付方式,1个人储蓄卡网银支付
	public String issuerId;// 银行代码
	public String signMsg;// 加密串

	public String ext1;// 扩展：version版本

}
