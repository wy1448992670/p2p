package controllers.wechat.others;

import java.util.List;

import net.sf.json.JSONObject;

import models.t_content_news;
import models.v_content_wechat_news;
import business.News;
import utils.ErrorInfo;
import controllers.BaseController;
import controllers.wechat.account.WechatAccountHome;

/**
 * 财富资讯
 * @author zhs
 * @date 2015-3-9 下午09:11:31
 */
public class WealthInfomation extends BaseController{

	/**
	 * 财富资讯信息
	 * @param mark 翻页用，用来标识是否是ajax请求翻页
	 */
	public static void wealthinfos(int mark){
		ErrorInfo error = new ErrorInfo();
		List<v_content_wechat_news> page = News.queryNewWechat(error);
		
		if(mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		
		render(page);
	}
	
	/**
	 * 财富资讯详情
	 * @param id 新闻id
	 */
	public static void wealthDetails(long id){
		ErrorInfo error = new ErrorInfo();
		
		t_content_news news = new t_content_news();
		news = News.queryNewWechatDetails(id, error);
		
        if(error.code < 0){
			
			WechatAccountHome.errorShow(error.msg);
		}
		
		render(news);
	}
}
