package controllers.ymd;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.apache.commons.lang.StringUtils;
import org.junit.rules.TestWatcher;

import business.ReportUserInfo;
import controllers.BaseController;

import play.db.jpa.JPA;
import play.db.jpa.JPABase;
import play.db.jpa.Transactional;
import services.UserService;
import services.ymd.FileHelperService;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.PageBean;
import utils.faceId.FaceService;
import utils.xhj.XhjService;


public class ReportUserInfoController extends BaseController {

	/**
	 * 运营商报告 用户通讯信息
	 * @throws Exception 
	 */
	public static void reportContactDetail() throws Exception {

		String idCard = params.get("idCard");
		String mobile = params.get("mobile");

		ErrorInfo error = new ErrorInfo();
		
		if (StringUtils.isEmpty(idCard)) {
			error.code = -1;
			error.msg = "用户身份证号不能为空！";
			render(error);
		}
		if (StringUtils.isEmpty(mobile)) {
			error.code = -1;
			error.msg = "用户手机号不能为空！";
			render(error);
		}

		PageBean<Map<String, Object>> contactDetail = ReportUserInfo.getReportContactDetail(params);
		
		render(contactDetail);
	}
	
	
}
