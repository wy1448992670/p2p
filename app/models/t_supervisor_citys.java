package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class t_supervisor_citys extends Model{
	public String province_id;
	public String city_id;
	public String province;
	public String city;
	public long supervisor_id;
	public Date create_time;
	public Date update_time;
	
	
	@Override
	public String toString() {
		return "t_supervisor_citys [province_id=" + province_id + ", city_id="
				+ city_id + ", province=" + province + ", city=" + city
				+ ", supervisor_id=" + supervisor_id + ", create_time="
				+ create_time + ", update_time=" + update_time + "]";
	}
	
}
