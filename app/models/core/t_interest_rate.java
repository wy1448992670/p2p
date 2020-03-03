package models.core;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.Transient;

import play.db.jpa.Model;

/**
 * <p>
 * 合作机构利息率
 * </p>
 *
 * @author zhangj
 * @since 2019-01-07
 */
@Entity
public class t_interest_rate extends Model {
	private static final long serialVersionUID = 1L;

	/** 合作机构id,-1是全局默认利息率,如果一个合作机构的t_interest_rate,对应product_id的数据为0条,使用默认利息率 */
	public long org_id;

	/** 产品id */
	public long product_id;
	
	/** 返款方式:1.按月还款,等额本息 2.按月付息,到期还本 3.一次性还款 select * from t_dict_bid_repayment_types */
	public int repayment_type_id;
	
	/** 借款期限单位-1: 年;0:月;1:日 */
	public Integer period_unit;

	/** 借款期限(单位 月) */
	public Integer period;

	/** 利息率 */
	public BigDecimal interest_rate;
	
	@Transient
	public t_service_cost_rate service_cost_rate;
	
	@Override
	public String toString() {
		return "t_interest_rate{" + "id=" + id + ", org_id=" + org_id + ", product_id=" + product_id + ", period_unit=" + period_unit + ", period="
				+ period + ", interest_rate=" + interest_rate + "}";
	}
}
