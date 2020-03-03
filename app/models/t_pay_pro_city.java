package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 富友省市代码
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午04:21:02
 */
@Entity
public class t_pay_pro_city extends Model {
	
	public String prov_num;
	
	public String prov_name;
	
	public String city_num;
	
	public String city_name;
}
