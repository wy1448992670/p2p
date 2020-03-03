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
public class t_user_automatic_invest_options extends Model {
	
	public long user_id;
	public Date time;
	public boolean status;
	public double amount;
	public int valid_type;
	public int valid_date;
	public double min_amount;
	public double max_amount;
	public double retention_amout;
	public double min_interest_rate;
	public double max_interest_rate;
	public int min_period;
	public int max_period;
	public int min_credit_level_id;
	public int max_credit_level_id;
	public String loan_type;
	public Date wait_time;  //自动投标排队等待时间
	
}
