package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
* @author zhs
* @version 6.0
* @created 2014年4月4日 下午5:24:44
 */
@Entity
public class t_invest_ensure_plans extends Model {
	public Date time;
	public String title;
	public double amount;
	public Date start_time;
	public Date end_time;
	public double compensate_amount;
}
