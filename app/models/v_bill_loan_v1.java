package models;
// default package

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import business.Bill;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 我的借款账单
 */

@Entity
public  class v_bill_loan_v1 extends Model {
	
	public long user_id;
	public String title;
	public int is_overdue; 
	public double repayment_amount;
	//public int status;
	public Date repayment_time;//最近一期未还款的时间
	public double amount;
	public int loan_periods;
	public int has_payed_periods;
	public int period_unit;
	public int borrow_period;
	public int bid_status; //还款状态
	public Date audit_time; //放款审核时间
	@Transient
	public String sign;
	
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
	}
	
	/*@Transient
	public int chechPeriod;*/

	/*public int getChechPeriod() {
		return Bill.checkPeriod(this.bid_id, this.period);
	}*/
}