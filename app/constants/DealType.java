package constants;

public class DealType {

	/*命名规范：解冻 THAW  冻结 FREEZE 扣除 CHARGE*/
	
	public static final int RECHARGE_USER = 1; //会员充值   (t_system_recharge_completed_sequences id)
	public static final int RECHARGE_HAND = 2; // 手工充值   (t_supervisors id)
	public static final int RECHARGE_OFFLINE = 3; // 线下充值   (t_supervisors id)

	public static final int PRICIPAL_PAY = 7 ; //本金垫付收款 (t_bills id)
	public static final int OVER_RECEIVE = 8; // 逾期收款     (t_bills id)
	public static final int NOMAL_RECEIVE = 9 ; //正常收款,普通标投资回款     (t_bill_invests id)
	public static final int AUCTION_DEBT_SUCCESS = 10 ; //竞拍成功    (t_invest_transfer_details 或 t_invest_transfers id)
	public static final int DIRECT_DEBT_SUCCESS = 11; // 定向转让成功 (t_invest_transfer_details 或 t_invest_transfers id)
	public static final int ADD_OVERDUE_FEE = 12; // 投资人获得逾期费 (t_invests id)
	public static final int OFFLINE_COLLECTION = 13 ; //线下收款     (t_bills id)
	public static final int ROLLBACK_WITHDRAWAL = 14 ; //提现回退     (t_user_withdrawals id)
	
	public static final int DEBT_TRANSFER_RECEIVE = 15 ; //债权转让投资回款     (t_debt_bill_invest id)
	/*----------用户可用金额增加-------------*/
	public static final int THAW_WITHDRAWALT = 101; //解冻余额提现  (t_user_withdrawals id)
	public static final int t102 = 102; // 解冻奖励提现  (t_user_withdrawals id)
	public static final int RELIEVE_FREEZE_FUND = 103; // 解冻借款保证金 (t_bids id)
	public static final int THAW_FREEZE_INVESTAMOUNT = 104; // 解冻投标金额   （t_invests id）
	public static final int THAW_FREEZE_AUCTIONAMOUNT = 105; // 解冻竞拍金额    (t_invest_transfer_details id)
	public static final int ADD_LOAN_FUND = 106; // 增加借款金额 (t_bids id)
	public static final int ADD_LOAN_BONUS = 107; // 增加投标奖励 (t_bids id)
	
	public static final int TRANS_MER_TO_USER = 109; //商户转账给用户
	public static final int RETURN_AUDIT_ITEM_FUND = 110; //返还用户审核资料费
	public static final int THAW_FREEZE_DEBT_INVESTAMOUNT = 111; // 解冻债权转让投标金额   (t_debt_invests id)
	public static final int RETURN_DEBT_TRANSFER_MANAGEFEE = 112; //平台退还债权转让管理费 (t_debt_transfer id)
	public static final int ADD_DEBT_TRANSFER_FUND = 113; //债权转让成功,增加转让人金额 (t_debt_transfer id)
	/*-----------用户冻结金额增加-------------*/
	public static final int FREEZE_WITHDRAWAL = 201; // 冻结余额提现  (t_user_withdrawals id)
	public static final int FREEZE_WITHDRAWAL_P = 202; // 冻结奖励提现  (t_user_withdrawals id)
	public static final int FREEZE_BID_BAIL = 203; // 冻结借款保证金 (t_bids id)
	public static final int FREEZE_SBID_BAIL = 204; // 冻结秒还保证金 (t_bids id)
	public static final int FREEZE_INVEST = 205; // 冻结投标金额   (t_invests id）
	public static final int FREEZE_DEBT = 206; // 冻结竞拍金额   (t_invest_transfer_details id)
	public static final int FREEZE_DEBT_INVEST = 207; // 冻结债权投标金额   (t_debt_invests id）
	
	/*------------用户总金额减少---------------*/
	public static final int CHARGE_HAND = 301; // 手动扣费        (t_supervisors id)
	public static final int CHARGE_RECHARGE_FEE = 302; // 扣除充值手续费  (t_user_recharge_details id)
	public static final int CHARGE_LEFT_WITHDRAWALT = 303; // 扣除余额提现    (t_user_withdrawals id)
	public static final int CHARGE_AWARD_WITHDRAWALT = 304; // 扣除奖励提现    (t_user_withdrawals id)
	public static final int CHARGE_WITHDRAWALT = 305; // 扣除提现手续费  (t_user_withdrawals id)
	public static final int CHARGE_VIP = 306; // 扣除vip费用     (t_user_vip_records id)
	public static final int CHARGE_AUDIT_ITEM = 307; // 扣除审核资料    (t_user_audit_items id)
	public static final int CHARGE_INVEST_FUND = 308; // 扣除投标冻结金额(t_invests id)
	public static final int CHARGE_LOAN_SERVER_FEE = 309; // 扣除借款管理费  (t_bids id)
	public static final int CHARGE_BONUS_FEE = 310; // 扣除借款奖励费  (t_bids id)
	public static final int CHARGE_OVER_PAY = 311; // 逾期还款        (t_bills id)
	public static final int CHARGE_NOMAL_PAY = 312; // 正常还款        (t_bills id)
	public static final int CHARGE_INVEST_FEE = 313; // 扣除理财管理费  (t_invests id)
	public static final int CHARGE_FREEZE_AUCTIONAMOUNT = 314; // 扣除竞拍冻结金额(t_invest_transfer_detailsid)
	public static final int t315 = 315; // 扣除定向转让金额(t_invest_transfer_detailsid)
	public static final int CHARGE_DEBT_TRANSFER_MANAGEFEE = 316; // 扣除债权转让管理费 (t_debt_transfer id)*
	public static final int CHARGE_OVERDUE_FEE = 317; // 扣除逾期费  (t_bill id)
	public static final int OFFLINE_REPAYMENT = 318; // 线下放款
	public static final int BILL_LOAN_SERVER_FEE = 329; // 扣除借款管理费  (t_bills id)
	
	
	public static final int IPS_NORMAL_RECHARGE = 320; //资金托管模式下普通网关充值
	public static final int REFUND_BONUS_FEE = 321; //退回投标奖励费
	public static final int TRANS_USER_TO_MER = 322; //用户转账给商户
	
	public static final int CHARGE_DEBT_INVEST_FUND = 323; // 扣除债权转让投标冻结金额(t_debt_invests id)
	
	public static final int CHARGE_AUTO_REPAYMENT_WITHDRAW = 330;//迁移用户,回款转移
	public static final int CHARGE_AUTO_BALANCE_WITHDRAW = 331;//迁移用户,余额转移
	/*------------其他记录----------*/
	public static final int CPS_COUNT = 51; // cps推广奖励（按个数）(t_users id)
//	public static final int CPS_AMOUNT = 52; // cps推广奖励（按理财金额）(t_invests id)
	public static final int INVITATION_AMOUNT = 53; // 财富圈，返佣记录
	public static final int CPS_RATE_COUNT = 55; // cps推广奖励（按理财金额分成）(t_users id)
	
	public static final int RED_RELEASESYS = 88; // 系统红红包发放
	
	/*************************************平台交易类型***********************************/
	
	public static final int ADD_CAPITAL = 1; //增加本金保障 (t_supervisors id)
	public static final int VIP_FEE = 2; //Vip会员费(t_user_vip_records id)
	public static final int RECHARGE_FEE = 3; //充值手续费(t_user_recharge_details id)
	public static final int ADVANCE_FEE = 4; //垫付理财账单(t_bills id)
	public static final int LOAN_FEE = 5; //借款管理费(t_bids id)
	public static final int INVEST_FEE = 6; //理财管理费(t_invests id)
	public static final int TRANSFER_FEE = 7; //债权转让管理费(t_debt_transfer id)
//	public static final int OVERDUE_FEE = 8; //逾期罚息费(t_bills id)
//	public static final int WITHDRAWAL_FEE = 9; //提现手续费(t_user_withdrawals id)
	public static final int AUDIT_FEE = 10; //资料审核费(t_user_audit_items id)
	public static final int WITHDRAWAL_FEE = 11; //提现手续费(t_t_user_withdrawals id)
//	public static final int TRANSFER_USER_TO_MER = 12; //用户转账给商户
//	public static final int TRANSFER_MER_TO_USER = 13; //商户转账给用户
	public static final int OFFLINE_PAY_FEE = 12; //	线下收款(t_bills id)
	public static final int CPS = 13; //cps推广费
	public static final int COMMISSION_DISCOUNT  = 14; //返佣
	public static final int AGENT_RECHARGE  = 15; //闪电快充
	public static final int RED_RELEASE = 16; //  系统红包发放
	public static final int BID_INCREASE_RATE = 17; //  标的加息
	public static final int INVEST_INCREASE_COUPON = 18; // 加息劵
	public static final int BILL_LOAN_FEE = 19; //借款管理费(t_bills id)
	public static final int PAYMENT_ON_COMPANY = 20; //公司垫付理财账单(t_bills id)
	
	//1 收入 2 支出 3 复合型（收入或支出）
	public static final int INCOME = 1; 
	public static final int PAY = 2; 
	public static final int INCOME_OR_PAY = 3; 
	
	/************************************支付方式*************************************/
	
	public static final String HAND = "手工充值";
	public static final String ACCOUNT = "账户转账";
	public static final String PAYMENT = "国付宝";
	
	
}
