package services;

import utils.WeChatUtil;

import com.shove.JSONUtils;
import com.shove.gateway.weixin.gongzhong.GongZhongObject;
import com.shove.gateway.weixin.gongzhong.utils.GongZhongUtils;
import com.shove.gateway.weixin.gongzhong.vo.weboauth.OauthAccessToken;
import com.shove.gateway.weixin.gongzhong.vo.weboauth.UserInfo;

import net.sf.json.JSONObject;

/**
 * 授权回调，得到微信用户相关信息类
 * 
 * @author fefrg
 *
 */
public class WeChatWebOAuthManageService extends GongZhongObject {

	public static String getBaseOauth2Url(String redirectUri, String state) {
		String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
				+ WeChatGongZhongService.appId
				+ "&redirect_uri="
				+ redirectUri
				+ "&response_type=code&scope=snsapi_base&state="
				+ state
				+ "#wechat_redirect";

		return url;
	}

	public static String getUserinfoOauth2Url(String redirectUri, String state) {
		String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
				+ WeChatGongZhongService.appId
				+ "&redirect_uri="
				+ redirectUri
				+ "&response_type=code&scope=snsapi_userinfo&state="
				+ state
				+ "#wechat_redirect";

		return url;
	}

	public static OauthAccessToken getAccessToken(String code) {

		String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
				+ WeChatGongZhongService.appId + "&secret="
				+ WeChatGongZhongService.appSecret + "&code=" + code
				+ "&grant_type=authorization_code";
		JSONObject result = WeChatUtil.httpRequest(url, "GET", null);

		return (OauthAccessToken) JSONUtils.toBean(result,
				OauthAccessToken.class);
	}

	public static OauthAccessToken refreshAccessToken(String refreshToken) {
		String result = GongZhongUtils.sendPost(
				"https://api.weixin.qq.com/sns/oauth2/refresh_token?appid="
						+ WeChatGongZhongService.appId
						+ "&grant_type=refresh_token&refresh_token="
						+ refreshToken, "");

		return (OauthAccessToken) JSONUtils.toBean(
				JSONObject.fromObject(result), OauthAccessToken.class);
	}

	public static UserInfo getUserInfo(String accessToken, String openId) {
		String url = "https://api.weixin.qq.com/sns/userinfo?access_token="
				+ accessToken + "&openid=" + openId;
		JSONObject result = WeChatUtil.httpRequest(url, "GET", null);

		return (UserInfo) JSONUtils.toBean(result, UserInfo.class);
	}
}