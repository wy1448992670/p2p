package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 发送的消息
* @author lwh
* @version 6.0
* @created 2014年4月4日 下午4:46:11
 */
@Entity
public class t_messages extends Model {
	public long sender_user_id;
	public long sender_supervisor_id;
	
	public Date time;
	
	public long receiver_user_id;
	public long receiver_supervisor_id;
	
	public String title;
	public String content;
	
	public boolean is_reply;
	public long message_id;//被回复的消息id
	
	public int is_erased;
	public Date delete_time;
}
