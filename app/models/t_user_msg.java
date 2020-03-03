package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class t_user_msg extends Model{
	public long user_id;
	public String msg;
	public Date ins_dt;
	
	
	public t_user_msg(){}
}
