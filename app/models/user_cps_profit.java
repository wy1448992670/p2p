package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

@Entity
public class user_cps_profit extends Model{
	public String user_mobile;
	public String bid_title;
	public double cps_reward;
	public Date ins_dt;
	
	public user_cps_profit(){}
	
	public user_cps_profit(String user_mobile, String bid_title, double cps_reward, Date ins_dt){
		this.user_mobile = user_mobile;
		this.bid_title = bid_title;
		this.cps_reward = cps_reward;
		this.ins_dt = ins_dt;
	}
}
