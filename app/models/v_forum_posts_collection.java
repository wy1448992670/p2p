package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;
@Entity
public class v_forum_posts_collection extends Model{
	public Date add_time;
	public String title;
}
