package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import models.t_dict_banks_data;
import models.t_dict_city;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.JPAUtil;
import constants.SQLTempletes;

/**
 * 银行支行业务类
 * @author yangxuan
 * @date 2015年4月24日 下午4:39:50
 */
public class DictBanksDate  implements Serializable{
	
	/**
	 * 银行code-name Table
	 */
	public static HashMap<String, String> bankCodeNameTable = null;
	
	/**
	 * 省份code-name Table
	 */
	public static HashMap<String, String> provinceCodeNameTable = null;
	
	/**
	 * 普通银行数组.
	 */
	private static String[] normalBank = null;
	
	/**
	 * 所有城市集合
	 */
	public static List<t_dict_city> citys = null;
	
	/**
	 * 常见银行字符串,通过约定配置. eg: xx银行,xx银行,xx银行
	 */
	private static String normalBankValue = Play.configuration.getProperty("bank_list");
	
	static{
		if(bankCodeNameTable == null){
			Logger.debug("初始化银行code-name数据");
			initBankCode2Name();
		}
		if(provinceCodeNameTable == null){
			Logger.debug("初始化省份code-name数据");
			initprovinceCode2Name();
		}
		if(normalBank == null){
			Logger.debug("unsupport init normalBank arrays.please TODO.");
		}
		
		citys = t_dict_city.findAll();
	}

	/**
	 * 是否为常用银行
	 * @param bankName
	 * @return
	 */
	private static boolean isNormalBank(String bankName){
		if(normalBankValue == null){
			return true;
		}
		
		if(normalBankValue.contains(bankName)){
			return true;
		}
		return false;
	}
	
	/**
	 * 初始化银行code-name
	 * @param error
	 */
	public static void initBankCode2Name(){
		bankCodeNameTable = new LinkedHashMap();
		String sql = null;
		List list = null;
		try{
		sql = "select bank_code,bank_name from "+SQLTempletes.TABLE_NAME+"_dict_banks_col order by id";
		list = JPA.em().createNativeQuery(sql).getResultList();
		for(Object obj : list){
			
			Object[] temp = (Object[]) obj;
			//判断是否为常见银行,如果是,则保存
			if(isNormalBank(temp[1]+"")){
				bankCodeNameTable.put(temp[0]+"", temp[1]+"");
			}
		}
		}catch(Exception e){
			
			Logger.error("初始化银行code-name时:%s", e.getMessage());
			JPA.setRollbackOnly();
		}
	}
	
	/**
	 * 初始化银行code-name
	 * @param error
	 */
	public static void initprovinceCode2Name(){
		provinceCodeNameTable = new HashMap<String, String>();
		String sql = null;
		List list = null;
		try{
		sql = "select code,name from "+SQLTempletes.TABLE_NAME+"_dict_province order by id";
		list = JPA.em().createNativeQuery(sql).getResultList();
		for(Object obj : list){
			Object[] temp = (Object[]) obj;
			provinceCodeNameTable.put(temp[0]+"", temp[1]+"");
		}
		}catch(Exception e){
			
			Logger.error("初始化省份code-name时:%s", e.getMessage());
			JPA.setRollbackOnly();
		}
	}
	
	public static void initNormalBank(){
		String bankList = Play.configuration.getProperty("bank_list");
	
		if(bankList != null){
			normalBank = bankList.split(","); 
		}
	}
	 
	/**
	 * 通过银行code,省份code获取城市code-name
	 * @param bankCode
	 * @param provinceCode
	 * @param error
	 * @return
	 */
	public static Map<String,String> queryCityCode2NameByProvinceCode(int provinceCode,ErrorInfo error){
		error.clear();
		Map<String,String> cityMaps  = new HashMap<String, String>();
		String sql = null;
		List list = null;
		try{
			sql = "select code,name from "+SQLTempletes.TABLE_NAME+"_dict_city where  province_code=? order by id";
			list = JPA.em().createNativeQuery(sql).setParameter(1, provinceCode).getResultList();
		for(Object obj : list){
			Object[] temp = (Object[]) obj;
			cityMaps.put(temp[0]+"", temp[1]+"");
		}
		}catch(Exception e){
			
			Logger.error("通过省份code查询城市code-name时:%s", e.getMessage());
			JPA.setRollbackOnly();
		}
		
		return cityMaps;
	}
	
	/**
	 * 条件搜索银行支行code-name
	 * @return
	 */
	public static Map<String,String> queryBankCode2NameByCondition(Map<String,Object> condition,ErrorInfo error){
		error.clear();
		long start = System.currentTimeMillis();
		Map<String,String> banks = new HashMap<String, String>();
		try{
			int cityCode = (Integer) condition.get("cityCode");
			int bankCode = (Integer) condition.get("bankCode");
			String searchValue = (String) condition.get("searchValue");
			StringBuffer buffer = new StringBuffer();
			List<Object> params = new ArrayList<Object>();
			buffer.append(" select bank_number,bank_name from t_dict_banks_data where 1=1 ");
			if(cityCode !=0){
				buffer.append(" and city_code=? ");
				params.add(cityCode);
			}
			if(bankCode!=0){
				buffer.append(" and bank_code=? ");
				params.add(bankCode);
			}
			if(StringUtils.isNotEmpty(searchValue)){
				buffer.append(" and bank_name like ? ");
				params.add("%"+ searchValue + "%");
			}
			Query query = JPA.em().createNativeQuery(buffer.toString());
			int paramsSize = params.size();
			for(int i = 0; i< paramsSize ; i++){
				query.setParameter(i+1, params.get(i));
			}
			
			List list = query.getResultList();
			for(Object obj : list){
				Object[] temp = (Object[]) obj;
				banks.put(temp[0]+"", temp[1]+"");
			}
		}catch(Exception e){
			
			error.code = -1;
			error.msg = "查询银行支行时异常";
			Logger.error("查询银行支行时:%s", e.getMessage());
			JPA.setRollbackOnly();
		}
		long end = System.currentTimeMillis();
		Logger.debug("查询支付银行支行耗时:%s ms", (end-start));
		return banks;
	}
	
	/**
	 * 条件搜索银行支行
	 * @param params
	 * @param error
	 * @return
	 */
	public static List<t_dict_banks_data> queryBankDate(Map<String,Object> params,ErrorInfo error){
		error.clear();
		Logger.debug("查询银行支行搜索条件:%s", params.toString());
		int bank_code = (Integer) params.get("bank_code");
		int city_code = (Integer) params.get("city_code");
		List<t_dict_banks_data> list  = null;
		try{
			
			list = t_dict_banks_data.find("bank_code = ? and city_code=?", bank_code,city_code).fetch();
	
		}catch(Exception e){
			
			error.code = -1;
			error.msg = "查询银行支行时异常";
			Logger.error("查询银行支行时:%s", e.getMessage());
			JPA.setRollbackOnly();
			return null;
		}
		return list;
	}
	
	/**
	 * 根据code查询银行名称
	 * @param error
	 */
	public static String queryBankByCode(int bankCode){
		String sql = "select bank_name from "+SQLTempletes.TABLE_NAME+"_dict_banks_col where bank_code = ?";
		List list = null;
		try{
			list = JPA.em().createNativeQuery(sql).setParameter(1, bankCode).getResultList();
		}catch(Exception e){
			Logger.error("查询银行名称时:%s", e.getMessage());
		}
		
		if(list != null && list.size() > 0){
			return list.get(0).toString();
		}
		
		return "";
	}
	
	/**
	 * 根据code查询省名称
	 * @param error
	 */
	public static String queryProvinceByCode(int cityCode){
		String sql = "select name from "+SQLTempletes.TABLE_NAME+"_dict_province where code = ?";
		List list = null;
		try{
			list = JPA.em().createNativeQuery(sql).setParameter(1, cityCode).getResultList();
		}catch(Exception e){
			Logger.error("查询省名称时:%s", e.getMessage());
		}
		
		if(list != null && list.size() > 0){
			return list.get(0).toString();
		}
		
		return "";
	}
	/**
	 * 根据code查询市名称
	 * @param error
	 */
	public static String queryCityByCode(int cityCode){
		String sql = "select name from "+SQLTempletes.TABLE_NAME+"_dict_city where code = ?";
		List list = null;
		try{
			list = JPA.em().createNativeQuery(sql).setParameter(1, cityCode).getResultList();
		}catch(Exception e){
			Logger.error("查询市名称时:%s", e.getMessage());
		}
		
		if(list != null && list.size() > 0){
			return list.get(0).toString();
		}
		
		return "";
	}

	/**
	 * 根据code查询省名称 (地理位置省份信息表)
	 * 
	 * @param error
	 */
	public static String queryAdProvinceByCode(int cityCode){
		// String sql = "select name from "+SQLTempletes.TABLE_NAME+"_dict_province where code = ?";
		String sql = "select name from " + SQLTempletes.TABLE_NAME + "_dict_ad_provinces where id = ?";
		List list = null;
		try{
			list = JPA.em().createNativeQuery(sql).setParameter(1, cityCode).getResultList();
		}catch(Exception e){
			Logger.error("查询省名称时:%s", e.getMessage());
		}
		
		if(list != null && list.size() > 0){
			return list.get(0).toString();
		}
		
		return "";
	}
	
	/**
	 * 根据code查询市名称 (地理位置城市信息表)
	 * 
	 * @param error
	 */
	public static String queryAdCityByCode(int cityCode){
		// String sql = "select name from "+SQLTempletes.TABLE_NAME+"_dict_city where code = ?";
		String sql = "select name from " + SQLTempletes.TABLE_NAME + "_dict_ad_citys where id = ?";
		List list = null;
		try{
			list = JPA.em().createNativeQuery(sql).setParameter(1, cityCode).getResultList();
		}catch(Exception e){
			Logger.error("查询市名称时:%s", e.getMessage());
		}
		
		if(list != null && list.size() > 0){
			return list.get(0).toString();
		}
		
		return "";
	}
	
	public int id;
	private int _id;
	public int bank_number;  //银行的联行号
	private int _bank_number;
	public int bank_code;  //支行号前3位数字，代表该支行所属的母行分类
	private int _bank_code;
	public String bank_name;  //银行名称
	private String _bank_name;
	public int county_code;  //县城code
	private int _county_code;
	public String county_name;  //县城名称
	private String _county_name;
	public int city_code;  //市 code
	private int _city_code;
	public String city_name;  //市 name
	private String _city_name;
	public int province_code;  //省份 code
	private int _province_code;
	public String province_name;  //省份 name
	private String _province_name;
	
	public int getId() {
		return this._id;
	}
	public void setId(int id) {
		this._id = id;
	}
	public int getBank_number() {
		return this._bank_number;
	}
	public void setBank_number(int bank_number) {
		this._bank_number = bank_number;
	}
	public int getBank_code() {
		return this._bank_code;
	}
	public void setBank_code(int bank_code) {
		this._bank_code = bank_code;
	}
	public String getBank_name() {
		return this._bank_name;
	}
	public void setBank_name(String bank_name) {
		this._bank_name = bank_name;
	}
	public int getCounty_code() {
		return this._county_code;
	}
	public void setCounty_code(int county_code) {
		this._county_code = county_code;
	}
	public String getCounty_name() {
		return this._county_name;
	}
	public void setCounty_name(String county_name) {
		this._county_name = county_name;
	}
	public int getCity_code() {
		return this._city_code;
	}
	public void setCity_code(int city_code) {
		this._city_code = city_code;
	}
	public String getCity_name() {
		return this._city_name;
	}
	public void setCity_name(String city_name) {
		this._city_name = city_name;
	}
	public int getProvince_code() {
		return this._province_code;
	}
	public void setProvince_code(int province_code) {
		this._province_code = province_code;
	}
	public String getProvince_name() {
		return this._province_name;
	}
	public void setProvince_name(String province_name) {
		this._province_name = province_name;
	}
	
	
	public static List<Map<String, Object>> findBankLimit(){
		String listSql = "select bank_name, bank_code,pay_limit,day_limit from t_dict_banks_col";
		List<Map<String, Object>> result = new ArrayList<>();
		List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), listSql);
		for(Map<String, Object> map : list) {
			//判断是否为常见银行,如果是,则保存
			if(isNormalBank((String) map.get("bank_name"))){
				result.add(map);
			}
		}
		return result;
	}
	
	public static void updateBankLimit(int bank_code, String pay_limit, String day_limit) {
		String sql = "update t_dict_banks_col set pay_limit = ?, day_limit = ? where bank_code = ?";
		JPAUtil.executeUpdate(new ErrorInfo(), sql, pay_limit, day_limit, bank_code);
	}
}
