package models;

import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 *账单提醒--最新的还款账单
 */
@Entity
public class v_bill_recently_pending extends Model{
	
	public long bid_id;
	public String bid_no;
	public String title;
	public long user_id;
	public int period;
	public String device_user_id;
	public String channel_id;
	public int device_type;
	public boolean is_bill_push;
	public String repay_time;
	
	@Transient
	public String sign;//加密ID
	
	public String getSign() {
		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
	}
}
