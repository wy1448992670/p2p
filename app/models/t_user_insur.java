package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 
 * @description.  银行卡投保记录
 *  
 * @modificationHistory.  
 * @author liulj 2017年2月24日上午10:34:22 TODO
 */
@Entity
public class t_user_insur extends Model{
	
	public String bank_name;
	public String bank_account;
	public String insur_code;
	public String insur_price;
	public String insur_limit;
	public Date insur_start;
	public Date insur_end;
	public String insur_bill;
	public Date buy_dt;
	public String remark;
	
	public t_user_insur(){}
	
}
