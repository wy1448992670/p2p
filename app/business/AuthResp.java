package business;

import java.io.Serializable;
import java.util.Date;

import constants.Constants;
import constants.SupervisorEvent;
import models.t_agencies;
import models.t_auth_resp;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;

public class AuthResp implements Serializable {
	
	
	public void create(String refID, Long status, String errorCode, String errorMsg) {
		t_auth_resp resp = new t_auth_resp();
		resp.refID = refID;
		resp.status = status;
		resp.errorCode = errorCode;
		resp.errorMsg = errorMsg;
		resp.insert_time = new Date();

		try {
			resp.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("保存个人信息（身份证或银行卡）认证请求:" +  e.getMessage());
			
			return;
		}
	}
}
