package controllers.supervisor.managementHome;

import java.util.HashMap;
import java.util.Map;

import payment.PaymentProxy;
import utils.ECharts;
import utils.ErrorInfo;
import business.BackstageSet;
import business.Bid;
import business.Bill;
import business.Debt;
import business.EChartsData;
import business.StationLetter;
import business.User;
import constants.Constants;
import controllers.supervisor.SupervisorController;

/**
 * 管理首页
 * @author zhs
 *
 */
public class HomeAction  extends SupervisorController {

	/**
	 * 管理首页
	 * @version 8.0.2
	 * @author yaoyi
	 */
	public static void showHome(){
		ErrorInfo error = new ErrorInfo();
		
		//查询会员注册总数（页面的累计注册会员） 取值： jobs.IndexStatisticsJob.regCount
		
		//查询成功借款标数量，成功借款总额，待还总额    取值：jobs.IndexStatisticsJob
	
		//平台浮存金        取值：jobs.IndexStatisticsJob.balanceFloatSum
		
		long t1 = System.currentTimeMillis();
		//待审核借款标个数  
		int waitAuditingBidCount = Bid.queryWaitAuditingBidCount(error);
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long t2 = System.currentTimeMillis();
		System.out.println("待审核借款标个数:"+(t2-t1));
		//将要到期标个数
		int expireBorrowingBidCount = Bid.queryExpireBorrowingBidCount(error);
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long t3 = System.currentTimeMillis();
		System.out.println("将要到期标个数:"+(t3-t2));
		//满标借款标个数        
		long fullBidCount = Bid.queryFullBidCount(error);
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long t4 = System.currentTimeMillis();
		System.out.println("满标借款标个数:"+(t4-t3));
		//转让申请个数
		long transferBidCount = Debt.querytransferBidCount(error);
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long t5 = System.currentTimeMillis();
		System.out.println("转让申请个数:"+(t5-t4));
        //-----财务-----
		//待放款借款标   
        long waitLendingBidCount = Bid.queryWaitLendingBidCount(error);
        if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
        
        long t6 = System.currentTimeMillis();
        System.out.println("待放款借款标 :"+(t6-t5));
		//待审核提现个数     status in 0 
        long waitWithdrawCount = User.queryWaitWithdrawCount(error, Constants.WITHDRAWAL_CHECK_PENDING);
        if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
        
        long t7 = System.currentTimeMillis();
        System.out.println("待审核提现个数:"+(t7-t6));
		//待付款提现个数    status in 1 
		long waitPayWithdrawCount = User.queryWaitWithdrawCount(error, Constants.WITHDRAWAL_PAYING);
	    if(error.code < 0){
	    	render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
	    }
        
	    long t8 = System.currentTimeMillis();
	    System.out.println("待付款提现个数:"+(t8-t7));
	    //到期还款金额   
	    double nextMonthRepaymentSum = Bill.queryNextMonthRepaymentSum(error);
	    if(error.code < 0){
	    	render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
	    }
	    
	    long t8_ = System.currentTimeMillis();
	    System.out.println("到期还款金额:"+(t8_-t8));
		//商户余额 资金托管用
	    double guaranteeAccountSum = 0.0;
	    if(Constants.IPS_ENABLE){
		    Map<String,Object> maps = PaymentProxy.getInstance().queryAmountByMerchant(error, Constants.PC);
		    guaranteeAccountSum = maps==null?0:Double.valueOf(maps.get("AvlBal")+"");
	    }
	    
	    long t9 = System.currentTimeMillis();
	    System.out.println("商户余额 资金托管用:"+(t9-t8_));
	    //-----运维-----
		//待回复站内信
		//int waitReplyMessageCount = StationLetter.queryWaitReplyMessageCount(error);
		//int waitReplyMessageCount = StationLetter.queryWaitReplyMessageCount(error);
		int waitReplyMessageCount = 0;
		if(error.code < 0){
		   	render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long t10 = System.currentTimeMillis();
		System.out.println("待回复站内信:"+(t10-t9));
	    //被举报会员
		int beReportedMemberCount = User.queryBeReportedMemberCount(error);
		if(error.code < 0){
		   	render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long t11 = System.currentTimeMillis();
		System.out.println("被举报会员:"+(t11-t10));
	    //已锁定会员
		int beLockMemberCount = User.queryBeLockMemberCount(error);
		if(error.code < 0){
		   	render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		long t12 = System.currentTimeMillis();
		System.out.println("已锁定会员:"+(t12-t11));
	    //黑名单会员
		int blacklistMemberCount = User.queryBlacklistMemberCount(error);
		if(error.code < 0){
		   	render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		Map<String, Object>result = new HashMap<String, Object>();
		result.put("waitAuditingBidCount", waitAuditingBidCount);
		result.put("expireBorrowingBidCount", expireBorrowingBidCount);
		result.put("fullBidCount", fullBidCount);
		result.put("transferBidCount", transferBidCount);
		result.put("waitLendingBidCount", waitLendingBidCount);
		result.put("waitWithdrawCount", waitWithdrawCount);
		result.put("waitPayWithdrawCount", waitPayWithdrawCount);
		result.put("nextMonthRepaymentSum", nextMonthRepaymentSum);
		result.put("guaranteeAccountSum", guaranteeAccountSum);
		result.put("waitReplyMessageCount", waitReplyMessageCount);
		result.put("beReportedMemberCount", beReportedMemberCount);
		result.put("beLockMemberCount", beLockMemberCount);
		result.put("blacklitMemberCount", blacklistMemberCount);
		
		render(result);
	}
	/**
	 * 保存首页配置信息
	 * @param display
	 */
	public static void saveIndexSetting(boolean display){
		ErrorInfo error = new ErrorInfo();
		BackstageSet bs = BackstageSet.getCurrentBackstageSet();
		bs.saveIndexSetting(display,error);
		renderJSON(error);
	}
	
	/*
	 * Echarts数据加载
	 */
	public static void showEchartsData(int type,String position){
		ErrorInfo error = new ErrorInfo();
		//根据type获取不同Echarts数据
		ECharts chartBean = new ECharts();
		if ("left".equals(position)) {
			chartBean = EChartsData.getMembersCount(error,type);
		}else if("right".equals(position)){
			chartBean = EChartsData.getMoneyNumber(error,type);
		}
		renderJSON(chartBean);
	}
}
