package bean;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import utils.ErrorInfo;
import business.CreditLevel;

/**
 * 我的账户->优质标列表
 * 
 * @author bsr
 * @version 6.0
 * @created 2015-1-11 上午11:10:17
 */
@Entity
public class QualityBid implements Serializable {
	@Id
	public Long id;
	public Long user_id;
	public String bid_image_filename;
	public String small_image_filename;
	public String title;
	public Double loan_schedule;
	public Double apr;
	public Double amount;
	public Integer period_unit;
	public Integer period;
	public Double has_invested_amount;
	public String repayment_type_name;
	
	@Transient
	public CreditLevel creditLevel;
	
	/**
	 * 信用积分
	 */
	public CreditLevel getCreditLevel() {
		return CreditLevel.queryUserCreditLevel(this.user_id, new ErrorInfo());
	}
}
