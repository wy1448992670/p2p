package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

@Entity
public class t_statistic_recharge extends Model {

	public int year;
	public int month;
	public int day;
	public double recharge_amount;
	public int recharge_count;
	public int recharge_menber;
	public int new_recharge_menber;
	public double average_recharge;
	public double average_each_recharge;
	public double max_recharge_amount;
	public double min_recharge_amount;
	
}
