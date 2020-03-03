package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

/**
 * 运营商报告用户信息
 */
@Entity
public class t_report_user_info extends Model {
	
	public long user_id;
	public String id_card;
	public String user_name;
	public String native_place;
	public String mobile;
	public String carrier_name;
	public Date reg_time;
	@Transient
	public Date create_time;
	@Transient
	public Date update_time;
}
