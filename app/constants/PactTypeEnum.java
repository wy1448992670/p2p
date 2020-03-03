package constants;

public enum PactTypeEnum {
	QZSQ("QZSQ", "电子签章自动签署授权协议"), CJFWXY("CJFWXY", "出借人服务协议"), JKFWXY("JKFWXY", "借款人服务协议"),
	ZXGL("ZXGL", "咨询与管理服务协议"), JKXY("JKXY", "借款协议"),YMDFQFWFXY("YMDFQFWFXY","亿美贷分期服务费协议"),DKXY("DKXY","代扣协议");

	private final String code;
	private final String name;

	private PactTypeEnum(String code, String name) {
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
	public static PactTypeEnum fromCode(String code) {
		for (PactTypeEnum auditType : values()) {
			if (auditType.getCode() == code) {
				System.out.println(code + "======================================");
				return auditType;
			}
		}

		return null;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

}
