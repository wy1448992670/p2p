package controllers.wechat.others;

import utils.ErrorInfo;
import controllers.BaseController;
import controllers.wechat.account.WechatAccountHome;
import business.News;

/**
 * 关于我们
 * @author zhs
 * @date 2015-3-9 下午08:48:56
 */
public class AboutUs extends BaseController{

	public static void aboutTeam(){
		ErrorInfo error = new ErrorInfo();
		
		String content = News.queryContentByTypeId(-1004, error);
		
		if(error.code < 0){
			
			WechatAccountHome.errorShow(error.msg);
		}
		
		render(content);
	}
	
}
