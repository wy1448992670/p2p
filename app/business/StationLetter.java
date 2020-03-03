package business;

import java.io.Serializable;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import com.shove.Convert;

import constants.SQLTempletes;
import constants.SupervisorEvent;
import constants.Templets;
import constants.UserEvent;
import constants.Constants.*;
import models.*;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.PageBean;
import utils.QueryUtil;

/**
 * 站内信
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-3-25 下午02:27:42
 */
public class StationLetter implements Serializable{

	public long id;
	private long _id = -1;

	public long senderUserId;
	public long senderSupervisorId;

	public long receiverUserId;
	public long receiverSupervisorId;
	
	public String senderUserName;
	public String senderSupervisorName;
	
	public String receiverUserName;
	public String receiverSupervisorName;
	
	private long _senderUserId;
	private long _senderSupervisorId;

	private long _receiverUserId;
	private long _receiverSupervisorId;
	
	private String _senderUserName;
	private String _senderSupervisorName;
	
	private String _receiverUserName;
	private String _receiverSupervisorName;

	public String title;
	public String content;
	public Date time;
	
	public String replyStatus;   //回复状态(详情页面显示)
	
	
	
	public StationLetter() {
		super();
	}
	
	public StationLetter(long id) {
		super();
		this.id = id;
	}

	public void setId(long id) {
		t_messages msg = null;

		try {
			msg = t_messages.findById(id);
		} catch (Exception e) {
			this._id = -1;
			e.printStackTrace();
			Logger.error(e.getMessage());

			return;
		}

		if (null == msg) {
			this._id = -1;

			return;
		}

		setInfomation(msg);
	}

	public long getId() {
		return _id;
	}

	/**
	 * 填充数据
	 * @param msg
	 */
	private void setInfomation(t_messages msg) {
		if (null == msg) {
			this._id = -1;

			return;
		}

		_id = msg.id;
		this.senderUserId = msg.sender_user_id;
		this.senderSupervisorId = msg.sender_supervisor_id;
		this.receiverUserId = msg.receiver_user_id;
		this.receiverSupervisorId = msg.receiver_supervisor_id;

		this.content = msg.content;
		this.title = msg.title;
		this.time = msg.time;
	}
	
	public long getSenderUserId() {
		return _senderUserId;
	}

	public void setSenderUserId(long senderUserId) {
		this._senderUserId = senderUserId;
		this._senderUserName = User.queryUserNameById(senderUserId, new ErrorInfo());
	}

	public long getSenderSupervisorId() {
		return _senderSupervisorId;
	}

	public void setSenderSupervisorId(long senderSupervisorId) {
		this._senderSupervisorId = senderSupervisorId;
		this._senderSupervisorName = Supervisor.queryNameById(senderSupervisorId, new ErrorInfo());
	}

	public long getReceiverUserId() {
		return _receiverUserId;
	}

	public void setReceiverUserId(long receiverUserId) {
		this._receiverUserId = receiverUserId;
		
		if (receiverUserId < 0) {
			return;
		}
		
		this._receiverUserName = User.queryUserNameById(receiverUserId, new ErrorInfo());
	}

	public long getReceiverSupervisorId() {
		return _receiverSupervisorId;
	}

	public void setReceiverSupervisorId(long receiverSupervisorId) {
		this._receiverSupervisorId = receiverSupervisorId;
		this._receiverSupervisorName = Supervisor.queryNameById(receiverSupervisorId, new ErrorInfo());
	}

	public String getSenderUserName() {
		return _senderUserName;
	}

	public void setSenderUserName(String senderUserName) {
		this._senderUserName = senderUserName;
		this._senderUserId = User.queryIdByUserName(senderUserName, new ErrorInfo());
	}

	public String getSenderSupervisorName() {
		return _senderSupervisorName;
	}

	public void setSenderSupervisorName(String senderSupervisorName) {
		this._senderSupervisorName = senderSupervisorName;
		this._senderSupervisorId = Supervisor.queryIdByName(senderSupervisorName, new ErrorInfo());
	}

	public String getReceiverUserName() {
		if (_receiverUserName != null) {
			return _receiverUserName;
		}
		
		if (receiverUserId < 0) {
			ErrorInfo error = new ErrorInfo();
			String sql = "select receiver_name from v_messages_supervisor_outbox where id = ?";
			
			try {
				_receiverUserName = v_messages_supervisor_outbox.find(sql, this.id).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				error.msg = "数据库异常";
				return null;
			}
		}
		
		return _receiverUserName;
	}

	public void setReceiverUserName(String receiverUserName) {
		this._receiverUserName = receiverUserName;
		this._receiverUserId = User.queryIdByUserName(receiverUserName, new ErrorInfo());
	}

	public String getReceiverSupervisorName() {
		return _receiverSupervisorName;
	}

	public void setReceiverSupervisorName(String receiverSupervisorName) {
		this._receiverSupervisorName = receiverSupervisorName;
		this._receiverSupervisorId = Supervisor.queryIdByName(receiverSupervisorName, new ErrorInfo());
	}
	
	public String getReplyStatus() {
		if (replyStatus != null) {
			return replyStatus;
		}
		
		ErrorInfo error = new ErrorInfo();
		String sql = "select reply_status from v_messages_supervisor_dustbin where id = ?";
		
		try {
			replyStatus = v_messages_supervisor_dustbin.find(sql, this.id).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		return replyStatus;
	}

	/**
	 * 用户给用户发送站内信
	 * @param error
	 * @return
	 */
	public int sendToUserByUser(ErrorInfo error) {
		error.clear();
		
		if (this.senderUserId < 1) {
			error.code = -1;
			error.msg = "发件人不存在";

			return error.code;
		}
		
		if (this.receiverUserId < 1) {
			error.code = -1;
			error.msg = "收件人不存在";

			return error.code;
		}
		
		if (this.receiverUserId == this.senderUserId) {
			error.code = -1;
			error.msg = "不能给自已发送站内信";

			return error.code;
		}
		
		if(StringUtils.isBlank(this.title)) {
			error.code = -1;
			error.msg = "站内信标题不能为空";

			return error.code;
		}
		
		if(this.title.length()>40) { 
			error.code = -1;
			error.msg = "站内信标题过长";

			return error.code;
		}
		if (StringUtils.isBlank(this.content)) {
			error.code = -1;
			error.msg = "内容不能为空";

			return error.code;
		}

		t_messages msg = new t_messages();
		msg.sender_user_id = this.senderUserId;
		msg.time = new Date();
		msg.receiver_user_id = this.receiverUserId;
		msg.title = this.title;
		msg.content = this.content;

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.userEvent(this.senderUserId, UserEvent.SEND_MSG, "发送站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "站内信发送成功";

		return 0;
	}

	/**
	 * 用户给系统管理员发送站内信
	 * @param error
	 * @return
	 */
	public int sendToSupervisorByUser(ErrorInfo error,long userId) {
		error.clear();
		
		if (this.senderUserId < 1) {
			error.code = -1;
			error.msg = "发件人不存在";

			return error.code;
		}
		if(StringUtils.isBlank(this.title)) {
			error.code = -1;
			error.msg = "标题不能为空";

			return error.code;
		}
		
		if(this.title.length()>40) {
			error.code = -1;
			error.msg = "站内信标题过长";

			return error.code;
		}
		
		if(StringUtils.isBlank(this.content)){
			error.code = -1;
			error.msg = "给管理员发站内信内容不能为空";

			return error.code;
			
		}

		t_messages msg = new t_messages();
		msg.sender_user_id = this.senderUserId;
		msg.time = new Date();
		msg.receiver_supervisor_id = SystemSupervisor.ID;
		msg.title = this.title;
		msg.content = this.content;

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.userEvent(userId, UserEvent.SEND_MSG, "发送站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;
		error.msg = "站内信发送成功";

		return 0;
	}

	/**
	 * 管理员给用户发送站内信
	 * @param error
	 * @return
	 */
	public int sendToUserBySupervisor(ErrorInfo error) {
		error.clear();
		
		if (this.senderSupervisorId < 1) {
			error.code = -1;
			error.msg = "发件人不存在";

			return error.code;
		}
		
		if (this.receiverUserId < 1) {
			error.code = -1;
			error.msg = "收件人不存在";

			return error.code;
		}

		t_messages msg = new t_messages();
		msg.sender_supervisor_id = this.senderSupervisorId;
		msg.time = new Date();
		msg.receiver_user_id = this.receiverUserId;
		msg.title = this.title;
		msg.content = Templets.replaceAllHTML(this.content);

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.supervisorEvent(this.senderSupervisorId, SupervisorEvent.SEND_MSG, "发送站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "站内信发送成功";

		return 0;
	}

	/**
	 * 管理员给多个用户发送站内信
	 * @param receiverNames:逗号分隔的用户名
	 * @param error
	 * @return
	 */
	public int sendToUsersBySupervisor(String receiverNames, ErrorInfo error) {
		error.clear();
		
		if (this.senderSupervisorId < 1) {
			error.code = -1;
			error.msg = "发件人不存在";

			return error.code;
		}
		
		if (StringUtils.isBlank(receiverNames)) {
			error.code = -1;
			error.msg = "收件人不能为空";
			
			return error.code;
		}
		
		receiverNames = receiverNames.replaceAll("\\s", "");
		String[] arrNames = receiverNames.split(",");
		
		if (arrNames.length == 1) {
			this.receiverUserName = arrNames[0];
			
			return sendToUserBySupervisor(error);
		}
		
		t_messages msg = new t_messages();
		msg.sender_supervisor_id = this.senderSupervisorId;
		msg.time = new Date();
		msg.receiver_user_id = UserGroupType.CUSTOM_USERS;
		msg.title = this.title;
		msg.content = Templets.replaceAllHTML(this.content);

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		int totalCount = arrNames.length;
		int successCount = 0;
		long msgId = msg.id;

		for (int i=0;i<totalCount;i++) {
			long userId = User.queryIdByUserName(arrNames[i], error);
			
			if (userId < 1) {
				error.clear();
				continue;
			}
			
			t_messages_receivers mr = new t_messages_receivers();
			mr.message_id = msgId;
			mr.user_id = userId;

			try {
				mr.save();
			} catch (Exception e) {
				Logger.error(e.getMessage());
				e.printStackTrace();
				error.code = -1;
				error.msg = "数据库异常";
				JPA.setRollbackOnly();

				return error.code;
			}
			
			successCount++;
		}
		
		if (successCount < totalCount) {
			error.code = -1;
			error.msg = "发送成功" + successCount + "条;" + "发送失败" + (totalCount - successCount) + "条";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(this.senderSupervisorId, SupervisorEvent.GROUP_SEND_MSG, "群发站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "站内信发送成功";

		return 0;
	}

	/**
	 * 管理员发送快捷站内信
	 * @param usertype
	 * @param error
	 * @return
	 */
	public int sendToUserGroupBySupervisor(long usertype, ErrorInfo error) {
		error.clear();
		
		if (this.senderSupervisorId < 1) {
			error.code = -1;
			error.msg = "发件人不存在";

			return error.code;
		}
		
		if (usertype > -1) {
			error.code = -1;
			error.msg = "接收的用户组不存在";

			return error.code;
		}

		t_messages msg = new t_messages();
		msg.sender_supervisor_id = this.senderSupervisorId;
		msg.time = new Date();
		msg.receiver_user_id = usertype;
		msg.title = this.title;
		msg.content = Templets.replaceAllHTML(this.content);

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.supervisorEvent(this.senderSupervisorId, SupervisorEvent.QUICKLY_SEND_MSG, "发送快捷站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "站内信发送成功";

		return 0;
	}

	/**
	 * 用户给用户回复站内信
	 * @param msgId
	 * @param error
	 * @return
	 */
	public int replyToUserByUser(long msgId, ErrorInfo error) {
		error.clear();
		
		if (this.senderUserId < 1) {
			error.code = -1;
			error.msg = "发件人不存在";

			return error.code;
		}
		
		t_messages oldMsg = null;

		try {
			oldMsg = t_messages.findById(msgId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == oldMsg) {
			error.code = -2;
			error.msg = "消息不存在";

			return error.code;
		}

		t_messages msg = new t_messages();
		msg.sender_user_id = this.senderUserId;
		msg.time = new Date();
		msg.receiver_user_id = oldMsg.sender_user_id;
		msg.message_id = msgId;
		msg.title = this.title;
		msg.content = this.content;
		msg.is_reply = true;

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.userEvent(User.currUser().id, UserEvent.REPLY_MSG, "回复站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "成功回复站内信";

		return 0;
	}

	/**
	 * 用户给管理员回复站内信
	 * @param msgId
	 * @param error
	 * @return
	 */
	public int replyToSupervisorByUser(long msgId, ErrorInfo error) {
		error.clear();
		
		if (this.senderUserId < 1) {
			error.code = -1;
			error.msg = "发件人不存在";

			return error.code;
		}

		t_messages oldMsg = null;

		try {
			oldMsg = t_messages.findById(msgId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == oldMsg) {
			error.code = -2;
			error.msg = "消息不存在";

			return error.code;
		}

		t_messages msg = new t_messages();
		msg.sender_user_id = this.senderUserId;
		msg.time = new Date();
		msg.receiver_supervisor_id = SystemSupervisor.ID;
		msg.message_id = msgId;
		msg.title = this.title;
		msg.content = this.content;
		msg.is_reply = true;

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.userEvent(User.currUser().id, UserEvent.REPLY_MSG, "回复站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;
		error.msg = "成功回复站内信";

		return 0;
	}
	
	/**
	 * 用户给某人(用户/管理员)回复站内信
	 * @param msgId
	 * @param error
	 * @return
	 */
	public int replyToSomebodyByUser(long msgId, ErrorInfo error) {
		error.clear();
		
		t_messages oldMsg = null;

		try {
			oldMsg = t_messages.findById(msgId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == oldMsg) {
			error.code = -2;
			error.msg = "消息不存在";

			return error.code;
		}
		
		if (oldMsg.sender_user_id > 0) {
			return replyToUserByUser(msgId, error);
		}
		
		if (oldMsg.sender_supervisor_id > 0) {
			return replyToSupervisorByUser(msgId, error);
		}
		
		error.code = 0;
		
		return 0;
	}

	/**
	 * 管理员给用户回复站内信
	 * @param msgId
	 * @param error
	 * @return
	 */
	public int replyToUserBySupervisor(long msgId, ErrorInfo error) {
		error.clear();
		
		if (this.senderSupervisorId < 1) {
			error.code = -1;
			error.msg = "发件人不存在";

			return error.code;
		}

		t_messages oldMsg = null;

		try {
			oldMsg = t_messages.findById(msgId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == oldMsg) {
			error.code = -2;
			error.msg = "消息不存在";

			return error.code;
		}

		t_messages msg = new t_messages();
		msg.sender_supervisor_id = this.senderSupervisorId;
		msg.time = new Date();
		msg.receiver_user_id = oldMsg.sender_user_id;
		msg.message_id = msgId;
		msg.title = this.title;
		msg.content = this.content;
		msg.is_reply = true;

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.supervisorEvent(this.senderSupervisorId, SupervisorEvent.QUICKLY_SEND_MSG, "回复站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "成功回复站内信";

		return 0;
	}

	/**
	 * 用户删除收件箱站内信
	 * @param userId
	 * @param msgId
	 * @param deleteType
	 * @param error
	 * @return
	 */
	public static int deleteInboxMsgByUser(long userId, long msgId, int deleteType, ErrorInfo error) {
		error.clear();

		t_messages msg = null;

		try {
			msg = t_messages.findById(msgId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == msg) {
			error.code = -2;
			error.msg = "消息不存在";

			return error.code;
		}

		t_messages_accepted tma = null;

		try {
			tma = t_messages_accepted.find("user_id = ? and message_id = ?", userId, msgId).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == tma) {
			tma = new t_messages_accepted();
			tma.user_id = userId;
			tma.time = new Date();
			tma.message_id = msgId;
		}

		tma.is_erased = deleteType;
		tma.delete_time = new Date();

		try {
			tma.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.userEvent(userId, UserEvent.DELETE_INBOX_MSG, "删除收件箱站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;

		return 0;
	}

	/**
	 * 用户删除发件箱站内信
	 * @param msgId
	 * @param deleteType
	 * @param error
	 * @return
	 */
	public static int deleteOutboxMsgByUser(long userId, long msgId, int deleteType, ErrorInfo error) {
		error.clear();

		t_messages msg = null;

		try {
			msg = t_messages.findById(msgId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == msg) {
			error.code = -2;
			error.msg = "消息不存在";

			return error.code;
		}

		msg.is_erased = deleteType;
		msg.delete_time = new Date();

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.userEvent(userId, UserEvent.DELETE_OUTBOX_MSG, "删除发件箱站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;

		return 0;
	}

	/**
	 * 管理员删除收件箱站内信
	 * @param msgId
	 * @param deleteType
	 * @param error
	 * @return
	 */
	public static int deleteInboxMsgBySupervisor(long msgId, int deleteType, ErrorInfo error) {
		error.clear();

		t_messages msg = null;

		try {
			msg = t_messages.findById(msgId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == msg) {
			error.code = -2;
			error.msg = "消息不存在";

			return error.code;
		}

		t_messages_accepted tma = null;

		try {
			tma = t_messages_accepted.find("supervisor_id > 0 and message_id = ?", msgId).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == tma) {
			tma = new t_messages_accepted();
			tma.supervisor_id = SystemSupervisor.ID;
			tma.time = new Date();
			tma.message_id = msgId;
		}

		tma.is_erased = deleteType;
		tma.delete_time = new Date();

		try {
			tma.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DELETE_INBOX_MSG, "删除收件箱站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;

		return 0;
	}

	/**
	 * 管理员删除发件箱信息
	 * @param msgId
	 * @param deleteType
	 * @param error
	 * @return
	 */
	public static int deleteOutboxMsgBySupervisor(long msgId, int deleteType, ErrorInfo error) {
		error.clear();

		t_messages msg = null;

		try {
			msg = t_messages.findById(msgId);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == msg) {
			error.code = -2;
			error.msg = "消息不存在";

			return error.code;
		}

		msg.is_erased = deleteType;
		msg.delete_time = new Date();

		try {
			msg.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DELETE_OUTBOX_MSG, "删除发件箱站内信", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;

		return 0;
	}
	
	/**
	 * 查询massageidlist
	 * @param userId
	 * @return
	 */
	public static List<Object> getMessageList(long userId){
		List<Object> list = new ArrayList<Object>();
		String sql = "SELECT m.id FROM (select * from t_messages where receiver_user_id = ? ORDER BY time DESC) m WHERE NOT EXISTS (SELECT a.id FROM t_messages_accepted a WHERE a.user_id = m.receiver_user_id and m.id = a.message_id)";
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, userId);
		list = (List<Object>)query.getResultList();
		return list;
	}

	/**
	 * 用户标记站内信已读
	 * @param userId
	 * @param msgId
	 * @param error
	 * @return
	 */
	public static int markUserMsgReaded(long userId, long msgId, ErrorInfo error) {
		error.clear();

		t_messages_accepted tma = null;

		try {
			tma = t_messages_accepted.find("user_id = ? and message_id = ?", userId, msgId).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null != tma) {
			error.code = 0;
			error.msg = "消息已标记为已读";

			return error.code;
		}

		tma = new t_messages_accepted();
		tma.user_id = userId;
		tma.time = new Date();
		tma.message_id = msgId;

		try {
			tma.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.userEvent(userId, UserEvent.MARK_MSG_READED, "标记站内信为已读", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;

		return 0;
	}

	/**
	 * 用户标记站内信未读
	 * @param userId
	 * @param msgId
	 * @param error
	 * @return
	 */
	public static int markUserMsgUnread(long userId, long msgId, ErrorInfo error) {
		error.clear();

		t_messages_accepted tma = null;

		try {
			tma = t_messages_accepted.find("user_id = ? and message_id = ?", userId, msgId).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == tma) {
			error.code = 0;
			error.msg = "消息已标记为未读";

			return error.code;
		}

		try {
			tma.delete();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.userEvent(userId, UserEvent.MARK_MSG_UNREAD, "标记站内信为未读", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;

		return 0;
	}

	/**
	 * 管理员标记站内信已读
	 * @param msgId
	 * @param error
	 * @return
	 */
	public static int markSupervisorMsgReaded(long msgId, ErrorInfo error) {
		error.clear();

		t_messages_accepted tma = null;

		try {
			tma = t_messages_accepted.find("supervisor_id = ? and message_id = ?",
					SystemSupervisor.ID, msgId).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null != tma) {
			error.code = 0;
			error.msg = "消息已标记为已读";

			return error.code;
		}

		tma = new t_messages_accepted();
		tma.supervisor_id = SystemSupervisor.ID;
		tma.time = new Date();
		tma.message_id = msgId;

		try {
			tma.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;

		return 0;
	}

	/**
	 * 管理员标记站内信未读
	 * @param msgId
	 * @param error
	 * @return
	 */
	public static int markSupervisorMsgUnread(long msgId, ErrorInfo error) {
		error.clear();

		t_messages_accepted tma = null;

		try {
			tma = t_messages_accepted.find("supervisor_id = ? and message_id = ?",
					SystemSupervisor.ID, msgId).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		if (null == tma) {
			error.code = 0;
			error.msg = "消息已标记为未读";

			return error.code;
		}

		try {
			tma.delete();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;

		return 0;
	}

	/**
	 * 查询系统消息(由管理员发出，且不是回复用户的站内信)
	 * @param userId
	 * @param page
	 * @param length
	 * @param keyword
	 * @param readStatus
	 * @param error
	 * @return
	 */
	public static PageBean<v_messages_system> queryUserSystemMsgs(long userId, int currPage, int pageSize,
			String keyword, int readStatus, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}

		String condition = "";

		if (StringUtils.isNotBlank(keyword)) {
			condition += " and (`m`.`title` like :keyword) ";
		}

		if (ReadStatus.Readed == readStatus) {
			condition += " and (`ma`.`user_id` = :userId) ";
		} else if (ReadStatus.Unread == readStatus) {
			condition += " and (isnull(`ma`.`user_id`)) ";
		}

		int begin = (currPage - 1) * pageSize;
		
		String sqlPage = "select `m`.`id` AS `id`,NULL AS `user_id`,`m`.`content` AS `content`,`m`.`title` AS `title`,`m`.`time` AS `time`,NULL AS `content`,if((`ma`.`user_id` = :userId),'已读','未读') AS `read_status` from ((`v_messages_system` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) left join `t_users` `u` on((`u`.`id` = :userId))) where ((`u`.`time` < `m`.`time`)  and (`ma`.`is_erased` = 0 OR isnull(`ma`.`is_erased`)) and (((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))) or (`m`.`user_id` = :userId) or (`m`.`user_id` = -(1)) or ((`m`.`user_id` = -(2)) and ((`u`.`master_identity` = 1) or (`u`.`master_identity` = 3))) or ((`m`.`user_id` = -(3)) and ((`u`.`master_identity` = 2) or (`u`.`master_identity` = 3))) or ((`m`.`user_id` = -(4)) and (`u`.`master_identity` = 3)) or ((`m`.`user_id` = -(5)) and (`u`.`is_email_verified` <> 1)) or ((`m`.`user_id` = -(6)) and (`u`.`is_blacklist` = 1)) or ((`m`.`user_id` = -(7)) and (`u`.`time` < `m`.`time`) and (timestampdiff(DAY,`u`.`time`,`m`.`time`) < 7)))"
				+ condition + ") order by `m`.`time` desc";
		Query queryPage = null;
		
		String sqlCount = "select count(`m`.`id`) AS `count` from ((`v_messages_system` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) left join `t_users` `u` on((`u`.`id` = :userId))) where ((`u`.`time` < `m`.`time`) and ((`ma`.`is_erased` = 0) or isnull(`ma`.`is_erased`)) and (((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))) or (`m`.`user_id` = :userId) or (`m`.`user_id` = -(1)) or ((`m`.`user_id` = -(2)) and ((`u`.`master_identity` = 1) or (`u`.`master_identity` = 3))) or ((`m`.`user_id` = -(3)) and ((`u`.`master_identity` = 2) or (`u`.`master_identity` = 3))) or ((`m`.`user_id` = -(4)) and (`u`.`master_identity` = 3)) or ((`m`.`user_id` = -(5)) and (`u`.`is_email_verified` <> 1)) or ((`m`.`user_id` = -(6)) and (`u`.`is_blacklist` = 1)) or ((`m`.`user_id` = -(7)) and (`u`.`time` < `m`.`time`) and (timestampdiff(DAY,`u`.`time`,`m`.`time`) < 7)))"
				+ condition + ")";
		Query queryCount = null;
		List<v_messages_system> page = null;
		Object obj = null;
		
		queryPage = JPA.em().createNativeQuery(sqlPage, v_messages_system.class);
		queryPage.setParameter("userId", userId);
		queryPage.setFirstResult(begin);
		queryPage.setMaxResults(pageSize);
		
		queryCount = JPA.em().createNativeQuery(sqlCount);
		queryCount.setParameter("userId", userId);
		
		if (StringUtils.isNotBlank(keyword)) {
			queryPage.setParameter("keyword", "%" + keyword + "%");
			queryCount.setParameter("keyword", "%" + keyword + "%");
		}

		try {
			page = queryPage.getResultList();
			obj = queryCount.getResultList().get(0);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		int count = Convert.strToInt(obj+"", 0);

		Map<String, Object> map = new HashMap<String, Object>();
		
		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}	
		
		map.put("readStatus", readStatus);

		PageBean<v_messages_system> bean = new PageBean<v_messages_system>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.page = page;
		bean.totalCount = (int) count;
		bean.conditions = map;
		
		error.code = 0;
		
		return bean;
	}
	
	/**
	 * 查询系统消息详情(上一条，当前，下一条)
	 * @param userId
	 * @param index
	 * @param keyword
	 * @param readStatus
	 * @param error
	 * @return
	 */
	public static PageBean<v_messages_system> queryUserSystemMsgDetail(long userId, int index,
			String keyword, int readStatus, ErrorInfo error) {
		error.clear();
		
		String condition = "";

		if (StringUtils.isNotBlank(keyword)) {
			condition += " and (`m`.`title` like :keyword) ";
		}

		if (ReadStatus.Readed == readStatus) {
			condition += " and (`ma`.`user_id` = :userId) ";
		} else if (ReadStatus.Unread == readStatus) {
			condition += " and (isnull(`ma`.`user_id`)) ";
		}
		
		int begin = index - 2;
		int pageSize = 3;
		
		if (begin < 0) {
			begin = 0;
		}

		String sqlPage = "select `m`.`id` AS `id`,NULL AS `user_id`,`m`.`title` AS `title`,`m`.`time` AS `time`,`m`.`content` AS `content`,if((`ma`.`user_id` = :userId),'已读','未读') AS `read_status` from ((`v_messages_system` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) left join `t_users` `u` on((`u`.`id` = :userId))) where ((`u`.`time` < `m`.`time`)  and (`ma`.`is_erased` = 0 OR isnull(`ma`.`is_erased`)) and (((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))) or (`m`.`user_id` = :userId) or (`m`.`user_id` = -(1)) or ((`m`.`user_id` = -(2)) and ((`u`.`master_identity` = 1) or (`u`.`master_identity` = 3))) or ((`m`.`user_id` = -(3)) and ((`u`.`master_identity` = 2) or (`u`.`master_identity` = 3))) or ((`m`.`user_id` = -(4)) and (`u`.`master_identity` = 3)) or ((`m`.`user_id` = -(5)) and (`u`.`is_email_verified` <> 1)) or ((`m`.`user_id` = -(6)) and (`u`.`is_blacklist` = 1)) or ((`m`.`user_id` = -(7)) and (`u`.`time` < `m`.`time`) and (timestampdiff(DAY,`u`.`time`,`m`.`time`) < 7)))"
				+ condition + ") order by `m`.`time` desc";
		Query queryPage = null;
		
		String sqlCount = "select count(`m`.`id`) AS `count` from ((`v_messages_system` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) left join `t_users` `u` on((`u`.`id` = :userId))) where ((`u`.`time` < `m`.`time`) and ((`ma`.`is_erased` = 0) or isnull(`ma`.`is_erased`)) and (((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))) or (`m`.`user_id` = :userId) or (`m`.`user_id` = -(1)) or ((`m`.`user_id` = -(2)) and ((`u`.`master_identity` = 1) or (`u`.`master_identity` = 3))) or ((`m`.`user_id` = -(3)) and ((`u`.`master_identity` = 2) or (`u`.`master_identity` = 3))) or ((`m`.`user_id` = -(4)) and (`u`.`master_identity` = 3)) or ((`m`.`user_id` = -(5)) and (`u`.`is_email_verified` <> 1)) or ((`m`.`user_id` = -(6)) and (`u`.`is_blacklist` = 1)) or ((`m`.`user_id` = -(7)) and (`u`.`time` < `m`.`time`) and (timestampdiff(DAY,`u`.`time`,`m`.`time`) < 7)))"
				+ condition + ")";
		Query queryCount = null;
		List<v_messages_system> page = null;
		Object obj = null;
		
		queryPage = JPA.em().createNativeQuery(sqlPage, v_messages_system.class);
		queryPage.setParameter("userId", userId);
		queryPage.setFirstResult(begin);
		queryPage.setMaxResults(pageSize);
		
		queryCount = JPA.em().createNativeQuery(sqlCount);
		queryCount.setParameter("userId", userId);
		
		if (StringUtils.isNotBlank(keyword)) {
			queryPage.setParameter("keyword", "%" + keyword + "%");
			queryCount.setParameter("keyword", "%" + keyword + "%");
		}

		try {
			page = queryPage.getResultList();
			obj = queryCount.getResultList().get(0);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		int count = Convert.strToInt(obj+"", 0);
		
		if (count < 1) {
			error.code = -1;
			error.msg = "消息不存在";

			return null;
		}

		PageBean<v_messages_system> bean = new PageBean<v_messages_system>();
		bean.pageSize = pageSize;
		bean.currPage = index;
		bean.page = page;
		bean.totalCount = (int) count;
		
		/**
		 * 当前消息标记为已读
		 */
		long msgId = -1;
		
		if (1 == index) {
			msgId = page.get(0).id;
		} else {
			msgId = page.get(1).id;
		}
		
		markUserMsgReaded(userId, msgId, error);
		
		error.code = 0;
		
		return bean;
	}

	/**
	 * 查询用户收件箱消息(管理员回复用户的站内信和借款标提问)
	 * @param userId
	 * @param page
	 * @param length
	 * @param keyword
	 * @param readStatus
	 * @param error
	 * @return
	 */
	public static PageBean<v_messages_user_inbox> queryUserInboxMsgs(long userId, int currPage, int pageSize,
			String keyword, int readStatus, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}

		String condition = "";

		if (StringUtils.isNotBlank(keyword)) {
			condition += " and ((`m`.`title` like :keyword) or (`m`.`sender_name` like :keyword)) ";
		}

		if (ReadStatus.Readed == readStatus) {
			condition += " and (`ma`.`user_id` = :userId) ";
		} else if (ReadStatus.Unread == readStatus) {
			condition += " and (isnull(`ma`.`user_id`)) ";
		}

		int begin = (currPage - 1) * pageSize;
		List<v_messages_user_inbox> page = null;
		
		String sqlPage = "select `m`.`id` AS `id`,NULL AS `user_id`,`m`.`sender_name` AS `sender_name`,`m`.`content` AS `content`, `m`.`title` AS `title`,`m`.`time` AS `time`,NULL AS `content`,if((`ma`.`user_id` = :userId),'已读','未读') AS `read_status` from (`v_messages_user_inbox` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) where ((`ma`.`is_erased` = 0 or isnull(`ma`.`is_erased`)) and ((`m`.`user_id` = :userId) or ((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))))"
				+ condition + ") order by `m`.`time` desc";
		Query queryPage = null;
		
		String sqlCount = "select count(`m`.`id`) AS `count` from (`v_messages_user_inbox` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) where (((`ma`.`is_erased` = 0) or isnull(`ma`.`is_erased`)) and ((`m`.`user_id` = :userId) or ((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))))"
				+ condition + ")";
		Query queryCount = null;
		Object obj = null;
		
		queryPage = JPA.em().createNativeQuery(sqlPage, v_messages_user_inbox.class);
		queryPage.setParameter("userId", userId);
		queryPage.setFirstResult(begin);
		queryPage.setMaxResults(pageSize);
		
		queryCount = JPA.em().createNativeQuery(sqlCount);
		queryCount.setParameter("userId", userId);
		
		if (StringUtils.isNotBlank(keyword)) {
			queryPage.setParameter("keyword", "%" + keyword + "%");
			queryCount.setParameter("keyword", "%" + keyword + "%");
		}

		try {
			obj = queryCount.getResultList().get(0);
			page = queryPage.getResultList();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		int count = Convert.strToInt(obj+"", 0);

		Map<String, Object> map = new HashMap<String, Object>();
		
		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}	
		
		map.put("readStatus", readStatus);

		PageBean<v_messages_user_inbox> bean = new PageBean<v_messages_user_inbox>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.page = page;
		bean.totalCount = (int) count;
		bean.conditions = map;
		
		error.code = 0;
		
		return bean;
	}
	
	/**
	 * 查询微信用户收件箱消息
	 * @param userId
	 * @param page
	 * @param length
	 * @param keyword
	 * @param readStatus
	 * @param error
	 * @return
	 */
	public static PageBean<v_messages_wechat_inbox> queryWechatInboxMsgs(long userId, int currPage, int pageSize,
			String keyword, int readStatus, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}

		String condition = "";

		if (StringUtils.isNotBlank(keyword)) {
			condition += " and ((`m`.`title` like :keyword) or (`m`.`sender_name` like :keyword)) ";
		}

		if (ReadStatus.Readed == readStatus) {
			condition += " and (`ma`.`user_id` = :userId) ";
		} else if (ReadStatus.Unread == readStatus) {
			condition += " and (isnull(`ma`.`user_id`)) ";
		}

		int begin = (currPage - 1) * pageSize;
		List<v_messages_wechat_inbox> page = null;
		
		String sqlPage = "select `m`.`id` AS `id`,NULL AS `user_id`,`m`.`sender_name` AS `sender_name`,`m`.`content` AS `content`, `m`.`title` AS `title`,`m`.`time` AS `time`,NULL AS `content`,if((`ma`.`user_id` = :userId),'已读','未读') AS `read_status` from (`v_messages_wechat_inbox` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) where ((`ma`.`is_erased` = 0 or isnull(`ma`.`is_erased`)) and ((`m`.`user_id` = :userId) or ((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))))"
				+ condition + ") order by `m`.`time` desc";
		Query queryPage = null;
		
		String sqlCount = "select count(`m`.`id`) AS `count` from (`v_messages_wechat_inbox` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) where (((`ma`.`is_erased` = 0) or isnull(`ma`.`is_erased`)) and ((`m`.`user_id` = :userId) or ((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))))"
				+ condition + ")";
		Query queryCount = null;
		Object obj = null;
		
		queryPage = JPA.em().createNativeQuery(sqlPage, v_messages_wechat_inbox.class);
		queryPage.setParameter("userId", userId);
		queryPage.setFirstResult(begin);
		queryPage.setMaxResults(pageSize);
		
		queryCount = JPA.em().createNativeQuery(sqlCount);
		queryCount.setParameter("userId", userId);
		
		if (StringUtils.isNotBlank(keyword)) {
			queryPage.setParameter("keyword", "%" + keyword + "%");
			queryCount.setParameter("keyword", "%" + keyword + "%");
		}

		try {
			obj = queryCount.getResultList().get(0);
			page = queryPage.getResultList();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		int count = Convert.strToInt(obj+"", 0);

		Map<String, Object> map = new HashMap<String, Object>();
		
		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}	
		
		map.put("readStatus", readStatus);

		PageBean<v_messages_wechat_inbox> bean = new PageBean<v_messages_wechat_inbox>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.page = page;
		bean.totalCount = (int) count;
		bean.conditions = map;
		
		error.code = 0;
		
		return bean;
	}
	
	/**
	 * 用户收件箱详情
	 * @param userId
	 * @param index
	 * @param keyword
	 * @param readStatus
	 * @param error
	 * @return
	 */
	public static PageBean<v_messages_user_inbox> queryUserInboxMsgDetail(long userId, int index,
			String keyword, int readStatus, ErrorInfo error) {
		error.clear();
		
		String condition = "";

		if (StringUtils.isNotBlank(keyword)) {
			condition += " and ((`m`.`title` like :keyword) or (`m`.`sender_name` like :keyword)) ";
		}

		if (ReadStatus.Readed == readStatus) {
			condition += " and (`ma`.`user_id` = :userId) ";
		} else if (ReadStatus.Unread == readStatus) {
			condition += " and (isnull(`ma`.`user_id`)) ";
		}
		
		int begin = index - 2;
		int pageSize = 3;
		
		if (begin < 0) {
			begin = 0;
		}

		String sqlPage = "select `m`.`id` AS `id`,NULL AS `user_id`,`m`.`sender_name` AS `sender_name`,`m`.`title` AS `title`,`m`.`time` AS `time`,`m`.`content` AS `content`,if((`ma`.`user_id` = :userId),'已读','未读') AS `read_status` from (`v_messages_user_inbox` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) where ((`ma`.`is_erased` = 0 or isnull(`ma`.`is_erased`)) and ((`m`.`user_id` = :userId) or ((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))))"
				+ condition + ") order by `m`.`time` desc";
		Query queryPage = null;
		
		String sqlCount = "select count(`m`.`id`) AS `count` from (`v_messages_user_inbox` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) where (((`ma`.`is_erased` = 0) or isnull(`ma`.`is_erased`)) and ((`m`.`user_id` = :userId) or ((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))))"
				+ condition + ")";
		Query queryCount = null;
		List<v_messages_user_inbox> page = null;
		Object obj = null;
		
		queryPage = JPA.em().createNativeQuery(sqlPage, v_messages_user_inbox.class);
		queryPage.setParameter("userId", userId);
		queryPage.setFirstResult(begin);
		queryPage.setMaxResults(pageSize);
		
		queryCount = JPA.em().createNativeQuery(sqlCount);
		queryCount.setParameter("userId", userId);
		
		if (StringUtils.isNotBlank(keyword)) {
			queryPage.setParameter("keyword", "%" + keyword + "%");
			queryCount.setParameter("keyword", "%" + keyword + "%");
		}

		try {
			page = queryPage.getResultList();
			obj = queryCount.getResultList().get(0);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		int count = Convert.strToInt(obj+"", 0);
		
		if (count < 1) {
			error.code = -1;
			error.msg = "消息不存在";

			return null;
		}

		PageBean<v_messages_user_inbox> bean = new PageBean<v_messages_user_inbox>();
		bean.pageSize = pageSize;
		bean.currPage = index;
		bean.page = page;
		bean.totalCount = (int) count;
		
		/**
		 * 当前消息标记为已读
		 */
		long msgId = -1;
		
		if (1 == index) {
			msgId = page.get(0).id;
		} else {
			msgId = page.get(1).id;
		}
		
		markUserMsgReaded(userId, msgId, error);
		
		error.code = 0;
		
		return bean;
	}
	
	/**
	 * 用户微信收件箱详情
	 * @param userId
	 * @param index
	 * @param keyword
	 * @param readStatus
	 * @param error
	 * @return
	 */
	public static PageBean<v_messages_wechat_inbox> queryWechatInboxMsgDetail(long userId, int index,
			String keyword, int readStatus, ErrorInfo error) {
		error.clear();
		
		String condition = "";

		if (StringUtils.isNotBlank(keyword)) {
			condition += " and ((`m`.`title` like :keyword) or (`m`.`sender_name` like :keyword)) ";
		}

		if (ReadStatus.Readed == readStatus) {
			condition += " and (`ma`.`user_id` = :userId) ";
		} else if (ReadStatus.Unread == readStatus) {
			condition += " and (isnull(`ma`.`user_id`)) ";
		}
		
		int begin = index - 2;
		int pageSize = 3;
		
		if (begin < 0) {
			begin = 0;
		}

		String sqlPage = "select `m`.`id` AS `id`,NULL AS `user_id`,`m`.`sender_name` AS `sender_name`,`m`.`title` AS `title`,`m`.`time` AS `time`,`m`.`content` AS `content`,if((`ma`.`user_id` = :userId),'已读','未读') AS `read_status` from (`v_messages_wechat_inbox` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) where ((`ma`.`is_erased` = 0 or isnull(`ma`.`is_erased`)) and ((`m`.`user_id` = :userId) or ((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))))"
				+ condition + ") order by `m`.`time` desc";
		Query queryPage = null;
		
		String sqlCount = "select count(`m`.`id`) AS `count` from (`v_messages_wechat_inbox` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) where (((`ma`.`is_erased` = 0) or isnull(`ma`.`is_erased`)) and ((`m`.`user_id` = :userId) or ((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))))"
				+ condition + ")";
		Query queryCount = null;
		List<v_messages_wechat_inbox> page = null;
		Object obj = null;
		
		queryPage = JPA.em().createNativeQuery(sqlPage, v_messages_wechat_inbox.class);
		queryPage.setParameter("userId", userId);
		queryPage.setFirstResult(begin);
		queryPage.setMaxResults(pageSize);
		
		queryCount = JPA.em().createNativeQuery(sqlCount);
		queryCount.setParameter("userId", userId);
		
		if (StringUtils.isNotBlank(keyword)) {
			queryPage.setParameter("keyword", "%" + keyword + "%");
			queryCount.setParameter("keyword", "%" + keyword + "%");
		}

		try {
			page = queryPage.getResultList();
			obj = queryCount.getResultList().get(0);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		int count = Convert.strToInt(obj+"", 0);
		
		if (count < 1) {
			error.code = -1;
			error.msg = "消息不存在";

			return null;
		}

		PageBean<v_messages_wechat_inbox> bean = new PageBean<v_messages_wechat_inbox>();
		bean.pageSize = pageSize;
		bean.currPage = index;
		bean.page = page;
		bean.totalCount = (int) count;
		
		/**
		 * 当前消息标记为已读
		 */
		long msgId = -1;
		
		if (1 == index) {
			msgId = page.get(0).id;
		} else {
			msgId = page.get(1).id;
		}
		
		markUserMsgReaded(userId, msgId, error);
		
		error.code = 0;
		
		return bean;
	}

	/**
	 * 查询用户发件箱消息
	 * @param userId
	 * @param page
	 * @param length
	 * @param keyword
	 * @param error
	 * @return
	 */
	public static PageBean<v_messages_user_outbox> queryUserOutboxMsgs(long userId, int currPage,
			int pageSize, String keyword, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}

		//String condition = "user_id = ?";
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_MESSAGES_USER_OUTBOX);
		sql.append(" and m.sender_user_id = ? ");
		
		List<Object> params = new ArrayList<Object>();
		params.add(userId);

		if (StringUtils.isNotBlank(keyword)) {
			sql.append(" and m.title like ? ");
			params.add("%" + keyword + "%");
		}
		sql.append("order by time desc");
		//String sql = "select new v_messages_user_outbox(m.id, m.receiver_name, m.title, m.time,m.content ) from v_messages_user_outbox as m where (" + condition + ")";
		
		List<v_messages_user_outbox> page = null;
		int count = 0;

		try {
			//page = v_messages_user_outbox.find(sql, params.toArray()).fetch(currPage, pageSize);
			//count = (int) v_messages_user_outbox.count(condition, params.toArray());
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_messages_user_outbox.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            page = query.getResultList();
            
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		Map<String, Object> map = new HashMap<String, Object>();
		
		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}
		
		PageBean<v_messages_user_outbox> bean = new PageBean<v_messages_user_outbox>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.page = page;
		bean.totalCount = (int) count;
		bean.conditions = map;
		
		error.code = 0;

		return bean;
	}
	
	/**
	 * 查询用户发件箱消息详情
	 * @param userId
	 * @param index
	 * @param keyword
	 * @param error
	 * @return
	 */
	public static PageBean<v_messages_user_outbox> queryUserOutboxMsgDetail(long userId, int index,
			String keyword, ErrorInfo error) {
		error.clear();
		
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_MESSAGES_USER_OUTBOX);
		sql.append(" and m.sender_user_id = ?");
				
		List<Object> params = new ArrayList<Object>();
		params.add(userId);

		if (StringUtils.isNotBlank(keyword)) {
			sql.append(" and ((m.title like ?) or (m.receiver_name like ?)) ");
			params.add("%" + keyword + "%");
			params.add("%" + keyword + "%");
		}
		
		int begin = index - 2;
		int pageSize = 3;
		
		if (begin < 0) {
			begin = 0;
		}

		int count = 0;
		List<v_messages_user_outbox> page = null;
		sql.append(" order by m.time desc");
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(), v_messages_user_outbox.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult(begin);
            query.setMaxResults(pageSize);
            page = query.getResultList();
            
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		if (count < 1) {
			error.code = -1;
			error.msg = "消息不存在";

			return null;
		}
		
		PageBean<v_messages_user_outbox> bean = new PageBean<v_messages_user_outbox>();
		bean.pageSize = pageSize;
		bean.currPage = index;
		bean.page = page;
		bean.totalCount = (int) count;
		
		error.code = 0;
		
		return bean;
	}

	/**
	 * 查询用户未读系统消息数量
	 * @param userId
	 * @param error
	 * @return
	 */
	public static int queryUserUnreadSystemMsgsCount(long userId, ErrorInfo error) {
		error.clear();

		String sql = "select count(id) as count from (select `m`.`id` AS `id`,:userId AS `user_id`,`m`.`title` AS `title`,`m`.`time` AS `time`,if((`ma`.`user_id` = :userId),'已读','未读') AS `read_status` from ((`v_messages_system` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) left join `t_users` `u` on((`u`.`id` = :userId))) where ((`u`.`time` < `m`.`time`) and (((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))) or (`m`.`user_id` = :userId) or (`m`.`user_id` = -(1)) or ((`m`.`user_id` = -(2)) and ((`u`.`master_identity` = 1) or (`u`.`master_identity` = 3))) or ((`m`.`user_id` = -(3)) and ((`u`.`master_identity` = 2) or (`u`.`master_identity` = 3))) or ((`m`.`user_id` = -(4)) and (`u`.`master_identity` = 3)) or ((`m`.`user_id` = -(5)) and (`u`.`is_email_verified` <> 1)) or ((`m`.`user_id` = -(6)) and (`u`.`is_blacklist` = 1)) or ((`m`.`user_id` = -(7)) and (`u`.`time` < `m`.`time`) and (timestampdiff(DAY,`u`.`time`,`m`.`time`) < 7))))) as inbox where inbox.read_status = '未读'";
		Query query = null;
		Object obj = null;
		query = JPA.em().createNativeQuery(sql);
		query.setParameter("userId", userId);

		try {
			obj = query.getResultList().get(0);
			/*
			 * map法 Query query = JPA.em().createNativeQuery(sql);
			 * query.unwrap(SQLQuery
			 * .class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			 * query.setParameter("userId", userId); List rows =
			 * query.getResultList(); Map row = (Map)rows.get(0); count =
			 * ((java.math.BigInteger)row.get("count")).intValue();
			 */
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		error.code = 0;
		
		return Convert.strToInt(obj+"", 0);
	}

	/**
	 * 查询用户收件箱未读消息数量
	 * @param userId
	 * @param error
	 * @return
	 */
	public static int queryUserUnreadInboxMsgsCount(long userId, ErrorInfo error) {
		error.clear();

		String sql = "select count(id) as count from (select `m`.`id` AS `id`,:userId AS `user_id`,`m`.`title` AS `title`,`m`.`time` AS `time`,if((`ma`.`user_id` = :userId),'已读','未读') AS `read_status` from (`v_messages_user_inbox` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) where ((`m`.`user_id` = :userId) or ((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))))) as inbox where inbox.read_status = '未读'";
		Query query = null;
		Object obj = null;
		query = JPA.em().createNativeQuery(sql);
		query.setParameter("userId", userId);

		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		error.code = 0;

		return Convert.strToInt(obj+"", 0);
	}

	/**
	 * 查询用户未读消息数量
	 * @param userId
	 * @param error
	 * @return
	 */
	public static int queryUserUnreadMsgsCount(long userId, ErrorInfo error) {
		error.clear();

		String sql = "select count(id) as count from (select `m`.`id` AS `id`,:userId AS `user_id`,`m`.`title` AS `title`,`m`.`time` AS `time`,if((`ma`.`user_id` = :userId),'已读','未读') AS `read_status` from ((`v_messages_system` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) left join `t_users` `u` on((`u`.`id` = :userId))) where ((`u`.`time` < `m`.`time`) and (((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))) or (`m`.`user_id` = :userId) or (`m`.`user_id` = -(1)) or ((`m`.`user_id` = -(2)) and ((`u`.`master_identity` = 1) or (`u`.`master_identity` = 3))) or ((`m`.`user_id` = -(3)) and ((`u`.`master_identity` = 2) or (`u`.`master_identity` = 3))) or ((`m`.`user_id` = -(4)) and (`u`.`master_identity` = 3)) or ((`m`.`user_id` = -(5)) and (`u`.`is_email_verified` <> 1)) or ((`m`.`user_id` = -(6)) and (`u`.`is_blacklist` = 1)) or ((`m`.`user_id` = -(7)) and (`u`.`time` < `m`.`time`) and (timestampdiff(DAY,`u`.`time`,`m`.`time`) < 7)))) union select `m`.`id` AS `id`,:userId AS `user_id`,`m`.`title` AS `title`,`m`.`time` AS `time`,if((`ma`.`user_id` = :userId),'已读','未读') AS `read_status` from (`v_messages_user_inbox` `m` left join `t_messages_accepted` `ma` on(((`m`.`id` = `ma`.`message_id`) and (`ma`.`user_id` = :userId)))) where ((`m`.`user_id` = :userId) or ((`m`.`user_id` = -(10)) and :userId in (select `t_messages_receivers`.`user_id` AS `user_id` from `t_messages_receivers` where (`t_messages_receivers`.`message_id` = `m`.`id`))))) as inbox where inbox.read_status = '未读'";
		Query query = null;
		Object obj = null;
		query = JPA.em().createNativeQuery(sql);
		query.setParameter("userId", userId);

		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		error.code = 0;

		return Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 查询管理员待回复消息
	 * @param page
	 * @param length
	 * @param keyword
	 * @param error
	 * @return
	 */
	public static PageBean<v_messages_supervisor_inbox> querySupervisorToReplyMsgs(int currPage,
			int pageSize, String keyword, int type, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}

		String condition = "status = '待回复'";
		List<Object> params = new ArrayList<Object>();

		if (StringUtils.isNotBlank(keyword)) {
			if (MessageKeywordType.Title == type) {
				condition += " and (title like ?) ";
				params.add("%" + keyword + "%");
			} else if (MessageKeywordType.SenderName == type) {
				condition += " and (sender_name like ?) ";
				params.add("%" + keyword + "%");
			} else {
				condition += " and ((title like ?) or (sender_name like ?)) ";
				params.add("%" + keyword + "%");
				params.add("%" + keyword + "%");
			}
		}

		int count = 0;
		List<v_messages_supervisor_inbox> page = null;

		try {
			count = (int) v_messages_supervisor_inbox.count(condition, params.toArray());
			page = v_messages_supervisor_inbox.find(condition, params.toArray()).fetch(currPage, pageSize);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		Map<String, Object> map = new HashMap<String, Object>();

		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}

		map.put("type", type);
		
		PageBean<v_messages_supervisor_inbox> bean = new PageBean<v_messages_supervisor_inbox>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.totalCount = count;
		bean.page = page;
		bean.conditions = map;
		
		error.code = 0;

		return bean;
	}
	
	/**
	 * 查询管理员已回复消息
	 * @param page
	 * @param length
	 * @param keyword
	 * @param type
	 * @param error
	 * @return
	 */
	public static PageBean<v_messages_supervisor_inbox> querySupervisorRepliedMsgs(int currPage,
			int pageSize, String keyword, int type, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}

		String condition = "status = '已回复'";
		List<Object> params = new ArrayList<Object>();

		if (StringUtils.isNotBlank(keyword)) {
			if (MessageKeywordType.Title == type) {
				condition += " and (title like ?) ";
				params.add("%" + keyword + "%");
			} else if (MessageKeywordType.SenderName == type) {
				condition += " and (sender_name like ?) ";
				params.add("%" + keyword + "%");
			} else {
				condition += " and ((title like ?) or (sender_name like ?)) ";
				params.add("%" + keyword + "%");
				params.add("%" + keyword + "%");
			}
		}

		int count = 0;
		List<v_messages_supervisor_inbox> page = null;

		try {
			count = (int) v_messages_supervisor_inbox.count(condition, params.toArray());
			page = v_messages_supervisor_inbox.find(condition, params.toArray()).fetch(currPage, pageSize);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		Map<String, Object> map = new HashMap<String, Object>();

		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}

		map.put("type", type);
		
		PageBean<v_messages_supervisor_inbox> bean = new PageBean<v_messages_supervisor_inbox>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.totalCount = count;
		bean.page = page;
		bean.conditions = map;
		
		error.code = 0;

		return bean;
	}
	
	/**
	 * 查询管理员已删除消息
	 * @param page
	 * @param length
	 * @param keyword
	 * @param type
	 * @param error
	 * @return
	 */
	public static PageBean<v_messages_supervisor_dustbin> querySupervisorDeletedMsgs(int currPage,
			int pageSize, String keyword, int type, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		String condition = "1=1";
		List<Object> params = new ArrayList<Object>();

		if (StringUtils.isNotBlank(keyword)) {
			if (MessageKeywordType.Title == type) {
				condition += " and (title like ?) ";
				params.add("%" + keyword + "%");
			} else if (MessageKeywordType.SenderName == type) {
				condition += " and (sender_name like ?) ";
				params.add("%" + keyword + "%");
			} else {
				condition += " and ((title like ?) or (sender_name like ?)) ";
				params.add("%" + keyword + "%");
				params.add("%" + keyword + "%");
			}
		}

		int count = 0;
		List<v_messages_supervisor_dustbin> page = null;

		try {
			count = (int) v_messages_supervisor_dustbin.count(condition, params.toArray());
			page = v_messages_supervisor_dustbin.find(condition, params.toArray()).fetch(currPage, pageSize);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		Map<String, Object> map = new HashMap<String, Object>();

		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}

		map.put("type", type);
		
		PageBean<v_messages_supervisor_dustbin> bean = new PageBean<v_messages_supervisor_dustbin>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.totalCount = count;
		bean.page = page;
		bean.conditions = map;
		
		error.code = 0;

		return bean;
	}
	
	/**
	 * 查询管理员发件箱消息
	 * @param page
	 * @param length
	 * @param keyword
	 * @param type
	 * @param error
	 * @return
	 */
	public static PageBean<v_messages_supervisor_outbox> querySupervisorOutboxMsgs(int currPage,
			int pageSize, String keyword, int type, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}

		String condition = "1=1";
		List<Object> params = new ArrayList<Object>();

		if (StringUtils.isNotBlank(keyword)) {
			if (MessageKeywordType.Title == type) {
				condition += " and (title like ?) ";
				params.add("%" + keyword + "%");
			} else if (MessageKeywordType.SenderName == type) {
				condition += " and (receiver_name like ?) ";
				params.add("%" + keyword + "%");
			} else {
				condition += " and ((title like ?) or (receiver_name like ?)) ";
				params.add("%" + keyword + "%");
				params.add("%" + keyword + "%");
			}
		}

		int count = 0;
		List<v_messages_supervisor_outbox> page = null;

		try {
			count = (int) v_messages_supervisor_outbox.count(condition, params.toArray());
			page = v_messages_supervisor_outbox.find(condition, params.toArray()).fetch(currPage, pageSize);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		Map<String, Object> map = new HashMap<String, Object>();

		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}

		map.put("type", type);
		
		PageBean<v_messages_supervisor_outbox> bean = new PageBean<v_messages_supervisor_outbox>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.totalCount = count;
		bean.page = page;
		bean.conditions = map;
		
		error.code = 0;

		return bean;
	}
	
	/**
	 * 查询回复的站内信(已回复站内信详情)
	 * @param id
	 * @param error
	 * @return
	 */
	public static StationLetter queryReplyMessage(long id, ErrorInfo error) {
		Long msgId = null;
		
		try {
			msgId = t_messages.find("select id from t_messages where is_reply = 1 and message_id = ?", id).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		if (null == msgId) {
			error.code = -2;
			error.msg = "没有找到回复站内信";
			
			return null;
		}
		
		error.code = 0;
		
		return new StationLetter(msgId);
	}
	
	/**
	 * 详情
	 * @param id
	 * @return
	 */
	public static StationLetter detail(long id) {
		return new StationLetter(id);
	}
	
	/**
	 * 管理员待回复站内信个数
	 * @param error
	 * @return
	 */
	public static int queryWaitReplyMessageCount(ErrorInfo error){
		error.clear();
		
		Object result = null;
		
		String sql = "select count(1) from v_messages_supervisor_inbox where ISNULL(reply_time)";

		Query query = JPA.em().createNativeQuery(sql);
		
		try{
			result = query.getSingleResult();
		}catch(Exception e){
			Logger.error("待回复站内信个数:" + e.getMessage());
			error.code = -1;
			error.msg = "查询待回复站内信个数";
			
			return 0;
		}
		
		return result==null?0:Integer.parseInt(result.toString());
	}
}
