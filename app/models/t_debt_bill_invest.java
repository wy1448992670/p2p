package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;
@Entity
public class t_debt_bill_invest extends Model{
	public long user_id;
	public long debt_invest_id;
	public long debt_id;
	public int periods;
	public String title;
	public Date receive_time;
	public double receive_corpus;
	public double receive_interest;
	//public double receive_increase_interest;//加息利息
	public double overdue_fine;//逾期罚款
	public int status;
	public Date real_receive_time;
	public double real_receive_corpus;
	public double real_receive_interest;
	//public double real_increase_interest;
	public boolean is_all_receiver;//是否全部属于受让人
	public long old_bill_id; //原账单ID
}
