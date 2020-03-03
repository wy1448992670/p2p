/******************************************************************
 *
 *    Java Lib For Android, Powered By Shenzhen Jiuzhou.
 *
 *    Copyright (c) 2001-2014 Digital Telemedia Co.,Ltd
 *    http://www.d-telemedia.com/
 *
 *    Package:     utils.faceId
 *
 *    Filename:    FaceService.java
 *
 *    Description: TODO(用一句话描述该文件做什么)
 *
 *    Copyright:   Copyright (c) 2001-2014
 *
 *    Company:     Digital Telemedia Co.,Ltd
 *
 *    @author:     zj
 *
 *    @version:    1.0.0
 *
 *    Create at:   2018年12月24日 下午2:38:19
 *
 *    Revision:
 *
 *    2018年12月24日 下午2:38:19
 *        - first revision
 *
 *****************************************************************/
package utils.faceId;

import java.io.File;
import java.util.Map;

import play.Play;
import utils.faceId.utils.ToolUtil;

/**
 * @ClassName FaceService
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年12月24日 下午2:38:19
 * @version 1.0.0
 */
public class FaceService {

	/**
	 * 内部使用的身份验证接口
	 * 
	 * @param params key说明： filePath(需要验证的图片所在路径（相对路径）),<br>
	 *               fileName(需要验证的图片的名称)
	 * @return 响应 json格式的字符串
	 */
	public static String ocridcard(Map<String, Object> params) {
		String relativeFilePath = params.get("relativeFilePath").toString();
		// 文件所在的父路径
		File fileFloder = Play.getFile(ToolUtil.getParentPath(relativeFilePath));
		// 获取本地文件对象
		File file = new File(fileFloder, ToolUtil.getFileName(relativeFilePath));
		String responseStr = FaceDao.ocridcard(file);
		return responseStr;
	}

	/**
	 * @Description 人脸比对
	 * @param idCardNumber
	 * @param idCardName
	 * @param fileRelativePath 需要比对的 身份证正面照（事先已经存放到了本地）
	 * @return
	 * @author: zj
	 */
	public static String verify(String idCardNumber, String idCardName, String fileRelativePath) {
		// 文件所在的父路径
		File fileFloder = Play.getFile(ToolUtil.getParentPath(fileRelativePath));
		// 获取本地文件对象
		File file = new File(fileFloder, ToolUtil.getFileName(fileRelativePath));
		return FaceDao.verify(idCardNumber, idCardName, file);

	}

	/**
	 * @Description 活体比对验证
	 * @param idCardNumber
	 * @param idCardName
	 * @param delta
	 * @param imageBest
	 * @param imageEnv
	 * @return
	 * @author: zj
	 */
	public static String verifyLiving(String idCardNumber, String idCardName, String delta, File imageBest,
			File imageEnv) {
		String responseMsg = FaceDao.verifyLiving(idCardNumber, idCardName, delta, imageBest, imageEnv);
		return responseMsg;
	}
}
