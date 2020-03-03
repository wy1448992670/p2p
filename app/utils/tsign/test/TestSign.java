package utils.tsign.test;

import java.util.ArrayList;
import java.util.List;

import com.timevale.esign.sdk.tech.bean.result.AddSealResult;
import com.timevale.esign.sdk.tech.bean.result.FileDigestSignResult;

import utils.tsign.eSign.SignHelper;
import utils.tsign.utils.FileHelper;

public class TestSign {
	
	public static void main(String[] args) {
/*		// 待签署的PDF文件路径
		String srcPdfFile = "e:/sign/2016-07-06-J21-001.doc.pdf";
		// 最终签署后的PDF文件路径
		String signedFolder = "e:/sign/" + File.separator;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		
		// 最终签署后PDF文件名称
		String signedFileName = "signed_" + sdf.format(new Date()) + ".pdf";

		// 初始化项目，做全局使用，只初始化一次即可
		SignHelper.initProject();
		
//		SignHelper.deleteAccount("DE7AD07A7FC34CD68B79E407806C8235");
//		SignHelper.deleteAccount("95FB499CE6B947FD98368C96CA9C1C35");
		
		System.out.println("----<场景演示：使用标准的模板印章签署，签署人之间用文件二进制流传递>----");
		// 使用标准的模板印章签署
		doSignWithTemplateSealByStream(srcPdfFile, signedFolder, signedFileName,"龚宁媚","352201198712160025","刘建","510225197001126538");
*/	
	
/*	SignHelper.deleteAccount(SignHelper.getAccountInfoByIdNo("91371502MA3DHUQR57"));
	SignHelper.deleteAccount(SignHelper.getAccountInfoByIdNo("91370685MA3DK6E25E"));*/
	}
	
	public static List<String> doSignWithTemplateSealByStream(String srcPdfFile, String signedFolder, String signedFileName,
			String borrName,String borrId,String investName,String investId) {
		// 创建个人客户账户
		String userPersonAccountId1 = null;
		String userPersonAccountId2 =null;
		
//		String userPersonAccountId1 = SignHelper.addPersonAccount("许小阳","350582198809160016");
//		String userPersonAccountId2 = SignHelper.addPersonAccount("孟金","130406198807210611");
		// 创建个人印章
		AddSealResult userPersonSealData1 = SignHelper.addPersonTemplateSeal(userPersonAccountId1);
		AddSealResult userPersonSealData2 = SignHelper.addPersonTemplateSeal(userPersonAccountId2);
		
		List<String> signServiceIds = new ArrayList<String>();
		
		// 个人客户签署，签署方式：关键字定位,以文件流的方式传递pdf文档
		FileDigestSignResult userPersonSignResult = SignHelper.userPersonSignByStream(FileHelper.getBytes(srcPdfFile),
				userPersonAccountId1, userPersonSealData1.getSealData(),borrId,40 * borrName.length());
		if (0 == userPersonSignResult.getErrCode()) {
			signServiceIds.add(userPersonSignResult.getSignServiceId());
		}
		userPersonSignResult = SignHelper.userPersonSignByStream(userPersonSignResult.getStream(),
				userPersonAccountId2, userPersonSealData2.getSealData(),investId,40 * investName.length());
		
		// 所有签署完成,将最终签署后的文件流保存到本地
		if (0 == userPersonSignResult.getErrCode()) {
			signServiceIds.add(userPersonSignResult.getSignServiceId());
			SignHelper.saveSignedByStream(userPersonSignResult.getStream(), signedFolder, signedFileName);
		}
		return signServiceIds;
	}

}
