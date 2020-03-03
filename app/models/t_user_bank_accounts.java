package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import business.User;
import play.db.jpa.Model;

/**
 * 
 * @author lzp
 * @version 6.0
 * @created 2014-4-4 下午3:41:24
 */

@Entity
public class t_user_bank_accounts extends Model {

	public long user_id;
	public Date time;
	public String bank_name;
	public String bank_code;
	public int province_code;
	public int city_code;
	public String branch_bank_name;  //支行名称
	public String province;  //支行所在省
	public String city;  //支行所在市
	public String account;
	public String account_name;
	public String mobile;
	public boolean verified;
	public Date verify_time;
	public long verify_supervisor_id;
	public String protocol_no;
	public Boolean is_valid;//是否有效  0：无效   1：有效
    
	@Transient
	public String userName;
	@Transient
	public Integer bankType;
	@Transient
	public int isProtocol = 0; // 是否支持协议绑卡;

	public Boolean is_sign;
	public String deduct_pact_url;
	
	/**
	 * @return the userName
	 */
	public String getUserName() {
		User user = new User();
		user.id = user_id;
		return user.name;
	}
	
}
