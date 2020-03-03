package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:22:49
 */
@Entity
public class v_user_reported_info extends Model {
	public Date register_time;
	public String name;
	public double credit_score;
	public String email;
	public String mobile;
	public int is_allow_login;
	public double user_amount;
	public double recharge_amount;
	public int invest_count;
	public double invest_amount;
	public long bid_count;
	public double bid_amount;
	public long overdue_bill_count;
	public long reported_count;
	public String credit_level_image_filename;
	
	@Transient
	public String sign;//加密ID

	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}
}
