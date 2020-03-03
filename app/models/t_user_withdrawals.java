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
public class t_user_withdrawals extends Model {

	public long user_id;
	
	public Date time;
	
	public double amount;
	
	public long bank_account_id;
	
	public int status;
	
	public int type;
	
	public long audit_supervisor_id;
	
	public Date audit_time;
	
	public long pay_supervisor_id;
	
	public Date pay_time;
	
	public String disagree_reason;
	
	/**
	 *service_fee：提现手续费
	 */
	public double service_fee;
	
	public boolean is_auto_create;

}
