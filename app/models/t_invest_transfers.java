package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 债权转让
* @author lwh
* @version 6.0
* @created 2014年4月4日 下午3:32:05
 */
@Entity
public class t_invest_transfers extends Model {
	public long invest_id;
	public Date time;
	public String mer_bill_no;
	public String title;
	public String transer_reason;
	public double debt_amount;
	public double transfer_price;
	public int type;
	public long specified_user_id;
	public int period;
	public int status;
	public String no_through_reason;
	public Date failure_time;
	public boolean is_quality_debt;
	public long audit_supervisor_id;
	public Date start_time;
	public Date end_time;
	public int join_times;
	public long transaction_user_id;
	public Date transaction_time;
	public double transaction_price;
	public int version;
	public String qr_code;
	
	/* 2014-11-14 债权转让协议 */
	public String pact;
	
	public t_invest_transfers(){
		
	}
	
	public t_invest_transfers(long id,Date end_time){
		this.id = id;
		this.end_time = end_time;
	}
}
