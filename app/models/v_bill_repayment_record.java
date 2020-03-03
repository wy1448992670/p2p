package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 借款账单历史还款情况
 * @author Administrator
 *
 */
@Entity
public class v_bill_repayment_record extends Model {

	public long bid_id;
	public double current_pay_amount;
	public String title;
	public int overdue_mark;
	public int status;
	public Date real_repayment_time;
	public Date repayment_time;
	
	public v_bill_repayment_record(){
		
	}
	
	public v_bill_repayment_record( double current_pay_amount, String title, int overdue_mark
			, int status, Date real_repayment_time, Date repayment_time){
		this.title = title;
		this.current_pay_amount = current_pay_amount;
		this.overdue_mark = overdue_mark;
		this.status = status;
	    this.real_repayment_time = real_repayment_time;
		this.repayment_time = repayment_time;
		
	}
}
