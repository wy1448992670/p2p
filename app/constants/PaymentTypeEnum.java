package constants;

/**
 * 还款方式
 * @ClassName PaymentTypeEnum
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年11月1日 下午8:07:06
 * @version 1.0.0
 */
public enum PaymentTypeEnum {
	DEBX(1, "按月还款、等额本息"), DQHB(2, "按月付息、到期还本"), YCHK(3, "一次性还款");

	private final int code;
	private final String name;

	private PaymentTypeEnum(int code, String name) {
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
	public static PaymentTypeEnum fromCode(int code) {
		for (PaymentTypeEnum auditType : values()) {
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
