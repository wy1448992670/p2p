package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 资金托管接口请求参数
 * 
 * @author 2015年3月25日22:04:10
 * 
 */
@Entity
public class t_mmm_data extends Model {

	public String mmmUserId;  //第三方唯一标示(系统保存的托管账户号)
	public String orderNum;  //请求动作唯一标识(通常为交易流水号)
	public Date op_time;  //操作时间
	public String parent_orderNum;  //关联流水号(单业务调用多接口)
	public String msg;  //操作描述
	public String data;  //请求数据
	public int status;  //状态:1未处理/失败，2成功
	public String type;  //接口类型
	public String url;  //日志补单地址，异步回调地址
}
