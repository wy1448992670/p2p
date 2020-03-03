package services;

import com.shove.JSONUtils;
import com.shove.gateway.weixin.gongzhong.utils.GongZhongUtils;
import com.shove.gateway.weixin.gongzhong.vo.menu.Menu;

import java.util.List;

import utils.WeChatUtil;
import net.sf.json.JSONObject;

/**
 * shove组件中有这个封装类，这里单独抽离出来
 * @author fefrg
 *
 */
public class WeChatMenuManageService {
	private static final String MENU_GET = "https://api.weixin.qq.com/cgi-bin/menu/get?";
	private static final String MENU_CREATE = "https://api.weixin.qq.com/cgi-bin/menu/create?";
	private static final String MENU_DELETE = "https://api.weixin.qq.com/cgi-bin/menu/delete?";

	public static List<Menu> getMenu() {
		String result = GongZhongUtils.sendPost(
				MENU_GET, "access_token="
						+ WeChatGongZhongService.getAccessToken());
		JSONObject obj = JSONObject.fromObject(result).getJSONObject("menu");

		return JSONUtils.toList(obj.getJSONArray("button"), Menu.class);
	}

	public static void deleteMenu() {
		GongZhongUtils.sendPost(
				MENU_DELETE,
				"access_token=" + WeChatGongZhongService.getAccessToken());
	}

	public static void createMenu(List<Menu> menus) {
		if ((menus == null) || (menus.size() <= 0)) {
			throw new RuntimeException("菜单不能位空");
		}

		if (menus.size() > 3) {
			throw new RuntimeException("一级菜单数组，个数应为1~3个");
		}

		JSONObject obj = new JSONObject();
		obj.put("button", menus);
		WeChatUtil.httpRequest(MENU_CREATE + "access_token=" + WeChatGongZhongService.getAccessToken(), "POST", obj.toString());
	}
}