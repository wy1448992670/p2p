package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
* @author zhs
* @version 6.0
* @created 2014年4月4日 下午5:20:24
 */
@Entity
public class t_invest_ensure_compensates extends Model {
	public long ensure_plan_id;
	public Date time;
	public long invest_id;
	public double amount;
	public long supervisor_id;
}
