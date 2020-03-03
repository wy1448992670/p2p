package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import play.db.jpa.Model;

/**
 * 借款标产品审核科目
 * 
 * @author bsr
 * @version 6.0
 * @created 2014年4月4日 下午3:59:34
 */
@Entity
public class t_product_audit_items_log extends Model {

	public long product_id;
	public Date time;
	public long audit_item_id;
	public boolean type;
	public String mark;

	public t_product_audit_items_log() {

	}

	@Transient
	public String aname; // 资料名称

	// 查询产品对应的审核资料(必选和可选)
	public t_product_audit_items_log(long audit_item_id, boolean type, String name) {
		this.audit_item_id = audit_item_id;
		this.type = type;
		this.aname = name;
	}
}
