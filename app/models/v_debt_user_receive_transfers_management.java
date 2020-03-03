package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 受让债权管理
* @author lwh
* @version 6.0
* @created 2014年4月21日 下午4:14:52
 */

@Entity
public class v_debt_user_receive_transfers_management extends Model {


	public Double transfer_price;
	public Double transaction_price;
	public Long user_id;
	public Long transer_id;
	public Double amount;
	public Double apr;
	public Date transaction_time;
	public String name;//借款者
	public String bid_no;
	public String title;
	public String transfer_title;  //转让标题
	public Integer status;//债权竞拍状态
	public Double max_price;
	public Integer type;
	public Double receiving_amount;//本息合计应收
	public Double has_received_amount;//已收金额
	public Double remain_received_amount;//剩余应收收金额
	public Double remain_received_corpus ;//剩余应收本金
	
	@Transient
	public Double receiving_amount_success;//已成交之后的新数据
	@Transient
	public Double has_received_amount_success;//已收金额
	@Transient
	public Double remain_received_amount_success;//剩余应收收金额
	@Transient
	public Double remain_received_corpus_success ;//剩余应收本金
	
	
	
	
	
	public Double getReceiving_amount_success() {
		
		Double temp = 0.0;
		Long investId = 0l;
		try {
			investId = t_invests.find("select id from t_invests where transfers_id = ? ", this.transer_id).first();
		} catch (Exception e) {
			e.printStackTrace();
			investId = 0l;
		}
		
		if(investId != null && investId > 0){
			try {
				temp = t_bill_invests.find(" select sum(receive_corpus + receive_interest + overdue_fine) from t_bill_invests where invest_id = ? and status in (-1,-2,-5,-6)", investId).first();
			} catch (Exception e) {
				e.printStackTrace();
				temp = 0.0;
			}
			
		}
		 
		if(temp == null){
			temp = 0.0;
		}
		
		return temp;
	}

	public Double getHas_received_amount_success() {
		
		
		Double temp = 0.0;
		Long investId = 0l;
		try {
			investId = t_invests.find("select id from t_invests where transfers_id = ? ", this.transer_id).first();
		} catch (Exception e) {
			e.printStackTrace();
			investId = 0l;
		}
		
		if(investId != null && investId > 0){
			try {
				temp = t_bill_invests.find(" select sum(receive_corpus + receive_interest + overdue_fine) from t_bill_invests where invest_id = ? and status in (-3,-4,0)", investId).first();
			} catch (Exception e) {
				e.printStackTrace();
				temp = 0.0;
			}
			
		}
		 
		if(temp == null){
			temp = 0.0;
		}
		
		return temp;
	}

	public Double getRemain_received_amount_success() {
		Double temp = 0.0;
		Long investId = 0l;
		try {
			investId = t_invests.find("select id from t_invests where transfers_id = ? ", this.transer_id).first();
		} catch (Exception e) {
			e.printStackTrace();
			investId = 0l;
		}
		
		if(investId != null && investId > 0){
			try {
				temp = t_bill_invests.find(" select sum(receive_corpus + receive_interest + overdue_fine) from t_bill_invests where invest_id = ? and status in (-1,-2,-5,-6)", investId).first();
			} catch (Exception e) {
				e.printStackTrace();
				temp = 0.0;
			}
			
		}
		 
		if(temp == null){
			temp = 0.0;
		}
		
		return temp;
	}

	public Double getRemain_received_corpus_success() {
		Double temp = 0.0;
		Long investId = 0l;
		try {
			investId = t_invests.find("select id from t_invests where transfers_id = ? ", this.transer_id).first();
		} catch (Exception e) {
			e.printStackTrace();
			investId = 0l;
		}
		
		if(investId != null && investId > 0){
			try {
				temp = t_bill_invests.find(" select sum(receive_corpus ) from t_bill_invests where invest_id = ? and status in (-1,-2,-5,-6)", investId).first();
			} catch (Exception e) {
				e.printStackTrace();
				temp = 0.0;
			}
			
		}
		 
		if(temp == null){
			temp = 0.0;
		}
		
		return temp;
	}

	@Transient
	public String signId;
	
	
	public String getSignId() {
		return Security.addSign(this.id, Constants.BID_ID_SIGN);
	}

	@Transient
	public String sign;

	public String getSign() {
		return Security.addSign(this.transer_id, Constants.BID_ID_SIGN);
	}
	
}

