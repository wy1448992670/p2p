package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

@Entity
public class v_user_invest_info extends Model {

	public Date register_time;
	public String name;
	public String email;
	public String mobile;
	public boolean is_allow_login;
	public boolean is_migration;
	public double credit_score;	
	public double user_amount;
	public int master_identity;
	public double recharge_amount;
	public long invest_count;
	public double invest_amount;
	public int invest_receive_count;
	public double receive_amount;
	public long transfer_count;
	public String credit_level_image_filename;
	public Long order_sort;
	
	@Transient
	public String sign;//加密ID

	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}
}