package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 借款产品标签
* @author lwh
* @version 6.0
* @created 2014年4月4日 下午4:10:23
 */
@Entity
public class t_product_lables extends Model {
	
	public long product_id;
	public String name;
	public String description;
}
