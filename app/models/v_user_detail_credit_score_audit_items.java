package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 *  信用积分明细(审核资料)
 * @author cp
 * @version 6.0
 * @created 2014年5月14日 下午9:42:46
 */
@Entity
public class v_user_detail_credit_score_audit_items extends Model {
	
	public long user_id;
	public Date audit_time;
	public String audit_item_name;
	public double score;
	public String suggestion;
}
