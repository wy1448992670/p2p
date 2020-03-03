package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
 * @author cp
 * @version 6.0
 * @created 2014年12月22日 下午3:24:08
 */
@Entity
public class t_ips_sequences extends Model {
	public Date time;
	public long p_mer_bill_no;
	public boolean status;
}
