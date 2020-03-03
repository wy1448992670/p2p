package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;
@Entity
public class t_debt_bills extends Model{
	public long debt_id;
	public long bill_id;
	public String title;
	public Date repayment_time;
	
	public double repayment_corpus;
	public double repayment_interest;
	public String repayment_debt_bill_no;
	
	public Date real_repayment_time;
	public double real_repayment_corpus;
	public double real_repayment_interest;
	
	public double repayment_increase_interest;//加息利息
}
