package services;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.cache.Cache;
import play.mvc.Http;
import utils.WeChatUtil;

import com.shove.JSONUtils;
import com.shove.Xml;
import com.shove.gateway.weixin.gongzhong.GongZhongObject;
import com.shove.gateway.weixin.gongzhong.ReceiveMessageInterface;
import com.shove.gateway.weixin.gongzhong.utils.GongZhongUtils;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveClickMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveGroupMessageNotice;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveImageMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveLinkMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveLocationMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveSubscribeMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveTextMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveVideoMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveVoiceMessage;

import constants.Constants;

/**
 * 
 * shove组件中有这个封装类，这里单独抽离出来
 * 
 * @author fefrg
 *
 */
public class WeChatGongZhongService extends GongZhongObject {
	public static final String APIURL = "https://api.weixin.qq.com/cgi-bin/";
	public static final String MEDIAURL = "http://file.api.weixin.qq.com/cgi-bin/media/";
	public static String token = "";
	public static String appId = "";
	public static String appSecret = "";
	public static final String UPLOAD_TYPE_IMAGE = "image";
	public static final String UPLOAD_TYPE_VOICE = "voice";
	public static final String UPLOAD_TYPE_VIDEO = "video";
	public static final String UPLOAD_TYPE_THUMB = "thumb";

	static {
		token = Constants.TOKEN;
		appId = Constants.APPID;
		appSecret = Constants.APPSECRET;
	}

	public static String execute(ReceiveMessageInterface receiveMessageInterface)
			throws Exception {
		Http.Response response = Http.Response.current();
		Http.Request request = Http.Request.current();

		response.encoding = "utf-8";
		request.encoding = "utf-8";

		// String method = request.actionMethod;//index得到的是方法名
		String method = request.method;// 得到是GET或者是POST
		if ("GET".equals(method)) {
			String result = verifyDeveloper();

			if (result != null) {

				response.print(result);
			}

			return null;
		}

		requestMessage(receiveMessageInterface);

		return null;
	}

	private static String verifyDeveloper() throws NoSuchAlgorithmException,
			IOException {
		Http.Response response = Http.Response.current();
		Http.Request request = Http.Request.current();

		response.encoding = "utf-8";
		request.encoding = "utf-8";
		String signature = request.params.get("signature");
		Logger.info("signature:%s", signature);
		String echostr = request.params.get("echostr");
		Logger.info("echostr:%s", echostr);
		String timestamp = request.params.get("timestamp");
		Logger.info("timestamp:%s", timestamp);
		String nonce = request.params.get("nonce");
		Logger.info("nonce:%s", nonce);

		if (StringUtils.isBlank(timestamp)) {
			Logger.info("timestamp 不能为空");

			return null;
		}
		if (StringUtils.isBlank(nonce)) {
			Logger.info("nonce 不能为空");

			return null;
		}

		String[] str = { token, timestamp, nonce };

		Arrays.sort(str);
		String total = "";
		for (String string : str) {
			total = total + string;
		}

		if (StringUtils.isBlank(total)) {
			Logger.info("验证开发身份失败");

			return null;
		}

		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		sha1.update(total.getBytes());
		byte[] codedBytes = sha1.digest();
		String codedString = new BigInteger(1, codedBytes).toString(16);

		if (codedString.equals(signature)) {
			return echostr;
		}

		return null;
	}

	public static String getAccessToken() {
		JSONObject jsonObject = null;
		String result = (String) Cache.get("WeiXinUtils.getAccessToken",
				String.class);

		if (result == null) {
			String parameter = "grant_type=client_credential&appid=" + appId
					+ "&secret=" + appSecret;
			jsonObject = WeChatUtil.httpRequest(
					"https://api.weixin.qq.com/cgi-bin/token?" + parameter,
					"GET", null);
			result = jsonObject.getString("access_token");
			Cache.set("WeiXinUtils.getAccessToken", result, "120mn");
		}

		return result;
	}

	private static void requestMessage(
			ReceiveMessageInterface receiveMessageInterface) throws Exception {
		Http.Response response = Http.Response.current();
		Http.Request request = Http.Request.current();

		response.encoding = "utf-8";
		request.encoding = "utf-8";

		String receiveMsg = GongZhongUtils.readStreamParameter(request.body);
		Map<String, Object> xmlMap = Xml.extractSimpleXMLResultMap(receiveMsg);
		String msgType = (String) xmlMap.get("MsgType");

		if ("text".equals(msgType)) {
			ReceiveTextMessage message = (ReceiveTextMessage) GongZhongUtils
					.map2Bean(xmlMap, ReceiveTextMessage.class);
			receiveMessageInterface.receiveTextMessage(message);

			return;
		}
		if ("image".equals(msgType)) {
			ReceiveImageMessage message = (ReceiveImageMessage) GongZhongUtils
					.map2Bean(xmlMap, ReceiveImageMessage.class);
			receiveMessageInterface.receiveImageMessage(message);

			return;
		}

		if ("video".equals(msgType)) {
			ReceiveVideoMessage message = (ReceiveVideoMessage) GongZhongUtils
					.map2Bean(xmlMap, ReceiveVideoMessage.class);
			receiveMessageInterface.receiveVideoMessage(message);

			return;
		}

		if ("voice".equals(msgType)) {
			ReceiveVoiceMessage message = (ReceiveVoiceMessage) GongZhongUtils
					.map2Bean(xmlMap, ReceiveVoiceMessage.class);
			receiveMessageInterface.receiveVoiceMessage(message);

			return;
		}

		if ("location".equals(msgType)) {
			ReceiveLocationMessage message = (ReceiveLocationMessage) GongZhongUtils
					.map2Bean(xmlMap, ReceiveLocationMessage.class);
			receiveMessageInterface.receiveLocationMessage(message);

			return;
		}

		if ("link".equals(msgType)) {
			ReceiveLinkMessage message = (ReceiveLinkMessage) GongZhongUtils
					.map2Bean(xmlMap, ReceiveLinkMessage.class);
			receiveMessageInterface.receiveLinkMessage(message);

			return;
		}

		if ("event".equals(msgType)) {
			String event = (String) xmlMap.get("Event");

			if ("subscribe".equals(event)) {
				ReceiveSubscribeMessage message = (ReceiveSubscribeMessage) GongZhongUtils
						.map2Bean(xmlMap, ReceiveSubscribeMessage.class);
				receiveMessageInterface.eventSubscribeMessage(message);
				return;
			}

			if ("unsubscribe".equals(event)) {
				ReceiveSubscribeMessage message = (ReceiveSubscribeMessage) GongZhongUtils
						.map2Bean(xmlMap, ReceiveSubscribeMessage.class);
				receiveMessageInterface.eventUnSubscribeMessage(message);
				return;
			}

			if (("CLICK".equals(event)) || ("VIEW".equals(event))) {
				ReceiveClickMessage message = (ReceiveClickMessage) GongZhongUtils
						.map2Bean(xmlMap, ReceiveClickMessage.class);
				receiveMessageInterface.eventClickMessage(message);
				return;
			}

			if ("MASSSENDJOBFINISH".equals(event)) {
				ReceiveGroupMessageNotice message = (ReceiveGroupMessageNotice) GongZhongUtils
						.map2Bean(xmlMap, ReceiveGroupMessageNotice.class);
				receiveMessageInterface.eventGroupMessageNotice(message);
			}

			return;
		}
	}

	public static String getQrcodeByTicket(String directory, String ticket)
			throws IOException {
		URL url = new URL("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket="
				+ ticket);

		return download(url, directory, ticket);
	}

	public static String createTempQrcode(String info, int sceneId) {
		String jsonStr = "{\"expire_seconds\": 1800, \"action_name\": \"QR_SCENE\", \""
				+ info + "\": {\"scene\": {\"scene_id\": " + sceneId + "}}}";
		return createQrcode(jsonStr);
	}

	public static String createLimitQrcode(String info, int sceneId) {
		String jsonStr = "{\"action_name\": \"QR_LIMIT_SCENE\", \"" + info
				+ "\": {\"scene\": {\"scene_id\": " + sceneId + "}}}";
		return createQrcode(jsonStr);
	}

	private static String createQrcode(String jsonStr) {
		String result = GongZhongUtils.sendPost(
				"https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token="
						+ getAccessToken(), jsonStr);
		return JSONUtils.getString(JSONObject.fromObject(result), "ticket");
	}

	private static String download(URL url, String directory, String fileName)
			throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		String contentType = connection.getContentType();
		if ("video/mpeg4".equals(contentType)) {
			fileName = fileName + ".mp4";
		} else if (("image/jpeg".equals(contentType))
				|| ("image/jpg".equals(contentType))) {
			fileName = fileName + ".jpg";
		} else if ("audio/mp3".equals(contentType)) {
			fileName = fileName + ".mp3";
		} else if ("audio/amr".equals(contentType)) {
			fileName = fileName + ".amr";
		} else {
			throw new RuntimeException("暂不支持 【" + contentType + "】 请联系升级接口");
		}

		DataInputStream in = new DataInputStream(connection.getInputStream());
		DataOutputStream out = new DataOutputStream(new FileOutputStream(
				directory + fileName));

		byte[] buffer = new byte[4096];
		int count = 0;

		while ((count = in.read(buffer)) > 0) {
			out.write(buffer, 0, count);
		}

		out.flush();
		out.close();
		in.close();

		return directory + fileName;
	}

	public static String downloadMedia(String mediaId, String directory)
			throws IOException {
		URL url = new URL(
				"http://file.api.weixin.qq.com/cgi-bin/media/get?access_token="
						+ getAccessToken() + "&media_id=" + mediaId);

		return download(url, directory, mediaId);
	}

	public static String uploadMedia(String type, String filePath)
			throws IOException {
		String _type = filePath.substring(filePath.lastIndexOf(".") + 1)
				.toUpperCase();

		if ((StringUtils.isBlank(filePath))
				|| ((!"JPG".equals(_type)) && (!"AMR".equals(_type))
						&& (!"MP3".equals(_type)) && (!"MP4".equals(_type)))) {
			throw new IOException("文件类型错误");
		}

		if ((StringUtils.isBlank(type))
				|| ((!type.equals("thumb")) && (!type.equals("image"))
						&& (!type.equals("video")) && (!type.equals("voice")))) {
			throw new IOException("type类型错误");
		}

		File file = new File(filePath);
		if ((!file.exists()) || (!file.isFile())) {
			throw new IOException("文件不存在");
		}

		URL urlObj = new URL(
				"http://file.api.weixin.qq.com/cgi-bin/media/upload?access_token="
						+ getAccessToken() + "&type=" + type);

		HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

		con.setRequestMethod("POST");
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);

		con.setRequestProperty("Connection", "Keep-Alive");
		con.setRequestProperty("Charset", "UTF-8");

		String boundary = "----------" + System.currentTimeMillis();
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary="
				+ boundary);

		StringBuilder sb = new StringBuilder();
		sb.append("--");
		sb.append(boundary);
		sb.append("\r\n");
		sb.append("Content-Disposition: form-data;name=\"file\";filename=\""
				+ file.getName() + "\"\r\n");
		sb.append("Content-Type:application/octet-stream\r\n\r\n");

		byte[] head = sb.toString().getBytes("utf-8");

		OutputStream out = new DataOutputStream(con.getOutputStream());

		out.write(head);

		DataInputStream in = new DataInputStream(new FileInputStream(file));
		int bytes = 0;
		byte[] bufferOut = new byte[1024];
		while ((bytes = in.read(bufferOut)) != -1) {
			out.write(bufferOut, 0, bytes);
		}
		in.close();

		byte[] foot = ("\r\n--" + boundary + "--\r\n").getBytes("utf-8");

		out.write(foot);
		out.flush();
		out.close();

		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = null;

		reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line = null;

		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}
		String result = null;
		if (result == null) {
			result = buffer.toString();
		}

		if (reader != null) {
			reader.close();
		}

		return result;
	}

	public static void main(String[] args) throws NoSuchAlgorithmException {

	}
}