package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

@Entity
public class v_bill_detail_v1 extends Model {

	public int status;
	public long user_id;
	public Date repayment_time;
	public Date produce_bill_time;//放款时间
	public String repayment_type;
	public double apr;
	public double user_amount; 
	public String user_name;
	public long bid_id;
	public String bid_title;
	public double loan_amount;
	public int loan_periods;
	public int current_period;
	public double current_pay_amount;
	//public double loan_principal_interest;
	public int has_payed_periods; 
	public int borrow_period;
	public int period_unit;
	public double remain_repay_amount;
	public int overdue_mark;
	
	@Transient
	public String sign;
	
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
	}
}
