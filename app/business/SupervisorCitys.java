package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import models.t_supervisor_citys;
import models.t_supervisors;
import models.v_supervisors;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.PageBean;
import constants.SupervisorEvent;

public class SupervisorCitys implements Serializable{
	
	public static PageBean<t_supervisor_citys> getCityList(long supervisorId, ErrorInfo error, PageBean<t_supervisor_citys> pageBean){
		int count = 0;
		 
		List<Object> params = new ArrayList<Object>();
		String condition = "1=1";
		
		condition += " and supervisor_id = ?";
		
		if(!StringUtils.isEmpty(String.valueOf(supervisorId))){
			params.add(supervisorId);
		}
		List<t_supervisor_citys> page = null;
		try {
		
			count = (int) t_supervisor_citys.count(condition, params.toArray());
			page = t_supervisor_citys.find(condition, params.toArray()).fetch(pageBean.currPage, pageBean.pageSize);
			
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		PageBean<t_supervisor_citys> bean = new PageBean<t_supervisor_citys>();
		bean.pageSize = pageBean.pageSize;
		bean.currPage = pageBean.currPage;
		bean.totalCount = count;
		bean.page = page;
		return bean;
	}
	
	
	public static void addSupervisorCitys(ErrorInfo error, t_supervisor_citys citys,long supervisorId){
		error.clear();
		try {
			String cityId = citys.city_id;
			String provinceId= citys.province_id;
			t_supervisor_citys isExsit = t_supervisor_citys.find(" city_id = ? and supervisor_id = ? and province_id = ? ", cityId, supervisorId,provinceId).first();
			
			if(isExsit == null){
				citys.save();
				
			} else {
				error.code = -2;
				error.msg = "该管理员可见地区已存在！";
			} 
			
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

		}
	}
	
	public static void deleteSupervisorCitys(ErrorInfo error, String city, long supervisorId){
		error.clear();
		try {
			t_supervisor_citys.delete("delete from t_supervisor_citys where city_id= ? and supervisor_id = ? ", city, supervisorId);
			
			error.code = 0;
			error.msg = "删除成功";
			
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();
		}
	}
	
 	
	public static void  deleteAllCitys(ErrorInfo error, long supervisorId){
		error.clear();
		//String city_ids = citys.toString();
		try {
			t_supervisor_citys.delete("delete from t_supervisor_citys where supervisor_id = ? ",supervisorId);
			
			error.code = 0;
			error.msg = "批量删除成功";
			
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "批量删除管理员可见地区异常";
			JPA.setRollbackOnly();
		}
	
	} 
}
