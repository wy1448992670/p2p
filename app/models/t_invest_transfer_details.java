package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
* @author zhs
* @version 6.0
* @created 2014年4月4日 下午5:25:22
 */
@Entity
public class t_invest_transfer_details extends Model {
	public long transfer_id;
	public Date time;
	public long user_id;
	public double offer_price;
	public int status;
}
