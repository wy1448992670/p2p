package models;

// Generated 2014-4-4 11:59:10 by Hibernate Tools 3.4.0.CR1

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 管理员事件类型
 * @author cp
 * @version 6.0
 * @created 2014年4月4日 下午3:35:39
 */
@Entity
public class t_supervisor_event_types extends Model {

	public String name;
	public String code;
	public String description;
	public boolean is_use;

}
