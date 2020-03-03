package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
 * @author lzp
 * @version 6.0
 * @created 2014-4-4 下午3:41:24
 */

@Entity
public class t_user_detail_types extends Model {

	public String name;
	
	public String code;
	
	public String description;	

}
