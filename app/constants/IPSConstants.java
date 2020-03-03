package constants;

import play.Play;

import com.shove.security.Encrypt;

public class IPSConstants {

	public static final String MER_CODE = Play.configuration.getProperty("pay.mer_no");//平台商户号
	public static final String MER_NAME = Play.configuration.getProperty("pay.mer_name");//平台商户名
	public static final String MER_IDENT_NO = Play.configuration.getProperty("pay.mer_ident_no");//平台证件号码
	
	public static final String GUARANTOR_CODE = Play.configuration.getProperty("pay.guarantor_code");//担保机构商户号
	public static final String GUARANTOR_NAME = Play.configuration.getProperty("pay.guarantor_name");//担保机构名称
	
	public static final String PLATFORM = "2";		//所在资金托管的平台id

	public static final String ACTION = Play.configuration.getProperty("pay.action");	//资金托管url
//	public static final String CALLBACK_URL = Play.configuration.getProperty("pay.callback.url");		//回调
	public static final String CALLBACK_URL = "front/PaymentAction/";  //回调地址域名不从配置文件取，修改为从request对象获取，配置（pay.callback.url）废弃
	public static final String DOMAIN = Encrypt.encrypt3DES(Play.configuration.getProperty("pay.domain"), Constants.ENCRYPTION_KEY);//p2p平台域名
	
	public static final String CACHE_TIME = "1h";	//标，投资等信息缓存时间
	
	public static final String INDENT_TYPE = "1";		//证件类型：1#身份证
	public static final String ACCT_TYPE = "1";		//账户类型：1#个人
	public static final String ACCT_AGENCY = "0";		//账户类型：0#机构
	public static final String UNFREEZENT_YPE = "1";		//1#解冻借款方;2#解冻担保方
	public static final String CHANNEL_TYPE = "1";	//充值渠道类型：1#网银充值
	public static final String IPS_FEE_TYPE = "1";	//谁付ips手续费：1#平台支付，2#用户支付
	public static final String VALID_TYPE = "N";	//自动还款有限期类型：N#长期有效
	public static final String VALID_DATE = "0";	//自动还款有效期：0
	public static final String OUT_TYPE = "1";	//提现模式：1#普通提现，2#定向提现<暂不开放>
	public static final String DAY_CYCLE_TYPE = "1";	//借款周期类型：1#天，3#月
	public static final String MONTH_CYCLE_TYPE = "3";	//借款周期类型：1#天，3#月
	public static final String PAID_MONTH_EQUAL_PRINCIPAL_INTEREST = "1"; // 按月还款、等额本息
	public static final String PAID_MONTH_ONCE_REPAYMENT = "2"; // 按月付息、一次还款
	public static final String ONCE_REPAYMENT = "3"; // 等额本金
	public static final String OTHER_REPAYMENT = "99"; // 还款方式：其他
	public static final String OPERATION_TYPE_1 = "1"; // 标的操作类型：1#新增，2#结束
	public static final String OPERATION_TYPE_2 = "2"; // 标的操作类型：1#新增，2#结束
	public static final String REPAYMENT_TYPE = "1"; // 还款类型：1#手动还款，2#自动还款
	public static final String REPAYMENT_TYPE_2 = "2"; // 还款类型：1#手动还款，2#自动还款
	public static final String BID_FLOW = "flow"; // 流标标示
	public static final String AUTO_PAYMENT = "autoPayment"; // 自动还款标示
	
	public static final String BID_CREATE = "create"; //发标
	public static final String BID_CANCEL = "cancel"; //审核中->撤销
	public static final String BID_CANCEL_B = "cancelB"; //提前借款->借款中不通过
	public static final String BID_CANCEL_S = "cancelS"; //审核中->审核不通过
	public static final String BID_CANCEL_I = "cancelI"; //借款中->借款中不通过
	public static final String BID_CANCEL_M = "cancelM"; //满标->放款不通过
	public static final String BID_CANCEL_F = "cancelF"; //提前借款->撤销
	public static final String BID_CANCEL_N = "cancelN"; //借款中->撤销
	public static final String BID_ADVANCE_LOAN = "flowA"; //提前借款->流标
	public static final String BID_FUNDRAISE = "flowI"; //借款中->流标
	
	public static double MIN_AMOUNT = 1.0;						//标的借款额度限额最小值
	public static int FAIL_CODE = -1315;	//已失败
	public static int PROCESS_CODE = -1316;	// 处理中
	public static String pErrMsg = "当前状态为处理中，在此时间段有资金变动且超出您当前操作的金额，可能会导致此操作失败!"; // 处理中的情况
	
	public class IPSOperation {
		public static final int CREATE_IPS_ACCT = 1;			//开户
		public static final int REGISTER_SUBJECT = 2;			//标的登记
		public static final int REGISTER_CREDITOR = 3;			//登记债权人接口
		public static final int REGISTER_GUARANTOR = 4;			//登记担保方
		public static final int REGISTER_CRETANSFER = 5;		//登记债权转让接口
		public static final int AUTO_NEW_SIGNING = 6;			//自动投标签约
		public static final int REPAYMENT_SIGNING = 7;			//自动还款签约
		public static final int DO_DP_TRADE = 8;				//充值
		public static final int TRANSFER = 9;					//转账(WS)
		public static final int REPAYMENT_NEW_TRADE = 10;		//还款
		public static final int GUARANTEE_UNFREEZE = 11;		//解冻保证金
		public static final int DO_DW_TRADE = 13;				//提现
		public static final int QUERY_FOR_ACC_BALANCE = 14;		//账户余额查询(WS)
		public static final int GET_BANK_LIST = 15;				//获取银行列表查询(WS)
		public static final int QUERY_MER_USER_INFO = 16;		//账户信息查询(WS)
		public static final int QUERY_TRADE = 17;				//交易查询(WS)
		
		public static final int MERCHANT_RECHARGE = 40;		    //商户充值
		public static final int MERCHANT_WITHDRAWAL = 41;		//商户提现
		public static final int QUERY_FOR_MER_BALANCE = 42;		//商户余额查询
		
		public static final int TRANSFER_USER_TO_MER = 32;		//用户转商户
		public static final int TRANSFER_MER_TO_USER = 33;		//商户转用户
		public static final int TRANSFER_MER_TO_USER_SINGLE = 34;	//商户转用户-单笔(WS)
		
		public static final int UNFREEZE_INVEST_AMOUNT = 35;	//解冻投资金额(post)
		public static final int UNFREEZE_INVEST_AMOUNT_WS = 36;	//解冻投资金额(ws)
		
		public static final int TRANSFER_ONE = 91;				//转账-1放款(WS)
		public static final int TRANSFER_TWO = 92;				//转账-2代偿(WS)
		public static final int TRANSFER_THREE = 93;			//转账-3代偿还款(WS)
		public static final int TRANSFER_FOUR = 94;				//转账-4债权转让(WS)
		public static final int LOGIN = 100;				    //登录
		public static final int QUERY_GATE_WAY = 101;			//查询domain标识的网关id（spay）
		public static final int USER_BIND_CARD = 102;			//用户绑卡（汇付）
	}
	
	public class RepairOperation {
//		public static final String CREATE_IPS_ACCT = "01";			//开户
		public static final String REGISTER_SUBJECT = "02";			//标的登记
		public static final String REGISTER_CREDITOR = "03";			//登记债权人接口
//		public static final String REGISTER_CREDITOR = "04";			//登记担保方
		public static final String REGISTER_CRETANSFER = "05";		//登记债权转让接口
//		public static final String AUTO_NEW_SIGNING = "06";			//自动投标签约
//		public static final String REPAYMENT_SIGNING = "07";			//自动还款签约
		public static final String DO_DP_TRADE = "08";				//充值
//		public static final String TRANSFER = "09";					//转账(WS)
		public static final String REPAYMENT_NEW_TRADE = "11";		//还款
//		public static final String GUARANTEE_UNFREEZE = "10";		//解冻保证金
		public static final String DO_DW_TRADE = "09";				//提现
//		public static final String QUERY_FOR_ACC_BALANCE = "14";		//账户余额查询(WS)
//		public static final String GET_BANK_LIST = "15";				//获取银行列表查询(WS)
//		public static final String QUERY_MER_USER_INFO = "16";		//账户信息查询(WS)
//		public static final String QUERY_TRADE = "17";				//交易查询(WS)
		
		public static final String TRANSFER_ONE = "14";				//转账-1放款(WS)
		public static final String TRANSFER_FOUR = "15";				//转账-4债权转让(WS)
		public static final String TRANSFER = "14"; 
	}
	
	public static class IPSWebUrl {
		public static final String CREATE_IPS_ACCT = CALLBACK_URL + "createAcctCB";					//开户
		public static final String REGISTER_SUBJECT = CALLBACK_URL + "registerSubjectCB";			//标的登记
		public static final String REGISTER_CREDITOR = CALLBACK_URL + "registerCreditorCB";			//登记债权人接口
		public static final String REGISTER_GUARANTOR = CALLBACK_URL + "registerGuarantorCB";		//登记担保方
		public static final String REGISTER_CRETANSFER = CALLBACK_URL + "registerCretansferCB";		//登记债权转让接口
		public static final String AUTO_NEW_SIGNING = CALLBACK_URL + "autoNewSigningCB";			//自动投标签约
		public static final String REPAYMENT_SIGNING = CALLBACK_URL + "repaymentSigningCB";			//自动还款签约
		public static final String DO_DP_TRADE = CALLBACK_URL + "doDpTradeCB";						//充值
		public static final String TRANSFER = CALLBACK_URL + "transferCB";							//转账(WS)
		public static final String REPAYMENT_NEW_TRADE = CALLBACK_URL + "repaymentNewTradeCB";		//还款
		public static final String GUARANTEE_UNFREEZE = CALLBACK_URL + "guaranteeUnfreezeCB";		//解冻保证金
		public static final String DO_DW_TRADE = CALLBACK_URL + "doDwTradeCB";						//提现
		public static final String QUERY_FOR_ACC_BALANCE = CALLBACK_URL + "queryForAccBalanceCB";	//账户余额查询(WS)
		public static final String GET_BANK_LIST = CALLBACK_URL + "getBankListCB";					//获取银行列表查询(WS)
		public static final String QUERY_MER_USER_INFO = CALLBACK_URL + "queryMerUserInfoCB";		//账户信息查询(WS)
		public static final String TRANSFER_USER_TO_MER = CALLBACK_URL + "transferUserToMerCB";		//转账-用户转商户
		public static final String TRANSFER_MER_TO_USER = CALLBACK_URL + "transferMerToUserCB";		//转账-商户转用户(WS)
		public static final String UNFREEZE_INVEST_AMOUNT = CALLBACK_URL + "unfreezeInvestAmountCB";//解冻投资金额
		public static final String USER_BIND_CARD = CALLBACK_URL + "userBindCardCB";	            //用户绑卡
	}
	
	public static class IPSS2SUrl {
		public static final String CREATE_IPS_ACCT = CALLBACK_URL + "createAcctCBSys";					//开户
		public static final String REGISTER_SUBJECT = CALLBACK_URL + "registerSubjectCBSys";			//标的登记
		public static final String REGISTER_CREDITOR = CALLBACK_URL + "registerCreditorCBSys";			//登记债权人接口
		public static final String REGISTER_CRETANSFER = CALLBACK_URL + "registerCretansferCBSys";		//登记债权转让接口
		public static final String REGISTER_GUARANTOR = CALLBACK_URL + "registerGuarantorCBSys";		//登记担保方
		public static final String AUTO_NEW_SIGNING = CALLBACK_URL + "autoNewSigningCBSys";				//自动投标签约
		public static final String REPAYMENT_SIGNING = CALLBACK_URL + "repaymentSigningCBSys";			//自动还款签约
		public static final String DO_DP_TRADE = CALLBACK_URL + "doDpTradeCBSys";						//充值
		public static final String DO_DP_TRADE_FOR_MER = CALLBACK_URL + "doDpTradeForMerCBSys";			//商户充值
		public static final String TRANSFER = CALLBACK_URL + "transferCBSys";							//转账(WS)
		public static final String COMPENSATE = CALLBACK_URL + "compensateCBSys";						//本金垫付(WS)
		public static final String OFFLINE_REPAYMENT = CALLBACK_URL + "offlineRepaymentCBSys";			//线下收款(WS)
		public static final String REPAYMENT_NEW_TRADE = CALLBACK_URL + "repaymentNewTradeCBSys";		//还款
		public static final String GUARANTEE_UNFREEZE = CALLBACK_URL + "guaranteeUnfreezeCBSys";		//解冻保证金
		public static final String DO_DW_TRADE = CALLBACK_URL + "doDwTradeCBSys";						//提现
		public static final String DO_DW_TRADE_FOR_MER = CALLBACK_URL + "doDwTradeForMerCBSys";			//商户提现
		public static final String QUERY_FOR_ACC_BALANCE = CALLBACK_URL + "queryForAccBalanceCBSys";	//账户余额查询(WS)
		public static final String GET_BANK_LIST = CALLBACK_URL + "getBankListCBSys";					//获取银行列表查询(WS)
		public static final String QUERY_MER_USER_INFO = CALLBACK_URL + "queryMerUserInfoCBSys";		//账户信息查询(WS)
		public static final String TRANSFER_USER_TO_MER = CALLBACK_URL + "transferUserToMerCBSys";	    //转账-用户转商户
		public static final String TRANSFER_MER_TO_USER = CALLBACK_URL + "transferMerToUserCBSys";	    //转账-商户转用户(WS)
		public static final String UNFREEZE_INVEST_AMOUNT = CALLBACK_URL + "unfreezeInvestAmountCBSys";	//解冻投资金额
		public static final String USER_BIND_CARD = CALLBACK_URL + "userBindCardCBSys";	                //用户绑卡
	}
	
	public static class IPSWSUrl {
		public static final String CREATE_IPS_ACCT = CALLBACK_URL + "createAcctCB";					//开户
		public static final String REGISTER_SUBJECT = CALLBACK_URL + "registerSubjectWS";			//标的登记
		public static final String REGISTER_CREDITOR = CALLBACK_URL + "registerCreditorWS";			//登记债权人接口
		public static final String REGISTER_GUARANTOR = CALLBACK_URL + "registerGuarantorCB";		//登记担保方
		public static final String REGISTER_CRETANSFER = CALLBACK_URL + "registerCretansferCB";		//登记债权转让接口
		public static final String AUTO_NEW_SIGNING = CALLBACK_URL + "autoNewSigningCB";			//自动投标签约
		public static final String REPAYMENT_SIGNING = CALLBACK_URL + "repaymentSigningCB";			//自动还款签约
		public static final String DO_DP_TRADE = CALLBACK_URL + "doDpTradeWS";						//充值
		public static final String DO_DP_TRADE_FOR_MER = CALLBACK_URL + "doDpTradeForMerWS";		//商户充值
		public static final String TRANSFER = CALLBACK_URL + "transferCB";							//转账(WS)
		public static final String COMPENSATE = CALLBACK_URL + "compensateCB";						//本金垫付
		public static final String OFFLINE_REPAYMENT = CALLBACK_URL + "offlineRepaymentCB";			//线下收款
		public static final String REPAYMENT_NEW_TRADE = CALLBACK_URL + "repaymentNewTradeWS";		//还款
		public static final String GUARANTEE_UNFREEZE = CALLBACK_URL + "guaranteeUnfreezeCB";		//解冻保证金
		public static final String DO_DW_TRADE = CALLBACK_URL + "doDwTradeWS";						//提现
		public static final String DO_DW_TRADE_FOR_MER = CALLBACK_URL + "doDwTradeForMerWS";		//商户提现
		public static final String QUERY_FOR_ACC_BALANCE = CALLBACK_URL + "queryForAccBalanceCB";	//账户余额查询(WS)
		public static final String GET_BANK_LIST = CALLBACK_URL + "getBankListCB";					//获取银行列表查询(WS)
		public static final String QUERY_MER_USER_INFO = CALLBACK_URL + "queryMerUserInfoCB";		//账户信息查询(WS)
		public static final String TRANSFER_USER_TO_MER = CALLBACK_URL + "transferUserToMerCB";		//转账-用户转商户
		public static final String TRANSFER_MER_TO_USER = CALLBACK_URL + "transferMerToUserCB";		//转账-商户转用户(WS)
	}
	
	public static class IPSPostUrl {
		public static final String CREATE_IPS_ACCT = CALLBACK_URL + "createAcctCB";					//开户
		public static final String REGISTER_SUBJECT = CALLBACK_URL + "registerSubjectPost";			//标的登记
		public static final String REGISTER_CREDITOR = CALLBACK_URL + "registerCreditorPost";		//登记债权人接口
		public static final String REGISTER_GUARANTOR = CALLBACK_URL + "registerGuarantorCB";		//登记担保方
		public static final String REGISTER_CRETANSFER = CALLBACK_URL + "registerCretansferCB";		//登记债权转让接口
		public static final String AUTO_NEW_SIGNING = CALLBACK_URL + "autoNewSigningCB";			//自动投标签约
		public static final String REPAYMENT_SIGNING = CALLBACK_URL + "repaymentSigningCB";			//自动还款签约
		public static final String DO_DP_TRADE = CALLBACK_URL + "doDpTradePost";					//充值
		public static final String DO_DP_TRADE_FOR_MER = CALLBACK_URL + "doDpTradeForMerPost";		//商户充值
		public static final String TRANSFER = CALLBACK_URL + "transferCB";							//转账(WS)
		public static final String REPAYMENT_NEW_TRADE = CALLBACK_URL + "repaymentNewTradePost";	//还款
		public static final String GUARANTEE_UNFREEZE = CALLBACK_URL + "guaranteeUnfreezeCB";		//解冻保证金
		public static final String DO_DW_TRADE = CALLBACK_URL + "doDwTradePost";					//提现
		public static final String DO_DW_TRADE_FOR_MER = CALLBACK_URL + "doDwTradeForMerPost";					//商户提现
		public static final String QUERY_FOR_ACC_BALANCE = CALLBACK_URL + "queryForAccBalanceCB";	//账户余额查询(WS)
		public static final String GET_BANK_LIST = CALLBACK_URL + "getBankListCB";					//获取银行列表查询(WS)
		public static final String QUERY_MER_USER_INFO = CALLBACK_URL + "queryMerUserInfoCB";		//账户信息查询(WS)
		public static final String TRANSFER_USER_TO_MER = CALLBACK_URL + "transferUserToMerCB";		//转账-用户转商户
		public static final String TRANSFER_MER_TO_USER = CALLBACK_URL + "transferMerToUserCB";		//转账-商户转用户(WS)
		public static final String UNFREEZE_INVEST_AMOUNT = CALLBACK_URL + "unfreezeInvestAmountPost";		//解冻投资金额
	}
	
	public class IpsCheckStatus {
		public static final int NONE = 0;
		public static final int EMAIL = 1;
		public static final int REAL_NAME = 2;
		public static final int MOBILE = 3;
		public static final int IPS = 4;
	}
	
	public class TransferType {
		public static final int INVEST = 1;					//投资(放款)
		public static final int COMPENSATE = 2;				//代偿
		public static final int COMPENSATE_REPAYMENT = 3;	//代偿还款
		public static final int CRETRANSFER = 4;			//债权转让
	}
	
	public class Status {
		public static final int SUCCESS = 1;		//成功
		public static final int FAIL = 2;			//失败
		public static final int HANDLING = 3;		//处理中
		public static final int NONE = 4;			//未查询到交易
		
		public static final int UNFREEZING = 10;		//待解冻
		public static final int UNFREEZED = 11;			//已解冻
	}
	
	public class CompensateType {
		/**
		 * 本金垫付
		 */
		public static final int COMPENSATE = 1;			
		
		/**
		 * 线下收款
		 */
		public static final int OFFLINE_REPAYMENT = 2;	
	}
	
	public class RegisterGuarantorType {
		public static final int COMPENSATE = 1;			//本金垫付
		public static final int OFFLINE_REPAYMENT = 2;	//待收款线下收款
		public static final int OFFLINE_REPAYMENT_OVERDUE = 3;	//逾期待收款线下收款
	}
	
	public static boolean IS_REPAIR_TEST = false; 
	
	/**
	 * 解冻投资金额的请求方式，默认WS
	 */
	public static boolean IS_WS_UNFREEZE = true;
	
	/**
	 * 商户号管理
	 */
	public static final int MER_RECHARGE = 1;  //商户充值
	public static final int MER_WITHDRAWAL = 2;  //商户提现
	public static final int UNKNOWN_FEE = -1;  //交易手续费
	public class MerchantDealDetailStatus {
		public static final int FAILED = -1;  //失败
		public static final int SUBMITED = 0;  //已提交
		public static final int HANDING = 1;  //处理中
		public static final int SUCCESSED = 2;	//成功
	}
	
	/**
	 * 资金托管处理状态(环讯)
	 */
	public class IPSDealStatus {
		public static final int NORMAL = 0;  //正常
		
		//标的
		public static final int BID_END_HANDING = 1; //标的结束处理中
		public static final int LOAN_HANDING = 2; //放款处理中
		
		//借款账单
		public static final int REPAYMENT_HANDING = 1; //还款处理中
		public static final int OFFLINEREPAYMENT_HANDING = 2; //线下收款处理中
		public static final int COMPENSATE_HANDING = 3; //本金垫付处理中

	}
	

	/**
	 * 闪电快充（代理充值）支付网关，目前支持通联
	 */
	public static final int AGENT_GATE_WAY_TONGLIAN = 1;
	
	/**
	 * 闪电快充（代理充值）状态（0：用户支付中，1：已支付，2：商户转账中，3：已转账）
	 */
	public class AgentPayStatus {
		/**
		 * 用户支付中
		 */
		public static final int AGENT_SUBMIT = 0;  
		
		/**
		 * 用户已支付充值金额
		 */
		public static final int AGENT_PREPAID = 1; 
		
		/**
		 * 商户已转账给用户
		 */
		public static final int AGENT_TRANSFER = 2;  
		
		/**
		 * 用户充值金额已到账，代理充值完成
		 */
		public static final int AGENT_COMPLETED = 3;  
		
	}

}
