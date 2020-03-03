package bean;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import constants.Constants;

/**
 * 合作机构标列表
 * @author mingjian
 * @version 6.0
 * @created 2015-1-9 晚上20:41:00
 *
 */
@Entity
public class AgencyBid implements Serializable{
	@Id
	public Long id;
	public String bid_no;
	public String title;
	public Long user_id;
	public String user_name;
	public Double amount;
	public String small_image_filename;
	public Double apr;
	public Integer period;
	public Integer period_unit;
	public Integer status;
	public Date time;
	public String agency_name;
	public Double loan_schedule;
	public Integer product_item_count;
	public Integer user_item_count_true;
	public Long order_sort;
	public String credit_level_image_filename;
	
	
	@Transient
	public String strStatus;
	
	public String getStrStatus() {
		switch (this.status) {
			case Constants.BID_AUDIT: this.strStatus = "审核中"; break;
			case Constants.BID_ADVANCE_LOAN: this.strStatus = "提前借款"; break;
			case Constants.BID_FUNDRAISE: this.strStatus = "借款中"; break;
			case Constants.BID_EAIT_LOAN: this.strStatus = "待放款"; break;
			case Constants.BID_REPAYMENT: this.strStatus = "还款中"; break;
			case Constants.BID_COMPENSATE_REPAYMENT: this.strStatus = "本金垫付还款中"; break;
			case Constants.BID_REPAYMENTS: this.strStatus = "已还款"; break;
			case Constants.BID_NOT_THROUGH: this.strStatus = "审核不通过"; break;
			case Constants.BID_PEVIEW_NOT_THROUGH: this.strStatus = "借款中不通过"; break;
			case Constants.BID_LOAN_NOT_THROUGH: this.strStatus = "放款不通过"; break;
			case Constants.BID_FLOW: this.strStatus = "流标"; break;
			case Constants.BID_REPEAL: this.strStatus = "撤销"; break;
			case Constants.BID_NOT_VERIFY: this.strStatus = "未验证"; break;
			case Constants.BID_AUDIT_VERIFY:
			case Constants.BID_ADVANCE_LOAN_VERIFY:
			case Constants.BID_FUNDRAISE_VERIFY: this.strStatus = "待验证"; break;
			default : this.strStatus = "状态有误,谨慎操作!"; break;
		}
	
		return this.strStatus;
	}
}
