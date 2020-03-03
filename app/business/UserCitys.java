package business;

import java.io.Serializable;

import play.Logger;
import models.t_new_city;
import models.t_new_province;
import models.t_user_city;
import utils.ErrorInfo;

public class UserCitys implements Serializable{
	
	public static void addUserCity(ErrorInfo error, t_user_city city){
		String provinceId = city.province_id;
		String province = NewProvince.getProvince(provinceId);	//获取省名称
		String city1 = NewCity.getCity(city.city_id);	//获取市名称
		Logger.info(province + ", " + city1);
		city.city = city1;
		city.province = province;
		try {
			city.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			return;
		}
		
	}
}
