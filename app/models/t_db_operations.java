package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 数据库操作记录
 * @author lzp
 * @version 6.0
 * @created 2014-7-22
 */
@Entity
public class t_db_operations extends Model {
	public long supervisor_id;
	public Date time;
	public String ip;
	public int type;
	public String filename;
}
