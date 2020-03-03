package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class t_invest_user extends Model{
	public String name;
	public String reality_name;
	public String mobile;
	public double amount;
	public String recommend_user_mobile;
	public String recommend_user_name;
	public Date invest_time;
	public String period;
	public String period_unit;
	public String title;
	public String is_only_new_user;//是否新手标
	public String repayment_type_name;
	public int repayment_type_id;
	public String red_amount;
	public double correct_interest;//加息奖励
	public String is_valid;//是否有效投资
	public double income_dt;//预计收益
	public double apr;
	public double award;
	public double bonus;
	public double increase_rate;//加息利息
	public int is_increase_rate;//是否加息
}
