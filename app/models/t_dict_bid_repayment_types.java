package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 借款标还款方式
 * 
 * @author zhs
 * @version 6.0
 * @created 2014年4月4日 下午4:05:02
 */
@Entity
public class t_dict_bid_repayment_types extends EnumModel {
	public String name;
	public String code;
	public String description;
	public boolean is_use;
	
	public t_dict_bid_repayment_types(){
		
	}
	
	/**
	 * 还款类型列表
	 * @param id ID
	 * @param name 名称
	 * @param is_use 是否启用
	 */
	public t_dict_bid_repayment_types(long id, String name, boolean is_use) {
		this.id = id;
		this.name = name;
		this.is_use = is_use;
	}
}
