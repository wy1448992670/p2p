package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class t_new_area extends Model{
	public String area_id;
	public String area;
	public String father;
}
