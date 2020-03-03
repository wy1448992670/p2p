package business;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import constants.PactTypeEnum;
import constants.PeriodUnitTypeEnum;
import play.Logger;
import play.Play;
import services.UserbankAccountService;
import utils.DateUtil;
import utils.NumberUtil;
import utils.evi.Evi;
import utils.tsign.TSign;

/**
 * 合同，协议 相关用具类
 * 
 * @ClassName PactTool
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年10月29日 下午3:16:27
 * @version 1.0.0
 */
public class PactTool {

	private static String BASE = Play.getFile("/").getAbsolutePath();
	private static String ROOT = "/public/userPact/";

	/**
	 * @Description 根据协议，合同类型，生成相关的编号
	 * @param pactType 协议类型
	 * @param bidId    标主键
	 * @param userId   用户主键
	 * @return 协议编号
	 * @author: zj
	 */
	public static String getPactNo(String pactType, Long bidId, Long userId) {
		Logger.info("pactType==================>" + pactType);
		Logger.info("bidId==================>" + bidId);
		Logger.info("userId==================>" + userId);
		if (PactTypeEnum.QZSQ.getCode().equals(pactType) || PactTypeEnum.CJFWXY.getCode().equals(pactType)
				|| PactTypeEnum.JKFWXY.getCode().equals(pactType)) {
			return pactType + "-" + userId + "-" + DateUtil.simple2(new Date());
		}
		if (PactTypeEnum.ZXGL.getCode().equals(pactType)) {
			return pactType + "-" + userId + "-J" + bidId + "-" + DateUtil.simple(new Date());
		}
		if (PactTypeEnum.JKXY.getCode().equals(pactType)) {
			return pactType + "-" + DateUtil.simple(new Date()) + "-J" + bidId + "-"
					+ NumberUtil.getNumberByZero(Invest.getInvestOrder(userId, bidId));
		}
		if (PactTypeEnum.YMDFQFWFXY.getCode().equals(pactType)) {
			return pactType + "-" + DateUtil.simple(new Date()) + "-J" + bidId + "-"
					+ NumberUtil.getNumberByZero(Invest.getInvestOrder(userId, bidId));
		}
		return "";
	}

	/**
	 * 
	 * @Description 签章工具
	 * @param srcPdfFile     pdf文件 完整路径
	 * @param signedFolder   pdf文件夹路径
	 * @param signedFileName 新的签章文件名
	 * @param user
	 * @param sceneName      存证名称
	 * @author: zj
	 * @return Map key1 :viewCertificateInfoUrl 存证id，key2：signResult value2: true 成功
	 *         false 失败
	 */
	public static Map<String, Object> signPdf(String srcPdfFile, String signedFolder, String pdfName, JSONObject user,
			String sceneName) {
		Logger.info("历史电子合同签章存证............................................");
		Map<String, Object> map = new HashMap<>();
		try {
			Logger.info("开始签章。。。。。。。。。。。。。。");
			try {
				try {
					File pf = new File(srcPdfFile);
					if (pf.exists()) {
						// 电子签章
						List<String> signServiceIds = TSign.doSignWithTemplateSealByStreamNew(srcPdfFile, signedFolder, pdfName,
								user);
						Logger.info("signServiceIds---------------" + signServiceIds);
						// 签章失败
						boolean flag = true;
						if (signServiceIds == null) {
							Logger.info("签章失败第一次---------------");
							flag = false;
							for (int i = 0; i < 2; i++) {
								signServiceIds = TSign.doSignWithTemplateSealByStreamNew(srcPdfFile, signedFolder, pdfName, user);
								if (signServiceIds != null) {
									break;
								}
								Logger.info("签章失败第" + (i + 2) + "次---------------");
							}
							if (signServiceIds == null) {

							} else {
								flag = true;
							}
						}
						map.put("signResult", flag);
						if (flag) {
							String signPdfPath = signedFolder + pdfName;
							File file = new File(signPdfPath);
							Logger.info("生成的签章pdf完整路径==============>" + signPdfPath);
							if (file.exists()) {
								Logger.info("开始处理存证---------------");
								// 存证,返回存证证明查看页面URL
								String viewCertificateInfoUrl = Evi.evi(signPdfPath, user, signServiceIds, sceneName);
								map.put("viewCertificateInfoUrl", viewCertificateInfoUrl);
							}
						}
						return map;
					} else {
						map.put("signResult", false);
						Logger.info("签章处理3次---------------");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * 此方法只可给协议卡代扣合同使用
	 * 
	 * @param srcPdfFile
	 * @param signedFolder
	 * @param pdfName
	 * @param user
	 * @return
	 * @author: zj
	 */
	public static Map<String, Object> signPdf(String srcPdfFile, String signedFolder, String pdfName, JSONObject user) {
		Logger.info("历史电子合同签章存证............................................");
		Map<String, Object> map = new HashMap<>();
		try {
			Logger.info("开始签章。。。。。。。。。。。。。。");
			try {
				try {
					File pf = new File(srcPdfFile);
					if (pf.exists()) {
						// 电子签章
						List<String> signServiceIds = TSign.doSignWithTemplateSealByStreamNew(srcPdfFile, signedFolder, pdfName,
								user);
						Logger.info("signServiceIds---------------" + signServiceIds);
						// 签章失败
						boolean flag = true;
						if (signServiceIds == null) {
							Logger.info("签章失败第一次---------------");
							flag = false;
							for (int i = 0; i < 2; i++) {
								signServiceIds = TSign.doSignWithTemplateSealByStreamNew(srcPdfFile, signedFolder, pdfName, user);
								if (signServiceIds != null) {
									break;
								}
								Logger.info("签章失败第" + (i + 2) + "次---------------");
							}
							if (signServiceIds == null) {
								//3次失败记录 不再处理
								UserbankAccountService.updateUserBank(user.getLongValue("userId"), user.getString("account"),
										"fail");
								Logger.info("================3次失败记录 不再处理===============");
							} else {
								flag = true;
							}
						}
						map.put("signResult", flag);
						return map;
					} else {
						map.put("signResult", false);
						Logger.info("签章处理3次---------------");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * 根据标的借款期限类型，返回实际借款天数
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param periodUnit
	 * @param period
	 * @return
	 * @author: zj
	 */
	public static int day(int periodUnit, int period) {
		if (PeriodUnitTypeEnum.YEAR.getCode() == periodUnit) {
			return period * 365;
		}
		if (PeriodUnitTypeEnum.MONTH.getCode() == periodUnit) {
			return period * 30;
		}
		if (PeriodUnitTypeEnum.DAY.getCode() == periodUnit) {
			return period;
		}
		return period;
	}

	/**
	 * @Description 得到实际的标期数
	 * @param periodUnit
	 * @param period
	 * @return
	 * @author: zj
	 */
	public static int factPeriod(int periodUnit, int period) {
		if (PeriodUnitTypeEnum.YEAR.getCode() == periodUnit) {
			return period * 12;
		}
		if (PeriodUnitTypeEnum.MONTH.getCode() == periodUnit) {
			return period;
		}
		if (PeriodUnitTypeEnum.DAY.getCode() == periodUnit) {
			return 1;
		}
		return period;
	}

	public static void main(String[] args) {
		Map<String, Object> map = new HashMap<>();
		System.out.println(map.get("name"));
	}
}
