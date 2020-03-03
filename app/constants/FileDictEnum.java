package constants;


/**
 * @ClassName FileDictEnum
 * @Description  文件类型
 * @author zj
 * @Date 2019年1月8日 上午9:23:37
 * @version 1.0.0
 */
public enum FileDictEnum {
	SFZZM(1, "身份证正面"),
	SFZFM(2, "身份证反面"),
	ZJRL(3, "最佳人脸"),
	HTQJ(4,"活体全景照片"),
	HBJT(5, "花呗截图"),
	JBJT(6, "借呗截图"),
	TBSHDZ(7, "淘宝收货地址"),
	SRZM(8, "收入证明"),
	SBJF(9, "社保缴费基数截图"),
	XYKED(10, "信用卡额度截图"),
	DSRZ(11, "电商认证"),
	QT(13, "其它"),
	YYZP(14, "医院照片"),
	ZFHT(15, "租房合同"),
	YYZZ(16, "营业执照"),
	YLXKZ(17, "医疗许可证"),
	SSD(18, "手术单"),
	FRSFZ(19, "法人身份证"),
	DSZHSFZ(20, "对私账户身份证"),
	YHK(21, "银行卡"),
	HZXY(22, "合作协议"),
	TMT(23, "脱敏图");

	private final int code;
	private final String name;

	private FileDictEnum(int code, String name) {
		this.code = code;
		this.name = name;
	}

	/**
	 * 通过code 返回相应的枚举对象
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param code
	 * @return
	 * @author: zj
	 */
	public static FileDictEnum fromCode(int code) {
		for (FileDictEnum auditType : values()) {
			if (auditType.getCode() == code) {
				System.out.println(code+"======================================");
				return auditType;
			}
		}

		return null;
	}

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

}
