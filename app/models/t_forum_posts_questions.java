package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

@Entity
public class t_forum_posts_questions extends Model{
	public String content;
	public String to_answer_user;
	public long user_id;
	public long posts_id;
	public long to_answer_user_id;
	public Date time;
	public int answer_status;
	public int read_status;
	
	@Transient
	public String userName;
}