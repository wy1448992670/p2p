package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import business.Bid;
import play.db.jpa.Model;
import utils.Security;
import utils.ServiceFee;
import constants.Constants;

/**
 * 审核中的借款标列表
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-21 上午10:47:08
 */
@Entity
public class v_bid_auditing extends Model {
	public String bid_no;
	public String title;
	public Long user_id;
	public String user_name;
	public Integer product_id;
	public String small_image_filename;
	public Double apr;
	public Integer period;
	public Integer period_unit;
	public Date time;
	public Double amount;
	public Integer status;
	@Transient
	public Double capital_interest_sum;
	
	@Transient
	public long product_item_count = 0;
	public long getProduct_item_count(){
		if(StringUtils.isBlank(this.mark)){
			
			return 0;
		}
		
		if(this.product_item_count > 0){
			
			return this.product_item_count;
		}
		
		this.product_item_count = Bid.queryProductItemCount(this.mark, true);
		return this.product_item_count;
	}
	
	public String mark;
	public String product_name;
	public Long order_sort;
	public String credit_level_image_filename;
	
	@Transient
	public long user_item_count_true = 0; 
	public long getUser_item_count_true(){
		if(StringUtils.isBlank(this.mark)){
			
			return 0;
		}
		
		if(this.user_item_count_true > 0){
			
			return this.user_item_count_true;
		}
		
		this.user_item_count_true = Bid.queryUserItemCount(this.mark, true, this.user_id, Constants.AUDITED);
		return this.user_item_count_true;
	}
	
	@Transient
	public long user_item_count_false = 0;
	public long getUser_item_count_false(){
		if(StringUtils.isBlank(this.mark)){
			
			return 0;
		}
		
		if(this.user_item_count_false > 0){
			
			return this.user_item_count_false;
		}
		
		this.user_item_count_false = Bid.queryUserItemCount(this.mark, true, this.user_id, Constants.NOT_PASS);
		return this.user_item_count_false;
	}
	public Integer repaymentId;
	
	@Transient
	public long user_item_submit = 0;
	public long getUser_item_submit(){
		if(StringUtils.isBlank(this.mark)){
			
			return 0;
		}
		if(this.user_item_submit > 0){
			
			return this.user_item_submit;
		}
		this.user_item_submit = Bid.queryUserSubmitItemCountAll(this.mark, this.user_id);
		return this.user_item_submit;
	}
	
	
	@Transient
	public String sign;
	
	public String getSign(){
		return Security.addSign(this.id, Constants.BID_ID_SIGN);
	}
	
	public Double getCapital_interest_sum() {
		double rate = ServiceFee.interestCompute(this.amount, this.apr, this.period_unit, this.period, this.repaymentId);
		
		return this.amount + rate;
	}
	
	//@Transient
	//public Object user_item_count_true; // 用户通过资料数
	//@Transient
	//public Object user_item_count_false; // 用户未通过资料数
	
	/*public Object getUser_item_count_true() {
		String hql = "SELECT count(uai2.id) AS user_item_count_true FROM("
				+ "select uai.id,  uai.audit_item_id from t_user_audit_items uai where status = ? and user_id = ? GROUP BY uai.audit_item_id) uai2 "
				+ "where uai2.audit_item_id IN( "
				+ "SELECT  pail.audit_item_id  FROM t_product_audit_items_log pail WHERE pail.mark = ? and type = 1)";

		Query query = JPA.em().createNativeQuery(hql);
		query.setParameter(1, Constants.AUDITED);
		query.setParameter(2, this.user_id);
		query.setParameter(3, this.mark);
		
		try {
			return query.getResultList().get(0);
		} catch (Exception e) {
			
			return 0;
		}
	}
	
	public Object getUser_item_count_false() {
		String hql = "SELECT count(uai2.id) AS user_item_count_true FROM("
				+ "select uai.id,  uai.audit_item_id from t_user_audit_items uai where status = ? and user_id = ? GROUP BY uai.audit_item_id) uai2 "
				+ "where uai2.audit_item_id IN( "
				+ "SELECT  pail.audit_item_id  FROM t_product_audit_items_log pail WHERE pail.mark = ? and type = 1)";

		Query query = JPA.em().createNativeQuery(hql);
		query.setParameter(1, Constants.NOT_PASS);
		query.setParameter(2, this.user_id);
		query.setParameter(3, this.mark);
		
		try {
			return query.getResultList().get(0);
		} catch (Exception e) {
			return 0;
		}
	}*/
}		