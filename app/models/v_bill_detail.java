package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

@Entity
public class v_bill_detail extends Model {

	public int status;
	public long user_id;
	public Date repayment_time;
	public Date produce_bill_time;
	public String repayment_type;
	public double apr;
	public double user_amount;
	public double user_balance;
	public String user_name;
	public long bid_id;
	public String bid_title;
	public double loan_amount;
	public int loan_periods;
	public int current_period;
	public Double current_pay_amount;
	public double loan_principal_interest;
	public int has_payed_periods;
	public String bill_number;
	
	@Transient
	public String sign;
	
	public Double service_amount;//服务费
	public Double repayment_interest;//应还利息
	public String tag;
	public Double repayment_corpus;//应还利息
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
	}
}
