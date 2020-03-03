package constants;

/**
 * 标类型
 * @ClassName ProductTypeEnum
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年9月28日 下午2:31:47
 * @version 1.0.0
 */
public enum ProductTypeEnum {
	UN_AUDIT(1, "未审核"), FIRST_AUDIT_PASS(2, "初审通过"), FIRST_AUDIT_NO_PASS(3, "初审未通过"), REVIEW_PASS(4,
			"复审通过"), REVIEW_NO_PASS(5, "复审未通过");

	private final int code;
	private final String name;

	private ProductTypeEnum(int code, String name) {
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
	public static ProductTypeEnum fromCode(int code) {
		for (ProductTypeEnum auditType : values()) {
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
