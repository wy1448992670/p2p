package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;

import constants.Constants;
import play.db.jpa.Model;
import utils.Security;


@Entity
public class t_red_packages_history extends Model {
 

	public String  name;	  // '红包名称',
	public String  type;	  // '红包类型   2 充值 3 投资',
	public Date time;	      // '创建时间',
	public Date use_time;//使用时间
	public long validity_time;// '有效期',
	public long validate_unit;// '有效期单位 -1年 0月 1日',
	public double money; 	  //  '红包金额',
	public long send_type;	  //获得方式 0自动  1手动,
	public long status;		  //'状态 0 有效 1已使用 -1已过期'
	public long user_id;		  // '用户ID' ,
	public String user_name;   //'用户名称',
	public boolean notice_email;// 通知方式邮件 
	public boolean notice_message;//'通知方式短信',
	public boolean notice_box ;//'通知方式站内',
	public long invest_id;	//投资ID
	public double valid_money; // 起投点' ,
	public int bid_period;//最低标的期限限制
	public int bid_period_unit;//借款期限-1: 年;0:月;1:日;
	public int is_new_user;//新手标是否使用
	public String remark; //投资红包，首投与再投、秒标与普通显示在此
	public Long type_id;//红包类型ID
	public int coupon_type;//优惠券类型  1  红包  2  加息券
	@Transient
	public String sign;
	@Transient
	public long days; //剩余天数
	@Transient
	public Date endDate; //截止日期
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		return Security.addSign(this.id, Constants.RID_ID_SIGN);
	}
}
