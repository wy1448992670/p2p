package controllers.front.help;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import play.Play;
import constants.Constants;
import constants.Constants.NewsTypeId;
import controllers.BaseController;
import models.t_content_advertisements_partner;
import models.t_content_news;
import models.v_news_types;
import business.CreditLevel;
import business.News;
import business.NewsType;
import business.Product;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 
 * @author liuwenhui
 * 
 */
public class HelpCenterAction extends BaseController {
	/**
	 * 首页
	 */
	public static void index(long typeId, String currPage, String pageSize, String keyword) {
		ErrorInfo error = new ErrorInfo();
		List<v_news_types> types = NewsType.queryTypeAndCount(NewsTypeId.HELP_CENTER, error);
		
		if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
		
		NewsType type = new NewsType();
		type.id = typeId;
		
		PageBean <t_content_news> pageBean = News.queryNewsByTypeId(typeId+"", currPage, pageSize, keyword, error);
		
		if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}

		render(types, pageBean, type, typeId);
	}
	
	/**
	 * 合作伙伴
	 */
	public static void partner(int currPage, int typeId,int pageSize) {
		ErrorInfo error = new ErrorInfo();
		
		PageBean<t_content_advertisements_partner> page = News.queryPartners(currPage, pageSize, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		List<Product> products = Product.queryProductNames(true, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		int id = typeId;
		NewsType parent = new NewsType();
		parent.id = 3;
		List<NewsType> types = NewsType.queryChildTypes(3, error);
		
		render(page, products, creditLevels, parent, types, id);
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年3月24日
	 * @description 合作伙伴-返回json
	 * @param currPage
	 * @param pageSize
	 */
	public static void partnerJson(int currPage,int pageSize,int type) {
		ErrorInfo error = new ErrorInfo();
		List<t_content_advertisements_partner> list = null;
		PageBean<t_content_advertisements_partner> page = null;
		if(type == 1){
			list = News.queryPartners(error);
		}else{
			page = News.queryPartners(currPage, pageSize, error);
			list = page.page;
		}
		if (error.code < 0) {
			renderJSON(error);
		}
		if(list != null && list.size() > 0){
			for(t_content_advertisements_partner partner : list){
				String src = partner.image_filename;
				if (StringUtils.contains(src, "images?uuid=")){
		        	if(!src.startsWith("http")){
		        		String uuid = StringUtils.substring(src, StringUtils.lastIndexOf(src, '=') + 1);
		        		String attachmentPath = Play.configuration.getProperty("attachments.path");
		        		src = (new StringBuilder().append(Play.ctxPath).append('/').append(attachmentPath).append('/').append(uuid)).toString();
		        	}
		        }
				partner.image_filename = src;
			}
		}
		
		renderJSON(type == 1 ? list : page);
	}
	
	/**
	 * 详情
	 */
	public static void detail(long newsId, String keyword) {
		
		
		ErrorInfo error = new ErrorInfo();
		
		List<v_news_types> types = NewsType.queryTypeAndCount(NewsTypeId.HELP_CENTER, error);
		
		if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
		
		List <News> newses = News.queryNewsDetail(newsId+"", keyword, error);
		
		if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
		
		Map<String, String> newsCount = News.queryCount(error);
		
		if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
		render(types, newses, keyword, newsCount);
	}
	
	/**
	 * 赞
	 * @param newsId
	 */
	public static void support(int newsId) {
		ErrorInfo error = new ErrorInfo();
		int support = News.support(newsId, error);
		
		if(error.code < 0) {
			renderJSON(error);
    	}
		
		renderText(support);
	}
	
	/**
	 * 踩
	 * @param newsId
	 */
	public static void opposition(int newsId) {
		ErrorInfo error = new ErrorInfo();
		int opposition = News.opposition(newsId, error);
		
		if(error.code < 0) {
			renderJSON(error);
    	}
		
		renderText(opposition);
	}
}
