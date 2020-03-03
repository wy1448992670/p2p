package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 积分明细（成功借款）
 * @author cp
 * @version 6.0
 * @created 2014年5月14日 下午9:42:46
 */
@Entity
public class v_user_detail_credit_score_loan extends Model {
	
	public long user_id;
	public String bid_no;
	public String title;
	public Date audit_time;
	public double score;
	
}
