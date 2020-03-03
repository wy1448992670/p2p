package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

/**
 * 积分商城 积分记录
 * 
 * @author yuy
 * @time 2015-10-13 16:10
 */
@Entity
public class t_mall_scroe_record extends Model {
	public long user_id;
	public String user_name;
	public Date time;
	public int type;// 积分类型，1：投资/借款，2：注册，3：签到，4：抽奖，5：兑换实物 6：兑换红包
	public Long relation_id;// 关联id：goods_id，invest_id
	public int scroe;// 积分
	public int status;// 赠送/消费状态，1：成功，2：等待赠送中
	public Double quantity;// 量，如兑换数量，理财金额
	public String description;// 描述，如兑换**
	public String remark;// 备注，如收货地址，理财产品

	public int levelDay;// 签到第几天
	/******** app查询兑换商品记录需要添加显示如下字段 2015-12-1 易健 ***********/

	@Transient
	public long goodsId;// 兑换商品id

	@Transient
	public String goodsName;// 兑换商品的名称

	@Transient
	public String goodsPicPath;// 兑换商品所在的图片路径

	/******** app查询兑换商品记录需要添加显示如下字段 2015-12-1 易健 ***********/

	public t_mall_scroe_record() {
		super();
	}

	public t_mall_scroe_record(long id, long user_id, String user_name, Date time, int type, Long relation_id, int scroe, int status,
			Double quantity, String description, String remark) {
		this.id = id;
		this.user_id = user_id;
		this.user_name = user_name;
		this.time = time;
		this.type = type;
		this.relation_id = relation_id;
		this.scroe = scroe;
		this.status = status;
		this.quantity = quantity;
		this.description = description;
		this.remark = remark;
	}

	public t_mall_scroe_record(Date time, int scroe, Double quantity, long goodsId, String goodsName, String goodsPicPath) {
		this.time = time;
		this.scroe = scroe;
		this.quantity = quantity;
		this.goodsId = goodsId;
		this.goodsName = goodsName;
		this.goodsPicPath = goodsPicPath;
	}

}
