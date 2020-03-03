package models.risk;

import play.db.jpa.Model;
import java.math.BigDecimal;
import javax.persistence.Entity;

import models.EnumModel;

/**
 * t_risk_manage_type.compare_type:
 * 1,数字区间:value is decimal,击中[min_value,max_value)得score
 * 2,比较:显示show_str,value.equls(compare_value),true得score
 * 34,日期区间;value is date.getTime(),击中[min_value,max_value)得score
 */
@Entity
public class t_risk_manage_score_option extends EnumModel {
	private static final long serialVersionUID = 1L;

	/** t_risk_manage_type.id */
	public Long type_id;

	public BigDecimal min_value;

	public BigDecimal max_value;

	public String compare_value;
	
	public String show_str;
	
	public BigDecimal score;

	public Integer sequence;

	/** 是否有效 0.无效 1.有效 */
	public boolean is_valid;

	@Override
	public String toString() {
		return "t_risk_manage_score_option [id=" + id + ", type_id=" + type_id + ", min_value=" + min_value + ", max_value="
				+ max_value + ", compare_value=" + compare_value + ", show_str=" + show_str + ", score=" + score
				+ ", sequence=" + sequence + ", is_valid=" + is_valid + "]";
	}
	
}