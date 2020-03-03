package constants;

@Deprecated
/**
 * {@link t_new_product}
 */
public enum ProductEnum {
	XIN(1,"信亿贷"),
	FANG(2,"房亿贷"),
	NONG(3,"农亿贷"),
	YI(4,"亿美贷"),
	CHE(5,"车亿贷");
	
	private final int code;
	private final String name;
	
	private ProductEnum(int code, String name) {
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
    public static ProductEnum getEnumByCode(int code){
        for(ProductEnum product:values()){
            if(code== product.getCode()){
                return product;
            }
        }
        return  null;
    }
	
}
