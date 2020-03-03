import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import models.t_users;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import payment.hf.util.HfConstants;
import payment.hf.util.SignUtils;
import play.Logger;
import play.test.UnitTest;
import utils.ErrorInfo;
import business.DataSafety;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class CustomerTest extends UnitTest{
	
	/**
	 * 资金平账
	 */
	@Test
	public void priceSign(){
		List<t_users> list = t_users.findAll();
		DataSafety safy = new DataSafety();
		ErrorInfo error = new ErrorInfo();
		for(t_users user : list){
			safy.updateSignWithLock(user.id, error);
		}
	}
	
	@Test
	public void validateJson()throws Exception{
		String path = "C:\\json.txt";
		String result = IOUtils.toString(new FileInputStream(new File(path)),"UTF-8");
		Logger.info("result : %s", result);
		JsonElement jsonEle = new JsonParser().parse(result);
				
	}
	
	@Test
	public void buildJosn(){
		JsonArray divDetailAarray = new JsonArray();
		JsonObject divDetailsJson = new JsonObject();
		divDetailsJson.addProperty("DivCustId", HfConstants.MERCUSTID);
		divDetailsJson.addProperty("DivAcctId", HfConstants.SERVFEEACCTID);
		divDetailsJson.addProperty("DivAmt", "0.17");
		divDetailAarray.add(divDetailsJson);
		String divDetails = divDetailAarray.toString();
		Logger.info("%s", divDetails);
	}
	
	@Test
	public void buildBatchRepayment(){
		JsonObject batchRepaymentJson = new JsonObject();
		JsonArray array = new JsonArray();
		JsonObject json = new JsonObject();
		json.addProperty("InCustId", "xxx");
		json.addProperty("InAcctId", "xxx");
		json.addProperty("OrdId", "201412102144");
		json.addProperty("SubOrdId", "xxx");
		json.addProperty("FeeObjFlag", "I");
		json.addProperty("TransAmt", "5.00");
		json.addProperty("Fee", "1.00");
		JsonArray divDetails = new JsonArray();
		JsonObject divJson = new JsonObject();
		divJson.addProperty("DivCustId", "xxx");
		divJson.addProperty("DivAcctId", "MDT000001");
		divJson.addProperty("DivAmt", "1.00");
		divDetails.add(divJson);
		json.add("DivDetails", divDetails);
		array.add(json);
		batchRepaymentJson.add("InDetails", array);
		Logger.info("%s", batchRepaymentJson.toString());
	}
	
	@Test
	public void batchRepaymentResp()throws Exception{
		String path = "C:\\json.txt";
		String result = IOUtils.toString(new FileInputStream(new File(path)),"UTF-8");
		Logger.info("result : %s", result);
		String value = excuteLoans(result);
		Logger.info("value : %s", value);
		JsonObject params = new JsonParser().parse(value).getAsJsonObject();
		StringBuffer buffer = new StringBuffer();
		String[] keys = HfConstants.getRespChkValueKeys(HfConstants.CMD_BATCHREPAYMENT);		
		for(String key : keys ){
			Logger.info("%s : %s", key,params.get(key));
			try {
				//GooleJson可能会解析出JsonNull,该对象不与null相等
				value = "".equals((params.get(key) instanceof JsonNull | params.get(key) == null)?"":params.get(key).getAsString())?"":URLDecoder.decode(params.get(key).getAsString(),"utf-8");
				Logger.info("%s : %s", key,value);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			buffer.append(value);
		}
		String chkValue = params.get("ChkValue").getAsString();
		
		boolean flag = false;
		try {
			Logger.info("---------------validResp chkValue明文：%s", buffer.toString());
			flag = SignUtils.verifyByRSA(buffer.toString(), chkValue);

		} catch (Exception e) {

			e.printStackTrace();

		}
		if (!flag) {

			Logger.error("汇付天下回调签名验证失败");

		}
	}
	
	public static void readFloder()throws Exception{
		String path = "C:\\hflog\\2015\\7\\14\\20150714batchRepaymentrequest.logs";
		List<String> list = IOUtils.readLines(new FileInputStream(new File(path)));
		Logger.info("content : %s", list.size());
	}
	
	public static void main(String[] args)throws Exception{
		readFloder();
	}
	
	/**
	 * 放款字符处理
	 * @param value
	 * @return
	 */
	public static String excuteLoans(String value){
		String tag = "CmdId\":\"Loans";
		Logger.info(" tag : %s ",tag);
		if(value.contains("CmdId\":\"Loans")){
			value = value.replaceAll("\"ProId\":\"", "ProId:").replaceAll("\"\\{\"", "\\{").replaceAll("\"\\}\"", "\\}\"");
			Logger.info("constains loans ");
		}
		return value;
	}
	
	@Test
	public void usrFreeze(){
		String ordId = "123912039120312";
		String usrCustId = "6000060001742217";
		String transAmt = "200000.00";
//		ChinaPnrReqModel model = ChinaPnrPaymentConver.buildUsrFreezeParams(ordId, usrCustId, transAmt);
//		ChinaPnrService service = new ChinaPnrService();
//		service.doUsrFreeze(model);
	}
	
	
	/**
	 * 资金解冻
	 */
	@Test
	public void UsrUnFreeze(){
//		ChinaPnrReqModel model = ChinaPnrPaymentConver.buildUsrUnFreezeParams("50000312320", "20150719", "201507190001701979");
//		ChinaPnrService service = new ChinaPnrService();
//		service.doUsrUnFreeze(model);
	}
	
	/**
	 * 查询商户子账户
	 */
	@Test
	public void chinapnrServicedoQueryAccts(){
//		ChinaPnrService service = new ChinaPnrService();
//		String result = service.doQueryAccts();
//		JsonObject json = new JsonParser().parse(result).getAsJsonObject();
//		JsonArray array = json.get("AcctDetails").getAsJsonArray();
//		String acctType = null;
//		String subAcctId = null;
//		String avlBal = null;
//		String acctBal = null;
//		String frzBal = null;
//		List<AcctsDto> list = new ArrayList<AcctsDto>();
//		AcctsDto dto = null;
//		for(JsonElement ele : array){
//			acctType = ele.getAsJsonObject().get("AcctType").getAsString();
//			subAcctId = ele.getAsJsonObject().get("SubAcctId").getAsString();
//			avlBal = ele.getAsJsonObject().get("AvlBal").getAsString();
//			acctBal = ele.getAsJsonObject().get("AcctBal").getAsString();
//			frzBal = ele.getAsJsonObject().get("FrzBal").getAsString();
//			dto = new AcctsDto(acctType, subAcctId, avlBal, acctBal, frzBal);
//			list.add(dto);
//		}
//		for(AcctsDto acctsDto : list){
//			Logger.info("%s", acctsDto.toString());
//		}
	}
	
	@Test
	public void chinapnrService_doQueryBalanceBg(){
//		ChinaPnrService service = new ChinaPnrService();
//		ChinaPnrReqModel model = new ChinaPnrReqModel();
//		model.setVersion(ChinaPnrConstants.VERSION1);
//		model.setMerCustId(ChinaPnrConstants.MERCUSTID);
//		model.setUsrCustId(ChinaPnrConstants.MERCUSTID);
//		service.doQueryBalanceBg(model);
	}
	
	
}
