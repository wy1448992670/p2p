package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class t_score_convert extends Model {
	public long user_id;
	public int credits;
	public String item_code;
	public Date time;
	public String description;
	public String order_num;
	public String type;
	public int face_price;
	public int actual_price;
	public String ip;
	public boolean wait_audit;
	public String params;
	public String mark;
	public boolean status;
}
