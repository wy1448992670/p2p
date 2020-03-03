package controllers.wechat.others;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import play.Logger;
import services.WeChatGongZhongService;
import services.WeChatMenuManageService;
import services.WeChatReceiveMessageService;
import services.WeChatWebOAuthManageService;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.WeChatUtil;
import business.BackstageSet;
import business.User;
import business.WeChatMenu;

import com.shove.gateway.weixin.gongzhong.vo.menu.Menu;
import com.shove.gateway.weixin.gongzhong.vo.weboauth.OauthAccessToken;

import constants.Constants;

import controllers.WeiXinController;
import controllers.supervisor.webContentManager.NewsManageAction;
import controllers.wechat.account.WechatAccountHome;
import controllers.wechat.service.BidAction;
import controllers.wechat.service.InvestAction;
import controllers.wechat.service.RegistAndLogin;
import controllers.wechat.service.TransferAction;

public class WeChatFrontGateway extends WeiXinController {

	/**
	 * 微信界面和用户进行沟通入口,包括用户发送消息，点击click事件
	 * 
	 * @throws Exception
	 */
	public static void microMessageEntrance() throws Exception {
		WeChatReceiveMessageService weChatReceiveMessageService = new WeChatReceiveMessageService();
		// 执行方法
		WeChatGongZhongService.execute(weChatReceiveMessageService);
	}

	/**
	 * 授权回调地址。 微信端用户点击view菜单事件，回调到这个地址。用户必须先授权（得到用户的openId)，才能判定用户是否绑定
	 */
	public static void userOAuth() {
		String code = params.get("code");
		String state = params.get("state");

		if (null != state && state.contains("#")) {
			Logger.info("转换state,state包含#:%s", state);
			state = state.split("#")[0];
			Logger.info("转换state之后：%s", state);
		} else {
			Logger.info("state不包含#:%s", state);
		}
		Logger.info("code:%s", code);
		// 同意授权
		if (!"authdeny".equals(code)) {
			try {
				Logger.info("%s", "获取oauthAccessToken之前");
				OauthAccessToken oauthAccessToken = WeChatWebOAuthManageService
						.getAccessToken(code);
				// 获取网页访问凭证
				//String accessToken = oauthAccessToken.getAccess_token();
				// 获取网页用户标识
				String openId = oauthAccessToken.getOpenid();
				Logger.info("openId:%s", openId);
				// 获取用户信息
				// UserInfo userInfo =
				// WeChatWebOAuthManageService.getUserInfo(accessToken, openId);
				// 查询用户是否绑定
				boolean flag = User.isBind(openId, new ErrorInfo());
				int opt = Integer.parseInt(state);
				if (flag) {
					/*
					 * 防止用户绑定了，再继续点击绑定，那么就让用户跳到解除绑定页面
					 */
					if (41 == opt) {
						// 如果绑定了，还点击绑定按钮，那么跳到解除绑定页面
						// flash.error("您的微信号已经绑定过账号，不能再次绑定");
						// WechatAccountHome.errorShow();
						// 解绑方法
						Logger.info("%s", "用户正在解绑");
						// 解绑过程中对OpenID进行Encrypt.encrypt3DES(arg0, arg1)加密
						openId = WeChatUtil.encryptOpenId(openId);
						RegistAndLogin.unBoundUser(openId);
					}

					/*
					 * 解绑
					 */
					if (42 == opt) {
						// 解绑方法
						Logger.info("%s", "用户正在解绑");
						// 解绑过程中对OpenID进行Encrypt.encrypt3DES(arg0, arg1)加密
						openId = WeChatUtil.encryptOpenId(openId);
						RegistAndLogin.unBoundUser(openId);
					}

					ErrorInfo error = new ErrorInfo();
					Map<String, String> map = User
							.findAccountAndPasswordByOpenId(openId, error);
					String account = map.get("account");
					String password = map.get("password");

					// 如果绑定，帮助当前用户直接登录(这里已经放在缓存中去了）
					User user = new User();
					user.name = account;
					// 以加密形式登录
					user.login(password, true, Constants.CLIENT_WECHAT, error);
					BackstageSet.getCurrentBackstageSet();
					// 其它按钮，都是跳转到指定的页面
					switch (opt) {
					case 11:
						RegistAndLogin.register();
						break;
					case 12:
						InvestAction.queryAllBids();
						break;
					case 13:
						BidAction.queryAllProducts();
						break;
					case 14:
						TransferAction.queryAllTransfers();
						break;
					case 21:
						WechatAccountHome.accountInfo();
						break;
					case 22:
						WechatAccountHome.myLoanBids(0, null,
								Constants.WECHAT_CURRPAGE, 0);
						break;
					case 23:
						WechatAccountHome.myInvestBids(0,
								Constants.WECHAT_CURRPAGE, null, 0);
						break;
					case 24:
						WechatAccountHome.transferDebts(
								Constants.WECHAT_CURRPAGE,
								Constants.WECHAT_PAGESIZE, null, null, null, 0);
						break;
					case 31:
						AboutUs.aboutTeam();
						break;
					case 32:
						WealthInfomation.wealthinfos(0);
						break;
					case 33:
						InterestCalculator.wealthToolkitCreditCalculator();
						break;
					}
				} else {
					if (42 == opt) {
						/*
						 * 如果用户已经解绑，再点击解绑，那么跳转到绑定界面
						 */
						Logger.info("%s", "用户正在绑定");
						// 绑定过程中对OpenID进行Encrypt.encrypt3DES(arg0, arg1)加密
						openId = WeChatUtil.encryptOpenId(openId);
						RegistAndLogin.bindUser(openId);
					}

					if (41 == opt) {
						// 如果是绑定按钮，则绑定
						Logger.info("%s", "用户正在绑定");
						// 绑定过程中对OpenID进行Encrypt.encrypt3DES(arg0, arg1)加密
						openId = WeChatUtil.encryptOpenId(openId);
						RegistAndLogin.bindUser(openId);
					} else {
						// 用户没有绑定，如果是必须要登录的操作，那么必须先登录
						switch (opt) {
						// 以下这些操作不需要登录
						case 11:
							RegistAndLogin.register();
							break;
						case 12:
							InvestAction.queryAllBids();
							break;
						case 13:
							BidAction.queryAllProducts();
							break;
						case 14:
							TransferAction.queryAllTransfers();
							break;
						case 31:
							AboutUs.aboutTeam();
							break;
						case 32:
							WealthInfomation.wealthinfos(0);
							break;
						case 33:
							InterestCalculator.wealthToolkitCreditCalculator();
							break;
						}

						// 其余的必须先进行登录操作，这时看用户有没有登录，如果缓存中有值，说明用户已经登录过了
						User user = User.currUser();
						if (null != user) {
							switch (opt) {
							// 这些操作必须要进行登录操作
							case 21:
								WechatAccountHome.accountInfo();
								break;
							case 22:
								WechatAccountHome.myLoanBids(0, null,
										Constants.WECHAT_CURRPAGE, 0);
								break;
							case 23:
								WechatAccountHome.myInvestBids(0,
										Constants.WECHAT_CURRPAGE, null, 0);
								break;
							case 24:
								WechatAccountHome.transferDebts(
										Constants.WECHAT_CURRPAGE,
										Constants.WECHAT_PAGESIZE, null, null,
										null, 0);
								break;
							}
						}

						// 如果缓存中没有值，则必须先登录
						RegistAndLogin.login();
					}
				}
			} catch (Exception e) {
				Logger.error("获取访问凭证时%s", e.getMessage());

				WechatAccountHome.errorShow(e.getMessage());
			}
		} else {
			// 用户不同意授权，那么跳转到错误提示页面

			WechatAccountHome.errorShow("您不同意授权");
		}
	}

	/**
	 * 微信多客服端插件
	 */
	public static void plugin() {

		render("wechat/plugin.html");
	}

	/**
	 * 客服端查询用户信息
	 * 
	 * @param openId
	 */
	public static void userInformation(String openId) {
		ErrorInfo error = new ErrorInfo();

		JSONObject json = new JSONObject();
		User user = User.getUserInformation(openId, error);
		if (error.code < 0 || null == user) {
			json.put("code", error.code);
			json.put("msg", error.msg);
			renderJSON(json);
		}

		json.put("user", user);

		renderJSON(json);
	}

	/**
	 * 初始化菜单
	 */
	public static void createMenu() {
		ErrorInfo error = new ErrorInfo();

		WeChatMenu.createMenu(error);

		renderText("创建成功");
	}

	/**
	 * 查询菜单
	 */
	public static void queryMenu() {
		List<Menu> list = WeChatMenuManageService.getMenu();

		renderJSON(JSONArray.fromObject(list));
	}

	/**
	 * 编辑菜单
	 */
	public static void editMenu() {
		String name = params.get("weChatMenuName");
		String idStr = params.get("weChatMenuId");
		if (StringUtils.isBlank(name)) {
			flash.put("info", "菜单名称不能为空");

			NewsManageAction.categoryManagement();
		}

		if (!NumberUtil.isNumeric(idStr)) {
			flash.put("info", "菜单名称id有误");

			NewsManageAction.categoryManagement();
		}

		long id = Long.parseLong(idStr);
		if (name.length() >= 10) {
			flash.put("info", "菜单名称长度不能大于10");

			NewsManageAction.categoryManagement();
		}

		ErrorInfo error = new ErrorInfo();
		WeChatMenu menu = new WeChatMenu();
		menu.editMenuName(name, id, error);

		if (error.code < 0) {
			flash.put("info", error.msg);

			NewsManageAction.categoryManagement();
		}

		flash.put("info", "编辑成功");

		NewsManageAction.categoryManagement();
	}

	/**
	 * 测试二维码入口,暂时没用
	 */
	public static void qrcode() {
		String key = params.get("key");
		if (null == key || key.equals("")) {

			renderText("不合法的请求");
		} else {
			if (key.equals("1")) {
				// 得到永久二维码ticket
				String ticket = WeChatGongZhongService.createLimitQrcode(
						"sp2p2weixin", 928);
				// 创建二维码并返回路径。
				try {
					String direction = WeChatGongZhongService
							.getQrcodeByTicket("public/", ticket);

					renderText(direction);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (key.equals("2")) {
				// 得到有时间限制的二维码ticket
				String ticket = WeChatGongZhongService.createTempQrcode(
						"sp2pweixin", 999);
				// 创建二维码并返回路径。
				try {
					String direction = WeChatGongZhongService
							.getQrcodeByTicket("public/", ticket);

					renderText(direction);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				renderText("不合法的请求");
			}
		}
	}

}
