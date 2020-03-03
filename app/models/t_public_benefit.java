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
public class t_public_benefit extends Model{
	
	public String name;
	public Date start_dt;
	public double plan_donate;
	public double actual_donate;
	public int donate_users;
	public String descr;
	public String cover;
	public String content;
	public Date ins_dt;
	
	public t_public_benefit(){}
	
}
