package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 财富圈，返佣记录
 * 
 * @author hys
 */
@Entity
public class t_wealthcircle_income extends Model{
	public long user_id;  //邀请人id
	public String invite_code;  //邀请码
	public Date time;  //记录创建时间
	public long invited_user_id;  //受邀人id
	public Date invest_time;  //受邀人投资时间
	public double invest_amount;  //受邀人投标金额
	public double invite_income;  //邀请人获得返佣金额
	public Date pay_time;  //佣金发放时间
	public int status;  //返佣状态。0未发，1已发
	
	public t_wealthcircle_income(){}

	public t_wealthcircle_income(Date invesTime, double investAmount, double inviteIncome){
		this.invest_time = invesTime;
		this.invest_amount = investAmount;
		this.invite_income = inviteIncome;
	}
}