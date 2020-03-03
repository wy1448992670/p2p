package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 区域
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午04:21:02
 */
@Entity
public class t_dict_ad_areas extends Model {
	public int city_id;
	public String name;
}
