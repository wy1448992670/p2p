package utils.evi.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.evi.bean.DisplayLinkParam;
import utils.evi.constant.SceneConfig;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/***
 * @Description: 场景式存证_数据字典创建辅助类
 * @Team: 公有云技术支持小组
 * @Author: 天云小生
 * @Date: 2018年01月16日
 */
public class SceneDataDictionaryHelper {
	private static Logger LOG = LoggerFactory.getLogger(SceneDataDictionaryHelper.class);

	/***
	 * 根据实际情况创建场景式存证数据字典(全局范围) 【提示:】场景式存证数据字典只需要创建一次,存在于数据字典中的ID可永久使用
	 * 
	 * @param projectId
	 * @param projectSecret
	 */
	public static void createSceneDataDictionary() {
		
		/* - - - - - - 定义业务凭证（名称）- - - - - - - - -   */
		// 这里以[房屋租赁行业]对应的[所属行业类型ID] 9c06e369-6600-4b2d-98cb-80ebae3088d9 进行后续操作
		// 所属行业类型ID-businessTempletId
		String businessTempletId = createIndustryType();
		
		/* - - - - - - 定义业务凭证中某一证据点名称 - - - - - - - - -   */
		// 这里以[房屋租赁合同签署]对应的[业务凭证（名称）ID] 598c1d9e-b6c7-4d0e-a1df-1b122efe6788 进行后续操作
		// 业务凭证（名称）ID-sceneTempletId
		String sceneTempletId = createSceneType(businessTempletId);
		
		/* - - - - - - 定义业务凭证中某一证据点的字段属性(参数名称与显示名称建立关联) - - - - - - - - -   */
		// 这里以[合同签署人信息]对应的[业务凭证中某一证据点名称ID] bbf273fa-b0c5-4b20-83b1-0deffda664f6 进行后续操作
		// 业务凭证中某一证据点名称ID-segmentTempletId
		String segmentTempletId = createSegmentType(sceneTempletId);
		createSegmentPropType(segmentTempletId);
	}

	/***
	 * 定义所属行业类型
	 */
	public static String createIndustryType() {

		// 行业名称列表(根据实际情况进行增减或修改,此处仅以"房屋租赁行业"行业为例)
		ArrayList<String> industries = new ArrayList<String>();
		industries.add("金融行业-P2P信贷");// 如:金融行业-P2P信贷,医药卫生

		// 请求参数-JSON字符串
		JSONObject param_json = new JSONObject();
		param_json.put("name", JSONArray.fromObject(industries));
		System.out.println("请求参数:" + param_json);

		// 请求签名值
		String signature = DigestHelper.getSignature(param_json.toString(), SceneConfig.PROJECT_SECRET,
				SceneConfig.ALGORITHM, SceneConfig.ENCODING);
		//System.out.println("请求签名值 = " + signature);

		// HTTP请求内容类型
		String ContentType = "application/json";

		// 设置HTTP请求头信息
		LinkedHashMap<String, String> headers = HttpHelper.getPOSTHeaders(SceneConfig.PROJECT_ID, signature,
				SceneConfig.ALGORITHM, ContentType, SceneConfig.ENCODING);

		// 向指定URL发送POST方法的请求
		JSONObject result = HttpHelper.sendPOST(SceneConfig.HTTP_BUS_TYPE_OFFICIAL_APIURL, param_json.toString(),
				headers, SceneConfig.ENCODING);
		
		System.out.println("[定义所属行业类型]接口返回json数据:" + result);
		// 将信息保存到日志文件
		LOG.info("[定义所属行业类型]接口返回json数据:" + result);
		return (String) result.getJSONObject("result").keys().next();
	}

	/***
	 * 定义业务凭证（名称）(如：房屋租赁合同签署)
	 * 
	 * @param businessTempletId
	 *            所属行业类型ID
	 */
	public static String createSceneType(String businessTempletId) {

		// 业务凭证（名称）列表(根据实际情况进行增减或修改,此处仅以"房屋租赁合同签署"为例)
		ArrayList<String> scenes = new ArrayList<String>();
		scenes.add("借款协议签署");

		// 请求参数-JSON字符串
		JSONObject param_json = new JSONObject();
		// businessTempletId对应的是[定义所属行业类型]时获取的[所属行业类型ID]
		param_json.put("businessTempletId", businessTempletId);
		param_json.put("name", JSONArray.fromObject(scenes));
		System.out.println("请求参数:" + param_json);

		// 请求签名值
		String signature = DigestHelper.getSignature(param_json.toString(), SceneConfig.PROJECT_SECRET,
				SceneConfig.ALGORITHM, SceneConfig.ENCODING);
		//System.out.println("请求签署值 = " + signature);

		// HTTP请求内容类型
		String ContentType = "application/json";

		// 设置HTTP请求头信息
		LinkedHashMap<String, String> headers = HttpHelper.getPOSTHeaders(SceneConfig.PROJECT_ID, signature,
				SceneConfig.ALGORITHM, ContentType, SceneConfig.ENCODING);
		// 向指定URL发送POST方法的请求
		JSONObject result = HttpHelper.sendPOST(SceneConfig.HTTP_SCENE_TYPE_OFFICIAL_APIURL, param_json.toString(),
				headers, SceneConfig.ENCODING);

		System.out.println("[定义业务凭证（名称）]接口返回json数据:" + result);
		// 将信息保存到日志文件
		LOG.info("[定义业务凭证（名称）]接口返回json数据:" + result);
		
		return (String) result.getJSONObject("result").keys().next();
	}
	
	/***
	 * 定义业务凭证中某一证据点名称(如：合同签署人信息)
	 * 
	 * @param sceneTempletId
	 *            业务凭证（名称）ID
	 */
	public static String createSegmentType(String sceneTempletId) {

		// 存证场景环节类型列表(根据实际情况进行增减或修改,此处仅以"合同签署人信息"为例)
		ArrayList<String> segments = new ArrayList<String>();
		segments.add("合同签署人信息");

		// 请求参数-JSON字符串
		JSONObject param_json = new JSONObject();
		// sceneTempletId对应的是[定义业务凭证（名称）]时获取的[业务凭证（名称）ID]
		param_json.put("sceneTempletId", sceneTempletId);
		param_json.put("name", JSONArray.fromObject(segments));
		System.out.println("请求参数:" + param_json);

		// 请求签名值
		String signature = DigestHelper.getSignature(param_json.toString(), SceneConfig.PROJECT_SECRET,
				SceneConfig.ALGORITHM, SceneConfig.ENCODING);
		//System.out.println("请求签署值 = " + signature);

		// HTTP请求内容类型
		String ContentType = "application/json";

		// 设置HTTP请求头信息
		LinkedHashMap<String, String> headers = HttpHelper.getPOSTHeaders(SceneConfig.PROJECT_ID, signature,
				SceneConfig.ALGORITHM, ContentType, SceneConfig.ENCODING);
		// 向指定URL发送POST方法的请求
		JSONObject result = HttpHelper.sendPOST(SceneConfig.HTTP_SEG_TYPE_OFFICIAL_APIURL, param_json.toString(),
				headers, SceneConfig.ENCODING);

		System.out.println("[定义业务凭证中某一证据点名称]接口返回json数据:" + result);
		// 将信息保存到日志文件
		LOG.info("[定义业务凭证中某一证据点名称]接口返回json数据:" + result);
		return (String) result.getJSONObject("result").keys().next();
	}
	
	/***
	 * 定义业务凭证中某一证据点的字段属性(如：姓名-name) 设置显示名称与参数名称的对应关系
	 * 
	 * @param segmentTempletId
	 *            业务凭证中某一证据点名称ID
	 */
	public static void createSegmentPropType(String segmentTempletId) {

		// 业务凭证中某一证据点的字段属性列表(根据实际情况进行增减或修改,此处仅以"姓名-realName|用户名-userName"为例)		
		
		DisplayLinkParam displayLinkParamRealName1 = new DisplayLinkParam();
		displayLinkParamRealName1.setDisplayName("甲方签署人");
		displayLinkParamRealName1.setParamName("realName_1");

		DisplayLinkParam displayLinkParamUserName1 = new DisplayLinkParam();
		// 用户在平台系统注册时的用户名
		displayLinkParamUserName1.setDisplayName("甲方签署人的用户名");
		displayLinkParamUserName1.setParamName("userName_1");
		
		DisplayLinkParam displayLinkParamRealName2 = new DisplayLinkParam();
		displayLinkParamRealName2.setDisplayName("乙方签署人");
		displayLinkParamRealName2.setParamName("realName_2");

		DisplayLinkParam displayLinkParamUserName2 = new DisplayLinkParam();
		// 用户在平台系统注册时的用户名
		displayLinkParamUserName2.setDisplayName("乙方签署人的用户名");
		displayLinkParamUserName2.setParamName("userName_2");

		ArrayList<String> segmentProp = new ArrayList<String>();
		segmentProp.add(JSONObject.fromObject(displayLinkParamRealName1).toString());
		segmentProp.add(JSONObject.fromObject(displayLinkParamUserName1).toString());
		segmentProp.add(JSONObject.fromObject(displayLinkParamRealName2).toString());
		segmentProp.add(JSONObject.fromObject(displayLinkParamUserName2).toString());

		// 请求参数-JSON字符串
		JSONObject param_json = new JSONObject();
		// segmentTempletId对应的是[定义业务凭证中某一证据点名称]时获取的[定义业务凭证中某一证据点名称ID]
		param_json.put("segmentTempletId", segmentTempletId);
		// 业务凭证中某一证据点字段属性列表
		param_json.put("properties", JSONArray.fromObject(segmentProp));
		System.out.println("请求参数:" + param_json);

		// 请求签名值
		String signature = DigestHelper.getSignature(param_json.toString(), SceneConfig.PROJECT_SECRET,
				SceneConfig.ALGORITHM, SceneConfig.ENCODING);
		//System.out.println("请求签署值 = " + signature);

		// HTTP请求内容类型
		String ContentType = "application/json";

		// 设置HTTP请求头信息
		LinkedHashMap<String, String> headers = HttpHelper.getPOSTHeaders(SceneConfig.PROJECT_ID, signature,
				SceneConfig.ALGORITHM, ContentType, SceneConfig.ENCODING);
		// 向指定URL发送POST方法的请求
		JSONObject result = HttpHelper.sendPOST(SceneConfig.HTTP_SEGPROP_OFFICIAL_APIURL, param_json.toString(),
				headers, SceneConfig.ENCODING);

		System.out.println("[定义业务凭证中某一证据点的字段属性]接口返回json数据:" + result);
		// 将信息保存到日志文件
		LOG.info("[定义业务凭证中某一证据点的字段属性]接口返回json数据:" + result);
	}

}
