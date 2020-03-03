package gateway.tonglian.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import play.Logger;
import play.Play;
import utils.DataUtil;
import constants.Constants;

/**
 * 通联 商户配置信息
 * 
 * @author yuy
 * @date:2015-5-15 11:45:40
 * @version :1.0
 *
 */
public class PropertyConfig {
	// 第三方支付
	public static String name = "通联支付";
	// 接口版本号，固定1.0
	public static String VERSION = "v1.0";
	// MD5 KEY
	public static String MD5_KEY = "1234567890";
	// 1代表UTF-8、2代表GBK、3代表GB2312；
	public static String INPUTCHARSET_UTF8 = "1";
	// 接收异步通知地址
	public static String RECEIVEURL = "front/fastPay/tonglian/asynCallBack";
	// 支付结束后返回地址
	public static String PICKUPURL = "front/fastPay/tonglian/synCallBack";
	// 0表示订单上送和交易结果通知都使用MD5进行签名;1表示商户用使用MD5算法验签上送订单，通联交易结果通知使用证书签名
	public static String SIGNTYPE = "0";
	// 商户编号
	public static String MERCHANTID = "100020091218001";
	// 0和156代表人民币、840代表美元、344代表港币，跨境支付商户不建议使用0
	public static String ORDERCURRENCY = "0";
	// 订单有效期 7days
	public static String VALID_ORDER = "10080";

	// 通联网银支付地址
	public static String BANKGATEWAY_URL = "http://ceshi.allinpay.com/gateway/index.do";

	// 异步通知成功标志
	public static String ASYN_NOTIFY_SUCCESS_FLAG = "1";

	// 扩展字段：订单版本号
	public static String VERSION_EXT1_INIT = "1";

	// 配置文件路径,加载Properties文件使用
	private static final String path = Play.configuration.getProperty("trust.funds.path") + "/agentpay.properties";

	// 常见配置属性
	private static Properties properties = null;

	static {
		if (properties == null) {
			loadProperties();
		}
	}

	/**
	 * 加载通联支付配置文件
	 */
	private static void loadProperties() {
		Logger.debug("读取agentpay.properties配置文件...");
		properties = new Properties();
		try {
			if (path == null) {
				throw new IllegalArgumentException("配置文件[agentpay.properties]路径为空，请配置正确路径");
			}
			properties.load(new FileInputStream(new File(path)));
			VERSION = properties.get("tonglian_version").toString();
			MERCHANTID = properties.get("tonglian_merchantid").toString();
			MD5_KEY = properties.get("tonglian_md5_key").toString();
			ORDERCURRENCY = properties.get("tonglian_ordercurrency").toString();
			VALID_ORDER = properties.get("tonglian_valid_order").toString();
			BANKGATEWAY_URL = properties.get("tonglian_bankgatewayurl").toString();
		} catch (IOException e) {
			Logger.error(e, "读取代理支付配置时出错");
		}
	}

	/**
	 * 是否测试环境
	 * 
	 * @return
	 */
	public static boolean isTest() {
		if (BANKGATEWAY_URL != null && BANKGATEWAY_URL.contains("ceshi") && "100020091218001".equals(MERCHANTID)) {
			return true;
		}
		return false;
	}

}
