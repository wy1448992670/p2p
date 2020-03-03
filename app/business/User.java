package business;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.jsoup.helper.StringUtil;

import com.alibaba.fastjson.JSON;
import com.google.zxing.BarcodeFormat;
import com.shove.Convert;
import com.shove.code.Qrcode;
import com.shove.security.Encrypt;

import baofu.rsa.RsaCodingUtil;
import baofu.util.SecurityUtil;
import bean.AuthReviewBean;
import business.Optimization.AuditItemOZ;
import constants.Constants;
import constants.CouponTypeEnum;
import constants.DealType;
import constants.FinanceTypeEnum;
import constants.IPSConstants.IpsCheckStatus;
import constants.PactTypeEnum;
import constants.SQLTempletes;
import constants.SupervisorEvent;
import constants.Templets;
import constants.UserEvent;
import constants.UserTypeEnum;
import controllers.wechat.account.WechatAccountHome;
import interfaces.UserBase;
import models.t_bid_publish;
import models.t_bids;
import models.t_bill_invests;
import models.t_bills;
import models.t_cps_info;
import models.t_credit_levels;
import models.t_dict_ad_citys;
import models.t_dict_ad_provinces;
import models.t_dict_cars;
import models.t_dict_educations;
import models.t_dict_houses;
import models.t_dict_maritals;
import models.t_dict_payment_gateways;
import models.t_enum_map;
import models.t_invest_user;
import models.t_invests;
import models.t_new_city;
import models.t_new_province;
import models.t_red_packages_history;
import models.t_red_packages_type;
import models.t_sequences;
import models.t_statistic_cps;
import models.t_system_recharge_completed_sequences;
import models.t_user_attention_users;
import models.t_user_audit_items;
import models.t_user_bank_accounts;
import models.t_user_blacklist;
import models.t_user_city;
import models.t_user_cps_income;
import models.t_user_details;
import models.t_user_donate;
import models.t_user_events;
import models.t_user_recharge_details;
import models.t_user_report_users;
import models.t_user_withdrawals;
import models.t_users;
import models.t_users_info;
import models.t_wealthcircle_income;
import models.t_wealthcircle_invite;
import models.v_bid_assigned;
import models.v_bill_invest_statistics;
import models.v_user_account_info;
import models.v_user_account_statistics;
import models.v_user_attention_info;
import models.v_user_bill;
import models.v_user_blacklist;
import models.v_user_blacklist_info;
import models.v_user_complex_info;
import models.v_user_cps_detail;
import models.v_user_cps_info;
import models.v_user_cps_user_count;
import models.v_user_cps_users;
import models.v_user_detail_credit_score_audit_items;
import models.v_user_detail_credit_score_invest;
import models.v_user_detail_credit_score_loan;
import models.v_user_detail_credit_score_normal_repayment;
import models.v_user_detail_credit_score_overdue;
import models.v_user_detail_score;
import models.v_user_details;
import models.v_user_for_details;
import models.v_user_for_message;
import models.v_user_for_personal;
import models.v_user_info;
import models.v_user_info_city;
import models.v_user_info_v1;
import models.v_user_invest_amount;
import models.v_user_invest_info;
import models.v_user_loan_info;
import models.v_user_loan_info_bad;
import models.v_user_loan_info_bad_d;
import models.v_user_loan_info_bill;
import models.v_user_loan_info_bill_d;
import models.v_user_loan_user_unassigned;
import models.v_user_locked_info;
import models.v_user_report_list;
import models.v_user_reported_info;
import models.v_user_scores;
import models.v_user_unverified_info;
import models.v_user_users;
import models.v_user_vip_info;
import models.v_user_withdrawal_info;
import models.v_user_withdrawals;
import models.v_users;
import models.v_users_apply;
import net.sf.json.JSONObject;
import payment.PaymentProxy;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.db.helper.JpaHelper;
import play.db.jpa.Blob;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.db.jpa.Model;
import play.libs.WS;
import play.mvc.Http.Request;
import play.mvc.Scope.Session;
import utils.Arith;
import utils.CacheManager;
import utils.CharUtil;
import utils.Converter;
import utils.DataUtil;
import utils.DateUtil;
import utils.EmailUtil;
import utils.EmptyUtil;
import utils.ErrorInfo;
import utils.GopayUtils;
import utils.IDCardValidate;
import utils.JPAUtil;
import utils.LLPayUtil;
import utils.NumberUtil;
import utils.PageBean;
import utils.QueryUtil;
import utils.RegexUtils;
import utils.Security;
import utils.ServiceFee;
import utils.reapal.agent.utils.RandomUtil;
import utils.ymsms.SMS;

/**
 * 用户的业务实体
 * 
 * @author cp
 * @version 6.0
 * @created 2014年3月21日 下午4:25:45
 */
public class User extends UserBase implements Serializable {
	private static final long serialVersionUID = -1L;
	/**
	 * 借款人标识
	 */
	static int BORROW = 0;
	/**
	 * 投资人标识
	 */
	static int INVEST = 1;
	public String openId;

	public String risk_result;
	// 0：未知 ，1：个人， 2：企业
	public int user_type;
	// 是否绑定卡
	public boolean is_bank;
	// 企业认证申请
	public AuthReviewBean authReview;
	public String year;
	public String month;
	public String day;
	public String datetime;
	public String pactFolder;

	public String b_reality_name;
	public Integer b_user_type;
	public Integer i_user_type;
	public String b_id_number;
	public String legalPerson;// 企业法人

	public t_users instance;
	private t_users_info user_info;
	
	public String getOpenId() {

		return this.openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String simulateLogin = null;

	public Long store_id;

	/**
	 * 缺少的字段 是否是有效会员（充值就是有效会员） 基本信息中的邮箱
	 */
	public long id;
	private long _id;

	public String sign;// 加密ID

	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}

	public void setId(long id) {

		if (id <= 0) {
			this._id = -1;

			return;
		}

		Model user = null;
		t_user_city userCity = t_user_city.find("user_id=? ", id).first();
		this.tUser_city = userCity;
		try {
			this.instance = t_users.findById(id);
			if (this.createBid) {
				user = this.instance;
			}
		} catch (Exception e) {
			Logger.info("t_users.findById(id)：" + e.getMessage());
			this._id = -1;
			return;
		}
		/* 发布借款user实体部分字段填充 */
		if (this.createBid) {
			/*
			 * try { user = t_users.findById(id); } catch (Exception e) {
			 * Logger.info("用户setId填充时（createBid=true）：" + e.getMessage()); this._id = -1;
			 * 
			 * return; }
			 */

			t_users tuser = (t_users) user;
			this._id = tuser.id;
			this.name = tuser.name;
			this.vipStatus = tuser.vip_status;
			this.balance = tuser.balance;
			this.balance2 = tuser.balance2;
			this.creditLine = tuser.credit_line;
			this.isAddBaseInfo = tuser.is_add_base_info;
			this.isEmailVerified = tuser.is_email_verified;
			this.mobile = tuser.mobile;
			this.email = tuser.email;
			this.forum_name = tuser.forum_name;
			this.creditAmount = tuser.credit_amount;
			this.creditReport = tuser.credit_report;
			this.lawSuit = tuser.law_suit;
			this.overdueAmount = tuser.overdue_amount;
			this.overdueCnt = tuser.overdue_cnt;
			this.financeType = tuser.finance_type;
			this.isMigration = tuser.is_migration;
			return;
		}

		if (this.lazy) {

			try {
				StringBuffer sql = new StringBuffer();
				List<v_user_users> v_user_users_list = null;

				sql.append(SQLTempletes.SELECT);
				sql.append(SQLTempletes.V_USER_USERS);
				sql.append(" and t_users.id = ?");
				/*
				 * from t_users LEFT JOIN t_dict_ad_citys ON t_users.city_id =
				 * t_dict_ad_citys.id LEFT JOIN t_dict_ad_provinces ON
				 * t_dict_ad_citys.province_id = t_dict_ad_provinces.id LEFT JOIN
				 * t_dict_educations ON t_users.education_id = t_dict_educations.id LEFT JOIN
				 * t_dict_maritals ON t_users.marital_id = t_dict_maritals.id LEFT JOIN
				 * t_dict_houses ON t_users.house_id = t_dict_houses.id LEFT JOIN t_dict_cars ON
				 * t_users.car_id = t_dict_cars.id
				 */

				EntityManager em = JPA.em();
				Query query = em.createNativeQuery(sql.toString(), v_user_users.class);
				query.setParameter(1, id);
				query.setMaxResults(1);
				v_user_users_list = query.getResultList();

				if (v_user_users_list.size() > 0) {
					user = v_user_users_list.get(0);
				}

			} catch (Exception e) {
				e.printStackTrace();
				Logger.info("用户setId填充时（lazy=true）：" + e.getMessage());
				this._id = -1;

				return;
			}
		} else {
			try {
				StringBuffer sql = new StringBuffer();
				List<v_users> v_users_list = null;

				sql.append(SQLTempletes.SELECT);
				sql.append(SQLTempletes.V_USERS);
				sql.append(" and t_users.id = ?");
				/*
				 * from t_users LEFT JOIN t_dict_ad_citys ON t_users.city_id =
				 * t_dict_ad_citys.id LEFT JOIN t_dict_ad_provinces ON
				 * t_dict_ad_citys.province_id = t_dict_ad_provinces.id LEFT JOIN
				 * t_dict_educations ON t_users.education_id = t_dict_educations.id LEFT JOIN
				 * t_dict_houses ON t_users.house_id = t_dict_houses.id LEFT JOIN
				 * t_dict_maritals ON t_users.marital_id = t_dict_maritals.id LEFT JOIN
				 * t_dict_cars ON t_users.car_id = t_dict_cars.id LEFT JOIN
				 * t_dict_secret_questions question1 ON t_users.secret_question_id1 =
				 * question1.id LEFT JOIN t_dict_secret_questions question2 ON
				 * t_users.secret_question_id2 = question2.id LEFT JOIN t_dict_secret_questions
				 * question3 ON t_users.secret_question_id3 = question3.id
				 */

				EntityManager em = JPA.em();
				Query query = em.createNativeQuery(sql.toString(), v_users.class);
				query.setParameter(1, id);
				query.setMaxResults(1);
				v_users_list = query.getResultList();

				if (v_users_list.size() > 0) {
					user = v_users_list.get(0);
				}

			} catch (Exception e) {
				e.printStackTrace();
				Logger.info("用户setId填充时：" + e.getMessage());
				this._id = -1;

				return;
			}
		}

		if (user == null) {
			this._id = -1;
			return;

		}

		if (this.lazy) {
			this.setInformationLazy(user);
		} else {
			this.setInformation(user);
		}

	}

	public long getId() {
		return _id;
	}

	/**
	 * 给所有属性赋值
	 * 
	 * @param obj
	 */
	public void setInformation(Model model) {
		if (!(model instanceof v_users)) {
			return;
		}
		v_users user = (v_users) model;
		User userOverdueInfo = findUserById(user.id);
		this._id = user.id;
		this.time = user.time;
		this._name = user.name;
		this.photo = user.photo;
		this.realityName = user.reality_name;
		this._password = user.password;
		this.passwordContinuousErrors = user.password_continuous_errors;
		this.isPasswordErrorLocked = user.is_password_error_locked;
		this.passwordErrorLockedTime = user.password_error_locked_time;
		this._payPassword = user.pay_password;
		this.payPasswordContinuousErrors = user.pay_password_continuous_errors;
		this.isPayPasswordErrorLocked = user.is_pay_password_error_locked;
		this.payPasswordErrorLockedTime = user.pay_password_error_locked_time;
		this.isSecretSet = user.is_secret_set;
		this.secretSetTime = user.secret_set_time;
		this.isAllowLogin = user.is_allow_login;

		this.secretQuestionId1 = user.secret_question_id1;
		this.secretQuestionId2 = user.secret_question_id2;
		this.secretQuestionId3 = user.secret_question_id3;

		this.answer1 = user.answer1;
		this.answer2 = user.answer2;
		this.answer3 = user.answer3;

		this.questionName1 = user.question_name1;
		this.questionName2 = user.question_name2;
		this.questionName3 = user.question_name3;

		this.loginCount = user.login_count;
		this.lastLoginTime = user.last_login_time;
		this.lastLoginIp = user.last_login_ip;
		this.lastLogoutTime = user.last_logout_time;
		this.email = user.email;
		this.isEmailVerified = user.is_email_verified;
		this.telephone = user.telephone;
		this.mobile = user.mobile;
		this.isMobileVerified = user.is_mobile_verified;

		this.idNumber = user.id_number;
		this.address = user.address;
		this.postcode = user.postcode;
		this._sex = user.sex;
		this._birthday = user.birthday;
		this.cityId = (int) user.city_id;
		this.familyAddress = user.family_address;
		this.familyTelephone = user.family_telephone;
		this.company = user.company;
		this.companyAddress = user.company_address;
		this.officeTelephone = user.office_telephone;
		this.faxNumber = user.fax_number;
		this.educationId = (int) user.education_id;
		this.maritalId = (int) user.marital_id;
		this.houseId = (int) user.house_id;
		this.carId = (int) user.car_id;
		this.isAddBaseInfo = user.is_add_base_info;

		/**
		 * 0 = 已擦除状态; 1 = 正常状态;
		 */
		this.isErased = user.is_erased;

		this.recommendUserId = user.recommend_user_id;
		this.recommendRewardType = user.recommend_reward_type;

		this.masterIdentity = user.master_identity;
		this.vipStatus = user.vip_status;
		this.balance = user.balance;
		this.balance2 = user.balance2;
		this.freeze = user.freeze;
		this.creditLine = user.credit_line;
		this.lastCreditLine = user.last_credit_line;
		this.score = user.score;
		this.creditScore = user.credit_score;
		this.creditLevelId = user.credit_level_id;

		// this.isActive = user.isActive; //是否是活跃 会员

		this.isRefusedReceive = user.is_refused_receive;
		this.refusedTime = user.refused_time;
		this.refusedReason = user.refused_reason;

		this.isBlacklist = user.is_blacklist;
		this.joinedTime = user.joined_time;
		this.joinedReason = user.joined_reason;

		this.assignedTime = user.assigned_time;
		this.assignedToSupervisorId = user.assigned_to_supervisor_id;

		this.ipsAcctNo = user.ips_acct_no;
		this.ipsBidAuthNo = user.ips_bid_auth_no;
		this.ipsRepayAuthNo = user.ips_repay_auth_no;

		this.cityName = user.city_name;
		this.provinceId = user.province_Id;
		this.provinceName = user.province_name;
		this.educationName = user.education_name;
		this.maritalName = user.marital_name;
		this.houseName = user.house_name;
		this.carName = user.car_name;
		this.qrcode = user.qr_code;
		this.risk_result = user.risk_result;
		this.user_type = user.user_type;
		this.is_bank = user.is_bank;
		this.riskType = user.risk_type;
		this.creditAmount = user.credit_amount;
		this.creditReport = user.credit_report;
		this.lawSuit = user.law_suit;
		this.overdueAmount = userOverdueInfo.overdueAmount;
		this.overdueCnt = userOverdueInfo.overdueCnt;
		this.currentOverdueAmount = userOverdueInfo.currentOverdueAmount;
		this.financeType = user.finance_type;
		this.isMigration = user.is_migration;
	}

	/**
	 * lazy 赋值
	 * 
	 * @param user
	 */
	public void setInformationLazy(Model model) {
		if (!(model instanceof v_user_users)) {
			return;
		}

		v_user_users user = (v_user_users) model;
		User userOverdueInfo = findUserById(user.id);
		this._id = user.id;
		this.time = user.time;
		this._name = user.name;
		this.creditScore = user.credit_score;
		this.creditLevelId = user.credit_level_id;
		this.photo = user.photo;
		this.realityName = user.reality_name;

		this.email = user.email;
		this.isEmailVerified = user.is_email_verified;

		this.mobile = user.mobile;
		this.isMobileVerified = user.is_mobile_verified;

		this.idNumber = user.id_number;

		this._sex = user.sex;
		this._birthday = user.birthday;

		this.cityName = user.city_name;
		this.provinceName = user.province_name;
		this.educationName = user.education_name;
		this.maritalName = user.marital_name;
		this.houseName = user.house_name;
		this.carName = user.car_name;
		this.user_type = user.user_type;
		this.creditAmount = user.credit_amount;
		this.creditReport = user.credit_report;
		this.lawSuit = user.law_suit;
		this.overdueAmount = userOverdueInfo.overdueAmount;
		this.overdueCnt = userOverdueInfo.overdueCnt;
		this.currentOverdueAmount = userOverdueInfo.currentOverdueAmount;
		this.financeType = user.finance_type;
	}

	public Date time;

	public String name;
	private String _name;

	public boolean isQueryName = true;

	/**
	 * 根据email，填充数据
	 * 
	 * @param email
	 */
	public void findUserByEmail(String email) {
		v_users user = null;
		try {
			StringBuffer sql = new StringBuffer();
			List<v_users> v_users_list = null;
			sql.append(SQLTempletes.SELECT);
			sql.append(SQLTempletes.V_USERS);
			sql.append(" and t_users.email = ?");
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_users.class);
			query.setParameter(1, email);
			query.setMaxResults(1);
			v_users_list = query.getResultList();
			if (v_users_list.size() > 0) {
				user = v_users_list.get(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("login for email ", e);
		}

		if (user == null || user.id <= 0) {
			this._name = name;
			this._id = -1;
			return;
		}

		this.setInformation(user);
	}

	/**
	 * 根据mobile，填充数据
	 * 
	 * @param mobile
	 */
	public void findUserByMobile(String mobile, int financeType) {
		v_users user = null;
		try {
			StringBuffer sql = new StringBuffer();
			List<v_users> v_users_list = null;
			sql.append(SQLTempletes.SELECT);
			sql.append(SQLTempletes.V_USERS);
			if (financeType == FinanceTypeEnum.INVEST.getCode()) {
				sql.append(" and t_users.mobile = ? and ( finance_type=? or finance_type is null) ");
			} else {
				sql.append(" and t_users.mobile = ? and finance_type=? ");
			}

			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_users.class);
			query.setParameter(1, mobile);
			query.setParameter(2, financeType);
			query.setMaxResults(1);
			v_users_list = query.getResultList();
			if (v_users_list.size() > 0) {
				user = v_users_list.get(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("login for mobile ", e);
		}

		if (user == null || user.id <= 0) {
			this._name = name;
			this._id = -1;
			return;
		}

		this.setInformation(user);
	}

	/**
	 * 根据name，填充数据
	 * 
	 * @param name
	 * @return -1执行失败 0执行成功
	 */
	@SuppressWarnings("unchecked")
	public void setName(String name) {
		this._name = name;

		if (!this.isQueryName) {
			return;
		}

		v_users user = null;

		try {
			StringBuffer sql = new StringBuffer();
			List<v_users> v_users_list = null;
			sql.append(SQLTempletes.SELECT);
			sql.append(SQLTempletes.V_USERS);
			sql.append(" and t_users.name = ? or t_users.email = ?");
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_users.class);
			query.setParameter(1, name);
			query.setParameter(2, name);
			query.setMaxResults(1);
			v_users_list = query.getResultList();
			if (v_users_list.size() > 0) {
				user = v_users_list.get(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("注册时,根据用户名查询用户信息时：" + e.getMessage());
		}

		if (user == null || user.id <= 0) {
			this._name = name;
			this._id = -1;
			return;
		}

		this.setInformation(user);
	}

	public String getName() {
		return this._name;

	}

	public String photo;
	public String realityName;

	public String password;
	private String _password;

	/**
	 * 用户密码加密
	 * 
	 * @param password
	 */
	public void setPassword(String password) {
		this._password = Encrypt.MD5(password + Constants.ENCRYPTION_KEY);
	}

	public String getPassword() {
		return this._password;
	}

	public int passwordContinuousErrors;
	public boolean isPasswordErrorLocked;
	public Date passwordErrorLockedTime;
	public String payPassword;
	private String _payPassword;

	/**
	 * 用户支付密码加密
	 * 
	 * @param password
	 */
	public void setPayPassword(String payPassword) {
		this._payPassword = Encrypt.MD5(payPassword + Constants.ENCRYPTION_KEY);
	}

	public String getPayPassword() {
		return this._payPassword;

	}

	public int payPasswordContinuousErrors;
	public boolean isPayPasswordErrorLocked;
	public Date payPasswordErrorLockedTime;

	public boolean isSecretSet;
	public Date secretSetTime;

	public long secretQuestionId1;
	public long secretQuestionId2;
	public long secretQuestionId3;

	public String questionName1;
	public String questionName2;
	public String questionName3;

	public String answer1;
	public String answer2;
	public String answer3;

	public String subNature;
	// public BigDecimal creditAmount;
	public String lawSuit;
	public String creditReport;

	/**
	 * 是否允许登录(管理员是否锁定 true锁定)
	 */
	public boolean isAllowLogin;

	public boolean getIsAllowLogin() {
		String sql = "select is_allow_login from t_users where id = ?";

		try {
			return t_users.find(sql, this._id).first();
		} catch (Exception e) {
			Logger.info(e.getMessage());

			return true;
		}
	}

	public long loginCount;
	public Date lastLoginTime;
	public String lastLoginIp;
	public String thisLoginIp;
	public Date lastLogoutTime;

	public String email;
	public String forum_name;

	public boolean isEmailVerified;

	public String telephone;
	public String mobile;
	public boolean isMobileVerified;

	public String idNumber;
	public String address;
	public String postcode;
	public String sex;
	private int _sex;
	public String id_number;

	public String getSex() {
		if (Constants.ONE == _sex) {
			return Constants.MAN;
		}

		if (Constants.TWO == _sex) {
			return Constants.WOMAN;
		}
		return Constants.UNKNOWN;
	}

	public void setSex(int sex) {
		this._sex = sex;
	}

	public Date birthday;
	private Date _birthday;

	public Date getBirthday() {
		if (this._birthday == null) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - this._age);
			this.birthday = calendar.getTime();
			return calendar.getTime();
		}
		return this._birthday;
	}

	public void setBirthday(Date birthday) {
		this._birthday = birthday;
	}

	public Integer age;
	private int _age;

	public int getAge() throws ParseException {

		if (idNumber == null || user_type == 2 || user_type == 0 || user_type == 3) {
			return -1;
		}
		String birthday = idNumber.substring(6, 14);// 342623199203160126

		SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");

		Calendar calendar = Calendar.getInstance();
		Date date = null;
		try {
			date = sd.parse(birthday);
		} catch (Exception e) {
			return -1;
		}
		calendar.setTime(date);
		Calendar currentDate = Calendar.getInstance();

		return currentDate.get(Calendar.YEAR) - calendar.get(Calendar.YEAR);

	}

	public void setAge(int age) {
		this._age = age;
	}

	public int cityId;
	public String cityName;
	public int provinceId;
	public String provinceName;

	public String familyAddress;
	public String familyTelephone;
	public String company;
	public String companyAddress;
	public String officeTelephone;
	public String faxNumber;
	public int educationId;
	public String educationName;

	public int maritalId;
	public String maritalName;

	public int houseId;
	public String houseName;

	public int carId;
	public String carName;

	public boolean isAddBaseInfo;
	public boolean isErased;

	public long recommendUserId;
	public String recommendUserName;

	public String spreadLink;
	public Integer overdueCnt;// 累计逾期次数
	public BigDecimal overdueAmount;// 累计逾期金额
	public BigDecimal currentOverdueAmount;// 当前逾期金额
	public boolean hasBids;// 是否发布过标的

	public t_user_city tUser_city;// 用户所在省份城市

	public String getSpreadLink() {
		return Constants.BASE_URL + "register?un=" + Encrypt.encrypt3DES(this.name, Constants.ENCRYPTION_KEY);
	}

	// public void setRecommendUserId(long recommendUserId) {
	// this.recommendUserId = recommendUserId;
	// this.recommendUserName = User.queryUserNameById(recommendUserId, new
	// ErrorInfo());
	// }

	public void setRecommendUserName(String recommendUserName) {
		this.recommendUserName = recommendUserName;
		this.recommendUserId = User.queryIdByUserName(recommendUserName, new ErrorInfo());
		if (StringUtils.startsWith(recommendUserName, Constants.NORMAL_CODE_PREFIX)) {
			try {
				t_wealthcircle_invite invite = t_wealthcircle_invite.find(" invite_code = ?", recommendUserName).first();
				if (null != invite) {
					this.recommendUserId = invite.user_id;
				}
			} catch (Exception e) {
				Logger.error("查询邀请码时：%s", e.getMessage());
			}

		}
	}

	public int recommendRewardType;

	/**
	 * 0 未确定 1 借款会员 2 投资会员 3 复合会员
	 */
	public int masterIdentity;
	public boolean vipStatus;
	public double balance;
	public double balance2;
	public double freeze;
	public double creditLine;
	public double lastCreditLine;
	public int score;
	public int creditScore;
	public Long creditLevelId;

	public boolean isRefusedReceive;
	public Date refusedTime;
	public String refusedReason;

	public boolean isBlacklist;
	public Date joinedTime;
	public String joinedReason;

	public Date assignedTime;
	public long assignedToSupervisorId;

	public boolean createBid;
	public boolean lazy;

	public String qqKey;
	private String _qqKey;

	public String ipsAcctNo;
	public String ipsBidAuthNo;
	public String ipsRepayAuthNo;

	public String deviceUserId;
	public String channelId;
	public int deviceType;
	public boolean isBillPush;
	public boolean isInvestPush;
	public boolean isActivityPush;
	public Boolean isMigration; //是否迁移用户
	
	public String getQqKey() {
		return this._qqKey;
	}

	public void setQqKey(String qqKey) {
		this._qqKey = qqKey;
	}

	public String weiboKey;
	private String _weiboKey;

	public String getWeiboKey() {
		return this._weiboKey;
	}

	public void setWeiboKey(String weiboKey) {
		this._weiboKey = weiboKey;
	}

	public String qrcode;

	/**
	 * 用户已上传的审核资料
	 */
	public List<Long> auditItems;

	public List<Long> getAuditItems() {

		return UserAuditItem.queryUserAuditItem(this.id, true);
	}

	// /**
	// * 用户所有上传资料最新记录
	// */
	// public List<UserAuditItem> userAuditItemList;
	//
	// public List<UserAuditItem> getUserAuditItemList(){
	//
	// return UserAuditItem.queryUserAllAuditItem(this.id);
	// }

	/**
	 * 信用等级
	 */
	public CreditLevel myCredit;

	public Integer riskType;

	public CreditLevel getMyCredit() {

		CreditLevel myCredit = new CreditLevel();

		if (null == this.creditLevelId) {

			return null;
		}

		myCredit.id = this.creditLevelId;

		return myCredit;
	}

	/**
	 * 更新用户的信用等级
	 */
	public static int updateCreditLevel(long userId, ErrorInfo error) {
		error.clear();
		error.code = -1;

		List<t_credit_levels> credit_levels = null;

		/* 查询到所有的信用等级信息 */
		try {
			credit_levels = t_credit_levels.find("is_enable = ? order by order_sort asc", true).fetch();
		} catch (Exception e) {
			Logger.error("更新用户信用等级->查询信用积分时：" + e.getMessage());
			error.msg = "更新用户信用等级->查询信用积分时有误！";

			return error.code;
		}

		if (null == credit_levels || credit_levels.size() == 0) {

			return error.code;
		}

		int length = credit_levels.size();
		Map<String, Object> map = new HashMap<String, Object>();
		int creditScore = 0;
		double creditLine = 0;

		map = JPAUtil.getMap(error, "SELECT credit_score AS credit_score,credit_line AS credit_line FROM t_users WHERE id = ?",
				userId);

		if (map.size() > 1) {
			Object obj1 = map.get("credit_score");
			Object obj2 = map.get("credit_line");

			if (null != obj1 && null != obj2) {
				creditScore = Integer.parseInt(obj1.toString());
				creditLine = Double.parseDouble(obj2.toString());
			}
		}

		String sql = "SELECT t.audit_item_id FROM t_user_audit_items t WHERE t.user_id = ? AND t.status = ?";
		int auditCount = 0;
		List auditedItems = new ArrayList();
		;

		EntityManager em = JPA.em();
		Query query = em.createQuery(sql).setParameter(1, userId).setParameter(2, Constants.AUDITED);

		/* 查询当前用户已经通过审核了的资料及总数 */
		try {
			auditedItems = query.getResultList();
			auditCount = (int) t_user_audit_items.count("user_id = ? and status = ?", userId, Constants.AUDITED);
		} catch (Exception e) {
			Logger.error("更新用户信用等级->查询用户审核资料时：" + e.getMessage());
			error.msg = "更新用户信用等级->查询用户审核资料时有误！";

			return error.code;
		}

		sql = "SELECT COUNT(a.id) FROM t_bills a JOIN t_bids b ON a.bid_id = b.id AND a.overdue_mark IN(?, ?, ?) AND b.user_id = ?";
		int overDueCount = 0;
		Object record = null;

		query = em.createNativeQuery(sql).setParameter(1, Constants.BILL_NORMAL_OVERDUE).setParameter(2, Constants.BILL_OVERDUE)
				.setParameter(3, Constants.BILL_BAD_DEBTS).setParameter(4, userId);

		/* 查询当前用户逾期账单的数量 */
		try {
			record = query.getSingleResult();
		} catch (Exception e) {
			Logger.error("更新用户信用等级->查询用户逾期账单时：" + e.getMessage());
			error.msg = "更新用户信用等级->查询用户逾期账单时有误！";

			return error.code;
		}

		if (null != record) {
			overDueCount = Integer.parseInt(record.toString());
		}

		int min_credit_score = 0;
		int min_audit_items = 0;
		boolean is_allow_overdue = false;
		boolean flag = true;
		String must_items = "";
		String[] mustItems = null;
		t_credit_levels t_credit_level = null;

		/* 如果用户的信用积分没有达到倒数第二个级别，就直接为hr */
		t_credit_level = credit_levels.get(length - 2);

		if (creditScore < t_credit_level.min_credit_score) {

			try {
				JPAUtil.executeUpdate(error,
						"UPDATE t_users u SET u.credit_level_id = ?,u.credit_score = ?,u.credit_line = ? WHERE u.id = ?",
						credit_levels.get(length - 1).id, creditScore, creditLine, userId);
			} catch (Exception e) {
				Logger.error("更新信用等级时：" + e.getMessage());

				error.code = -1;
				error.msg = "更新用户信用等级->数据未更新";

				return error.code;
			}

			error.code = 0;

			return error.code;
		}

		/* 根据信用等级的条件从高等级到低等级来匹配用户的信用等级 */
		for (int i = 0; i < length - 1; i++) {
			t_credit_level = credit_levels.get(i);

			min_credit_score = t_credit_level.min_credit_score;
			min_audit_items = t_credit_level.min_audit_items;
			is_allow_overdue = t_credit_level.is_allow_overdue;
			must_items = t_credit_level.must_items;
			flag = true;

			if (null != must_items && 0 != must_items.length()) {

				if (null == auditedItems || 0 == auditedItems.size()) {

					continue;
				}

				mustItems = must_items.split(",");
				for (String it : mustItems) {

					if (!(flag = flag & auditedItems.contains(Long.parseLong(it)))) {

						break;
					}
				}
			}

			/* 如果用户不满足此次的等级条件，就直接匹配下一个等级条件 */
			if (!flag | (is_allow_overdue && overDueCount > 0) | (creditScore < min_credit_score)
					| (auditCount < min_audit_items)) {

				continue;
			}

			/* 如果用户满足当前等级的所有条件，那么此次的等级为用户的信用等级 */
			try {
				JPAUtil.executeUpdate(error,
						"UPDATE t_users u SET u.credit_level_id = ?,u.credit_score = ?,u.credit_line = ? WHERE u.id = ?",
						t_credit_level.id, creditScore, creditLine, userId);
			} catch (Exception e) {
				Logger.error("更新信用等级时：" + e.getMessage());

				error.code = -1;
				error.msg = "更新用户信用等级->数据未更新";

				return error.code;
			}

			error.code = 0;

			return error.code;
		}

		/* 如果用户不满足所有的等级条件，那么就默认为最低等级hr */
		try {
			JPAUtil.executeUpdate(error,
					"UPDATE t_users u SET u.credit_level_id = ?,u.credit_score = ?,u.credit_line = ? WHERE u.id = ?",
					credit_levels.get(length - 1).id, creditScore, creditLine, userId);
		} catch (Exception e) {
			Logger.error("更新信用等级时：" + e.getMessage());

			error.code = -1;
			error.msg = "更新用户信用等级->数据未更新";

			return error.code;
		}

		error.code = 0;

		return error.code;
	}

	/**
	 * 金额
	 */
	public v_user_for_details balanceDetail;

	public v_user_for_details getBalanceDetail() {
		ErrorInfo error = new ErrorInfo();
		v_user_for_details forDetail = DealDetail.queryUserBalance(this.id, error);
		return forDetail;
	}

	public double getBalance() {
		String sql = "select balance from t_users where id = ?";
		Query query = JPAUtil.createNativeQuery(sql, this.id);

		try {
			return Convert.strToDouble(query.getSingleResult().toString(), 0);
		} catch (Exception e) {
			Logger.error("查询用户余额：", e.getMessage());
		}

		return 0;
	}

	public v_user_scores userScore;

	public v_user_scores getUserScore() {
		v_user_scores user_scores = null;
		StringBuffer sql = new StringBuffer();
		List<v_user_scores> v_user_scores_list = null;

		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_SCORES);
		sql.append(" and t_users.id = ?");

		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql.toString(), v_user_scores.class);
		query.setParameter(1, this.id);
		query.setMaxResults(1);
		v_user_scores_list = query.getResultList();

		if (v_user_scores_list.size() > 0) {
			user_scores = v_user_scores_list.get(0);
		}

		return user_scores;
	}

	public User() {

	}

	/**
	 * 注册
	 * 
	 * @return
	 */
	public int register(int client, ErrorInfo error) {
		error.clear();

		if(true) {//关闭注册
			error.code = -3;
			error.msg = "注册失败";
			return error.code;
		}

		t_users user = new t_users();
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String keyWord = backstageSet.keywords;
		if (client != 2) {

			if (StringUtils.isNotBlank(keyWord)) {
				String[] keywords = keyWord.split("，");

				for (String word : keywords) {
					if (this._name.contains(word)) {
						error.code = -1;
						error.msg = "对不起，注册的用户名包含敏感词汇，请重新输入用户名";

						return error.code;
					}
				}
			}
		}
		if (StringUtils.isNotBlank(this.recommendUserName)) {

			if (this.recommendUserId > 0) {
				user.recommend_user_id = this.recommendUserId;
				user.recommend_reward_type = backstageSet.cpsRewardType;
				user.recommend_time = new Date();
			}
		}

		user.client = client;
		user.is_mobile_verified = isMobileVerified;
		user.mobile = this.mobile;
		user.name = this._name;
		user.time = new Date();
		user.password = this._password;
		user.email = this.email;
		user.credit_line = backstageSet.initialAmount;
		user.last_credit_line = backstageSet.initialAmount;
		user.photo = Constants.DEFAULT_PHOTO;
		user.qq_key = this.qqKey;
		user.weibo_key = this.weiboKey;
		user.credit_level_id = (long) Constants.ONE; // 初始最低信用等级
		user.user_type = UserTypeEnum.NONE.getCode();
		user.finance_type = this.financeType;// 用户理财类型 0 借款人 1 投资人
		user.store_id = this.store_id;

		String uuid = UUID.randomUUID().toString();
		Qrcode code = new Qrcode();

		try {
			Blob blob = new Blob();
			code.create(this.getSpreadLink(), BarcodeFormat.QR_CODE, 100, 100, new File(blob.getStore(), uuid).getAbsolutePath(),
					"png");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("创建二维码图片失败" + e.getMessage());
			error.code = -5;
			error.msg = "对不起，由于平台出现故障，此次注册失败！";

			return error.code;
		}

		user.qr_code = uuid;

		try {
			user.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("注册时，保存注册信息时：" + e.getMessage());
			error.code = -5;
			error.msg = "对不起，由于平台出现故障，此次注册失败！";

			return error.code;
		}

		this.id = user.id;

		String sign1 = Encrypt.MD5("" + this._id + 0.00 + 0.00 + Constants.ENCRYPTION_KEY);

		// if(Constants.IPS_ENABLE) {
		// sign1 = Encrypt.MD5(""+this._id + 0.00 + 0.00 + 0.00 +
		// Constants.ENCRYPTION_KEY);
		// }

		String sign2 = Encrypt.MD5("" + this._id + 0.00 + 0.00 + 0.00 + 0.00 + Constants.ENCRYPTION_KEY);

		String sql = "update t_users set sign1 = ?, sign2 = ? where id = ?";
		Query query = JPA.em().createQuery(sql).setParameter(1, sign1).setParameter(2, sign2).setParameter(3, this.id);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次注册失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次注册失败！";
			return error.code;
		}

		DealDetail.userEvent(this.id, UserEvent.REGISTER, "注册成功", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		if (StringUtils.startsWith(this.recommendUserName, Constants.NORMAL_CODE_PREFIX)) {
			String strSql = "update t_wealthcircle_invite set invited_user_id = ? ,invited_user_name = ?, invited_register_time = ? ,status=2 where invite_code= ? and status = 1 and is_active = 1";
			Query createQuery = JPA.em().createQuery(strSql).setParameter(1, user.id).setParameter(2, user.name)
					.setParameter(3, new Date()).setParameter(4, this.recommendUserName);
			int result = 0;
			try {
				result = createQuery.executeUpdate();
			} catch (Exception e) {
				JPA.setRollbackOnly();
				e.printStackTrace();
				error.code = -3;
				error.msg = "对不起，注册失败";

				return error.code;
			}

			if (result == 0) {
				JPA.setRollbackOnly();
				error.code = -3;
				error.msg = "对不起，注册失败！";
				return error.code;
			}
		}

		// 发送注册站内信
		TemplateStation message = new TemplateStation();
		message.id = Templets.M_REGISTER;
		String mcontent = message.content;
		if (client == 2) {
			mcontent = mcontent.replace("userName", this.mobile);
		} else {
			mcontent = mcontent.replace("userName", this._name);
		}

		if (message.status) {
			StationLetter letter = new StationLetter();
			letter.senderSupervisorId = 1;
			letter.receiverUserId = this.id;
			letter.title = message.title;
			letter.content = mcontent;

			letter.sendToUserBySupervisor(error);
		}

		User.setCurrUser(this);

		// 发送注册短信
		try {
			TemplateSms sms = new TemplateSms();// 发送短信
			sms.id = Templets.M_REG_MSG;// 发放注册短信;
			String smscontent = sms.content;
			sms.addSmsTask(user.mobile, smscontent);
			Logger.info("******************发送注册短信结束*********************");
		} catch (Exception e1) {
			Logger.error("短信发送失败!");
		}

		AuditItemOZ statistic = new AuditItemOZ();
		statistic.create(this.id); // 创建记录

		error.code = 0;
		error.msg = "恭喜你，注册成功！";

		try {
			// 注册成功 发放注册红包
			String redTypeName = Constants.RED_PACKAGE_TYPE_REGIST;
			long status = Constants.RED_PACKAGE_TYPE_STATUS_ENABLED;
			// t_red_packages_type redPackageType = RedPackage.isExist(redTypeName, status);

			List<t_red_packages_type> reds = RedPackage.getRegRed(redTypeName, status);
			if (null != reds && reds.size() > 0) {

				for (t_red_packages_type redPackageType : reds) {
					String desc = "";
					if (redPackageType.coupon_type == CouponTypeEnum.REDBAG.getCode()) {
						desc = "注册发放红包";
					} else {
						desc = "注册发放加息券";
					}
					RedPackageHistory.sendRedPackage(this, redPackageType, desc);
				}
				Logger.info("注册红包短信通知成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("注册发放红包失败");
		}
		try {
			// 注册成功 发放自定义红包
			List<t_red_packages_type> redPacks = RedPackage.findCustomRedPack();
			if (redPacks != null && redPacks.size() > 0) {
				for (t_red_packages_type redPackageType : redPacks) {
					com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(redPackageType.rules);
					String start = json.getString("start");
					String end = json.getString("end");
					Date today = new Date();
					Date sd = DateUtil.strDateToStartDate(start);
					Date ed = DateUtil.strDateToEndDate(end);
					if (today.getTime() >= sd.getTime() && today.getTime() <= ed.getTime()) {
						User u = User.findCustomRedUser(redPackageType.id, user.id);
						if (u != null) {
							// 自定义红包有效期-自定义日期结束
							long c = ed.getTime() - today.getTime();
							long h = c / 1000 / 3600;
							long y = c / 1000 % 3600;
							if (y > 0) {// 剩余不到1小时 定义为1小时
								h += 1;
							}
							redPackageType.validate_unit = 0;
							redPackageType.validity_time = h;

							if (h == 0) {
								continue;
							}
							String desc = redPackageType.name;
							RedPackageHistory.sendRedPackage(u, redPackageType, desc);
							Logger.info("自定义红包短信通知成功");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("自定义红包失败");
		}

		return error.code;
	}

	/**
	 * 登录
	 * 
	 * @param password
	 * @return
	 */
	public int login(String password, boolean encrypt, int client, ErrorInfo error) {
		error.clear();
		int rows = 0;

		if (this.isAllowLogin) {
			error.code = -1;
			error.msg = "你已经被管理员禁止登录";

			return error.code;
		}

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String ip = DataUtil.getIp();

		if (backstageSet.isOpenPasswordErrorLimit == Constants.OPEN_LOCK && this.isPasswordErrorLocked) {

			long lockTimes = backstageSet.lockingTime * 60 * 1000;
			long leftTime = lockTimes - (System.currentTimeMillis() - this.passwordErrorLockedTime.getTime());

			if (leftTime > 0) {
				error.code = -2;
				error.msg = "由于连续输入错误密码达到上限，用户已经被锁定";

				return error.code;
			}

			this.isPasswordErrorLocked = Constants.FALSE;
			this.passwordContinuousErrors = Constants.ZERO;
			this.passwordErrorLockedTime = null;

			DealDetail.userEvent(this.id, UserEvent.LOGIN, "解除锁定", error);

			if (error.code < 0) {
				JPA.setRollbackOnly();
				return error.code;
			}

		}

		EntityManager em = JPA.em();

		if (!encrypt) {
			// 密码传入为明文则进行加密处理
			password = Encrypt.MD5(password + Constants.ENCRYPTION_KEY);
		}

		if (!password.equalsIgnoreCase(this._password)) {
			error.msg = "对不起，您的密码有误!";

			if (backstageSet.isOpenPasswordErrorLimit == Constants.OPEN_LOCK) {
				this.passwordContinuousErrors += 1;

				if (this.passwordContinuousErrors >= backstageSet.passwordErrorCounts) {

					Query query = em
							.createQuery("update t_users set last_login_time = ?, last_login_ip = ?,"
									+ "password_continuous_errors = ?, is_password_error_locked = ?,"
									+ "password_error_locked_time = ?, login_client = ? where id = ?")
							.setParameter(1, new Date()).setParameter(2, ip).setParameter(3, this.passwordContinuousErrors)
							.setParameter(4, Constants.TRUE).setParameter(5, new Date()).setParameter(6, client)
							.setParameter(7, this.id);

					try {
						rows = query.executeUpdate();
					} catch (Exception e) {
						JPA.setRollbackOnly();
						e.printStackTrace();
						Logger.info("登录密码错误,锁定用户时：" + e.getMessage());
						error.code = -3;
						error.msg = "对不起，由于平台出现故障，此次登录失败！";

						return error.code;
					}

					if (rows == 0) {
						JPA.setRollbackOnly();
						error.code = -1;
						error.msg = "数据未更新";

						return error.code;
					}

					DealDetail.userEvent(this.id, UserEvent.LOGIN, "连续登录错误锁定", error);

					if (error.code < 0) {
						JPA.setRollbackOnly();
						return error.code;
					}

					error.code = -3;
					error.msg = "由于连续输入错误密码达到上限，账号已被锁定，请于" + backstageSet.lockingTime + "分钟后登录";

					return error.code;

				}

				Query query = em
						.createQuery("update t_users set last_login_time = ?, last_login_ip = ?,"
								+ "password_continuous_errors = ?, is_password_error_locked = ?,"
								+ "password_error_locked_time = ?, login_client = ? where id = ?")
						.setParameter(1, new Date()).setParameter(2, "127.0.0.1").setParameter(3, this.passwordContinuousErrors)
						.setParameter(4, this.isPasswordErrorLocked).setParameter(5, null).setParameter(6, client)
						.setParameter(7, this.id);

				try {
					rows = query.executeUpdate();
				} catch (Exception e) {
					JPA.setRollbackOnly();
					e.printStackTrace();
					Logger.info("登录密码错误,更新用户错误次数时：" + e.getMessage());
					error.code = -5;
					error.msg = "对不起，由于平台出现故障，此次登录失败！";

					return error.code;
				}

				if (rows == 0) {
					JPA.setRollbackOnly();
					error.code = -1;
					error.msg = "数据未更新";

					return error.code;
				}

				error.code = -6;

				return error.code;
			}

			error.code = -7;

			return error.code;
		}

		if (backstageSet.isOpenPasswordErrorLimit == Constants.OPEN_LOCK) {
			Query saveUser = em
					.createQuery("update t_users set last_login_time = ?, last_login_ip = ?,"
							+ "login_count = login_count + 1, password_continuous_errors = ?, is_password_error_locked = ?,"
							+ "password_error_locked_time = ?, login_client = ? where id = ?")
					.setParameter(1, new Date()).setParameter(2, ip).setParameter(3, Constants.ZERO)
					.setParameter(4, Constants.FALSE).setParameter(5, null).setParameter(6, client).setParameter(7, this.id);

			try {
				rows = saveUser.executeUpdate();
			} catch (Exception e) {
				JPA.setRollbackOnly();
				e.printStackTrace();
				Logger.info("登录时,更新用户登录信息时：" + e.getMessage());
				error.code = -5;
				error.msg = "对不起，由于平台出现故障，此次登录失败！";

				return error.code;
			}

			if (rows == 0) {
				JPA.setRollbackOnly();
				error.code = -1;
				error.msg = "数据未更新";

				return error.code;
			}
		} else {
			Query saveUser = em
					.createQuery("update t_users set last_login_time = ?, last_login_ip = ?"
							+ ",login_count = login_count + 1, login_client = ? where id = ?")
					.setParameter(1, new Date()).setParameter(2, ip).setParameter(3, client).setParameter(4, this.id);

			try {
				rows = saveUser.executeUpdate();
			} catch (Exception e) {
				JPA.setRollbackOnly();
				e.printStackTrace();
				Logger.info("登录时,更新用户登录信息时：" + e.getMessage());
				error.code = -6;
				error.msg = "对不起，由于平台出现故障，此次登录失败！";

				return error.code;
			}

			if (rows == 0) {
				JPA.setRollbackOnly();
				error.code = -1;
				error.msg = "数据未更新";

				return error.code;
			}
		}

		DealDetail.userEvent(this.id, UserEvent.LOGIN, "登录成功", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		setCurrUser(this);

		utils.Cache cache = CacheManager.getCacheInfo("online_user_" + this.id + "");

		if (null == cache) {
			cache = new utils.Cache();
			long timeout = 1800000;// 单位毫秒
			CacheManager.putCacheInfo("online_user_" + this.id, cache, timeout);
		}

		error.code = 0;

		return error.code;
	}

	/**
	 * 编辑信息
	 * 
	 * @return
	 */
	public void edit(ErrorInfo error) {
		error.clear();

		if (this._sex == 0 || this._sex == 3) {
			error.code = -1;
			error.msg = "请选择性别";

			return;
		}

		if (this._age < 10 || this._age > 120) {
			error.code = -1;
			error.msg = "请输入正确的年龄";

			return;
		}

		if (this.cityId == 0) {
			error.code = -1;
			error.msg = "请选择户口所在地";

			return;
		}

		if (this.educationId == 0) {
			error.code = -1;
			error.msg = "请选择文化程度";

			return;
		}

		if (this.maritalId == 0) {
			error.code = -1;
			error.msg = "请选择婚姻情况";

			return;
		}

		if (this.carId == 0) {
			error.code = -1;
			error.msg = "请选择购车情况";

			return;
		}

		if (this.houseId == 0) {
			error.code = -1;
			error.msg = "请选择购房情况";

			return;
		}

		EntityManager em = JPA.em();

		Query query = em
				.createQuery("update t_users set " + "sex=?," + "birthday=?," + "city_id=?," + "education_id=?," + "marital_id=?,"
						+ "car_id=?," + "house_id=?," + "is_add_base_info=?," + "reality_name=?," + "id_number=?,"
						+ "user_type=? " + "where id = ?")
				.setParameter(1, this._sex).setParameter(2, this.birthday).setParameter(3, this.cityId)
				.setParameter(4, this.educationId).setParameter(5, this.maritalId).setParameter(6, this.carId)
				.setParameter(7, this.houseId).setParameter(8, Constants.TRUE).setParameter(9, this.realityName)
				.setParameter(10, this.idNumber).setParameter(11, UserTypeEnum.PERSONAL.getCode()).setParameter(12, this.id);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("首次编辑基本信息时,保存用户编辑的信息时：" + e.getMessage());
			error.code = -2;
			error.msg = "对不起，由于平台出现故障，此次编辑信息保存失败！";

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return;
		}

		DealDetail.userEvent(this.id, UserEvent.ADD_BASIC_INFORMATION, "添加用户资料", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		setCurrUser(this.id); // 更新缓存数据

		error.msg = "保存基本资料成功";
		error.code = 0;

	}

	/**
	 * 编辑信息(包含实名认证)
	 * 
	 * @return
	 */
	public void editInfo(String realityName, int sex, int age, int city, String idNumber, int education, int marital, int car,
			int house, int financeType, ErrorInfo error) {
		error.clear();

		if (StringUtils.isBlank(realityName)) {
			error.code = -1;
			error.msg = "请输入真实姓名";

			return;
		}

		if (sex == 0 || sex == 3) {
			error.code = -1;
			error.msg = "请选择性别";

			return;
		}

		if (age < 10 || age > 100) {
			error.code = -1;
			error.msg = "请输入正确的年龄,10到100岁";

			return;
		}

		if (StringUtils.isBlank(idNumber)) {
			error.code = -1;
			error.msg = "请输入身份证号码";
		}

		if (city == 0) {
			error.code = -1;
			error.msg = "请选择户口所在地";

			return;
		}

		if (education == 0) {
			error.code = -1;
			error.msg = "请选择文化程度";

			return;
		}

		if (marital == 0) {
			error.code = -1;
			error.msg = "请选择婚姻情况";

			return;
		}

		if (car == 0) {
			error.code = -1;
			error.msg = "请选择购车情况";

			return;
		}

		if (house == 0) {
			error.code = -1;
			error.msg = "请选择购房情况";

			return;
		}

		if (!"".equals(IDCardValidate.chekIdCard(sex, idNumber))) {
			error.code = -1;
			error.msg = "请输入正确的身份证号码";

			return;
		}

		User.isIDNumberExist(idNumber, this.idNumber, financeType, error);

		if (error.code < 0) {
			return;
		}

		this.realityName = realityName;
		this.setSex(sex);
		this.age = age;
		this.setBirthday(this.birthday);
		this.cityId = city;
		this.idNumber = idNumber;
		this.educationId = education;
		this.maritalId = marital;
		this.carId = car;
		this.houseId = house;
		this.isAddBaseInfo = true;

		EntityManager em = JPA.em();

		Query query = em
				.createQuery("update t_users set " + "reality_name=?," + "sex=?," + "birthday=?," + "id_number=?," + "city_id=?,"
						+ "education_id=?," + "marital_id=?," + "car_id=?," + "house_id=?," + "is_add_base_info=? "
						+ "where id = ?")
				.setParameter(1, this.realityName).setParameter(2, this._sex).setParameter(3, this._birthday)
				.setParameter(4, this.idNumber).setParameter(5, this.cityId).setParameter(6, this.educationId)
				.setParameter(7, this.maritalId).setParameter(8, this.carId).setParameter(9, this.houseId)
				.setParameter(10, Constants.TRUE).setParameter(11, this.id);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("首次编辑基本信息时,保存用户编辑的信息时：" + e.getMessage());
			error.code = -2;
			error.msg = "对不起，由于平台出现故障，此次编辑信息保存失败！";

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return;
		}

		DealDetail.userEvent(this.id, UserEvent.ADD_BASIC_INFORMATION, "添加用户资料", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		setCurrUser(this.id); // 更新缓存数据

		error.msg = "保存基本资料成功";
		error.code = 0;

	}

	/**
	 * 安全退出
	 * 
	 * @return
	 */
	public int logout(ErrorInfo error) {
		error.clear();

		EntityManager em = JPA.em();
		Query query = em.createQuery("update t_users set last_logout_time = ? where id = ?").setParameter(1, new Date())
				.setParameter(2, this.id);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("安全退出时,保存安全退出的信息时：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次安全退出信息保存失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		DealDetail.userEvent(this.id, UserEvent.LOGOUT, "安全退出", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		User.removeCurrUser();
		CacheManager.clearByKey("online_user_" + this.id);

		error.code = 0;

		return error.code;
	}

	/**
	 * 更新安全问题
	 * 
	 * @param info
	 * @return
	 */
	public void updateSecretQuestion(boolean flag, ErrorInfo error) {
		error.clear();

		if (this.secretQuestionId1 < 1 || this.secretQuestionId2 < 1 || this.secretQuestionId3 < 1) {
			error.code = -1;
			error.msg = "请选择安全问题";

			return;
		}

		if (StringUtils.isBlank(this.answer1) || StringUtils.isBlank(this.answer2) || StringUtils.isBlank(this.answer3)) {
			error.code = -1;
			error.msg = "安全问题答案不能为空";

			return;
		}

		EntityManager em = JPA.em();
		Query query = null;

		query = em
				.createQuery("update t_users set secret_question_id1=?,"
						+ "secret_question_id2=?,secret_question_id3=?,answer1=?,answer2=?,answer3=?,"
						+ "is_secret_set=?,secret_set_time=? where id=?")
				.setParameter(1, this.secretQuestionId1).setParameter(2, this.secretQuestionId2)
				.setParameter(3, this.secretQuestionId3).setParameter(4, this.answer1).setParameter(5, this.answer2)
				.setParameter(6, this.answer3).setParameter(7, Constants.TRUE).setParameter(8, new Date())
				.setParameter(9, this.id);
		int rows1 = 0;
		int rows2 = 0;

		try {
			rows1 = query.executeUpdate();
			rows2 = JpaHelper
					.execute("update t_dict_secret_questions set use_count = use_count + 1 where " + "id = ? or id = ? or id = ?",
							this.secretQuestionId1, this.secretQuestionId2, this.secretQuestionId2)
					.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("保存安全问题时：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次保存安全问题失败！";

			return;
		}

		if (rows1 == 0 || rows2 == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return;
		}

		DealDetail.userEvent(this.id, flag ? UserEvent.EDIT_QUESTION : UserEvent.RESET_QUESTION, flag ? "修改安全问题" : "重置安全问题",
				error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		this.isSecretSet = true;
		this.questionName1 = SecretQuestion.queryQuestionById(this.secretQuestionId1, error);
		this.questionName2 = SecretQuestion.queryQuestionById(this.secretQuestionId2, error);
		this.questionName3 = SecretQuestion.queryQuestionById(this.secretQuestionId3, error);

		setCurrUser(this);

		error.code = 0;
		error.msg = "安全问题设置成功";

	}

	/**
	 * 安全问题校验
	 * 
	 * @param secretQuestionId1
	 * @param secretQuestionId2
	 * @param secretQuestionId3
	 * @param questionName1
	 * @param questionName2
	 * @param questionName3
	 * @param info
	 * @return
	 */
	public int verifySafeQuestion(String answer1, String answer2, String answer3, ErrorInfo error) {
		error.clear();

		if (StringUtils.isBlank(answer1) || StringUtils.isBlank(answer2) || StringUtils.isBlank(answer3)) {
			error.code = -1;
			error.msg = "请输入安全问题答案";

			return error.code;
		}

		if (!answer1.equals(this.answer1) || !answer2.equals(this.answer2) || !answer3.equals(this.answer3)) {
			error.code = -1;
			error.msg = "对不起，你的安全问题回答错误！";

			return error.code;
		}

		error.code = 0;
		error.msg = "安全问题回答正确";

		return error.code;
	}

	/**
	 * 添加(重置)支付密码
	 * 
	 * @param payPassword1
	 * @param payPassword2
	 * @param info
	 * @return
	 */
	public int addPayPassword(boolean flag, String payPassword1, String payPassword2, ErrorInfo error) {
		error.clear();

		if (StringUtils.isBlank(payPassword1) || StringUtils.isBlank(payPassword2)) {
			error.code = -1;
			error.msg = "请输入交易密码";

			return error.code;
		}
		if (!RegexUtils.isValidPassword(payPassword1)) {
			error.code = -1;
			error.msg = "请设置符合要求的密码";

			return error.code;
		}
		if (!payPassword1.equals(payPassword2)) {
			error.code = -1;
			error.msg = "对不起，两次输入密码不一致！";

			return error.code;
		}

		String payPassword = Encrypt.MD5(payPassword1 + Constants.ENCRYPTION_KEY);

		if (this.password.equalsIgnoreCase(payPassword)) {
			error.code = -1;
			error.msg = "对不起，为了账户安全，请勿将交易密码设置和登录密码一致！";

			return error.code;
		}

		String sql = "update t_users set pay_password = ? where id = ?";
		EntityManager em = JPA.em();
		Query query = em.createQuery(sql).setParameter(1, payPassword).setParameter(2, this.id);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("添加交易密码时：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次保存交易密码失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		DealDetail.userEvent(this.id, flag ? UserEvent.ADD_PAY_PASSWORD : UserEvent.RESET_PAY_PASSWORD,
				flag ? "添加交易密码" : "重置交易密码", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		this._payPassword = payPassword;

		setCurrUser(this);

		if (!flag) {
			error.msg = "交易密码重置成功！";

		} else {

			error.msg = "交易密码添加成功！";
		}

		return 0;
	}

	/**
	 * 修改支付密码
	 * 
	 * @param oldPayPassword  旧密码
	 * @param newPayPassword1 新密码1
	 * @param newPayPassword2 新密码1
	 * @param info
	 * @return
	 */
	public int editPayPassword(String oldPayPassword, String newPayPassword1, String newPayPassword2, ErrorInfo error) {
		error.clear();

		if (StringUtils.isBlank(oldPayPassword)) {
			error.code = -1;
			error.msg = "请输入原交易密码";

			return error.code;
		}

		if (StringUtils.isBlank(newPayPassword1) || StringUtils.isBlank(newPayPassword2)) {
			error.code = -1;
			error.msg = "请输入新交易密码";

			return error.code;
		}

		if (!RegexUtils.isValidPassword(newPayPassword1)) {
			error.code = -1;
			error.msg = "请设置符合要求的密码";

			return error.code;
		}

		if (!newPayPassword1.equals(newPayPassword2)) {
			error.code = -1;
			error.msg = "对不起，两次输入密码不一致！";

			return error.code;
		}

		String oldPassword = Encrypt.MD5(oldPayPassword + Constants.ENCRYPTION_KEY);
		String payPassword = Encrypt.MD5(newPayPassword1 + Constants.ENCRYPTION_KEY);

		if (!oldPassword.equals(this._payPassword)) {
			error.code = -2;
			error.msg = "对不起，密码错误！";

			return error.code;
		}

		if (this._password.equalsIgnoreCase(payPassword)) {
			error.code = -1;
			error.msg = "对不起，为了账户安全，请勿将交易密码设置和登录密码一致！";

			return error.code;
		}

		String sql = "update t_users set pay_password = ? where id = ?";
		EntityManager em = JPA.em();
		Query query = em.createQuery(sql).setParameter(1, payPassword).setParameter(2, this.id);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("更新交易密码时：" + e.getMessage());
			error.code = -3;
			error.msg = "对不起，由于平台出现故障，此次更新交易密码失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		DealDetail.userEvent(this.id, UserEvent.EDIT_PAY_PASSWORD, "修改交易密码", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		Logger.info("用户可用金额：" + this.balanceDetail.user_amount);
		Logger.info("用户冻结金额：" + this.balanceDetail.freeze);

		setCurrUser(this);

		error.code = 0;
		error.msg = "交易密码修改成功！";

		return error.code;
	}

	/**
	 * 校验支付密码
	 * 
	 * @param payPassword
	 * @param error
	 * @return
	 */
	public int verifyPayPassword(String payPassword, ErrorInfo error) {
		error.clear();

		if (StringUtils.isBlank(this._payPassword)) {
			error.code = -1;
			error.msg = "对不起，你还未设置交易密码，请先设置交易密码！";

			return error.code;

		}

		if (!this._payPassword.equalsIgnoreCase(Encrypt.MD5(payPassword + Constants.ENCRYPTION_KEY))) {
			error.code = -1;
			error.msg = "对不起，你的交易密码不正确！";

			return error.code;
		}

		error.code = 0;

		return error.code;

	}

	/**
	 * 更换头像
	 * 
	 * @param file 上传的文件
	 * @return
	 */
	public int editPhoto(ErrorInfo error) {
		error.clear();
		int rows = 0;

		try {
			rows = DataUtil.update("t_users", new String[] { "photo" }, new String[] { "id" },
					new Object[] { this.photo, this._id }, error);
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("更换头像时" + e.getMessage());

			error.code = -1;
			error.msg = "更换头像时出现异常！";

			new User().id = User.currUser().id;

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		DealDetail.userEvent(this.id, UserEvent.EDIT_PHOTO, "更换头像", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		setCurrUser(this);

		error.code = 0;
		error.msg = "更换头像成功！";

		return error.code;
	}

	/**
	 * 修改密码
	 * 
	 * @param oldPassword  旧密码
	 * @param newPassword1 新密码1
	 * @param newPassword2 新密码2
	 * @return
	 */
	public int editPassword(String oldPassword, String newPassword1, String newPassword2, ErrorInfo error) {
		error.clear();

		if (StringUtils.isBlank(oldPassword)) {
			error.code = -1;
			error.msg = "请输入原密码";

			return error.code;
		}

		if (StringUtils.isBlank(newPassword1) || StringUtils.isBlank(newPassword2)) {
			error.code = -1;
			error.msg = "密码不能为空";

			return error.code;
		}

		if (!newPassword1.equals(newPassword2)) {
			error.code = -1;
			error.msg = "对不起，两次密码输入不一致！";

			return error.code;
		}

		if (!RegexUtils.isValidPassword(newPassword1)) {
			error.code = -1;
			error.msg = "请设置符合要求的密码";

			return error.code;
		}

		if (!Encrypt.MD5(oldPassword + Constants.ENCRYPTION_KEY).equalsIgnoreCase(this._password)) {
			error.code = -2;
			error.msg = "密码错误！";

			return error.code;
		}

		EntityManager em = JPA.em();
		String sql = "update t_users set password = ? where id = ?";
		Query query = em.createQuery(sql).setParameter(1, Encrypt.MD5(newPassword1 + Constants.ENCRYPTION_KEY)).setParameter(2,
				this._id);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改密码时时,更新保存用户密码时：" + e.getMessage());
			error.code = -3;
			error.msg = "对不起，由于平台出现故障，此次密码修改保存失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		DealDetail.userEvent(this.id, UserEvent.EDIT_PASSWORD, "修改密码", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		logout(error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		error.code = 0;
		error.msg = "密码修改成功，请重新登录！";

		return error.code;
	}

	/**
	 * 修改密码
	 * 
	 * @param oldPassword  旧密码
	 * @param newPassword1 新密码1
	 * @param newPassword2 新密码2
	 * @return
	 */
	public int editPasswordApp(String oldPassword, String newPassword1, String newPassword2, ErrorInfo error, t_users t) {
		error.clear();

		if (StringUtils.isBlank(oldPassword)) {
			error.code = -1;
			error.msg = "请输入原密码";

			return error.code;
		}

		if (StringUtils.isBlank(newPassword1) || StringUtils.isBlank(newPassword2)) {
			error.code = -1;
			error.msg = "密码不能为空";

			return error.code;
		}

		if (!newPassword1.equals(newPassword2)) {
			error.code = -1;
			error.msg = "对不起，两次密码输入不一致！";

			return error.code;
		}

		if (!RegexUtils.isValidPassword(newPassword1)) {
			error.code = -1;
			error.msg = "请设置符合要求的密码";

			return error.code;
		}

		if (!Encrypt.MD5(oldPassword + Constants.ENCRYPTION_KEY).equalsIgnoreCase(t.password)) {
			error.code = -2;
			error.msg = "密码错误！";

			return error.code;
		}

		EntityManager em = JPA.em();
		String sql = "update t_users set password = ? where id = ?";
		Query query = em.createQuery(sql).setParameter(1, Encrypt.MD5(newPassword1 + Constants.ENCRYPTION_KEY)).setParameter(2,
				this._id);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改密码时时,更新保存用户密码时：" + e.getMessage());
			error.code = -3;
			error.msg = "对不起，由于平台出现故障，此次密码修改保存失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		DealDetail.userEvent(this.id, UserEvent.EDIT_PASSWORD, "修改密码", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		logout(error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		error.code = 0;
		error.msg = "密码修改成功，请重新登录！";

		return error.code;
	}

	/**
	 * 微信修改密码
	 * 
	 * @param newPassword1 新密码1
	 * @param newPassword2 新密码2
	 * @return
	 */
	public int editWechatPassword(String newPassword1, String newPassword2, ErrorInfo error) {
		error.clear();

		if (StringUtils.isBlank(newPassword1) || StringUtils.isBlank(newPassword2)) {
			error.code = -1;
			error.msg = "密码不能为空";

			return error.code;
		}

		if (!newPassword1.equals(newPassword2)) {
			error.code = -1;
			error.msg = "对不起，两次密码输入不一致！";

			return error.code;
		}

		if (!RegexUtils.isValidPassword(newPassword1)) {
			error.code = -1;
			error.msg = "请设置符合要求的密码";

			return error.code;
		}

		EntityManager em = JPA.em();
		String sql = "update t_users set password = ? where id = ?";
		Query query = em.createQuery(sql).setParameter(1, Encrypt.MD5(newPassword1 + Constants.ENCRYPTION_KEY)).setParameter(2,
				this._id);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改密码时时,更新保存用户密码时：" + e.getMessage());
			error.code = -3;
			error.msg = "对不起，由于平台出现故障，此次密码修改保存失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		DealDetail.userEvent(this.id, UserEvent.EDIT_PASSWORD, "修改密码", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		error.code = 0;
		error.msg = "密码修改成功，请重新登录！";

		return error.code;
	}

	/**
	 * 通过手机重置用户密码
	 * 
	 * @param oldPassword
	 * @param newPassword1
	 * @param newPassword2
	 * @param error
	 * @return
	 */
	public static void updatePasswordByMobile(String mobile, String code, String password, String confirmPassword,
			String randomID, String captcha, ErrorInfo error, int financeType) {
		error.clear();

		if (StringUtils.isBlank(mobile)) {
			error.code = -1;
			error.msg = "请输入手机号码";

			return;
		}

		if (Constants.CHECK_PIC_CODE) {

			if (StringUtils.isBlank(captcha)) {
				error.code = -1;
				error.msg = "请输入图形验证码";

				return;
			}

			if (StringUtils.isBlank(randomID) || StringUtils.isBlank((String) Cache.get(randomID))) {
				error.code = -1;
				error.msg = "图形验证码失效，请刷新";

				return;
			}

			String cCode = Cache.get(randomID).toString();
			Cache.delete(randomID);

			if (!captcha.equalsIgnoreCase(cCode)) {
				error.code = -1;
				error.msg = "图形验证码错误";

				return;
			}
		}

		if (StringUtils.isBlank(code)) {
			error.code = -1;
			error.msg = "请输入验证码";

			return;
		}

		if (StringUtils.isBlank(password)) {
			error.code = -1;
			error.msg = "请输入新密码";

			return;
		}

		if (StringUtils.isBlank(confirmPassword)) {
			error.code = -1;
			error.msg = "请输入确认密码";

			return;
		}

		if (!RegexUtils.isMobileNum(mobile)) {
			error.code = -1;
			error.msg = "请输入正确的手机号码";

			return;
		}

		if (!password.equals(confirmPassword)) {
			error.code = -1;
			error.msg = "两次输入密码不一致";

			return;
		}

		long userId = User.queryIdByMobile(mobile, financeType, error);

		if (error.code < 0) {
			error.code = -1;
			error.msg = "该手机号码不存在";

			return;
		}

		if (Constants.CHECK_MSG_CODE) {
			String cCode1 = (Cache.get(mobile)).toString();

			if (cCode1 == null) {
				error.code = -1;
				error.msg = "验证码已失效，请重新点击发送验证码";

				return;
			}

			if (!code.equals(cCode1)) {
				error.code = -1;
				error.msg = "手机验证错误";

				return;
			}
		}

		EntityManager em = JPA.em();
		String sql = "update t_users set password = ? where id = ?";
		Query query = em.createQuery(sql).setParameter(1, Encrypt.MD5(password + Constants.ENCRYPTION_KEY)).setParameter(2,
				userId);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改密码时时,更新保存用户密码时：" + e.getMessage());
			error.code = -3;
			error.msg = "对不起，由于平台出现故障，此次密码修改保存失败！";

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return;
		}

		DealDetail.userEvent(userId, UserEvent.RESET_PASSWORD_MOBILE, "通过手机重置用户密码", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		error.code = 0;
		error.msg = "密码修改成功，下次登录启用新密码！";
	}

	public static void updatePasswordByMobileApp(String mobile, String password, ErrorInfo error, int financeType) {
		error.clear();

		if (StringUtils.isBlank(password)) {
			error.code = -1;
			error.msg = "请输入新密码";

			return;
		}

		if (!RegexUtils.isMobileNum(mobile)) {
			error.code = -1;
			error.msg = "请输入正确的手机号码";

			return;
		}

		long userId = User.queryIdByMobile(mobile, financeType, error);

		if (error.code < 0) {
			error.code = -1;
			error.msg = "该手机号码不存在";

			return;
		}
		if (!RegexUtils.isValidPassword(password)) {
			error.code = -1;
			error.msg = "请设置符合要求的密码";

			return;
		}

		EntityManager em = JPA.em();
		String sql = "update t_users set password = ? where id = ?";
		Query query = em.createQuery(sql).setParameter(1, Encrypt.MD5(password + Constants.ENCRYPTION_KEY)).setParameter(2,
				userId);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改密码时时,更新保存用户密码时：" + e.getMessage());
			error.code = -3;
			error.msg = "对不起，由于平台出现故障，此次密码修改保存失败！";

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return;
		}

		DealDetail.userEvent(userId, UserEvent.RESET_PASSWORD_MOBILE, "通过手机重置用户密码", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		error.code = 0;
		error.msg = "密码修改成功，下次登录启用新密码！";
	}

	/**
	 * 通过邮箱重置用户密码
	 * 
	 * @param password
	 * @param confirmPassword
	 * @param error
	 */
	public void updatePasswordByEmail(String password, String confirmPassword, ErrorInfo error) {
		error.clear();

		if (StringUtils.isBlank(password)) {
			error.code = -1;
			error.msg = "请输入新密码";

			return;
		}

		if (StringUtils.isBlank(confirmPassword)) {
			error.code = -1;
			error.msg = "请输入确认密码";

			return;
		}

		if (!password.equals(confirmPassword)) {
			error.code = -1;
			error.msg = "两次输入密码不一致";

			return;
		}

		EntityManager em = JPA.em();
		String sql = "update t_users set password = ? where id = ?";
		Query query = em.createQuery(sql).setParameter(1, Encrypt.MD5(password + Constants.ENCRYPTION_KEY)).setParameter(2,
				this.id);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改密码时时,更新保存用户密码时：" + e.getMessage());
			error.code = -3;
			error.msg = "对不起，由于平台出现故障，此次密码修改保存失败！";

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return;
		}

		DealDetail.userEvent(this.id, UserEvent.RESET_PASSWORD_EMAIL, "通过邮箱重置用户密码", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		if (currUser() != null) {
			logout(error);

			if (error.code < 0) {
				JPA.setRollbackOnly();
				return;
			}
		}

		error.code = 0;
		error.msg = "重置密码成功，下次登录启用新密码！";
	}

	/**
	 * 通过邮箱重置用户密码
	 * 
	 * @param password
	 * @param confirmPassword
	 * @param error
	 */
	public void updatePayPasswordByEmail(String password, String confirmPassword, ErrorInfo error) {
		error.clear();

		if (StringUtils.isBlank(password)) {
			error.code = -1;
			error.msg = "请输入新密码";

			return;
		}

		if (StringUtils.isBlank(confirmPassword)) {
			error.code = -1;
			error.msg = "请输入确认密码";

			return;
		}

		if (!password.equals(confirmPassword)) {
			error.code = -1;
			error.msg = "两次输入密码不一致";

			return;
		}

		if (!RegexUtils.isValidPassword(confirmPassword)) {
			error.code = -1;
			error.msg = "请设置符合要求的密码";

			return;
		}

		String payPassword = Encrypt.MD5(confirmPassword + Constants.ENCRYPTION_KEY);

		if (this.password.equals(payPassword)) {
			error.code = -1;
			error.msg = "交易密码和登录密码不能一样";

			return;
		}

		EntityManager em = JPA.em();
		String sql = "update t_users set pay_password = ? where id = ?";
		Query query = em.createQuery(sql).setParameter(1, Encrypt.MD5(password + Constants.ENCRYPTION_KEY)).setParameter(2,
				this.id);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改密码时时,更新保存用户密码时：" + e.getMessage());
			error.code = -3;
			error.msg = "对不起，由于平台出现故障，此次密码修改保存失败！";

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return;
		}

		DealDetail.userEvent(this.id, UserEvent.RESET_PASSWORD_EMAIL, "通过邮箱重置用户密码", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		if (currUser() != null) {
			logout(error);

			if (error.code < 0) {
				JPA.setRollbackOnly();
				return;
			}
		}

		error.code = 0;
		error.msg = "重置交易密码成功，下次使用启用新密码！";
	}

	/**
	 * 修改手机号码
	 * 
	 * @param mobile 修改后的手机号码
	 * @return 0运行成功
	 */
	public int editMobile(String code, ErrorInfo error) {
		error.clear();

		if (StringUtils.isBlank(this.mobile)) {
			error.code = -1;
			error.msg = "请输入手机号码";

			return error.code;
		}

		if (!RegexUtils.isMobileNum(this.mobile)) {
			error.code = -1;
			error.msg = "请输入正确的手机号码";

			return error.code;
		}

		if (Constants.CHECK_MSG_CODE) {
			// String cCode = (String) Cache.get(mobile);
			/* 2014-11-17 */
			String cCode = null;
			try {
				Object obj = Cache.get(mobile);
				cCode = obj.toString();
			} catch (Exception e) {
			}

			if (cCode == null) {
				error.code = -1;
				error.msg = "验证码已失效，请重新点击发送验证码";

				return error.code;
			}
			if (!code.equals(cCode)) {
				error.code = -1;
				error.msg = "手机验证错误";

				return error.code;
			}
		}

		isMobileExist(this.mobile, null, error);

		if (error.code < 0) {
			return error.code;
		}

		EntityManager em = JPA.em();
		String sql = "update t_users set mobile = ?,is_mobile_verified = 1 where id = ?";
		Query query = em.createQuery(sql).setParameter(1, this.mobile).setParameter(2, this._id);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("保存手机号码时：" + e.getMessage());
			error.code = -3;
			error.msg = "保存手机号码时出现异常";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		DealDetail.userEvent(this.id, UserEvent.EDIT_MOBILE, "修改绑定手机", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		this.isMobileVerified = true;
		setCurrUser(this);

		error.code = 0;
		error.msg = "安全手机绑定成功！";

		return 0;

	}

	/**
	 * 激活邮箱
	 * 
	 * @param error
	 */
	public void activeEmail(ErrorInfo error) {
		error.clear();

		if (this.isEmailVerified) {
			error.code = -1;
			error.msg = "你的邮箱已激活，无需再次激活";

			return;
		}

		int rows = 0;

		try {
			rows = JpaHelper
					.execute("update t_users set is_email_verified = ? where id = ? and is_email_verified = 0", true, this.id)
					.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("激活邮箱时：" + e.getMessage());
			error.code = -1;
			error.msg = "激活邮箱时出现异常";

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return;
		}

		this.isEmailVerified = true;
		DealDetail.userEvent(this.id, UserEvent.VERIFIED_EMAIL, "激活邮箱", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		// logout(error);

		error.code = 0;
		error.msg = "邮箱已激活，请登录！";
	}

	/**
	 * 管理员激活用户
	 * 
	 * @param supervisorId
	 * @param userId
	 * @param info
	 * @return
	 */
	public static int activeUserBySupervisor(long userId, ErrorInfo error) {
		error.clear();

		int code = DataUtil.update("t_users", new String[] { "is_email_verified" }, new String[] { "id" },
				new Object[] { Constants.TRUE, userId }, error);

		if (code < 0) {
			return code;
		}

		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.VERIFIED_EMAIL, "手动激活用户", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		error.code = 0;
		error.msg = "用户激活成功！";

		return error.code;
	}

	/**
	 * 修改邮箱
	 * 
	 * @param info
	 * @return
	 */
	public int editEmail(ErrorInfo error) {
		error.clear();

		if (StringUtils.isBlank(email)) {
			error.code = -1;
			error.msg = "请输入邮箱";

			return error.code;
		}

		if (!RegexUtils.isEmail(email)) {
			error.code = -1;
			error.msg = "请输入正确的邮箱地址";

			return error.code;
		}

		User.isEmailExist(email, null, error);

		if (error.code < 0) {

			return error.code;
		}

		String sql = "update t_users set email = ?, is_email_verified = 0 where id = ?";

		int rows = 0;

		try {
			rows = JpaHelper.execute(sql, this.email, this.id).executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("更新用户邮箱时：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次更新邮箱失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		DealDetail.userEvent(this.id, UserEvent.EDIT_EMAIL, "修改邮箱", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		this.isEmailVerified = false;
		setCurrUser(this);

		error.code = 0;
		error.msg = "安全邮箱绑定成功，系统将使用新的邮箱，请及时激活";

		return error.code;
	}

	/**
	 * 修改邮箱
	 * 
	 * @param email
	 * @param error
	 * @return
	 */
	public int checkEmail(String email, ErrorInfo error) {
		error.clear();

		if (StringUtils.isBlank(email) || !RegexUtils.isEmail(email)) {
			error.code = -1;
			error.msg = "邮箱格式有误！";

			return error.code;
		}
		User.isEmailExist(email, null, error);
		if (0 > error.code) {

			return error.code;
		}

		String sql = "update t_users t set t.email = ? where t.id = ?";
		Query query = JPA.em().createQuery(sql).setParameter(1, email).setParameter(2, this.id);
		int row = 0;
		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			Logger.error("更新用户邮箱时有误！");
			error.code = -1;
			error.msg = "更新用户信息时有误！";

			return error.code;
		}

		if (0 >= row) {
			error.code = -1;
			error.msg = "邮箱未更新";
		}

		return error.code;
	}

	/**
	 * 实名认证
	 * 
	 * @param realName
	 * @param idNumber
	 */
	public int updateCertification(String realName, String idNumber, Long id, ErrorInfo error) {
		error.clear();

		String sql = "update t_users set reality_name = ?, id_number = ? ,user_type = ? where id = ?";

		/* 创建测试用户修改 换成下面的 */
		// String sql = "update t_users set reality_name = ?, id_number = ?, is_bank =
		// true where id = ?";

		int rows = 0;

		try {
			rows = JpaHelper.execute(sql, realName, idNumber, UserTypeEnum.PERSONAL.getCode(), this.id).executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info("实名认证：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次实名验证失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		this.realityName = realName;
		this.idNumber = idNumber;
		setCurrUser(this);

		error.clear();
		error.msg = "实名认证成功";

		// String redTypeName = Constants.RED_PACKAGE_TYPE_AUTH;//实名认证
		//
		// long status = Constants.RED_PACKAGE_TYPE_STATUS_ENABLED;//启用状态
		//
		// List<t_red_packages_type> reds = RedPackage.getRegRed(redTypeName, status);
		// if(null != reds && reds.size() > 0){
		// String desc = "实名认证发放红包";
		// for(t_red_packages_type redPackageType : reds){
		// RedPackageHistory.sendRedPackage(this, redPackageType,desc);
		// }
		// Logger.error("实名认证发放红包短信通知成功");
		// }
		// 实名认证送积分
		try {
			Score.sendScore(id, Score.AUTH, realName, error);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return error.code;
	}

	/**
	 * 手机认证
	 * 
	 * @param mobile
	 * @param code
	 * @param error
	 */
	public int checkMoible(String mobile, String code, ErrorInfo error) {
		error.clear();

		if (StringUtils.isBlank(mobile)) {
			error.code = -1;
			error.msg = "请输入手机号码";

			return error.code;
		}

		if (!RegexUtils.isMobileNum(mobile)) {
			error.code = -1;
			error.msg = "请输入正确的手机号码";

			return error.code;
		}

		if (Constants.CHECK_MSG_CODE) {
			String cCode = (String) Cache.get(mobile);

			if (cCode == null) {
				error.code = -1;
				error.msg = "验证码已失效，请重新点击发送验证码";

				return error.code;
			}

			if (!code.equals(cCode)) {
				error.code = -1;
				error.msg = "验证码错误";

				return error.code;
			}
		}

		isMobileExist(mobile, null, error);

		if (error.code < 0) {
			return error.code;
		}

		String sql = "update t_users set mobile = ?, is_mobile_verified = ? where id = ?";
		int rows = 0;

		try {
			rows = JpaHelper.execute(sql, mobile, true, this.id).executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info("手机认证：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次更新邮箱失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		this.mobile = mobile;
		this.isMobileVerified = true;
		setCurrUser(this);

		error.clear();
		error.msg = "手机认证成功";

		return error.code;
	}

	/**
	 * 用户名是否存在
	 * 
	 * @param userName
	 * @param info
	 * @return 0不存在
	 */
	public static int isNameExist(String userName, ErrorInfo error) {
		error.clear();
		if (StringUtils.isBlank(userName)) {
			error.code = -1;
			error.msg = "用户名不能为空";

			return error.code;
		}
		String sql = "select name from t_users where name = ?";

		String name = null;

		try {
			name = t_users.find(sql, userName).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("判断用户名是否存在时,根据用户名查询数据时：" + e.getMessage());
			error.code = -10;
			error.msg = "对不起，由于平台出现故障，此次用户名是否存在的判断失败！";

			return error.code;
		}

		if (name != null && name.equalsIgnoreCase(userName)) {
			error.code = -2;
			error.msg = "用户名已存在";

			return -2;
		}

		error.code = 0;
		error.msg = "用户名不存在";

		return error.code;
	}

	/**
	 * 身份证是否存在
	 * 
	 * @param userName
	 * @param info
	 * @return 0不存在
	 */
	public static int isIDNumberExist(String newIdNumber, String OldIdNumber, int financeType, ErrorInfo error) {
		error.clear();

		if (StringUtils.isBlank(newIdNumber)) {
			error.code = -1;
			error.msg = "身份证号码不能为空";

			return error.code;
		}

		String sql = "select id_number from t_users where id_number = ? and finance_type = ? ";
		String name = null;

		try {
			name = t_users.find(sql, newIdNumber, financeType).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("判断身份证是否存在时,根据身份证查询数据时：" + e.getMessage());
			error.code = -10;
			error.msg = "对不起，由于平台出现故障，此次用身份证是否存在的判断失败！";

			return error.code;
		}

		if (name != null) {

			if (null != OldIdNumber && OldIdNumber.equals(name)) {

				error.code = 0;

				return error.code;
			}

			error.code = -2;
			error.msg = "此身份证已开户，请重新输入！";

			return -2;
		}

		error.code = 0;

		return error.code;
	}

	/**
	 * 是否是有效会员
	 * 
	 * @param userName
	 * @param info
	 * @return 0不存在
	 */
	public static int isActiveUser(long userId, ErrorInfo error) {
		error.clear();

		if (userId <= 0) {
			error.code = -1;
			error.msg = "传入参数有误";

			return error.code;
		}

		String sql = "select new t_users(id, is_active) from t_users where id = ?";
		t_users user = null;

		try {
			user = t_users.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("判断身份证是否存在时,根据身份证查询数据时：" + e.getMessage());
			error.code = -10;
			error.msg = "对不起，由于平台出现故障，此次用身份证是否存在的判断失败！";

			return error.code;
		}

		if (user == null) {
			error.code = -1;
			error.msg = "该用户不存在";

			return error.code;
		}

		if (user.is_active) {
			error.code = 0;

			return error.code;
		}

		error.code = -1;

		return error.code;
	}

	/**
	 * 判断更新为有效会员
	 * 
	 * @param userId
	 * @param error
	 */
	public static void updateActive(long userId, ErrorInfo error) {
		error.clear();

		if (User.isActiveUser(userId, error) < 0) {
			int rows = 0;
			try {
				rows = JPA.em().createQuery("update t_users set is_active = 1 where id = ?").setParameter(1, userId)
						.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				Logger.info("更新用户为有效用户时：" + e.getMessage());

				error.code = -1;
				error.msg = "更新有效用户失败";

				return;
			}

			if (rows < 0) {
				error.msg = "更新有效用户失败";
				JPA.setRollbackOnly();

				return;
			}
		}

		error.code = 0;
	}

	/**
	 * 邮箱是否被注册
	 * 
	 * @param email
	 * @param info
	 * @return
	 */
	public static int isEmailExist(String newUserEmail, String oldUserEmail, ErrorInfo error) {
		error.clear();

		if (StringUtils.isBlank(newUserEmail)) {
			error.code = -1;
			error.msg = "请输入邮箱地址";

			return error.code;
		}

		String sql = "select email from t_users where email = ?";
		String email = null;

		try {
			email = t_users.find(sql, newUserEmail).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("判断邮箱是否被注册时,根据邮箱查询数据时：" + e.getMessage());
			error.code = -10;
			error.msg = "对不起，由于平台出现故障，此次邮箱是否被注册判断失败！";

			return error.code;
		}

		if (email != null) {

			if (null != oldUserEmail && oldUserEmail.equals(email)) {
				error.code = 0;

				return error.code;
			}

			error.code = -2;
			error.msg = "该邮箱已经注册";

			return error.code;
		}

		error.code = 0;

		return error.code;
	}

	/**
	 * 手机号码是否已经存在
	 * 
	 * @param email
	 * @return
	 */
	public static int isMobileExist(String newUserMobile, String oldUserMobile, ErrorInfo error) {
		error.clear();

		String sql = "select mobile from t_users where mobile = ?";
		String mobile = null;

		try {
			mobile = t_users.find(sql, newUserMobile).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("判手机号码是否已经存在时,根据手机号码查询数据时：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次手机号码是否已经存在判断失败！";

			return error.code;
		}

		if (mobile != null) {

			if (oldUserMobile != null && oldUserMobile.equals(mobile)) {
				error.code = 0;

				return error.code;
			}

			error.code = -2;
			error.msg = "号码被使用";

			return error.code;
		}

		error.code = 0;

		return 0;
	}

	/**
	 * 忘记用户名，根据email获得用户名，然后发送模板邮件至该邮箱
	 * 
	 * @param email
	 * @return
	 */
	public static int forgetName(String email, ErrorInfo error) {

		return EmailUtil.emailFindUserName(email, error);
	}

	/**
	 * 管理员重置用户密码
	 * 
	 * @param supervisorId
	 * @param userId
	 * @param info
	 * @return
	 */
	public static int resetPasswordBySupervisor(long supervisorId, long userId, ErrorInfo error) {
		error.msg = "";

		String sql = "select new t_users(name, email) from t_users where id = ?";
		t_users user = null;

		try {
			user = t_users.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("管理员重置用户密码时,根据用户ID查询数据时：" + e.getMessage());
			error.msg = "对不起，由于平台出现故障，此次重置用户密码失败！";

			return -1;
		}

		if (user == null) {
			error.msg = "用户不存在，请确认是否操作有误！";

			return -1;
		}

		return EmailUtil.emailFindUserPassword(user.name, user.email, error);
	}

	/**
	 * 管理员将一个用户添加到黑名单
	 * 
	 * @param supervisonId 管理员id
	 * @param id           用户id
	 * @param reason       原因
	 * @param info
	 * @return
	 */
	public static int addBlacklistBySupervisor(long userId, String reason, ErrorInfo error) {
		error.clear();

		if (StringUtils.isBlank(reason)) {
			error.code = -1;
			error.msg = "加入黑名单原因不能为空！";

			return error.code;
		}

		if (userId <= 0) {
			error.code = -1;
			error.msg = "传入参数有误！";

			return error.code;
		}

		/*
		 * 下面注释的内容，应该跳转到编写黑名单理由前判断
		 */
		String sql = "select is_blacklist from t_users where id = ?";
		boolean is_blacklist = false;

		try {
			is_blacklist = t_users.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("管理员添加黑名单时,根据用户ID查询数据时：" + e.getMessage());
			error.msg = "对不起，由于平台出现故障，此次添加黑名单失败！";

			return -1;
		}
		if (is_blacklist) {
			error.msg = "该用户已在黑名单中";
			return -1;
		}

		EntityManager em = JPA.em();
		String updateSql = "update t_users set is_blacklist=?,joined_time=?,joined_reason=? where id=?";
		Query query = em.createQuery(updateSql).setParameter(1, true).setParameter(2, new Date()).setParameter(3, reason)
				.setParameter(4, userId);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("管理员添加黑名单时,更新用户数据时：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次添加黑名单失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ADD_BLACKLIST, "加入黑名单", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		error.code = 0;
		error.msg = "添加黑名单用户成功";

		return 0;
	}

	/**
	 * 管理员将一个用户解除黑名单
	 * 
	 * @param supervisonId 管理员id
	 * @param id           用户id
	 * @param info
	 * @return
	 */
	public static long editBlacklist(long userId, ErrorInfo error) {
		error.clear();

		// String sql = "select is_blacklist from t_users where id = ?";
		// boolean is_blacklist = false;
		//
		// try {
		// is_blacklist = t_users.find(sql, userId).first();
		// } catch(Exception e) {
		// e.printStackTrace();
		// Logger.info("管理员解除黑名单时,根据用户ID查询数据时："+e.getMessage());
		// info.msg = "对不起，由于平台出现故障，此次解除黑名单失败！";
		//
		// return -1;
		// }
		// if(！is_blacklist) {
		// info.msg = "该用户不在黑名单中";
		// return -1;
		// }

		EntityManager em = JPA.em();
		String updateSql = "update t_users set is_blacklist=?,joined_time=?,joined_reason=? where id=?";
		Query query = em.createQuery(updateSql).setParameter(1, false).setParameter(2, null).setParameter(3, null).setParameter(4,
				userId);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("管理员解除黑名单时,更新用户数据时：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次解除黑名单失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DELETE_BLACKLIST, "解除黑名单", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		error.code = 0;
		error.msg = "解除黑名单用户成功";

		return 0;
	}

	/**
	 * 管理员分配用户
	 * 
	 * @param supervisonId   分配的管理员
	 * @param toSupervisonId 被分配管理员的id
	 * @param userId         被分配的用户
	 * @param info
	 * @return
	 */
	public static int assignUser(long supervisorId, String typeStr, long bidId, ErrorInfo error) {
		error.clear();

		long userId = 0;

		if (!NumberUtil.isNumericInt(typeStr)) {
			error.code = -1;
			error.msg = "传入的类型参数有误";

			return error.code;
		}

		String hql = "select user_id from t_bids where id = ?";

		try {
			userId = t_bids.find(hql, bidId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起，由于平台出现故障，此次分配用户失败！";

			return -1;
		}

		if (userId <= 0) {
			error.code = -1;
			error.msg = "传入的类型参数有误";

			return error.code;
		}

		/* 客服可以更改，所以不需要进行此校验 */
		// String sql = "select assigned_to_supervisor_id from t_users where id = ?";
		// long assignedToSupervisorId = -1;
		//
		// try {
		// assignedToSupervisorId = t_users.find(sql, userId).first();
		// } catch(Exception e) {
		// e.printStackTrace();
		// Logger.info("管理员分配用户时,根据用户ID查询数据时："+e.getMessage());
		// error.msg = "对不起，由于平台出现故障，此次分配用户失败！";
		//
		// return -1;
		// }
		//
		// if(assignedToSupervisorId > 0) {
		// error.msg = "该用户已被分配！";
		//
		// return -1;
		// }

		// int type = Integer.parseInt(typeStr);
		//
		// if(type != 2) {
		// error.code = -1;
		// error.msg = "对不起！已有借款标单独分配出去，不能进行分配此会员所有标的操作";
		//
		// return error.code;
		// }

		String sql = "select sum(manage_supervisor_id) from t_bids where user_id = ? and manage_supervisor_id <> ?";
		Long temp = 0l;

		try {
			temp = t_bids.find(sql, userId, supervisorId).first();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (temp > 0) {// 说明该会员已经有单个借款标被分配
			error.code = -1;
			error.msg = "对不起！已有借款标单独分配出去，不能进行分配此会员所有标的操作";

			return error.code;
		}

		EntityManager em = JPA.em();
		String updateSql = "update t_users set assigned_time=?, assigned_to_supervisor_id=? where id=?";
		Query query = em.createQuery(updateSql).setParameter(1, new Date()).setParameter(2, supervisorId).setParameter(3, userId);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("管理员分配用户时,更新用户数据时：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次分配用户失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();

			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		updateSql = "update t_bids set manage_supervisor_id = ? where user_id = ?";
		query = em.createQuery(updateSql).setParameter(1, supervisorId).setParameter(2, userId);
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			Logger.error("管理员分配用户时,添加标的分配人员时：" + e.getMessage());

			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次分配用户失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.REASSIGN_USER, "分配会员所有标", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		error.code = 0;
		error.msg = "用户分配成功!";

		return 0;
	}

	/**
	 * 重新分配用户所有的借款标
	 * 
	 * @param supervisorId
	 * @param typeStr
	 * @param toSupervisorIdStr
	 * @param userIdStr
	 * @param error
	 * @return
	 */
	public static int assignUserAgain(long supervisorId, String typeStr, String toSupervisorIdStr, String userIdStr,
			ErrorInfo error) {
		error.clear();

		long userId = 0;
		long toSupervisorId = 0;

		if (!NumberUtil.isNumericInt(userIdStr)) {
			error.code = -1;
			error.msg = "传入的借款会员ID有误";

			return error.code;
		}

		if (!NumberUtil.isNumericInt(toSupervisorIdStr)) {
			error.code = -2;
			error.msg = "传入的管理员参数有误";

			return error.code;
		}

		if (!NumberUtil.isNumericInt(typeStr)) {
			error.code = -1;
			error.msg = "传入的类型参数有误";

			return error.code;
		}

		userId = Long.parseLong(userIdStr);
		toSupervisorId = Long.parseLong(toSupervisorIdStr);
		EntityManager em = JPA.em();
		String updateSql = "update t_users set assigned_time=?, assigned_to_supervisor_id=? where id=?";

		Query query = em.createQuery(updateSql).setParameter(1, new Date()).setParameter(2, toSupervisorId).setParameter(3,
				userId);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("管理员分配用户时,更新用户数据时：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次分配用户失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.ASSIGN_USER, "分配会员所有标", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		error.code = 0;
		error.msg = "用户分配成功!";

		return 0;
	}

	/**
	 * 模拟登录管理员id加密
	 * 
	 * @param adminId
	 * @return
	 */
	public static String encrypt() {
		Supervisor supervisor = Supervisor.currSupervisor();
		String time = Long.toString(new DateUtil().getHours());
		String id = Long.toString(supervisor.id);
		String all = id + time + Constants.ENCRYPTION_KEY;

		return com.shove.security.Encrypt.MD5(all.trim());
	}

	/**
	 * 管理员锁定用户
	 * 
	 * @param SupervisonId
	 * @param userId
	 * @param info
	 * @return
	 */
	public static int lockUser(long userId, ErrorInfo error) {
		error.clear();

		// String sql = "select is_allow_login from t_users where id = ?";
		// boolean isLocked = false;
		//
		// try {
		// isLocked = t_users.find(sql, userId).first();
		// } catch(Exception e) {
		// e.printStackTrace();
		// Logger.info("管理员锁定用户时,根据用户ID查询数据时："+e.getMessage());
		// info.msg = "对不起，由于平台出现故障，此次锁定用户失败！";
		//
		// return -1;
		// }
		//
		// if(isLocked) {
		// info.msg = "该用户已被锁定！";
		//
		// return -1;
		// }

		EntityManager em = JPA.em();
		String updateSql = "update t_users set is_allow_login=?,lock_time=? where id=?";
		Query query = em.createQuery(updateSql).setParameter(1, Constants.TRUE).setParameter(2, new Date()).setParameter(3,
				userId);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("管理员锁定用户时,更新用户数据时：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次锁定用户失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.LOCK_USER, "锁定", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		error.code = 0;
		error.msg = "锁定用户成功!";

		return 0;
	}

	/**
	 * 管理员开启用户
	 * 
	 * @param SupervisonId
	 * @param userId
	 * @param info
	 * @return
	 */
	public static int openUser(long userId, ErrorInfo error) {
		error.clear();

		// String sql = "select is_allow_login from t_users where id = ?";
		// boolean isLocked = false;
		//
		// try {
		// isLocked = t_users.find(sql, userId).first();
		// } catch(Exception e) {
		// e.printStackTrace();
		// Logger.info("管理员开启用户时,根据用户ID查询数据时："+e.getMessage());
		// info.msg = "对不起，由于平台出现故障，此次开启用户失败！";
		//
		// return -1;
		// }
		//
		// if(!isLocked) {
		// info.msg = "该用户未被锁定";
		//
		// return -1;
		// }

		EntityManager em = JPA.em();
		String updateSql = "update t_users set is_allow_login=?,lock_time=? where id=?";
		Query query = em.createQuery(updateSql).setParameter(1, Constants.FALSE).setParameter(2, null).setParameter(3, userId);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("管理员开启用户时,更新用户数据时：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次开启用户失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.OPEN_USER, "分配会员所有标", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return error.code;
		}

		error.code = 0;
		error.msg = "开启用户成功！";

		return 0;
	}

	/**
	 * 管理员拒收用户的站内信
	 * 
	 * @param SupervisonId 操作的管理员
	 * @param reason       原因
	 * @param info
	 * @return
	 */
	public static int refusedMessage(long supervisorId, long userId, String reason, ErrorInfo error) {
		error.clear();

		/* 查询的时一个布尔值，如果用户不存在会回出现空指针异常，不能用first,需用fetch */
		// String sql = "select is_refused_receive from t_users where id = ?";
		// boolean isRefused = false;
		//
		// try {
		// isRefused = t_users.find(sql, userId).first();
		// } catch(Exception e) {
		// e.printStackTrace();
		// Logger.info("管理员拒收用户的站内信时,根据用户ID查询数据时："+e.getMessage());
		// info.msg = "对不起，由于平台出现故障，此次拒收用户的站内信失败！";
		//
		// return -1;
		// }
		//
		// if(isRefused) {
		// info.msg = "该用户已被拒收";
		// return -1;
		// }

		EntityManager em = JPA.em();
		String updateSql = "update t_users set is_refused_receive=?,refused_time=?,refused_reason=? where id=?";
		Query query = em.createQuery(updateSql).setParameter(1, true).setParameter(2, new Date()).setParameter(3, reason)
				.setParameter(4, userId);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("管理员拒收用户的站内信时,更新用户数据时：" + e.getMessage());
			error.msg = "对不起，由于平台出现故障，此次拒收用户的站内信失败！";

			return -1;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.REFUSE_MSG, "拒收会员站内信", error);

		if (error.code < 0) {
			return error.code;
		}

		error.code = 0;
		error.msg = "添加拒收用户成功";

		return 0;
	}

	/**
	 * 管理员接收用户信息
	 * 
	 * @param supervisorId
	 * @param userId
	 * @param info
	 * @return
	 */
	public static int recieverMessage(long supervisorId, long userId, ErrorInfo error) {
		error.clear();

		// String sql = "select is_refused_receive from t_users where id = ?";
		// boolean isRefused = false;
		//
		// try {
		// isRefused = t_users.find(sql, userId).first();
		// } catch(Exception e) {
		// e.printStackTrace();
		// info.code = -1;
		// info.msg = "查询用户数据出现错误";
		//
		// return info.code;
		// }
		//
		// if(!isRefused) {
		// info.code = -2;
		// info.msg = "该用户未被拒收";
		//
		// return info.code;
		// }

		EntityManager em = JPA.em();
		String updateSql = "update t_users set is_refused_receive=?,refused_time=?,refused_reason=? where id=?";
		Query query = em.createQuery(updateSql).setParameter(1, Constants.FALSE).setParameter(2, null).setParameter(3, null)
				.setParameter(4, userId);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("管理员接收用户的站内信时,更新用户数据时：" + e.getMessage());
			error.code = -3;
			error.msg = "更新用户数据出现错误";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.RECEIVE_MSG, "接收会员站内信", error);

		if (error.code < 0) {
			return error.code;
		}

		error.code = 0;
		error.msg = "解除拒收用户成功";

		return 0;
	}

	/**
	 * 手工充值
	 */
	public static void rechargeByHand(long supervisorId, String name, double amount, ErrorInfo error) {
		error.clear();

		if (amount <= 0) {
			error.code = -1;
			error.msg = "请输入正确的充值金额";

			return;
		}

		if (StringUtils.isBlank(name)) {
			error.code = -1;
			error.msg = "充值对象不能为空";

			return;
		}

		long userId = User.queryIdByUserName(name, error);

		if (userId < 0) {
			error.code = -1;
			error.msg = "该用户名不存在";

			return;
		}

		DataSafety data = new DataSafety();
		data.id = userId;

		if (!data.signCheck(error)) {
			JPA.setRollbackOnly();

			return;
		}

		DealDetail.addUserFund(userId, amount);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		Map<String, Double> forDetail = DealDetail.queryUserFund(userId, error);

		if (error.code < 0) {
			return;
		}

		double balance = forDetail.get("user_amount");
		double freeze = forDetail.get("freeze");
		double receiveAmount = forDetail.get("receive_amount");

		DealDetail detail = new DealDetail(userId, DealType.RECHARGE_HAND, amount, supervisorId, balance, freeze, receiveAmount,
				"管理员手动充值");

		detail.addDealDetail(error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		// 添加充值记录
		User.sequence(userId, 0, amount, Constants.ORDINARY_RECHARGE, error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		data.updateSignWithLock(userId, error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.HAND_RECHARGE, "手工充值", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		// String encryString = Security.encryCookie(userId);

		// User user = (User) Cache.get("userId_" + encryString);
		//
		// if(user != null) {
		// user.balance = balance;
		// }

		t_users user = User.queryUserByUserName(name, error);

		if (error.code < 0) {
			return;
		}

		String date = DateUtil.dateToString(new Date());

		// 发送站内信 [date]财务人员给您充值了￥[money]元，备注：[remark]
		TemplateStation station = new TemplateStation();
		station.id = Templets.M_HAND_RECHARGE;

		if (station.status) {
			String mContent = station.content.replace("date", date);
			mContent = mContent.replace("money", DataUtil.formatString(amount));
			mContent = mContent.replace("remark", "无");

			StationLetter letter = new StationLetter();
			letter.senderSupervisorId = 1;
			letter.receiverUserId = userId;
			letter.title = station.title;
			letter.content = mContent;

			letter.sendToUserBySupervisor(error);
		}

		// 发送邮件 [date] 财务人员给您充值了￥[money]元，备注：[remark]
		TemplateEmail email = new TemplateEmail();
		email.id = Templets.E_HAND_RECHARGE;

		if (email.status) {
			String eContent = email.content.replace("date", date);
			eContent = eContent.replace("money", DataUtil.formatString(amount));
			eContent = eContent.replace("remark", "无");
			email.addEmailTask(user.email, email.title, eContent);
		}

		// 发送短信 尊敬的userName: [date]platformName给您充值了￥money元,备注:remark
		String platformName = BackstageSet.getCurrentBackstageSet().platformName;
		/*
		 * TemplateSms sms = new TemplateSms(); if(StringUtils.isNotBlank(user.mobile))
		 * { sms.id = Templets.S_HAND_RECHARGE;
		 * 
		 * if(sms.status) { String eContent = sms.content.replace("userName",
		 * user.name); eContent = eContent.replace("date", date); eContent =
		 * eContent.replace("platformName", platformName); eContent =
		 * eContent.replace("money", DataUtil.formatString( amount)); eContent =
		 * eContent.replace("remark","无"); TemplateSms.addSmsTask(user.mobile,
		 * eContent); } }
		 */

		error.code = 0;
		error.msg = "手动充值成功！";
	}

	/**
	 * 申请提现,老提现,只支持用户手动提现,不支持后台提现
	 * 
	 * @param money      提现金额
	 * @param bankName   银行名称
	 * @param cardNumber 银行账号
	 * @param flag       是否资金托管账户提现，true：资金托管账户提现，false：平台账户提现
	 * @return
	 */
	public long withdrawal(double amount, long bankId, String payPassword, int type, boolean flag, ErrorInfo error) {
		return withdrawal(amount, bankId, payPassword, type, flag, false, error);
	}

	/**
	 * 申请提现(新),支持后台提现
	 * 
	 * @param money      提现金额
	 * @param bankName   银行名称
	 * @param cardNumber 银行账号
	 * @param flag       是否资金托管账户提现，true：资金托管账户提现，false：平台账户提现
	 * @return
	 */
	public long withdrawal(double amount, long bankId, String payPassword, int type, boolean flag, boolean isAuto,
			ErrorInfo error) {
		error.clear();

		if (!(Constants.IPS_ENABLE && flag) && !isAuto) {// 自动提现没有密码
			if (StringUtils.isBlank(this.payPassword)) {
				error.code = -1;
				error.msg = "对不起，为了您的账户安全，请先设置交易密码";
				JPA.setRollbackOnly();
				return -1;
			}

			if (!Encrypt.MD5(payPassword + Constants.ENCRYPTION_KEY).equalsIgnoreCase(this.payPassword)) {
				error.code = -1;
				error.msg = "对不起，交易密码错误";
				JPA.setRollbackOnly();
				return -1;
			}
		}

		DataSafety data = new DataSafety();

		data.id = this.id;

		if (!data.signCheck(error)) {
			JPA.setRollbackOnly();
			return -1;
		}

		double money = User.queryRechargeIn(this.id, error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return -1;
		}

		v_user_for_details forDetail = DealDetail.queryUserBalance(this.id, error);
		double balance = forDetail.user_amount;
		double freeze = forDetail.freeze;
		double receiveAmount = forDetail.receive_amount;

		if (Constants.IPS_ENABLE) {
			if ((Constants.IS_WITHDRAWAL_INNER && balance - money < amount)
					|| (!Constants.IS_WITHDRAWAL_INNER && ServiceFee.maxWithdralAmount(balance - money) < amount)) {
				error.code = -1;
				error.msg = "对不起，已超出最大提现金额";
				JPA.setRollbackOnly();
				return error.code;
			}
		} else {
			// 非资金托管模式下无需计算最大可提现金额。
			if (Constants.WITHDRAWAL_PAY_TYPE == Constants.ONE
					|| (Constants.WITHDRAWAL_PAY_TYPE == Constants.TWO && balance - money < amount)) {
				error.code = -1;
				error.msg = "对不起，已超出最大提现金额";
				JPA.setRollbackOnly();
				return error.code;
			}
		}

		if (!(Constants.IPS_ENABLE && flag)) {
			UserBankAccounts account = new UserBankAccounts();
			account.id = bankId;

			if (account.id < 0 || account.userId != this.id) {
				error.code = -1;
				error.msg = "请选择正确的银行账号";
				JPA.setRollbackOnly();
				return -1;
			}
		}

		t_user_withdrawals withdrawal = new t_user_withdrawals();

		withdrawal.time = new Date();
		withdrawal.user_id = this.id;
		withdrawal.amount = amount;
		withdrawal.type = type;
		withdrawal.bank_account_id = bankId;
		withdrawal.is_auto_create = isAuto;
		double pFee = Arith.round(User.withdrawalFee(amount), 2);
		withdrawal.service_fee = pFee;
		if (Constants.IPS_ENABLE && flag) {
			withdrawal.status = 3;// 资金托管提交中
		} else {
			withdrawal.status = 0;
		}

		try {
			withdrawal.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("保存申请提现金额时：" + e.getMessage());
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "请保存申请提现金额时出现异常";

			return -1;
		}

		if (!(Constants.IPS_ENABLE && flag)) {

			// 外扣需要扣除手续费
			if (Constants.WITHDRAWAL_PAY_TYPE == Constants.ONE) {
				amount += pFee;
			}

			DealDetail.freezeFund(this.id, amount, error);

			if (error.code < 0) {
				JPA.setRollbackOnly();
				return error.code;
			}

			forDetail = DealDetail.queryUserBalance(this.id, error);
			if (Constants.IPS_ENABLE && !flag) {
				balance = forDetail.user_amount2;
			} else {
				balance = forDetail.user_amount;
			}

			balance = forDetail.user_amount;
			freeze = forDetail.freeze;

			receiveAmount = forDetail.receive_amount;

			DealDetail detail = new DealDetail(this.id, type == 1 ? DealType.FREEZE_WITHDRAWAL : DealType.FREEZE_WITHDRAWAL_P,
					amount, withdrawal.id, balance, freeze, receiveAmount, "冻结提现金额(包含手续费" + pFee + "元)");

			detail.addDealDetail(error);

			if (error.code < 0) {
				JPA.setRollbackOnly();

				return -1;
			}

			data.updateSignWithLock(this.id, error);

			if (error.code < 0) {
				JPA.setRollbackOnly();

				return -1;
			}
		}
		if (isAuto) {
			DealDetail.userEvent(this.id, UserEvent.AUTO_APPLY_WITHDRAWALT, "自动申请提现", error);
		} else {
			DealDetail.userEvent(this.id, UserEvent.APPLY_WITHDRAWALT, "申请提现", error);
		}

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return -1;
		}
		this.balance = balance;
		this.freeze = freeze;
		if (!isAuto) {
			setCurrUser(this);
		}

		error.code = 0;
		error.msg = "提现申请成功，请等待管理员的审核";

		// 发送短信 尊敬的userName: 你在平台申请提现，提现金额￥amount元，请确认是否本人操作
		if (!isAuto) {
			TemplateSms sms = new TemplateSms();
			User user = new User();
			user.id = this.id;
			sms.id = Templets.S_WITHDRAW_APPLICATION;
			if (sms.status && StringUtils.isNotBlank(user.mobile)) {
				String eContent = sms.content.replace("userName", user.name);
				eContent = eContent.replace("amount", DataUtil.formatString(withdrawal.amount));
				TemplateSms.addSmsTask(user.mobile, eContent);
			}
		}
		return withdrawal.id;
	}

	/**
	 * 提现回退（乾多多）
	 * 
	 * @param pMerBillNo
	 */
	public static void rollbackWithdrawal(long withdrawalId, ErrorInfo error) {

		// 如果为已付款状态2，则修改为退回状态-2
		Query query = JpaHelper.execute("update t_user_withdrawals set status = -2, audit_supervisor_id = ?,"
				+ " audit_time = ? where id = ? and status = 2", -1L, new Date(), withdrawalId);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();

			Logger.info("确定提现付款时：" + e.getMessage());

			error.code = -1;
			error.msg = "付款提现失败";

			return;
		}

		if (rows == 0) { // 用于防重复回退
			JPA.setRollbackOnly();

			error.code = -1;
			error.msg = "数据未更新";

			return;
		}

		t_user_withdrawals withdrawal = null;

		try {
			withdrawal = t_user_withdrawals.find("id = ?", withdrawalId, 2).first();
		} catch (Exception e) {
			Logger.error("提现回退（乾多多）查询提现记录时：" + e.getMessage());

			error.code = -1;
			error.msg = "提现回退（乾多多）查询提现记录时出现异常";

			return;
		}

		if (withdrawal == null) {
			error.code = -1;
			error.msg = "查询提现记录不存在";

			return;
		}

		DataSafety data = new DataSafety();

		data.id = withdrawal.user_id;

		if (!data.signCheck(error)) {
			JPA.setRollbackOnly();

			return;
		}

		DealDetail.addUserFund(withdrawal.user_id, withdrawal.amount);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		data.updateSignWithLock(withdrawal.user_id, error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		DealDetail.userEvent(withdrawal.id, UserEvent.ROLLBACK_WITHDRAWALT, "提现回退", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		v_user_for_details forDetail = DealDetail.queryUserBalance(withdrawal.user_id, error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		double balance = forDetail.user_amount;
		double freeze = forDetail.freeze;
		double receiveAmount = forDetail.receive_amount;

		DealDetail detail = new DealDetail(withdrawal.user_id, DealType.ROLLBACK_WITHDRAWAL, withdrawal.amount, withdrawal.id,
				balance, freeze, receiveAmount, "提现回退");

		detail.addDealDetail(error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		error.code = 0;
		error.msg = "提现回退成功";
	}

	/**
	 * 转账
	 * 
	 * @param money     转账金额
	 * @param recivedId 接收方
	 * @return
	 */
	public int transfer(double money, long recivedId) {
		/* 判断转账方可用金额是否足够 */

		/* 根据上面的判断结果，查询接收用户金额 */

		/* 接收方添加金额(总金额，可用金额) */

		/* 保存接收方金额 */

		/* 减少转账方金额（总金额，可用金额） */

		/* 保存接收方金额 */

		/* 添加转账记录 */

		return 0;
	}

	/**
	 * 前台--我的账户--提现（查询提现记录）
	 * 
	 * @param id
	 * @return
	 */
	public static PageBean<v_user_withdrawals> queryWithdrawalRecord(long userId, String typeStr, String beginTimeStr,
			String endTimeStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		int type = 0;
		Date beginTime = null;
		Date endTime = null;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (NumberUtil.isNumericInt(typeStr)) {
			type = Integer.parseInt(typeStr);
		}

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (type < 0 || type > 4) {
			type = 0;
		}

		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("type", type);
		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_WITHDRAWALS);
		sql.append(" and w.user_id = ? ");

		List<Object> params = new ArrayList<Object>();
		params.add(userId);

		if (type != 0) {
			sql.append(SQLTempletes.WITHDRAWAL_TYPE[type]);
		}

		if (beginTime != null) {
			sql.append(" and w.time > ? ");
			params.add(beginTime);
		}

		if (endTime != null) {
			sql.append(" and w.time < ? ");
			params.add(endTime);
		}

		List<v_user_withdrawals> withdrawals = new ArrayList<v_user_withdrawals>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_withdrawals.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			withdrawals = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询提现记录时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询提现记录时出现异常！";

			return null;
		}

		PageBean<v_user_withdrawals> page = new PageBean<v_user_withdrawals>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = withdrawals;

		error.code = 0;
		return page;
	}

	/**
	 * 查询限制内充值的金额（限制时间内充值的金额不能提现）
	 * 
	 * @return
	 */
	public static double queryRechargeIn(long userId, ErrorInfo error) {
		error.clear();

		/*
		 * 01-24 限制时间内提现金额从配置文件读取 int day = Constants.WITHDRAWAL_DAY;
		 * 
		 * if(day == 0) return 0;
		 * 
		 * double amount = 0;
		 * 
		 * String sql =
		 * "select ifnull(sum(detail.amount),0) as amount from t_user_details detail" +
		 * " where detail.user_id = ? and (detail.operation = 1 or detail.operation = 2) and "
		 * + " detail.time > DATE_ADD(now(),INTERVAL - ? DAY)";
		 * 
		 * try{ amount = ((BigDecimal) JPA.em().createNativeQuery(sql) .setParameter(1,
		 * userId).setParameter(2, day) .getSingleResult()).doubleValue();
		 * }catch(Exception e) { e.printStackTrace();
		 * Logger.error("查询提现记录时："+e.getMessage()); error.code = -1; error.msg =
		 * "查询提现记录时出现异常！";
		 * 
		 * return error.code; } error.code = 0;
		 */

		return 0.00;
	}

	/**
	 * 后台--提现管理
	 * 
	 * @param id
	 * @return
	 */
	public static PageBean<v_user_withdrawal_info> queryWithdrawalBySupervisor(long supervisorId, int status, String beginTimeStr,
			String endTimeStr, String key, String orderTypeStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		/* 判断是提现记录的状态 */
		String timeStr = "";
		switch (status) {
		case Constants.WITHDRAWAL_SPEND:
			timeStr = "w.pay_time";
			break;

		case Constants.WITHDRAWAL_NOT_PASS:
			timeStr = "w.audit_time";
			break;

		case Constants.WITHDRAWAL_CHECK_PENDING:

		case Constants.WITHDRAWAL_PAYING:
			timeStr = "w.time";
			break;

		default:
			timeStr = "w.time";
			break;
		}

		Date beginTime = null;
		Date endTime = null;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;
		int orderType = StringUtils.isBlank(orderTypeStr) ? 0 : Integer.parseInt(orderTypeStr);

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_WITHDRAWAL_INFO_V2);

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("key", key);
		conditionMap.put("orderType", orderType);

		if (beginTime != null) {
			sql.append(" and " + timeStr + " >= ? ");
			params.add(beginTime);
		}

		if (endTime != null) {
			sql.append(" and " + timeStr + " <= ? ");
			params.add(endTime);
		}

		if (StringUtils.isNotBlank(key)) {
			sql.append(" and ( t_users.name like ? or t_users.email like ? or t_users.mobile like ? ) ");
			params.add("%" + key + "%");
			params.add("%" + key + "%");
			params.add("%" + key + "%");
		}

		sql.append(" and w.status = ? ");
		params.add(status);

		sql.append(SQLTempletes.WITHDRAWAL_ORDER_TYPE[orderType]);

		List<v_user_withdrawal_info> withdrawals = new ArrayList<v_user_withdrawal_info>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_withdrawal_info.class);

			Logger.info("待审核提现列表" + sql);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			withdrawals = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询提现记录时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询提现记录时出现异常！";

			return null;
		}

		PageBean<v_user_withdrawal_info> page = new PageBean<v_user_withdrawal_info>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = withdrawals;

		error.code = 0;
		return page;
	}

	public static BigDecimal countWithdrawalWaitAudit(ErrorInfo error, String beginTimeStr, String endTimeStr, String key) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		StringBuffer sql = new StringBuffer(
				"select ifnull(sum(t.amount),0) as sumcount from t_user_withdrawals t ,t_users u where u.id = t.user_id AND t.status = 0 ");

		if (StringUtils.isNotBlank(key)) {
			sql.append(" and ( u.name like '%" + key + "%'  or u.email like '%" + key + "%' or u.mobile like '%" + key + "%' )");
		}

		if (null != beginTimeStr && !"".equals(beginTimeStr)) {
			sql.append(" and t.time >= '" + sdf.format(DateUtil.strDateToStartDate(beginTimeStr)) + "'");
		}

		if (null != endTimeStr && !"".equals(endTimeStr)) {
			sql.append(" and t.time <= '" + sdf.format(DateUtil.strDateToEndDate(endTimeStr)) + "'");
		}

		BigDecimal countamount = null;
		try {
			Query q = JPA.em().createNativeQuery(sql.toString());
			countamount = (BigDecimal) q.getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询成功提现总额时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询成功提现总额时异常！";
		}
		return countamount;
	}

	/**
	 * 后台--提现管理--详情
	 */
	public static v_user_withdrawals queryWithdrawalDetailBySupervisor(long supervisorId, long withdrawalId, ErrorInfo error) {
		error.clear();

		if (withdrawalId < 0) {
			error.code = -1;
			error.msg = "传入参数有误！";

			return null;
		}

		v_user_withdrawals withdrawal = null;

		try {

			StringBuffer sql = new StringBuffer();
			List<v_user_withdrawals> v_user_withdrawals_list = null;
			sql.append(SQLTempletes.SELECT);
			sql.append(SQLTempletes.V_USER_WITHDRAWALS);
			sql.append(" and w.id = ?");
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_withdrawals.class);
			query.setParameter(1, withdrawalId);
			query.setMaxResults(1);
			v_user_withdrawals_list = query.getResultList();
			if (v_user_withdrawals_list.size() > 0) {
				withdrawal = v_user_withdrawals_list.get(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询提现管理详情时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询提现管理详情时出现异常！";

			return null;
		}

		error.code = 0;
		return withdrawal;
	}

	public static BigDecimal countWithdrawalPass(ErrorInfo error, String beginTimeStr, String endTimeStr, String key) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		StringBuffer sql = new StringBuffer(
				"select ifnull(sum(t.amount),0) as sumcount from t_user_withdrawals t ,t_users u where u.id = t.user_id AND t.status = 2 ");

		if (StringUtils.isNotBlank(key)) {
			sql.append(" and ( u.name like '%" + key + "%'  or u.email like '%" + key + "%' or u.mobile like '%" + key + "%' )");
		}

		if (null != beginTimeStr && !"".equals(beginTimeStr)) {
			sql.append(" and t.pay_time >= '" + sdf.format(DateUtil.strDateToStartDate(beginTimeStr)) + "'");
		}

		if (null != endTimeStr && !"".equals(endTimeStr)) {
			sql.append(" and t.pay_time <= '" + sdf.format(DateUtil.strDateToEndDate(endTimeStr)) + "'");
		}

		BigDecimal countamount = null;
		try {
			Query q = JPA.em().createNativeQuery(sql.toString());
			countamount = (BigDecimal) q.getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询成功提现总额时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询成功提现总额时异常！";
		}
		return countamount;
	}

	/**
	 * 充值记录查询 统计总额
	 * 
	 * @param error
	 * @return
	 */
	public static BigDecimal countRecharge(ErrorInfo error, int status, int type, String name, String startDate, String endDate) {

		if (status == 1) {
			return new BigDecimal(0);
		}

		StringBuffer sql = new StringBuffer(
				"select ifnull(sum(urd.amount),0) from t_user_recharge_details urd JOIN t_users u ON urd.user_id = u.id where 1=1 ");

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		if (type > 0) {
			sql.append(" and urd.type = " + (type - 1));
		}

		sql.append(" and urd.is_completed = 1");

		if (StringUtils.isNotBlank(name)) {
			sql.append(
					" and ( u.name like '%" + name + "%'  or u.email like '%" + name + "%' or u.mobile like '%" + name + "%' )");
		}

		if (null != startDate && !"".equals(startDate)) {
			sql.append(" and urd.time >= '" + sdf.format(DateUtil.strDateToStartDate(startDate)) + "'");
		}

		if (null != endDate && !"".equals(endDate)) {
			sql.append(" and urd.time <= '" + sdf.format(DateUtil.strDateToEndDate(endDate)) + "'");
		}

		BigDecimal countamount = null;
		try {
			Logger.info("充值合计" + sql.toString());
			Query q = JPA.em().createNativeQuery(sql.toString());
			countamount = (BigDecimal) q.getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询成功充值总额时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询成功充值总额时异常！";
		}
		return countamount;
	}

	/**
	 * 审核提现通过
	 */
	public static void auditWithdrawalPass(long supervisorId, long withdrawalId, ErrorInfo error) {
		error.clear();

		if (withdrawalId <= 0) {
			error.code = -1;
			error.msg = "传入参数有误！";

			return;
		}

		t_user_withdrawals withdrawal = t_user_withdrawals.findById(withdrawalId);

		if (withdrawal.status != 0) {
			error.code = -1;
			error.msg = "状态已更改，请勿重复提交！";

			return;
		}

		int rows = 0;

		try {
			rows = JpaHelper
					.execute("update t_user_withdrawals set status = 1, audit_supervisor_id = ?,"
							+ " audit_time = ? where id = ? and status = 0", supervisorId, new Date(), withdrawalId)
					.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.error("审核提现通过时：" + e.getMessage());
			error.code = -1;
			error.msg = "查审核提现通过时出现异常！";

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return;
		}

		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.AUDIT_WITHDRAWAL, "审核提现通过", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		error.code = 0;
		error.msg = "提现审核通过保存成功！";

		v_user_users user = User.queryUserById(withdrawal.user_id, error);
		if (user != null) {
			SMS.send(user.mobile, "尊敬的用户：您的提现申请已成功，将尽快为您审核到账，请您耐心等待！如有疑问请咨询客服热线021-6438-0510", SMS.SEND_TYPE_SYSTEM);
		}

	}

	/**
	 * @author wangyun 
	 * @date 2019年10月18日
	 * @param amount
	 * @param userId
	 * 亿亿金科未迁移用户还款时自动提现
	 */
	public static void migrationAutoWithdraw(double amount, long userId, int dealType, ErrorInfo error) {
		//已迁移用户自动提现,生成提现账单
		List<UserBankAccounts> userBanks  = UserBankAccounts.queryUserAllBankAccount(userId);
		if(userBanks == null || userBanks.size() == 0 ) {
			error.code = -1;
			error.msg = "用户未绑卡";
			return;
		}
		
		UserBankAccounts userBank = userBanks.get(0);
		User user = new User();
		user.id = userId;
		DataSafety data = new DataSafety();
		
		data.id = userId;

		if (!data.signCheck(error)) {
			error.code = -1;
			error.msg="用户资金被篡改！";
			JPA.setRollbackOnly();
			return;
		}
 

		v_user_for_details forDetail = DealDetail.queryUserBalance(userId, error);
		double balance = forDetail.user_amount;
		double freeze = forDetail.freeze;
		double receiveAmount = forDetail.receive_amount;
 
		t_user_withdrawals withdrawal = new t_user_withdrawals();

		withdrawal.time = new Date();
		withdrawal.user_id = userId;
		withdrawal.amount = amount;
		withdrawal.type = 1;//1 余额提现 2 奖金提现
		withdrawal.bank_account_id = userBank.getId();
		withdrawal.is_auto_create = true;
		double pFee = Arith.round(User.withdrawalFee(amount), 2);
		withdrawal.service_fee = pFee;
		withdrawal.status = 2; //已付款

		try {
			withdrawal.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("自动提现金额时：" + e.getMessage());
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "自动提现金额时出现异常";
			return  ;
		}

		// 减少用户资金
		DealDetail.minusUserFund(userId, amount, error);
		if (error.code < 0) {
			JPA.setRollbackOnly();
			return  ;
		}

		forDetail = DealDetail.queryUserBalance(userId, error);
		balance = forDetail.user_amount;
		freeze = forDetail.freeze;

		receiveAmount = forDetail.receive_amount;
		
		DealDetail detail = new DealDetail(userId, dealType,
				amount, withdrawal.id, balance, freeze, receiveAmount, "自动提现" + amount + "元");

		detail.addDealDetail(error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		data.updateSignWithLock(userId, error);
		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}
		DealDetail.userEvent(userId, UserEvent.AUTO_APPLY_WITHDRAWALT, "自动提现", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}
 
		return;
	}
	/**
	 * 审核提现不通过
	 * 
	 * @param supervisorId
	 * @param withdrawalId
	 * @param reason
	 * @param flag         资金托管第三方是否需要审核 true 需要审核
	 * @param error
	 */
	public static void auditWithdrawalDispass(long supervisorId, long withdrawalId, String reason, boolean flag,
			ErrorInfo error) {
		error.clear();

		if (withdrawalId <= 0) {
			error.code = -1;
			error.msg = "传入参数有误！";

			return;
		}

		if (StringUtils.isBlank(reason)) {
			error.code = -2;
			error.msg = "不通过原因不能为空！";

			return;
		}

		t_user_withdrawals withdrawal = t_user_withdrawals.findById(withdrawalId);

		if (!(withdrawal.status == 0 || withdrawal.status == 1 || withdrawal.status == 4)) {
			error.code = -1;
			error.msg = "状态已更改，请勿重复提交！";

			return;
		}

		DataSafety dataSafety = new DataSafety();
		dataSafety.id = withdrawal.user_id;

		if (!dataSafety.signCheck(error)) {
			error.code = -1;
			error.msg = "用户资金异常！";

			return;
		}

		int rows = 0;

		try {
			if (flag) {
				rows = JpaHelper.execute(
						"update t_user_withdrawals set status = -2, audit_supervisor_id = ?,"
								+ " audit_time = ?, disagree_reason = ? where id = ? and status = 4",
						supervisorId, new Date(), reason, withdrawalId).executeUpdate();
			} else {
				rows = JpaHelper.execute(
						"update t_user_withdrawals set status = -1, audit_supervisor_id = ?,"
								+ " audit_time = ?, disagree_reason = ? where id = ? and (status = 0 or status = 1)",
						supervisorId, new Date(), reason, withdrawalId).executeUpdate();
			}

		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.error("审核提现通过时：" + e.getMessage());
			error.code = -1;
			error.msg = "查审核提现通过时出现异常！";

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return;
		}

		double amount = withdrawal.amount; // 申请提现金额
		double pfee = withdrawal.service_fee; // 提现手续费

		// 外扣解冻服务费
		if (Constants.WITHDRAWAL_PAY_TYPE == Constants.ONE) {
			amount += pfee; // 实际冻结金额 = 申请提现金额 + 外扣手续费
		}

		DealDetail.relieveFreezeFund(withdrawal.user_id, amount, error);

		if (error.code < 0) {

			return;
		}

		v_user_for_details forDetail = DealDetail.queryUserBalance(withdrawal.user_id, error);

		if (error.code < 0) {
			return;
		}

		double balance = forDetail.user_amount;
		if (Constants.IPS_ENABLE) {
			balance = forDetail.user_amount2;
		}
		double freeze = forDetail.freeze;
		double receiveAmount = forDetail.receive_amount;

		DealDetail detail = new DealDetail(withdrawal.user_id, DealType.THAW_WITHDRAWALT, amount, withdrawal.id, balance, freeze,
				receiveAmount, "解冻提现金额");

		if (Constants.IPS_ENABLE) {
			detail.addDealDetail2(error);
		} else {
			detail.addDealDetail(error);
		}

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.AUDIT_WITHDRAWAL, "审核提现不通过", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		dataSafety.updateSignWithLock(withdrawal.user_id, error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		t_users user = User.queryUserByUserId(withdrawal.user_id, error);
		String date = DateUtil.dateToString(withdrawal.time);
		// 发送站内信 [date]您申请的提现￥[money]元，提现失败，请重新申请
		TemplateStation station = new TemplateStation();
		station.id = Templets.M_RECHARGE_FAIL;

		if (station.status) {
			String mContent = station.content.replace("date", date);
			mContent = mContent.replace("money", DataUtil.formatString(withdrawal.amount));

			StationLetter letter = new StationLetter();
			letter.senderSupervisorId = 1;
			letter.receiverUserId = withdrawal.user_id;
			letter.title = station.title;
			letter.content = mContent;

			letter.sendToUserBySupervisor(error);
		}

		// 发送邮件 [date]您申请的提现￥[money]元，提现失败，请重新申请
		TemplateEmail email = new TemplateEmail();
		email.id = Templets.E_RECHARGE_FAIL;

		if (email.status) {
			String eContent = email.content.replace("date", date);
			eContent = eContent.replace("money", DataUtil.formatString(withdrawal.amount));
			email.addEmailTask(user.email, email.title, eContent);
		}

		error.code = 0;
		error.msg = "审核不通过！";
	}

	/**
	 * 提现管理--付款通知初始化
	 * 
	 * @param supervisorId
	 * @param withdrawalId
	 */
	public static v_user_withdrawals withdrawalDetail(long supervisorId, long withdrawalId, ErrorInfo error) {
		error.clear();

		if (withdrawalId <= 0) {
			error.code = -1;
			error.msg = "传入参数有误！";
		}

		v_user_withdrawals withdrawal = null;

		try {
			StringBuffer sql = new StringBuffer();
			List<v_user_withdrawals> v_user_withdrawals_list = null;
			sql.append(SQLTempletes.SELECT);
			sql.append(SQLTempletes.V_USER_WITHDRAWALS);
			sql.append(" and w.id = ?");
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_withdrawals.class);
			query.setParameter(1, withdrawalId);
			query.setMaxResults(1);
			v_user_withdrawals_list = query.getResultList();
			if (v_user_withdrawals_list.size() > 0) {
				withdrawal = v_user_withdrawals_list.get(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询提现管理详情时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询提现管理详情时出现异常！";

			return null;
		}

		error.code = 0;
		return withdrawal;
	}

	/**
	 * 提现管理--付款通知（1站内信、2短信、3邮件） 注意：只适用于资金托管模式
	 * 
	 * @param userId       提现用户id
	 * @param serviceFee   手续费
	 * @param withdrawalId 提现记录id
	 * @param type         通知类型, 以","隔开（1站内信、2短信、3邮件）
	 * @param chargeMode   内扣(false)/外扣(true)
	 * @param successed    true：资金托管模式下，回调结果为提现成功，false：资金托管模式下，回调结果为提现处理中
	 * @param error
	 */
	public static void withdrawalNotice(long userId, double serviceFee, long withdrawalId, String type, boolean chargeMode,
			boolean successed, ErrorInfo error) {
		error.clear();

		if (userId <= 0) {
			error.code = -1;
			error.msg = "传入参数有误！";

			return;
		}

		if (withdrawalId <= 0) {
			error.code = -1;
			error.msg = "传入参数有误！";

			return;
		}

		t_user_withdrawals withdrawal = t_user_withdrawals.findById(withdrawalId);
		double amount = withdrawal.amount;

		// 资金托管审核中，冻结提现金额，并返回
		if (Constants.IS_WITHDRAWAL_AUDIT && !successed) {

			DataSafety data = new DataSafety();

			data.id = userId;

			if (!data.signCheck(error)) {

				return;
			}

			double money = User.queryRechargeIn(userId, error);

			if (error.code < 0) {
				return;
			}

			v_user_for_details forDetail = DealDetail.queryUserBalance(userId, error);
			double balance = forDetail.user_amount;
			double freeze = forDetail.freeze;
			double receiveAmount = forDetail.receive_amount;

			if ((Constants.IS_WITHDRAWAL_INNER && balance - money < amount)
					|| (!Constants.IS_WITHDRAWAL_INNER && balance - money < amount + serviceFee)) {
				error.code = -1;
				error.msg = "对不起，已超出最大提现金额";

				return;
			}

			DealDetail.freezeFund(userId, amount, error);

			if (error.code < 0) {

				return;
			}

			forDetail = DealDetail.queryUserBalance(userId, error);
			balance = forDetail.user_amount;
			freeze = forDetail.freeze;
			receiveAmount = forDetail.receive_amount;

			DealDetail detail = new DealDetail(userId, DealType.FREEZE_WITHDRAWAL, amount, withdrawalId, balance, freeze,
					receiveAmount, "冻结提现金额");

			detail.addDealDetail(error);

			if (error.code < 0) {
				JPA.setRollbackOnly();

				return;
			}

			data.updateSignWithLock(userId, error);

			if (error.code < 0) {
				JPA.setRollbackOnly();

				return;
			}

			// 提现需审核，且返回状态码为M00010F（处理中）
			Query query = JpaHelper.execute("update t_user_withdrawals set status = 4, audit_supervisor_id = ?,"
					+ " audit_time = ? where id = ? and status = 3", -1L, new Date(), withdrawalId);

			int rows = 0;

			try {
				rows = query.executeUpdate();
			} catch (Exception e) {
				JPA.setRollbackOnly();

				Logger.info("确定提现付款时：" + e.getMessage());

				error.code = -1;
				error.msg = "付款提现失败";

				return;
			}

			if (rows == 0) {
				JPA.setRollbackOnly();

				error.code = -1;
				error.msg = "数据未更新";

				return;
			}

			error.code = 0;
			error.msg = "审核中，请耐心等待";

			return;
		}

		if (type == null || type.length() < 1) {
			error.code = -1;
			error.msg = "传入参数有误！";

			return;
		}

		long supervisorId = -1;

		// 更新状态为已提现。并防重复where status != 2
		Query query = JpaHelper.execute("update t_user_withdrawals set status = 2, audit_supervisor_id = ?,"
				+ " pay_time = ? where id = ? and status != 2", supervisorId, new Date(), withdrawalId);
		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("确定提现付款时：" + e.getMessage());

			error.code = -1;
			error.msg = "付款提现失败";

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "提现已成功";

			return;
		}

		DataSafety data = new DataSafety();

		data.id = userId;

		if (!data.signCheck(error)) {
			JPA.setRollbackOnly();

			return;
		}

		if (chargeMode) {
			DealDetail.minusUserFund(userId, amount + serviceFee, error); // 减少账户余额,外扣：实际扣款=提现金额+手续费
		} else {
			DealDetail.minusUserFund(userId, amount, error); // 减少账户余额，内扣：实际扣款=提现金额
		}

		if (error.code < 0) {

			return;
		}

		v_user_for_details forDetail = DealDetail.queryUserBalance(userId, error);

		if (error.code < 0) {
			return;
		}

		double balance = forDetail.user_amount;
		double freeze = forDetail.freeze;
		double receiveAmount = forDetail.receive_amount;

		/* 添加交易记录 */
		DealDetail detail = new DealDetail(userId,
				withdrawal.type == 1 ? DealType.CHARGE_LEFT_WITHDRAWALT : DealType.CHARGE_AWARD_WITHDRAWALT,
				chargeMode ? amount : amount - serviceFee, supervisorId, balance + serviceFee, freeze, receiveAmount, "提现付款");

		detail.addDealDetail(error);
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		// 只要服务费大于0 都需要添加记录
		if (serviceFee >= 0) {

			detail = new DealDetail(userId, DealType.CHARGE_WITHDRAWALT, serviceFee, withdrawalId, balance, freeze, receiveAmount,
					"提现管理费");

			detail.addDealDetail(error);

			if (error.code < 0) {
				JPA.setRollbackOnly();

				return;
			}
		}

		DealDetail.addPlatformDetail(DealType.WITHDRAWAL_FEE, withdrawalId, userId, -1, DealType.ACCOUNT, serviceFee, 1, "提现管理费",
				error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		data.updateSignWithLock(userId, error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.NOTICE_WITHDRAWAL, "提现通知", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		t_users user = User.queryUserByUserId(withdrawal.user_id, error);
		String date = DateUtil.dateToString(withdrawal.time);

		TemplateStation station = new TemplateStation();
		station.id = Templets.M_RECHARGE_SUCCESS;
		TemplateEmail email = new TemplateEmail();
		email.id = Templets.E_RECHARGE_SUCCESS;
		String[] types = type.split(",");

		for (String str : types) {
			int t = 0;

			try {
				t = Integer.parseInt(str);
			} catch (Exception e) {
				error.code = -1;
				error.msg = "付款方式有误!";

				return;
			}
			/*
			 * 调用站内信/短信/邮件的模板
			 */
			switch (t) {
			case 1:
				// 发送站内信 [date]您申请的提现￥[money]元,财务人员已经处理完毕，请您查收
				if (station.status) {
					String mContent = station.content.replace("date", date);
					mContent = mContent.replace("money", DataUtil.formatString(withdrawal.amount));

					StationLetter letter = new StationLetter();
					letter.senderSupervisorId = 1;
					letter.receiverUserId = withdrawal.user_id;
					letter.title = station.title;
					letter.content = mContent;

					letter.sendToUserBySupervisor(error);
				}
				break;
			case 2:
				// 发送邮件 [date]您申请的提现￥[money]元,财务人员已经处理完毕，请您查收

				if (email.status) {
					String eContent = email.content.replace("date", date);
					eContent = eContent.replace("money", DataUtil.formatString(withdrawal.amount));
					email.addEmailTask(user.email, email.title, eContent);
				}
				break;
			}
		}

		error.code = 0;
		error.msg = "通知发送成功！";
	}

	/**
	 * 提现管理--付款通知（1站内信、2短信、3邮件） 普通网关专用 ,固定内扣 version8.0.2
	 * 
	 * @param userId       提现用户id
	 * @param serviceFee   手续费
	 * @param withdrawalId 提现记录id
	 * @param type         通知类型, 以","隔开（1站内信、2短信、3邮件）
	 * @param error
	 */
	public static void withdrawalNoticeForNormalGateway(long userId, double serviceFee, long withdrawalId, String type,
			ErrorInfo error) {
		error.clear();

		if (userId <= 0) {
			error.code = -1;
			error.msg = "传入参数有误！";

			return;
		}

		if (withdrawalId <= 0) {
			error.code = -1;
			error.msg = "传入参数有误！";

			return;
		}

		if (type == null || type.length() < 1) {
			error.code = -1;
			error.msg = "传入参数有误！";

			return;
		}

		t_user_withdrawals withdrawal = t_user_withdrawals.findById(withdrawalId);
		if (withdrawal.status != 1) {
			error.code = -1;
			error.msg = "已提现或正在审核中";

			return;
		}

		double amount = withdrawal.amount;
		// 普通网关，提现服务费直接从withdrawal读取
		serviceFee = withdrawal.service_fee;

		long supervisorId = Supervisor.currSupervisor().id;

		// 更新状态为已提现。并防重复where status != 2
		Query query = JpaHelper.execute("update t_user_withdrawals set status = 2, audit_supervisor_id = ?,"
				+ " pay_time = ? where id = ? and status != 2", supervisorId, new Date(), withdrawalId);
		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("确定提现付款时：" + e.getMessage());

			error.code = -1;
			error.msg = "付款提现失败";

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "提现已成功";

			return;
		}

		DataSafety data = new DataSafety();
		data.id = userId;

		if (!data.signCheck(error)) {
			JPA.setRollbackOnly();
			return;
		}

		DealDetail.minusUserFreezeFund(userId, amount, error); // 减少冻结金额

		if (error.code < 0) {

			return;
		}

		v_user_for_details forDetail = DealDetail.queryUserBalance(userId, error);

		if (error.code < 0) {
			return;
		}

		double balance = forDetail.user_amount;
		double freeze = forDetail.freeze;
		double receiveAmount = forDetail.receive_amount;

		/* 添加交易记录 */
		DealDetail detail = new DealDetail(userId,
				withdrawal.type == 1 ? DealType.CHARGE_LEFT_WITHDRAWALT : DealType.CHARGE_AWARD_WITHDRAWALT, amount - serviceFee,
				supervisorId, balance, freeze + serviceFee, receiveAmount, "提现付款");
		detail.addDealDetail(error);
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		// 只要服务费大于0 都需要添加记录
		if (serviceFee >= 0) {

			detail = new DealDetail(userId, DealType.CHARGE_WITHDRAWALT, serviceFee, withdrawalId, balance, freeze, receiveAmount,
					"提现管理费");

			detail.addDealDetail(error);

			if (error.code < 0) {
				JPA.setRollbackOnly();

				return;
			}
		}

		DealDetail.addPlatformDetail(DealType.WITHDRAWAL_FEE, withdrawalId, userId, -1, DealType.ACCOUNT, serviceFee, 1, "提现管理费",
				error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		data.updateSignWithLock(userId, error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.NOTICE_WITHDRAWAL, "提现通知", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}

		t_users user = User.queryUserByUserId(withdrawal.user_id, error);
		String date = DateUtil.dateToString(withdrawal.time);

		TemplateStation station = new TemplateStation();
		station.id = Templets.M_RECHARGE_SUCCESS;
		TemplateEmail email = new TemplateEmail();
		email.id = Templets.E_RECHARGE_SUCCESS;
		String[] types = type.split(",");

		for (String str : types) {
			int t = 0;

			try {
				t = Integer.parseInt(str);
			} catch (Exception e) {
				error.code = -1;
				error.msg = "付款方式有误!";

				return;
			}
			/*
			 * 调用站内信/短信/邮件的模板
			 */
			switch (t) {
			case 1:
				// 发送站内信 [date]您申请的提现￥[money]元,财务人员已经处理完毕，请您查收
				if (station.status) {
					String mContent = station.content.replace("date", date);
					mContent = mContent.replace("money", DataUtil.formatString(withdrawal.amount));

					StationLetter letter = new StationLetter();
					letter.senderSupervisorId = 1;
					letter.receiverUserId = withdrawal.user_id;
					letter.title = station.title;
					letter.content = mContent;

					letter.sendToUserBySupervisor(error);
				}
				break;
			case 2:
				// 发送邮件 [date]您申请的提现￥[money]元,财务人员已经处理完毕，请您查收

				if (email.status) {
					String eContent = email.content.replace("date", date);
					eContent = eContent.replace("money", DataUtil.formatString(withdrawal.amount));
					email.addEmailTask(user.email, email.title, eContent);
				}
				break;
			}
		}

		error.code = 0;
		error.msg = "通知发送成功！";
	}

	/**
	 * 打印付款单
	 * 
	 * @param supervisorId
	 * @param withdrawalId
	 * @param error
	 */
	public static v_user_withdrawals printPayBill(long withdrawalId, ErrorInfo error) {
		error.clear();

		if (withdrawalId <= 0) {
			error.code = -1;
			error.msg = "传入参数有误！";
		}

		v_user_withdrawals withdrawal = null;

		try {
			StringBuffer sql = new StringBuffer();
			List<v_user_withdrawals> v_user_withdrawals_list = null;
			sql.append(SQLTempletes.SELECT);
			sql.append(SQLTempletes.V_USER_WITHDRAWALS);
			sql.append(" and w.id = ?");
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_withdrawals.class);
			query.setParameter(1, withdrawalId);
			query.setMaxResults(1);
			v_user_withdrawals_list = query.getResultList();
			if (v_user_withdrawals_list.size() > 0) {
				withdrawal = v_user_withdrawals_list.get(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询提现管理详情时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询提现管理详情时出现异常！";

			return null;
		}

		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.PRINT_PAYMENT, "打印付款单", error);

		error.code = 0;

		return withdrawal;
	}

	/**
	 * 查询提现不通过的原因
	 */
	public static Object[] withdrawalDispassReason(long withdrawalId, ErrorInfo error) {
		error.clear();

		if (withdrawalId <= 0) {
			error.code = -1;
			error.msg = "传入参数有误！";
		}

		List<?> objs;
		Object[] reason = null;

		String sql = "SELECT w.disagree_reason,u.name FROM t_user_withdrawals w,t_users u WHERE w.user_id = u.id AND w.id = ?";

		try {
			objs = (List<?>) JPA.em().createQuery(sql).setParameter(1, withdrawalId).getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询提现管理详情时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询提现管理详情时出现异常！";

			return null;
		}

		if (null == objs || 0 == objs.size()) {

			return null;
		}

		reason = (Object[]) objs.get(0);

		return reason;
	}

	/**
	 * 查询用户的黑名单
	 * 
	 * @param info
	 * @return
	 */
	public static PageBean<v_user_blacklist> queryBlacklist(long userId, String key, int currPage, int pageSize,
			ErrorInfo error) {
		error.clear();

		List<v_user_blacklist> myBlacklist = new ArrayList<v_user_blacklist>();

		PageBean<v_user_blacklist> page = new PageBean<v_user_blacklist>();
		page.currPage = currPage;
		page.pageSize = pageSize;

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_BLACKLIST);

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("key", key);

		sql.append(" and t_user_blacklist.user_id = ? ");
		params.add(userId);

		if (StringUtils.isNotBlank(key)) {
			sql.append(" and t_users.name like ? ");
			params.add("%" + key + "%");
		}

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_blacklist.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			myBlacklist = query.getResultList();

			page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询用户黑名单时，根据用户ID查询黑名单时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询用户黑名单时出现异常";

			return null;
		}

		page.page = myBlacklist;
		page.conditions = conditionMap;

		error.code = 0;

		return page;
	}

	/**
	 * 前台—>查询用户的推广会员
	 * 
	 * @param info
	 * @return
	 */
	public static PageBean<v_user_cps_users> queryCpsSpreadUsers(long userId, String typeStr, String key, String yearStr,
			String monthStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		int type = 0;
		int year = -1;
		int month = -1;
		int currPage = 1;
		int pageSize = Constants.PAGE_SIZE;

		if (NumberUtil.isNumericInt(typeStr)) {
			type = Integer.parseInt(typeStr);
		}

		if (NumberUtil.isNumericInt(yearStr)) {
			year = Integer.parseInt(yearStr);
		}

		if (NumberUtil.isNumericInt(monthStr)) {
			month = Integer.parseInt(monthStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (type < 0 || type > 2) {
			type = 0;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_CPS_USERS);

		StringBuffer sqlCount = new StringBuffer("");
		sqlCount.append(SQLTempletes.V_USER_CPS_USERS_COUNT);

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("type", type);
		conditionMap.put("key", key);
		conditionMap.put("year", yearStr);
		conditionMap.put("month", monthStr);

		sql.append(" and t_users.recommend_user_id = ? ");
		sqlCount.append(" and t_users.recommend_user_id = ? ");
		params.add(userId);

		if (type != 0) {
			sql.append(SQLTempletes.MY_CPS[type]);
			sqlCount.append(SQLTempletes.MY_CPS[type]);
		}

		if (StringUtils.isNotBlank(key)) {
			sql.append("and t_users.name like ? ");
			sqlCount.append("and t_users.name like ? ");
			params.add("%" + key + "%");
		}

		if (year != -1 && month != -1) {
			sql.append("and ? = year(t_users.time) and ? = month(t_users.time) ");
			sqlCount.append("and ? = year(t_users.time) and ? = month(t_users.time) ");
			params.add(year);
			params.add(month);
		}

		sql.append("order by t_users.time desc");

		List<v_user_cps_users> users = new ArrayList<v_user_cps_users>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_cps_users.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			Query queryCount = em.createNativeQuery(sqlCount.toString());
			for (int n = 1; n <= params.size(); n++) {
				queryCount.setParameter(n, params.get(n - 1));
			}
			count = Convert.strToInt(queryCount.getResultList().get(0) + "", 0);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询我成功推广的会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询我成功推广的会员时出现异常！";

			return null;
		}

		PageBean<v_user_cps_users> page = new PageBean<v_user_cps_users>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 前台—>查询用户的推广会员下面的统计
	 * 
	 * @return
	 */
	public static v_user_cps_user_count queryCpsCount(long userId, ErrorInfo error) {
		v_user_cps_user_count user = null;
		List<v_user_cps_user_count> v_user_cps_user_count_list = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_CPS_USER_COUNT);
		sql.append(" and t_users.id = ?");

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_cps_user_count.class);
			query.setParameter(1, userId);
			query.setMaxResults(1);
			v_user_cps_user_count_list = query.getResultList();

			if (v_user_cps_user_count_list.size() > 0) {
				user = v_user_cps_user_count_list.get(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询用户的推广会员下面的统计时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询用户的推广会员下面的统计时出现异常！";

			return null;
		}

		error.code = 0;

		return user;
	}

	/**
	 * 前台—>查询用户的推广收入
	 * 
	 * @param userId
	 * @param info
	 * @param currPage
	 * @return
	 */
	public static PageBean<t_user_cps_income> queryCpsSpreadIncome(long userId, String yearStr, String monthStr,
			String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();
		StringBuffer conditions = new StringBuffer("  ");
		List<Object> values = new ArrayList<Object>();
		values.add(userId);
		int year = -1;
		int month = -1;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (NumberUtil.isNumericInt(yearStr)) {
			year = Integer.parseInt(yearStr);

			if (year != -1) {
				conditions.append(" and year = ? ");
				values.add(year);
			}

		}

		if (NumberUtil.isNumericInt(monthStr)) {
			month = Integer.parseInt(monthStr);

			if (month != -1) {
				conditions.append(" and month = ? ");
				values.add(month);
			}

		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		PageBean<t_user_cps_income> page = new PageBean<t_user_cps_income>();

		conditionMap.put("year", year);
		conditionMap.put("month", month);

		if (year != -1 && month != -1) {
			conditions.append(" and year = ? and month = ? ");
			values.add(year);
			values.add(month);
		}

		page.conditions = conditionMap;
		List<t_user_cps_income> users = new ArrayList<t_user_cps_income>();
		List count = null;
		String sql = "select count(1) as amount  from (select id,year,month,user_id,recommend_user_id,spread_user_account,effective_user_account,invalid_user_account, sum(cps_reward) as cps_reward, status"
				+ " from t_user_cps_income where 1=1 and user_id = ?" + conditions.toString() + " group by year, month) a ";

		Query query = JPA.em().createNativeQuery(sql);

		for (int i = 0; i < values.size(); i++) {
			query.setParameter(i + 1, values.get(i));
		}

		try {

			count = query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询我成功推广的会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询我的推广收入时出现异常！";

			return null;
		}

		if (null == count)
			return page;

		int _count = 0;
		if (count.size() > 0) {
			_count = Convert.strToInt(count.get(0) + "", 0);
		}

		if (0 == _count)
			return page;

		String hql = "select * from (select id,year,month,user_id,recommend_user_id,spread_user_account,effective_user_account,invalid_user_account, sum(cps_reward) as cps_reward, status "
				+ " from t_user_cps_income where 1=1 and user_id = ?  group by year, month) as a  where 1 = 1 "
				+ conditions.toString();

		Query query1 = JPA.em().createNativeQuery(hql, t_user_cps_income.class);

		for (int i = 0; i < values.size(); i++) {
			query1.setParameter(i + 1, values.get(i));
		}
		try {
			users = query1.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询我成功推广的会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询我的推广收入时出现异常！";

			return null;
		}

		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = _count;

		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 后台—>推广会员列表
	 * 
	 * @param supervisorId
	 * @param name
	 * @param orderType
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static PageBean<t_cps_info> queryCpsUserInfo(String name, int orderType, int currPage, int pageSize) {
		StringBuffer sql = new StringBuffer("");

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("name", name);
		conditionMap.put("orderType", orderType);

		if (name != null && !name.equals("")) {
			sql.append("name like ? ");
			params.add("%" + name + "%");
		}

		sql.append("order by " + Constants.ORDER_TYPE_CPS_DETAIL[orderType]);

		PageBean<t_cps_info> page = new PageBean<t_cps_info>();
		page.currPage = currPage;
		page.pageSize = pageSize;

		try {
			page.totalCount = (int) t_cps_info.count(sql.toString(), params.toArray());
			page.page = t_cps_info.find(sql.toString(), params.toArray()).fetch(page.currPage, page.pageSize);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询推广会员列表时：" + e.getMessage());

			return null;
		}

		page.conditions = conditionMap;

		return page;
	}

	/**
	 * 后台—>推广会员列表详情
	 * 
	 * @param supervisorId
	 * @param name
	 * @param beginTime
	 * @param endTime
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static PageBean<v_user_cps_detail> queryCpsDetail(long userId, String name, String beginTime, String endTime,
			int currPage, int pageSize) {

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_CPS_DETAIL);
		sql.append(" and recommend_user_id = ? ");
		List<Object> params = new ArrayList<Object>();
		params.add(userId);

		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("name", name);
		conditionMap.put("beginTime", beginTime);
		conditionMap.put("endTime", endTime);

		if (name != null && !name.equals("")) {
			sql.append(" and name like ? ");
			params.add("%" + name + "%");
		}

		if (StringUtils.isNotBlank(beginTime)) {
			sql.append(" and time > ? ");
			params.add(DateUtil.strDateToStartDate(beginTime));
		}

		if (StringUtils.isNotBlank(endTime)) {
			sql.append(" and time < ? ");
			params.add(DateUtil.strDateToEndDate(endTime));
		}

		PageBean<v_user_cps_detail> page = new PageBean<v_user_cps_detail>();
		page.currPage = currPage;
		page.pageSize = pageSize;

		List<v_user_cps_detail> details = new ArrayList<v_user_cps_detail>();

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_cps_detail.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			details = query.getResultList();

			page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询查询推广会员列表详情时：" + e.getMessage());

			return page;
		}

		page.page = details;
		page.conditions = conditionMap;

		return page;
	}

	/**
	 * 后台—>佣金发放明细（operation=4）
	 * 
	 * @param supervisorId
	 * @param name
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static PageBean<t_user_details> queryCpsCommissionDetail(long supervisorId, String name, int currPage, int pageSize) {

		StringBuffer conditions = new StringBuffer("operation = 4 ");
		List<Object> values = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("name", name);

		if (name != null && !name.equals("")) {
			conditions.append("name like ?");
			values.add("%" + name + "%");
		}

		PageBean<t_user_details> page = new PageBean<t_user_details>();
		page.currPage = currPage;
		page.pageSize = pageSize;

		List<t_user_details> details = new ArrayList<t_user_details>();

		try {
			page.totalCount = (int) t_user_details.count(conditions.toString(), values.toArray());
			details = t_user_details.find(conditions.toString(), values.toArray()).fetch(currPage, page.pageSize);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询佣金发放明细时：" + e.getMessage());

			return null;
		}

		page.page = details;
		page.conditions = conditionMap;

		return page;
	}

	/**
	 * 佣金发放明细表（operation=4）
	 * 
	 * @param supervisorId
	 * @param year
	 * @param month
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static PageBean<t_statistic_cps> queryCpsOfferInfo(long supervisorId, int year, int month, int currPage) {

		StringBuffer conditions = new StringBuffer("1=1 ");
		List<Object> values = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("year", year);
		conditionMap.put("month", month);

		PageBean<t_statistic_cps> page = new PageBean<t_statistic_cps>();
		page.currPage = Constants.ONE;
		page.pageSize = Constants.FIVE;

		if (currPage != 0) {
			page.currPage = currPage;
		}

		if (year != 0) {
			conditions.append("and year = ? ");
			values.add(year);
		}

		if (month != 0) {
			conditions.append("and month = ? ");
			values.add(month);
		}

		List<t_statistic_cps> offerInfo = new ArrayList<t_statistic_cps>();

		try {
			page.totalCount = (int) t_statistic_cps.count(conditions.toString(), values.toArray());
			offerInfo = t_statistic_cps.find(conditions.toString(), values.toArray()).fetch(page.currPage, page.pageSize);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询佣金发放明细表时：" + e.getMessage());

			return null;
		}

		page.page = offerInfo;
		page.conditions = conditionMap;

		return page;
	}

	/**
	 * 关注用户
	 * 
	 * @param userId
	 * @param error
	 * @return
	 */
	public static long attentionUser(long userId, long attentionUserId, ErrorInfo error) {
		error.clear();

		long size = 0;

		try {
			size = t_user_attention_users.count("user_id = ? and attention_user_id = ?", userId, attentionUserId);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("关注用户时，获取关注的用户时：" + e.getMessage());
			error.code = -1;
			error.msg = "关注用户失败";

			return error.code;
		}

		if (size > 0) {
			error.code = -1;
			error.msg = "你已经关注了该用户！";

			return error.code;
		}

		t_user_attention_users attention = new t_user_attention_users();

		attention.time = new Date();
		attention.user_id = userId;
		attention.attention_user_id = attentionUserId;

		try {
			attention.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("关注用户时，保存关注的用户时：" + e.getMessage());
			error.code = -1;
			error.msg = "关注用户失败";

			return error.code;
		}

		DealDetail.userEvent(userId, UserEvent.ATTENTION_USER, "关注用户", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}

		error.code = 0;
		error.msg = "关注该用户成功";

		return attention.id;
	}

	/**
	 * 取消关注的用户
	 * 
	 * @param id
	 * @param userId
	 * @param info
	 * @return
	 */
	public static int cancelAttentionUser(long attentionId, ErrorInfo error) {
		error.clear();

		t_user_attention_users attentionUser = null;

		try {
			attentionUser = t_user_attention_users.findById(attentionId);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("取消关注用户时，获取关注的用户时：" + e.getMessage());
			error.code = -1;
			error.msg = "取消关注用户失败！";

			return error.code;
		}

		if (attentionUser == null) {
			error.code = -1;
			error.msg = "取消关注用户不存在！";

			return error.code;
		}

		try {
			attentionUser.delete();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("取消关注用户时，保存取消关注的用户时：" + e.getMessage());
			error.code = -1;
			error.msg = "取消关注用户失败";

			return error.code;
		}

		// String sql = "delete t_user_attention_users where id = ?";
		//
		// try {
		// JpaHelper.execute(sql,attentionId).executeUpdate();
		// }catch (Exception e) {
		// e.printStackTrace();
		// Logger.info("取消关注用户时，保存取消关注的用户时："+e.getMessage());
		// info.code = -1;
		// info.msg = "取消关注用户失败";
		//
		// return info.code;
		// }

		DealDetail.userEvent(attentionUser.user_id, UserEvent.CANCEL_ATTENTION, "取消关注用户", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}

		error.code = 0;
		error.msg = "取消关注用户成功";

		return 0;
	}

	/**
	 * 判断是否为关注的人
	 * 
	 * @param userId
	 * @param attentionUserId
	 * @param error
	 * @return
	 */
	public static boolean isAttentionUser(long userId, long attentionUserId, ErrorInfo error) {
		error.clear();

		long count = 0;

		try {
			count = t_user_attention_users.count("user_id = ? and attention_user_id =?", userId, attentionUserId);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("判断是否为关注用户时：" + e.getMessage());
			error.code = -1;
			error.msg = "根据ID查询关注用户失败";

			return false;
		}

		if (count <= 0) {
			error.code = -1;
			error.msg = "对不起，你并未关注此用户，你无权查看该用户信息！";

			return false;
		}

		error.code = 0;

		return true;
	}

	/**
	 * 根据用户和关注的用户返回关注的信息
	 * 
	 * @param userId
	 * @param attentionUserId
	 * @param error
	 * @return
	 */
	public static t_user_attention_users queryAttentionUser(long userId, long attentionUserId, ErrorInfo error) {
		error.clear();

		t_user_attention_users attentionUser = null;

		try {
			attentionUser = t_user_attention_users.find("user_id = ? and attention_user_id =?", userId, attentionUserId).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("判断是否为关注用户时：" + e.getMessage());
			error.code = -1;
			error.msg = "根据ID查询关注用户失败";

			return null;
		}

		error.code = 0;

		return attentionUser;
	}

	/**
	 * 前台--会员详情
	 * 
	 * @param userId
	 * @param error
	 * @return
	 */
	public static v_user_for_personal queryUserInformation(long userId, ErrorInfo error) {
		error.clear();

		v_user_for_personal userInformation = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_FOR_PERSONAL);
		sql.append(" and t_users.id = ?");

		List<v_user_for_personal> v_user_for_personal_list = null;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_for_personal.class);
			query.setParameter(1, userId);
			query.setMaxResults(1);
			v_user_for_personal_list = query.getResultList();

			if (v_user_for_personal_list.size() > 0) {
				userInformation = v_user_for_personal_list.get(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询会员详情时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询会员详情时出现异常";

			return null;
		}

		error.code = 0;

		return userInformation;
	}

	/**
	 * 修改备注名
	 * 
	 * @param attentionId
	 * @param info
	 * @return
	 */
	public static int updateAttentionUser(long attentionId, String noteName, ErrorInfo error) {
		error.clear();

		if (attentionId <= 0) {
			error.code = -1;
			error.msg = "传入参数有误";

			return error.code;
		}

		if (StringUtils.isBlank(noteName)) {
			error.code = -1;
			error.msg = "备注名不能为空";

			return error.code;
		}

		if (noteName.length() > 10) {
			error.code = -1;
			error.msg = "备注名最多只能输入十个字符！";

			return error.code;
		}

		t_user_attention_users attentionUser = null;

		try {
			attentionUser = t_user_attention_users.findById(attentionId);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("修改备注名,根据ID查询关注用户时：" + e.getMessage());
			error.code = -1;
			error.msg = "根据ID查询关注用户失败";

			return error.code;
		}

		if (attentionUser == null) {
			error.code = -2;
			error.msg = "关注的用户不存在";

			return error.code;
		}

		attentionUser.note_name = noteName;

		try {
			attentionUser.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查修改备注名，保存备注名时：" + e.getMessage());
			error.code = -2;
			error.msg = "保存备注名失败";

			return error.code;
		}

		DealDetail.userEvent(attentionUser.user_id, UserEvent.EDIT_NOTE_NAME, "修改关注用户备注名", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}

		error.code = 0;
		error.msg = "修改备注名成功！";

		return 0;
	}

	/**
	 * 查询关注的用户
	 * 
	 * @param userId
	 * @param info
	 * @return
	 */
	public static PageBean<v_user_attention_info> queryAttentionUsers(long userId, int currPage, int pageSize, ErrorInfo error) {
		error.clear();

		pageSize = Constants.PAGE_SIZE;

		if (currPage == 0) {
			currPage = Constants.ONE;
		}

		if (pageSize == 0) {
			pageSize = Constants.PAGE_SIZE;
		}
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_ATTENTION_INFO);
		sql.append(" and t_user_attention_users.user_id = ? ");
		List<Object> params = new ArrayList<Object>();
		params.add(userId);

		List<v_user_attention_info> attentionUsers = new ArrayList<v_user_attention_info>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_attention_info.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			attentionUsers = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("修改备注名,根据ID查询关注用户时：" + e.getMessage());
			error.code = -1;
			error.msg = "根据ID查询关注用户失败";

			return null;
		}

		PageBean<v_user_attention_info> page = new PageBean<v_user_attention_info>();
		page.currPage = currPage;
		page.pageSize = pageSize;
		page.totalCount = count;

		page.page = attentionUsers;

		error.code = 0;

		return page;
	}

	/**
	 * blackUserId是否在id的黑名单中
	 * 
	 * @param id
	 * @param blackUserId
	 * @param info        错误信息
	 * @return
	 */
	public static int isInMyBlacklist(long id, long blackUserId, ErrorInfo error) {
		error.clear();
		t_user_blacklist myBlack = null;

		Logger.info("id%sblackUserId%s", id, blackUserId);
		String sql = "select count(1) from t_user_blacklist where user_id = ? and black_user_id = ?";
		BigInteger rows = null;

		try {
			rows = (BigInteger) JPA.em().createNativeQuery(sql).setParameter(1, id).setParameter(2, blackUserId)
					.getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("判断是否在黑名单时，根据用户ID查询黑名单时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询黑名单失败";

			return error.code;
		}

		if (rows.intValue() > 0) {
			error.code = -2;
			error.msg = "该用户已在你的黑名单中";

			return error.code;
		}

		error.code = 0;

		return 0;
	}

	/**
	 * 用户添加自己的黑名单
	 * 
	 * @param bidId  关联在标
	 * @param reason 原因
	 * @return
	 */
	public int addBlacklist(long bidId, String reason, ErrorInfo error) {
		error.clear();

		String sql = "select user_id from t_bids where id = ?";
		List<Long> blackUserIds = null;

		try {
			blackUserIds = t_bids.find(sql, bidId).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("用户添加自己的黑名单,根据标ID查询用户ID时" + e.getMessage());
			error.code = -1;
			error.msg = "根据借款标查询用户id失败";

			return error.code;
		}

		if (blackUserIds == null || blackUserIds.size() == 0) {
			error.code = -2;
			error.msg = "关联的标不存在，数据有有误！";

			return error.code;
		}

		if (User.isInMyBlacklist(this.id, blackUserIds.get(0), error) < 0) {

			error.code = -4;
			error.msg = "该用户已在你的黑名单中！";

			return error.code;
		}

		if (reason == "" || reason == null || reason.length() == 0) {
			error.msg = "原因不能为空";
			error.code = -1;

			return error.code;
		}

		t_user_blacklist blacklist = new t_user_blacklist();

		blacklist.time = new Date();
		blacklist.user_id = this.id;
		blacklist.bid_id = bidId;
		blacklist.black_user_id = blackUserIds.get(0);
		blacklist.reason = reason;

		try {
			blacklist.save();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -3;
			error.msg = "添加黑名单失败";

			return error.code;
		}

		DealDetail.userEvent(this.id, UserEvent.ADD_BLACKLIST, "添加黑名单", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}

		error.code = 0;
		error.msg = "添加黑名单成功";

		return 0;
	}

	/**
	 * 用户删除自己的黑名单
	 * 
	 * @param blacklistId
	 * @param info
	 * @return
	 */
	public int deleteBlacklist(long userId, long blacklistId, ErrorInfo error) {
		error.clear();

		if (blacklistId <= 0) {
			error.code = -1;
			error.msg = "传入参数有误";

			return error.code;
		}

		t_user_blacklist blacklist = null;
		try {
			blacklist = t_user_blacklist.findById(blacklistId);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("用户删除自己的黑名单,根据黑名单ID查询黑名单时" + e.getMessage());
			error.code = -1;
			error.msg = "根据黑名单ID查询黑名单失败";

			return error.code;
		}

		if (blacklist == null) {
			error.code = -2;
			error.msg = "操作有误，无数据";

			return error.code;
		}

		try {
			blacklist.delete();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("用户删除自己的黑名单,删除黑名单时" + e.getMessage());
			error.code = -3;
			error.msg = "用户删除自己的黑名单失败";

			return error.code;
		}

		DealDetail.userEvent(userId, UserEvent.DELETE_BLACKLIST, "删除黑名单", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}

		error.code = 0;
		error.msg = "删除黑名单成功";

		return 0;
	}

	/**
	 * 根据id获得用户名
	 * 
	 * @return
	 */
	public static String queryUserNameById(long id, ErrorInfo error) {
		error.clear();

		String sql = "select name from t_users where id = ? limit 1";
		Object userName = null;

		try {
			userName = JPA.em().createNativeQuery(sql).setParameter(1, id).getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("根据id获得用户名时，根据用户ID查询用户名时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询用户名失败";

			return null;
		}

		error.code = 0;

		return userName == null ? "*" : userName.toString();
	}

	/**
	 * 根据id获得用户
	 * 
	 * @return
	 */
	public static t_users queryUserByUserId(long userId, ErrorInfo error) {
		error.clear();

		String sql = "select new t_users(name, mobile, email) from t_users where id = ?";
		t_users user = null;

		try {
			user = t_users.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("根据用户id查询用户时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询用户失败";

			return null;
		}

		error.code = 0;

		return user;
	}

	/**
	 * 根据id获得用户
	 * 
	 * @return
	 */
	public static t_users queryUser2ByUserId(long userId, ErrorInfo error) {
		error.clear();

		String sql = "select new t_users(reality_name, id_number, ips_acct_no, ips_bid_auth_no) from t_users where id = ?";
		t_users user = null;

		try {
			user = t_users.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("根据用户id查询用户时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询用户失败";

			return null;
		}

		error.code = 0;

		return user;
	}

	/**
	 * 根据用户名获得用户
	 * 
	 * @return
	 */
	public static t_users queryUserByUserName(String name, ErrorInfo error) {
		error.clear();

		String sql = "select new t_users(name, mobile, email,id) from t_users where name = ?";
		t_users user = null;

		try {
			user = t_users.find(sql, name).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("根据用户名查询用户时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询用户失败";

			return null;
		}

		error.code = 0;

		return user;
	}

	/**
	 * 根据email获得用户名
	 * 
	 * @return
	 */
	public static t_users queryUserByEmail(String email, ErrorInfo error) {
		error.clear();

		String sql = "select new t_users(id, name) from t_users where email = ?";
		t_users user = null;

		try {
			user = t_users.find(sql, email).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("根据id获得用户名时，根据用户ID查询用户名时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询用户名失败";

			return null;
		}

		error.code = 0;

		return user;
	}

	/**
	 * 根据mobile获得用户名
	 * 
	 * @return
	 */
	public static t_users queryUserByMobile(String mobile, ErrorInfo error) {
		error.clear();

		String sql = "select new t_users(id, name) from t_users where mobile = ?";
		t_users user = null;

		try {
			user = t_users.find(sql, mobile).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("根据mobile获得用户名时，根据用户mobile查询用户名时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询用户名失败";

			return null;
		}

		error.code = 0;

		return user;
	}

	/**
	 * 根据用户id获得该用户的信用额度credit_line
	 * 
	 * @return
	 */
	public static double queryCreditLineById(long id, ErrorInfo error) {
		error.clear();

		String sql = "select credit_line from t_users where id = ?";
		Double creditLine = null;

		try {
			creditLine = t_users.find(sql, id).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询用户信用额度时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询用户信用额度失败";

			return 0;
		}

		if (creditLine == null) {
			error.code = 1;
			error.msg = "信用额度为空";

			return 0;
		}

		error.code = 0;

		return (creditLine == null) ? 0 : creditLine.doubleValue();
	}

	/**
	 * 根据用户名查询id
	 * 
	 * @return
	 */
	public static long queryIdByUserName(String userName, ErrorInfo error) {
		error.clear();

		String sql = "select id from t_users where name = ?";
		List<Long> ids = null;

		try {
			ids = t_users.find(sql, userName).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("根据用户名查询id时，根据用户名查询用户ID时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询用户id失败";

			return error.code;
		}

		if (ids == null || ids.size() == 0) {
			error.code = -1;
			error.msg = "用户不存在";

			return error.code;
		}

		error.code = 0;

		return ids.get(0);
	}

	/**
	 * 根据手机号码查询id
	 * 
	 * @return
	 */
	public static long queryIdByMobile(String mobile, int financeType, ErrorInfo error) {
		error.clear();
		String sql = "";
		if (INVEST == financeType) {
			sql = "select id from t_users where mobile = ?   and (finance_type=? or finance_type is null  )";
		} else if (BORROW == financeType) {
			sql = "select id from t_users where mobile = ? and finance_type=? ";
		}
		List<Long> ids = null;

		try {
			ids = t_users.find(sql, mobile, financeType).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("根据用户名查询id时，根据用户名查询用户ID时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询用户id失败";

			return error.code;
		}

		if (ids == null || ids.size() == 0) {
			error.code = -1;
			error.msg = "用户不存在";

			return error.code;
		}

		error.code = 0;

		return ids.get(0);
	}

	/**
	 * 举报用户
	 * 
	 * @param userName
	 * @param reason
	 * @param bidId            关联的借款标（如果不关联，填0）
	 * @param investTransferId 关联的债权（如果不关联，填0）
	 * @param info
	 * @return
	 */
	public int addReportAUser(String userName, String reason, long bidId, long investTransferId, ErrorInfo error) {
		error.clear();

		long rId = this.queryIdByUserName(userName, error);

		if (rId <= 0) {
			error.code = -1;
			error.msg = "用户不存在";

			return error.code;
		}

		t_user_report_users report = null;

		try {
			report = t_user_report_users.find("user_id = ? and reported_user_id =?", this.id, rId).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("举报用户时，判断用户是否已被举报时：" + e.getMessage());
			error.code = -2;
			error.msg = "查询举报名单失败";

			return error.code;
		}

		if (report != null) {
			error.code = -3;
			error.msg = "该用户已在你的举报名单中";

			return error.code;
		}

		t_user_report_users reportUser = new t_user_report_users();

		reportUser.user_id = this.id;
		reportUser.time = new Date();
		reportUser.reported_user_id = rId;
		reportUser.reason = reason;
		reportUser.relation_bid_id = bidId;
		reportUser.relation_invest_transfer_id = investTransferId;

		try {
			reportUser.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("举报用户时，保存举报信息时：" + e.getMessage());
			error.code = -4;
			error.msg = "查询举报名单失败";

			return error.code;
		}

		DealDetail.userEvent(this.id, UserEvent.REPORT_USER, "举报用户", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}

		error.code = 0;
		error.msg = "举报成功！";

		return 0;
	}

	/**
	 * 更新会员类型
	 * 
	 * @param userId
	 * @param type   1 借款 2 理财
	 * @param info
	 * @return
	 */
	public static int updateMasterIdentity(long userId, int type, int client, ErrorInfo error) {
		error.clear();

		if (type != Constants.ONE && type != Constants.TWO) {
			error.code = -1;
			error.msg = "传入数据有误数据";

			return -1;
		}

		String sql = "select master_identity from t_users where id = ?";

		int identity = -1;

		try {
			identity = t_users.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("更新会员类型" + e.getMessage());
			error.code = -2;
			error.msg = "查询用户信息失败";

			return error.code;
		}

		switch (identity) {
		case Constants._ONE:
			error.code = -3;
			error.msg = "查询用户不存在";

			return error.code;

		case Constants.THREE:
			error.msg = "用户性质为复合会员";

			return 0;

		case Constants.ZERO:

			switch (type) {
			case Constants.ONE:
				forMasterIdentity(userId, Constants.ONE, client, error);

				return error.code;

			case Constants.TWO:
				forMasterIdentity(userId, Constants.TWO, client, error);

				return error.code;
			}

		case Constants.ONE:

			switch (type) {
			case Constants.ONE:
				error.msg = "用户性质为借款会员";
				return 0;

			case Constants.TWO:
				forMasterIdentity(userId, Constants.THREE, client, error);

				return error.code;
			}

		case Constants.TWO:

			switch (type) {
			case Constants.ONE:
				forMasterIdentity(userId, Constants.THREE, client, error);

				return error.code;

			case Constants.TWO:
				error.msg = "用户性质为借款会员";

				return 0;

			}

		}

		error.code = -8;
		error.msg = "未知错误";

		return error.code;
	}

	private static void forMasterIdentity(long userId, int masterIdentity, int client, ErrorInfo error) {
		EntityManager em = JPA.em();

		String master_time = "";

		if (masterIdentity == Constants.ONE) {
			master_time = "master_time_loan";
		} else if (masterIdentity == Constants.TWO) {
			master_time = "master_time_invest";
		} else {
			master_time = "master_time_complex";
		}

		Query queryTwo = em
				.createQuery("update t_users set master_identity = ?, " + master_time + " = ?, master_client = ? where id = ?")
				.setParameter(1, masterIdentity).setParameter(2, new Date()).setParameter(3, client).setParameter(4, userId);

		int rows = 0;

		try {
			rows = queryTwo.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("更新会员类型" + e.getMessage());
			error.code = -7;
			error.msg = "更新用户性质失败";

			Logger.error("-----------111User6575-------------");

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			Logger.error("-----------111User6584-------------");

			return;
		}

		error.code = 0;
	}

	/**
	 * 财富统计
	 * 
	 * @param id
	 * @param info
	 * @return
	 */
	public v_user_invest_amount getInvestAmount() {
		v_user_invest_amount investAmounts = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_INVEST_AMOUNT);
		sql.append(" and t_users.id = ?");

		List<v_user_invest_amount> v_user_invest_amount_list = null;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_invest_amount.class);
			query.setParameter(1, id);
			query.setMaxResults(1);
			v_user_invest_amount_list = query.getResultList();

			if (v_user_invest_amount_list.size() > 0) {
				investAmounts = v_user_invest_amount_list.get(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询财富统计时：" + e.getMessage());

			return null;
		}

		return investAmounts;
	}

	/*********************************************
	 * 性别，房车，婚姻等的查询
	 ***********************/
	public static void queryBasic() {

		List<t_dict_cars> cars = null;
		List<t_dict_ad_provinces> provinces = null;
		List<t_dict_ad_citys> citys = null;
		List<t_dict_educations> educations = null;
		List<t_dict_houses> houses = null;
		List<t_dict_maritals> maritals = null;
		List<t_new_province> newProvince = null;
		List<t_new_city> newCity = null;

		try {
			cars = t_dict_cars.findAll();
			provinces = t_dict_ad_provinces.findAll();
			citys = t_dict_ad_citys.findAll();
			educations = t_dict_educations.findAll();
			houses = t_dict_houses.findAll();
			maritals = t_dict_maritals.findAll();
			newProvince = t_new_province.findAll();
			newCity = t_new_city.findAll();

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询基本信息时：" + e.getMessage());

			return;
		}

		Cache.set("cars", cars);
		Cache.set("provinces", provinces);
		Cache.set("citys", citys);
		Cache.set("educations", educations);
		Cache.set("houses", houses);
		Cache.set("maritals", maritals);
		Cache.set("newProvince", newProvince);
		Cache.set("newCity", newCity);
	}

	public static List<t_dict_maritals> queryAllMaritals() {
		List<t_dict_maritals> maritals = (List<t_dict_maritals>) Cache.get("maritals");
		if (maritals == null) {
			User.queryBasic();
			maritals = (List<t_dict_maritals>) Cache.get("maritals");
		}
		return maritals;

	}

	/**
	 * 根据provinceId查询所有的市
	 * 
	 * @param citys
	 * @param cityId
	 * @return
	 */
	public static List<t_dict_ad_citys> queryCity(long provinceId) {

		List<t_dict_ad_citys> citys = (List<t_dict_ad_citys>) Cache.get("citys");

		if (citys == null) {
			User.queryBasic();
			citys = (List<t_dict_ad_citys>) Cache.get("citys");
		}
		List<t_dict_ad_citys> cityList = new ArrayList<t_dict_ad_citys>();

		for (t_dict_ad_citys city : citys) {

			if (city.province_id == provinceId) {
				cityList.add(city);
			}
		}

		return cityList;
	}

	/**
	 * 根据provinceId查询所有的市
	 * 
	 * @param citys
	 * @param cityId
	 * @return
	 */
	public static List<t_new_city> queryNewCity(String provinceId) {

		List<t_new_city> citys = (List<t_new_city>) Cache.get("newCity");

		if (citys == null) {
			User.queryBasic();
			citys = (List<t_new_city>) Cache.get("newCity");
		}
		List<t_new_city> cityList = new ArrayList<t_new_city>();

		for (t_new_city city : citys) {

			if (city.father.equals(provinceId)) {
				cityList.add(city);
			}
		}

		return cityList;
	}

	/**
	 * 根据市查询省
	 * 
	 * @param citys
	 * @param cityId
	 * @return
	 */
	public static int queryProvince(long cityId) {

		t_dict_ad_citys city = t_dict_ad_citys.findById(cityId);

		if (city == null) {
			return -1;
		}

		return city.province_id;
	}

	/**
	 * 根据市查询省
	 * 
	 * @param citys
	 * @param cityId
	 * @return
	 */
	public static String queryNewProvince(long cityId) {

		t_new_city city = t_new_city.findById(cityId);

		if (city == null) {
			return "";
		}

		return city.father;
	}

	public static List<t_new_province> queryAllNewProvince() {
		List<t_new_province> province = (List<t_new_province>) Cache.get("newProvince");
		if (province == null) {
			User.queryBasic();
			province = (List<t_new_province>) Cache.get("newProvince");
		}
		return province;
	}

	/**
	 * 获取App调用的用户信息
	 * 
	 * @param id
	 * @return
	 */
	public static User currAppUser(String id) {

		User user = (User) Cache.get("userId_" + id);

		if (user == null) {

			return null;
		}

		return user;
	}

	/**
	 * 获得当前缓存中的user
	 * 
	 * @return
	 */
	public static User currUser() {
		/* 定时任务下，无法获取当前登录用户 */
		if (Session.current() == null) {
			return null;
		}

		String encryString = Session.current().getId();
		if (StringUtils.isBlank(encryString)) {
			return null;
		}

		String userId = Cache.get("front_" + encryString) + "";
		if (StringUtils.isBlank(userId)) {

			return null;
		}
		User user = (User) Cache.get("userId_" + userId);
		if (user == null) {

			return null;
		}

		// 被管理员锁定，退出登录
		if (user.isAllowLogin) {
			user.removeCurrUser();

			return null;
		}

		return user;
	}

	/**
	 * 添加cookie和cache
	 * 
	 * @param user
	 */
	public static void setCurrUser(User user) {

		if (user == null) {
			return;
		}

		/* 定时任务下，无法设置当前登录用户 */
		if (Session.current() == null) {
			return;
		}

		String encryString = Session.current().getId();
		// 设置用户凭证
		// Cache.set("front_"+encryString, user.id, Constants.CACHE_TIME_HOURS_12);
		Cache.set("front_" + encryString, user.id, Constants.CACHE_TIME_MINUS_30);
		// 设置用户登录成功信息
		// Cache.set("userId_"+user.id, user, Constants.CACHE_TIME_HOURS_12);
		Cache.set("userId_" + user.id, user, Constants.CACHE_TIME_MINUS_30);
	}

	/**
	 * 设置当前用户
	 * 
	 * @param userId
	 */
	public static void setCurrUser(long userId) {
		if (userId < 1) {
			return;
		}

		User user = new User();
		user.id = userId;
		User.setCurrUser(user);
	}

	/**
	 * 退出时清除cookie和缓存
	 */
	public static void removeCurrUser() {
		String encryString = Session.current().getId();
		String userId = Cache.get("front_" + encryString) + "";
		Cache.delete("front_" + encryString);
		Cache.delete("userId_" + userId);

		cleanCacheLock(userId);
	}

	/**
	 * 账户总览查询（账户总览--温馨提示）
	 * 
	 * @param userId
	 * @return
	 */
	public static v_user_account_statistics queryAccountStatistics(long userId, ErrorInfo error) {
		error.clear();

		v_user_account_statistics accountStatistics = null;
		List<v_user_account_statistics> v_user_account_statistics_list = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_ACCOUNT_STATISTICS);
		sql.append(" and a.id = ?");

		try {
			// findById：如果id不存在，则得到空
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_account_statistics.class);
			query.setParameter(1, userId);
			query.setMaxResults(1);
			v_user_account_statistics_list = query.getResultList();

			if (v_user_account_statistics_list.size() > 0) {
				accountStatistics = v_user_account_statistics_list.get(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询财富统计(温馨提示)时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询财富统计(温馨提示)时系统异常！";

			return null;
		}

		error.code = 0;

		return accountStatistics;
	}

	/**
	 * 根据用户id查询不同类别获得的积分
	 * 
	 * @param userId
	 * @return
	 */
	public static v_user_detail_score queryCreditScore(long userId) {
		v_user_detail_score creditScore = null;
		List<v_user_detail_score> v_user_detail_score_list = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_DETAIL_SCORE);
		sql.append(" and t_users.id = ? group by t_users.id");

		try {
			// findById：如果id不存在，则得到空
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_detail_score.class);
			query.setParameter(1, userId);
			query.setMaxResults(1);
			v_user_detail_score_list = query.getResultList();

			if (v_user_detail_score_list.size() > 0) {
				creditScore = v_user_detail_score_list.get(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询不同类别获得的积分时：" + e.getMessage());

			return null;
		}

		return creditScore;
	}

	/**
	 * 查询积分明细（成功借款）
	 * 
	 * @param id
	 * @param type
	 * @param currPage
	 * @param key
	 * @return
	 */
	public static PageBean<v_user_detail_credit_score_loan> queryCreditDetailLoan(long id, int currPage, int pageSize,
			String key) {
		if (currPage == 0) {
			currPage = 1;
		}

		List<v_user_detail_credit_score_loan> creditScore = new ArrayList<v_user_detail_credit_score_loan>();
		PageBean<v_user_detail_credit_score_loan> page = new PageBean<v_user_detail_credit_score_loan>();
		page.currPage = currPage;
		page.pageSize = pageSize;

		if (pageSize == 0) {
			page.pageSize = Constants.FIVE;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_DETAIL_CREDIT_SCORE_LOAN);

		List<Object> params = new ArrayList<Object>();

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("key", key);

		if (StringUtils.isBlank(key)) {
			sql.append(" and t_users.id = ?");
			params.add(id);
		} else {
			sql.append(" and t_bids.title like ? and t_users.id = ?");
			params.add("%" + key + "%");
			params.add(id);
		}

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_detail_credit_score_loan.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * page.pageSize);
			query.setMaxResults(page.pageSize);
			creditScore = query.getResultList();

			page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询积分明细（成功借款）时：" + e.getMessage());

			return null;
		}

		page.page = creditScore;
		page.conditions = conditionMap;

		return page;
	}

	/**
	 * 根据查询积分明细（审核资料）
	 * 
	 * @param id
	 * @param type
	 * @param currPage
	 * @param key
	 * @return
	 */
	public static PageBean<v_user_detail_credit_score_audit_items> queryCreditDetailAuditItem(long id, int currPage, int pageSize,
			String key, ErrorInfo error) {
		error.clear();

		if (currPage == 0) {
			currPage = 1;
		}

		List<v_user_detail_credit_score_audit_items> creditScore = new ArrayList<v_user_detail_credit_score_audit_items>();
		PageBean<v_user_detail_credit_score_audit_items> page = new PageBean<v_user_detail_credit_score_audit_items>();
		page.currPage = currPage;
		page.pageSize = pageSize;

		if (pageSize == 0) {
			page.pageSize = Constants.FIVE;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_DETAIL_CREDIT_SCORE_AUDIT_ITEMS);

		List<Object> params = new ArrayList<Object>();

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("key", key);

		if (StringUtils.isBlank(key)) {
			sql.append(" and t_users.id = ?");
			params.add(id);
		} else {
			sql.append(" and t_dict_audit_items.name like ? and t_users.id = ?");
			params.add("%" + key + "%");
			params.add(id);
		}

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_detail_credit_score_audit_items.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * page.pageSize);
			query.setMaxResults(page.pageSize);
			creditScore = query.getResultList();

			page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询信用积分明细时：" + e.getMessage());
			error.code = -1;
			error.msg = "信用积分明细查询出现错误";
			return null;
		}

		page.page = creditScore;
		page.conditions = conditionMap;

		error.code = 0;

		return page;
	}

	/**
	 * 根据查询积分明细（成功投标）
	 * 
	 * @param id
	 * @param currPage
	 * @param key
	 * @return
	 */
	public static PageBean<v_user_detail_credit_score_invest> queryCreditDetailInvest(long id, int currPage, int pageSize,
			String key) {
		if (currPage == 0) {
			currPage = 1;
		}

		List<v_user_detail_credit_score_invest> creditScore = new ArrayList<v_user_detail_credit_score_invest>();
		PageBean<v_user_detail_credit_score_invest> page = new PageBean<v_user_detail_credit_score_invest>();
		page.currPage = currPage;
		page.pageSize = pageSize;

		if (pageSize == 0) {
			page.pageSize = Constants.FIVE;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_DETAIL_CREDIT_SCORE_INVEST);

		List<Object> params = new ArrayList<Object>();

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("key", key);

		if (StringUtils.isBlank(key)) {
			sql.append(" and t_users.id = ? ");
			params.add(id);
		} else {
			sql.append(" and t_bids.title like ? and t_users.id = ? ");
			params.add("%" + key + "%");
			params.add(id);
		}
		sql.append(" order by `t_user_details_credit_score`.`time` desc");

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_detail_credit_score_invest.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * page.pageSize);
			query.setMaxResults(page.pageSize);
			creditScore = query.getResultList();

			page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询积分明细（成功投标）时：" + e.getMessage());

			return null;
		}

		page.page = creditScore;
		page.conditions = conditionMap;

		return page;
	}

	/**
	 * 根据查询积分明细（正常还款）
	 * 
	 * @param id
	 * @param currPage
	 * @param key
	 * @return
	 */
	public static PageBean<v_user_detail_credit_score_normal_repayment> queryCreditDetailRepayment(long id, int currPage,
			int pageSize, String key) {
		if (currPage == 0) {
			currPage = 1;
		}

		List<v_user_detail_credit_score_normal_repayment> creditScore = new ArrayList<v_user_detail_credit_score_normal_repayment>();
		PageBean<v_user_detail_credit_score_normal_repayment> page = new PageBean<v_user_detail_credit_score_normal_repayment>();
		page.currPage = currPage;
		page.pageSize = pageSize;

		if (pageSize == 0) {
			page.pageSize = Constants.FIVE;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_DETAIL_CREDIT_SCORE_NORMAL_REPAYMENT);

		List<Object> params = new ArrayList<Object>();

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("key", key);

		if (StringUtils.isBlank(key)) {
			sql.append(" and t_users.id = ?");
			params.add(id);
		} else {
			sql.append(" and t_bids.title like ? and t_users.id = ?");
			params.add("%" + key + "%");
			params.add(id);
		}

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_detail_credit_score_normal_repayment.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * page.pageSize);
			query.setMaxResults(page.pageSize);
			creditScore = query.getResultList();

			page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询积分明细（正常还款）时：" + e.getMessage());

			return null;
		}

		page.page = creditScore;
		page.conditions = conditionMap;

		return page;
	}

	/**
	 * 根据查询积分明细（逾期扣分）
	 * 
	 * @param id
	 * @param currPage
	 * @param key
	 * @return
	 */
	public static PageBean<v_user_detail_credit_score_overdue> queryCreditDetailOverdue(long id, int currPage, int pageSize,
			String key) {
		if (currPage == 0) {
			currPage = 1;
		}

		List<v_user_detail_credit_score_overdue> creditScore = new ArrayList<v_user_detail_credit_score_overdue>();
		PageBean<v_user_detail_credit_score_overdue> page = new PageBean<v_user_detail_credit_score_overdue>();
		page.currPage = currPage;
		page.pageSize = pageSize;

		if (pageSize == 0) {
			page.pageSize = Constants.FIVE;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_DETAIL_CREDIT_SCORE_OVERDUE);

		List<Object> params = new ArrayList<Object>();

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("key", key);

		if (StringUtils.isBlank(key)) {
			sql.append(" and t_users.id = ?");
			params.add(id);

		} else {
			sql.append(" and t_bids.title like ? and t_users.id = ?");
			params.add("%" + key + "%");
			params.add(id);
		}

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_detail_credit_score_overdue.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * page.pageSize);
			query.setMaxResults(page.pageSize);
			creditScore = query.getResultList();

			page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询积分明细（逾期扣分）时：" + e.getMessage());

			return null;
		}

		page.page = creditScore;
		page.conditions = conditionMap;

		return page;
	}

	public static v_user_users queryUserById(long id, ErrorInfo error) {
		error.clear();
		v_user_users user = null;

		try {
			StringBuffer sql = new StringBuffer();
			List<v_user_users> v_user_users_list = null;
			sql.append(SQLTempletes.SELECT);
			sql.append(SQLTempletes.V_USER_USERS);
			sql.append(" and t_users.id = ?");
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_users.class);
			query.setParameter(1, id);
			query.setMaxResults(1);
			v_user_users_list = query.getResultList();
			if (v_user_users_list.size() > 0) {
				user = v_user_users_list.get(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询会员管理详情时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询会员详情失败";

			return null;
		}

		error.code = 0;

		return user;
	}

	/**
	 * 拒收人员名单
	 * 
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static PageBean<t_users> queryRefusedUser(int currPage, int pageSize, String keyword, ErrorInfo error) {
		error.clear();

		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}

		List<t_users> refusedUser = new ArrayList<t_users>();
		String condition = "(is_refused_receive=true)";
		List<Object> params = new ArrayList<Object>();

		if (StringUtils.isNotBlank(keyword)) {
			condition += " and (name like ?)";
			params.add("%" + keyword + "%");
		}

		String sql = "select new t_users(id, name, refused_time, is_refused_receive, refused_reason, is_allow_login) "
				+ "from t_users where " + condition;

		PageBean<t_users> page = new PageBean<t_users>();
		page.currPage = currPage;
		page.pageSize = pageSize;

		try {
			page.totalCount = (int) t_users.count(condition, params.toArray());
			refusedUser = t_users.find(sql, params.toArray()).fetch(currPage, page.pageSize);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询所有的会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询所有的会员时：数据库异常";

			return null;
		}

		Map<String, Object> map = new HashMap<String, Object>();

		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}

		page.conditions = map;
		page.page = refusedUser;

		error.code = 0;

		return page;
	}

	/**
	 * 拒收人员详情
	 * 
	 * @param userId
	 * @param error
	 * @return
	 */
	public static t_users queryRefusedUserDetail(long userId, ErrorInfo error) {
		error.clear();
		t_users user = null;
		String sql = "select new t_users(id, name, refused_time, is_refused_receive, refused_reason, is_allow_login) from t_users where (id = ?)";

		try {
			user = t_users.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询拒收人员详情时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询拒收人员详情时：数据库异常";

			return null;
		}

		error.code = 0;

		return user;
	}

	/**
	 * 通过名字查询用户
	 * 
	 * @param name
	 * @param error
	 * @return
	 */
	public static t_users queryUserByName(String name, ErrorInfo error) {
		error.clear();
		t_users user = null;

		try {
			user = t_users.find("select new t_users(id, name, reality_name, email) from t_users where name = ?", name).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			error.code = -1;
			error.msg = "查询用户失败";

			return null;
		}

		if (user == null) {
			error.code = -2;
			error.msg = "账号为" + name + "的用户不存在";

			return null;
		}

		error.code = 0;
		error.msg = "查询管理员成功";

		return user;
	}

	/**
	 * 查询所有的会员
	 * 
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_user_info> queryUserBySupervisor(String name, String email, String beginTimeStr, String endTimeStr,
			String beginLoginTimeStr, String endLoginTimeStr, String key, String orderTypeStr, String currPageStr,
			String pageSizeStr, ErrorInfo error) {
		error.clear();

		Date beginTime = null;
		Date endTime = null;
		Date beginLoginTime = null;
		Date endLoginTime = null;
		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}
		if (RegexUtils.isDate(beginLoginTimeStr)) {
			beginLoginTime = DateUtil.strDateToStartDate(beginLoginTimeStr);
		}

		if (RegexUtils.isDate(endLoginTimeStr)) {
			endLoginTime = DateUtil.strDateToEndDate(endLoginTimeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (orderType < 0 || orderType > 24) {
			orderType = 0;
		}

		StringBuffer sql = new StringBuffer();
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_INFO_V3);

		List<Object> paramsCount = new ArrayList<Object>();
		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		/* 为sql条件添加参数 */
		for (int i = 1; i <= 4; i++) {
			params.add(Constants.BID_REPAYMENT);
			params.add(Constants.BID_REPAYMENTS);
			params.add(Constants.BID_COMPENSATE_REPAYMENT);
		}

		conditionMap.put("name", name);
		conditionMap.put("email", email);
		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("beginLoginTime", beginLoginTimeStr);
		conditionMap.put("endLoginTime", endLoginTimeStr);
		conditionMap.put("key", key);
		conditionMap.put("orderType", orderType);

		if (StringUtils.isNotBlank(key)) {
			sql.append("and t_users.name like ? ");
			params.add("%" + key + "%");
			paramsCount.add("%" + key + "%");
		}

		if (StringUtils.isNotBlank(name)) {
			sql.append("and (t_users.name like ? or t_users.email like ? or t_users.mobile like ?) ");
			params.add("%" + name + "%");
			params.add("%" + name + "%");
			params.add("%" + name + "%");
			paramsCount.add("%" + name + "%");
			paramsCount.add("%" + name + "%");
			paramsCount.add("%" + name + "%");
		}

		if (StringUtils.isNotBlank(email)) {
			sql.append("and t_users.email like ? ");
			params.add("%" + email + "%");
			paramsCount.add("%" + email + "%");
		}

		if (beginTime != null) {
			sql.append("and t_users.time > ? ");
			params.add(beginTime);
			paramsCount.add(beginTime);
		}

		if (endTime != null) {
			sql.append("and t_users.time < ? ");
			params.add(endTime);
			paramsCount.add(endTime);
		}

		if (beginLoginTime != null) {
			sql.append("and t_users.last_login_time > ? ");
			params.add(beginLoginTime);
			paramsCount.add(beginLoginTime);
		}

		if (endLoginTime != null) {
			sql.append("and t_users.last_login_time < ? ");
			params.add(endLoginTime);
			paramsCount.add(endLoginTime);
		}

		Logger.info("orderType:" + orderType);
		sql.append("order by " + Constants.ORDER_TYPE_V2[orderType]);

		List<v_user_info> users = new ArrayList<v_user_info>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Logger.info("sql:" + sql.toString());
			Query query = em.createNativeQuery(sql.toString(), v_user_info.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), paramsCount);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询所有的会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询全部会员列表时出现异常！";

			return null;
		}

		PageBean<v_user_info> page = new PageBean<v_user_info>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 查询所有的会员
	 * 
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_user_info> queryUserBySupervisorV2(String name, String email, String beginTimeStr, String endTimeStr,
			String beginLoginTimeStr, String endLoginTimeStr, String key, String orderTypeStr, String currPageStr,
			String pageSizeStr, ErrorInfo error, String isBank, String isRealname, String recommend_user_name, String risk_result,
			String is_first_invest, String user_type, String finance_type) {
		error.clear();

		Date beginTime = null;
		Date endTime = null;
		Date beginLoginTime = null;
		Date endLoginTime = null;
		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}
		if (RegexUtils.isDate(beginLoginTimeStr)) {
			beginLoginTime = DateUtil.strDateToStartDate(beginLoginTimeStr);
		}

		if (RegexUtils.isDate(endLoginTimeStr)) {
			endLoginTime = DateUtil.strDateToEndDate(endLoginTimeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (orderType < 0 || orderType > 22) {
			orderType = 0;
		}

		StringBuffer sql = new StringBuffer();
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_INFO_V3);
		Logger.info("sql-1:" + sql.toString());
		List<Object> paramsCount = new ArrayList<Object>();
		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		/* 为sql条件添加参数 */
		for (int i = 1; i <= 4; i++) {
			params.add(Constants.BID_REPAYMENT);
			params.add(Constants.BID_REPAYMENTS);
			params.add(Constants.BID_COMPENSATE_REPAYMENT);
		}

		conditionMap.put("name", name);
		conditionMap.put("email", email);
		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("beginLoginTime", beginLoginTimeStr);
		conditionMap.put("endLoginTime", endLoginTimeStr);
		conditionMap.put("key", key);
		conditionMap.put("orderType", orderType);
		conditionMap.put("isBank", isBank);
		conditionMap.put("isRealname", isRealname);
		conditionMap.put("recommend_user_name", recommend_user_name);
		conditionMap.put("risk_result", risk_result);
		conditionMap.put("is_first_invest", is_first_invest);
		conditionMap.put("user_type", user_type);
		conditionMap.put("finance_type", finance_type);

		if (StringUtils.isNotBlank(key)) {
			sql.append("and t_users.name like ? ");
			params.add("%" + key + "%");
			paramsCount.add("%" + key + "%");
		}

		if (StringUtils.isNotBlank(name)) {
			sql.append("and (t_users.name like ? or t_users.email like ? or t_users.mobile like ?) ");
			params.add("%" + name + "%");
			params.add("%" + name + "%");
			params.add("%" + name + "%");
			paramsCount.add("%" + name + "%");
			paramsCount.add("%" + name + "%");
			paramsCount.add("%" + name + "%");
		}

		if (StringUtils.isNotBlank(recommend_user_name)) {
			sql.append(" and instr(recommend.name,?) > 0 ");
			params.add(recommend_user_name);
			paramsCount.add(recommend_user_name);
		}

		if (StringUtils.isNotBlank(email)) {
			sql.append("and t_users.email like ? ");
			params.add("%" + email + "%");
			paramsCount.add("%" + email + "%");
		}

		if (beginTime != null) {
			sql.append("and t_users.time > ? ");
			params.add(beginTime);
			paramsCount.add(beginTime);
		}

		if (endTime != null) {
			sql.append("and t_users.time < ? ");
			params.add(endTime);
			paramsCount.add(endTime);
		}

		if (beginLoginTime != null) {
			sql.append("and t_users.last_login_time > ? ");
			params.add(beginLoginTime);
			paramsCount.add(beginLoginTime);
		}

		if (endLoginTime != null) {
			sql.append("and t_users.last_login_time < ? ");
			params.add(endLoginTime);
			paramsCount.add(endLoginTime);
		}
		if (null != user_type && Integer.parseInt(user_type) != -1) {
			sql.append("and t_users.user_type = ? ");
			params.add(user_type);
			paramsCount.add(user_type);
		}
		if (null != finance_type && Integer.parseInt(finance_type) != -1) {
			sql.append("and t_users.finance_type = ? ");
			params.add(finance_type);
			paramsCount.add(finance_type);
		}

		if ("1".equals(isBank)) {
			sql.append(
					"and (SELECT IFNULL(COUNT(*), 0) FROM `t_user_bank_accounts` WHERE `t_users`.`id` = `t_user_bank_accounts`.`user_id`) > 0 ");
		} else if ("0".equals(isBank)) {
			sql.append(
					"and (SELECT IFNULL(COUNT(*), 0) FROM `t_user_bank_accounts` WHERE `t_users`.`id` = `t_user_bank_accounts`.`user_id`) = 0 ");
		}

		if ("1".equals(is_first_invest)) {
			sql.append("and (SELECT IFNULL(count(1),0) FROM t_invests WHERE t_invests.user_id = t_users.id) > 0 ");
		} else if ("0".equals(is_first_invest)) {
			sql.append("and (SELECT IFNULL(count(1),0) FROM t_invests WHERE t_invests.user_id = t_users.id) = 0 ");
		}

		if ("1".equals(isRealname)) {
			sql.append("and t_users.reality_name is not null ");
		} else if ("0".equals(isRealname)) {
			sql.append("and t_users.reality_name is null ");
		}

		if (StringUtils.isNotBlank(risk_result)) {
			sql.append("and t_users.risk_result = ? ");
			params.add(risk_result);
			paramsCount.add(risk_result);
		}
		Logger.info("orderType:" + orderType);
		sql.append("order by " + Constants.ORDER_TYPE_V2[orderType]);

		List<v_user_info> users = new ArrayList<v_user_info>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Logger.info("sql:" + sql.toString());
			Query query = em.createNativeQuery(sql.toString(), v_user_info.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), paramsCount);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询所有的会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询全部会员列表时出现异常！";

			return null;
		}

		PageBean<v_user_info> page = new PageBean<v_user_info>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 查询借款会员
	 * 
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_user_loan_info> queryLoanUserBySupervisor(String statusTypeStr, String name, String email,
			String beginTimeStr, String endTimeStr, String key, String orderTypeStr, String currPageStr, String pageSizeStr,
			ErrorInfo error) {
		error.clear();

		int statusType = 0;
		Date beginTime = null;
		Date endTime = null;
		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (NumberUtil.isNumericInt(statusTypeStr)) {
			statusType = Integer.parseInt(statusTypeStr);
		}

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (statusType < 0 || statusType > 2) {
			statusType = 0;
		}

		if (orderType < 0 || orderType > 8) {
			orderType = 0;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_LOAN_INFO);

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("statusType", statusType);
		conditionMap.put("name", name);
		conditionMap.put("email", email);
		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("key", key);
		conditionMap.put("orderType", orderType);

		if (statusType != 0) {
			sql.append(Constants.STATUS_TYPE[statusType]);
		}

		if (StringUtils.isNotBlank(name)) {
			sql.append("and t_users.name like ? ");
			params.add("%" + name + "%");
		}

		if (StringUtils.isNotBlank(email)) {
			sql.append("and t_users.email like ? ");
			params.add("%" + email + "%");
		}

		if (beginTime != null) {
			sql.append("and t_users.time > ? ");
			params.add(beginTime);
		}

		if (endTime != null) {
			sql.append("and t_users.time < ? ");
			params.add(endTime);
		}

		if (orderType != 0) {
			sql.append("order by " + Constants.LOAN_USER_ORDER[orderType]);
		}

		List<v_user_loan_info> users = new ArrayList<v_user_loan_info>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_loan_info.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询所有的会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询借款会员列表时出现异常！";

			return null;
		}

		PageBean<v_user_loan_info> page = new PageBean<v_user_loan_info>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 查询理财会员
	 * 
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_user_invest_info> queryInvestUserBySupervisor(String name, String email, String beginTimeStr,
			String endTimeStr, String key, String orderTypeStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		Date beginTime = null;
		Date endTime = null;
		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (orderType < 0 || orderType > 8) {
			orderType = 0;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_INVEST_INFO);

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("name", name);
		conditionMap.put("email", email);
		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("key", key);
		conditionMap.put("orderType", orderType);

		if (StringUtils.isNotBlank(name)) {
			sql.append("and t_users.name like ? ");
			params.add("%" + name + "%");
		}

		if (StringUtils.isNotBlank(email)) {
			sql.append("and t_users.email like ? ");
			params.add("%" + email + "%");
		}

		if (beginTime != null) {
			sql.append("and t_users.time > ? ");
			params.add(beginTime);
		}

		if (endTime != null) {
			sql.append("and t_users.time < ? ");
			params.add(endTime);
		}

		if (orderType != 0) {
			sql.append("order by " + Constants.INVEST_USER_ORDER[orderType]);
		}

		List<v_user_invest_info> users = new ArrayList<v_user_invest_info>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_invest_info.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询所有的会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询理财会员列表时出现异常！";

			return null;
		}

		PageBean<v_user_invest_info> page = new PageBean<v_user_invest_info>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 查询理财会员
	 * 
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_user_invest_info> queryInvestUserBySupervisor(int noPage, String name, String email,
			String beginTimeStr, String endTimeStr, String key, String orderTypeStr, String currPageStr, String pageSizeStr,
			ErrorInfo error) {
		error.clear();

		Date beginTime = null;
		Date endTime = null;
		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (orderType < 0 || orderType > 8) {
			orderType = 0;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		String sss = "`t_users`.`id` AS `id`,t_credit_levels.image_filename AS credit_level_image_filename,t_credit_levels.order_sort AS order_sort,`t_users`.`name` AS `name`,`t_users`.`time` AS `register_time`,`t_users`.`credit_score` AS `credit_score`,`t_users`.`email` AS `email`,`t_users`.`mobile` AS `mobile`,`t_users`.`is_allow_login` AS `is_allow_login`,`t_users`.`master_identity` AS `master_identity`,`t_users`.is_migration,((`t_users`.`balance` + `t_users`.`balance2`)) AS `user_amount`,(select ifnull(sum(`t_user_details`.`amount`),0) AS `recharge_amount` from `t_user_details` where ((`t_user_details`.`user_id` = `t_users`.`id`) and (`t_user_details`.`operation` in (1,2,3)))) AS `recharge_amount`,(select count(`t_invests`.`bid_id`) AS `invest_count` from `t_invests`,t_bids where ((`t_invests`.`user_id` = `t_users`.`id` and t_invests.bid_id = t_bids.id) and (t_bids.`status` IN (4, 5, 14)))) AS `invest_count`,(select ifnull(sum(`t_invests`.`amount`),0) AS `invest_amount` from `t_invests`,t_bids where (`t_invests`.`user_id` = `t_users`.`id` AND t_invests.bid_id = t_bids.id AND t_bids.`status` IN (4, 5, 14))) AS `invest_amount`,(select count(0) AS `invest_receive_count` from `t_bids` `bid` where ((`bid`.`status` = 4) and `bid`.`id` in (select `inv`.`bid_id` from `t_invests` `inv` where ((`inv`.`user_id` = `t_users`.`id`) and (`inv`.`transfer_status` <> -(1)))))) AS `invest_receive_count`,(select ifnull(sum(((`t_bill_invests`.`receive_corpus` + `t_bill_invests`.`receive_interest`) + `t_bill_invests`.`overdue_fine`)),0) AS `receive_amount` from `t_bill_invests` where ((`t_bill_invests`.`user_id` = `t_users`.`id`) and (`t_bill_invests`.`status` in (-(1) ,-(2) ,-(5) ,-(6))))) AS `receive_amount`,(select count(DISTINCT t_invests.bid_id) AS `transfer_count` from `t_invests`,t_invest_transfers where ((`t_invests`.`user_id` = `t_users`.`id` AND t_invest_transfers.invest_id = t_invests.id) and (`t_invests`.`transfer_status` = 1 AND t_invest_transfers.`status` IN (1, 2, 4)))) AS `transfer_count` from `t_users` LEFT JOIN t_credit_levels ON t_users.credit_level_id = t_credit_levels.id where `t_users`.`master_identity` in (2,3) and `t_users`.finance_type=1 ";
		sql.append(sss);

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("name", name);
		conditionMap.put("email", email);
		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("key", key);
		conditionMap.put("orderType", orderType);

		if (StringUtils.isNotBlank(name)) {
			sql.append("and (t_users.name like ? or t_users.reality_name like ? or t_users.mobile like ? ) ");
			params.add("%" + name + "%");
			params.add("%" + name + "%");
			params.add("%" + name + "%");
		}

		if (StringUtils.isNotBlank(email)) {
			sql.append("and t_users.email like ? ");
			params.add("%" + email + "%");
		}

		if (beginTime != null) {
			sql.append("and t_users.time > ? ");
			params.add(beginTime);
		}

		if (endTime != null) {
			sql.append("and t_users.time < ? ");
			params.add(endTime);
		}

		if (orderType != 0) {
			sql.append("order by " + Constants.INVEST_USER_ORDER[orderType]);
		}

		List<v_user_invest_info> users = new ArrayList<v_user_invest_info>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_invest_info.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
			if (noPage == Constants.NO_PAGE) {
				users = query.getResultList();
			} else {
				query.setFirstResult((currPage - 1) * pageSize);
				query.setMaxResults(pageSize);
				users = query.getResultList();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询所有的会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询理财会员列表时出现异常！";

			return null;
		}

		PageBean<v_user_invest_info> page = new PageBean<v_user_invest_info>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 查询复合会员
	 * 
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_user_complex_info> queryComplexUserBySupervisor(String name, String email, String beginTimeStr,
			String endTimeStr, String key, String orderTypeStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		Date beginTime = null;
		Date endTime = null;
		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (orderType < 0 || orderType > 12) {
			orderType = 0;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_COMPLEX_INFO);

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("name", name);
		conditionMap.put("email", email);
		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("key", key);
		conditionMap.put("orderType", orderType);

		if (StringUtils.isNotBlank(name)) {
			sql.append(" and t_users.name like ? ");
			params.add("%" + name + "%");
		}

		if (StringUtils.isNotBlank(email)) {
			sql.append(" and t_users.email like ? ");
			params.add("%" + email + "%");
		}

		if (beginTime != null) {
			sql.append(" and t_users.time > ? ");
			params.add(beginTime);
		}

		if (endTime != null) {
			sql.append(" and t_users.time < ? ");
			params.add(endTime);
		}

		if (orderType != 0) {
			sql.append("order by " + Constants.COMPLEX_USER_ORDER[orderType]);
		}

		List<v_user_complex_info> users = new ArrayList<v_user_complex_info>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_complex_info.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询复合会员列表时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询复合会员列表时出现异常！";

			return null;
		}

		PageBean<v_user_complex_info> page = new PageBean<v_user_complex_info>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 查询vip会员
	 * 
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_user_vip_info> queryVipUserBySupervisor(String name, String email, String beginTimeStr,
			String endTimeStr, String key, String orderTypeStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();
		Date beginTime = null;
		Date endTime = null;
		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (orderType < 0 || orderType > 12) {
			orderType = 0;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_VIP_INFO);

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("name", name);
		conditionMap.put("email", email);
		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("key", key);
		conditionMap.put("orderType", orderType);

		if (StringUtils.isNotBlank(name)) {
			sql.append("and t_users.name like ? ");
			params.add("%" + name + "%");
		}

		if (StringUtils.isNotBlank(email)) {
			sql.append("and t_users.email like ? ");
			params.add("%" + email + "%");
		}

		if (beginTime != null) {
			sql.append("and t_users.time > ? ");
			params.add(beginTime);
		}

		if (endTime != null) {
			sql.append("and t_users.time < ? ");
			params.add(endTime);
		}

		if (orderType != 0) {
			sql.append("order by " + SQLTempletes.VIP_USER_ORDER[orderType]);
		}

		List<v_user_vip_info> users = new ArrayList<v_user_vip_info>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_vip_info.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询vip会员列表时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询vip会员列表时出现异常！";

			return null;
		}

		PageBean<v_user_vip_info> page = new PageBean<v_user_vip_info>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 查询cps会员
	 * 
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_user_cps_info> queryCpsUserBySupervisor(String name, String email, String beginTimeStr,
			String endTimeStr, String key, String orderTypeStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		Date beginTime = null;
		Date endTime = null;
		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (orderType < 0 || orderType > 8) {
			orderType = 0;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_CPS_INFO);

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("name", name);
		conditionMap.put("email", email);
		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("key", key);
		conditionMap.put("orderType", orderType);

		if (StringUtils.isNotBlank(name)) {
			sql.append(" and name like ? ");
			params.add("%" + name + "%");
		}

		if (StringUtils.isNotBlank(email)) {
			sql.append(" and email like ? ");
			params.add("%" + email + "%");
		}

		if (beginTime != null) {
			sql.append(" and t_users.time > ? ");
			params.add(beginTime);
		}

		if (endTime != null) {
			sql.append(" and t_users.time < ? ");
			params.add(endTime);
		}

		if (orderType != 0) {
			sql.append("order by " + Constants.CPS_USER_ORDER[orderType]);
		}

		List<v_user_cps_info> users = new ArrayList<v_user_cps_info>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_cps_info.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询cps会员列表时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询cps会员列表时出现异常！";

			return null;
		}

		PageBean<v_user_cps_info> page = new PageBean<v_user_cps_info>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 查询未激活会员
	 * 
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_user_unverified_info> queryUnverifiedUserBySupervisor(String name, String email, String beginTimeStr,
			String endTimeStr, String key, String orderTypeStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		Date beginTime = null;
		Date endTime = null;
		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (orderType < 0 || orderType > 2) {
			orderType = 0;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_UNVERIFIED_INFO);

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("name", name);
		conditionMap.put("email", email);
		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("key", key);
		conditionMap.put("orderType", orderType);

		if (StringUtils.isNotBlank(name)) {
			sql.append("and t_users.name like ? ");
			params.add("%" + name + "%");
		}

		if (StringUtils.isNotBlank(email)) {
			sql.append("and t_users.email like ? ");
			params.add("%" + email + "%");
		}

		if (beginTime != null) {
			sql.append("and t_users.time > ? ");
			params.add(beginTime);
		}

		if (endTime != null) {
			sql.append("and t_users.time < ? ");
			params.add(endTime);
		}

		sql.append("order by id desc ");
		List<v_user_unverified_info> users = new ArrayList<v_user_unverified_info>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_unverified_info.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询未激活会员列表时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询未激活会员列表时出现异常！";

			return null;
		}

		PageBean<v_user_unverified_info> page = new PageBean<v_user_unverified_info>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 查询锁定会员
	 * 
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_user_locked_info> queryLockedUserBySupervisor(String name, String email, String beginTimeStr,
			String endTimeStr, String key, String orderTypeStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		Date beginTime = null;
		Date endTime = null;
		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (orderType < 0 || orderType > 6) {
			orderType = 0;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_LOCKED_INFO);

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("name", name);
		conditionMap.put("email", email);
		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("key", key);
		conditionMap.put("orderType", orderType);

		if (StringUtils.isNotBlank(name)) {
			sql.append("and t_users.name like ? ");
			params.add("%" + name + "%");
		}

		if (StringUtils.isNotBlank(email)) {
			sql.append("and t_users.email like ? ");
			params.add("%" + email + "%");
		}

		if (beginTime != null) {
			sql.append("and t_users.lock_time >= ? ");
			params.add(beginTime);
		}

		if (endTime != null) {
			sql.append("and t_users.lock_time <= ? ");
			params.add(endTime);
		}

		if (orderType != 0) {
			sql.append("order by " + SQLTempletes.LOCKED_USER_ORDER[orderType]);
		}

		List<v_user_locked_info> users = new ArrayList<v_user_locked_info>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_locked_info.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询已锁定会员会员列表时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询已锁定会员列表时出现异常！";

			return null;
		}

		PageBean<v_user_locked_info> page = new PageBean<v_user_locked_info>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 查询被举报会员列表
	 * 
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_user_reported_info> queryReportedUserBySupervisor(String name, String email, String beginTimeStr,
			String endTimeStr, String key, String orderTypeStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		Date beginTime = null;
		Date endTime = null;
		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (orderType < 0 || orderType > 4) {
			orderType = 0;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_REPORTED_INFO);

		StringBuffer sqlCount = new StringBuffer("");
		sqlCount.append(SQLTempletes.V_USER_REPORTED_INFO_COUNT);

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("name", name);
		conditionMap.put("email", email);
		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("key", key);
		conditionMap.put("orderType", orderType);

		if (StringUtils.isNotBlank(name)) {
			sql.append("and t_users.name like ? ");
			sqlCount.append("and t_users.name like ? ");
			params.add("%" + name + "%");
		}

		if (StringUtils.isNotBlank(email)) {
			sql.append("and t_users.email like ? ");
			sqlCount.append("and t_users.email like ? ");
			params.add("%" + email + "%");
		}

		if (beginTime != null) {
			sql.append("and t_users.time > ? ");
			sqlCount.append("and t_users.time > ? ");

			params.add(beginTime);
		}

		if (endTime != null) {
			sql.append("and t_users.time < ? ");
			sqlCount.append("and t_users.time < ? ");
			params.add(endTime);
		}

		if (orderType != 0) {
			sql.append("order by " + Constants.REPORTED_USER_ORDER[orderType]);
		}

		List<v_user_reported_info> users = new ArrayList<v_user_reported_info>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_reported_info.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			Query queryCount = em.createNativeQuery(sqlCount.toString());
			for (int n = 1; n <= params.size(); n++) {
				queryCount.setParameter(n, params.get(n - 1));
			}
			count = Convert.strToInt(queryCount.getResultList().get(0) + "", 0);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询被举报会员列表时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询被举报会员列表时出现异常！";

			return null;
		}

		PageBean<v_user_reported_info> page = new PageBean<v_user_reported_info>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 后台--被举报会员列表--举报会员列表
	 * 
	 * @param supervisorId
	 * @param reportedUserId
	 * @param currPage
	 * @param pageSize
	 */
	public static PageBean<v_user_report_list> queryReportUserBySupervisor(long supervisorId, long reportedUserId,
			String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		int currPage = Constants.ONE;
		int pageSize = Constants.TWO;

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}
		List<v_user_report_list> reportUsers = new ArrayList<v_user_report_list>();

		PageBean<v_user_report_list> page = new PageBean<v_user_report_list>();
		page.currPage = currPage;
		page.pageSize = pageSize;

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_REPORT_LIST);
		sql.append(" and t_user_report_users.reported_user_id = ?");
		List<Object> params = new ArrayList<Object>();
		params.add(reportedUserId);

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_report_list.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * page.pageSize);
			query.setMaxResults(page.pageSize);
			reportUsers = query.getResultList();

			page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询举报会员列表时：" + e.getMessage());
			error.code = -2;
			error.msg = "查询举报会员列表时出现异常！";

			return null;
		}

		page.page = reportUsers;

		error.code = 0;

		return page;
	}

	/**
	 * 举报某个用户记录列表
	 * 
	 * @param pageBean 分页对象
	 * @param userId   用户ID
	 * @param error    信息值
	 * @return List<t_user_report_users>
	 */
	public static List<t_user_report_users> queryBidRecordByUser(PageBean<t_user_report_users> pageBean, long userId,
			ErrorInfo error) {
		error.clear();

		int count = -1;
		String condition = "from t_user_report_users uru,t_users u where uru.user_id = u.id and uru.reported_user_id = ?";

		try {
			count = (int) t_user_report_users.count(condition, userId);
		} catch (Exception e) {
			Logger.error("用户->举报某个用户记录列表,查询总记录数:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "加载举报记录列表失败!";

			return null;
		}

		if (count < 1)
			return new ArrayList<t_user_report_users>();

		pageBean.totalCount = count;

		String hql = "select new t_user_report_users(u.name, uru.reason, uru.time, uru.situation) " + condition;
		List<t_user_report_users> reportUsers = null;

		try {
			reportUsers = t_user_report_users.find(hql, userId).fetch(pageBean.currPage, pageBean.pageSize);
		} catch (Exception e) {
			Logger.error("用户->举报某个用户记录列表,查询总记录数:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "加载举报记录列表失败!";

			return null;
		}

		error.code = 0;

		return reportUsers;
	}

	/**
	 * 查询黑名单会员列表
	 * 
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_user_blacklist_info> queryBlacklistUserBySupervisor(String name, String email, String beginTimeStr,
			String endTimeStr, String key, String orderTypeStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		Date beginTime = null;
		Date endTime = null;
		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (orderType < 0 || orderType > 6) {
			orderType = 0;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_BLACKLIST_INFO);

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("name", name);
		conditionMap.put("email", email);
		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("key", key);
		conditionMap.put("orderType", orderType);

		if (StringUtils.isNotBlank(name)) {
			sql.append(" and t_users.name like ? ");
			params.add("%" + name + "%");
		}

		if (StringUtils.isNotBlank(email)) {
			sql.append(" and t_users.email like ? ");
			params.add("%" + email + "%");
		}

		if (beginTime != null) {
			sql.append(" and t_users.joined_time > ? ");
			params.add(beginTime);
		}

		if (endTime != null) {
			sql.append(" and t_users.joined_time < ? ");
			params.add(endTime);
		}

		if (orderType != 0) {
			sql.append(" order by " + Constants.BLACK_USER_ORDER[orderType]);
		}

		List<v_user_blacklist_info> users = new ArrayList<v_user_blacklist_info>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_blacklist_info.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询黑名单会员列表时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询黑名单会员列表时出现异常！";

			return null;
		}

		PageBean<v_user_blacklist_info> page = new PageBean<v_user_blacklist_info>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 查询不同类别会员的个数(用于后台站内信群发)
	 * 
	 * @return
	 */
	public static Map<String, String> queryUserType(ErrorInfo error) {

		String sql = "select count(*) as all_user_count,count( ( case when (t_users.master_identity = 1)"
				+ " then t_users.id end)) as loan_user_count, count( ( case when (t_users.master_identity = 2)"
				+ " then t_users.id end)) as invest_user_count, count( ( case when (t_users.master_identity = 3)"
				+ " then t_users.id end)) as complex_user_count, count( ( case when (t_users.is_email_verified = 0)"
				+ " then t_users.id end)) as unverified_user_count, count( ( case when (t_users.is_blacklist = 1)"
				+ " then t_users.id end)) as black_user_count, count( ( case when ((now() - INTERVAL 7 DAY)< t_users.time)"
				+ " then t_users.id end)) as new_user_count from t_users";

		Object[] obj = null;
		try {
			obj = (Object[]) JPA.em().createNativeQuery(sql).getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询资金管理—>账户信息时：" + e.getMessage());

			error.code = -1;
			error.msg = "查询资金管理—>账户信息时出现异常";

			return null;
		}

		if (obj == null) {
			error.code = -1;
			error.msg = "查询资金管理—>账户信息时出现异常";

			return null;
		}

		Map<String, String> userMap = new HashMap<String, String>();
		userMap.put("all_user_count", obj[0].toString());
		userMap.put("loan_user_count", obj[1].toString());
		userMap.put("invest_user_count", obj[2].toString());
		userMap.put("complex_user_count", obj[3].toString());
		userMap.put("unverified_user_count", obj[4].toString());
		userMap.put("black_user_count", obj[5].toString());
		userMap.put("new_user_count", obj[6].toString());

		error.code = 0;

		return userMap;
	}

	/**
	 * 用于 资金管理—>账户信息
	 * 
	 * @param userId
	 * @return
	 */
	public static List<v_user_details> queryUserDetail(long userId, ErrorInfo error) {

		List<v_user_details> userDetails = new ArrayList<v_user_details>();
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_DETAILS);
		sql.append(" and t_users.id = ? ");
		sql.append(SQLTempletes.V_USER_DETAILS_GROUP_ORDER);

		List<Object> params = new ArrayList<Object>();
		params.add(userId);

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_details.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult(0);
			query.setMaxResults(4);
			userDetails = query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询资金管理—>账户信息时：" + e.getMessage());

			error.code = -1;
			error.msg = "查询资金管理—>账户信息时出现异常";

			return null;
		}

		error.code = 0;

		return userDetails;
	}

	/**
	 * 我的会员账单--借款会员管理
	 * 
	 * @param userId
	 * @return
	 */
	public static PageBean<v_user_loan_info_bill> queryUserInfoBill(long supervisor, String typeStr, String startTimeStr,
			String endTimeStr, String name, String orderTypeStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		int type = 0;
		int order = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.TEN;

		String[] typeCondition = { " ", " and overdue_bill_count <= 0 and bad_bid_count<= 0 ",
				" and overdue_bill_count > 0 and bad_bid_count<= 0 ", " and overdue_bill_count <= 0 and bad_bid_count > 0 " };
		String[] orderCondition = { " ", " order by bid_count asc ", " order by bid_count  desc ", " order by invest_count asc ",
				" order by invest_count desc ", " order by overdue_bill_count asc ", " order by overdue_bill_count  desc ",
				" order by bad_bid_count asc ", " order by bad_bid_count desc " };

		StringBuffer sql = new StringBuffer("");
		StringBuffer sqlCount = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(
				" id,supervisor_id,type,name,register_time,user_amount,last_login_time,credit_level_image_filename,bid_count,bid_amount,invest_count,invest_amount,bid_loaning_count,bid_repaymenting_count,overdue_bill_count,bad_bid_count from (");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_LOAN_INFO_BILL);
		sql.append(") t1 where supervisor_id = ? ");

		List<Object> params = new ArrayList<Object>();
		params.add(supervisor);

		PageBean<v_user_loan_info_bill> page = new PageBean<v_user_loan_info_bill>();
		List<v_user_loan_info_bill> userBills = new ArrayList<v_user_loan_info_bill>();

		int count = 0;

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (NumberUtil.isNumericInt(typeStr)) {
			type = Integer.parseInt(typeStr);
			sql.append(typeCondition[type]);
		}

		if (StringUtils.isNotBlank(startTimeStr) && StringUtils.isNotBlank(endTimeStr)) {
			Date start = DateUtil.strDateToStartDate(startTimeStr);
			Date end = DateUtil.strDateToEndDate(endTimeStr);
			sql.append(" and  register_time >= ? and register_time <= ? ");
			params.add(start);
			params.add(end);
		}

		if (StringUtils.isNotBlank(name)) {
			sql.append(" and name like ?");
			params.add("%" + name + "%");
		}

		sqlCount.append(sql.toString());
		// 去掉id重复的行
		sql.append(" GROUP BY id");

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			order = Integer.parseInt(orderTypeStr);
			sql.append(orderCondition[order]);
		}

		page.pageSize = pageSize;
		page.currPage = currPage;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_loan_info_bill.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			userBills = query.getResultList();

			count = QueryUtil.getQueryCount1ByCondition(em, sqlCount.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询借款会员管理时：" + e.getMessage());
			error.code = -3;
			error.msg = "";

			return page;
		}

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keywords", name);
		conditionMap.put("type", type);
		conditionMap.put("startDate", startTimeStr);
		conditionMap.put("endDate", endTimeStr);
		conditionMap.put("order", order);

		page.page = userBills;
		page.totalCount = count;
		page.conditions = conditionMap;

		error.code = 0;

		return page;
	}

	/**
	 * 我的会员账单--坏账会员管理
	 * 
	 * @param userId
	 * @return
	 */
	public static PageBean<v_user_loan_info_bad> queryUserInfoBad(long supervisor, String typeStr, String startTimeStr,
			String endTimeStr, String name, String orderTypeStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		int type = 0;
		int order = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.TEN;

		String[] typeCondition = { " ", " and overdue_bill_count <= 0 and bad_bid_count<= 0 ",
				" and overdue_bill_count > 0 and bad_bid_count<= 0 ", " and overdue_bill_count <= 0 and bad_bid_count > 0 " };
		String[] orderCondition = { " ", " order by bid_count asc ", " order by bid_count  desc ", " order by invest_count asc ",
				" order by invest_count desc ", " order by overdue_bill_count asc ", " order by overdue_bill_count  desc ",
				" order by bad_bid_count asc ", " order by bad_bid_count desc " };

		StringBuffer sql = new StringBuffer("");
		StringBuffer sqlCount = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(
				" id,supervisor_id,type,name,register_time,user_amount,last_login_time,credit_level_image_filename,bid_count,bid_amount,invest_count,invest_amount,bid_loaning_count,bid_repaymenting_count,overdue_bill_count,bad_bid_count,bad_bid_amount from (");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_LOAN_INFO_BAD);
		sql.append(" ) t1 where  supervisor_id = ? ");

		List<Object> params = new ArrayList<Object>();
		params.add(supervisor);

		PageBean<v_user_loan_info_bad> page = new PageBean<v_user_loan_info_bad>();
		List<v_user_loan_info_bad> userBills = new ArrayList<v_user_loan_info_bad>();

		int count = 0;

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (NumberUtil.isNumericInt(typeStr)) {
			type = Integer.parseInt(typeStr);
			sql.append(typeCondition[type]);
		}

		if (StringUtils.isNotBlank(startTimeStr) && StringUtils.isNotBlank(endTimeStr)) {
			Date start = DateUtil.strDateToStartDate(startTimeStr);
			Date end = DateUtil.strDateToEndDate(endTimeStr);
			sql.append(" and  register_time >= ? and register_time <= ? ");
			params.add(start);
			params.add(end);
		}

		if (StringUtils.isNotBlank(name)) {
			sql.append(" and name like ?");
			params.add("%" + name + "%");
		}

		sqlCount.append(sql.toString());
		// 去掉id重复的行
		sql.append(" GROUP BY id");

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			order = Integer.parseInt(orderTypeStr);
			sql.append(orderCondition[order]);
		}

		page.pageSize = pageSize;
		page.currPage = currPage;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_loan_info_bad.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			userBills = query.getResultList();

			count = QueryUtil.getQueryCount1ByCondition(em, sqlCount.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询坏账会员管理时：" + e.getMessage());
			error.code = -3;
			error.msg = "";

			return page;
		}

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keywords", name);
		conditionMap.put("type", type);
		conditionMap.put("startDate", startTimeStr);
		conditionMap.put("endDate", endTimeStr);
		conditionMap.put("order", order);

		page.page = userBills;
		page.totalCount = count;
		page.conditions = conditionMap;

		error.code = 0;

		return page;
	}

	/**
	 * 部门账单管理--借款会员管理
	 * 
	 * @param userId
	 * @return
	 */
	public static PageBean<v_user_loan_info_bill_d> queryUserInfoBillD(int noPage, String typeStr, String startDateStr,
			String endDateStr, String name, String orderTypeStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		int type = 0;
		int order = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.TEN;

		String[] typeCondition = { " ", " and overdue_bill_count <= 0 and bad_bid_count<= 0 ",
				" and overdue_bill_count > 0 and bad_bid_count<= 0 ", " and overdue_bill_count <= 0 and bad_bid_count > 0 " };
		String[] orderCondition = { " ", " order by bid_count asc ", " order by bid_count  desc ", " order by invest_count asc ",
				" order by invest_count desc ", " order by overdue_bill_count asc ", " order by overdue_bill_count  desc ",
				" order by bad_bid_count asc ", " order by bad_bid_count desc " };

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(
				" id,supervisor_id,supervisor_name,name,register_time,user_amount,last_login_time,credit_level_image_filename,bid_count,bid_amount,invest_count,invest_amount,bid_loaning_count,bid_repaymenting_count,overdue_bill_count,bad_bid_count from (");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_LOAN_INFO_BILL_D);
		sql.append(" ) t1 where  1=1 ");

		List<Object> params = new ArrayList<Object>();

		PageBean<v_user_loan_info_bill_d> page = new PageBean<v_user_loan_info_bill_d>();
		List<v_user_loan_info_bill_d> userBills = new ArrayList<v_user_loan_info_bill_d>();

		int count = 0;

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (NumberUtil.isNumericInt(typeStr)) {
			type = Integer.parseInt(typeStr);
			sql.append(typeCondition[type]);
		}

		if (StringUtils.isNotBlank(startDateStr) && StringUtils.isNotBlank(endDateStr)) {
			Date start = DateUtil.strToYYMMDDDate(startDateStr);
			Date end = DateUtil.strToYYMMDDDate(endDateStr);
			sql.append(" and  register_time >= ? and register_time <= ? ");
			params.add(start);
			params.add(end);
		}

		if (StringUtils.isNotBlank(name)) {
			sql.append(" and name like ?");
			params.add("%" + name + "%");
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			order = Integer.parseInt(orderTypeStr);
			sql.append(orderCondition[order]);
		}

		page.pageSize = pageSize;
		page.currPage = currPage;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_loan_info_bill_d.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}

			if (noPage != Constants.NO_PAGE) {
				query.setFirstResult((currPage - 1) * pageSize);
				query.setMaxResults(pageSize);
			}

			userBills = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询本月到期账单情况时：" + e.getMessage());
			error.code = -3;
			error.msg = "";

			return page;
		}

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keywords", name);
		conditionMap.put("type", type);
		conditionMap.put("startDate", startDateStr);
		conditionMap.put("endDate", endDateStr);
		conditionMap.put("order", order);

		page.page = userBills;
		page.totalCount = count;
		page.conditions = conditionMap;

		error.code = 0;

		return page;
	}

	/**
	 * 部门账单管理--借款会员管理
	 * 
	 * @param userId
	 * @return
	 */
	public static PageBean<v_bid_assigned> queryBidInfoBillD(int noPage, String typeStr, String startDateStr, String endDateStr,
			String name, String orderTypeStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		// int type = 0;
		int order = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.TEN;

		// String [] typeCondition = {" "," and overdue_bill_count <= 0 and
		// bad_bid_count<= 0 ",
		// " and overdue_bill_count > 0 and bad_bid_count<= 0 "," and overdue_bill_count
		// <= 0 and bad_bid_count > 0 "};
		String[] orderCondition = { " order by id desc ", " order by amount asc ", " order by amount desc ", " order by apr asc ",
				" order by apr desc " };

		StringBuffer conditions = new StringBuffer(" 1=1   ");
		List<Object> values = new ArrayList<Object>();

		PageBean<v_bid_assigned> page = new PageBean<v_bid_assigned>();
		List<v_bid_assigned> userBills = new ArrayList<v_bid_assigned>();

		int count = 0;

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (StringUtils.isNotBlank(startDateStr) && StringUtils.isNotBlank(endDateStr)) {
			Date start = DateUtil.strDateToStartDate(startDateStr);
			Date end = DateUtil.strDateToEndDate(endDateStr);
			conditions.append(" and  audit_time >= ? and audit_time <= ? ");
			values.add(start);
			values.add(end);
		}

		if (StringUtils.isNotBlank(name)) {
			conditions.append(" and user_name like ?");
			values.add("%" + name + "%");
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			order = Integer.parseInt(orderTypeStr);
			conditions.append(orderCondition[order]);
		}

		page.pageSize = pageSize;
		page.currPage = currPage;

		try {
			count = (int) v_bid_assigned.count(conditions.toString(), values.toArray());

			if (noPage != Constants.NO_PAGE) {
				userBills = v_bid_assigned.find(conditions.toString(), values.toArray()).fetch(currPage, page.pageSize);
			} else {
				userBills = v_bid_assigned.find(conditions.toString(), values.toArray()).fetch();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询本月到期账单情况时：" + e.getMessage());
			error.code = -3;
			error.msg = "";

			return page;
		}

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keywords", name);
		// conditionMap.put("type", type);
		conditionMap.put("startDate", startDateStr);
		conditionMap.put("endDate", endDateStr);
		conditionMap.put("order", order);

		page.page = userBills;
		page.totalCount = count;
		page.conditions = conditionMap;

		error.code = 0;

		return page;
	}

	/**
	 * 部门账单管理--坏账会员管理
	 * 
	 * @param userId
	 * @return
	 */
	public static PageBean<v_user_loan_info_bad_d> queryUserInfoBadD(int noPage, String typeStr, String startDateStr,
			String endDateStr, String name, String orderTypeStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		int type = 0;
		int order = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.TEN;

		String[] typeCondition = { " ", " and overdue_bill_count <= 0 and bad_bid_count<= 0 ",
				" and overdue_bill_count > 0 and bad_bid_count<= 0 ", " and overdue_bill_count <= 0 and bad_bid_count > 0 " };
		String[] orderCondition = { " ", " order by bid_count asc ", " order by bid_count  desc ", " order by invest_count asc ",
				" order by invest_count desc ", " order by overdue_bill_count asc ", " order by overdue_bill_count  desc ",
				" order by bad_bid_count asc ", " order by bad_bid_count desc " };

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(
				"distinct id,supervisor_name,type,name,register_time,user_amount,last_login_time,credit_level_image_filename,bid_count,bid_amount,invest_count,invest_amount,bid_loaning_count,bid_repaymenting_count,overdue_bill_count,bad_bid_count,bad_bid_amount from (");
		sql.append(SQLTempletes.SELECT);

		sql.append(SQLTempletes.V_USER_LOAN_INFO_BAD_D);
		sql.append(" ) t1 where  1=1 ");

		List<Object> params = new ArrayList<Object>();

		PageBean<v_user_loan_info_bad_d> page = new PageBean<v_user_loan_info_bad_d>();
		List<v_user_loan_info_bad_d> userBills = new ArrayList<v_user_loan_info_bad_d>();

		int count = 0;

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (NumberUtil.isNumericInt(typeStr)) {
			type = Integer.parseInt(typeStr);
			sql.append(typeCondition[type]);
		}

		if (StringUtils.isNotBlank(startDateStr) && StringUtils.isNotBlank(endDateStr)) {
			Date start = DateUtil.strDateToStartDate(startDateStr);
			Date end = DateUtil.strDateToEndDate(endDateStr);
			sql.append(" and  register_time >= ? and register_time <= ? ");
			params.add(start);
			params.add(end);
		}

		if (StringUtils.isNotBlank(name)) {
			sql.append(" and name like ?");
			params.add("%" + name + "%");
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			order = Integer.parseInt(orderTypeStr);
			sql.append(orderCondition[order]);
		}

		page.pageSize = pageSize;
		page.currPage = currPage;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_loan_info_bad_d.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}

			if (noPage != Constants.NO_PAGE) {
				query.setFirstResult((currPage - 1) * pageSize);
				query.setMaxResults(pageSize);
			}

			userBills = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询部门账单管理--坏账会员管理时：" + e.getMessage());
			error.code = -3;
			error.msg = "";

			return page;
		}

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keywords", name);
		conditionMap.put("type", type);
		conditionMap.put("startDate", startDateStr);
		conditionMap.put("endDate", endDateStr);
		conditionMap.put("order", order);

		page.page = userBills;
		page.totalCount = count;
		page.conditions = conditionMap;

		error.code = 0;

		return page;
	}

	/**
	 * 部门账单管理--待分配的借款会员列表
	 * 
	 * @param userId
	 * @return
	 */
	public static PageBean<v_user_loan_user_unassigned> queryUserUnassigned(String name, String startDate, String endDate,
			int productIdStr, String orderType, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		int type = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.TEN;
		String[] orderCondition = { " ", " order by t_users.time asc ", " order by t_users.time desc ",
				" order by t_bids.id asc ", " order by t_bids.id desc ", " order by amount asc ", " order by amount  desc " };

		StringBuffer sql = new StringBuffer("");
		StringBuffer conditions = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_LOAN_USER_UNASSIGNED);

		List<Object> params = new ArrayList<Object>();
		List<Object> paramsCount = new ArrayList<Object>();

		params.add(Supervisor.currSupervisor().id);

		List<v_user_loan_user_unassigned> userBills = new ArrayList<v_user_loan_user_unassigned>();
		int count = 0;

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (productIdStr != 0) {
			conditions.append(" and product_id = ?");
			params.add(productIdStr);
			paramsCount.add(productIdStr);
		}

		if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
			Date start = DateUtil.strDateToStartDate(startDate);
			Date end = DateUtil.strDateToEndDate(endDate);
			conditions.append(" and  t_users.time >= ? and t_users.time <= ? ");
			params.add(start);
			params.add(end);
			paramsCount.add(start);
			paramsCount.add(end);
		}

		if (StringUtils.isNotBlank(name)) {
			conditions.append(" and  t_users.name like ? ");
			params.add("%" + name + "%");
			paramsCount.add("%" + name + "%");
		}

		if (NumberUtil.isNumericInt(orderType)) {
			type = Integer.parseInt(orderType);
			conditions.append(orderCondition[type]);
		}

		PageBean<v_user_loan_user_unassigned> page = new PageBean<v_user_loan_user_unassigned>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("name", name);
		conditionMap.put("startDate", startDate);
		conditionMap.put("endDate", endDate);
		conditionMap.put("order", type);
		conditionMap.put("productIdStr", productIdStr);

		EntityManager em = JPA.em();
		Query query = null;
		List<?> countList = null;

		try {
			query = em.createNativeQuery(SQLTempletes.V_USER_LOAN_USER_UNASSIGNED_COUNT + conditions.toString());

			for (int n = 1; n <= paramsCount.size(); n++) {
				query.setParameter(n, paramsCount.get(n - 1));
			}

			countList = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询待分配的借款会员列表总记录时：" + e.getMessage());
			error.code = -3;
			error.msg = "";

			return null;
		}

		if (null == countList || countList.size() == 0)
			return page;

		count = Integer.parseInt(countList.get(0).toString());

		try {
			query = em.createNativeQuery(sql.toString() + conditions.toString(), v_user_loan_user_unassigned.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			userBills = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询待分配的借款会员列表时：" + e.getMessage());
			error.code = -3;
			error.msg = "";

			return null;
		}

		page.pageSize = pageSize;
		page.currPage = currPage;
		page.page = userBills;
		page.totalCount = count;
		page.conditions = conditionMap;

		error.code = 0;

		return page;
	}

	/**
	 * 查询用户所有的交易记录
	 * 
	 * @param error
	 * @return
	 */
	public static List<v_user_details> queryAllDetails(ErrorInfo error) {
		error.clear();

		List<v_user_details> details = new ArrayList<v_user_details>();
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_DETAILS);
		sql.append(" and t_users.id = ? ");
		sql.append(SQLTempletes.V_USER_DETAILS_GROUP_ORDER);

		List<Object> params = new ArrayList<Object>();
		params.add(User.currUser().id);

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_details.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			details = query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询交易记录时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询交易记录失败";

			return null;
		}

		error.code = 0;

		return details;
	}

	/**
	 * 查询交易记录
	 * 
	 * @param userId
	 * @param type      类别
	 * @param beginTime 开始时间
	 * @param endTime   结束时间
	 * @param currPage
	 * @param pageSize
	 * @return
	 */

	public static PageBean<v_user_details> queryUserDetails(long userId, long type, String beginTime, String endTime,
			int currPage, int pageSize) {
		if (currPage == 0) {
			currPage = 1;
		}

		if (pageSize == 0) {
			pageSize = Constants.PAGE_SIZE;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_DETAILS);

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("type", type);
		conditionMap.put("dateBegin", beginTime);
		conditionMap.put("dateEnd", endTime);

		if (type != 0) {
			switch ((int) type) {
			/* 充值 */
			case 1:
				sql.append(
						" and (t_user_details.operation = ? or t_user_details.operation = ? or t_user_details.operation = ?) ");
				params.add(NumberUtil.getLongVal(DealType.RECHARGE_USER));
				params.add(NumberUtil.getLongVal(DealType.RECHARGE_HAND));
				params.add(NumberUtil.getLongVal(DealType.RECHARGE_OFFLINE));

				break;

			/* 提现 */
			case 2:
				sql.append(" and (t_user_details.operation = ? or " + "t_user_details.operation = ? or "
						+ "t_user_details.operation = ? or " + "t_user_details.operation = ? or "
						+ "t_user_details.operation = ?) ");
				params.add(NumberUtil.getLongVal(DealType.THAW_WITHDRAWALT));
				params.add(NumberUtil.getLongVal(DealType.FREEZE_WITHDRAWAL));
				params.add(NumberUtil.getLongVal(DealType.FREEZE_WITHDRAWAL_P));
				params.add(NumberUtil.getLongVal(DealType.CHARGE_LEFT_WITHDRAWALT));
				params.add(NumberUtil.getLongVal(DealType.CHARGE_AWARD_WITHDRAWALT));

				break;

			/* 服务费 */
			case 3:
				sql.append(" and (t_user_details.operation = ? or " + "t_user_details.operation = ? or "
						+ "t_user_details.operation = ? or " + "t_user_details.operation = ? or "
						+ "t_user_details.operation = ?) ");
				params.add(NumberUtil.getLongVal(DealType.CHARGE_RECHARGE_FEE));
				params.add(NumberUtil.getLongVal(DealType.CHARGE_WITHDRAWALT));
				params.add(NumberUtil.getLongVal(DealType.CHARGE_LOAN_SERVER_FEE));
				params.add(NumberUtil.getLongVal(DealType.CHARGE_INVEST_FEE));
				params.add(NumberUtil.getLongVal(DealType.CHARGE_DEBT_TRANSFER_MANAGEFEE));

				break;

			/* 账单还款 */
			case 4:
				sql.append(" and (t_user_details.operation = ? or t_user_details.operation = ?) ");
				params.add(NumberUtil.getLongVal(DealType.CHARGE_OVER_PAY));
				params.add(NumberUtil.getLongVal(DealType.CHARGE_NOMAL_PAY));

				break;

			/* 账单收入 */
			case 5:
				sql.append(
						" and (t_user_details.operation = ? or t_user_details.operation = ? or t_user_details.operation = ? or t_user_details.operation = ?) ");
				params.add(NumberUtil.getLongVal(DealType.PRICIPAL_PAY));
				params.add(NumberUtil.getLongVal(DealType.OVER_RECEIVE));
				params.add(NumberUtil.getLongVal(DealType.NOMAL_RECEIVE));
				params.add(NumberUtil.getLongVal(DealType.OFFLINE_COLLECTION));
				break;
			// 收入
			case 6:
				sql.append(" and ifnull(`t_user_detail_types`.`type`,0) = ?  ");
				params.add(NumberUtil.getLongVal(DealType.INCOME));
				break;
			// 支出
			case 7:
				sql.append(" and ifnull(`t_user_detail_types`.`type`,0) = ?");
				params.add(NumberUtil.getLongVal(DealType.PAY));
				break;
			}
		}

		if (StringUtils.isNotBlank(beginTime)) {
			sql.append(" and t_user_details.time >= ? ");
			params.add(DateUtil.strDateToStartDate(beginTime));
		}

		if (StringUtils.isNotBlank(endTime)) {
			sql.append(" and t_user_details.time <= ? ");
			params.add(DateUtil.strDateToEndDate(endTime));
		}

		sql.append(" and t_users.id = ? ");
		params.add(userId);
		sql.append(" and t_user_details.amount > 0 ");
		sql.append(SQLTempletes.V_USER_DETAILS_GROUP_ORDER);

		PageBean<v_user_details> page = new PageBean<v_user_details>();
		page.currPage = currPage;
		page.pageSize = pageSize;
		page.conditions = conditionMap;

		List<v_user_details> userDetails = new ArrayList<v_user_details>();

		// 处理协议支付-处理中数据显示
		if (page.currPage == 1) {
			// 充值记录-处理中
			List<t_user_recharge_details> userRecharges = ProtocolPay.findRecharge4I();
			if (userRecharges != null && !userRecharges.isEmpty()) {
				for (t_user_recharge_details user_recharge_details : userRecharges) {
					if (user_recharge_details.user_id == userId) {// 只筛选当前登录的用户
						v_user_details user_details = new v_user_details();
						user_details.user_id = user_recharge_details.user_id;
						user_details.time = user_recharge_details.time;
						user_details.operation = DealType.RECHARGE_USER;
						user_details.amount = user_recharge_details.amount;
						user_details.user_balance = 0;
						user_details.balance = 0;
						user_details.freeze = 0;
						user_details.recieve_amount = 0;
						user_details.summary = "交易处理中";
						user_details.name = "会员充值";
						user_details.type = 1;
						userDetails.add(user_details);
					}
				}
			}
		}

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_details.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			// userDetails = query.getResultList();
			userDetails.addAll(query.getResultList());

			page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询交易记录时：" + e.getMessage());

			return null;
		}

		page.page = userDetails;

		return page;
	}

	/**
	 * 用户最近动态
	 * 
	 * @param userId
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static PageBean<t_user_events> queryUserEvnets(long userId, ErrorInfo error, String currPageStr, String pageSizeStr) {
		error.clear();

		int currPage = Constants.ONE;
		int pageSize = Constants.TWO;

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		List<t_user_events> userDetails = new ArrayList<t_user_events>();
		int count = 0;

		try {
			count = (int) t_user_events.count("user_id = ?", userId);
			userDetails = t_user_events.find("user_id = ? order by time desc", userId).fetch(currPage, pageSize);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询用户最近动态时：" + e.getMessage());
			error.code = -1;
			error.msg = "用户最近动态查询失败";

			return null;
		}

		PageBean<t_user_events> page = new PageBean<t_user_events>();
		page.currPage = currPage;
		page.pageSize = pageSize;
		page.totalCount = count;

		page.page = userDetails;

		error.code = 0;

		return page;
	}

	/**
	 * 理财情况统计表
	 * 
	 * @param userId
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static PageBean<v_bill_invest_statistics> queryUserInvestStatistics(long userId, int year, int month, int orderType,
			int currPage, ErrorInfo error) {
		error.clear();

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		List<Object> values = new ArrayList<Object>();
		User user = User.currUser();

		conditionMap.put("year", year);
		conditionMap.put("month", month);
		conditionMap.put("orderType", orderType);

		StringBuffer conditions = new StringBuffer("1=1 and user_id = ? ");

		values.add(user.id);

		if (year != 0) {
			conditions.append("and year = ? ");
			values.add(year);
		}

		if (month != 0) {
			conditions.append("and month = ? ");
			values.add(month);
		}

		if (orderType != 0) {
			conditions.append(Constants.INVEST_STATISTICS[orderType]);
		}

		List<v_bill_invest_statistics> bills = new ArrayList<v_bill_invest_statistics>();
		int count = 0;

		try {
			count = (int) v_bill_invest_statistics.count(conditions.toString(), values.toArray());
			bills = v_bill_invest_statistics.find(conditions.toString(), values.toArray()).fetch(currPage, Constants.PAGE_SIZE);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询理财情况统计表时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询理财情况统计表时出现异常";

			return null;
		}

		PageBean<v_bill_invest_statistics> page = new PageBean<v_bill_invest_statistics>();

		page.pageSize = Constants.PAGE_SIZE;
		page.currPage = currPage;
		page.page = bills;
		page.totalCount = count;
		page.conditions = conditionMap;

		error.code = 0;

		return page;
	}

	/**
	 * 前台--站内信
	 * 
	 * @param userId
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @return
	 */
	public static PageBean<v_user_for_message> queryUserForMessage(long userId, String currPageStr, String pageSizeStr,
			ErrorInfo error) {
		error.clear();

		int currPage = Constants.ONE;
		int pageSize = Constants.TEN;

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_FOR_MESSAGE);
		sql.append(" and user_id = ? ");
		List<Object> params = new ArrayList<Object>();
		params.add(userId);

		List<v_user_for_message> users = new ArrayList<v_user_for_message>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_for_message.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("发站内信选择会员时：" + e.getMessage());

			return null;
		}

		PageBean<v_user_for_message> page = new PageBean<v_user_for_message>();

		page.pageSize = pageSize;
		page.currPage = currPage;
		page.page = users;
		page.totalCount = count;

		error.code = 0;

		return page;
	}

	/**
	 * 查询账户信息（资金管理中的账户信息）
	 * 
	 * @param userId
	 * @return
	 */
	public static v_user_account_info queryUserAccountInfo(long userId, ErrorInfo error) {
		error.clear();

		v_user_account_info accountInfo = null;
		List<v_user_account_info> v_user_account_info_list = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_ACCOUNT_INFO);
		sql.append(" and t_users.id = ?");

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_account_info.class);
			query.setParameter(1, userId);
			query.setMaxResults(1);
			v_user_account_info_list = query.getResultList();

			if (v_user_account_info_list.size() > 0) {
				accountInfo = v_user_account_info_list.get(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询账户信息（资金管理中的账户信息）时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询账户信息（资金管理中的账户信息）出现异常！";

			return null;
		}

		error.code = 0;

		return accountInfo;
	}

	/**
	 * 查询注册会员总数
	 * 
	 * @param error
	 * @return
	 */
	public static long queryTotalRegisterUserCount(ErrorInfo error) {
		error.clear();

		long count = 0;

		try {
			count = t_users.count();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			error.code = -1;
			error.msg = "查询注册会员总数失败";

			return -1;
		}

		error.code = 0;

		return count;
	}

	/**
	 * 查询今日注册会员总数
	 * 
	 * @param error
	 * @return
	 */
	public static long queryTodayRegisterUserCount(ErrorInfo error) {
		error.clear();

		long count = 0;

		try {
			count = t_users.count("DATEDIFF(NOW(), time) < 1");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			error.code = -1;
			error.msg = "查询今日注册会员数失败";

			return -1;
		}

		error.code = 0;

		return count;
	}

	/**
	 * 查询在线会员数量
	 * 
	 * @return
	 */
	public static long queryOnlineUserNum() {
		return CacheManager.getCacheSize("online_user_");
	}

	public long successLoanCount; // 成功借款次数
	public long normalRepaymentCount; // 正常还款次数
	public long overdueRepaymentCount; // 逾期
	public long flowBids; // 流标

	public long getSuccessLoanCount() {

		ErrorInfo error = new ErrorInfo();

		return querySuccessLoanCount(this.id, error);
	}

	public long getNormalRepaymentCount() {
		ErrorInfo error = new ErrorInfo();

		return queryNormalRepaymentCount(this.id, error);
	}

	public long getOverdueRepaymentCount() {
		ErrorInfo error = new ErrorInfo();

		return queryOverdueRepaymentCount(this.id, error);
	}

	public long getFlowBids() {
		ErrorInfo error = new ErrorInfo();
		return queryFlowBids(this.id, error);
	}

	/**
	 * 统计用户成功借款次数
	 * 
	 * @param userId
	 * @param error
	 * @return
	 */
	public static long querySuccessLoanCount(long userId, ErrorInfo error) {
		String sql = "select count(*) from t_bids where user_id = ? and status in (4,5)";

		long count = 0;

		try {
			count = t_bids.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}

		error.code = 1;
		return count;
	}

	/**
	 * 用户正常还款次数
	 * 
	 * @param userId
	 * @return
	 */
	public static long queryNormalRepaymentCount(long userId, ErrorInfo error) {

		long count = 0;
		List<Long> bidIds = null;

		String sql = "select id from t_bids where user_id = ? and status in (4,5)";

		try {
			bidIds = t_bids.find(sql, userId).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}

		if (bidIds.size() > 0) {

			String idStr = StringUtils.join(bidIds, ",");

			sql = "select count(*) from t_bills where bid_id in ( " + idStr + " ) and status = 0";

			try {
				count = t_bills.find(sql).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
			}
		}
		error.code = 1;
		return count;
	}

	/**
	 * 用户逾期还款次数
	 * 
	 * @param userId
	 * @return
	 */
	public static long queryOverdueRepaymentCount(long userId, ErrorInfo error) {

		long count = 0;
		List<Long> bidIds = null;

		String sql = "select id from t_bids where user_id = ? and status in (4,5)";

		try {
			bidIds = t_bids.find(sql, userId).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}

		if (bidIds.size() > 0) {

			String idStr = StringUtils.join(bidIds, ",");

			sql = "select count(*) from t_bills where bid_id in ( " + idStr + " ) and status = -3 ";

			try {
				count = t_bills.find(sql).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
			}
		}
		error.code = 1;
		return count;
	}

	/**
	 * 用户借入金额
	 * 
	 * @return
	 */
	public static Double loanAmount(long userId, ErrorInfo error) {

		Double loanAmount = 0.0;

		String sql = "select sum(amount) from t_bids where user_id = ? and status in (4,5)";

		try {
			loanAmount = t_bids.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}

		if (null == loanAmount) {
			loanAmount = 0.0;
		}
		error.code = 1;
		return loanAmount;
	}

	/**
	 * 用户待付款金额
	 * 
	 * @param userId
	 * @param error
	 * @return
	 */
	public static Double pendingRepaymentAmount(long userId, ErrorInfo error) {

		Double count = 0.0;

		List<Long> bidIds = null;

		String sql = "select id from t_bids where user_id = ? and status = 4 ";

		try {
			bidIds = t_bids.find(sql, userId).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}

		if (bidIds.size() > 0) {

			String idStr = StringUtils.join(bidIds, ",");
			sql = "select sum(repayment_corpus + repayment_interest +overdue_fine) from t_bills where bid_id in ( " + idStr
					+ " ) and status in (-1,-2)";

			try {
				count = t_bills.find(sql).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
			}

			if (null == count) {
				count = 0.0;
			}
		}

		error.code = 1;
		return count;

	}

	/**
	 * 用户理财投标笔数
	 * 
	 * @param userId
	 * @param error
	 * @return
	 */
	public static long financialCount(long userId, ErrorInfo error) {

		long count = 0;

		String sql = "select count(*) from t_invests where user_id = ?";

		try {
			count = t_invests.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		error.code = 1;
		return count;

	}

	/**
	 * 用户待收金额
	 * 
	 * @param userId
	 * @param error
	 * @return
	 */
	public static Double receivingAmount(long userId, ErrorInfo error) {

		Double count = 0.0;

		String sql = "select sum(receive_corpus + receive_interest + overdue_fine) from t_bill_invests where user_id = ? and status in (-1,-2)";

		try {
			count = t_bill_invests.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}

		if (null == count) {
			count = 0.0;
		}
		error.code = 1;
		return count;
	}

	/**
	 * 用户流标次数
	 * 
	 * @param userId
	 * @param error
	 * @return
	 */
	public static long queryFlowBids(long userId, ErrorInfo error) {

		long count = 0;

		String sql = "select count(*) from t_bids where user_id = ? and status in (-1,-2,-3,-4)";

		try {
			count = t_bids.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}

		error.code = 1;
		return count;
	}

	/**
	 * 查询借款用户历史记录情况
	 * 
	 * @param userId
	 * @return
	 */
	public static Map<String, String> historySituation(long userId, ErrorInfo error) {

		Map<String, String> historySituationMap = new HashMap<String, String>();
		java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");// 保留2位小数

		long successLoanCount = querySuccessLoanCount(userId, error);

		historySituationMap.put("successBidCount", successLoanCount + "");// 成功借款次数

		long normalRepaymentCount = queryNormalRepaymentCount(userId, error);

		historySituationMap.put("normalRepaymentCount", normalRepaymentCount + "");// 正常还款次数

		long overdueRepaymentCount = queryOverdueRepaymentCount(userId, error);

		historySituationMap.put("overdueRepaymentCount", overdueRepaymentCount + "");// 逾期还款次数

		double loanAmount = loanAmount(userId, error);

		historySituationMap.put("loanAmount", loanAmount + "");// 借入金额

		double pendingRepaymentAmount = pendingRepaymentAmount(userId, error);

		historySituationMap.put("pendingRepaymentAmount", df.format(pendingRepaymentAmount));// 待付款金额

		long financialCount = financialCount(userId, error);

		historySituationMap.put("financialCount", financialCount + "");// 理财投标笔数

		Double receivingAmount = receivingAmount(userId, error);

		historySituationMap.put("receivingAmount", df.format(receivingAmount));// 待收金额

		long flowBids = queryFlowBids(userId, error);

		historySituationMap.put("flowBids", flowBids + "");// 流标次数

		return historySituationMap;
	}

	/**
	 * 查询债权用户历史记录情况
	 * 
	 * @param userId
	 * @return
	 */
	public static Map<String, String> debtUserhistorySituation(long userId, ErrorInfo error) {

		Map<String, String> historySituationMap = new HashMap<String, String>();

		long successBidCount = querySuccessLoanCount(userId, error);

		historySituationMap.put("successBidCount", successBidCount + "");// 成功借款次数

		long normalRepaymentCount = queryNormalRepaymentCount(userId, error);

		historySituationMap.put("normalRepaymentCount", normalRepaymentCount + "");// 正常还款次数

		long overdueRepaymentCount = queryOverdueRepaymentCount(userId, error);

		historySituationMap.put("overdueRepaymentCount", overdueRepaymentCount + "");// 逾期还款次数

		long flowBids = queryFlowBids(userId, error);

		historySituationMap.put("flowBids", flowBids + "");// 流标次数

		return historySituationMap;
	}

	/**
	 * 环迅
	 * 
	 * @param money
	 * @param bankType
	 * @param error
	 * @return
	 */
	public static Map<String, String> ipay(BigDecimal money, int bankType, int type, String rechargeMark, int client,
			ErrorInfo error) {
		error.clear();

		if (bankType < 0 || bankType > 5) {
			error.code = -1;
			error.msg = "传入参数有误";

			return null;
		}

		t_dict_payment_gateways gateway = gateway(Constants.IPS_GATEWAY, error);

		if (error.code < 0) {
			return null;
		}

		DecimalFormat currentNumberFormat = new DecimalFormat("#0.00");
		Map<String, String> args = new HashMap<String, String>();

		String billno = NumberUtil.getBillNo(type);

		args.put("Mer_code", gateway.pid);
		args.put("Billno", billno);
		args.put("Amount", currentNumberFormat.format(money));
		args.put("Date", DateUtil.simple(new Date()));
		args.put("Currency_Type", "RMB");
		args.put("Gateway_Type", Constants.IPS_TYPE[bankType]);
		args.put("Lang", "");
		args.put("Merchanturl", Constants.IPS_MERCHANT_URL);
		args.put("Attach", rechargeMark);
		args.put("OrderEncodeType", "5");
		args.put("RetEncodeType", "17");
		args.put("Rettype", "1");
		args.put("ServerUrl", Constants.IPS_SERVER_URL);
		StringBuilder singnMd5 = new StringBuilder();
		singnMd5.append("billno");
		singnMd5.append(args.get("Billno"));
		singnMd5.append("currencytype");
		singnMd5.append(args.get("Currency_Type"));
		singnMd5.append("amount");
		singnMd5.append(args.get("Amount"));
		singnMd5.append("date");
		singnMd5.append(args.get("Date"));
		singnMd5.append("orderencodetype");
		singnMd5.append(args.get("OrderEncodeType"));
		String ips = gateway._key;
		singnMd5.append(ips);

		cryptix.jce.provider.MD5 b = new cryptix.jce.provider.MD5();

		args.put("SignMD5", b.toMD5(singnMd5.toString()).toLowerCase());
		String url = Constants.IPS_URL;
		args.put("url", url);

		sequence(Constants.GATEWAY_IPS, billno, money.doubleValue(), Constants.GATEWAY_RECHARGE, client, error);

		if (error.code < 0) {
			return null;
		}

		return args;
	}

	/**
	 * 所有支付接口
	 * 
	 * @param error
	 * @return
	 */
	public static List<t_dict_payment_gateways> gateways(ErrorInfo error) {
		error.clear();

		List<t_dict_payment_gateways> gateways = null;

		try {
			gateways = t_dict_payment_gateways.findAll();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询支付方式时：" + e.getMessage());

			error.code = -1;
			error.msg = "查询支付方式失败";

			return null;
		}

		error.code = 0;

		return gateways;
	}

	/**
	 * 用于前台充值，判断支付方式是否使用
	 * 
	 * @param error
	 * @return
	 */
	public static List<t_dict_payment_gateways> gatewayForUse(ErrorInfo error) {
		error.clear();

		List<t_dict_payment_gateways> gateways = null;

		String sql = "select new t_dict_payment_gateways(id, is_use) from t_dict_payment_gateways";

		try {
			gateways = t_dict_payment_gateways.find(sql).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询支付方式是否使用时：" + e.getMessage());

			error.code = -1;
			error.msg = "查询支付方式失败";

			return null;
		}

		error.code = 0;

		return gateways;
	}

	/**
	 * 支付接口
	 */
	public static t_dict_payment_gateways gateway(long way, ErrorInfo error) {
		t_dict_payment_gateways gateway = null;

		try {
			gateway = t_dict_payment_gateways.find("id = ? and is_use = ?", way, true).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询支付方式时：" + e.getMessage());

			error.code = -1;
			error.msg = "查询支付方式失败";

			return null;
		}

		if (gateway == null) {
			error.code = -1;
			error.msg = "您选择的充值方式已停止使用，请选择其他充值方式";

			return null;
		}

		error.code = 0;

		return gateway;
	}

	/**
	 * 保存支付接口
	 */
	public static void saveGateways(int select1, String account1, String pid1, String key1, int select2, String account2,
			String pid2, String key2, ErrorInfo error) {
		error.clear();

		Query query1 = JPA.em()
				.createQuery("update t_dict_payment_gateways set " + " account =  ?,  pid = ? , _key = ?, is_use = ? where id= 1")
				.setParameter(1, account1).setParameter(2, pid1).setParameter(3, key1)
				.setParameter(4, select1 == 0 ? false : true);

		Query query2 = JPA.em()
				.createQuery("update t_dict_payment_gateways set " + " account =  ?,  pid = ? , _key = ?, is_use = ? where id= 2")
				.setParameter(1, account2).setParameter(2, pid2).setParameter(3, key2)
				.setParameter(4, select2 == 0 ? false : true);

		try {
			query1.executeUpdate();
			query2.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("保存支付接口时：" + e.getMessage());

			error.code = -1;
			error.msg = "保存支付接口失败";

			return;
		}

		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.PAYMENT_SET, "设置支付方式", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		error.msg = "保存成功";
	}

	/**
	 * 充值前
	 * 
	 * @param payNumber
	 * @param amount
	 * @param error
	 */
	public static void sequence(int gateway, String payNumber, double amount, int type, int client, ErrorInfo error) {
		error.clear();

		t_user_recharge_details detail = new t_user_recharge_details();

		detail.user_id = User.currUser().id;
		detail.time = new Date();
		detail.payment_gateway_id = gateway;
		detail.pay_number = payNumber;
		detail.amount = amount;
		detail.is_completed = false;
		detail.type = type;
		detail.client = client;

		try {
			detail.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("充值插入充值数据时：" + e.getMessage());

			error.code = -1;
			error.msg = "充值请求失败";

			return;
		}

		error.code = 0;
	}

	/**
	 * 连连认证充值前 发验证码时生成订单
	 *
	 * @param payNumber
	 * @param amount
	 * @param error
	 */
	public static void sequence_new(long userId, int gateway, String uniqueKey, String payNumber, double amount, int type,
			int client, String bankCardNo, ErrorInfo error) {
		error.clear();

		t_user_recharge_details detail;
		if (StringUtils.isNotEmpty(uniqueKey)) {
			detail = t_user_recharge_details.find(" unique_key = ? ", uniqueKey).first();
			if (detail == null) {

				detail = new t_user_recharge_details();
				detail.user_id = userId;
				// detail.user_id = 1;
				detail.time = new Date();
				detail.payment_gateway_id = gateway;
				detail.unique_key = uniqueKey;
//				detail.pay_number = payNumber;
				detail.amount = amount;
				detail.is_completed = false;
				detail.type = type;
				detail.client = client;
				detail.bank_card_no = bankCardNo;

				try {
					detail.save();
				} catch (Exception e) {
					e.printStackTrace();
					Logger.info("充值插入充值数据时：" + e.getMessage());

					error.code = -1;
					error.msg = "充值请求失败";

					return;
				}
			} else {
				error.code = -1;
				error.msg = "充值请求失败";
			}

		}

		error.code = 0;
	}

	/**
	 * 连连认证充值前
	 *
	 * @param payNumber
	 * @param amount
	 * @param error
	 */
	public static void sequence(long userId, int gateway, String payNumber, double amount, int type, int client,
			String bankCardNo, ErrorInfo error) {
		error.clear();

		t_user_recharge_details detail = new t_user_recharge_details();

		detail.user_id = userId;
		// detail.user_id = 1;
		detail.time = new Date();
		detail.payment_gateway_id = gateway;
		detail.pay_number = payNumber;
		detail.amount = amount;
		detail.is_completed = false;
		detail.type = type;
		detail.client = client;
		detail.bank_card_no = bankCardNo;

		try {
			detail.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("充值插入充值数据时：" + e.getMessage());

			error.code = -1;
			error.msg = "充值请求失败";

			return;
		}

		error.code = 0;
	}

	/**
	 * 充值记录,pay_number=detail.id 可用于后台自动充值
	 * 
	 * @param userId
	 * @param gateway
	 * @param amount
	 * @param type
	 * @param client
	 * @param bankCardNo
	 * @param error
	 * @return
	 */
	public static t_user_recharge_details sequence(long userId, double amount, String bankCardNo, int recharge_for_type,
			long recharge_for_id, int gateway, int type, int client) {

		t_user_recharge_details detail = new t_user_recharge_details();
		detail.user_id = userId;
		detail.time = new Date();
		detail.payment_gateway_id = gateway;
		detail.recharge_for_type = recharge_for_type;
		detail.recharge_for_id = recharge_for_id;
		detail.amount = amount;
		detail.is_completed = false;
		detail.type = type;
		detail.client = client;
		detail.bank_card_no = bankCardNo;

		try {
			detail.save();
			detail.pay_number = Constants.BF_GATEWAY + "at" + detail.id + RandomUtil.getRandom(6);
			detail.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("充值插入充值数据时：" + e.getMessage());
			throw e;
		}

		return detail;
	}

	/**
	 * 充值前
	 * 
	 * @param payNumber
	 * @param amount
	 * @param error
	 */
	public static void sequence(Date date, int gateway, String payNumber, double amount, int type, int client, ErrorInfo error) {
		error.clear();

		t_user_recharge_details detail = new t_user_recharge_details();

		detail.user_id = User.currUser().id;
		detail.time = date;
		detail.payment_gateway_id = gateway;
		detail.pay_number = payNumber;
		detail.amount = amount;
		detail.is_completed = false;
		detail.type = type;
		detail.client = client;

		try {
			detail.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("充值插入充值数据时：" + e.getMessage());

			error.code = -1;
			error.msg = "充值请求失败";

			return;
		}

		error.code = 0;
	}

	/**
	 * 手工充值
	 * 
	 * @param userId
	 * @param gateway
	 * @param amount
	 * @param type
	 * @param error
	 */
	public static void sequence(long userId, int gateway, double amount, int type, ErrorInfo error) {
		error.clear();

		t_user_recharge_details detail = new t_user_recharge_details();

		detail.user_id = userId;
		detail.type = type;
		detail.amount = amount;
		detail.time = new Date();
		detail.completed_time = new Date();
		detail.payment_gateway_id = gateway;
		detail.is_completed = true;

		try {
			detail.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("充值插入充值数据时：" + e.getMessage());

			error.code = -1;
			error.msg = "充值请求失败";

			return;
		}

		error.code = 0;
	}

	/**
	 * 充值确认
	 * 
	 * @param payNumber
	 * @param amount
	 * @param error
	 */
	public static void recharge(String payNumber, double amount, ErrorInfo error) {
		error.clear();

		long userId = -1;
		t_user_recharge_details user_recharge = null;
		try {
			user_recharge = t_user_recharge_details.find("pay_number = ?", payNumber).first();

		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("判断是否已经充值时：" + e.getMessage());

			error.code = -1;
			error.msg = "充值回调失败";

			return;
		}

		if (user_recharge == null) {
			error.code = -1;
			error.msg = "充值回调失败";

			return;
		}

		JPA.em().refresh(user_recharge, LockModeType.PESSIMISTIC_WRITE);

		if (user_recharge.is_completed) {
			error.code = Constants.ALREADY_RUN;
			error.msg = "充值已完成";

			return;
		}

		userId = Convert.strToLong(user_recharge.user_id + "", -1);
		if (userId < 0) {
			error.code = -1;
			error.msg = "不存在的用户";

			return;
		}

		User user = new User();
		user.id = userId;

		DataSafety data = new DataSafety();

		data.id = user.id;

		if (!data.signCheck(error)) {
			JPA.setRollbackOnly();

			return;
		}

		String sql = "update t_user_recharge_details set is_completed = ?, completed_time = ? where pay_number = ? and is_completed = 0";
		int rows = 0;

		try {
			rows = JPA.em().createQuery(sql).setParameter(1, true).setParameter(2, new Date()).setParameter(3, payNumber)
					.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info(e.getMessage());

			error.code = -1;
			error.msg = "数据库异常";

			return;
		}

		if (rows == 0) {
			error.code = Constants.ALREADY_RUN;
			error.msg = "充值已完成";

			return;
		}

		t_system_recharge_completed_sequences sequence = new t_system_recharge_completed_sequences();
		sequence.pay_number = payNumber;
		sequence.time = new Date();
		sequence.amount = amount;

		try {
			sequence.save();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info("在充值确认插入更新数据时：" + e.getMessage());
			error.code = Constants.ALREADY_RUN;
			error.msg = "充值已完成";

			return;
		}

		int type = 0;
		// 更新用户资金
		DealDetail.addUserFund(user.id, amount);
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}
		v_user_for_details forDetail = DealDetail.queryUserBalance(user.id, error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}
		double balance = forDetail.user_amount;
		DealDetail detail = new DealDetail(user.id, DealType.RECHARGE_USER, amount, sequence.id, balance, forDetail.freeze,
				forDetail.receive_amount, "充值");
		detail.addDealDetail(error);
		// 添加交易记录
		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		data.updateSignWithLock(user.id, error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		DealDetail.userEvent(user.id, UserEvent.RECHARGE, "充值", error);
		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}
		// 发送站内信 [userName]充值了￥[money]元
		TemplateStation station = new TemplateStation();
		station.id = Templets.M_USER_RECHARGE;
		if (station.status) {
			String mContent = station.content.replace("userName", user.name);
			mContent = mContent.replace("money", DataUtil.formatString(amount));
			mContent = mContent.replace("date", DateUtil.dateToString(new Date()));
			StationLetter letter = new StationLetter();
			letter.senderSupervisorId = 1;
			letter.receiverUserId = userId;
			letter.title = station.title;
			letter.content = mContent;
			letter.sendToUserBySupervisor(error);
		}
		// 发送邮件[userName]充值了￥[money]元
		TemplateEmail email = new TemplateEmail();
		email.id = Templets.E_USER_RECHARGE;
		if (email.status) {
			String eContent = email.content.replace("userName", user.name);
			eContent = eContent.replace("money", DataUtil.formatString(amount));
			eContent = eContent.replace("date", DateUtil.dateToString(new Date()));
			email.addEmailTask(user.email, email.title, eContent);
		}

		// 后台充值
		if (user_recharge.client == Constants.CLIENT_BACKSTAGE && user_recharge.recharge_for_type != null
				&& user_recharge.recharge_for_type.equals(t_enum_map
						.getEnumNameMapByTypeName("t_user_recharge_details.recharge_for_type").get("t_bill.id").enum_code)
				&& user_recharge.recharge_for_id != null) {

			// 尊敬的userName:您于date成功支付money元,该笔支付用于bid_id号借款第period期借款账单的还款请求,
			// 请登录亿亿理财借款端查看还款进度.中亿云投资有限公司客服电话:021-6438-0510

			TemplateSms sms = new TemplateSms();
			sms.id = Templets.S_AUTO_RECHARGE_FOR_BILL_SUCCESS;
			if (sms.status && StringUtils.isNotBlank(user.mobile)) {
				String eContent = sms.content.replace("userName", user.name);
				eContent = eContent.replace("date", DateUtil.dateToString(new Date()));
				eContent = eContent.replace("money", DataUtil.formatString(amount));
				t_bills bill = t_bills.findById(user_recharge.recharge_for_id);
				eContent = eContent.replace("bid_id", bill.bid_id + "");
				eContent = eContent.replace("period", bill.periods + "");

				TemplateSms.addSmsTask(user.mobile, eContent);
			}
		} else {
			// 发送短信 [userName]充值了￥[money]元
			TemplateSms sms = new TemplateSms();
			sms.id = Templets.S_RECHARGE_SUCCESS;
			if (sms.status && StringUtils.isNotBlank(user.mobile)) {
				String eContent = sms.content.replace("userName", user.name);
				eContent = eContent.replace("money", DataUtil.formatString(amount));
				TemplateSms.addSmsTask(user.mobile, eContent);
			}
		}
		user.balance = balance;
		User.setCurrUser(user);
		error.code = 0;
	}

	/**
	 * 环迅返回sign校验
	 * 
	 * @param content
	 * @param signature
	 * @param error
	 */
	public static void validSign(String content, String signature, ErrorInfo error) {
		error.clear();

		t_dict_payment_gateways gateway = gateway(Constants.IPS_GATEWAY, error);

		if (error.code < 0) {
			return;
		}

		cryptix.jce.provider.MD5 b = new cryptix.jce.provider.MD5();
		String SignMD5 = b.toMD5(content + gateway._key).toLowerCase();

		if (!SignMD5.equals(signature)) {
			error.code = -1;
			error.msg = "验证失败";
		}

		error.code = 0;
	}

	/**
	 * 国付宝
	 * 
	 * @param money
	 * @param bankType
	 * @return
	 */
	public static Map<String, String> gpay(BigDecimal money, int bankType, int type, String rechargeMark, int client,
			ErrorInfo error) {
		error.clear();
		/* 2014-11-17 */
		// if(bankType < 0 || bankType > 17) {
		if (bankType < 0 || bankType > 20) {
			error.code = -1;
			error.msg = "传入参数有误";

			return null;
		}

		t_dict_payment_gateways gateway = gateway(Constants.GO_GATEWAY, error);

		if (error.code < 0) {
			return null;
		}

		DecimalFormat currentNumberFormat = new DecimalFormat("#0.00");
		String version = "2.1";
		// String charset = "utf-8";
		// String language = "";
		// String signType = "";
		// 交易代码
		String tranCode = Constants.TRANCODE;

		String merchantID = gateway.pid;
		String merOrderNum = NumberUtil.getBillNo(type); // 订单号 ---- 支付流水号
		String tranAmt = currentNumberFormat.format(money);
		// String feeAmt = "0";
		String frontMerUrl = Constants.GO_MER_URL;
		String backgroundMerUrl = Constants.GO_MER_BACK_URL;
		String tranIP = GopayUtils.getIpAddr(Request.current());
		// String gopayServerTime = GopayUtils.getGopayServerTime();
		String tranDateTime = DateUtil.simple2(new Date());

		Map<String, String> args = new HashMap<String, String>();
		args.put("version", version);
		args.put("charset", "2");
		args.put("language", "1");
		args.put("signType", "1");
		args.put("tranCode", tranCode);
		args.put("merchantID", merchantID);
		args.put("merOrderNum", merOrderNum);
		args.put("tranAmt", tranAmt);
		args.put("feeAmt", "");
		args.put("tranDateTime", tranDateTime);
		args.put("frontMerUrl", frontMerUrl);
		args.put("backgroundMerUrl", backgroundMerUrl);
		args.put("currencyType", Constants.CURRENCYTYPE);
		args.put("virCardNoIn", gateway.account);
		String bank = Constants.GO_TYPE[bankType];
		if (!"DEFAULT".equals(bank)) {
			args.put("bankCode", bank);
			args.put("userType", "1");
			// args.put("bankCode", "");
			// args.put("userType", "");

		} /*
			 * else { args.put("bankCode", bank); args.put("bankCode", ""); }
			 */

		args.put("orderId", "");
		args.put("gopayOutOrderId", "");
		args.put("tranIP", tranIP);
		args.put("respCode", "");
		args.put("VerficationCode", gateway._key);
		args.put("gopayServerTime", "");
		args.put("merRemark1", rechargeMark);
		Logger.info("args:%s", args);
		// 组织加密明文
		StringBuffer plain = new StringBuffer();
		plain.append("version=[");
		plain.append(version);
		plain.append("]tranCode=[");
		plain.append(tranCode);
		plain.append("]merchantID=[");
		plain.append(merchantID);
		plain.append("]merOrderNum=[");
		plain.append(merOrderNum);
		plain.append("]tranAmt=[");
		plain.append(tranAmt);
		plain.append("]feeAmt=[]");
		plain.append("tranDateTime=[");
		plain.append(tranDateTime);
		plain.append("]frontMerUrl=[");
		plain.append(frontMerUrl);
		plain.append("]backgroundMerUrl=[");
		plain.append(backgroundMerUrl);
		plain.append("]orderId=[]gopayOutOrderId=[]tranIP=[");
		plain.append(tranIP);
		plain.append("]respCode=[]gopayServerTime=[]");
		plain.append("VerficationCode=[");
		plain.append(gateway._key);
		plain.append("]");
		String signValue = GopayUtils.md5(plain.toString());
		String url = Constants.GO_URL;
		args.put("signValue", signValue);
		args.put("url", url);

		sequence(Constants.GATEWAY_GUO, merOrderNum, money.doubleValue(), Constants.GATEWAY_RECHARGE, client, error);

		if (error.code < 0) {
			return null;
		}

		return args;
	}

	/**
	 * 连连网银支付 2015年12月12日 连连支付网银充值
	 */
	public static Map<String, String> LLpay(BigDecimal money, int bankType, int type, String rechargeMark, int client,
			int rechargeType, String card_no, ErrorInfo error) {

		Map<String, String> args = new HashMap<String, String>();

		User user = User.currUser();

		user.id = user.id;

		String merOrderNum = NumberUtil.getBillNo(type); // 订单号 ---- 支付流水号

		DecimalFormat currentNumberFormat = new DecimalFormat("#0.00");

		// 认证支付需要绑定银行卡
		if (rechargeType == 1) {

			UserBankAccounts bankUser = new UserBankAccounts();

			// 查询是否有已经验证的银行卡
			t_user_bank_accounts bank = UserBankAccounts.queryById(user.id);

			if (bank == null) {
				// 查询用户是否已经有该未验证的银行账号
				t_user_bank_accounts banks = UserBankAccounts.queryByIds(user.id, card_no);
				if (banks == null) {
					bankUser.userId = user.id;
					bankUser.bankName = Constants.GO_BANK_NAME[bankType];
					bankUser.bankCode = Constants.GO_CODE[bankType];
					bankUser.account = card_no;
					bankUser.accountName = user.realityName;
					bankUser.addBankAccount(error);
				} else {
					if (!banks.account.equals(card_no.trim())) {
						bankUser.userId = user.id;
						bankUser.bankName = Constants.GO_BANK_NAME[bankType];
						bankUser.bankCode = Constants.GO_CODE[bankType];
						bankUser.account = card_no;
						bankUser.accountName = user.realityName;
						bankUser.addBankAccount(error);
					}
				}
			}

		}
		// 版本号
		args.put("version", Constants.LL_WEB_PAY_VERSION);
		// 交易支付的商户编号
		args.put("oid_partner", Constants.LL_PAY_OID_PARTNER);
		// 商户平台用户id,
		args.put("user_id", user.id + "");
		// args.put("user_id", user.mobile+"");
		// 时间戳
		args.put("timestamp", DateUtil.simple2(new Date()));
		// 签名方式
		args.put("sign_type", Constants.LL_PAY_SIGN_TYPE);
		// 商户业务类型
		args.put("busi_partner", "101001");
		// 商户唯一订单
		args.put("no_order", merOrderNum);
		// 商户订单时间
		args.put("dt_order", DateUtil.simple2(new Date()));
		// 商品名称
		args.put("name_goods", "充值");
		// 订单描述
		// args.put("info_order", "您正在上海正田平台进行账户充值，充值金额为:"+money+"元;");
		// 交易金额
		args.put("money_order", currentNumberFormat.format(money) + "");
		// 异步通知地址
		args.put("notify_url", Constants.LL_WEB_PAY_NOTIFY_URL);
		// 同步回调地址
		args.put("url_return", Constants.LL_WEB_PAY_RETURN_URL);
		// 用户ip地址
		args.put("userreq_ip", LLPayUtil.getIpAddr(Request.current()));
		// 指定银行编号
		args.put("bank_code", "0" + Constants.GO_CODE[bankType]);
		// 支付方式--借记卡
		if (rechargeType == 1) {
			args.put("pay_type", "D");
		} else {
			args.put("pay_type", "1");
		}
		if (rechargeType == 1) {

			// 风控参数
			args.put("risk_item", createRiskItem(user));

			// 证件类型
			args.put("id_type", "0");
			// 证件号码
			args.put("id_no", user.idNumber);
			// 银行账号姓名
			args.put("acct_name", user.realityName);
			// 设置修改标记 -- 不可以修改
			// args.put("flag_modify", "1");
		}

		if (rechargeType == 1) {

			args.put("card_no", card_no);
		}
		// 加签名
		String sign = LLPayUtil.addSign(JSON.parseObject(JSON.toJSONString(args)), Constants.LL_PAY_PRIMARY_KEY,
				Constants.LL_PAY_MD5_KEY);

		args.put("sign", sign);

		if (rechargeType == 1) {

			// 认证请求服务地址
			args.put("req_url", Constants.LL_AUTH_PAY_SERVICE_URL);
		} else {

			// 网银请求的服务地址
			args.put("req_url", Constants.LL_WEB_PAY_SERVICE_URL);
		}

		Logger.info("*****************************************打印连连网银充值参数开始*************");
		Logger.info(args.toString());
		Logger.info("*****************************************打印连连网银充值参数结束*************");

		if (rechargeType == 1) {

			// 认证充值前操作
			sequence(user.id, Constants.LL_GATEWAY, merOrderNum, money.doubleValue(), Constants.GATEWAY_RECHARGE, client, card_no,
					error);

		} else {

			// 网银充值前操作
			sequence(Constants.LL_GATEWAY, merOrderNum, money.doubleValue(), Constants.GATEWAY_RECHARGE, client, error);
		}

		if (error.code < 0) {

			return null;
		}
		return args;
	}

	/**
	 * 连连支付构造风控参数 2015年12月12日 desctription
	 */
	public static String createRiskItem(User user) {
		JSONObject riskItemObj = new JSONObject();
		riskItemObj.put("user_info_full_name", user.realityName);
		riskItemObj.put("frms_ware_category", "2009");
		riskItemObj.put("user_info_mercht_userno", user.id);
		riskItemObj.put("user_info_dt_register", DateUtil.simple2(user.time));
		riskItemObj.put("user_info_id_no", user.idNumber);

		riskItemObj.put("user_info_bind_phone", user.mobile);

		riskItemObj.put("user_info_identify_state", "1");
		riskItemObj.put("user_info_identify_type", "1");

		return riskItemObj.toString();
	}

	/**
	 * 国付宝掉单处理
	 * 
	 * @param uid
	 * @param payNumber
	 * @param user_recharge
	 * @param error
	 */
	public static void goffSingle(String payNumber, t_user_recharge_details user_recharge, ErrorInfo error) {
		String uid = user_recharge.user_id + "";
		String url = Constants.GO_URL;
		t_dict_payment_gateways gateway = User.gateway(Constants.GO_GATEWAY, error);
		// 交易代码
		String tranCode = Constants.TRAN_QUERY_CODE;

		String merchantID = gateway.pid;
		String merOrderNum = NumberUtil.getBillNo(uid); // 订单号 ---- 支付流水号
		String orgOrderNum = payNumber; // 原订单号
		String tranIP = GopayUtils.getIpAddr(Request.current());
		String tranDateTime = DateUtil.simple2(new Date());
		String currencyType = Constants.CURRENCYTYPE;
		String tranAmt = user_recharge.amount + "";
		String virCardNoIn = gateway.account;
		String VerficationCode = gateway._key;
		String orgtranDateTime = user_recharge.time + "";
		orgtranDateTime = orgtranDateTime.replace("-", "").replace(" ", "").replace(":", "");
		orgtranDateTime = orgtranDateTime.substring(0, 14);

		StringBuffer plain = new StringBuffer();
		plain.append("tranCode=[");
		plain.append(tranCode);
		plain.append("]merchantID=[");
		plain.append(merchantID);
		plain.append("]merOrderNum=[");
		plain.append(merOrderNum);
		plain.append("]tranAmt=[");
		plain.append(tranAmt);
		plain.append("]ticketAmt=[]");
		plain.append("tranDateTime=[");
		plain.append(tranDateTime);
		plain.append("]currencyType=[");
		plain.append(currencyType);
		plain.append("]merURL=[");
		plain.append("]customerEMail=[");
		plain.append("]authID=[]orgOrderNum=[");
		plain.append(orgOrderNum);
		plain.append("]orgtranDateTime=[");
		plain.append(orgtranDateTime);
		plain.append("]orgtranAmt=[]orgTxnType=[");
		plain.append("]orgTxnStat=[]msgExt=[");
		plain.append("]virCardNo=[]virCardNoIn=[");
		plain.append(virCardNoIn);
		plain.append("]tranIP=[");
		plain.append(tranIP);
		plain.append("]isLocked=[");
		plain.append("]feeAmt=[0]respCode=[");
		plain.append("]VerficationCode=[");
		plain.append(VerficationCode);
		plain.append("]");
		String signValue = GopayUtils.md5(plain.toString());
		GopayUtils.validateQuerySign(tranCode, merchantID, merOrderNum, tranAmt, "", currencyType, "", tranDateTime, "", "",
				virCardNoIn, tranIP, "", "", orgtranDateTime, orgOrderNum, "", "", "", "", "", VerficationCode, signValue);

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("tranCode", tranCode);
		args.put("merchantID", merchantID);
		args.put("merOrderNum", merOrderNum);
		args.put("tranAmt", tranAmt);
		args.put("ticketAmt", "");
		args.put("tranDateTime", tranDateTime);
		args.put("currencyType", currencyType);
		args.put("merURL", "");
		args.put("customerEMail", "");
		args.put("authID", "");
		args.put("orgOrderNum", orgOrderNum);
		args.put("orgtranDateTime", orgtranDateTime);
		args.put("orgtranAmt", "");
		args.put("orgTxnType", "");
		args.put("orgTxnStat", "");
		args.put("msgExt", "");
		args.put("virCardNo", "");
		args.put("virCardNoIn", virCardNoIn);
		args.put("tranIP", tranIP);
		args.put("isLocked", "");
		args.put("feeAmt", "0");
		args.put("respCode", "");
		args.put("VerficationCode", VerficationCode);
		args.put("signValue", signValue);

		// 调用国付宝订单查询接口
		String strXML;

		try {
			strXML = WS.url(url).params(args).get().getString();
		} catch (Exception e) {
			Logger.info("调用国付宝订单查询接口:" + e.getMessage());
			error.code = -1;
			error.msg = "调用国付宝订单查询接口出现异常，补单失败！";

			return;
		}

		if (StringUtils.isBlank(strXML)) {
			Logger.error("调用国付宝订单查询接口返回null");

			error.code = -1;
			error.msg = "调用国付宝订单查询接口返回空串，补单失败！";

			return;
		}

		Logger.info("------------------strXML:-------------------" + strXML);

		JSONObject result = (JSONObject) Converter.xmlToObj(strXML);

		String respCode = result.getString("respCode");
		tranCode = result.getString("tranCode");
		merchantID = result.getString("merchantID");
		merOrderNum = result.getString("merOrderNum");
		tranAmt = result.getString("tranAmt");
		String feeAmt = result.getString("feeAmt");
		currencyType = result.getString("currencyType");
		String merURL = result.getString("merURL");
		tranDateTime = result.getString("tranDateTime");
		String customerEMail = result.getString("customerEMail");
		String virCardNo = result.getString("virCardNo");
		virCardNoIn = result.getString("virCardNoIn");
		tranIP = result.getString("tranIP");
		String msgExt = result.getString("msgExt");
		orgtranDateTime = result.getString("orgtranDateTime");
		String orgTxnStat = result.getString("orgTxnStat");
		String orgTxnType = result.getString("orgTxnType");
		String orgtranAmt = result.getString("orgtranAmt");
		orgOrderNum = result.getString("orgOrderNum");
		String authID = result.getString("authID");
		String isLocked = result.getString("isLocked");
		String signVal = result.getString("signValue");

		if (GopayUtils.validateQuerySign(tranCode, merchantID, merOrderNum, tranAmt, feeAmt, currencyType, merURL, tranDateTime,
				customerEMail, virCardNo, virCardNoIn, tranIP, msgExt, respCode, orgtranDateTime, orgOrderNum, orgtranAmt,
				orgTxnType, orgTxnStat, authID, isLocked, gateway._key, signVal)) {
			error.code = -1;
			error.msg = "验证失败，支付失败！";
		}

		if (!"0000".equals(respCode)) {
			error.code = -2;

			if ("ST01".equals(respCode)) {
				error.msg = "补单失败！(原订单不存在)";
			} else if ("ST10".equals(respCode)) {
				error.msg = "补单失败！(交易重复，订单号已存在)";
			} else {
				error.msg = "补单失败！(" + respCode + ")";
			}
			return;
		}

		if (!"0000".equals(orgTxnStat)) {
			error.code = -3;
			error.msg = "交易未成功订单，不可补单！(" + orgTxnStat + ")";
			return;
		}

		if (!payNumber.equals(orgOrderNum)) {
			error.code = -4;
			error.msg = "订单号与交易订单号不匹配！";
			return;
		}

		User.recharge(payNumber, Double.parseDouble(tranAmt), error);

		if (error.code < 0) {
			error.code = -4;
			return;
		}

		error.code = 1;
		error.msg = "补单成功";
	}

	/**
	 * 查询是否已绑定QQ
	 * 
	 * @param qqKey
	 * @param error
	 * @return
	 */
	public static boolean isBindedQQ(String qqKey, ErrorInfo error) {
		error.clear();

		long count = -1;

		try {
			// 调用构造方法t_users(String name, String email),该处实际上将密码存储在email中
			t_users usr = t_users.find(" select new t_users(name,password) from t_users where (qq_key = ?)", qqKey).first();
			if (null != usr) {
				User user = new User();
				user.name = usr.name;
				count = user.login(usr.email, true, Constants.CLIENT_PC, error);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("QQ绑定信息 ：" + e.getMessage());
			error.code = -1;
			error.msg = "QQ绑定新查询失败";

			return false;
		}
		if (error.code < 0) {
			return false;
		}
		if (count < 0) {
			error.code = 1;
			error.msg = "QQ未进行绑定";

			return false;
		}

		error.code = 1;

		return true;
	}

	/**
	 * 查询是否绑定微博
	 * 
	 * @param weiboKey
	 * @param error
	 * @return
	 */
	public static boolean isBindedWEIBO(String weiboKey, ErrorInfo error) {
		error.clear();

		long count = -1;

		try {
			// 调用构造方法t_users(String name, String email),该处实际上将密码存储在email中
			t_users usr = t_users.find(" select new t_users(name,password) from t_users where (weibo_key = ?)", weiboKey).first();
			if (null != usr) {
				User user = new User();
				user.name = usr.name;
				count = user.login(usr.email, true, Constants.CLIENT_PC, error);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("微博绑定信息 ：" + e.getMessage());
			error.code = -1;
			error.msg = "微博绑定新查询失败";

			return false;
		}
		if (error.code < 0) {
			return false;
		}
		if (count < 0) {
			error.code = 1;
			error.msg = "微博未进行绑定";

			return false;
		}

		error.code = 1;

		return true;
	}

	public int bindingQQ(ErrorInfo error) {
		error.clear();

		EntityManager em = JPA.em();
		Query query = em.createQuery("update t_users set qq_key = ? where id = ?").setParameter(1, this.qqKey).setParameter(2,
				this.id);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("绑定QQ登录信息：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次绑定QQ信息保存失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		error.code = 1;

		return error.code;
	}

	public int bindingWEIBO(ErrorInfo error) {
		error.clear();

		EntityManager em = JPA.em();
		Query query = em.createQuery("update t_users set weibo_key = ? where id = ?").setParameter(1, this.weiboKey)
				.setParameter(2, this.id);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("绑定微博登录信息：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次绑定微博信息保存失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		error.code = 1;

		return error.code;
	}

	public int isOverdue; // 是否有过逾期

	/**
	 * 用户是否是否有过逾期
	 */
	public int getIsOverdue() {
		String sql = "select u.id from t_users u JOIN t_bids b ON u.id = b.id JOIN t_bills bill ON bill.bid_id = b.id and u.id = ? and b.`status` IN(?, ?) and bill.overdue_mark IN(?, ?, ?)";

		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, this.id);
		query.setParameter(2, Constants.BID_REPAYMENT);
		query.setParameter(3, Constants.BID_REPAYMENTS);
		query.setParameter(4, Constants.BILL_NORMAL_OVERDUE);
		query.setParameter(5, Constants.BILL_OVERDUE);
		query.setParameter(6, Constants.BILL_BAD_DEBTS);
		List<?> row = null;

		try {
			row = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("用户是否是否有过逾期：" + e.getMessage());

			return 0;
		}

		return row == null || row.size() == 0 ? 0 : 1;
	}

	/**
	 * 更新IPS账号信息
	 * 
	 * @param error
	 */
	public void updateIpsAcctNo(long userId, String pIpsAcctNo, ErrorInfo error) {
		error.clear();

		String sql = "update t_users set ips_acct_no = ? where id= ?";
		int rows = 0;

		try {
			rows = JpaHelper.execute(sql, pIpsAcctNo, userId).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("添加IPS账号时：" + e.getMessage());

			error.code = -1;
			error.msg = "添加IPS失败";

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return;
		}

		DealDetail.userEvent(userId, UserEvent.IPS_ACCT_NO, "开户成功", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		this.ipsAcctNo = pIpsAcctNo;
		setCurrUser(this);

		error.code = 0;
		error.msg = "开户成功！";
	}

	/**
	 * 更新自动投标授权号信息
	 * 
	 * @param error
	 */
	public void updateIpsBidAuthNo(long userId, ErrorInfo error) {
		error.clear();

		String sql = "update t_users set ips_bid_auth_no = ? where id= ?";
		int rows = 0;

		try {
			rows = JpaHelper.execute(sql, this.ipsBidAuthNo, userId).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("添加自动投标授权号时：" + e.getMessage());

			error.code = -1;
			error.msg = "添加自动投标授权号失败";

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return;
		}

		sql = "update t_user_automatic_invest_options set status = true where user_id = ?";
		rows = 0;

		try {
			rows = JpaHelper.execute(sql, userId).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("开启自动投标时：" + e.getMessage());

			error.code = -1;
			error.msg = "开启自动投标失败";

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return;
		}

		DealDetail.userEvent(userId, UserEvent.IPS_BID_AUTH_NO, "添加自动投标授权号", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		User user = User.currUser();

		if (user != null) {
			user.ipsBidAuthNo = this.ipsBidAuthNo;

			setCurrUser(user);
		}

		error.code = 0;
		error.msg = "添加自动投标授权号成功！";
	}

	/**
	 * 更新自动还款授权号信息
	 * 
	 * @param error
	 */
	public void updateIpsRepayAuthNo(long userId, ErrorInfo error) {
		error.clear();

		String sql = "update t_users set ips_repay_auth_no = ? where id= ?";
		int rows = 0;

		try {
			rows = JpaHelper.execute(sql, this.ipsRepayAuthNo, userId).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("添加自动还款授权号时：" + e.getMessage());

			error.code = -1;
			error.msg = "添加自动还款授权号失败";

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return;
		}

		DealDetail.userEvent(userId, UserEvent.IPS_REPAY_AUTH_NO, "添加自动还款授权号", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		User user = User.currUser();

		if (user != null) {
			user.ipsBidAuthNo = this.ipsBidAuthNo;

			setCurrUser(user);
		}

		error.code = 0;
		error.msg = "添加自动还款授权号成功！";
	}

	/**
	 * 根据当前用户ID计算产生的CPS推广费
	 * 
	 * @param userId
	 * @param error
	 * @param managefee  管理费
	 * @param relationId 投资ID或者借款ID
	 */
	@Deprecated
	public static void rewardCPS(long userId, double managefee, long relationId, ErrorInfo error) {

		if (userId > 0)
			return;

		Long recommendUserId = 0l;// 推荐用户
		Integer CPStype = 0;// 奖励方式
		Calendar cal = Calendar.getInstance();// 使用日历类
		int year = cal.get(Calendar.YEAR);// 得到年
		int month = cal.get(Calendar.MONTH) + 1;// 得到月，因为从0开始的，所以要加1

		try {
			recommendUserId = t_users.find(" select recommend_user_id from t_users where id = ? ", userId).first();
			CPStype = t_users.find(" select recommend_reward_type from t_users where id = ? ", userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "查询当前用户的推荐者异常";

			return;
		}

		if (recommendUserId == null || recommendUserId == 0) {
			error.code = 3;
			error.msg = "该用户没有被任何会员推荐";

			return;
		}

		// 推广规则：1:-按会员数
		if (CPStype == null) {
			error.code = -2;
			error.msg = "查询当前用户的推荐奖励方式异常";

			return;
		}
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		// 查询当前CPS推广费设置规则
		double rewardForCounts = backstageSet.rewardForCounts;// 按会员数，每个的金额
		if (CPStype == Constants.CPSREWARDTYPE_USER_COUNT) {// 1:-按会员数

			if (User.isActiveUser(userId, error) >= 0) {// 已被奖励
				error.msg = "已被奖励";
				error.code = 2;
				return;
			}
			saveOrUpdateCPSIncome(year, month, recommendUserId, userId, rewardForCounts, error);
		}

		// 按理财收益分成
		else if (CPStype == Constants.CPSREWARDTYPE_USER_PROFIT) {

		}

		User.updateActive(userId, error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return;
		}
		return;
	}

	/**
	 * 
	 * @author liulj @creationDate. 2016年9月28日 下午4:53:38 @description.
	 *         查询所有通过cps推广注册的用户
	 * 
	 * @return
	 */
	public static List<t_users> cpsUsers() {
		// BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		// backstageSet.cpsRewardType;
		// List<t_users> users = t_users.find("select new
		// t_users(id,name,mobile,recommend_user_id,recommend_reward_type,recommend_time)
		// from t_users where recommend_reward_type = ? and recommend_user_id > 0",
		// Constants.CPSREWARDTYPE_USER_PROFIT).fetch();
		List<t_users> users = t_users.find(
				"select new t_users(id,name,mobile,recommend_user_id,recommend_reward_type,recommend_time) from t_users where recommend_user_id > 0")
				.fetch();
		return users;
	}

	public int getIpsStatus() {
		if (StringUtils.isNotBlank(this.ipsAcctNo)) {
			return IpsCheckStatus.IPS;
		}

		if (this.isMobileVerified && this.isEmailVerified && StringUtils.isNotBlank(this.realityName)
				&& StringUtils.isNotBlank(this.idNumber)) {
			return IpsCheckStatus.MOBILE;
		}

		if (StringUtils.isNotBlank(this.realityName) && StringUtils.isNotBlank(this.idNumber) && this.isEmailVerified) {
			return IpsCheckStatus.REAL_NAME;
		}

		if (this.isEmailVerified) {
			return IpsCheckStatus.EMAIL;
		}

		return IpsCheckStatus.NONE;
	}

	/**
	 * 保存或更新用户推广收入表
	 * 
	 * @param year
	 * @param month
	 * @param userId
	 * @param cpsReward
	 * @param error
	 */
	public static void saveOrUpdateCPSIncome(int year, int month, long userId, long recommendUserId, double cpsReward,
			ErrorInfo error) {

		t_user_cps_income cpsIncome = null;
		long spread_user_account = -1;
		long effective_user_account = -1;

		try {
			cpsIncome = t_user_cps_income.find(" year = ? and month = ? and user_id = ? and recommend_user_id =? ", year, month,
					userId, recommendUserId).first();
			spread_user_account = t_users.find("select count(id) from t_users where recommend_user_id = ? ", userId).first();
			effective_user_account = t_users
					.find("select count(id) from t_users where recommend_user_id = ? and is_active = 1", userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -2;
			error.msg = "查询当前用户的推荐奖励收入异常";

			return;
		}

		if (null == cpsIncome) {
			cpsIncome = new t_user_cps_income();
			cpsIncome.year = year;
			cpsIncome.month = month;
			cpsIncome.user_id = userId;
			cpsIncome.recommend_user_id = recommendUserId;
			cpsIncome.spread_user_account = 1;
			cpsIncome.effective_user_account = 1;
			cpsIncome.invalid_user_account = 0;
			cpsIncome.cps_reward = cpsReward;
		} else {
			cpsIncome.spread_user_account = spread_user_account;
			cpsIncome.effective_user_account = effective_user_account;
			cpsIncome.invalid_user_account = spread_user_account - effective_user_account;
			cpsIncome.cps_reward = cpsIncome.cps_reward + cpsReward;
		}

		try {
			cpsIncome.save();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -3;
			error.msg = "保存当前用户的推荐奖励收入异常";

			return;
		}

		error.code = 0;

	}

	/**
	 * 保存app端userId和channelId,用于推送
	 * 
	 * @param userId
	 * @param channelId
	 * @param deviceType
	 * @param error
	 */
	public void updateChannel(String userId, String channelId, int deviceType, ErrorInfo error) {
		error.clear();

		try {
			JPA.em().createQuery("update t_users set device_user_id = ?, channel_id = ?, device_type = ? where id = ?")
					.setParameter(1, userId).setParameter(2, channelId).setParameter(3, deviceType).setParameter(4, this.id)
					.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("保存百度云推送信息时：" + e.getMessage());
			error.code = -2;
			error.msg = "保存百度云推送信息失败";

			return;
		}
	}

	/**
	 * 保存推送设置
	 * 
	 * @param userId
	 * @param error
	 */
	public void pushSetting(long userId, ErrorInfo error) {
		error.clear();

		String sql = "update t_users set is_bill_push = ?, is_invest_push = ?, is_activity_push = ? where id = ?";

		try {
			JPA.em().createQuery(sql).setParameter(1, isBillPush).setParameter(2, isInvestPush).setParameter(3, isActivityPush)
					.setParameter(4, userId).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("保存推送设置失败：" + e.getMessage());
			error.code = -2;
			error.msg = "保存推送设置失败";

			return;
		}

		error.code = 0;
		error.msg = "推送设置保存成功";
	}

	/**
	 * 获取推送推送设置
	 * 
	 * @param userId
	 * @param error
	 */
	public static t_users queryPushSetting(long userId, ErrorInfo error) {
		error.clear();

		String sql = "select new t_users(id, device_user_id, channel_id, device_type, is_bill_push, is_invest_push, is_activity_push) from t_users where id = ?";
		t_users user = null;

		try {
			user = t_users.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("获取推送设置失败：" + e.getMessage());
			error.code = -2;
			error.msg = "获取推送设置失败";

			return null;
		}

		error.code = 0;

		return user;
	}

	/**
	 * 根据用户id获取第三方支付账号
	 * 
	 * @param userId
	 * @param error
	 * @return
	 */
	public static String queryIpsAcctNo(long userId, ErrorInfo error) {
		error.clear();

		String sql = "select ips_acct_no from t_users where id = ? limit 1";

		Object ipsAcctNo = null;

		try {
			ipsAcctNo = JPA.em().createNativeQuery(sql).setParameter(1, userId).getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("根据用户id获取第三方支付账号时：" + e.getMessage());

			error.code = -1;
			error.msg = "根据用户id获取第三方支付账号失败";

			return null;
		}

		return ipsAcctNo == null ? "" : ipsAcctNo.toString();
	}

	/**
	 * 根据用户名获取第三方支付账号
	 * 
	 * @param userId
	 * @param error
	 * @return
	 */
	public static String queryIpsAcctNoByName(String name, ErrorInfo error) {
		error.clear();

		String sql = "select ips_acct_no from t_users where name = ? limit 1";

		Object ipsAcctNo = null;

		try {
			ipsAcctNo = JPA.em().createNativeQuery(sql).setParameter(1, name).getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("根据用户id获取第三方支付账号时：" + e.getMessage());

			error.code = -1;
			error.msg = "根据用户id获取第三方支付账号失败";

			return null;
		}

		return ipsAcctNo == null ? "" : ipsAcctNo.toString();
	}

	/**
	 * 获取ips账号
	 * 
	 * @return
	 */
	public String getIpsAcctNo() {
		if (null == this.ipsAcctNo) {
			String sql = "select ips_acct_no from t_users where id = ? limit 1";

			try {
				this.ipsAcctNo = JPA.em().createNativeQuery(sql).setParameter(1, this.id).getResultList().get(0).toString();
			} catch (Exception e) {
				Logger.info("根据用户id获取第三方支付账号时：" + e.getMessage());

				return null;
			}
		}

		return this.ipsAcctNo;
	}

	/**
	 * 查询userId
	 * 
	 * @param ipsAcctNo
	 * @return
	 */
	public static long queryUserId(String ipsAcctNo, ErrorInfo error) {
		error.clear();
		Long userId = null;

		try {
			userId = t_users.find("select id from t_users where ips_acct_no = ?", ipsAcctNo).first();
		} catch (Exception e) {
			Logger.info(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (userId == null) {
			error.code = -1;
			error.msg = "用户不存在";

			return error.code;
		}

		return userId;
	}

	/**
	 * 根据用户id获取第三方自动投标签约号
	 * 
	 * @param userId
	 * @param error
	 * @return
	 */
	public static String queryIpsBidAuthNo(long userId, ErrorInfo error) {
		error.clear();

		String sql = "select ips_bid_auth_no from t_users where id = ?";

		String ipsAcctNo = null;

		try {
			ipsAcctNo = t_users.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("根据用户id获取第三方自动投标签约号时：" + e.getMessage());

			error.code = -1;
			error.msg = "根据用户id获取第三方自动投标签约号失败";

			return null;
		}

		return ipsAcctNo;
	}

	/**
	 * 查询用户信息供投资使用
	 * 
	 * @param userId
	 * @param error
	 * @return
	 */
	public static t_users queryUserforInvest(long userId, ErrorInfo error) {
		error.clear();

		String sql = "select id ,name , balance ,is_blacklist ,pay_password, master_identity, mobile from t_users  where id = ? ";
		Object[] obj = null;

		try {
			obj = (Object[]) JPA.em().createNativeQuery(sql).setParameter(1, userId).getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起！系统异常！请您联系平台管理员！";
			error.code = -2;

			return null;
		}

		if (null == obj) {
			error.msg = "对不起！系统异常！请您联系平台管理员！";
			error.code = -2;

			return null;
		}

		t_users user = new t_users();

		user.id = null == obj[0] ? 0 : Long.parseLong(obj[0].toString());
		user.name = null == obj[1] ? "" : obj[1].toString();
		user.balance = null == obj[2] ? 0 : Double.parseDouble(obj[2].toString());
		user.is_blacklist = null == obj[3] ? false : Boolean.parseBoolean(obj[3].toString());
		user.pay_password = null == obj[4] ? "" : obj[4].toString();
		user.master_identity = null == obj[5] ? 0 : ((Byte) obj[5]).intValue();
		user.mobile = null == obj[6] ? "" : obj[6].toString();
		error.code = 1;

		return user;
	}

	/**
	 * 查询用户信息供投资使用
	 * 
	 * @param userId
	 * @param error
	 * @return
	 */
	public static User queryUserforIPS(long userId, ErrorInfo error) {
		error.clear();

		String sql = "select name , id_number ,reality_name ,ips_acct_no, mobile, ips_bid_auth_no from t_users  where id = ? ";
		Object[] obj = null;

		try {
			obj = (Object[]) JPA.em().createNativeQuery(sql).setParameter(1, userId).getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起！系统异常！请您联系平台管理员！";
			error.code = -2;

			return null;
		}

		if (null == obj) {
			error.msg = "对不起！系统异常！请您联系平台管理员！";
			error.code = -2;

			return null;
		}

		User user = new User();
		user._id = userId;

		user.isQueryName = false;

		user.name = obj[0].toString();
		user.idNumber = obj[1] == null ? "" : obj[1].toString();
		user.realityName = obj[2] == null ? "" : obj[2].toString();
		user.ipsAcctNo = obj[3] == null ? "" : obj[3].toString();
		user.mobile = obj[4] == null ? "" : obj[4].toString();
		user.ipsBidAuthNo = obj[5] == null ? "" : obj[5].toString();

		error.code = 1;

		return user;
	}

	/**
	 * 获取用户信用等级名称
	 * 
	 * @param image
	 */
	public static String queryCreditLevelByImage(String image) {
		String hql = "select name from t_credit_levels  where image_filename=?";
		String name = image;

		try {
			name = t_credit_levels.find(hql, image).first();
		} catch (Exception e) {
			Logger.error("用户->获取用户信用等级名称:" + e.getMessage());

			return image;
		}

		return name;
	}

	/**
	 * 查询用户的推广收入
	 * 
	 * @param userId
	 * @return
	 */
	public static double queryTotalCpsIncome(long userId) {
		String hql = "select sum(cps_reward_ed) from t_user_cps_income where user_id = ?";
		Double income = null;

		try {
			income = t_user_cps_income.find(hql, userId).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());

			return 0;
		}

		return income == null ? 0 : income;
	}

	/* 微信业务 */
	/**
	 * 绑定用户，一个sp2p账号，只能被一个微信号绑定，因此是一对一的。
	 * 
	 * @param account
	 * @param password
	 * @param openId
	 * @param gongzhongId
	 * @param error
	 * @return
	 */
	public static void bindUser(String account, String password, String openId, ErrorInfo error) {
		// 密码传入为明文进行加密处理
		password = Encrypt.MD5(password + Constants.ENCRYPTION_KEY);
		/**
		 * 判断sp2p用户是否存在
		 */
		List list = t_users.find("name = ? and password = ?", account, password).fetch();

		if (null != list && list.size() > 0) {
			// 存在用户则进行绑定
			t_users user = (t_users) list.get(0);
			String open_id = user.open_id;

			if (null != open_id && !"".equals(open_id)) {
				// 说明已经绑定过，因此不能再次绑定
				error.code = -1;
				error.msg = "该账号已经被绑定了，多个微信号不能绑定一个账号";

				return;
			}

			// 为空，则更改open_id字段
			user.open_id = openId;
			try {
				user.save();
			} catch (Exception e) {
				JPA.setRollbackOnly();
				Logger.error("保存微信用户时%s", e.getMessage());

				error.code = -2;
				error.msg = "绑定账号时异常";

				return;
			}

			error.code = 1;
			error.msg = "绑定成功";
		} else {
			error.code = -2;
			error.msg = "用户名不存在或者密码错误";

			return;
		}

	}

	/**
	 * 解绑用户
	 * 
	 * @param openId
	 * @param error
	 */
	public static void unBoundUser(String account, String password, String openId, ErrorInfo error) {
		// 密码传入为明文进行加密处理
		password = Encrypt.MD5(password + Constants.ENCRYPTION_KEY);
		/**
		 * 判断sp2p用户是否存在
		 */
		List list = t_users.find("name = ? and password = ?", account, password).fetch();

		if (null != list && list.size() > 0) {
			// 存在用户且openId不为null且等于openId则进行解绑
			t_users user = (t_users) list.get(0);
			String open_id = user.open_id;

			if (null != open_id && open_id.equals(openId)) {
				user.open_id = null;
				try {
					user.save();
				} catch (Exception e) {
					JPA.setRollbackOnly();
					Logger.error("解绑微信用户时%s", e.getMessage());

					error.code = -1;
					error.msg = "绑定账号时异常";

					return;
				}

				error.code = 1;
				error.msg = "解绑成功";
				return;
			}

			if (null == open_id) {
				error.code = -2;
				error.msg = "您尚未绑定账号";
				return;
			}

		} else {
			error.code = -3;
			error.msg = "用户名不存在或者密码错误";

			return;
		}
	}

	/**
	 * 是否绑定账号
	 * 
	 * @param openId
	 * @param error
	 * @return
	 */
	public static boolean isBind(String openId, ErrorInfo error) {
		try {
			List list = t_users.find("open_id = ?", openId).fetch();
			// 如果绑定，则提醒用户
			if (null != list && list.size() > 0) {
				error.code = 1;
				error.msg = "您的微信号已经绑定过账号了，不能再次绑定！";

				return true;
			}
		} catch (Exception e) {
			Logger.error("查询用户是否绑定时%s", e.getMessage());

			error.code = -1;
			error.msg = "查询用户是否绑定时异常";

			return false;
		}

		return false;

	}

	/**
	 * 根据openId查询用户账号密码
	 * 
	 * @param openId
	 * @param error
	 * @return
	 */
	public static Map<String, String> findAccountAndPasswordByOpenId(String openId, ErrorInfo error) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			List<t_users> list = t_users.find("open_id = ?", openId).fetch();
			if (null != list && list.size() > 0) {
				t_users user = list.get(0);
				map.put("account", user.name);
				map.put("password", user.password);
				error.code = 1;

				return map;
			}
		} catch (Exception e) {
			Logger.info("查询用户信息时%s", e.getMessage());
			error.code = -1;
			error.msg = "查询用户时异常";

			return null;
		}

		error.code = -2;
		error.msg = "您微信号没有绑定账号";

		return null;
	}

	/**
	 * 根据openId得到用户基本信息
	 * 
	 * @param name
	 * @return
	 */
	public static User getUserInformation(String openId, ErrorInfo error) {
		error.clear();

		List<t_users> list = t_users.find("open_id", openId).fetch();
		if (null != list && list.size() > 0) {
			t_users t_user = list.get(0);

			User user = new User();
			user.name = t_user.name;
			error.code = 1;

			return user;

		}

		error.code = -1;
		error.msg = "微信用户尚未绑定账号";

		return null;
	}

	/**
	 * 查询用户开户请求信息
	 * 
	 * @param error
	 * @param userId
	 * @return
	 */
	public static Map<String, Object> queryIpsInfo(ErrorInfo error, long userId) {
		return JPAUtil.getMap(error, "select ips_acct_no,reality_name,id_number,email,mobile from t_users where id = ?", userId);
	}

	/**
	 * 更新用户开户请求信息
	 * 
	 * @param error
	 * @param userId
	 * @return
	 */
	public static int updateIpsInfo(ErrorInfo error, long userId, String realityName, String idNumber, String email,
			String mobile, int financeType) {
		error.clear();

		User user = new User();
		user.id = userId;

		if (StringUtils.isBlank(realityName)) {
			error.code = -1;
			error.msg = "真实姓名不能为空";

			return error.code;
		}

		if (!CharUtil.isChinese(realityName)) {
			error.code = -1;
			error.msg = "真实姓名只能为汉字";

			return error.code;
		}

		if (StringUtils.isBlank(idNumber) || !"".equals(IDCardValidate.chekIdCard(0, idNumber))) {
			error.code = -1;
			error.msg = "请输入正确的身份证号码";

			return error.code;
		}

		// 判断身份证号码是否已经被其他人注册
		User.isIDNumberExist(idNumber, user.idNumber, financeType, error);
		if (error.code < 0) {

			return error.code;
		}

		if (StringUtils.isBlank(email) || !RegexUtils.isEmail(email)) {
			error.code = -1;
			error.msg = "请输入正确的邮箱";

			return error.code;
		}

		// 判断邮箱是否已经被其他人注册
		User.isEmailExist(email, user.email, error);
		if (error.code < 0) {

			return error.code;
		}

		if (StringUtils.isBlank(mobile) || !RegexUtils.isMobileNum(mobile)) {
			error.code = -1;
			error.msg = "请输入正确的手机号码";

			return error.code;
		}

		// 判断手机是否已经被其他人注册
		User.isMobileExist(mobile, user.mobile, error);
		if (error.code < 0) {

			return error.code;
		}

		return JPAUtil.executeUpdate(error, "update t_users set reality_name=?,id_number=?,email=?,mobile=? where id = ?",
				realityName, idNumber, email, mobile, userId);
	}

	/**
	 * 查询已激活且非黑名单用户
	 * 
	 * @param key
	 * @param orderTypeStr
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @return
	 */
	public static PageBean<v_user_info> queryActiveUser(String name, String email, String beginTimeStr, String endTimeStr,
			String key, String orderTypeStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();
		Date beginTime = null;
		Date endTime = null;
		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (orderType < 0 || orderType > 14) {
			orderType = 0;
		}

		StringBuffer sql = new StringBuffer();
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_ACTIVE_V2);

		List<Object> paramsCount = new ArrayList<Object>();
		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		/* 为sql条件添加参数 */
		for (int i = 1; i <= 4; i++) {
			params.add(Constants.BID_REPAYMENT);
			params.add(Constants.BID_REPAYMENTS);
			params.add(Constants.BID_COMPENSATE_REPAYMENT);
		}

		conditionMap.put("name", name);
		conditionMap.put("email", email);
		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("key", key);
		conditionMap.put("orderType", orderType);

		if (StringUtils.isNotBlank(key)) {
			sql.append("and t_users.name like ? ");
			params.add("%" + key + "%");
			paramsCount.add("%" + key + "%");
		}

		if (StringUtils.isNotBlank(name)) {
			sql.append("and t_users.name like ? ");
			params.add("%" + name + "%");
			paramsCount.add("%" + name + "%");
		}

		if (StringUtils.isNotBlank(email)) {
			sql.append("and t_users.email like ? ");
			params.add("%" + email + "%");
			paramsCount.add("%" + email + "%");
		}

		if (beginTime != null) {
			sql.append("and t_users.time > ? ");
			params.add(beginTime);
			paramsCount.add(beginTime);
		}

		if (endTime != null) {
			sql.append("and t_users.time < ? ");
			params.add(endTime);
			paramsCount.add(endTime);
		}

		sql.append("order by " + Constants.ORDER_TYPE[orderType]);

		List<v_user_info> users = new ArrayList<v_user_info>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_info.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), paramsCount);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询所有的会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询全部会员列表时出现异常！";

			return null;
		}

		PageBean<v_user_info> page = new PageBean<v_user_info>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 查询已激活且非黑名单借款用户
	 * 
	 * @param key
	 * @param orderTypeStr
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @return
	 */
	public static PageBean<v_user_info> queryActiveBorrowUser(String name, String email, String beginTimeStr, String endTimeStr,
			String key, String orderTypeStr, String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();
		Date beginTime = null;
		Date endTime = null;
		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (orderType < 0 || orderType > 14) {
			orderType = 0;
		}

		StringBuffer sql = new StringBuffer();
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_ACTIVE_V3);

		List<Object> paramsCount = new ArrayList<Object>();
		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		/* 为sql条件添加参数 */
		for (int i = 1; i <= 4; i++) {
			params.add(Constants.BID_REPAYMENT);
			params.add(Constants.BID_REPAYMENTS);
			params.add(Constants.BID_COMPENSATE_REPAYMENT);
		}

		conditionMap.put("name", name);
		conditionMap.put("email", email);
		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("key", key);
		conditionMap.put("orderType", orderType);

		if (StringUtils.isNotBlank(key)) {
			sql.append("and t_users.name like ? ");
			params.add("%" + key + "%");
			paramsCount.add("%" + key + "%");
		}

		if (StringUtils.isNotBlank(name)) {
			sql.append("and t_users.name like ? ");
			params.add("%" + name + "%");
			paramsCount.add("%" + name + "%");
		}

		if (StringUtils.isNotBlank(email)) {
			sql.append("and t_users.email like ? ");
			params.add("%" + email + "%");
			paramsCount.add("%" + email + "%");
		}

		if (beginTime != null) {
			sql.append("and t_users.time > ? ");
			params.add(beginTime);
			paramsCount.add(beginTime);
		}

		if (endTime != null) {
			sql.append("and t_users.time < ? ");
			params.add(endTime);
			paramsCount.add(endTime);
		}

		sql.append("order by " + Constants.ORDER_TYPE[orderType]);

		List<v_user_info> users = new ArrayList<v_user_info>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_info.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), paramsCount);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询所有的会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询全部会员列表时出现异常！";

			return null;
		}

		PageBean<v_user_info> page = new PageBean<v_user_info>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 判断用户是否是黑名单或者未激活用户
	 * 
	 * @param userName
	 * @param info
	 * @return 1是，2否
	 */
	public static int isBlackOrUnActiveUser(String userName) {

		if (StringUtils.isBlank(userName)) {
			return 1;
		}

		String sql = "select name from t_users where ((is_email_verified = 0  and is_mobile_verified = 0)or is_blacklist = 1) and name = ?";
		String name = null;

		try {
			name = t_users.find(sql, userName).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("判断用户名是否存在时,根据用户名查询数据时：" + e.getMessage());

		}

		if (name != null && name.equalsIgnoreCase(userName)) {

			return 1;
		}

		return 2;
	}

	/**
	 * 查询所有会员的对账信息
	 * 
	 * @param name
	 * @param currPage
	 * @param pageSize
	 * @param error
	 * @return
	 */

	public static PageBean<v_user_bill> queryUserBlance(String name, int currPage, int pageSize, ErrorInfo error) {
		error.clear();

		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 5;
		}

		StringBuffer sql = new StringBuffer(
				"select id as id, name as userName, ips_acct_no as account, balance as systemBlance, freeze as systemFreeze ,'' as pBlance, '' as pFreeze, 0 as status from t_users where 1=1 and ips_acct_no is not null");
		List<Object> params = new ArrayList<Object>(0);
		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("name", name);
		if (!StringUtils.isEmpty(name)) {
			sql.append(" and name like ?");
			params = new ArrayList<Object>(1);
			params.add("%" + name + "%");
		}
		sql.append(" order by time desc");
		List<v_user_bill> users = new ArrayList<v_user_bill>(pageSize);
		int count = 0;
		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_bill.class);
			if (!StringUtils.isEmpty(name)) {
				query.setParameter(1, params.get(0));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();
			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			Logger.error("查询所有的会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询全部会员列表时出现异常！";
			return null;
		}
		if (null != users && 0 != users.size()) {
			for (v_user_bill user : users) {
				// 循环遍历去调用服务接口查询余额，可能会出问题，比如用户没有申请资金托管账号
				// 捕获异常后将余额设为空
				try {
					User u = new User();
					u.id = user.getId();
					Map<String, Object> map = PaymentProxy.getInstance().queryAmount(error, Constants.PC, u);
					if (null != map) {
						user.pBlance = Double.valueOf(map.get("pBlance").toString()); // map中pBlance不许改动，各个托管在自己实现类里面进行转化
						user.pFreeze = Double.valueOf(map.get("pFreeze").toString()); // map中pFreeze不许改动，各个托管在自己实现类里面进行转化
					}
				} catch (Exception e) {
					e.printStackTrace();
					user.pBlance = 0.00;
					user.pFreeze = 0.00;
				}

			}
		}
		PageBean<v_user_bill> page = new PageBean<v_user_bill>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 第三方客户ID号查询user信息
	 * 
	 * @date 2015-07-03
	 * @author yangxuan
	 * @param ipsAcctNo 第三方客户ID号
	 * @return
	 */
	public static t_users queryUserByIpsAcctNo(String ipsAcctNo) {
		t_users user = t_users.find("ips_acct_no = ?", ipsAcctNo).first();
		return user;
	}

	/**
	 * 提现服务费
	 * 
	 * @param pTrdAmt
	 * @return
	 */
	public static double withdrawalFee(double pTrdAmt) {
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();

		return backstageSet.withdrawBase + ((pTrdAmt - backstageSet.withdrawFee) > 0
				? Arith.round(((pTrdAmt - backstageSet.withdrawFee) * maxRate(backstageSet.withdrawRate)) / 100, 2)
				: 0);
	}

	/**
	 * 单笔交易商户收取服务费最大费率（默认）：1%
	 * 
	 * @author yaoyi version 8.1.2 date:20150908
	 * @param withdrawRate
	 * @return
	 */
	private static double maxRate(double withdrawRate) {
		return new BigDecimal(withdrawRate).compareTo(new BigDecimal(Constants.WITHDRAW_MAXRATE)) > 0 ? Constants.WITHDRAW_MAXRATE
				: withdrawRate;
	}

	/**
	 * 生成流水号(最长20位)
	 * 
	 * @param userId    (不能为负，系统行为：0)
	 * @param operation
	 * @return
	 */
	public static String createBillNo() {

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		t_sequences sequence = new t_sequences();
		sequence.save();
		return format.format(new Date()) + sequence.id + "";
	}

	/**
	 * 更新流水号状态
	 * 
	 * @param pMerBillNo
	 * @param error
	 */
	public static void updateMerNo(String pMerBillNo, ErrorInfo error) {
		error.clear();

		long merBillNo = Long.parseLong(pMerBillNo);

		String sel = "select count(1) from t_ips_sequences where p_mer_bill_no = ? and status = 1";
		String sql = "update t_ips_sequences set status = 1 where p_mer_bill_no = ? and status = 0";
		int row = 0;
		long rowSel = 0;

		try {
			rowSel = ((BigInteger) JPA.em().createNativeQuery(sel).setParameter(1, merBillNo).getSingleResult()).intValue();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("更新流水号时：" + e.getMessage());
			error.code = -1;
			error.msg = "更新流水号时失败";

			return;
		}

		if (rowSel >= 1) {
			error.code = Constants.ALREADY_RUN; // 已执行
			error.msg = "已执行";

			return;
		}

		try {
			row = JpaHelper.execute(sql).setParameter(1, merBillNo).executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("更新流水号时：" + e.getMessage());
			error.code = -1;
			error.msg = "更新流水号时失败";

			return;
		}

		if (row <= 0) {
			JPA.setRollbackOnly();
			error.code = Constants.ALREADY_RUN;
			error.msg = "已执行";

			return;
		}

		error.code = 1;
	}

	/**
	 * 发放cps 推广费
	 * 
	 * @throws Exception
	 * 
	 */
	public static void payForCps() {
		Logger.info("发放cps 推广费");
		ErrorInfo error = new ErrorInfo();
		error.clear();
		List<t_user_cps_income> cpsList = null;
		try {
			cpsList = t_user_cps_income.find(" status = ? ", Constants.ZERO).fetch();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "查询所有cps记录时链接数据库失败";
			Logger.info(error.msg);
			return;
		}

		if (cpsList == null) {
			error.code = -2;
			error.msg = "未查询所到cps记录";
			Logger.info(error.msg);

			return;
		}

		double amount = 0;
		for (int i = 0; i < cpsList.size(); i++) {

			amount = cpsList.get(i).cps_reward;
			amount = new BigDecimal(amount).setScale(4, RoundingMode.HALF_EVEN).doubleValue();
			User user = new User();
			user.id = cpsList.get(i).user_id;
			if (amount == 0 || amount < 0.01) { // 金额为0，修改状态为已发
				updateIncomeStatus(cpsList.get(i).id, 1, error);
				continue;
			}
			if (Constants.IPS_ENABLE) {
				if (StringUtils.isBlank(user.ipsAcctNo)) {
					continue;
				}
				// 资金托管cps发放, 无论是否完成，先将状态码改成处理中, 方式异常情况下，商户资金一直外流，若有问题当做异常单处理进行补单；
				updateIncomeStatus(cpsList.get(i).id, 2, error);
				// 资金托管cps发放接口, 同步不处理业务，由异步处理, cps发放状态码0:未处理; 2:处理中; 1:处理完成
				PaymentProxy.getInstance().grantCps(error, Constants.PC, amount, cpsList.get(i).id, user.id);
				continue;
			}
			// 普通网关的业务逻辑
			User.updateUserBanlance(error, amount, cpsList.get(i).id, user.id);
		}
	}

	/**
	 * 发放佣金（财富圈）
	 * 
	 */
	public static void payForInvitation() {
		ErrorInfo error = new ErrorInfo();
		error.clear();
		List<t_wealthcircle_income> weaIncomeList = null;
		try {
			weaIncomeList = t_wealthcircle_income.find("status = ? ", Constants.ZERO).fetch();
		} catch (Exception e) {
			Logger.error("查询返佣记录时，%s", e.getMessage());
			error.code = -1;
			error.msg = "查询返佣记录数据库异常";

			return;
		}

		if (weaIncomeList == null) {
			error.code = -2;
			error.msg = "未查询返佣记录";
			Logger.info(error.msg);

			return;
		}

		// 关闭自动事务
		JPAPlugin.closeTx(false);

		for (t_wealthcircle_income weaIncome : weaIncomeList) {

			// 开启返佣事务
			JPAPlugin.startTx(false);

			try {

				if (weaIncome.invite_income == 0) {// 金额为0，修改状态为已发
					updateInvitationStatus(weaIncome.id, 1, error);

					continue;
				}

				if (Constants.IPS_ENABLE) {

					String ipsAcctNo = User.queryIpsAcctNo(weaIncome.user_id, error);

					if (StringUtils.isBlank(ipsAcctNo)) { // 未开户

						continue;
					}

					// 返佣发放, 无论是否完成，先将状态码改成处理中, 防止异常情况下，商户资金一直外流，若有问题当做异常单处理进行补单；
					JPAUtil.transactionBegin();
					updateInvitationStatus(weaIncome.id, 2, error);
					JPAUtil.transactionCommit();

					// 资金托管返佣发放接口, 同步不处理业务，由异步处理, 返佣发放状态码 0:未发; 2:发放中; 1:已发
					PaymentProxy.getInstance().grantInvitation(error, Constants.PC, weaIncome.invite_income, weaIncome.id,
							ipsAcctNo);

					continue;
				}

				// 普通网关的业务逻辑
				User.addBanlanceForInvitation(error, weaIncome.invite_income, weaIncome.id, weaIncome.user_id);

			} catch (Exception e) {
				Logger.error("返佣失败：" + e.getMessage());
				continue;
			} finally {
				// 关闭自动投标事务
				JPAPlugin.closeTx(false);
			}
		}

		// 开启自动事务
		JPAPlugin.startTx(false);
	}

	/**
	 * 修改推广费的状态
	 * 
	 * @param userCpsIncomeId
	 * @param status          状态码
	 * @param error
	 */
	public static void updateIncomeStatus(long userCpsIncomeId, int status, ErrorInfo error) {

		JPAUtil.transactionBegin();
		error.clear();
		int row = 0;
		String sql = " update t_user_cps_income set status = ?,cps_reward_ed = cps_reward_ed+cps_reward, cps_reward = 0 where id = ?";
		try {
			row = JPA.em().createQuery(sql).setParameter(1, status).setParameter(2, userCpsIncomeId).executeUpdate();
		} catch (Exception e) {
			Logger.info("判断是否发放cps推广费时" + e.getMessage());
			error.code = -1;
			error.msg = "发放推广费链接数据库失败";
			return;
		}
		if (row == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "修改发放推广费状态失败!";

			return;
		}
		JPAUtil.transactionCommit();
	}

	/**
	 * 修改返佣的状态
	 * 
	 * @param userCpsIncomeId
	 * @param error
	 */
	public static void updateInvitationStatus(long inviteIncomeId, int status, ErrorInfo error) {
		error.clear();
		int row = 0;

		String sql = null;
		if (status == 1) { // 已发
			sql = "update t_wealthcircle_income set status = ?, pay_time = NOW() where id = ? and status <> ?";
		} else {
			sql = "update t_wealthcircle_income set status = ? where id = ? and status <> ?";
		}

		try {
			row = JPA.em().createNativeQuery(sql).setParameter(1, status).setParameter(2, inviteIncomeId).setParameter(3, status)
					.executeUpdate();
		} catch (Exception e) {
			Logger.error("修改返佣的状态时" + e.getMessage());
			error.code = -1;
			error.msg = "修改返佣的状态数据库异常";
			return;
		}
		if (row == 0) {
			JPA.setRollbackOnly();
			error.code = Constants.ALREADY_RUN;
			error.msg = "已执行";

			return;
		}
	}

	/**
	 * 发放推广记录修改用户账户资金
	 * 
	 * @param error
	 * @param amount          金额
	 * @param userCpsIncomeId cps发放记录表id
	 * @param userId          奖励cps的用户id
	 */
	public static void updateUserBanlance(ErrorInfo error, double amount, long userCpsIncomeId, long userId) {
		error.clear();
		User user = new User();
		user.id = userId;

		updateIncomeStatus(userCpsIncomeId, 1, error);

		DataSafety data = new DataSafety();
		data.id = user.id;

		if (!data.signCheck(error)) {
			JPA.setRollbackOnly();
			return;
		}

		DealDetail.addUserFund(user.id, amount);

		v_user_for_details forDetail = DealDetail.queryUserBalance(user.id, error);
		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		DealDetail detail = new DealDetail(user.id, DealType.CPS_RATE_COUNT, amount, userCpsIncomeId, forDetail.user_amount,
				forDetail.freeze, forDetail.receive_amount, "收入cps推广费用");
		detail.addDealDetail(error);
		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		// 添加CPS推广费交易记录
		DealDetail.addPlatformDetail(DealType.CPS, userCpsIncomeId, -1, userId, DealType.ACCOUNT, amount, 2, "CPS推广费", error);

		data.updateSignWithLock(user.id, error);
		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		TemplateStation station = new TemplateStation();
		station.id = Templets.M_REWARDS;

		if (station.status) {
			String mContent = station.content.replace("userName", user.name);
			mContent = mContent.replace("money", String.format("%.2f", amount));
			mContent = mContent.replace("date", DateUtil.dateToString(new Date()));
			TemplateStation.addMessageTask(userId, station.title, mContent);
		}

		// 发送邮件[userName]充值了￥[money]元
		TemplateEmail email = new TemplateEmail();
		email.id = Templets.M_REWARDS;

		if (email.status) {
			String eContent = email.content.replace("userName", user.name);
			eContent = eContent.replace("money", String.format("%.2f", amount));
			eContent = eContent.replace("date", DateUtil.dateToString(new Date()));
			email.addEmailTask(user.email, email.title, eContent);
		}

		// 发送短信 [userName]充值了￥[money]元
		TemplateSms sms = new TemplateSms();
		if (StringUtils.isNotBlank(user.mobile)) {
			sms.id = Templets.M_REWARDS;
			if (sms.status) {
				String eContent = sms.content.replace("userName", user.name);
				eContent = eContent.replace("money", String.format("%.2f", amount));
				eContent = eContent.replace("date", DateUtil.dateToString(new Date()));
				TemplateSms.addSmsTask(user.mobile, eContent);
			}
		}
		error.code = 0;
	}

	/**
	 * 发放佣金，增加用户余额
	 * 
	 * @param error
	 * @param amount         佣金
	 * @param inviteIncomeId 返佣记录id
	 * @param userId         邀请人id
	 */
	public static void addBanlanceForInvitation(ErrorInfo error, double amount, long inviteIncomeId, long userId) {
		error.clear();

		User user = new User();
		user.id = userId;

		DataSafety data = new DataSafety();
		data.id = user.id;

		if (!data.signCheck(error)) {
			JPA.setRollbackOnly();
			return;
		}

		/**
		 * 更新佣金发放状态为已发，防重复
		 */
		updateInvitationStatus(inviteIncomeId, 1, error);
		if (error.code < 0) {
			return;
		}

		DealDetail.addUserFund(user.id, amount);

		v_user_for_details forDetail = DealDetail.queryUserBalance(user.id, error);
		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		DealDetail detail = new DealDetail(user.id, DealType.INVITATION_AMOUNT, amount, inviteIncomeId, forDetail.user_amount,
				forDetail.freeze, forDetail.receive_amount, "获得财富圈推广佣金");
		detail.addDealDetail(error);
		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		// 添加佣金交易记录
		DealDetail.addPlatformDetail(DealType.COMMISSION_DISCOUNT, inviteIncomeId, -1, userId, DealType.ACCOUNT, amount, 2,
				"返佣金额", error);

		data.updateSignWithLock(user.id, error);
		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		TemplateStation station = new TemplateStation();
		station.id = Templets.M_REWARDS;

		if (station.status) {
			String mContent = station.content.replace("userName", user.name);
			mContent = mContent.replace("money", String.format("%.2f", amount));
			mContent = mContent.replace("date", DateUtil.dateToString(new Date()));
			TemplateStation.addMessageTask(userId, station.title, mContent);
		}

		// 发送邮件[userName]充值了￥[money]元
		TemplateEmail email = new TemplateEmail();
		email.id = Templets.M_REWARDS;

		if (email.status) {
			String eContent = email.content.replace("userName", user.name);
			eContent = eContent.replace("money", String.format("%.2f", amount));
			eContent = eContent.replace("date", DateUtil.dateToString(new Date()));
			email.addEmailTask(user.email, email.title, eContent);
		}

		// 发送短信 [userName]充值了￥[money]元
		TemplateSms sms = new TemplateSms();
		if (StringUtils.isNotBlank(user.mobile)) {
			sms.id = Templets.M_REWARDS;
			if (sms.status) {
				String eContent = sms.content.replace("userName", user.name);
				eContent = eContent.replace("money", String.format("%.2f", amount));
				eContent = eContent.replace("date", DateUtil.dateToString(new Date()));
				TemplateSms.addSmsTask(user.mobile, eContent);
			}
		}
		error.code = 0;
	}

	/**
	 * 修改基本资料
	 * 
	 * @param error
	 */
	public void updateBaseInfo(ErrorInfo error) {
		EntityManager em = JPA.em();

		Query query = em
				.createQuery("update t_users set reality_name=?,sex=?,"
						+ "id_number=?,city_id=?,education_id=?,marital_id=?,car_id=?,house_id=?,birthday=?,is_add_base_info = ?"
						+ "where id = ?")
				.setParameter(1, this.realityName).setParameter(2, this._sex).setParameter(3, this.idNumber)
				.setParameter(4, this.cityId).setParameter(5, this.educationId).setParameter(6, this.maritalId)
				.setParameter(7, this.carId).setParameter(8, this.houseId).setParameter(9, this.birthday)
				.setParameter(10, Constants.TRUE).setParameter(11, this.id);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("保存用户编辑的信息时：" + e.getMessage());
			error.code = -2;
			error.msg = "对不起，由于平台出现故障，此次编辑信息保存失败！";

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return;
		}

	}

	/**
	 * 修改邮箱(原邮箱为空)
	 * 
	 * @param id
	 * @param email
	 * @param error
	 */
	public static void updateEmail(long id, String email, ErrorInfo error) {

		User.isEmailExist(email, null, error);
		if (0 < error.code) {
			return;
		}

		String sql = "update t_users set email = ? where id = ? and email is null";
		Query query = JPA.em().createQuery(sql).setParameter(1, email).setParameter(2, id);
		int rows = 0;
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，修改失败！";
		}
		if (rows != 1) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，修改失败！";
		} else {
			error.code = 0;
			error.msg = "修改成功！";
		}
	}

	/**
	 * 修改手机(原手机为空)
	 * 
	 * @param id
	 * @param email
	 * @param error
	 */
	public static void updateMobile1(long id, String mobile, ErrorInfo error) {

		if (StringUtils.isBlank(mobile)) {
			error.code = -1;
			error.msg = "手机号码不能为空";
			return;
		}
		if (!RegexUtils.isMobileNum(mobile)) {
			error.code = -1;
			error.msg = "请输入正确的手机号码";
			return;
		}
		String sql = "update t_users set mobile = ? where id = ?";
		Query query = JPA.em().createQuery(sql).setParameter(1, mobile).setParameter(2, id);
		int rows = 0;
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，修改失败！";
		}
		if (rows != 1) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，修改失败！";
		} else {
			error.code = 0;
			error.msg = "修改成功！";
		}
	}

	/**
	 * 修改手机(原手机存在)
	 * 
	 * @param id
	 * @param email
	 * @param error
	 */
	public static void updateMobile(long id, String mobile, ErrorInfo error) {

		User.isMobileExist(mobile, null, error);
		if (0 < error.code) {
			return;
		}

		String sql = "update t_users set mobile = ? where id = ?";
		Query query = JPA.em().createQuery(sql).setParameter(1, mobile).setParameter(2, id);
		int rows = 0;
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，修改失败！";
		}
		if (rows != 1) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，修改失败！";
		} else {
			error.code = 0;
			error.msg = "修改成功！";
		}
	}

	/** add v8.0.1 为每一个用户在缓存中添加一个对象锁，防止并发访问时（多点登陆）出现脏读 begin */

	public static final String USER_LOCK = "User_Lock_";

	/**
	 * 获取对象锁,每个用户在缓存中有一个锁
	 * 
	 * @param userId 用户id
	 * @return ReentrantLock 锁
	 */
	private static ReentrantLock getLock(long userId) {

		ReentrantLock lock = (ReentrantLock) Cache.get(USER_LOCK + userId);
		if (lock == null) {
			synchronized (User.class) { // 单例，一个用户，一个锁
				lock = (ReentrantLock) Cache.get(USER_LOCK + userId);
				if (lock == null) {
					lock = new ReentrantLock();
					Cache.set(USER_LOCK + userId, lock);
				}
			}
		}

		return lock;
	}

	/**
	 * 加锁
	 * 
	 * @param userId
	 */
	public static void addLock(long userId) {
		getLock(userId).lock();
	}

	/**
	 * 解锁
	 * 
	 * @param userId
	 */
	public static void deleteLock(long userId) {
		ReentrantLock lock = getLock(userId);

		if (lock == null) {

			return;
		}

		if (lock.isHeldByCurrentThread()) {

			lock.unlock();
		}

	}

	/**
	 * 清除锁缓存
	 */
	public static void cleanCacheLock(String userId) {

		ReentrantLock lock = (ReentrantLock) Cache.get(USER_LOCK + userId);

		if (lock == null) {

			return;
		}

		if (lock.isHeldByCurrentThread()) {
			lock.unlock();
		}

		if (!lock.hasQueuedThreads()) {
			Cache.delete(USER_LOCK + userId);
		}
	}

	/** add v8.0.1 为每一个用户在缓存中添加一个对象锁，防止并发访问时（多点登陆）出现脏读 end */

	/**
	 * <Description functions in a word> 查询相应时间注册会员数目的数据 <Detail description>
	 * 
	 * @author ChenZhipeng
	 * @param error
	 * @param time
	 * @return [Parameters description]
	 * @return long [Return type description]
	 * @exception throws [Exception] [Exception description]
	 * @see [Related classes#Related methods#Related properties]
	 */
	public static int queryUserCount(ErrorInfo error, String startTime, String endTime, int type) {
		error.clear();
		Object count = null;

		String sql = null;
		String hour = null;
		if (type == Constants.YESTERDAY) {
			sql = "SELECT COUNT(id) FROM t_users WHERE TO_DAYS(NOW()) - TO_DAYS(time) = 1 AND HOUR(time) <= ?";
			if (endTime.contains(":")) {
				hour = endTime.substring(0, endTime.indexOf(":"));
				if ("00".equals(hour)) {
					hour = "24";
				}
			}
		} else {
			sql = "SELECT COUNT(id) FROM t_users WHERE time>=? AND time <= ? ";
		}

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql);
			if (type == Constants.YESTERDAY) {
				count = query.setParameter(1, hour).getSingleResult();
			} else {
				count = query.setParameter(1, startTime).setParameter(2, endTime).getSingleResult();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询昨日数据失败");
			error.code = -1;
			error.msg = "查询昨日数据失败";

			return -1;
		}

		error.code = 0;

		return count == null ? 0 : Integer.parseInt(count.toString());

	}

	/***
	 * <Description functions in a word> 查询相应时间理财会员数目的数据 <Detail description>
	 * 
	 * @author ChenZhipeng
	 * @param error
	 * @param time
	 * @param isType
	 * @return [Parameters description]
	 * @return long [Return type description]
	 * @exception throws [Exception] [Exception description]
	 * @see [Related classes#Related methods#Related properties]
	 */
	public static int queryFinancialUserCount(ErrorInfo error, String startTime, String endTime, int type) {
		error.clear();
		Object count = null;

		String sql = null;
		String hour = null;
		if (type == Constants.YESTERDAY) {
			sql = "SELECT COUNT(id) FROM t_users WHERE (TO_DAYS( NOW() ) - TO_DAYS(master_time_invest) = 1 AND HOUR(master_time_invest) <= ?) OR (TO_DAYS( NOW() ) - TO_DAYS(master_time_complex) = 1 AND HOUR(master_time_complex)<= ?)";
			if (endTime.contains(":")) {
				hour = endTime.substring(0, endTime.indexOf(":"));
				if ("00".equals(hour)) {
					hour = "24";
				}
			}
		} else {
			sql = "SELECT COUNT(id) FROM t_users WHERE master_time_invest >=? AND master_time_invest <= ?"
					+ " OR (master_time_complex >= ? AND master_time_complex <= ? AND ISNULL(master_time_invest))";
		}

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql);
			if (type == Constants.YESTERDAY) {
				count = query.setParameter(1, hour).setParameter(2, hour).getSingleResult();
			} else {
				count = query.setParameter(1, startTime).setParameter(2, endTime).setParameter(3, startTime)
						.setParameter(4, endTime).getSingleResult();
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询昨日数据失败");
			error.code = -1;
			error.msg = "查询昨日数据失败";

			return -1;
		}

		error.code = 0;

		return count == null ? 0 : Integer.parseInt(count.toString());
	}

	/**
	 * 查询待审核提现个数 待付款提现个数
	 * 
	 * @param error
	 * @param status
	 * @return
	 */
	public static int queryWaitWithdrawCount(ErrorInfo error, int status) {
		error.clear();

		Object result = null;

		String sql = "select count(1) from t_user_withdrawals where status=? ";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, status);

		try {
			result = query.getSingleResult();

		} catch (Exception e) {
			Logger.error("待审核/待付款提现个数:" + e.getMessage());
			error.code = -1;
			error.msg = "查询待审核/待付款个数异常";

			return 0;
		}

		return result == null ? 0 : Integer.parseInt(result.toString());
	}

	/**
	 * 查询被举报会员数
	 */
	public static int queryBeReportedMemberCount(ErrorInfo error) {
		error.clear();

		Object result = null;

		String sql = "SELECT COUNT(DISTINCT reported_user_id) FROM t_user_report_users";
		Query query = JPA.em().createNativeQuery(sql);

		try {
			result = query.getSingleResult();
		} catch (Exception e) {
			Logger.error("被举报会员个数:" + e.getMessage());
			error.code = -1;
			error.msg = "查询被举报会员个数异常";

			return 0;
		}

		return result == null ? 0 : Integer.parseInt(result.toString());
	}

	/**
	 * 查询被锁定会员数
	 */
	public static int queryBeLockMemberCount(ErrorInfo error) {
		error.clear();

		Object result = null;

		String sql = "select count(1) from t_users where is_allow_login=1 ";
		Query query = JPA.em().createNativeQuery(sql);

		try {
			result = query.getSingleResult();
		} catch (Exception e) {
			Logger.error("被锁定会员个数:" + e.getMessage());
			error.code = -1;
			error.msg = "查询被锁定会员个数异常";

			return 0;
		}

		return result == null ? 0 : Integer.parseInt(result.toString());
	}

	/**
	 * 查询黑名单会员数
	 */
	public static int queryBlacklistMemberCount(ErrorInfo error) {
		error.clear();

		Object result = null;

		String sql = "select count(1) from t_users where is_blacklist=1 ";
		Query query = JPA.em().createNativeQuery(sql);

		try {
			result = query.getSingleResult();
		} catch (Exception e) {
			Logger.error("黑名单会员个数:" + e.getMessage());
			error.code = -1;
			error.msg = "查询黑名单会员个数异常";

			return 0;
		}

		return result == null ? 0 : Integer.parseInt(result.toString());
	}

	public static String queryUserMobile(long userId) {
		String sql = "SELECT mobile FROM t_users WHERE id = ?";

		Object mobile = null;

		try {
			mobile = JPA.em().createNativeQuery(sql).setParameter(1, userId).getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();

			return "";
		}

		return mobile == null ? "" : mobile.toString();
	}

	// 保存汇付资金托管回调信息
	public void updateTrustAcctount(long userId, String pIpsAcctNo, String realName, String idNumber, String mobile, String email,
			ErrorInfo error) {
		error.clear();

		StringBuffer sql = new StringBuffer("update t_users set ips_acct_no = ?");

		List<String> conditions = new ArrayList<String>();

		if (StringUtils.isNotBlank(realName)) {
			sql.append(", reality_name = ?");
			conditions.add(realName);
		}

		if (StringUtils.isNotBlank(idNumber)) {
			sql.append(", id_number = ?");
			conditions.add(idNumber);
		}

		if (StringUtils.isNotBlank(mobile)) {
			sql.append(", mobile = ?, is_mobile_verified = 1");
			conditions.add(mobile);
		}

		if (StringUtils.isNotBlank(email)) {
			sql.append(", email= ?, is_email_verified = 1");
			conditions.add(email);
		}

		sql.append(" where id= ?");

		int rows = 0;

		Query query = JpaHelper.execute(sql.toString()).setParameter(1, pIpsAcctNo);

		for (int i = 0; i < conditions.size(); i++) {
			query.setParameter(i + 2, conditions.get(i));
		}

		query.setParameter(conditions.size() + 2, userId);

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("保存回调信息时：" + e.getMessage());

			error.code = -1;
			error.msg = "回调信息保存失败";

			return;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return;
		}

		DealDetail.userEvent(userId, UserEvent.IPS_ACCT_NO, "开户成功", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return;
		}

		this.realityName = realName;
		this.idNumber = idNumber;
		this.ipsAcctNo = pIpsAcctNo;
		setCurrUser(this);

		error.code = 0;
		error.msg = "开户成功！";
	}

	/**
	 * 
	 * @Title: queryByPaynumber
	 * @Description:
	 * @param: @param  payNumber
	 * @param: @param  error
	 * @param: @return
	 * @return: t_user_recharge_details
	 * @throws @author luochengwei
	 * @Date 2015-8-25 下午03:47:12
	 */
	public static t_user_recharge_details queryByPaynumber(String payNumber, ErrorInfo error) {
		error.clear();

		t_user_recharge_details user_recharge = null;
		try {
			user_recharge = t_user_recharge_details.find("pay_number = ?", payNumber).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("判断是否已经充值时：" + e.getMessage());

			error.code = -1;
			error.msg = "充值回调失败";
			return user_recharge;
		}

		if (user_recharge == null) {
			error.code = -1;
			error.msg = "充值回调失败";
			return user_recharge;
		}

		error.code = 1;
		return user_recharge;
	}

	public static PageBean<t_bid_publish> queryBidsPublishList(int orderType, int productId, String bidTitle, String startDate,
			String endDate, int currPage, int pageSize) {

		StringBuffer sql = new StringBuffer(" 1=1 ");

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		if (productId > 0) {

			params.add(productId);

			sql.append(" and product_id=?");
		}
		conditionMap.put("productId", productId);

		if (StringUtils.isNotBlank(bidTitle)) {

			params.add("%" + bidTitle + "%");

			sql.append(" and bid_title like ?");
		}
		conditionMap.put("bidTitle", bidTitle);

		if (StringUtils.isNotBlank(startDate)) {

			params.add(DateUtil.strDateToStartDate(startDate));

			sql.append(" and publish_time >= ?");
		}
		conditionMap.put("startDate", startDate);

		if (StringUtils.isNotBlank(endDate)) {

			params.add(DateUtil.strDateToStartDate(endDate));

			sql.append(" and publish_time <= ?");
		}
		conditionMap.put("endDate", endDate);

		conditionMap.put("orderType", orderType);

		sql.append("order by " + Constants.ORDER_TYPE_BID_PUBLISH[orderType]);

		PageBean<t_bid_publish> page = new PageBean<t_bid_publish>();
		page.currPage = currPage;
		page.pageSize = pageSize;

		try {
			page.totalCount = (int) t_bid_publish.count(sql.toString(), params.toArray());
			page.page = t_bid_publish.find(sql.toString(), params.toArray()).fetch(page.currPage, page.pageSize);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询发标公告列表时：" + e.getMessage());

			return null;
		}

		page.conditions = conditionMap;

		return page;
	}

	/**
	 * 添加发标公告
	 * 
	 * @param bidPublish
	 * @param error
	 */
	public static void addBidPublish(t_bid_publish bidPublish, ErrorInfo error) {

		try {
			bidPublish.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();
		}

		error.msg = "添加成功";
		error.code = 0;
	}

	/**
	 * 前台首页的发标公告
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> queryBidsPublishListForFront(boolean allFlag) {

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		String sql = " select  year(create_time) as year,month(create_time) as month,day(create_time) as day  "
				+ " from t_bid_publish group by year(create_time),month(create_time),day(create_time) order by create_time desc ";

		if (!allFlag) {
			sql += "  limit 2 ";
		}

		try {

			list = JPAUtil.getList(new ErrorInfo(), sql);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询发标公告列表时：" + e.getMessage());

			return null;
		}

		return list;
	}

	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年4月9日
	 * @description 新首页查询发标公告
	 * @param allFlag
	 * @param num
	 * @return
	 */
	public static List<Map<String, Object>> queryBidsPublishListForFront(boolean allFlag, int num) {

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		String sql = " select bid_title as title, year(create_time) as year,month(create_time) as month,day(create_time) as day  "
				+ " from t_bid_publish group by bid_title, year(create_time),month(create_time),day(create_time) order by create_time desc ";

		if (!allFlag) {
			sql += "  limit " + num;
		}

		try {

			list = JPAUtil.getList(new ErrorInfo(), sql);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询发标公告列表时：" + e.getMessage());

			return null;
		}

		return list;
	}

	/**
	 * 根据年月日查询具体的公告内容
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static List<t_bid_publish> queryBidPublishByYearMonthDay(int year, int month, int day) {

		List<t_bid_publish> resultList = new ArrayList<t_bid_publish>();

		String sql = "select * from t_bid_publish where year(create_time)=? and month(create_time)=? and day(create_time)=?";

		try {

			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), t_bid_publish.class);
			query.setParameter(1, year);
			query.setParameter(2, month);
			query.setParameter(3, day);
			resultList = query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询发标公告列表时：" + e.getMessage());

			return null;
		}

		return resultList;
	}

	/**
	 * 根据手机号码查找用户名
	 * 
	 * @param email
	 * @return
	 */
	public static String selectNameByMobile(String mobile, ErrorInfo error) {
		error.clear();

		String sql = "select name from t_users where mobile = ?";

		String name = null;

		try {
			name = t_users.find(sql, mobile).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("页面注册时,根据手机号码查找用户名：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障！";

			return null;
		}
		if (StringUtils.isBlank(name)) {
			error.code = -1;

			error.msg = "该手机号码没有进行注册";

			return null;
		}
		error.code = 1;

		return name;
	}

	public static Map<String, String> baofuPay(User user, BigDecimal money, int bankType, int type, String rechargeMark,
			int client, String card_no, ErrorInfo error) {

		error.clear();

		String acc_no = card_no;// 银行卡号
		String pay_code = Constants.BAOFU_TYPE[bankType];// 银行编码
		String id_holder = user.realityName;// 姓名
		String mobile = user.mobile;// 手机号
		String id_card = user.idNumber;// 身份证号

		Map<String, String> args = new HashMap<String, String>();

		String merOrderNum = NumberUtil.getBillNo(type); // 订单号 ---- 支付流水号

		// #########################判断认证银行卡是否存在######################################

		// ################################################

		/*
		 * UserBankAccounts bankUser = new UserBankAccounts();
		 * 
		 * //查询是否有已经验证的银行卡 t_user_bank_accounts bank =
		 * UserBankAccounts.queryById(user.id);
		 * 
		 * if(bank == null) { //查询用户是否已经有该未验证的银行账号 t_user_bank_accounts banks =
		 * UserBankAccounts.queryByIds(user.id,card_no); if(banks == null) {
		 * bankUser.userId = user.id; bankUser.bankName =
		 * Constants.BAOFU_BANK_NAME[bankType]; bankUser.bankCode =
		 * Constants.BAOFU_TYPE[bankType]; //bankUser.bankCode = 10023; bankUser.account
		 * = card_no; bankUser.accountName = user.realityName;
		 * bankUser.addBankAccount(error); }else {
		 * if(!banks.account.equals(card_no.trim())) { bankUser.userId = user.id;
		 * bankUser.bankName = Constants.BAOFU_BANK_NAME[bankType]; bankUser.bankCode =
		 * Constants.BAOFU_TYPE[bankType]; //bankUser.bankCode = 10023; bankUser.account
		 * = card_no; bankUser.accountName = user.realityName;
		 * bankUser.addBankAccount(error); } }
		 * 
		 * if(error.code < 0){ error.msg="该认证银行卡号已经存在，请选择别的银行卡号"; return null; } }
		 */

		// 金额
		BigDecimal txn_amt_num = money.multiply(BigDecimal.valueOf(100));// 金额转换成分
		String txn_amt = String.valueOf(txn_amt_num.longValue());// 支付金额

		// 获取私钥文件
		String pfxpath = Play.configuration.getProperty("baofu.private.key.file.path");

		File pfxfile = new File(pfxpath);
		if (!pfxfile.exists()) {
			Logger.info("系统获取商户的私钥文件失败，请查看该路径：" + pfxpath + "下面是否存在文件");
			error.code = -1;
			error.msg = "系统获取商户的私钥文件失败";
			return null;
		}

		String trade_date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());// 交易日期

		String trans_id = merOrderNum;// 商户订单号

		String version = "4.0.0.0";// 版本号
		String terminal_id = Play.configuration.getProperty("terminal.id"); // 终端号
		String member_id = Play.configuration.getProperty("member.id"); // 商户号
		String pfxpwd = Play.configuration.getProperty("pfx.pwd");// 商户私钥证书密码
		String txn_type = "03311";

		String input_charset = "1";
		String language = "1";
		String txn_sub_type = "03"; // 交易子类
		String data_type = "json"; // 加密报文的数据类型（xml/json）

		String page_url = Constants.BASE_URL + Play.configuration.getProperty("baofu.web.pay.return.url");
		String return_url = Constants.BASE_URL + Play.configuration.getProperty("baofu.web.pay.notify.url");// 商户改成自已的地址。返回方法在returnurl内请参考
		// 出错时宝付回跳页面
		String back_url = "";
		if (Constants.CLIENT_WECHAT == client) {
			WechatAccountHome.recharge();

		} else if (Constants.CLIENT_APP == client) {
			back_url = Constants.BASE_URL + "front/account/rechargeApp?id=" + user.sign;
		} else {
			back_url = Constants.BASE_URL + "front/account/recharge";
		}

		String serviceUrl = Play.configuration.getProperty("baofu.web.pay.service.url");

		// 充值送红包
		String redTypeName = Constants.RED_PACKAGE_TYPE_RECHARGE;// 红包类型

		long status = Constants.RED_PACKAGE_TYPE_STATUS_ENABLED;// 启用状态
		long id = 0;
		models.t_red_packages_type redPackageType = RedPackage.isExist(redTypeName, status);// 红包类型是否存在
		if (null != redPackageType && redPackageType.validity_money <= money.doubleValue()) {
			String desc = "APP充值发放红包";
			id = RedPackageHistory.sendRedPackageTo(user, redPackageType, desc);
			Logger.error("APP充值发放红包短信通知成功");
		} else {
			Logger.error("APP充值发放红包发放失败!造成原因数据库中不存在充值类型红包或者未达到条件");
		}

		Map<String, String> ArrayData = new HashMap<String, String>();

		ArrayData.put("txn_sub_type", txn_sub_type);
		ArrayData.put("biz_type", "0000");
		ArrayData.put("terminal_id", terminal_id);
		ArrayData.put("member_id", member_id);
		ArrayData.put("pay_code", pay_code);
		ArrayData.put("acc_no", acc_no);
		ArrayData.put("id_card_type", "01");
		ArrayData.put("id_card", id_card);
		ArrayData.put("id_holder", id_holder);
		ArrayData.put("mobile", mobile);
		ArrayData.put("valid_date", "");// 暂不支持信用卡（传空）
		ArrayData.put("valid_no", "");// 暂不支持信用卡（传空）
		ArrayData.put("trans_id", trans_id);
		ArrayData.put("txn_amt", txn_amt);
		ArrayData.put("trade_date", trade_date);
		ArrayData.put("commodity_name", "商品名称");
		ArrayData.put("commodity_amount", "1");// 商品数量（默认为1）
		ArrayData.put("user_name", "用户名称");
		ArrayData.put("page_url", page_url);
		ArrayData.put("return_url", return_url);
		// ArrayData.put("additional_info",
		// ((txn_amt_num.longValue())/100.00)+"_"+pay_code);1020000
		ArrayData.put("additional_info", ((txn_amt_num.longValue()) / 100.00) + "_" + pay_code);
		ArrayData.put("req_reserved", id + "_" + bankType);// 备用字段,增加拿到充值红包id

		Logger.info("############################用户==" + user.name + "添加红包id======" + id + "到宝付备用字段$$$$$$$$$$$$$$$$$$$$$$$$$$$$");

		JSONObject jsonObjectFromMap = JSONObject.fromObject(ArrayData);

		String XmlOrJson = jsonObjectFromMap.toString();

		Logger.info("====请求明文:" + XmlOrJson);

		String data_content = "";

		try {

			String base64str = SecurityUtil.Base64Encode(XmlOrJson);

			data_content = RsaCodingUtil.encryptByPriPfxFile(base64str, pfxpath, pfxpwd);

		} catch (Exception e) {

			e.printStackTrace();

			Logger.info("宝付支付对加密数据进行加密出错");

			error.code = -1;

			error.msg = "宝付支付对加密数据进行加密出错";

			return null;
		}

		args.put("version", version);

		args.put("input_charset", input_charset);

		args.put("language", language);

		args.put("terminal_id", terminal_id);

		args.put("txn_type", txn_type);

		args.put("txn_sub_type", txn_sub_type);

		args.put("member_id", member_id);

		args.put("data_type", data_type);

		args.put("data_content", data_content);

		args.put("back_url", back_url);

		args.put("url", serviceUrl);

		Logger.info("*****************************************打印连连网银充值参数开始*************");
		Logger.info(args.toString());
		Logger.info("*****************************************打印连连网银充值参数结束*************");

		// 认证充值前操作
		sequence(user.id, Constants.BF_GATEWAY, merOrderNum, money.doubleValue(), Constants.GATEWAY_RECHARGE, client, card_no,
				error);

		if (error.code < 0) {

			return null;
		}

		error.code = 1;

		return args;
	}

	/**
	 * 充值回调红包修改状态
	 * 
	 * @param id
	 * @param user
	 * @param error
	 */
	public static void updateCallbackAppRedPackage(long id, User user, ErrorInfo error) {
		error.clear();
		try {

			int row = RedPackage.updateRedPackagesHistory(id, Constants.RED_PACKAGE_STATUS_UNUSED);
			if (row > 0) {
				t_red_packages_history history = t_red_packages_history.findById(id);

				Map<String, Double> detail = DealDetail.queryUserFund(user.id, error);
				DealDetail dealDetail = new DealDetail(user.id, DealType.RED_RELEASESYS, history.money, DealType.RED_RELEASE,
						detail.get("user_amount"), detail.get("freeze"), detail.get("receive_amount"), Constants.RED_PACKAGE_APP);
				dealDetail.addDealDetail(error);

				/* 更新自己的防篡改 */
				DataSafety data = new DataSafety(); // 借款会员数据防篡改对象
				data.updateSignWithLock(user.id, error);
				if (error.code < 0) {
					error.code = -1;
					error.msg = "放篡改更新失败!";
					Logger.error("放篡改更新失败!");

				}
				// 用户通知方式
				RedPackageHistory.userNotice(user, history, Constants.RED_PACKAGE_APP);
			}
		} catch (NumberFormatException e) {
			Logger.info("充值红包回调修改异常");
			e.printStackTrace();
		}
	}

	/**
	 * 宝付手机端充值
	 * 
	 * @param money
	 * @param bankType
	 * @param type
	 * @param isApp
	 * @param error
	 * @return
	 * @throws UnsupportedEncodingException
	 */

	public static Map<String, String> bfAppPay(long userId, BigDecimal money, String payCode, String realityName, String userName,
			String mobiles, String bank_card, String idCard, int type, boolean isApp, ErrorInfo error)
			throws UnsupportedEncodingException {
		error.clear();
		t_dict_payment_gateways gateway = gateway(Constants.GATEWAY_BOFU, error);

		if (error.code < 0) {
			return null;
		}
		String version = "4.0.0.0";// 版本
		String input_charset = "1";// 字符集 utf-8
		String language = "1";// 中文
		String txn_type = "03311";// 交易类型
		String data_type = "xml";// 加密数据类型
		// String back_url = Constants.BF_MERCHANT_APP_URL;// 页面通知地址

		String txn_sub_type = "02";// 交易子类 02：支付类交易
		// String biz_type="0000";//接入类型： 0000为储蓄卡支付
		String terminal_id = Constants.BF_END_NUM_SDK;// 终端号
		String member_id = gateway.pid;// 商户号

		String pay_code = String.valueOf(payCode);// 银行编码CCB
		String acc_no = String.valueOf(bank_card);// 银行卡号

		// String id_card_type="01";//身份证类型：01认为身份证号
		String id_card = idCard;// 身份证号
		String id_holder = String.valueOf(realityName);// 持卡人姓名
		String mobile = String.valueOf(mobiles);// 银行卡预留手机号
		// String valid_date="";//卡有效期
		// String valid_no="";//卡安全码

		String trans_id = NumberUtil.getBillNo(type);// 商户订单号
		double b = money.multiply(new BigDecimal(100)).doubleValue();
		String txn_amt = money.multiply(new BigDecimal(100)).toString();// 交易金额
		String trade_date = DateUtil.simple2(new Date());// 订单日期
		String user_name = String.valueOf(userName);// 用户名
		String page_url = Constants.BF_MERCHANT_APP_URL;// 页面通知地址
		String return_url = Constants.BF_MERCHANT_APP_URLBG;// 服务器通知地址
		String data_context = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> " + "<data_content>"
				+ "<txn_sub_type>02</txn_sub_type> " + "<biz_type>0000</biz_type> " + "<terminal_id>" + terminal_id
				+ "</terminal_id> " + "<member_id>" + member_id + "</member_id> " + "<pay_code>" + pay_code + "</pay_code> "
				+ "<acc_no>" + acc_no + "</acc_no> " + "<id_card_type>01</id_card_type> " + "<id_card>" + id_card + "</id_card> "
				+ "<id_holder>" + id_holder + "</id_holder> " + "<mobile>" + mobile + "</mobile> " + "<valid_date /> "
				+ "<valid_no /> " + "<trans_id>" + trans_id + "</trans_id> " + "<txn_amt>" + txn_amt + "</txn_amt> "
				+ "<trade_date>" + trade_date + "</trade_date> " + "<user_name>" + user_name + "</user_name> " + "<page_url>"
				+ page_url + "</page_url> " + "<return_url>" + return_url + "</return_url> " + "</data_content>";
		Logger.info("bfAppPay info[%s]", data_context);
		String base64str = SecurityUtil.Base64Encode(data_context);
		String data_content = RsaCodingUtil.encryptByPriPfxFile(base64str, Play.configuration.getProperty("pfx.path"),
				Play.configuration.getProperty("pfx.pwd"));

		Map<String, String> args = new HashMap<String, String>();
		args.put("version", version);
		args.put("input_charset", input_charset);
		args.put("language", language);
		args.put("terminal_id", terminal_id);
		args.put("txn_type", txn_type);
		args.put("txn_sub_type", txn_sub_type);
		args.put("member_id", member_id);
		args.put("data_type", data_type);
		args.put("data_content", data_content);
		sequence(userId, Constants.BF_GATEWAY, trans_id, money.doubleValue(), Constants.GATEWAY_RECHARGE, 2, acc_no, error);
		return args;

	}

	/**
	 * 
	 * @author liulj @creationDate. 2017年2月24日 下午4:46:25 @description. 投保记录
	 * 
	 * @param beginTime
	 * @param endTime
	 * @param userName
	 * @param pageSize
	 * @param currPage
	 * @return
	 */
	public static PageBean<Map<String, Object>> findUserBankInsur(String beginTime, String endTime, String userName, int pageSize,
			int currPage) {
		// String sql = "select bi.id, bi.user_id, bi.bid_id, bi.title, bi.status,
		// bi.receive_time as real_receive_time, bi.receive_corpus as
		// real_receive_corpus, bi.receive_interest as real_receive_interest from
		// t_bill_invests bi left join t_bids b on (bi.bid_id = b.id) where (b.status =
		// ? or b.status = ?) and bi.user_id = ? and b.audit_time > '2016-10-26
		// 00:00:00'";
		pageSize = pageSize == 0 ? 10 : pageSize;
		currPage = currPage == 0 ? 1 : currPage;
		StringBuffer sql = new StringBuffer(
				" FROM t_users u INNER JOIN t_user_bank_accounts ub ON ub.user_id = u.id INNER JOIN t_user_insur ui ON ui.bank_account = ub.account WHERE ub.account IS NOT NULL");

		List<Object> p = new ArrayList<Object>();
		if (StringUtils.isNotBlank(beginTime)) {
			sql.append(" AND ui.buy_dt > ?");
			p.add(beginTime);
		}
		if (StringUtils.isNotBlank(endTime)) {
			sql.append(" AND ui.buy_dt < ?");
			p.add(endTime);
		}
		if (StringUtils.isNotBlank(userName)) {
			sql.append(" AND (u.name = ? OR u.reality_name = ?)");
			p.add(userName);
			p.add(userName);
		}

		String listSql = "SELECT ui.*,u.name,u.reality_name,ub.account,ub.bank_name".concat(sql.toString())
				.concat(" ORDER BY ui.buy_dt desc LIMIT ?,?");
		String cntSql = "SELECT count(*) as count".concat(sql.toString());

		int count = 0;
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), cntSql, p.toArray());
		if (countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null) {
			count = ((BigInteger) countMap.get(0).get("count")).intValue();
		}

		p.add((currPage - 1) * pageSize);
		p.add(pageSize);
		System.out.println(listSql + ";" + ((currPage - 1) * pageSize) + ";" + pageSize);
		List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), listSql, p.toArray());

		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("userName", userName);
		conditionMap.put("beginTime", beginTime);
		conditionMap.put("endTime", endTime);

		PageBean<Map<String, Object>> page = new PageBean<Map<String, Object>>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = list;

		return page;
	}

	/**
	 * 
	 * @author liulj @creationDate. 2017年2月24日 下午5:14:13 @description.
	 *         查询没有投保的银行卡或投保过期的银行卡
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> findUserBankInsurExpr(Long userId, boolean isTest) {
		String sql = "SELECT u.id, u.name,u.reality_name,ub.account,u.id_number,u.mobile,ub.bank_name FROM t_users u LEFT JOIN t_user_bank_accounts ub ON ub.user_id = u.id WHERE ub.account IS NOT NULL AND u.id_number IS NOT NULL AND (SELECT COUNT(*) as count FROM t_user_insur ui WHERE ui.bank_account = ub.account AND ui.insur_end > ui.insur_start) = 0";
		if (userId != null) {
			sql = sql.concat(" AND u.id = " + userId);
		}
		if (isTest) {
			sql = sql.concat(" AND u.time > '2017-03-09 00:00:00'");
		}
		sql = sql.concat(" order by u.time desc LIMIT 100");
		List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), sql);
		return list;
	}

	/**
	 * 
	 * @author liulj @creationDate. 2017年2月24日 下午4:49:29 @description. 查询银行卡是否购买保险
	 * 
	 * @param bankAccount
	 * @return
	 */
	public static int findUserBankInsur(String bankAccount) {
		// String sql = "select bi.id, bi.user_id, bi.bid_id, bi.title, bi.status,
		// bi.receive_time as real_receive_time, bi.receive_corpus as
		// real_receive_corpus, bi.receive_interest as real_receive_interest from
		// t_bill_invests bi left join t_bids b on (bi.bid_id = b.id) where (b.status =
		// ? or b.status = ?) and bi.user_id = ? and b.audit_time > '2016-10-26
		// 00:00:00'";

		String sql = "SELECT COUNT(*) as count FROM t_user_insur ui WHERE ui.bank_account = ?  AND ui.insur_end > CURTIME()";
		int count = 0;
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), sql, bankAccount);
		if (countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null) {
			count = ((BigInteger) countMap.get(0).get("count")).intValue();
		}
		return count;
	}

	/**
	 * 
	 * @author liulj @creationDate. 2017年2月24日 下午4:49:29 @description.
	 *         查询某用户的银行卡是否购买保险
	 * 
	 * @param bankAccount
	 * @return
	 */
	public static int findUserBankInsur(Long userId) {
		// String sql = "select bi.id, bi.user_id, bi.bid_id, bi.title, bi.status,
		// bi.receive_time as real_receive_time, bi.receive_corpus as
		// real_receive_corpus, bi.receive_interest as real_receive_interest from
		// t_bill_invests bi left join t_bids b on (bi.bid_id = b.id) where (b.status =
		// ? or b.status = ?) and bi.user_id = ? and b.audit_time > '2016-10-26
		// 00:00:00'";

		String sql = "SELECT COUNT(*) AS count FROM t_user_bank_accounts ub INNER JOIN t_user_insur ui ON ui.bank_account = ub.account WHERE ub.user_id = ? AND ui.insur_end > CURTIME() AND ui.insur_code IS NOT NULL";
		int count = 0;
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), sql, userId);
		if (countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null) {
			count = ((BigInteger) countMap.get(0).get("count")).intValue();
		}
		return count;
	}

	/**
	 * 
	 * @author liulj @creationDate. 2017年2月27日 上午10:57:28 @description.
	 *         查询某用户的银行卡保险详情
	 * 
	 * @param userId
	 * @return
	 */
	public static Map<String, Object> findUserBankInsurDetail(Long userId) {
		String sql = "SELECT ui.* FROM t_user_bank_accounts ub INNER JOIN t_user_insur ui ON ui.bank_account = ub.account WHERE ub.user_id = ? AND ui.insur_end > CURTIME()";
		Map<String, Object> result = JPAUtil.getMap(new ErrorInfo(), sql, userId);
		result = result == null ? new HashMap<String, Object>() : result;
		return result;
	}

	/**
	 * 
	 * @author liulj @creationDate. 2017年2月27日 上午10:57:28 @description.
	 *         查询某用户的银行卡保险详情--fix--多张银行卡
	 * 
	 * @param userId
	 * @return
	 */
	public static List<Map<String, Object>> findUserALLBankInsurDetail(Long userId) {
		String sql = "SELECT ui.* FROM t_user_bank_accounts ub LEFT JOIN t_user_insur ui ON ui.bank_account = ub.account WHERE ub.user_id = ? AND ui.insur_end > CURTIME()";
		List<Map<String, Object>> result = JPAUtil.getList(new ErrorInfo(), sql, userId);
		result = result == null ? new ArrayList<Map<String, Object>>() : result;
		return result;
	}

	/**
	 * 
	 * @author liulj @creationDate. 2017年3月17日 下午3:33:11 @description. 查询用户捐款记录
	 * 
	 * @param beginTime
	 * @param endTime
	 * @param userName
	 * @param pageSize
	 * @param currPage
	 * @return
	 */
	public static PageBean<Map<String, Object>> findUserDonate(String beginTime, String endTime, String name, int usedType,
			int orderType, int pageSize, int currPage) {
		// String sql = "select bi.id, bi.user_id, bi.bid_id, bi.title, bi.status,
		// bi.receive_time as real_receive_time, bi.receive_corpus as
		// real_receive_corpus, bi.receive_interest as real_receive_interest from
		// t_bill_invests bi left join t_bids b on (bi.bid_id = b.id) where (b.status =
		// ? or b.status = ?) and bi.user_id = ? and b.audit_time > '2016-10-26
		// 00:00:00'";
		pageSize = pageSize == 0 ? 10 : pageSize;
		currPage = currPage == 0 ? 1 : currPage;
		String order[] = { "ud.ins_dt desc", "(ud.user_donate+ud.admin_donate) desc", "(ud.user_donate+ud.admin_donate) asc" };

		StringBuffer sql = new StringBuffer(" FROM t_user_donate ud");
		sql.append(" LEFT JOIN t_users u ON u.id = ud.user_id");
		sql.append(" LEFT JOIN t_bids b ON b.id = ud.bid_id");
		sql.append(" LEFT JOIN t_public_benefit pb ON pb.id = ud.benefit_id");
		sql.append(" WHERE 1 = 1");

		List<Object> p = new ArrayList<Object>();
		if (StringUtils.isNotBlank(beginTime)) {
			sql.append(" AND ud.ins_dt > ?");
			p.add(beginTime.concat(" 00:00:00"));
		}
		if (StringUtils.isNotBlank(endTime)) {
			sql.append(" AND ud.ins_dt < ?");
			p.add(endTime.concat(" 23:59:59"));
		}
		if (StringUtils.isNotBlank(name)) {
			sql.append(" AND (u.name = ? OR u.mobile = ? OR b.title = ? OR pb.name = ?)");
			p.add(name);
			p.add(name);
			p.add(name);
			p.add(name);
		}
		if (usedType == 1) {
			sql.append(" AND ud.benefit_id > 0");
		} else if (usedType == 2) {
			sql.append(" AND ud.benefit_id = 0");
		}

		String listSql = "SELECT ud.*,u.name as username,u.mobile,b.title as bid_name, pb.name as benefit_name"
				.concat(sql.toString()).concat(" ORDER BY ").concat(order[orderType]).concat(" LIMIT ?,?");
		String cntSql = "SELECT count(*) as count".concat(sql.toString());

		int count = 0;
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), cntSql, p.toArray());
		if (countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null) {
			count = ((BigInteger) countMap.get(0).get("count")).intValue();
		}

		p.add((currPage - 1) * pageSize);
		p.add(pageSize);
		System.out.println(listSql + ";" + ((currPage - 1) * pageSize) + ";" + pageSize);
		List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), listSql, p.toArray());

		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("name", name);
		conditionMap.put("beginTime", beginTime);
		conditionMap.put("endTime", endTime);
		conditionMap.put("orderType", orderType);
		conditionMap.put("usedType", usedType);

		PageBean<Map<String, Object>> page = new PageBean<Map<String, Object>>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = list;

		return page;
	}

	/**
	 * 
	 * @author liulj @creationDate. 2017年3月17日 下午5:09:02 @description. 公益项目列表
	 * 
	 * @param beginTime
	 * @param endTime
	 * @param name
	 * @param orderType
	 * @param pageSize
	 * @param currPage
	 * @return
	 */
	public static PageBean<Map<String, Object>> findPublicBenefit(String beginTime, String endTime, String name, int orderType,
			int pageSize, int currPage) {
		// String sql = "select bi.id, bi.user_id, bi.bid_id, bi.title, bi.status,
		// bi.receive_time as real_receive_time, bi.receive_corpus as
		// real_receive_corpus, bi.receive_interest as real_receive_interest from
		// t_bill_invests bi left join t_bids b on (bi.bid_id = b.id) where (b.status =
		// ? or b.status = ?) and bi.user_id = ? and b.audit_time > '2016-10-26
		// 00:00:00'";
		pageSize = pageSize == 0 ? 10 : pageSize;
		currPage = currPage == 0 ? 1 : currPage;
		String order[] = { "pb.start_dt desc", "pb.plan_donate desc", "pb.plan_donate asc", "pb.actual_donate desc",
				"pb.actual_donate asc", "pb.donate_users desc", "pb.donate_users asc" };

		StringBuffer sql = new StringBuffer(" FROM t_public_benefit pb");
		// sql.append(" LEFT JOIN t_user_donate ud ON ud.benefit_id = pb.id");
		sql.append(" WHERE 1 = 1");

		List<Object> p = new ArrayList<Object>();
		if (StringUtils.isNotBlank(beginTime)) {
			sql.append(" AND pb.start_dt > ?");
			p.add(beginTime.concat(" 00:00:00"));
		}
		if (StringUtils.isNotBlank(endTime)) {
			sql.append(" AND pb.start_dt < ?");
			p.add(endTime.concat(" 23:59:59"));
		}
		if (StringUtils.isNotBlank(name)) {
			sql.append(" AND pb.name = ?");
			p.add(name);
		}

		/*
		 * public String name; public Date start_dt; public double plan_donate; public
		 * double actual_donate; public int donate_users; public String descr; public
		 * String cover; public String content; public Date ins_dt;
		 */

		String listSql = "SELECT pb.id, pb.name, pb.start_dt, pb.plan_donate, pb.actual_donate, pb.donate_users, pb.descr, pb.cover, pb.ins_dt"
				.concat(sql.toString()).concat(" ORDER BY ").concat(order[orderType]).concat(" LIMIT ?,?");
		String cntSql = "SELECT count(*) as count".concat(sql.toString());

		int count = 0;
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), cntSql, p.toArray());
		if (countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null) {
			count = ((BigInteger) countMap.get(0).get("count")).intValue();
		}

		p.add((currPage - 1) * pageSize);
		p.add(pageSize);
		System.out.println(listSql + ";" + ((currPage - 1) * pageSize) + ";" + pageSize);
		List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), listSql, p.toArray());

		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("name", name);
		conditionMap.put("beginTime", beginTime);
		conditionMap.put("endTime", endTime);
		conditionMap.put("orderType", orderType);

		PageBean<Map<String, Object>> page = new PageBean<Map<String, Object>>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = list;

		return page;
	}

	/**
	 * 
	 * @author liulj @creationDate. 2017年3月20日 下午12:42:21 @description. 查询公益账户
	 * 
	 * @return
	 */
	public static JSONObject findPublicBenefitAccount(Long userId) {
		JSONObject result = new JSONObject();
		// 累计募捐（用户和平台）
		Object total_donate = t_user_donate.find("SELECT SUM(ud.user_donate+ud.admin_donate) FROM t_user_donate ud").first();
		result.put("total_donate", total_donate == null ? 0 : total_donate);
		// 累计募捐笔数（仅用户）
		Object total_num = t_user_donate.find("SELECT COUNT(ud.id) FROM t_user_donate ud").first();
		result.put("total_num", total_num == null ? 0 : total_num);
		// 累计实际捐款（使用）
		Object used_donate = t_user_donate
				.find("SELECT SUM(ud.user_donate+ud.admin_donate) FROM t_user_donate ud WHERE ud.benefit_id > 0").first();
		result.put("used_donate", used_donate == null ? 0 : used_donate);
		// 账户剩余
		Object rem_donate = t_user_donate
				.find("SELECT SUM(ud.user_donate+ud.admin_donate) FROM t_user_donate ud WHERE ud.benefit_id = 0").first();
		result.put("rem_donate", rem_donate == null ? 0 : rem_donate);

		result.put("user_donate_num", 0);
		result.put("user_donate_sum", 0);

		if (userId != null) {
			// 用户捐款笔数
			Object user_donate_num = t_user_donate.find("SELECT COUNT(ud.id) FROM t_user_donate ud WHERE ud.user_id = ?", userId)
					.first();
			result.put("user_donate_num", user_donate_num == null ? 0 : user_donate_num);
			// 用户捐款总额
			Object user_donate_sum = t_user_donate
					.find("SELECT SUM(ud.user_donate) FROM t_user_donate ud WHERE ud.user_id = ?", userId).first();
			result.put("user_donate_sum", user_donate_sum == null ? 0 : user_donate_sum);
		}

		return result;
	}

	/**
	 * 
	 * @author liulj @creationDate. 2017年5月16日 下午3:14:50 @description. 查询用户投资理财信息
	 * 
	 * @param userId
	 * @return
	 */
	public static List<Map<String, Object>> findUserInvests(Long userId, Long bidId, int length) {

		List<Object> p = new ArrayList<Object>();

		StringBuffer sql = new StringBuffer(
				"SELECT   case b.period_unit when  -1 then  '年'  when  0  then  '月' when 1 then  '日' end as period_unit_name,b.period_unit,b.period,b.tag,bu.mobile as borr_mobile,u.mobile as invest_mobile,bu.user_type as b_user_type,u.user_type as  i_user_type, i.user_id,u.name,u.id_number,u.mobile,u.reality_name,");
		sql.append(
				"b.user_id AS b_user_id,bu.`name` AS b_name,bu.id_number AS b_id_number ,bu.mobile AS b_mobile,bu.reality_name AS b_reality_name,b.service_amount,");
		sql.append(
				"i.time,i.amount,i.correct_amount,i.correct_interest,b.id AS bid_id, i.id AS invest_id,b.status,b.apr,dlp.name AS dlp_name,b.repayment_type_id,b.audit_time,");
		sql.append("IF(b.repayment_type_id=1,4,IF(b.repayment_type_id=2,3,IF(b.period_unit=1,1,2))) AS y,");
		sql.append("DATE_FORMAT(b.repayment_time,'%d') AS dd,");
		sql.append("(SELECT MAX(repayment_time) FROM t_bills WHERE bid_id = b.id) AS last_repayment_time,");
		sql.append("(SELECT COUNT(*)+1 FROM t_invests WHERE id < i.id AND bid_id = b.id) AS ser");
		sql.append(" FROM t_invests i");
		sql.append(" LEFT JOIN t_bids b ON b.id = i.bid_id");
		sql.append(" LEFT JOIN t_users u ON u.id = i.user_id");
		sql.append(" LEFT JOIN t_users bu ON bu.id = b.user_id");
		sql.append(" LEFT JOIN t_dict_loan_purposes dlp ON dlp.id = b.loan_purpose_id");
		sql.append(" WHERE (b.`status` = 4 OR b.`status` = 14 OR b.`status` = 5) AND i.pact_location IS NULL");

		if (userId != null && userId != 0) {
			sql.append(" AND i.user_id = ?");
			p.add(userId);
		}
		if (bidId != null && bidId != 0) {
			sql.append(" AND i.bid_id = ?");
			p.add(bidId);
		}

		if (p.size() == 0) {
			sql.append(" LIMIT 0,?");
			p.add(length);
		}

		List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), sql.toString(), p.toArray());
		return list;
	}

	public static List<Map<String, Object>> findUserHistoryInvests(Long userId, Long bidId, int length) {
		List<Object> p = new ArrayList<Object>();

		StringBuffer sql = new StringBuffer(
				"SELECT bu.user_type as b_user_type,u.user_type as  i_user_type, i.id, i.user_id,u.name,u.id_number,u.mobile,u.reality_name,i.pact_location as pactLocation,");
		sql.append(
				"b.user_id AS b_user_id,bu.`name` AS b_name,bu.id_number AS b_id_number ,bu.mobile AS b_mobile,bu.reality_name AS b_reality_name,");
		sql.append(
				"i.time,i.amount,i.correct_amount,i.correct_interest,b.id AS bid_id, i.id AS invest_id,b.status,b.apr,dlp.name AS dlp_name,b.repayment_type_id,b.audit_time,");
		sql.append("IF(b.repayment_type_id=1,4,IF(b.repayment_type_id=2,3,IF(b.period_unit=1,1,2))) AS y,");
		sql.append("DATE_FORMAT(b.repayment_time,'%d') AS dd,");
		sql.append("(SELECT MAX(repayment_time) FROM t_bills WHERE bid_id = b.id) AS last_repayment_time,");
		sql.append("(SELECT COUNT(*)+1 FROM t_invests WHERE id < i.id AND bid_id = b.id) AS ser");
		sql.append(" FROM t_invests i");
		sql.append(" LEFT JOIN t_bids b ON b.id = i.bid_id");
		sql.append(" LEFT JOIN t_users u ON u.id = i.user_id");
		sql.append(" LEFT JOIN t_users bu ON bu.id = b.user_id");
		sql.append(" LEFT JOIN t_dict_loan_purposes dlp ON dlp.id = b.loan_purpose_id");
		sql.append(
				" WHERE (b.`status` = 4 OR b.`status` = 14 OR b.`status` = 5) AND i.pact_location is not null AND i.certificate_url IS NULL ");

		if (userId != null && userId != 0) {
			sql.append(" AND i.user_id = ?");
			p.add(userId);
		}
		if (bidId != null && bidId != 0) {
			sql.append(" AND i.bid_id = ?");
			p.add(bidId);
		}

		if (p.size() == 0) {
			sql.append(" order by i.id desc LIMIT 0,?");
			p.add(length);
		}

		List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), sql.toString(), p.toArray());
		return list;
	}

	/**
	 * 
	 * @author liulj @creationDate. 2017年5月22日 下午3:09:44 @description.
	 * 
	 * @param investId
	 * @param pactLoaction
	 * @return
	 */
	public static int updateUserPact(long invest_id, String pactLoaction, String viewCertificateInfoUrl) {
		String sql = "update t_invests set pact_location = ?,certificate_url = ? where id = ?";
		return JPAUtil.executeUpdate(new ErrorInfo(), sql, pactLoaction, viewCertificateInfoUrl, invest_id);
	}

	/**
	 * 
	 * @author liulj @creationDate. 2017年5月22日 下午4:09:31 @description. 理财账单
	 * 
	 * @param userId
	 * @return
	 */
	public static PageBean<Map<String, Object>> findUserSimpleInvests(Long userId, int pageSize, int currPage) {
		/*
		 * SELECT b.title,b.status,b.audit_time, (SELECT MAX(repayment_time) FROM
		 * t_bills WHERE bid_id = b.id) AS last_repayment_time,
		 * b.apr,i.correct_amount,i.correct_interest,b.id AS bid_id, i.id AS invest_id,
		 * i.pact_location,i.user_id FROM t_invests i LEFT JOIN t_bids b ON b.id =
		 * i.bid_id WHERE i.user_id = 999 AND (b.`status` = 4 OR b.`status` = 14)
		 */
		pageSize = pageSize == 0 ? 10 : pageSize;
		currPage = currPage == 0 ? 1 : currPage;

		StringBuffer sqlSelect = new StringBuffer("SELECT b.title,b.status,b.audit_time, ");
		sqlSelect.append(" (SELECT MAX(repayment_time) FROM t_bills WHERE bid_id = b.id) AS last_repayment_time, ");
		sqlSelect.append(
				" b.apr,i.correct_amount,i.correct_interest,i.correct_increase_interest,b.id AS bid_id, i.id AS invest_id, ");
		sqlSelect.append(" i.pact_location,i.user_id, ");
		sqlSelect.append(
				" case when activity1.rate is not null or activity2.rate is not null THEN 1 else CASE when red_history.coupon_type=2 and red_history.status in (1,2) and red_history.money>0 then 1 else CAST( b.is_increase_rate AS signed ) end end AS isIncreaseRate,  ");
		sqlSelect.append(
				" CASE WHEN activity1.rate IS NOT NULL THEN activity1.rate when  b.increase_rate is not null AND b.increase_rate != 0 then  b.increase_rate WHEN red_history.coupon_type = 2 AND red_history. STATUS IN (1, 2) AND red_history.money > 0  THEN red_history.money ELSE 0 END + IFNULL(activity2.rate,0) AS increaseRate,  ");
		sqlSelect.append(" ifnull(increase_rate_name,'') as increaseRateName, ");
		sqlSelect.append(
				"(SELECT IFNULL(SUM(bi.receive_interest),0)+IFNULL(SUM(bi.receive_increase_interest),0) FROM t_bill_invests bi WHERE bi.invest_id = i.id AND (bi.status = 0 OR bi.status = -3 OR bi.status = -4)) AS correct_interest_ed");

		StringBuffer sqlFrom = new StringBuffer(" FROM t_invests i ");
		sqlFrom.append(" left join t_red_packages_history red_history on red_history.invest_id =i.id  ");
		sqlFrom.append(" left JOIN t_activity_increase_rate_detail activity1 on i.increase_activity_id1= activity1.id  ");
		sqlFrom.append(" left JOIN t_activity_increase_rate_detail activity2 on i.increase_activity_id2= activity2.id  ");
		sqlFrom.append(" LEFT JOIN t_bids b ON b.id = i.bid_id");
		sqlFrom.append(" WHERE i.user_id = ? AND (b.`status` = 4 OR b.`status` = 14 OR b.`status` = 5)");
		sqlFrom.append(" ORDER BY b.audit_time DESC");

		String listSql = sqlSelect.toString().concat(sqlFrom.toString()).concat(" LIMIT ?,?");
		String cntSql = "SELECT count(*) as count".concat(sqlFrom.toString());

		int count = 0;
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), cntSql, userId);
		if (countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null) {
			count = ((BigInteger) countMap.get(0).get("count")).intValue();
		}

		List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), listSql, userId, (currPage - 1) * pageSize, pageSize);

		PageBean<Map<String, Object>> page = new PageBean<Map<String, Object>>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;

		page.page = list;

		return page;
	}

	/**
	 *
	 * @param userId
	 * @param parameters {userIdSign,status,orderby,asc_or_desc,pageSize,currPage}
	 * @param pageSize
	 * @param currPage
	 * @return 筛选排序投资列表
	 * @author zqq @creationDate. 2018年8月4日下午6:22:51
	 */
	public static PageBean<Map<String, Object>> findUserSimpleInvestsByCondition(Long userId, Map<String, String> parameters,
			int pageSize, int currPage) {
		pageSize = pageSize == 0 ? 10 : pageSize;
		currPage = currPage == 0 ? 1 : currPage;
		String status = parameters.get("status"); // default:0:全部,1:还款中,2:已还款,10:可转让,11:转让中,20:已转让,30:不可转让
		String orderby = parameters.get("orderby");// default:0:放款时间,1:最后还款时间,2:下次还款时间,3:待收金额
		String ascOrDesc = parameters.get("asc_or_desc");// "asc"|"desc"

		String investCondition = "";
		String debtInvestCondition = "";
		String orderbyCondition = "";
		StringBuffer sqlSelect = new StringBuffer();
		// -------------------------------------verify
		// ascOrDesc-------------------------------------
		if (ascOrDesc == null || (!ascOrDesc.toLowerCase().equals("asc") && !ascOrDesc.toLowerCase().equals("desc"))) {
			if ("2".equals(orderby)) { // orderby:2:默认正序
				ascOrDesc = "asc";
			} else { // orderby:0:默认倒序,1:默认倒序,3:默认倒序
				ascOrDesc = "desc";
				;
			}
		}
		// ---------------------status->investCondition &
		// debtInvestCondition-------------------------
		if ("1".equals(status)) {// 1:还款中
			investCondition += " AND bid.status in(4,14)	AND (debt_tran.STATUS IS NULL OR debt_tran.STATUS <> 3) ";
			debtInvestCondition += " AND bid.status in(4,14) ";
		} else if ("2".equals(status)) {// 2:已还款
			investCondition += " AND (bid.status = 5  or debt_tran.STATUS = 3) ";
			debtInvestCondition += " AND bid.status =5 ";
		} else if ("10".equals(status)) {// 10:可转让
			investCondition += " AND can_debt_id.id IS NOT NULl and debt_tran.id is null ";
			debtInvestCondition += " AND false ";
		} else if ("11".equals(status)) {// 11:转让中
			investCondition += " AND can_debt_id.id IS NOT NULl and debt_tran.STATUS IN ( 1, 2 ) ";
			debtInvestCondition += " AND false ";
		} else if ("20".equals(status)) {// 20:已转让
			investCondition += " AND can_debt_id.id IS NOT NULl and debt_tran.STATUS = 3  ";
			debtInvestCondition += " AND false ";
		} else if ("30".equals(status)) {// 30:不可转让
			investCondition += " AND (can_debt_id.id IS     NULl or  debt_tran.id is not null) ";
			debtInvestCondition += " AND true ";
		} else { // default:0:全部
			investCondition += " AND true ";
			debtInvestCondition += " AND true ";
		}
		// -----------------------------orderby +
		// ascOrDesc->orderbyCondition----------------------------
		if ("1".equals(orderby)) {// 1:最后还款时间
			orderbyCondition += "  (case when status =5 then 1 " + " when is_debt_bill=0 and debt_tran_status=3 then 1 "
					+ " else 0 " + " end) ,last_repayment_time " + ascOrDesc + " ";
		} else if ("2".equals(orderby)) {// 2:下次还款时间
			orderbyCondition += "  (case when status =5 then 1 " + " when is_debt_bill=0 and debt_tran_status=3 then 1 "
					+ " else 0 " + " end) ,next_repay_time " + ascOrDesc + " ,audit_time desc ";
		} else if ("3".equals(orderby)) {// 3:待收金额
			orderbyCondition += "  (case when status =5 then 1 " + " when is_debt_bill=0 and debt_tran_status=3 then 1 "
					+ " else 0 " + " end) ,havnt_amount " + ascOrDesc + " ,audit_time	desc ";
		} else {// default:0:放款时间
			orderbyCondition += "  (case when status =5 then 1 " + " when is_debt_bill=0 and debt_tran_status=3 then 1 "
					+ " else 0 " + " end) ,audit_time " + ascOrDesc + " ";
		}
		// ----------------------------------------sqlSelect
		// begin------------------------------------------
		sqlSelect.append(" 	SELECT ");
		sqlSelect.append(" 		bid.id AS bid_id, ");
		sqlSelect.append(" 		bid.title, ");
		sqlSelect.append(" 		case when debt_tran.STATUS = 3 then 5 else bid.STATUS end as STATUS, "); // #4,14还款中|5已还款
																											// ,如果已转让,算已还款
		sqlSelect.append(" 		bid.audit_time, ");
		sqlSelect.append(" 		bid.apr, ");
		sqlSelect.append(
				" 		CASE WHEN activity1.rate IS NOT NULL THEN activity1.rate when  bid.increase_rate is not null AND bid.increase_rate != 0 then  bid.increase_rate WHEN red_history.coupon_type = 2 AND red_history. STATUS IN (1, 2) AND red_history.money > 0  THEN red_history.money ELSE 0 END rate1, ");
		sqlSelect.append(" 		IFNULL(activity2.rate,0) rate2, ");
		sqlSelect.append(
				" 		case when activity1.rate is not null or activity2.rate is not null THEN 1 else CASE when red_history.coupon_type=2 and red_history.status in (1,2) and red_history.money>0 then 1 else CAST( bid.is_increase_rate AS signed ) end end AS isIncreaseRate, ");
		sqlSelect.append(
				" 		CASE WHEN activity1.rate IS NOT NULL THEN activity1.rate when  bid.increase_rate is not null AND bid.increase_rate != 0 then  bid.increase_rate WHEN red_history.coupon_type = 2 AND red_history. STATUS IN (1, 2) AND red_history.money > 0  THEN red_history.money ELSE 0 END + IFNULL(activity2.rate,0) AS increaseRate,  ");
		sqlSelect.append(" 		bid.increase_rate_name AS increaseRateName, ");

		sqlSelect.append(" 		invest.id AS invest_id, ");
		sqlSelect.append(" 		invest.user_id, ");
		sqlSelect.append(" 		invest.correct_amount, ");
		sqlSelect.append(" 		invest.correct_interest, ");
		sqlSelect.append(" 		invest.correct_increase_interest, ");
		sqlSelect.append(" 		IFNULL(invest.correct_increase_interest1,0) correct_increase_interest1, ");
		sqlSelect.append(" 		IFNULL(invest.correct_increase_interest2,0) correct_increase_interest2, ");
		sqlSelect.append(" 		invest.pact_location, ");

		sqlSelect.append(" 		bill_invest.last_repayment_time, ");
		sqlSelect.append(" 		bill_invest.next_repay_time, ");

		sqlSelect.append(
				" 		ifnull(bill_invest.hav_receive_interest,0)+ifnull(bill_invest.hav_receive_increase_interest,0)  hav_correct_interest, ");
		sqlSelect.append(
				" 		ifnull(bill_invest.havnt_receive_corpus,0)+ifnull(bill_invest.havnt_receive_interest,0)+ifnull(bill_invest.havnt_receive_increase_interest,0)  havnt_amount, ");
		sqlSelect.append(" 		CASE ");
		sqlSelect.append(" 			WHEN can_debt_id.id IS NULL THEN 0 "); // #不可转让
		sqlSelect.append(" 			WHEN debt_tran.STATUS IN ( 1, 2 ) THEN 2 "); // #转让中
		sqlSelect.append(" 			WHEN debt_tran.STATUS = 3 THEN 3 "); // #已转让
		sqlSelect.append(" 			ELSE 1 "); // #可转让
		sqlSelect.append(" 		END AS can_debt_transfer, ");
		sqlSelect.append(" 		debt_tran.status debt_tran_status, ");
		sqlSelect.append(" 		0 AS is_debt_bill ");
		sqlSelect.append(" 	FROM t_invests invest ");
		sqlSelect.append(" 	left join t_red_packages_history red_history on red_history.invest_id =invest.id ");
		sqlSelect.append(" 	left JOIN t_activity_increase_rate_detail activity1 on invest.increase_activity_id1= activity1.id ");
		sqlSelect.append(" 	left JOIN t_activity_increase_rate_detail activity2 on invest.increase_activity_id2= activity2.id ");
		sqlSelect.append(" 	inner JOIN t_bids bid ON bid.id = invest.bid_id ");
		sqlSelect.append(
				" 	LEFT JOIN t_debt_transfer debt_tran ON debt_tran.invest_id = invest.id AND debt_tran.STATUS IN ( 1, 2, 3 ) ");
		// #show full columns from t_debt_transfer
		// #标的状态（1待审核 2转让中 3成功，包括:还款中，已还款 4未通过 5 转让失败（流标，撤标）
		sqlSelect.append(" 	LEFT JOIN( ");
		sqlSelect.append(" 		SELECT bill_invest.invest_id, ");
		sqlSelect.append(" 		MAX( bill_invest.receive_time) as last_repayment_time, ");
		sqlSelect.append(
				" 		MIN( case when bill_invest.STATUS in(-1,-2) then bill_invest.receive_time else null end) as next_repay_time, "); // #约定下次付款时间,未还,逾期未还
		sqlSelect.append(
				" 		SUM( case when bill_invest.STATUS in(0,-3,-4,-7,-8) then IFNULL(bill_invest.real_receive_corpus,0) else 0 end  )  as hav_receive_corpus, ");
		sqlSelect.append(
				" 		SUM( case when bill_invest.STATUS in(0,-3,-4,-7,-8) then IFNULL(bill_invest.real_receive_interest,0) else 0 end  ) as hav_receive_interest, ");
		sqlSelect.append(
				" 		SUM( case when bill_invest.STATUS in(0,-3,-4,-7,-8) then IFNULL(bill_invest.real_increase_interest,0) else 0 end  )  as hav_receive_increase_interest, ");
		sqlSelect.append(
				" 		SUM( case when bill_invest.STATUS in(-1,-2) then IFNULL(bill_invest.receive_corpus,0) else 0 end  )  as havnt_receive_corpus, ");
		sqlSelect.append(
				" 		SUM( case when bill_invest.STATUS in(-1,-2) then IFNULL(bill_invest.receive_interest,0) else 0 end  ) as havnt_receive_interest, ");
		sqlSelect.append(
				" 		SUM( case when bill_invest.STATUS in(-1,-2) then IFNULL(bill_invest.receive_increase_interest,0) else 0 end  )  as havnt_receive_increase_interest ");
		sqlSelect.append(" 		FROM t_bill_invests bill_invest ");
		sqlSelect.append(" 		WHERE true ");
		// #show full columns from t_bill_invests
		// #收款状态:-1 未收款 -2 逾期未收款-3 本金垫付收款-4 逾期收款 -5 待收款 -6 逾期待收款 -7 已转让出 0 正常收款
		sqlSelect.append(" 		group by bill_invest.invest_id ");
		sqlSelect.append(" 	) AS bill_invest on bill_invest.invest_id = invest.id ");
		sqlSelect.append(" 	LEFT JOIN ( ");
		sqlSelect.append(" 		select bid.id ");
		sqlSelect.append(" 		from t_bids bid ");
		sqlSelect.append(" 		left join ( ");
		sqlSelect.append(" 			SELECT ");
		sqlSelect.append(" 				bill.bid_id, ");
		sqlSelect.append(" 				SUM( CASE WHEN bill.STATUS = - 1 THEN 1 ELSE 0 END ) AS haventpay, "); // #已付次数
		sqlSelect.append(" 				SUM( CASE WHEN bill.STATUS IN ( - 2, - 3, 0 ) THEN 1 ELSE 0 END ) AS havepay, "); // #已付次数
		sqlSelect.append(
				" 				SUM( CASE WHEN bill.STATUS = - 1 AND bill.overdue_mark != 0 THEN 1 ELSE 0 END ) AS nowbadpay, "); // #逾期未付次数
		// #bill_hvntpay.next_repay_time,#约定下次付款时间
		sqlSelect.append(
				" 				MIN(CASE WHEN bill.status=-1 then bill.repayment_time else null end ) as next_repay_time, ");
		// #bill_hvpay.previous_repay_time,#约定上次付款时间
		// #bill_hvpay.previous_real_repay_time,#实际上次付款时间
		sqlSelect.append(" 				MAX(bill.repayment_time) as last_repayment_time "); // #约定末期付款时间
		sqlSelect.append(" 			FROM t_bills bill ");
		sqlSelect.append(" 			WHERE true ");
		sqlSelect.append(" 			GROUP BY bill.bid_id ");
		sqlSelect.append(" 		)bill on bid.id=bill.bid_id ");
		sqlSelect.append(" 		WHERE true ");
		sqlSelect.append(" 		AND ");
		sqlSelect.append(" 		( ");
		sqlSelect.append(" 			(bid.repayment_type_id = 3 "); // #一次性还款
		sqlSelect.append(" 			AND date_add( now( ), INTERVAL - 30 DAY ) > bid.audit_time "); // #发款日期
		sqlSelect.append(" 			AND date_add( now( ), INTERVAL 33 DAY ) < bill.next_repay_time) ");
		sqlSelect.append(" 		OR ");
		sqlSelect.append(" 			(bid.repayment_type_id IN ( 1, 2 ) "); // #1按月还款、等额本息 2按月付息、到期还本
		sqlSelect.append(" 			AND bill.haventpay > 0 ");
		sqlSelect.append(" 			AND bill.havepay > 0 ");
		sqlSelect.append(" 			AND bill.nowbadpay = 0 ");
		sqlSelect.append(" 			AND date_add( now(), INTERVAL 3 DAY ) < bill.next_repay_time ) ");
		sqlSelect.append(" 		) ");
		sqlSelect.append(" 		AND bid.is_debt_transfer = 1 ");
		sqlSelect.append(" 	)can_debt_id on can_debt_id.id=bid.id ");
		sqlSelect.append(" 	WHERE true ");
		sqlSelect.append(" 	AND bid.status in(4,5,14) ");
		sqlSelect.append(" 	AND invest.user_id = ? ");
		sqlSelect.append(investCondition);

		sqlSelect.append(" 	union all ");

		sqlSelect.append(" 	SELECT ");
		sqlSelect.append(" 		debt_tran.id AS bid_id, ");
		sqlSelect.append(" 		debt_tran.title, ");
		// #debt_tran.STATUS,
		sqlSelect.append(" 		bid.status, ");
		sqlSelect.append(" 		debt_tran.time AS audit_time, ");
		sqlSelect.append(" 		debt_tran.apr, ");
		sqlSelect.append(" 		0 AS rate1, ");
		sqlSelect.append(" 		0 AS rate2, ");
		sqlSelect.append(" 		0 as isIncreaseRate, ");
		sqlSelect.append(" 		0 as increaseRate, ");
		sqlSelect.append(" 		'' as increaseRateName, ");

		sqlSelect.append(" 		debt_invest.id AS invest_id, ");
		sqlSelect.append(" 		debt_invest.user_id, ");
		sqlSelect.append(" 		debt_invest.correct_amount, ");
		sqlSelect.append(" 		debt_invest.correct_interest, ");
		sqlSelect.append(" 		0 as correct_increase_interest, ");
		sqlSelect.append(" 		0 as correct_increase_interest1, ");
		sqlSelect.append(" 		0 as correct_increase_interest2, ");
		sqlSelect.append(" 		null pact_location, ");

		sqlSelect.append(" 		debt_bill_invest.last_repayment_time, ");
		sqlSelect.append(" 		debt_bill_invest.next_repay_time, ");

		sqlSelect.append(
				" 		IFNULL(debt_bill_invest.hav_receive_interest,0)+IFNULL(debt_bill_invest.hav_receive_increase_interest,0) AS hav_correct_interest, ");
		sqlSelect.append(
				" 		IFNULL(debt_bill_invest.havnt_receive_corpus,0)+IFNULL(debt_bill_invest.havnt_receive_interest,0)+IFNULL(debt_bill_invest.havnt_receive_increase_interest,0)   AS havnt_amount, ");
		sqlSelect.append(" 		0 as can_debt_transfer, ");
		sqlSelect.append(" 		debt_tran.STATUS as debt_tran_status, ");
		sqlSelect.append(" 		1 AS is_debt_bill ");
		sqlSelect.append(" 	FROM ");
		sqlSelect.append(" 		t_debt_invest debt_invest ");
		sqlSelect.append(" 	INNER JOIN t_debt_transfer debt_tran ON debt_invest.debt_id = debt_tran.id ");
		sqlSelect.append(" 	inner JOIN t_bids bid ON bid.id = debt_tran.bid_id ");
		sqlSelect.append(" 	LEFT JOIN( ");
		sqlSelect.append(" 		SELECT ");
		sqlSelect.append(" 			debt_bill_invest.debt_invest_id, ");
		sqlSelect.append(
				" 			SUM(case when debt_bill_invest.status in (0,-3,-4) then IFNULL(debt_bill_invest.receive_interest,0) else 0 end)	hav_receive_interest, ");
		sqlSelect.append(
				" 			SUM(case when debt_bill_invest.status in (0,-3,-4) then IFNULL(debt_bill_invest.receive_increase_interest,0) else 0 end)	hav_receive_increase_interest, ");
		sqlSelect.append(
				" 			SUM(case when debt_bill_invest.status in (-1,-2) then IFNULL(debt_bill_invest.receive_corpus,0) else 0 end) havnt_receive_corpus, ");
		sqlSelect.append(
				" 			SUM(case when debt_bill_invest.status in (-1,-2) then IFNULL(debt_bill_invest.receive_interest,0) else 0 end)	havnt_receive_interest, ");
		sqlSelect.append(
				" 			SUM(case when debt_bill_invest.status in (-1,-2) then IFNULL(debt_bill_invest.receive_increase_interest,0) else 0 end)	havnt_receive_increase_interest, ");
		sqlSelect.append(
				" 			MIN(case when debt_bill_invest.status in (-1,-2) then debt_bill_invest.receive_time else null end) next_repay_time, ");
		sqlSelect.append(" 			MAX(debt_bill_invest.receive_time) last_repayment_time ");
		sqlSelect.append(" 		FROM	t_debt_bill_invest debt_bill_invest ");
		sqlSelect.append(" 		WHERE true ");
		sqlSelect.append(" 		group by debt_bill_invest.debt_invest_id ");
		sqlSelect.append(" 	)debt_bill_invest on debt_bill_invest.debt_invest_id = debt_invest.id ");
		sqlSelect.append(" 	WHERE true ");
		sqlSelect.append(" 		AND debt_invest.user_id = ? ");
		sqlSelect.append(" 		AND debt_tran.STATUS = 3 ");
		sqlSelect.append(debtInvestCondition);
		sqlSelect.append(" order by ");
		sqlSelect.append(orderbyCondition);

		// -------------------------------------------------query-----------------------------------
		String limitSql = sqlSelect + " LIMIT ?,? ";
		String cntSql = " SELECT count(1) as count from (" + sqlSelect + ")cnt ";
		Logger.info("app筛选排序理财账单sql: " + limitSql);
		int count = 0;
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), cntSql, userId, userId);
		if (countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null) {
			count = ((BigInteger) countMap.get(0).get("count")).intValue();
		}

		List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), limitSql, userId, userId, (currPage - 1) * pageSize,
				pageSize);

		PageBean<Map<String, Object>> page = new PageBean<Map<String, Object>>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;

		page.page = list;

		return page;
	}

	/**
	 * @author liulj @creationDate. 2017年5月22日 下午4:22:55 @description. 回款计划
	 * 
	 * @param userId
	 * @return
	 */
	public static List<Map<String, Object>> findUserInvestsReturned(Long userId, Long invest_id) {
		/*
		 * SELECT id,receive_time,SUM(receive_corpus) AS
		 * receive_corpus,SUM(receive_interest) AS
		 * receive_interest,user_id,bid_id,status FROM t_bill_invests WHERE bid_id = 570
		 * AND user_id = 999 GROUP BY bid_id,DATE_FORMAT(receive_time,'%m-%d-%Y') ORDER
		 * BY receive_time
		 */
		StringBuffer sql = new StringBuffer(
				"SELECT bi.id,v.id,receive_time,receive_corpus,receive_interest,bi.user_id,bi.bid_id,receive_increase_interest,case when v.increase_activity_id1 IS NOT NULL  then '全场加息' when b.is_increase_rate = 1 then '标的加息' when h.id is not null then '加息券加息' else '' END name1,receive_increase_interest1,case when v.increase_activity_id2 IS NOT NULL then case when activity2.type = 2 then '首投加息' else '尾投加息' end else '' end  name2, receive_increase_interest2,");
		sql.append(
				"CASE WHEN ( SELECT red_history.money FROM t_red_packages_history red_history WHERE red_history.coupon_type = 2 AND red_history. STATUS IN (1, 2) AND red_history.invest_id = bi.invest_id ) > 0 THEN TRUE WHEN ( SELECT is_increase_rate FROM t_bids WHERE id = bi.bid_id ) = 1 THEN TRUE when v.increase_activity_id1 is not null or  v.increase_activity_id2 is not null then TRUE ELSE FALSE END AS isIncreaseRate, ");

		sql.append(" bi.status as status ");
		sql.append(" FROM t_bill_invests bi ");
		sql.append(" LEFT JOIN t_invests v ON bi.invest_id = v.id ");
		sql.append(" left JOIN t_bids b on v.bid_id = b.id ");
		sql.append(" left JOIN t_red_packages_history h on h.invest_id = v.id and h.coupon_type = 2 and h.`status` in (1,2) ");
		sql.append(
				" LEFT JOIN (SELECT a.type,a.`name`,b.id from t_activity_increase_rate a JOIN t_activity_increase_rate_detail b on a.id = b.activity_id ) activity1 on v.increase_activity_id1 = activity1.id ");
		sql.append(
				" LEFT JOIN (SELECT a.type,a.`name`,b.id from t_activity_increase_rate a JOIN t_activity_increase_rate_detail b on a.id = b.activity_id ) activity2 on v.increase_activity_id2 = activity2.id ");
		sql.append(" WHERE bi.invest_id = ? AND bi.user_id = ?");
		sql.append(" ORDER BY receive_time");
		List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), sql.toString(), invest_id, userId);
		return list;
	}

	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年5月6日
	 * @description 按生日查询用户
	 * @param date
	 * @return
	 */
	public static List<t_users> findBirthDay(String date) {
		String sql = "from t_users where SUBSTR(id_number,11,4) = ?";
		List<t_users> users = t_users.find(sql, date).fetch();
		return users;
	}

	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年5月9日
	 * @description 查询可领红包用户
	 * @param red_id
	 * @return
	 */
	public static List<User> findCustomRedUser(long red_id) {
		String sql = "select u.id,u.name from t_users u left JOIN (select id,user_id from t_red_packages_history where type_id = ?) h on u.id = h.user_id where  h.id is null limit 100";
		List<Map<String, Object>> lists = JPAUtil.getList(new ErrorInfo(), sql, red_id);
		List<User> users = new ArrayList<User>();
		for (Map<String, Object> map : lists) {
			Long userId = ((BigInteger) map.get("id")).longValue();
			String name = (String) map.get("name");
			User user = new User();
			user._id = userId;
			user._name = name;
			users.add(user);
		}
		return users;
	}

	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年5月9日
	 * @description 验证用户是否能领取自定义红包
	 * @param red_id
	 * @param user_id
	 * @return
	 */
	public static User findCustomRedUser(long red_id, long user_id) {
		String sql = "select count(1) from t_red_packages_history where type_id=? and user_id=?";
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), sql, red_id, user_id);
		int count = 0;
		if (countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null) {
			count = ((BigInteger) countMap.get(0).get("count")).intValue();
		}
		if (count == 0) {
			User user = new User();
			user.id = user_id;
			return user;
		}
		return null;
	}

	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年6月14日
	 * @description 查询所有未实名认证用户
	 * @return
	 */
	public static List<t_users> findNoAuthUser() {
		String sql = "from t_users where reality_name is null";
		List<t_users> users = t_users.find(sql).fetch();
		return users;
	}

	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年6月14日
	 * @description 查询所有未绑定银行卡用户
	 * @return
	 */
	public static List<Map<String, Object>> findNoBankUser() {
		String sql = "SELECT u.name FROM t_users u LEFT JOIN t_user_bank_accounts b on u.id = b.user_id where b.id is null group by u.id";
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), sql);
		return countMap;
	}

	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年6月14日
	 * @description 查询所有未投资用户
	 * @return
	 */
	public static List<Map<String, Object>> findNoInvestUser() {
		// String sql = "SELECT u.name FROM t_users u LEFT JOIN t_invests i on i.user_id
		// = u.id LEFT JOIN t_bids b on i.bid_id = b.id and b.`status` in(?,?) where
		// b.id is null group by u.id";
		String sql = "SELECT u.name FROM t_users u where (select IFNULL(SUM(i.amount),0) from t_invests i join t_bids b on i.bid_id = b.id where u.id = i.user_id and b.status in(?,?,?)) =0";
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), sql, Constants.BID_REPAYMENT,
				Constants.BID_COMPENSATE_REPAYMENT, Constants.BID_REPAYMENTS);
		return countMap;
	}

	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年8月16日
	 * @description 查询待付款提现列表
	 * @param supervisorId
	 * @param status
	 * @param beginTimeStr
	 * @param endTimeStr
	 * @param key
	 * @param error
	 * @return
	 */
	public static List<v_user_withdrawal_info> queryWithdrawalBySupervisor(long supervisorId, int status, String beginTimeStr,
			String endTimeStr, String key, ErrorInfo error) {
		error.clear();
		/* 判断是提现记录的状态 */
		String timeStr = "";
		switch (status) {
		case Constants.WITHDRAWAL_SPEND:
			timeStr = "w.pay_time";
			break;

		case Constants.WITHDRAWAL_NOT_PASS:
			timeStr = "w.audit_time";
			break;

		case Constants.WITHDRAWAL_CHECK_PENDING:

		case Constants.WITHDRAWAL_PAYING:
			timeStr = "w.time";
			break;

		default:
			timeStr = "w.time";
			break;
		}

		Date beginTime = null;
		Date endTime = null;

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_WITHDRAWAL_INFO_V2);

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("key", key);

		if (beginTime != null) {
			sql.append(" and " + timeStr + " >= ? ");
			params.add(beginTime);
		}

		if (endTime != null) {
			sql.append(" and " + timeStr + " <= ? ");
			params.add(endTime);
		}

		if (StringUtils.isNotBlank(key)) {
			sql.append(" and t_users.name like ? or t_users.email like ? or t_users.mobile like ? ");
			params.add("%" + key + "%");
			params.add("%" + key + "%");
			params.add("%" + key + "%");
		}

		sql.append(" and w.status = ? ");
		params.add(status);
		sql.append(" order by w.time desc");

		List<v_user_withdrawal_info> withdrawals = new ArrayList<v_user_withdrawal_info>();
		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_user_withdrawal_info.class);

			Logger.info("待审核提现列表" + sql);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			withdrawals = query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询提现记录时：" + e.getMessage() + " id:1" + sql + "==================================");
			error.code = -1;
			error.msg = "查询提现记录时出现异常！";

			return null;
		}

		// if(withdrawals != null && withdrawals.size() > 0){
		// for(v_user_withdrawal_info ui : withdrawals){
		// User.withdrawalNoticeForNormalGateway(ui.user_id, 0, ui.id, "1,2", error);
		// //普通网关提现
		// }
		// }
		// try {
		// JPAUtil.transactionCommit();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		error.code = 0;
		return withdrawals;
	}

	public static void updateRealityName(long id, String reality_name, String id_number, ErrorInfo error) {

		if (StringUtils.isBlank(reality_name)) {
			error.code = -1;
			error.msg = "真实不能为空";
			return;
		}
		if (StringUtils.isBlank(id_number)) {
			error.code = -1;
			error.msg = "身份证号不能为空";
			return;
		}
		String sql = "update t_users set reality_name = ?, id_number = ? where id = ?";
		Query query = JPA.em().createQuery(sql).setParameter(1, reality_name).setParameter(2, id_number).setParameter(3, id);
		int rows = 0;
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，修改失败！";
		}
		if (rows != 1) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，修改失败！";
		} else {
			error.code = 0;
			error.msg = "修改成功！";
		}
	}

	/**
	 * add by wangyun 查询管理员可见城市用户列表
	 * 
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_user_info_city> queryUserBySupviCity(String name, String provinceId, String cityId,
			String beginTimeStr, String endTimeStr, String beginLoginTimeStr, String endLoginTimeStr, String key,
			String orderTypeStr, String currPageStr, String pageSizeStr, ErrorInfo error, String isBank,
			String recommend_user_mobile, String risk_result) {
		error.clear();

		Date beginTime = null;
		Date endTime = null;
		Date beginLoginTime = null;
		Date endLoginTime = null;
		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}
		if (RegexUtils.isDate(beginLoginTimeStr)) {
			beginLoginTime = DateUtil.strDateToStartDate(beginLoginTimeStr);
		}

		if (RegexUtils.isDate(endLoginTimeStr)) {
			endLoginTime = DateUtil.strDateToEndDate(endLoginTimeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (orderType < 0 || orderType > 5) {
			orderType = 0;
		}

		StringBuffer sql = new StringBuffer();
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_INFO_V4);

		sql.append(" and EXISTS ( SELECT usercity.city_id FROM t_user_city usercity"
				+ " LEFT JOIN t_supervisor_citys supcity ON supcity.city_id = usercity.city_id "
				+ "WHERE supcity.supervisor_id = " + Supervisor.currSupervisor().id + " AND `t_users`.id = usercity.user_id ) ");

		List<Object> paramsCount = new ArrayList<Object>();
		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		/* 为sql条件添加参数 */
		for (int i = 1; i <= 4; i++) {
			params.add(Constants.BID_REPAYMENT);
			params.add(Constants.BID_REPAYMENTS);
			params.add(Constants.BID_COMPENSATE_REPAYMENT);
		}

		conditionMap.put("name", name);
		conditionMap.put("provinceId", provinceId);
		conditionMap.put("cityId", cityId);
		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("beginLoginTime", beginLoginTimeStr);
		conditionMap.put("endLoginTime", endLoginTimeStr);
		conditionMap.put("key", key);
		conditionMap.put("orderType", orderType);
		conditionMap.put("isBank", isBank);

		conditionMap.put("recommend_user_mobile", recommend_user_mobile);
		conditionMap.put("risk_result", risk_result);

		if (StringUtils.isNotBlank(cityId)) {
			sql.append("and t_user_city.city_id = ? ");
			params.add(cityId);
			paramsCount.add(cityId);
		}
		if (StringUtils.isNotBlank(provinceId)) {
			sql.append("and t_user_city.province_id = ? ");
			params.add(provinceId);
			paramsCount.add(provinceId);
		}
		if (StringUtils.isNotBlank(key)) {
			sql.append("and t_users.name like ? ");
			params.add("%" + key + "%");
			paramsCount.add("%" + key + "%");
		}

		if (StringUtils.isNotBlank(name)) {
			sql.append("and (t_users.name like ? or t_users.email like ? or t_users.mobile like ?) ");
			params.add("%" + name + "%");
			params.add("%" + name + "%");
			params.add("%" + name + "%");
			paramsCount.add("%" + name + "%");
			paramsCount.add("%" + name + "%");
			paramsCount.add("%" + name + "%");
		}

		if (StringUtils.isNotBlank(recommend_user_mobile)) {
			sql.append(" and instr(recommend.mobile,?) > 0 ");
			params.add(recommend_user_mobile);
			paramsCount.add(recommend_user_mobile);
		}

		if (beginTime != null) {
			sql.append("and t_users.time > ? ");
			params.add(beginTime);
			paramsCount.add(beginTime);
		}

		if (endTime != null) {
			sql.append("and t_users.time < ? ");
			params.add(endTime);
			paramsCount.add(endTime);
		}

		if (beginLoginTime != null) {
			sql.append("and t_users.last_login_time > ? ");
			params.add(beginLoginTime);
			paramsCount.add(beginLoginTime);
		}

		if (endLoginTime != null) {
			sql.append("and t_users.last_login_time < ? ");
			params.add(endLoginTime);
			paramsCount.add(endLoginTime);
		}

		if (StringUtils.isNotBlank(risk_result)) {
			sql.append("and t_users.risk_result = ? ");
			params.add(risk_result);
			paramsCount.add(risk_result);
		}

		Logger.info("orderType:" + orderType);
		sql.append("order by " + Constants.USER_CITY_ADDR[orderType]);

		List<v_user_info_city> users = new ArrayList<v_user_info_city>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Logger.info("查询管理员可见城市用户列表 sql:" + sql.toString());
			Query query = em.createNativeQuery(sql.toString(), v_user_info_city.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), paramsCount);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询管理员可见城市用户列表时出现异常！";

			return null;
		}

		PageBean<v_user_info_city> page = new PageBean<v_user_info_city>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		Logger.info("page.conditions: " + page.conditions);
		page.page = users;
		error.code = 0;

		return page;
	}

	public static PageBean<t_invest_user> queryUserInvest(String name, String recommend_user_name, String beginTimeStr,
			String endTimeStr, String is_only_new_user, String red_amount, String is_increase_rate, String period_unit,
			String repayment_type_name, String is_valid, String currPageStr, String pageSizeStr, String orderTypeStr,
			ErrorInfo error) {

		Date beginTime = null;
		Date endTime = null;
		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if (RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (orderType < 0 || orderType > 7) {
			orderType = 0;
		}
		StringBuffer sql = new StringBuffer();
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_INVEST);

		List<Object> paramsCount = new ArrayList<Object>();
		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("name", name);
		conditionMap.put("beginTime", beginTimeStr);
		conditionMap.put("endTime", endTimeStr);
		conditionMap.put("orderType", orderType);
		conditionMap.put("is_valid", is_valid);
		conditionMap.put("recommend_user_name", recommend_user_name);
		conditionMap.put("repayment_type_name", repayment_type_name);
		conditionMap.put("pageSize", pageSizeStr);
		conditionMap.put("currPage", currPageStr);
		conditionMap.put("is_only_new_user", is_only_new_user);
		conditionMap.put("is_increase_rate", is_increase_rate);
		conditionMap.put("period_unit", period_unit);
		conditionMap.put("red_amount", red_amount);

		if (StringUtils.isNotBlank(name)) {
			sql.append("and (t_users.name like ? or t_users.email like ? or t_users.mobile like ?) ");
			params.add("%" + name + "%");
			params.add("%" + name + "%");
			params.add("%" + name + "%");
			paramsCount.add("%" + name + "%");
			paramsCount.add("%" + name + "%");
			paramsCount.add("%" + name + "%");
		}

		if (StringUtils.isNotBlank(recommend_user_name)) {
			sql.append(" and recommend.`name` = ? ");
			params.add(recommend_user_name);
			paramsCount.add(recommend_user_name);
		}

		if (beginTime != null) {
			sql.append("and t_invests.time > ? ");
			params.add(beginTime);
			paramsCount.add(beginTime);
		}

		if (endTime != null) {
			sql.append("and t_invests.time < ? ");
			params.add(endTime);
			paramsCount.add(endTime);
		}

		if (StringUtils.isNotBlank(is_only_new_user)) {
			sql.append(" and t_bids.is_only_new_user = ? ");
			params.add(is_only_new_user);
			paramsCount.add(is_only_new_user);
		}

		if (StringUtils.isNotBlank(red_amount)) {
			if (red_amount.equals("1")) {
				sql.append(" and red.money > 0 ");
			} else {
				sql.append(" and IFNULL(red.money,'0') = 0 ");
			}

		}
		if (StringUtils.isNotBlank(is_increase_rate)) {
			sql.append(" and t_bids.is_increase_rate = ? ");
			params.add(is_increase_rate);
			paramsCount.add(is_increase_rate);
		}

		if (StringUtils.isNotBlank(period_unit)) {
			sql.append(" and t_bids.period_unit = ? ");
			params.add(period_unit);
			paramsCount.add(period_unit);
		}

		if (StringUtils.isNotBlank(is_valid)) {
			if (is_valid.equals("1")) {
				sql.append(" and  t_bids.`status` in (?, ?, ?) ");

			} else if (is_valid.equals("0")) {
				sql.append(" and  t_bids.`status` not in (?, ?, ?) ");
			}
			params.add(Constants.BID_REPAYMENT);
			params.add(Constants.BID_REPAYMENTS);
			params.add(Constants.BID_COMPENSATE_REPAYMENT);

			paramsCount.add(Constants.BID_REPAYMENT);
			paramsCount.add(Constants.BID_REPAYMENTS);
			paramsCount.add(Constants.BID_COMPENSATE_REPAYMENT);
		}

		if (StringUtils.isNotBlank(repayment_type_name)) {// 传值是1,2,3
			sql.append(" and t_bids.repayment_type_id = ? ");
			params.add(repayment_type_name);
			paramsCount.add(repayment_type_name);
		}

		Logger.info("orderType:" + orderType);
		sql.append("order by " + Constants.USER_INVEST[orderType]);

		List<t_invest_user> users = new ArrayList<t_invest_user>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Logger.info("查询客服用户投资列表 sql:" + sql.toString());
			Query query = em.createNativeQuery(sql.toString(), t_invest_user.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), paramsCount);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询管理员可见城市用户列表时出现异常！";

			return null;
		}

		PageBean<t_invest_user> page = new PageBean<t_invest_user>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		Logger.info("page.conditions: " + page.conditions);
		page.page = users;
		error.code = 0;

		return page;
	}

	/**
	 * 修改用户类型 维护企业和个体工商户的 注册地
	 * 
	 * @param userId
	 * @param userType
	 */
	public static void updateUserType(long userId, int userType, int proviceId, int cityId) {
		String sql = "update t_users set user_type = ? where id = ?";
		Query query = t_users.em().createNativeQuery(sql);
		query.setParameter(1, userType);
		query.setParameter(2, userId);
		query.executeUpdate();

		ErrorInfo error = new ErrorInfo();
		t_user_city userCity = new t_user_city();
		userCity.city_id = String.valueOf(cityId);
		userCity.province_id = String.valueOf(proviceId);
		userCity.user_id = userId;
		UserCitys.addUserCity(error, userCity);
	}

	/**
	 * 修改用户类型
	 * 
	 * @param userId
	 * @param userType
	 */
	public static void updateUserType(long userId, int userType) {
		String sql = "update t_users set user_type = ? where id = ?";
		Query query = t_users.em().createNativeQuery(sql);
		query.setParameter(1, userType);
		query.setParameter(2, userId);
		query.executeUpdate();
	}

	/**
	 * 根据userId查询一个 用户信息
	 * 
	 * @param userId
	 * @return
	 */
	public static t_users getUserByIdUserId(long userId) throws Exception {
		t_users t_users = null;
		t_users = t_users.findById(userId);
		return t_users;
	}

	/**
	 * 根据user_id查询该用户的逾期信息
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param userId
	 * @return
	 * @author: zj
	 */
	public static User findUserById(long userId) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(" select ");
		sBuffer.append(" count(1) as overdue_cnt, ");
		sBuffer.append(" ifnull(sum(bill.repayment_corpus+bill.repayment_interest),0) as overdue_amount, ");
		sBuffer.append(
				" ifnull(sum(case when bill.status=-1 then (bill.repayment_corpus+bill.repayment_interest) else 0 end ),0) as current_overdue_amount ");
		sBuffer.append(" from t_bids bid ");
		sBuffer.append(" inner join 	t_bills bill on bill.bid_id=bid.id ");
		sBuffer.append(" where bill.overdue_mark<>0 ");
		sBuffer.append(" and bid.user_id=? ");
		String sql = sBuffer.toString();
		User user = new User();
		Query query = JPA.em().createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		query.setParameter(1, userId);
		List<Map<String, Object>> list = query.getResultList();
		if (list != null) {
			com.alibaba.fastjson.JSONObject jsonObject = (com.alibaba.fastjson.JSONObject) com.alibaba.fastjson.JSON
					.toJSON(list.get(0));
			user.overdueAmount = jsonObject.getBigDecimal("overdue_amount");
			user.overdueCnt = jsonObject.getInteger("overdue_cnt");
			user.currentOverdueAmount = jsonObject.getBigDecimal("current_overdue_amount");
		}
		return user;
	}

	public static int editUserBaseinfo(ErrorInfo error, long userId, int userType, int sex, String education, String car,
			String cityId, long adCityId, String house, String marital, double creditAmount, String email, String lawSuit,
			String creditReport, String birthday, String legal_person, Long industry, BigDecimal reg_capital,
			String business_scope, String income_debt_info, String asset_info, String other_finance_info, String other_info) {
		try {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date date = null;
			try {
				date = format.parse(birthday);
			} catch (ParseException e) {
			}

			/*
			 * String sql =
			 * "update t_users set sex = ?,education_Id = ?,car_id = ?,house_id = ?,marital_id = ?,email = ?,"
			 * +
			 * "law_suit = ?,credit_report = ?,credit_amount = ?,city_id = ?,user_type = ?,birthday=? where id = ?"
			 * ; Query query = t_users.em().createNativeQuery(sql); query.setParameter(1,
			 * sex); query.setParameter(2, StringUtils.isBlank(education) ? "0" :
			 * education); query.setParameter(3, StringUtils.isBlank(car) ? "0" : car);
			 * query.setParameter(4, StringUtils.isBlank(house) ? "0" : house);
			 * query.setParameter(5, StringUtils.isBlank(marital) ? "0" : marital);
			 * query.setParameter(6, email); query.setParameter(7, lawSuit);
			 * query.setParameter(8, creditReport); query.setParameter(9, creditAmount);
			 * query.setParameter(10, adCityId); query.setParameter(11, userType);
			 * query.setParameter(12, date); query.setParameter(13, userId);
			 * query.executeUpdate();
			 */
			t_users t_user = JPA.em().find(t_users.class, userId, LockModeType.PESSIMISTIC_WRITE);
			JPA.em().refresh(t_user, LockModeType.PESSIMISTIC_WRITE);
			t_user.sex = sex;
			t_user.education_id = Integer.parseInt(StringUtils.isBlank(education) ? "0" : education);
			t_user.car_id = Integer.parseInt(StringUtils.isBlank(car) ? "0" : car);
			t_user.house_id = Integer.parseInt(StringUtils.isBlank(house) ? "0" : house);
			t_user.marital_id = Integer.parseInt(StringUtils.isBlank(marital) ? "0" : marital);
			if (!StringUtil.isBlank(email)) {
				t_user.email = email;
			}
			t_user.law_suit = lawSuit;
			t_user.credit_report = creditReport;
			t_user.credit_amount = BigDecimal.valueOf(creditAmount);
			t_user.city_id = (int) adCityId;
			t_user.user_type = userType;
			if (t_user.user_type != 1) {
				t_user.birthday = date;
			}
			t_user.save();
			System.out.println("t_user.save();");
			User user = new User();
			user.id = userId;
			if (user.user_type != 1) {
				user.getUserInfo().legal_person = legal_person;
				user.getUserInfo().industry_id = industry;
				user.getUserInfo().reg_capital = reg_capital;
			}
			user.getUserInfo().business_scope = business_scope;
			user.getUserInfo().income_debt_info = income_debt_info;
			user.getUserInfo().asset_info = asset_info;
			user.getUserInfo().other_finance_info = other_finance_info;
			user.getUserInfo().other_info = other_info;
			user.getUserInfo().save();

			t_user_city userCity = t_user_city.find("user_id = ?", userId).first();
			if (userCity == null) {
				userCity = new t_user_city();
			}
			userCity.city_id = cityId;
			t_new_city newCity = t_new_city.find("city_id = ?", cityId).first();
			userCity.province_id = newCity.father;
			userCity.user_id = userId;
			UserCitys.addUserCity(error, userCity);

			error.code = 1;
			error.msg = "修改成功！";
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			error.code = -1;
			error.msg = "保存用户基础信息失败！";
		}
		return error.code;
	}

	/**
	 * 手机号码是否已经存在
	 * 
	 * @param email
	 * @return
	 */
	public static int isMobileExist(String newUserMobile, String oldUserMobile, int financeType, ErrorInfo error) {
		error.clear();
		String sql = "";
		if (financeType == FinanceTypeEnum.INVEST.getCode()) {
			sql = "select mobile from t_users where mobile = ? and ( finance_type=? or  finance_type is null) ";
		} else {
			sql = "select mobile from t_users where mobile = ? and  finance_type=? ";
		}

		String mobile = null;

		try {
			mobile = t_users.find(sql, newUserMobile, financeType).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("判手机号码是否已经存在时,根据手机号码查询数据时：" + e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次手机号码是否已经存在判断失败！";

			return error.code;
		}

		if (mobile != null) {

			if (oldUserMobile != null && oldUserMobile.equals(mobile)) {
				error.code = 0;

				return error.code;
			}

			error.code = -2;
			error.msg = "号码被使用";

			return error.code;
		}

		error.code = 0;

		return 0;
	}

	/**
	 * 借款端实名认证
	 * 
	 * @param realName
	 * @param idNumber
	 */
	public int updateBorrowCertification(String realName, String idNumber, int maritalId, int cityId, Long id, ErrorInfo error) {
		error.clear();

		String sql = "update t_users set reality_name = ?, id_number = ? ,user_type = ?,marital_Id = ?,city_id = ? where id = ?";

		int rows = 0;

		try {
			rows = JpaHelper.execute(sql, realName, idNumber, UserTypeEnum.PERSONAL.getCode(), maritalId, cityId, this.id)
					.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info("实名认证：" + e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次实名验证失败！";

			return error.code;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		this.realityName = realName;
		this.idNumber = idNumber;
		this.maritalId = maritalId;
		this.cityId = cityId;
		setCurrUser(this);

		error.clear();
		error.msg = "实名认证成功";

		try {
			Score.sendScore(id, Score.AUTH, realName, error);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return error.code;
	}

	/**
	 * 校验用户基本信息
	 * 
	 * @param userId
	 * @param userType
	 * @param sex
	 * @param education
	 * @param car
	 * @param cityId
	 * @param adCityId
	 * @param house
	 * @param marital
	 * @param creditAmount
	 * @param email
	 * @param lawSuit
	 * @param creditReport
	 * @param error
	 */
	public static void validUserBaseinfo(long userId, int userType, int sex, String education, String car, String cityId,
			long adCityId, String house, String marital, double creditAmount, String email, String lawSuit, String creditReport,
			ErrorInfo error) {
		if (userType == 0) {
			error.code = -1;
			error.msg = "主体性质不能为空!";
			return;
		}
		if (userType == 1) {
			if (sex == 0) {
				error.code = -1;
				error.msg = "请选择性别!";
				return;
			}
			if (StringUtils.isBlank(education)) {
				error.code = -1;
				error.msg = "文化水平不能为空!";
				return;
			}
			if (StringUtils.isBlank(car)) {
				error.code = -1;
				error.msg = "购车情况不能为空!";
				return;
			}
			if (StringUtils.isBlank(house)) {
				error.code = -1;
				error.msg = "购房状况不能为空!";
				return;
			}
			if (StringUtils.isBlank(marital)) {
				error.code = -1;
				error.msg = "婚姻状况不能为空!";
				return;
			}
		}
		if (StringUtils.isBlank(cityId)) {
			error.code = -1;
			error.msg = "借款人所在地不能为空!";
			return;
		}
		if (adCityId <= 0) {
			error.code = -1;
			error.msg = "借款人居住地不能为空!";
			return;
		}
		if (creditAmount < 0) {
			error.code = -1;
			error.msg = "授信金额不能为负数!";
			return;
		}
		/*
		 * if (StringUtils.isBlank(email)) { error.code = -1; error.msg = "邮箱不能为空!";
		 * return; }
		 */
		/*
		 * User user = new User(); user.findUserByEmail(email.trim()); if (user.getId()
		 * > 0 && userId != user.getId()) { error.code = -1; error.msg = "邮箱已被占用!";
		 * return; }
		 */
		if (StringUtils.isBlank(lawSuit)) {
			error.code = -1;
			error.msg = "涉诉情况不能为空!";
			return;
		}
		if (StringUtils.isBlank(creditReport)) {
			error.code = -1;
			error.msg = "近半年征信报告状况不能为空!";
			return;
		}
	}

	/**
	 * 用户是否发布过标的
	 * 
	 * @param userId
	 * @return
	 */
	public static boolean isHasBids(long userId) {
		long userBidsCount = t_bids.count("user_id=?", userId);
		return userBidsCount > 0 ? true : false;
	}

	/**
	 * 重置用户信息
	 * 
	 * @param userId
	 */
	public static void resetUser(long userId, ErrorInfo error) {
		StringBuilder sql = new StringBuilder();
		sql.append("update t_users ");
		sql.append("set reality_name = null,email = null,is_email_verified = 0,sex = 0,birthday = null,id_number = null,");
		sql.append("city_id = 0,education_id = 0,marital_id = 0,house_id = 0,car_id = 0,is_bank = 0,");
		sql.append("user_type = 0,credit_amount = null,law_suit = null,credit_report = null ");
		sql.append("where id = ?");
		try {
			Query query = JpaHelper.execute(sql.toString(), userId);
			query.executeUpdate();

			sql = new StringBuilder();
			sql.append("delete from t_user_bank_accounts where user_id = ?");
			query = JpaHelper.execute(sql.toString(), userId);
			query.executeUpdate();

			sql = new StringBuilder();
			sql.append("delete from t_user_city where user_id = ?");
			query = JpaHelper.execute(sql.toString(), userId);
			query.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "重置失败！";
			JPA.setRollbackOnly();
		}
	}

	/**
	 * 根据userId，返回相关用户信息 （仅供协议类合同生成使用）
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param userId
	 * @return
	 * @author: zj
	 */
	public static User findUser(long userId) {
		String sql = "";
		User user = null;
		sql = " select   user.reality_name, case  user.user_type  when 1 then  '身份证' when  2   then  '统一社会信用代码'   when  3  then  '统一社会信用代码' end   as card_name ,user.name,user.id_number, user.mobile,year( curdate( )) as  year ,month(curdate()) as month,day(curdate()) as day,replace(REPLACE(REPLACE(DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s'),'-',''),':',''),' ','') as datetime, concat('U',user.id) as pact_folder,user.user_type   "
				+ " , concat(tuc.province,tuc.city) as  address , tui.legal_person  from  t_users  user  left join t_user_city     tuc   on  user.id=tuc.user_id "
				+ "  left join t_users_info  tui   on  user.id=tui.user_id " + " where  user.id=? ";
		Logger.info("sql=============>" + sql);
		Query query = JPA.em().createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		query.setParameter(1, userId);
		List users = query.getResultList();
		if (users == null || users.size() <= 0) {
			return null;
		} else {
			Map<String, Object> objects = (Map<String, Object>) users.get(0);
			user = new User();
			user.realityName = EmptyUtil.obj2Str(objects.get("reality_name"));
			user.name = EmptyUtil.obj2Str(objects.get("name"));
			user.idNumber = EmptyUtil.obj2Str(objects.get("id_number"));
			user.carName = EmptyUtil.obj2Str(objects.get("card_name"));
			user.year = EmptyUtil.obj2Str(objects.get("year"));
			user.month = EmptyUtil.obj2Str(objects.get("month"));
			user.day = EmptyUtil.obj2Str(objects.get("day"));
			user.datetime = EmptyUtil.obj2Str(objects.get("datetime"));
			user.pactFolder = EmptyUtil.obj2Str(objects.get("pact_folder"));
			user.user_type = Integer.parseInt(objects.get("user_type").toString());
			user.address = EmptyUtil.obj2Str(objects.get("address"));
			user.legalPerson = EmptyUtil.obj2Str(objects.get("legal_person"));
			return user;
		}
	}

	/**
	 * 协议合同相关保存记录（协议合同相关使用）
	 * 
	 * @param userId
	 * @param autoPactLoaction   电子签章自动签署授权协议路径
	 * @param userPactLocation   出借人服务协议路径 （借款人 借款人服务协议路径）
	 * @param userCertificateUrl 出借人服务协议存证号 （借款人 借款人服务协议存证号）
	 * @param flag               操作类型
	 * @return
	 * @author Sxy
	 */
	public static void updateUserNewPact(long userId, Long bidId, String autoPactLoaction, String userPactLocation,
			String userCertificateUrl, String pactType) throws Exception {
		Query query = null;
		String sql = " update t_users ";

		if (pactType.equals(PactTypeEnum.QZSQ.getCode())) {
			sql = sql + " set auto_pact_location = ? where id = ? ";
			query = t_users.em().createNativeQuery(sql);
			query.setParameter(1, autoPactLoaction);
			query.setParameter(2, userId);
			query.executeUpdate();
		} else if (pactType.equals(PactTypeEnum.ZXGL.getCode())) {// 咨询与管理服务协议
			Bid.updateConsultPact(bidId, userPactLocation, userCertificateUrl);
		} else if (pactType.equals(PactTypeEnum.YMDFQFWFXY.getCode())) {// 亿美贷（分期）服务费协议
			Bid.updateYmdFqFwxyPact(bidId, userPactLocation, userCertificateUrl);
		} else {
			sql = sql + " set user_pact_location = ? ,user_certificate_url=? where id = ? ";
			query = t_users.em().createNativeQuery(sql);
			query.setParameter(1, userPactLocation);
			query.setParameter(2, userCertificateUrl);
			query.setParameter(3, userId);
			query.executeUpdate();
		}
	}

	/**
	 * 
	 * 
	 * @Description 判断指定类型合同是否已经记录
	 * @param userId
	 * @param pactType
	 * @return true 存在 false 不存在
	 * @throws Exception
	 * @author: zj
	 */
	public static boolean checkPact(long userId, String pactType) throws Exception {
		// 是否存在记录标识
		boolean existFlag = false;
		String sql = "";
		if (pactType.equals(PactTypeEnum.QZSQ.getCode())) {
			sql = "select * from t_users where auto_pact_location is null and id = ?";
			List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), sql, userId);
			if (list.size() == 0) {
				existFlag = true;
			}
		} else {
			if (pactType.equals(PactTypeEnum.CJFWXY.getCode()) || pactType.equals(PactTypeEnum.JKFWXY.getCode())) {
				sql = "select * from t_users where (user_pact_location is null or user_certificate_url is null) and id = ?";
				List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), sql, userId);
				if (list.size() == 0) {
					existFlag = true;
				}
			}
		}
		return existFlag;
	}

	/**
	 * 用户更新信息，生成合同时使用
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param userId
	 * @param pactType
	 * @return
	 * @throws Exception
	 * @author: zj
	 */
	public static boolean checkPact2(long userId, String pactType) throws Exception {
		// 是否存在记录标识
		boolean existFlag = false;
		String sql = "";
		if (pactType.equals(PactTypeEnum.QZSQ.getCode())) {
			sql = "select * from t_users where auto_pact_location is null and id = ?";
			List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), sql, userId);
			if (list.size() == 0) {
				existFlag = true;
			}
		} else {
			if (pactType.equals(PactTypeEnum.CJFWXY.getCode()) || pactType.equals(PactTypeEnum.JKFWXY.getCode())) {
				sql = "select * from t_users where (user_pact_location is null and user_certificate_url is null) and id = ?";
				List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), sql, userId);
				if (list.size() == 0) {
					existFlag = true;
				}
			}
		}
		return existFlag;
	}

	public t_users_info getUserInfo() {
		if (this.user_info == null) {
			this.user_info = t_users_info.find("user_id=?", this.id).first();
		}
		if (this.user_info == null) {
			this.user_info = new t_users_info();
			this.user_info.user_id = this._id;
			this.user_info.save();
		}
		return this.user_info;
	}

	static public String hideRealityName(String realityName) {
		int realityNameLength = realityName == null ? 0 : realityName.length();

		if (realityNameLength == 0) {
			return "******";
		}

		int frontLength = 0;// 前显示长度
		int backLength = 0;// 后显示长度
		if (realityNameLength < 2) {// [0,2]
			frontLength = 1;
		} else if (realityNameLength < 5) {// [2,4]
			frontLength = 1;
		} else {// [5,+]
			frontLength = 2;
		}
		if (realityNameLength < 2) {// [0,1]
			backLength = 0;
		} else if (realityNameLength < 5) {// [3,5]
			// backLength=1;
			backLength = 0;
		} else {// [6,+]
				// backLength=2;
			backLength = 2;
		}
		String borrowerRealityName = "";
		if (realityName != null) {
			borrowerRealityName = realityName.substring(0, frontLength);
			for (int i = 0; i < realityNameLength - frontLength - backLength; i++) {
				borrowerRealityName += "*";
			}
			borrowerRealityName += realityName.substring(borrowerRealityName.length());
		}
		return borrowerRealityName;

	}

	static public String hideString(String cleartext) {
		String ciphertext = "";
		int cleartextLength = cleartext == null ? 0 : cleartext.length();

		int frontLength = 0;// 前显示长度
		int backLength = 0;// 后显示长度

		if (cleartextLength == 0) {
			return "*";
		} else if (cleartextLength == 1) {

		} else if (cleartextLength == 2) {
			frontLength = 1;
		} else if (cleartextLength == 3) {
			frontLength = 1;
			backLength = 1;
		} else {
			frontLength = cleartextLength * 3 / 11;
			backLength = cleartextLength * 4 / 11;
		}

		ciphertext = cleartext.substring(0, frontLength);
		int hideLength = cleartextLength - frontLength - backLength;
		for (int i = 0; i < hideLength; i++) {
			ciphertext += "*";
		}
		ciphertext += cleartext.substring(ciphertext.length());

		return ciphertext;

	}

	static public String hideIdNumber(String idNumber) {
		String borrowerIdNumber = "*";
		if (idNumber != null && idNumber.length() > 5) {
			borrowerIdNumber = idNumber.substring(0, 2);
			for (int i = 0; i < idNumber.length() - 5; i++) {
				borrowerIdNumber += "*";
			}
			borrowerIdNumber += idNumber.substring(idNumber.length() - 3, idNumber.length());
		}
		return borrowerIdNumber;
	}

	/**
	 * 查询借款会员 （给贷后管理菜单下用户列表使用）
	 * 
	 * @param currPage
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static PageBean<v_users_apply> queryLoanUserBySupervisor(String mobile, String id_number, String reality_name,
			String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(" 	select  a.id,a.mobile,a.reality_name,a.id_number,b.apply_amount,b.apply_time from t_users a "
				+ "	left join 	( select  *from t_borrow_apply  tba  where  tba.id in  (	select max(id)  as id from t_borrow_apply  group by user_id) ) b "
				+ "	on   a.id=b.user_id  where  a.finance_type=0 ");

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("mobile", mobile);
		conditionMap.put("id_number", id_number);
		conditionMap.put("reality_name", reality_name);

		if (StringUtils.isNotBlank(mobile)) {
			sql.append("and a.mobile like ? ");
			params.add("%" + mobile + "%");
		}

		if (StringUtils.isNotBlank(id_number)) {
			sql.append("and a.id_number like ? ");
			params.add("%" + id_number + "%");
		}
		if (StringUtils.isNotBlank(reality_name)) {
			sql.append("and a.reality_name like ? ");
			params.add("%" + reality_name + "%");
		}

		sql.append("order by b.apply_time desc");

		List<v_users_apply> users = new ArrayList<v_users_apply>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_users_apply.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询所有的会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询借款会员列表时出现异常！";

			return null;
		}

		PageBean<v_users_apply> page = new PageBean<v_users_apply>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 查询活跃会员
	 * 
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_user_info_v1> queryActiveUser(String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		StringBuffer sql = new StringBuffer();
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_INFO_V5);

		List<Object> paramsCount = new ArrayList<Object>();
		List<Object> params = new ArrayList<Object>();

		List<v_user_info_v1> users = new ArrayList<v_user_info_v1>();
		int count = 0;

		try {
			EntityManager em = JPA.em();
			Logger.info("sql:" + sql.toString());
			Query query = em.createNativeQuery(sql.toString(), v_user_info_v1.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			users = query.getResultList();

			// count = QueryUtil.getQueryCountByCondition(em, sql.toString(), paramsCount);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询活跃会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询活跃会员列表时出现异常！";

			return null;
		}

		PageBean<v_user_info_v1> page = new PageBean<v_user_info_v1>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = 10;
		page.page = users;

		error.code = 0;

		return page;
	}

	/**
	 * 查询投资过的用户
	 * 
	 * @return
	 * @author: zj
	 */
	public static List<Map<String, Object>> findInvestUser() {
		String sql = "SELECT u.name FROM t_users u where u.finance_type=1 and (select IFNULL(SUM(i.amount),0) from t_invests i join t_bids b on i.bid_id = b.id where u.id = i.user_id and b.status in(?,?,?)) !=0";
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), sql, Constants.BID_REPAYMENT,
				Constants.BID_COMPENSATE_REPAYMENT, Constants.BID_REPAYMENTS);
		return countMap;
	}

	/**
	 * 半年内有投资过的用户
	 * 
	 * @return
	 * @author: zj
	 */
	public static List<Map<String, Object>> findhalfYearInvestUser() {
		String sql = "SELECT u.name FROM t_users u where u.finance_type=1 and (select IFNULL(SUM(i.amount),0) from t_invests i join t_bids b on i.bid_id = b.id where u.id = i.user_id and b.status in(?,?,?) and  DATE_FORMAT(i.time,'%Y-%m-%d')  >(SELECT DATE_SUB(CURDATE(), INTERVAL 180 DAY) ) ) !=0";
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), sql, Constants.BID_REPAYMENT,
				Constants.BID_COMPENSATE_REPAYMENT, Constants.BID_REPAYMENTS);
		return countMap;
	}

	/**
	 * 按照余额范围查找用户
	 * 
	 * @return
	 * @author: zj
	 */
	public static List<Map<String, Object>> findByBalanceUser(int balance_s, int balance_b) {
		String sql = "select  u.`name` from t_users u where u.finance_type=1 and  u.balance  between  ?  and   ? ";
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), sql, balance_s, balance_b);
		return countMap;
	}
}
