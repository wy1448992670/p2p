package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 系统参数
 * 
 * @author cp
 * @version 6.0
 * @created 2014年4月4日 下午4:06:00
 */
@Entity
public class t_system_options extends Model {

	public String _key;
	public String _value;
	public String description;
}
