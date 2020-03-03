package business;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import models.t_mall_address;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import constants.MallConstants;

/**
 * 积分商城 收货地址
 * 
 * @author yuy
 * @time 2015-10-13 17:17
 *
 */
public class MallAddress {

	/**
	 * 查询收货地址
	 * 
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static List<t_mall_address> queryAddressByList(long user_id, ErrorInfo error) {
		
		List<t_mall_address> list = null;
		try {
			list = t_mall_address.find(" user_id = ? order by time desc ", user_id).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询收货地址时:" + e.getMessage());
			error.code = MallConstants.DML_ERROR_CODE;
			return null;
		}

		return list;
	}

	/**
	 * 查询收货地址
	 * 
	 * @param id
	 * @return
	 */
	public static t_mall_address queryAddressById(long id) {
		t_mall_address address = null;
		try {
			address = t_mall_address.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询收货地址时：" + e.getMessage());
		}
		return address;
	}

	/**
	 * 保存积分规则
	 * 
	 * @param goods
	 * @return
	 */
	public static int saveAddress(t_mall_address address) {
		if (address == null)
			return MallConstants.COM_ERROR_CODE;
		// update
		if (address.id != null) {
			address = clone(address);
		}

		try {
			address.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("保存积分规则时：" + e.getMessage());
			return MallConstants.DML_ERROR_CODE;
		}
		return MallConstants.SUCCESS_CODE;
	}

	/**
	 * 克隆
	 * 
	 * @param goods
	 * @return
	 */
	private static t_mall_address clone(t_mall_address address) {
		if (address == null)
			return null;
		if (address.id == null)
			return address;

		t_mall_address address_ = queryAddressById(address.id);
		address_.receiver = address.receiver;
		address_.tel = address.tel;
		address_.address = address.address;
		address_.postcode = address.postcode;
		return address_;
	}

	/**
	 * 查询收货地址
	 * 
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static boolean queryAddressHasDefault(long user_id, ErrorInfo error) {

		List<t_mall_address> list = null;
		try {
			list = t_mall_address.find(" user_id = ? and is_default = 1 ", user_id).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询收货地址时:" + e.getMessage());
			error.code = MallConstants.DML_ERROR_CODE;
			return false;
		}
		if (list == null || list.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 查询用户默认收货地址
	 * 
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static t_mall_address queryAddress(long user_id, ErrorInfo error) {

		List<t_mall_address> list = null;
		try {
			list = t_mall_address.find(" user_id = ? and is_default = 1 ", user_id).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询收货地址时:" + e.getMessage());
			error.code = MallConstants.DML_ERROR_CODE;
			return null;
		}
		if (list == null || list.size() == 0) {
			return null;
		} else {
			return list.get(0);
		}
	}
	
	/**
	 * 默认唯一，修改其他默认地址
	 * @param userId
	 */
	public static void updateMallDefaultStatue(long userId){
		String sql = "update t_mall_address set is_default = 0 where user_id = ? ";
		
		Query query = JPA.em().createQuery(sql).setParameter(1, userId);
		try {
			query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info(e.getMessage());
			
		}
	}
	
	/**
	 * 删除地址
	 * @param id
	 */
	public static int deleteUserAddress(long id){
		return t_mall_address.delete(" id = ? ", id);
	}
	
	/**
	 * 编辑地址
	 * @param t
	 */
	public static void eidtUserAddress(t_mall_address t){
		String sql = "update t_mall_address set time = ?,receiver = ?,tel = ?,address = ?,province_id = ?,city_id=?, is_default = ? where id = ?";
		Query query = JPA.em().createQuery(sql);
		query.setParameter(1, new Date());
		query.setParameter(2, t.receiver);
		query.setParameter(3, t.tel);
		query.setParameter(4, t.address);
		query.setParameter(5, t.province_id);
		query.setParameter(6, t.city_id);
		query.setParameter(7, t.is_default);
		query.setParameter(8, t.id);
		
		
		try {
			query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info(e.getMessage());
			
		}
	}
}
