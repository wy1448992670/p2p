package dao;

import java.util.Date;

import javax.persistence.Query;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import models.t_users;
import play.db.jpa.JPA;

public class UserDao {

	/**
	 * @Description 更新用户表字段 id_picture_authentication_status
	 *              id_picture_authentication_time
	 * @param userId 用户id
	 * @param status 状态
	 * @author: zj
	 */
	public static void updateUserIdCardStatus(long userId, int status) {
		String sql = " update t_users set id_picture_authentication_status = ?, id_picture_authentication_time = ?  where  id=? ";
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, status).setParameter(2, new Date()).setParameter(3, userId);
		query.executeUpdate();
	}

	/**
	 * @Description 修改活体认证状态
	 * @param userId
	 * @param status
	 * @author: zj
	 */
	public static void updateUserLivingStatus(long userId, int status) {
		String sql = " update t_users set living_authentication_status = ?,living_authentication_time = ?  where id=? ";
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, status).setParameter(2, new Date()).setParameter(3, userId);
		query.executeUpdate();
	}

	/**
	 * @Description 通过id查询一条用户信息
	 * @param userId
	 * @return
	 * @author: zj
	 */
	public static t_users getUserById(long userId) {
		t_users users = t_users.findById(userId);
		return users;
	}

	/**
	 * @Description 更新用户身份证姓名 卡号 性别
	 * @param userId
	 * @param name
	 * @param idCardNo
	 * @author: zj
	 */
	public static void updateUsersIdCardInfo(long userId, String name, String idCardNo) {
		
		int sex = 3; //未知
		String sexNum = idCardNo.substring(16, 17);
		if(Integer.parseInt(sexNum) % 2 != 0) {
			sex = 1; //男
		}else {
			sex = 2; //女
		}
		
		String sql = " update t_users set id_number = ?,reality_name = ?,sex = ?  where id=? ";
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, idCardNo).setParameter(2, name).setParameter(3, sex).setParameter(4, userId);
		query.executeUpdate();
	}

	/**
	 * @Description 更新用户类型
	 * @param userType
	 * @param id
	 * @author: zj
	 */
	public static void updateUserType(int userType,long id) {
		String sqlString = " update t_users set user_type=? where id=? ";
		Query query = JPA.em().createNativeQuery(sqlString).setParameter(1, userType).setParameter(2,id);
		query.executeUpdate();
	}

	/**
	 * @Description 更新用户
	 * @param t_users
	 * @author: zj
	 */
	public static void updateUser(t_users t_users) {
		t_users.save();
	}

	/**
	 * @Description 根据id查询一个用户
	 * @param id
	 * @return
	 * @author: zj
	 */
	public static t_users getUsersById(long id) {
		return t_users.findById(id);
	}
}
