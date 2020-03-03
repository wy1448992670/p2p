package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 权限类型
* @author lwh
* @version 6.0
* @created 2014年4月4日 下午4:39:00
 */
@Entity
public class t_right_types extends Model {
	
	public String name;
	public String code;
	public String description;
	public Boolean is_use;
	
	@Override
	public String toString() {
		return "t_right_types [name=" + name + ", code=" + code
				+ ", description=" + description + ", is_use=" + is_use
				+ ", id=" + id + "]\n";
	}
}
