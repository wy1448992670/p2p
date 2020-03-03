package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import business.CreditLevel;
import play.db.jpa.Model;
import utils.ErrorInfo;

/**
 * 前台--提现记录
 * @author cp
 * @version 6.0
 * @created 2014年6月13日 下午4:13:52
 */

@Entity
public class v_user_withdrawals extends Model {

	public long user_id;
	
	public String name;
	
	public double amount;
	
	public String account;
	
	public String bank_name;
	
	public String branch_bank_name;  //支行名称
	
	public String province;  //支行所在省
	
	public String city;  //支行所在市
	
	public String account_name;
	
	public int status;
	
	public Date time;
	
	public Date audit_time;
	
	public Date pay_time;
	
	/**
	 *service_fee：提现手续费
	 */
	public double service_fee;
	
	@Transient
	public CreditLevel creditLevel;
	
	public CreditLevel getCreditLevel() {
		
		ErrorInfo error = new ErrorInfo();
		
		return CreditLevel.queryUserCreditLevel(this.user_id, error);
	}
	
	public v_user_withdrawals() {
		
	}
	
	/**
	 *前台--我的账户--提现
	 */
	public v_user_withdrawals(double amount, String bank_name, String account,
			Date time, Date pay_time, int status) {
		
		this.amount = amount;
		this.bank_name = bank_name;
		this.account = account;
		this.time = time;
		this.pay_time = pay_time;
		this.status = status;
		
	}
	
	/**
	 * 后台--提现管理
	 */
	public v_user_withdrawals(double amount, String bank_name, 
			String account, String account_name) {
		
		this.amount = amount;
		this.bank_name = bank_name;
		this.account = account;
		this.account_name = account_name;
	}
	
	/**
	 * 后台--提现管理--付款通知
	 */
	public v_user_withdrawals(String name, double amount, String bank_name, 
			String account, String account_name) {
		
		this.amount = amount;
		this.bank_name = bank_name;
		this.account = account;
		this.account_name = account_name;
	}
	
	/**
	 * 待付款提现列表--付款单
	 */
	public v_user_withdrawals(String name, double amount, Date time, Date audit_time,
			int status, String account, String account_name, String bank_name) {
		
		this.name = name;
		this.amount = amount;
		this.time = time;
		this.audit_time = audit_time;
		this.status = status;
		this.account = account;
		this.account_name = account_name;
		this.bank_name = bank_name;
	}
}
	
