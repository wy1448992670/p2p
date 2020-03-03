package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;
@Entity
public class t_debt_invest extends Model{
	public long user_id;
	public long old_user_id;
	public long debt_id;
	public long invest_id;
	public long bid_id;
	public double amount;
	public double red_amount;
	public Date time;
	public double correct_amount;
	public double correct_interest;
}
