package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 银行字典
 * @author yangxuan
 * @date 2015年4月25日 下午4:25:29
 */
@Entity
public class t_dict_banks_col extends Model{

	public int bank_code;
	public String bank_name;
	
}
