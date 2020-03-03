package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
* @author zhs
* @version 6.0
* @created 2014年4月4日 下午5:00:24
 */
@Entity
public class t_dict_educations extends EnumModel {
	public String name;
	public String code;
	public String description;
	public boolean is_use;
}
