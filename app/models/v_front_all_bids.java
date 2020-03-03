package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;

@Entity
public class v_front_all_bids extends Model{
	
	public Date time;
	//public String credit_name;
	//public String credit_image_filename;
	public String product_filename;
	public String product_name;
	public int show_type;
	public String title;
	public Double amount;
	public Integer period;
	public Integer  period_unit;
	public Integer status;
	public Double apr;
	public Boolean is_hot;
	public Boolean is_agency;
	public String agency_name;
	public Double has_invested_amount;
	public String bid_image_filename;
	public String small_image_filename;
	public Double loan_schedule;
	public Integer  bonus_type;
	public Double bonus;
	public Long user_id;
	public Double award_scale;
	public Date repayment_time;
	public String no;
	public Long credit_level_id;
	public int repayment_type_id;
	public String repay_name;
	public Boolean is_show_agency_name;
	public Integer product_id;

	//活动名称
	public String name1;
	public String name2;
	public String name3;

	//活动利率
	public Double rate1;
	public Double rate2;
	public Double rate3;
	
	public double min_invest_amount;
	public double average_invest_amount;
	//加息
	public Boolean isIncreaseRate;
	public Double increaseRate;
	public String increaseRateName;
	
	@Transient
	public t_credit_levels creditLevel;
	@Transient
	public int dayBid;
	
	public t_credit_levels getCreditLevel() {
		
		if (null == this.credit_level_id ) {
			
			return null;
		}
		
		return t_credit_levels.findById(this.credit_level_id);
	}

	public int getDayBid() {
		
		if(period_unit == 0){
			return 1;
		}
		return 0;
	}

	@Transient
	public String strStatus;
	
	public String getStrStatus() {
		if(null == this.strStatus){
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
				default : this.strStatus = "状态有误,谨慎操作!"; break;
			}
		}
		
		return this.strStatus;
	}
	
	
	
}
