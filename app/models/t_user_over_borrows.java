package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
 * @author lzp
 * @version 6.0
 * @created 2014-4-4 下午3:41:24
 */

@Entity
public class t_user_over_borrows extends Model {

	public long user_id;
	public Date time;
	public double amount;
	public String reason;
	public double credit_line;
	public int status;
	public double pass_amount;
	public long audit_supervisor_id;
	public Date audit_time;
	public String audit_opinion;
	
	public t_user_over_borrows() {
		
	}
	
	public t_user_over_borrows(long id, double amount, String reason, Date time, int status) {
		this.id = id;
		this.amount = amount;
		this.reason = reason;
		this.time = time;
		this.status = status;
	}
}
