package payment;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import models.t_mmm_data;
import models.t_return_data;
import models.t_sequences;
import play.Logger;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;
import play.mvc.Scope.Params;
import utils.ErrorInfo;
import utils.JPAUtil;
import constants.Constants;
import constants.PayType;

/**
 * 资金托管业务,基类
 * 
 * 		注意：基类中提供的方法不允许修改，如果某些接口有特殊要求，请在自己的子类中重写该方法
 *
 * @author hys
 * @createDate  2015年8月29日 上午10:47:57
 *
 */
public abstract class PaymentBaseService {
	
	private final Gson gson = new Gson();
	
	/**
	 * 生成流水号(最长20位)
	 * @param userId (不能为负，系统行为：0)
	 * @param operation
	 * @return
	 */
	public String createBillNo() {
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");
		t_sequences sequence = new t_sequences();
		sequence.save();		
		return format.format(new Date()) + sequence.id+"" ;
	}
	
	/**
	 * 打印提交日志
	 * @param param 提交参数集合
	 * @param mark 日志头提示
	 * @param payType 接口类型
	 */
	public abstract void printRequestData(Map<String, String> param, String mark, PayType payType);

	/**
	 * 打印回调日志信息
	 * @param paramMap 回调参数集合即params.all();
	 * @param desc 回调日志头信息
	 * @param type 接口类型
	 * @param is_save_log 是否需要数据库日志
	 */
	public abstract void printData(Map<String, String> paramMap, String desc, PayType payType);
	
	/**
	 * 根据流水号查询提交参数
	 * @param orderNum
	 * @return
	 */
	public Map<String, String> queryRequestData(String orderNum, ErrorInfo error){
		error.clear();
		
		t_mmm_data data = null;
		
		try {
			data = t_mmm_data.find("orderNum = ?", orderNum).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("根据流水号查询提交参数时，%s", e.getMessage());
			
			error.code = -1;
			error.msg = "根据流水号查询提交参数失败";
			
			return null;
		}
		
		if(data == null){
			
			error.code = -2;
			error.msg = "根据流水号查询提交参数时传入的流水账单号【"+orderNum+"】有误";
			
			return null;
		}
		
		Map<String, String> map = gson.fromJson(data.data, new TypeToken<Map<String, String>>(){}.getType());
		
		error.code = 1;
		error.msg = "查询流水号账单时信息成功!";
		
		return map;
		
		
	}
	
	/**
	 * 根据流水号查询回调参数
	 */
	public Map<String,String> queryReturnData(String orderNum, ErrorInfo error){
		error.clear();
		
		t_return_data data = null;
		
		try {
			data = t_return_data.find("orderNum = ?", orderNum).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("根据流水号查询回调参数时，%s", e.getMessage());
			
			error.code = -1;
			error.msg = "根据流水号查询回调参数失败";
			
			return null;
		}
		
		if(data == null){
			
			error.code = -2;
			error.msg = "根据流水号查询回调参数时传入的流水账单号有误";
			
			return null;
		}
		
		Map<String, String> map = gson.fromJson(data.data, new TypeToken<Map<String, String>>(){}.getType());
		
		error.code = 1;
		error.msg = "查询流水号账单时信息成功!";
		
		return map;
	}
	
	/**
	 * 获取资金托管接口参数
	 * @param params 
	 * @return
	 */
	public Map<String,String> getRespParams(Params params){
		String reqparams = null;
		try {
		 
			//如果直接使用params.allSimple()则会出现乱码
			reqparams = URLDecoder.decode(URLDecoder.decode(params.urlEncode(),"UTF-8"),"UTF-8");
			
		} catch (UnsupportedEncodingException e1) {
			
			Logger.error("回调UrlDecode时 : %s " ,e1.getMessage());
		}
		
		Map<String,String> paramMap = null;
		if (null != reqparams) {
			paramMap = new HashMap();
			String param[] = reqparams.split("&");
			for (int i = 0; i < param.length; i++) {
				String content = param[i];
				String key = content.substring(0, content.indexOf("="));
				String value = content.substring(content.indexOf("=") + 1,
						content.length());
				try {
					paramMap.put(key, URLDecoder.decode(value,"UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					
					Logger.error("汇付天下回调构造参数UrlDecode时%s", e1.getMessage());
				}
			}
		}
		
		return paramMap;
	}
	
}
