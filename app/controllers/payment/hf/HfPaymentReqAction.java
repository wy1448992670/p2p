package controllers.payment.hf;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import models.t_bids;
import models.t_bills;
import models.t_invests;
import models.t_users;

import org.apache.commons.lang.StringUtils;
import org.json.XML;

import payment.ips.service.IpsPaymentService;
import payment.ips.util.IpsConstants;
import payment.ips.util.IpsPaymentUtil;
import play.cache.Cache;
import utils.ErrorInfo;
import utils.JPAUtil;
import business.Bid;
import business.Bill;
import business.Debt;
import business.OverBorrow;
import business.User;
import business.Vip;

import com.google.gson.Gson;
import com.shove.Convert;

import constants.Constants;
import constants.Constants.RechargeType;
import constants.IPSConstants;
import constants.IPSConstants.CompensateType;
import constants.PayType;
import controllers.front.account.AccountHome;
import controllers.payment.PaymentBaseAction;
import controllers.supervisor.bidManager.BidPlatformAction;
import controllers.supervisor.financeManager.PayableBillManager;
import controllers.supervisor.financeManager.ReceivableBillManager;

/**
 * 请求环讯资金托管，Action
 * @author xiaoqi
 *
 */
public class HfPaymentReqAction extends PaymentBaseAction{
	
	private static HfPaymentReqAction instance = null;
	
	private HfPaymentReqAction(){
		
	}
	
	public static HfPaymentReqAction getInstance(){
		if(instance == null){
			synchronized (HfPaymentReqAction.class) {
				if(instance == null){
					instance = new HfPaymentReqAction();
				}
			}
		}
		
		return instance;
	}
	
}
