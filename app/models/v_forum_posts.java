package models;



import javax.persistence.Entity;

import play.db.jpa.Model;
@Entity
public class v_forum_posts extends Model{
	
	public String title;
	public java.util.Date add_time;
	public java.util.Date show_time;
	public int read_count;
	public int answers_count;
	public String typeName;
	public String content;
	public long user_id;
	public int status;
	public String userName;
	
	public int rId;
}
