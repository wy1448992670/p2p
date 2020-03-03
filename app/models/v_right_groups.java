package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 权限组
 * @author md005
 */
@Entity
public class v_right_groups extends Model {
	public String name;
	public String description;
	public String right_modules;
	public long supervisor_count;
}
