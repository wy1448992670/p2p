package utils.faceId;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import play.Logger;
import play.Play;
import play.libs.IO;
import utils.net.HttpUtil;

/**
 * Face++ 人脸识别相关工具类
 * 
 * @author Administrator
 *
 */
public class FaceDao {
	static String year = "";
	static String month = "";
	static String day = "";
	private static String BASE = Play.getFile("/").getAbsolutePath();
	/**
	 * @Field @COMPARISON_TYPE : 有源比对 “有源比对”或“无源比对”。取值只为“1”或“0”，取其他值返回错误码400
	 */
	static String COMPARISON_TYPE = "1";

	/**
	 * @Field @FACE_IMAGE_TYPE :
	 *        确定待比对图片的类型。取值只为“meglive”、“facetoken”、“raw_image”、“meglive_flash”
	 *        四者之一，取其他值返回错误码400（BAD_ARGUMENTS）。 “meglive”，表示本次比对照为从FaceID活体检测SDK
	 *        MegLive 中生成的最优照片。以下的“比对图片 四选一”参数组里，必须使用“配合MegLive
	 *        SDK使用时”这一组参数，否则可能返回错误码400（MISSING_ARGUMENTS）。
	 *        “facetoken”，表示本次比对时，用户已经调用了FaceID的detect方法从原始图片中检测出人脸，用一个facetoken表示，然后将这个facetoken作为待比对的人脸。以下的“比对图片
	 *        四选一””参数组里，必须使用“调用detect后获得facetoken时”这一组参数，否则可能返回错误码400（MISSING_ARGUMENTS）。
	 *        “raw_image”，表示本次比对时，用户直接上传一张可能包含人脸的图片作为待比对的图片，FaceID将先检测图中人脸然后将人脸与参照脸比对。以下的“比对图片
	 *        四选一””参数组里，必须使用“直接上传一张照片时”这一组参数，否则可能返回错误码400（MISSING_ARGUMENTS
	 *        “meglive_flash”，表示本次比对的照片为从 FaceID 炫彩活体 SDK MegLiveFlash
	 *        中生成的照片。以下的“四选一”参数组里，必须使用“配合MegLiveFlash
	 *        SDK使用时”这一组参数，否则可能返回错误码400（MISSING_ARGUMENTS）。
	 */
	static String FACE_IMAGE_TYPE = "raw_image";
	// 活体检测使用
	static String FACE_IMAGE_TYPE_LIVING = "meglive";

	/**
	 * @Field @FACE_QUALITY_THRESHOLD : 验证照中（最大的一张）人脸图像质量分的阈值（缺省值为30）。<br>
	 *        验证照人脸图像质量低于该阈值就直接返回错误码400（LOW_QUALITY）。本参数只能传入0至100的整数，<br>
	 *        传入其他整数或非整数字符串均返回错误码400（BAD_ARGUMENTS）。
	 */
	static String FACE_QUALITY_THRESHOLD = "30";

	/**
	 * @Field @MULTI_ORIENTED_DETECTION : 对image参数和image_ref[x]参数启用图片旋转检测功能。<br>
	 *        当image参数或image_ref[x]参数中传入的图片未检测到人脸时，是否对图片尝试旋转90度、180度、270度后再检测人脸。本参数取值只能是
	 *        “1” 或 "0" （缺省值为“0”）: “1”：启用旋转检测（启用旋转检测后，会增加API的调用时间） “0”：不启用旋转检测
	 * 
	 *        其他值：返回错误码400（BAD_ARGUMENTS）
	 * 
	 */
	static String MULTI_ORIENTED_DETECTION = "1";

	/**
	 * @Description 接收app传过来的图片保存到本地服务器
	 * @param file
	 * @author: zj
	 * @return 图片相对路径
	 */
	public static String saveFile(File file) {
		String fileName = file.getName();
		Logger.info("文件名称========>" + fileName);
		String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1);
		Logger.info("文件后缀名========>" + fileExt);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		// 新图片名称
		String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + fileExt;
		Logger.info("新文件名称==========>"+newFileName);
		year = sdf.format(new Date());
		sdf = new SimpleDateFormat("MM");
		month = sdf.format(new Date());
		sdf = new SimpleDateFormat("dd");
		day = sdf.format(new Date());
		
		//图片的相对路径
		String imagePathPrefix =Play.configuration.getProperty("image.path.prefix");
		
		////图片的绝对路径
		String imagePathPrefix_absolute =BASE.concat(Play.configuration.getProperty("image.path.prefix"));
		
		File fileFloder = new File(imagePathPrefix_absolute + year + "/" + month + "/" + day);
		if (!fileFloder.exists()) {// 如果文件夹不存在
			fileFloder.mkdirs();// 创建文件夹
		}

		//绝对父路径
		String parentPath_a = imagePathPrefix_absolute + year + "/" + month + "/" + day;
		
		//相对父路径
		String parentPath_r = imagePathPrefix + year + "/" + month + "/" + day;
		
		System.out.println("parentPath========>"+parentPath_a);
		System.out.println("file========>"+file);
		// 输出对象
		File outImageFile = new File(parentPath_a, newFileName);
		System.out.println("=============="+System.getProperty("user.dir"));
		try {
			// 输入对象
			FileInputStream fis = new FileInputStream(file);
			System.out.println("fis========>"+fis);
			System.out.println("outImageFile========>"+outImageFile);
			IO.write(fis, outImageFile);
			return "/" + parentPath_r + "/" + newFileName;
		} catch (Exception e) {
			throw new RuntimeException("图片上传错误=========>" + e.getMessage());
		}
	}

	/**
	 * 证件识别（身份证）
	 * 
	 * @param apiKey    用于验证客户身份的API Key；对于每一个客户此字段不会变更，相当于用户名。
	 * @param apiSecret 用于验证客户身份的API Secret；对于每一个客户可以申请变更此字段，相当于密码。
	 * @param image     一个图片，二进制文件，需要用Post Multipart/Form-Data的方式上传。<br>
	 *                  注：图片的文件大小小于2MB。支持的图片最小是400x400像素，最大是4096x4096像素。
	 * @return json格式字符串
	 */
	public static String ocridcard(File image) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("api_key", Play.configuration.getProperty("face.appkey"));
		params.put("api_secret", Play.configuration.getProperty("face.appSecret"));
		params.put("image", image);
		String apiUrl = Play.configuration.getProperty("face.ocridcard.url");
		String responseMsg = HttpUtil.doPostHasFile(apiUrl, params);
		Logger.info("人脸比对结果==========>" + responseMsg);
		return responseMsg;
	}

	/**
	 * @Description 人脸比对
	 * @param idCardNumber 卡号
	 * @param idCardName   姓名
	 * @param image        身份证正面
	 * @return
	 * @author: zj
	 */
	public static String verify(String idCardNumber, String idCardName, File image) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("api_key", Play.configuration.getProperty("face.appkey"));
		params.put("api_secret", Play.configuration.getProperty("face.appSecret"));
		params.put("idcard_name", idCardName);
		params.put("idcard_number", idCardNumber);
		params.put("comparison_type", COMPARISON_TYPE);
		params.put("face_image_type", FACE_IMAGE_TYPE);
		params.put("face_quality_threshold", FACE_QUALITY_THRESHOLD);
		params.put("multi_oriented_detection", MULTI_ORIENTED_DETECTION);
		params.put("image", image);
		String apiUrl = Play.configuration.getProperty("face.verify.url");
		String responseMsg = HttpUtil.doPostHasFile(apiUrl, params);
		Logger.info("人脸比对结果==========>" + responseMsg);
		return responseMsg;
	}

	/**
	 * @Description 活体身份验证
	 * @param idCardNumber
	 * @param idCardName
	 * @param delta        在配合MegLive SDK使用时，用于校验上传数据的校验字符串，此字符串会由MegLive SDK直接返回。
	 * @param imageBest    此参数请传MegLive SDK返回的质量最佳的人脸图片。
	 * @param imageEnv     用于假脸判定，请传MegLive
	 *                     SDK返回的用作云端假脸攻击判定的照片，FaceID将使用image_env进行假脸判定，<br>
	 *                     完整返回face_genuineness对象中的所有字段。注意：此参数需要MegLive SDK<br>
	 *                     2.4.1版本以及更新的版本配合支持，<br>
	 *                     低于2.4.1版本的MegLive SDK不返回这张照片。
	 * @return
	 * @author: zj
	 */
	public static String verifyLiving(String idCardNumber, String idCardName, String delta, File imageBest,
			File imageEnv) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("api_key", Play.configuration.getProperty("face.appkey"));
		params.put("api_secret", Play.configuration.getProperty("face.appSecret"));
		params.put("idcard_name", idCardName);
		params.put("idcard_number", idCardNumber);
		params.put("comparison_type", COMPARISON_TYPE);
		params.put("face_image_type", FACE_IMAGE_TYPE_LIVING);
		params.put("delta", delta);
		params.put("image_best", imageBest);
		params.put("image_env", imageEnv);
		String apiUrl = Play.configuration.getProperty("face.verify.url");
		String responseMsg = HttpUtil.doPostHasFile(apiUrl, params);
		Logger.info("活体身份验证结果==========>" + responseMsg);
		return responseMsg;
	}

	/**
	 * @Description 得到图片本地存放的位置
	 * @return
	 * @author: zj
	 */
	public static String getLocaFilePath() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		String year = sdf.format(new Date());
		sdf = new SimpleDateFormat("MM");
		String month = sdf.format(new Date());
		sdf = new SimpleDateFormat("dd");
		String day = sdf.format(new Date());
		return Play.configuration.getProperty("image.path.prefix2") + year + "/" + month + "/" + day;
	}
	
	public static void main(String[] args) {
		System.out.println(System.getProperty("user.dir"));
	}
}
