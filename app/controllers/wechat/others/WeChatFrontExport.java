package controllers.wechat.others;

import java.io.IOException;
import java.text.MessageFormat;

import net.sf.json.JSONObject;
import play.mvc.Http;
import services.WeChatGongZhongService;

import com.shove.JSONUtils;
import com.shove.gateway.weixin.gongzhong.vo.message.Article;
import com.shove.gateway.weixin.gongzhong.vo.message.Message;
import com.shove.gateway.weixin.gongzhong.vo.message.reply.ReplyArticlesMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.reply.ReplyImageMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.reply.ReplyMusicMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.reply.ReplyTextMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.reply.ReplyVideoMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.reply.ReplyVoiceMessage;

/**
 * 回复消息工具类，将消息组装成xml“写”给微信端到用户。(shove组件中已经封装这个类，这里单独抽离出来）
 * @author fefrg
 *
 */
public class WeChatFrontExport {
	
	/**
	 * 回复文本消息
	 * @param message
	 * @throws IOException
	 */
	public static void replyTextMessage(
			ReplyTextMessage message) throws IOException {
		String msgType = null;
		if(message.getMsgId() == 100) {
			msgType = "transfer_customer_service";
		} else {
			msgType = Message.TYPE_TEXT;
		}
		String xml = "<xml><ToUserName><![CDATA[{0}]]></ToUserName><FromUserName><![CDATA[{1}]]></FromUserName>"
				+ "<CreateTime>{2,number,#}</CreateTime><MsgType><![CDATA[{3}]]></MsgType><Content><![CDATA[{4}]]></Content></xml>";

		xml = MessageFormat.format(
				xml,
				new Object[] { message.getToUserName(),
						message.getFromUserName(),
						Long.valueOf(message.getCreateTime()), msgType,
						message.getContent() });

		Http.Response.current().print(xml);
	}

	public static void replyImageMessage(
			ReplyImageMessage message) throws IOException {
		String xml = "<xml><ToUserName><![CDATA[{0}]]></ToUserName><FromUserName><![CDATA[{1}]]></FromUserName><CreateTime>{2,number,#}</CreateTime><MsgType><![CDATA[{3}]]></MsgType><Image><MediaId><![CDATA[{4}]]></MediaId></Image></xml>";

		xml = MessageFormat.format(
				xml,
				new Object[] { message.getToUserName(),
						message.getFromUserName(),
						Long.valueOf(message.getCreateTime()), "image",
						message.getMediaId() });

		Http.Response.current().print(xml);
	}

	public static void replyImageMessage(
			ReplyImageMessage message, String imageFilePath) throws IOException {
		String xml = "<xml><ToUserName><![CDATA[{0}]]></ToUserName><FromUserName><![CDATA[{1}]]></FromUserName><CreateTime>{2,number,#}</CreateTime><MsgType><![CDATA[{3}]]></MsgType><Image><MediaId><![CDATA[{4}]]></MediaId></Image></xml>";

		JSONObject obj = JSONObject.fromObject(WeChatGongZhongService.uploadMedia(
				"image", imageFilePath));
		message.setMediaId(JSONUtils.getString(obj, "thumb_media_id"));

		xml = MessageFormat.format(
				xml,
				new Object[] { message.getToUserName(),
						message.getFromUserName(),
						Long.valueOf(message.getCreateTime()), "image",
						message.getMediaId() });

		Http.Response.current().print(xml);
	}

	public static void replyReplyVoiceMessage(
			ReplyVoiceMessage message) throws IOException {
		String xml = "<xml><ToUserName><![CDATA[{0}]]></ToUserName><FromUserName><![CDATA[{1}]]></FromUserName><CreateTime>{2,number,#}</CreateTime><MsgType><![CDATA[{3}]]></MsgType><Voice><MediaId><![CDATA[{4}]]></MediaId></Voice></xml>";

		xml = MessageFormat.format(
				xml,
				new Object[] { message.getToUserName(),
						message.getFromUserName(),
						Long.valueOf(message.getCreateTime()), "voice",
						message.getMediaId() });

		Http.Response.current().print(xml);
	}

	public static void replyReplyVoiceMessage(
			ReplyVoiceMessage message, String voiceFilePath) throws IOException {
		String xml = "<xml><ToUserName><![CDATA[{0}]]></ToUserName><FromUserName><![CDATA[{1}]]></FromUserName><CreateTime>{2,number,#}</CreateTime><MsgType><![CDATA[{3}]]></MsgType><Voice><MediaId><![CDATA[{4}]]></MediaId></Voice></xml>";

		JSONObject obj = JSONObject.fromObject(WeChatGongZhongService.uploadMedia(
				"voice", voiceFilePath));
		message.setMediaId(JSONUtils.getString(obj, "thumb_media_id"));

		xml = MessageFormat.format(
				xml,
				new Object[] { message.getToUserName(),
						message.getFromUserName(),
						Long.valueOf(message.getCreateTime()), "voice",
						message.getMediaId() });

		Http.Response.current().print(xml);
	}

	public static void replyVideoMessage(
			ReplyVideoMessage message) throws IOException {
		String xml = "<xml><ToUserName><![CDATA[{0}]]></ToUserName><FromUserName><![CDATA[{1}]]></FromUserName><CreateTime>{2,number,#}</CreateTime><MsgType><![CDATA[{3}]]></MsgType><Video><MediaId><![CDATA[{4}]]></MediaId><Title><![CDATA[{5}]]></Title><Description><![CDATA[{6}]]></Description></Video></xml>";

		xml = MessageFormat.format(
				xml,
				new Object[] { message.getToUserName(),
						message.getFromUserName(),
						Long.valueOf(message.getCreateTime()), "video",
						message.getMediaId(), message.getTitle(),
						message.getDescription() });

		Http.Response.current().print(xml);
	}

	public static void replyVideoMessage(
			ReplyVideoMessage message, String videoFilePath) throws IOException {
		String xml = "<xml><ToUserName><![CDATA[{0}]]></ToUserName><FromUserName><![CDATA[{1}]]></FromUserName><CreateTime>{2,number,#}</CreateTime><MsgType><![CDATA[{3}]]></MsgType><Video><MediaId><![CDATA[{4}]]></MediaId><Title><![CDATA[{5}]]></Title><Description><![CDATA[{6}]]></Description></Video></xml>";

		JSONObject obj = JSONObject.fromObject(WeChatGongZhongService.uploadMedia(
				"video", videoFilePath));
		message.setMediaId(JSONUtils.getString(obj, "thumb_media_id"));

		xml = MessageFormat.format(
				xml,
				new Object[] { message.getToUserName(),
						message.getFromUserName(),
						Long.valueOf(message.getCreateTime()), "video",
						message.getMediaId(), message.getTitle(),
						message.getDescription() });

		Http.Response.current().print(xml);
	}

	public static void replyMusicMessage(
			ReplyMusicMessage message) throws IOException {
		String xml = "<xml><ToUserName><![CDATA[{0}]]></ToUserName><FromUserName><![CDATA[{1}]]></FromUserName><CreateTime>{2,number,#}</CreateTime><MsgType><![CDATA[{3}]]></MsgType><Music><Title><![CDATA[{4}]]></Title><Description><![CDATA[{5}]]></Description><MusicUrl><![CDATA[{6}]]></MusicUrl><HQMusicUrl><![CDATA[{7}]]></HQMusicUrl><ThumbMediaId><![CDATA[{8}]]></ThumbMediaId></Music></xml>";

		xml = MessageFormat.format(
				xml,
				new Object[] { message.getToUserName(),
						message.getFromUserName(),
						Long.valueOf(message.getCreateTime()), "music",
						message.getTitle(), message.getDescription(),
						message.getMusicUrl(), message.getHQMusicUrl(),
						message.getThumbMediaId() });

		Http.Response.current().print(xml);
	}

	public static void replyMusicMessage(
			ReplyMusicMessage message, String thumbMediaFilePath)
			throws IOException {
		String xml = "<xml><ToUserName><![CDATA[{0}]]></ToUserName><FromUserName><![CDATA[{1}]]></FromUserName><CreateTime>{2,number,#}</CreateTime><MsgType><![CDATA[{3}]]></MsgType><Music><Title><![CDATA[{4}]]></Title><Description><![CDATA[{5}]]></Description><MusicUrl><![CDATA[{6}]]></MusicUrl><HQMusicUrl><![CDATA[{7}]]></HQMusicUrl><ThumbMediaId><![CDATA[{8}]]></ThumbMediaId></Music></xml>";

		JSONObject obj = JSONObject.fromObject(WeChatGongZhongService.uploadMedia(
				"thumb", thumbMediaFilePath));
		message.setMediaId(JSONUtils.getString(obj, "thumb_media_id"));

		xml = MessageFormat.format(
				xml,
				new Object[] { message.getToUserName(),
						message.getFromUserName(),
						Long.valueOf(message.getCreateTime()), "music",
						message.getTitle(), message.getDescription(),
						message.getMusicUrl(), message.getHQMusicUrl(),
						message.getThumbMediaId() });

		Http.Response.current().print(xml);
	}

	public static void replyArticleMessage(
			ReplyArticlesMessage message) throws IOException {
		if ((message.getArticles() == null)
				|| (message.getArticles().size() == 0)
				|| (message.getArticles().size() > 10)) {
			throw new RuntimeException("Articles为空或超过限制");
		}
		String xml = "<xml><ToUserName><![CDATA[{0}]]></ToUserName><FromUserName><![CDATA[{1}]]></FromUserName><CreateTime>{2,number,#}</CreateTime><MsgType><![CDATA[{3}]]></MsgType><ArticleCount>{4,number,#}</ArticleCount><Articles>{5}</Articles></xml>";

		StringBuffer item = new StringBuffer();
		for (Article article : message.getArticles()) {
			item.append(MessageFormat
					.format("<item><Title><![CDATA[{0}]]></Title><Description><![CDATA[{1}]]></Description><PicUrl><![CDATA[{2}]]></PicUrl><Url><![CDATA[{3}]]></Url></item>",
							new Object[] { article.getTitle(),
									article.getDescription(),
									article.getPicUrl(), article.getUrl() }));
		}
		xml = MessageFormat.format(
				xml,
				new Object[] { message.getToUserName(),
						message.getFromUserName(),
						Long.valueOf(message.getCreateTime()), "news",
						Integer.valueOf(message.getArticleCount()),
						item.toString() });
		Http.Response.current().print(xml);
	}
}