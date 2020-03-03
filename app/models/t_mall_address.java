package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 积分商城 收货地址
 * 
 * @author yuy
 * @time 2015-10-13 16:10
 */
@Entity
public class t_mall_address extends Model {
	public long user_id;
	public Date time;
	public String receiver;// 收货人姓名
	public String tel;// 电话号码
	public String address;// 收货地址
	public String postcode;// 邮政编码

	public int province_id;

	public int city_id;

	public boolean is_default; // 是否默认收货地址

	public t_mall_address() {
		super();
	}

	public t_mall_address(long id, long user_id, Date time, String receiver, String tel, String address, String postcode, boolean is_default,
			int province_id, int city_id) {
		this.id = id;
		this.user_id = user_id;
		this.time = time;
		this.receiver = receiver;
		this.tel = tel;
		this.address = address;
		this.postcode = postcode;
		this.is_default = is_default;
		this.province_id = province_id;
		this.city_id = city_id;
	}

}
