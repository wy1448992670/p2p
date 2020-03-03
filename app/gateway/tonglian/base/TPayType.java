package gateway.tonglian.base;

/**
 * 通联 支付类型
 * 
 * @author yuy
 * @date 2015-05-18 20:33
 */
public class TPayType {

	public static final String ALL_PAY = "0"; // 0代表未指定支付方式，即显示该商户开通的所有支付方式

	public static final String SAVE_PAY = "1";// 个人储蓄卡网银支付

	public static final String ENTERPRISE_PAY = "4"; // 企业网银支付

	public static final String CREDIT_PAY = "11"; // 个人信用卡网银支付

	public static final String VISA_PAY = "23"; // 外卡支付

	public static final String AUTH_PAY = "28"; // 认证支付

}
