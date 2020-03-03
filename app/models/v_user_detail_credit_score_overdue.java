package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 积分明细（逾期扣分）
 * @author cp
 * @version 6.0
 * @created 2014年6月20日 上午11:23:48
 */
@Entity
public class v_user_detail_credit_score_overdue extends Model {

	public long user_id;
	public String bid_no;
    public String title;
    public String period;
    public Double score;
    public Date audit_time;
}
