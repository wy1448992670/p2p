package models;

// Generated 2014-4-4 11:59:10 by Hibernate Tools 3.4.0.CR1

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 管理员
 * @author cp
 * @version 6.0
 * @created 2014年4月4日 下午3:54:48
 */
@Entity
public class t_supervisors extends Model {

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
	public String last_login_city;
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
	public boolean is_customer;
	public String customer_num;
}
