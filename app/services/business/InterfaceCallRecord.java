package services.business;

import java.util.Date;

import models.t_interface_call_record;
import utils.OrderNoFactory;

public class InterfaceCallRecord {
	/**
	 * 
	 * @param requestParams 请求参数json
	 * @param businessId 业务ID
	 * @param business_type 业务类型
	 * @param api_id 接口ID
	 * @param initStatus 初始状态 0,未处理 1,处理中 2,完成 3,异常
	 * @return
	 */
	public static t_interface_call_record saveInterfaceCallRecordRequest(String requestParams,Long businessId,
			int business_type, String api_id, int initStatus) {
		t_interface_call_record record = new t_interface_call_record();
		record.request_id = OrderNoFactory.getNo();
		record.api_id = api_id;
		record.business_type = business_type; //1,身份证照片实名 2,活体认证 3,防欺诈接口
		record.business_id = businessId;//credit_apply.id
		record.request_time = new Date();
		record.request_params = requestParams;
		record.request_status =  initStatus; //处理中
		return record; 
	}
}
