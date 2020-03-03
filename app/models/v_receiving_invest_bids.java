package models;

import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;


@Entity
public class v_receiving_invest_bids extends Model{
	
	public Long user_id;
	public Long bid_id;
	public Double bid_amount;
	public String title;
	public String no;
	public Double apr;
	public Double increaseRate;
	public Double rate1;
	public Double rate2;
	public Double receiving_amount;//本息合计应收
	public Double has_received_amount;//已收金额
	public Long overdue_payback_period;
	public Long has_payback_period;
	public Integer period;
	public Integer period_unit;
	public boolean is_sec_bid;
	public String name;
	public Integer transfer_status;
	public Long transfers_id;
	public Double invest_amount;
	public boolean is_agency;
	
	@Transient
	public String sign;
	
	@Transient
	public String sign2;

	public String getSign() {
		return Security.addSign(this.id, Constants.BID_ID_SIGN);
	}
	
	public String getSign2() {
		return Security.addSign(this.bid_id, Constants.BID_ID_SIGN);
	}
	
	
}
