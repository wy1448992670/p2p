package services;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import models.t_wechat_customers;
import net.sf.json.JSONObject;
import utils.WeChatUtil;

import com.shove.JSONUtils;
import com.shove.gateway.weixin.gongzhong.GongZhongObject;
import com.shove.gateway.weixin.gongzhong.vo.customservice.Account;
import com.shove.gateway.weixin.gongzhong.vo.customservice.Record;
import com.shove.gateway.weixin.gongzhong.vo.message.Message;

public class WeChatCustomerService extends GongZhongObject {
	/**
	 * 获取所有客服信息
	 * 
	 * @return
	 */
	public static List<t_wechat_customers> getKFList() {
		JSONObject reslutObj = WeChatUtil.httpRequest(
				"https://api.weixin.qq.com/cgi-bin/customservice/getkflist?access_token="
						+ WeChatGongZhongService.getAccessToken(), "GET", null);

		return JSONUtils.toList(reslutObj.get("kf_list"),
				t_wechat_customers.class);
	}

	/**
	 * 获取在线客服
	 * 
	 * @return
	 */
	public static List<Account> getOnlineKFList() {
		JSONObject reslutObj = WeChatUtil.httpRequest(
				"https://api.weixin.qq.com/cgi-bin/customservice/getonlinekflist?access_token="
						+ WeChatGongZhongService.getAccessToken(), "GET", null);

		return JSONUtils.toList(reslutObj.get("kf_online_list"), Account.class);
	}

	/**
	 * 获取客服聊天记录
	 * 
	 * @param starttime
	 * @param endtime
	 * @param openid
	 * @param pagesize
	 * @param pageindex
	 * @return
	 */
	public static List<Record> getRecordList(Date starttime, Date endtime,
			String openid, int pagesize, int pageindex) {
		String str = "{\"starttime\" : " + starttime.getTime() / 1000L
				+ ",\"endtime\" : " + endtime.getTime() / 1000L
				+ ",\"openid\" : \"" + openid + "\",\"pagesize\" : " + pagesize
				+ ",\"pageindex\" : " + pageindex + "}";

		JSONObject reslutObj = WeChatUtil.httpRequest(
				"https://api.weixin.qq.com/cgi-bin/customservice/getrecord?access_token="
						+ WeChatGongZhongService.getAccessToken(), "POST", str);

		return JSONUtils.toList(reslutObj.get("recordlist"), Record.class);
	}

	/**
	 * 添加客服
	 * 
	 * @param kf_account
	 * @param nickname
	 * @param password
	 * @return
	 */
	public static JSONObject addCustomer(String kf_account, String nickname,
			String password) {
		String str = "{\"kf_account\" : " + kf_account + ",\"nickname\" : "
				+ nickname + ",\"password\" : " + password + "}";
		JSONObject resultObj = WeChatUtil.httpRequest(
				"https://api.weixin.qq.com/customservice/kfaccount/add?access_token="
						+ WeChatGongZhongService.getAccessToken(), "POST", str);

		return resultObj;
	}

	/**
	 * 设置客服信息
	 * 
	 * @param kf_account
	 * @param nickname
	 * @param password
	 * @return
	 */
	public static JSONObject updateCustomer(String kf_account, String nickname,
			String password) {
		String str = "{\"kf_account\" : " + kf_account + ",\"nickname\" : "
				+ nickname + ",\"password\" : " + password + "}";
		JSONObject resultObj = WeChatUtil.httpRequest(
				"https://api.weixin.qq.com/customservice/kfaccount/update?access_token="
						+ WeChatGongZhongService.getAccessToken(), "POST", str);

		return resultObj;
	}

	/**
	 * 上传客服图像
	 * 
	 * @return
	 */
	public static JSONObject uploadCustomerImage() {

		return null;
	}

	/**
	 * 删除指定客服
	 * 
	 * @param kf_account
	 * @return
	 */
	public static JSONObject deleteCustomer(String kf_account) {
		String requestUrl = "https://api.weixin.qq.com/customservice/kfaccount/del?access_token=ACCESS_TOKEN&kf_account=KFACCOUNT";
		requestUrl = requestUrl.replace("ACCESS_TOKEN",WeChatGongZhongService.getAccessToken());
		requestUrl = requestUrl.replace("KFACCOUNT", kf_account);

		JSONObject resultObj = WeChatUtil.httpRequest(requestUrl, "GET", null);

		return resultObj;

	}

	/**
	 * 查询是否有可接入的在线客服人员,绑定就算在线
	 * 
	 * @return
	 */
	public static boolean isUseCustomer() {
		List<Account> list = getOnlineKFList();
		if (null != list && list.size() > 0) {
			for (Account account : list) {
				int person = account.getAuto_accept()
						- account.getAccepted_case();

				if (person > 0) {

					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 转接，这个方法在play中不适用
	 * @param response
	 * @param message
	 * @throws IOException
	 */
	public static void transferCustomerService(HttpServletResponse response,
			Message message) throws IOException {
		response.getWriter()
				.print("<xml><ToUserName><![CDATA["
						+ message.getFromUserName()
						+ "]]></ToUserName>"
						+ "<FromUserName><![CDATA["
						+ message.getToUserName()
						+ "]]></FromUserName>"
						+ "<CreateTime>"
						+ message.getCreateTime()
						+ 1
						+ "</CreateTime><MsgType>"
						+ "<![CDATA[transfer_customer_service]]></MsgType></xml>");
	}

	/**
	 * 转接，特定客服，这个方法在play中适用，需要改。
	 * @param response
	 * @param message
	 * @param kfAccount
	 * @throws IOException
	 */
	public static void transferCustomerService(HttpServletResponse response,
			Message message, String kfAccount) throws IOException {
		response.getWriter().print(
				"<xml><ToUserName><![CDATA[" + message.getFromUserName()
						+ "]]></ToUserName>" + "<FromUserName><![CDATA["
						+ message.getToUserName() + "]]></FromUserName>"
						+ "<CreateTime>" + message.getCreateTime() + 1
						+ "</CreateTime><MsgType>"
						+ "<![CDATA[transfer_customer_service]]></MsgType>"
						+ "<TransInfo><KfAccount>" + kfAccount
						+ "</KfAccount></TransInfo></xml>");
	}

}