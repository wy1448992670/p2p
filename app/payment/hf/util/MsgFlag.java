package payment.hf.util;

import java.io.Serializable;

/**
 * 消息
 * 
 * @author yangxuan
 * @date 2015年7月14日
 */
public class MsgFlag implements Serializable {

	private boolean sucess;
	private String msg;

	public boolean isSucess() {
		return sucess;
	}

	public void setSucess(boolean sucess) {
		this.sucess = sucess;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	private MsgFlag() {
		super();
	}

	
	public MsgFlag(boolean sucess, String msg) {
		super();
		this.sucess = sucess;
		this.msg = msg;
	}

}
