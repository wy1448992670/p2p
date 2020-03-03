package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import business.CreditLevel;
import play.db.jpa.Model;
import utils.ErrorInfo;
import utils.Security;


/**
 * 未通过的债权转让标
* @author lwh
* @version 6.0
* @created 2014年4月21日 下午3:45:59
 */

@Entity
public class v_debt_no_pass_transfers extends Model {

	public String name;
	public String title;
	public String credit_level_image_filename;
	public Long order_sort;
	public Integer period;
	public Integer period_unit;
	public Double invest_amount;
	public Double bid_amount;
	public Double apr;
	public Integer type;
	public Integer status;
	public Long bid_id;
	public Long user_id;
	public Double transfer_price;
	public Double debt_amount;
	public Date time;
	public Date start_time;
	public Date failure_time;
	public String no;
	public String no_through_reason;
	public Long has_payback_period;//已还账单
	public Long overdue_payback_period;//逾期账单
	public Double receiving_amount;//应收本息
	public Double has_received_amount;//已收本息
	public Double remain_received_corpus;//剩余应收本金
	
	@Transient
	public CreditLevel creditLevel;
	
	public CreditLevel getCreditLevel() {
		
		ErrorInfo error = new ErrorInfo();
		
		return CreditLevel.queryUserCreditLevel(this.user_id, error);
	}
	
	@Transient
	public String sign;

	public String getSign() {
		return Security.addSign(this.id,Constants.BID_ID_SIGN);
	}
}

	