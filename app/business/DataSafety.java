package business;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_users;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import constants.Constants;

/**
 * 数据防篡改
 * Description:
 * @author zhs
 * vesion: 6.0 
 * @date 2014-8-1 上午10:39:41
 */
public class DataSafety implements Serializable{
	
	public long id;
	private long _id;
	
	public String sign1;
	public String sign2;
	public String balance1;
	public String freeze1;
	public String amount;
	public String balance2;
	public String freeze2;
	public String recieveAmount;
	
	public void setId(long id){
		
		this._id = id;
		
		Map<String,Object> userMap = new HashMap<String,Object>();
		
		String sql = "select new Map(a.balance as balance, a.freeze as freeze, a.sign1 as sign1) from t_users as a where a.id = ?";
		
		try{
			userMap = t_users.find(sql, id).first();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("根据用户id查找用户信息时："+e.getMessage());
			this._id = -1;
			JPA.setRollbackOnly();
			
			return;
		}
		
		if(userMap != null) {
			this.balance1 = String.valueOf((Double)userMap.get("balance"));
			this.freeze1 = String.valueOf((Double)userMap.get("freeze"));
			this.sign1 = (String)userMap.get("sign1");
		}
		
		List userDetails = null;
        String sql2 = "SELECT ud.amount, ud.balance,ud.freeze,ud.recieve_amount,u.sign2 "
        		+ "FROM t_users u LEFT JOIN t_user_details ud ON u.id = ud.user_id WHERE u.id = ? ORDER BY ud.id DESC limit 1 ";
		try{
			userDetails = JPA.em().createNativeQuery(sql2).setParameter(1, id).getResultList();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("根据用户id查找用户明细表信息时："+e.getMessage());
			this._id = -1;
			JPA.setRollbackOnly();
			
			return;
		}
		
		if(userDetails != null && userDetails.size() > 0) {
			
			Object[] userDetail = (Object[]) userDetails.get(0);
			
			this.amount = userDetail[0]==null ? (0.00 + "") : String.valueOf(Double.parseDouble(userDetail[0].toString()));
			this.balance2 = userDetail[1]==null ? (0.00 + "") : String.valueOf(Double.parseDouble(userDetail[1].toString()));
			this.freeze2 = userDetail[2]==null ? (0.00 + "") : String.valueOf(Double.parseDouble(userDetail[2].toString()));
			this.recieveAmount = userDetail[3]==null ? (0.00 + "") : String.valueOf(Double.parseDouble(userDetail[3].toString()));
			this.sign2 = userDetail[4].toString();
		}
	}
	
	public long getId(){
		return this._id;
	}

	/**
	 * 对比数据库里面的值，判断是否被篡改
	 * @param error
	 * @return false 已被篡改  true 未被篡改
	 */
	public boolean signCheck(ErrorInfo error){
		error.clear();
		
		if(StringUtils.isBlank(this.sign1) || StringUtils.isBlank(this.sign2)){
			error.code = -1;
			error.msg = "尊敬的用户，你的账户资金出现异常变动，请速联系管理员";
			return false;
		}
		
		String userSign1 = com.shove.security.Encrypt.MD5(Long.toString(this._id) + this.balance1 + this.freeze1 + Constants.ENCRYPTION_KEY);
		
		String userSign2 = com.shove.security.Encrypt.MD5(Long.toString(this._id) + this.balance2 + 
				this.freeze2 + this.amount + this.recieveAmount + Constants.ENCRYPTION_KEY);
		
		if(!this.sign1.equalsIgnoreCase(userSign1)){
			error.code = -2;
			error.msg = "尊敬的用户，你的账户资金出现异常变动，请速联系管理员";
			Logger.error("账户资金出现异常变动");
			return false;
		}
		
		if(!this.sign2.equalsIgnoreCase(userSign2)){
			error.code = -3;
			error.msg = "尊敬的用户，你的交易资金出现异常变动，请速联系管理员";
			Logger.error("交易资金出现异常变动");
			return false;
		}
		
		return true;
	} 
	
	/**
	 * MD5生成新的标记
	 * @param error
	 * @return
	 */
	private int updateSign(ErrorInfo error){
		error.clear();

		EntityManager em = JPA.em();
		String userSign1 = com.shove.security.Encrypt.MD5(Long.toString(this._id) + this.balance1 + this.freeze1 + Constants.ENCRYPTION_KEY);
		
		String userSign2 = com.shove.security.Encrypt.MD5(Long.toString(this._id) + this.balance2 + 
				this.freeze2 + this.amount + this.recieveAmount + Constants.ENCRYPTION_KEY);
		
		String updateSql = "update t_users set sign1 = ?, sign2 = ? where id = ?";
		
		Query query = em.createQuery(updateSql).setParameter(1, userSign1).setParameter(2, userSign2).setParameter(3, this._id);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("更改用户防篡改标志时："+e.getMessage());
			error.code = -1;
			error.msg = "更改用户防篡改标志出现错误";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		if(rows < 0){
			error.code = -1;
			error.msg = "更改用户防篡改标志操作没有执行";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		
		return error.code;
	}
	
	/**
	 * 更新防篡改标识,加锁     add v8.0.1
	 * 
	 * @param userId
	 * @param error
	 */
	public void updateSignWithLock(long userId, ErrorInfo error) {
		
		if (userId == 0) {
			Logger.info("更新防篡改标识,加锁:userId等于0");
			
			return ;
		}
		
		User.addLock(userId);  //加锁
		
		try {
			setId(userId);
			updateSign(error);
		} finally {
			
			User.deleteLock(userId);  //解锁
		}
	}
}