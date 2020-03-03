package constants;

/**
 * 
 * @author hyl
 */
public enum OperationTypeEnum {
	NOT_PASSED("审核不通过",0), PASSED("审核通过",1), RESET("重置",2);
	
	private String name;
	private int code;

	private OperationTypeEnum(String name, int code) {
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
}
