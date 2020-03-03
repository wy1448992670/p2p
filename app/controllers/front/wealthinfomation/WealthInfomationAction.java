package controllers.front.wealthinfomation;

import java.util.List;
import constants.Constants;
import controllers.BaseController;
import business.Ads;
import business.CreditLevel;
import business.Invest;
import business.News;
import business.NewsType;
import business.Product;
import models.t_content_advertisements;
import models.t_content_news;
import models.v_bill_board;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 
 * @author liuwenhui
 *
 */
public class WealthInfomationAction extends BaseController{
	/**
	 * 前台财富资讯首页
	 */
    public static void home(String typeId){
    	ErrorInfo error = new ErrorInfo();
    	
    	/* 此注释是因为财富咨询中换成了最新资讯新闻  */
    	//Bid bid = null; // 最新发布的借款标 
		//List<Bid> bids = Bid.queryAdvertisement(error);
		
		//if(null != bids && bids.size() > 0)
		//	bid = bids.get(0);
		
    	//List<NewsType> types = NewsType.queryChildTypes(1L, error);
    	
    	/*if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<t_content_advertisements> ads = Ads.queryAdsByLocation(Constants.FUN_PAGE, error);

    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<t_content_news> homeNews = News.queryNewForFrontHome(error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<t_content_news> headlines = News.queryNewForFrontHeadlines(error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<t_content_news> marquee = News.queryNewForFrontMarquee(error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<t_content_news> newsNotice = News.queryNewForFront(Constants.NewsTypeId.OFFICIAL_AMMOUNCEMENT, 5, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<t_content_news> lt1 = News.queryNewForFront(Constants.NewsTypeId.INTERNET_BANKING, 7, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}

    	List<t_content_news> lt2 = News.queryNewForFront(Constants.NewsTypeId.LOAN_MONPOLY, 7, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}

    	List<t_content_news> lb1 = News.queryNewForFront(Constants.NewsTypeId.BORROWING_TECHNIQUES, 7, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}

    	List<t_content_news> lb2 = News.queryNewForFront(Constants.NewsTypeId.MONEY_TIPS, 7, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}

    	List<t_content_news> rb1 = News.queryNewForFront(Constants.NewsTypeId.SUCCESS_STROY, 3, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<v_bill_board> investBillboard = Invest.investBillboard();//理财风云榜
    	
    	List<Product> products = Product.queryProductNames(true, error);
    	
    	List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);
    	
    	render(types, ads, homeNews, headlines, marquee, newsNotice, lt1, lt2, lb1, lb2, rb1, investBillboard,creditLevels, products);
    	*/
    	
    	String currPage = params.get("currPage");
    	String pageSize = params.get("pageSize");
    	
    	NewsType type = new NewsType();
    	type.id = Integer.parseInt(typeId);
	
    	PageBean <t_content_news>  pageBean = News.queryNewsByTypeId(typeId, currPage, pageSize, "", error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<NewsType> types = NewsType.queryChildTypes(1L, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	//List<t_content_advertisements> ads = Ads.queryAdsByLocation(Constants.NEWS_PAGE, error);
    	
//    	if(error.code < 0) {
//    		render(Constants.ERROR_PAGE_PATH_FRONT);
//    	}
    	
    	render(pageBean, types,typeId,type);
    }
    
    /**
	 * 前台财富资讯首页（Rest风格）
	 */
    public static void restHome(String typeId){
    	ErrorInfo error = new ErrorInfo();
    	
    	/* 此注释是因为财富咨询中换成了最新资讯新闻  */
    	//Bid bid = null; // 最新发布的借款标 
		//List<Bid> bids = Bid.queryAdvertisement(error);
		
		//if(null != bids && bids.size() > 0)
		//	bid = bids.get(0);
		
    	//List<NewsType> types = NewsType.queryChildTypes(1L, error);
    	
    	/*if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<t_content_advertisements> ads = Ads.queryAdsByLocation(Constants.FUN_PAGE, error);

    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<t_content_news> homeNews = News.queryNewForFrontHome(error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<t_content_news> headlines = News.queryNewForFrontHeadlines(error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<t_content_news> marquee = News.queryNewForFrontMarquee(error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<t_content_news> newsNotice = News.queryNewForFront(Constants.NewsTypeId.OFFICIAL_AMMOUNCEMENT, 5, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<t_content_news> lt1 = News.queryNewForFront(Constants.NewsTypeId.INTERNET_BANKING, 7, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}

    	List<t_content_news> lt2 = News.queryNewForFront(Constants.NewsTypeId.LOAN_MONPOLY, 7, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}

    	List<t_content_news> lb1 = News.queryNewForFront(Constants.NewsTypeId.BORROWING_TECHNIQUES, 7, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}

    	List<t_content_news> lb2 = News.queryNewForFront(Constants.NewsTypeId.MONEY_TIPS, 7, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}

    	List<t_content_news> rb1 = News.queryNewForFront(Constants.NewsTypeId.SUCCESS_STROY, 3, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<v_bill_board> investBillboard = Invest.investBillboard();//理财风云榜
    	
    	List<Product> products = Product.queryProductNames(true, error);
    	
    	List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);
    	
    	render(types, ads, homeNews, headlines, marquee, newsNotice, lt1, lt2, lb1, lb2, rb1, investBillboard,creditLevels, products);
    	*/
    	
    	String currPage = params.get("currPage");
    	String pageSize = params.get("pageSize");
    	
    	NewsType type = new NewsType();
    	type.id = Integer.parseInt(typeId);
	
    	PageBean <t_content_news>  pageBean = News.queryNewsByTypeId(typeId, currPage, pageSize, "", error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<NewsType> types = NewsType.queryChildTypes(1L, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	//List<t_content_advertisements> ads = Ads.queryAdsByLocation(Constants.NEWS_PAGE, error);
    	
//    	if(error.code < 0) {
//    		render(Constants.ERROR_PAGE_PATH_FRONT);
//    	}
    	
    	render(pageBean, types,typeId,type);
    }
    
    /**
     * 前台财富首页--新闻列表
     */
    public static void newList(){
    	ErrorInfo error = new ErrorInfo();
    	List<NewsType> types = NewsType.queryChildTypes(1L, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<t_content_advertisements> ads = Ads.queryAdsByLocation(Constants.NEWS_PAGE, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	render(types, ads);
    }
    
    /**
     * 前台财富首页--新闻列表ajax
     */
    public static void newsList(String typeId) {
    	ErrorInfo error = new ErrorInfo();
    	String currPage = params.get("currPage");
    	String pageSize = params.get("pageSize");
    	
    	PageBean <t_content_news>  newsList = News.queryNewsByTypeId(typeId, currPage, pageSize, "", error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<NewsType> types = NewsType.queryChildTypes(1L, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<t_content_advertisements> ads = Ads.queryAdsByLocation(Constants.NEWS_PAGE, error);
    	
//    	if(error.code < 0) {
//    		render(Constants.ERROR_PAGE_PATH_FRONT);
//    	}
    	
    	render(newsList, types,typeId, ads);
    }
    
    /**
     * 前台财富首页--新闻详情页面
     */
    public static void newDetails(String id){
    	//String id = params.get("id");
    	ErrorInfo error = new ErrorInfo();
    	
    	List<NewsType> types = NewsType.queryChildTypes(1L, error);
    	
    	
    	List <News> newsDetail = News.queryNewsDetail(id, null, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
//    	List<t_content_advertisements> ads = Ads.queryAdsByLocation(Constants.NEWS_DETAIL_PAGE, error);
//    	
//    	if(error.code < 0) {
//    		render(Constants.ERROR_PAGE_PATH_FRONT);
//    	}
    	
    	render(newsDetail,types);
    }
    
    /**
     * 前台财富首页--新闻详情页面(Rest风格)
     */
    public static void restNewDetails(String id){
    	//String id = params.get("id");
    	ErrorInfo error = new ErrorInfo();
    	
    	List<NewsType> types = NewsType.queryChildTypes(1L, error);
    	
    	
    	List <News> newsDetail = News.queryNewsDetail(id, null, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
//    	List<t_content_advertisements> ads = Ads.queryAdsByLocation(Constants.NEWS_DETAIL_PAGE, error);
//    	
//    	if(error.code < 0) {
//    		render(Constants.ERROR_PAGE_PATH_FRONT);
//    	}
    	
    	render(newsDetail,types);
    }
    /*财富工具箱*/
    public static void wealthBox(){
    	render();
    }
    /*信用等级图标说明*/
    public static void creditratingiconDescription(){
    	render();
    }
    /*借款标图标说明*/
    public static void borrowIconDescription(){
    	render();
    }
    
}
