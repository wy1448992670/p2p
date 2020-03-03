package constants;


/**
 * @ClassName RiskReportStatusEnum
 * @Description TODO 摩羯 status 状态
 * @author zj
 * @Date Jan 29, 2019 11:26:37 AM
 * @version 1.0.0
 */
public enum RiskReportStatusEnum {
	SUCCESS(1, "成功"),
	FAIL(2, "失败"),
	CREATE(3, "创建");

	private final int code;
	private final String name;

	private RiskReportStatusEnum(int code, String name) {
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
	public static RiskReportStatusEnum fromCode(int code) {
		for (RiskReportStatusEnum auditType : values()) {
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
