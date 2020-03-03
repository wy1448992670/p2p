package payment;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import utils.ErrorInfo;

import business.User;

/**
 * p2p调用托管实现类接口业务标准
 * 
 * @author xiaoqi
 *
 */
public interface PaymentInterface {

	/**
	 * 开户
	 * @param user
	 * @param obj
	 * @return
	 */
	public Map<String, Object> register(ErrorInfo error, int client,  Object... obj);
	
	/**
	 * 充值
	 * @param obj
	 * @return
	 */
	public Map<String, Object> recharge(ErrorInfo error, int client, Object... obj); 
	
	/**
	 * 标的发布
	 * @param error
	 * @param obj
	 * @return
	 */
	public Map<String, Object> bidCreate(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 投标
	 * @param obj
	 * @return
	 */
	public Map<String, Object> invest(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 自动投标签约
	 * @param error
	 * @param client
	 * @param obj
	 * @return
	 */
	public Map<String, Object> autoInvestSignature(ErrorInfo error, int client, Object... obj);
	
	
	/**
	 * 自动投标
	 * @param obj
	 * @return
	 */
	public Map<String, Object> autoInvest(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 提现
	 * @param obj
	 * @return
	 */
	public Map<String, Object> withdraw(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 标的审核通过（放款）
	 * @param obj
	 * @return
	 */
	public Map<String, Object> bidAuditSucc(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 标的审核失败(流标)
	 * @param obj
	 * @return
	 */
	public Map<String, Object> bidAuditFail(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 申请vip
	 * @param error
	 * @param obj
	 * @return
	 */
	public Map<String, Object> applyVIP(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 标的资料审核
	 * @param error
	 * @param obj
	 * @return
	 */
	public Map<String, Object> bidDataAudit(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 申请信用额度
	 * @param error
	 * @param obj
	 * @return
	 */
	public Map<String, Object> applyCredit(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 债权转让
	 * @param error
	 * @param obj
	 * @return
	 */
	public Map<String, Object> debtorTransfer(ErrorInfo error, int client, Object... obj); 
	
	/**
	 * 债权转让成交
	 * @param error
	 * @param client
	 * @param pDetails 债权转让明细
	 * @param pOriMerBillNo 登记债权转让投资流水号
	 * @param pBidNo 标的号
	 * @param parentOrderno 父流水号
	 * @return
	 */
	public Map<String, Object> debtorTransferConfirm(ErrorInfo error, int client, LinkedList< Map<String, String>> pDetails, String pBidNo, String parentOrderno, String debtId, String dealpwd); 
	
	
	/**
	 * 垫付
	 * @param error
	 * @param obj
	 * @return
	 */
	public Map<String, Object> advance(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 线下收款
	 * @param error
	 * @param obj
	 * @return
	 */
	public Map<String, Object> offlineRepayment(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 垫付还款
	 * @param error
	 * @param obj
	 * @return
	 */
	public Map<String, Object> advanceRepayment(ErrorInfo error, int client, Object... obj);
	
	
	/**
	 * 还款
	 * @param error
	 * @param obj
	 * @return
	 */
	public Map<String, Object> repayment(ErrorInfo error, int client, Object... obj); 
	
	/**
	 * 自动还款
	 * @param error
	 * @param obj
	 * @return
	 */
	public Map<String, Object> autoRepayment(ErrorInfo error, int client, Object... obj); 
	
	
	/**
	 * 托管用户账户余额余额查询
	 * @param error
	 * @param obj
	 * @return
	 */
	public Map<String, Object> queryAmount(ErrorInfo error, int client, Object... obj);
	
	
	/**
	 * 银行列表查询
	 * @param error
	 * @param obj
	 * @return
	 */
	public List<Map<String, Object>> queryBanks(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 用户绑卡
	 * @param error
	 * @param obj
	 * @return
	 */
	public Map<String, Object> userBindCard(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 自动还款签约
	 * @param error
	 * @param client
	 * @param obj
	 * @return
	 */
	public Map<String, Object> autoRepaymentSignature(ErrorInfo error, int client, Object... obj);
	
	/**
	 * cps奖励发放
	 * @param error
	 * @param client
	 * @param obj
	 * @return
	 */
	public Map<String, Object> grantCps(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 佣金发放
	 * @param error
	 * @param client
	 * @param obj
	 * @return
	 */
	public Map<String, Object> grantInvitation(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 代理充值（闪电快充），商户转账给用户
	 * @param error
	 * @param client
	 * @param obj
	 * @return
	 */
	public Map<String, Object> agentRecharge(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 商户充值
	 * @param error
	 * @param client
	 * @param obj
	 * @return
	 */
	public Map<String, Object> merchantRecharge(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 商户提现
	 * @param error
	 * @param client
	 * @param obj
	 * @return
	 */
	public Map<String, Object> merWithdrawal(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 查询商户余额
	 * @param error
	 * @param client
	 * @param obj
	 * @return
	 */
	public Map<String, Object> queryAmountByMerchant(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 用户登入
	 * @param error
	 * @param client
	 * @param obj
	 * @return
	 */
	public Map<String, Object> loginAccount(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 日志对账
	 * 
	 * @param error
	 * @param client
	 * @param obj
	 * @return
	 */
	public Map<String, Object> queryLog(ErrorInfo error, int client, Object... obj);
	
	/**
	 * 查询用户绑定银行卡列表
	 * 
	 * @param error
	 * @param client
	 * @param obj
	 * @return
	 */
	public Map<String, Object> queryBindedBankCard(ErrorInfo error, int client, Object... obj);
	
}
