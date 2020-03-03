package constants;

/**
 * 借款期限单位类型
 * 
 * @ClassName AuditTypeEnum
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年7月17日 上午10:47:58
 * @version 1.0.0
 */
public enum PeriodUnitTypeEnum {
	YEAR(-1, "年"), MONTH(0, "月"), DAY(1, "日");

	private final int code;
	private final String name;

	private PeriodUnitTypeEnum(int code, String name) {
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
	public static PeriodUnitTypeEnum fromCode(int code) {
		for (PeriodUnitTypeEnum unitName : values()) {
			if (unitName.getCode() == code) {
				System.out.println(code + "======================================");
				return unitName;
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
