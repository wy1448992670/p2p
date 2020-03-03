package controllers.ymd;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import constants.FileRelationTypeEnum;
import constants.RiskReportIsValidStatusEnum;
import constants.RiskReportStatusEnum;
import controllers.supervisor.SupervisorController;
import models.t_users;
import models.risk.t_risk_report;
import net.sf.json.JSONObject;
import play.Logger;
import services.RiskReportService;
import services.UserService;
import services.ymd.FileHelperService;
import utils.JSONUtils;
import utils.mjkj.MjkjService;

/**
 * @ClassName BackFileUploadController
 * @Description 后台文件上传接口
 * @author zj
 * @Date Jan 22, 2019 8:25:38 PM
 * @version 1.0.0
 */

public class BackFileUploadController extends SupervisorController {

	/**
	 * @Description 文件上传
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
	 * @Description 所有图片一起提交(添加医院机构使用)<br>
	 *              (接收的参数结构：图片的url )
	 * @author: zj
	 */
	public static String allFileSubmit() {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "成功");
		try {
			// 上传流程类型
			int relationType = FileRelationTypeEnum.ORGNIZATION.getCode();
			long relationId = Long.parseLong(params.get("orgId"));// 组织结构id
			List<String> result = FileHelperService.allFileSubmit(relationType, relationId, request);
			Logger.info("医院机构所有文件提交返回信息========>" + result);
			jsonMap.put("data", result);
			Logger.info("医院机构所有文件提交返回信息json========>" + JSON.toJSONString(jsonMap));
			return JSON.toJSONString(jsonMap);
		} catch (Exception e) {
			jsonMap.put("error", "0");
			jsonMap.put("msg", "失败");
			Logger.error(e, "医院机构文件提交异常=====>" + e.getMessage());
			return JSON.toJSONString(jsonMap);
		}

	}

	/**
	 * @Description 删除文件关系
	 * @return
	 * @author: zj
	 */
	public static String delFileRelation() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("error", "-1");
		map.put("msg", "成功");
		try {

			long fileRelationId = Long.parseLong(params.get("fileRelationId"));
			FileHelperService.delFileRelationById(fileRelationId);
			return JSON.toJSONString(map);
		} catch (Exception e) {
			map.put("error", "0");
			map.put("msg", "失败");
			Logger.error(e, "删除文件关系出错======>" + e.getMessage());
			return JSON.toJSONString(map);
		}
	}

	/**
	 * @Description 摩羯H5授权成功回调接口
	 * @return
	 * @author: zj
	 */
	public static void getOperatorAuthCallBack() {
		Logger.info("===========摩羯H5授权登录回调开始=============");
		String resultString = "false";
		String mxcode = params.get("mxcode");
		String loginDone = params.get("loginDone");
		String message = params.get("message");
		String taskId = params.get("taskId");
		String userId = params.get("userId");

		if (userId == null) {
			userId = "0";
		}

		Logger.info("mxcode=========>" + mxcode + ",loginDone=========>" + loginDone);
		Logger.info("message=====>" + message);
		Logger.info("taskId========>" + taskId);

		t_risk_report report = new t_risk_report();
		report.user_id = Long.parseLong(userId);

		if ("2".equals(mxcode) && "1".equals(loginDone)) {
			report.status = RiskReportStatusEnum.CREATE.getCode();
			report.is_valid = RiskReportIsValidStatusEnum.INVALID.getCode();// 无效

			resultString = "success";
			UserService.updateUser(report.user_id, 1);

		} else {
			UserService.updateUser(report.user_id, -1);
			report.status = RiskReportStatusEnum.FAIL.getCode();// 失败
			report.is_valid = RiskReportIsValidStatusEnum.INVALID.getCode();// 无效
		}
		report.h5_response_task_id = taskId;
		RiskReportService.addRiskReportFirst(report);
		Logger.info("resultString=========>" + resultString);
		render(resultString);
	}

	/**
	 * @Description 报告通知回调
	 * @author: zj
	 */
	public static void getOperatorAuthResultCallBack() {
		StringBuilder sb = new StringBuilder();
		String line = "";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(request.body));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			String str = sb.toString();
			MjkjService.parseReportResult(str);
			Logger.info("用户资信报告通知结果===========>" + str);
			response.status = 201;// 全部成功后返回201状态给摩羯 否则会重复回调此接口
		} catch (Exception e) {
			Logger.error(e, "获取资信报告异常======>" + e.getMessage());
		}
	}

}
