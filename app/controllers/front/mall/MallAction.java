package controllers.front.mall;

import java.util.Date;
import java.util.List;
import java.util.Map;

import models.t_mall_address;
import models.t_mall_goods;
import models.t_mall_scroe_record;
import models.t_mall_scroe_rule;
import models.t_red_packages_type;
import models.v_mall_goods_views;
import models.v_news_types;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;
import utils.TimeUtil;
import business.DictBanksDate;
import business.MallAddress;
import business.MallGoods;
import business.MallScroeRecord;
import business.MallScroeRule;
import business.News;
import business.NewsType;
import business.RedPackageHistory;
import business.User;

import com.google.gson.Gson;

import constants.Constants;
import constants.MallConstants;
import controllers.BaseController;
import controllers.front.account.LoginAndRegisterAction;

/**
 * 积分商城
 * 
 * @author yuy
 * @time 2015-10-16 16:30
 *
 */

public class MallAction extends BaseController {

	
	public static void home2() {
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
		if (pageSize == 0) {
			pageSize = 5;
		}

		PageBean<v_mall_goods_views> pageBean = MallGoods.queryHasExchangedGoodsOrder(currPage, pageSize, error);
		render(pageBean);
	}


	/**
	 * 商城指引/常见问题 详情
	 */
	public static void contentDetail(long newsId, int type) {

		ErrorInfo error = new ErrorInfo();

		List<v_news_types> types = NewsType.queryTypeAndCount(type, error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}

		List<News> newses = News.queryNewsDetail(newsId + "", null, error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}

		Map<String, String> newsCount = News.queryCount(error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}

		render(types, type, newses, newsCount);
	}

	/**
	 * 已兑换商品列表
	 */
	public static void exchangedGoods() {
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();

		List<Map<String, Object>> exgoodsList = MallGoods.queryHasExchangedGoodsByList(user.id, error);
		if (error.code < 0) {
			flash.error("抱歉，系统出错，请联系管理员");
		}
		render(exgoodsList);
	}

	/**
	 * 个人积分记录
	 */
	public static void scroeRecord() {
		User user = User.currUser();
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
		PageBean<t_mall_scroe_record> page = MallScroeRecord.queryScroeRecordByPage(user.id, null, 0, null, null, currPage, pageSize, 0, error);
		render(page);
	}

	/**
	 * 签到
	 */
	public static void sign(String user_id) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		User user = new User();
		long userId = Security.checkSign(user_id, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			json.put("error", error);
			renderJSON(json);
		}
		user.id = userId;

		Date today = new Date();

//		// 验证签到积分规则
//		t_mall_scroe_rule signRule = MallScroeRule.queryRuleDetailByType(MallConstants.SIGN);
//		if (signRule == null) {
//			error.code = MallConstants.COM_ERROR_CODE;
//			error.msg = "对不起，签到送积分活动暂停";
//			json.put("error", error);
//			renderJSON(json);
//		}
//
//		json.put("time", TimeUtil.dateToStrCurr(today));// yyyy年MM月dd日
//		json.put("scroe", signRule.scroe);

		// 验证当日是否签过
		String dateStr = TimeUtil.dateToStrDate(today);// yyyy-MM-dd
		int count = MallScroeRecord.queryScoreRecordByDate(user.id, dateStr, MallConstants.SIGN, error);
		if (error.code < 0) {
			error.msg = "对不起，签到失败，请联系客服";
			json.put("error", error);
			renderJSON(json);
		}

		if (count > 0) {
			error.code = -100;
			error.msg = "亲，今天您已签过到了";
			json.put("error", error);
			renderJSON(json);
		}

		// 下次获取积分数获取
		int[] score = MallScroeRecord.queryScroeRecord(userId, error);

		if (error.code < 0) {
			error.code = -101;
			error.msg = "获取积分数失败";
			json.put("error", error);
			renderJSON(json);
		}

		MallScroeRecord.saveScroeSignRecord(user, score, error);
		if (error.code < 0) {
			error.msg = "对不起，签到失败，请联系客服";
		} else {
			error.msg = "亲，签到成功";
		}
		json.put("error", error);
		renderJSON(json);
	}

	/**
	 * 商品详情
	 * 
	 * @param goods_id
	 */
	public static void goodsDetail(long goods_id) {
		User user = User.currUser();
		if (null == user) {
			LoginAndRegisterAction.logining();
		}
		t_mall_goods goods = MallGoods.queryGoodsDetailById(goods_id);
		render(goods);
	}

	/**
	 * 开始兑换 查询收货地址
	 */
	public static void exchange() {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		User user = User.currUser();
		// 查询收货地址
		List<t_mall_address> addressList = MallAddress.queryAddressByList(user.id, error);
		if (error.code < 0) {
			error.code = MallConstants.COM_ERROR_CODE;
			error.msg = "对不起，系统出错，请联系客服";
			json.put("error", error);
			renderJSON(json);
		}
		renderJSON(new Gson().toJson(addressList));
	}

	/**
	 * 添加地址
	 */
	public static void addAddress() {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		User user = new User();

		String address_id_str = params.get("address_id");
		String receiver = params.get("receiver");
		String tel = params.get("tel");
		String where = params.get("address");
		String province_id = params.get("province");
		String city_id = params.get("city");
		String isDefault = params.get("is_default");
		String user_id = params.get("user_id");
		String mall_goods_id = params.get("mall_goods_id");

		boolean is_default = false;
		if (isDefault != null && "true".equals(isDefault)) {
			is_default = true;
		}

		long userId = Security.checkSign(user_id, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			json.put("error", error);
			renderJSON(json);
		}
		user.id = userId;

		long address_id = 0;
		if (StringUtils.isNotBlank(address_id_str)) {
			address_id = Long.parseLong(address_id_str);
		}

		if (address_id > 0) {

			t_mall_goods goods = new t_mall_goods();
			goods = t_mall_goods.findById(Long.valueOf(mall_goods_id));

			t_mall_address address = t_mall_address.findById(address_id);
			String province = DictBanksDate.queryAdProvinceByCode(address.province_id);
			String city = DictBanksDate.queryAdCityByCode(address.city_id);
			String postDetail = address.receiver + " " + address.tel + " " + province + city + " " + address.address;

			// 添加兑换记录
			MallScroeRecord.saveScroeExchangeRecord(1, user, goods, postDetail, error);

			if (error.code < 0) {
				JPA.setRollbackOnly();
				json.put("error", error);
				renderJSON(json);
			}

			// 更新商品记录
			MallGoods.updateSurplus(1, user.id, Long.valueOf(mall_goods_id), error);

			error.code = MallConstants.SUCCESS_CODE;
			json.put("error", error);
			renderJSON(json);
		}

		// 查询收货地址
		t_mall_address address = new t_mall_address();
		address.id = address_id == 0 ? null : address_id;
		address.user_id = user.id;
		address.time = new Date();
		address.receiver = receiver;
		address.tel = tel;
		address.address = where;
		address.province_id = Integer.parseInt(province_id);
		address.city_id = Integer.parseInt(city_id);
		address.is_default = is_default;
		if (is_default) {
			boolean has_default = MallAddress.queryAddressHasDefault(user.id, error);
			if (has_default) {
				error.code = MallConstants.COM_ERROR_CODE;
				error.msg = "只能设置一个默认地址";
				json.put("error", error);
				renderJSON(json);
			}
		}
		int result = MallAddress.saveAddress(address);
		if (result < 0) {
			error.code = MallConstants.COM_ERROR_CODE;
			error.msg = "对不起，系统出错，请联系客服";
			json.put("error", error);
			renderJSON(json);
		} else {

			t_mall_goods goods = new t_mall_goods();
			goods = t_mall_goods.findById(Long.valueOf(mall_goods_id));

			String province = DictBanksDate.queryAdProvinceByCode(Integer.parseInt(province_id));
			String city = DictBanksDate.queryAdCityByCode(Integer.parseInt(city_id));
			String postDetail = address.receiver + " " + address.tel + " " + province + city + " " + address.address;

			// 添加兑换记录
			MallScroeRecord.saveScroeExchangeRecord(1, user, goods, postDetail, error);

			if (error.code < 0) {
				JPA.setRollbackOnly();
				json.put("error", error);
				renderJSON(json);
			}

			// 更新商品记录
			MallGoods.updateSurplus(1, user.id, Long.valueOf(mall_goods_id), error);
		}
		error.code = MallConstants.SUCCESS_CODE;
		json.put("error", error);
		renderJSON(json);
	}

	/**
	 * 积分商城——积分兑换红包
	 */
	public static void scroeConvertRedPackages() {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		User user = new User();

		String id = params.get("id");
		String user_id = params.get("user_id");

		long userId = Security.checkSign(user_id, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			json.put("error", error);
			renderJSON(json);
		}
		user.id = userId;

		if (id == null || "".equals(id))
		{
			error.code = -1;
			error.msg = "兑换红包信息异常";
			json.put("error", error);
			renderJSON(json);
		}
		t_red_packages_type red = t_red_packages_type.findById(Long.valueOf(id));
		if (red == null || red.id <= 0) {
			error.code = -1;
			error.msg = "兑换红包信息异常";
			json.put("error", error);
			renderJSON(json);
		}

		String postDetail = "";
		// 添加兑换记录
		MallScroeRecord.saveScroeExchangeRedRecord(1, user, red, postDetail, error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			json.put("error", error);
			renderJSON(json);
		}

		// 更新红包记录
		String desc = "APP积分兑换红包";
		RedPackageHistory.scroeExchangeRedPack(user, red, desc, error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			json.put("error", error);
			renderJSON(json);
		}

		error.code = MallConstants.SUCCESS_CODE;
		json.put("error", error);
		renderJSON(json);

		error.code = MallConstants.SUCCESS_CODE;
		json.put("error", error);
		renderJSON(json);
	}

	/**
	 * 修改地址
	 */
	public static void modifyAddress() {
		JSONObject json = new JSONObject();

		String address_id_str = params.get("address_id");
		long address_id = 0;
		if (StringUtils.isNotBlank(address_id_str)) {
			address_id = Long.parseLong(address_id_str);
		}

		// 查询收货地址
		t_mall_address address = MallAddress.queryAddressById(address_id);
		json.put("address", address);
		renderJSON(json);
	}

	public static void updateAddress() {
		ErrorInfo error = new ErrorInfo();
		String address = params.get("address");
		String remark = params.get("remark");
		String rid = params.get("rid");
		String address3 = "";
		if (address != null && !address.equals("")) {
			String[] address2 = address.split(" ");
			for (int i = 0; i < address2.length; i++) {
				if (i == 0) {
					address3 += "收货地址：" + address2[0] + " ";
				}
				if (i == 1) {
					address3 += "收货人：" + address2[1] + " ";
				}
				if (i == 2) {
					address3 += "手机号码：" + address2[2] + " ";
				}
			}
		}
		address3 = address3 + " 备注：" + remark;
		int code = MallScroeRecord.updateAddess(rid, address3);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("code", code);
		renderJSON(jsonObject);
	}

	/**
	 * 立即兑换
	 */
	public static void submitExchange() {
		ErrorInfo error = new ErrorInfo();
		String address = params.get("address");
		String remark = params.get("remark");
		String goods_id_str = params.get("goods_id");
		String exchangeNum_str = params.get("exchangeNum");

		long goods_id = 0;

		int exchangeNum = 0;
		if (StringUtils.isNotBlank(goods_id_str)) {
			goods_id = Long.parseLong(goods_id_str);
		}
		if (StringUtils.isNotBlank(exchangeNum_str)) {
			exchangeNum = Integer.parseInt(exchangeNum_str);
		}

		t_mall_goods goods = MallGoods.queryGoodsDetailById(goods_id);
		if (goods == null) {
			flash.error("对不起，商品已下架");
			goodsDetail(goods_id);
		}

		User user = User.currUser();
		String address3 = "";

		if (address != null && !address.equals("")) {
			String[] address2 = address.split(" ");
			for (int i = 0; i < address2.length; i++) {
				if (i == 0) {
					address3 += "收货地址：" + address2[0] + " ";
				}
				if (i == 1) {
					address3 += "收货人：" + address2[1] + " ";
				}
				if (i == 2) {
					address3 += "手机号码：" + address2[2] + " ";
				}
			}
		}

		String postDetail = address3 + " 备注：" + remark;

		MallGoods.exchangeHandle(exchangeNum, user, goods, postDetail, error);
		if (error.code == Constants.ALREADY_RUN) {// 没有更新到
			flash.error("对不起，您的积分不足或者该商品已兑换完毕，请兑换其他商品");
			goodsDetail(goods_id);
		}
		if (error.code < 0) {
			flash.error("对不起，兑换失败，请联系客服或稍候重试");
			goodsDetail(goods_id);
		}
		flash.error("兑换成功");
		goodsDetail(goods_id);
	}
	
	public static void logout() {
		User user = User.currUser();

		if (user == null) {
			LoginAndRegisterAction.login();
		}

		ErrorInfo error = new ErrorInfo();

		user.logout(error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("error", error);
		renderJSON(jsonObject);
	}
}
