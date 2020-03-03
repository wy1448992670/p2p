package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
* @author zhs
* @version 6.0
* @created 2014年4月4日 下午5:27:06
 */
@Entity
public class t_invests extends Model {
	public long user_id;
	public Date time;
	public String mer_bill_no;
	public String ips_bill_no;
	public long bid_id;
	public double amount;
	public double fee;//理财管理费
	public int transfer_status;
	public long transfers_id;
	public Date transfers_time;//债权转让计息时间
	public boolean is_automatic_invest;
	public double correct_amount;
	public double correct_interest;
	public double correct_increase_interest;
	
	public String pact;
	public String intermediary_agreement;
	public String guarantee_invest;

	public Long increase_activity_id1;
	public Long increase_activity_id2;
	public Double correct_increase_interest1;
	public Double correct_increase_interest2;

	public int client;//客户端  1 pc  2 app  3 wechat 4 other
	
	public double red_amount;
	/**投资奖励**/
	public double award;
	
	/**借款管理费分摊到每次投资的费用**/
	public double bid_fee;
	
	public t_invests(long user_id,long bid_id){
		this.bid_id = bid_id;
		this.user_id = user_id;
	}
	
	public t_invests() {
		
	}

}
