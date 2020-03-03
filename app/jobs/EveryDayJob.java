package jobs;

import java.util.Calendar;

import constants.Constants;
import business.Invest;
import business.Vip;
import play.jobs.Job;
import play.jobs.On;
import reports.StatisticAuditItems;
import reports.StatisticBorrow;
import reports.StatisticDebt;
import reports.StatisticInvest;
import reports.StatisticInvitation;
import reports.StatisticMember;
import reports.StatisticProduct;
import reports.StatisticRecharge;
import reports.StatisticSecurity;
import utils.ErrorInfo;
import utils.JPAUtil;


/**
 * 每天定时定点任务,每天23:50执行
 * @author lwh
 *
 */
@On("0 50 23 * * ?")
public class EveryDayJob extends BaseJob{
	
	public void doJob() {
		if(!"1".equals(IS_JOB))return;
		
		ErrorInfo error = new ErrorInfo();
		
		Calendar cal=Calendar.getInstance();//使用日历类  
		int year = cal.get(Calendar.YEAR);// 得到年
		int month = cal.get(Calendar.MONTH) + 1;// 得到月，因为从0开始的，所以要加1

      	StatisticAuditItems.executeUpdate(error);//审核科目库统计
      	
		StatisticProduct.executeUpdate(error);//借款标产品销售情况
		
		StatisticBorrow.executeUpdate(error);//借款情况统计
		
		StatisticInvest.investSituationStatistic();//理财情况统计表
		
		StatisticInvest.platformIncomeStatistic();//平台收入
		
		StatisticInvest.platformWithdrawStatistic();//系统提现
		
		StatisticInvest.platformFloatstatistics();//平台浮存金统计
		
		StatisticRecharge.executeUpdate(error);//充值统计
		
		StatisticMember.executeUpdate(error);//会员数据统计分
		
		StatisticSecurity.executeUpdate(error);//本金保障统计
		
		Vip.vipExpiredJob(); //vip过期处理
		
		Invest.creatBidPactJob();  //生成借款理财债权协议
		
		Invest.creatDebtPactJob();  //定时执行生成债权协议
		
		if(Constants.DEBT_USE) {
			StatisticDebt.debtSituationStatistics();//债权转让情况统计分析表
		}
		
		/** 财富圈报表统计 */
		JPAUtil.transactionBegin();
		StatisticInvitation.saveOrUpdateRecord(year, month);
		StatisticInvitation.saveOrUpdateDetailRecord(year, month);
		JPAUtil.transactionCommit();
		
	}
}
