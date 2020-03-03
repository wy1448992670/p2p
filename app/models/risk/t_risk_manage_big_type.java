package models.risk;

import java.math.BigDecimal;
import javax.persistence.Entity;
import play.db.jpa.Model;

@Entity
public class t_risk_manage_big_type extends Model {
	private static final long serialVersionUID = 1L;

	/**
	 * 1.信亿贷 2.房亿贷 3.农亿贷 4.亿美贷 constants.ProductEnum
	 */
	public int product_id;

	public String name;

	/** 总分:限制t_risk_manage_type.sum(top_score) */
	public BigDecimal total_score;

	/** 显示顺序 */
	public int sequence;

	/** 是否有效 0.无效 1.有效 */
	public boolean is_valid;

	@Override
	public String toString() {
		return "t_risk_manage_big_type [id=" + id + ", product_id=" + product_id + ", name=" + name + ", total_score=" + total_score
				+ ", sequence=" + sequence + ", is_valid=" + is_valid + "]";
	}
	
	
}