package constants;

/**
 * 标类型编号前缀
 * 
 * @ClassName BidEnum
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年7月16日 下午1:23:36
 * @version 1.0.0
 */
public enum BidPreFixEnum {
	//转让标
	TRANSFER("Z", "债权转让标");
	// 成员变量
	private String code;
	private String name;

	// 构造方法
	private BidPreFixEnum(String name, String code) {
		this.name = name;
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
