package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 系统升级日志
 * @author Administrator
 *
 */
@Entity
public class t_system_upgrade_logs extends Model {
	
	public Date log_time;
	public String log_title;
	public String log_content;
	public String log_upgrade_packs;

	public t_system_upgrade_logs() {

	}

	/**
	 * 系统升级日志
	 * @param id
	 * @param log_title
	 */
	public t_system_upgrade_logs(long id, String log_title) {
		this.id = id;
		this.log_title = log_title;
	}
}
