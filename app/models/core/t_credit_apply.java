package models.core;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.t_dict_bid_repayment_types;
import play.db.jpa.Model;

@Entity
public class t_credit_apply extends Model implements Cloneable {
	private static final long serialVersionUID = 1L;

	public long user_id;

	/** 产品类型:ProductEnum枚举: 1信亿贷 2房亿贷 3农亿贷 4亿美贷 */
	public Long product_id;

	/** 合作用户|机构 t_users.id */
	public Long consociation_user_id;

	/** 是否授权代付给合作人|机构 */
	public boolean accredit_pay_for_consociation;

	/** 项目总金额 */
	public BigDecimal items_amount;

	/** 申请额度 */
	public BigDecimal apply_credit_amount;

	/**  机审分数 */
	public BigDecimal machine_score;

	/** 机审额度 */
	public BigDecimal machine_credit_amount;

	/**  审批额度 */
	public BigDecimal audit_credit_amount;

	/** 借款期限单位-1: 年;0:月;1:日 */
	public Integer apply_period_unit;

	/** 借款期限 */
	public Integer apply_period;

	/** 审批利息 */
	public BigDecimal apply_apr;

	/** 服务费 */
	public BigDecimal service_amount;

	/** 服务费率 */
	public BigDecimal service_cost_rate;

	/** 服务费计算规则:1.(总服务费=借款金额*服务费率) */
	public Integer service_cost_rule;

	/** 服务费支付方式:1.按月还款(总服务费平摊到每期) (待开发 2.放款前支付 3.首期还款 4.末期还款) */
	public Integer service_payment_model;

	/** 返款方式:1.按月还款,等额本息 2.按月付息,到期还本 3.一次性还款 select * from t_dict_bid_repayment_types */
	@Access(AccessType.PROPERTY)
	public Integer repayment_type_id;
	@Transient
	public t_dict_bid_repayment_types repayment_type;
	
	/** 
	 * -3.终审不通过 -2.初审不通过 -1.机审不通过 0.待审 1.机审通过 2.初审通过 3.终审通过 4.冻结 5.关闭
	 * select * from  t_enum_map where enum_type=4 
	 */
	public int status;

	/** 申请时间 */
	public Date apply_time;

	/** 审核时间 */
	public Date audit_time;
	
	/** 最后更新时间 */
	public Date update_time;
	
	/** 风控接口状态 */

	/** 风控接口状态 1成功 0 失败 -1同盾接口失败 -2 星护甲接口失败 -3 居住地验证失败*/
	public Integer interface_risk_status;

	/** 风控接口调用结果描述 */
	public String interface_risk_msg;

	/** 风控接口调用时间 */
	public Date interface_risk_time;

	public String remark;

	public void setRepayment_type_id(Integer repayment_type_id) {
		if(repayment_type_id==null){
			this.repayment_type_id=repayment_type_id;
			this.repayment_type = null;
			return;
		}
		this.repayment_type = new t_dict_bid_repayment_types().getEnumById(repayment_type_id);
		if (this.repayment_type == null) {
			throw new IllegalArgumentException("错误的返款方式！");
        }
		this.repayment_type_id = repayment_type_id;
	}

	@Override
	public String toString() {
		return "t_credit_apply [id=" + id + ", user_id=" + user_id + ", product_id=" + product_id + ", consociation_user_id="
				+ consociation_user_id + ", accredit_pay_for_consociation=" + accredit_pay_for_consociation
				+ ", items_amount=" + items_amount + ", apply_credit_amount=" + apply_credit_amount + ", machine_score="
				+ machine_score + ", machine_credit_amount=" + machine_credit_amount + ", audit_credit_amount="
				+ audit_credit_amount + ", apply_period_unit=" + apply_period_unit + ", apply_period=" + apply_period
				+ ", apply_apr=" + apply_apr + ", service_amount=" + service_amount + ", service_cost_rate="
				+ service_cost_rate + ", service_cost_rule=" + service_cost_rule + ", service_payment_model="
				+ service_payment_model + ", repayment_type_id=" + repayment_type_id + ", status=" + status
				+ ", apply_time=" + apply_time + ", audit_time=" + audit_time + ", interface_risk_status="
				+ interface_risk_status + ", interface_risk_msg=" + interface_risk_msg + ", interface_risk_time="
				+ interface_risk_time + ", remark=" + remark + "]";
	}
	
	/*
	 * 对敏感信息做隐藏
	 */
	public t_credit_apply hide_data() throws CloneNotSupportedException {
		t_credit_apply credit_apply=(t_credit_apply)this.clone();
		credit_apply.user_id=0;
		credit_apply.consociation_user_id=0L;
		
		return credit_apply;
	}
	
	public boolean canAudit() {
		if(this.product_id==4 && this.status==1) {
			return true;
		}
		return false;
	}
	
	public boolean canFreeze() {
		if(this.product_id==4 && this.status==3) {
			return true;
		}
		return false;
	}
	
	public boolean canUnfreeze() {
		if(this.product_id==4 && this.status==4) {
			return true;
		}
		return false;
	}
	
	public boolean canClose() {
		if(this.product_id==4 && (this.status==3 || this.status==4)) {
			return true;
		}
		return false;
	}
	
	public boolean isClose() {
    	if(this.product_id==4 && (this.status < 0 || this.status==5 )) {
    		return true;
    	}
    	return false;
    }
	/**
	 * 申请是否被拒绝
	 * @return
	 */
	public boolean isNotThrough() {
		if(this.product_id==4 && this.status<0) {
			return true;
		}
		return false;
	}
}
