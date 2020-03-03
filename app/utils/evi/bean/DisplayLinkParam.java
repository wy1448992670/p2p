package utils.evi.bean;

/***
 * @Description: 环节的业务数据中,传的参数名称与最终显示名称对应关系
 * @Team: 公有云技术支持小组
 * @Author: 天云小生
 * @Date: 2018年01月14日
 */
public class DisplayLinkParam {
	// 属性的显示名称
	private String displayName;
	// 属性的参数名称
	private String paramName;
	
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getParamName() {
		return paramName;
	}
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}
	

}
