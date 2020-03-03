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
public class t_user_transfer_records extends Model {

	public Date time;
	
	public long transfer_user_id;

	public long transfer_bank_id;
	
	public double amount;
	
	public long receive_user_id;
	
	public long receive_bank_id;

}
