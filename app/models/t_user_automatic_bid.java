package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;


/**
 * 
* @author lwh
* @version 6.0
* @created 2014年4月16日 下午5:02:32
 */
@Entity
public class t_user_automatic_bid extends Model {
	public long user_id;
	public Date time;
	public long bid_id;
}
