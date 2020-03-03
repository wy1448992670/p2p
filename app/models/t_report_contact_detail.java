package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

/**
 *	运营商报告用户通讯详情
 */
@Entity
public class t_report_contact_detail extends Model {
	
	public Long user_info_id;
	public String number;
	public String group_name;
	public String company_name;
	@Transient
	public Date create_time;
	@Transient
	public Date update_time;

	public t_report_contact_detail() {

	}
	
	public t_report_contact_detail(long id, long user_info_id, String peer_num, String group_name, String company_name) {
		this.id = id;
		this.user_info_id = user_info_id;
		this.number = peer_num;
		this.group_name = group_name;
		this.company_name = company_name;
	}
}
