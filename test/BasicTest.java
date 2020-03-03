import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import models.v_user_for_details;

import org.apache.poi.hssf.util.HSSFColor.DARK_TEAL;
import org.junit.Test;

import play.Logger;
import play.db.jpa.JPA;
import play.test.UnitTest;
import reports.StatisticInvitation;
import utils.ErrorInfo;
import business.BackstageSet;
import business.Bid;
import business.Bill;
import business.DataSafety;
import business.DealDetail;
import business.EChartsData;
import business.Invest;
import business.StationLetter;
import business.User;
import business.Wealthcircle;

public class BasicTest extends UnitTest {
	
	public static void main(String[] args) {
		BackstageSet backstageSet = (BackstageSet)null;
		
		System.out.println(backstageSet);
	}
	
	@Test
	public void ttt(){
		Bill bill = new Bill();
		bill.systemMakeOverdue(new ErrorInfo());
	}
	
	@Test
	public void aaa(){
		ErrorInfo error = new ErrorInfo();
		System.out.println("======"+StationLetter.queryWaitReplyMessageCount(error));
	}
	
	@Test
	public void testInviteIncome(){
		
		
		Wealthcircle.addInviteIncome(9);
		
	}
	
	@Test
	public void testqueryPeriod(){
		Bid.queryPeriodByBidId(1);
	}
	
	@Test
	public void testPayforInvataion(){
		
		
		User.payForInvitation();
	}
	
	@Test
	public void testCheskSign(){
		
		DataSafety da = new DataSafety();
		da.id = 14;
		da.signCheck(new ErrorInfo());
	}
	
	@Test
	public void testupSign(){
		
		DataSafety da = new DataSafety();
		da.updateSignWithLock(14, new ErrorInfo());
	}
	
	@Test
	public void testStatisInvitation(){
		Calendar cal=Calendar.getInstance();//使用日历类  
	    
		int year = cal.get(Calendar.YEAR);// 得到年
		int month = cal.get(Calendar.MONTH) + 1;// 得到月，因为从0开始的，所以要加1
		
		StatisticInvitation.saveOrUpdateRecord(year, month);
		StatisticInvitation.saveOrUpdateDetailRecord(year, month);
	}
	
	@Test
	public void testStatis(){
		String sql = "SELECT DISTINCT(user_id), user_name FROM t_wealthcircle_invite";
		
		List<Object[]> list = null;
		
		try {
			list = JPA.em().createNativeQuery(sql).getResultList();
		} catch (Exception e) {
			Logger.error("查询所有拥有邀请码的会员时，%s", e.getMessage());
			
			return ;
		}
		
		for(Object[] o : list){
			System.out.println(Long.parseLong(o[0].toString()));
			System.out.println(o[1].toString());

		}
		
		System.out.println(list);
	}
	
	@Test
	public void testThread(){
		int id = 5;
		for (int i = 0; i < 100; i++) {
			final int ii = i;
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					User.addLock(1);
					
					User.deleteLock(1);
				}
			}).start();;
		}
	}

	private static void  oneUser(long id){
		ErrorInfo error = new ErrorInfo();

		User user = new User();
		user.id = id;
		
		DataSafety ds = new DataSafety();
		ds.id = user.id;
		ds.signCheck(error);

		if(error.code < 0){
			Logger.info("[userId = %s]验签失败，[code = %s]", user.id, error.code);
			
			throw  new RuntimeException("验签失败");
		}
		
		DealDetail.addUserFund(user.id, 1);
		
		v_user_for_details forDetail = DealDetail.queryUserBalance(user.id, error);
		if (error.code < 0) {
			return;
		}
		DealDetail dd = new DealDetail(user.id, 100, 10, -1l, forDetail.user_amount, forDetail.freeze,
				forDetail.receive_amount, "并发测试");
		dd.addDealDetail(error);;
		if (error.code < 0) {
			return;
		}
		
		ds.updateSignWithLock(user.id, error);
		
		
		if(error.code < 0){
			Logger.info("[userId = %s]更新验签失败，[code = %s]", user.id, error.code);
			
			throw  new RuntimeException("更新验签失败");
		}

	}
	
}
