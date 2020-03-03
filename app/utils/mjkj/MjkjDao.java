/******************************************************************
 *
 *    Java Lib For Android, Powered By Shenzhen Jiuzhou.
 *
 *    Copyright (c) 2001-2014 Digital Telemedia Co.,Ltd
 *    http://www.d-telemedia.com/
 *
 *    Package:     utils.mjkj
 *
 *    Filename:    MjkjService.java
 *
 *    Description: TODO(用一句话描述该文件做什么)
 *
 *    Copyright:   Copyright (c) 2001-2014
 *
 *    Company:     Digital Telemedia Co.,Ltd
 *
 *    @author:     zj
 *
 *    @version:    1.0.0
 *
 *    Create at:   Jan 24, 2019 11:46:14 AM
 *
 *    Revision:
 *
 *    Jan 24, 2019 11:46:14 AM
 *        - first revision
 *
 *****************************************************************/
package utils.mjkj;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import play.Logger;
import play.Play;
import utils.GzipUtil;

/**
 * @ClassName MjkjService
 * @Description 摩羯科技第三方接口
 * @author zj
 * @Date Jan 24, 2019 11:46:14 AM
 * @version 1.0.0
 */
public class MjkjDao {

	/**
	 * @Description 得到运营商h5页面url地址
	 * @param userId
	 * @param themeColor
	 * @return url地址
	 * @author: zj
	 */
	public static String getOperatorH5Url(long userId, String themeColor, String calbackUrl) {
		String domainUrl = Play.configuration.getProperty("zyy.mjkj.domainUrl");
		String apiKey = Play.configuration.getProperty("zyy.mjkj.apiKey");
		String urlString = Play.configuration.getProperty("zyy.mjkj.url").replace("{apiKey}", apiKey)
				.replace("{backUrl}", URLEncoder.encode(calbackUrl.concat("/pc/ymd/getOperatorAuthCallBack")))
				.replace("{userId}", userId + "").replace("{themeColor}", themeColor);
		return domainUrl.concat(urlString);
	}

	public static void main(String[] args) {
		Logger.info(Play.applicationPath.getPath());
	}

	/**
	 * 获取运营商报告数据最原始 最全的
	 * 
	 * @这里用一句话描述这个方法的作用
	 * @param mobile
	 * @param taskId
	 * @return
	 * @author: zj
	 */
	public static String getMxData(String mobile, String taskId) {
		try {
			String token = Play.configuration.getProperty("zyy.mjkj.token");
			URL url = new URL(Play.configuration.getProperty("zyy.mjkj.report.mxreport.url").replace("{mobile}", mobile)
					.replace("{taskId}", taskId));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Authorization", "token " + token);
			connection.connect();
			String result = GzipUtil.uncompress(connection.getInputStream());
			connection.disconnect();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("方法 getMxData() 出错============>" , e.getMessage());
			return null;
		}
	}
}