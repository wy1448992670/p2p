package models.core;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class t_credit_increase_apply extends Model {
    private static final long serialVersionUID = 1L;

    public long credit_apply_id;

    /** 提额前额度 */
    public BigDecimal prepare_credit_amount;

    /** 新申请额度:prepare_credit_amount+申请提额额度 */
    public BigDecimal apply_credit_amount;

    /** 新审批额度:prepare_credit_amount+审批提额额度 */
    public BigDecimal audit_credit_amount;

    /**
     * -3.终审不通过 -2.初审不通过 -1.机审不通过 0.待审 1.机审通过 2.初审通过 3.终审通过
     * select * from  t_enum_map where enum_type=5
     */
    public int status;//-3.终审不通过 -2.初审不通过 -1.机审不通过 0.待审 1.机审通过 2.初审通过 3.终审通过

    public Date apply_time;

    public Date audit_time;

    /** 说明 */
    public String remark;

    public boolean isPass() {
    	if(status==3) {
    		return true;
    	}
    	return false;
    }
    
    public boolean isNotPass() {
    	if(status < 0) {
    		return true;
    	}
    	return false;
    }
	public boolean canAudit() {
		if(this.status == 0) {
			return true;
		}
		return false;
	}
	
    public BigDecimal getApplyIncreaseAmount() {
    	if(apply_credit_amount==null) {
    		return null;
    	}
    	return apply_credit_amount.subtract(prepare_credit_amount);
    }
    
    public BigDecimal getAuditIncreaseAmount() {
    	if(audit_credit_amount==null) {
    		return null;
    	}
    	return audit_credit_amount.subtract(prepare_credit_amount);
    }
	@Override
	public String toString() {
		return "t_credit_increase_apply [id=" + id + ", credit_apply_id=" + credit_apply_id + ", prepare_credit_amount="
				+ prepare_credit_amount + ", apply_credit_amount=" + apply_credit_amount + ", audit_credit_amount="
				+ audit_credit_amount + ", status=" + status + ", apply_time=" + apply_time + ", audit_time=" + audit_time
				+ ", remark=" + remark + "]";
	}

}
