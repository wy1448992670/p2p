package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 代理充值记录详情
 *
 * @author hys
 * @createDate  2015年8月17日 下午2:36:16
 *
 */
@Entity
public class t_user_agent_pay extends Model {

	public long user_id;  //用户id

	public String user_name;  //用户名

	public String usercustId;  //资金托管账号

	public Date time;  //创建时间

	public double amount;  //充值金额

	public int status;  //状态（0：用户支付中，1：已支付，2：商户转账中，3：已转账）

	public Date paid_time;  //用户完成支付充值金额的时间

	public Date transfered_time;  //商户完成转账给用户的时间

	public Date completed_time;  //代理充值完成的时间
	
	public int agent;  //代理充值的网关，目前支持：通联
	
	public long agent_order_no;  //用户支付的流水号
	
	public long mer_order_no;  //商户转账给用户的流水号

}
