package models;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

/**
 * 用于用户的一些详情显示
 * @author cp
 * @version 6.0
 * @created 2014年6月20日 下午5:01:31
 */
@Entity
public class v_user_users extends Model {
	
	public String city_name;
	
	public String province_name;
	
	public String education_name;
	
	public String marital_name;
	
	public String house_name;
	
	public String car_name;
	
	public Date time;
	
	public String name;
	
	public int credit_score;
	
	public Long credit_level_id;
	
	public String photo;
	
	public String reality_name;
	
	public String email;
	
	public boolean is_email_verified;
	
	public String mobile;
	
	public boolean is_mobile_verified;
	
	public String id_number;
	
	public int sex;
	
	public Date birthday;

	public Integer user_type;
	
	public BigDecimal credit_amount;
	public String law_suit;
	public String credit_report;
	@Transient
	public int overdue_cnt;//逾期次数
	@Transient
	public BigDecimal overdue_amount;//逾期金额 
	public Integer finance_type;//用户理财类型    0   借款人  1  投资人
	
	public Boolean is_migration;//是否迁移用户
}
