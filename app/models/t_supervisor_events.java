package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 管理员事件
 * @author cp
 * @version 6.0
 * @created 2014年4月4日 下午3:26:03
 */
@Entity
public class t_supervisor_events extends Model{

	public long supervisor_id;
	public Date time;
	public String ip;
	public long type_id;
	public String descrption;

}
