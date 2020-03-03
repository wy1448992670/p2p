package controllers.wechat.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import models.v_debt_auction_records;
import models.v_front_all_debts;

import business.Debt;
import business.User;
import business.UserAuditItem;

import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import constants.Constants;

import controllers.BaseController;
import controllers.wechat.account.WechatAccountHome;

/**
 * 债权转让相关
 * 
 * @author Administrator
 *
 */
public class TransferAction extends BaseController {

	/**
	 * 查询第一页债权转让
	 */
	public static void queryAllTransfers() {

		ErrorInfo error = new ErrorInfo();

		String orderType = params.get("orderType");
		String keywords = params.get("keywords");

		PageBean<v_front_all_debts> pageBean = Debt.queryAllDebtTransfers(
				Constants.WECHAT_CURRPAGE, Constants.WECHAT_PAGESIZE, null,
				null, null, orderType, keywords, error);

		if (error.code < 0) {
			flash.error(error.msg);
			WechatAccountHome.errorShow(error.msg);
		}

		render(pageBean);
	}

	/**
	 * ajax查看债权转让
	 */
	public static void queryAllTransfersAjax() {
		ErrorInfo error = new ErrorInfo();

		int currPage = Constants.WECHAT_CURRPAGE;
		int pageSize = Constants.WECHAT_PAGESIZE;

		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		String orderType = params.get("orderType");
		String keywords = params.get("keywords");
		PageBean<v_front_all_debts> pageBean = Debt.queryAllDebtTransfers(
				currPage, pageSize, null, null, null, orderType, keywords,
				error);

		if (error.code < 0) {
			flash.error(error.msg);
			WechatAccountHome.errorShow(error.msg);
		}

		List<v_front_all_debts> page = pageBean.page;
		List<Map<String, String>> listMap = new ArrayList<Map<String, String>>();
		if (page != null) {
			for (int i = 0; i < page.size(); i++) {
				Map<String, String> map = new HashMap<String, String>();
				String end_time = page.get(i).end_time.getTime() + "";
				if (page.get(i).repayment_time != null) {
					String repayment_time = new SimpleDateFormat("yyyy-MM-dd")
							.format(page.get(i).repayment_time);
					map.put("repayment_time", repayment_time);
				} else {
					map.put("repayment_time", "");
				}
				String debt_amount = NumberUtil
						.amountFormat(page.get(i).debt_amount);
				String transfer_price = NumberUtil
						.amountFormat(page.get(i).transfer_price);
				Double max_price = page.get(i).max_price;
				if (max_price == null) {
					map.put("max_price", "0");
				} else {
					map.put("max_price",
							NumberUtil.amountFormat(page.get(i).max_price));
				}
				map.put("end_time", end_time);
				map.put("debt_amount", debt_amount);
				map.put("transfer_price", transfer_price);

				listMap.add(map);
			}
		}

		JSONObject json = new JSONObject();
		json.put("error", error);
		json.put("pageBean", pageBean);
		json.put("listMap", listMap);

		renderJSON(json);

	}

	/**
	 * 查询债权转让详情信息
	 * 
	 * @param debtId
	 * @param small_image_filename
	 * @param success
	 * @param description
	 */
	public static void queryTransferDetail(long debtId, String small_image_filename) {
		ErrorInfo error = new ErrorInfo();

		Debt debt = new Debt();
		debt.id = debtId;

		User user = User.currUser();
		Long bidUserId = Debt.getBidUserId(debtId, error);

		if (error.code < 0) {
			flash.error(error.msg);
			WechatAccountHome.errorShow(error.msg);
		}

		Map<String, String> historySituationMap = User.historySituation(
				bidUserId, error);// 借款者历史记录情况

		if (error.code < 0) {
			flash.error(error.msg);
			WechatAccountHome.errorShow(error.msg);
		}

		Long investUserId = Debt.getInvestUserId(debtId, error);

		if (error.code < 0) {
			flash.error(error.msg);
			WechatAccountHome.errorShow(error.msg);
		}

		Map<String, String> debtUserhistorySituationMap = new HashMap<String, String>();

		debtUserhistorySituationMap = User.debtUserhistorySituation(
				investUserId, error);// 债权者历史记录情况

		if (error.code < 0) {
			flash.error(error.msg);
			WechatAccountHome.errorShow(error.msg);
		}

		List<UserAuditItem> uItems = UserAuditItem.queryUserAllAuditItem(
				bidUserId, debt.invest.bid.mark); // 用户正对产品上传的资料集合

		/*
		 * 查找前10条竞拍记录
		 */
		PageBean<v_debt_auction_records> pageBean = Debt
				.queryDebtAllAuctionRecords(Constants.WECHAT_CURRPAGE, Constants.WECHAT_PAGESIZE,
						debtId, error);

		if (error.code < 0) {
			flash.error(error.msg);
			WechatAccountHome.errorShow(error.msg);
		}
		
		render(debt, user, historySituationMap, debtUserhistorySituationMap,
				 uItems, small_image_filename, pageBean);
	}

	/**
	 * ajax查询债权竞拍记录
	 * @param debtId
	 */
	public static void queryTransferAuctionRecordsAjax(long debtId){
		
		ErrorInfo error = new ErrorInfo();
		
		int currPage = Constants.WECHAT_CURRPAGE;
		int pageSize = Constants.WECHAT_PAGESIZE;

		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}
		
		PageBean<v_debt_auction_records> pageBean = Debt.queryDebtAllAuctionRecords(currPage, pageSize, debtId,error);
		
		JSONObject json = new JSONObject();
		json.put("pageBean", pageBean);
		json.put("error", error);
		renderJSON(json);
		
	}
	/**
	 * 竞拍债权 AJAX
	 * 
	 * @param debtId
	 * @param small_image_filename
	 */
	public static void transfer(long debtId, String small_image_filename, String price, String password) {
		JSONObject json = new JSONObject();
		User user = User.currUser();

		if (null == user) {
			RegistAndLogin.login();
		}

		if (User.currUser().simulateLogin != null) {
			if (User.currUser().simulateLogin.equalsIgnoreCase(User.encrypt())) {
				flash.error("模拟登录不能进行该操作");
				String url = request.headers.get("referer").value();
				redirect(url);
			} else {
				flash.error("模拟登录超时，请重新操作");
				String url = request.headers.get("referer").value();
				redirect(url);
			}
		}

		/*
		 * if (Constants.IPS_ENABLE && (User.currUser().getIpsStatus() !=
		 * IpsCheckStatus.IPS)) { CheckAction.approve(); }
		 */
		String offerPriceStr = price;
		String dealpwd = password;
		ErrorInfo error = new ErrorInfo();

		if (StringUtils.isBlank(offerPriceStr)) {
		
			json.put("code", "-1");
			json.put("msg", "对不起！竞拍价格不能为空！");
			
			renderJSON(json);
		}

		boolean b = offerPriceStr.matches("^[1-9][0-9]*$");
		if (!b) {
			
			json.put("code", "-1");
			json.put("msg", "对不起！竞拍价格只能是正整数！");
			
			renderJSON(json);
		}

		int offerPrice = Integer.parseInt(offerPriceStr);
		Debt.auctionDebt(user.id, offerPrice, debtId, dealpwd, Constants.CLIENT_WECHAT, error);

		if (error.code < 0) {
			
			json.put("code", error.code);
			json.put("msg", error.msg);
			
			renderJSON(json);
		} else {
			
			
			json.put("code", 1);
			json.put("msg", "竞拍成功");
			
			renderJSON(json);
			//WechatAccountHome.debeManage(2, error.msg);
			//queryTransferDetail(debtId, small_image_filename);
		}
	}

	/**
	 * 分享
	 */
	public static void shareTransferPage() {

		render();
	}
}
