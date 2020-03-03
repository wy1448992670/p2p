package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 会员详情--个人信息
 * @author cp
 * @version 6.0
 * @created 2014年6月18日 下午8:23:45
 */
@Entity
public class v_user_for_personal extends Model {
	
	public String name;
	public Date time;
	public String photo;
	public Date last_login_time;
	public String cityName;
	public String provinceName;
	public Date expiry_date;
	public long invest_count;
	public long bid_count;
	
	@Transient
	public String sign;//加密ID
	
	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}
}
