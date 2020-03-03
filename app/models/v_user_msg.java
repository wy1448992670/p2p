package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

@Entity
public class v_user_msg extends Model{
	public long user_id;
	public String msg;
	public Date ins_dt;
	
	public String photo;
	public String name;
	@Transient
	public String date;
	
	public v_user_msg(){}
}
