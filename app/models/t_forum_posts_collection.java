package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;
@Entity
public class t_forum_posts_collection extends Model{
	public long posts_id;
	public long user_id;
	public Date time;
	
}
