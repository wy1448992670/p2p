package constants;

import java.util.Map;

import play.Logger;
import utils.ErrorInfo;

public enum PayType {
	
	/*** 开户*/
	REGISTER(1,true,true,"托管开户成功","/front/account/home","账户总览","/front/account/trustAccount","资金托管认证注册"),
	
	/*** 充值*/
	RECHARGE(2,true,true,"充值成功","/front/account/dealRecord","交易记录","/front/account/recharge","充值"),
	
	/** 标的发布  */
	BIDCREATE(3,true,true),
	
	/*** 投标*/
	INVEST(4,true,true),
	
	/*** 自动投标*/
	AUTO_INVEST(5,true,true),
	
	/*** 提现*/
	WITHDRAW(6,true,true,"提现成功","/front/account/dealRecord","交易记录","/front/account/withdrawal","提现"),
	
	/*** 标的审核通过*/
	BID_AUDIT_SUCC(7,true,true),
	
	/*** 流标*/
	BID_AUDIT_FAIL(8,true,true),
	
	/*** 提高信用额度*/
	APPLY_CREDIT(9,true,true),
	
	/*** 申请vip*/
	APPLY_VIP(10,true,true),
	
	/*** 登记债权转让*/
	DEBTOR_TRANSFER(11,true,true,"债权转让成功","/front/account/dealRecord","交易记录","/front/account/myDebts","债权转让"),
	
	/*** 债权转让成交*/
	DEBTOR_TRANSFER_CONFIRM(12,true,true),
	
	/*** 垫付(登记担保方)*/
	ADVANCE(13,true,true),
		
	/*** 还款*/
	REPAYMENT(14,true,true),
	
	/*** 余额查询*/
	QUERY_AMOUNT(15,true,false),
	
	/*** 获取银行列表*/
	QUERY_BANKS(16,true,false),
	
	/**解冻资金**/
	UNFREEZE(17,true,false),
	
	/**自动还款签约**/
	AUTO_REPAYMENT_SIGNATURE(18,true,true),
	
	/**自动投资签约**/
	AUTO_INVEST_SIGNATURE(19,true,true,"自动投标签约成功","/front/account/auditmaticInvest","投标机器人"),
	
	/**用户绑卡**/
	USER_BIND_CARD(20,true,true),
	
	/**提现复核**/
	CASH_AUDIT(21,true,true),
	
	/**资金冻结**/
	USRFREEZE(22,true,true),
	
	/**放款**/
	LOANS(23,true,true),
	
	/*** 垫付(本金垫付)*/
	ADVANCE_CONFIRM(24,true,true),
	
	/**本金垫付后借款人还款**/
	ADVANCE_REPAYMENT(25,true,true,"还款成功","/front/account/myLoanBills","我的借款账单"),
	
	/**转账**/
	TRANSFER(26,true,true),
	
	/**CPS推广**/
	GRANTCPS(27,true,true),
	
	/**投标奖励**/
	AWARD(28,true,true),
	
	/**借款管理费**/
	BID_FEE(29,true,true),
	
	/**佣金发放**/
	GRANT_INVITATION(30,true,true),
	
	/**投资超标**/
	OVER_BIDINVEST(31,true,true),
	
	/**用户支付**/
	USER_ACCOUNT_PAY(32,true,true),
	
	/**代理充值**/
	AGENTRECHARGE(33,true,true),
	
	/**用户登录**/
	LOGIN_ACCOUNT(34,true,false),
	
	/**查询用户绑定银行卡列表**/
	QUERY_BINDED_CARDS(35,true,false),
	
	/*** 线下收款(登记担保方)*/
	OFFLINE_REPAYMENT(36,true,true);
	
	private int typeNum;  //类型编号

	private boolean isPrintLog;  //是否将接口参数打印到日志文件
	
	private boolean isSaveLog;  //是否将接口参数保存到数据库
	
	private String successUrl;  //交易成功跳转的页面
	private String successUrlDesc;  //交易成功跳转的页面的描述
	private String successTip;  //交易成功提示语
	
	private String failedUrl;  //交易失败跳转的页面
	private String failedUrlDesc;  //交易失败跳转的页面的描述
	
	private PayType(int typeNum, boolean isPrintLog, boolean isSaveLog){
		this.typeNum = typeNum;
		this.isPrintLog = isPrintLog;
		this.isSaveLog = isSaveLog;
	}
	
	private PayType(int typeNum, boolean isPrintLog, boolean isSaveLog,
			String successTip, String successUrl, String successUrlDesc ){
		this.typeNum = typeNum;
		this.isSaveLog = isSaveLog;
		this.successTip = successTip;
		this.successUrl = successUrl;
		this.successUrlDesc = successUrlDesc;
	}
	
	private PayType(int typeNum, boolean isPrintLog, boolean isSaveLog, 
			String successTip, String successUrl, String successUrlDesc,
			String failedUrl, String failedUrlDesc){
		this.typeNum = typeNum;
		this.isSaveLog = isSaveLog;
		this.successTip = successTip;
		this.successUrl = successUrl;
		this.successUrlDesc = successUrlDesc;
		this.failedUrl = failedUrl;
		this.failedUrlDesc = failedUrlDesc;
	}
	
	/**
	 * 根据类型编号获取相应枚举名称
	 * 
	 * @param typeNum
	 * @return
	 */
	public static String getNameByTypeNum(String typeNum){
		PayType[] types = values();
		for(PayType p : types){
			if(p.toString().equals(typeNum)){
				return p.name();
			}
		}
		
		Logger.info("编号为%s的枚举不存在", typeNum);
		return null;
	}
	
	public boolean isPrintLog() {
		return isPrintLog;
	}

	public void setPrintLog(boolean isPrintLog) {
		this.isPrintLog = isPrintLog;
	}

	public int getTypeNum() {
		return typeNum;
	}
	
	public boolean getIsSaveLog() {
		return isSaveLog;
	}

	public void setSaveLog(boolean isSaveLog) {
		this.isSaveLog = isSaveLog;
	}

	public void setTypeNum(int typeNum) {
		this.typeNum = typeNum;
	}

	public String getSuccessUrl() {
		return successUrl;
	}

	public void setSuccessUrl(String successUrl) {
		this.successUrl = successUrl;
	}

	public String getSuccessUrlDesc() {
		return successUrlDesc;
	}

	public void setSuccessUrlDesc(String successUrlDesc) {
		this.successUrlDesc = successUrlDesc;
	}

	public String getSuccessTip() {
		return successTip;
	}

	public void setSuccessTip(String successTip) {
		this.successTip = successTip;
	}

	public String getFailedUrl() {
		return failedUrl;
	}

	public void setFailedUrl(String failedUrl) {
		this.failedUrl = failedUrl;
	}

	public String getFailedUrlDesc() {
		return failedUrlDesc;
	}

	public void setFailedUrlDesc(String failedUrlDesc) {
		this.failedUrlDesc = failedUrlDesc;
	}
}
