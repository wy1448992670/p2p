package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

@Entity
public class v_user_cps_info extends Model {
	
	public Date register_time;
	public String name;
	public double credit_score;
	public String email;
	public String mobile;
	public boolean is_allow_login;
	public boolean vip_status;
	public long recommend_user_id;
	public String recommend_user_name;
	public boolean is_blacklist;
	public double user_amount;
	public double recharge_amount;
	public double invest_amount;
	public double bid_amount;
	public double commission_amount;
	public String credit_level_image_filename;
	
	@Transient
	public String sign;//加密ID

	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}
}