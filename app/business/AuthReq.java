package business;

import java.io.Serializable;
import java.util.Date;

import models.t_auth_req;
import play.Logger;

public class AuthReq implements Serializable {

	public void create(String refID, Long uid, Integer type) {
		t_auth_req req = new t_auth_req();
		req.refID = refID;
		req.uid = uid;
		req.type = type;
		req.insert_time = new Date();

		try {
			req.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("保存个人信息（身份证或银行卡）认证响应:" +  e.getMessage());
			
			return;
		}
	}
}
