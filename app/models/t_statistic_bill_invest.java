package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 理财情况统计表
 * @author mingjian
 * @version 6.0
 * @created 2014-12-26
 */
@Entity
public class t_statistic_bill_invest extends Model {
	public long invest_id;
    public long user_id;
    public int year;
    public int month;
    public int invest_count;
    public double average_loan_amount;
    public int average_invest_period;
    public double average_invest_amount;
    public int invest_fee_back; 
    public double invest_amount;
}
