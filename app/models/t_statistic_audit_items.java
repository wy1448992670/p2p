package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 审核科目库查询分析表
 * @author lzp
 * @version 6.0
 * @created 2014-7-11
 */
@Entity
public class t_statistic_audit_items extends Model {
	public int audit_item_id;
	public int year;
	public int month;
	public String no;
	public String name;
	public int credit_score;
	public double audit_fee;
	public int borrow_user_num;
	public int submit_user_num;
	public double submit_per;
	public int audit_pass_num;
	public double pass_per;
	public int relate_product_num;
	public int relate_overdue_bid_num;
	public int relate_bad_bid_num;
	public int risk_control_ranking;
}
