package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 借款标产品审核科目
 * 
 * @author bsr
 * @version 6.0
 * @created 2014年4月4日 下午3:59:34
 */
@Entity
public class t_product_audit_items extends Model {

	public long product_id;
	public Date time;
	public long audit_item_id;
	public boolean type;
	public String mark;
}
