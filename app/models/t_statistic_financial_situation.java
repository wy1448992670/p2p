package models;

import javax.persistence.Entity;
import play.db.jpa.Model;


/**
 * 理财情况统计分析表
 * @author lwh
 *
 */
@Entity
public class t_statistic_financial_situation extends Model{
	
	public int year ;
	public int month ;
	public double invest_accoumt ;
	public double invest_accoumt_pc ;
	public double invest_accoumt_app ;
	public double invest_accoumt_wechat ;
	public double increase_invest_account ;
	public double increase_invest_account_pc ;
	public double increase_invest_account_app ;
	public double increase_invest_account_wechat ;
	public long invest_user_account ;
	public long invest_user_account_pc ;
	public long invest_user_account_app ;
	public long invest_user_account_wechat ;
	public long increase_invest_user_account ;
	public long increase_invest_user_account_pc ;
	public long increase_invest_user_account_app ;
	public long increase_invest_user_account_wechat ;
	public double per_capita_invest_amount ;
	public double per_capita_invest_amount_pc ;
	public double per_capita_invest_amount_app ;
	public double per_capita_invest_amount_wechat ;
	public double per_bid_average_invest_amount ;
	public double per_capita_invest_debt ;
	public double per_capita_balance ;
	public double invest_user_conversion ;
}
