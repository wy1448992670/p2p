package utils;



/**
 * 错误参数
 * 
 * @author lzp
 * @version 6.0
 * @created 2014-3-21 下午7:52:38
 */

public class ErrorInfo {

	public String msg;
	public int code;
	public String FRIEND_INFO  = "亲、由于系统繁忙。";
	public String PROCESS_INFO = "系统已把错误信息发送到后台管理员,会尽快的处理,给您带来的不便,敬请原谅。";
	
	/**返回页面路径**/
	public String returnUrl;
	/**返回页面描述**/
	public String returnMsg;
	
	public ErrorInfo() {
		this.code = 0;
		this.msg = "";
	}

	public ErrorInfo(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public void clear() {
		this.code = 0;
		this.msg = "";
	}

	public void setWrongMsg(String msg) {
		this.code = -1;
		this.msg = msg;
	}
	
	@Override
	public String toString() {
		return "ErrorInfo [msg=" + msg + ", code=" + code + ", FRIEND_INFO= " + FRIEND_INFO + ", PROCESS_INFO=" + PROCESS_INFO + "]";
	}
	
	/**
	 * 构建错误信息
	 * @param error
	 * @param code
	 * @param msg
	 * @param returnUrl
	 * @param returnMsg
	 * @return
	 */
	public static ErrorInfo createError(ErrorInfo error, int code, String msg, String returnUrl, String returnMsg){		
		error.msg = msg;
		error.code = code;
		error.returnMsg = returnMsg;
		error.returnUrl = returnUrl;
		return error;
		
	}
	
}
