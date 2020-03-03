package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 为t_user_details准备字段
 * @author cp
 * @version 6.0
 * @created 2014年6月15日 下午5:11:48
 */
@Entity
public class v_user_for_details extends Model {
	
	public double user_amount;
	public double user_amount2;
	public double freeze;
	public double credit_line;
	public double receive_amount;
}
