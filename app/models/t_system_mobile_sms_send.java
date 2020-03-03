package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 已发送短信表
 * @author cp
 * @version 6.0
 * @created 2014年8月21日 下午8:24:40
 */
@Entity
public class t_system_mobile_sms_send extends Model {

	public Date time;
	public String mobile;
	public String body;
}
