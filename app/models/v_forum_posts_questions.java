package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;
@Entity
public class v_forum_posts_questions extends Model{
	public long posts_id;
	public String title;
	public String content;
	public int show_image;
	public Date time;
	public String userName;
	public int answers_count;
	public int read_count;
	@Transient
	public String timeBetween;
	
}
