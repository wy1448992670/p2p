package controllers.supervisor.userManager;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import com.alibaba.fastjson.JSON;

import models.v_user_cps_info;
import utils.CharsetUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.PageBean;
import business.BillInvests;
import business.Product;
import business.User;
import constants.Constants;
import controllers.supervisor.SupervisorController;

/**
 * 
 * 类名:BankInsur
 * 功能:银行卡投保记录
 */

public class BankInsur extends SupervisorController {

	public static void list(String beginTime, String endTime, String userName, int pageSize, int currPage){
		PageBean<Map<String, Object>> result = User.findUserBankInsur(beginTime, endTime, userName, pageSize, currPage);
		System.out.println(JSON.toJSONString(result));
		render(result);
	}
	
}
