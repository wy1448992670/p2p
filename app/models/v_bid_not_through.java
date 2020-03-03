package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import constants.Constants;
import play.db.jpa.Model;
import utils.Security;
import utils.ServiceFee;

/**
 * 未通过的借款标列表
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-21 上午10:47:08
 */
@Entity
public class v_bid_not_through extends Model {
	public String bid_no;
	public String title;
	public Long user_id;
	public String user_name;
	public String credit_level_image_filename;
	public Integer product_id;
	public String small_image_filename;
	public Double apr;
	public Integer period;
	public Integer period_unit;
	public Date time;
	public Date audit_time;
	public Double amount;
	public Integer status;
	public Integer product_item_count;
	public String mark;
	public Integer user_item_count_true;
	public Integer repaymentId;
	public Integer user_item_submit;
    
	//添加该字段是因为微信里面需要用到id加密字符串
	@Transient
	public String sign;
	
	public String getSign() {
		return Security.addSign(this.id, Constants.BID_ID_SIGN);
	}
	
	@Transient
	public String strStatus;
	
	public String getStrStatus() {
		switch (this.status) {
			case Constants.BID_NOT_THROUGH: this.strStatus = "审核不通过"; break;
			case Constants.BID_PEVIEW_NOT_THROUGH: this.strStatus = "借款中不通过"; break;
			case Constants.BID_LOAN_NOT_THROUGH: this.strStatus = "放款不通过"; break;
			case Constants.BID_FLOW: this.strStatus = "流标"; break;
			case Constants.BID_REPEAL: this.strStatus = "撤销"; break;
			case Constants.BID_NOT_VERIFY: this.strStatus = "未验证"; break;
			default : this.strStatus = "状态有误,谨慎操作!"; break;
		}
	
		return this.strStatus;
	}
	
	@Transient
	public Double capital_interest_sum;
	
	public Double getCapital_interest_sum() {
		double rate = ServiceFee.interestCompute(this.amount, this.apr, this.period_unit, this.period, this.repaymentId);
		
		return this.amount + rate;
	}
}