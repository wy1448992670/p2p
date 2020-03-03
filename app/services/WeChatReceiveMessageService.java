package services;

import java.io.IOException;
import java.util.Date;

import org.h2.util.StringUtils;

import play.Logger;
import utils.ErrorInfo;
import utils.WeChatUtil;
import business.BackstageSet;
import business.User;

import com.shove.gateway.weixin.gongzhong.ReceiveMessageInterface;
import com.shove.gateway.weixin.gongzhong.vo.message.Message;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveClickMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveGroupMessageNotice;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveImageMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveLinkMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveLocationMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveSubscribeMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveTemplateMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveTextMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveVideoMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.receive.ReceiveVoiceMessage;
import com.shove.gateway.weixin.gongzhong.vo.message.reply.ReplyTextMessage;

import constants.Constants;
import constants.OptionKeys;
import controllers.wechat.others.WeChatFrontExport;

/**
 * 实现接收信息接口类。（接口是shove中封装的，这里我们实现它）
 * 
 * @author fefrg
 *
 */
public class WeChatReceiveMessageService implements ReceiveMessageInterface {

	/**
	 * 接受CLICK事件, 目前只有咨询是click事件，key为1，信息会到多客服端
	 */
	public String eventClickMessage(ReceiveClickMessage receiveTextMessage) {
		String content = "";
		ReplyTextMessage replyTextMessage = new ReplyTextMessage();

		replyTextMessage.setFromUserName(receiveTextMessage.getToUserName());
		replyTextMessage.setToUserName(receiveTextMessage.getFromUserName());
		replyTextMessage.setCreateTime(new Date().getTime());
		int key = 0;
		if (StringUtils.isNumber(receiveTextMessage.getEventKey())) {
			key = Integer.parseInt(receiveTextMessage.getEventKey());
		}
		switch (key) {
		case 1:
			/**
			 * 首先查询是否有可接入的在线客服
			 */
			boolean flag = WeChatCustomerService.isUseCustomer();
			if (!flag) {
				// 返回提示语
				content = "您好，现在客服人员比较繁忙\n  请您待会再试\n更多-->在线咨询";
				replyTextMessage.setContent(content);
				// 返回类型为text
				// replyTextMessage.setMsgType(Message.TYPE_TEXT);
			} else {

				// 查询咨询语
				content = BackstageSet.getWeiXinLanguage(
						"weixin_consulting_language", new ErrorInfo());
				replyTextMessage.setContent(content);
				// 根据msgId来判断是否是发送到客服
				replyTextMessage.setMsgId(100);
				// 返回类型为transfer_customer_service,信息转发到多客服那边
				// replyTextMessage.setMsgType("transfer_customer_service");
			}
			try {
				// 发送消息
				WeChatFrontExport.replyTextMessage(replyTextMessage);
			} catch (IOException e) {
				Logger.error("发送咨询语时%s", e.getMessage());
			}
			break;
		case 2:
			break;
		case 3:
			break;

		}
		return null;
	}

	/**
	 * 接收用户发送过来的消息,如果用户一直不点在线咨询，那么一直给用户回复这句提示 直到用户点击click事件(在线咨询)，那么信息就会被转发到多客服。
	 */
	public String receiveTextMessage(ReceiveTextMessage receiveTextMessage) {

		ReplyTextMessage replyTextMessage = new ReplyTextMessage();

		replyTextMessage.setFromUserName(receiveTextMessage.getToUserName());
		replyTextMessage.setToUserName(receiveTextMessage.getFromUserName());
		replyTextMessage.setMsgType(Message.TYPE_TEXT);
		replyTextMessage.setCreateTime(new Date().getTime());

		String content = "您好，有需要咨询晓风网贷\n请点击更多-->在线咨询\n我们的客服人员会为您详细解答";
		replyTextMessage.setContent(content);

		try {
			WeChatFrontExport.replyTextMessage(replyTextMessage);
		} catch (IOException e) {
			Logger.error("发送咨询消息时%s", e.getMessage());
		}

		return null;
	}

	public String eventGroupMessageNotice(ReceiveGroupMessageNotice arg0) {

		return null;
	}

	/**
	 * 用户关注公众号
	 */
	public String eventSubscribeMessage(ReceiveSubscribeMessage arg0) {
		try {
			// 向用户发送关注语，并且提示用户进行绑定
			ReplyTextMessage replyTextMessage = new ReplyTextMessage();
			replyTextMessage.setFromUserName(arg0.getToUserName());
			replyTextMessage.setToUserName(arg0.getFromUserName());
			replyTextMessage.setCreateTime(new Date().getTime());
			replyTextMessage.setMsgType(Message.TYPE_TEXT);
			replyTextMessage.setMsgId(arg0.getMsgId());

			String url = "<a href=\"https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=snsapi_base&state=${STATE}#wechat_redirect\">${BIND}</a>";
			url = url.replace("APPID", Constants.APPID);
			url = url.replace("REDIRECT_URI",
					WeChatUtil.urlEncodeUTF8(Constants.REDIRECT_URI));

			String content = null;
			ErrorInfo error = new ErrorInfo();
			String openId = arg0.getFromUserName();
			if (!User.isBind(openId, error)) {
				content = BackstageSet.getWeiXinLanguage(
						"weixin_welcome_to_language_bind", error);
				if (null != content
						&& content.contains(OptionKeys.getvalue("bind", error))) {
					url = url.replace("${STATE}", "41").replace("${BIND}",
							OptionKeys.getvalue("bind", error));
					content = content.replace(
							OptionKeys.getvalue("bind", error), url);
				} else {
					content += url;
				}
			} else {
				content = BackstageSet.getWeiXinLanguage(
						"weixin_welcome_to_language_unbound", error);
				if (null != content
						&& content.contains(OptionKeys.getvalue("unbound",
								error))) {
					url = url.replace("${STATE}", "42").replace("${BIND}",
							OptionKeys.getvalue("unbound", error));
					content = content.replace(
							OptionKeys.getvalue("unbound", error), url);
				} else {
					content += url;
				}
			}

			replyTextMessage.setContent(content);
			// 向用户发送关注语
			WeChatFrontExport.replyTextMessage(replyTextMessage);
		} catch (IOException e) {

			Logger.error("向用户发送关注语时%s", e.getMessage());
		}

		return null;
	}

	/**
	 * 用户取消关注公众号
	 */
	public String eventUnSubscribeMessage(ReceiveSubscribeMessage arg0) {

		return null;
	}

	/**
	 * 接受图片消息
	 */
	public String receiveImageMessage(ReceiveImageMessage arg0) {

		return null;
	}

	/**
	 * 接受超链接
	 */
	public String receiveLinkMessage(ReceiveLinkMessage arg0) {

		return null;
	}

	/**
	 * 接受位置信息
	 */
	public String receiveLocationMessage(ReceiveLocationMessage arg0) {

		return null;
	}

	/**
	 * 接受视频消息
	 */
	public String receiveVideoMessage(ReceiveVideoMessage arg0) {

		return null;
	}

	/**
	 * 接受音频消息
	 */
	public String receiveVoiceMessage(ReceiveVoiceMessage arg0) {

		return null;
	}

	public String receiveTemplateSendJobFinishMessag(ReceiveTemplateMessage arg0) {

		return null;
	}

}
