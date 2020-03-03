package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 借款情况统计分析表
 * @author lzp
 * @version 6.0
 * @created 2014-7-11
 */
@Entity
public class t_statistic_borrow extends Model {
	public int year;
	public int month;
	public double total_borrow_amount;
	public double total_borrow_amount_pc;
	public double total_borrow_amount_app;
	public double total_borrow_amount_wechat;
	public double this_month_borrow_amount;
	public double this_month_borrow_amount_pc;
	public double this_month_borrow_amount_app;
	public double this_month_borrow_amount_wechat;
	public int total_borrow_user_num;
	public int total_borrow_user_num_pc;
	public int total_borrow_user_num_app;
	public int total_borrow_user_num_wechat;
	public int new_borrow_user_num;
	public int new_borrow_user_num_pc;
	public int new_borrow_user_num_app;
	public int new_borrow_user_num_wechat;
	public double finished_borrow_amount;
	public double finished_borrow_amount_pc;
	public double finished_borrow_amount_app;
	public double finished_borrow_amount_wechat;
	public double repaying_borrow_amount;
	public int released_bids_num;
	public int released_bids_num_pc;
	public int released_bids_num_app;
	public int released_bids_num_wechat;
	public double released_borrow_amount;
	public double average_annual_rate;
	public double average_annual_rate_pc;
	public double average_annual_rate_app;
	public double average_annual_rate_wechat;
	public double average_borrow_amount;
	public double average_borrow_amount_pc;
	public double average_borrow_amount_app;
	public double average_borrow_amount_wechat;
	public int overdue_bids_num;
	public double overdue_amount;
	public double overdue_per;
	public int bad_bids_num;
	public double bad_bill_amount;
	public double bad_bill_amount_per;
}
