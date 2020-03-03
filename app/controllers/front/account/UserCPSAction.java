package controllers.front.account;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;

import business.BackstageSet;
import business.User;
import business.UserCpsProfit;
import constants.Constants;
import controllers.BaseController;
import controllers.SubmitRepeat;
import controllers.interceptor.AccountInterceptor;
import play.Logger;
import play.mvc.With;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;

/**
 * 
 * @author cp
 *
 */
@With({ AccountInterceptor.class, SubmitRepeat.class })
public class UserCPSAction extends BaseController {

	/**
	 *  h5页面ajax调用,查询用户推广详情
	 */
	public static String findUserCPSProfit() {
		// 老版app使用 18年4月份后新app不使用
		// TODO Auto-generated method stub
		Map<String, Object> result = new HashMap<String, Object>();

		String userIdSign = request.params.get("id");

		if (StringUtils.isBlank(userIdSign)) {
			result.put("error", "-2");
			result.put("msg", "用户id有误");

		} else {
			ErrorInfo error = new ErrorInfo();
			long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			if (error.code < 0) {
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");

			} else {

				PageBean<Map<String, Object>> page = null;
				try {
					System.out.println("userId:" + userId + " currPage:" + Integer.valueOf(request.params.get("currPage")));
					page = UserCpsProfit.cpsUsersProfit(userId, Integer.valueOf(request.params.get("currPage")), 20);
				} catch (NumberFormatException e) {
					result.put("error", "-2");
					result.put("msg", e.getMessage());
					e.printStackTrace();
					return JSON.toJSONString(result);
				} catch (Exception e) {
					result.put("error", "-2");
					result.put("msg", e.getMessage());
					e.printStackTrace();
					return JSON.toJSONString(result);
				}

				if (page.page != null) {
					for (Map ucp : page.page) {
						ucp.put("name",User.hideString(ucp.get("name")==null?"":ucp.get("name").toString()));
					}
				}
				result.put("error", "-1");
				result.put("msg", "查询成功");
				result.put("page", page);
				result.put("totalNum", page.totalCount);

				Logger.info("分成记录：" + JSON.toJSONString(page, true));
			}
		}

		return JSON.toJSONString(result);
	}
	
	public static String activityRule(){
		
		Map<String, Object> jsonMap = new HashMap<String,Object>();
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		SimpleDateFormat spdformat = new SimpleDateFormat("yyyy年MM月dd日");
		String cpsRelationStartDate = spdformat.format(backstageSet.cpsRelationStartDate);
		DecimalFormat decimalFormat = new java.text.DecimalFormat("#.##");
		String rewardForRate = decimalFormat.format(backstageSet.rewardForRate);
		
		jsonMap.put("cpsRelationStartDate", cpsRelationStartDate);
		jsonMap.put("rewardForRate", rewardForRate);
		
		return JSON.toJSONString(jsonMap);
		
	}
	
}
