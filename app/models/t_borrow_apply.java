package models;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Transient;

import constants.UserTypeEnum;
import models.core.t_new_product;
import play.db.jpa.Model;
@Entity
public class t_borrow_apply extends Model{
	private static final long serialVersionUID = 2744220176413467928L;
	public String borrow_no;
	public Long user_id;
	
	//产品类型:constants.ProductEnum类: 1信亿贷 2房亿贷 3农亿贷 4亿美贷
	@Access(AccessType.PROPERTY)
	public long product_id;
	@Transient
	public String product_name;
	
    /**
     * version 3.0
     * product_id=4 then t_credit_apply.id else null
     */
	public Long credit_apply_id;
    
	//申请借款性质:constants.UserTypeEnum类: 1个人借款 2企业借款 3个体工商户借款
	@Access(AccessType.PROPERTY)
	public int loan_property_id;//借款性质
	@Transient
	public String loan_property_name;
	
	@Access(AccessType.PROPERTY)
	public Long loan_purpose_id;//借款用途
	@Transient
	public String loan_purpose_name;
	@Transient
	transient public t_dict_loan_purposes loan_purpose;
	
	/** 合作用户|机构 t_users.id */
	public Long consociation_user_id;
	
	/**  是否授权代付给合作人|机构 */
	public Boolean accredit_pay_for_consociation;
	
	public BigDecimal apply_amount;//申请金额
	public BigDecimal approve_amount;//审批金额
	
	//借款申请剩余金额,分标标的会有借款剩余金额
	@Transient
	public BigDecimal bidOverPlusMoney;
	
    /** 借款期限单位-1: 年;0:月;1:日 */
	public Integer period_unit;
	public Integer period;
	
    /** 审批利息 */
	public BigDecimal interest_rate;

    /** 服务费 */
	public BigDecimal service_amount;

    /** 服务费率 */
	public BigDecimal service_cost_rate;

    /** 服务费计算规则:1.(总服务费=借款金额*服务费率) */
	public Integer service_cost_rule;

    /** 服务费支付方式:1.按月还款(总服务费平摊到每期) (待开发 2.放款前支付 3.首期还款 4.末期还款) */
	public Integer service_payment_model;

    /**
     * 返款方式:1.按月还款,等额本息 2.按月付息,到期还本 3.一次性还款
     * select * from t_dict_bid_repayment_types
     */
	@Access(AccessType.PROPERTY)
	public Integer repayment_type_id;
	@Transient
	public t_dict_bid_repayment_types repayment_type;
	
	@Access(AccessType.PROPERTY)
	public Long apply_area;//申请区域
	@Transient
	public String apply_area_name;//申请区域名称
	
	@Access(AccessType.PROPERTY)
	public Long allot_area;//分配区域
	
	@Transient
	public String allot_area_name;//分配区域
	
	public Date apply_time;
	/*
	1	未分配
	2	已分配
	3	已提交
	4	已初审
	5	已通过
	6	借款取消
	7	审核不通过
	 */
	public int status;
	public Date allot_time;
	public Long allot_admin;
	public Date submit_time;
	public Long submit_admin;
	public Date audit_time;
	public Long audit_admin;
	public Date recheck_time;
	public Long recheck_admin;
	public String reason;
	
	
	public long getProduct_id() {
		return product_id;
	}
	public void setProduct_id(long product_id) {
		//ProductEnum product = constants.ProductEnum.getEnumByCode(product_id);
		t_new_product product=new t_new_product().getEnumById(product_id);
		
		if(product == null) {
			throw new IllegalArgumentException("错误的产品类型！");
		}
		this.product_id = product_id;
		this.product_name = product.name;
	}
	public int getLoan_property_id() {
		return loan_property_id;
	}
	public void setLoan_property_id(int loan_property_id) {
		UserTypeEnum userType = UserTypeEnum.getEnumByCode(loan_property_id);
		if(userType == null || userType.getCode()==0) {
			throw new IllegalArgumentException("错误的借款性质！");
		}
		this.loan_property_id = loan_property_id;
		this.loan_property_name = userType.getName();
	}
	public Long getLoan_purpose_id() {
		return loan_purpose_id;
	}
	
	public void setLoan_purpose_id(Long loan_purpose_id) {
		if (loan_purpose_id != null) {
			this.loan_purpose = new t_dict_loan_purposes().getEnumById(loan_purpose_id);
			//this.loan_purpose = t_dict_loan_purposes.findById(loan_purpose_id);
			if (this.loan_purpose == null) {
				throw new IllegalArgumentException("错误的借款用途！");
			}
			this.loan_purpose_id = loan_purpose_id;
			this.loan_purpose_name = loan_purpose.name;
		}
		
	}
	
	public Long getApply_area() {
		return apply_area;
	}
	public void setApply_area(Long apply_area_id) {
		if(apply_area_id==null)return;
		t_agencies agency = new t_agencies().getEnumById(apply_area_id);
 		//t_agencies agency =  t_agencies.findById(apply_area_id);
		if(agency == null || !agency.is_use) {
			throw new IllegalArgumentException("错误的申请区域！");
		}
		this.apply_area = apply_area_id;
		this.apply_area_name = agency.area;
	}
	public Long getAllot_area() {
		return allot_area;
	}
	
	public void setAllot_area(Long allot_area_id) {
		if(allot_area_id==null)return;
		t_agencies agency = new t_agencies().getEnumById(allot_area_id);
		//t_agencies agency =  t_agencies.findById(allot_area_id);
		if(agency == null || !agency.is_use) {
			throw new IllegalArgumentException("错误的分配区域！");
		}
		this.allot_area = allot_area_id;
		this.allot_area_name = agency.name;
	}
	
	public void setApply_amount(BigDecimal apply_amount) {
		if(apply_amount.compareTo(BigDecimal.ZERO)<0){
			throw new IllegalArgumentException("申请金额不能小于0！");
		}
		this.apply_amount = apply_amount;
	}

	public void setApprove_amount(BigDecimal approve_amount) {
		if(approve_amount==null)return;
		if(approve_amount.compareTo(BigDecimal.ZERO)<0){
			throw new IllegalArgumentException("审批金额不能小于0！");
		}
		this.approve_amount = approve_amount;
	}

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
		return "t_borrow_apply [borrow_no=" + borrow_no + ", user_id=" + user_id + ", product_id=" + product_id
				+ ", product_name=" + product_name + ", loan_property_id=" + loan_property_id + ", loan_purpose_id="
				+ loan_purpose_id + ", apply_amount=" + apply_amount + ", approve_amount=" + approve_amount
				+ ", period=" + period + ", apply_area=" + apply_area + ", allot_area=" + allot_area + ", apply_time="
				+ apply_time + ", status=" + status + ", allot_time=" + allot_time + ", allot_admin=" + allot_admin
				+ ", submit_time=" + submit_time + ", submit_admin=" + submit_admin + ", audit_time=" + audit_time
				+ ", audit_admin=" + audit_admin + ", recheck_time=" + recheck_time + ", recheck_admin=" + recheck_admin
				+ ", reason=" + reason + "]";
	}
	
	public boolean canAudit() {
		if(this.product_id==4 && this.status==1) {
			return true;
		}
		return false;
	}
	
	public boolean canClose() {
		if(this.product_id==4 && this.status==5 ) {
			return true;
		}
		return false;
	}
}
