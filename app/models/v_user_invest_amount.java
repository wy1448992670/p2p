package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 账户总揽--财富统计
 * @author cp
 * @version 6.0
 * @created 2014年5月21日 下午4:53:45
 */
@Entity
public class v_user_invest_amount extends Model{
	
	public long bid_count;
	public long invest_count;
	public long transfer_count;
	public double invest_amount;
	public double invest_interest;
}
