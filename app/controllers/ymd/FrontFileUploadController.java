package controllers.ymd;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;

import constants.Constants;
import controllers.BaseController;
import controllers.interceptor.YMDInterceptor;
import models.risk.t_risk_report;
import net.sf.json.JSONObject;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.mvc.Http.Request;
import play.mvc.With;
import services.RiskReportService;
import services.UserService;
import services.ymd.FileHelperService;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.JSONUtils;
import utils.Security;
import utils.mjkj.MjkjService;

/**
 * @ClassName FrontFileUploadController
 * @Description 前台APP文件上传接口 亿美贷使用
 * @author zj
 * @Date Jan 22, 2019 8:26:21 PM
 * @version 1.0.0
 */
@SuppressWarnings("unchecked")

public class FrontFileUploadController extends BaseController {

	/**
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param file         文件类
	 * @param uploadUserId 操作人id
	 * @param relationId   业务id 比如 可以是用户id 申请单id 等
	 * @param fileDictId   文件类型
	 * @param relationType 业务类型
	 * @param requiredNum  最少数量
	 * @param maxNum       最大数量
	 * @param sequence     排序
	 * @param isVisible    是否可见 true false
	 * @return
	 * @author: zj
	 */
	public static String uploadFile(File file) {
		Logger.info("=================开始接收文件===============");
		Logger.debug("request.url：" + request.url);
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		try {
			jsonMap.put("error", "-1");
			jsonMap.put("msg", "成功");
			String imagePathString = FileHelperService.addFileOnLocal(file);
			jsonMap.put("imagePath", imagePathString);
			return JSONUtils.printObject(jsonMap);
		} catch (IOException e) {
			Logger.error("保存文件到本地出错=====>" + e.getMessage(), e);
		}
		jsonMap.put("error", "0");
		jsonMap.put("msg", "失败");
		return JSONObject.fromObject(jsonMap).toString();
	}

	/**
	 * @Description ocr身份证
	 * @param file         文件类
	 * @param uploadUserId 操作人id
	 * @param relationId   业务id 比如 可以是用户id 申请单id 等
	 * @param fileDictId   文件类型
	 * @param relationType 业务类型
	 * @param requiredNum  最少数量
	 * @param maxNum       最大数量
	 * @param sequence     排序
	 * @param isVisible    是否可见 true false
	 * @return
	 * @author: zj
	 */
	public static String ocridcard(File file) {
		ErrorInfo error = new ErrorInfo();
		Request request = Request.current();
		Logger.debug("request.url：" + request.url);
		Set<String> keys = request.params.data.keySet();
		Iterator var11 = keys.iterator();
		while (var11.hasNext()) {
			String t_key = (String) var11.next();
			System.out.println("t_key:" + t_key + " value:" + request.params.get(t_key));
		}

		Logger.info("=================开始接收文件===============");
		long relationId = Security.checkSign(request.params.get("userId"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		int fileDictId = Integer.parseInt(request.params.get("fileDictId"));
		int relationType = Integer.parseInt(request.params.get("relationType"));

		Map<String, Object> jsonMap = new HashMap<String, Object>();
		try {
			jsonMap.put("error", "-1");
			jsonMap.put("msg", "成功");

			String ocrMsg = FileHelperService.ocridcard(file, relationId, relationId, fileDictId, relationType, 1, 1, 1, 1, true,
					false, false);

			Map<String, Object> info = new HashMap<String, Object>();

			com.alibaba.fastjson.JSONObject objJson = JSON.parseObject(ocrMsg);

			if (fileDictId == 1) {// 解析正面
				info.put("name", objJson.getJSONObject("name").getString("result"));
				info.put("idcard_number", objJson.getJSONObject("idcard_number").getString("result"));
				info.put("address", objJson.getJSONObject("address").getString("result"));
				info.put("valid_date_start", "");
				info.put("valid_date_end", "");
				// 缓存正面信息

				// 将解析出来的信息暂存在缓存 后续会使用到
				Cache.set("ymd_idcard_" + relationId, info, "24h");

			}
			if (fileDictId == 2) {// 解析反面
				info.put("name", "");
				info.put("idcard_number", "");
				info.put("address", "");
				info.put("valid_date_start", objJson.getJSONObject("valid_date_start").getString("result"));
				info.put("valid_date_end", objJson.getJSONObject("valid_date_end").getString("result"));
			}

//			info.put("name", "徐姐夫");
//			info.put("idcard_number", "110119110119110119");
//			info.put("address", "伊拉克省叙利亚市默罕默德村");
//			info.put("valid_date_start", "19120801");
//			info.put("valid_date_end", "19220921");

			// 封装返回给前端的数据
			jsonMap.put("info", info);
			// 相关数据入库
			FileHelperService.addFileOnLocal(file, relationId, relationId, fileDictId, relationType, 1, 1, 1, 1, true, false,
					false);

			String resultString = JSONUtils.printObject(jsonMap);
			Logger.info("校验身份证ocr结果==========>" + resultString);
			return resultString;
		} catch (Exception e) {
			e.printStackTrace();
			jsonMap.put("error", "0");
			jsonMap.put("msg", "失败");
			try {
				return JSONUtils.printObject(jsonMap);
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}
		}

	}

	/**
	 * @Description 判断老用户当前实名认证信息是否与之前的相同
	 * @param userId
	 * @param name
	 * @param idCardNo
	 * @return false 不同 true 相同
	 * @author: zj
	 */
	public static String checkIdCardInfo() {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "成功");
		try {
			ErrorInfo error = new ErrorInfo();
			long relationId = Security.checkSign(request.params.get("userId"), Constants.USER_ID_SIGN, Constants.VALID_TIME,
					error);

			Map<String, Object> map = new HashMap<String, Object>();
			if (Cache.get("ymd_idcard_" + relationId) != null) {
				map = (Map<String, Object>) Cache.get("ymd_idcard_" + relationId);
			}

			String name = map.get("name").toString();
			String idCardNo = map.get("idcard_number").toString();
			String address = map.get("address").toString();
			boolean flag = UserService.checkUserIdCardInfo(relationId, name, idCardNo);
			if (!flag) {
				jsonMap.put("error", "0");
				jsonMap.put("msg", "姓名或者身份证卡号有误");
			}
			return JSON.toJSONString(jsonMap);
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error("身份证校验异常=========>" + e.getMessage(), e);
			jsonMap.put("error", "0");
			jsonMap.put("msg", "失败");
			return JSON.toJSONString(jsonMap);
		}

	}

	/**
	 * @Description 活体检测
	 * @param imageBest    最佳质量图
	 * @param imageEnv     全景照片
	 * @param delta        校验字符串
	 * @param fileDictId   最佳图片id
	 * @param fileDictId2  全景图片id
	 * @param uploadUserId 操作人id
	 * @param relationId   业务id 比如 可以是用户id 申请单id 等
	 * @return
	 * @author: zj
	 */
	public static String verifyLiving(File imageBest, File imageEnv) {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "成功");
		ErrorInfo error = new ErrorInfo();

		long uploadUserId = 0l;

		try {
			long userId = Security.checkSign(request.params.get("userId"), Constants.USER_ID_SIGN, Constants.VALID_TIME,
					error);
			Logger.info("verifyLiving userId==========>" + userId);

			Map<String, Object> map = new HashMap<String, Object>();
			if (Cache.get("ymd_idcard_" + userId) != null) {
				map = (Map<String, Object>) Cache.get("ymd_idcard_" + userId);
			}

			String name = map.get("name").toString();
			String idCardNo = map.get("idcard_number").toString();
			String address = map.get("address").toString();
			// 校验前面步骤中身份证信息是否与老用户的实名信息相同
			boolean flag = UserService.checkUserIdCardInfo(userId, name, idCardNo);
			if (!flag) {
				jsonMap.put("error", "1");
				jsonMap.put("msg", "身份证信息与之前实名认证信息不符");
				return JSON.toJSONString(jsonMap);
			}

			// 存放身份证信息返回给前端
			Map<String, Object> idCardInfo = new HashMap<String, Object>();

			idCardInfo.put("idCardNumber", idCardNo);
			idCardInfo.put("idCardName", name);

			String delta = request.params.get("delta");

			int fileDictId = Integer.parseInt(request.params.get("fileDictId"));
			Logger.info("fileDictId==========>" + fileDictId);

			int fileDictId2 = Integer.parseInt(request.params.get("fileDictId2"));
			Logger.info("fileDictId2==========>" + fileDictId2);

			uploadUserId = Security.checkSign(request.params.get("uploadUserId"), Constants.USER_ID_SIGN, Constants.VALID_TIME,
					error);
			Logger.info("uploadUserId==========>" + uploadUserId);
			// 活体检测结果
			boolean isrecet = FileHelperService.verifyLiving(idCardNo, name, delta, imageBest, imageEnv, uploadUserId, userId,
					fileDictId, fileDictId2, 2, 1, 1, 1, true, false, false, 1);
			if (!isrecet) {

				UserService.updateUserIdCardStatus(userId, -1);
				UserService.updateUserLivingStatus(uploadUserId, -1);
				jsonMap.put("error", "0");
				jsonMap.put("msg", "失败");
				return JSON.toJSONString(jsonMap);
			}
			boolean result = UserService.updateUserType(userId, 1);
			if (!result) {
				jsonMap.put("error", "2");
				jsonMap.put("msg", "用户类型异常，不能是企业或者个体工商户");
				return JSON.toJSONString(jsonMap);
			}

			UserService.updateUserIdCardInfo(userId, 1, name, idCardNo, address);
			UserService.updateUserLivingStatus(uploadUserId, 1);

			// 删掉当前用户缓存的身份资料信息
			Cache.delete("ymd_idcard_" + userId);

			jsonMap.put("idCardInfo", idCardInfo);
			return JSON.toJSONString(jsonMap);
		} catch (Exception e) {
			Logger.error("活体校验异常============>" + e.getMessage(), e);
			jsonMap.put("error", "0");
			jsonMap.put("msg", "失败");
			UserService.updateUserLivingStatus(uploadUserId, -1);
			JPA.setRollbackOnly();
			return JSON.toJSONString(jsonMap);
		}
	}

	/**
	 * @Description app端选择图片并保存提交
	 * @param saveResultJson app端保存结果
	 * @return
	 * @author: zj
	 */
	@Deprecated
	public static String selectFileSave() {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "成功");
		try {

			Request request = Request.current();
			Logger.debug("request.url：" + request.url);
			Set<String> keys = request.params.data.keySet();
			Iterator var11 = keys.iterator();
			while (var11.hasNext()) {
				String t_key = (String) var11.next();
				System.out.println("t_key:" + t_key + " value:" + request.params.get(t_key));
			}

			String imgs = request.params.get("imgs");
			Logger.info("imgs=========>" + imgs);
			int relationType = Integer.parseInt(request.params.get("relationType"));
			long relationId;
			try {
				// 根据业务传递不同的参数
				relationId = Integer.parseInt(request.params.get("relationId"));
			} catch (Exception e) {
				relationId = Security.checkSign(request.params.get("relationId"), Constants.USER_ID_SIGN, Constants.VALID_TIME,
						error);
				// e.printStackTrace();
			}

			FileHelperService.saveImageInfo(imgs, relationId, relationType);
			return JSON.toJSONString(jsonMap);
		} catch (Exception e) {
			jsonMap.put("error", "0");
			jsonMap.put("msg", "失败");
			Logger.info(e.getMessage(), e);
			return JSON.toJSONString(jsonMap);
		}
	}

	/**
	 * @Description 额度申请下单后，将用户关联的文件信息 复制一份到 申请单
	 * @return
	 * @author: zj
	 */
	public static String creditApplyCopyFileRelation() {
		Map<String, Object> jsoMap = new HashMap<String, Object>();
		jsoMap.put("error", "-1");
		jsoMap.put("msg", "成功");

		try {
			int relationType = Integer.parseInt(params.get("relationType"));
			long relationId = Long.parseLong(params.get("relationId"));
			int relationType2 = Integer.parseInt(params.get("relationType2"));
			long relationId2 = Long.parseLong(params.get("relationId2"));
			FileHelperService.creditApplyCopyFileRelation(relationType, relationId, relationType2, relationId2);
			return JSON.toJSONString(jsoMap);
		} catch (Exception e) {
			jsoMap.put("error", "0");
			jsoMap.put("msg", "失败");
			Logger.error(e, "copy file_relation关系 失败======>" + e.getMessage());
			return JSON.toJSONString(jsoMap);
		}
	}

	/**
	 * @Description 返回 运营商认证 H5页面url
	 * @return
	 * @author: zj
	 */
	public static String getOperatorAuthH5URL() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		try {
			// long userId =1601;
			long userId = Security.checkSign(request.params.get("userId"), Constants.USER_ID_SIGN, Constants.VALID_TIME,
					error);
			String themeColor = params.get("themeColor");
			String calbackUrl = "http://" + request.domain + ":" + request.port;
			Logger.info(calbackUrl);
			resultMap.put("error", "-1");
			resultMap.put("msg", "成功");
			resultMap.put("url", MjkjService.getOperatorH5Url(userId, themeColor, calbackUrl));
			// 获取h5页面结果
			String result = JSON.toJSONString(resultMap);
			Logger.info("获取h5页面结果===========>" + result);
		} catch (Exception e) {
			resultMap.put("error", "0");
			resultMap.put("msg", "失败");
			Logger.error(e, "获取摩羯h5页面异常==========>" + e.getMessage());
			return JSON.toJSONString(resultMap);
		}

		return JSON.toJSONString(resultMap);
	}
}
