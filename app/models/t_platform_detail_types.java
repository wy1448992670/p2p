package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 平台交易记录类型
 * @author cp
 * @version 6.0
 * @created 2014年7月16日 上午11:21:13
 */
@Entity
public class t_platform_detail_types extends Model {
	
	public String name;
	public int type;
	public String description;
}
