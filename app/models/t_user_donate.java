package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 
 * @description.  用户捐款记录
 *  
 * @modificationHistory.  
 * @author liulj 2017年3月17日上午10:37:51 TODO
 */
@Entity
public class t_user_donate extends Model{
	
	public long user_id;
	public long benefit_id;
	public long bid_id;
	public int is_donate;
	public double invest_corpus;
	public double invest_periods;
	public double user_donate;
	public double admin_donate;
	public Date ins_dt;
	
	public t_user_donate(){}
	
}
