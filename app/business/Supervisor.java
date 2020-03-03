package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_right_groups;
import models.t_right_groups_of_supervisor;
import models.t_rights;
import models.t_rights_of_supervisor;
import models.t_supervisors;
import models.v_supervisors;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.mvc.Scope.Session;
import utils.CacheManager;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.PageBean;
import utils.QueryUtil;
import utils.RegexUtils;
import utils.Security;
import constants.Constants;
import constants.Constants.Sex;
import constants.Constants.SupervisorLevel;
import constants.Constants.SystemSupervisor;
import constants.OptionKeys;
import constants.SQLTempletes;
import constants.SupervisorEvent;

/**
 * 管理员
 * 
 * @author lzp
 * @version 6.0
 * @created 2014-4-7 上午10:30:50
 */

public class Supervisor implements Serializable{
	public long id;
	private long _id = -1;
	public String sign;//加密ID

	public Date time;

	public String name;
	private String _name;
	public String realityName;

	public String password;
	public String _password;
	public int passwordContinuousErrors;
	public boolean isPasswordErrorLocked;
	public Date passwordErrorLockedTime;

	public boolean isAllowLogin;
	public long loginCount;
	public Date lastLoginTime;
	public String lastLoginIp;
	public String lastLoginCity;
	public Date lastLogoutTime;
	public String loginIp;
	public String loginCity;

	public String email;
	public String telephone;
	public String mobile1;
	public String officeTelephone;
	public String faxNumber;
	public int sex;
	public Date birthday;
	public int level;//管理员等级 0普通管理员 1超级管理员

	public Boolean isErased;
	public long createrId;
	public String ukey;
	public boolean isCustomer;
	public String customerNum;
	
	public static long onlineSupperSupervisorNum;
	public static long onlineNormalSupervisorNum;
	
	public List<t_right_groups> groups;  //管理员所属的权限组
	public String groupIds;
	public String groupNames;

	public List<Long> allRightIds;		 //管理员的权限+管理员所属组的权限
	public String rightIds;				 //管理员的权限
	
	public String keySign; //跟保存在云盾的管理员id加密后的字符串做对比

	
	
	public Supervisor() {
		super();
	}
	
	public Supervisor(long id) {
		super();
		setId(id);
	}
	
	public Supervisor(String name) {
		super();
		setName(name);
	}
	

	public long getId() {
		return _id;
	}

	public void setId(long id) {
		t_supervisors supervisor = null;

		try {
			supervisor = t_supervisors.findById(id);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			this._id = -1;

			return;
		}

		if (null == supervisor) {
			this._id = -1;

			return;
		}

		setInfomation(supervisor);
	}
	
	public String getSign() {
		return Security.addSign(this.id, Constants.SUPERVISOR_ID_SIGN);
	}
	
	public String getKeySign() {
		String DesId = com.shove.security.Encrypt.encrypt3DES(Long.toString(this.id), Constants.ENCRYPTION_KEY);
		DesId = DesId.substring(0, 16);
		String time =  Long.toString(new DateUtil().getHours());
		String sign = com.shove.security.Encrypt.MD5(DesId + time + Constants.ENCRYPTION_KEY);
		return sign;
	}
	
	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;

		List<t_supervisors> li = null;

		try {
			li = t_supervisors.find("byName", name).fetch();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			this._id = -1;

			return;
		}
		
		if (li.size() < 1) {
			this._id = -1;

			return;
		}

		t_supervisors supervisor = li.get(0);
		setInfomation(supervisor);
	}

	public String getPassword() {
		return _password;
	}
	
	public void setPassword(String password) {
		_password = com.shove.security.Encrypt.MD5(password + Constants.ENCRYPTION_KEY);
	}
	
	public int getAge() {
		return DateUtil.getAge(this.birthday);
	}
	
	public String getLoginIp() {
		return loginIp;
	}
	
	public String getLastLoginCity() {
		return lastLoginCity;
	}

	public String getLoginCity() {
		return loginCity;
	}

	public List<t_right_groups> getGroups() {
		ErrorInfo error = new ErrorInfo();
		
		if (null == groups) {
			groups = queryGroups(_id, error);
		}
		
		return groups;
	}

	public String getGroupIds() {
		ErrorInfo error = new ErrorInfo();
		
		if (null == groupIds) {
			groupIds = queryGroupIds(_id, error);
		}
		
		return groupIds;
	}
	
	public String getGroupNames() {
		ErrorInfo error = new ErrorInfo();
		
		if (null == groupNames) {
			groupNames = queryGroupNames(_id, error);
		}
		
		return groupNames;
	}

	public List<Long> getAllRightIds() {
		ErrorInfo error = new ErrorInfo();
		
		if (null == allRightIds) {
			allRightIds = queryAllRightIds(_id, error);
		}
		
		return allRightIds;
	}

	public String getRightIds() {
		ErrorInfo error = new ErrorInfo();
		
		if (null == rightIds) {
			rightIds = queryRightIds(_id, error);
		}
		
		return rightIds;
	}

	/**
	 * 填充基本信息
	 * @param supervisor
	 */
	private void setInfomation(t_supervisors supervisor) {
		if (null == supervisor) {
			this._id = -1;
			
			return;
		}

		_id = supervisor.id;
		this.time = supervisor.time;
		_name = supervisor.name;
		this.realityName = supervisor.reality_name;
		_password = supervisor.password;
		this.passwordContinuousErrors = supervisor.password_continuous_errors;
		this.isPasswordErrorLocked = supervisor.is_password_error_locked;
		this.passwordErrorLockedTime = supervisor.password_error_locked_time;
		this.isAllowLogin = supervisor.is_allow_login;
		this.loginCount = supervisor.login_count;
		this.lastLogoutTime = supervisor.last_logout_time;
		this.loginIp = supervisor.last_login_ip;
		this.lastLoginIp = supervisor.last_login_ip;
		this.lastLoginTime = supervisor.last_login_time;
		this.loginCity = supervisor.last_login_city;
		this.lastLoginCity = supervisor.last_login_city;
		this.email = supervisor.email;
		this.telephone = supervisor.telephone;
		this.mobile1 = supervisor.mobile1;
		this.officeTelephone = supervisor.office_telephone;
		this.faxNumber = supervisor.fax_number;
		this.sex = supervisor.sex;
		this.birthday = supervisor.birthday;
		this.level = supervisor.level;
		this.isErased = supervisor.is_erased;
		this.createrId = supervisor.creater_id;
		this.ukey = supervisor.ukey;
		this.isCustomer = supervisor.is_customer;
		this.customerNum = supervisor.customer_num;
	}

	/**
	 * 填充数据库实体
	 * @param supervisor
	 * @param error
	 * @return
	 */
	private int fillDBE(t_supervisors supervisor, ErrorInfo error) {
		error.clear();
		
		if (SupervisorLevel.Normal != this.level && SupervisorLevel.Super != this.level) {
			error.code = -1;
			error.msg = "管理员等级数据异常";
			
			return error.code;
		}
		
		if (StringUtils.isBlank(this.realityName)) {
			error.code = -1;
			error.msg = "真实姓名不能为空";
			
			return error.code;
		}
		
		if (this.realityName.length() > 50) {
			error.code = -1;
			error.msg = "真实姓名长度需在0~50之间";
			
			return error.code;
		}
		
		if (StringUtils.isBlank(this.mobile1)) {
			error.code = -1;
			error.msg = "手机不能为空";
			
			return error.code;
		}
		
		if (!RegexUtils.isMobileNum(this.mobile1)) {
			error.code = -1;
			error.msg = "手机格式有误，请重新输入";
			
			return error.code;
		}
		
		if (StringUtils.isBlank(this.email)) {
			error.code = -1;
			error.msg = "邮箱不能为空";
			
			return error.code;
		}
		
		if (!RegexUtils.isEmail(this.email)) {
			error.code = -1;
			error.msg = "邮箱格式不正确，请重新输入";
			
			return error.code;
		}
		
		supervisor.time = this.time;
		supervisor.name = _name;
		supervisor.reality_name = this.realityName;
		supervisor.password = _password;
		supervisor.password_continuous_errors = this.passwordContinuousErrors;
		supervisor.is_password_error_locked = this.isPasswordErrorLocked;
		supervisor.password_error_locked_time = this.passwordErrorLockedTime;
		supervisor.is_allow_login = this.isAllowLogin;
		supervisor.login_count = this.loginCount;
		supervisor.last_logout_time = this.lastLogoutTime;
		supervisor.last_login_ip = this.lastLoginIp;
		supervisor.last_login_time = this.lastLoginTime;
		supervisor.email = this.email;
		supervisor.telephone = this.telephone;
		supervisor.mobile1 = this.mobile1;
		supervisor.office_telephone = this.officeTelephone;
		supervisor.fax_number = this.faxNumber;
		supervisor.sex = this.sex;
		supervisor.birthday = this.birthday;
		supervisor.level = this.level;
		supervisor.is_erased = this.isErased;
		supervisor.creater_id = this.createrId;
		supervisor.ukey = this.ukey;
		supervisor.is_customer = this.isCustomer;
		supervisor.customer_num = this.customerNum;

		try {
			supervisor.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;

		return 0;
	}
	
	/**
	 * 修改名字
	 * @param error
	 * @return
	 */
	private int eidtName(ErrorInfo error) {
		error.clear();
		
		this._name = "gl"+this._id;
		String sqlUpdate = "update t_supervisors set name = :name where id = :id";
		Query queryUpdate = JPA.em().createQuery(sqlUpdate).setParameter("name", "gl"+this._id).setParameter("id", this._id);
		int rows = 0;
		
		try {
			rows = queryUpdate.executeUpdate();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		error.code = 0;
		
		return error.code;
	}
	
	/**
	 * 添加管理员
	 * @param error
	 * @return
	 */
	public long create(ErrorInfo error) {
		error.clear();
		
		if (isEmailExist(this.email, error)) {
			return error.code;
		}

		this.time = new Date();
		t_supervisors supervisor = new t_supervisors();

		if (fillDBE(supervisor, error) < 0) {
			return error.code;
		}

		this._id = supervisor.id;
		
		eidtName(error);
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.CREATE_SUPERVISOR, "添加管理员", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "添加管理员成功";

		return this._id;
	}
	
	/**
	 * 登录前验证云盾信息
	 * @param userName
	 * @param password
	 * @param sign
	 * @return
	 */
	public static String checkUkey(String userName, String password, String sign, String keyTime, ErrorInfo info){
		String error = com.shove.security.Encrypt.encrypt3DES( Constants.CLOUD_SHIELD_SIGN_FAULT, Constants.ENCRYPTION_KEY);
		String result =null;
		String adminId = null;
		
		String time =  Long.toString(new DateUtil().getHours());
		if(!keyTime.equalsIgnoreCase(time)){
            result = com.shove.security.Encrypt.encrypt3DES( Constants.CLOUD_SHIELD_SERVICE_TIME, Constants.ENCRYPTION_KEY);
			
			return result;
		}
		
		String all = userName + password + time + Constants.ENCRYPTION_KEY;
		
		//把传过来的userName，password都3DES解密
		String userName2 = com.shove.security.Encrypt.decrypt3DES(userName, Constants.ENCRYPTION_KEY);
        String password2 = com.shove.security.Encrypt.decrypt3DES(password, Constants.ENCRYPTION_KEY);
        String MD5pass = com.shove.security.Encrypt.MD5(password2 + Constants.ENCRYPTION_KEY);

		//把传过来的userName，password，time 用MD5加密验证签名
		String sign2 = com.shove.security.Encrypt.MD5(all.trim());
		
		if(!sign.equalsIgnoreCase(sign2))
		{
			return error;
		}
		
		String sql = "select id from t_supervisors where name = ? ";
		List<Object> idList = new ArrayList<Object>();
		
		try {
			idList = t_supervisors.find(sql, userName2).fetch();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			info.code = -1;
			info.msg = "查询管理员是否存在失败";
			
			return error;
		}
		
		//查询管理员是否存在
		if(idList.size() == 0){
			result = com.shove.security.Encrypt.encrypt3DES( "-1", Constants.ENCRYPTION_KEY);
			
			return result;
		}
		
		long id = queryAdminId(userName2, MD5pass , info);
		
		if(info.code < 0){
			return error;
		}
		
		//管理员密码错误
		if(id < 1){
            result = com.shove.security.Encrypt.encrypt3DES( "-2", Constants.ENCRYPTION_KEY);
			
			return result;
		}
		
		adminId = com.shove.security.Encrypt.encrypt3DES( Long.toString(id), Constants.ENCRYPTION_KEY);//3DES加密用于传到控件
		
		return adminId;
	} 
	
	/*
	 * 加密管理员id生成校验字符串
	 */
	public static String encryptAdminId(){
		Supervisor supervisor = Supervisor.currSupervisor();
		String DesId = com.shove.security.Encrypt.encrypt3DES(Long.toString(supervisor.id), Constants.ENCRYPTION_KEY);
		DesId = DesId.substring(0, 16);
		String time =  Long.toString(new DateUtil().getHours());
		String sign = com.shove.security.Encrypt.MD5(DesId + time + Constants.ENCRYPTION_KEY);
		
		return sign;
	} 
	
	/**
	 * 根据管理员名称和密码查询id
	 * @param name
	 * @param password
	 * @param error
	 * @return
	 */
	public static long queryAdminId(String name, String password ,ErrorInfo error){
		String sql = "select id from t_supervisors where name = ? and password = ? ";
		Long adminId = null;
		
		try {
			adminId = t_supervisors.find(sql, name, password).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			error.code = -1;
			error.msg = "查询管理员详情失败";
			
			return 0;
		}
		
		error.code = 0;
		
		return adminId = null == adminId ? 0 : adminId.longValue();
	}
	
	/**
	 * 登录
	 * @param password
	 * @param error
	 * @return
	 */
	public int login(String password, ErrorInfo error) {
		error.clear();

		if (this._id < 0 || this.isErased) {
			error.code = -1;
			error.msg = "管理员不存在";

			return error.code;
		}
		
		if (!isAllowLogin) {
			error.code = -2;
			error.msg = "你已经被限制登录";

			return error.code;
		}
		
		if(this.isPasswordErrorLocked) {
			String sLockTimeLength = OptionKeys.getvalue("SECURITY_LOCK_TIME", error);
			
			if (error.code < 0) {
				return error.code;
			}
			
			long lockTimeLength = (null == sLockTimeLength) ? (3*60) : Long.parseLong(sLockTimeLength);//单位分钟
			long remainingTime = lockTimeLength*60*1000 - (System.currentTimeMillis() - this.passwordErrorLockedTime.getTime());	
			
			if(remainingTime > 0) {
				error.code = -3;
				error.msg = "账号已被锁定，请于"+lockTimeLength+"分钟后登录";
				
				return error.code;
			}
			
			this.isPasswordErrorLocked = false;
			this.passwordContinuousErrors = 0;
			this.passwordErrorLockedTime = null;
		}

		/**
		 * 密码错误
		 */
		if (StringUtils.isNotBlank(this.password) && !this.password.equals(com.shove.security.Encrypt.MD5(password + Constants.ENCRYPTION_KEY))) {
			/*
			String sIsOpenPasswordErrorLimit = OptionKeys.getvalue("IS_OPNE_PASSWORD_ERROR_LIMIT", error);
			
			if (error.code < 0) {
				return error.code;
			}
			
			int isOpenPasswordErrorLimit = (null == sIsOpenPasswordErrorLimit) ? 3 : Integer.parseInt(sIsOpenPasswordErrorLimit);
			
			if (0 == isOpenPasswordErrorLimit) {
				error.code = -4;
				error.msg = "用户名与密码不匹配";
				
				return error.code;
			}
			
			this.passwordContinuousErrors = this.isSuperSupervisor() ? 0 : this.passwordContinuousErrors+1; // 屏蔽超级管理员的错误锁定
			String sMaxContinuousErrors = OptionKeys.getvalue("SECURITY_LOCK_AT_PASSWORD_CONTINUOUS_ERRORS", error);
			
			if (error.code < 0) {
				return error.code;
			}
			
			int maxContinuousErrors = (null == sMaxContinuousErrors) ? 3 : Integer.parseInt(sMaxContinuousErrors);
			
			if (this.passwordContinuousErrors >= maxContinuousErrors) {
				String sql = "update t_supervisors set last_login_time = ?, last_login_ip = ?, last_login_city = ?, password_continuous_errors = ?, is_password_error_locked = ?, password_error_locked_time = ? where id = ?";
				Query query = JPA.em().createQuery(sql);
				query.setParameter(1, new Date());
				query.setParameter(2, this.loginIp);
				query.setParameter(3, this.loginCity);
				query.setParameter(4, this.passwordContinuousErrors);
				query.setParameter(5, true);
				query.setParameter(6, new Date());
				query.setParameter(7, this.id);
				int rows = 0;
				
				try {
					rows = query.executeUpdate();
				} catch(Exception e) {
					e.printStackTrace();
					Logger.info("登录密码错误,锁定用户时："+e.getMessage());
					error.code = -1;
					error.msg = "用户名与密码不匹配，登录失败";
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				if(rows == 0) {
					JPA.setRollbackOnly();
					error.code = -1;
					error.msg = "数据未更新";
					
					return error.code;
				}
				
				String sLockTimeLength = OptionKeys.getvalue("SECURITY_LOCK_TIME", error);
				
				if (error.code < 0) {
					return error.code;
				}
				
				long lockTimeLength = (null == sLockTimeLength) ? (3*60*60) : Long.parseLong(sLockTimeLength);
				
				error.code = -5;
				error.msg = "连续" + maxContinuousErrors + "次密码错误，账号已被锁定，请于"+lockTimeLength+"分钟后登录";
				
				return error.code;
			}
			
			String sql = "update t_supervisors set last_login_time = ?, last_login_ip = ?, last_login_city = ?, password_continuous_errors = ?, is_password_error_locked = ?, password_error_locked_time = ? where id = ?";
			Query query = JPA.em().createQuery(sql);
			query.setParameter(1, new Date());
			query.setParameter(2, this.loginIp);
			query.setParameter(3, this.loginCity);
			query.setParameter(4, this.passwordContinuousErrors);
			query.setParameter(5, false);
			query.setParameter(6, new Date());
			query.setParameter(7, this.id);
			int rows = 0;
			
			try {
				rows = query.executeUpdate();
			} catch(Exception e) {
				e.printStackTrace();
				Logger.info("登录密码错误,更新用户登录信息时："+e.getMessage());
				error.code = -1;
				error.msg = "用户名与密码不匹配，登录失败";
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			if(rows == 0) {
				JPA.setRollbackOnly();
				error.code = -1;
				error.msg = "数据未更新";
				
				return error.code;
			}*/
			
			error.code = -6;
			error.msg = "用户名与密码不匹配";

			return error.code;
		}
		
		/**
		 * 登录成功
		 */
		String sql = "update t_supervisors set last_login_time = ?, last_login_ip = ?, last_login_city = ?, login_count = login_count + 1, password_continuous_errors = ?, is_password_error_locked = ?, password_error_locked_time = ? where id = ?";
		Query query = JPA.em().createQuery(sql);
		query.setParameter(1, new Date());
		query.setParameter(2, this.loginIp);
		query.setParameter(3, this.loginCity);
		query.setParameter(4, 0);
		query.setParameter(5, false);
		query.setParameter(6, null);
		query.setParameter(7, this.id);
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("登录时,更新用户登录信息时："+e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次登录失败！";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		setCurrSupervisor(this);
		
		utils.Cache cache = null;
		
		if (this.level == SupervisorLevel.Super) {
			cache = CacheManager.getCacheInfo("online_supper_supervisor_" + this.id + "");
		} else {
			cache = CacheManager.getCacheInfo("online_normal_supervisor_" + this.id + "");
		}
		
		if (null == cache) {
			cache = new utils.Cache();
			long timeout = 1800000;//单位毫秒
			
			if (this.level == SupervisorLevel.Super) {
				CacheManager.putCacheInfo("online_supper_supervisor_" + this.id, cache, timeout);
			} else {
				CacheManager.putCacheInfo("online_normal_supervisor_" + this.id, cache, timeout);
			}
		}
		
		DealDetail.supervisorEvent(this.id, SupervisorEvent.LOGIN, "登录", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "登录成功";
		
		return error.code;
	}

	/**
	 * 退出
	 */
	public int logout(ErrorInfo error) {
		error.clear();
		
		deleteCurrSupervisor();
		
		Query query = JPA.em().createQuery("update t_supervisors set last_logout_time = ? where id = ?");
		query.setParameter(1, new Date());
		query.setParameter(2, this.id);
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("安全退出时,保存安全退出的信息时："+e.getMessage());
			error.code = -1;
			error.msg = "对不起，由于平台出现故障，此次安全退出信息保存失败！";
			JPA.setRollbackOnly();
			
			return error.code;
		} finally {
			if (this.level == SupervisorLevel.Super) {
				CacheManager.clearByKey("online_supper_supervisor_" + this.id);
			} else {
				CacheManager.clearByKey("online_normal_supervisor_" + this.id);
			}
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(this.id, SupervisorEvent.LOGOUT, "注销", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "亲，你已安全退出";
		
		return error.code;
	}

	/**
	 * 是否有某权限
	 * @param rightId
	 * @return
	 */
	public boolean haveRight(int rightId) {
		if(this.name.equals("admin")) {
			return true;
		}
		for (Long id : this.allRightIds) {
			if (id == rightId) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * 是否有某权限
	 * @param rightId
	 * @return
	 */
	public boolean haveRight(String action) {
		ErrorInfo error = new ErrorInfo();
		
		List<Integer> rightIds = Right.queryRightIdByAction(action, error);
		
		if (error.code < 0) {
			return false;
		}
		
		for (Integer right : rightIds) {
			
			if (haveRight(right)) {
				
				return true;
			}
		}
		
		return false;
	}

	/**
	 * 锁定/启用
	 * @param isAllowLogin
	 * @param error
	 * @return
	 */
	public int enable(boolean isAllowLogin, ErrorInfo error) {
		error.clear();
		
		if (_id == SystemSupervisor.ID) {
			error.code = -1;
			error.msg = "不能锁定超级管理员";

			return error.code;
		}

		String sqlUpdate = "update t_supervisors set is_allow_login = :isAllowLogin where id = :id";
		Query queryUpdate = JPA.em().createQuery(sqlUpdate).setParameter("isAllowLogin", isAllowLogin).setParameter("id", _id);
		int rows = 0;
		
		try {
			rows = queryUpdate.executeUpdate();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		if (isAllowLogin) {
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ENABLE_SUPERVISOR, "启用管理员", error);
			
			if (error.code < 0) {
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			error.msg = "启用成功";
		} else {
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DISABLE_SUPERVISOR, "锁定管理员", error);
			
			if (error.code < 0) {
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			error.msg = "锁定成功";
		}
		
		error.code = 0;
		
		return error.code;
	}
	
	/**
	 * 修改管理员基本信息
	 * @param error
	 * @return
	 */
	public int edit(ErrorInfo error) {
		error.clear();

		t_supervisors supervisor = null;

		try {
			supervisor = t_supervisors.findById(this._id);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == supervisor) {
			error.code = -2;
			error.msg = "管理员不存在";

			return error.code;
		}
		
		if (fillDBE(supervisor, error) < 0) {
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.EDIT_SUPERVISOR, "编辑管理员", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "编辑管理员成功";

		return error.code;
	}
	
	/**
	 * 设置权限
	 * @param rightIds
	 * @param error
	 * @return
	 */
	public int editRights(String rightIds, ErrorInfo error) {
		error.clear();
		
		if (this.id == SystemSupervisor.ID) {
			error.code = -1;
			error.msg = "不能修改系统管理员的权限";
			
			return error.code;
		}
		
		String sql = "delete from t_rights_of_supervisor as ros where ros.supervisor_id = :supervisorId";
		Query query = JPA.em().createQuery(sql).setParameter("supervisorId", this._id);

		try {
			query.executeUpdate();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		if (StringUtils.isBlank(rightIds)) {
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.GRANT_TO_SUPERVISOR, "给管理员分配权限", error);
			
			if (error.code < 0) {
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			error.msg = "设置管理员权限成功";
			
			return error.code;
		}
		
		String[] arrRights = rightIds.split(",");
		
		for (String rightId : arrRights) {
			t_rights_of_supervisor ros = new t_rights_of_supervisor();
			ros.supervisor_id = this._id;
			ros.right_id = Long.parseLong(rightId);;

			try {
				ros.save();
			} catch (Exception e) {
				Logger.error(e.getMessage());
				e.printStackTrace();
				error.code = -1;
				error.msg = "数据库异常";
				JPA.setRollbackOnly();

				return error.code;
			}
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.GRANT_TO_SUPERVISOR, "给管理员分配权限", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "设置管理员权限成功";
		
		return error.code;
	}
	
	/**
	 * 设置权限组
	 * @param groupIds
	 * @param error
	 * @return
	 */
	public int editGroups(String groupIds, ErrorInfo error) {
		error.clear();
		
//		/**
//		 * 系统管理员必须属于超级管理员组
//		 */
//		if (SystemSupervisor.ID == this.id) {
//			if (!RegexUtils.contains(groupIds, RegexUtils.getCommaSparatedRegex(SystemSupervisor.ID+""))) {
//				error.code = -1;
//				error.msg = "系统管理员必须属于超级管理员组";
//				
//				return error.code;
//			}
//		}

		String sql = "delete from t_right_groups_of_supervisor as ros where ros.supervisor_id = :supervisorId";
		Query query = JPA.em().createQuery(sql);
		query.setParameter("supervisorId", this._id);

		try {
			query.executeUpdate();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		if (StringUtils.isBlank(groupIds)) {
			DealDetail.supervisorEvent(this.id, SupervisorEvent.EDIT_SUPERVISOR_GROUP, "编辑管理员权限组", error);
			
			if (error.code < 0) {
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			error.msg = "设置管理员权限组成功";
			
			return error.code;
		}
		
		groupIds = groupIds.replaceAll("\\s+", "");
		String[] arrIds = groupIds.split(",");
		
		for (String groupId : arrIds) {
			RightGroup.addSupervisor(Long.parseLong(groupId), this.name, error);
		}
		
		DealDetail.supervisorEvent(this.id, SupervisorEvent.EDIT_SUPERVISOR_GROUP, "编辑管理员权限组", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		
		saveRights(error);		
		if (error.code < 0) {
			return error.code;
		}
		
		error.code = 0;
		error.msg = "设置管理员权限组成功";

		return error.code;
	}
	
	/**
	 * 是否是管理员自己的密码
	 * @param password
	 * @return
	 */
	public boolean isMyPassword(String password) {
		if (StringUtils.isBlank(password)) {
			return false;
		}
		
		password = com.shove.security.Encrypt.MD5(password+Constants.ENCRYPTION_KEY);
		
		if (password.equalsIgnoreCase(_password)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 添加客服
	 * @param supervisorIds
	 * @param error
	 * @return
	 */
	public static int addCustomers(List<Long> supervisorIds, ErrorInfo error) {
		error.clear();
		
		if (supervisorIds.size() < 1) {
			error.code = -1;
			error.msg = "请选择客服";
			
			return error.code;
		}
		
		for (long supervisorId : supervisorIds) {
			t_supervisors supervisor = null;

			try {
				supervisor = t_supervisors.findById(supervisorId);
			} catch (Exception e) {
				Logger.error(e.getMessage());
				e.printStackTrace();
				error.code = -1;
				error.msg = "数据库异常";

				return error.code;
			}
			
			if (null == supervisor || supervisor.id == SystemSupervisor.ID) {
				continue;
			}
			
			supervisor.is_customer = true;
			supervisor.customer_num = "KF"+supervisor.id;
			
			try {
				supervisor.save();
			} catch (Exception e) {
				Logger.error(e.getMessage());
				e.printStackTrace();
				error.code = -1;
				error.msg = "数据库异常";
				JPA.setRollbackOnly();

				return error.code;
			}
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ADD_CUSTOMER, "添加客服", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "添加客服成功";
		
		return error.code;
	}
	
	/**
	 * 查询客服
	 * @param currPage
	 * @param pageSize
	 * @param lockType
	 * @param keywordType
	 * @param keyword
	 * @param error
	 * @return
	 */
	public static PageBean<v_supervisors> queryCustomers(int currPage, int pageSize,
			int lockType, int keywordType, String keyword, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		if (lockType < 0 || lockType > 2) {
			lockType = 0;
		}
		
		if (keywordType < 0 || keywordType > 4) {
			keywordType = 0;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_SUPERVISORS);
		sql.append(" and is_customer = true ");
		
		List<Object> params = new ArrayList<Object>();
		
		if (lockType != 0) {
			if (1 == lockType) {
				sql.append(" and (is_allow_login = true) ");
			} else {
				sql.append(" and (is_allow_login = false) ");
			}
		}
		
		if (StringUtils.isNotBlank(keyword)) {
			sql.append(Constants.QUERY_CUSTOMER_KEYWORD_TYPE[keywordType]);
			
			if (0 == keywordType) {
				params.add("%" + keyword + "%");
				params.add("%" + keyword + "%");
				params.add("%" + keyword + "%");
				params.add("%" + keyword + "%");
			} else {
				params.add("%" + keyword + "%");
			}
		}

		int count = 0;
		List<v_supervisors> page = null;
 
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_supervisors.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            page = query.getResultList();
            
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("lockType", lockType);
		map.put("keywordType", keywordType);

		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}
		
		PageBean<v_supervisors> bean = new PageBean<v_supervisors>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.totalCount = count;
		bean.page = page;
		bean.conditions = map;
		
		error.code = 0;

		return bean;
	}
	
	/**
	 * 查询管理员根据supervisorIds
	 * @param supervisorIds
	 * @return
	 */
	public static List<v_supervisors> querySupervisors(List<Long> supervisorIds, ErrorInfo error) {
		error.clear();
		
		if (supervisorIds.size() < 1) {
			return null;
		}

		List<String> conditions = new ArrayList<String>();
		List<Long> params = new ArrayList<Long>();
		
		for (int i = 0; i < supervisorIds.size(); i++) {
			conditions.add("?");
			params.add(supervisorIds.get(i));
		}
		
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_SUPERVISORS);
		sql.append(" and id in (");
		sql.append(StringUtils.join(conditions, ","));
		sql.append(")");
		
		List<v_supervisors> supervisors = null;
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_supervisors.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            supervisors = query.getResultList();
            
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		error.code = 0;
		
		return supervisors;
	}
	
	/**
	 * 重置管理员密码
	 * @param supervisorId
	 * @param error
	 * @return
	 */
	public static int resetPassword(long supervisorId, ErrorInfo error) {
		error.clear();
		
		if (supervisorId == SystemSupervisor.ID) {
			error.code = -1;
			error.msg = "不能重置超级管理员的密码";
			
			return error.code;
		}
		
		String password = com.shove.security.Encrypt.MD5(Constants.SUPERVISOR_INITIAL_PASSWORD + Constants.ENCRYPTION_KEY);
		String sqlUpdate = "update t_supervisors set password = :password where id = :id and id <> 1";
		Query queryUpdate = JPA.em().createQuery(sqlUpdate).setParameter("password", password).setParameter("id", supervisorId);
		int rows = 0;

		try {
			rows = queryUpdate.executeUpdate();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.RESET_SUPERVISOR_PASSWORD, "重置管理员密码", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "重置密码成功";
		
		return error.code;
	}

	/**
	 * 删除管理员
	 * @param id
	 * @param error
	 * @return
	 */
	public static int delete(long id, ErrorInfo error) {
		error.clear();
		
		if (SystemSupervisor.ID == id) {
			error.code = -1;
			error.msg = "系统管理员不能删除";
			
			return error.code;
		}

		t_supervisors supervisor = null;

		try {
			supervisor = t_supervisors.findById(id);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == supervisor) {
			error.code = -2;
			error.msg = "管理员不存在";

			return error.code;
		}

		supervisor.is_erased = true;

		try {
			supervisor.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DELETE_SUPERVISOR, "删除管理员", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "删除管理员成功";

		return error.code;
	}
	
	/**
	 * 详情
	 * @param id
	 * @param error
	 * @return
	 */
	public static v_supervisors detail(long supervisorId, ErrorInfo error) {
		error.clear();
		
		v_supervisors supervisor = null;
		List<v_supervisors> v_supervisors_list = null; 
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_SUPERVISORS);
		sql.append(" and id = ?");
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_supervisors.class);
            query.setParameter(1, supervisorId);
            query.setMaxResults(1);
            v_supervisors_list = query.getResultList();
            
            if(v_supervisors_list.size() > 0){
            	supervisor = v_supervisors_list.get(0);
            }
            
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			error.code = -1;
			error.msg = "查询管理员详情失败";
			
			return null;
		}
		
		if(supervisor == null ) {
			error.code = -1;
			error.msg = "管理员不存在";
			
			return null;
		}
		
		error.code = 0;
		
		return supervisor;
	}
	
	/**
	 * 根据id获得name
	 * @param id
	 * @param error
	 * @return
	 */
	public static String queryNameById(long id, ErrorInfo error) {
		error.clear();
		
		String sql = "select name from t_supervisors where id = ?";
		String userName = null;
		
		try {
			userName = t_supervisors.find(sql, id).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			error.code = -1;
			error.msg = "查询管理员失败";
			
			return null;
		}
		
		if(userName == null ) {
			error.code = -1;
			error.msg = "管理员不存在";
			
			return null;
		}
		
		error.code = 0;
		
		return userName;
	}
	
	/**
	 * 根据name获得id
	 * @param name
	 * @param error
	 * @return
	 */
	public static long queryIdByName(String name, ErrorInfo error) {
		error.clear();
		
		if (StringUtils.isBlank(name)) {
			return -1;
		}
		
		String sql = "select id from t_supervisors where name = ?";
		Long id_ = null;
		
		try {
			id_ = t_supervisors.find(sql, name).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			error.code = -1;
			error.msg = "查询管理员失败";
			
			return error.code;
		}
		
		error.code = 0;
		
		return id_==null ? -1 : id_.longValue();
	}
	
	/**
	 * 通过名字查询管理员
	 * @param name
	 * @param error
	 * @return
	 */
	public static t_supervisors querySupervisorByName(String name, ErrorInfo error) {
		error.clear();
		t_supervisors supervisor = null;
		
		try {
			supervisor = t_supervisors.find("name = ?", name).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			error.code = -1;
			error.msg = "查询管理员失败";
			
			return null;
		}
		
		if(supervisor == null ) {
			error.code = -2;
			error.msg = "账号为"+name+"的管理员不存在";
			
			return null;
		}
		
		error.code = 0;
		error.msg = "查询管理员成功";
		
		return supervisor;
	}

	/**
	 * 查询管理员
	 * @param currPage
	 * @param pageSize
	 * @param keyword
	 * @param error
	 * @return
	 */
	public static PageBean<v_supervisors> querySupervisors(int currPage,
			int pageSize, String keyword, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}

		String condition = "(1=1) and id <> 1";//不显示超级管理
		List<Object> params = new ArrayList<Object>();
		
		if (StringUtils.isNotBlank(keyword)) {
			condition += " and name like ? or reality_name like ? or mobile1 like ? or email like ?";
			params.add("%" + keyword + "%");
			params.add("%" + keyword + "%");
			params.add("%" + keyword + "%");
			params.add("%" + keyword + "%");
		}

		int count = 0;
		List<v_supervisors> page = null;

		try {
			count = (int) v_supervisors.count(condition, params.toArray());
			page = v_supervisors.find(condition, params.toArray()).fetch(currPage, pageSize);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		Map<String, Object> map = new HashMap<String, Object>();

		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}
		
		PageBean<v_supervisors> bean = new PageBean<v_supervisors>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.totalCount = count;
		bean.page = page;
		bean.conditions = map;
		
		error.code = 0;
		error.msg = "分配成功";

		return bean;
	}
	
	/**
	 * 查询管理员
	 * @param currPage
	 * @param pageSize
	 * @param keyword
	 * @param error
	 * @return
	 */
	public static PageBean<v_supervisors> queryCandidateCustomers(int currPage,
			int pageSize, String keyword, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_SUPERVISORS);
		sql.append(" and (is_customer = false or is_customer is null) and id <> ? and level = ?");
		
		List<Object> params = new ArrayList<Object>();
		params.add(SystemSupervisor.ID);
		params.add(SupervisorLevel.Normal);
		
		if (StringUtils.isNotBlank(keyword)) {
			sql.append(" and name like ? or reality_name like ? or mobile1 like ? or email like ?");
			params.add("%" + keyword + "%");
			params.add("%" + keyword + "%");
			params.add("%" + keyword + "%");
			params.add("%" + keyword + "%");
		}
		
		//客服所在的权限组需要包含账单催收模块
		sql.append(" and exists (select 1 from t_right_groups where id = ( select group_id from t_right_groups_of_supervisor where supervisor_id = `t_supervisors`.`id` limit 1 ) and find_in_set(right_modules, ?))");
		params.add(Constants.CUSTOMER_RIGHT_ID);

		int count = 0;
		List<v_supervisors> page = null;
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_supervisors.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            page = query.getResultList();
            
            String countSql = "select count(1) c from ("+ sql.toString() +")";
            count = em.createNativeQuery(countSql).getFirstResult();
//            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		Map<String, Object> map = new HashMap<String, Object>();

		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}
		
		PageBean<v_supervisors> bean = new PageBean<v_supervisors>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.totalCount = count;
		bean.page = page;
		bean.conditions = map;
		
		error.code = 0;

		return bean;
	}
	
	/**
	 * 查询管理员的权限组
	 * @param supervisorId
	 * @param error
	 * @return
	 */
	public static List<t_right_groups> queryGroups(long supervisorId, ErrorInfo error) {
		error.clear();
		
		String sql = "select rg from t_right_groups as rg where rg.id in (select gos.group_id from t_right_groups_of_supervisor as gos where gos.supervisor_id = ?)";
		List<t_right_groups> list = null;
		
		try {
			list = t_right_groups.find(sql, supervisorId).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		error.code = 0;
		
		return list;
	}
	
	/**
	 * 查询管理员权限组ids
	 * @param supervisorId
	 * @param error
	 * @return
	 */
	public static String queryGroupIds(long supervisorId, ErrorInfo error) {
		error.clear();

		String sql = "select group_id from t_right_groups_of_supervisor where supervisor_id = ?";
		List<Long> list = null;

		try {
			list = t_right_groups_of_supervisor.find(sql, supervisorId).fetch();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		error.code = 0;
		
		if (null == list) {
			return null;
		}

		return StringUtils.join(list, ",");
	}
	
	/**
	 * 查询管理员权限组names
	 * @param supervisorId
	 * @param error
	 * @return
	 */
	public static String queryGroupNames(long supervisorId, ErrorInfo error) {
		error.clear();

		String sql = "select name from t_right_groups as rg where rg.id in (select gos.group_id from t_right_groups_of_supervisor as gos where gos.supervisor_id = ?)";
		List<String> list = null;

		try {
			list = t_right_groups.find(sql, supervisorId).fetch();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		error.code = 0;
		
		if (null == list) {
			return null;
		}

		return StringUtils.join(list, ",");
	}
	
	/**
	 * 查询管理员所有的权限
	 * @param supervisorId
	 * @param error
	 * @return
	 */
	public static List<Long> queryAllRightIds(long supervisorId, ErrorInfo error) {
		error.clear();

//		String sql = "select r.id from t_rights as r where r.id in (select ros.right_id from t_rights_of_supervisor as ros where ros.supervisor_id = ?1) or r.id in (select rog.right_id from t_rights_of_group as rog where rog.group_id in (select gos.group_id from t_right_groups_of_supervisor as gos where gos.supervisor_id = ?2 ))";
		String sql = "select r.id from t_rights as r where r.id in (select ros.right_id from t_rights_of_supervisor as ros where ros.supervisor_id = ?1)";
		List<Long> list = null;

		try {
			list = t_rights.find(sql, supervisorId).fetch();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		error.code = 0;

		return list;
	}
	
	/**
	 * 查询管理员权限ids
	 * @param supervisorId
	 * @param error
	 * @return
	 */
	public static String queryRightIds(long supervisorId, ErrorInfo error) {
		error.clear();

		String sql = "select right_id from t_rights_of_supervisor where supervisor_id = ?)";
		List<Long> list = null;

		try {
			list = t_rights_of_supervisor.find(sql, supervisorId).fetch();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		error.code = 0;

		if (null == list) {
			return null;
		}

		return StringUtils.join(list, ",");
	}
	
	/**
	 * 查询系统管理员用户名
	 * @param supervisorId
	 * @param error
	 * @return
	 */
	public static String querySystemSupervisorName() {
		String sql = "select name from t_supervisors where id = ?)";
		String name = null;

		try {
			name = t_supervisors.find(sql, SystemSupervisor.ID).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();

			return null;
		}
		
		return name;
	}
	
	/**
	 * 获得当前缓存中的supervisor
	 * @return
	 */
	public static Supervisor currSupervisor() {
		if(Session.current()==null){
			return null;
		}
		String encryString = Session.current().getId();
		
		if(StringUtils.isBlank(encryString)) {
			
			return null;
		}
		
		String supervisorId = Cache.get("background_"+encryString) + "";
		if(StringUtils.isBlank(supervisorId)){
			
			return null;
		}
		
		Supervisor supervisor = (Supervisor) Cache.get("supervisor_"+supervisorId);
		
		if(supervisor == null) {
			
			return null;
		}
		
		return supervisor;
	}
	
	/**
	 * 将当前对象放入缓存中
	 * @param supervisor
	 */
	public static void setCurrSupervisor(Supervisor supervisor) {
		String encryString = Session.current().getId();
		//设置管理员凭证
		Cache.set("background_"+encryString, supervisor.id, Constants.CACHE_TIME_HOURS_12);
		//设置管理员登录成功信息
		Cache.set("supervisor_"+supervisor.id, supervisor, Constants.CACHE_TIME_HOURS_12);
	}
	
	/**
	 * 将当前对象放入缓存中
	 * @param supervisor
	 */
	public static void setCurrSupervisor(long supervisorId) {
		if (supervisorId < 1) {
			return;
		}
		
		Supervisor supervisor = new Supervisor();
		supervisor.id = supervisorId;
		Supervisor.setCurrSupervisor(supervisor);
	}
	
	/**
	 * 从缓存中删除当前管理员
	 */
	public static void deleteCurrSupervisor() {
		String encryString = Session.current().getId();
		String supervisorId = Cache.get("background_"+encryString) + "";
		Cache.delete("background_"+encryString);
		Cache.delete("supervisor_"+supervisorId);
	}
	
	/**
	 * 在线超级管理员数量
	 * @return
	 */
	public static long getOnlineSupperSupervisorNum() {
		return CacheManager.getCacheSize("online_supper_supervisor_");
	}

	/**
	 * 在线普通管理员数量
	 * @return
	 */
	public static long getOnlineNormalSupervisorNum() {
		return CacheManager.getCacheSize("online_normal_supervisor_");
	}
	
	/**
	 * 管理员是否登录
	 * @return
	 */
	public static boolean isLogin() {
		return (null != currSupervisor());
	}
	
	/**
	 * 邮箱是否被注册
	 * @param email
	 * @param info
	 * @return
	 */
	public static boolean isEmailExist(String email, ErrorInfo error){
		error.clear();
		
		if(StringUtils.isBlank(email)) {
			error.code = -1;
			error.msg = "邮箱不能为空";
			
			return false;
		}
		
		t_supervisors supervisor = null;
		
		try {
			supervisor = t_supervisors.find("email = ?", email).first();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			error.code = -10;
			error.msg = "数据库异常！";
			
			return false;
		}
		
		if(supervisor != null) {
			error.code = -2;
			error.msg = "该邮箱已经注册";
			
			return true;	
		}
		
		error.code = 0;
		
		return false;
	}
	
	/**
	 * 判断当前管理员是否超级管理员
	 */
	public boolean isSuperSupervisor(){
		/* 系统管理员 */
		if(Constants.SystemSupervisor.ID == this.id)
			return true;
		
		/* level判断 */
		if(Constants.SupervisorLevel.Super == this.level) {
			
			/* 超级管理员权限组判断 */
			Object obj = null;
			
			try {
				obj = t_right_groups_of_supervisor.find("supervisor_id = ? and group_id = ?", this.id, Constants.SystemSupervisorGroup.ID);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.info(e.getMessage());
				
				return false;
			}
			
			if(null == obj)
				return false;
			else
				return true;
			
		}
		
		return false;
	}

	public boolean haveLeft(String action) {
		ErrorInfo error = new ErrorInfo();
		
		List<Integer> rightIds = Right.querySameLevelRightIdByAction(action, error);
		
		if (error.code < 0) {
			return false;
		}
		
		for (Integer right : rightIds) {
			
			if (haveRight(right)) {
				
				return true;
			}
		}
		
		return false;
	}

	/**
	 * 保存用户权限
	 */
	public int saveRights(ErrorInfo error) {
		
		String deletesql = "delete from t_rights_of_supervisor where supervisor_id = :supervisor_id";
		Query queryUpdate1 = JPA.em().createQuery(deletesql).setParameter("supervisor_id", this.id);
		
		String savesql = "insert into t_rights_of_supervisor (supervisor_id, right_id) select :supervisor_id, right_id from t_rights_of_group where group_id in ( select group_id from t_right_groups_of_supervisor where supervisor_id = :supervisor_id )";
		Query queryUpdate = JPA.em().createNativeQuery(savesql).setParameter("supervisor_id", this.id);
		try {
			queryUpdate1.executeUpdate();
			queryUpdate.executeUpdate(); 
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		error.code = 0;

		return 0;
	}
}
