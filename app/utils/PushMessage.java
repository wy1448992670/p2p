package utils;

import play.Logger;
import com.baidu.yun.channel.auth.ChannelKeyPair;
import com.baidu.yun.channel.client.BaiduChannelClient;
import com.baidu.yun.channel.exception.ChannelClientException;
import com.baidu.yun.channel.exception.ChannelServerException;
import com.baidu.yun.channel.model.PushBroadcastMessageRequest;
import com.baidu.yun.channel.model.PushBroadcastMessageResponse;
import com.baidu.yun.channel.model.PushUnicastMessageRequest;
import com.baidu.yun.channel.model.PushUnicastMessageResponse;
import com.baidu.yun.core.log.YunLogEvent;
import com.baidu.yun.core.log.YunLogHandler;
import constants.Constants;

public class PushMessage {
	
	/**
	 * 推送初始化
	 * @return
	 */
	private static BaiduChannelClient initPushClient() {
		// 1. 设置developer平台的ApiKey/SecretKey
		ChannelKeyPair pair = new ChannelKeyPair(Constants.API_KEY, Constants.SECRET_KEY);
		// 2. 创建BaiduChannelClient对象实例
		BaiduChannelClient channelClient = new BaiduChannelClient(pair);
		// 3. 若要了解交互细节，请注册YunLogHandler类
		channelClient.setChannelLogHandler(new YunLogHandler() {
			@Override
			public void onHandle(YunLogEvent event) {
				System.out.println(event.getMessage());
			}
		});

		return channelClient;
	}

	/**
	 * 向所有客户端推送通知
	 * @param content 传入json的字符串
	 * @return
	 */
	public static int pushNoticeMessage(String contentAnd, String contentIos) {
		BaiduChannelClient channelClient = initPushClient();
		
		try {
			// 4. 创建请求类对象
			PushBroadcastMessageRequest request = new PushBroadcastMessageRequest();
			request.setDeviceType(3);// device_type => 1: web 2: pc 3:android 4:ios 5:wp
			request.setMessageType(1); //默认0 通知 1
			request.setMessage(contentAnd); 
			// 5. 调用pushMessage接口
			PushBroadcastMessageResponse response = channelClient.pushBroadcastMessage(request);
			// 6. 认证推送成功
			System.out.println("android push amount : " + response.getSuccessAmount());
			
			// 4. 创建请求类对象
			PushBroadcastMessageRequest requestIOS = new PushBroadcastMessageRequest();
			requestIOS.setDeviceType(4);// device_type => 1: web 2: pc 3:android 4:ios 5:wp
			requestIOS.setMessageType(1); //默认0 通知 1
			requestIOS.setMessage(contentIos);
			// 5. 调用pushMessage接口
			PushBroadcastMessageResponse responseIOS = channelClient.pushBroadcastMessage(requestIOS);
			// 6. 认证推送成功
			System.out.println("ios push amount : " + responseIOS.getSuccessAmount());
			
			return response.getSuccessAmount();
		} catch (ChannelClientException e) {
			// 处理客户端错误异常
			e.printStackTrace();
			
			return -1;
		} catch (ChannelServerException e) {
			// 处理服务端错误异常
			System.out.println(String.format("request_id: %d, error_code: %d, error_message: %s",
					e.getRequestId(), e.getErrorCode(), e.getErrorMsg()));
			
			return -2;
		}
	}
	
	/**
	 * 向所有客户端推送消息
	 * @param Content
	 * @return
	 */
	public static int pushBroadcastMessage(String Content) {
		BaiduChannelClient channelClient = initPushClient();
		
		try {
			// 4. 创建请求类对象
			PushBroadcastMessageRequest request = new PushBroadcastMessageRequest();
			request.setDeviceType(3);// device_type => 1: web 2: pc 3:android 4:ios 5:wp
			request.setMessage(Content);
			// 5. 调用pushMessage接口
			PushBroadcastMessageResponse response = channelClient.pushBroadcastMessage(request);
			// 6. 认证推送成功
			System.out.println("push amount : " + response.getSuccessAmount());
			
			return response.getSuccessAmount();
		} catch (ChannelClientException e) {
			// 处理客户端错误异常
			e.printStackTrace();
			
			return -1;
		} catch (ChannelServerException e) {
			// 处理服务端错误异常
			System.out.println(String.format("request_id: %d, error_code: %d, error_message: %s",
					e.getRequestId(), e.getErrorCode(), e.getErrorMsg()));
			
			return -2;
		}
	}
	
	/**
	 * 向特定用户推送通知
	 * @param userId
	 * @param channelId
	 * @param content json格式的字符串
	 * @return
	 */
	public static int pushUnicastMessage(String userId, String channelId, int deviceType, String content) {
		BaiduChannelClient channelClient = initPushClient();
		
		try {
			PushUnicastMessageRequest request = new PushUnicastMessageRequest();
	        request.setDeviceType(deviceType == 1 ? 3 : 4);
//	        request.setChannelId(channelId);
	        request.setUserId(userId);

	        request.setMessageType(1);
	        request.setMessage(content);

	        // 5. 调用pushMessage接口
	        PushUnicastMessageResponse response = channelClient
	                .pushUnicastMessage(request);
			
            Logger.info("push amount : " + response.getSuccessAmount());
			
			return response.getSuccessAmount();
        } catch (ChannelClientException e) {
            // 处理客户端错误异常
            e.printStackTrace();
            
            return -1;
        } catch (ChannelServerException e) {
            // 处理服务端错误异常
            System.out.println(String.format(
                    "request_id: %d, error_code: %d, error_message: %s",
                    e.getRequestId(), e.getErrorCode(), e.getErrorMsg()));
            
            return -2;
        }
	}
}
