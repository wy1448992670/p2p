package models;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

@Entity
public class v_users extends Model {

	public String city_name;
	
	public int province_Id;
	
	public String province_name;
	
	public String education_name;
	
	public String marital_name;
	
	public String house_name;
	
	public String car_name;
	
	public boolean is_add_base_info;
	
	public Date time;
	
	public String name;
	
	public String photo;
	
	public String reality_name;
	
	public String password;
	
	public int password_continuous_errors;
	
	public boolean is_password_error_locked;
	
	public Date password_error_locked_time;
	
	public String pay_password;
	
	public int pay_password_continuous_errors;
	
	public boolean is_pay_password_error_locked;
	
	public Date pay_password_error_locked_time;
	
	public boolean is_secret_set;
	
	public Date secret_set_time;
	
	public long secret_question_id1;
	public long secret_question_id2;
	public long secret_question_id3;
	
	public String answer1;
	public String answer2;
	public String answer3;
	
	public String question_name1;
	public String question_name2;
	public String question_name3;
	
	public boolean is_allow_login;

	public long login_count;
	
	public Date last_login_time;
	
	public String last_login_ip;
	
	public Date last_logout_time;
	
	public String email;
	
	public boolean is_email_verified;
	
	public String telephone;
	
	public String mobile;
	
	public boolean is_mobile_verified;
	
	public String id_number;
	
	public String address;
	
	public String postcode;
	
	public int sex;
	
	public Date birthday;
	
	public int city_id;
	
	public String family_address;
	
	public String family_telephone;
	
	public String company;
	
	public String company_address;
	
	public String office_telephone;

	public String fax_number;
	
	public int education_id;
	
	public int marital_id;
	
	public int house_id;

	public int car_id;
	
	public boolean is_erased;
	
	public long recommend_user_id;
	
	public int recommend_reward_type;
	
	public int master_identity;
	
	public boolean vip_status;
	
	public double balance;
	
	public double balance2;
	
	public double freeze;
	
	public double credit_line;
	
	public double last_credit_line;
	
	public int score;
	
	public int credit_score;
	
	public Long credit_level_id;
	
	public boolean is_refused_receive;
	
	public Date refused_time;
	
	public String refused_reason;
	
	public boolean is_blacklist;
	
	public Date joined_time;
	
	public String joined_reason;
	
	public Date assigned_time;
	
	public long assigned_to_supervisor_id;
	
	public String qr_code;
	
	public String ips_acct_no;
	
	public String ips_bid_auth_no;
	
	public String ips_repay_auth_no;
	
	public String risk_result;
	public Integer risk_type;
	public int user_type;
	public boolean is_bank;
	
	public BigDecimal credit_amount;
	public String law_suit;
	public String credit_report;
	@Transient
	public int overdue_cnt;//逾期次数
	@Transient
	public BigDecimal overdue_amount;//逾期金额
	
	public Integer finance_type;//用户理财类型 0  借款人 1 投资人
	@Transient
	public BigDecimal apply_amount;
	@Transient
	public Date  apply_time;
	public Boolean is_migration;//是否迁移用户
//	public Integer age;
}
