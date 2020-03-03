package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 消息发送模板
* @author lwh
* @version 6.0
* @created 2014年4月4日 下午3:51:27
 */
@Entity
public class t_message_sms_templates extends Model {
	
	public Date time;
	public String scenarios;
	public String content;
	public double size;
	public boolean status;
	public String title;
}
