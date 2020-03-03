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
public class t_user_events extends Model {

	public long user_id;
	
	public Date time;
	
	public String ip;
	
	public long type_id;
	
	public String descrption;

	public t_user_events(long user_id, Date time, String ip, long type_id,
			String descrption) {
		this.user_id = user_id;
		this.time = time;
		this.ip = ip;
		this.type_id = type_id;
		this.descrption = descrption;
	}

	public t_user_events() {
		// TODO Auto-generated constructor stub
	}
	
	

}
