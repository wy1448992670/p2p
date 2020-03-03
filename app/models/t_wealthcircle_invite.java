/**
 * 
 */
package models;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import business.User;
import business.Optimization.UserOZ;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;
import constants.Constants;

/**
 * 财富圈，邀请码实体类
 * 
 * @author hys
 */
@Entity
public class t_wealthcircle_invite extends Model {
	public long user_id;  //邀请人id
	public String user_name;  //邀请人用户名  	
	public Date time;  //创建时间
	public String invite_code;  //邀请码
	public long invited_user_id;  //被邀请人id
	public String invited_user_name;  //被邀请人用户名
	public Date invited_register_time;  //被邀人注册时间
	public int status;  //1：未使用，2：已使用，3：已过期
	public int effective_time;  //有效期限，单位月，0月表示永久有效
	public int type;  //邀请码来源。1：理财，2：赠送
	public double current_total_invist_amount;  //获得该邀请码时，邀请人的累计收益
	public double qual_amount;  //邀请码资质金额
	public double invite_income_rate;  //当时送邀请码时的返佣年利率
	public double invited_user_discount;  //当时送邀请码时的受邀请人理财手续费折扣
	public long distribution_id;  //赠送邀请码的管理员Id
	public int is_active; //是否启用
	
	@Transient
	public String invitedUserSign;
	public String getInvitedUserSign(){
		
		if(null == this.invitedUserSign){
			this.invitedUserSign = Security.addSign(this.invited_user_id, Constants.USER_ID_SIGN);
		}
		
		return this.invitedUserSign;
	}
	
	public t_wealthcircle_invite(){}
	public t_wealthcircle_invite(long id, String invite_code, int status, Date time, int type, Date invited_register_time, int effective_time){
		this.id = id;
		this.invite_code = invite_code;
		this.status = status;
		this.time = time;
		this.type = type;
		this.invited_register_time = invited_register_time;
		this.effective_time = effective_time;
	}
	
	public t_wealthcircle_invite(long id, String invite_code, int status, int type, String user_name, long user_id, long invited_user_id, String invited_user_name, int is_active){
		this.id = id;
		this.invite_code = invite_code;
		this.status = status;
		this.type = type;
		this.user_name = user_name;
		this.user_id = user_id;
		this.invited_user_id = invited_user_id;
		this.invited_user_name = invited_user_name;
		this.is_active = is_active;
	}
	
	public t_wealthcircle_invite(long id, String invite_code, int status, int type, String user_name, long user_id){
		this.id = id;
		this.invite_code = invite_code;
		this.status = status;
		this.type = type;
		this.user_name = user_name;
		this.user_id = user_id;
	}
	
	public t_wealthcircle_invite(long invited_user_id, String invited_user_name, String invite_code, Date invited_register_time){
		this.invited_user_id = invited_user_id;
		this.invited_user_name = invited_user_name;
		this.invite_code = invite_code;
		this.invited_register_time = invited_register_time;
	}
	/**
	 * 受邀会员累计投资金额
	 */
	@Transient
	public double total_invest_amount;  
	public double getTotal_invest_amount(){
		String sql = "SELECT SUM(invest_amount) FROM t_wealthcircle_income WHERE invited_user_id = ?";
		
		Object count = null;
		
		try {
			count = JPA.em().createNativeQuery(sql).setParameter(1, this.invited_user_id).getSingleResult();
		} catch (Exception e) {
			Logger.error("邀请码带来的累计投资金额时，%s", e.getMessage());

		}
		
		return count == null ? 0 : ((BigDecimal)count).doubleValue();
	}
	
	/**
	 * 邀请码带来的累计佣金
	 */
	@Transient
	public double total_income;  
	public double getTotal_income(){
		String sql = "SELECT SUM(invite_income) FROM t_wealthcircle_income WHERE invite_code = ? AND status = 1";
		
		Object count = null;
		
		try {
			count = JPA.em().createNativeQuery(sql).setParameter(1, this.invite_code).getSingleResult();
		} catch (Exception e) {
			Logger.error("邀请码带来的累计佣金时，%s", e.getMessage());

		}
		
		return count == null ? 0 : ((BigDecimal)count).doubleValue();
	}
	
	/**
	 * 邀请码状态
	 * @return
	 */
	public String getStatusString(){
		if (this.status == 1){
			return "未使用";
		}else if(this.status == 2){
			return "已使用";
		}else if(this.status == 3){
			return "已过期";
		}else{
			return "";
		}
	}
	
	/**
	 * 邀请码来源
	 * @return
	 */
	public String getTypeString(){
		if (this.type == 1){
			return "理财";
		}else{
			return "赠送";
		}
	}
	
	/**
	 * 计算到期日期
	 * @return
	 */
	public Date getActiveDate(){
		if (effective_time != 0 && null != time){
			Calendar cal = Calendar.getInstance();
			cal.setTime(this.time);
			cal.add(java.util.Calendar.MONTH, effective_time);
			return cal.getTime();
		}
		else{
			return new Date();
		}
	}
	
	/**
	 * 受邀人充值金额
	 * @return
	 */
	public double getInvitedUserInvestMoney(){
		double money = 0;
		try {
			ErrorInfo error = new ErrorInfo();
			if (StringUtils.isEmpty(invited_user_name)){
				return 0;
			}
			PageBean<v_user_info> page = User.queryUserBySupervisor(invited_user_name, null, null, null, null, null, null, null, null, null, error);
			if (null != page && null != page.page && 0 != page.page.size()){
				return page.page.get(0).recharge_amount;
			}
		} catch (Exception e) {
		}
		return money;
		
	}
	
	/**
	 * 受邀人投资金额
	 * @return
	 */
	public double getInvitedUserMoney(){
		double money = 0;
		try {
			if (this.invited_user_id == 0){
				return 0;
			}
			UserOZ accountInfo = new UserOZ(this.invited_user_id);
			return accountInfo.getInvest_amount();
		} catch (Exception e) {
		}
		return money;
	}
	
	public double getUserIncome(){
		String sql = "SELECT SUM(invite_income) FROM t_wealthcircle_income WHERE invite_code = ?";
		
		Object count = null;
		
		try {
			count = JPA.em().createNativeQuery(sql).setParameter(1, this.invite_code).getSingleResult();
		} catch (Exception e) {
			Logger.error("邀请码带来的累计佣金时，%s", e.getMessage());

		}
		
		return count == null ? 0 : ((BigDecimal)count).doubleValue();
	}
}