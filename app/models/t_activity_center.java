package models;

import javax.persistence.Entity;

import play.db.jpa.Model;
@Entity
public class t_activity_center extends Model{
	public String title;
	public String location;
	public String resolution;
	public String file_size;
	public String file_format;
	public int _order;
	public String url;
	public String first_image_url;
	public String info_image_url;
	public String rule_image_url;
	
}
