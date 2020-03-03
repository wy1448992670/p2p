package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

@Entity
public class t_enum_map extends Model{
	private static final long serialVersionUID = -3486637946845670898L;

	public Integer enum_type;//枚举类型 enum_type=0时,enum_code为enum_type的外键
	public Integer enum_code;//枚举code
	public String enum_name;//枚举名
	public String description;
	public Boolean is_deleted;//是否有效
	
	@Transient					//map_key	enum_code,enum_name
	transient public static Map<Integer, Map<Integer,t_enum_map>> typeCodeEnumCodeMap = null;
	@Transient
	transient public static Map<Integer, Map<String,t_enum_map>> typeCodeEnumNameMap = null;
	@Transient
	transient public static Map<Integer, List<t_enum_map>> typeCodeEnumList = null;
	
	@Transient
	transient public static Map<String, Map<Integer,t_enum_map>> typeNameEnumCodeMap = null;
	@Transient
	transient public static Map<String, Map<String,t_enum_map>> typeNameEnumNameMap = null;
	@Transient
	transient public static Map<String,List<t_enum_map>> typeNameEnumList = null;
	
	static public void init(){
		List<t_enum_map> list=t_enum_map.findAll();
		typeCodeEnumCodeMap = new HashMap<Integer, Map<Integer,t_enum_map>>();
		typeCodeEnumNameMap = new HashMap<Integer, Map<String,t_enum_map>>();
		typeCodeEnumList = new HashMap<Integer, List<t_enum_map>>();

		typeNameEnumCodeMap = new HashMap<String, Map<Integer,t_enum_map>>();
		typeNameEnumNameMap = new HashMap<String, Map<String,t_enum_map>>();
		typeNameEnumList = new HashMap<String, List<t_enum_map>>();
		
		for(t_enum_map enumObject:list){
			//填充enumMapByKeyCode
			Map<Integer,t_enum_map> enumCodeMap=typeCodeEnumCodeMap.get(enumObject.enum_type);
			if(enumCodeMap==null){
				enumCodeMap=new HashMap<Integer,t_enum_map>();
				typeCodeEnumCodeMap.put(enumObject.enum_type,enumCodeMap);
			}
			enumCodeMap.put(enumObject.enum_code, enumObject);
			
			Map<String,t_enum_map> enumNameMap=typeCodeEnumNameMap.get(enumObject.enum_type);
			if(enumNameMap==null){
				enumNameMap=new HashMap<String,t_enum_map>();
				typeCodeEnumNameMap.put(enumObject.enum_type,enumNameMap);
			}
			enumNameMap.put(enumObject.enum_name, enumObject);
			
			List<t_enum_map> enumList=typeCodeEnumList.get(enumObject.enum_type);
			if(enumList==null){
				enumList=new ArrayList<t_enum_map>();
				typeCodeEnumList.put(enumObject.enum_type,enumList);
			}
			enumList.add(enumObject);
		}
		for(t_enum_map enumObject:list){
			String enumTypeName=getEnumCodeMapByTypeCode(0).get(enumObject.enum_type).enum_name;
			
			//填充enumMapByKeyCode
			Map<Integer,t_enum_map> enumCodeMap=typeNameEnumCodeMap.get(enumTypeName);
			if(enumCodeMap==null){
				enumCodeMap=new HashMap<Integer,t_enum_map>();
				typeNameEnumCodeMap.put(enumTypeName,enumCodeMap);
			}
			enumCodeMap.put(enumObject.enum_code, enumObject);
			
			Map<String,t_enum_map> enumNameMap=typeNameEnumNameMap.get(enumTypeName);
			if(enumNameMap==null){
				enumNameMap=new HashMap<String,t_enum_map>();
				typeNameEnumNameMap.put(enumTypeName,enumNameMap);
			}
			enumNameMap.put(enumObject.enum_name, enumObject);
			
			List<t_enum_map> enumList=typeNameEnumList.get(enumTypeName);
			if(enumList==null){
				enumList=new ArrayList<t_enum_map>();
				typeNameEnumList.put(enumTypeName,enumList);
			}
			enumList.add(enumObject);
		}
	}
	
	static public  Map<Integer,t_enum_map> getEnumCodeMapByTypeCode(Integer typeCode){
		if(typeCodeEnumCodeMap==null){
			init();
		}
		//空处理
		Map<Integer,t_enum_map> resultMap=typeCodeEnumCodeMap.get(typeCode);
		if(resultMap==null){
			resultMap=new HashMap<Integer,t_enum_map>();
			typeCodeEnumCodeMap.put(typeCode, resultMap);
		}
		return resultMap;
	}
	static public  Map<String,t_enum_map> getEnumNameMapByTypeCode(Integer typeCode){
		if(typeCodeEnumNameMap==null){
			init();
		}
		//空处理
		Map<String,t_enum_map> resultMap=typeCodeEnumNameMap.get(typeCode);
		if(resultMap==null){
			resultMap=new HashMap<String,t_enum_map>();
			typeCodeEnumNameMap.put(typeCode, resultMap);
		}
		return resultMap;
	}
	static public  List<t_enum_map> getEnumListByTypeCode(Integer typeCode){
		if(typeCodeEnumList==null){
			init();
		}
		//空处理
		List<t_enum_map> resultList=typeCodeEnumList.get(typeCode);
		if(resultList==null){
			resultList=new ArrayList<t_enum_map>();
			typeCodeEnumList.put(typeCode, resultList);
		}
		return resultList;
	}
	
	
	static public  Map<Integer,t_enum_map> getEnumCodeMapByTypeName(String typeName){
		if(typeNameEnumCodeMap==null){
			init();
		}
		//空处理
		Map<Integer,t_enum_map> resultMap=typeNameEnumCodeMap.get(typeName);
		if(resultMap==null){
			resultMap=new HashMap<Integer,t_enum_map>();
			typeNameEnumCodeMap.put(typeName, resultMap);
		}
		return resultMap;
	}

	static public  Map<String,t_enum_map> getEnumNameMapByTypeName(String typeName){
		if(typeNameEnumNameMap==null){
			init();
		}
		//空处理
		Map<String,t_enum_map> resultMap=typeNameEnumNameMap.get(typeName);
		if(resultMap==null){
			resultMap=new HashMap<String,t_enum_map>();
			typeNameEnumNameMap.put(typeName, resultMap);
		}
		return resultMap;
	}
	
	static public  List<t_enum_map> getEnumListByTypeName(String typeName){
		if(typeNameEnumList==null){
			init();
		}
		//空处理
		List<t_enum_map> resultList=typeNameEnumList.get(typeName);
		if(resultList==null){
			resultList=new ArrayList<t_enum_map>();
			typeNameEnumList.put(typeName, resultList);
		}
		return resultList;
	}
	
}

