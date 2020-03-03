package constants;

import javax.persistence.Query;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import models.t_system_options;

public class OptionKeys {

	public static final String SERVICE_EMAIL_SERVER = "service_email_server";
	public static final String SERVICE_EMAIL_ACCOUNT = "service_email_account";
	public static final String SERVICE_EMAIL__PASSWORD = "service_email_password";
	public static final String SERVICE_SMS_ACCOUNT = "service_sms_account";
	public static final String SERVICE_SMS_PASSWORD = "service_sms_password";
	public static final String SERVICE_MAIL_ACCOUNT = "service_mail_account";
	public static final String SERVICE_MAIL_PASSWORD = "service_mail_password";
	public static final String PLATFORM_TELEPHONE = "platform_telephone";
	public static final String PLATFORM_NAME = "platform_name";
	public static final String PLATFORM_STARTUP_TIME = "platform_startup_time";
	public static final String PLATFORM_LOGO_FINENAME = "platform_logo_filename";
	public static final String COMPANY_NAME = "company_name";
	public static final String COMPANY_PROVINCE = "company_province";
	public static final String COMPANY_CITY = "company_city";
	public static final String COMPANY_ADDREDD = "company_address";
	public static final String COMPANY_TELEPHONE1 = "company_telephone1";
	public static final String CONTACT_MOBILE1 = "contact_mobile1";
	public static final String CONTACT_MOBILE2 = "contact_mobile12";
	public static final String COMPANY_FAX = "company_fax";
	public static final String COMPANY_EMAIL = "company_email";
	public static final String COMPANY_CONTACT_NAME = "company_contact_name";
	public static final String COMPANY_QQ1 = "company_qq1";
	public static final String COMPANY_QQ2 = "company_qq2";
	public static final String SITT_ICP_NUMBER = "site_icp_number";
	public static final String WORK_START_TIME = "work_start_time";
	public static final String WORK_END_TIME = "work_end_time";
	public static final String LABLE_BEGINNER_INTRODUCTION = "lable_beginner_introduction";
	public static final String LABLE_ABOUT_LOAN = "lable_about_loan";
	public static final String LABLE_ABOUT_FINANCING = "lable_about_financing";
	public static final String LABLE_ABOUT_US = "lable_about_us";
	public static final String LABLE_HELP_CENTRE = "lable_help_centre";
	public static final String LABLE_CUSTOMER_SUPPORT = "lable_customer_support";
	public static final String LABLE_FRIEND_LINK = "lable_friend_link";
	public static final String BILL_REPAYMENT_TYPE = "bill_repayment_type";
	public static final String AUDIT_MECHANISM = "audit_mechanism";
	public static final String AUDIT_MODE = "audit_mode";
	public static final String AUDIT_SCALE = "audit_scale";
	public static final String LOAN_NUMBER = "loan_number";
	public static final String LOAN_BILL_NUMBER = "loan_bill_number";
	public static final String INVEST_BILL_NUMBER = "invests_bill_number";
	public static final String AGENCIES_NUMBER = "agencies_number";
	public static final String TRANFER_NUMBER = "transfer_number";
	public static final String AUDIT_ITEM_NUMBER = "audit_item_number";
	public static final String SEO_TITLE = "seo_title";
	public static final String SEO_DESCRIPTION = "seo_description";
	public static final String SEO_KEYWORDS = "seo_keywords";
	public static final String VERSION_NAME = "version_name";
	public static final String VERSION = "version";
	public static final String DB_VERSION = "db_version";
	public static final String BORROW_FEE = "borrow_fee";
	public static final String BORROW_FEE_MONTH = "borrow_fee_month";
	public static final String BORROW_FEE_RATE = "borrow_fee_rate";
	public static final String INVESTMENT_FEE = "investment_fee";
	public static final String DEBT_REANSFER_FEE = "debt_transfer_fee";
	public static final String OVERDUE_FEE = "overdue_fee";
	public static final String WITHDRAW_FEE = "withdraw_fee";
	public static final String RECHARGE_FEE = "recharge_fee";
	public static final String VIP_FEE = "vip_fee";
	public static final String VIP_DISCOUNT = "vip_discount";
	public static final String VIP_AUDIT_PERIOD = "vip_audit_period";
	public static final String VIP_MIN_TIME = "vip_min_time";
	public static final String VIP_TIME_TYPE = "vip_time_type";
	public static final String NORMAL_REPAYMENT_SCORE = "normal_repayment_score";
	public static final String SUCCESSFUL_BORROW_SOORE = "successful_borrow_score";
	public static final String SUCCESSFUL_INVEST_SCORE = "successful_invest_score";
	public static final String MONEY_TO_SYSTEM_SCORE = "money_to_system_score";
	public static final String OVERDUE_BILL_SCORE = "overdue_bill_score";
	public static final String CREDIT_LIMIT = "credit_limit";
	public static final String CPS_REWARD_TYPE = "cps_reward_type";
	public static final String CPS_REWARD_TYPE_1 = "cps_reward_type_1";
	public static final String CPS_REWARD_TYPE_2 = "cps_reward_type_2";
	public static final String IS_OPNE_PASSWORD_ERROR_LIMIT = "isopen_password_error_limit";
	public static final String SECURITY_LOCK_AT_PASSWORD_CONTINUOUS_ERRORS = "security_lock_at_password_continuous_errors";
	public static final String SECURITY_LOCK_TIME = "security_lock_time";
	public static final String SECURITY_IS_USERNAME_LIMIT_WORDS = "security_is_username_limit_words";
	public static final String SECURITY_MIN_USERNAME_LENGTH = "security_min_username_length";
	public static final String SECURITY_MAX_USERNAME_LENGTH = "security_max_username_length";
	public static final String SECURITY_MIN_PASSWORD_LENGTH = "security_min_password_length";
	public static final String SECURITY_MAX_PASSWORD_LENGTH = "security_max_password_length";
	public static final String SECURITY_PASSWORD_REGEX = "security_password_regex";
	public static final String CREDIT_INITIAL_AMOUNT = "credit_initial_amount";
	public static final String REGISTER_NEG = "register_neg";
	
	public static String getvalue(String key, ErrorInfo info) {

		info.msg = "";

		if (key == null) {
			info.msg = "系统参数的key不能为空";
			return null;

		}

		String value = null;
		String sql = "select _value from t_system_options where _key = ?";

		try {
			value = t_system_options.find(sql, key).first();
		} catch (Exception e) {
			e.printStackTrace();
			info.msg = "系统设置查询出现错误";
			return null;

		}
		return value;

	}

	/**
	 * 修改 OptionKeys
	 * 
	 * @param value _value
	 * @param key _key
	 * @param info 错误信息
	 */
	public static void siteValue(String key, String value, ErrorInfo error) {
		error.clear();
		
		String hql = "update t_system_options o set o._value=? where o._key=?";
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, value);
		query.setParameter(2, key);

		try {
			error.code = query.executeUpdate();
		} catch (Exception e) {
			Logger.error("修改 OptionKeys 出现异常!" + e.getMessage());
			error.msg = "设置失败!";
			
			return;
		}
		
		error.msg = error.code > 0 ? "设置成功!" : "设置失败!";
	}
	
//	public static List<String> getvalue(String[] keys, ErrorInfo info) {
//		
//		info.msg = "";
//		
//		if(keys == null) {
//			info.msg = "系统参数的key不能为空";
//			return null;
//		}
//		StringBuffer sql = new StringBuffer("select value from where ");
//		
//		for (String key : keys) {
//			sql.append(" " + key + " = ?,");
//		}
//		String mysql = sql.substring(0, sql.length() - 1);
//		List<String> valuse  = null;
//		
//		try {
//			values = t_system_options.find(mysql,).fetch();
//		} catch(Exception e) {
//				e.printStackTrace();
//				info.msg = "系统设置查询出现错误";
//				return null;
//				
//		}
//		return value;
//		
//	}
	
}
