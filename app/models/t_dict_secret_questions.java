package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
* @author zhs
* @version 6.0
* @created 2014年4月4日 下午5:21:05
 */
@Entity
public class t_dict_secret_questions extends Model {
	public String name;
	public String type;
	public int use_count;
	public boolean is_use;
}
