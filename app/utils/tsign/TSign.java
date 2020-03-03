package utils.tsign;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.timevale.esign.sdk.tech.bean.result.AddSealResult;
import com.timevale.esign.sdk.tech.bean.result.FileDigestSignResult;

import business.Invest;
import business.User;
import constants.UserTypeEnum;
import play.Logger;
import utils.ErrorInfo;
import utils.tsign.eSign.SignHelper;
import utils.tsign.utils.FileHelper;

public class TSign {

	public static List<String> doSignWithTemplateSealByStream(String srcPdfFile, String signedFolder,
			String signedFileName, JSONObject json) {
		String borrName = json.getString("b_reality_name");
		String borrId = json.getString("b_id_number");
		String investName = json.getString("reality_name");
		String investId = json.getString("id_number");
		long id = json.getLongValue("id");
		int b_user_type = json.getIntValue("b_user_type");
		int i_user_type = json.getIntValue("i_user_type");
		String key1=json.getString("key1");//甲方key
		String key2=json.getString("key2");//乙方key
		

		Logger.info("签章传递的参数json:" + json.toJSONString());

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
			return null;
		}
		// 创建个人或者企业印章
		AddSealResult userPersonSealData1 = getAccountId(b_user_type, userPersonAccountId1);
		AddSealResult userPersonSealData2 = getAccountId(i_user_type, userPersonAccountId2);

		if (userPersonSealData1 == null || userPersonSealData2 == null) {
			return null;
		}

		List<String> signServiceIds = new ArrayList<String>();

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
				key1, b_weight);
		Logger.info("签章处理结果" + userPersonSignResult.getMsg());
		Logger.info("签章处理结果code" + userPersonSignResult.getErrCode());
		if (0 == userPersonSignResult.getErrCode()) {
			signServiceIds.add(userPersonSignResult.getSignServiceId());
			// 签署投资人
			userPersonSignResult = SignHelper.userPersonSignByStream(userPersonSignResult.getStream(),
					userPersonAccountId2, userPersonSealData2.getSealData(), key2, i_weight);
			Logger.info("签章处理结果" + userPersonSignResult.getMsg());
			Logger.info("签章处理结果code" + userPersonSignResult.getErrCode());
			// 所有签署完成,将最终签署后的文件流保存到本地
			if (0 == userPersonSignResult.getErrCode()) {
				signServiceIds.add(userPersonSignResult.getSignServiceId());
			SignHelper.saveSignedByStream(userPersonSignResult.getStream(), signedFolder, signedFileName);
			} else {
				 Invest.updatePact(id, new ErrorInfo());
			}
		} else {
			 Invest.updatePact(id, new ErrorInfo());
		}

		return signServiceIds;
	}

	
	/**
	 * 
	 * @Description 生成签章（新的协议使用）
	 * @param srcPdfFile      pdf文件  完整路径
	 * @param signedFolder  pdf文件夹路径
	 * @param signedFileName    新的签章文件名
	 * @param user
	 * @return
	 * @author: zj
	 */
	public static List<String> doSignWithTemplateSealByStreamNew(String srcPdfFile, String signedFolder,
			String signedFileName, JSONObject json) {
		String borrName = json.getString("b_reality_name");
		String borrId = json.getString("b_id_number");
		String investName = json.getString("reality_name");
		String investId = json.getString("id_number");
		long id = json.getLongValue("id");
		int b_user_type = json.getIntValue("b_user_type");
		int i_user_type = json.getIntValue("i_user_type");

		Logger.info("签章传递的参数user===================>:" + JSON.toJSONString(json));

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
			return null;
		}
		// 创建个人或者企业印章
		AddSealResult userPersonSealData1 = getAccountId(b_user_type, userPersonAccountId1);
		AddSealResult userPersonSealData2 = getAccountId(i_user_type, userPersonAccountId2);

		if (userPersonSealData1 == null || userPersonSealData2 == null) {
			return null;
		}

		List<String> signServiceIds = new ArrayList<String>();

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
				userPersonAccountId1, userPersonSealData1.getSealData(), json.getString("key1"), b_weight);
		Logger.info("借款人签章处理结果" + userPersonSignResult.getMsg());
		Logger.info("借款人签章处理结果code" + userPersonSignResult.getErrCode());
		if (0 == userPersonSignResult.getErrCode()) {
			signServiceIds.add(userPersonSignResult.getSignServiceId());
			// 签署投资人
			userPersonSignResult = SignHelper.userPersonSignByStream(userPersonSignResult.getStream(),
					userPersonAccountId2, userPersonSealData2.getSealData(), json.getString("key2"), i_weight);
			Logger.info("投资人签章处理结果" + userPersonSignResult.getMsg());
			Logger.info("投资人签章处理结果code" + userPersonSignResult.getErrCode());
			// 所有签署完成,将最终签署后的文件流保存到本地
			if (0 == userPersonSignResult.getErrCode()) {
				signServiceIds.add(userPersonSignResult.getSignServiceId());
				SignHelper.saveSignedByStream(userPersonSignResult.getStream(), signedFolder, signedFileName);
			} else {
			}
		} else {
		}

		return signServiceIds;
	}
	
	
	
	
	/**
	 * 根据用户类型 返回不同的印章类型
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param user_type
	 * @param accountId
	 * @return
	 * @author: zj
	 */
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
