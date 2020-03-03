package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 本金保障收支记录
 */
@Entity
public class v_platform_detail extends Model{
	
	public Date time;
	public int operation;
	public String name;
	public long relation_id;
	public String from_pay;
	public String to_receive;
	public String payment;
	public int type;
	public double amount;
	public double balance;
	public String summary;
}
