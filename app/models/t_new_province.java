package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

@Entity
public class t_new_province extends Model{
	public String province_id;
	public String province;
	
	@Transient
	public List<t_new_city> citys;
	
}
