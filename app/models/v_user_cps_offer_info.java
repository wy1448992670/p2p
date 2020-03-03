package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 后台—>佣金发放统计表
 * @author cp
 * @version 6.0
 * @created 2014年5月23日 上午11:20:47
 */
@Entity
public class v_user_cps_offer_info extends Model {
	
	public int year;
	public int month;
	public long cps_count; //cps会员数
	public long recommend_count; //推广注册会员数
	public long recharge_count; //推广充值会员数
	public double bid_amount;
	public double manage_fee;
	public double invest_amount;
	public double should_pay_commission_amount;
	public double has_payed_commission_amount;
}
