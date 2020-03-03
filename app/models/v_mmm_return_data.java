package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;
@Entity
public class v_mmm_return_data extends Model{ 
	public String orderNum; //流水号
	public int status;		//传输状态
	public String user_name;		//用户名	
	public String user_ips_actno;	//用户ips账号
	public Date return_time;				//回调时间
	public Date send_time;					//发送时间
	public String parent_orderNum;	//父级流水号
	public String msg;				//用户信息
	public long send_id;			//提交参数Id
	public String type;				//接口类型
	public String url;				//补单地址
}

