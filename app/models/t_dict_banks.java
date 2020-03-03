package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 银行
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午04:25:40
 */
@Entity
public class t_dict_banks extends Model {
	public String name;
	public String code;
	public String description;
	public boolean is_use;
}
