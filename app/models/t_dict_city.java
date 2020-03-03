package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 城市实体
 * @author zyf
 *
 */
@Entity
public class t_dict_city extends Model{

	private static final long serialVersionUID = 1608801312463284629L;
	
	public String code;
	public String name;
	public String province_code;

}
