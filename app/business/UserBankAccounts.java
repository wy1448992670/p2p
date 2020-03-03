package business;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import constants.Constants;
import constants.CouponTypeEnum;
import constants.UserEvent;
import models.t_red_packages_type;
import models.t_user_bank_accounts;
import models.t_users;
import play.Logger;
import play.db.jpa.JPA;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.RegexUtils;

/**
 * 银行账户业务实体类
 * @author lwh
 * @version 6.0
 * @created 2014年4月8日 下午4:13:53
 */
public class UserBankAccounts implements Serializable{
	private static final long serialVersionUID = 1L;
	private long _id;
	public long id;
	public long userId;
	public Date time;
	public String bankName;
	public String bankCode;  //支行code
	public int provinceCode;  //支行所在省code
	public int cityCode;  //支行所在市code
	public String branchBankName;  //支行名称
	public String province;  //支行所在省
	public String city;  //支行所在市
	public String account;
	public String subAccount;
	public String accountName;
	public String mobile;
	public boolean verified;
//	public Date verifyTime;
//	public long verifySupervisorId;
	
	public long bankInsur = 0;
	public String protocolNo; // 绑卡协议号
	public int isProtocol; // 是否支持协议绑卡
	public Boolean isValid;// 是否有效  false：无效   true：有效
	public boolean isValidMobile = Boolean.TRUE;//是否校验手机号 
	public Boolean isSign;//是否签协议0   false  否 1是   true  
	public String pact_url;//代扣协议地址
	public UserBankAccounts(){
		
	}

	public long getId() {
		return _id;
	}
	
	/**
	 * 获得隐藏的账号
	 */
	public String getSubAccount() {
		if(StringUtils.isBlank(this.account))
			return "";
		
		int len = this.account.length();
		
		if(len < 16 || len > 19){
			return "卡号有误!";
		}
		
		return this.account.substring(0, 6) + "..." + this.account.substring(len - 4, len);
	}

	public void setId(long id) {
 		t_user_bank_accounts userBankAccounts = null;

		try {
			userBankAccounts = t_user_bank_accounts.findById(id);
		} catch (Exception e) {
			this._id = -1;
			
			return;
		}

		if (null == userBankAccounts) {
			this._id = -1;

			return;
		}
		
		this._id = userBankAccounts.id;
		this.userId = userBankAccounts.user_id;
		this.time = userBankAccounts.time;
		this.bankName = userBankAccounts.bank_name;
		this.bankCode = userBankAccounts.bank_code;
		this.provinceCode = userBankAccounts.province_code;
		this.cityCode = userBankAccounts.city_code;
		this.account = userBankAccounts.account;
		this.accountName = userBankAccounts.account_name;
		this.mobile = userBankAccounts.mobile;
		this.branchBankName = userBankAccounts.branch_bank_name;
		this.province = userBankAccounts.province;
		this.city = userBankAccounts.city;
		this.verified = userBankAccounts.verified;
		this.protocolNo = userBankAccounts.protocol_no;
		this.isValid=userBankAccounts.is_valid;
		this.isSign =userBankAccounts.is_sign;
//		this.verifyTime = userBankAccounts.verify_time;
//		this.verifySupervisorId = userBankAccounts.verify_supervisor_id;
		
		// 银行卡投标情况
		String sql = "select count(*) from t_user_insur where insur_end > CURTIME() and bank_account = ?";
//		this.bankInsur = t_user_insur.count(sql, userBankAccounts.account);

		this.bankInsur = 0;
	}
	
	/**
	 * 用户添加设置银行帐号
	 * @param isNeedBranchBankInfo 是否支持提现银行卡支行信息绑定，true，支持，false，不支持
	 * @param error
	 * @return 返回-1代表绑定失败，返回 1 代表绑定成功
	 */
	public int addUserBankAccount(ErrorInfo error, boolean isNeedBranchBankInfo){
		error.clear();
		
		if(!isNeedBranchBankInfo){  //不支持提现银行卡支行信息时,branchBankName,province,city字段为null
			this.branchBankName = "";
			this.province = "";
			this.city = "";
		}
		
		if(isNeedBranchBankInfo && StringUtils.isBlank(this.branchBankName)) {  //支持提现银行卡支行信息时,branchBankName必填
			error.code = -1;
			error.msg = "支行名称不能为空";
			
			return error.code;
		}
		
		if(!isNeedBranchBankInfo && StringUtils.isBlank(this.bankName)) {  //不支持提现银行卡支行信息时，bankName必填
			error.code = -1;
			error.msg = "银行名称不能为空";
			
			return error.code;
		}
		
		if(StringUtils.isBlank(this.account)) {
			error.code = -1;
			error.msg = "账号不能为空";
			
			return error.code;
		}
		
		if(!RegexUtils.isBankAccount(this.account)) {
            error.code = -1;
            error.msg = "银行账号格式错误，应该是16-22位数字！";
            
            return error.code;
        }
		
		if(this.isValidMobile && !RegexUtils.isMobileNum(this.mobile)) {
            error.code = -1;
            error.msg = "预留手机号格式错误";
            
            return error.code;
        }
		
		if(StringUtils.isBlank(this.accountName)) {
			error.code = -1;
			error.msg = "真实姓名不能为空";
			
			return error.code;
		}
		User user_ = new User();
		user_.id = userId;
		boolean flag=this.isReuseBank(userId,account,user_.financeType, accountName);
		
		if(flag){
			error.msg = "该银行账户已存在，请重新输入!";
			error.code = -1;
			
			return error.code;
		}
		
		t_user_bank_accounts userBankAccounts = new t_user_bank_accounts();
		
		userBankAccounts.time=new Date();
		userBankAccounts.user_id=this.userId;
		userBankAccounts.bank_name = this.bankName.trim();
		userBankAccounts.bank_code = this.bankCode;
		userBankAccounts.province_code = this.provinceCode;
		userBankAccounts.city_code = this.cityCode;
		userBankAccounts.branch_bank_name = this.branchBankName.trim();
		userBankAccounts.province = this.province;
		userBankAccounts.city = this.city;
		userBankAccounts.account= this.account.trim();
		userBankAccounts.account_name= this.accountName.trim();
		userBankAccounts.mobile= this.mobile.trim();
		userBankAccounts.protocol_no = this.protocolNo;
		userBankAccounts.is_valid = true;
		boolean isBank = false;
		userBankAccounts.is_sign = this.isSign;
		try{
			userBankAccounts.save();


			/*start   创建测试用户修改  以下内容注释即可*/

			t_users user = t_users.findById(this.userId);

			isBank = user.is_bank;
			if(!isBank){
				user.is_bank = true;
				user.save();
			}

			/*end   创建测试用户修改*/
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("添加银行卡时：" + e.getMessage());
			error.code = -1;
			error.msg = "银行卡添加失败!";
			
			return error.code;
		}
		
		DealDetail.userEvent(this.userId, UserEvent.ADD_BANK, "添加银行账户", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "银行卡添加成功!";
		
		if(!isBank){
			//添加银行卡送积分
			Score.sendScore(userId,Score.BANK ,userBankAccounts.account, error);
		}

		try {
			//添加银行卡发红包
			String redTypeName = Constants.RED_PACKAGE_TYPE_BANK;//注册类型

			long status  = Constants.RED_PACKAGE_TYPE_STATUS_ENABLED;//启用状态

			List<t_red_packages_type> reds = RedPackage.getRegRed(redTypeName, status);
			if(null != reds && reds.size() > 0){
				for(t_red_packages_type redPackageType : reds){
					String desc = "";
					if(redPackageType.coupon_type == CouponTypeEnum.REDBAG.getCode() ) {
						desc = "添加银行卡发放红包";
					}else{
						desc = "添加银行卡发放加息券";
					}

					User user = new User();
					user.setId(userId);
					RedPackageHistory.sendRedPackage(user, redPackageType,desc);
				}
				Logger.info("添加银行卡发放优惠券短信通知成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}
	
	/**
	 * 修改用户银行帐号
	 * @param countId 帐号ID
	 * @param account
	 * @param accountName
	 */
	public int editUserBankAccount(long accountId, long userId, boolean isNeedBranchBankInfo, ErrorInfo error){
		error.clear();
		
		if(!isNeedBranchBankInfo){  //不支持提现银行卡支行信息时,branchBankName,province,city字段为null
			this.branchBankName = "";
			this.province = "";
			this.city = "";
		}
		
		if(isNeedBranchBankInfo && StringUtils.isBlank(this.branchBankName)) {  //支持提现银行卡支行信息时,branchBankName必填
			error.code = -1;
			error.msg = "支行名称不能为空";
			
			return error.code;
		}
		
		if(!isNeedBranchBankInfo && StringUtils.isBlank(this.bankName)) {  //不支持提现银行卡支行信息时，bankName必填
			error.code = -1;
			error.msg = "银行名称不能为空";
			
			return error.code;
		}
		
		if(StringUtils.isBlank(this.account)) {
			error.code = -1;
			error.msg = "账号不能为空";
			
			return error.code;
		}
		
		String account = t_user_bank_accounts.find("select account from t_user_bank_accounts where id = ?", accountId).first();
		if(!this.account.equals(account)) {
			error.msg = "银行账号不能修改!";
			error.code = -1;
			
			return error.code;
		}
		
		if(!RegexUtils.isBankAccount(this.account)) {
            error.code = -1;
            error.msg = "银行账号格式错误，应该是16-22位数字！";
            
            return error.code;
        }
		
		if(!RegexUtils.isMobileNum(this.mobile)) {
            error.code = -1;
            error.msg = "预留手机号格式错误";
            
            return error.code;
        }
		
		if(StringUtils.isBlank(this.accountName)) {
			error.code = -1;
			error.msg = "真实姓名不能为空";
			
			return error.code;
		}
		
		if(!isRightBank(accountId, userId, error)) {
			
			return error.code;
		}

		EntityManager em = JPA.em();
		String sql = "update t_user_bank_accounts set bank_name = ?,  bank_code=?, province=?, province_code=?, city=?, city_code=?, branch_bank_name=?, account = ?,account_name = ?,mobile = ? ,is_sign = ? where id = ?";
		
		Query query = em.createQuery(sql);
		query.setParameter(1, this.bankName.trim());
		query.setParameter(2, this.bankCode);
		query.setParameter(3, this.province);
		query.setParameter(4, this.provinceCode);
		query.setParameter(5, this.city);
		query.setParameter(6, this.cityCode);
		query.setParameter(7, this.branchBankName.trim());  //
		query.setParameter(8, this.account.trim());
		query.setParameter(9, this.accountName.trim());
		query.setParameter(10, this.mobile.trim());
		query.setParameter(11, true);
		query.setParameter(12, accountId);
		
		int rows = 0;
		
		try{
			rows = query.executeUpdate();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("编辑银行卡时：" + e.getMessage());
			error.code = -2;
			error.msg = "银行卡编辑失败!";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.userEvent(this.userId, UserEvent.EDIT_BANK, "编辑银行账户", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "银行卡账户编辑成功！";
		
		return 0;
	}
	
	
	public int appEditUserBankAccount(long accountId, long userId, boolean isNeedBranchBankInfo, ErrorInfo error,UserBankAccounts userB){
		error.clear();
		
		if(!isNeedBranchBankInfo){  //不支持提现银行卡支行信息时,branchBankName,province,city字段为null
			this.branchBankName = "";
			this.province = "";
			this.city = "";
		}
		
		if(isNeedBranchBankInfo && StringUtils.isBlank(this.branchBankName)) {  //支持提现银行卡支行信息时,branchBankName必填
			error.code = -1;
			error.msg = "支行名称不能为空";
			
			return error.code;
		}
		
		if(!isNeedBranchBankInfo && StringUtils.isBlank(this.bankName)) {  //不支持提现银行卡支行信息时，bankName必填
			error.code = -1;
			error.msg = "银行名称不能为空";
			
			return error.code;
		}
		
		if(StringUtils.isBlank(this.account)) {
			error.code = -1;
			error.msg = "账号不能为空";
			
			return error.code;
		}
		
		String account = t_user_bank_accounts.find("select account from t_user_bank_accounts where id = ?", accountId).first();
		if(!this.account.equals(account)) {
			error.msg = "银行账号不能修改!";
			error.code = -1;
			
			return error.code;
		}
		
		if(!RegexUtils.isBankAccount(this.account)) {
			error.code = -1;
			error.msg = "银行账号格式错误，应该是16-22位数字！";
			
			return error.code;
		}
		
		if(StringUtils.isBlank(this.accountName)) {
			error.code = -1;
			error.msg = "真实姓名不能为空";
			
			return error.code;
		}
		
		if(!isRightBank(accountId, userId, error)) {
			
			return error.code;
		}
		
		EntityManager em = JPA.em();
		String sql = "update t_user_bank_accounts set bank_name = ?,  bank_code=?, province=?, province_code=?, city=?, city_code=?, branch_bank_name=?, account = ?,account_name = ? where id = ?";
		
		Query query = em.createQuery(sql);
		query.setParameter(1, userB.bankName.trim());
		query.setParameter(2, userB.bankCode);
		query.setParameter(3, userB.province);
		query.setParameter(4, userB.provinceCode);
		query.setParameter(5, userB.city);
		query.setParameter(6, userB.cityCode);
		query.setParameter(7, userB.branchBankName.trim());  //
		query.setParameter(8, userB.account.trim());
		query.setParameter(9, userB.accountName.trim());
		query.setParameter(10, accountId);
		
		int rows = 0;
		
		try{
			rows = query.executeUpdate();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("编辑银行卡时：" + e.getMessage());
			error.code = -2;
			error.msg = "银行卡编辑失败!";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.userEvent(this.userId, UserEvent.EDIT_BANK, "编辑银行账户", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "银行卡账户编辑成功！";
		
		return 0;
	}
	
	/**
	 * 删除银行卡
	 * @param accountId
	 * @param error
	 * @return
	 */
	public static int deleteUserBankAccount(long userId, long accountId, ErrorInfo error){
		error.clear();
		
		if(accountId == 0) {
			error.code = -1;
			error.msg = "参数传入有误";
			
			return error.code;
		}
		
		if(!isRightBank(accountId, userId, error)) {
			
			return error.code;
		}
		
		EntityManager em = JPA.em();
		String sql = "delete t_user_bank_accounts where id = ?";
		
		Query query = em.createQuery(sql).setParameter(1, accountId);
		
		int rows = 0;
		
		try{
			rows = query.executeUpdate();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("删除银行卡时：" + e.getMessage());
			error.code = -2;
			error.msg = "银行卡删除失败!";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.userEvent(userId, UserEvent.DELETE_BANK, "删除银行账户", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "银行卡账户删除成功！";
		
		return 0;
	}
	
	
	
	/**
	 * 查询用户所有银行帐号信息
	 * @param userId
	 */
	public  static List<UserBankAccounts> queryUserAllBankAccount(long userId){
		
		List<UserBankAccounts> userBankAccountsList = new ArrayList<UserBankAccounts>();
		
		List<Long> idList = null;
		
		try{
			idList = t_user_bank_accounts.find("select id from t_user_bank_accounts where user_id = ? and is_valid = 1 order by id asc", userId).fetch();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询用户所有银行卡信息时：" + e.getMessage());
		}
		
		if(idList != null && idList.size() > 0 ){
			
			for(Long ids : idList){
				long id = ids;
				UserBankAccounts userBankAccount = new UserBankAccounts();
				userBankAccount.id = id;
				userBankAccountsList.add(userBankAccount);
			}
		}
		
		return userBankAccountsList;
	}
	
	/**
	 * 查询用户所有银行帐号信息
	 * @param userId
	 */
	public  static List<UserBankAccounts> queryUserAllVerifiedBankAccount(long userId){
		
		List<UserBankAccounts> userBankAccountsList = null;
		
		List<Long> idList = null;
		
		try{
			idList = t_user_bank_accounts.find("select id from t_user_bank_accounts where user_id = ? order by id asc", userId).fetch();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询用户所有银行卡信息时：" + e.getMessage());
		}
		
		if(idList != null && idList.size() > 0 ){
			
			userBankAccountsList = new ArrayList<UserBankAccounts>();
			
			UserBankAccounts userBankAccount = new UserBankAccounts();
			userBankAccount.id = idList.get(0);
			
			userBankAccountsList.add(userBankAccount);
		}
		
		return userBankAccountsList;
	}

	
	/**
	 * 判断是否重复使用同一银行帐号
	 * @param userId
	 * @param account
	 * @param accountName
	 * @return
	 */
	 
	public boolean isReuseBank(long userId,String account, int financeType, String accountName){
		
//		int count = (int) t_user_bank_accounts.count("account=?  ",account.replaceAll(" ", ""));
		long count  = 0;
		try {
			count =  t_user_bank_accounts.find(" select count(acct.id) from t_user_bank_accounts acct, t_users user "
					+ "where acct.user_id = user.id and acct.account= ? and user.finance_type = ? ", account.replaceAll(" ", ""), financeType).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(count > 0){
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * 绑定收款帐号
	 * @param accountId
	 * @return
	 */
	public int bindAccount(long accountId,long bidId,ErrorInfo error){
		
		EntityManager em = JPA.em();
		
		String sql = "update t_bids set bank_account_id = ?";
		Query query = em.createQuery(sql);
		query.setParameter(1, accountId);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			
			error.msg = "对不起！绑定银行账户失败！请您重试或联系平台管理员！";
			error.code = -1;
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		error.code = 0;
		
		return 1;
	}
	
	/**
	 * 判断一个银行账号是否属于一个用户
	 * @param accountId
	 * @param userId
	 * @param error
	 * @return
	 */
	public static boolean isRightBank(long accountId, long userId, ErrorInfo error) {
		UserBankAccounts account = new UserBankAccounts();
		account.id = accountId;
		
		if(account.id < 0 || account.userId != userId) {
			error.code = -1;
			error.msg = "请选择正确的银行账号";
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * 2015年11月25日
	 * 查询用户绑定的银行卡
	 */
	public static t_user_bank_accounts queryById(long userId) {
		t_user_bank_accounts bank = null;
		
		try{
			bank = t_user_bank_accounts.find("from t_user_bank_accounts where user_id = ? order by id asc", userId).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.error(e,"查询用户所有银行卡信息时：" + e.getMessage());
		}
		
		return bank;
	}

	/**
	 * 查询用户绑定的可用的银行卡
	 */
	public static t_user_bank_accounts querValidBankyById(long userId) {
		t_user_bank_accounts bank = null;

		try{
			bank = t_user_bank_accounts.find("from t_user_bank_accounts where user_id = ? and is_valid=1 order by id asc", userId).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.error(e,"查询用户所有银行卡信息时：" + e.getMessage());
		}

		return bank;
	}
	
	/**
	 * 
	 * @author liulj
	 * @creationDate. Apr 4, 2018 3:38:43 PM 
	 * @description.  查询用户银行卡
	 * 
	 * @param userId
	 * @return
	 */
	public static List<t_user_bank_accounts> queryMoreById(long userId) {
		List<t_user_bank_accounts> bank = null;
		
		try{
			bank = t_user_bank_accounts.find("from t_user_bank_accounts where user_id = ? and is_valid = 1 order by id asc", userId).fetch();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.error(e,"查询用户所有银行卡信息时：" + e.getMessage());
		}
		
		return bank;
	}
	
	/**
	 * 用户添加设置银行帐号
	 * @param isNeedBranchBankInfo 是否支持提现银行卡支行信息绑定，true，支持，false，不支持
	 * @param error
	 * @return 返回-1代表绑定失败，返回 1 代表绑定成功
	 */
	public int addBankAccount(ErrorInfo error){
		error.clear();
		
		t_user_bank_accounts userBankAccounts = new t_user_bank_accounts();
		
		userBankAccounts.time=new Date();
		userBankAccounts.user_id=this.userId;
		userBankAccounts.bank_name = this.bankName.trim();
		userBankAccounts.bank_code = this.bankCode;
		//userBankAccounts.province_code = this.provinceCode;
		//userBankAccounts.city_code = this.cityCode;
		//userBankAccounts.branch_bank_name = this.branchBankName.trim();
		//userBankAccounts.province = this.province;
		//userBankAccounts.city = this.city;
		userBankAccounts.account= this.account.trim();
		userBankAccounts.account_name= this.accountName.trim();
		
		try{
			userBankAccounts.save();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("添加银行卡时：" + e.getMessage());
			error.code = -1;
			error.msg = "银行卡添加失败!";
			
			return error.code;
		}
		
		DealDetail.userEvent(this.userId, UserEvent.ADD_BANK, "添加银行账户", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "银行卡添加成功!";
		
		return 0;
	}
	
	/**
	 * 
	 * @Title:        queryById 
	 * @Description:  查询用户银行卡
	 * @param:        @param userId
	 * @param:        @return    
	 * @return:       t_user_bank_accounts    
	 * @throws 
	 * @author        luochengwei
	 * @Date          2015-8-25 下午03:00:45
	 */
	public static t_user_bank_accounts queryByIds(long userId,String account) {
		t_user_bank_accounts bank = null;
		
		try{
			bank = t_user_bank_accounts.find("from t_user_bank_accounts where user_id = ? and verified = 0 and account = ?", userId,account).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.error(e,"查询用户所有银行卡信息时：" + e.getMessage());
		}
		
		return bank;
	}
	
	/**
	 * 
	 * @Title:        queryById 
	 * @Description:  查询用户是否绑定银行卡
	 * @param:        @param userId
	 * @param:        @return    
	 * @return:       t_user_bank_accounts    
	 * @throws 
	 * @author        luochengwei
	 * @Date          2015-8-25 下午03:00:45
	 */
	public static t_user_bank_accounts queryByAccount(String account,long userId) {
		t_user_bank_accounts bank = null;
		
		try{
			bank = t_user_bank_accounts.find("from t_user_bank_accounts where user_id = ? and account = ? and verified = 0", userId,account).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.error(e,"查询用户所有银行卡信息时：" + e.getMessage());
		}
		
		return bank;
	}
	
	/***
	 * 
	 * @Title:        updateBankNo 
	 * @Description:  充值成功后绑定银行卡
	 * @param:        @param error
	 * @param:        @return    
	 * @return:       int    
	 * @throws 
	 * @author        luochengwei
	 * @Date          2015-6-6 上午11:16:57
	 */
	public static  void updateBankNo(String account,String bank_code,ErrorInfo error){
		error.clear();
		EntityManager em = JPA.em();
		//int bankCode = Integer.parseInt(bank_code.substring(1, bank_code.length()));
		String bankName = "";
		for (int i = 0; i < Constants.BAOFU_TYPE.length; i++) {
			if(Constants.BAOFU_TYPE[i].equals(bank_code)) {
				bankName = Constants.BAOFU_BANK_NAME[i];
				break;
			}
		}
		/*for (int i = 0; i < Constants.GO_CODE.length; i++) {
			if(Constants.GO_CODE[i].equals(bankCode+"")) {
				bankName = Constants.GO_BANK_NAME[i];
				break;
			}
		}*/
		int row = 0;
		 String sql = "update t_user_bank_accounts set verified = 1,bank_code = ?,bank_name = ? , verify_time=?  where account = ?";
		 Query query = em.createNativeQuery(sql).setParameter(1,bank_code).setParameter(2,bankName).setParameter(3,DateUtil.dateToString(new Date())).setParameter(4, account);
		 try {
			row = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.error(e, "绑定银行卡出现：", e.getMessage());
			error.msg = "绑定银行卡异常";
			error.code = -1;
			return;
		}
		if(row == 0) {
			JPA.setRollbackOnly();
			error.msg = "绑定银行卡失败";
			error.code = -1;
			return;
		}

		error.msg = "绑定银行卡成功";
		error.code = 1;
		return;
	}
	
	
	public void updateUserBankAccountForPlatform(ErrorInfo error){
		
		error.clear();
		
		String account = t_user_bank_accounts.find("select account from t_user_bank_accounts where account = ? and id !=?", this.account,this.id).first();
		
		if(StringUtils.isNotBlank(account)){
			
			error.code = -1;
			
			error.msg = "您填写的银行账号已经存在，修改失败！！";
			
			return ;
		}
		
		EntityManager em = JPA.em();
		
		String sql = "update t_user_bank_accounts set bank_name = ?,  bank_code=?, province=?, province_code=?, city=?, city_code=?, branch_bank_name=?, account = ?,account_name = ? where id = ?";
		
		Query query = em.createQuery(sql);
		query.setParameter(1, this.bankName.trim());
		query.setParameter(2, this.bankCode);
		query.setParameter(3, this.province);
		query.setParameter(4, this.provinceCode);
		query.setParameter(5, this.city);
		query.setParameter(6, this.cityCode);
		query.setParameter(7, this.branchBankName.trim());  //
		query.setParameter(8, this.account.trim());
		query.setParameter(9, this.accountName.trim());
		query.setParameter(10, this.id);
		
		int rows = 0;
		
		try{
			rows = query.executeUpdate();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("编辑银行卡时：" + e.getMessage());
			error.code = -2;
			error.msg = "银行卡编辑失败!";
			
			return ;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return ;
		}
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return ;
		}
		
		error.code = 0;
		error.msg = "银行卡账户编辑成功！";
		
		return ;
		
	}
	
	/**
	 * 待审核提现列表 导出用户提现银行卡信息
	 * @param error
	 * @return
	 */
	public static List<Map<String, Object>> queryUserBanksList(){
		ErrorInfo errInfo = new ErrorInfo();
		/**
		 sql:
		 	SELECT 
				ub.user_id as user_id,
				(SELECT tu.reality_name FROM t_users tu WHERE tu.id = ub.user_id) AS name,
				ub.account AS account,
				(SELECT tdp.name FROM t_dict_province tdp WHERE tdp.code = ub.province_code) AS province,
				ub.city AS city,
				ub.branch_bank_name AS branch,
				ub.bank_name AS bankname,
				(SELECT w.amount FROM t_user_withdrawals w WHERE w.user_id = ub.user_id) AS amount,
				(SELECT w.status FROM t_user_withdrawals w WHERE w.user_id = ub.user_id) AS status
			FROM 
				t_user_bank_accounts ub
     		WHERE 
     			ub.province is NOT NULL
		*/
		String hql = "SELECT tu.reality_name as name,ub.account,tdp.name as province,ub.city AS city,ub.branch_bank_name AS branch,ub.bank_name AS bankname,IFNULL(w.amount,0) as amount FROM t_user_withdrawals w LEFT JOIN t_users tu ON w.user_id = tu.id and w.status = 0 LEFT JOIN t_user_bank_accounts ub ON ub.user_id = tu.id LEFT JOIN t_dict_province tdp ON tdp.CODE = ub.province_code where ub.province IS NOT NULL  and amount > 0";
		
		/** fix-180502-多卡多条记录问题 **/
		hql = "SELECT tu.reality_name as name,ub.account,tdp.name as province,ub.city AS city,ub.branch_bank_name AS branch,ub.bank_name AS bankname,IFNULL(w.amount,0) as amount FROM t_user_withdrawals w INNER JOIN t_users tu ON w.user_id = tu.id and w.status = 0 LEFT JOIN t_user_bank_accounts ub ON ub.id = w.bank_account_id LEFT JOIN t_dict_province tdp ON tdp.CODE = ub.province_code where amount > 0";
		
		return JPAUtil.getList(errInfo, hql);
		
	}
	
	
	/**
	 * 
	 * @author liulj
	 * @creationDate. Apr 26, 2018 4:48:26 PM 
	 * @description.  修改银行卡绑卡协议号
	 * 
	 * @param id
	 * @param protocolNo
	 * @param error
	 * @return
	 */
	public static int updateBankProtocolNo(long id, String protocolNo,boolean isSign, ErrorInfo error){
		
		EntityManager em = JPA.em();
		
		String sql = "update t_user_bank_accounts set protocol_no = ? , is_sign = ? where id = ?";
		Query query = em.createQuery(sql);
		query.setParameter(1, protocolNo);
		query.setParameter(2, isSign);
		query.setParameter(3, id);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			
			error.msg = "对不起！绑定银行账户失败！请您重试或联系平台管理员！";
			error.code = -1;
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "银行卡绑定成功!";
		
		return 1;
	}
	

	/**
	 * 是否签约（多张银行卡）
	 * @author wangyun
	 * @date 2019年4月2日
	 * @param userId
	 * @return
	 */
	public static boolean isNotSign(long userId){
		
		String sql = "select count(1) as count from t_user_bank_accounts  where user_id = ? and is_valid = 1  and protocol_no is not null and is_sign = 0 ";
		
		List<Map<String,Object>> countMap =  JPAUtil.getList(new ErrorInfo(), sql ,userId);
		int count = 0;
		if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
			count = ((BigInteger)countMap.get(0).get("count")).intValue();
		}
		
		return count == 0 ? false : true;
	}
	
	
	
	public static void updateBankIsSign(long userId,   ErrorInfo error){
		
		List<t_user_bank_accounts> banks = null;
		
		try{
			banks = t_user_bank_accounts.find("from t_user_bank_accounts where user_id = ? and is_valid = 1 and protocol_no is not null and is_sign = 0 ", userId).fetch();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.error(e,"查询用户所有银行卡信息时：" + e.getMessage());
			error.code = -1;
			error.msg= "查询用户所有银行卡信息异常";
			return;
		}
		
		try {
			if(banks != null && banks.size() > 0) {
				for (t_user_bank_accounts bank : banks) {
					bank.is_sign = true;
					bank.save();
				}
			}	
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg= "查询用户所有银行卡信息异常";
			return;
		}
		
		 
	}
}