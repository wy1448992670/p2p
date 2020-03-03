package constants;

/**
 * 债权标审核状态
 * 
 * @ClassName AuditTypeEnum
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年7月17日 上午10:47:58
 * @version 1.0.0
 */
public enum BidStatusEnum {
	SHZ(0, "审核中"), 
	TQJK(1, "提前借款"),
	CSWTG(2, "初审未通过"),
	DFK(3,"待放款"),
	HKZ(4, "还款中"),
	YHK(5, "已还款"),
	SHBTG(-1, "审核不通过"),
	JKZBTG(-2, "借款中不通过");

	private final int code;
	private final String name;

	private BidStatusEnum(int code, String name) {
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
	public static BidStatusEnum fromCode(int code) {
		for (BidStatusEnum auditType : values()) {
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
