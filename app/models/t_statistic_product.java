package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 借款标产品销售情况分析表
 * @author lzp
 * @version 6.0
 * @created 2014-7-11
 */
@Entity
public class t_statistic_product extends Model {
	public int product_id;
	public int year;
	public int month;
	public String name;
	public int released_bids_num;
	public double released_amount;
	public double average_bid_amount;
	public double per;
	public int overdue_num;
	public double overdue_per;
	public int bad_bids_num;
	public double bad_bids_per;
	public double bids_num;
	public int invest_user_num;
	public double average_annual_rate;
	public int success_bids_num;
	public double success_bids_amount;
	public double manage_fee_amount;
}
