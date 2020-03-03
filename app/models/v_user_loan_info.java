package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

@Entity
public class v_user_loan_info extends Model {

	public Date register_time;//注册时间
	public String name;//会员名
	public String email;
	public String mobile;
	public double credit_score;
	public boolean is_allow_login;
	public double user_amount;//账户余额
	public long bid_count;
	public double bid_amount;
	public double avg_apr;//借款均年利率
	public long bid_loaning_count;//借款中的借款标数量
	public long bid_repayment_count;//还款中的借款标数量
	public long overdue_bill_count;//逾期账单数量
	public long bad_bid_count;//坏账借款标数量
	public double repayment_amount;//应还总额
	public long audit_item_count;//已审核科目数
	public String supervisor_name;//客服
	public String credit_level_image_filename;
	
	@Transient
	public String sign;//加密ID

	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}
}