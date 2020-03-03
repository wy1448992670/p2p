/*
 * @(#)Pact.java 2017年5月23日上午11:36:10
 * Copyright 2015 Sopell, Inc. All rights reserved.
 */
package business;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;

import constants.Constants;
import constants.PactTypeEnum;
import constants.PaymentTypeEnum;
import constants.PeriodUnitTypeEnum;
import constants.UserTypeEnum;
import models.core.t_new_product;
import play.Logger;
import play.Play;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.NumberToCN;
import utils.evi.Evi;
import utils.tsign.TSign;
import utils.word.OfficeToPDF;
import utils.word.WordPOI;

/**
 * @description.
 * 
 * @modificationHistory.
 * 
 * @author liulj 2017年5月23日上午11:36:10 TODO
 */

public class Pact {

	private static String BASE = Play.getFile("/").getAbsolutePath();
	private static String ROOT = "/public/pact/";
	/** 合同模板 默认 房易贷到期还本模板 **/
	private static String PACT_DOC = BASE.concat(ROOT).concat("FYD-JKXY.doc");
	/** 合同名称-word **/
	private static String PACT_WORD = "/%s.doc";
	private static String ID_CARDNAME = "身份证号码";
	private static String BUSINESS_LICENSE_NO = "营业执照号";
	private static String NO_NAME = "身份证号码或营业执照号";
	private static String BID_TYPE_NAME = "亿美贷";

	public static void doJob(Long userId, Long bidId, int length) {

		try {
			Logger.info("默认合同模板===============>" + PACT_DOC);
			// 查询用户已放款的投资账单
			List<Map<String, Object>> invests = User.findUserInvests(userId, bidId, length);

			if (invests != null) {
				for (Map<String, Object> invest : invests) {
					JSONObject investJson = new JSONObject(invest);

					// 处理自动签章协议等操作
					try {
						// 电子签章自动签署授权协议(投资人，出借人)
						NewPact.createPact(investJson.getLongValue("user_id"), null, PactTypeEnum.QZSQ.getCode(), 1);
						Logger.info(investJson.getLongValue("user_id") + "=====电子签章自动签署授权协议(投资人，出借人)完成========");
						// 电子签章自动签署授权协议(上标成功，借款人)
						NewPact.createPact(investJson.getLongValue("b_user_id"), null, PactTypeEnum.QZSQ.getCode(), 1);
						Logger.info(investJson.getLongValue("b_user_id") + "=====电子签章自动签署授权协议(上标成功，借款人)完成========");
						// 出借人服务协议（投资成功，出借人）
						NewPact.createPact(investJson.getLongValue("user_id"), null, PactTypeEnum.CJFWXY.getCode(), 1);
						Logger.info(investJson.getLongValue("user_id") + "=====出借人服务协议（投资成功，出借人）完成========");
						// 借款人服务协议(上标成功，借款人)
						NewPact.createPact(investJson.getLongValue("b_user_id"), null, PactTypeEnum.JKFWXY.getCode(), 1);
						Logger.info(investJson.getLongValue("b_user_id") + "=====借款人服务协议(上标成功，借款人)完成========");

						// 咨询与管理服务协议(放款成功，借款人 ，此功能去掉了)
						/*
						 * NewPact.createPact(investJson.getLongValue("b_user_id"),
						 * investJson.getLongValue("bid_id"), PactTypeEnum.ZXGL.getCode(), 1);
						 * Logger.info(investJson.getLongValue("bid_id") +
						 * "=====咨询与管理服务协议(放款成功，借款人)完成========");
						 */
					} catch (Exception e) {
						Logger.error("============协议合同生成出错=========");
						e.printStackTrace();
					}

					
					try {
						if (BID_TYPE_NAME.equals(investJson.getString("tag"))) {// 如果是亿美贷 需要加上服务费
							PactYmd.createPact(investJson.getLongValue("b_user_id"), investJson.getLongValue("bid_id"),
									PactTypeEnum.YMDFQFWFXY.getCode());
						}
					} catch (Exception e) {
						e.printStackTrace();
						Logger.error("=============亿美贷借款服务费协议合同生成出错================");
					}

					Logger.info("查询用户已放款的投资账单==============>" + investJson);
					// 得到每笔投资账单得还款金额
					BillInvests billInvests = BillInvests.findBillInfo(investJson.getIntValue("invest_id"),
							investJson.getIntValue("repayment_type_id"));
					// 合同编号
					String pact_no = String.format("%s-%s-%03d",
							DateUtil.dateFormat(investJson.getDate("audit_time"), "yyyy-MM-dd"),
							"J" + investJson.getLongValue("bid_id"), investJson.getIntValue("ser"));
					// 标编码
					String pactFolder = "J" + investJson.getLongValue("bid_id");
					// 合同文件名
					String docName = String.format(PACT_WORD, pact_no);

					// word版电子合同路径
					String investDocPath = BASE.concat(ROOT).concat(pactFolder).concat(docName);

					// 判断文件合同文件是否存在
					File file = new File(BASE.concat(ROOT).concat(pactFolder));
					if (!file.exists()) {
						file.mkdir();
					}

					String b_id_number = investJson.getString("b_id_number");
					// b_id_number = StringUtils.replace(b_id_number,
					// StringUtils.substring(b_id_number, 6, 14), "******");

					Map<String, String> map = new HashMap<String, String>();
					if (UserTypeEnum.PERSONAL.getCode() == investJson.getIntValue("b_user_type")) {
						map.put("${BORR_NO_NAME}", ID_CARDNAME);
					} else if (UserTypeEnum.COMPANY.getCode() == investJson.getIntValue("b_user_type")
							|| UserTypeEnum.INDIVIDUAL.getCode() == investJson.getIntValue("b_user_type")) {
						map.put("${BORR_NO_NAME}", BUSINESS_LICENSE_NO);
					} else {
						map.put("${BORR_NO_NAME}", NO_NAME);
					}

					if (UserTypeEnum.PERSONAL.getCode() == investJson.getIntValue("i_user_type")) {
						map.put("${INVEST_NO_NAME}", ID_CARDNAME);
					} else if (UserTypeEnum.COMPANY.getCode() == investJson.getIntValue("i_user_type")
							|| UserTypeEnum.INDIVIDUAL.getCode() == investJson.getIntValue("i_user_type")) {
						map.put("${INVEST_NO_NAME}", BUSINESS_LICENSE_NO);
					} else {
						map.put("${INVEST_NO_NAME}", NO_NAME);
					}
					map.put("${PACT_NO}", pact_no);
					map.put("${BORR_NAME}", investJson.getString("b_reality_name"));
					map.put("${BORR_IDCARD}", b_id_number);
					map.put("${BORR_MOBILE}", investJson.getString("borr_mobile"));
					map.put("${BORR_ACCOUNT}", investJson.getString("b_name"));

					map.put("${INVEST_NAME}", investJson.getString("reality_name"));
					map.put("${INVEST_IDCARD}", investJson.getString("id_number"));
					map.put("${INVEST_MOBILE}", investJson.getString("invest_mobile"));
					map.put("${INVEST_ACCOUNT}", investJson.getString("name"));
					map.put("${SUM_NUMBER}", investJson.getString("correct_amount"));
					map.put("${SUM_TXT}", NumberToCN.change(investJson.getDoubleValue("correct_amount")));
					map.put("${INTEREST}", Double.valueOf(investJson.getString("apr")).toString());
					map.put("${START_YEAR}", DateUtil.dateFormat(investJson.getDate("audit_time"), "yyyy"));
					map.put("${START_MONTH}", DateUtil.dateFormat(investJson.getDate("audit_time"), "MM"));
					map.put("${START_DAY}", DateUtil.dateFormat(investJson.getDate("audit_time"), "dd"));
					map.put("${END_YEAR}", DateUtil.dateFormat(investJson.getDate("last_repayment_time"), "yyyy"));
					map.put("${END_MONTH}", DateUtil.dateFormat(investJson.getDate("last_repayment_time"), "MM"));

					map.put("${USE}", investJson.getString("dlp_name"));
					map.put("${REPAY_TYPE}", investJson.getString("y"));
					map.put("${REPAY_DAY}", investJson.getString("dd"));
					map.put("${YEAR}", DateUtil.getYear() + "");
					map.put("${MONTH}", DateUtil.getMonth() + "");
					map.put("${DAY}", DateUtil.getDay() + "");
					map.put("${COUNT_MONTH}", investJson.getString("period"));
					map.put("${PERIOD_UNIT}", investJson.getString("period_unit_name"));

					// 实际期数
					int factPeriod = PactTool.factPeriod(investJson.getIntValue("period_unit"), investJson.getIntValue("period"));
					map.put("${PERIOD}", factPeriod + "");
					map.put("${PAYTYPE_NUM}", paymentTypeNum(investJson.getIntValue("repayment_type_id"),
							investJson.getString("period_unit_name")));
					map.put("${END_DAY}", DateUtil.dateFormat(investJson.getDate("last_repayment_time"), "dd"));

					if (PaymentTypeEnum.DEBX.getCode() == investJson.getIntValue("repayment_type_id")) {
						map.put("${PAYMENTTYPENAME}", PaymentTypeEnum.DEBX.getName());
					}
					if (PaymentTypeEnum.YCHK.getCode() == investJson.getIntValue("repayment_type_id")) {
						map.put("${PAYMENTTYPENAME}", PaymentTypeEnum.YCHK.getName());
					}
					if (PaymentTypeEnum.DQHB.getCode() == investJson.getIntValue("repayment_type_id")) {
						map.put("${PAYMENTTYPENAME}", PaymentTypeEnum.DQHB.getName());
					}

					map.put("${PER_REPAYMENT_1}", "/");
					map.put("${PER_TXT_1}", "/");
					map.put("${PER_REPAYMENT_2}", "/");
					map.put("${PER_TXT_2}", "/");
					map.put("${PER_REPAYMENT_3}", "/");
					map.put("${PER_TXT_3}", "/");
					map.put("${END_DAY_3}", "/");
					map.put("${END_DAY_4}", "/");
					map.put("${PER_REPAYMENT_4}", "/");
					map.put("${FINAL_REPAYMENT_4}", "/");
					map.put("${FINAL_TXT_4}", "/");
					map.put("${PER_TXT_4}", "/");
					// if (ProductEnum.FANG.getName().equals(investJson.getString("tag"))) {// 房易贷
					t_new_product newProduct = new t_new_product().getEnumByCode("FANG");// 房易贷
					if (newProduct.name.equals(investJson.getString("tag"))) {// 房易贷
						// 一次性还款
						if (PaymentTypeEnum.YCHK.getCode() == investJson.getIntValue("repayment_type_id")) {
							if (PeriodUnitTypeEnum.DAY.getName().equals(investJson.getString("period_unit_name"))) {// 按天计息
								// 每期还款金额
								map.put("${PER_REPAYMENT_1}", billInvests.allAmount + "");
								// 每期还款金额大写
								map.put("${PER_TXT_1}", NumberToCN.change(billInvests.allAmount));
							} else {// 按月计息
									// 每期还款金额
								map.put("${PER_REPAYMENT_2}", billInvests.allAmount + "");
								// 每期还款金额大写
								map.put("${PER_TXT_2}", NumberToCN.change(billInvests.allAmount));
							}
							PACT_DOC = BASE.concat(ROOT).concat("FYD-JKXY.doc");
						} else if (PaymentTypeEnum.DEBX.getCode() == investJson.getIntValue("repayment_type_id")) {// 等额本息
							// 每期还款金额
							map.put("${PER_REPAYMENT_3}", billInvests.allAmount + "");
							// 每期还款金额大写

							if (BID_TYPE_NAME.equals(investJson.getString("tag"))) {// 如果是亿美贷 需要加上服务费
								map.put("${PER_TXT_3}", NumberToCN.change(billInvests.allAmount));
							}
							map.put("${PER_TXT_3}", NumberToCN.change(billInvests.allAmount));
							map.put("${END_DAY_3}", DateUtil.dateFormat(investJson.getDate("last_repayment_time"), "dd"));
							PACT_DOC = BASE.concat(ROOT).concat("FYD-JKXY.doc");
						} else {// 到期还本
							map.put("${END_DAY_4}", DateUtil.dateFormat(investJson.getDate("last_repayment_time"), "dd"));
							// 每期还款金额
							map.put("${PER_REPAYMENT_4}", billInvests.receiveInterest + "");
							// 到期还本金额
							map.put("${FINAL_REPAYMENT_4}", billInvests.allAmount + billInvests.receiveInterest + "");
							// 到期还本金额大写
							map.put("${FINAL_TXT_4}", NumberToCN.change(billInvests.allAmount + billInvests.receiveInterest));
							// 每期还款金额大写
							map.put("${PER_TXT_4}", NumberToCN.change(billInvests.receiveInterest));
							PACT_DOC = BASE.concat(ROOT).concat("FYD-JKXY.doc");
						}
					} else {// 信易贷//亿美贷
						// 一次性还款
						if (PaymentTypeEnum.YCHK.getCode() == investJson.getIntValue("repayment_type_id")) {
							if (PeriodUnitTypeEnum.DAY.getName().equals(investJson.getString("period_unit_name"))) {// 按天计息
								// 每期还款金额
								map.put("${PER_REPAYMENT_1}", billInvests.allAmount + "");
								// 每期还款金额大写
								map.put("${PER_TXT_1}", NumberToCN.change(billInvests.allAmount));
							} else {// 按月计息
									// 每期还款金额
								map.put("${PER_REPAYMENT_2}", billInvests.allAmount + "");
								// 每期还款金额大写
								map.put("${PER_TXT_2}", NumberToCN.change(billInvests.allAmount));
							}
							PACT_DOC = BASE.concat(ROOT).concat("XYD-JKXY.doc");
						} else if (PaymentTypeEnum.DEBX.getCode() == investJson.getIntValue("repayment_type_id")) {// 等额本息
							// 每期还款金额
							map.put("${PER_REPAYMENT_3}", billInvests.allAmount + "");

								map.put("${PER_TXT_3}", NumberToCN.change(billInvests.allAmount));


							map.put("${END_DAY_3}", DateUtil.dateFormat(investJson.getDate("last_repayment_time"), "dd"));
							PACT_DOC = BASE.concat(ROOT).concat("XYD-JKXY.doc");
							if (BID_TYPE_NAME.equals(investJson.getString("tag"))) {// 如果是亿美贷借款，合同模板就要换成亿美贷的合同模板了
								PACT_DOC = BASE.concat(ROOT).concat("YMD-FQ-JKXY.doc");
							}
						} else {// 到期还本
							map.put("${END_DAY_4}", DateUtil.dateFormat(investJson.getDate("last_repayment_time"), "dd"));
							// 每期还款金额
							map.put("${PER_REPAYMENT_4}", billInvests.receiveInterest + "");
							// 到期还本金额
							map.put("${FINAL_REPAYMENT_4}", billInvests.allAmount + billInvests.receiveInterest + "");
							// 到期还本金额大写
							map.put("${FINAL_TXT_4}", NumberToCN.change(billInvests.allAmount + billInvests.receiveInterest));
							// 每期还款金额大写
							map.put("${PER_TXT_4}", NumberToCN.change(billInvests.receiveInterest));
							PACT_DOC = BASE.concat(ROOT).concat("XYD-JKXY.doc");
						}
					}
					try {
						Logger.info("使用的借款协议模板路径：==========>" + PACT_DOC);
						WordPOI.replaceAndGenerateWord(PACT_DOC, investDocPath, map);
						System.out.println("doc合同生成成功:" + investDocPath);
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						String investPdfPath = investDocPath.concat(".pdf");
						Logger.info("生成签章pdf的源文件路径：==========>" + investDocPath);
						Logger.info("生成签章pdf的目标路径：==========>" + investPdfPath);
						OfficeToPDF.office2PDF(investDocPath, investPdfPath);

						User.updateUserPact(investJson.getLongValue("invest_id"),
								ROOT.concat(pactFolder).concat(docName).concat(".pdf"), null);
						System.out.println("PDF合同生成成功:" + investPdfPath);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void doHistoryJob(Long userId, Long bidId, int length, String sceneName) {
		Logger.info("历史电子合同签章存证。。。");

		try {
			// 查询用户已放款且已生成PDF和未存证的投资账单
			List<Map<String, Object>> invests = User.findUserHistoryInvests(userId, bidId, length);

			if (invests != null) {
				Logger.info("开始签章。。。。。。。。。。。。。。");
				for (Map<String, Object> invest : invests) {

					JSONObject investJson = new JSONObject(invest);
					investJson.put("key2", Constants.JIAFANG_KEY);
					investJson.put("key1", Constants.YIFANG_KEY);
					System.out.println(investJson);

					// 标编码
					String pactFolder = "J" + investJson.getLongValue("bid_id") + "/";

					String pactLocation = investJson.getString("pactLocation");
					Logger.info("pactLocation:" + pactLocation + "========================");
					String loc[] = pactLocation.split("/");
					String pdfName = loc[loc.length - 1];
					Logger.info("pdfName:" + pdfName + "========================");
					String docName = pdfName.substring(0, pdfName.length() - 4);
					Logger.info("docName:" + docName + "========================");
					// word版电子合同路径
					String investDocPath = BASE.concat(ROOT).concat(pactFolder).concat(docName);
					Logger.info("word版电子合同路径" + investDocPath);
					try {
						String investPdfPath = BASE.concat(pactLocation);
						String investSignPdfPath = investDocPath.concat(".sign.pdf");
						Logger.info("investSignPdfPath:" + investSignPdfPath + "========================");
						try {
							File pf = new File(investPdfPath);
							if (pf.exists()) {
								// 电子签章
								List<String> signServiceIds = TSign.doSignWithTemplateSealByStream(investPdfPath,
										BASE.concat(ROOT).concat(pactFolder), docName.concat(".sign.pdf"), investJson);
								Logger.info("signServiceIds---------------" + signServiceIds);
								// 签章失败
								boolean flag = true;
								if (signServiceIds == null) {
									Logger.info("签章失败第一次---------------");
									flag = false;
									for (int i = 0; i < 2; i++) {
										signServiceIds = TSign.doSignWithTemplateSealByStream(investPdfPath,
												BASE.concat(ROOT).concat(pactFolder), docName.concat(".sign.pdf"), investJson);
										if (signServiceIds != null) {
											break;
										}
										Logger.info("签章失败第" + (i + 2) + "次---------------");
									}
									if (signServiceIds == null) {
										// 失败3次，直接设置失败
										User.updateUserPact(investJson.getLongValue("invest_id"),
												ROOT.concat(pactFolder).concat(docName).concat(".pdf"), "fail");
									} else {
										flag = true;
									}
								}
								if (flag) {
									File file = new File(investSignPdfPath);
									if (file.exists()) {
										Logger.info("开始处理存证---------------");
										// 存证,返回存证证明查看页面URL
										String viewCertificateInfoUrl = Evi.evi(investSignPdfPath, investJson, signServiceIds,
												sceneName);
										if (StringUtils.isNotBlank(viewCertificateInfoUrl)) {
											User.updateUserPact(investJson.getLongValue("invest_id"),
													ROOT.concat(pactFolder).concat(docName).concat(".sign.pdf"),
													viewCertificateInfoUrl);
											System.out.println("PDF合同存证成功:" + investSignPdfPath);
										}
									}
								}
							} else {
								Logger.info("签章处理3次---------------");
								Invest.updatePact(investJson.getLongValue("id"), new ErrorInfo());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据还款方式 得到模板中的序号
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param paymentType
	 * @param periodUnitName
	 * @return
	 * @author: zj
	 */
	public static String paymentTypeNum(int paymentType, String periodUnitName) {
		if (PaymentTypeEnum.DEBX.getCode() == paymentType) {
			return "3";
		}
		if (PaymentTypeEnum.DQHB.getCode() == paymentType) {
			return "4";
		}
		if (PaymentTypeEnum.YCHK.getCode() == paymentType) {
			if (PeriodUnitTypeEnum.MONTH.getName().equals(periodUnitName)) {
				return "2";
			} else {
				return "1";
			}
		}

		return "";
	}

	public static void main(String[] args) {
	System.out.println(PACT_DOC);
	}
}
