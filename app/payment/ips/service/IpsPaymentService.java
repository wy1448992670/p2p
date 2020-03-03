package payment.ips.service;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.t_bill_invests;
import models.t_bills;
import models.t_invests;
import models.t_mmm_data;
import models.t_users;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import payment.PaymentBaseService;
import payment.ips.util.IpsConstants;
import payment.ips.util.IpsPaymentUtil;
import play.Logger;
import play.mvc.Scope.Params;
import utils.ErrorInfo;
import utils.JPAUtil;
import business.Bid;
import business.Bill;
import business.BillInvests;
import business.User;

import com.google.gson.Gson;
import com.shove.Convert;

import constants.Constants;
import constants.PayType;
import controllers.BaseController;

/**
 * 环迅资金托管业务类
 *
 * @author hys
 * @createDate  2015年8月29日 下午3:13:13
 *
 */
public class IpsPaymentService extends PaymentBaseService{
	
	public static payment.ips.util.XmlUtil xmlUtil = new payment.ips.util.XmlUtil();
	
	private final Gson gson = new Gson();
	
	/**
	 * 开户
	 * @param error
	 * @param pIdentNo 身份证号
	 * @param pRealName 真实姓名
	 * @param pMobileNo 手机号
	 * @param pEmail 邮箱
	 * @return
	 */
	public LinkedHashMap<String, String> register(ErrorInfo error, String pIdentNo, String pRealName, String pMobileNo, String pEmail){
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		xmlMap.put("pMerBillNo", this.createBillNo());
		xmlMap.put("pIdentType", "1");
		xmlMap.put("pIdentNo", pIdentNo);
		xmlMap.put("pRealName", pRealName);
		xmlMap.put("pMobileNo", pMobileNo);
		xmlMap.put("pEmail", pEmail);
		xmlMap.put("pSmDate", IpsPaymentUtil.getFormatDate("yyyyMMdd"));
		xmlMap.put("pWebUrl", BaseController.getBaseURL() + "payment/ips/returnRegister");
		xmlMap.put("pS2SUrl", BaseController.getBaseURL() + "payment/ips/returnRegister");
		xmlMap.put("pMemo1", xmlMap.get("pMerBillNo"));
		xmlMap.put("pMemo2", "开户");	
		return xmlMap;	
	}
	
	/**
	 * 充值
	 * @param error
	 * @param pIdentNo 身份证号
	 * @param pRealName 真实姓名
	 * @param pIpsAcctNo 第三方唯一标识
	 * @param pTrdAmt 充值金额
	 * @param pTrdBnkCode 银行卡编码
	 * @return
	 */
	public LinkedHashMap<String, String> recharge(ErrorInfo error, String pIdentNo, String pRealName, String pIpsAcctNo, String pTrdAmt, String pTrdBnkCode){
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		String pMerBillNo = this.createBillNo();
		xmlMap.put("pMerBillNo", pMerBillNo);
		xmlMap.put("pAcctType", "1");
		xmlMap.put("pIdentNo",pIdentNo);
		xmlMap.put("pRealName", pRealName);
		xmlMap.put("pIpsAcctNo", pIpsAcctNo);
		xmlMap.put("pTrdDate", IpsPaymentUtil.getFormatDate("yyyyMMdd"));
		xmlMap.put("pTrdAmt", IpsPaymentUtil.formatAmount(pTrdAmt));
		xmlMap.put("pChannelType", "1");
		xmlMap.put("pTrdBnkCode", pTrdBnkCode);
		xmlMap.put("pMerFee", "0.00");
		xmlMap.put("pIpsFeeType", "1");
		xmlMap.put("pWebUrl", BaseController.getBaseURL()+ "payment/ips/returnRecharge");
		xmlMap.put("pS2SUrl", BaseController.getBaseURL() + "payment/ips/returnRecharge");
		xmlMap.put("pMemo1", pMerBillNo);
		xmlMap.put("pMemo2", "充值");
		xmlMap.put("pMemo3", "3");
		return xmlMap;
	}
	
	/**
	 * 标的发布
	 * @param pLendAmt 借款金额
	 * @param pGuaranteesAmt 保证金
	 * @param pOperationType 标的操作类  1:新增； 2:结束
	 * @param pLendFee 借款手续费
	 * @param pIdentNo 借款人身份证号
	 * @param pRealName 借款人正式姓名
	 * @param pIpsAcctNo 借款人第三方标识
	 * @return
	 */
	public LinkedHashMap<String, String> bidCreate(double pLendAmt, double pGuaranteesAmt, int pOperationType, double pLendFee, String pIdentNo, String pRealName, String pIpsAcctNo){
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		String pMerBillNo = this.createBillNo();//商户订单号		
		xmlMap.put("pMerBillNo", pMerBillNo);
		xmlMap.put("pBidNo", pMerBillNo);
		xmlMap.put("pRegDate", IpsPaymentUtil.getFormatDate("yyyyMMdd"));
		xmlMap.put("pLendAmt", IpsPaymentUtil.formatAmount(pLendAmt+"")); //借款金额
		xmlMap.put("pGuaranteesAmt", IpsPaymentUtil.formatAmount(pGuaranteesAmt+"")); //保证金
		xmlMap.put("pTrdLendRate", "45.00");
		xmlMap.put("pTrdCycleType", "3");
		xmlMap.put("pTrdCycleValue", "60");
		xmlMap.put("pLendPurpose ", "借款"); //借款用途
		xmlMap.put("pRepayMode", "99"); //还款方式
		xmlMap.put("pOperationType", 1+"");
		xmlMap.put("pLendFee", IpsPaymentUtil.formatAmount(pLendFee * 2 +"")); //手续费
		xmlMap.put("pAcctType", "1"); //账户类型 1:个人;
		xmlMap.put("pIdentNo", pIdentNo);
		xmlMap.put("pRealName", pRealName);
		xmlMap.put("pIpsAcctNo", pIpsAcctNo);
		xmlMap.put("pWebUrl", BaseController.getBaseURL() + "payment/ips/returnBidCreate");
		xmlMap.put("pS2SUrl", BaseController.getBaseURL() + "payment/ips/returnBidCreate");
		xmlMap.put("pMemo1", pMerBillNo);
		xmlMap.put("pMemo2", "标的发布");
		
		return xmlMap;
	}
	
	/**
	 * 自动投资接口
	 * @param pBidNo 标的号，t_bids表的bid_no字段
	 * @param pTrdAmt 投资金额
	 * @param pIdentNo 投资人身份证号
	 * @param pRealName 投资人真实姓名
	 * @param pAccount 投资人第三方唯一标识
	 * @param pAuthNo 自动投标签约号
	 * @param xmlMap 回调方法需求的参数, 与接口无关
	 * @return
	 * @throws Exception 
	 */
	public LinkedHashMap<String, String> autoInvest(String pBidNo, double pTrdAmt, String pIdentNo, String pRealName, String pAccount, String pAuthNo, LinkedHashMap<String, String> tempMap, ErrorInfo error) throws Exception{
		
		LinkedHashMap<String, String> xmlMap =  this.invest(pBidNo, pTrdAmt, pIdentNo, pRealName, pAccount, "2", pAuthNo);
		
		String xml = "";
		xml = IpsPaymentUtil.parseMapToXml(xmlMap);
		String str3DesXmlPana = xml;
		str3DesXmlPana = com.ips.security.utility.IpsCrypto.triDesEncrypt(xml, IpsConstants.des_key, IpsConstants.des_iv);
		str3DesXmlPana = str3DesXmlPana.replaceAll("\r", "");
		str3DesXmlPana = str3DesXmlPana.replaceAll("\n", "");
		String strSign = com.ips.security.utility.IpsCrypto.md5Sign(IpsConstants.terraceNoOne + str3DesXmlPana + IpsConstants.cert_md5);		
		HashMap<String, String> dataMap = new HashMap<String, String>(); 
		dataMap.put("pMerCode", IpsConstants.terraceNoOne);
		dataMap.put("p3DesXmlPara", str3DesXmlPana);
		dataMap.put("pSign", strSign);
		
		//回调需要的参数
		xmlMap.putAll(tempMap);		
		printRequestData(xmlMap, "自动投标提交参数", PayType.AUTO_INVEST);
		
		String data = IpsPaymentUtil.postMethod(IpsConstants.POST_URL + "registerCreditor.aspx", dataMap, "UTF-8");
		data = data.split("</form>")[1] + "</form>";
		org.json.JSONObject jsonObj = XML.toJSONObject(data);
		org.json.JSONObject form = (org.json.JSONObject) jsonObj.get("form");
		org.json.JSONArray inputs = (org.json.JSONArray) form.get("input");		
		LinkedHashMap<String, String> returnMap = new LinkedHashMap<String, String>();
		for(int i=0; i < inputs.length(); i++){
			org.json.JSONObject obj = (org.json.JSONObject) inputs.get(i);
			String name = obj.getString("name");
			String value = obj.getString("value");
			returnMap.put(name, value);
		}
		IpsPaymentUtil.printData(returnMap, "自动投标回调参数", PayType.AUTO_INVEST);
		
		Map<String, String> parseXml = IpsPaymentUtil.parseXmlToJson(returnMap.get("p3DesXmlPara"));
		
		return returnMap;	
		
	}
	
	/**
	 * 手动投资接口
	 * @param pBidNo 标的号，t_bids表的bid_no字段
	 * @param pTrdAmt 投资金额
	 * @param pIdentNo 投资人身份证号
	 * @param pRealName 投资人真实姓名
	 * @param pAccount 投资人第三方唯一标识
	 * @return
	 */
	public LinkedHashMap<String, String> normalInvest(String pBidNo, double pTrdAmt, String pIdentNo, String pRealName, String pAccount){
		
		return this.invest(pBidNo, pTrdAmt, pIdentNo, pRealName, pAccount, "1", "1");
		
	}
	
	/**
	 * 投资接口
	 * @param pBidNo 标的号，t_bids表的bid_no字段
	 * @param pTrdAmt 投资金额
	 * @param pIdentNo 投资人身份证号
	 * @param pRealName 投资人真实姓名
	 * @param pAccount 投资人第三方唯一标识
	 * @param pRegType 1：手劢投标 2：自动投标
	 * @param pAuthNo 自动投标签约号
	 * @return
	 */
	public LinkedHashMap<String, String> invest(String pBidNo, double pTrdAmt, String pIdentNo, String pRealName, String pAccount, String pRegType, String pAuthNo){
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		String pMerBillNo = this.createBillNo();
		xmlMap.put("pMerBillNo", pMerBillNo);//投资流水号
		xmlMap.put("pMerDate", IpsPaymentUtil.getFormatDate("yyyyMMdd"));//商户日期
		xmlMap.put("pBidNo", pBidNo); //标的号
		xmlMap.put("pContractNo", this.createBillNo()); //合同号
		xmlMap.put("pRegType", pRegType); //1：手劢投标 2：自劢投标
		xmlMap.put("pAuthNo", pAuthNo);//手动投标时:1;自动投标时:投资签约号
		xmlMap.put("pAuthAmt",IpsPaymentUtil.formatAmount(pTrdAmt+"")); //债权金额
		xmlMap.put("pTrdAmt", IpsPaymentUtil.formatAmount(pTrdAmt+"")); //交易金额
		xmlMap.put("pFee", "0.00"); //投资人手续费
		xmlMap.put("pAcctType", "1"); //0#机构（暂未开放） ；1#个人
		xmlMap.put("pIdentNo", pIdentNo); //投资人身份证号
		xmlMap.put("pRealName", pRealName); //投资人真实姓名
		xmlMap.put("pAccount", pAccount); //投资人第三方唯一标识
		xmlMap.put("pUse", "投资"); //投资用途
		xmlMap.put("pWebUrl", BaseController.getBaseURL() + "payment/ips/returnInvest"); //同步回调地址
		xmlMap.put("pS2SUrl", BaseController.getBaseURL() + "payment/ips/returnInvest"); //异步回调地址
		xmlMap.put("pMemo1", pMerBillNo); //备注1，用于存储该次动作流水号
		xmlMap.put("pMemo2", "投资"); //备注2，真正备注
		xmlMap.put("pMemo3", "投资"); //备注2，真正备注
		return xmlMap;
	}
	
	/**
	 * 流标
	 * @param dataMap 标的发布时，t_mmm_data表数据
	 * @return 
	 * @throws Exception 
	 */
	public Map<String, String> bidAuditFail(Map<String, String> map, ErrorInfo error){
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();		
		String pMerBillNo = this.createBillNo();
		xmlMap.put("pMerBillNo", map.get("pMerBillNo"));
		xmlMap.put("pBidNo", map.get("pBidNo"));
		xmlMap.put("pRegDate", IpsPaymentUtil.getFormatDate("yyyyMMdd"));
		xmlMap.put("pLendAmt", map.get("pLendAmt"));
		xmlMap.put("pGuaranteesAmt", map.get("pGuaranteesAmt"));
		xmlMap.put("pTrdLendRate", map.get("pTrdLendRate"));
		xmlMap.put("pTrdCycleType", map.get("pTrdCycleType"));
		xmlMap.put("pTrdCycleValue", map.get("pTrdCycleValue"));
		xmlMap.put("pLendPurpose ", "借款");
		xmlMap.put("pRepayMode", map.get("pRepayMode"));
		xmlMap.put("pOperationType", "2");
		xmlMap.put("pLendFee", map.get("pLendFee"));
		xmlMap.put("pAcctType", "1");
		xmlMap.put("pIdentNo", map.get("pIdentNo"));
		xmlMap.put("pRealName", map.get("pRealName"));
		xmlMap.put("pIpsAcctNo", map.get("pIpsAcctNo"));
		xmlMap.put("pWebUrl", BaseController.getBaseURL() + "payment/ips/returnBidAuditFail");
		xmlMap.put("pS2SUrl", BaseController.getBaseURL() + "payment/ips/returnBidAuditFail");
		xmlMap.put("pMemo1", pMerBillNo);
		xmlMap.put("pMemo2", "标的审核不通过");
		xmlMap.put("pMemo3", "标的审核不通过");
		
		String xml = IpsPaymentUtil.parseMapToXml(xmlMap);
		String str3DesXmlPana = xml;
		str3DesXmlPana = com.ips.security.utility.IpsCrypto.triDesEncrypt(xml, IpsConstants.des_key, IpsConstants.des_iv);
		str3DesXmlPana = str3DesXmlPana.replaceAll("\r", "");
		str3DesXmlPana = str3DesXmlPana.replaceAll("\n", "");
		String strSign = com.ips.security.utility.IpsCrypto.md5Sign(IpsConstants.terraceNoOne + str3DesXmlPana + IpsConstants.cert_md5);		
	
		HashMap<String, String> dataMap = new HashMap<String, String>(); 
		dataMap.put("pMerCode", IpsConstants.terraceNoOne);
		dataMap.put("p3DesXmlPara", str3DesXmlPana);
		dataMap.put("pSign", strSign);
		
		// 保存回调所需参数
		xmlMap.put("bidId", map.get("bidId"));
		xmlMap.put("typeStr", map.get("typeStr"));
		
		//回调需要的参数，在这插入xmlMap， 回调根据流水号，在获取
		printRequestData(xmlMap, "标的审核不通过", PayType.BID_AUDIT_FAIL);
		
		String data = IpsPaymentUtil.postMethod(IpsConstants.POST_URL + "registerSubject.aspx", dataMap, "UTF-8");
		data = data.split("</form>")[1] + "</form>";
		
		LinkedHashMap<String, String> returnMap = new LinkedHashMap<String, String>();
		try {
			JSONObject jsonObj = XML.toJSONObject(data);
			JSONObject form = (JSONObject) jsonObj.get("form");
			JSONArray inputs = (JSONArray) form.get("input");		
			for(int i=0; i < inputs.length(); i++){
				JSONObject obj = (JSONObject) inputs.get(i);
				String name = obj.getString("name");
				String value = obj.getString("value");
				returnMap.put(name, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "流标时，回调参数解析异常";
			
			return null;
		}
		
		IpsPaymentUtil.printData(returnMap, "标的审核不通过", PayType.BID_AUDIT_FAIL);	
		
		Map<String, String> parseXml = null;
		try {
			parseXml = IpsPaymentUtil.parseXmlToJson(returnMap.get("p3DesXmlPara"));
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -2;
			error.msg = "流标时，回调参数解析异常";
			
			return null;
		}
		
		IpsPaymentUtil.checkSign(returnMap, "标的审核不通过", parseXml, PayType.BID_AUDIT_FAIL.name(), error);
		
		return parseXml;
		
	}
	/**
	 * 满标复审
	 * @param pDetailsList 转账明细[{'pOriMerBillNo':'原商户订单号', 'pTrdAmt':'转账金额', 'pFAcctType':'转出方账户类型, 1:为个人' 
	 * 								'pFIpsAcctNo':'转出方 IPS 托管账户号', 'pFTrdFee':'转出方手续费明细','pTAcctType':'转入方账户类型',
	 * 								'pTIpsAcctNo':'转出方 IPS 托管账户号', 'pFTrdFee':'转出方明细手续费'}]
	 * @param pBidNo 标的号
	 * @param ipsAcctNo 借款人第三方唯一标示
	 * @param parentOrderno 父流水号，管控满标审核与满标解冻借款人保证金原子性
	 * @return
	 * @throws Exception 
	 */
	public Map<String, String> bidAuditSucc(List<Map<String, String>> pDetailsList, String pBidNo, String ipsAcctNo, String parentOrderno, long bid, ErrorInfo error) throws Exception{
		
		String pMerBillNo = this.createBillNo();
		LinkedHashMap<String, String> xmlMap = transfer(pDetailsList, pMerBillNo, pBidNo, "1", "payment/ips/returnBidAuditSucc", "满标审核(转账)");
		
		LinkedHashMap<String, String> map  = new LinkedHashMap<String, String>();
		map.putAll(xmlMap);
		map.put("pIpsAcctNo", ipsAcctNo);
		map.put("parentOrderno", parentOrderno);
		map.put("bid", bid+"");	
		printRequestData(map, "满标审核提交参数", PayType.BID_AUDIT_SUCC);
		
		String xml = IpsPaymentUtil.sendWebService(IpsConstants.soap_url + IpsConstants.Transfer, IpsConstants.Transfer, xmlMap, "pMerCode", "p3DesXmlPara", "pSign");	
		xmlUtil.SetDocument(xml);
		xml = xmlUtil.getNodeValue(IpsConstants.TransferResult);
		Map<String, String> dataMap = IpsPaymentUtil.parseNoEncryptXmlToJson(xml);
		
		IpsPaymentUtil.printData(dataMap, "满标审核同步回调参数", PayType.BID_AUDIT_SUCC);
		
		Map<String, String> parseXml = null;
		if (dataMap.get("p3DesXmlPara") != null && !"".equals(dataMap.get("p3DesXmlPara"))) {
			parseXml = IpsPaymentUtil.parseXmlToJson(dataMap.get("p3DesXmlPara"));
		}		
		IpsPaymentUtil.checkSign(dataMap, "满标审核通过(转账)", parseXml,  PayType.BID_AUDIT_SUCC.name(), error);	
		
		return parseXml;
	}
	
	/**
	 * 解冻保证金
	 * @param pBidNo 标的号
	 * @param pUnfreezeAmt 解冻金额
	 * @param pUnfreezenType //解冻类型,1#解冻借款方；2#解冻担保方
	 * @param pIdentNo 身份证号
	 * @param pRealName 真实姓名
	 * @param pIpsAcctNo 第三方唯一标识
	 * @param parentOrderno 父流水号 
	 * @param parentOrderno 标的id 
	 * @return
	 * @throws Exception 
	 */
	public Map<String, String> unFreeze(String pBidNo,  double pUnfreezeAmt, String pUnfreezenType, String pIdentNo, String pRealName, String pIpsAcctNo, String parentOrderno, long bid, ErrorInfo error) throws Exception{
		
		error.clear();
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		String pMerBillNo = this.createBillNo();
		xmlMap.put("pMerBillNo", pMerBillNo);//商户订单号 
		xmlMap.put("pBidNo", pBidNo);//标的号
		xmlMap.put("pUnfreezeDate", IpsPaymentUtil.getFormatDate("yyyyMMdd")); //解冻日期
		xmlMap.put("pUnfreezeAmt", IpsPaymentUtil.formatAmount(pUnfreezeAmt+"")); //解冻金额
		xmlMap.put("pUnfreezenType", pUnfreezenType); //解冻类型,1#解冻借款方；2#解冻担保方
		xmlMap.put("pAcctType", "1");//解冻者账户类型
		xmlMap.put("pIdentNo", pIdentNo); //解冻者证件号码
		xmlMap.put("pRealName" , pRealName); //解冻者姓名 
		xmlMap.put("pIpsAcctNo", pIpsAcctNo); //解冻者 IPS 账号
		xmlMap.put("pS2SUrl", BaseController.getBaseURL() + "payment/ips/returnUnFreeze"); //异步返回地址
		xmlMap.put("pMemo1", pMerBillNo); //流水号
		xmlMap.put("pMemo2", "解冻保证金"); 
		xmlMap.put("pMemo3", "解冻保证金");
		
		LinkedHashMap<String, String> map  = new LinkedHashMap<String, String>();
		map.putAll(xmlMap);
		map.put("pIpsAcctNo", pIpsAcctNo);
		map.put("parentOrderno", parentOrderno);
		map.put("bid", bid+"");		
		printRequestData(map, "解冻保证金提交参数", PayType.UNFREEZE);
		
		String xml = IpsPaymentUtil.sendWebService(IpsConstants.soap_url + IpsConstants.GuaranteeUnfreeze, IpsConstants.GuaranteeUnfreeze, xmlMap, "argMerCode", "arg3DesXmlPara", "argSign");	
		xmlUtil.SetDocument(xml);
		xml = xmlUtil.getNodeValue(IpsConstants.GuaranteeUnfreezeResult);
		Map<String, String> dataMap = IpsPaymentUtil.parseNoEncryptXmlToJson(xml);
		
		IpsPaymentUtil.printData(dataMap, "解冻保证金同步回调参数", PayType.UNFREEZE);
		
		Map<String, String> parseXml = null;
		if (dataMap.get("p3DesXmlPara") != null && !"".equals(dataMap.get("p3DesXmlPara"))) {
			parseXml = IpsPaymentUtil.parseXmlToJson(dataMap.get("p3DesXmlPara"));
		}		
		IpsPaymentUtil.checkSign(dataMap, "解冻保证金", parseXml,  PayType.UNFREEZE.name(), error);	
		
		return parseXml;
	}
	
	/**
	 * 自动还款
	 * @param pBidNo 标的号
	 * @param pMerBillNo 流水号
	 * @param pOutAcctNo 借款人ips号 
	 * @param pIpsAuthNo 自动还款签约号
	 * @param pOutAmt 还款总金额
	 * @param pOutFee 总手续费
	 * @param pDetails 还款明细 [{'pCreMerBillNo':'登记债权人时提交的订单号', 'pInAcctNo':'投资 IPS托管账户号', 'pInFee':'转入方手续费', 'pOutInfoFee':'转出方手续费', 'pInAmt':'转入金额'}]
	 * @return
	 */
	public LinkedHashMap<String, String> autoRepayment(String pBidNo, String pMerBillNo, String pOutAcctNo, String pIpsAuthNo, double pOutAmt, double pOutFee, List<LinkedHashMap<String, String>> pDetails){						
		
		return this.repayment(pBidNo, pMerBillNo, "2", pIpsAuthNo, pOutAcctNo, pOutAmt, pOutFee, pDetails);		
	}
	
	/**
	 * 手动还款
	 * @param pBidNo 标的号
	 * @param pMerBillNo 流水号
	 * @param pOutAcctNo 借款人ips号 
	 * @param pOutAmt 还款总金额
	 * @param pOutFee 总手续费
	 * @param pDetails 还款明细 [{'pCreMerBillNo':'登记债权人时提交的订单号', 'pInAcctNo':'投资 IPS托管账户号', 'pInFee':'转入方手续费', 'pOutInfoFee':'转出方手续费', 'pInAmt':'转入金额'}]
	 * @return
	 */
	public LinkedHashMap<String, String> normalRepayment(String pBidNo, String pMerBillNo, String pOutAcctNo, double pOutAmt, double pOutFee, List<LinkedHashMap<String, String>> pDetails){						
		
		return this.repayment(pBidNo, pMerBillNo, "1", "0", pOutAcctNo, pOutAmt, pOutFee, pDetails);		
	}		
	/**
	 * 还款
	 * @param pBidNo 标的号
	 * @param pMerBillNo 流水号
	 * @param pRepayType 还款类型，1#手劢还款，2#自劢还款
	 * @param pIpsAuthNo 自动还款时，为自动还款签约号，手动还款时为
	 * @param pOutAcctNo 借款人ips号 
	 * @param pOutAmt 还款总金额
	 * @param pOutFee 总手续费
	 * @param pDetails 还款明细 [{'pCreMerBillNo':'登记债权人时提交的订单号', 'pInAcctNo':'投资 IPS托管账户号', 'pInFee':'转入方手续费', 'pOutInfoFee':'转出方手续费', 'pInAmt':'转入金额'}]
	 * @return
	 */
	public LinkedHashMap<String, String> repayment(String pBidNo, String pMerBillNo, String pRepayType, String pIpsAuthNo, String pOutAcctNo, double pOutAmt, double pOutFee, List<LinkedHashMap<String, String>> pDetails){
				
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		xmlMap.put("pBidNo", pBidNo);
		xmlMap.put("pRepaymentDate", IpsPaymentUtil.getFormatDate("yyyyMMdd"));
		xmlMap.put("pMerBillNo", pMerBillNo);
		xmlMap.put("pRepayType", pRepayType);
		xmlMap.put("pIpsAuthNo", pIpsAuthNo);
		xmlMap.put("pOutAcctNo", pOutAcctNo);
		xmlMap.put("pOutAmt", IpsPaymentUtil.formatAmount(pOutAmt+""));
		xmlMap.put("pOutFee", IpsPaymentUtil.formatAmount(pOutFee+""));
		xmlMap.put("pWebUrl", BaseController.getBaseURL() + "payment/ips/returnRepayment");
		xmlMap.put("pS2SUrl", BaseController.getBaseURL() + "payment/ips/returnRepayment");
		xmlMap.put("pDetails", IpsPaymentUtil.parseListToXml(pDetails));
		xmlMap.put("pMemo1", this.createBillNo());
		xmlMap.put("pMemo2", "还款");
		xmlMap.put("pMemo3", "还款");
		return xmlMap;
		
	}
	
	/**
	 * 自动还款签约
	 * @param pIdentNo 身份证号
	 * @param pRealName 真实姓名
	 * @param pIpsAcctNo 第三方唯一标示
	 * @return
	 */
	public LinkedHashMap<String, String> autoRepaymentSignature(String pIdentNo, String pRealName, String pIpsAcctNo) {
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		String pMerBillNo = this.createBillNo();
		xmlMap.put("pMerBillNo", pMerBillNo);
		xmlMap.put("pSigningDate",IpsPaymentUtil.getFormatDate("yyyyMMdd"));
		xmlMap.put("pIdentType", "1");
		xmlMap.put("pIdentNo", pIdentNo);
		xmlMap.put("pRealName", pRealName);
		xmlMap.put("pIpsAcctNo", pIpsAcctNo);
		xmlMap.put("pValidType", "N");
		xmlMap.put("pValidDate", "0");
		xmlMap.put("pWebUrl", BaseController.getBaseURL() + "payment/ips/returnAutoRepaymentSignature");
		xmlMap.put("pS2SUrl", BaseController.getBaseURL() + "payment/ips/returnAutoRepaymentSignature");
		xmlMap.put("pMemo1", pMerBillNo);
		xmlMap.put("pMemo2", "自动还款签约");
		xmlMap.put("pMemo3", "自动还款签约");
		return xmlMap;
	}
	
	/**
	 * 自动投标签约
	 * @param pIdentNo 身份证号
	 * @param pRealName 真实姓名
	 * @param pIpsAcctNo 第三方唯一标示
	 * @return
	 */
	public LinkedHashMap<String, String> autoInvestSignature(String pIdentNo, String pRealName, String pIpsAcctNo) {
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		String pMerBillNo = this.createBillNo();
		xmlMap.put("pMerBillNo", pMerBillNo);
		xmlMap.put("pSigningDate", IpsPaymentUtil.getFormatDate("yyyyMMdd"));
		xmlMap.put("pIdentNo", pIdentNo);
		xmlMap.put("pRealName", pRealName);
		xmlMap.put("pIpsAcctNo", pIpsAcctNo);
		xmlMap.put("pValidType", "M");
		xmlMap.put("pValidDate", "12");
		xmlMap.put("pTrdCycleType", "D");
		xmlMap.put("pSTrdCycleValue", "1");
		xmlMap.put("pETrdCycleValue", "1800");
		xmlMap.put("pSAmtQuota", "1.00");
		xmlMap.put("pEAmtQuota", "100000000.00");
		xmlMap.put("pSIRQuota", "1.00");
		xmlMap.put("pEIRQuota", "48.00");
		xmlMap.put("pWebUrl", BaseController.getBaseURL() + "payment/ips/returnAutoInvestSignature");
		xmlMap.put("pS2SUrl", BaseController.getBaseURL() + "payment/ips/returnAutoInvestSignature");
		xmlMap.put("pMemo1", pMerBillNo);
		xmlMap.put("pMemo2", "自动投标签约");
		xmlMap.put("pMemo3", "自动投标签约");
		return xmlMap;
	}
	/**
	 * 登记债权转让
	 * @param pBidNo 标的号
	 * @param pFromName 出让方账户姓名
	 * @param pFromAccount 出让方账户
	 * @param pFromIdentNo 出让方证件号码
	 * @param pToAccountName 受让方账户姓名
	 * @param pToAccount 受让方账户
	 * @param pToIdentNo 受让方证件号码 
	 * @param pCreMerBillNo 登记债权人时提交的订单号
	 * @param pCretAmt 债权面额
	 * @param pPayAmt 支付金额
	 * @param pFromFee 出让方手续费
	 * @return
	 */
	public LinkedHashMap<String, String> registerCretansfer(String pBidNo, String pFromName, String pFromAccount, String pFromIdentNo, String pToAccountName, String pToAccount, String pToIdentNo, String pCreMerBillNo, double pCretAmt, double pPayAmt, double pFromFee){
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		String pMerBillNo = this.createBillNo();
		xmlMap.put("pMerBillNo", "Z"+pCreMerBillNo);
		xmlMap.put("pMerDate", IpsPaymentUtil.getFormatDate("yyyyMMdd"));
		xmlMap.put("pBidNo", pBidNo);
		xmlMap.put("pContractNo", pBidNo);
		xmlMap.put("pFromAccountType", "1");
		xmlMap.put("pFromName", pFromName);
		xmlMap.put("pFromAccount", pFromAccount);
		xmlMap.put("pFromIdentType", "1");
		xmlMap.put("pFromIdentNo", pFromIdentNo);
		xmlMap.put("pToAccountType", "1");
		xmlMap.put("pToAccountName", pToAccountName);
		xmlMap.put("pToAccount", pToAccount);
		xmlMap.put("pToIdentType", "1");
		xmlMap.put("pToIdentNo", pToIdentNo);
		xmlMap.put("pCreMerBillNo", pCreMerBillNo);
		xmlMap.put("pCretAmt",IpsPaymentUtil.formatAmount(pCretAmt+""));
		xmlMap.put("pPayAmt", IpsPaymentUtil.formatAmount(pPayAmt+""));
		xmlMap.put("pFromFee", IpsPaymentUtil.formatAmount(pFromFee+""));
		xmlMap.put("pToFee", "0.00");
		xmlMap.put("pCretType", "1");
		xmlMap.put("pWebUrl", BaseController.getBaseURL() + "payment/ips/returnDebtorTransfer");
		xmlMap.put("pS2SUrl", BaseController.getBaseURL() + "payment/ips/returnDebtorTransfer");
		xmlMap.put("pMemo1", pMerBillNo);
		xmlMap.put("pMemo2", "登记债权转让");
		xmlMap.put("pMemo3", "登记债权转让");
		return xmlMap;
	}
	
	
	/**
	 * 债权转让成交
	 * @param pDetailsList 转账明细[{'pOriMerBillNo':'原商户订单号', 'pTrdAmt':'转账金额', 'pFAcctType':'转出方账户类型, 1:为个人' 
	 * 								'pFIpsAcctNo':'转出方 IPS 托管账户号', 'pFTrdFee':'转出方手续费明细','pTAcctType':'转入方账户类型',
	 * 								'pTIpsAcctNo':'转出方 IPS 托管账户号', 'pFTrdFee':'转出方明细手续费'}]
	 * @param pBidNo 标的号
	 * @param parentOrderno 父流水号
	 * @return
	 * @throws Exception 
	 */
	public Map<String, String> auctingDebtConfirm(List<Map<String, String>> pDetailsList, String pBidNo, String parentOrderno, String debtId, String dealpwd, ErrorInfo error)throws Exception {
		
		LinkedHashMap<String, String> xmlMap = transfer(pDetailsList, this.createBillNo(), pBidNo, "4", "payment/ips/returnDebtorTransferConfirm", "债权转让成交(转账)");
		
		LinkedHashMap<String, String> map  = new LinkedHashMap<String, String>();
		map.putAll(xmlMap);
		map.put("pIpsAcctNo", pDetailsList.get(0).get("pTIpsAcctNo"));
		map.put("debtId", debtId);
		map.put("dealpwd", dealpwd);
		printRequestData(map, "债权转让成交(转账)提交参数", PayType.DEBTOR_TRANSFER_CONFIRM);	
		
		String xml = IpsPaymentUtil.sendWebService(IpsConstants.soap_url + IpsConstants.Transfer, IpsConstants.Transfer, xmlMap, "pMerCode", "p3DesXmlPara", "pSign");
		
		xmlUtil.SetDocument(xml);
		
		xml = xmlUtil.getNodeValue(IpsConstants.TransferResult);
		
		LinkedHashMap<String, String> dataMap = IpsPaymentUtil.parseNoEncryptXmlToJson(xml);
		
		IpsPaymentUtil.printData(dataMap, "债权转让成交(转账)同步回调参数", PayType.DEBTOR_TRANSFER_CONFIRM);
		
		Map<String, String> parseXml = null;
		if (dataMap.get("p3DesXmlPara") != null && !"".equals(dataMap.get("p3DesXmlPara"))) {
			parseXml = IpsPaymentUtil.parseXmlToJson(dataMap.get("p3DesXmlPara"));
		}
		IpsPaymentUtil.checkSign(dataMap, "债权转让成交(转账)", parseXml, PayType.DEBTOR_TRANSFER_CONFIRM.name(), error);
		parseXml.put("pMerBillNo", xmlMap.get("pMerBillNo"));
		return parseXml;
	}			
	/**
	 * 登记担保人（逾期垫付)
	 * @param pBidNo标的号
	 * @param pAmount 担保金额, 目前为标的金额1.5倍,便于逾期罚息及利息高的情况
	 * @return
	 * @throws Exception
	 */
	public LinkedHashMap<String, String> advance(String pBidNo, double pAmount){


		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		
		String pMerBillNo = this.createBillNo();//商户订单号		
		xmlMap.put("pMerBillNo", pMerBillNo);
		xmlMap.put("pMerDate", IpsPaymentUtil.getFormatDate("yyyyMMdd"));
		xmlMap.put("pBidNo", pBidNo);
		xmlMap.put("pAmount", IpsPaymentUtil.formatAmount(pAmount+""));
		xmlMap.put("pMarginAmt", "0");
		xmlMap.put("pProFitAmt", "0");
		xmlMap.put("pAcctType", "1");
		xmlMap.put("pFromIdentNo", IpsConstants.pFromIdentNo);
		xmlMap.put("pAccountName ", IpsConstants.pAccountName);
		xmlMap.put("pAccount", IpsConstants.pAccount);
		xmlMap.put("pWebUrl", BaseController.getBaseURL() + "payment/ips/returnAdvance");
		xmlMap.put("pS2SUrl", BaseController.getBaseURL() + "payment/ips/returnAdvance");
		xmlMap.put("pMemo1", pMerBillNo);
		xmlMap.put("pMemo2", "登记担保方");
		xmlMap.put("pMemo3", "登记担保方");		
		return xmlMap;		
	} 
	
	/**
	 * 垫付成交(逾期垫付)
	 * @param pDetailsList 转账明细
	 * @param pBidNo 标的号
	 * @param pMerBillNo 垫付流水号，防止重复垫付，以"D" + t_bill.merBillNo作为流水号生成规则, D打头为垫付，正常还款无前缀, P打头为垫付还款
	 * @param returnMap 回调方法需要的参数
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> advanceConfirm(List<Map<String, String>> pDetailsList, String pBidNo, String pMerBillNo, LinkedHashMap<String, String> returnMap, ErrorInfo error){
		
		LinkedHashMap<String, String> xmlMap = transfer(pDetailsList, pMerBillNo, pBidNo, "2", "payment/ips/returnAdvanceConfirm", "垫付转账成交");
			
		//保存回调需要的参数及提交接口的参数
		returnMap.putAll(xmlMap);
		this.printRequestData(returnMap, "垫付转账成交提交参数", PayType.ADVANCE_CONFIRM);	
		
		String xml = "";
		try {
			xml = IpsPaymentUtil.sendWebService(IpsConstants.soap_url + IpsConstants.Transfer, IpsConstants.Transfer, xmlMap, "pMerCode", "p3DesXmlPara", "pSign");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("垫付成交,webservice接口请求时，%s", e.getMessage());
			
			error.code = -1;
			error.msg = "垫付成交,webservice接口请求异常";
			
			return null;
		}
		
		xmlUtil.SetDocument(xml);
		
		xml = xmlUtil.getNodeValue(IpsConstants.TransferResult);
		
		LinkedHashMap<String, String> dataMap = null;
		try {
			dataMap = IpsPaymentUtil.parseNoEncryptXmlToJson(xml);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("将未加密的xml转化成Map字符串时，%s", e.getMessage());
			
			error.code = -2;
			error.msg = "将未加密的xml转化成Map字符串异常";
			
			return null;
		}
		
		Map<String, String> parseXml = null;
		if (dataMap.get("p3DesXmlPara") != null && !"".equals(dataMap.get("p3DesXmlPara"))) {
			try {
				parseXml = IpsPaymentUtil.parseXmlToJson(dataMap.get("p3DesXmlPara"));
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("将加密的xml转化成Map字符串时，%s", e.getMessage());
				
				error.code = -3;
				error.msg = "将加密的xml转化成Map字符串异常";
				
				return null;
			}	
		}
		
		dataMap.put("pMemo1", xmlMap.get("pMemo1"));
		
		IpsPaymentUtil.printData(dataMap, "垫付转账成交同步回调参数", PayType.ADVANCE_CONFIRM);
		
		IpsPaymentUtil.checkSign(dataMap, "垫付转账成交", parseXml, PayType.ADVANCE_CONFIRM.name(), error);
		
		if(error.code < 0){
			
			return null;
		}
	
		
		parseXml.put("pMerBillNo", xmlMap.get("pMerBillNo"));
		return parseXml;
		
	}
	
	/**
	 * 垫付还款(待偿还款)
	 * @param pDetailsList 转账明细
	 * @param pBidNo 标的号
	 * @param pMerBillNo 垫付流水号，防止重复垫付，以"P" + t_bill.merBillNo作为流水号生成规则, D打头为垫付，正常还款无前缀, P打头为垫付还款
	 * @param returnMap 回调方法需要的参数
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> AdvanceRepayment(List<Map<String, String>> pDetailsList, String pBidNo, String pMerBillNo, LinkedHashMap<String, String> returnMap, ErrorInfo error){
		
		LinkedHashMap<String, String> xmlMap = transfer(pDetailsList, pMerBillNo, pBidNo, "3", "payment/ips/returnAdvanceRepayment", "垫付还款");
			
		//保存回调需要的参数及提交接口的参数
		returnMap.putAll(xmlMap);
		this.printRequestData(returnMap, "垫付还款提交参数", PayType.ADVANCE_REPAYMENT);	
		
		String xml;
		try {
			xml = IpsPaymentUtil.sendWebService(IpsConstants.soap_url + IpsConstants.Transfer, IpsConstants.Transfer, xmlMap, "pMerCode", "p3DesXmlPara", "pSign");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("垫付还款,webservice接口请求时，%s", e.getMessage());
			
			error.code = -1;
			error.msg = "垫付还款,webservice接口请求异常";
			
			return null;
		}
		
		xmlUtil.SetDocument(xml);
		
		xml = xmlUtil.getNodeValue(IpsConstants.TransferResult);
		
		LinkedHashMap<String, String> dataMap;
		try {
			dataMap = IpsPaymentUtil.parseNoEncryptXmlToJson(xml);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("将未加密的xml转化成Map字符串时，%s", e.getMessage());
			
			error.code = -2;
			error.msg = "将未加密的xml转化成Map字符串异常";
			
			return null;
		}	
		
		Map<String, String> parseXml = null;
		if (dataMap.get("p3DesXmlPara") != null && !"".equals(dataMap.get("p3DesXmlPara"))) {
			try {
				parseXml = IpsPaymentUtil.parseXmlToJson(dataMap.get("p3DesXmlPara"));
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("将加密的xml转化成Map字符串时，%s", e.getMessage());
				
				error.code = -3;
				error.msg = "将加密的xml转化成Map字符串异常";
				
				return null;
			}	
		}		
		dataMap.put("pMemo1", xmlMap.get("pMemo1"));		
		IpsPaymentUtil.printData(dataMap, "垫付还款同步回调参数", PayType.ADVANCE_REPAYMENT);		
		IpsPaymentUtil.checkSign(dataMap, "垫付还款成交", parseXml, PayType.ADVANCE_REPAYMENT.name(), error);		
		parseXml.put("pMerBillNo", xmlMap.get("pMerBillNo"));
		
		return parseXml;
		
	}	
	
	
	/**
	 * 转账
	 * @param pDetailsList 转账明细列表
	 * @param pMerBillNo 流水号
	 * @param pBidNo 标的号
	 * @param pTransferType 转账类型 1：投资; 2：代偿 ，3：代偿还款; 4：债权转让
	 * @param pS2SUrl 异步通知地址
	 * @param mark 备注
	 * @return
	 */
	public LinkedHashMap<String, String> transfer(List<Map<String, String>> pDetailsList, String pMerBillNo, String pBidNo, String pTransferType, String pS2SUrl, String mark){
		String pDetails = "";
		String pTransferMode = "1";
		if ("1".equals(pTransferType)){
			
			pTransferMode = "2";
		}
		
		for (int i = 0; i < pDetailsList.size(); i++){
			Map<String, String> pDetailsMap = pDetailsList.get(i);
			pDetails += "<pRow>" 
				+ "<pOriMerBillNo>" + pDetailsMap.get("pOriMerBillNo") + "</pOriMerBillNo>"
				+ "<pTrdAmt>" + String.format("%.2f", Convert.strToDouble(pDetailsMap.get("pTrdAmt"), 0)) + "</pTrdAmt>"
				+ "<pFAcctType>" + pDetailsMap.get("pFAcctType") + "</pFAcctType>"
				+ "<pFIpsAcctNo>" + pDetailsMap.get("pFIpsAcctNo") + "</pFIpsAcctNo>"
				+ "<pFTrdFee>" + String.format("%.2f", Convert.strToDouble(pDetailsMap.get("pFTrdFee"), 0)) + "</pFTrdFee>"
				+ "<pTAcctType>" + pDetailsMap.get("pTAcctType") + "</pTAcctType>"
				+ "<pTIpsAcctNo>" + pDetailsMap.get("pTIpsAcctNo") + "</pTIpsAcctNo>"
				+ "<pTTrdFee>" + String.format("%.2f", Convert.strToDouble(pDetailsMap.get("pTTrdFee"), 0)) + "</pTTrdFee>"
			+ "</pRow>";
		}
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		xmlMap.put("pMerBillNo", pMerBillNo);
		xmlMap.put("pBidNo", pBidNo);
		xmlMap.put("pDate", IpsPaymentUtil.getFormatDate("yyyyMMdd"));
		xmlMap.put("pTransferType", pTransferType);
		xmlMap.put("pTransferMode", pTransferMode);
		xmlMap.put("pS2SUrl", BaseController.getBaseURL() + pS2SUrl);
		xmlMap.put("pDetails", pDetails);
		xmlMap.put("pMemo1", this.createBillNo());
		xmlMap.put("pMemo2", mark);
		xmlMap.put("pMemo3", mark);
		return xmlMap;
	}
	/**
	 * 提现(备注由于环讯，收用户手续费，不通知p2p平台，故此版本，环讯手续费由p2p商户垫付，p2p商户再从用户收取手续费即pMerFee字段, 此种模式类似外扣)
	 * @param pIdentNo 身份证号
	 * @param pRealName 真实姓名
	 * @param pIpsAcctNo 第三方唯一标示
	 * @param pTrdAmt 提现金额
	 * @param pMerFee 商户平台收用户手续费
	 * @return
	 */
	public LinkedHashMap<String, String> withdraw(String pIdentNo, String pRealName, String pIpsAcctNo, double pTrdAmt, double pMerFee){
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();

		String pMerBillNo = this.createBillNo();
		xmlMap.put("pMerBillNo", pMerBillNo);
		xmlMap.put("pAcctType", "1");
		xmlMap.put("pOutType", "1"); //不生效
		xmlMap.put("pBidNo", "1"); //不生效
		xmlMap.put("pContractNo", "1"); //不生效
		xmlMap.put("pDwTo", "1"); //不生效
		xmlMap.put("pIdentNo", pIdentNo);
		xmlMap.put("pRealName", pRealName);
		xmlMap.put("pIpsAcctNo", pIpsAcctNo);
		xmlMap.put("pDwDate", IpsPaymentUtil.getFormatDate("yyyyMMdd"));
		xmlMap.put("pTrdAmt", IpsPaymentUtil.formatAmount(pTrdAmt+""));
		xmlMap.put("pMerFee", IpsPaymentUtil.formatAmount(pMerFee+""));
		xmlMap.put("pIpsFeeType", "1");
		xmlMap.put("pWebUrl", BaseController.getBaseURL() + "payment/ips/returnWithdraw");
		xmlMap.put("pS2SUrl", BaseController.getBaseURL() + "payment/ips/returnWithdraw");
		xmlMap.put("pMemo1", pMerBillNo);
		xmlMap.put("pMemo2", "提现");
		xmlMap.put("pMemo3", "提现");
		return xmlMap;
	}
	
	/**
	 * 获取银行列表
	 * @param xmlMap
	 * @throws Exception 
	 */
	public List<Map<String, Object>> queeryBankList() throws Exception{
		
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();		
		xmlMap.put("argMerCode", IpsConstants.terraceNoOne);
		IpsPaymentUtil.printData(xmlMap, "银行列表同步提交参数", PayType.QUERY_BANKS);
		String xml = IpsPaymentUtil.sendWebService(IpsConstants.soap_url + IpsConstants.GetBankList, IpsConstants.GetBankList, xmlMap, "argMerCode", "arg3DesXmlPara", "argSign");			
		xmlUtil.SetDocument(xml);
		xml = xmlUtil.getNodeValue(IpsConstants.GetBankListResult);
		LinkedHashMap<String, String> dataMap = IpsPaymentUtil.parseNoEncryptXmlToJson(xml);
		IpsPaymentUtil.printData(dataMap, "银行列表同步回调参数", PayType.QUERY_BANKS);
		
		String bankStr = dataMap.get("pBankList");
		String[] banks = bankStr.split("#");
		List<Map<String, Object>> bankList = new LinkedList<Map<String, Object>>();
		for(int i = 0; i < banks.length; i++){
			Map<String, Object> map = new HashMap<String, Object>();
			String[] bank = banks[i].split("\\|");
			map.put("code", bank[2]);
			map.put("name", bank[0]);
			bankList.add(map);
		}
		return bankList;
	}	
	/**
	 * 查询可用余额
	 * @param argIpsAccount 环讯ips账号
	 * @throws Exception
	 */
	public LinkedHashMap<String, String> queryAmount(String argIpsAccount) throws Exception{
		LinkedHashMap<String, String> xmlMap = new LinkedHashMap<String, String>();
		xmlMap.put("argMerCode", IpsConstants.terraceNoOne);
		xmlMap.put("argIpsAccount", argIpsAccount);
		return xmlMap;
		
	}
	
	/**
	 * 获取银行列表待签名串(备注，只针对获取银行列表用，原因:获取银行列表待签名串与其他不同)
	 * @param dataMap 参数
	 * @param params 待签名参数
	 * @return
	 */
	private String getQueryBankListSign(LinkedHashMap<String, String> dataMap, String... params){
		
		String plainSign = "";
		for(int i = 0; i < params.length; i++){
			plainSign = plainSign + "<"+params[i]+">" +dataMap.get(params[i])+ "</"+params[i]+">";
		}
		plainSign = plainSign + IpsConstants.cert_md5;
		return plainSign;
		
	}
	
	/**
	 * 查询还款提交第三方需要的参数
	 * @param bill
	 * @param isAutoRepayment 自动还款:true; 手动还款false
	 * @return 
	 */
	public LinkedHashMap<String, String> queryRepaymentData(Bill bill, boolean isAutoRepayment){
		
		t_bills t_bill = t_bills.findById(bill.id);
		
		//标的初始化
		Bid bid = new Bid();
		bid.id = bill.bidId;
		
		//初始化借款人
		User borrower = new User();
		borrower.id = bid.userId;
		t_users u = t_users.findById(borrower.id);
		
		double pOutAmt = 0.00; //还款总金额=本金 + 利息 +罚息
		double pOutFee = 0.00; //投资管理费总和
		
		//投资利息管理费费率
		double managementRate = Bid.queryInvestRate(bid.id);		
 		if(managementRate != 0){
 			managementRate = managementRate / 100;
		}
		//投资列表
		List<t_bill_invests> list = t_bill_invests.find(" bid_id = ? and periods = ? and mer_bill_no is not null and status not in (0,-3,-7,-4) ", bill.bidId, t_bill.periods).fetch();
		
		List<LinkedHashMap<String, String>> pDetails = new LinkedList<LinkedHashMap<String, String>>();
		for(int i = 0; i< list.size(); i++){
			
			t_bill_invests invest = list.get(i);
			//投资人收益
			double pInAmt = invest.receive_interest + invest.receive_corpus + invest.overdue_fine;
			pOutAmt = pOutAmt + pInAmt;
			
			double pOutInfoFee = BillInvests.getInvestManagerFee(invest.receive_interest, managementRate, invest.user_id);// 投资管理费
			pOutFee = pOutFee + pOutInfoFee;
			
			//初始化投资人
			User invester = new User();
			invester.id = invest.user_id;
			
			LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
			map.put("pCreMerBillNo", invest.mer_bill_no);
			map.put("pInAcctNo", invester.ipsAcctNo);
			map.put("pInFee", IpsPaymentUtil.formatAmount(pOutInfoFee+""));
			map.put("pOutInfoFee", "0.00");
			map.put("pInAmt", IpsPaymentUtil.formatAmount(pInAmt+""));
			pDetails.add(map);
		}
		LinkedHashMap<String, String> paramMap = null;
		
		if(isAutoRepayment){			
			paramMap = this.autoRepayment(bid.bidNo, t_bill.mer_bill_no, borrower.ipsAcctNo, u.ips_repay_auth_no, pOutAmt, 0.00, pDetails);
		}else{
			paramMap = this.normalRepayment(bid.bidNo, t_bill.mer_bill_no, borrower.ipsAcctNo, pOutAmt, 0.00, pDetails);
		}
		
		return paramMap;
	}
	
	/**
	 * 垫付查询
	 * @param billId 账单bill_id
	 * @return
	 */
	public List<Map<String, String>> queryAdvance(Long billId, ErrorInfo error){
		
		//查询需要垫付的账单
		t_bills bill = t_bills.findById(billId);
				
		double investmentRate = Bid.queryInvestRate(bill.bid_id);
 		
 		if(investmentRate != 0){
 			investmentRate = investmentRate /100;
 		}
 		
		String sql = "select new Map(invest.mer_bill_no as mer_bill_no, invest.id as id, invest.invest_id as investId, invest.receive_corpus as receive_corpus,invest.receive_interest as " + "receive_interest, invest.overdue_fine as overdue_fine, invest.user_id as user_id, invest.overdue_fine) "
				+ "from t_bill_invests as invest where invest.bid_id = ? and invest.periods = ? and invest.status not in (?,?,?,?)";

		List<Map<String, Object>> investList = t_bill_invests.find(sql, bill.bid_id, bill.periods, Constants.FOR_DEBT_MARK, Constants.NORMAL_RECEIVABLES, Constants.ADVANCE_PRINCIIPAL_RECEIVABLES, Constants.OVERDUE_RECEIVABLES).fetch();
		
		List<Map<String, String>> pDetails = new LinkedList< Map<String, String>>();
		
		for (Map<String, Object> map : investList) {

			double receiveInterest = (Double) map.get("receive_interest");// 本期的投资利息
			double receiveCorpus = (Double) map.get("receive_corpus"); //本期投资本金
			double receiveFees = (Double) map.get("overdue_fine"); //本期投资罚息
		
			//支付给投资人的本金+利息+罚息
			double pTrdAmt = receiveInterest + receiveCorpus + receiveFees;
			
			//初始化投资记录
			long investId  = (Long) map.get("investId");			
			t_invests invest = t_invests.findById(investId);
						
			//初始化投资人
			User investUser = new User();
			investUser.id = invest.user_id;
			
			//初始化投资利息管理费
			double investManageFee = BillInvests.getInvestManagerFee(receiveInterest, investmentRate, invest.user_id);// 投资管理费
			
			String pOriMerBillNo = map.get("mer_bill_no").toString();
			//组装垫付列表
			Map<String, String> pDetailsMap = new HashMap<String, String>();
			pDetailsMap.put("pOriMerBillNo", pOriMerBillNo); //登记债权人即投资时，提交的流水号
			pDetailsMap.put("pTrdAmt", IpsPaymentUtil.formatAmount(pTrdAmt+"")); //垫付金额
			pDetailsMap.put("pFAcctType", "1"); //账户类型，1：个人
			pDetailsMap.put("pFIpsAcctNo", IpsConstants.pAccount); //垫付人ips账号
			pDetailsMap.put("pFTrdFee", "0.00"); //担保方手续费为0
			pDetailsMap.put("pTAcctType", "1");//账户类型，1：个人
			pDetailsMap.put("pTIpsAcctNo", investUser.ipsAcctNo); //收款人ips账号
			pDetailsMap.put("pTTrdFee", IpsPaymentUtil.formatAmount(investManageFee+"")); //理财利息管理费
			pDetails.add(pDetailsMap);
		}
		return pDetails;
	}
	
	/**
	 * 垫付还款查询
	 * @param billId 账单bill_id
	 * @param userId 借款人id
	 * @return
	 */
	public List<Map<String, String>> queryAdvanceRepayment(Long billId, long userId, ErrorInfo error){
		
		//1.查询本期借款账单的账单数据
		String sql = "select new Map(user.ips_acct_no as ips_acct_no,user.mobile as mobile,bid.id as bid_id,bid.bid_no as bid_no,bill.overdue_mark as overdue_mark, bill.repayment_corpus as " +
				"repayment_corpus, bill.repayment_interest as repayment_interest, bill.overdue_fine as overdue_fine," +
				" bill.status as status, bill.periods as period) from t_bills as bill,t_bids as bid, t_users as user where bill.bid_id = bid.id and bid.user_id = user.id and bill.id = ?";
		
		Map<String, Object> result = t_bills.find(sql, billId).first();
		Double repaymentCorpus = (Double)result.get("repayment_corpus"); //本金
		Double repaymentInterest = (Double)result.get("repayment_interest"); //利息
		Double repayOverdueFine = (Double)result.get("overdue_fine"); //罚息
		
		double pTrdAmt = repaymentCorpus + repaymentInterest + repayOverdueFine; //还款总额
		t_bills bill = t_bills.findById(billId);
		User user = new User();
		user.id = userId;
		
		//组装垫付列表
		List<Map<String, String>> pDetails = new LinkedList< Map<String, String>>();
		Map<String, String> pDetailsMap = new HashMap<String, String>();
		
		pDetailsMap.put("pOriMerBillNo", bill.mer_bill_no); //垫付时提交的流水号(D打头的流水号)
		pDetailsMap.put("pTrdAmt", IpsPaymentUtil.formatAmount(pTrdAmt+"")); //垫付金额
		pDetailsMap.put("pFAcctType", "1"); //账户类型，1：个人
		pDetailsMap.put("pFIpsAcctNo", user.ipsAcctNo); //垫付人ips账号
		pDetailsMap.put("pFTrdFee", "0.00"); //担保方手续费为0
		pDetailsMap.put("pTAcctType", "1");//账户类型，1：个人
		pDetailsMap.put("pTIpsAcctNo", IpsConstants.pAccount); //收款人ips账号
		pDetailsMap.put("pTTrdFee", "0.00"); //理财利息管理费
		pDetails.add(pDetailsMap);
		
		return pDetails;
	}

	@Override
	public void printRequestData(Map<String, String> param, String mark, PayType payType){
		
		Logger.info("******************"+mark+"开始******************");
		for(Entry<String, String> entry : param.entrySet()){			
			Logger.info("***********"+entry.getKey() + "--" + entry.getValue());
		}
		Logger.info("******************"+mark+"结束******************");
		
		if(payType.getIsSaveLog()){		
			JPAUtil.transactionBegin();
			t_mmm_data t_mmm_data = new t_mmm_data();
			t_mmm_data.mmmUserId = param.get("pIpsAcctNo") == null ? "-1" : param.get("pIpsAcctNo");
			t_mmm_data.orderNum = param.get("pMemo1");
			t_mmm_data.parent_orderNum = param.get("parentOrderno");
			t_mmm_data.op_time = new Date();
			t_mmm_data.msg = mark;
			t_mmm_data.data = gson.toJson(param);
			t_mmm_data.status = 1;
			t_mmm_data.type = payType.name();		
			t_mmm_data.url = param.get("pS2SUrl");
			t_mmm_data.save();
			JPAUtil.transactionCommit();
		}
	}

	@Override
	public void printData(Map<String, String> paramMap, String desc,
			PayType payType) {
		// TODO Auto-generated method stub
		
	}
}
