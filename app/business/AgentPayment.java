package business;

import gateway.tonglian.base.PropertyConfig;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Query;

import models.t_user_agent_pay;
import models.t_users;
import models.v_user_for_details;

import org.apache.commons.lang.StringUtils;

import payment.PaymentProxy;
import play.Logger;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import constants.Constants;
import constants.DealType;
import constants.IPSConstants;
import constants.IPSConstants.AgentPayStatus;
import constants.Templets;
import constants.UserEvent;
import controllers.front.account.TongLianPaymentAction;

/**
 * 闪电快充（代理充值）业务类
 *
 * @author hys
 * @createDate  2015年7月10日 下午5:48:12
 *
 */
public class AgentPayment {
	
	public long userId;  //用户id
	public String userName;  //用户名
	public String usercustId;  //资金托管账号
	public Date time;  //创建时间
	public double amount;  //充值金额
	public int status;  //状态（0：用户支付中，1：已支付，2：商户转账中，3：已转账）
	public Date paidTime;  //用户完成支付充值金额的时间
	public Date transferedTime;  //商户完成转账给用户的时间
	public Date completedTime;  //代理充值完成的时间
	public int agent;  //代理充值的网关，目前支持：通联
	public long agentOrderNo;  //用户支付的流水号
	public long merOrderNo;  //商户转账给用户的流水号
	
	public AgentPayment(){}
	
	/**
	 * 创建代理充值对象
	 * 
	 * @param agentOrderNo  用户支付流水号
	 * @param error
	 */
	public AgentPayment(long agentOrderNo, ErrorInfo error){
		error.clear();
		
		t_user_agent_pay uap = queryAgentPayByAgentNo(agentOrderNo);
		
		if(uap == null){
			error.code = -1;
			error.msg = "用户支付订单[%s]不存在";
			Logger.info("创建代理充值对象时，订单[%s]不存在", agentOrderNo);
			
			return;
		}
		
		this.userId = uap.user_id;
		this.userName = uap.user_name;
		this.usercustId = uap.usercustId;
		this.time = uap.time;
		this.amount = uap.amount;
		this.status = uap.status;
		this.paidTime = uap.paid_time;
		this.transferedTime = uap.transfered_time;
		this.completedTime = uap.completed_time;
		this.agent = uap.agent;
		this.agentOrderNo = uap.agent_order_no;
		this.merOrderNo = uap.mer_order_no;
	}

	/**
	 * 准备数据，请求支付接口
	 * @param bankCode
	 * @param amount
	 * @param error
	 */
	public static void pay(String bankCode, double amount, ErrorInfo error) {
		error.clear();
		
		User user = User.currUser();
		user.setId(user.id);

		if (!Constants.IPS_ENABLE) {
			error.code = -1;
			error.msg = "非资金托管模式，不支持闪电快充";
			
			return;
		}

		if (user.ipsAcctNo == null) {
			error.code = -2;
			error.msg = "未开通资金托管帐号，无法使用闪电快充功能";
			
			return;
		}
		
		String agentOrderNo = User.createBillNo();  //支付流水号
		
		String merOrderNo = User.createBillNo();  //商户转用户的流水号

		// 插入用户充值记录 t_user_recharge_details
		User.sequence(Constants.GATEWAY_TONGLIAN, agentOrderNo, amount, Constants.AGENT_RECHARGE, Constants.CLIENT_PC, error);
		if (error.code < 0){
			return;
		}

		// 插入代理充值记录 t_user_agent_pay
		insertUserAgentPay(user, agentOrderNo, merOrderNo, amount, AgentPayStatus.AGENT_SUBMIT, IPSConstants.AGENT_GATE_WAY_TONGLIAN, error);
		if (error.code < 0){
			return;
		}
		
		//请求通联支付（网银支付）接口
		TongLianPaymentAction.cyberbankPay(agentOrderNo, bankCode, amount, user.ipsAcctNo);

	}
	
	/**
	 * 用户支付成功
	 * 
	 * @param amount  用户实际支付金额
	 * @param error
	 */
	public void paySuccess(double amount, ErrorInfo error) {
		error.clear();

		// 用户支付成功   支付中->用户已支付  
		updateAgentRechargeStatus(this.agentOrderNo, AgentPayStatus.AGENT_SUBMIT, AgentPayStatus.AGENT_PREPAID, error);
		if (error.code < 0) {
			Logger.info("[%s-回调]--[订单:%s]updateAgentRechargeStatus（支付中->用户已支付）时，[%s]", PropertyConfig.name, this.agentOrderNo, error.msg);
			
			return;
		}
		
		JPAUtil.transactionCommit();  //提交事务,转账只执行一次。
		

		if(this.amount != amount){
			Logger.info("用户支付成功,准备转账给用户时，用户实际支付金额[%s]与计划充值金额[%s]不一致", amount, this.amount);
		}
		
		transfer(amount, error);  //转账给用户
	}
	
	/**
	 * 商户转账给用户
	 * 
	 * @param amount  转账金额
	 * @param error
	 */
	public void transfer(double amount, ErrorInfo error) {
		error.clear();
		
		// 查询用户资金托管账号
		String ipsAcctNo = User.queryIpsAcctNo(this.userId, error);

		if (ipsAcctNo == null) {
			error.code = -1;
			error.msg = "未开通资金托管帐号，无法转账给用户";
			
			return;
		}

		// 商户转账给用户--调用商户转用户接口（WS）
		PaymentProxy.getInstance().agentRecharge(error, Constants.PC, ipsAcctNo, amount, this.merOrderNo, this.agentOrderNo);
		if (error.code < 0) {
			Logger.error("[%s-回调]--" + "[订单:%s]agentRecharge时，[%s]", PropertyConfig.name, this.agentOrderNo, error.msg);
			
			return ;
		}
	}
	
	/**
	 * 商户转账给用户
	 * 
	 * @param amount  转账金额
	 * @param error
	 */
	public void reTransfer(ErrorInfo error) {
		error.clear();
		
		// 查询商户资金是否充足
		Map<String,Object> maps = PaymentProxy.getInstance().queryAmountByMerchant(error, Constants.PC);
		Double merBalance = Double.valueOf(maps.get("AvlBal")+"");
		
		if (merBalance < amount) {
			error.code = -1;
			error.msg = "商户资金不足，无法转账给用户";
			
			return;
		}
		
		//更新转账流水号，并防止重复转账
		updateMerOrderNo(error);
		if(error.code < 0){
			
			return;
		}
		
		transfer(this.amount, error);

	}
	
	/**
	 * 更新转账流水号，重新转账
	 * @param error
	 */
	private void updateMerOrderNo(ErrorInfo error) {
		error.clear();
		
		long merOrderNo = Long.parseLong(User.createBillNo());
		
		String update_sql = "update t_user_agent_pay set mer_order_no = ? where agent_order_no = ? and mer_order_no = ?";

		Query query = JpaHelper.execute(update_sql).setParameter(1, merOrderNo).setParameter(2, this.agentOrderNo).setParameter(3, this.merOrderNo);

		int row = 0;
		
		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error("更新转账流水号时异常，%s", e.getMessage());
			
			error.code = -1;
			error.msg = "更新转账流水号时异常";
			
			return;
		}
		
		if(row == 0){  //转账流水号已经被更新过了，防止重复转账
			JPA.setRollbackOnly();
			Logger.info("更新转账流水号时，已执行");
			
			error.code = Constants.ALREADY_RUN;
			error.msg = "已执行";
			
			return;
			
		}
		
		this.merOrderNo = merOrderNo;
	}

	/**
	 * 商户转账给用户成功
	 * 
	 * @param amount  商户实际转账给用户的金额
	 * @param error
	 */
	public void transferSuccess(double amount, ErrorInfo error) {
		error.clear();
		
		// 商户转账给用户成功   用户已支付->商户已转账  
		updateAgentRechargeStatus(this.agentOrderNo, AgentPayStatus.AGENT_PREPAID, AgentPayStatus.AGENT_TRANSFER, error);
		if (error.code < 0) {
			Logger.info("[%s-回调]--[订单:%s]updateAgentRechargeStatus(用户已支付->商户已转账)，[%s]", PropertyConfig.name, this.agentOrderNo, error.msg);
		
			return;
		}
		
		// 代理充值完成 
		lastBackHandle(amount, error);
		if (error.code < 0) {
			Logger.error("[%s-回调]--[订单:%s]lastBackHandle时，[%]", PropertyConfig.name, this.agentOrderNo, error.msg);
			
			return;
		}
		
		Logger.info("[%s-回调]--[订单:%s]充值成功，结束", PropertyConfig.name, this.agentOrderNo);
	}
	

	/**
	 * 代理充值完成，修改用户充值记录为成功，增加用户金额，通知用户
	 * 
	 * @param amount 商户实际转账给用户的金额
	 * @param error
	 */
	private void lastBackHandle(double amount, ErrorInfo error) {
		error.clear();
		
		// 代理充值完成   商户已转账->代理充值完成  
		updateAgentRechargeStatus(this.agentOrderNo, AgentPayStatus.AGENT_TRANSFER, AgentPayStatus.AGENT_COMPLETED, error);
		if (error.code < 0) {
			Logger.info("[%s-回调]--[订单:%s]updateAgentRechargeStatus(商户已转账->代理充值完成)，[%s]", PropertyConfig.name, agentOrderNo, error.msg);
			return;
		}
		
		//更新充值结果，成功	
		String sql = "update t_user_recharge_details set is_completed = ?, completed_time = ? where pay_number = ? and is_completed = 0";
		int rows = 0;
		
		try{
			rows = JPA.em().createQuery(sql).setParameter(1, true).setParameter(2, new Date()).setParameter(3, String.valueOf(this.agentOrderNo)).executeUpdate();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.error("代理充值完成，修改用户充值记录为成功，增加用户金额，通知用户时，%s" + e.getMessage());
			
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();
			
			return;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = Constants.ALREADY_RUN;
			error.msg = "已执行";
			
			return ;
		}
		
		// 验证
		DataSafety data = new DataSafety();
		data.id = this.userId;
		if (!data.signCheck(error)) {
			return;
		}

		//  增加用户资金
		DealDetail.addUserFund(this.userId, amount);  
		
		v_user_for_details forDetail = DealDetail.queryUserBalance(this.userId, error);
		if (error.code < 0) {
			return;
		}
		
		DealDetail dd = new DealDetail(this.userId, DealType.RECHARGE_USER, this.amount, -1l, forDetail.user_amount, forDetail.freeze,
				forDetail.receive_amount, "闪电快充");
		dd.addDealDetail(error);;
		if (error.code < 0) {
			return;
		}
		
		//平台支出，转账金额
		DealDetail.addPlatformDetail(DealType.AGENT_RECHARGE, this.agentOrderNo, -1, this.userId , DealType.ACCOUNT, amount, 2, "闪电快充，商户转账", error);
		
		// 修改sign
		data.updateSignWithLock(this.userId, error);
		if (error.code < 0) {
			return;
		}
		
		// 插入用户事件
		DealDetail.userEvent(this.userId, UserEvent.RECHARGE, "闪电快充", error);
		if (error.code < 0) {
			return;
		}
		
		// 发送站内信 [userName]充值了￥[money]元
		TemplateStation station = new TemplateStation();
		station.id = Templets.M_USER_RECHARGE;
		if (station.status) {
			String mContent = station.content.replace("userName", this.userName);
			mContent = mContent.replace("money", String.format("%.2f", this.amount));
			mContent = mContent.replace("date", DateUtil.dateToString(new Date()));
			TemplateStation.addMessageTask(userId, station.title, mContent);
		}
	}

	/**
	 * 插入代理充值记录
	 * 
	 * @param user
	 * @param pMerBillNo
	 * @param money
	 * @param error
	 */
	private static void insertUserAgentPay(User user, String agentOrderNo, String merOrderNo, double amount, int status, int agent, ErrorInfo error) {
		error.clear();
		
		t_user_agent_pay pay = new t_user_agent_pay();
		pay.user_id = user.id;
		pay.user_name = user.name;
		pay.usercustId = user.ipsAcctNo;
		pay.time = new Date();
		pay.amount = amount;
		pay.status = status;  
		pay.agent = agent;  
		pay.agent_order_no = Long.parseLong(agentOrderNo);
		pay.mer_order_no = Long.parseLong(merOrderNo);

		try {
			pay.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("添加代理充值记录时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "添加代理充值记录时出现异常!";
			
			return;
		}
	}
	
	/**
	 * 更新代理充值记录状态
	 * 
	 * @param agentOrderNo  用户支付流水号
	 * @param fromStatus  原始状态
	 * @param toStatus	修改后状态
	 */
	private static void updateAgentRechargeStatus(long agentOrderNo, int fromStatus, int toStatus, ErrorInfo error) {
		error.clear();
		
		String update_sql = "";
		
		switch (toStatus) {
		case AgentPayStatus.AGENT_PREPAID:   //用户支付成功
			update_sql = "update t_user_agent_pay set status = ?, paid_time = ? where agent_order_no = ? and status = ?";
			break;
		case AgentPayStatus.AGENT_TRANSFER:   //商户转账给用户成功
			update_sql = "update t_user_agent_pay set status = ?, transfered_time = ? where agent_order_no = ? and status = ?";
			break;
		case AgentPayStatus.AGENT_COMPLETED:   //代理充值完成
			update_sql = "update t_user_agent_pay set status = ?, completed_time = ? where agent_order_no = ? and status = ?";
			break;
		default:
			error.code = -1;
			error.msg = "更新代理充值记录状态时，参数非法：toStatus=" + toStatus;
			return;
		}

		Query query = JpaHelper.execute(update_sql)
				.setParameter(1, toStatus)
				.setParameter(2, new Date())
				.setParameter(3, agentOrderNo)
				.setParameter(4, fromStatus);
	
		int update_num = 0;

		try {
			update_num = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error("更新代理充值记录状态时异常，%s", e.getMessage());
			
			error.code = -1;
			error.msg = "更新代理充值记录状态时异常";
			
			return;
		}

		if (update_num == 0) {
			JPA.setRollbackOnly();
			error.code = Constants.ALREADY_RUN; // 已执行
			error.msg = "已执行";
			return;
		}
	}
	
	/**
	 * 查询代理充值记录
	 * 
	 * @param agentOrderNo  用户支付充值金额的流水号
	 * @return 
	 */
	private static t_user_agent_pay queryAgentPayByAgentNo(long agentOrderNo) {
		return t_user_agent_pay.find("agent_order_no = ?", agentOrderNo).first();
	}
	
}
