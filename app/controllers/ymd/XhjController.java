/******************************************************************
 *
 *    Java Lib For Android, Powered By Shenzhen Jiuzhou.
 *
 *    Copyright (c) 2001-2014 Digital Telemedia Co.,Ltd
 *    http://www.d-telemedia.com/
 *
 *    Package:     controllers.ymd
 *
 *    Filename:    XhjController.java
 *
 *    Description: TODO(用一句话描述该文件做什么)
 *
 *    Copyright:   Copyright (c) 2001-2014
 *
 *    Company:     Digital Telemedia Co.,Ltd
 *
 *    @author:     zj
 *
 *    @version:    1.0.0
 *
 *    Create at:   2018年12月26日 下午7:57:16
 *
 *    Revision:
 *
 *    2018年12月26日 下午7:57:16
 *        - first revision
 *
 *****************************************************************/
package controllers.ymd;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import controllers.BaseController;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import play.Logger;
import utils.xhj.XhjService;

/**
 * @ClassName XhjController
 * @Description 星护甲接口
 * @author zj
 * @Date 2018年12月26日 下午7:57:16
 * @version 1.0.0
 */
public class XhjController extends BaseController {

	/**
	 * @Description 查询个人征信
	 * @param personName 姓名
	 * @param idNumber   身份证卡号
	 * @param mobileNo   手机号
	 * @return 非空 json格式的征信信息 空 表明查询接口失败 可能参数有问题
	 * @author: zj
	 */
	public static String getPersonsCreditInfo(String personName, String idNumber, String mobileNo) {
		Map<String, String> json = XhjService.getPersonsCreditInfo(personName, idNumber, mobileNo);
		JSONObject arrayInfo = JSONObject.fromObject(json);
		JSONObject result = JSONObject.fromObject(arrayInfo.get("result")); 
		
		
		//智多分
		JSONObject contactsInfo =  JSONObject.fromObject(result.get("contactsInfo"));  
		if(!contactsInfo.isEmpty()) {
			String score = contactsInfo.getString("score");
			Logger.info("获取智多分分数: 【%s】" ,score);
			
			//是否存在黑名单
			String directContactsBlacklist = contactsInfo.getString("directContactsBlacklist");//直接联系人黑名单
			Logger.info("直接联系人黑名单： 【%s】" ,directContactsBlacklist);
			//通讯录人数
			String directContacts = contactsInfo.getString("directContacts") ;//直接联系人数
			Logger.info("直接联系人数： 【%s】" ,directContacts);
		}
		
		//不良行为等级
		JSONObject publicSecurityInfo =  JSONObject.fromObject(result.get("publicSecurityInfo")); 
		JSONObject badbehaviorInfo =  JSONObject.fromObject(publicSecurityInfo.get("badbehaviorInfo"));  
		String level = badbehaviorInfo.getString("level");
		Logger.info("不良行为等级： 【%s】" ,level);
		
		//号码归属地
		JSONObject telecomInfo =  JSONObject.fromObject(result.get("telecomInfo"));  
		if(!telecomInfo.isEmpty()) {
			String mobileOwnership = telecomInfo.getString("mobileOwnership");
			Logger.info("号码归属地： 【%s】" ,mobileOwnership);
			//入网时间
			String inTime = telecomInfo.getString("inTime");
			Logger.info("入网时间： 【%s】" ,inTime);
			//常用联系人是否一致
			
			//在网状态
			String onLineStatus = telecomInfo.getString("onLineStatus");
			Logger.info("在网状态： 【%s】" ,onLineStatus);
		}
		
		//姓名身份证号与手机号匹配情况
		JSONObject baseInfo =  JSONObject.fromObject(result.get("baseInfo"));  
		String nameMatchIdNumber = baseInfo.getString("nameMatchIdNumber");
		Logger.info("姓名身份证号与手机号匹配情况： 【%s】" ,nameMatchIdNumber);
		
		//借款机构数
		JSONObject appInfo =  JSONObject.fromObject(result.get("appInfo"));  
		String borrowingAppCount = appInfo.getString("borrowingAppCount");//贷款app数量
		Logger.info("借款机构数： 【%s】" ,borrowingAppCount);
		//理财机构数
		String financialAppCount = appInfo.getString("financialAppCount");//理财app数量
		Logger.info("理财机构数： 【%s】" ,financialAppCount);
		
		//借款金额
		JSONObject borrowingInfo =  JSONObject.fromObject(result.get("borrowingInfo"));  
		BigDecimal amount = BigDecimal.ZERO;
		if(!borrowingInfo.isEmpty()) {
			JSONArray record =  JSONArray.fromObject(borrowingInfo.get("record"));  
			for (int i = 0; i < record.size(); i++) {
				JSONObject recordDetail = JSONObject.fromObject(record.get(i));
//				System.err.println("recordDetail: "+recordDetail+"--------------");
				String amountRange = recordDetail.getString("amount");
//				amount.add(new BigDecimal(recordDetail.getString("amount")));
			}
//			System.err.println(amount+"-------------");
			//逾期金额
			String overdueAmount = borrowingInfo.getString("overdueAmount");
			Logger.info("逾期金额： 【%s】" ,overdueAmount);
		}
		
		
		//逾期天数
		JSONObject overdueInfo =  JSONObject.fromObject(result.get("overdueInfo"));  
		String isOverdue180 = overdueInfo.getString("isOverdue180");
		String isOverdue90 = overdueInfo.getString("isOverdue90");
		Logger.info("是否逾期180天： 【%s】" ,isOverdue180);
		Logger.info("是否逾期90天： 【%s】" ,isOverdue90);
		//居住地址匹配度
		
		//有无涉诉
		return null;
	}

	/**
	 * @Description 休息地验证
	 * @param personName
	 * @param idNumber
	 * @param mobileNo
	 * @param workOrRestAddress 休息地详细地址（推荐的格式为省+市+区+街道或小区楼号，<br>
	 *                          如：浙江省杭州市西湖区古墩路1号。注意，不可出现两个“-”符号）
	 * @return
	 * @author: zj
	 */
	public static String rtAddrVer(String personName, String idNumber, String mobileNo, String workOrRestAddress) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("personName", "汪云");
		map.put("idNumber", "342623199203160126");
		map.put("mobileNo", "18019935601");
		map.put("workOrRestAddress", "上海市浦东新区浦东大道1623弄16号楼3单元307");
		return XhjService.rtAddrVer(personName, idNumber, mobileNo, workOrRestAddress);
	}
	
	/**
	 * @Description 同盾分
	 * @param idNumber
	 * @param mobileNo
	 * @return
	 * @author: zj
	 */
	public static String getTdCommonScore(String idNumber, String mobileNo) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("idNumber", "342623199203160126");
		map.put("mobileNo", "18019935601");
		return XhjService.getTdCommonScore(idNumber, mobileNo);
	}
	
	
	public static void main(String[] args) {
		String json = "{}";
		JSONObject jsonObject = JSONObject.fromObject(json);
		
		System.err.println(jsonObject.isEmpty());
		System.err.println(jsonObject.isNullObject());
		System.err.println();
	}
}
