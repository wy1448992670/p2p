package gateway.tonglian.service;

import gateway.tonglian.base.PropertyConfig;
import gateway.tonglian.bean.PayResult;
import gateway.tonglian.utils.TLPayUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.mvc.Scope;

/**
 * 通连支付服务类
 * 
 * @author yuy
 * @date 2015-05-15 15:02
 *
 */
public class TonglPayService {

	/**
	 * 异步请求信息处理
	 * 
	 * @return
	 */
	public static boolean checkSign(String jsonStr) {
		// 验签
		try {
			if (!TLPayUtil.checkSign(jsonStr, PropertyConfig.MD5_KEY, PayResult.checkedSignKey)) {
				Logger.info(log("支付结果通知验签失败"));
				return false;
			}
		} catch (Exception e) {
			Logger.info(log("支付结果通知报文解析异常：") + e);
			return false;
		}
		Logger.info(log("支付结果通知数据接收处理成功"));
		return true;
		// 解析异步通知对象
		// PayDataBean payDataBean = JSON.parseObject(reqStr,
		// PayDataBean.class);
		// TODO:更新订单，发货等后续处理
	}

	@SuppressWarnings("deprecation")
	public static Map<String, String> parseToMap(Scope.Params params) {
		Logger.info(log("进入支付结果通知数据处理"));

		// 接收
		String reqparams = null;
		try {
			reqparams = URLDecoder.decode(URLDecoder.decode(params.urlEncode(), "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			Logger.error(log("回调UrlDecode时 : %s "), e1.getMessage());
		}
		Map<String, String> paramMap = null;
		if (null != reqparams) {
			paramMap = new HashMap<String, String>();
			String param[] = reqparams.split("&");
			for (int i = 0; i < param.length; i++) {
				String content = param[i];
				String key = content.substring(0, content.indexOf("="));
				String value = content.substring(content.indexOf("=") + 1, content.length());
				try {
					paramMap.put(key, URLDecoder.decode(value, "UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					Logger.error(log("通联回调构造参数UrlDecode时%s"), e1.getMessage());
				}
				try {
					Logger.debug(log("~~~%s : %s"), key, URLDecoder.decode(value, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					Logger.error(log("回调UrlDecode时 : %s "), e.getMessage());
				}

			}
		}
		Logger.info(log("RespParams：" + paramMap.toString() + ""));
		return paramMap;
	}

	/**
	 * @param str
	 * @return
	 */
	public static String log(String str) {
		return "[" + PropertyConfig.name + "]：" + str;
	}

}
