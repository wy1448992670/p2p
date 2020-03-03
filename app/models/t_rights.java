package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 权限
* @author lwh
* @version 6.0
* @created 2014年4月4日 下午4:30:22
 */
@Entity
public class t_rights extends Model {
	public long type_id;
	public String name;
	public String code;
	public String description;
	
	@Override
	public String toString() {
		return "t_rights [type_id=" + type_id + ", name=" + name + ", code="
				+ code + ", description=" + description + ", id=" + id + "]\n";
	}
}
