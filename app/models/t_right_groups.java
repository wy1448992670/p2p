package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 权限组
* @author lwh
* @version 6.0
* @created 2014年4月4日 下午4:23:26
 */
@Entity
public class t_right_groups extends Model {
	public String name;
	public String description;
	public String right_modules;
}
