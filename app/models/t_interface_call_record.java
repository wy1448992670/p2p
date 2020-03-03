package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

@Entity
public class t_interface_call_record extends Model{
	public String request_id;
	public String api_id;//接口id
	public Integer business_type;//业务编码 1,身份证照片实名 2,活体认证 3,防欺诈接口
	public Long business_id;//业务id:business_type=1:user.id 2:user.id 3:credit_apply.id
	public Date request_time;
	public String request_params;
	public Integer request_status;//请求结果0,未处理 1,处理中 2,完成 3,异常
	public Integer business_status;//业务状态1成功,0失败
	public String result_msg;
	public String response_params;
	public Date response_time;
	
}
