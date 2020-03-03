package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 后台—>推广员列表
 * @author cp
 * @version 6.0
 * @created 2014年5月22日 下午5:35:11
 */
@Entity
public class v_user_cps_user_count extends Model{

	public long cps_count;
	public long active_count;
	public long unactive_count;
	public double commission_amount;
}
