package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 管理员
 * @author md005
 */
@Entity
public class v_supervisors extends Model {

	public Date time;
	public String name;
	public String reality_name;
	public String password;
	public int password_continuous_errors;
	public boolean is_password_error_locked;
	public Date password_error_locked_time;
	public boolean is_allow_login;
	public long login_count;
	public Date last_login_time;
	public String last_login_ip;
	public Date last_logout_time;
	public String email;
	public String telephone;
	public String mobile1;
	public String office_telephone;
	public String fax_number;
	public int sex;
	public Date birthday;
	public int level;
	public Boolean is_erased;
	public long creater_id;
	public String ukey;
	public String right_group;
	public Boolean is_customer;
	public String customer_num;
	public int customer_user_count;
	
	@Transient
	public String sign;//加密ID
	@Transient
	public String sign2;//不带时间戳的加密ID
	
	public String getSign() {
		return Security.addSign(this.id, Constants.SUPERVISOR_ID_SIGN);
	}
	
	/**
	 * 在添加客服中用到
	 */
	public String getSign2() {
		return com.shove.security.Encrypt.encrypt3DES(this.id + "", Constants.SUPERVISOR_ID_SIGN);
	}
}
