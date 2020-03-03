package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class t_new_city extends Model {
	public String city_id;
	public String city;
	public String father;
}
