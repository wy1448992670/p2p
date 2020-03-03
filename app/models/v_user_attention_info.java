package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

@Entity
public class v_user_attention_info extends Model{

	public long user_id;
	public Date time;
	public long attention_user_id;
	public String attention_user_name;
	public String attention_user_photo;
	public String note_name;
	
	@Transient
	public String sign;//加密ID
	
	@Transient
	public String signAttentionUserId;//加密ID
	
	public String getSign() {
		return Security.addSign(this.user_id, Constants.USER_ID_SIGN);
	}

	public String getSignAttentionUserId() {
		return Security.addSign(this.attention_user_id, Constants.USER_ID_SIGN);
	}
}
