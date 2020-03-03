/******************************************************************
 *
 *    Java Lib For China, Powered By Chinese Programmer.
 *
 *    Copyright (c) 2001-2099 Digital Telemedia Co.,Ltd
 *    http://www.china.com/
 *
 *    Package:     services
 *
 *    Filename:    UserbankAccountService.java
 *
 *    Description: give you  a little color see see  
 *
 *    Copyright:   Copyright (c) 2001-2014
 *
 *    Company:     Digital Telemedia Co.,Ltd
 *
 *    @author:     zj
 *
 *    @version:    1.0.0
 *
 *    Create at:   Apr 2, 2019 1:40:34 PM
 *
 *    Revision:
 *
 *    Apr 2, 2019 1:40:34 PM
 *        - first revision
 *
 *****************************************************************/
package services;

import java.util.List;

import models.t_user_bank_accounts;
import play.Logger;
import utils.ErrorInfo;
import utils.JPAUtil;

/**
 * @ClassName UserbankAccountService
 * @这里用一句话描述这个方法的作用
 * @author zj
 * @Date Apr 2, 2019 1:40:34 PM
 * @version 1.0.0
 */
public class UserbankAccountService {

	/**
	 * 查询所有需要签章代扣合同的银行卡
	 * 
	 * @return
	 * @author: zj
	 */
	public static List<t_user_bank_accounts> queryAllNeedDeductPact() {
		List<t_user_bank_accounts> bank = null;
		try {
			bank = t_user_bank_accounts
					.find(" from t_user_bank_accounts where is_valid = 1 and is_sign=1 and deduct_pact_url is null").fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e, "查询协议银行卡未生成代扣协议合同的记录时：" + e.getMessage());
		}
		return bank;
	}

	/**
	 * 更新用户银行卡协议路径
	 * 
	 * @param userId
	 * @param account
	 * @author: zj
	 */
	public static void updateUserBank(long userId, String account, String deductPact_url) {
		ErrorInfo error = new ErrorInfo();
		String sql = " update t_user_bank_accounts set deduct_pact_url=? where user_id=? and is_valid=1 and account=?  ";
		JPAUtil.executeUpdate(error, sql, deductPact_url, userId, account);
	}

}
