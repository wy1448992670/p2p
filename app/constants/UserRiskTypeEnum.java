package constants;

public enum UserRiskTypeEnum {
	SAFE(1,"安全型","保守型"),CONSERVATIVE(2,"保守型","稳健型"),STEADY(3,"稳健型","平衡型"),POSITIVE(4,"积极型","积极型"),AGGRESSIVE(5,"进取型","激进型");
	private int code;
	private String name;
	private String oldName;
	
	private UserRiskTypeEnum(int code, String name,String oldName) {
		this.code = code;
		this.name = name;
		this.oldName=oldName;
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
	
	public String getOldName() {
		return oldName;
	}

	public void setOldName(String oldName) {
		this.oldName = oldName;
	}
	/**
	 * <p>老版API(<1.2)根据名字获取枚举对象
	 * @param name
	 * @return
	 */
	public static UserRiskTypeEnum getByOldName(String name) {
		for (UserRiskTypeEnum item : UserRiskTypeEnum.values()) {
			if(item.oldName.equals(name)) {
				return item;
			}
		}
		
		return null;
	}
	/**
	 * <p>根据名字获取枚举对象
	 * @param name
	 * @return
	 */
	public static UserRiskTypeEnum get(String name) {
		for (UserRiskTypeEnum item : UserRiskTypeEnum.values()) {
			if(item.name.equals(name)) {
				return item;
			}
		}
		
		return null;
	}
	/**
	 * <p>根据编码获取枚举对象
	 * @param name
	 * @return
	 */
	public static UserRiskTypeEnum get(int code) {
		for (UserRiskTypeEnum item : UserRiskTypeEnum.values()) {
			if(item.code == code) {
				return item;
			}
		}
		
		return null;
	}
}
