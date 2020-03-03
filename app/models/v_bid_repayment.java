package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 已完成的借款标列表
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-21 上午10:47:08
 */
@Entity
public class v_bid_repayment extends Model {
	public String bid_no;
	public Date time;
	public String title;
	public Long user_id;
	public String user_name;
	public String credit_level_image_filename;
	public Double amount;
	public Integer product_id;
	public String small_image_filename;
	public String product_name;
	public Integer period_unit;
	public Double apr;
	public Integer period;
	public Integer status;
	public Date audit_time;
	public Date last_repay_time;
	public String repayment_type_name;
	public Integer repayment_count;
	public Integer overdue_count;
	public Double capital_interest_sum;
	public Long manage_supervisor_id;
	public String supervisor_name;
	
	@Transient
	public String sign;
	
	/* 2014-11-16添加，上面的sign加密规则搞错了，怕改了有影响故此新加了一个属性*/
	@Transient
	public String sign2;
	
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
	}
	
	public String getSign2() {
		return Security.addSign(this.id, Constants.BID_ID_SIGN);
	}
}