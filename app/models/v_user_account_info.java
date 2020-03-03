package models;

import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 资金管理中的账户信息
 * @author cp
 * @version 6.0
 * @created 2014年5月21日 下午7:26:42
 */
@Entity
public class v_user_account_info extends Model {
	public double user_account;
	public double user_account2;
	public double freeze;
	public double invest_amount;
	public long invest_count;
	public double bid_amount;
	public long bid_count;
	public double receive_amount;
	public double repayment_amount;
	
	@Transient
	public String sign;//加密ID
	
	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}
}
