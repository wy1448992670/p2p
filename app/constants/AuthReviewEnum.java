package constants;

/**
 * 
 * @author hyl
 */
public enum AuthReviewEnum {
	UNREVIEW("待审核",0), NOT_PASSED("审核不通过",1), PASSED("审核通过",2), RESET("已重置",3);
	
	private String name;
	private int code;

	private AuthReviewEnum(String name, int code) {
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
