package utils.evi.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import utils.evi.util.SceneDataDictionaryHelper;
import utils.evi.util.SceneHelper;

/***
 * @Description: 场景式存证_Demo
 * @Version: Ver_1.0
 * @Team: 公有云技术支持小组
 * @Author: 天云小生
 * @Date: 2018年01月18日
 */

public class SceneEviTest {

	public static void evi(String filePath, JSONObject json, List<String> sids,String sceneName) {

		String borrName = json.getString("b_reality_name");
		String borrId = json.getString("b_id_number");
		String borrAccount = json.getString("b_name");
		String investName = json.getString("reality_name");
		String investId = json.getString("id_number");
		String investAccount = json.getString("name");

		/***
		 * 本demo中涉及的信息均为方便演示，实际开发中请根据贵司的实际业务进行调整
		 */
		// 待保全文档 默认路径在项目下的files文件夹下

		System.out.println("- - - - - - - [只需要执行一次的场景式存证数据字典创建 - Start] - - - - - - -");
		// 根据实际情况定义场景式存证数据字典(全局范围) 【提示:】场景式存证数据字典只需要创建一次,存在于数据字典中的ID可永久使用
		// SceneDataDictionaryHelper.createSceneDataDictionary();
		System.out.println("- - - - - - - [只需要执行一次的场景式存证数据字典创建 - End] - - - - - - -");

		// 业务凭证（名称）ID-sceneTempletId
		String sceneTemplateId = "e719a38c-7bd7-4e6c-b950-ea677a57a4ef";
		// 业务凭证中某一证据点名称ID-segmentTempletId
		String segmentTempletId = "b6535fc2-da8b-4bf5-9df4-e805fd13cbc3";

		// 证据点ID
		String segment_evId = null;
		// 待保全文档上传Url
		String fileUploadUrl = null;
		// 场景式存证编号
		String scene_evId = null;
		// 存证证明页面查看完整Url
		String viewCertificateInfoUrl = null;

		System.out.println("- - - - - - - [第一步:创建证据链,获取场景式存证编号 - Start] - - - - - - -");
		// 场景式存证编号(请妥善保管场景式存证编号,以便日后查询存证证明)
		scene_evId = SceneHelper.createChainOfEvidence(sceneTemplateId,sceneName);
		System.out.println("- - - - - - - [创建证据链,获取场景式存证编号 - End] - - - - - - -");

		System.out.println("- - - - - - - [第二步:创建原文存证（基础版）证据点,获取存证环节编号和待存证文档上传Url - Start] - - - - - - -");
		// 创建原文存证（基础版）证据点,
		// 原文基础版存证成功后将原文同时推送到e签宝服务端和司法鉴定中心,不会推送到公证处
		// (请询问e签宝对接人员确认贵司所购买的存证类型)
		net.sf.json.JSONObject standard_Result = SceneHelper.createSegmentOriginal_Standard(filePath, segmentTempletId,
				borrName, borrAccount, investName, investAccount);

		// 原文存证（基础版）证据点ID
		segment_evId = standard_Result.getString("evid");
		System.out.println("原文存证（基础版）证据点ID= " + segment_evId);
		// 待保全文档上传Url
		fileUploadUrl = standard_Result.getString("url");
		System.out.println("待保全文档上传Url= " + fileUploadUrl);
		System.out.println("- - - - - - - [创建原文存证（基础版）证据点,获取存证环节编号和待存证文档上传Url - End] - - - - - - -");

		System.out.println("- - - - - - - [第三步:上传待存证文档 - Start] - - - - - - -");
		// 待存证文档上传
		SceneHelper.uploadOriginalDocumen(segment_evId, fileUploadUrl, filePath);
		System.out.println("- - - - - - - [上传待存证文档 - End] - - - - - - -");

		System.out.println("- - - - - - - [第四步:追加证据点,将证据点追加到已存在的证据链内形成证据链 - Start] - - - - - - -");
		// 向已存在的证据链中追加证据(如追加补充协议的签署存证信息)
		SceneHelper.appendEvidence(scene_evId, segment_evId, sids);
		System.out.println("- - - - - - - [追加证据点,将证据点追加到已存在的证据链内形成证据链 - End] - - - - - - -");

		System.out.println("- - - - - - - [第五步:场景式存证编号关联到指定的用户,以便指定用户日后可以顺利出证 - Start] - - - - - - -");
		SceneHelper.relateSceneEvIdWithUser(scene_evId, borrName, borrId, investName, investId, 0, 0);// 后两位参数后来加的
																										// 。根据实际改掉
		System.out.println("- - - - - - - [场景式存证编号关联到指定的用户,以便指定用户日后可以顺利出证 - End] - - - - - - -");

		System.out.println("- - - - - - - [第六步:通过贵司的系统跳转到存证证明查看页面,以便指定用户进行存证查看 - Start] - - - - - - -");
		// 存证证明页面查看完整Url
		viewCertificateInfoUrl = SceneHelper.getViewCertificateInfoUrl(scene_evId, investId);
		System.out.println("存证证明页面查看完整Url = " + viewCertificateInfoUrl);
		System.out.println("- - - - - - - [通过贵司的系统跳转到存证证明查看页面,以便指定用户进行存证查看 - End] - - - - - - -");
	}

	public static void main(String[] args) {

		JSONObject json = new JSONObject();
		json.put("b_reality_name", "龚宁媚 ");
		json.put("b_id_number", "352201198712160025");
		json.put("b_name", "zyy13656896647");
		json.put("reality_name", "刘建");
		json.put("id_number", "510225197001126538");
		json.put("name", "zyy13989760068");

		List<String> sids = new ArrayList<String>();
		evi("e:/sign/signed_20180307105135.pdf", json, sids,"借款协议签署");

	}
}