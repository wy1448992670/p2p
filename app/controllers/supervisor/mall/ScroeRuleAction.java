package controllers.supervisor.mall;

import java.util.Date;

import models.t_mall_scroe_rule;

import org.apache.commons.lang.StringUtils;

import utils.ErrorInfo;
import utils.PageBean;
import business.BackstageSet;
import business.MallScroeRule;
import constants.MallConstants;
import controllers.supervisor.SupervisorController;

/**
 * 积分商城：规则
 * 
 * @author yuy
 * @created 2015-10-14
 */
public class ScroeRuleAction extends SupervisorController {

	/**
	 * 规则列表
	 */
	public static void scroeRuleList() {
		ErrorInfo error = new ErrorInfo();
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		int currPage = 0;
		int pageSize = 0;
		if (StringUtils.isNotBlank(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}
		if (StringUtils.isNotBlank(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}
		PageBean<t_mall_scroe_rule> page = MallScroeRule.queryMallScroeRuleByPage(currPage, pageSize, error);
		if (error.code < 0) {
			flash.error("抱歉，系统出错，请联系管理员");
		}

		double scroeLine = MallConstants.MALL_INVEST_SENDSCROE_LINE;
		render(page, scroeLine);
	}

	/**
	 * 保存规则
	 */
	public static void saveScroeRule() {
		String id = params.get("id");
		String typeStr = params.get("type");
		String scroeStr = params.get("scroe");

		t_mall_scroe_rule rule = new t_mall_scroe_rule();
		rule.id = StringUtils.isNotBlank(id) ? Long.parseLong(id) : null;
		rule.type = StringUtils.isNotBlank(typeStr) ? Integer.parseInt(typeStr) : 0;
		rule.time = new Date();
		rule.scroe = StringUtils.isNotBlank(scroeStr) ? Integer.parseInt(scroeStr) : 0;
		rule.status = MallConstants.STATUS_ENABLE;

		int result = MallScroeRule.saveRuleDetail(rule);
		if (result < 0) {
			if (result == MallConstants.DATA_DUPL_CODE)
				flash.error("抱歉，相同类型的规则已存在");
			else
				flash.error("抱歉，保存失败，请联系管理员");
		} else {
			flash.error("保存成功");
		}
		scroeRuleList();
	}

	/**
	 * 编辑规则 页面
	 * 
	 * @param id
	 * @param flag
	 *            1:新增 2：修改
	 */
	public static void editScroeRule(long id, int flag) {
		t_mall_scroe_rule rule = null;
		if (flag == MallConstants.MODIFY)
			rule = MallScroeRule.queryRuleDetailById(id);
		render(rule, flag);
	}

	/**
	 * 暂停规则
	 * 
	 * @param id
	 */
	public static void stopScroeRule(long id, int status) {
		int result = MallScroeRule.stopRule(id, status);
		String opeStr = status == MallConstants.STATUS_ENABLE ? MallConstants.STR_ENABLE : MallConstants.STR_DISABLE;
		if (result < 0) {
			flash.error("抱歉，%s失败，请联系管理员", opeStr);
		} else {
			flash.error("%s成功", opeStr);
		}
		scroeRuleList();
	}

	/**
	 * 添加积分商城页面
	 * 
	 * @param id
	 */
	public static void addScroeRule() {
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();

		render(backstageSet);
	}
}
