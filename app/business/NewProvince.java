package business;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import models.t_new_city;
import models.t_new_province;

public class NewProvince implements Serializable{

	
	
	public static String getProvince(String provinceId){
		String province = t_new_province.find("select t.province from t_new_province t where t.province_id = ? ", provinceId).first();
		return province;
	}
	
	/**
	 * 根据省ID查询该省下所有的市
	 * 
	 * @param provinceId
	 * @return
	 */
	public static List<t_new_city> getCityList(String provinceId){
		return t_new_city.find("select c.city,c.city_id,c.father from t_new_city c where c.father = ?", provinceId).fetch();
	}
	
	public static List<t_new_province> getProvinceList(){
		return t_new_province.findAll();
	}
}
