package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 积分明细（成功投标）
 * @author cp
 * @version 6.0
 * @created 2014年5月14日 下午9:42:46
 */
@Entity
public class v_user_detail_credit_score_invest extends Model {
	
	public long user_id;
	public String bid_no;
	public String name;
	public String title;
	public double score;
	public Date invest_time;
}
