import org.junit.Assert;
import org.junit.Test;

import business.RedPackageHistory;
import constants.Constants;
import jobs.CheckRedPackageStatus;
import models.t_red_packages_history;
import play.test.UnitTest;

public class RedPackageHistoryTest extends UnitTest{

	@Test
	public void testCheckGQ() {
		t_red_packages_history history = t_red_packages_history.findById(15033L);
		RedPackageHistory.checkGQ(history);
	}
	
	@Test
	public void testUpdateStatusExpired() {
		int rows = RedPackageHistory.updateStatusExpired(53115L, Constants.RED_PACKAGE_STATUS_OVERDUE);
		boolean isSuccess = rows > 0?true:false;
		Assert.assertEquals(true, isSuccess);
	}
	
	@Test
	public void testJob() {
		CheckRedPackageStatus job = new CheckRedPackageStatus();
		job.doJob();
	}
}
