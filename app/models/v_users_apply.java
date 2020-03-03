package models;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

@Entity
public class v_users_apply extends Model {
	
	public String reality_name;
	
	public String mobile;
		
	public String id_number;
	

	public BigDecimal apply_amount;

	public Date  apply_time;

}
