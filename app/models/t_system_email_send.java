package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 已发送邮件表
 * @author cp
 * @version 6.0
 * @created 2014年8月21日 下午8:23:43
 */
@Entity
public class t_system_email_send extends Model {

	public Date time;
	public String email;
	public String title;
	public String body;
}
