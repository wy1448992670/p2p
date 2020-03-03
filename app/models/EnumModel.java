package models;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.alibaba.fastjson.annotation.JSONField;

import constants.ProductEnum;
import play.cache.Cache;
import play.db.jpa.Model;

/**
 * @see controllers.supervisor.systemSettings.supervisorAction.enumModelInit();
 * @author admin
 *
 */
@MappedSuperclass
public abstract class EnumModel extends Model{
	/*
	@Transient
	transient private static Map<String, List<EnumModel>> enumListMap = new HashMap<String, List<EnumModel>>();
	@Transient
	transient private static Map<String, Map<Long,EnumModel>> enumMapMap = new HashMap<String, Map<Long,EnumModel>>();
	*/
	
	public <T extends EnumModel> void init(){
		System.out.println(this.getClass()+".init();");
		String clazzName = this.getClass().getName();

		//List<EnumModel> list=this.findAll();
		List<T> list=null;
		try {
			list = (List<T>)this.getClass().getMethod("findAll").invoke(null);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//enumListMap.put(this.getClass().getName(),list);
		//Cache.delete("EnumModel_enumListMap"+clazzName);
		Cache.set("EnumModel_enumListMap"+clazzName,list);
		Map<Long,EnumModel> map=new HashMap<Long,EnumModel>();
		//enumMapMap.put(this.getClass().getName(),map);
		for(EnumModel enumModel:list){
			map.put(enumModel.id,enumModel);
		}
		//Cache.delete("EnumModel_enumMapMap"+clazzName);
		Cache.set("EnumModel_enumMapMap"+clazzName,map);
	}
	
	@Transient
	@JSONField(serialize = false)
	public <T extends EnumModel> Map<Long,T> fetchEnumMap(){//getEnumMap
		Object enumMapObjcet=Cache.get("EnumModel_enumMapMap"+this.getClass().getName());
		if(enumMapObjcet==null){
			this.init();
			return fetchEnumMap();
		}
		Map<Long, T> enumMap=(Map<Long,T>)enumMapObjcet;
		return enumMap;
		/*
		if(enumMapMap.get(this.getClass().getName())==null||enumMapMap.get(this.getClass().getName()).isEmpty()){
			this.init();
		}
		return (Map<Long, T>) enumMapMap.get(this.getClass().getName());
		*/
	}
	
	@Transient
	@JSONField(serialize = false)
	public <T extends EnumModel> List<T> fetchEnumList(){//getEnumList
		Object enumListObjcet=Cache.get("EnumModel_enumListMap"+this.getClass().getName());
		if(enumListObjcet==null){
			this.init();
			return fetchEnumList();
		}
		List<T> enumList=(List<T>)enumListObjcet;
		return enumList;
		
		/*
		if(enumListMap.get(this.getClass().getName())==null||enumListMap.get(this.getClass().getName()).isEmpty()){
			this.init();
		}
		return (List<T>) enumListMap.get(this.getClass().getName());
		*/
	}
	
	/**
     * 根据id获取对象
	* @param id
	* @return
	*/
	public <T extends EnumModel> T getEnumById(long id){
		return (T)fetchEnumMap().get(id);
	}
	
}