package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/***
 * 
 * <Description functions in a word> 后台运营数据-Echarts数据 <Detail description>
 * 
 * @author ChenZhipeng
 * @version [Version NO, 2015年8月18日]
 * @see [Related classes/methods]
 * @since [product/module version]
 */
@Entity
public class t_statistic_index_echart_data extends Model {
	public Date time;
	public int new_financial_members_count;
	public int new_register_members_count;
	public double invest_money;
	public double recharge_money;
	public int type;
	public int time_type;

	public t_statistic_index_echart_data() {

	}

	public t_statistic_index_echart_data(int new_financial_members_count) {
		this.new_financial_members_count = new_financial_members_count;
	}

	public t_statistic_index_echart_data(long id,Date time, int new_financial_members_count,
			int new_register_members_count, double invest_money,
			double recharge_money, int type, int time_type) {
		this.id = id;
		this.time = time;
		this.new_financial_members_count = new_financial_members_count;
		this.new_register_members_count = new_register_members_count;
		this.invest_money = invest_money;
		this.recharge_money = recharge_money;
		this.type = type;
		this.time_type = time_type;
	}
	
	
}
