package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * cps推广--我的推广收入
 * @author cp
 * @version 6.0
 * @created 2014年6月13日 上午11:46:00
 */
@Entity
public class v_user_cps_income extends Model {

	public int award_year;
	public int award_month;
	public int recommend_year;
	public int recommend_month;
	public long cps_count;
	public long active_count;
	public long unactive_count;
	public double award_amount;
}
