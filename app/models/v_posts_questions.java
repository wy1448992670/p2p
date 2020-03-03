package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import constants.Constants;

import play.db.jpa.Model;
import utils.Security;
@Entity
public class v_posts_questions extends Model{
	
	public long user_id;
	public String userName;
	public String forumName;
	public String toAnswerUser;
	public String content;
	public String typeName;
	public Date time;
	public int answers_count;
	public int readStatus;
	public long posts_id;
	
	@Transient
	public String timeBetween;
	@Transient
	public String signUserId;
	@Transient
	public boolean answersShow=true;
	
	public String getSignUserId() {
		return Security.addSign(this.user_id, Constants.USER_ID_SIGN);
	}
}
