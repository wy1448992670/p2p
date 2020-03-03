package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;

/**
 * 标(优质标、机构合作、借款中)
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-5-13 下午09:26:09
 */
@Entity
public class v_bids extends Model {
	public String bid_no;
	public Long user_id;
	public String user_name;
	public Long product_id;
	public String product_name;
	public Long credit_id;
	public String bid_image_filename;
	public String small_image_filename;
	public String title;
	public Double loan_schedule;
	public Double apr;
	public Double amount;
	public Integer period_unit;
	public Integer period;
	public Double has_invested_amount;
	public Boolean is_quality;
	public Boolean is_hot;
	public Integer status;
	public Boolean is_agency;
	public Long agency_id;
	public Boolean is_show_agency_name;
	public Integer bonus_type;
	public Double bonus;
	public Double award_scale;
	public String agency_name;
	public Date time;
	public Long repayment_type_id;
	public String repayment_type_name;
	public Integer product_item_count;
	public String mark;
	public String credit_level_image_filename;
	public Long order_sort;
	public Integer overdue_count;
	public Integer user_item_count_true; 
	public Integer user_item_count_false;
	
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
			case Constants.BID_ADVANCE_LOAN_VERIFY: this.strStatus = "待验证";break;
			case Constants.BID_FUNDRAISE_VERIFY: this.strStatus = "待验证";break;
			default : this.strStatus = "状态有误,谨慎操作!"; break;
		}
	
		return this.strStatus;
	}

//	@Transient
//	public CreditLevel creditLevel;
//	
//	/**
//	 * 信用积分
//	 */
//	public CreditLevel getCreditLevel() {
//		return CreditLevel.queryUserCreditLevel(this.user_id, new ErrorInfo());
//	}
	
	/*@Transient
	public Object user_item_count_true; // 用户通过资料数
	@Transient
	public Object user_item_count_false; // 用户未通过资料数
	
	public Object getUser_item_count_true() {
		String hql = "SELECT count(uai2.id) AS user_item_count_true FROM("
				+ "select uai.id,  uai.audit_item_id from t_user_audit_items uai where status = ? GROUP BY uai.audit_item_id) uai2 "
				+ "where uai2.audit_item_id IN( "
				+ "SELECT  pail.audit_item_id  FROM t_product_audit_items_log pail WHERE pail.mark = ?)";

		Query query = JPA.em().createNativeQuery(hql);
		query.setParameter(1, Constants.AUDITED);
		query.setParameter(2, this.mark);
		
		try {
			return query.getResultList().get(0);
		} catch (Exception e) {
			
			return 0;
		}
	}
	
	public Object getUser_item_count_false() {
		String hql = "SELECT count(uai2.id) AS user_item_count_true FROM("
				+ "select uai.id,  uai.audit_item_id from t_user_audit_items uai where status = ? GROUP BY uai.audit_item_id) uai2 "
				+ "where uai2.audit_item_id IN( "
				+ "SELECT  pail.audit_item_id  FROM t_product_audit_items_log pail WHERE pail.mark = ?)";

		Query query = JPA.em().createNativeQuery(hql);
		query.setParameter(1, Constants.NOT_PASS);
		query.setParameter(2, this.mark);
		
		try {
			return query.getResultList().get(0);
		} catch (Exception e) {
			return 0;
		}
	}*/
}
