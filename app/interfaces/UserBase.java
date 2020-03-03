package interfaces;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import models.t_system_options;
import models.t_users;
import play.Play;
import play.libs.Time;
import play.mvc.Http.Cookie;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import utils.ErrorInfo;

import com.shove.security.Encrypt;

import constants.Constants;
import constants.OptionKeys;

public class UserBase implements Serializable{

	public long id;

	public String password;
	protected String _password;
	
	public Date time;
	
	public String name;
	protected String _name;
	
	public String realityName;
	
	public int passwordContinuousErrors;
	public boolean isPasswordErrorLocked;
	public Date passwordErrorLockedTime;

	public boolean isAllowLogin;
	public long loginCount;
	public Date lastLoginTime;
	public String lastLoginIp;
	public Date lastLogoutTime;
	
	public  String email;
	protected String _email;
	
	public boolean isEmailVerified;
	public String telephone;
	public String mobile;
	public boolean isMobileVerified;
	public String idNumber;
	public String address;
	public String postcode;
	public String sex;
	protected int _sex;
	
	public Date birthday;
	protected Date _birthday;
	
	public int age;
	protected int _age;
	public BigDecimal creditAmount;//授信金额
	public String lawSuit;//涉诉情况
	public String creditReport;//征信报告状况 
	public Integer financeType;//用户理财类型
	/**
	 * 登录
	 * @param password
	 * @return
	 */
	public long login(String password){
		/*获取锁定的错误次数*/
		t_system_options option = t_system_options.find("byKey", OptionKeys.SECURITY_IS_USERNAME_LIMIT_WORDS).first();
		//this.errorCount>=Integer.parseInt(option.value)
		if(this.isAllowLogin){
//			this.errorInfo = "你已经被管理员禁止登录";
		}
		
		if(this.isPasswordErrorLocked) {
			t_system_options lockTime = t_system_options.find("byKey", OptionKeys.SECURITY_LOCK_TIME).first();
			if(new Date().getTime()-this.passwordErrorLockedTime.getTime() < Long.parseLong(lockTime._value)*1000) {
//				this.errorInfo = "用户已经锁定";
				return -1;//用户已经锁定
				
			}
		}
		
		t_users user = t_users.findById(this.id);
		/*密码判断*/
		if(!Encrypt.MD5(password).equals(this.password)) {
			/*密码错误，错误次数加1*/
//			this.errorInfo = "密码与用户名不匹配";
			user.password_continuous_errors += 1;
			
			if(this.passwordContinuousErrors >= Integer.parseInt(option._value)) {
				user.is_password_error_locked = true;
				user.password_error_locked_time = new Date();
				
			}
			user.save();
			return -1;
			
		}
		
		user.last_login_time = new Date();
		user.last_login_ip = Request.current().remoteAddress;
		
		user.save();
		
		setCookie(this.id);
		
		return 0;
	}
	
	public long  logout(){
		Response.current().setCookie(this.getClass().getSimpleName(), "", null, "/", null, false, true);
		return 0;
		
	};
	
	/**
	 * 写入cookie
	 * @param id
	 */
	public void setCookie(long id) {
		String secret = Play.configuration.getProperty("application.secret");
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		String cookie = Encrypt.encrypt3DES(id+","+simple.format(new Date()),secret);
//		String cookie = id+"hello";
		String cookieKey = this.getClass().getSimpleName();
		Response.current().setCookie(cookieKey, cookie, null, "/", null, false, true);
	}
	
	public long getCookie(ErrorInfo e) {
		
		Cookie cookie = Request.current().cookies.get(this.getClass().getSimpleName());
		
		if(cookie == null ) {
			e.msg = "cookie不存在";
			return -1;
		}
		
		String secret = cookie.value;
		
		if(secret ==null || secret.equals("")) {
			e.msg = "cookie中的值为空";
			return -1;
		}
		
		secret = Encrypt.decrypt3DES(secret, Play.configuration.getProperty("application.secret"));
		
		if(secret == null) {
			e.msg = "cookie解密后值为空";
			return -1;
		}
		
		String [] secrets = secret.split(",");
		
		SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		
		try {
			date = simpleDate.parse(secrets[1]);
		} catch (ParseException e1) {
			e.msg = "时间转换异常";
			e1.printStackTrace();
			return -1;
			
		}
		long millisecond = System.currentTimeMillis()-date.getTime();
		
		if(millisecond>48*60*60*1000) {
			e.msg = "cookie已经过期";
			return -1;
			
		}
		
		if (StringUtils.isBlank(secrets[0])) {
			e.msg = "cookie的id为空";
			return -1;
					
		}
		long id = Long.parseLong(secrets[0]);
		
		return id;
		
	}
	
}
