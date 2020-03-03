package models.risk;

import java.math.BigDecimal;

import javax.persistence.AccessType;
import javax.persistence.Entity;
import play.db.jpa.Model;

@Entity
public class t_risk_manage_score extends Model {
	private static final long serialVersionUID = 1L;

	/** t_credit_apply.id */
	public long credit_apply_id;

	/** t_risk_manage_type.id */
	@javax.persistence.Access(AccessType.PROPERTY)
	public long type_id;
	@javax.persistence.Transient
	t_risk_manage_type risk_manage_type;
	
	/** 输入值 */
	public String value;

	public BigDecimal score;

	public void setType_id(long type_id) {
		this.risk_manage_type=new t_risk_manage_type().getEnumById(type_id);
		if(this.risk_manage_type == null) {
			throw new IllegalArgumentException("错误的打分项！");
		}
		this.type_id = type_id;
	}
	
	@Override
	public String toString() {
		return "t_risk_manage_score [id=" + id + ", credit_apply_id=" + credit_apply_id + ", type_id=" + type_id + ", value=" + value
				+ ", score=" + score + "]";
	}

}