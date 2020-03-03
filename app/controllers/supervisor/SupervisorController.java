package controllers.supervisor;

import java.util.ArrayList;
import java.util.List;

import com.shove.JSONUtils;

import business.User;
import constants.Constants;
import controllers.BaseController;
import controllers.TrustFunds;
import controllers.interceptor.SupervisorInterceptor;
import models.t_dict_ad_citys;
import models.t_new_city;
import net.sf.json.JSONArray;
import play.mvc.With;
import utils.ErrorInfo;

/**
 * 后台控制器基类
 * @author lzp
 * @version 6.0
 * @created 2014-7-1
 */
@With({SupervisorInterceptor.class,TrustFunds.class})
public class SupervisorController extends BaseController {
	/**
	 * <p>获取省份下面所有城市</p>
	 * @param provinceId
	 */
	public static void getNewCity(String provinceId) {
		ErrorInfo error = new ErrorInfo();
		List<t_new_city> cityList = new ArrayList<>();
		try {
			cityList = User.queryNewCity(provinceId);
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "获取城市列表失败！";
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		renderJSON(JSONUtils.toJSONString(cityList));
	}
	
	/**
	 * 根据省获得市联动
	 */
	public static void getAdCity(long provinceId){
		List<t_dict_ad_citys> cityList = User.queryCity(provinceId);
		JSONArray json = JSONArray.fromObject(cityList);
		renderJSON(json);
	}
}
