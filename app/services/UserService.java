/******************************************************************
 *
 *    Java Lib For Android, Powered By Shenzhen Jiuzhou.
 *
 *    Copyright (c) 2001-2014 Digital Telemedia Co.,Ltd
 *    http://www.d-telemedia.com/
 *
 *    Package:     services
 *
 *    Filename:    UserService.java
 *
 *    Description: TODO(用一句话描述该文件做什么)
 *
 *    Copyright:   Copyright (c) 2001-2014
 *
 *    Company:     Digital Telemedia Co.,Ltd
 *
 *    @author:     zj
 *
 *    @version:    1.0.0
 *
 *    Create at:   2019年1月2日 下午5:59:21
 *
 *    Revision:
 *
 *    2019年1月2日 下午5:59:21
 *        - first revision
 *
 *****************************************************************/
package services;

import java.util.Date;

import com.alibaba.druid.util.StringUtils;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import business.User;
import dao.UserCityDao;
import dao.UserDao;
import models.t_users;
import play.db.jpa.Transactional;

/**
 * @ClassName UserService
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2019年1月2日 下午5:59:21
 * @version 1.0.0
 */
public class UserService {

	/**
	 * @Description 将新的身份认证信息与老的信息比较<br>
	 *              如果名字 或者卡号不匹配则返回false
	 * @param userId
	 * @param name
	 * @param idCardNo
	 * @return true 匹配 false 不匹配
	 * @author: zj
	 */
	public static boolean checkUserIdCardInfo(long userId, String name, String idCardNo) {
		t_users t_users = UserDao.getUserById(userId);
		if (t_users != null) {// 存在老用户
			if (!StringUtils.isEmpty(t_users.reality_name)) {// 老用户实名认证过
				if (!name.equals(t_users.reality_name) || !idCardNo.equals(t_users.id_number)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @Description 用户实名认证通过后，一系列表更新操作
	 * @param userId 用户id
	 * @param status 状态
	 * @author: zj
	 * @throws Exception
	 */
	@Transactional
	public static void updateUserIdCardInfo(long userId, int status, String name, String idCardNo, String address)
			throws Exception {
		UserDao.updateUserIdCardStatus(userId, status);
		UserDao.updateUsersIdCardInfo(userId, name, idCardNo);
		UserCityDao.addOrUpdateUserCity(userId, idCardNo, address);
	}

	/**
	 * @Description 修改活体认证状态
	 * @param userId
	 * @param status
	 * @author: zj
	 */
	public static void updateUserLivingStatus(long userId, int status) {
		UserDao.updateUserLivingStatus(userId, status);
	}

	/**
	 * @Description 更新用户表字段 id_picture_authentication_status
	 *              id_picture_authentication_time
	 * @param userId 用户id
	 * @param status 状态
	 * @author: zj
	 */
	public static void updateUserIdCardStatus(long userId, int status) {
		UserDao.updateUserIdCardStatus(userId, status);
	}

	/**
	 * @Description 更新用户类型 （app端 实名认证使用）
	 * @param id
	 * @param userType 用户类型
	 * @return
	 * @author: zj
	 */
	public static boolean updateUserType(long id, int userType) {
		t_users users = UserDao.getUserById(id);
		if (users.user_type == null || users.user_type == 0 || users.user_type == 1) {
			UserDao.updateUserType(userType, id);
			return true;
		} else {
			return false;
		}
	}

	public static t_users getUserById(long userId) {
		return UserDao.getUserById(userId);
	}

	/**
	 * @Description 更新用户状态
	 * @param id     t_users 主键
	 * @param status mobile_operator_authentication_status 对应此字段的值
	 * @author: zj
	 */
	public static void updateUser(long id, int status) {
		t_users user = t_users.findById(id);
		user.mobile_operator_authentication_status = status;
		user.mobile_operator_authentication_time = new Date();
		UserDao.updateUser(user);
	}

}
