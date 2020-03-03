package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

@Entity
public class v_bill_invest_detail extends Model {
    
	public long user_id;
	public long invest_id;
  	public Date receive_time;
  	public String name;
  	public int current_period;
  	public Date audit_time;
	public String repayment_type;
	public Double apr;
	public long bid_id;
	public String title;
	public int invest_status;
	public Double amount;
	public int loan_periods;
	public int invest_periods;
	public Double receive_corpus;
	public Double still_receiving_corpus;
	public Double should_received_amount;
	public Double should_receive_all_amount;
	public int has_payed_periods;
	public Double has_received_amount;
	public int has_received_periods;
	public Double loan_principal_interest;
	public Double current_receive_amount;
	public Double invest_amount;
	public String invest_number;
	
	@Transient
	public String sign;
	
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
	}
}
