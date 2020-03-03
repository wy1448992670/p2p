import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import play.Logger;
import play.libs.WS;
import play.test.UnitTest;
import utils.ErrorInfo;
import business.PaymentLog;


public class RepairTest extends UnitTest{
	
	private void submitForm(String url,Map<String,String> maps){
		Logger.info(WS.url(url).setParameters(maps).post().getString());
	}
	/**
	 * 用户注册补单
	 */
	@Test
	public void resigter_repair(){
		String url = "http://localhost:9000/payment/chinapnr/userRegisterAyns";
		Map<String,String> maps = new HashMap<String, String>();
		maps.put("UsrCustId", "6000060001739187");
		maps.put("BgRetUrl", "http://localhost:9000/payment/chinapnr/userRegisterAyns");
		maps.put("UsrName", "张扬");
		maps.put("IdType", "00");
		maps.put("MerPriv", "5000095");
		maps.put("RetUrl", "http://localhost:9000/payment/chinapnr/userRegister");
		maps.put("UsrMp", "13620913216");
		maps.put("TrxId", "214516902555612621");
		maps.put("UsrId", "ddcf_12312321");
		maps.put("RespCode", "000");
		maps.put("time", "2015-07-16 10:56:06");
		maps.put("RespDesc", "成功");
		maps.put("IdNo", "110101198808085638	");
		maps.put("ChkValue", "17F52FAD11D4A912B0AAED88C29C94AF1CC72942FC7878B7517E0AF5DD973C1B49931177DDF2BF74AC344154070B78C6C09BA18196613870B27C210BA699BA2FA3AB8D9E922EE48D692A53509863DDA2C67A5B52718C6C1BE7E9C22649E0E419BEF15D1E150BF1822E4356527669FD4C8A587EDD65A0AE11BE4BD889BEFAF4DC");
		maps.put("MerCustId", "6000060001368461");
		maps.put("UsrEmail", "yangxuan@eims.com");
		maps.put("CmdId", "UserRegister");
		submitForm(url,maps);
	}
	
	/**
	 * 充值补单
	 */
	@Test
	public void netsave_repair(){
		String url = "http://localhost:9000/payment/chinapnr/netSaveAyns";
		Map<String,String> maps = new HashMap<String, String>();
		maps.put("CardId", "");
		maps.put("UsrCustId", "6000060001739187");
		maps.put("FeeCustId", "6000060001739187");
		maps.put("TransAmt", "1000000.00");
		maps.put("BgRetUrl", "http://localhost:9000/payment/chinapnr/netSaveAyns");
		maps.put("GateBankId", "CIB");
		maps.put("RetUrl", "http://localhost:9000/payment/chinapnr/netSave");
		maps.put("MerPriv", "");
		maps.put("TrxId", "201507160000208454");
		maps.put("FeeAcctId", "MDT000001");
		maps.put("RespCode", "000");
		maps.put("time", "2015-07-16 11:14:32");
		maps.put("GateBusiId", "B2C");
		maps.put("FeeAmt", "2500.00");
		maps.put("CashierSysId", "0000208454");
		maps.put("RespDesc", "成功");
		maps.put("OrdDate", "20150716");
		maps.put("ChkValue", "5EE4D18D2038F76C331D921333AC7A0EFD1E7B67E0FD0FC062B5A714962135CC04C53F9E177A8B3A7B95B964545107539BEEA1C20C297ECA39D5D26D6872B9D1D6B2C6F606756C35F6572F22372BD337A2622C9BE63E6710CB3A6CB827CF7829D6D7F4C6F150EB2CCD6FF1DD36E9CC21DE3AC64FADD7EB37B3DDA6F34676D178");
		maps.put("MerCustId", "6000060001368461");
		maps.put("OrdId", "5000098");
		maps.put("Version", "10");
		maps.put("CmdId", "NetSave");
		maps.put("CashierAcctDate", "20150716");
		submitForm(url, maps);
	}
	
	/**
	 * 提现对账
	 */
	@Test
	public void cash_repair(){
		String url = "http://localhost:9000/payment/chinapnr/cashAyns";
		Map<String,String> maps = new HashMap<String, String>();
		maps.put("UsrCustId", "6000060001739187");
		maps.put("BgRetUrl", "http://localhost:9000/payment/chinapnr/cashAyns");
		maps.put("FeeCustId", "6000060001739187");
		maps.put("TransAmt", "1500.00");
		maps.put("OpenBankId", "CCB");
		maps.put("RespExt", "");
		maps.put("MerPriv", "");
		maps.put("RetUrl", "http://localhost:9000/payment/chinapnr/cash");
		maps.put("ServFee", "15.00");
		maps.put("FeeAcctId", "MDT000001");
		maps.put("RespCode", "000");
		maps.put("time", "2015-07-16 14:25:58");
		maps.put("FeeAmt", "2.75");
		maps.put("RespDesc", "成功");
		maps.put("OpenAcctId", "436742234092");
		maps.put("ChkValue", "CA7F830EE33C89D6F876F14338B23C5D63C59CA42F6B49E4473E486E3DB233DABD98E5C94807BCF0B84B380FA7DC9F30FD48CF99FCF9F02522A5616C541A0FA7A7D9DF33F0E8948F730D9E1C85E47827674D9CF0FB1C2A13D94A2D9468C0060067D74D834C08AC4027F114ECE706ADC9C0277883E469960611256B9C33D8ADF6");
		maps.put("MerCustId", "6000060001368461");
		maps.put("ServFeeAcctId", "MDT000001");
		maps.put("OrdId", "5000105");
		maps.put("RealTransAmt", "1500.00");
		maps.put("CmdId", "Cash");
		submitForm(url, maps);
	}
	
	/**
	 * 债权转让补单
	 */
	@Test
	public void creditAssign_repair(){
		String url = "http://localhost:9000/payment/chinapnr/creditAssignAyns";
		Map<String,String> maps = new HashMap<String, String>();
		maps.put("BgRetUrl", "http://localhost:9000/payment/chinapnr/creditAssignAyns");
		maps.put("BuyCustId", "6000060001731461");
		maps.put("Fee", "0.00");
		maps.put("RetUrl", "http://localhost:9000/payment/chinapnr/creditAssign");
		maps.put("MerPriv", "");
		maps.put("RespExt", "");
		maps.put("CreditDealAmt", "5001.00");
		maps.put("RespCode", "000");
		maps.put("OrdDate", "20150715");
		maps.put("RespDesc", "成功");
		maps.put("ChkValue", "75C80C50306C995B4774D2118D0C88CB08B6FE9B38E46EB7D6A985A13E3558FE4379DEB4F6518BD1B25D26FFBB2C4E665247D320FD0540E8FD0BF542D72ECEAD02EDC7E1465DBA5469F8A90D52C4139D7231C072AE96524735A680CA16DF88D1249F5AECD64B7707F265DB952002452764475CFE275FC40EB7B41727F6C35222");
		maps.put("MerCustId", "6000060001381865");
		maps.put("OrdId", "5000065");
		maps.put("CreditAmt", "5000.00");
		maps.put("SellCustId", "6000060001683655");
		maps.put("CmdId", "CreditAssign");
		submitForm(url,maps);
		
	}
	
	/**
	 * 投资补单
	 */
	@Test
	public void initiativeTender_repair(){
		String url = "http://localhost:9000/payment/chinapnr/initiativeTenderAyns";
		Map<String,String> maps = new HashMap<String, String>();
		maps.put("UsrCustId", "6000060001742217");
		maps.put("BgRetUrl", "http://localhost:9000/payment/chinapnr/initiativeTenderAyns");
		maps.put("TransAmt", "1000.00");
		maps.put("FreezeOrdId", "50001171");
		maps.put("RespExt", "");
		maps.put("MerPriv", "");
		maps.put("RetUrl", "http://localhost:9000/payment/chinapnr/initiativeTender");
		maps.put("TrxId", "201507160000853427");
		maps.put("RespCode", "000");
		maps.put("time", "2015-07-16 18:35:32");
		maps.put("FreezeTrxId", "201507160001693393");
		maps.put("RespDesc", "成功");
		maps.put("OrdDate", "20150716");
		maps.put("ChkValue", "7931055E7598116CAE7E626F445D19CE954CE0F239B63B7A0FB1503C47243B2A3DBF5CF2283E4AA69905CF2ED1B44A3487CB9EBD7E294E1245A9DF87019365536CEB6AA10C11AFFF982D41F330F808AC60201A6E2A86CCED5D6A3CA40396C2EC8010561D6BA313849548358B765AD3C143AD02DB700873BD19775C1107D489E5");
		maps.put("MerCustId", "6000060001368461");
		maps.put("OrdId", "5000117");
		maps.put("CmdId", "InitiativeTender");
		maps.put("IsFreeze", "Y");
		submitForm(url, maps);
	}
	
	@Test
	public void advanceRepayment_repair(){
		String url = "http://localhost:9000/payment/chinapnr/usrAcctPayAyns";
		Map<String,String> maps = new HashMap<String, String>();
		maps.put("OrdId", "150717215182");
		maps.put("MerPriv", "ADVANCEREPAYMENT");
		submitForm(url, maps);
	}
	
	@Test
	public void returnDate(){
		Map<String,String> returnDate = PaymentLog.getReturnData(Long.valueOf("5000108"), new ErrorInfo());
		String realTransAmt = returnDate.get("RealTransAmt");
		String rtransAmt= returnDate.get("TransAmt");
	}
	 
	public static String list2Str(final List<String> list, final int nThreads) throws Exception {
		if (list == null || list.isEmpty()) {
			return null;
		}

		int len = 0;
		for (String str : list) {
			len += str.length();
		}
		StringBuffer ret = new StringBuffer(len);

		final int size = list.size();
		ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
		List<Future<String>> futures = new ArrayList<Future<String>>(nThreads);
		
		try {
			for (int i = 0; i < nThreads; i++) {
				final int j = i;
				Callable<String> task = new Callable<String>() {
					@Override
					public String call() throws Exception {
						
						int len = 0;
						for (int n = size / nThreads * j; n < size / nThreads * (j + 1); n++) {
							len += list.get(n).length();
						}
						
						StringBuffer sb = new StringBuffer(len);
						for (int n = size / nThreads * j; n < size / nThreads * (j + 1); n++) {
							sb.append(list.get(n));
						}
						return sb.toString();
					}
				};
				futures.add(executorService.submit(task));
			}

			for (Future<String> future : futures) {
				ret.append(future.get());
			}
		} finally {
			executorService.shutdown();
		}
		
		return ret.toString();
	}

	public static void main(String[] args)throws Exception{
		List<String> list = new ArrayList<String>();
		for(int i = 0 ; i<1000000;i++){
			list.add(new Random().nextInt(100000)+"");
		}
		long start = System.currentTimeMillis();
		System.out.println(list2Str(list, 20).length());
		long end = System.currentTimeMillis();
		System.out.println(end-start);
		
	}

}
