package models.core;

import java.math.BigDecimal;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class t_organization extends Model {
	private static final long serialVersionUID = 1L;

	/** 用户id */
	public Long user_id;
	
	/** 机构是否使用 */
	public boolean is_use;
	
	/** 机构类型:1.亿美贷产品(product_id=4)合作机构 */
	public Integer org_type;
	
	/** BD人员t_person.id */
	public Long bd_person_id;
	

	@Override
	public String toString() {
		return "t_organization [id=" + id +", user_id=" + user_id + ", is_use=" + is_use + ", bd_person_id=" + bd_person_id 
				+ ", bd_person_id=" + bd_person_id +  "]";
	}


}
