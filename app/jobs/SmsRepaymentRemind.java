package jobs;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

import business.TemplateSms;
import constants.Templets;
import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Every;
import play.jobs.On;
import utils.DataUtil;
import utils.ymsms.SMS;

/**
 * 还款提醒短信发送
 * @author sxy
 *
 */
@On("0 0 9 * * ?")
//@Every("2min")
public class SmsRepaymentRemind extends BaseJob {
	
	public void doJob() {
		
		if(!"1".equals(IS_JOB))return;
		
		String notOverdueSql = " select bill.id, bill.bid_id, bill.status, bill.repayment_time, "
				+ " bill.repayment_corpus + bill.repayment_interest + bill.service_amount as amount, `user`.mobile "
				+ " from t_bills bill "
				+ " inner join t_bids bid on bill.bid_id = bid.id "
				+ " inner join t_users user on `user`.id = bid.user_id "
				+ " where bid.id in( select id from t_bids where tag='亿美贷') "
				+ " and bill.`status` = -1 "
				+ " and bill.repayment_time>=date_sub(date_format(curdate(),'%Y-%m-%d %H:%i:%s'),interval -6 day) and bill.repayment_time<date_sub(date_format(curdate(),'%Y-%m-%d %H:%i:%s'),interval -7 day) ";
		
		String OverdueSql = "select bill.id, bill.bid_id, bill.status, bill.repayment_time, "
				+ " bill.repayment_corpus + bill.repayment_interest + bill.service_amount as amount, `user`.mobile "
				+ " from t_bills bill "
				+ " inner join t_bids bid on bill.bid_id = bid.id "
				+ " inner join t_users user on `user`.id = bid.user_id "
				+ " where bid.id in( select id from t_bids where tag='亿美贷') "
				+ " and bill.`status` = -1 "
				+ " and bill.repayment_time>=date_sub(date_format(curdate(),'%Y-%m-%d %H:%i:%s'),interval 1 day) and bill.repayment_time<date_format(curdate(),'%Y-%m-%d %H:%i:%s') ";
		
		String content = null;
		
		Logger.info("< ================== 伊美贷还款提醒短信发送开始 ================== >");
		
		List<Map<String, Object>> notOverdueList = null;
		List<Map<String, Object>> overdueList = null;
		
		Query query1 = JPA.em().createNativeQuery(notOverdueSql);
		query1.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		notOverdueList = query1.getResultList();
		//System.out.println(notOverdueList);
		Query query2 = JPA.em().createNativeQuery(OverdueSql);
		query2.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		overdueList = query2.getResultList();
		
		try {
			
			if(notOverdueList.size()!=0) {
				for(Map<String, Object> remindBill : notOverdueList) {
					SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日");
					Calendar calendar = new GregorianCalendar();
					calendar.setTime((Date) remindBill.get("repayment_time"));
					calendar.add(calendar.DATE, -3);
					
					String remindDate = sdf.format(calendar.getTime());
					
					//(亿美贷还款提醒)您本期账单待还amount元，请在remindDate前进行还款，以免产生逾期。客服电话：021-6438-0510
					TemplateSms sms = new TemplateSms();
					sms.setId(Templets.S_NOT_OVERDUE_REMIND);
					if(sms.status) {
						content = sms.content;
						content = content.replace("amount", DataUtil.formatString(remindBill.get("amount")));
						content = content.replace("remindDate", remindDate);
						TemplateSms.addSmsTask((String)remindBill.get("mobile"), content);
					}
				}
			}
			
			if(overdueList.size()!=0) {
				for(Map<String, Object> remindBill : overdueList) {
					
					//(亿美贷还款提醒)您本期账单待还amount元，已产生逾期，请尽快结清本期账单。客服电话：021-6438-0510
					TemplateSms sms = new TemplateSms();
					sms.setId(Templets.S_OVERDUE_REMIND);
					if(sms.status) {
						content = sms.content;
						content = content.replace("amount", DataUtil.formatString(remindBill.get("amount")));
						TemplateSms.addSmsTask((String)remindBill.get("mobile"), content);
					}
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.error("伊美贷还款提醒短信发送时错误", e.getMessage());
		}
		
	}
	
}
