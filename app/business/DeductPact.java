
package business;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import constants.Constants;
import constants.PactTypeEnum;
import models.t_user_bank_accounts;
import models.t_users;
import play.Logger;
import play.Play;
import services.UserService;
import services.UserbankAccountService;
import utils.DateUtil;
import utils.FileUtil;
import utils.word.OfficeToPDF;
import utils.word.WordPOI;

/**
 * 代扣协议
 * 
 * @这里用一句话描述这个方法的作用
 * @author zj
 * @Date Apr 2, 2019 10:57:43 AM
 * @version 1.0.0
 */
@SuppressWarnings("rawtypes")
public class DeductPact {

	private static String BASE = Play.getFile("/").getAbsolutePath();
	private static String ROOT = "/public/userPact/";
	private static String ROOT_PATH = BASE.concat(ROOT);// 模板文件前缀路径

	public static void createDeductPact() throws Exception {
		// doc 模板完整路径
		String doc_template_url = ROOT_PATH.concat("DKXY.doc");
		String pactNo = "";// 合同号
		String pactFolder = "";
		JSONObject jsonObject = new JSONObject();

		// 查找出所有需要签章的代扣银行卡记录
		List<t_user_bank_accounts> list = UserbankAccountService.queryAllNeedDeductPact();

		Map<String, String> map = new HashMap<String, String>();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			t_user_bank_accounts t_user_bank_accounts = (t_user_bank_accounts) iterator.next();

			t_users users = UserService.getUserById(t_user_bank_accounts.user_id);

			pactFolder = "k" + t_user_bank_accounts.user_id;
			// 合同号
			pactNo = PactTypeEnum.DKXY.getCode().concat("-").concat(DateUtil.simple(new Date())).concat("-")
					.concat(t_user_bank_accounts.user_id + "-").concat(t_user_bank_accounts.account);
			map.put("${YEAR}", DateUtil.getYear() + "");
			map.put("${MONTH}", DateUtil.getMonth() + "");
			map.put("${DAY}", DateUtil.getDay() + "");
			map.put("${REALITY_NAME}", users.reality_name);
			map.put("${IDCARD}", users.id_number);
			map.put("${MOBILE}", users.mobile);
			map.put("${ACCOUTNAME}", t_user_bank_accounts.account_name);
			map.put("${CARDNO}", t_user_bank_accounts.account);
			map.put("${PACT_NO}", pactNo);

			jsonObject.put("key2", Constants.JIAFANG_KEY_XY); // 甲方签章关键词
			jsonObject.put("key1", Constants.YIFANG_KEY_XY); // 乙方签章关键词
			jsonObject.put("b_reality_name", Constants.GSMC);
			jsonObject.put("b_id_number", Constants.SHXYDM);
			jsonObject.put("reality_name", users.reality_name);
			jsonObject.put("id_number", users.id_number);
			jsonObject.put("b_user_type", 2);
			jsonObject.put("i_user_type", users.user_type);
			jsonObject.put("userId", users.id);
			jsonObject.put("account", t_user_bank_accounts.account);//卡号

			// 生成doc的路径 以及完整的文件名
			String destDocPath = ROOT_PATH.concat("k" + t_user_bank_accounts.user_id).concat("/").concat(pactNo).concat(".doc");

			// 每个有协议卡的用户创建文件夹
			FileUtil.mkDir(BASE.concat(ROOT).concat("k" + t_user_bank_accounts.user_id));
			// 生成合同 doc
			WordPOI.replaceAndGenerateWord(doc_template_url, destDocPath, map);
			Logger.info("代扣合同doc 生成成功:=================>" + destDocPath);

			// 将要生成的pdf合同的路径
			String pdfPath = destDocPath.concat(".pdf");
			// doc 转为 pdf
			OfficeToPDF.office2PDF(destDocPath, pdfPath);
			Logger.info("代扣合同pdf  生成成功:=================>" + pdfPath);
			// pdf签章。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。
			Map<String, Object> signResultMap = PactTool.signPdf(pdfPath, BASE.concat(ROOT).concat(pactFolder).concat("/"),
					pactNo.concat(".doc").concat(".sign.pdf"), jsonObject);

			// 签章结果
			boolean signResult = (Boolean) signResultMap.get("signResult");

			if (signResult) {// 签章成功 记录到表
				UserbankAccountService.updateUserBank(t_user_bank_accounts.user_id, t_user_bank_accounts.account,
						ROOT.concat(pactFolder).concat("/").concat(pactNo.concat(".doc").concat(".sign.pdf")));
			}
		}
	}
}
