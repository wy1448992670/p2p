package controllers.supervisor.webContentManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import business.DictBanksDate;
import controllers.supervisor.SupervisorController;

public class PayLimitAction extends SupervisorController{
	/**
	 * 
	 * @author liulj
	 * @creationDate. Jun 29, 2018 1:19:04 PM 
	 * @description.  
	 * insert into `p2p_dev`.`t_right_actions` ( `id`, `right_id`, `action`, `description`) values ( '1460', '25', 'supervisor.webContentManager.PayLimitAction.payLimit', '银行卡限额设置');
	 * insert into `p2p_dev`.`t_right_actions` ( `id`, `right_id`, `action`, `description`) values ( '1461', '25', 'supervisor.webContentManager.PayLimitAction.payLimitSet', '银行卡限额设置');
	 * 
	 * @param title
	 */
	
	public static void payLimit(String title){
		List<Map<String, Object>> banklimit = DictBanksDate.findBankLimit();
		
		render(banklimit);
	}
	
	public static void payLimitSet(int bank_code, String pay_limit, String day_limit){
		try {
			DictBanksDate.updateBankLimit(bank_code, pay_limit, day_limit);
			renderText("1");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			renderText("0");
		}
	}
	
}
