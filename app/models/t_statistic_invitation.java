package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 财富圈佣金发放明细报表（月报）
 *
 * @author hys
 * @createDate  2015年7月31日 下午3:25:12
 *
 */
@Entity
public class t_statistic_invitation extends Model {

	public Date time; 
	public int year;  
	public int month; 
	public int invite_code_count;  //新增邀请码数
	public int invited_user_count;  //新增受邀会员数
	public int invited_recharge_user_count;  //新增充值会员
	public int invited_invest_user_count;  //新增理财会员
	public double invest_amount;  //受邀理财金额
	public double invitation_income;  //返佣金额
	
}
