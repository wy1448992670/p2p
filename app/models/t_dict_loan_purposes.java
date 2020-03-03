package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 借款用途
 * 
 * @author bsr
 * @version 6.0
 * @created 2014年4月4日 下午5:08:12
 */
@Entity
public class t_dict_loan_purposes extends EnumModel {
	public String name;
	public String code;
	public String description;
	public boolean is_use;
	public int _order; // 排序

	public t_dict_loan_purposes() {

	}

	// 查询所有借款用途
	public t_dict_loan_purposes(long id, String name, boolean is_use, int _order) {
		this.id = id;
		this.name = name;
		this.is_use = is_use;
		this._order = _order;
	}
}
