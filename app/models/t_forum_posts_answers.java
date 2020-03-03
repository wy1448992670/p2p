package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;
@Entity
public class t_forum_posts_answers extends Model{
	public long question_id;
	public long user_id;
	public long posts_id;
	public Date time;
	public String content;
	public String question_user_name;
	public int type;
	public int read_status;
	
}
