package constants;

/**
 * 优惠券类型
 * 
 * @ClassName AuditTypeEnum
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年7月17日 上午10:47:58
 * @version 1.0.0
 */
public enum CouponTypeEnum {
	/**
	 * 红包
	 */
	REDBAG(1, "红包"), 
	/**
	 * 加息券
	 */
	INTEREST_RATE_COUPONS(2, "加息券");

	private final int code;
	private final String name;

	private CouponTypeEnum(int code, String name) {
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
	public static CouponTypeEnum fromCode(int code) {
		for (CouponTypeEnum auditType : values()) {
			if (auditType.getCode() == code) {
				System.out.println("===="+code+"========");
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
