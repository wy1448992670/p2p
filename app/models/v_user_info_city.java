package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

@Entity
public class v_user_info_city extends Model {
	
	public String name;
	public Date register_time;
	public double score;
	public String email;
	public String mobile;
	public boolean is_activation;
	public boolean is_add_base_info;
	public boolean is_blacklist;
	public double user_amount;
	public Date last_login_time;
	public boolean is_allow_login;
	public long invest_count;
	public double invest_amount;
	public long bid_count;
	public double bid_amount;
	public long recharge_amount;
	public long audit_item_count;
	public String credit_level_image_filename;
	public Long order_sort;
	public String realityName;
	public String province;
	public String city;
	// 累计收益=预计到账收益+已到账收益
	public double profit;
	
	// 实名认证信息
	public String reality_name,id_number;
	public long bank;
	public String recommend_user_name, recommend_user_mobile;
	public String risk_result;

	@Transient
	public String sign;//加密ID

	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}

}