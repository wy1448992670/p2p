package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 用户的审计资料统计
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-21 上午10:47:08
 */
@Entity
public class v_user_audit_item_stats extends Model{
	public String user_name;
	public Date time;
	public String email;
	public String mobile;
	public Integer sum_count;
	public Integer audited_count;
	public Integer not_pass_count;
	public Integer auditing_count;
	public Integer bid_success_count;
	public Integer bid_loaning_count;
	public Integer invest_count;
	public String credit_level_image_filename;
	
//	@Transient
//	public CreditLevel creditLevel;
	@Transient
	public String signUserId; // 加密用户ID

	public String getSignUserId() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}

//	/**
//	 * 信用积分
//	 */
//	public CreditLevel getCreditLevel() {
//		return CreditLevel.queryUserCreditLevel(this.id, new ErrorInfo());
//	}
}