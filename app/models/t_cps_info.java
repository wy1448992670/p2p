package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 推广员
 * 
 * @author fei
 *
 */
@Entity
public class t_cps_info extends Model {

	private static final long serialVersionUID = 6766812551061241736L;
	public String name;
	public Date time;
	public long register_length;
	public long recommend_count;
	public long recharge_count;
	public double active_rate;
	public double bid_amount;
	public double invest_amount;
	public String credit_level_image_filename;
	public double commission_amount;

	@Transient
	public String sign;// 加密ID

	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}

}
