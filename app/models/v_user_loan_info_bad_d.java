package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 部门账单管理--坏账会员管理
 */

@Entity
public  class v_user_loan_info_bad_d extends Model {
	
	//public long supervisor_id;
	public String supervisor_name;
	public String name;
	public Date register_time;
	public double user_amount;
	public Date last_login_time;
	public int bid_count;
	public double bid_amount;
	public int invest_count;
	public double invest_amount;
	public int bid_loaning_count;
	public int bid_repaymenting_count;
	public int overdue_bill_count;
	public int bad_bid_count;
	public double bad_bid_amount;
	public String credit_level_image_filename;
	
	@Transient
	public String sign;//加密ID
	
	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}
}