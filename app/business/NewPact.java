
package business;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import constants.Constants;
import constants.PactTypeEnum;
import constants.UserTypeEnum;
import play.Logger;
import play.Play;
import utils.EmptyUtil;
import utils.FileUtil;
import utils.MoneyUtil;
import utils.word.OfficeToPDF;
import utils.word.WordPOI;

/**
 * 用户协议
 * 
 * @ClassName NewPact
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年10月29日 上午10:31:37
 * @version 1.0.0
 */
public class NewPact {

	private static String BASE = Play.getFile("/").getAbsolutePath();
	private static String ROOT = "/public/userPact/";
	private static String ROOT_PATH = BASE.concat(ROOT);// 模板文件前缀路径 

	/**
	 * 生成协议合同类
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param userId
	 * @param bidId
	 * @param pactType 协议类型 参见 PactTypeEnum 类的定义
	 * @param flag     操作类型 1新增 2修改
	 * @throws Exception
	 * @author: zj
	 */
	@SuppressWarnings({ "unused", "static-access" })
	public static void createPact(Long userId, Long bidId, String pactType, int flag) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("userId", userId);
		String autoPactLoaction = null;
		String userPactLocation = null;
		String userCertificateUrl = null;
		String sceneName = "";
		User user = User.findUser(userId);
		String pactFolder = user.pactFolder;
		// 生成对应的协议合同编号
		String pactNo = PactTool.getPactNo(pactType, bidId, userId);
		Logger.info("协议合同获取的user对象==============>" + JSON.toJSONString(user));
		// 模板路径
		String PACT_TEMPLET = "";
		if (PactTypeEnum.QZSQ.getCode().equals(pactType)) {
			PACT_TEMPLET = ROOT_PATH.concat("QZSQ.doc");
			sceneName = PactTypeEnum.QZSQ.getName();
			jsonObject.put("key2", user.realityName); // 甲方签章关键词
			jsonObject.put("key1", Constants.GSMC_KEY); // 乙方签章关键词
			autoPactLoaction = ROOT.concat(pactFolder).concat("/").concat(pactNo).concat(".doc.pdf");
		}
		if (PactTypeEnum.CJFWXY.getCode().equals(pactType)) {
			PACT_TEMPLET = ROOT_PATH.concat("CJFWXY.doc");
			sceneName = PactTypeEnum.CJFWXY.getName();
			jsonObject.put("key2", Constants.JIAFANG_KEY_XY); // 甲方签章关键词
			jsonObject.put("key1", Constants.YIFANG_KEY_XY); // 乙方签章关键词
			userPactLocation = ROOT.concat(pactFolder).concat("/").concat(pactNo).concat(".doc.pdf");
		}
		if (PactTypeEnum.JKFWXY.getCode().equals(pactType) && UserTypeEnum.PERSONAL.getCode() == user.user_type) {
			PACT_TEMPLET = ROOT_PATH.concat("JKFWXY-GR.doc");
			sceneName = PactTypeEnum.JKFWXY.getName();
			jsonObject.put("key2", Constants.JIAFANG_KEY_XY); // 甲方签章关键词
			jsonObject.put("key1", Constants.YIFANG_KEY_XY); // 乙方签章关键词
			userPactLocation = ROOT.concat(pactFolder).concat("/").concat(pactNo).concat(".doc.pdf");
		} else if (PactTypeEnum.JKFWXY.getCode().equals(pactType) && (UserTypeEnum.COMPANY.getCode() == user.user_type
				|| UserTypeEnum.INDIVIDUAL.getCode() == user.user_type)) {
			PACT_TEMPLET = ROOT_PATH.concat("JKFWXY-QY-GT.doc");
			sceneName = PactTypeEnum.JKFWXY.getName();
			jsonObject.put("key2", Constants.JIAFANG_KEY_XY); // 甲方签章关键词
			jsonObject.put("key1", Constants.YIFANG_KEY_XY); // 乙方签章关键词
			userPactLocation = ROOT.concat(pactFolder).concat("/").concat(pactNo).concat(".doc.pdf");
		}
		if (PactTypeEnum.ZXGL.getCode().equals(pactType) && UserTypeEnum.PERSONAL.getCode() == user.user_type) {
			PACT_TEMPLET = ROOT_PATH.concat("ZXGL-GR.doc");
			sceneName = PactTypeEnum.ZXGL.getName();
			jsonObject.put("key2", Constants.JIAFANG_KEY_XY); // 甲方签章关键词
			jsonObject.put("key1", Constants.YIFANG_KEY_XY); // 乙方签章关键词
			userPactLocation = ROOT.concat(pactFolder).concat("/").concat(pactNo).concat(".doc.pdf");
		} else if (PactTypeEnum.ZXGL.getCode().equals(pactType) && (UserTypeEnum.COMPANY.getCode() == user.user_type
				|| UserTypeEnum.INDIVIDUAL.getCode() == user.user_type)) {
			sceneName = PactTypeEnum.ZXGL.getName();
			PACT_TEMPLET = ROOT_PATH.concat("ZXGL-QY-GT.doc");
			jsonObject.put("key2", Constants.JIAFANG_KEY_XY); // 甲方签章关键词
			jsonObject.put("key1", Constants.YIFANG_KEY_XY); // 乙方签章关键词
			userPactLocation = ROOT.concat(pactFolder).concat("/").concat(pactNo).concat(".doc.pdf");
		}
		Logger.info("PACT_TEMPLET=============>" + PACT_TEMPLET);

		jsonObject.put("b_reality_name", Constants.GSMC);
		jsonObject.put("b_id_number", Constants.SHXYDM);
		jsonObject.put("reality_name", user.realityName);
		jsonObject.put("id_number", user.idNumber);
		jsonObject.put("b_user_type", 2);
		jsonObject.put("i_user_type", user.user_type);

		Logger.info("创建出借人服务协议doc user:{}==========>" + JSON.toJSONString(user));

		map.put("${PACT_NO}", pactNo);
		map.put("${YEAR}", user.year);
		map.put("${MONTH}", user.month);
		map.put("${DAY}", user.day);
		map.put("${BORROW_NAME}", user.realityName);
		map.put("${USER_NAME}", user.name);
		map.put("${ID_NUMBER}", user.idNumber);
		map.put("${CARD_NAME}", user.carName);
		map.put("${MOBILE}", user.mobile);
		map.put("${ADDRESS}", user.address);
		map.put("${LEGAL_PERSON}", user.legalPerson);

		// bidId不为空，说明需要获取标相关信息 ，协议里需要标相关金额等信息
		if (bidId != null) {
			Bid bid = Bid.findBIdInfoById(bidId);
			map.put("${AMOUNT}", bid.amount + "");
			map.put("${AMOUNTCN}", MoneyUtil.number2CN(new BigDecimal(bid.amount)));
			map.put("${PERIOD}", PactTool.day(bid.periodUnit, bid.period) + "");

			map.put("${PER}", bid.per);
		}

		// 生成doc的路径 以及完整的文件名
		String investDocPath = ROOT_PATH.concat(pactFolder).concat("/").concat(pactNo).concat(".doc");

		
		boolean existFlag = user.checkPact(userId, pactType);
		//如果是更新用户信息，或者 用户协议合同路径为空，则需要重新生成协议合同，并且记录入库
		if (2 == flag || (existFlag == false)) {
			try {
				FileUtil.mkDir(BASE.concat(ROOT).concat(pactFolder));
				WordPOI.replaceAndGenerateWord(PACT_TEMPLET, investDocPath, map);// 生成doc文件
				Logger.info("doc合同生成成功:=================>" + investDocPath);
				String investPdfPath = investDocPath.concat(".pdf");
				// doc 转为 pdf
				OfficeToPDF.office2PDF(investDocPath, investPdfPath);
				Logger.info("PDF合同生成成功:" + investPdfPath);

				// 记录pdf路径到数据库
				User.updateUserNewPact(userId, bidId, autoPactLoaction, userPactLocation, userCertificateUrl, pactType);
				// 电子签章自动签署授权协议不做签章和存证
				if (!PactTypeEnum.QZSQ.getCode().equals(pactType)) {
					try {
						// pdf签章及存证。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。
						Map<String, Object> signResultMap = PactTool.signPdf(investPdfPath,
								BASE.concat(ROOT).concat(pactFolder).concat("/"),
								pactNo.concat(".doc").concat(".sign.pdf"), jsonObject, sceneName);

						// 存证成功后返回的id
						userCertificateUrl = EmptyUtil.obj2Str(signResultMap.get("viewCertificateInfoUrl"));
						// 签章结果
						boolean signResult = (Boolean) signResultMap.get("signResult");

						if (signResult) {// 签章成功
							autoPactLoaction = ROOT.concat(pactFolder).concat("/").concat(pactNo).concat(".doc")
									.concat(".sign.pdf");
							userPactLocation = autoPactLoaction;
							User.updateUserNewPact(userId, bidId, autoPactLoaction, userPactLocation,
									userCertificateUrl, pactType);
						}
					} catch (Exception e) {
						if (StringUtils.isBlank(userCertificateUrl)) {
							Logger.info("存证出了问题");
						}
						Logger.error("签章出了问题");
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				Logger.error(e, e.getMessage());
			}
		}

	}

}
