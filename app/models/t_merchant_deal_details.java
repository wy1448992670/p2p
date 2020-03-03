package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 商户号交易信息
 *
 * @author hys
 * @createDate  2015年5月5日 上午8:40:00
 *
 */
@Entity
public class t_merchant_deal_details extends Model{
	
	public Date time;  //创建时间
	public int type;  //交易类型，1：充值，2：提现
	public double amount;  //金额
	public double fee;  //手续费
	public double balance;  //商户余额
	public int status;  //交易状态：-1失败，0已提交，1处理中，2成功
	public long mer_bill_no;  //交易流水号

}
