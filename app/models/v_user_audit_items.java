package models;

import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Transient;
import org.apache.commons.lang.StringUtils;
import constants.Constants;
import business.ProductAuditItem;
import play.Logger;
import play.db.jpa.Model;
import utils.Security;

/**
 * 会员审核资料
 * 
 * @author lzp
 * @version 6.0
 * @created 2014-6-18
 */
@Entity
public class v_user_audit_items extends Model {
	public String no;
	public String user_name;
	public Long audit_item_id;
	public Long user_id;
	public String name;
	public Integer period;
	public Date time;
	public Integer credit_score;
	public String image_file_name;
	public Integer status;
	public Date submit_time;
	public Date audit_time;
	public Date expire_time;
	public Boolean is_over_borrow;
	public Long over_borrow_id;
	public String description;
	public Integer type;
	public Boolean is_visible;
	public String mark;
	
	@Transient
	public String sign;
	@Transient
	public String signItemId;
	@Transient
	public String signUserId;
	@Transient
	public List<String> productNames; // 关联的借款标
	@Transient
	public String strStatus; // String状态值
	@Transient	
	public String images; // UUID图片字符串

	public String getImages() {
		List<String> items = null;
		String hql = "select image_file_name from t_user_audit_items where user_id = ? and audit_item_id = ? order by audit_item_id";
		
		try {
			items = t_user_audit_items.find(hql, this.user_id, this.audit_item_id).fetch();
		} catch (Exception e) {
			Logger.error("用户资料->查询用户同一资料集合:" + e.getMessage());
			
			return "";
		}
		
		if(null == items || items.size() == 0) return "";
		
		StringBuffer buffer = new StringBuffer();
		int len = 0;
		
		for (String image : items) {
			if(StringUtils.isBlank(image))
				continue;
				
			len = image.length();
			
			if(len <= 0) 
				continue;
			
			///* 数据库中存放/images?uuid=???，故截取/images?uuid=,，若数据库存放有所变动，请修改*/
			//image = image.substring(13, len); 
			buffer.append(image).append(":");
		}
		
		return buffer.toString();
	}

	/**
	 * 每个用户资料关联的借款标产品
	 */
	public List<String> getProductNames() {
		try {
			return ProductAuditItem.queryProductName(this.audit_item_id);
		} catch (Exception e) {
			Logger.error("资料(数据实体)->查询资料关联到的产品名称:" + e.getMessage());

			return null;
		}
	}

	public String getSignUserId() {
		return Security.addSign(this.user_id, Constants.USER_ID_SIGN);
	}

	public String getSignItemId() {
		return Security.addSign(this.audit_item_id, Constants.ITEM_ID_SIGN);
	}
	
	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ITEM_ID_SIGN);
	}
	
	public String getStrStatus() {
		if(null == this.strStatus) {
			switch (this.status) {
				case Constants.UNCOMMITTED: this.strStatus = "未提交"; break;
				case Constants.AUDITING: this.strStatus = "审核中"; break;
				case Constants.AUDITED: this.strStatus = "通过审核"; break;
				case Constants.EXPIRED: this.strStatus = "过期失效"; break;
				case Constants.UPLOAD: this.strStatus = "上传未付款"; break;
				case Constants.NOT_PASS: this.strStatus = "未通过审核"; break;
			}
		}
		
		return this.strStatus;
	}
}
