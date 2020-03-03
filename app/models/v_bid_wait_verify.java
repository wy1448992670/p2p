package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import play.db.jpa.Model;
import utils.ErrorInfo;
import utils.ServiceFee;
import business.CreditLevel;

/**
 * 待验证的借款标列表
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-12-11 下午04:01:29
 */
@Entity
public class v_bid_wait_verify extends Model {
	public String bid_no;
	public String title;
	public Long user_id;
	public String user_name;
	public Integer product_id;
	public String small_image_filename;
	public Double apr;
	public Integer period;
	public Integer period_unit;
	public Date time;
	public Double amount;
	public Integer status;
	@Transient
	public Double capital_interest_sum;
	public Integer product_item_count;
	public String mark;
	public String product_name;
	public Integer user_item_count_true;
	public Integer user_item_count_false;
	public Integer repaymentId;

	@Transient
	public CreditLevel creditLevel;

	public Double getCapital_interest_sum() {
		double rate = ServiceFee.interestCompute(this.amount, this.apr,
				this.period_unit, this.period, this.repaymentId);

		return this.amount + rate;
	}

	/**
	 * 信用积分
	 */
	public CreditLevel getCreditLevel() {
		return CreditLevel.queryUserCreditLevel(this.user_id, new ErrorInfo());
	}
}
