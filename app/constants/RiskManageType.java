package constants;

public enum RiskManageType {
	UNREVIEW("待审核",0), 
	NOT_PASSED("审核不通过",1), 
	PASSED("审核通过",2), 
	RESET("已重置",3);
	
	private String name;
	private int code;
	
	private RiskManageType(String name,int code) {
		this.code = code;
		this.name = name;
	}
	
}
