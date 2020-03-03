package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 城市
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午04:21:36
 */
@Entity
public class t_dict_ad_citys extends Model {
	public int province_id;
	public String name;
}
