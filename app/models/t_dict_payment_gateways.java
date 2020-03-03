package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
* @author zhs
* @version 6.0
* @created 2014年4月4日 下午5:17:41
 */
@Entity
public class t_dict_payment_gateways extends Model {
	public String name;
	public String account;
	public String pid;
	public String _key;
	public boolean is_use;
	
	public t_dict_payment_gateways() {
		
	}
	
	public t_dict_payment_gateways(long id, boolean is_use) {
		this.id = id;
		this.is_use = is_use;
	}
}
