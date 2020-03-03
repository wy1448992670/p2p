package payment.ips.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import play.Logger;
import play.Play;

import com.shove.Convert;

import constants.Constants;

public class IpsConstants {

	//环讯配置文件
	public static Properties properties = null;
	
	
	//环讯配置文件路径
	private static final String path = Play.configuration.getProperty("trust.funds.path")+"/ips/ips.properties";
	
	/**des_key秘钥**/
	public static final String des_key;
	
	/**des_iv秘钥**/
	public static final String des_iv;
	
	/**商户号**/
	public static final String terraceNoOne;
	
	/**cert_md5秘钥**/
	public static final String cert_md5;
	
	/**页面跳转提交地址**/
	public static final String POST_URL;
	
	/**ws提交地址**/
	public static final String WS_URL;
	
	public static final String soap_url;
	
	/**担保方IPS账户**/
	public static final String pAccount;
	
	/**担保方身份证号**/
	public static final String pFromIdentNo;
	
	/**担保方真实姓名**/
	public static final String pAccountName;
	
	public static String GetBankList = "GetBankList";
	
	public static String QueryForAccBalance = "QueryForAccBalance";
	
	public static String Transfer = "Transfer";
	
	/**
	 * 解冻保证金
	 */
	public static String GuaranteeUnfreeze = "GuaranteeUnfreeze";
	public static String GuaranteeUnfreezeResult = "GuaranteeUnfreezeResult";	
	public static String GetBankListResult = "GetBankListResult";	
	public static String TransferResult = "TransferResult";		
	/**
	 * 加载环讯配置文件
	 */
	private static void loadProperties(){
		Logger.debug("读取ips配置文件...");
		properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(path))); 
			
		} catch (FileNotFoundException e) {

			Logger.error("读取环讯配置库时 :%s", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	static{			
		
		if(properties==null){
			loadProperties();
		}
		des_key = properties.getProperty("des_key");
		des_iv = properties.getProperty("des_iv");
		terraceNoOne = properties.getProperty("terraceNoOne");
		cert_md5 = properties.getProperty("cert_md5");
		POST_URL = properties.getProperty("POST_URL");
		WS_URL = properties.getProperty("WS_URL");
		soap_url = properties.getProperty("soap_url");
		pAccount = properties.getProperty("pAccount");
		pFromIdentNo = properties.getProperty("pFromIdentNo");
		pAccountName = properties.getProperty("pAccountName");		
		initSupport();	
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
}
