package controllers.supervisor.mall;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import business.MallGoods;
import constants.Constants;
import constants.MallConstants;
import constants.Templets;
import controllers.supervisor.SupervisorController;
import models.t_mall_goods;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 积分商城：商品
 * 
 * @author yuy
 * @created 2015-10-14
 */
public class GoodsAction extends SupervisorController {

	/**
	 * 商品列表
	 */
	public static void goodsList() {
		ErrorInfo error = new ErrorInfo();
		String orderType = params.get("orderType");
		String orderStatus = params.get("orderStatus");
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		String name = params.get("name");
		String statusStr = params.get("status");
		int currPage = 0;
		int pageSize = 0;
		int status = 0;
		if (StringUtils.isNotBlank(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}
		if (StringUtils.isNotBlank(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}
		if (StringUtils.isNotBlank(statusStr)) {
			status = Integer.parseInt(statusStr);
		}
		PageBean<t_mall_goods> page = MallGoods.queryMallGoodsByPage(name, status, orderType, orderStatus, currPage, pageSize, error);
		if (error.code < 0) {
			flash.error("抱歉，系统出错，请联系管理员");
		}
		render(page);
	}

	/**
	 * 保存商品
	 */
	public static void saveGoods() {
		String id = params.get("id");
		String name = params.get("name");
		String picPath = params.get("filename");
		String introduction = params.get("introduction");
		String countStr = params.get("total");
		String maxExchangeCountStr = params.get("max_exchange_count");
		String exchangeScroeStr = params.get("exchange_scroe");

		try {
			introduction = URLDecoder.decode(introduction, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			flash.error("保存失败");
			goodsList();
		}
		if (StringUtils.length(introduction) > 16777215) {
			flash.error("对不起,商品介绍字数不能超过16777215个字符");
			goodsList();
		}
		introduction = Templets.replaceAllHTML(introduction);

		if (StringUtils.isBlank(picPath) || picPath.contains(Constants.DEFAULT_IMAGE)) {
			flash.error("请选择上传商品图片");
			goodsList();
		}

		t_mall_goods goods = new t_mall_goods();
		goods.id = StringUtils.isNotBlank(id) ? Long.parseLong(id) : null;
		goods.name = name;
		goods.time = new Date();
		goods.pic_path = picPath;
		goods.introduction = introduction;
		goods.total = StringUtils.isNotBlank(countStr) ? Integer.parseInt(countStr) : 0;
		goods.max_exchange_count = StringUtils.isNotBlank(maxExchangeCountStr) ? Integer.parseInt(maxExchangeCountStr) : 0;
		goods.surplus = goods.max_exchange_count;
		goods.exchange_scroe = StringUtils.isNotBlank(exchangeScroeStr) ? Integer.parseInt(exchangeScroeStr) : 0;
		goods.status = MallConstants.STATUS_ENABLE;
		goods.visible = MallConstants.VISIBLE;

		int result = MallGoods.saveGoodsDetail(goods);
		if (result < 0) {
			flash.error("抱歉，保存失败，请联系管理员");
		} else {
			flash.error("保存成功");
		}
		goodsList();
	}

	/**
	 * 编辑商品 页面
	 * 
	 * @param id
	 * @param flag
	 *            1:新增 2：修改
	 */
	public static void editGoods(long id, int flag) {
		t_mall_goods goods = null;
		if (flag == MallConstants.MODIFY)
			goods = MallGoods.queryGoodsDetailById(id);
		render(goods, flag);
	}

	/**
	 * 删除商品
	 * 
	 * @param id
	 */
	public static void deleteGoods(long id) {
		int result = MallGoods.deleteGoodsDetail(id);
		if (result < 0) {
			flash.error("抱歉，删除失败，请联系管理员");
		} else {
			flash.error("删除成功");
		}
		goodsList();
	}

	/**
	 * 暂停/开启商品兑换
	 * 
	 * @param id
	 */
	public static void stopGoods(long id, int status) {
		int result = MallGoods.stopGoodsExchange(id, status);
		String opeStr = status == MallConstants.STATUS_ENABLE ? MallConstants.STR_ENABLE : MallConstants.STR_DISABLE;
		if (result < 0) {
			flash.error("抱歉，%s失败，请联系管理员", opeStr);
		} else {
			flash.error("%s成功", opeStr);
		}
		goodsList();
	}
}
