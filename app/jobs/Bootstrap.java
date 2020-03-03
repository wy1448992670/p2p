package jobs;
import business.BackstageSet;
import constants.Constants;
import payment.PaymentProxy;
import play.Play;
import play.jobs.OnApplicationStart;
import utils.tsign.eSign.SignHelper;

@OnApplicationStart
public class Bootstrap extends BaseJob {
 
    public void doJob() {
//    	if(!"1".equals(IS_JOB))return;
    	
	    new BackstageSet();
	     
	    BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
	     
	    Play.configuration.setProperty("mail.smtp.host",backstageSet.emailWebsite);
	    Play.configuration.setProperty("mail.smtp.user",backstageSet.mailAccount);
	    Play.configuration.setProperty("mail.smtp.pass",backstageSet.mailPassword);
	    
	    if(Constants.IPS_ENABLE){
	    	this.initPayment();
	    }
	    
	   //电子签章、存证， 初始化项目，做全局使用，只初始化一次即可
		SignHelper.initProject();
    }
    
    /**
     * 初始化支付网关
     */
    private void initPayment(){
    	PaymentProxy.getInstance().init();
    }
 
}