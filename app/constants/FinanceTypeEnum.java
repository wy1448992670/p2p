package constants;

/**
 * 用户理财类型
 * 
 * @ClassName AppVersionEnum
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年10月16日 下午3:15:28
 * @version 1.0.0
 */
public enum FinanceTypeEnum {
	BORROW(0, "借款人"), INVEST(1, "投资人");

	private final int code;
	private final String name;

	private FinanceTypeEnum(int code, String name) {
		this.code = code;
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

}
