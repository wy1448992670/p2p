package models;

import javax.persistence.Entity;
import play.db.jpa.Model;
import java.util.Date;

/**
 * 
 * @author lzp
 * @version 6.0
 * @created 2014-4-4 下午3:32:26
 */

@Entity
public class t_user_audit_items extends Model {
	public Long user_id;
	public Date time;
	public Date submit_time;
	public Long audit_item_id;
	public Integer status;
	public String image_file_name;
	public Date expire_time;
	public Boolean is_over_borrow;
	public Long over_borrow_id;
	public Long audit_supervisor_id ;
	public Date audit_time;
	public String suggestion;
	public Boolean is_visible; 
	public String mark;
	
	public Integer status_type;
	
	public t_user_audit_items(){
		
	}
	
	/**
	 * 前台首页资料显示查询
	 * @param audit_item_id
	 * @param mark
	 */
	public t_user_audit_items(long audit_item_id, String mark){
		this.audit_item_id = audit_item_id;
		this.mark = mark;
	}
	
	/**
	 * 查询是否关联超额借款、超额借款ID
	 * @param is_over_borrow
	 * @param over_borrow_id
	 */
	public t_user_audit_items(Boolean is_over_borrow, Long over_borrow_id, String mark) {
		this.is_over_borrow = is_over_borrow;
		this.over_borrow_id = over_borrow_id;
		this.mark = mark;
	}
}
