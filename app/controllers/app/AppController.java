package controllers.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.t_activity_center;
import models.t_content_advertisements;
import models.t_dict_ad_citys;
import models.t_dict_ad_provinces;
import models.t_forum_posts;
import models.t_forum_type;
import models.t_mall_address;
import models.t_mall_goods;
import models.t_mall_scroe_record;
import models.t_red_packages_type;
import models.t_users;
import models.v_forum_posts;
import models.v_forum_posts_questions;
import models.v_posts_questions;
import models.v_repayment_news;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.cache.Cache;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;
import business.Ads;
import business.BackstageSet;
import business.Bid;
import business.MallAddress;
import business.MallGoods;
import business.MallScroeRecord;
import business.Posts;
import business.RedPackage;
import business.User;

import com.alibaba.fastjson.JSON;
import com.shove.gateway.GeneralRestGateway;
import com.shove.gateway.GeneralRestGatewayInterface;

import constants.Constants;
import controllers.BaseController;
import controllers.app.request.*;

/**
 * app控制器
 * Description:处理app网关传过来的参数并调用对应的处理方法
 * @author zhs
 * vesion: 6.0 
 * @date 2014-10-29 上午11:46:34
 */
public class AppController extends BaseController implements GeneralRestGatewayInterface {

	/**
	 * app端请求服务器的入口
	 * @throws IOException
	 */
	public static void index() throws IOException {
		StringBuilder errorDescription = new StringBuilder();
		AppController app = new AppController();
	
//		if(Play.mode == Play.Mode.DEV){
//			TestApp.t(app, errorDescription);
//			Logger.error("%s", errorDescription);
//			return;
//		}
		
    	int code = com.yiyilc.http.gateway.GeneralRestGateway.handle(Constants.APP_ENCRYPTION_KEY, 3000, app, errorDescription);
		//int code = com.shove.gateway.GeneralRestGateway.handle(Constants.APP_ENCRYPTION_KEY, 3000, app, errorDescription);
    	System.out.println(code);
    	if(code < 0) {
    		Logger.error("%s", errorDescription);
    	}

	}
	
	/**
	 * 扫描二维码下载app
	 */
	public static void download(){
		render();
	}
	
	/**
	 * 标的分享
	 */
	public static void shareBid(long bidId,String recommend){
		Bid bid = new Bid();
		bid.id = bidId;
		render(bid,recommend);
	}

	/**
	 * 积分收入明细
	 */
	public static void mallScroeEarnings(int currPage, String user_id, int Mark) {
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(user_id, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
		if (error.code < 0) {
			unUser();
		}
		int pageSize = Constants.APP_PAGESIZE;
		PageBean<t_mall_scroe_record> page = MallScroeRecord.queryScroeRecordByApp(userId, currPage, pageSize, error);

		// 下拉分页
		if (Mark == 2) {
			JSONObject json = new JSONObject();
			json.put("page", page);

			renderJSON(json);
		}

		render(page, user_id);
	}

	/**
	 * 积分消耗明细
	 */
	public static void mallScroeExpend(int currPage, String user_id, int Mark) {
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(user_id, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
		if (error.code < 0) {
			unUser();
		}
		int pageSize = Constants.APP_PAGESIZE;
		PageBean<t_mall_scroe_record> page = MallGoods.queryHasExchangedGoodsForPage(userId, currPage, pageSize, error);

		// 下拉分页
		if (Mark == 2) {
			JSONObject json = new JSONObject();
			json.put("page", page);

			renderJSON(json);
		}

		render(page, user_id);
	}

	/**
	 * 关于积分
	 */
	public static void scroeInfo() {

		render();
	}

	/**
	 * 商品详情
	 */
	public static void mallGoodsDetail(long id, String user_id) {

		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(user_id, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
		if (error.code < 0) {
			unUser();
		}
		t_mall_goods t_mall_goods = MallGoods.queryGoodsDetailById(id);

		List<t_dict_ad_provinces> provinces = (List<t_dict_ad_provinces>) Cache.get("provinces");
		User user = new User();
		user.id = userId;
		List<t_dict_ad_citys> cityList = User.queryCity(user.provinceId);

		t_mall_address address = MallAddress.queryAddress(userId, error);

		int totalScroe = MallScroeRecord.currentMyScroe(userId);

		render(user_id, t_mall_goods, provinces, cityList, address, totalScroe);
	}

	/***
	 * 积分商城
	 * 
	 * @param user_id 用户sign
	 * @param score 用户签到所获积分 0：不是从签到送积分页面请求 其他：签到页面
	 */
	public static void mallGoodsInfo(String user_id, int score) {

		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(user_id, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
		if (error.code < 0) {
			unUser();
		}
		int totalScroe = MallScroeRecord.currentMyScroe(userId);

		List<t_mall_goods> mallList = MallGoods.queryGoods();

		List<t_red_packages_type> redlist = RedPackage.queryRedPackagesList();

		int[] nscores = MallScroeRecord.queryScroeRecord(userId, error);

		int nscore = nscores[0];
		int totalSign = nscores[1];

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();

		// 总共签到的次数
		// int totalSign = MallScroeRecord.queryScoreSignCount(userId, MallConstants.SIGN, error);
		// totalSign = totalSign % 5;
		render(totalScroe, mallList, user_id, redlist, score, totalSign, nscore, backstageSet);
	}

	/**
	 * 收益分享
	 * 
	 * @param bidId
	 * @param recommend
	 */
	public static void applogining() {
		render();
	}

	/**
	 * 收益分享
	 * 
	 * @param bidId
	 * @param recommend
	 */
	public static void shareIncome(String avator,String income,String recommend){
		render(avator,income,recommend);
	}
	
	public static String addSign(long id, String action) {
		String des=com.shove.security.Encrypt.encrypt3DES(id+","+action+","+DateUtil.dateToString(new Date()), 
				Constants.APP_ENCRYPTION_KEY);
		String md5=com.shove.security.Encrypt.MD5(des+Constants.APP_ENCRYPTION_KEY);
		String sign=des+md5.substring(0, 8);
		
		Logger.info("id:"+id+"; action:"+action+" "+DateUtil.dateToString(new Date()) + " "+Constants.APP_ENCRYPTION_KEY);
		return sign;
	}
	
	/**
	 * 论坛首页显示
	 */
	public static void showForum(String user_id,int Mark){
		ErrorInfo error = new ErrorInfo();
		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String content = params.get("content");
		
	
		String userIdtttt = Security.addSign(4,Constants.USER_ID_SIGN);
		
		System.out.print("&&&&&&&&&&&&&&&&&&&&&=="+userIdtttt+"==%%%%%%%");
		
		int count = 0;
		if(StringUtils.isNotBlank(user_id)){
			long userIds = Security.checkSign(user_id, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
			if(error.code < 0){
				unUser();
			}
			
			count = Posts.queryMyNewCount(userIds);
		}
		
		
		
		//帖子查询
		PageBean<t_forum_posts> page = Posts.earchPostsByName(content,currPage,pageSize);
		//下拉分页
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		// 广告条图片
		List<t_content_advertisements> homeAds = Ads.queryAdsByLocation(Constants.HOME_APP_WAP, error); 
		//帖子类型
		List<t_forum_type> typeList1 = Posts.queryPostsType(Constants.APP_WAP_POST_TYPE_PAGE_O,Constants.APP_WAP_POST_TYPE_SIZE);
		List<t_forum_type> typeList2 = Posts.queryPostsType(Constants.APP_WAP_POST_TYPE_PAGE_T,Constants.APP_WAP_POST_TYPE_SIZE);
		String userId = user_id;
		
		render(page,typeList1,typeList2,userId,homeAds,count);
	}
	
	/**
	 * 用户失效跳转页面
	 */
	public static void unUser(){
		render();
	}
	
	/**
	 * 
	 * @param url 1前往充值 2产品中心
	 */
	public static void activityCenterConnect(){
		String name = params.get("name");
		int url = 0;
		if(name.equals(Constants.APP_URL_NAME_FIRST)){
			url = 1;
		}else if(name.equals(Constants.APP_URL_NAME_TOW)){
			url = 2;
		}
		render(url);
	}
	
	/**
	 * 论坛搜索
	 */
	public static void forumSearch(int Mark){
		//ErrorInfo error = new ErrorInfo();
		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String showType = params.get("showType");
		String content = params.get("content");
		String userId = params.get("user_id");
		/*if(StringUtils.isBlank(userId)){
			flash.error("用户信息错误");
			render();
		}
		Security.checkSign(userId, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
		if(error.code < 0){
			unUser();
		}*/
		
		
		PageBean<t_forum_posts> page = new PageBean<t_forum_posts>();
		
		if(StringUtils.isNotBlank(showType)){
			//帖子查询
			page = Posts.earchPostsByName(content,currPage,pageSize);
			//下拉分页
			if(Mark == 2){
				JSONObject json = new JSONObject();
				json.put("page", page);
				
				renderJSON(json);
			}
		}else{
			page.page = new ArrayList<t_forum_posts>();
		}
		
		render(page,content,userId);
	}
	
	/**
	 * 我的社区
	 */
	public static void myCommunity(String userIdStr){
		ErrorInfo error = new ErrorInfo();
		//String userIdStr = params.get("userId");
		/*if(StringUtils.isBlank(userIdStr)){
			render();
		}
		long userId = Long.parseLong(userIdStr);*/
		
		
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
		if(userId < 0){
			unUser();
		}
		t_users user = t_users.findById(userId);
		
		//long userId = 4;
		String name = "";
		boolean type = true;
		if(StringUtils.isBlank(user.forum_name)){
			name =  user.mobile.substring(0,4) + "****" + user.mobile.substring(8, user.mobile.length());
		}else{
			name = user.forum_name;
			type = false;
		}
		//查询我的新消息
		int count = Posts.queryMyNewCount(userId);
		//render(name,Security.addSign(userId, Constants.BID_ID_SIGN),type);
		render(name,userIdStr,type,count);
	}
	
	/**
	 * 用户发帖页面
	 * @param userId
	 */
	public static void userPosts(String userId,long postsType){
		List<t_forum_type> list = Posts.queryUserPostsType();
		if(postsType == 0){
			postsType = list.get(0).id;
		}
		render(list,postsType,userId);
	}
	
	/**
	 * 用户发帖
	 */
	public static void userAddPosts(){
		ErrorInfo error = new ErrorInfo();
		String userIdStr  = params.get("userId");
		String postsTypeStr  = params.get("postsType");
		String title  = params.get("title");
		String contents  = params.get("contents");
		
		if(StringUtils.isBlank(postsTypeStr)) {
			flash.error("帖子类型错误");
			userPosts(userIdStr,0);
		}
		if(StringUtils.isBlank(userIdStr)) {
			flash.error("用户信息错误");
			userPosts(userIdStr,Long.parseLong(postsTypeStr));
		}
		
		if(StringUtils.isBlank(title)) {
			flash.error("标题不能为空");
			userPosts(userIdStr,Long.parseLong(postsTypeStr));
		}
		if(StringUtils.isBlank(contents)) {
			flash.error("内容不能为空");
			userPosts(userIdStr,Long.parseLong(postsTypeStr));
		} 
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
	
		if(userId < 0){
			unUser();
		}
		Posts tPosts = new Posts();
		tPosts.show_time = new Date();
		
		tPosts.user_id = userId;
		tPosts.type_id = Integer.parseInt(postsTypeStr);
		tPosts.show_image = 0;
		tPosts.title = title;
		tPosts.content = contents;
		t_users user = t_users.find(" id = ? ", userId).first();
		
		tPosts.name = user.forum_name != null ? user.forum_name : user.mobile.substring(0,4)+"***"+user.mobile.substring(8);
		//tPosts.keywords = keywords;
		
		tPosts.addPostsInfo(0, error);
		
		flash.error(error.msg);
		showForum(userIdStr, 0);
		//userPosts(userIdStr,Long.parseLong(postsTypeStr));
	}
	/**
	 * 我的帖子
	 * @param userId
	 */
	public static void myPosts(String userId,int Mark){
		ErrorInfo error = new ErrorInfo();
		String currPage  = params.get("currPage");
		if(StringUtils.isBlank(userId)){
			flash.error("参数错误");
			myCommunity(userId);
		}
		long userIds = Security.checkSign(userId, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
		
		PageBean<v_forum_posts> page = Posts.queryForumPosts(0,  null,
				"0", "0", currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error,userIds);
		if(userIds < 0){
			unUser();
		}
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		render(page,userId);
	}
	
	/**
	 * 我的帖子详细信息
	 * @param id
	 * @param userId
	 */
	public static void myPostsInfo(String id,String userId,int Mark){
		ErrorInfo error = new ErrorInfo();
		PageBean<v_posts_questions> page = new PageBean<v_posts_questions>();
		if(StringUtils.isBlank(id)){
			page.page = new ArrayList<v_posts_questions>();
			flash.error("参数错误");
			render(page);
		}
		
		
		long ids = Long.parseLong(id);
		
		//查询帖子详细信息
		String currPageStr = params.get("currPage");
		
		page = Posts.showAppPostsQuestions(ids, 0, null, currPageStr, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		long userIds = 0;
		if(StringUtils.isNotBlank(userId)){
			userIds = Security.checkSign(userId, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
			
			
			if(error.code < 0){
				unUser();
			}
			
			//判断是否显示回复按钮
			for(v_posts_questions vo : page.page){
				if(userIds == vo.user_id){
					vo.answersShow = false;
				}
			}
			
			String answers = params.get("answers");
			String questionsId = params.get("questionsId");
			//查看我的消息时改变查看状态
			if(answers == null || !"0".equals(answers)){
				if(StringUtils.isNotBlank(questionsId)){
					Posts.updateMyNewStatus(Long.parseLong(questionsId));
				}
			}else{
				//针对管理员回复
				if(StringUtils.isNotBlank(questionsId)){
					Posts.updateAdminMyNewStatus(Long.parseLong(questionsId));
				}
				
			}
			
		}
		
		//增加浏览次数
		Posts.updatePostReadCount(ids);
		boolean type = false;
		boolean collection = false;
		
		
		t_forum_posts t = Posts.queryPosts(ids);
		
		if(userIds > 0 && null != t && userIds != t.user_id){
			type = true;
			//是否有收藏
			collection = Posts.queryMyCollection(userIds, ids) > 0 ? true : false;
		}
		
		
		render(id,userId,t,page,type,collection,ids);
	}
	
	/**
	 * 提交帖子回复
	 */
	public static void submitAnswers(){
		String id = params.get("id");
		String userId = params.get("userId");
		
		String answersUserId = params.get("answersUserId");
		String content = params.get("textContent");
		
		if(StringUtils.isBlank(content)){
			flash.error("回复内容不能为空");
			myPostsInfo(id,userId,0);
		}
		if(StringUtils.isBlank(id) || Long.parseLong(id) <= 0){
			flash.error("参数错误");
			myPostsInfo("",userId,0);
		}
		if(StringUtils.isBlank(userId)){
			flash.error("参数错误");
			myPostsInfo(id,"",0);
		}
		
		ErrorInfo error = new ErrorInfo();
		
		long userIds = Security.checkSign(userId, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
		
		if(userIds < 0){
			unUser();
		}
		
		
		Posts p = new Posts();
		long answersUserIdSin = 0;
		if(StringUtils.isNotBlank(answersUserId)){
		
			answersUserIdSin = Security.checkSign(answersUserId, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
			
			if(answersUserIdSin < 0){
				flash.error("参数错误");
				myPostsInfo(id,userId,0);
			}
			
			p.to_answer_user_id = answersUserIdSin;
			if(answersUserIdSin == 0){
				p.to_answer_user = "管理员";
			}else{
				t_users t = t_users.findById(answersUserIdSin);
				p.to_answer_user = t.forum_name == null ? t.mobile : t.forum_name;
			}
			
		}
		
		p.posts_id = Long.parseLong(id);
		p.content = content;
		p.user_id = userIds;
		p.savePostsAnswers(error);
		if(error.code < 0){
			
			flash.error("回帖失败");
		}else{
			
			flash.error("回帖成功");
		}
		//增加回帖次数
		Posts.updateAppAnswersCount(Long.parseLong(id));
		myPostsInfo(id,userId,0);
	}
	
	/**
	 * app删除帖子
	 * @param id
	 */
	public static void appDeletePosts(String id,String userId){
		ErrorInfo error = new ErrorInfo();
		if(StringUtils.isBlank(id)){
			flash.error("参数错误");
			myPostsInfo(id,userId,0);
		}
		if(StringUtils.isBlank(userId)){
			flash.error("参数错误");
			myPostsInfo(id,userId,0);
		}
		long userIds = Security.checkSign(userId, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
		
		if(userIds < 0){
			unUser();
		}
		Posts.deletePosts(Long.parseLong(id),-1,error);
		flash.error("帖子删除成功");
		//myPostsInfo(id,userId,0);
		showForum(userId,0);
	}
	
	/**
	 * 跳转修改昵称
	 * @param userId
	 */
	public static void updateForumName(String userId){
		render(userId);
	}
	
	/**
	 * 提交社区昵称
	 * @param userId
	 * @param name
	 */
	public static void submitForumName(String userId,String forumName){
		ErrorInfo error = new ErrorInfo();
		if(StringUtils.isBlank(forumName)) {
			flash.error("昵称为空");
			updateForumName(userId);
		}
		if(StringUtils.isBlank(userId)) {
			flash.error("用户参数");
			updateForumName(userId);
		}
		
		long userIds = Security.checkSign(userId, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
		if(userIds < 0){
			
			unUser();
		}
		Posts.saveForumName(forumName,userIds,error);
		if(error.code < 0){
			flash.error("修改昵称失败");
		}else{
			flash.error("修改昵称成功");
		}
		myCommunity(userId);
	}
	
	/**
	 * 我的回帖
	 */
	public static void showMyAnswersPosts(String userId,int Mark){
		ErrorInfo error = new ErrorInfo();
		String currPageStr = params.get("currPage");
		
		if(StringUtils.isBlank(userId)){
			flash.error("参数错误");
			myCommunity(userId);
		}
		
		long userIds = Security.checkSign(userId, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
		if(userIds < 0){
			
			unUser();
		}
		PageBean<v_posts_questions> page = Posts.showUserQuestions(0l, 0, null, currPageStr, Constants.PAGE_POSTS_SHOW_SIZESTR, error,userIds,0l);
		
		
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		render(page,userId);
	}
	
	/**
	 * 我的消息
	 */
	public static void showMyNews(String userId,int Mark){
		ErrorInfo error = new ErrorInfo();
		String currPageStr = params.get("currPage");
		
		if(StringUtils.isBlank(userId)){
			flash.error("参数错误");
			myCommunity(userId);
		}
		
		long userIds = Security.checkSign(userId, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
		
		if(userIds < 0){
			
			unUser();
		}
		
		PageBean<v_posts_questions> page = Posts.showUserQuestions(0l, 0, null, currPageStr, Constants.PAGE_POSTS_SHOW_SIZESTR, error,0l,userIds);
		
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		render(page,userId);
	}
	
	/**
	 * 我的收藏
	 * @param userId
	 */
	public static void showMyCollection(String userId,int Mark){
		ErrorInfo error = new ErrorInfo();
		String currPage  = params.get("currPage");
		if(StringUtils.isBlank(userId)){
			flash.error("参数错误");
			myCommunity(userId);
		}
		long userIds = Security.checkSign(userId, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
		if(userIds < 0){
			
			unUser();
		}
		
		PageBean<v_forum_posts> page = Posts.queryCollectionPosts( currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error,userIds);
		
		
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		render(page,userId);
	}
	
	/**
	 * 操作收藏
	 * @param postsId
	 * @param userId
	 */
	public static void eidtCollection(String postsIdStr,String userIdStr){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		if(StringUtils.isBlank(postsIdStr)){
			json.put("error", "参数错误");
			renderJSON(json);
		}
		if(StringUtils.isBlank(userIdStr)){
			json.put("error", "参数错误");
			renderJSON(json);
		}
		
		long postsId = Long.parseLong(postsIdStr);
		
		long userIds = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.APP_VALID_TIME, error);
		if(userIds < 0){
			
			unUser();
		}
		
		
		boolean collection = Posts.queryMyCollection(userIds, postsId) > 0 ? true : false;
		String hStatus= "";
		if(collection){
			hStatus = "删除收藏成功";
		}else{
			hStatus = "收藏帖子成功";
		}
		Posts.eidtColection(postsId, userIds, collection);
		
		json.put("error", hStatus);
		renderJSON(json);
	}
	
	/**
	 * 还款公告
	 */
	public static void repaymentNews(){
		ErrorInfo error = new ErrorInfo();
		String currPage  = params.get("currPage");
		PageBean<v_repayment_news> page = Posts.queryRepaymentNews(currPage, "3", error);
		render(page);
	}
	
	/**
	 * 亿亿看板
	 */
	public static void youFind(String userIdStr,int Mark){
		ErrorInfo error = new ErrorInfo();
		String currPage  = params.get("currPage");
		String userId = userIdStr;
		PageBean<t_forum_posts> page = Posts.queryForumPostsByTypeId(Constants.APP_WAP_FORUNM_YOU_FIND, currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		
		render(page,userId);
	}
	
	/**
	 * 每日签到
	 * @param userIdStr
	 */
	public static void everyDaySign(String userIdStr,int Mark){
		ErrorInfo error = new ErrorInfo();
		String currPage  = params.get("currPage");
		String userId = userIdStr;
		PageBean<t_forum_posts> page = Posts.queryForumPostsByTypeId(Constants.APP_WAP_FORUNM_EVERY_DAY, currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		
		render(page,userId);
	}
	
	/**
	 * 求助学堂最近回复
	 * @param userIdStr
	 */
	public static void helpSchool(String userIdStr,int Mark){
		ErrorInfo error = new ErrorInfo();
		String currPage  = params.get("currPage");
		
		//查询回复
		PageBean<v_forum_posts_questions> questionPage = Posts.queryForumQuestion(Constants.APP_WAP_FORUNM_HELP_SCHOOL, currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		
		
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", questionPage);
			
			renderJSON(json);
		}
		//查询发帖
		PageBean<t_forum_posts> page = Posts.queryForumPostsByTypeId(Constants.APP_WAP_FORUNM_HELP_SCHOOL, currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		String userId = userIdStr;
		render(userId,questionPage,page);
	}
	/**
	 * 帮助学堂最近发帖
	 * @param userIdStr
	 */
	public static void helpSchoolPosts(String userIdStr,int Mark){
		ErrorInfo error = new ErrorInfo();
		String currPage  = params.get("currPage");
		
		PageBean<t_forum_posts> page = Posts.queryForumPostsByTypeId(Constants.APP_WAP_FORUNM_HELP_SCHOOL, currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		String userId = userIdStr;
		render(userId,page);
	}
	
	/**
	 * 新人报道最近回复
	 * @param userIdStr
	 */
	public static void newStudent(String userIdStr,int Mark){
		ErrorInfo error = new ErrorInfo();
		String currPage  = params.get("currPage");
		//回复查询
		PageBean<v_forum_posts_questions> questionPage = Posts.queryForumQuestion(Constants.APP_WAP_FORUNM_NEW_STUDENT, currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", questionPage);
			
			renderJSON(json);
		}
		//帖子查询
		PageBean<t_forum_posts> page = Posts.queryForumPostsByTypeId(Constants.APP_WAP_FORUNM_NEW_STUDENT, currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		String userId = userIdStr;
		render(userId,questionPage,page);
	} 
	
	/**
	 * 新人报道最近发帖
	 * @param userIdStr
	 */
	public static void newStudentPosts(String userIdStr,int Mark){
		ErrorInfo error = new ErrorInfo();
		String currPage  = params.get("currPage");
		
		PageBean<t_forum_posts> page = Posts.queryForumPostsByTypeId(Constants.APP_WAP_FORUNM_NEW_STUDENT, currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		String userId = userIdStr;
		render(userId,page);
	}
	
	/**
	 * 社区活动最近回复
	 * @param userIdStr
	 */
	public static void forumActivity(String userIdStr,int Mark){
		ErrorInfo error = new ErrorInfo();
		String currPage  = params.get("currPage");
		
		//回复查询
		PageBean<v_forum_posts_questions> questionPage = Posts.queryForumQuestion(Constants.APP_WAP_FORUNM_FORUM_ACTIVITY, currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", questionPage);
			
			renderJSON(json);
		}
		//帖子查询
		PageBean<t_forum_posts> page = Posts.queryForumPostsByTypeId(Constants.APP_WAP_FORUNM_FORUM_ACTIVITY, currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		String userId = userIdStr;
		render(userId,questionPage,page);
	}
	
	/**
	 * 社区活动最近发帖
	 * @param userIdStr
	 */
	public static void forumActivityPosts(String userIdStr,int Mark){
		ErrorInfo error = new ErrorInfo();
		String currPage  = params.get("currPage");
		
		PageBean<t_forum_posts> page = Posts.queryForumPostsByTypeId(Constants.APP_WAP_FORUNM_FORUM_ACTIVITY, currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		String userId = userIdStr;
		render(userId,page);
	}
	
	/**
	 * 理财心得最近回复
	 * @param userIdStr
	 */
	public static void investmentIdea(String userIdStr,int Mark){
		ErrorInfo error = new ErrorInfo();
		String currPage  = params.get("currPage");
		
		//回复查询
		PageBean<v_forum_posts_questions> questionPage = Posts.queryForumQuestion(Constants.APP_WAP_FORUNM_INVESTMENT_IDEA, currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", questionPage);
			
			renderJSON(json);
		}
		//帖子查询
		PageBean<t_forum_posts> page = Posts.queryForumPostsByTypeId(Constants.APP_WAP_FORUNM_INVESTMENT_IDEA, currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		String userId = userIdStr;
		render(userId,questionPage,page);
	}
	
	/**
	 * 理财心得最近发帖
	 * @param userIdStr
	 */
	public static void investmentIdeaPosts(String userIdStr,int Mark){
		ErrorInfo error = new ErrorInfo();
		String currPage  = params.get("currPage");
		
		PageBean<t_forum_posts> page = Posts.queryForumPostsByTypeId(Constants.APP_WAP_FORUNM_INVESTMENT_IDEA, currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		String userId = userIdStr;
		render(userId,page);
	}
	
	/**
	 * 亿亿闲聊最近回复
	 * @param userIdStr
	 */
	public static void liveChat(String userIdStr,int Mark){
		ErrorInfo error = new ErrorInfo();
		String currPage  = params.get("currPage");
		//回复查询
		PageBean<v_forum_posts_questions> questionPage = Posts.queryForumQuestion(Constants.APP_WAP_FORUNM_LIVE_CHAT, currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", questionPage);
			
			renderJSON(json);
		}
		//帖子查询
		PageBean<t_forum_posts> page = Posts.queryForumPostsByTypeId(Constants.APP_WAP_FORUNM_LIVE_CHAT, currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		String userId = userIdStr;
		render(userId,questionPage,page);
	}
	
	/**
	 * 亿亿闲聊最近发帖
	 * @param userIdStr
	 */
	public static void liveChatPosts(String userIdStr,int Mark){
		ErrorInfo error = new ErrorInfo();
		String currPage  = params.get("currPage");
		
		PageBean<t_forum_posts> page = Posts.queryForumPostsByTypeId(Constants.APP_WAP_FORUNM_LIVE_CHAT, currPage, Constants.PAGE_POSTS_SHOW_SIZESTR, error);
		if(Mark == 2){
			JSONObject json = new JSONObject();
			json.put("page", page);
			
			renderJSON(json);
		}
		String userId = userIdStr;
		render(userId,page);
	}
	
	/**
	 * 显示活动中心
	 */
	public static void showActivityCenter(){
		String userId  = params.get("user_id");
		List<t_activity_center> list = t_activity_center.findAll();
		render(list,userId);
	}
	
	/**
	 * 活动中心内容
	 * @param id
	 */
	public static void activityCenterInfo(long id){
		t_activity_center t = t_activity_center.findById(id);
		String userId  = params.get("user_id");
		render(t,userId);
	}
	
	public static void clesrFlash(){
		
		flash.error("");
	}
	
	public static void testisCroll(){
		render();
	}
	
	/**
	 * 根据opt的值调用相对应的方法
	 */
	@Override
	public String delegateHandleRequest(Map<String, String> parameters,
			StringBuilder errorDescription) throws RuntimeException {
		String result = null;
		System.out.println("2===============");
//		//判断系统是否授权
//		try{
//			License.update(BackstageSet.getCurrentBackstageSet().registerCode);
//			
//			if(!(License.getDomainNameAllow() && License.getWebPagesAllow())) {
//				try {
//					result = RequestData.checkAuthorize();
//					return "此版本非正版授权，请联系晓风软件购买正版授权！";
//				} catch (IOException e) {
//					Logger.error("进行正版校验时:%s：", e.getMessage());
//				}
//			}
//		}catch (Exception e) {
//			Logger.info("进行正版校验时:%s：" + e.getMessage());
//			return "正版校验失败";
//		}
		Logger.info("params:%s", JSON.toJSONString(parameters));
		
		switch(Integer.valueOf(parameters.get("OPT"))){
		case AppConstants.APP_LOGIN:
			try {
				result = RequestData.login(parameters);
			} catch (IOException e) {
				Logger.error("用户登录时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_REGISTER:
			try {
				result = RequestData.register(parameters);
			} catch (IOException e) {
				Logger.error("注册用户时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_MOBILE_EXIST:
			try {
				result = RequestData.appMobileExist(parameters);
			} catch (IOException e) {
				Logger.error("注册用户时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_BASEINFO:
			try {
				result = RequestData.queryBaseInfo(parameters);
			} catch (IOException e) {
				Logger.error("查询基本信息时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_FIND_PWD_BY_SMS:
			try {
				result = RequestData.findPwdBySms(parameters);
			} catch (IOException e) {
				Logger.error("根据短信找回密码时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_CONFIRM_CODE:
			try {
				result = RequestData.confirmCode(parameters);
			} catch (IOException e) {
				Logger.error("确认验证码时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_COMMIT_NEW_PWD:
			try {
				result = RequestData.commitPassword(parameters);
			} catch (IOException e) {
				Logger.error("重置密码时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_SAVE_CELLPHONE:
			try {
				result = RequestData.saveCellphone(parameters);
			} catch (IOException e) {
				Logger.error("绑定手机号码时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_SERVICE_AGREEMENT:
			try {
				result = RequestData.ServiceAgreement(parameters);
			} catch (IOException e) {
				Logger.error("查询注册服务协议时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_REPAYMENT_CALCULATOR:
			try {
				result = RequestData.RepaymentCalculator(parameters);
			} catch (IOException e) {
				Logger.error("运行还款计算器时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ALL_BIDS: 
			try { 
			result = RequestData.queryAllbids(parameters); 
			} catch (Exception e) { 
			Logger.error("查询借款标列表时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_QUERY_H_BIDS: 
			try { 
				result = RequestData.queryUserbids(parameters); 
			} catch (Exception e) { 
				Logger.error("查询借款标列表时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_BID_DETAIL: 
			try { 
				result = RequestData.bidDetail(parameters); 
			} catch (Exception e) { 
				e.printStackTrace();
				Logger.error("查询标详情时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_INVEST_BID_RECORD: 
			try { 
				result = RequestData.queryBidInvestRecords(parameters); 
			} catch (Exception e) { 
				e.printStackTrace();
				Logger.error("查询借款标投标记录时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_ALL_QUESTION: 
			try { 
				result = RequestData.queryAllQuestions(parameters); 
			} catch (Exception e) { 
				e.printStackTrace();
				Logger.error("查询借款标提问以及回答列表时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_ADD_QUESTIONS: 
			try { 
				result = RequestData.addQuestion(parameters); 
			} catch (Exception e) { 
				e.printStackTrace();
				Logger.error("查询借款标提问记录时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_INVEST_DETAIL: 
			try { 
			result = RequestData.investDetail(parameters); 
			} catch (Exception e) { 
				e.printStackTrace();
				Logger.error("查询投标详情时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_INVEST: 
			try { 
				result = RequestData.invest(parameters); 
			} catch (Exception e) { 
				e.printStackTrace();
				Logger.error("投标操作时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_LOAN_PRODUCT:
			try {
				result = RequestData.loanProduct(parameters);
			} catch (IOException e) {
				e.printStackTrace();
				Logger.error("查询借款产品列表时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_PRODUCT_INFO:
			try {
				result = RequestData.productInfo(parameters);
			} catch (IOException e) {
				e.printStackTrace();
				Logger.error("查询借款标产品详情时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_PRODUCT_DETAIL:
			try {
				result = RequestData.productDetails(parameters);
			} catch (IOException e) {
				Logger.error("获取借款产品信息时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_CREATE_BID:
			try {
				result = RequestData.createBid(parameters);
			} catch (IOException e) {
				e.printStackTrace();
				Logger.error("发布借款时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_USER_STATUS:
			try {
				result = RequestData.UserStatus(parameters);
			} catch (IOException e) {
				e.printStackTrace();
				Logger.error("获取完善用户资料状态时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_SAVE_BASEINFO:
			try {
				result = RequestData.saveBaseInfo(parameters);
			} catch (IOException e) {
				e.printStackTrace();
				Logger.error("完善用户资料时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ACTIVE_EMAIL:
			try {
				result = RequestData.activeEmail(parameters);
			} catch (IOException e) {
				Logger.error("通过后台发送激活邮件时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_VIP_APPLY:
			try {
				result = RequestData.vipApply(parameters);
			} catch (IOException e) {
				Logger.error("申请vip时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_VIP_AGREEMENT:
			try {
				result = RequestData.vipAgreement(parameters);
			} catch (IOException e) {
				Logger.error("查询VIP会员服务条款时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_TWO_DIMENSIONANL_CODE:
			try {
				result = RequestData.TwoDimensionalCode(parameters);
			} catch (IOException e) {
				Logger.error("生成二维码时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_SPREAD_USER:
			try {
				result = RequestData.spreadUser(parameters);
			} catch (IOException e) {
				Logger.error("查询推广的会员列表时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ALL_DEBTS: 
			try { 
			result = RequestData.queryAllDebts(parameters); 
			} catch (Exception e) { 
			Logger.error("查询所有债权时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_DEBT_DETAIL: 
			try { 
			result = RequestData.debtDetail(parameters); 
			} catch (Exception e) { 
			Logger.error("查询债权转让标详情时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_DEBTAUCTION_RECORDS: 
			try { 
			result = RequestData.debtAuctionRecords(parameters); 
			} catch (Exception e) { 
			Logger.error("查询债权竞拍记录时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_ACTION_DEBT_DETAIL: 
			try { 
			result = RequestData.auctionDebtDetail(parameters); 
			} catch (Exception e) { 
			Logger.error("获取竞拍相关信息时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_AUCTION: 
			try { 
			result = RequestData.auction(parameters); 
			} catch (Exception e) { 
			Logger.error("债权竞拍时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_INVEST_BILLS: 
			try { 
			result = RequestData.investBills(parameters); 
			} catch (Exception e) { 
				e.printStackTrace();
			Logger.error("查询理财账单时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_BILL_DETAIL: 
			try { 
			result = RequestData.billDetail(parameters); 
			} catch (Exception e) { 
			Logger.error("查询理财账单时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_CURRENT_BILL_DETAIL: 
			try { 
			result = RequestData.currentBillDetail(parameters); 
			} catch (Exception e) { 
			Logger.error("查询本期账单明细时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_BILL_BID_DETAIL: 
			try { 
			result = RequestData.billBidDetail(parameters); 
			} catch (Exception e) { 
			Logger.error("查询账单借款标详情时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_HISTORY_REPAYMENT: 
			try { 
			result = RequestData.historicalRepayment(parameters); 
			} catch (Exception e) { 
			Logger.error("查询账单历史收款情况时：%s：", e.getMessage()); 
			} 
			break; 
		case AppConstants.APP_INVEST_RECORDS: 
			try { 
			result = RequestData.investRecords(parameters); 
			} catch (Exception e) { 
				e.printStackTrace();
			Logger.error("查询投标记录时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_LOANING_INVEST_BIDS:
			try {
			result = RequestData.queryUserAllloaningInvestBids(parameters);
			} catch (Exception e) {
			Logger.error("查询等待满标的理财标时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_RECEVING_INVEST_BIDS:
			try {
			result = RequestData.queryUserAllReceivingInvestBids(parameters);
			} catch (Exception e) {
			Logger.error("查询收款中的理财标列表时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_TRANSFER_DEBT:
			try {
			result = RequestData.transferDebt(parameters);
			} catch (Exception e) {
			Logger.error("转让债权时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_SUCCESS_DEBT:
			try {
			result = RequestData.queryUserSuccessInvestBids(parameters);
			} catch (Exception e) {
			Logger.error("查询已成功的理财标时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_DEBT_TRANSFER:
			try {
			result = RequestData.queryUserAllDebtTransfers(parameters);
			} catch (Exception e) {
			Logger.error("债权转让管理时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_DEBT_DETAILS_SUCCESS:
			try {
			result = RequestData.debtDetailsSuccess(parameters);
			} catch (Exception e) {
			Logger.error("查询债权转让成功详情页面时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_SUCCESS_DEBT_DETAILS:
			try {
			result = RequestData.debtDetailsTransfering(parameters);
			} catch (Exception e) {
			Logger.error("查询债权转让中详情页面时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_DEBT_DETAILS_NO_PASS:
			try {
			result = RequestData.debtDetailsNoPass(parameters);
			} catch (Exception e) {
			Logger.error("查询债权转让不通过详情页面时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_DEBT_TRANSFER_DETAIL:
			try {
			result = RequestData.debtTransferDetail(parameters);
			} catch (Exception e) {
			Logger.error("查询债权转让详情时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_DEBT_TRANSFER_BID_DETAIL:
			try {
			result = RequestData.debtTransferBidDetail(parameters);
			} catch (Exception e) {
			Logger.error("查询债权转让借款标详情页面时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_TRANSACT:
			try {
			result = RequestData.transact(parameters);
			} catch (Exception e) {
			Logger.error("成交债权时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ACTION_RECORDS:
			try {
			result = RequestData.queryAuctionRecords(parameters);
			} catch (Exception e) {
			Logger.error("creditorIdStr时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_RECEIVED_DEBT_TRANSFER:
			try {
			result = RequestData.queryUserAllReceivedDebtTransfers(parameters);
			} catch (Exception e) {
			Logger.error("查询用户受让债权管理列表时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_RECEIVE_DEBT_DETAIL_SUCCESS:
			try {
			result = RequestData.receiveDebtDetailSuccess(parameters);
			} catch (Exception e) {
			Logger.error("查询受让债权的详情 [竞拍成功]时：%s：", e.getMessage());
			}
		    break;
		case AppConstants.APP_RECEIVE_DEBT_DETAIL_AUCTION:
			try {
			result = RequestData.receiveDebtDetailAuction(parameters);
			} catch (Exception e) {
			Logger.error("查询受让债权的详情 [竞拍中]时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_RECEIVE_DEBT_DETAIL:
			try {
			result = RequestData.receiveDebtDetail(parameters);
			} catch (Exception e) {
			Logger.error("查询债权受让详情 [竞拍成功,竞拍中,定向转让]时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_RECEIVE_DEBT_BID_DETAIL:
			try {
			result = RequestData.receiveDebtBidDetail(parameters);
			} catch (Exception e) {
			Logger.error("查询受让的借款标详情 [竞拍成功,竞拍中,定向转让]时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_INCREASE_ACTION:
			try {
			result = RequestData.increaseAuction(parameters);
			} catch (Exception e) {
			Logger.error("加价竞拍时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ACCEPT_DEBTS:
			try {
			result = RequestData.acceptDebts(parameters);
			} catch (Exception e) {
			Logger.error("受定向转让债权时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_NOT_ACCEPT:
			try {
			result = RequestData.notAccept(parameters);
			} catch (Exception e) {
			Logger.error("拒绝接受定向债权转让时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_INVEST_STATISTICS:
			try {
			result = RequestData.investStatistics(parameters);
			} catch (Exception e) {
			Logger.error("查询理财情况统计表时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_UPDATE_ROBOTS:
			try {
			result = RequestData.saveOrUpdateRobot(parameters);
			} catch (Exception e) {
			Logger.error("设置投标机器人时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_AUTO_INVEST:
			try {
			result = RequestData.autoInvest(parameters);
			} catch (Exception e) {
			Logger.error("进入自动投标页面时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_CLOSE_ROBOT:
			try {
			result = RequestData.closeRobot(parameters);
			} catch (Exception e) {
			Logger.error("关闭投标机器人时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ATTENTION_DEBTS:
			try {
			result = RequestData.attentionDebts(parameters);
			} catch (Exception e) {
			Logger.error("收藏的债权列表时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ATTENTION_BIDS:
			try {
			result = RequestData.attentionBids(parameters);
			} catch (Exception e) {
			Logger.error("查询收藏的借款标时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ATTENTION_USERS_LSIT:
		    try {
			result = RequestData.myAttentionUser(parameters);
			} catch (Exception e) {
			Logger.error("查询用户关注用户列表时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_BLACK_LIST:
			try {
			result = RequestData.blackList(parameters);
			} catch (Exception e) {
			Logger.error("用户黑名单时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_REPORT_USERS:
			try {
			result = RequestData.reportUser(parameters);
			} catch (Exception e) {
			Logger.error("举报用户时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ADD_BLACK:
			try {
			result = RequestData.addBlack(parameters);
			} catch (Exception e) {
			Logger.error("拉黑对方时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_ATTENTION_USERS:
			try {
			result = RequestData.attentionUser(parameters);
			} catch (Exception e) {
			Logger.error("关注用户时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_COLLECT_BID:
			try {
			result = RequestData.collectBid(parameters);
			} catch (Exception e) {
			Logger.error("收藏借款标时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_COLLECT_DEBT:
			try {
			result = RequestData.collectDebt(parameters);
			} catch (Exception e) {
			Logger.error("收藏债权时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_HELP_CENTER: 
			try { 
			result = RequestData.helpCenter(parameters); 
			} catch (Exception e) { 
			Logger.error("进入帮助中心页面时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_HELP_CENTER_CONTENT: 
			try { 
			result = RequestData.helpCenterContent(parameters); 
			} catch (Exception e) { 
			Logger.error("查询帮助中心内容列表时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_HELP_CENTER_DETAIL: 
			try { 
			result = RequestData.helpCenterDetail(parameters); 
			} catch (Exception e) { 
			Logger.error("查询帮助中心列表详情时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_COMPANY_INTRODUCTION: 
			try { 
			result = RequestData.companyIntroduction(parameters); 
			} catch (Exception e) { 
			Logger.error("查询公司介绍时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_MANAGEMENT_TEAM: 
			try { 
			result = RequestData.managementTeam(parameters); 
			} catch (Exception e) { 
			Logger.error("查询管理团队时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_EXPER_ADVISOR: 
			try { 
			result = RequestData.expertAdvisor(parameters); 
			} catch (Exception e) { 
			Logger.error("查询专家顾问时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SEND_STATION: 
			try { 
			result = RequestData.sendStation(parameters); 
			} catch (Exception e) { 
			Logger.error("发送站内信时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SYSTEM_SMS: 
			try { 
			result = RequestData.systemSms(parameters); 
			} catch (Exception e) { 
			Logger.error("查询系统信息时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_DELETE_SYSTEM_SMS: 
			try { 
			result = RequestData.deleteSystemSmgs(parameters); 
			} catch (Exception e) { 
			Logger.error("删除系统信息时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_INBOX_SMGS: 
			try { 
			result = RequestData.inboxMsgs(parameters); 
			} catch (Exception e) { 
			Logger.error("查询收件箱信息时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_DELETE_INBOX_SMGS: 
			try { 
			result = RequestData.deleteInboxMsgs(parameters); 
			} catch (Exception e) { 
			Logger.error("除收件箱信息时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_MARK_MSGS_READED: 
			try { 
			result = RequestData.markMsgsReaded(parameters); 
			} catch (Exception e) { 
			Logger.error("标记为已读时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_MARK_MSGS_UNREAD: 
			try { 
			result = RequestData.markMsgsUnread(parameters); 
			} catch (Exception e) { 
			Logger.error("标记为未读时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_LOAN_BILLS: 
			try { 
			result = RequestData.queryMyLoanBills(parameters); 
			} catch (Exception e) { 
				e.printStackTrace();
			Logger.error("查询借款账单时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_LOAN_BILL_DETAILS: 
			try { 
			result = RequestData.loanBillDetails(parameters); 
			} catch (Exception e) { 
				e.printStackTrace();
			Logger.error("查询借款账单详情时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SUBMIT_REPAYMENT: 
			try { 
			result = RequestData.submitRepayment(parameters); 
			} catch (Exception e) { 
				e.printStackTrace();
			Logger.error("还款时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_AUDITING_LOAN_BIDS: 
			try { 
			result = RequestData.auditingLoanBids(parameters); 
			} catch (Exception e) { 
			Logger.error("查询审核中的借款标列表时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_AUDITING_BIDS: 
			try { 
			result = RequestData.loaningBids(parameters); 
			} catch (Exception e) { 
			Logger.error("查询等待满标的借款标列表时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_REPAYMENT_BIDS: 
			try { 
			result = RequestData.repaymentBids(parameters); 
			} catch (Exception e) { 
			Logger.error("查询还款中的借款标列表时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SUCCESS_BIDS: 
			try { 
			result = RequestData.successBids(parameters); 
			} catch (Exception e) { 
			Logger.error("查询已成功的借款标列表时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_AUDIT_MATERIALS: 
			try { 
			result = RequestData.auditMaterials(parameters); 
			} catch (Exception e) { 
			Logger.error("审核资料认证时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_AUDIT_MATERIALS_SAMEITEM: 
			try { 
			result = RequestData.auditMaterialsSameItem(parameters); 
			} catch (Exception e) { 
			Logger.error("查询审核资料认证详情时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SPREAD_USER_INCOME: 
			try { 
			result = RequestData.spreadUserIncome(parameters); 
			} catch (Exception e) { 
			Logger.error("查询我推广的收入时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_DEAL_RECORD: 
			try { 
			result = RequestData.dealRecord(parameters); 
			} catch (Exception e) { 
			Logger.error("查询交易记录时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_BANK_INFO: 
			try { 
			result = RequestData.bankInfos(parameters); 
			} catch (Exception e) { 
				e.printStackTrace();
			Logger.error("银行卡管理时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_ADD_BANK: 
			try {
				Logger.info("进入添加银行卡...");
				result = RequestData.addBankcardInfo(parameters);
			} catch (Exception e) { 
				e.printStackTrace();
			Logger.error("添加银行卡时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_EDIT_BANK: 
			try { 
			result = RequestData.editBank(parameters); 
			} catch (Exception e) { 
				e.printStackTrace();
			Logger.error("编辑银行卡时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_QUERY_ANSWERS: 
			try { 
			result = RequestData.queryAnswers(parameters); 
			} catch (Exception e) { 
			Logger.error("查询安全问题时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_VERIFYE_QUESTION: 
			try { 
			result = RequestData.verifySafeQuestion(parameters); 
			} catch (Exception e) { 
			Logger.error("校验安全问题时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SAVE_PAY_PWD: 
			try { 
			result = RequestData.savePayPassword(parameters); 
			} catch (Exception e) { 
			Logger.error("保存交易密码时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_EDIT_PAY_PWD: 
			try { 
			result = RequestData.editPayPassword(parameters); 
			} catch (Exception e) { 
			Logger.error("修改交易密码时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SAVE_PWD: 
			try { 
			result = RequestData.savePassword(parameters); 
			} catch (Exception e) { 
			Logger.error("保存登录密码时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_QUESTION_STATUS: 
			try { 
			result = RequestData.questionStatus(parameters); 
			} catch (Exception e) { 
			Logger.error("查询安全问题设置的状态时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_QUESTION_CONTENT: 
			try { 
			result = RequestData.questionContent(parameters); 
			} catch (Exception e) { 
			Logger.error("获取安全问题内容时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SAVE_SAFE_QUESTION: 
			try { 
			result = RequestData.saveSafeQuestion(parameters); 
			} catch (Exception e) { 
			Logger.error("保存安全问题时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_EMAIL_STATUS: 
			try { 
			result = RequestData.emailStatus(parameters); 
			} catch (Exception e) { 
			Logger.error("查询邮箱激活状态时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SAVE_EMAIL: 
			try { 
			result = RequestData.saveEmail(parameters); 
			} catch (Exception e) { 
			Logger.error("修改邮箱时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_PHONE_STATUS: 
			try { 
			result = RequestData.phoneStatus(parameters); 
			} catch (Exception e) { 
			Logger.error("查询安全手机详情及状态时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_MY_CREDIT: 
			try { 
			result = RequestData.myCredit(parameters); 
			} catch (Exception e) { 
			Logger.error("查询我的信用等级时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_VIEW_CREDIT_RULE: 
			try { 
			result = RequestData.viewCreditRule(parameters); 
			} catch (Exception e) { 
			Logger.error("查看信用等级规则时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_CREDIT_INTEGRAL: 
			try { 
			result = RequestData.creditintegral(parameters); 
			} catch (Exception e) { 
			Logger.error("查看信用积分规则时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_AUDIT_ITEM_SCORE: 
			try { 
			result = RequestData.auditItemScore(parameters); 
			} catch (Exception e) { 
			Logger.error("查询审核资料积分明细时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_CREDIT_DETAIL_REPATMENT: 
			try { 
			result = RequestData.creditDetailRepayment(parameters); 
			} catch (Exception e) { 
			Logger.error("查询正常还款积分明细时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_CREDIT_DETAIL_LOAN: 
			try { 
			result = RequestData.creditDetailLoan(parameters); 
			} catch (Exception e) { 
			Logger.error("查询成功借款积分明细时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_CREDIT_DETAIL_INVEST: 
			try { 
			result = RequestData.creditDetailInvest(parameters); 
			} catch (Exception e) { 
			Logger.error("查询成功投标积分明细时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_CREDIT_DETAIL_OVERDUE: 
			try { 
			result = RequestData.creditDetailOverdue(parameters); 
			} catch (Exception e) { 
			Logger.error("查询逾期扣分积分明细时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_APPLY_FOR_OVER_BORROW: 
			try { 
			result = RequestData.applyForOverBorrow(parameters); 
			} catch (Exception e) { 
			Logger.error("申请超额借款时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_OVER_BORROW_LIST: 
			try { 
			result = RequestData.overBorrowLists(parameters); 
			} catch (Exception e) { 
			Logger.error("查询申请超额借款记录列表时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_HOME: 
			try { 
			result = RequestData.home(parameters); 
			System.out.println(result);
			} catch (Exception e) { 
			Logger.error("查询首页时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SELECT_AUDIT_ITEMS_INIT: 
			try { 
			result = RequestData.selectAuditItemsInit(parameters); 
			} catch (Exception e) { 
			Logger.error("选择超额借款审核资料库时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_WEALTH_TOOLKIT_CREDIT_CALCULATOR: 
			try { 
			result = RequestData.wealthToolkitCreditCalculator(parameters); 
			} catch (Exception e) { 
			Logger.error("查询信用计算器规则时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_APR_CALCULATOR: 
			try { 
			result = RequestData.aprCalculator(parameters); 
			} catch (Exception e) { 
			Logger.error("查询利率计算器时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_RECRUITMENT: 
			try { 
			result = RequestData.recruitment(parameters); 
			} catch (Exception e) { 
			Logger.error("查询招贤纳士时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_PARTNERS: 
			try { 
			result = RequestData.partners(parameters); 
			} catch (Exception e) { 
			Logger.error("查询合作伙伴时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_VERSION: 
			try { 
			result = RequestData.appVersion(parameters); 
			} catch (Exception e) { 
			Logger.error("获取APP版本时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_SERVICE_HOTLINE: 
			try { 
			result = RequestData.serviceHotline(parameters); 
			} catch (Exception e) { 
			Logger.error("获取客服热线时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_NEWS_DETAIL: 
			try { 
			result = RequestData.newsDetail(parameters); 
			} catch (Exception e) { 
			Logger.error("查询财富资讯新闻详情时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_WEALTH_INFO_HOME: 
			try { 
			result = RequestData.wealthinfoHome(parameters); 
			} catch (Exception e) { 
			Logger.error("查询财富资讯首页时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_WEALTH_INFO_NEWS_LIST: 
			try { 
			result = RequestData.wealthinfoNewsList(parameters); 
			} catch (Exception e) { 
			Logger.error("查询财富资讯各个栏目下的新闻列表时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_RESET_SAFE_QUESTION: 
			try { 
				result = RequestData.resetSafeQuestion(parameters); 
				} catch (Exception e) { 
				Logger.error("通过邮箱重置安全问题时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_DELETE_BANK: 
			try { 
				result = RequestData.deleteBank(parameters); 
				} catch (Exception e) { 
				Logger.error("删除银行卡时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_OUTBOX_MSGS: 
			try { 
				result = RequestData.outboxMsgs(parameters); 
				} catch (Exception e) { 
				Logger.error("查询发件箱信息时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_OUTBOX_MSGS_DETAIL: 
			try { 
				result = RequestData.outboxMsgDetail(parameters); 
				} catch (Exception e) { 
				Logger.error("查询发件箱详情信息时：%s：", e.getMessage()); 
				} 
				break;	
		case AppConstants.APP_SYSTEM_MSGS_DETAIL: 
			try { 
				result = RequestData.systemMsgDetail(parameters); 
				} catch (Exception e) { 
				Logger.error("查询系统邮件详情信息时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_INBOX_MSGS_DETAIL: 
			try { 
				result = RequestData.inboxMsgDetail(parameters); 
				} catch (Exception e) { 
				Logger.error("查询收件箱消息详情时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_USER_INFO_STATUS: 
			try { 
				result = RequestData.userInfoStatus(parameters); 
				} catch (Exception e) { 
				Logger.error("查询用户邮箱，手机，安全问题，交易密码状态时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_KITNET_CALCULATOR: 
			try { 
				result = RequestData.kitNetValueCalculator(parameters); 
				} catch (Exception e) { 
				Logger.error("查询净值计算器时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_BID_QUESTIONS: 
			try { 
				result = RequestData.bidQuestions(parameters); 
				} catch (Exception e) { 
				Logger.error("针对当前用户的所有借款提问时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_BID_QUESTIONS_DETAILS: 
			try { 
				result = RequestData.bidQuestionDetail(parameters); 
				} catch (Exception e) { 
				Logger.error("查询提问详情时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_CREDIT_ITEM: 
			try { 
				result = RequestData.creditItem(parameters); 
				} catch (Exception e) { 
				Logger.error("查询审核科目积分明细时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_VIEW_OVER_BORROW: 
			try { 
				result = RequestData.viewOverBorrow(parameters); 
				} catch (Exception e) { 
				Logger.error("查看超额申请详情时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_SUBMIT_WITHDRAWAL: 
			try { 
				result = RequestData.submitWithdrawal(parameters); 
				} catch (Exception e) { 
				Logger.error("申请提现时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_WITHDRAWAL: 
			try { 
				result = RequestData.withdrawal(parameters); 
				} catch (Exception e) { 
				Logger.error("提现初始信息时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_WITHDRAWAL_RECORDS: 
			try { 
				result = RequestData.withdrawalRecords(parameters); 
				} catch (Exception e) { 
				Logger.error("查询提现记录时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_FILE: 
			try { 
				result = RequestData.uploadFile(parameters); 
				} catch (Exception e) { 
				Logger.error("上传文件时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_DELETE_OUTBOX_SMGS: 
			try { 
				result = RequestData.deleteOutboxMsgByUser(parameters); 
				} catch (Exception e) { 
				Logger.error("删除发件箱站内信时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_OFFICIAL_ACTIVITY: 
			try { 
				result = RequestData.queryOfficialActivity(parameters); 
				} catch (Exception e) { 
				Logger.error("查询官方活动时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_CANCEL_ATTENTION_USERS: 
			try { 
				result = RequestData.cancelAttentionUser(parameters); 
				} catch (Exception e) { 
				Logger.error("取消关注用户时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_VIP_FEE: 
			try { 
				result = RequestData.vipInfo(parameters); 
				} catch (Exception e) { 
				Logger.error("获取vip相关信息时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_DELETE_BLACKLIST: 
			try { 
				result = RequestData.deleteBlackList(parameters); 
				} catch (Exception e) { 
				Logger.error("获取vip相关信息时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_DELETE_ATTENTION_BID: 
			try { 
				result = RequestData.deleteAttentionBid(parameters); 
				} catch (Exception e) { 
				Logger.error("获取vip相关信息时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_DELETE_ATTENTION_DEBT: 
			try { 
				result = RequestData.deleteAttentionBebt(parameters); 
				} catch (Exception e) { 
				Logger.error("获取vip相关信息时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_PUSH_SETTINT: 
			try { 
				result = RequestData.pushSetting(parameters); 
				} catch (Exception e) { 
				Logger.error("保存推送设置时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_PUSH_QUERY: 
			try { 
				result = RequestData.queryPushSetting(parameters); 
				} catch (Exception e) { 
				Logger.error("获取推送设置时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_AUDIT_ITEMS: 
			try { 
				result = RequestData.createUserAuditItem(parameters); 
				} catch (Exception e) { 
				Logger.error("提交用户资料时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_FIRST_DEAl_DEBT: 
			try { 
				result = RequestData.firstDealDebt(parameters); 
				} catch (Exception e) { 
				Logger.error("债权用户初步成交债权时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_AUDIT_SUBMIT_UPLOADED_ITEMS: 
			try { 
				result = RequestData.submitUploadedItems(parameters); 
				} catch (Exception e) { 
				Logger.error("提交用户资料时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_CLEAR_AUDIT_ITEMS: 
			try { 
				result = RequestData.clearAuditItem(parameters); 
				} catch (Exception e) { 
				Logger.error("清空用户未付款资料时时：%s：", e.getMessage()); 
				} 
				break;
		case AppConstants.APP_START_MAP:
			try{
				result = RequestData.getStartMap(parameters);
			} catch(Exception e) {
				Logger.error("APP端启动图时%s", e.getMessage());
			}
			break;
		case AppConstants.APP_ALL_PRODUCTS:
			try {
				result = RequestData.queryAllProducts();
			} catch (Exception e) {
				Logger.error("查询借款标类型：" + e.getMessage());
			}
			break;
		case AppConstants.APP_ALL_REPAYMENT_TYPES:
			try {
				result = RequestData.queryAllRepaymentTypes();
			} catch (Exception e) {
				Logger.error("查询还款方式时：" + e.getMessage());
			}
			break;
		case AppConstants.APP_ACOUNT_CAPITAL:
			try {
				result = RequestData.queryAccountCapital(parameters);
			} catch (Exception e) {
				Logger.error("查询账户资产时："+ e.getMessage());
			}
			break;
		case AppConstants.APP_ACOUNT_INCOME:
			try {
				result = RequestData.queryAccountIncome(parameters);
			} catch (Exception e) {
				Logger.error("查询账户收益时："+ e.getMessage());
			}
			break;
		case AppConstants.APP_REGISTER_MOBILE:
			try {
				result = RequestData.registerMobile(parameters);
			} catch (Exception e) {
				Logger.error("手机账户注册时：" + e.getMessage());
			}
			break;
		case AppConstants.APP_BANK_PROVINCE_LIST:
			try {
				result = RequestData.getBankAndProvinceList();
			} catch (Exception e) {
				Logger.error("获取银行和城市列表时：", e);
			}
			break;
		case AppConstants.APP_PROTOCOL_BANK_PROVINCE_LIST:
			try {
				result = RequestData.getProtocolBankAndProvinceList();
			} catch (Exception e) {
				Logger.error("获取银行和城市列表时：", e);
			}
			break;
		case AppConstants.APP_PROVINCE_LIST:
			try {
				//result = RequestData.getProvinceList();
			} catch (Exception e) {
				Logger.error("获取省：", e);
			}
			break;
		case AppConstants.APP_CITY_LIST:
			try {
				result = RequestData.getCityeList(parameters);
			} catch (Exception e) {
				Logger.error("获取城市：" , e);
			}
			break;
		case AppConstants.APP_BANK_LIST:
			try {
				//result = RequestData.getBankList();
			} catch (Exception e) {
				Logger.error("获取银行：", e);
			}
			break;
		case AppConstants.APP_UPDATE_EMAIL:
			try {
				//result = RequestData.updateEmail(parameters);
			} catch (Exception e) {
				Logger.error("修改邮箱：", e);
			}
			break;
		case AppConstants.APP_UPDATE_MOBILE:
			try {
				result = RequestData.updateMobile(parameters);
			} catch (Exception e) {
				Logger.error("修改手机 ：", e);
			}
			break;
		case AppConstants.APP_PLATFORM_INFO:
			try {
				result = RequestData.getPlatformInfo(parameters);
			} catch (Exception e) {
				Logger.error("获取平台信息 ：", e);
			}
			break;

		case AppConstants.APP_POSTS_SEARCH:
			try {
				result = RequestData.postsEarch(parameters);
			} catch (Exception e) {
				Logger.error("论坛搜索 ：", e);
			}
			break;
		case AppConstants.APP_POSTS:
			try {
				result = RequestData.appPosts(parameters);
			} catch (Exception e) {
				Logger.error("论坛发帖 ：", e);
			}
			break;
		case AppConstants.APP_POSTS_FIRST:
			try {
				result = RequestData.postsFirst(parameters);
			} catch (Exception e) {
				Logger.error("论坛首页 ：", e);
			}
			break;
		case AppConstants.APP_POSTS_UPDATE_NAME:
			try {
				result = RequestData.appUpdateName(parameters);
			} catch (Exception e) {
				Logger.error("论坛修改昵称 ：", e);
			}
			break;
		case AppConstants.APP_POSTS_QUERY_NAME:
			try {
				result = RequestData.queryForumName(parameters);
			} catch (Exception e) {
				Logger.error("论坛查询昵称 ：", e);
			}
			break;
		case AppConstants.APP_USER_POSTS:
			try {
				result = RequestData.queryUserPosts(parameters);
			} catch (Exception e) {
				Logger.error("我的帖子 ：", e);
			}
			break;
		case AppConstants.APP_USER_COLLECTION:
			try {
				result = RequestData.addUserPostsCollection(parameters);
			} catch (Exception e) {
				Logger.error("用户收藏帖子 ：", e);
			}
			break;
		case AppConstants.APP_USER_COLLECTION_LIST:
			try {
				result = RequestData.queryUserPostsCollection(parameters);
			} catch (Exception e) {
				Logger.error("用户收藏帖子列表 ：", e);
			}
			break;
		case AppConstants.APP_POSTS_INFO:
			try {
				result = RequestData.appShowPosts(parameters);
			} catch (Exception e) {
				Logger.error("根据id显示帖子内容 ：", e);
			}
			break;
		case AppConstants.APP_POSTS_ANSWERS:
			try {
				result = RequestData.appPostsAnswers(parameters);
			} catch (Exception e) {
				Logger.error("用户回帖 ：", e);
			}
			break;
		case AppConstants.APP_REDPACKAGE_MYSELF:
			try {
				result = RequestData.getRedPackageList(parameters);
			} catch (Exception e) {
				Logger.error("获取红包信息 ：", e);
			}
			break;
		case AppConstants.APP_RECHARGE:
			try {
				result = RequestData.recharge(parameters);
			} catch (Exception e) {
				Logger.error("充值", e);
			}
			break;
		case AppConstants.APP_SHOW_RECHARGE:
			try {
				result = RequestData.showRecharge(parameters);
			} catch (Exception e) {
				Logger.error("显示充值页面", e);
			}
			break;
		case AppConstants.RECHARGE_INFO:
			try { 
				result = RequestData.rechargeInfo(parameters);
			} catch (Exception e) {
				Logger.error("充值前：%s", e.getMessage());
			}
			break;
		case AppConstants.APP_BID_USER_INFO:
			try {
				result = RequestData.appBidUserInfo(parameters);
			} catch (Exception e) {
				Logger.error("标的借款人信息", e);
			}
			break;
		case AppConstants.APP_USER_RED_INFO:
			try {
				result = RequestData.getRedList(parameters);
			} catch (Exception e) {
				Logger.error("用户红包信息", e);
			}
			break;
		case AppConstants.APP_REPAYMENT_CALCULATE:
			try {
				result = RequestData.appRepaymentCalculate(parameters);
			} catch (Exception e) {
				Logger.error("app还款计算", e);
			}
			break;
		case AppConstants.APP_USER_HOME:
			try {
				result = RequestData.appUserHome(parameters);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("app用户中心", e);
			}
			break;
		case AppConstants.APP_USER_EXCHANGE_INFO:
			try {
				result = RequestData.appExchangeInfo(parameters);
			} catch (Exception e) {
				Logger.error("app用户兑换信息", e);
			}
			break;
		case AppConstants.APP_USER_CPS:
			try {
				result = RequestData.getUserCpsInfo(parameters);
			} catch (Exception e) {
				Logger.error("cps推广信息", e);
			}
			break;
		case AppConstants.APP_CPS_REWARD:
			try {
				result = RequestData.queryCpsReward(parameters);
			} catch (Exception e) {
				Logger.error("cps奖励信息查询", e);
			}
			break;
		case AppConstants.APP_USER_SAVE_ADDRESS:
			try {
				result = RequestData.appUserSaveAddress(parameters);
			} catch (Exception e) {
				Logger.error("增加用户收货地址", e);
			}
			break;
		case AppConstants.GET_MALL_PROVINCE_LIST:
			try {
				result = RequestData.getMallProvinceList(parameters);
			} catch (Exception e) {
				Logger.error("app查询积分商城省份地址", e);
			}
			break;
		case AppConstants.QUERY_USER_ADDRESS:
			try {
				result = RequestData.queryUseraddress(parameters);
			} catch (Exception e) {
				Logger.error("app查询用户收货地址", e);
			}
			break;
		case AppConstants.DELETE_USER_ADDRESS:
			try {
				result = RequestData.deleteUserAddress(parameters);
			} catch (Exception e) {
				Logger.error("app删除用户收货地址", e);
			}
			break;
		case AppConstants.USER_READ_ALL:
			try {
				result = RequestData.userReadAll(parameters);
			} catch (Exception e) {
				Logger.error("app用户标记全读", e);
			}
			break;
		case AppConstants.APP_USER_SEND_MSG:
			try {
				result = RequestData.appUserSendMsg(parameters);
			} catch (Exception e) {
				Logger.error("app用户反馈信息", e);
			}
			break;
		case AppConstants.APP_INVEST_BILL_DETAILS:
			try {
				result = RequestData.appInvestBillDetails(parameters);
			} catch (Exception e) {
				Logger.error("app用户理财账单详情", e);
			}
			break;
		case AppConstants.APP_USER_ACCOUNT_HOME:
			try {
				result = RequestData.appUserAccountHome(parameters);
			} catch (Exception e) {
				Logger.error("app用户账户中心银行卡个数查询", e);
			}
			break;
		case AppConstants.APP_SIGN_SCORE:
			try {
				result = RequestData.appUserSignScore(parameters);
			} catch (Exception e) {
				Logger.error("app用户签到", e);
			}
			break;
		case AppConstants.RED_PACKAGE_RULE:
			try {
				result = RequestData.getRedPackageRule(parameters);
			} catch (Exception e) {
				Logger.error("红包使用规则", e);
			}
			break;
		case AppConstants.USER_REALITY_NAME:
			try {
				result = RequestData.userRealityName(parameters);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("充值时实名认证", e);
			}
			break;
		case AppConstants.ADD_USER_REALITY_NAME:
			try {
				result = RequestData.addUserRealityName(parameters);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("充值时实名认证", e);
			}
			break;
		case AppConstants.APP_USER_CPS_PROFIT_OLD:
			try {//老版app使用 18年4月份后新app不使用
				result = RequestData.findUserCPSProfitOld(parameters);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("cps分成记录", e);
			}
			break;
		case AppConstants.APP_USER_CPS_PROFIT:
			try {
				result = RequestData.findUserCPSProfit(parameters);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("cps分成记录", e);
			}
			break;
		case AppConstants.APP_USER_BANK_INSUR_DETAIL:
			try {
				result = RequestData.findUserBankInusrDetail(parameters);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("银行卡投保详情", e);
			}
			break;
		case AppConstants.APP_PUBLIC_BENEFIT_HOME:
			try {
				result = RequestData.findPublicBenefit(parameters);
			} catch (Exception e) {
				Logger.error("亿亿公益首页", e);
			}
			break;
		case AppConstants.APP_PUBLIC_BENEFIT_LIST:
			try {
				result = RequestData.findPublicBenefitList(parameters);
			} catch (Exception e) {
				Logger.error("亿亿公益活动列表", e);
			}
			break;
		case AppConstants.APP_PUBLIC_BENEFIT_DETAIL:
			try {
				result = RequestData.findPublicBenefitDetail(parameters);
			} catch (Exception e) {
				Logger.error("亿亿公益活动详情", e);
			}
			break;
		case AppConstants.APP_PUBLIC_BENEFIT_RULE:
			try {
				result = RequestData.findPublicBenefitRule(parameters);
			} catch (Exception e) {
				Logger.error("亿亿公益活动规则", e);
			}
			break;
		case AppConstants.APP_USER_INVEST_LIST:
			try {
				result = RequestData.findUserInvestList(parameters);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("理财账单", e);
			}
			break;
		case AppConstants.APP_USER_INVEST_RETURNED:
			try {
				result = RequestData.findUserInvestReturn(parameters);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("回款计划", e);
			}
			break;
		case AppConstants.APP_USER_INVEST_CERT:
			try {
				result = RequestData.certUrl(parameters);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("电子存证", e);
			}
			break;
			
		case AppConstants.APP_USER_SCORE:
			try {
				result = RequestData.findUserScoreReturn(parameters);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("我的积分", e);
			}
			break;
		case AppConstants.APP_USER_SCORE_RECORD:
			try {
				result = RequestData.findUserScoreRecord(parameters);
			} catch (Exception e) {
				Logger.error("积分获取记录", e);
			}
			break;
		case AppConstants.APP_USER_SIGN:
			try {
				result = RequestData.appUserSign(parameters);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("app用户签到", e);
			}
			break;
		case AppConstants.APP_DUIBA_URL:
			try {
				result = RequestData.duibaUrl(parameters);
			} catch (Exception e) {
				Logger.error("获取兑吧URL时", e);
			}
			break;
		case AppConstants.APP_RISK_RESULT:
			try {
				result = RequestData.saveRiskResult(parameters);
			} catch (Exception e) {
				Logger.error("保存风险评测结果时", e);
			}
			break;
		case AppConstants.APP_PROTO_PAY_SMS:
			try {
				result = RequestData.getProtoPaySms(parameters);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("保存风险评测结果时", e);
			}
			break;
		case AppConstants.APP_PAY_LIMIT:
			try {
				result = RequestData.getBankPayLimit(parameters);
			} catch (Exception e) {
				Logger.error("银行卡限额说明", e);
			}
			break;
		case AppConstants.APP_APPLY_DEBT_TRANSFER:
			try {
				result = RequestData.getApplyDebtTransfer(parameters);
			} catch (Exception e) {
				Logger.error("申请债权转让时", e);
			}
			break;
		case AppConstants.APP_DEBT_TRANSFER_PAY:
			try {
				result = RequestData.getDebtTransferPay(parameters);
			} catch (Exception e) {
				Logger.error("债权转让支付时：", e);
			}
			break;
		case AppConstants.APP_DEBT_TRANSFER_LIST:
			try {
				result = RequestData.getDebtTransferList(parameters);
			} catch (Exception e) {
				Logger.error("债权转让账单：", e);
			}
			break;
		case AppConstants.APP_DEBT_TRANSFER_DETAIL_V1:
			try {
				result = RequestData.getDebtTransferDetail(parameters);
			} catch (Exception e) {
				Logger.error("债权转让账单详情：", e);
			}
			break;
		case AppConstants.APP_BID_DETAIL_V1:
			try {
				result = RequestData.queryUserbidsV1(parameters);
			} catch (Exception e) {
				Logger.error("查询借款标列表时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_QUERY_DEBT_DETAIL:
			try {
				result = RequestData.queryUserBidsDebtDetail(parameters);
			} catch (Exception e) {
				Logger.error("查询债权转让标的详情时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_INVEST_DEBT_DETAIL:
			try {
				result = RequestData.queryUserInvestDebtDetail(parameters);
			} catch (Exception e) {
				Logger.error("查询债权投资标的详情时：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_DEBT_REPAYMENT_CALCULATE:
			try {
				result = RequestData.appDebtRepaymentCalculate(parameters);
			} catch (Exception e) {
				Logger.error("债权投资计算器：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_DEBT_INVEST:
			try {
				result = RequestData.debtInvest(parameters);
			} catch (Exception e) {
				Logger.error("债权转让投资：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_DEBT_INVEST_DETAIL:
			try {
				result = RequestData.debtInvestDetail(parameters);
			} catch (Exception e) {
				Logger.error("债权转让投资记录：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_DEBT_INVEST_RETURNED:
			try {
				result = RequestData.findUserDebtInvestReturn(parameters);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("回款计划", e);
			}
			break;
		case AppConstants.APP_USER_INVEST_LIST_BY_CONDITION:
			try {
				result = RequestData.findUserInvestListByCondition(parameters);
			} catch (Exception e) {
				Logger.error("返回筛选投资列表", e);
			}
			break;
		case AppConstants.APP_DEBT_SERVICE_AGREEMENT:
			try {
				result = RequestData.DebtAgreement(parameters);
			} catch (IOException e) {
				Logger.error("查询用户债权服务协议时：%s：", e.getMessage());
			}
			break;
		case AppConstants.CONTENT_SERVICE:
			try {
				result = RequestData.ContentService(parameters);
			} catch (IOException e) {
				Logger.error("查询APP_INFORMATION：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_BID_USER_RISK:
			try {
				result = RequestData.bidUserRisk(parameters);
			} catch (IOException e) {
				Logger.error("查询标的用户风险评估：%s：", e.getMessage());
			}
			break;
			case AppConstants.APP_BORROW_APPLY_LIST:
				try {
					result = RequestData.getMyBorrowApplyList(parameters);
				} catch (IOException e) {
					Logger.error("获取借款申请列表：%s：", e.getMessage());
				}
				break;
		case AppConstants.APP_BORROW_APPLY:
			try {
				result = RequestData.borrowApply(parameters);
			} catch (IOException e) {
				e.printStackTrace();
				Logger.error("借款申请：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_BORROW_APPLY_SUBMIT:
			try {
				result = RequestData.borrowApplySubmit(parameters);
			} catch (IOException e) {
				e.printStackTrace();
				Logger.error("借款申请提交：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_CERTIFICATION_PERSON:
			try {
				result = RequestData.certificationPerson(parameters);
			} catch (IOException e) {
				e.printStackTrace();
				Logger.error("借款申请提交：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_CERTIFICATION_COMPANY:
			try {
				result = RequestData.certificationCompany(parameters);
			} catch (IOException e) {
				Logger.error("借款申请提交：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_AREA_NEW:
			try {
				result = RequestData.getNewArea(parameters);
			} catch (IOException e) {
				Logger.error("获取新地区编码：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_BORROW_HOME: 
			try { 
			result = RequestData.borrowHome(parameters); 
			System.out.println(result);
			} catch (Exception e) { 
				e.printStackTrace();
			Logger.error("查询借款app首页时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_BORROW_VERSION: 
			try { 
			result = RequestData.borrowAppVersion(parameters); 
			System.out.println(result);
			} catch (Exception e) { 
				e.printStackTrace();
			Logger.error("查询借款app版本时：%s：", e.getMessage()); 
			} 
			break;
		case AppConstants.APP_BORROW_LOG:
			try {
				result = BorrowApplyRequest.getBorrowAndBidLog(parameters);
			} catch (IOException e) {
				e.printStackTrace();
				Logger.error("我的借款日志：%s：", e.getMessage());
			}
			break;
		case AppConstants.USER_BASIC_INFO:
			try {
				result = RequestData.userBasicInfo(parameters);
			} catch (IOException e) {
				e.printStackTrace();
				Logger.error("用户基本信息：%s：", e.getMessage());
			}
			break;
		case AppConstants.APP_START_MAP_BORROW:
			try{
				result = RequestData.getBorrowStartMap(parameters);
			} catch(Exception e) {
				e.printStackTrace();
				Logger.error("借款APP端启动图时%s", e.getMessage());
			}
			break;
		case AppConstants.APP_IOS_AUDIT:
			try{
				result = RequestData.getIosAudit(parameters);
			} catch(Exception e) {
				e.printStackTrace();
				Logger.error("ios版本审核状态时: %s", e.getMessage());
			}
			break;
		case AppConstants.APP_YMD_BASE_INFO_ENUM:
			try{
				result = RequestData.basicInfoEnums(parameters);
			} catch(Exception e) {
				e.printStackTrace();
				Logger.error("亿美贷基础信息枚举: %s", e.getMessage());
			}
			break;
		case AppConstants.APP_YMD_USER_INFO_SUBMIT:
			try{
				result = RequestData.creditApplyUserInfoSubmit(parameters);
			} catch(Exception e) {
				e.printStackTrace();
				Logger.error("亿美贷基础信息提交: %s", e.getMessage());
			}
			break;
		case AppConstants.APP_YMD_FLOW_NODE:
			try{
				result = YMDFlowRequest.getFlowNode(parameters);
			} catch(Exception e) {
				e.printStackTrace();
				Logger.error("亿美贷APP流程节点: %s", e.getMessage());
			}
			break;
		case AppConstants.CREDIT_APPLY:
			try{
				result = RequestData.creditApply(parameters);
			} catch(Exception e) {
				e.printStackTrace();
				Logger.error("亿美贷信用申请: %s", e.getMessage());
			}
			break;
		case AppConstants.CREDIT_APPLY_SUBMIT:
			try{
				result = RequestData.submitCreditApply(parameters);
			} catch(Exception e) {
				e.printStackTrace();
				Logger.error("亿美贷信用申请下单: %s", e.getMessage());
			}
			break;
		case AppConstants.USE_CREDIT:
			try{
				result = RequestData.useCredit(parameters);
			} catch(Exception e) {
				e.printStackTrace();
				Logger.error("亿美贷额度使用: %s", e.getMessage());
			}
			break;
		case AppConstants.YMD_ORGANIZATION_LIST:
			try{
				result = RequestData.ymdOrganizationList(parameters);
			} catch(Exception e) {
				e.printStackTrace();
				Logger.error("亿美贷机构列表: %s", e.getMessage());
			}
			break;
		case AppConstants.YMD_ORG_PROJECT:
			try{
				result = RequestData.ymdOrgProject(parameters);
			} catch(Exception e) {
				e.printStackTrace();
				Logger.error("亿美贷机构项目: %s", e.getMessage());
			}
			break;
		case AppConstants.YMD_PRO_INTEREST_AND_SERVICE_RATE:
			try{
				result = RequestData.ymdProInterestAndServiceRate(parameters);
			} catch(Exception e) {
				e.printStackTrace();
				Logger.error("亿美贷机构项目利息: %s", e.getMessage());
			}
			break;
		case AppConstants.INCREASE_CREDIT_SUBMIT:
			try{
				result = RequestData.increaseCredit(parameters);
			} catch(Exception e) {
				e.printStackTrace();
				Logger.error("提额申请提交: %s", e.getMessage());
			}
			break;
		case AppConstants.APP_YMD_USER_FILE_SUBMIT:
			try{
				result = RequestData.creditApplyUserFileSubmit(parameters);
			} catch(Exception e) {
				e.printStackTrace();
				Logger.error("用户风控补充资料,文件提交: %s", e.getMessage());
			}
			break;
		case AppConstants.SIGN_PROTOCOL:
			try{
				result = RequestData.signProtocal(parameters);
			} catch(Exception e) {
				e.printStackTrace();
				Logger.error("确认签署代扣协议: %s", e.getMessage());
			}
			break;
			
		}
		System.out.println(result);
		return result;
	}
}
