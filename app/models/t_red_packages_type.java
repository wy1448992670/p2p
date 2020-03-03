package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;

import constants.Constants;
import play.db.jpa.Model;
import utils.Security;


@Entity
public class t_red_packages_type extends Model {


	public String  name;	  // '红包类型',
	public String  typeName;	  // '红包类型',
	public Date time;	      // '创建时间',
	public long validity_time;// '有效期',
	public long validate_unit;// '有效期单位 -1年 0月 1日',
	public double money; 	 //  '红包金额',
	public double obtain_money;//获取条件',
	public double validity_money;//起投金额',
	public boolean notice_email;// 通知方式邮件 
	public boolean notice_message;//'通知方式短信',
	public boolean notice_box ;//'通知方式站内',
	public boolean status;//'状态 -1 禁用中  0 启用中'
	public int is_new_user;// 新手标是否使用 
	public int bid_period;//最低标的期限限制 0-无限制
	public int bid_period_unit;//借款期限-1: 年;0:月;1:日;
	public String rules;//规则
	public String item_code;//积分兑换-商品代码
	public Double received_money_start;//回款本金 起始
	public Double received_money_end;//回款本金 截止
	public Double all_invest_money;//累计投资金额
	public Integer all_invest_count;//累计投资笔数
	public Integer reg_time;//注册月数
	public Integer coupon_type;//优惠券类型  1  红包  2  加息券
	@Transient
	public String sign;

	/**
	 * 获取加密ID
	 */
	public String getSign() {
		return Security.addSign(this.id, Constants.RID_ID_SIGN);
	}
}
