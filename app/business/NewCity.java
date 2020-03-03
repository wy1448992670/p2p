package business;

import java.io.Serializable;
 
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import models.t_activity_center;
import models.t_new_area;
import models.t_new_city;
import models.t_new_province;

public class NewCity implements Serializable{
 
	public static String getCity(String cityId){
		return t_new_city.find("select city from t_new_city where city_id = ? ", cityId).first();
	}
	
	
	public static List<t_new_city> getCityList(String provinceId){
		List<t_new_city> list = new ArrayList<t_new_city>();
		
		try {
			if(StringUtils.isNotBlank(provinceId)){
				list = t_new_city.find(" father = ? ", provinceId).fetch();
			} 
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询市出错！", e);
		}
		
		return list;
	}
	
	public static List<t_new_area> getAreaList(String city_id){
		List<t_new_area> area = new ArrayList<t_new_area>();
		try {
			if(StringUtils.isNotBlank(city_id)){
				area = t_new_area.find(" t.father = ?", city_id).fetch();
			} 
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("setAreaList时，根据city_id查询地区时："+e.getMessage()); 
			return null;
		}
		return area;
	}
	
}
