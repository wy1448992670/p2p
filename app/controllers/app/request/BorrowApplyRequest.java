package controllers.app.request;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import business.LogCore;
import constants.Constants;
import models.t_bids;
import models.t_borrow_apply;
import models.t_log;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.JSONUtils;
import utils.Security;

public class BorrowApplyRequest {
	/**
	 * 返回我的借款日志 app接口方法
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public static String getBorrowAndBidLog(Map<String, String> params) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		//验证权限
		String userIdStr = params.get("userId");
		String test = params.get("test");
		if(StringUtils.isBlank(userIdStr) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			return JSONUtils.printObject(jsonMap);
		}
		long userId=0;
		if("test".equals(test)){
			userId = Long.parseLong(userIdStr);
		}else{
			userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			if(error.code < 0){
				jsonMap.put("error", "-2");
				jsonMap.put("msg", "解析用户id有误");
				return JSONUtils.printObject(jsonMap);
			}
		}
		
		//1:查t_bids.id,2:查t_borrow_apply.id
		String bid_id = params.get("bid_id");
		Long bidId=null;
		String borrow_apply_id = params.get("borrow_apply_id");
		Long borrowApplyId=null;
		
		if(StringUtils.isNotBlank(bid_id) && StringUtils.isNumeric(bid_id)){
			bidId=Long.parseLong(bid_id);
		}
		if(StringUtils.isNotBlank(borrow_apply_id) && StringUtils.isNumeric(borrow_apply_id)){
			borrowApplyId=Long.parseLong(borrow_apply_id);
		}
		if(bidId==null && borrowApplyId==null ){//  
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "查询id有误");
			return JSONUtils.printObject(jsonMap);
		}
		
		List<Map<String,String>> dataList=new ArrayList<Map<String,String>>();
		
		if(bidId!=null){
			List<t_bids> bidList=t_bids.find("id=? and user_id=? ",bidId, userId).fetch();
			if(bidList.size()!=1){
				jsonMap.put("error", "-2");
				jsonMap.put("msg", "bid_id没有查到对应数据");
				return JSONUtils.printObject(jsonMap);
			}
			t_bids bid=bidList.get(0);
			if(bid.borrow_apply_id==null){
				jsonMap.put("error", "-2");
				jsonMap.put("msg", "bid_id没有查到对应数据");
				return JSONUtils.printObject(jsonMap);
			}
			if(borrowApplyId!=null && ! borrowApplyId.equals(bid.borrow_apply_id)){
				jsonMap.put("error", "-2");
				jsonMap.put("msg", "bid_id没有查到对应数据");
				return JSONUtils.printObject(jsonMap);
			}else{
				borrowApplyId=bid.borrow_apply_id;
			}
			
			String sql=" select current_period,all_period,if(current_period=all_period,real_repayment_time,repayment_time) time,real_repayment_time,repayment_time "+
					" from ( "+
					"	select bid_id "+
					"				,count(case when t_bills.status in(-2,-3,0) then 1 else null end ) current_period "+	//已还期数
					"				,count(1) all_period "+		//总期数
					"				,MAX(case when t_bills.status in(-2,-3, 0 ) then real_repayment_time else null end) real_repayment_time "+	//最后一次实际还款时间
					"				,MIN(case when t_bills.status in(-1) then repayment_time else null end) repayment_time "+ //最近一期预还款时间
					"	from t_bills  "+
					"	where bid_id=? "+
					"	group by bid_id "+
					" )bid_bill ";
			List<Map<String, Object>> result=JPAUtil.getList(error, sql,bid.id);
			if(result.size()==1){
				Map<String, Object> map=result.get(0);
				Map<String,String> logMap=new HashMap<String,String>();
				
				logMap.put("time", DateUtil.dateToString((Date)map.get("time")));
				BigInteger current_period=(BigInteger)map.get("current_period");
				BigInteger all_period=(BigInteger)map.get("all_period");
				if(current_period.equals(all_period)){
					logMap.put("type", "normal");
					logMap.put("description_title", "已还款（"+current_period+"/"+all_period+"）");
					logMap.put("description", "您的还款已完成，感谢您对亿亿的支持祝您生活愉快。");
				}else{
					logMap.put("type", "bill");
					logMap.put("description_title", "还款中..（"+current_period+"/"+all_period+"）");
					logMap.put("description", "我们已经为您生成还款账单，请在我的-我要还款中进行查看，或者直接点击去还款进行查看");
				}
				dataList.add(logMap);
			}
			
			List<t_log> logs=LogCore.getLog("t_bids.status",bid.id);
			for(int i=logs.size()-1;i>=0;i--){
				t_log log=logs.get(i);
				if(StringUtils.isBlank(log.description_title)){
					continue;
				}
				Map<String,String> logMap=new HashMap<String,String>();
				logMap.put("type", "normal");
				logMap.put("time",DateUtil.dateToString(log.time));
				logMap.put("description_title", log.description_title);
				logMap.put("description", log.description);
				dataList.add(logMap);
			}
			if(bid.status>0){
				Map<String,String> logMap=new HashMap<String,String>();
				logMap.put("type", "borrow");
				logMap.put("time",DateUtil.dateToString(bid.time));
				logMap.put("description_title", "标的募集中");
				logMap.put("description1", "您的标的正在募集中,标的信息为:");
				logMap.put("bid_title", bid.title);
				logMap.put("description2", ",请登录亿亿理财进行查看");
				logMap.put("bid_amount", bid.amount+"");
				logMap.put("bid_has_invested_amount", bid.has_invested_amount+"");
				dataList.add(logMap);
			}

		}
		
		List<t_borrow_apply> borrowApplyList=t_borrow_apply.find("id=? and user_id=? ",borrowApplyId, userId).fetch();
		if(borrowApplyList.size()!=1){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "没有查到对应数据");
			return JSONUtils.printObject(jsonMap);
		}
		
		List<t_log> logs=LogCore.getLog("t_borrow_apply.status",borrowApplyId);
		for(int i=logs.size()-1;i>=0;i--){
			t_log log=logs.get(i);
			if(StringUtils.isBlank(log.description_title)){
				continue;
			}
			Map<String,String> logMap=new HashMap<String,String>();
			logMap.put("type", "normal");
			if(log.status==7){
				logMap.put("type", "refuse");
			}
			logMap.put("time", DateUtil.dateToString(log.time));
			logMap.put("description_title", log.description_title);
			logMap.put("description", log.description);
			dataList.add(logMap);
		}
		sortLog(dataList);
		jsonMap.put("data", dataList);
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功！");
		return JSONUtils.printObject(jsonMap);
	}
	
	static private void sortLog(List<Map<String,String>> dataList){
		//匿名类
        Collections.sort(dataList, new Comparator<Map<String,String>>() {
            @Override
            public int compare(Map<String,String> h1, Map<String,String> h2) {
            	SimpleDateFormat sdf =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            	Date date1 = null;
            	Date date2 = null;
				try {
					date1 = sdf.parse(h1.get("time"));
					date2 = sdf.parse(h2.get("time"));
				} catch (ParseException e) {
					e.printStackTrace();
					return 0;
				}
                return (int)(date2.getTime()-date1.getTime());//倒序,日期大的排前面
            }
        });
	}
}
