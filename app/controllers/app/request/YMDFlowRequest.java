package controllers.app.request;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.helper.StringUtil;

import com.google.gson.Gson;

import constants.Constants;
import models.core.t_new_product;
import services.ymd.YMDFlowService;
import utils.ErrorInfo;
import utils.JSONUtils;
import utils.NumberUtil;
import utils.Security;
import utils.mjkj.MjkjService;

public class YMDFlowRequest {
	
	//亿美贷APP流程节点
	public static String getFlowNode(Map<String, String> params) throws IOException, CloneNotSupportedException{
		Gson gson=new Gson();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		//验证权限
		String userIdStr = params.get("userId");
		long userId=0;
		userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			return gson.toJson(jsonMap);
		}
		String productIdStr =params.get("productId");
		Long productId=null;
		if(!StringUtil.isBlank(productIdStr) && NumberUtil.isNumericInt(productIdStr)){
			productId=Long.parseLong(productIdStr);
		}else {
			productId=new t_new_product().getEnumByCode("YI").id;//4L 亿美贷product_id,最初的默认产品
		}
		
		/**
		 * forceCreditStatus 是否强制进入[额度申请状态]节点.
		 * 如果:
		 * [额度申请单]不存在,则重新计算一遍用户审核流程.
		 * [额度申请单]存在,且forceCreditStatus==false,且[额度申请状态]是[审核拒绝]|[额度关闭],则重新计算一遍用户审核流程,
		 * [额度申请单]存在,且forceCreditStatus==false,且[额度申请状态]不是[审核拒绝]|[额度关闭],则进入[额度申请状态]节点,
		 * [额度申请单]存在,且forceCreditStatus==true,则进入[额度申请状态]节点,
		 * if(creditApply==null || (creditApply.isClose() &&! forceCreditStatus) ) {
		 * 		重新计算一遍用户审核流程
		 * }else{
		 * 		强制进入[额度申请状态]节点
		 * }
		 */
		String forceCreditStatusStr =params.get("forceCreditStatus");
		boolean forceCreditStatus=false;
		if(!StringUtil.isBlank(forceCreditStatusStr) && NumberUtil.isBoolean(forceCreditStatusStr)){
			forceCreditStatus=Boolean.parseBoolean(forceCreditStatusStr);
		}
		
		Map<String,Object> result=null;
		
		result=YMDFlowService.getFlowNode(userId,productId,forceCreditStatus);
		
		//flowNode==5时,需要给出运营商授权页面的url,在YMDFlowRequest中设置
		if(result.get("flowNode").equals(5)) {
			String themeColor = params.get("themeColor");
			String calbackUrl = "http://" + params.get("request.domain") + ":" + params.get("request.port");
			result.put("mobile_operator_authentication_url", MjkjService.getOperatorH5Url(userId, themeColor, calbackUrl));
		}
		
		jsonMap.put("data",result);
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功！");
		
		return gson.toJson(jsonMap);
	}
	
}
