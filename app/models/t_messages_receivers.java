package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 消息收件人列表
 * @author lzp
 * @version 6.0
 * @created 2014年4月4日 下午3:39:54
 */
@Entity
public class t_messages_receivers extends Model {
	public long message_id;
	public long user_id;
	public long supervisor_id;
}
