package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 已读的消息
* @author lwh
* @version 6.0
* @created 2014年4月4日 下午3:39:54
 */
@Entity
public class t_messages_accepted extends Model {
	
	public long user_id;
	public long supervisor_id;
	public Date time;
	public long message_id;
	public int is_erased;
	public Date delete_time;
}
