package controllers.front.quicklogin.qzone;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.JsonObject;
import constants.Constants;
import play.libs.Codec;
import play.libs.Crypto;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Http.Request;
import play.mvc.Scope.Params;
import play.mvc.results.Redirect;

public class QZoneOAuth2 {

	private static final String CLIENT_ID_NAME = "client_id";
	private static final String REDIRECT_URI = "redirect_uri";
	private static final String RESPONSE_TYPE = "response_type";
	private static final String STATE = "state";
	
    public String authorizationURL;
    public String accessTokenURL;
    public String clientid;
    public String secret;
    public String responseType;
    public String state;

    public QZoneOAuth2() {
        this.accessTokenURL = Constants.ACCESSTOKENURL_QQ;
        this.authorizationURL = Constants.AUTHORIZATIONURL_QQ;
        this.clientid = Constants.CLIENTID_QQ;
        this.secret = Constants.SECRET_QQ;
        this.responseType = Constants.RESPONSETYPE_QQ;
        this.state = Crypto.sign(Codec.UUID());
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
		parameters.put(STATE, state);
		throw new Redirect(authorizationURL, parameters);
	}
    
    public void retrieveVerificationCode() {
        retrieveVerificationCode(Request.current().getBase() + Request.current().url);
    }

    public Response retrieveAccessToken(String callbackURL) {
        String accessCode = Params.current().get("code");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", clientid);
        params.put("client_secret", secret);
        params.put("redirect_uri", callbackURL);
        params.put("code", accessCode);
        HttpResponse response = WS.url(accessTokenURL).params(params).get();
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
        public final Error error;
        public final WS.HttpResponse httpResponse;
        private Response(String accessToken, Error error, WS.HttpResponse response) {
            this.accessToken = accessToken;
            this.error = error;
            this.httpResponse = response;
        }
        public Response(WS.HttpResponse response) {
            this.httpResponse = response;
            Map<String, String> querystring = response.getQueryString();
            if (querystring.containsKey("access_token")) {
                this.accessToken = querystring.get("access_token");
                this.error = null;
            } else {
                this.accessToken = null;
                this.error = Error.oauth2(response);
            }
        }
        public static Response error(Error error, WS.HttpResponse response) {
            return new Response(null, error, response);
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