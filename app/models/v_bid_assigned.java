package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;
import utils.ServiceFee;

/**
 * 待放款/已放款 借款标列表
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-5-28 上午10:32:40
 */
@Entity
public class v_bid_assigned extends Model {
	public String bid_no;
	public String title;
	public long user_id;
	public String user_name;
	public double amount;
	public String product_name;
	public String small_image_filename;
	public double apr;
	public int period;
	public int period_unit;
	public Date audit_time;
	public long manage_supervisor_id;
	public String supervisor_name;
	public int repaymentId;
	public String credit_level_image_filename;
	@Transient
	public double capital_interest_sum;
	
	@Transient
	public String sign;
	@Transient
	public String signUserId;
	
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		return Security.addSign(this.id, Constants.BID_ID_SIGN);
	}
	
	public double getCapital_interest_sum() {
		double rate = ServiceFee.interestCompute(this.amount, this.apr, this.period_unit, this.period, this.repaymentId);
		
		return this.amount + rate;
	}

	/**
	 * 获取加密user_id
	 */
	public String getSignUserId() {
		return Security.addSign(this.user_id, Constants.USER_ID_SIGN);
	}
}
