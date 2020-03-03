package models.core;

import java.math.BigDecimal;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * <p>
 * 机构项目类目表
 * </p>
 *
 * @author zhangj
 * @since 2019-01-07
 */
@Entity
public class t_service_cost_rate extends Model {
	private static final long serialVersionUID = 1L;

	/** 合作机构id,-1是全局默认服务费,如果一个合作机构的t_service,对应product_id的数据为0条,使用默认服务费 */
	public Long org_id;

	/** 产品id */
	public Long product_id;

	/** 借款期限单位-1: 年;0:月;1:日 */
	public Integer period_unit;

	/** 借款期限(单位 月) */
	public Integer period;

	/** 服务费率 */
	public BigDecimal service_cost_rate;

	/** 服务费计算规则:1.(总服务费=借款金额*服务费率) */
	public Integer service_cost_rule;

	/** 服务费支付方式:1.按月还款(总服务费平摊到每期) (待开发 2.放款前支付 3.首期还款 4.末期还款) */
	public Integer service_payment_model;

	@Override
	public String toString() {
		return "t_service{" + "id=" + id + ", org_id=" + org_id + ", product_id=" + product_id + ", period_unit="
				+ period_unit + ", period=" + period + ", service_cost_rate=" + service_cost_rate
				+ ", service_cost_rule=" + service_cost_rule + ", service_payment_model=" + service_payment_model + "}";
	}
}
