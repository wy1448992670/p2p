package controllers.front.quicklogin.weibo;

import org.apache.commons.lang.StringUtils;
import interfaces.OAthBean;
import business.User;
import com.google.gson.JsonObject;
import constants.Constants;
import controllers.BaseController;
import controllers.front.account.AccountHome;
import play.cache.Cache;
import play.libs.WS;
import utils.ErrorInfo;
import utils.Security;

public class WeiBoAPI extends BaseController{

	public static WeiBoOAuth2 WEIBO = new WeiBoOAuth2();

	public static void index() {
		JsonObject obj = getInfo();
    	
    	if(null == obj){
    		login();
    		return;
    	}
    	renderTemplate("front/quicklogin/qzone/QZoneAPI/index.html",obj);
    }

	/**
     * 获取授权用户信息
     * @return
     */
    private static JsonObject getInfo(){
    	JsonObject obj = null;
		OAthBean oath = (OAthBean) Cache.get(session.getId());
		Cache.delete(session.getId());
		if (oath != null && oath.openid != null) {
			//获取用户资料
			JsonObject reciveJson = WS.url(Constants.GETUSERINFOURL_WB+"?access_token=%s&uid=%s",
	    			WS.encode(oath.access_token),WS.encode(oath.openid)).get().getJson().getAsJsonObject();
			if(reciveJson != null){
				obj = new JsonObject();
				// 绑定第三方openid
				//用于区分绑定是来源于QQ和微博
				String bindKey = Security.addSign(Constants.IDENTIFIED_WB, Constants.BASE_URL);
		        String bindVal = oath.openid;
		        
		        obj.addProperty("bindKey", bindKey);
		        obj.addProperty("bindVal", bindVal);
		        obj.addProperty("title", "微博");
		        obj.addProperty("picurl", reciveJson.get("profile_image_url").getAsString());
		        obj.addProperty("nickname", reciveJson.get("screen_name").getAsString());
			}
		}
        return obj;
    }
    
    public static void login() {
    	String clientid_WB=Constants.CLIENTID_WB;
    	String secret_WB=Constants.SECRET_WB;
    	if("".equals(clientid_WB)||"".equals(secret_WB)){
    		renderTemplate("Application/untutored.html");
    	}
        if (WeiBoOAuth2.isCodeResponse()) {
        	//获取授权令牌和用户ID
            WeiBoOAuth2.Response response = WEIBO.retrieveAccessToken(authURL());
            OAthBean oath = new OAthBean();
            oath.access_token = response.accessToken;
            oath.openid = response.uid;
            if(StringUtils.isBlank(oath.openid)){
            	flash.error("授权错误，请联系管理员!");
            	renderTemplate("front/account/LoginAndRegisterAction/login.html");
            	return;
            }
            Cache.set(session.getId(), oath);
            
            //首次授权则添加openid到t_user中在index页面进行注册或绑定账号，否则根据openid查询用户的信息并登录
            ErrorInfo error = new ErrorInfo();
            boolean bindFlag = false;
            
            bindFlag = User.isBindedWEIBO(oath.openid, error);
            if(bindFlag){
            	AccountHome.home();
            	return;
            }
            if(error.code < 0){
            	flash.error(error.msg);
            	renderTemplate("front/account/LoginAndRegisterAction/login.html");
            	return;
            }
            index();

        }
        //获取授权码
        WEIBO.retrieveVerificationCode(authURL());
    }
    
    static String authURL() {
        return play.mvc.Router.getFullUrl("front.quicklogin.weibo.WeiBoAPI.login");
    }
}
