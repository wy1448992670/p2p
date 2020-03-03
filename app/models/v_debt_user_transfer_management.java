package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 债权转让管理
* @author lwh
* @version 6.0
* @created 2014年4月21日 下午4:11:46
 */

@Entity
public class v_debt_user_transfer_management extends Model {
	
	public Long invest_id;
	public Long user_id;
	public Long bid_id;
	public Double transfer_price;
	public Double transaction_price;
	public Integer type;
	public String title;//借款标题
	public String transfer_title;  //转让标题
	public Double amount;
	public Double apr;
	public Date end_time;
	public Integer join_times;
	public Integer join_person_num;
	public Integer status;
	public String name;//借款人
	public String bid_no;
	public Double receiving_amount;//本息合计应收
	public Double has_received_amount;//已收金额
	public Double remain_received_amount;//剩余应收收金额
	public Double remain_received_corpus ;//剩余应收本金
	public Double max_price ;//最高出价
	
	@Transient
	public String sign;

	public String getSign() {
		return Security.addSign(this.id, Constants.BID_ID_SIGN);
	}
	
	
	
	
	
}

