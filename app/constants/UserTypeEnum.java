package constants;

import java.util.Arrays;
import java.util.List;

public enum UserTypeEnum {
	NONE(0,"未知"), PERSONAL(1,"个人用户"), COMPANY(2,"企业用户"),INDIVIDUAL(3,"个体工商户");
	private int code;
	private String name;
	
	private UserTypeEnum(int code,String name) {
		this.code = code;
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	 /**
     * 根据code获取去value
     * @param code
     * @return
     */
    public static UserTypeEnum getEnumByCode(int code){
        for(UserTypeEnum userType:values()){
            if(code==userType.getCode()){
                return userType;
            }
        }
        return  null;
    }
 
    public static List<UserTypeEnum> getEnumList(){
    	return Arrays.asList(values());
    }
    
    public static void main(String[] args) {
		for (UserTypeEnum type : UserTypeEnum.values()) {
			System.err.println(type.name);
		}
	}
}
