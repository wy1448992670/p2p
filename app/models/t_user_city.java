package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class t_user_city extends Model{
	public String province_id;
	public String province;
	public String city_id;
	public String city;
	public long user_id;
	public String address;
}
