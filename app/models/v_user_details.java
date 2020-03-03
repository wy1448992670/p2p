package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 交易记录
 * @author cp
 * @version 6.0
 * @created 2014年5月20日 下午7:54:31
 */
@Entity
public class v_user_details extends Model {
	public long user_id;
	public Date time;
	public long operation;
	public double amount;
	public double user_balance;
	public double balance;
	public double freeze;
	public double recieve_amount;
	public String summary;
	public String name;
	public int type;
}
