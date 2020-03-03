package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 转让中债权转让标
* @author lwh
* @version 6.0
* @created 2014年4月21日 下午4:04:16
 */

@Entity
public class v_debt_transfering extends Model {

	public String name;
	public String credit_level_image_filename;
	public Long order_sort;
	public Long bid_id;
	public Long user_id;
	public Integer period;
	public Integer period_unit;
	public Double invest_amount;
	public Double bid_amount;
	public Double apr;
	public Integer type;
	public Double transfer_price;
	public Double debt_amount;
	public Date time;
	public Integer status;
	public String no;
	public Long has_payback_period;//已还账单
	public Long overdue_payback_period;//逾期账单
	public Double receiving_amount;//应收本息
	public Double has_received_amount;//已收本息
	public Double remain_received_corpus;//剩余应收本金
	public Date end_time;
	public Integer join_times;
	public Integer join_person_num;
	public String title;  //债权转让标题
	@Transient
	public String sign;

	public String getSign() {
		return Security.addSign(this.id,Constants.BID_ID_SIGN);
	}
}

