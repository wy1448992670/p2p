package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;

import constants.Constants;
import play.db.jpa.Model;
import utils.Security;


@Entity
public class t_red_packages_history_count extends Model {
 

	public String  name;	  	// '红包类型',
	public String  type;	  	// '红包类型',
	public long totalCount;		// '红包总个数',
	public long userCount;		// '已使用红包数
	public double totalAmount;		// '红包总金额
}
