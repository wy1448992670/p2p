package models;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Transient;

import constants.Constants;
import play.data.validation.Match;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import utils.Security;

/**
 * 用户
 * @author cp
 * @version 6.0
 * @created 2014年4月16日 下午2:14:51
 */
@Entity
public class t_users extends Model implements Cloneable {
	
	public Date time;
	
	@Required(message="姓名不能为空")
	@MaxSize(value=20,message="姓名长度为2-20个字符")
	@MinSize(value=2,message="姓名长度为2-20个字符")
	@Match(value="^[\u4E00-\u9FA5A-Za-z0-9_]+$",message="姓名不能含有特殊字符")
	public String name;
	
	@Required(message="邮箱不能为空")
	@Match(value="^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$",message="邮箱格式不正确")
	public String email;
	
	@Required(message="密码不能为空")
	public String password;
	
	public String photo;
	
	public String reality_name;
	
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
	
	public boolean is_allow_login;
	
	public Date lock_time;
	
	public long login_count;
	
	public Date last_login_time;
	
	public String last_login_ip;
	
	public Date last_logout_time;
	
	public boolean is_email_verified;
	
	public String telephone;
	
	public String mobile;

	public boolean is_mobile_verified;
	
	public String id_number;
	//居住地址
	public String address;
	//居住地邮编
	public String postcode;
	
	public int sex;
	
	public String getSexNamed() {
		if(sex==1) {
			return "男";
		}else if(sex==2) {
			return "女";
		}else {
			return "未知";
		}
	}
	
	public Date birthday;
	
	//城市Id,居住地 t_dict_city
	public int city_id;
	
	public String family_address;
	
	public String family_telephone;
	//公司全称
	public String company;
	//公司城市Id, t_dict_city
	public int company_city_id;
	
	
	public String company_address;
	
	public String office_telephone;
	
	public String fax_number;
	
	//t_dict_educations
	@Access(AccessType.PROPERTY)
	public int education_id;
	@Transient
	public t_dict_educations educations;
	public void setEducation_id(int education_id) {
		this.education_id = education_id;
		this.educations=new t_dict_educations().getEnumById(education_id);
	}
	
	//t_dict_maritals
	@Access(AccessType.PROPERTY)
	public int marital_id;
	@Transient
	public t_dict_maritals marital;
	public void setMarital_id(int marital_id) {
		this.marital_id = marital_id;
		this.marital=new t_dict_maritals().getEnumById(marital_id);
	}
	
	//购房情况 t_dict_houses
	@Access(AccessType.PROPERTY)
	public int house_id;
	@Transient
	public t_dict_houses house;
	public void setHouse_id(int house_id) {
		this.house_id = house_id;
		this.house=new t_dict_houses().getEnumById(house_id);
	}
	
	//购车情况 t_dict_cars
	@Access(AccessType.PROPERTY)
	public int car_id;
	@Transient
	public t_dict_cars car;
	public void setCar_id(int car_id) {
		this.car_id = car_id;
		this.car=new t_dict_cars().getEnumById(car_id);
	}
	
	public int login_client;
	
	public int master_client;
	
	public boolean is_add_base_info;
	
	public boolean is_erased;
	
	public Date recommend_time;
	
	public long recommend_user_id;
	
	public int recommend_reward_type;
	
	public int master_identity;
	
	public Date master_time_loan;
	
	public Date master_time_invest;
	
	public Date master_time_complex;
	
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
	
	public String sign1;
	public String sign2;
	
	public String qq_key;
	public String weibo_key;
	public String qr_code;
	
	public String ips_acct_no;
	public String ips_bid_auth_no;
	public String ips_repay_auth_no;
	
	public String device_user_id;
	public String channel_id;
	public int device_type;
	
	public boolean is_bill_push;
	public boolean is_invest_push;
	public boolean is_activity_push;
	
	public String forum_name;
	
	public boolean is_bank;
	
	public String risk_result, risk_answer;
	public Integer risk_type;

	public Long store_id;
	
	/**
	 * 是否时 活跃会员（充值就成为活跃会员）
	 */
	public boolean is_active;

	public Integer user_type;
	
	public BigDecimal credit_amount;//授信金额
	public String law_suit;//涉诉情况
	public String credit_report;//征信报告状况 
	@Transient
	public int overdue_cnt;//逾期次数
	@Transient
	public BigDecimal overdue_amount;//逾期金额

	public Integer finance_type;//用户理财类型
	
	public String auto_pact_location;//电子签章自动签署协议
	public String user_pact_location;//出借人或借款人合同
	public String user_certificate_url;//出借人或借款人存证
	
	//身份证照片认证实名接口 状态:0.未实名,-1实名失败,1实名成功
	public Integer id_picture_authentication_status;
	
	//身份证照片认证实名接口 时间,id_picture_authentication_status=-1|1有,0没有
	public Date id_picture_authentication_time;
	
	//身份证发证时间
	public Date idcard_start_date;
	
	//身份证过期时间
	public Date idcard_end_date;
	
	//活体认证接口 状态:0.未实名,-1实名失败,1实名成功
	public Integer living_authentication_status;
	
	//活体认证接口 时间,living_authentication_status=-1|1有,0没有
	public Date living_authentication_time;
	
	//运营商认证状态:0.未认证,-1认证失败,1认证成功
	public Integer mobile_operator_authentication_status;
	
	//运营商认证时间,mobile_operator_authentication_status=-1,1有
	public Date mobile_operator_authentication_time;
	
	//是否是虚拟用户 0 否 1 是,如:医院机构
	public boolean is_virtual_user;
	
	//用户风控补充资料,文件提交状态 0|null:未提交,1:已提交
	public Integer credit_apply_file_status;
	
	//用户风控补充资料,文件提交时间 credit_apply_file_status=1有
	public Date credit_apply_file_time;
	
	//是否是迁移用户 0 否 1是
	public boolean is_migration;
	public Date migration_time;

	public t_users() {
	}

	public t_users(String name, String email) {
		this.name = name;
		this.email = email;
	}
	
	public t_users(long id, boolean isActive) {
		this.id = id;
		this.is_active = isActive;
	}
	
	public t_users(double balance, double balance2, double freeze) {
		super();
		this.balance = balance;
		this.balance2 = balance2;
		this.freeze = freeze;
	}

	/**
	 * 用于查询拒收名单
	 */
	public t_users(long id, String name, Date refused_time, boolean is_refused_receive, String refused_reason, boolean is_allow_login) {
		this.id = id;
		this.name = name;
		this.refused_time = refused_time;
		this.is_refused_receive = is_refused_receive;
		this.refused_reason = refused_reason;
		this.is_allow_login = is_allow_login;
	}
	
	/**
	 * 用于查询用户通过名字
	 */
	public t_users(long id, String name, String realityName, String email) {
		this.id = id;
		this.name = name;
		this.reality_name = realityName;
		this.email = email;
	}
	
	/**
	 * 用于查询用户通过名字(发送系统通知)
	 */
	public t_users(String name, String mobile, String email) {
		this.name = name;
		this.mobile = mobile;
		this.email = email;
	}
	
	/**
	 * 用于查询用户通过名字(发送系统通知)
	 */
	public t_users( String name, String mobile, String email,long id) {
		this.id = id;
		this.name = name;
		this.mobile = mobile;
		this.email = email;
	}
	
	/**
	 * 查询用户信息(用于资金托管)
	 */
	public t_users(String realityName, String idNumber, String ipsAcctNo, String ipsBidAuthNo) {
		this.reality_name = realityName;
		this.id_number = idNumber;
		this.ips_acct_no = ipsAcctNo;
		this.ips_repay_auth_no = ipsBidAuthNo;
	}
		
	/**
	 * 选择用户
	 * @param id ID
	 * @param name 名称
	 */
	public t_users(long id, String name){
		this.id = id;
		this.name = name;
	}
	
	/**
	 * 查询用户资金
	 * @param id ID
	 * @param name 名称
	 */
	public t_users(double balance, double freeze){
		this.balance = balance;
		this.freeze = freeze;
	}
	
	/**
	 * 百度云推送查询
	 * @param id
	 * @param deviceUserId
	 * @param channelId
	 * @param deviceType
	 */
	public t_users(long id, String deviceUserId, String channelId, int deviceType, boolean isBillPush, boolean isInvestPush, boolean isActivityPush){
		this.id = id;
		this.device_user_id = deviceUserId;
		this.channel_id = channelId;
		this.device_type = deviceType;
		this.is_bill_push = isBillPush;
		this.is_invest_push = isInvestPush;
		this.is_activity_push = isActivityPush;
	}
	
	/**
	 * 
	 * cps推荐用户
	 *
	 * @param id
	 * @param name
	 * @param recommend_user_id
	 * @param recommend_reward_type
	 * @param recommend_time
	 */
	public t_users(long id, String name, String mobile, long recommend_user_id,int recommend_reward_type, Date recommend_time){
		this.id = id;
		this.name = name;
		this.mobile = mobile;
		this.recommend_user_id = recommend_user_id;
		this.recommend_reward_type = recommend_reward_type;
		this.recommend_time = recommend_time;
	}
	
	public String open_id;  //微信openId
	public int client;  //注册来源
	
	/*
	 * 对敏感信息做隐藏
	 */
	public t_users hide_data() throws CloneNotSupportedException {
		t_users user=(t_users)this.clone();
		
		user.id=0L;
		user.password="";
		user.pay_password="";
		user.secret_question_id1=0;
		user.secret_question_id2=0;
		user.secret_question_id3=0;
		user.answer1="";
		user.answer2="";
		user.answer3="";
		user.balance=0;
		user.balance2=0;
		user.freeze=0;
		user.sign1="";
		user.sign2="";
		user.ips_acct_no="";
		user.ips_bid_auth_no="";
		user.ips_repay_auth_no="";
		user.open_id="";
		
		return user;
	}

	@Transient
	public String sign;//加密ID

	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}
	@Transient
	public int bankCardCount;//加密ID
	
	//过滤emoji表情,生僻字等utf-8中占4个字节的字符,mysql utf-8格式不能存4个字节的字符
	//D800..0xDBFF
	//0xDC00..0xDFFF
	static public String userName4ByteToStar(String name) {
		return name.replaceAll("[\\ud800\\udc00-\\udbff\\udfff]|[\\u2600-\\u27ff]", "*");
	}
}
