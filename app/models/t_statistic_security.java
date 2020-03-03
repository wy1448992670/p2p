package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

@Entity
public class t_statistic_security extends Model {

	public int year;
	public int month;
	public int day;
	public double balance;
	public double pay;
	public int advance_acount;
	public double max_advance_amount;
	public double min_advance_amount;
	public double recharge_amount;
	public double income_amount;
	public double loan_amount;
	public double bad_debt_amount;
	public double bad_debt_income_rate;
	public double bad_debt_guarantee_rate;
	public double bad_loan_rate;
	
}
