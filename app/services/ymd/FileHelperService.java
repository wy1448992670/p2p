/******************************************************************
 *
 *    Java Lib For Android, Powered By Shenzhen Jiuzhou.
 *
 *    Copyright (c) 2001-2014 Digital Telemedia Co.,Ltd
 *    http://www.d-telemedia.com/
 *
 *    Package:     services.ymd
 *
 *    Filename:    FileHelperService.java
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
 *    Create at:   2018年12月25日 下午2:34:18
 *
 *    Revision:
 *
 *    2018年12月25日 下午2:34:18
 *        - first revision
 *
 *****************************************************************/
package services.ymd;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import dao.ymd.FileHelperDao;
import models.file.t_file;
import models.file.t_file_relation;
import play.Logger;
import play.db.jpa.Transactional;
import play.mvc.Http.Request;
import services.file.FileService;
import utils.faceId.FaceDao;

/**
 * @ClassName FileHelperService
 * @Description 文件处理类
 * @author zj
 * @Date 2018年12月25日 下午2:34:18
 * @version 1.0.0
 */
public class FileHelperService {
	static String RESULT_CODE = "1001";

	/**
	 * @Description 接收前端传过来的文件保存到本地
	 * @param file
	 * @param userId       上传人id
	 * @param relationId   具体业务相关di
	 * @param fileDictId   文件类型表 身份证正面 1 身份证反面 2
	 * @param relationType 业务类型 1.实名认证 2.银行卡认证 3,运营商认证 4.基础信息-补充资料 5.下单--资料上传
	 *                     6.提额--补充资料 7.合作机构创建
	 * @param requiredNum  最少文件数
	 * @param maxNum       最多文件数
	 * @param sequence     排序号
	 * @param isVisible    是否可见 true 可见 false 不可见
	 * @author: zj
	 * 
	 */
	@Transactional
	public static String addFileOnLocal(File file, long userId, Long relationId, int fileDictId, int relationType,
			int uploaderType, int requiredNum, int maxNum, int sequence, boolean isVisible, boolean canRewrite,
			boolean isTemp) {
		String imagePath = FaceDao.saveFile(file);
		t_file newFile = FileHelperDao.saveFile(fileDictId, imagePath, uploaderType, userId);
		// FileHelperDao.saveFileRelationDict(relationType, fileDictId, requiredNum,
		// maxNum, sequence, isVisible,
		// canRewrite);
		FileHelperDao.saveFileRelation(relationType, relationId, newFile.getId().intValue(), isVisible, sequence,
				canRewrite, isTemp);
		return imagePath;
	}

	/**
	 * @Description 此方法不涉及第三方接口，单个上传只保存文件到磁盘，<br>
	 *              不做数据库记录。后续整体提交才做数据库关系记录
	 * @param file
	 * @return
	 * @author: zj
	 */
	public static String addFileOnLocal(File file) {
		String imagePath = FaceDao.saveFile(file);
		return imagePath;
	}

	/**
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param file         最佳人脸照
	 * @param file2        最佳全景
	 * @param userId
	 * @param relationId
	 * @param fileDictId   文件类型 3 最佳人脸 4 最佳全景
	 * @param fileDictId2
	 * @param relationType
	 * @param uploaderType
	 * @param requiredNum
	 * @param maxNum
	 * @param sequence
	 * @param isVisible
	 * @param canRewrite
	 * @param isTemp
	 * @return
	 * @author: zj
	 */
	public static void addFileOnLocalVerifyLiving(File file, File file2, long userId, Long relationId, int fileDictId,
			int fileDictId2, int relationType, int uploaderType, int requiredNum, int maxNum, int sequence,
			boolean isVisible, boolean canRewrite, boolean isTemp) {
		// 存放活体最佳人脸照 只是部分参数不同而已 file fileDictId
		addFileOnLocal(file, userId, relationId, fileDictId, relationType, uploaderType, requiredNum, maxNum, sequence,
				isVisible, canRewrite, isTemp);
		// 存放活体最佳全景 只是部分参数不同而已 file fileDictId
		addFileOnLocal(file2, userId, relationId, fileDictId2, relationType, uploaderType, requiredNum, maxNum,
				sequence, isVisible, canRewrite, isTemp);
	}

	/**
	 * 
	 * @Description 身份证上传
	 * @param file
	 * @param userId       用户id
	 * @param relationId   用户id
	 * @param fileDictId   文件类型表 身份证正面 1 身份证反面 2
	 * @param relationType 1.users实名认证 2.users用户风控资料 3.credit_apply
	 *                     4.increase_credit_apply 5.borrow_apply 6.users.user_type
	 *                     in (2,3)企业用户资料 7.orgnization合作机构资料
	 * @param uploaderType 1. 用户 2.管理员
	 * @param requiredNum  至少上传图片数量
	 * @param maxNum       最多上传图片数量
	 * @param sequence     排序号 默认传1
	 * @param isVisible    是否可见 1 可见 2 不可见
	 * @param canRewrite   能否修改关系 传false
	 * @param isTemp       是否是临时关系 true 是 false 否 传 false
	 * @return 身份证解析结果
	 * @author: zj
	 */
	@Transactional
	public static String ocridcard(File file, long userId, Long relationId, int fileDictId, int relationType,
			int uploaderType, int requiredNum, int maxNum, int sequence, boolean isVisible, boolean canRewrite,
			boolean isTemp) {
		FileHelperDao.delFileRelationByFileDict(relationType,relationId,fileDictId);
		// 身份证ocr 信息返回
		String response = FaceDao.ocridcard(file);
		// String response="";

		// 这里判断ocr结果
		JSONObject objJson = JSON.parseObject(response);
		String resulString = objJson.getString("result");
		if (RESULT_CODE.equals(resulString)) {// 身份证解析成功才能保存入库
			addFileOnLocal(file, userId, relationId, fileDictId, relationType, uploaderType, requiredNum, maxNum,
					sequence, isVisible, canRewrite, isTemp);
		}
		return response;
	}

	/**
	 * @Description 活体验证
	 * @param idCardNumber
	 * @param idCardName
	 * @param delta
	 * @param imageBest
	 * @param imageEnv
	 * @param userId
	 * @param relationId
	 * @param fileDictId
	 * @param fileDictId2
	 * @param relationType
	 * @param requiredNum
	 * @param maxNum
	 * @param sequence
	 * @param isVisible
	 * @param isTemp
	 * @param canRewrite
	 * @param uploaderType
	 * @return
	 * @author: zj
	 */
	public static boolean verifyLiving(String idCardNumber, String idCardName, String delta, File imageBest,
			File imageEnv, long userId, long relationId, int fileDictId, int fileDictId2, int relationType,
			int requiredNum, int maxNum, int sequence, boolean isVisible, boolean isTemp, boolean canRewrite,
			int uploaderType) {
		boolean flag = false;
		String resultMsgString = FaceDao.verifyLiving(idCardNumber, idCardName, delta, imageBest, imageEnv);
		JSONObject json = JSON.parseObject(resultMsgString);
		if (!json.containsKey("error_message")) {// 没有这个key
			if (json.getJSONObject("result_faceid").getFloat("confidence") > json.getJSONObject("result_faceid")
					.getJSONObject("thresholds").getFloat("1e-5")) {// 是同一个人
				addFileOnLocalVerifyLiving(imageBest, imageEnv, userId, relationId, fileDictId, fileDictId2,
						relationType, uploaderType, requiredNum, maxNum, sequence, isVisible, canRewrite, isTemp);
				flag = true;
			}

		}
		Logger.info("活体校验结果=========>" + resultMsgString);
		return flag;
	}

	/**
	 * @Description 亿美贷 风控图片资料一起提交（仅供app端使用。pc端数据结构不同，有单独的方法使用，分开便于今后维护 互不影响）
	 * @param jsonString
	 * @author: zj
	 */
	public static void saveImageInfo(String jsonString, long userId, int relationType) {

		JSONArray jsonArray = JSONArray.parseArray(jsonString);

		String fileDictId = "";
		String files = "";
		String realPath = "";

		for (Iterator iterator = jsonArray.iterator(); iterator.hasNext();) {
			JSONObject object = (JSONObject) iterator.next();
			fileDictId = object.getString("fileDictId");
			int sequence = FileService.getSequenceByRelationTypeAndFileDict(relationType, Integer.parseInt(fileDictId));
			files = object.getString("files");
			String[] filePath = files.split("\\|");
			for (String path : filePath) {
				realPath = path;
				FileHelperDao.saveImageInfo(Integer.parseInt(fileDictId), realPath, 1, relationType, userId, true,
						false, false, 1, 1, sequence);
			}
		}
	}

	/**
	 * @Description 所有文件一起提交 其实保存的是文件的url相关记录 文件真实上传已经在选择文件后完成（此方法仅供pc端医院机构上传图片使用）
	 * @param relationType 业务节点id 参考 t_file_relation_dict 表 中 relation_type 的定义
	 * @param realPath     图片的相对路径
	 * @param fileDictId   文件的类型 id 参考 t_file_dict 表中的id 定义
	 * @param relationId   具体业务的id 可以是 user_id 或者 apply_id 等 具体业务具体设计
	 * @param fileInfo     pc端传来的文件信息，是个数组，里面包含了url 业务id等相关信息 需要循环做解析
	 * @return json字符串
	 * @author: zj
	 */
	@Transactional
	public static List allFileSubmit(int relationType, long relationId, Request request) {

		// 响应结果json串
		Map<String, Object> responseMap = new HashMap<String, Object>();
		List idList = new ArrayList();
		long fileId = 0L;

		List<String> list = new ArrayList<String>();
		String[] imgPathString = request.params.getAll("imgPathString");
		String[] fileDictId = request.params.getAll("fileDictId");
		for (int i = 0; i < fileDictId.length; i++) {
			fileId = FileHelperDao.saveImageInfo(Integer.parseInt(fileDictId[i]), imgPathString[i], 2, relationType,
					relationId, true, false, false, 1, 1, 1);
			idList.add(fileId);
		}
		return idList;
	}

	public static void main(String[] args) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<Long> list = new ArrayList();
		list.add(1L);
		list.add(2L);
		List list2 = new ArrayList();
		list2.add(3L);
		list2.add(4L);

		map.put("fileType", "yyzp");
		map.put("id", list);
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put("fileType", "hzxy");
		map2.put("id", list2);

		List list9 = new ArrayList();
		list9.add(map);
		list9.add(map2);
		System.out.println(JSON.toJSONString(list9));
	}

	/**
	 * @Description 删除 t_file_relation 表
	 * @param relationType 节点类型
	 * @param relationId   业务表主键
	 * @param fileId       文件id
	 * @author: zj
	 */
	@Deprecated
	public static void delFileRelation(int relationType, long relationId, long fileId) {
		System.out.println("FileHelperDao.delFileRelation("+relationType+", "+relationId+", "+fileId+");");
		FileHelperDao.delFileRelation(relationType, relationId, fileId);
	}
	
	/**
	 * @Description 删除 t_file_relation 表
	 * @param relationType 节点类型
	 * @param relationId   业务表主键
	 * @param fileId       文件id
	 * @author: zj
	 */
	public static void delFileRelationById(long file_relation_id) {
		FileHelperDao.delFileRelationById(file_relation_id);
	}


	/**
	 * @Description 额度申请下单后，将用户关联的文件信息 复制一份到 申请单
	 * @param relation_type  业务类型 3.users用户风控资料
	 * @param relation_id    业务表主键id userId
	 * @param relation_type2 业务类型2 4 credit_apply
	 * @param relation_id2   业务表主键id2 目前是 申请单id
	 * @author: zj
	 */
	public static void creditApplyCopyFileRelation(int source_relation_type,long source_relation_id,int target_relation_type,long target_relation_id) {
		List<t_file_relation> file_relations= FileService.getFileShowByRelation(source_relation_type, source_relation_id);
		for (Iterator iterator = file_relations.iterator(); iterator.hasNext();) {
			t_file_relation t_file_relation = (t_file_relation) iterator.next();
			FileHelperDao.saveFileRelation(target_relation_type, target_relation_id, t_file_relation.file_id, t_file_relation.is_visible
					, t_file_relation.file_dict_sequence,t_file_relation.can_rewrite, t_file_relation.is_temp);
		}
	}

}
