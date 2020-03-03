package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 信用等级
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午04:19:12
 */
@Entity
public class t_credit_levels extends Model {
	public Date time;
	public String name;
	public String image_filename;
	public boolean is_enable;
	public boolean is_allow_overdue;
	public int min_credit_score;
	public int min_audit_items;
	public String suggest;
	public String must_items;
	public int order_sort;
	
	public t_credit_levels() {

	}
	
	public t_credit_levels(long id, String name, String image_filename) {
		this.id = id;
		this.name = name;
		this.image_filename = image_filename;
	}

	public t_credit_levels(long id, String name, String image_filename,int order_sort) {
		this.id = id;
		this.name = name;
		this.image_filename = image_filename;
		this.order_sort = order_sort;
	}
}
