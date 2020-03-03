package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 后台--提现管理
 * @author cp
 * @version 6.0
 * @created 2014年6月13日 下午7:41:55
 */

@Entity
public class v_user_withdrawal_info extends Model {

	public long user_id;
	public String name;
	public double credit_score;
	public double user_amount;
	public Date time;
	public int type;
	public Date audit_time;
	public Date pay_time;
	public double amount;
	public int status;
	public double repayment_amount;
	public double receive_amount;
	public String credit_level_image_filename;
	public String reality_name;
	public int finance_type; // 用户理财类型 0 借款人 1 投资人
	public boolean is_auto_create;
	@Transient
	public String signUserId; // 加密用户ID
	
	/**
	 * 获取加密用户ID
	 */
	public String getSignUserId() {
		return Security.addSign(this.user_id, Constants.USER_ID_SIGN);
	}
}
