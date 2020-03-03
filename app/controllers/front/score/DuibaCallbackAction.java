package controllers.front.score;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JSONUtils;
import utils.Security;
import utils.SignTool;
import business.Score;
import business.User;
import models.t_score_convert;
import constants.Constants;
import controllers.BaseController;

/**
 * 
* Description: 兑吧回调接口
* @author xinsw
* @date 2017年9月11日
 */
public class DuibaCallbackAction extends BaseController {
	
	public static void deduct(){
		Map<String,String> pd = params.allSimple();
		Set<String> keys = pd.keySet();
		for(String k : keys){
			Logger.info("====>" + k + "-----" + pd.get(k));
		}
		
		String uid = params.get("uid");
		String credits = params.get("credits");
		String item_code = params.get("itemCode");
		String time = params.get("timestamp");
		String description = params.get("description");
		String order_num = params.get("orderNum");
		String type = params.get("type");
		String face_price = params.get("facePrice");
		String actual_price = params.get("actualPrice");
		String ip = params.get("ip");
		String wait_audit = params.get("waitAudit");
		String param = params.get("params");
		String appKey = params.get("appKey");
		String phone = params.get("phone");
		
		Map<String,String> map = new HashMap<String, String>();
		map.put("appKey", appKey);
		map.put("uid", uid);
		map.put("credits", credits);
		map.put("itemCode", item_code);
		map.put("time", time);
		map.put("description", description);
		map.put("orderNum", order_num);
		map.put("type", type);
		map.put("facePrice", face_price);
		map.put("actualPrice", actual_price);
		map.put("ip", ip);
		map.put("waitAudit", wait_audit);
		map.put("params", param);
		map.put("sign", params.get("sign"));
//		map.put("paramsTest46", "46");
		if(StringUtils.isNotBlank(phone)){
			map.put("phone", phone);
		}
		
		t_score_convert convert = new t_score_convert();
		try {
			convert.description = description;
			convert.ip = ip;
			convert.item_code = item_code;
			convert.order_num = order_num;
			convert.params = param;
			convert.type = type;
			convert.user_id = Integer.parseInt(uid);
			convert.credits = Integer.parseInt(credits);
			convert.actual_price = Integer.parseInt(actual_price);
			convert.face_price = Integer.parseInt(face_price);
			convert.wait_audit = Boolean.parseBoolean(wait_audit);
			
			convert.time = new Date(Long.parseLong(time));
		} catch (Exception e) {
			Map<String,Object> data = new HashMap<String, Object>();
			e.printStackTrace();
			data.put("status", "fail");
			data.put("errorMessage", "信息有误！");
			renderJSON(map);
		}
		
		Map<String,Object> data = Score.deduction(convert,pd);
		
		renderJSON(data);
	}
	
	public static void convert(){
		Map<String,String> pd = params.allSimple();
		Set<String> keys = pd.keySet();
		for(String k : keys){
			Logger.info("====>" + k + "-----" + pd.get(k));
		}
		
		String msg = "fail";
		try {
			msg = Score.convert(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		renderText(msg);
	}
	
	public static void recharge(){
		Map<String,String> pd = params.allSimple();
		Set<String> keys = pd.keySet();
		for(String k : keys){
			Logger.info("====>" + k + "-----" + pd.get(k));
		}
		
		Map<String,Object> data = Score.recharge(params);
		
		renderJSON(data);
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月20日
	 * @description 进入兑吧积分商城
	 * @param dbredirect
	 * @param userId
	 */
	public static void login(String dbredirect,String userId){
		ErrorInfo error = new ErrorInfo();
		long uid =  Security.checkSign(userId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		User user = new User();
		user.id = uid;
		String url = Score.getDuiBaUrl(user,dbredirect, error);
		redirect(url);
	}
}
