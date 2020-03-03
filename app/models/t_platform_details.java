package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 平台交易记录
 * @author cp
 * @version 6.0
 * @created 2014年7月16日 上午11:20:25
 */
@Entity
public class t_platform_details extends Model {
	
	public Date time;
	public int operation;
	public long relation_id;
	public long from_pay;
	public long to_receive;
	public String from_pay_name;
	public String to_receive_name;
	public String payment;
	public double amount;
	public int type;
	public double balance;
	public String summary;
	
	
}
