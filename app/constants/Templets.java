package constants;

/**
 * 模板常量
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-8-22 下午08:09:49
 */
public class Templets {
	
	/**
	 *  邮件模板常量
	 */
	public static final long E_FIND_USERNAME = 1; //找回用户名
	public static final long E_ACTIVE = 2; //注册激活
	public static final long E_RECHARGE = 3; //充值密码
	public static final long E_RESET_QUERSTION = 4; //重置安全问题邮箱
	public static final long E_RECHARGE_SUCCESS = 5; //提现成功
	public static final long E_RECHARGE_FAIL = 6; //提现失败
	public static final long E_WITHDRAWAL_SUCCESS = 7; //充值成功
	public static final long E_WITHDRAWAL_FAIL = 8; //充值失败
	public static final long E_VIP_SUCCESS = 9; //vip成功
	public static final long E_VIP_FAIL = 10; //vip失败
	public static final long E_56 = 11; //实名认证成功 
	public static final long E_5 = 12; //实名认证失败 
	public static final long E_6 = 13; //学历认证成功
	public static final long E_7 = 14; //学历认证失败
	public static final long E_8 = 15; //额度申请
	public static final long E_9 = 16; //额度申请失败
	public static final long E_10 = 17; //流标
	public static final long E_CREATE_BID = 18; //用户发标
	public static final long E_FIRST_SUCCESS = 19; //初审成功
	public static final long E_FIRST_FAIL = 20; // 返还资金
	public static final long E_14 = 21; //秒还成功还款
	public static final long E_15 = 22; //提前还款
	public static final long E_FULL_SUCCESS = 23; //满标审核通过
	public static final long E_FULL_FAIL = 24; //满标审核失败
	public static final long E_18 = 25; //流转标借款成功
	public static final long E_19 = 26; //收投资到
	public static final long E_20 = 27; //借款标留言
	public static final long E_TENDER_OVER = 28; //投资回收
	public static final long E_22 = 29; //秒还成功回收
	public static final long E_INVEST_RECEIVE = 30; //成功回收
	public static final long E_24 = 31; //网站垫付
	public static final long E_25 = 22; //满标投资成功
	public static final long E_26 = 33; //满标投资失败
	public static final long E_27 = 34; //流转标投资成功
	public static final long E_INVEST = 35; //投资成功
	public static final long E_HAND_RECHARGE = 36; //手动充值
	public static final long E_HAND_CHARGE = 37; //手动扣费
	public static final long E_AUCTION_FREEZE = 38; //竞拍冻结
	public static final long E_AUCTION_SUCCESS = 39; //竞拍成功
	public static final long E_AUCTION_FAILURE = 40; //竞拍失败
	public static final long E_DEBT_TRANSFER_SUCCESS = 41; //债权转让成功
	public static final long E_SUCCESS_PAY = 42; //成功还款
	public static final long E_36 = 43; //vip续费
	public static final long E_37 = 44; //vip续费提醒
	public static final long E_REWARDS = 45; //好友邀请奖励
	public static final long E_DEBT_PASS = 46; //债权转让申请成功
	public static final long E_DEBT_NO_PASS = 47; //债权转让申请失败
	public static final long E_DEBT_TRANSFER_FAILURE = 48; //债权转让失败
	public static final long E_FINANC_RELEASE = 49; // 放款
	public static final long E_FUND_RAISEING_FAIL = 50; // 借款中不通过
	public static final long E_AUDIT_ITEM_SUCCESS = 51; // 审核资料通过
	public static final long E_AUDIT_ITEM_FAIL = 52; // 审核资料不通过
	public static final long E_INVEST_DEDUCT = 53; // 投资扣费
	public static final long E_DEAL_DEBT_FIRST = 54; // 初步成交债权
	public static final long E_FIND_DELPWD_EMAL = 55; // 通过邮箱找回交易密码
	public static final long E_USER_RECHARGE = 56; //用户前台充值

	public static final long E_RED_PACKETS=57;//发放红包
	public static final long E_RED_RATE=59;//发放加息券邮件
	/**
	 *  短信模板常量
	 */
	public static final long S_RECHARGE_SUCCESS = 1; //充值成功
	public static final long S_TENDER = 2; //投标成功
	public static final long S_FINANC_RELEASE = 3; //放款成功
	public static final long S_RECOVER_ADVANCE_SUCCESS = 4; //成功回款
	public static final long S_REPAY_SUCCESS = 5; //成功还款
	public static final long S_WITHDRAW_APPLICATION = 6; //提现申请
	public static final long S_AUTO_RECHARGE_FOR_BILL_SUCCESS = 7; //后台还款自动充值成功
	
	public static final long S_RED_PACKETS=57;//发放红包
	public static final long S_FINANC_RELEASE_NO_SERVICE_FEE=60;//放款成功,无标的服务费
	
	public static final long S_NOT_OVERDUE_REMIND=61;//亿美贷未逾期还款提醒
	public static final long S_OVERDUE_REMIND=62;//亿美贷已逾期还款提醒
	
	/**
	 *  站内信模板常量
	 */
	public static final long M_REGISTER = 1; //注册成功
	public static final long M_RECHARGE_SUCCESS = 2; //提现成功
	public static final long M_RECHARGE_FAIL = 3; //提现失败
	public static final long M_WITHDRAWAL_SUCCESS = 4; //充值成功
	public static final long M5_WITHDRAWAL_FAIL = 5; //充值失败
	public static final long M_VIP_SUCCESS = 6; //vip成功
	public static final long M_VIP_FAIL = 7; //vip失败
	public static final long M_56 = 8; //实名认证成功 
	public static final long M_5 = 9; //实名认证失败 
	public static final long M_6 = 10; //学历认证成功
	public static final long M_7 = 11; //学历认证失败
	public static final long M_8 = 12; //额度申请
	public static final long M_9 = 13; //额度申请失败
	public static final long M_10 = 14; //流标
	public static final long M_CREATE_BID = 15; //用户发标
	public static final long M_FIRST_SUCCESS = 16; //初审成功
	public static final long M_FIRST_FAIL = 17; //返还资金
	public static final long M_14 = 18; //秒还成功还款
	public static final long M_15 = 19; //提前还款
	public static final long M_FULL_SUCCESS = 20; //满标审核通过
	public static final long M_FULL_FAIL = 21; //满标审核失败
	public static final long M_18 = 22; //流转标借款成功
	public static final long M_19 = 23; //收投资到
	public static final long M_20 = 24; //借款标留言
	public static final long M_TENDER_OVER = 25; //投资回收
	public static final long M_22 = 26; //秒还成功回收
	public static final long M_INVEST_RECEIVE = 27; //成功回收
	public static final long M_24 = 28; //网站垫付
	public static final long M_25 = 29; //满标投资成功
	public static final long M_26 = 30; //满标投资失败
	public static final long M_27 = 31; //流转标投资成功
	public static final long M_INVEST = 32; //投资成功
	public static final long M_HAND_RECHARGE = 33; //手动充值
	public static final long M_HAND_CHARGE = 34; //手动扣费
	public static final long M_AUCTION_FREEZE = 35; //竞拍冻结
	public static final long M_AUCTION_SUCCESS = 36; //竞拍成功
	public static final long M_AUCTION_FAILURE = 37; //竞拍失败
	public static final long M_DEBT_TRANSFER_SUCCESS = 38; //债权转让成功
	public static final long M_SUCCESS_PAY = 39; //成功还款
	public static final long M_36 = 40; //vip续费
	public static final long M_37 = 41; //vip续费提醒
	public static final long M_REWARDS = 42; //好友邀请奖励
	public static final long M_DEBT_PASS = 43; //债权审核通过
	public static final long M_DEBT_NO_PASS = 44; //债权审核不通过
	public static final long M_DEBT_TRANSFER_FAILURE = 45; //债权转让失败
	public static final long M_FINANC_RELEASE = 46; // 放款
	public static final long M_FUND_RAISEING_FAIL = 47; // 借款中不通过
	public static final long M_AUDIT_ITEM_SUCCESS = 48; // 审核资料通过
	public static final long M_AUDIT_ITEM_FAIL = 49; // 审核资料不通过
	public static final long M_INVEST_DEDUCT = 50; // 投资扣费
	public static final long M_DEAL_DEBT_FIRST = 51; // 初步成交债权
	public static final long M_USER_RECHARGE=53;//用户前台充值
	
	public static final long M_RED_PACKETS=57;//发放红包
	public static final long M_REG_MSG=58;//注册成功短信
	public static final long M_RED_RATE=59;//发放加息券短信
	/**
	 * 防拦截html转换代码
	 */
	public static final String HTML_JING = "html_j"; // #
	public static final String HTML_IMG = "html_i"; // <img
	public static final String HTML_A = "html_a";  // <a
	public static final String HTML_FRAME = "html_f"; // <frame
	
	public static String replaceAllHTML(String content) {
		if(null == content || content.length() < 1)
			return content;
		
		return content.replaceAll(Templets.HTML_JING, "#").replaceAll(
				Templets.HTML_IMG, "<img").replaceAll(Templets.HTML_A, "<a")
				.replaceAll(Templets.HTML_FRAME, "<frame");
	}
	
	/*2014-11-14添加平台协议 */
	public static final int BID_PACT_BID = 1;//平台借款协议（借款人）
	public static final int BID_PACT_INVEST = 2;//平台借款协议（投资人）
	public static final int INTERMEDIARY_AGREEMENT_BID = 3;//居间服务协议(借款人)
	public static final int INTERMEDIARY_AGREEMENT_INVEST = 4;//居间服务协议(投资人)
	public static final int GUARANTEE_BID = 5;//保障函(借款人)
	public static final int GUARANTEE_INVEST = 6;//保障函(投资人)
	public static final int DEBT_TRANSFER_AGREEMENT = 7;//债权转让协议
	
	
	/* 平台债权协议替换字段*/
	public static final String TRANSFER_REALITY_NAME = "transfer_reality_name";//原债权人
	public static final String TRANSFER_ID_NUMBER = "transfer_id_number";//身份证号码
	public static final String TRANSFER_USER_NAME = "transfer_user_name";//平台用户名
	public static final String TRANSFEREE_REALITY_NAME = "transferee_reality_name";//新债权人
	public static final String TRANSFEREE_ID_NUMBER = "transferee_id_number";//身份证号码
	public static final String TRANSFEREE_USER_NAME = "transferee_user_name";//平台用户名
	
	public static final String LOAN_REALITY_NAME = "loan_reality_name";//借款人姓名
	public static final String LOAN_ID_NUMBER = "loan_id_number";//身份证号码
	public static final String SUM_MONEY = "sum_money";//债权价值
	public static final String COMPANY_NAME = "company_name";//见证人
	public static final String DATE = "date";//日期：
	public static final String LOAN_USER_NAME = "loan_user_name";//借款人用户
	
	
	
	/*投资保障函替换字段*/
	public static final String INVEST_NAME = "invest_name";
	public static final String INVEST_REALITY_NAME = "invest_reality_name";
	public static final String PACT_NO = "pact_no";
	public static final String CHINESE_AMOUNT = "chinese_amount";
	public static final String PERIOD = "period";
	public static final String PERIOD_UNIT = "unit";
	public static final String LOAN_NAME = "loan_name";
	
	/*居间服务协议（投资人）*/
	public static final String ID_NUMBER = "id_number";
	
	/*借款合同（投资人）*/
	public static final String INVEST_LIST = "invest_list";
	public static final String PURPOSE_NAME = "purpose_name";
	public static final String AMOUNT = "amount";
	public static final String APR = "apr";
	public static final String REPAYMENT_NAME = "repayment_name";
	public static final String CAPITAL_INTEREST_SUM = "capital_interest_sum";
	public static final String INVEST_BILL_LIST = "invest_bill_list";
	public static final String REPAYMENT_TIME = "repayment_time";
	
}
