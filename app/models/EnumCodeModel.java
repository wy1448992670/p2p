package models;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.alibaba.fastjson.annotation.JSONField;

import play.cache.Cache;

@MappedSuperclass
public abstract class EnumCodeModel extends EnumModel{
	public String code_name;//指代Enum里的对象引用名
	
	public <T extends EnumModel> void init(){
		super.init();
		
		Map<String,EnumCodeModel> map=new HashMap<String,EnumCodeModel>();
		for(EnumModel enumModel:fetchEnumList()){
			map.put(((EnumCodeModel)enumModel).code_name,(EnumCodeModel) enumModel);
		}
		Cache.delete("EnumModel_enumCodeMapMap"+this.getClass().getName());
		Cache.set("EnumModel_enumCodeMapMap"+this.getClass().getName(),map);
	}
	
	@Transient
	@JSONField(serialize = false)
	public <T extends EnumCodeModel> Map<String,T> fetchEnumCodeMap(){//getEnumCodeMap
		Object enumMapObjcet=Cache.get("EnumModel_enumCodeMapMap"+this.getClass().getName());
		if(enumMapObjcet==null){
			this.init();
			return fetchEnumCodeMap();
		}
		Map<String, T> enumMap=(Map<String,T>)enumMapObjcet;
		return enumMap;
	}
	
	/**
     * 根据id获取对象
	* @param id
	* @return
	*/
	public <T extends EnumCodeModel> T getEnumByCode(String codeName){
		return (T)fetchEnumCodeMap().get(codeName);
	}
	
}