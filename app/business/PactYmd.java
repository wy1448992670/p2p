/*
 * @(#)Pact.java 2017年5月23日上午11:36:10
 * Copyright 2015 Sopell, Inc. All rights reserved.
 */
package business;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import constants.Constants;
import constants.PactTypeEnum;
import play.Logger;
import play.Play;
import utils.DateUtil;
import utils.EmptyUtil;
import utils.FileUtil;
import utils.MoneyUtil;
import utils.word.OfficeToPDF;
import utils.word.WordPOI;

/**
 * @ClassName PactYmd
 * @Description 亿美贷合同相关方法
 * @author zj
 * @Date Feb 25, 2019 11:56:10 AM
 * @version 1.0.0
 */
public class PactYmd {

	private static String BASE = Play.getFile("/").getAbsolutePath();
	private static String ROOT = "/public/userPact/";
	private static String ROOT_PATH = BASE.concat(ROOT);// 亿美贷合同协议模板文件前缀路径

	/**
	 * @Description 亿美贷借款服务费协议合同创建 及签章
	 * @param userId
	 * @param bidId
	 * @param pactType
	 * @throws Exception
	 * @author: zj
	 */
	public static void createPact(Long userId, Long bidId, String pactType) throws Exception {
		// 如果生成过合同 直接退出
		if (!Bid.check(bidId)) {
			return;
		}
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
		// 模板路径
		String PACT_TEMPLET = "";
		if (PactTypeEnum.YMDFQFWFXY.getCode().equals(pactType)) {// 亿美贷（分期）服务费协议
			PACT_TEMPLET = ROOT_PATH.concat("YMD-FQ-FWFXY.doc");
			sceneName = PactTypeEnum.YMDFQFWFXY.getName();
			jsonObject.put("key2", user.realityName); // 甲方签章关键词
			jsonObject.put("key1", Constants.GSMC); // 乙方签章关键词
			userPactLocation = ROOT.concat(pactFolder).concat("/").concat(pactNo).concat(".doc.pdf");
		}
		jsonObject.put("b_reality_name", Constants.GSMC);
		jsonObject.put("b_id_number", Constants.SHXYDM);
		jsonObject.put("reality_name", user.realityName);
		jsonObject.put("id_number", user.idNumber);
		jsonObject.put("b_user_type", 2);
		jsonObject.put("i_user_type", user.user_type);

		Logger.info("创建亿美贷（分期）服务费协议 线上 user:{}==========>" + JSON.toJSONString(user));

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
			int factPeriod = PactTool.factPeriod(bid.periodUnit, bid.period);// 实际期数
			map.put("${FACT_PERIOD}", factPeriod + "");
			map.put("${START_YEAR}", DateUtil.dateFormat(bid.first_repayment_time, "yyyy"));
			map.put("${START_MONTH}", DateUtil.dateFormat(bid.first_repayment_time, "MM"));
			map.put("${START_DAY}", DateUtil.dateFormat(bid.first_repayment_time, "dd"));
			map.put("${END_YEAR}", DateUtil.dateFormat(bid.last_repayment_time, "yyyy"));
			map.put("${END_MONTH}", DateUtil.dateFormat(bid.last_repayment_time, "MM"));
			map.put("${END_DAY}", DateUtil.dateFormat(bid.last_repayment_time, "dd"));
			if (bid.service_amount == null) {
				bid.service_amount = new BigDecimal(0);
			}
			map.put("${SERVICE_AMOUNT}", bid.service_amount.toString());
			map.put("${SERVICE_AMOUNT_CN}", MoneyUtil.number2CN(bid.service_amount));
			// 每期需要支付的服务费
			map.put("${SINGLE_SERVICE_AMOUNT}",
					bid.service_amount.divide(new BigDecimal(factPeriod), 2, BigDecimal.ROUND_HALF_UP).toString());
			map.put("${PER}", bid.per);
		}

		// 生成doc的路径 以及完整的文件名
		String investDocPath = ROOT_PATH.concat(pactFolder).concat("/").concat(pactNo).concat(".doc");

		// boolean existFlag = user.checkPact(userId, pactType);
		// 如果是更新用户信息，或者 用户协议合同路径为空，则需要重新生成协议合同，并且记录入库
		// if (2 == flag || (existFlag == false)) {
		try {
			FileUtil.mkDir(BASE.concat(ROOT).concat(pactFolder));
			WordPOI.replaceAndGenerateWord(PACT_TEMPLET, investDocPath, map);// 生成doc文件
			Logger.info("创建亿美贷（分期）服务费协议doc生成成功:=================>" + investDocPath);
			String investPdfPath = investDocPath.concat(".pdf");
			// doc 转为 pdf
			OfficeToPDF.office2PDF(investDocPath, investPdfPath);
			Logger.info("创建亿美贷（分期）服务费协PDF合同生成成功:" + investPdfPath);

			// 记录pdf路径到数据库
			User.updateUserNewPact(userId, bidId, autoPactLoaction, userPactLocation, userCertificateUrl, pactType);
			// 电子签章自动签署授权协议不做签章和存证
			if (!PactTypeEnum.QZSQ.getCode().equals(pactType)) {
				try {
					// pdf签章及存证。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。
					Map<String, Object> signResultMap = PactTool.signPdf(investPdfPath,
							BASE.concat(ROOT).concat(pactFolder).concat("/"), pactNo.concat(".doc").concat(".sign.pdf"),
							jsonObject, sceneName);

					// 存证成功后返回的id
					userCertificateUrl = EmptyUtil.obj2Str(signResultMap.get("viewCertificateInfoUrl"));
					// 签章结果
					boolean signResult = (Boolean) signResultMap.get("signResult");

					if (signResult) {// 签章成功
						autoPactLoaction = ROOT.concat(pactFolder).concat("/").concat(pactNo).concat(".doc").concat(".sign.pdf");
						userPactLocation = autoPactLoaction;
						User.updateUserNewPact(userId, bidId, autoPactLoaction, userPactLocation, userCertificateUrl, pactType);
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
		// }

	}
}
