package constants;

/**
 * 标的风险等级
 * 
 * @ClassName BidRiskTypeEnum
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年9月28日 下午3:12:49
 * @version 1.0.0
 */
public enum BidRiskTypeEnum {
	DFX(1, "低风险"), ZDFX(2, "中低风险"), ZFX(3, "中风险"), ZGFX(4, "中高风险"), GFX(5, "高风险");
	private int code;
	private String name;

	private BidRiskTypeEnum(int code, String name) {
		this.code = code;
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * <p>
	 * 根据名字获取枚举对象
	 * 
	 * @param name
	 * @return
	 */
	public static BidRiskTypeEnum get(String name) {
		for (BidRiskTypeEnum item : BidRiskTypeEnum.values()) {
			if (item.name.equals(name)) {
				return item;
			}
		}

		return null;
	}

	/**
	 * <p>
	 * 根据编码获取枚举对象
	 * 
	 * @param name
	 * @return
	 */
	public static BidRiskTypeEnum get(int code) {
		for (BidRiskTypeEnum item : BidRiskTypeEnum.values()) {
			if (item.code == code) {
				return item;
			}
		}

		return null;
	}
}
