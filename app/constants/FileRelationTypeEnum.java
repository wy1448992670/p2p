package constants;


/**
 * @ClassName FileRelationTypeEnum
 * @Description 文件关系类型
 * @author zj
 * @Date 2019年1月8日 上午9:37:03
 * @version 1.0.0
 */
public enum FileRelationTypeEnum {
	IDCARD_OCR(1, "身份证照片实名认证 "),
	VERIFYLIVING(2, "活体认证"),
	FIRST_AUDIT_NO_PASS(3, "用户风控资料"),
	CREDIT_APPLY(4,"下单申请"),
	INCREASE_CREDIT_APPLY (5, "增加额度申请过"),
	BORROW_APPLY(6, "借款申请"),
	ENTERPRISE_USER_PROFILE(7, "企业用户资料"),
	ORGNIZATION(8, "合作机构代码");

	private final int code;
	private final String name;

	private FileRelationTypeEnum(int code, String name) {
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
	public static FileRelationTypeEnum fromCode(int code) {
		for (FileRelationTypeEnum auditType : values()) {
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
