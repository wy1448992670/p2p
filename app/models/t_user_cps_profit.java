package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

/**
 * 
 * @description.  CPS分成统计
 *  
 * @modificationHistory.  
 * @author liulj 2017年2月24日上午10:33:46 TODO
 */
@Entity
public class t_user_cps_profit extends Model{
	public long user_id;
	public String user_mobile;
	public long recommend_user_id;
	public long bid_id;
	public String bid_title;
	public int bid_is_loan;
	public long invest_id;
	public double invest_corpus;
	public double invest_interest;
	public Date invest_time;
	public double cps_reward;
	public double cps_rate;
	public Date ins_dt;
	
	public t_user_cps_profit(){}
	
	public t_user_cps_profit(String user_mobile, String bid_title, double cps_reward, Date ins_dt){
		this.user_mobile = user_mobile;
		this.bid_title = bid_title;
		this.cps_reward = cps_reward;
		this.ins_dt = ins_dt;
	}
}
