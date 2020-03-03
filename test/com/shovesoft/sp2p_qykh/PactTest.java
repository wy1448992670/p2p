package com.shovesoft.sp2p_qykh;

import org.apache.commons.lang.StringUtils;

import com.timevale.esign.sdk.tech.bean.result.AddSealResult;
import com.timevale.esign.sdk.tech.bean.result.FileDigestSignResult;

import constants.UserTypeEnum;
import play.Logger;
import utils.tsign.eSign.SignHelper;
import utils.tsign.utils.FileHelper;

public class PactTest {
	public static void main(String[] args) {
		SignHelper.initProject();
		System.out.println("==================");
		
		String srcPdfFile="D:\\workspace\\yiyilc_qykh\\yiyilc_sp2p\\public\\pact\\J1552\\2018-10-23-J1552-010.doc.pdf";
		String signedFolder="D:\\workspace\\yiyilc_qykh\\yiyilc_sp2p\\public\\pact\\J1552\\";
		String signedFileName="2018-10-23-J1552-010.doc.sign.pdf";
		String borrName = "张旭";
		String borrId ="370406198408045051";
		String investName ="郑丽平";
		String investId = "370405198801034627";
	
		int b_user_type =1;
		int i_user_type =1;

		//Logger.info("签章传递的参数json:" + json.toJSONString());

		if (!StringUtils.isBlank(SignHelper.getAccountInfoByIdNo(borrId, b_user_type))) {
			SignHelper.deleteAccount(SignHelper.getAccountInfoByIdNo(borrId, b_user_type));
		}
		if (!StringUtils.isBlank(SignHelper.getAccountInfoByIdNo(investId, i_user_type))) {
			SignHelper.deleteAccount(SignHelper.getAccountInfoByIdNo(investId, i_user_type));
		}

		// 创建个人或者企业账户
		String userPersonAccountId1 = SignHelper.addPersonAccount(borrName, borrId, b_user_type);
		System.out.println(userPersonAccountId1 + "-------------------------");

		String userPersonAccountId2 = SignHelper.addPersonAccount(investName, investId, i_user_type);
		System.out.println(userPersonAccountId2 + "-------------------------------------");
		if (userPersonAccountId1 == null || userPersonAccountId2 == null) {
			
		}
		// 创建个人或者企业印章
		AddSealResult userPersonSealData1 = getAccountId(b_user_type, userPersonAccountId1);
		AddSealResult userPersonSealData2 = getAccountId(i_user_type, userPersonAccountId2);

		if (userPersonSealData1 == null || userPersonSealData2 == null) {
		
		}

	//	List<String> signServiceIds = new ArrayList<String>();

		// 个人客户签署，签署方式：关键字定位,以文件流的方式传递pdf文档
		// 签署借款人
		int b_weight = 0;
		int i_weight = 0;
		if (UserTypeEnum.PERSONAL.getCode() == b_user_type) {
			b_weight = 40 * borrName.length();
		} else {
			b_weight = 120;
		}
		if (UserTypeEnum.PERSONAL.getCode() == i_user_type) {
			i_weight = 40 * investName.length();
		} else {
			i_weight = 120;
		}
		FileDigestSignResult userPersonSignResult = SignHelper.userPersonSignByStream(FileHelper.getBytes(srcPdfFile),
				userPersonAccountId1, userPersonSealData1.getSealData(),
				borrId, b_weight);
		Logger.info("签章处理结果" + userPersonSignResult.getMsg());
		Logger.info("签章处理结果code" + userPersonSignResult.getErrCode());
		if (0 == userPersonSignResult.getErrCode()) {
		//	signServiceIds.add(userPersonSignResult.getSignServiceId());
			// 签署投资人
			userPersonSignResult = SignHelper.userPersonSignByStream(userPersonSignResult.getStream(),
					userPersonAccountId2, userPersonSealData2.getSealData(), investId, i_weight);
			Logger.info("签章处理结果" + userPersonSignResult.getMsg());
			Logger.info("签章处理结果code" + userPersonSignResult.getErrCode());
			// 所有签署完成,将最终签署后的文件流保存到本地
			if (0 == userPersonSignResult.getErrCode()) {
				//signServiceIds.add(userPersonSignResult.getSignServiceId());
			SignHelper.saveSignedByStream(userPersonSignResult.getStream(), signedFolder, signedFileName);
			} else {
				// Invest.updatePact(id, new ErrorInfo());
			}
		} else {
			// Invest.updatePact(id, new ErrorInfo());
		}

		
		
		
		
	}
	
	
	public static AddSealResult getAccountId(int user_type, String accountId) {
		if (UserTypeEnum.PERSONAL.getCode() == user_type) {
			AddSealResult addSealResult = SignHelper.addPersonTemplateSeal(accountId);
			return addSealResult;
		} else {
			AddSealResult addSealResult = SignHelper.addOrganizeTemplateSeal(accountId);
			return addSealResult;
		}

	}
}
