package utils.evi.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.evi.bean.CertificateBean;
import utils.evi.bean.IdsBean;
import utils.evi.constant.SceneConfig;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/***
 * @Description: 场景式存证_辅助类
 * @Team: 公有云技术支持小组
 * @Author: 天云小生
 * @Date: 2018年01月11日
 */
public class SceneHelper {
	private static Logger LOG = LoggerFactory.getLogger(SceneHelper.class);

	/***
	 * 创建证据链:将已创建的存证环节串联起来,形成证据链 (此处以原文存证和签署记录ID为例,实际对接时请替换成动态传值)
	 * 
	 * @param sceneTemplateId
	 *            业务凭证（名称）ID
	 * @param segment_evId
	 *            业务凭证中某一证据点名称ID
	 */
	public static String createChainOfEvidence(String sceneTemplateId,String sceneName) {

		// 请求参数-JSON字符串
		JSONObject param_json = new JSONObject();
		param_json.put("sceneName", sceneName);
		param_json.put("sceneTemplateId", sceneTemplateId);
		param_json.put("linkIds", null);
		System.out.println("请求参数:" + param_json);
		// 请求签名值
		String signature = DigestHelper.getSignature(param_json.toString(), SceneConfig.PROJECT_SECRET,
				SceneConfig.ALGORITHM, SceneConfig.ENCODING);
		// System.out.println("请求签署值 = " + signature);

		// HTTP请求内容类型
		String ContentType = "application/json";

		// 设置HTTP请求头信息
		LinkedHashMap<String, String> headers = HttpHelper.getPOSTHeaders(SceneConfig.PROJECT_ID, signature,
				SceneConfig.ALGORITHM, ContentType, SceneConfig.ENCODING);
		// 向指定URL发送POST方法的请求
		JSONObject result = HttpHelper.sendPOST(SceneConfig.HTTP_VOUCHER_OFFICIAL_APIURL, param_json.toString(),
				headers, SceneConfig.ENCODING);
		// 场景式存证编号
		String scene_evId = null;
		int errCode = result.getInt("errCode");
		if (0 == errCode) {
			// 场景式存证编号
			scene_evId = result.getString("evid");
			System.out.println("场景式存证编号 = " + scene_evId);
			LOG.info("场景式存证编号 = " + scene_evId);
		} else {
			System.out.println("创建证据链,获取场景式存证编号异常：errCode = " + errCode + " msg = " + result.get("msg"));

		}
		return scene_evId;
	}

	/***
	 * 追加证据:向已存在的证据链中追加新证据
	 * 
	 * @param scene_evId
	 *            业务凭证（名称）ID
	 * @param segment_evId
	 *            业务凭证中某一证据点名称ID
	 */
	public static void appendEvidence(String scene_evId, String segment_evId, List<String> sids) {

		// 创建原文存证（基础版或高级版）证据点时返回的evid(可关联多个证据点ID)
		IdsBean ids0 = new IdsBean();
		ids0.setType("0");
		// 创建存证环节时返回的存证编号(请根据实际情况进行动态传值)
		ids0.setValue(segment_evId);

		/*
		 * // 电子签署SDK中签署接口返回的签署记录ID(可关联多个签署记录ID) //
		 * 请将与本签署文档有关的签署记录ID都进行关联,否则存证证明页面将无法完整显示签署记录 IdsBean signServiceId_1 = new
		 * IdsBean(); signServiceId_1.setType("1"); //
		 * 电子签署SDK中签署接口返回的签署记录ID(请根据实际情况进行动态传值)
		 * signServiceId_1.setValue("971216786840129541");
		 * 
		 * IdsBean signServiceId_2 = new IdsBean(); signServiceId_2.setType("1"); //
		 * 电子签署SDK中签署接口返回的签署记录ID(请根据实际情况进行动态传值)
		 * signServiceId_2.setValue("971216794868027393");
		 */

		// 时间戳服务接口中返回的时间戳数据记录ID(如果未使用e签宝的时间戳服务该参数可以不传递)
		// (可关联多个时间戳数据记录ID)
		// IdsBean ids2 = new IdsBean();
		// ids2.setType("2");
		// 时间戳服务接口返回的时间戳数据记录ID(请根据实际情况进行动态传值,如未使用该服务可以不传值)
		// ids2.setValue("129ab3d5-d4f9-4880-9f70-0c6767af6003");

		// 实名认证服务接口中返回的实名认证请求ID(如果未使用e签宝的实名认证服务该参数可以不传递)
		// (可关联多个实名认证请求ID)
		// IdsBean ids3 = new IdsBean();
		// ids3.setType("3");
		// 实名认证服务接口中返回的实名认证请求ID(请根据实际情况进行动态传值,如未使用该服务可以不传值)
		// ids3.setValue("cbf52ea8-8055-4fbd-b362-92949ca294cb");

		// 存证环节ID列表-证据链(根据实际情况进行填写)
		ArrayList<String> linkIds = new ArrayList<String>();
		linkIds.add(JSONObject.fromObject(ids0).toString());
		for (String sid : sids) {
			IdsBean signServiceId = new IdsBean();
			signServiceId.setType("1");
			signServiceId.setValue(sid);
			linkIds.add(JSONObject.fromObject(signServiceId).toString());
		}
		// linkIds.add(JSONObject.fromObject(ids2).toString());
		// linkIds.add(JSONObject.fromObject(ids3).toString());

		// 请求参数-JSON字符串
		JSONObject param_json = new JSONObject();
		param_json.put("evid", scene_evId);
		param_json.put("linkIds", JSONArray.fromObject(linkIds));
		System.out.println("请求参数:" + param_json);
		// 请求签名值
		String signature = DigestHelper.getSignature(param_json.toString(), SceneConfig.PROJECT_SECRET,
				SceneConfig.ALGORITHM, SceneConfig.ENCODING);
		// System.out.println("请求签署值 = " + signature);

		// HTTP请求内容类型
		String ContentType = "application/json";

		// 设置HTTP请求头信息
		LinkedHashMap<String, String> headers = HttpHelper.getPOSTHeaders(SceneConfig.PROJECT_ID, signature,
				SceneConfig.ALGORITHM, ContentType, SceneConfig.ENCODING);
		// 向指定URL发送POST方法的请求
		JSONObject result = HttpHelper.sendPOST(SceneConfig.HTTP_VOUCHER_APPEND_OFFICIAL_APIURL, param_json.toString(),
				headers, SceneConfig.ENCODING);

		int errCode = result.getInt("errCode");
		if (0 == errCode) {
			// 追加证据是否成功标识
			boolean isAppendSuccess = result.getBoolean("success");
			System.out.println("追加证据点成功状态 = " + isAppendSuccess);
			LOG.info("追加证据点成功状态 = " + isAppendSuccess);
		} else {
			System.out.println("追加证据点异常：errCode = " + errCode + " msg = " + result.get("msg"));

		}
	}

	/***
	 * 创建原文存证基础版证据点, 原文基础版存证成功后将原文同时推送到e签宝服务端和司法鉴定中心,
	 * 不会推送到公证处(请询问e签宝对接人员确认贵司所购买的存证类型)
	 * 
	 * @param filePath
	 * @param segmentTempletId
	 *            业务凭证中某一证据点名称ID
	 */
	public static JSONObject createSegmentOriginal_Standard(String filePath, String segmentTempletId, String borrName,
			String borrAccount, String investName, String investAccount) {
		// 设置业务数据(与本文档有关的签署人信息)
		JSONObject segmentDataJSON = new JSONObject();
		segmentDataJSON.put("realName_1", borrName);
		segmentDataJSON.put("userName_1", borrAccount);
		segmentDataJSON.put("realName_2", investName);
		segmentDataJSON.put("userName_2", investAccount);
		String segmentData = segmentDataJSON.toString();

		Map<String, String> fileInfo = FileHelper.getFileInfo(filePath);

		JSONObject contentJSON = new JSONObject();
		// 待保全文档名称（文件名中不允许含有? * : " < > \ / | [ ] 【】）
		contentJSON.put("contentDescription", fileInfo.get("FileName"));
		// 待保全文档大小，单位：字节
		contentJSON.put("contentLength", fileInfo.get("FileLength"));
		// 待保全文档内容字节流MD5的Base64编码值
		contentJSON.put("contentBase64Md5", DigestHelper.getContentMD5(filePath));

		JSONObject original_Std_JSON = new JSONObject();
		original_Std_JSON.put("segmentTempletId", segmentTempletId);
		// segmentData为Json形式的字符串,并非Json格式数据,如:"segmentData":"{\"name\":\"张三\",\"address\":\"位于浙江省的采购方\"}"
		original_Std_JSON.put("segmentData", "\"" + segmentData + "\"");
		original_Std_JSON.put("content", contentJSON);

		System.out.println("请求参数:" + original_Std_JSON.toString());

		// 请求签名值
		String signature = DigestHelper.getSignature(original_Std_JSON.toString(), SceneConfig.PROJECT_SECRET,
				SceneConfig.ALGORITHM, SceneConfig.ENCODING);

		// HTTP请求内容类型
		String ContentType = "application/json";

		// 设置HTTP请求头信息
		LinkedHashMap<String, String> headers = HttpHelper.getPOSTHeaders(SceneConfig.PROJECT_ID, signature,
				SceneConfig.ALGORITHM, ContentType, SceneConfig.ENCODING);

		// 向指定URL发送POST方法的请求
		JSONObject result = HttpHelper.sendPOST(SceneConfig.HTTP_ORIGINAL_STANDARD_OFFICIAL_APIURL,
				original_Std_JSON.toString(), headers, SceneConfig.ENCODING);

		int errCode = result.getInt("errCode");
		if (0 == errCode) {
			// 存证环节编号
			String segment_evId = result.getString("evid");
			// 待保全文档上传Url
			String fileUploadUrl = result.getString("url");
			LOG.info("证据点ID= " + segment_evId + " 待保全文档上传Url= " + fileUploadUrl);
		} else {
			System.out.println("获取证据点ID异常：errCode = " + errCode + " msg = " + result.get("msg"));

		}
		return result;
	}

	/***
	 * 创建原文存证高级版证据点 原文高级版存证成功后将原文同时推送到e签宝服务端、司法鉴定中心和公证处(请询问e签宝对接人员确认贵司所购买的存证类型)
	 * 
	 * @param filePath
	 * @param segmentTempletId
	 *            业务凭证中某一证据点名称ID
	 */
	public static JSONObject createSegmentOriginal_Advanced(String filePath, String segmentTempletId) {
		// 设置业务数据(与本文档有关的签署人信息)
		JSONObject segmentDataJSON = new JSONObject();
		segmentDataJSON.put("realName_1", "赵明丽");
		segmentDataJSON.put("userName_1", "zhaomingli@贵司系统内的登录账号");
		segmentDataJSON.put("realName_2", "金明丽");
		segmentDataJSON.put("userName_2", "jinmingli@贵司系统内的登录账号");
		String segmentData = segmentDataJSON.toString();

		Map<String, String> fileInfo = FileHelper.getFileInfo(filePath);

		JSONObject contentJSON = new JSONObject();
		// 待保全文档名称（文件名中不允许含有? * : " < > \ / | [ ] 【】）
		contentJSON.put("contentDescription", fileInfo.get("FileName"));
		// 待保全文档大小，单位：字节
		contentJSON.put("contentLength", fileInfo.get("FileLength"));
		// 待保全文档内容字节流MD5的Base64编码值
		contentJSON.put("contentBase64Md5", DigestHelper.getContentMD5(filePath));

		JSONObject original_Std_JSON = new JSONObject();
		original_Std_JSON.put("segmentTempletId", segmentTempletId);
		// segmentData为Json形式的字符串,并非Json格式数据,如:"segmentData":"{\"name\":\"张三\",\"address\":\"位于浙江省的采购方\"}"
		original_Std_JSON.put("segmentData", "\"" + segmentData + "\"");
		original_Std_JSON.put("content", contentJSON);

		System.out.println("请求参数:" + original_Std_JSON.toString());

		// 请求签名值
		String signature = DigestHelper.getSignature(original_Std_JSON.toString(), SceneConfig.PROJECT_SECRET,
				SceneConfig.ALGORITHM, SceneConfig.ENCODING);

		// HTTP请求内容类型
		String ContentType = "application/json";

		// 设置HTTP请求头信息
		LinkedHashMap<String, String> headers = HttpHelper.getPOSTHeaders(SceneConfig.PROJECT_ID, signature,
				SceneConfig.ALGORITHM, ContentType, SceneConfig.ENCODING);

		// 向指定URL发送POST方法的请求
		JSONObject result = HttpHelper.sendPOST(SceneConfig.HTTP_ORIGINAL_ADVANCED_OFFICIAL_APIURL,
				original_Std_JSON.toString(), headers, SceneConfig.ENCODING);

		int errCode = result.getInt("errCode");
		if (0 == errCode) {
			// 证据点ID
			String evId = result.getString("evid");
			System.out.println("存证环节编号= " + evId);
			// 待保全文档上传Url
			String fileUploadUrl = result.getString("url");
			System.out.println("待保全文档上传Url=" + fileUploadUrl);
			LOG.info("证据点ID= " + evId + " 待保全文档上传Url= " + fileUploadUrl);
		} else {
			System.out.println("获取证据点ID异常：errCode = " + errCode + " msg = " + result.get("msg"));

		}
		return result;
	}

	/***
	 * 创建摘要存证证据点,
	 * 摘要版存证不会将原文进行推送,仅是将原文的摘要(SHA256)推送到e签宝服务端和司法鉴定中心,文件摘要(SHA256)不支持存放到公证处(
	 * 请询问e签宝对接人员确认贵司所购买的存证类型)
	 * 
	 * @param filePath
	 * @param segmentTempletId
	 *            业务凭证中某一证据点名称ID
	 */
	public static JSONObject createSegmentOriginal_Digest(String filePath, String segmentTempletId) {
		// 设置业务数据(与本文档有关的签署人信息)
		JSONObject segmentDataJSON = new JSONObject();
		segmentDataJSON.put("realName_1", "赵明丽");
		segmentDataJSON.put("userName_1", "zhaomingli@贵司系统内的登录账号");
		segmentDataJSON.put("realName_2", "金明丽");
		segmentDataJSON.put("userName_2", "jinmingli@贵司系统内的登录账号");
		String segmentData = segmentDataJSON.toString();

		Map<String, String> fileInfo = FileHelper.getFileInfo(filePath);
		// 原文SHA256摘要
		String fileDigestSHA256 = DigestHelper.getFileSHA256(filePath);

		// System.out.println("原文SHA256摘要 = " + fileDigestSHA256);

		JSONObject contentJSON = new JSONObject();
		// 待保全文档名称（文件名中不允许含有? * : " < > \ / | [ ] 【】）
		contentJSON.put("contentDescription", fileInfo.get("FileName"));
		// 原文SHA256摘要
		contentJSON.put("contentDigest", fileDigestSHA256);

		JSONObject original_Digest_JSON = new JSONObject();
		original_Digest_JSON.put("segmentTempletId", segmentTempletId);
		// segmentData为Json形式的字符串,并非Json格式数据,如:"segmentData":"{\"name\":\"张三\",\"address\":\"位于浙江省的采购方\"}"
		original_Digest_JSON.put("segmentData", "\"" + segmentData + "\"");
		original_Digest_JSON.put("content", contentJSON);

		System.out.println("请求参数:" + original_Digest_JSON.toString());

		// 请求签名值
		String signature = DigestHelper.getSignature(original_Digest_JSON.toString(), SceneConfig.PROJECT_SECRET,
				SceneConfig.ALGORITHM, SceneConfig.ENCODING);

		// HTTP请求内容类型
		String ContentType = "application/json";

		// 设置HTTP请求头信息
		LinkedHashMap<String, String> headers = HttpHelper.getPOSTHeaders(SceneConfig.PROJECT_ID, signature,
				SceneConfig.ALGORITHM, ContentType, SceneConfig.ENCODING);

		// 向指定URL发送POST方法的请求
		JSONObject result = HttpHelper.sendPOST(SceneConfig.HTTP_ORIGINAL_DIGEST_OFFICIAL_APIURL,
				original_Digest_JSON.toString(), headers, SceneConfig.ENCODING);

		int errCode = result.getInt("errCode");
		if (0 == errCode) {
			// 证据点ID
			String evId = result.getString("evid");
			System.out.println("创建摘要存证证据点ID= " + evId);
			LOG.info("创建摘要存证证据点ID= " + evId);
		} else {
			System.out.println("创建摘要存证证据点异常：errCode = " + errCode + " msg = " + result.get("msg"));

		}
		return result;
	}

	/***
	 * 待存证文档上传
	 * 
	 * @param evId
	 *            证据点ID
	 * @param fileUploadUrl
	 * @param filePath
	 * @return
	 */
	public static JSONObject uploadOriginalDocumen(String evId, String fileUploadUrl, String filePath) {

		// ContentMD5 内容字节流MD5的Base64编码值
		String ContentMD5 = DigestHelper.getContentMD5(filePath);
		// HTTP请求内容类型
		String ContentType = "application/octet-stream";
		// 设置HTTP请求头信息
		LinkedHashMap<String, String> headers = HttpHelper.getPUTHeaders(ContentMD5, ContentType, SceneConfig.ENCODING);
		// 向指定URL发送PUT方法的请求
		JSONObject result = HttpHelper.sendPUT(evId, fileUploadUrl, filePath, headers);
		int errCode = result.getInt("errCode");
		if (200 == errCode) {
			System.out.println("存证环节编号= " + evId + " 待存证文档上传成功！");
		} else {
			System.out.println("存证环节编号= " + evId + " 待存证文档上传异常:Http状态码  = " + errCode + " msg = " + result.get("msg"));

		}
		return result;
	}

	/***
	 * 场景式存证编号关联到指定用户(以便指定用户日后可以顺利出证)
	 *
	 * @param scene_evId
	 *            场景式存证编号
	 * @return
	 */
	public static void relateSceneEvIdWithUser(String scene_evId, String borrName, String borrId, String investName,
			String investId, int b_user_type, int i_user_type) {
		// 用户证件信息
		List<CertificateBean> certificates = new ArrayList<CertificateBean>();

		// 接口调用方的个人类客户证件信息(请根据实际情况进行替换)
		// 与该场景式存证编号有关的客户证件信息
		CertificateBean personBean1 = new CertificateBean();
		personBean1.setName(borrName);
		if (b_user_type == 1) {// 个人类型
			personBean1.setType("ID_CARD");
		} else if (b_user_type == 2 || b_user_type == 3) {// 企业/个体工商户
			personBean1.setType("CODE_USC");
		}
		personBean1.setNumber(borrId);

		// 接口调用方的个人类客户证件信息(请根据实际情况进行替换)
		// 与该场景式存证编号有关的客户证件信息
		CertificateBean personBean2 = new CertificateBean();
		personBean2.setName(investName);
		if (i_user_type == 1) {
			personBean2.setType("ID_CARD");
		} else if (i_user_type == 2 || i_user_type == 3) {
			personBean2.setType("CODE_USC");
		}
		personBean2.setNumber(investId);

		certificates.add(personBean1);
		certificates.add(personBean2);

		// 请求参数-JSON字符串
		JSONObject param_json = new JSONObject();
		param_json.put("evid", scene_evId);
		param_json.put("certificates", JSONArray.fromObject(certificates));
		System.out.println("请求参数:" + param_json);

		// 请求签名值
		String signature = DigestHelper.getSignature(param_json.toString(), SceneConfig.PROJECT_SECRET,
				SceneConfig.ALGORITHM, SceneConfig.ENCODING);

		// HTTP请求内容类型
		String ContentType = "application/json";

		// 设置HTTP请求头信息
		LinkedHashMap<String, String> headers = HttpHelper.getPOSTHeaders(SceneConfig.PROJECT_ID, signature,
				SceneConfig.ALGORITHM, ContentType, SceneConfig.ENCODING);

		// 向指定URL发送POST方法的请求
		JSONObject result = HttpHelper.sendPOST(SceneConfig.HTTP_RELATE_USER_OFFICIAL_APIURL, param_json.toString(),
				headers, SceneConfig.ENCODING);

		int errCode = result.getInt("errCode");
		if (0 == errCode) {
			// 场景式存证编号关联到指定用户是否成功标识
			boolean isAppendSuccess = result.getBoolean("success");
			System.out.println("场景式存证编号关联到指定用户成功状态 = " + isAppendSuccess);
			LOG.info("场景式存证编号关联到指定用户成功状态 = " + isAppendSuccess);
		} else {
			System.out.println("场景式存证编号关联到指定用户成功状态异常：errCode = " + errCode + " msg = " + result.get("msg"));

		}
	}

	/***
	 * 拼接存证证明查看页面完整Url,以便指定用户进行存证查看
	 *
	 * @param scene_evId
	 *            场景式存证编号
	 * @return
	 */
	public static String getViewCertificateInfoUrl(String scene_evId, String number) {

		String timestampString = null;
		// 存证证明页面查看地址Url的有效期：
		String reverse = "false";
		if ("false".equals(reverse)) {
			// false表示timestamp字段为链接的生效时间，在生效30分钟后该链接失效
			long timestamp = System.currentTimeMillis();
			timestampString = ToolsHelper.stampToString(timestamp);// 当前系统的时间戳(毫秒级)
		} else {
			// true表示timestamp字段为链接的失效时间,假设2018年12月31日23点59分59秒链接失效
			timestampString = ToolsHelper.dateToStamp("2018-12-31 23:59:59");
			System.out.println("毫秒级时间戳 = " + timestampString);
		}
		// 证件类型
		String type = "ID_CARD";
		// 证件号码，指定这个用户查看这个存证证明时，页面中的证明持有人一栏显示的是该用户名称
		// String number = "540101198709260015";

		StringBuffer param = new StringBuffer();
		param.append("id=" + scene_evId);
		param.append("&projectId=" + SceneConfig.PROJECT_ID);
		param.append("&timestamp=" + timestampString);
		param.append("&reverse=" + reverse);
		param.append("&type=" + type);
		param.append("&number=" + number);
		System.out.println("动态链接Url部分:" + param);
		// 请求签名值
		String signature = DigestHelper.getSignature(param.toString(), SceneConfig.PROJECT_SECRET, "HmacSHA256",
				"UTF-8");
		// 存证证明页面查看完整Url
		String viewCertificateInfoUrl = SceneConfig.HTTPS_VIEWPAGE_OFFICIAL_URL + "?" + param.toString() + "&signature="
				+ signature;
		return viewCertificateInfoUrl;
	}
}
