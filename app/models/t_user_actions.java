package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class t_user_actions extends Model {
	public long user_id;
	public int type;
	public Date action_time;
	public int status;
	public String msg;
	public String record;
}
