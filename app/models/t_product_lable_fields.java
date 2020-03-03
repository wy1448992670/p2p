package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 借款产品标签添加字段
* @author lwh
* @version 6.0
* @created 2014年4月4日 下午4:04:28
 */
@Entity
public class t_product_lable_fields extends Model {
	
	public long lable_id;
	public String name;
	public String content;
	public int type;
	public String description;
}
