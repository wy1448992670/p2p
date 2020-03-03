package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 用于防止用户重复提款
 * @author cp
 * @version 6.0
 * @created 2014年4月4日 下午4:08:37
 */
@Entity
public class t_system_recharge_completed_sequences extends Model {

	public String pay_number;
	public Date time;
	public double amount;

}
