package payment.fy.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shove.Convert;

import constants.Constants;

import play.Logger;
import play.Play;
/**
 * 富友托管常理类
 * @author xiaoqi
 *
 */
public class FyConstants {
	
	//富友配置文件
	public static Properties properties = null;
	
	
	//富友配置文件路径
	private static final String path = Play.configuration.getProperty("trust.funds.path")+"/fy/fy.properties";

	/**商户代码 **/
	public static  String mchnt_cd="";
	
	/**商户登录账户 **/
	public static  String mchnt_name="";
	
	/**请求接口路径**/
	public static String  post_url = "";
	
	/**用户注册提交地址**/
	public final static String register = "reg.action";
	
	/**用户信息修改提交地址**/
	public final static String modifyUserInf = "modifyUserInf.action";
	
	/**预授权接口**/
	public final static String preAuth = "preAuth.action";
	
	/**冻结**/
	public final static String freeze = "freeze.action";
	
	/**解冻**/
	public final static String unFreeze = "unFreeze.action";
	
	/**预授权撤销接口**/
	public final static String preAuthCancel = "preAuthCancel.action";
	
	/**转账（商户与个人之间）**/
	public final static String transferBmu = "transferBmu.action";
	
	/**转账（个人与个人之间）**/
	public final static String transferBu = "transferBu.action";
	
	/**余额查询**/
	public final static String balanceAction = "BalanceAction.action";
	
	/**充值**/
	public final static String recharge = "500002.action";
	
	/**提现**/
	public final static String withdraw = "500003.action";
	
	/**
	 * 私钥 ,富友分配给商户的
	 */
	public static PrivateKey privateKey;
	/**
	 * 公钥，富友的公钥
	 */
	public static PublicKey publicKey;
	/**
	 * 私钥文件路径 如：
	 */
	public static String privateKeyPath = "";
	
	/**错误信息**/
	public static Map<String, String> error = new HashMap<String, String>();
	/**
	 * 公钥文件路径 如：
	 */
	public static String publicKeyPath = "";

	/**
	 * 加载富友配置文件
	 */
	private static void loadProperties(){
		Logger.debug("读取富友配置文件...");
		properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(path))); 
			
		} catch (FileNotFoundException e) {

			Logger.error("读取富友配置库时 :%s", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	/**
	 * 不同支付平台差异性融合
	 * @return 
	 */
	public static void initSupport(){
				
		Constants.DEBT_USE  = Convert.strToBoolean(properties.getProperty("debt.use"), true);	    //是否有债权转让
		Constants.IS_DEBT_TWO  = Convert.strToBoolean(properties.getProperty("is.debt.two"), true);	    //是否支持二次债权转让
		Constants.IS_LOGIN  = Convert.strToBoolean(properties.getProperty("is.login"), false);	//是否需要登陆
		Constants.IS_SECOND_BID  = Convert.strToBoolean(properties.getProperty("is.second.bid"), false);//是否有秒还标
		Constants.IS_FLOW_BID  = Convert.strToBoolean(properties.getProperty("is.flow.bid"), false);	//是否有自动流标
		Constants.IS_WITHDRAWAL_AUDIT = Convert.strToBoolean(properties.getProperty("is.withdrawal.audit"), false);	//提现后是否需要审核
		Constants.IS_WITHDRAWAL_INNER = Convert.strToBoolean(properties.getProperty("is.withdrawal.inner"), true);	//是否支持提现内扣
		Constants.IS_GUARANTOR = Convert.strToBoolean(properties.getProperty("is.guarantor"), false);	//是否需要登记担保方
		Constants.IS_OFFLINERECEIVE = Convert.strToBoolean(properties.getProperty("is.offlineReceive"), true);	//是否支持本金垫付、线下收款	
		Constants.BORROW_MANAGE_MAXRATE = Convert.strToDouble(properties.getProperty("borrowManageMaxrate"), 50.0);	//最大借款管理费率
		Constants.DEBT_TRANSFER_MAXRATE = Convert.strToDouble(properties.getProperty("debtTransferMaxrate"), 100.0);	//最大债权转让费率
		Constants.WITHDRAW_MAXRATE = Convert.strToDouble(properties.getProperty("maxWithdrawRate"), 1.0);	//最大提现手续费费率
		
	}
	
	
	static {
		
		if(properties==null){
			loadProperties();
		}
		java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		mchnt_cd = properties.getProperty("mchnt_cd");
		post_url = properties.getProperty("post_url");
		mchnt_name = properties.getProperty("mchnt_name");
		
		privateKeyPath = Play.configuration.getProperty("trust.funds.path") +  properties.getProperty("privateKeyPath");
		publicKeyPath = Play.configuration.getProperty("trust.funds.path")  + properties.getProperty("publicKeyPath");		
		Gson gson = new Gson();
		error = gson.fromJson(properties.getProperty("error"), new TypeToken<Map<String, String>>(){}.getType());
		initSupport();
	}
	
	
}
