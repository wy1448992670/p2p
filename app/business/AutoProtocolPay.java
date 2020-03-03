package business;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.LockModeType;

import org.apache.commons.lang.StringUtils;

import com.shove.Convert;

import constants.Constants;
import constants.DealType;
import constants.Templets;
import constants.UserEvent;
import models.t_system_recharge_completed_sequences;
import models.t_user_bank_accounts;
import models.t_user_recharge_details;
import models.v_user_for_details;
import play.Logger;
import play.db.jpa.JPA;
import utils.DataUtil;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.baofoo.business.SinglePay;
import utils.baofoo.util.Log;

/**
 *  协议支付 
 * date 2019年3月26日
 * author wangyun
 */
public class AutoProtocolPay {
	/**
	 *  支付
	 * wangyun
	 * 2019年3月26日
	 * @param userId
	 * @param protocolNo
	 * @param amount
	 * @throws Exception
	 */
	public static void autoPay(Long userId, String protocolNo, Double amount) throws Exception {
		if(StringUtils.isEmpty(protocolNo)) {
			Logger.error("协议号不能为空！");
			return;
		}
		if(amount.isNaN()) {
			Logger.error("金额格式不正确！");
			return;
		}
		
		ErrorInfo error = new ErrorInfo();
		/*=============================== 生成平台订单  =========================================*/
		// 对批量迁移的用户以及后面重复绑定的卡的协议号 用户协议号需要用到有效的协议号和userid
		/**
		 * 用户分离时,一个老用户可能产生 借款人id 投资人id
		 * 协议号一样,调用宝付需要协议绑定时的user_id验证
		 *
		 * 同一银行卡号,如果协议绑两次,会有两个协议号,只有最后一次的绑定数据有用
		 * 调用宝付需要使用最后一次协议号和用户id
		 */
		List<t_user_bank_accounts> banks = t_user_bank_accounts.find(" protocol_no = ? ",protocolNo).fetch();
		if(banks == null){
			Log.Write("查找协议支付号绑定银行卡信息出错！protocolNo: " + protocolNo);
			return;
		}else {
			if (banks.size() == 1) {
				//查找同一银行卡号是否绑定过多次
				List<t_user_bank_accounts> banks_same_account = t_user_bank_accounts.find(" account = ? ", banks.get(0).account).fetch();

				if (banks_same_account != null && banks_same_account.size() > 1) {
					//取最后绑定的协议号
//					long uid=0;
					long max_bank_account_id = 0;
					for (t_user_bank_accounts account : banks_same_account){
						if(account.id > max_bank_account_id) {
							max_bank_account_id = account.id;
							//uid = account.user_id;
							protocolNo = account.protocol_no;
						}
					}
//					theUserId=uid;
				}
			} else if (banks.size() > 1) {//迁移过来的数据
				//取最小的userid,因为这个协议号是之前认证的（不传user_id 舍弃这个）
				//theUserId = Math.min(banks.get(0).user_id, banks.get(1).user_id);
				
			}
		}
		//生成支付单号 充值记录
		String payNumber = "TID"+System.currentTimeMillis();
		createRecharge(userId, banks.get(0).account, amount, payNumber, error);
		if(error.code < 0) {
			Logger.info("生成支付账单失败！【%s】,【%s】 " ,banks.get(0).account, error.msg);
			return;
		}
		
		/*===============================  请求支付通道  =============================*/
		Map<String, String> payResult = SinglePay.execute(protocolNo, new BigDecimal(amount), payNumber);
		
		
		
		/*===============================  处理支付返回信息 ===========================*/
	
		double rechargeAmount = new BigDecimal(payResult.get("succ_amt")).divide(new BigDecimal("100")).doubleValue();
		String orderNo = payResult.get("trans_id");//
//		String baofooOrderNo = payResult.get("order_id");//宝付订单号
		String message = payResult.get("biz_resp_msg").toString();
		
		//支付成功更新订单及用户余额信息
		if(payResult.get("resp_code").toString().equals("S")){
			Log.Write("支付成功！[trans_id:"+payResult.get("trans_id")+"]");
			User.recharge(payNumber, rechargeAmount, error);
			
		} else if(payResult.get("resp_code").toString().equals("I")){	
			Log.Write("处理中！");
			
		} else if(payResult.get("resp_code").toString().equals("F")){
			Log.Write("支付失败！ " + message);
			
		}/* else {
			throw new Exception(responseData.get("biz_resp_msg").toString());
		}*/
		 
		if(error.code < 0) {
			Logger.info("支付信息更新失败！【%s】", orderNo);
		}
	}
	
	
	/**
	 *  生成订单信息
	 * wangyun
	 * 2019年3月26日
	 */
	public static void createRecharge(Long userId, String bankCardNo, Double amount,String payNumber,ErrorInfo error) {
		if(StringUtils.isEmpty(bankCardNo)) {
			error.code = -1;
			error.msg = "银行卡为空！";
			return;
		}
		
		t_user_recharge_details detail;
		detail = t_user_recharge_details.find(" pay_number= ? ", payNumber).first();
		if(detail != null) {
			error.code = -1;
			error.msg = "订单已存在！";
			return;
		}
		
		if(StringUtils.isNotEmpty(bankCardNo)) {
			detail = new t_user_recharge_details();
			detail.user_id = userId;
			// detail.user_id = 1;
			detail.time = new Date();
			detail.payment_gateway_id = -4;//宝付支付
			detail.unique_key = null;
			detail.pay_number = payNumber;
			detail.amount = amount;
			detail.is_completed = false;
			detail.type = Constants.QUARTZ_RECHARGE; //定时任务充值
			detail.client = Constants.CLIENT_OTHERS;//其他
			detail.bank_card_no = bankCardNo;

			try {
				detail.save();
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("充值插入充值数据时：" + e.getMessage());
				error.code = -1;
				error.msg = "充值请求失败" + e.getMessage(); 
				return;
			}
		} 

	}
	
	
	
	
}