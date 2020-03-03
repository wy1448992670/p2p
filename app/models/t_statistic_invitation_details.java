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
public class t_statistic_invitation_details extends Model {

	public Date time; 
	public int year;  
	public int month; 
	public String user_name;  //邀请人用户名
	public int total_invite_code;  //累计邀请码数量
	public double invited_user_invest_amount;  //受邀理财金额
	public double invitation_income;  //返佣金额
	
}
