package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 等待发送的短信
 * @author cp
 * @version 6.0
 * @created 2014年4月4日 下午4:03:20
 */
@Entity
public class t_system_mobile_sms_sending extends Model {

	public Date time;
	public String mobile;
	public String body;
	public boolean is_sent;
	public int try_times;
	public Date sent_time;


}
