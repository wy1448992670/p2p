package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 权限与action关联表
 * @author lzp
 * @version 6.0
 * @created 2014-6-5
 */
@Entity
public class t_right_actions extends Model {
	public int right_id;
	public String action;
	public String description;
}
