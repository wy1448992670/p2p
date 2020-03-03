package controllers.front.principal;

import java.util.List;
import java.util.Map;
import constants.Constants;
import controllers.BaseController;
import models.t_content_advertisements;
import models.t_content_news;
import business.Ads;
import business.CreditLevel;
import business.DealDetail;
import business.News;
import business.NewsType;
import business.Product;
import utils.ErrorInfo;

/**
 * 
 * @author liuwenhui
 *
 */
public class PrincipalGuaranteeAction extends BaseController{
	/*前台本金保障首页*/
    public static void principalGuaranteeHome(){
    	
    	ErrorInfo error = new ErrorInfo();
    	
    	List<t_content_advertisements> ads = Ads.queryAdsByLocation(Constants.ENSURE_PAGE, error);

    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	t_content_news principal = News.getPrincipalGuaranteeNews(21l,error);//什么是本金保障计划
    	
    	if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	t_content_news principalrul = News.getPrincipalGuaranteeNews(22l,error);//本金保障规则
    	
    	if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
    	t_content_news paymentProcess = News.getPrincipalGuaranteeNews(23l,error);//赔付流程
    	
    	if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
    	t_content_news investmentStrategy = News.getPrincipalGuaranteeNews(24l,error);//投资攻略三部曲
    	
    	if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
    	t_content_news FAQ = News.getPrincipalGuaranteeNews(25l,error);//本金保障常见问题
    	
    	if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
    	
    	Map<String, Double> currTotal = DealDetail.currTotal(error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<Product> products = Product.queryProductNames(true, error);
    	
    	List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);
    	
    	List<NewsType> types = NewsType.queryChildTypes(4, error);
    	
    	render(ads,currTotal,principal,principalrul,paymentProcess,investmentStrategy,FAQ, products, types, creditLevels);
     }
    
    /*前台本金保障首页*/
    public static void principalGuaranteeAppHome(){
    	
    	ErrorInfo error = new ErrorInfo();
    	
    	List<t_content_advertisements> ads = Ads.queryAdsByLocation(Constants.ENSURE_PAGE, error);

    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	t_content_news principal = News.getPrincipalGuaranteeNews(21l,error);//什么是本金保障计划
    	
    	if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	t_content_news principalrul = News.getPrincipalGuaranteeNews(22l,error);//本金保障规则
    	
    	if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
    	t_content_news paymentProcess = News.getPrincipalGuaranteeNews(23l,error);//赔付流程
    	
    	if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
    	t_content_news investmentStrategy = News.getPrincipalGuaranteeNews(24l,error);//投资攻略三部曲
    	
    	if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
    	t_content_news FAQ = News.getPrincipalGuaranteeNews(25l,error);//本金保障常见问题
    	
    	if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
    	
    	Map<String, Double> currTotal = DealDetail.currTotal(error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	List<Product> products = Product.queryProductNames(true, error);
    	
    	List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);
    	
    	List<NewsType> types = NewsType.queryChildTypes(4, error);
    	
    	render(ads,currTotal,principal,principalrul,paymentProcess,investmentStrategy,FAQ, products, types, creditLevels);
     }
    	
    }
   
