package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 银行支行字典
 * @author yangxuan
 * @date 2015年4月24日 下午4:36:42
 */
@Entity
public class t_dict_banks_data extends Model{

	public int bank_number;  //银行的联行号
	public int bank_code;  //支行号前3位数字，代表该支行所属的母行分类
	public String bank_name;  //银行名称
	public int county_code;  //县城code
	public String county_name;  //县城名称
	public int city_code;  //市 code
	public String city_name;  //市 name
	public int province_code;  //省份 code
	public String province_name;  //省份 name
	
}
