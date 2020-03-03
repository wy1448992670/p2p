package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
* @author lwh
* @version 6.0
* @created 2014年4月9日 下午8:30:30
 */
@Entity
public class v_debt_auction_records extends Model {

	public double debt_amount;
	
	public double offer_price;
	
	public String name;
	
	public int status;
	
	public Date time;
	
	public long transfer_id;
}