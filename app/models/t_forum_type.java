package models;

import javax.persistence.Entity;

import play.db.jpa.Model;
@Entity
public class t_forum_type extends Model{
	
	public String name;
	public int status;
	public int rId;
	public String url;
}
