package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 权限组所拥有权限
* @author lwh
* @version 6.0
* @created 2014年4月4日 下午4:33:27
 */
@Entity
public class t_rights_of_group extends Model {
	public long group_id;
	public long right_id;
}
