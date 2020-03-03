package controllers.front.quicklogin.weibo;

import java.util.HashMap;
import java.util.Map;
import net.sf.json.JSONObject;
import com.google.gson.JsonObject;
import constants.Constants;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Http.Request;
import play.mvc.Scope.Params;
import play.mvc.results.Redirect;

public class WeiBoOAuth2 {

	private static final String CLIENT_ID_NAME = "client_id";
	private static final String REDIRECT_URI = "redirect_uri";
	private static final String RESPONSE_TYPE = "response_type";
	
    public String authorizationURL;
    public String accessTokenURL;
    public String clientid;
    public String secret;
    public String responseType;

    public WeiBoOAuth2() {
        this.accessTokenURL = Constants.ACCESSTOKENURL_WB;
        this.authorizationURL = Constants.AUTHORIZATIONURL_WB;
        this.clientid = Constants.CLIENTID_WB;
        this.secret = Constants.SECRET_WB;
        this.responseType = Constants.RESPONSETYPE_WB;
    }

    public static boolean isCodeResponse() {
        return Params.current().get("code") != null;
    }

	/**
	 * First step of the OAuth2 process: redirects the user to the authorisation page
	 * 
	 * @param callbackURL
	 */
	public void retrieveVerificationCode(String callbackURL) {
		retrieveVerificationCode(callbackURL, new HashMap<String, String>());
	}

	/**
	 * First step of the oAuth2 process. This redirects the user to the authorisation page on the oAuth2 provider. This is a helper method that only takes one parameter name,value pair and then
	 * converts them into a map to be used by {@link #retrieveVerificationCode(String, Map)}
	 * 
	 * @param callbackURL
	 *            The URL to redirect the user to after authorisation
	 * @param parameters
	 *            Any additional parameters that weren't included in the constructor. For example you might need to add a response_type.
	 */
	public void retrieveVerificationCode(String callbackURL, String parameterName, String parameterValue) {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(parameterName, parameterValue);
		retrieveVerificationCode(callbackURL, parameters);
	}
	
	/**
	 * First step of the oAuth2 process. This redirects the user to the authorisation page on the oAuth2 provider.
	 * 
	 * @param callbackURL
	 *            The URL to redirect the user to after authorisation
	 * @param parameters
	 *            Any additional parameters that weren't included in the constructor. For example you might need to add a response_type.
	 */
	public void retrieveVerificationCode(String callbackURL, Map<String, String> parameters) {
		parameters.put(CLIENT_ID_NAME, clientid);
		parameters.put(REDIRECT_URI, callbackURL);
		parameters.put(RESPONSE_TYPE, responseType);
		throw new Redirect(authorizationURL, parameters);
	}
    
    public void retrieveVerificationCode() {
        retrieveVerificationCode(Request.current().getBase() + Request.current().url);
    }

    public Response retrieveAccessToken(String callbackURL) {
        String accessCode = Params.current().get("code");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("client_id", clientid);
        params.put("client_secret", secret);
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", callbackURL);
        params.put("code", accessCode);
        HttpResponse response = WS.url(accessTokenURL).params(params).post();
        return new Response(response);
    }

    public Response retrieveAccessToken() {
        return retrieveAccessToken(Request.current().getBase() + Request.current().url);
    }

    /**
     * @deprecated Use @{link play.libs.OAuth2.retrieveVerificationCode()} instead
     */
    @Deprecated
    public void requestAccessToken() {
        retrieveVerificationCode();
    }

    /**
     * @deprecated Use @{link play.libs.OAuth2.retrieveAccessToken()} instead
     */
    @Deprecated
    public String getAccessToken() {
        return retrieveAccessToken().accessToken;
    }

    public static class Response {
        public final String accessToken;
        public final String uid;
        public final Error error;
        public final WS.HttpResponse httpResponse;
        private Response(String accessToken,String uid, Error error, WS.HttpResponse response) {
            this.accessToken = accessToken;
            this.uid = uid;
            this.error = error;
            this.httpResponse = response;
        }
        public Response(WS.HttpResponse response) {
            this.httpResponse = response;
            
            JSONObject queryJson = JSONObject.fromObject(response.getString());
            if (queryJson != null) {
                this.accessToken = queryJson.get("access_token").toString();
                this.uid =  queryJson.get("uid").toString();
                this.error = null;
            } else {
                this.accessToken = null;
                this.uid = null;
                this.error = Error.oauth2(response);
            }
        }
        public static Response error(Error error, WS.HttpResponse response) {
            return new Response(null,null, error, response);
        }
    }

    public static class Error {
        public final Type type;
        public final String error;
        public final String description;
        public enum Type {
            COMMUNICATION,
            OAUTH,
            UNKNOWN
        }
        private Error(Type type, String error, String description) {
            this.type = type;
            this.error = error;
            this.description = description;
        }
        static Error communication() {
            return new Error(Type.COMMUNICATION, null, null);
        }
        static Error oauth2(WS.HttpResponse response) {
            if (response.getQueryString().containsKey("error")) {
                Map<String, String> qs = response.getQueryString();
                return new Error(Type.OAUTH,
                        qs.get("error"),
                        qs.get("error_description"));
            } else if (response.getContentType().startsWith("text/javascript")) { // Stupid Facebook returns JSON with the wrong encoding
                JsonObject jsonResponse = response.getJson().getAsJsonObject().getAsJsonObject("error");
                return new Error(Type.OAUTH,
                        jsonResponse.getAsJsonPrimitive("type").getAsString(),
                        jsonResponse.getAsJsonPrimitive("message").getAsString());
            } else {
                return new Error(Type.UNKNOWN, null, null);
            }
        }
        @Override public String toString() {
            return "OAuth2 Error: " + type + " - " + error + " (" + description + ")";
        }
    }

}