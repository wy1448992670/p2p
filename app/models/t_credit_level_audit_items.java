package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 信用等级的审计项目
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午04:17:01
 */
@Entity
public class t_credit_level_audit_items extends Model {
	public long audit_item_id;
	public long credit_level_id;
}
