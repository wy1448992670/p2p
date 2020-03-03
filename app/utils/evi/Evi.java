package utils.evi;

import java.util.ArrayList;
import java.util.List;

import utils.evi.util.SceneDataDictionaryHelper;
import utils.evi.util.SceneHelper;

import com.alibaba.fastjson.JSONObject;

import play.Logger;

public class Evi {
	
	/**
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param filePath
	 * @param json
	 * @param sids
	 * @param sceneName     存证名称
	 * @return
	 * @author: zj
	 */
	public static String evi(String filePath,JSONObject json,List<String> sids,String sceneName){
		if(sids == null){
			sids = new ArrayList<String>();
		}
		String borrName = json.getString("b_reality_name");
		String borrId = json.getString("b_id_number");
		String borrAccount = json.getString("b_name");
		String investName = json.getString("reality_name");
		String investId = json.getString("id_number");
		String investAccount = json.getString("name");
		//借款人类型
		int b_user_type = json.getIntValue("b_user_type");
		//投资人类型
		int i_user_type = json.getIntValue("i_user_type");
		Logger.info(json.toJSONString()+"存证接收的参数===============================");
		/***
		 * 本demo中涉及的信息均为方便演示，实际开发中请根据贵司的实际业务进行调整
		 */
		// 待保全文档 默认路径在项目下的files文件夹下
		
		System.out.println("- - - - - - - [只需要执行一次的场景式存证数据字典创建 - Start] - - - - - - -");
		// 根据实际情况定义场景式存证数据字典(全局范围) 【提示:】场景式存证数据字典只需要创建一次,存在于数据字典中的ID可永久使用
//		SceneDataDictionaryHelper.createSceneDataDictionary();
		System.out.println("- - - - - - - [只需要执行一次的场景式存证数据字典创建 - End] - - - - - - -");

		// 业务凭证（名称）ID-sceneTempletId  972fb240-2de1-449d-9ed7-88bbddfe6fce
		String sceneTemplateId = "61cc2c4f-480b-4038-8ad0-703386107f5e";
		// 业务凭证中某一证据点名称ID-segmentTempletId ab57e740-aa07-456d-8ed0-7db3dafddd87
		String segmentTempletId = "ab57e740-aa07-456d-8ed0-7db3dafddd87";

		// 证据点ID
		String segment_evId = null;
		// 待保全文档上传Url
		String fileUploadUrl = null;
		// 场景式存证编号
		String scene_evId = null;
		
		System.out.println("- - - - - - - [第一步:创建证据链,获取场景式存证编号 - Start] - - - - - - -");
		// 场景式存证编号(请妥善保管场景式存证编号,以便日后查询存证证明)
		scene_evId = SceneHelper.createChainOfEvidence(sceneTemplateId,sceneName);
		System.out.println("- - - - - - - [创建证据链,获取场景式存证编号 - End] - - - - - - -");
		
		System.out.println("- - - - - - - [第二步:创建原文存证（基础版）证据点,获取存证环节编号和待存证文档上传Url - Start] - - - - - - -");
		// 创建原文存证（基础版）证据点,
		// 原文基础版存证成功后将原文同时推送到e签宝服务端和司法鉴定中心,不会推送到公证处
		// (请询问e签宝对接人员确认贵司所购买的存证类型)
		net.sf.json.JSONObject standard_Result = SceneHelper.createSegmentOriginal_Standard(filePath, segmentTempletId,borrName,borrAccount,investName,investAccount);

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
		SceneHelper.appendEvidence(scene_evId, segment_evId,sids);
		System.out.println("- - - - - - - [追加证据点,将证据点追加到已存在的证据链内形成证据链 - End] - - - - - - -");

		System.out.println("- - - - - - - [第五步:场景式存证编号关联到指定的用户,以便指定用户日后可以顺利出证 - Start] - - - - - - -");
		SceneHelper.relateSceneEvIdWithUser(scene_evId,borrName,borrId,investName,investId,b_user_type,i_user_type);
		System.out.println("- - - - - - - [场景式存证编号关联到指定的用户,以便指定用户日后可以顺利出证 - End] - - - - - - -");
		
/*		System.out.println("- - - - - - - [第六步:通过贵司的系统跳转到存证证明查看页面,以便指定用户进行存证查看 - Start] - - - - - - -");
		// 存证证明页面查看完整Url
		String viewCertificateInfoUrl = null;
		// 存证证明页面查看完整Url
		viewCertificateInfoUrl = SceneHelper.getViewCertificateInfoUrl(scene_evId,investId);
		System.out.println("存证证明页面查看完整Url = " + viewCertificateInfoUrl);
		System.out.println("- - - - - - - [通过贵司的系统跳转到存证证明查看页面,以便指定用户进行存证查看 - End] - - - - - - -");
*/		
		return scene_evId;
	}
	
	public static void main(String[] args) {
		
		JSONObject json = new JSONObject();
		json.put("b_reality_name", "张旭");
		json.put("b_id_number", "370406198408045051");
		json.put("b_name", "zzy15588226615");
		json.put("reality_name", "郑丽平");
		json.put("id_number", "370405198801034627");
		json.put("name", "美女妹妹");
		json.put("b_user_type", 1);
		json.put("i_user_type", 1);
		

		List<String> sids = new ArrayList<String>();
	String investId=evi("D:\\workspace\\yiyilc_qykh\\yiyilc_sp2p\\public\\pact\\J1552\\2018-10-23-J1552-010.doc.sign.pdf", json,sids,"借款协议签署");
	String viewCertificateInfoUrl = SceneHelper.getViewCertificateInfoUrl(investId,"370405198801034627");
	System.out.println("存证证明页面查看完整Url = " + viewCertificateInfoUrl);
		
//		SceneDataDictionaryHelper.createSceneDataDictionary();
	}
}
