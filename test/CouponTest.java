import org.junit.Test;

import business.TemplateSms;
import jobs.BirthDayJob;
import jobs.CustomRedPackMainJob;
import jobs.InvestsRedPackJob;
import play.test.UnitTest;

public class CouponTest extends UnitTest {
	
	@Test
	public void testCoupon() {
		TemplateSms sms = new TemplateSms();
		sms.id = 58;
		System.out.println(sms.content);
	}
	
	/**
	 * 发放生日优惠券
	 * Description: 
	 */
	@Test
	public void birthDayJobTest() {
		BirthDayJob birthDayJob = new BirthDayJob();
		birthDayJob.doJob();
	}
	
	/**
	 * 发放累计投资优惠券
	 * Description: 
	 */
	@Test
	public void investsRedPackJobTest() {
		
		InvestsRedPackJob job = new InvestsRedPackJob();
		job.doJob();
	}
	/**
	 * 发放自定义优惠券
	 * Description: 
	 */
	@Test
	public void customRedPackJobTest() {
		CustomRedPackMainJob job = new CustomRedPackMainJob();
		job.doJob();
		try {
			Thread.sleep(3600*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
