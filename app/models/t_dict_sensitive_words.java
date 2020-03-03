package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
* @author zhs
* @version 6.0
* @created 2014年4月4日 下午5:20:48
 */
@Entity
public class t_dict_sensitive_words extends Model {
	public String word;
}
