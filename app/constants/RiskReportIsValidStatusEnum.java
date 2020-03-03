package constants;


/**
 * @ClassName RiskReportStatusEnum
 * @Description TODO 摩羯 is_valid 状态
 * @author zj
 * @Date Jan 29, 2019 11:26:37 AM
 * @version 1.0.0
 */
public enum RiskReportIsValidStatusEnum {

	
	VALID(1, "有效"),
	INVALID(2, "无效");

	private final int code;
	private final String name;

	private RiskReportIsValidStatusEnum(int code, String name) {
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
	public static RiskReportIsValidStatusEnum fromCode(int code) {
		for (RiskReportIsValidStatusEnum auditType : values()) {
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
