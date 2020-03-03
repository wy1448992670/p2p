package payment.ips.service;

import java.util.Map;

import play.mvc.Scope.Params;

/**
 * 环迅资金托管回调业务类
 *
 * @author hys
 * @createDate  2015年8月29日 下午6:53:30
 *
 */
public class IpsPaymentCallBackService extends IpsPaymentService{

	@Override
	public Map<String,String> getRespParams(Params params){
		return params.allSimple();
	}

	
}
