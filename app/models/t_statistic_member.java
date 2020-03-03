package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

@Entity
public class t_statistic_member extends Model {

	public int year;
	public int month;
	public int day;
	public int new_member;
	public int new_member_pc;
	public int new_member_app;
	public int new_member_wechat;
	public int new_recharge_member;
	public int new_recharge_member_pc;
	public int new_recharge_member_app;
	public int new_recharge_member_wechat;
	public double new_member_recharge_rate;
	public double new_member_recharge_rate_pc;
	public double new_member_recharge_rate_app;
	public double new_member_recharge_rate_wechat;
	public int new_vip_count;
	public int new_vip_count_pc;
	public int new_vip_count_app;
	public int new_vip_count_wechat;
	public int member_count;
	public int member_count_pc;
	public int member_count_app;
	public int member_count_wechat;
	public double member_activity;
	public double member_activity_pc;
	public double member_activity_app;
	public double member_activity_wechat;
	public int borrow_member_count;
	public int invest_member_count;
	public int composite_member;
	public int vip_count;
	public int vip_count_pc;
	public int vip_count_app;
	public int vip_count_wechat;
}
