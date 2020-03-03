package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import business.User;
import play.db.jpa.Model;
import utils.ErrorInfo;

/**
 * 
 * @author lzp
 * @version 6.0
 * @created 2014-4-4 下午3:41:24
 */

@Entity
public class t_user_recharge_details extends Model {

	public long user_id;
	
	public Date time;
	
	public int payment_gateway_id;

	public String unique_key;
	
	public String pay_number;
	
	public double amount;
	
	public boolean is_completed;
	
	public Date completed_time;
	
	public String order_no;
	
	public Integer recharge_for_type;//后台自动充值原因:1.bill_id 
	
	public Long recharge_for_id;//recharge_for_type:1 bill.id

	public int type;
	
	public int client; 
	
	public String bank_card_no;
	
	@Transient
	public String name;
	
	public String getName() {
		return User.queryUserNameById(this.user_id, new ErrorInfo());
	}
}
