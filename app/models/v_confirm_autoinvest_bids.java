package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 符合自动投标条件的所有标的
* @author lwh
* @version 6.0
* @created 2014年4月17日 上午11:30:12
 */
@Entity
public class v_confirm_autoinvest_bids extends Model {
	
	public long user_id;
	public double amount;
	public int period;
	public double min_invest_amount;
	public double average_invest_amount;
	public double min_interest_rate;
	public double max_interest_rate;
	public int status;
	public double loan_schedule;
	public double has_invested_amount;
	public int loan_type;
	public Date audit_time;
	public double apr;
	public int credit_level_id;
	public int num;
	public int period_unit;
}
