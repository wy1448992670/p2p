package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 用户关注的债权转让标
* @author lwh
* @version 6.0
* @created 2014年5月15日 上午9:20:15
 */

@Entity
public class v_user_attention_invest_transfers extends Model {


	
	public Long user_id;
	public Long bid_id;
	public Long invest_transfer_id;
	public String bid_user_name;
	public String transfer_user_name;
	public String transfer_title;
	public String bid_title;
	public Double amount;
	public Double invest_amount;
	public Double apr;
	public Double debt_amount;
	public Double transfer_price;
	public Date end_time;
	public Integer join_times;
	public Integer status;
	public String debt_transfer_no;
	public Double max_offer_price;
	public String transfer_reason;
	public Boolean is_quality_debt;
	
	@Transient
	public String sign;//加密ID
	
	public String getSign() {
		return Security.addSign(this.user_id, Constants.USER_ID_SIGN);
	}

	public v_user_attention_invest_transfers() {
	}

}