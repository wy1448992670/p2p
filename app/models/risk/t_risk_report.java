package models.risk;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

@Entity
public class t_risk_report extends Model {
	private static final long serialVersionUID = 1L;

	public Long user_id;
	public Integer status;
	public Integer is_valid;
	public String report_response;
	public String h5_response_task_id;
	@Transient
	public Date create_time;
	@Transient
	public Date update_time;
	@Transient
	public String reportUrl;
	@Override
	public String toString() {
		return "t_risk_report [user_id=" + user_id + ", status=" + status + ", is_valid=" + is_valid + ", report_response="
				+ report_response + ", h5_response_task_id=" + h5_response_task_id + ", create_time=" + create_time
				+ ", update_time=" + update_time + ", reportUrl=" + reportUrl + "]";
	}



}