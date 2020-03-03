package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 资金托管记录
 * @author lzp
 * @version 6.0
 * @created 2014-10-23
 */
@Entity
public class t_ips_details extends Model {
	public String mer_bill_no;
	public String user_name;
	public Date time;
	public int type;
	public int status;
	public String memo;
}
