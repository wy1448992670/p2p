package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 管理员所属权限
* @author lwh
* @version 6.0
* @created 2014年4月4日 下午4:26:24
 */
@Entity
public class t_right_groups_of_supervisor extends Model {
	
	public long supervisor_id;
	public long group_id;
}
