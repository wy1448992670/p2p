/******************************************************************
 *
 *    Java Lib For Android, Powered By Shenzhen Jiuzhou.
 *
 *    Copyright (c) 2001-2014 Digital Telemedia Co.,Ltd
 *    http://www.d-telemedia.com/
 *
 *    Package:     dao
 *
 *    Filename:    UserCityDao.java
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
 *    Create at:   2019年1月3日 下午4:12:36
 *
 *    Revision:
 *
 *    2019年1月3日 下午4:12:36
 *        - first revision
 *
 *****************************************************************/
package dao;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import business.NewCity;
import business.NewProvince;
import models.t_user_city;
import play.db.jpa.JPA;

/**
 * @ClassName UserCityDao
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2019年1月3日 下午4:12:36
 * @version 1.0.0
 */
public class UserCityDao {

	/**
	 * @Description 保存用户的户籍地址
	 * @param userId
	 * @param address
	 * @author: zj
	 */
	public static void updateAddress(long userId, String address) {
		String sql = " update t_user_city set address=? where user_id=? ";
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, address).setParameter(2, userId);
		query.executeUpdate();
	}

	/**
	 * @Description 根据userId，查询是否存在记录
	 * @param userId
	 * @return
	 * @author: zj
	 */
	public static boolean isExistByUserId(long userId) {
		boolean flag = false;
		Long id = 0L;
		t_user_city t_user_city = new t_user_city();
		id = t_user_city.find(" select id from t_user_city where user_id=? ", userId).first();
		if (id != null && id > 0) {
			return true;
		}
		return flag;
	}

	/**
	 * @Description 新增user_city数据
	 * @param userId
	 * @param address
	 * @author: zj
	 * @throws Exception 
	 */
	public static void addOrUpdateUserCity(long userId, String idCardNo, String address) throws Exception {
		
		//实名认证后添加地址
		//截取身份证前6位
		if(idCardNo==null || idCardNo.length()<4){
			throw new Exception("保存户籍地址时,没有身份证信息");
		}
		String provinceId = idCardNo.substring(0, 2);
		String cityId = idCardNo.substring(0, 4);
		provinceId = provinceId + "0000"; //补全省 ，6位数
		cityId = cityId + "00"; //补全区 ，6位数
		
		t_user_city user_city = t_user_city.find(" user_id = ? ", userId).first();
		if(user_city==null){
			user_city = new t_user_city();
		}
		
		user_city.user_id = userId;
		user_city.province_id = provinceId;
		user_city.province = NewProvince.getProvince(provinceId);	//获取省名称
		user_city.city_id = cityId;
		user_city.city = NewCity.getCity(cityId);
		if(!StringUtils.isBlank(address)){
			user_city.address = address;
		}
		
		user_city.save();
		
	}
}
