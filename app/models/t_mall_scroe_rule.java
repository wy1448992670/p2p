package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 积分商城 积分规则
 * 
 * @author yuy
 * @time 2015-10-13 16:10
 */
@Entity
public class t_mall_scroe_rule extends Model {
	public Date time;
	public int type;// 积分类型，1：投资/借款，2：注册，3：签到，4：抽奖
	public int scroe;// 最大可兑换数量
	public int status;// 是否启用，1：启用，2：暂停

	public t_mall_scroe_rule() {
		super();
	}

	public t_mall_scroe_rule(long id, Date time, int type, int scroe, int status) {
		this.id = id;
		this.time = time;
		this.type = type;
		this.scroe = scroe;
		this.status = status;
	}

}
