package models;

import javax.persistence.Entity;
import play.db.jpa.Model;


/**
 * 平台提现统计表
 * @author lwh
 *
 */
@Entity
public class t_statistic_withdraw extends Model{
	public int year;
	public int month;
	public int day;
	public long payment_number;
	public double payment_sum;
	public long apply_withdraw_account;
	public double apply_withdraw_sum;
	public double average_withdraw_amount;
	public double max_withdraw_amount;
	public double min_withdraw_amount;
	
}
