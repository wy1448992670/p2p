package controllers.front.quicklogin.qzone;

import org.apache.commons.lang.StringUtils;
import interfaces.OAthBean;
import net.sf.json.JSONObject;
import business.User;
import com.google.gson.JsonObject;
import constants.Constants;
import controllers.BaseController;
import controllers.front.account.AccountHome;
import play.cache.Cache;
import play.libs.WS;
import utils.ErrorInfo;
import utils.Security;

public class QZoneAPI extends BaseController{

	public static QZoneOAuth2 QZONE = new QZoneOAuth2();

    public static void index() {
    	JsonObject obj = getInfo();
    	
    	if(null == obj){
    		login();
    		return;
    	}
    	
		render(obj);
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
			JsonObject reciveJson = WS.url(Constants.GETUSERINFOURL_QQ+"?access_token=%s&oauth_consumer_key=%s&openid=%s", 
	        		WS.encode(oath.access_token),WS.encode(oath.client_id),WS.encode(oath.openid)).get().getJson().getAsJsonObject();
			
			if(reciveJson != null){
				obj = new JsonObject();
				// 绑定第三方openid
				//用于区分绑定是来源于QQ和微博
				String bindKey = Security.addSign(Constants.IDENTIFIED_QQ, Constants.BASE_URL);
		        String bindVal = oath.openid;
		        
		        obj.addProperty("bindKey", bindKey);
		        obj.addProperty("bindVal", bindVal);
		        obj.addProperty("title", "QQ");
		        obj.addProperty("picurl", reciveJson.get("figureurl").getAsString());
		        obj.addProperty("nickname", reciveJson.get("nickname").getAsString());
			}
		}
        return obj;
    }
    
    public static void login() {
    	String clientid_QQ=Constants.CLIENTID_QQ;
    	String secret_QQ=Constants.SECRET_QQ;
    	if("".equals(clientid_QQ)||"".equals(secret_QQ)){
    		renderTemplate("Application/untutored.html");
    	}
        if (QZoneOAuth2.isCodeResponse()) {
        	//获取授权令牌
            QZoneOAuth2.Response response = QZONE.retrieveAccessToken(authURL());
            OAthBean oath = new OAthBean();
            oath.access_token = response.accessToken;
            //获取应用开放ID和用户ID
            String callBackParam = WS.url(Constants.GETOPENIDURL_QQ+"?access_token=%s", WS.encode(oath.access_token)).get().getString();
            callBackParam = callBackParam.substring(10, callBackParam.length()-4);
            JSONObject 	objParam = net.sf.json.JSONObject.fromObject(callBackParam);
            oath.openid = objParam.get("openid").toString();
            oath.client_id = objParam.get("client_id").toString();
            if(StringUtils.isBlank(oath.openid)){
            	flash.error("授权错误，请联系管理员!");
            	renderTemplate("front/account/LoginAndRegisterAction/login.html");
            	return;
            }
            Cache.set(session.getId(), oath);
            
            //首次授权则添加openid到t_user中在index页面进行注册或绑定账号，否则根据openid查询用户的信息并登录
            ErrorInfo error = new ErrorInfo();
            boolean bindFlag = false;
            bindFlag = User.isBindedQQ(oath.openid, error);
            
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
        QZONE.retrieveVerificationCode(authURL());
    }
    
    static String authURL() {
        return play.mvc.Router.getFullUrl("front.quicklogin.qzone.QZoneAPI.login");
    }
}
