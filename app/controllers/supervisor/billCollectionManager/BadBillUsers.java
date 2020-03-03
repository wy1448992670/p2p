package controllers.supervisor.billCollectionManager;

import constants.Constants;
import controllers.supervisor.SupervisorController;
import business.Bid;
import business.Supervisor;
import business.User;
import models.t_bids;
import models.v_user_loan_info_bad;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;

/**
 * 
 * 类名:BadBillUsers
 * 功能:
 */

public class BadBillUsers extends SupervisorController {

	//坏账会员管理
	public static void BadBillUserManager(){
		
		ErrorInfo error = new ErrorInfo();
		
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		String type = params.get("type");
		String startDate = params.get("startDate");
		String endDate = params.get("endDate");
		String orderType = params.get("orderType");
		String name = params.get("keywords");
		
		Supervisor supervisor = Supervisor.currSupervisor();
		
		PageBean<v_user_loan_info_bad> page = new PageBean<v_user_loan_info_bad>();
		page =	User.queryUserInfoBad(supervisor.id,type, startDate, endDate, name, orderType, currPageStr, pageSizeStr, error);
		
		render(page);
	}

	//借款标详情
	public static void bidDetail(long bidId){
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.id = bidId;
		
		render(bid);
	}
	
	//用户所有的借款标目录
	public static void userBadBidDetail(String sign,int type,String keywords,String status,int pageNum,int pageSize){
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			renderJSON(error);
		}
		
		Supervisor supervisor = Supervisor.currSupervisor();
		PageBean<t_bids> page = Bid.queryUserInfoBillDetail( pageNum, pageSize,userId, type, supervisor.id, keywords, status, error);
		renderArgs.put("sign", sign);
		renderArgs.put("type", type);
		
		render(page);
	}
	
	public static void billCollectionManagerLeft(){
		
		
		render();
	}
}
