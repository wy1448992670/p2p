package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang.StringUtils;
import constants.Constants;
import constants.SupervisorEvent;
import constants.Templets;
import models.t_message_station_templates;
import models.t_system_message_sending;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.PageBean;

public class TemplateStation extends Template  implements Serializable{
	public long id;
	private long _id;

	public void setId(long id) {
		t_message_station_templates station = null;
		try {
			station = t_message_station_templates.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("setId,根据id查询站内信模板时" + e.getMessage());
			this._id = -1;

			return;
		}

		if (station == null) {
			this._id = -1;

			return;
		}

		this._id = station.id;
		this.time = station.time;
		this.scenarios = station.scenarios;
		this.title = station.title;
		this.content = station.content;
		this.size = station.size;
		this.status = station.status;
		this.type = station.type;
		this.replacedContent = replaceKeywords(this.content);
	}

	public long getId() {
		return _id;
	}

	public Date time;

	public String scenarios;
	public String title;
	public String content;
	public String replacedContent;
	public double size;
	public boolean status;
	public int type;
	
	public String getTitle() {
		if (null == title && this.id > 0) {
			try {
				String sql = "select title from t_message_station_templates where id = ?";
				this.title = t_message_station_templates.find(sql, this.id).first();
			} catch (Exception e) {
				e.printStackTrace();
				Logger.info("查询站内信模板标题时" + e.getMessage());
			}
		}
		
		return title;
	}

	public String getContent() {
		if (null == content && this.id > 0) {
			String sql = "select content from t_message_station_templates where id = ?";
			
			try {
				this.content = t_message_station_templates.find(sql, this.id).first();
			} catch (Exception e) {
				e.printStackTrace();
				Logger.info("查询站内信模板标题时" + e.getMessage());
			}
		}

		return content;
	}
	
	public String getReplacedContent() {
		return replaceKeywords(this.content);
	}

	/**
	 * 添加站内信模板
	 * @param info
	 * @return
	 */
	public int create(ErrorInfo error) {
		error.clear();

		t_message_station_templates station = new t_message_station_templates();

		station.time = new Date();
		station.scenarios = this.scenarios;
		station.title = this.title;
		station.content = Templets.replaceAllHTML(this.content);
		station.size = this.size;
		station.status = Constants.TRUE;
		station.type = this.type;

		try {
			station.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("create,添加站内信模板时" + e.getMessage());
			error.code = -1;
			error.msg = "根据站内信模板id查询失败";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.CREATE_MSG_TEMPLATE, "添加站内信模板", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		this._id = station.id;
		error.code = 0;
		error.msg = "添加站内信模板成功";

		return 0;
	}

	/**
	 * 编辑站内信模板
	 * @param id
	 * @param error
	 * @return
	 */
	public int edit(long id, ErrorInfo error) {
		error.clear();

		t_message_station_templates station = null;

		try {
			station = t_message_station_templates.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("edit,根据站内信模板查询站内信时" + e.getMessage());
			error.code = -1;
			error.msg = "根据站内信模板id查询失败";

			return error.code;
		}

		if (station == null) {
			error.code = -2;
			error.msg = "该站内信模板不存在";

			return error.code;
		}

		station.scenarios = this.scenarios;
		station.title = this.title;
		station.content = Templets.replaceAllHTML(this.content);
		station.size = this.size;

		try {
			station.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("edit,保存站内信模板时" + e.getMessage());
			error.code = -3;
			error.msg = "保存站内信模板失败";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.EDIT_MSG_TEMPLATE, "编辑站内信模板", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "站内信模板编辑成功";

		return 0;
	}

	/**
	 * 更新站内信模板状态
	 * @param id
	 * @param status true使用 false暂停
	 * @param info
	 * @return
	 */
	public static int updateStatus(long id, boolean status, ErrorInfo error) {
		error.clear();

		EntityManager em = JPA.em();
		Query query = em
				.createQuery("update t_message_station_templates set status = ? where id = ?")
				.setParameter(1, status).setParameter(2, id);
		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("updateStatus,根据站内信模板查询站内信时" + e.getMessage());
			error.code = -1;
			error.msg = "更新站内信模板状态失败";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}

		if (status) {
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ENABLE_MSG_TEMPLATE, "启用站内信模板", error);
		} else {
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DISABLE_MSG_TEMPLATE, "暂停站内信模板", error);
		}

		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "更新站内信模板状态成功";
		
		return error.code;
	}
	
	/**
	 * 查询站内信模板
	 * @param templateType  0 自定义模板 1 系统模板
	 * @param currPage
	 * @param pageSize
	 * @param type
	 * @param keyword
	 * @param error
	 * @return
	 */
	public static PageBean<t_message_station_templates> query(int templateType, int currPage, int pageSize, int type, String keyword, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		String condition = "1 = 1";
		List<Object> params = new ArrayList<Object>();
		
		condition += " and type = ?";
		params.add(templateType);
		
		if (StringUtils.isNotBlank(keyword)) {
			if(type == 1) {
				condition += " and title like ?";
				params.add("%" + keyword + "%");
			}else if(type == 2) {
				condition += " and content like ?";
				params.add("%" + keyword + "%");
			}
		}
		
		int count = 0;
		List<t_message_station_templates> page = null;
		
		try {
			count = (int) t_message_station_templates.count(condition, params.toArray());
			page = t_message_station_templates.find(condition, params.toArray()).fetch(currPage, pageSize);
		} catch(Exception e) {
			e.printStackTrace();
			Logger.error("查询站内信板时"+e.getMessage());
			error.code = -1;
			error.msg = "查询站内信模板失败";
			
			return null;
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("type", type);
		map.put("currPage", currPage);
		map.put("pageSize", pageSize);
		
		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}
		
		PageBean<t_message_station_templates> bean = new PageBean<t_message_station_templates>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.page = page;
		bean.totalCount = (int) count;
		bean.conditions = map;
		
		error.code = 0;

		return bean;
	}
	
	/**
	 * 查询站内信模板场景
	 * @param info
	 * @return
	 */
	public static List<TemplateStation> queryScenarios(ErrorInfo error) {
		error.clear();

		List<t_message_station_templates> templates = null;
		String sql = "select new t_message_station_templates(id,scenarios) from t_message_station_templates where type = 1 and status = 1";

		try {
			templates = t_message_station_templates.find(sql).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("queryScenarios,查询站内信模板场景时" + e.getMessage());
			error.code = -1;
			error.msg = "查询站内信模板场景失败";

			return null;
		}

		if (templates == null || templates.size() == 0) {
			error.code = 0;
			error.msg = "无";

			return new ArrayList<TemplateStation>();
		}

		List<TemplateStation> stations = new ArrayList<TemplateStation>();
		TemplateStation station = null;

		for (t_message_station_templates template : templates) {

			station = new TemplateStation();
			station._id = template.id;
			station.scenarios = template.scenarios;
			stations.add(station);
		}
		
		error.code = 0;

		return stations;
	}
	
	/**
	 * 发送系统站内信
	 * @param receiveUserId
	 * @param title
	 * @param content
	 */
	public static void addMessageTask(long receiveUserId, String title, String content) {
		t_system_message_sending sending = new t_system_message_sending();
		
		sending.time = new Date();
		sending.receiver_user_id = receiveUserId;
		sending.title = title;
		sending.body = content;
		
		try {
			sending.save();
		}catch(Exception e){
			Logger.info("发送系统站内信时："+e.getMessage());
		}
	}
	
	/**
	 * 定时任务发送系统站内信
	 */
	public static void dealStationTask() {
		ErrorInfo error = new ErrorInfo();
		List<t_system_message_sending> sendings = null;
		
		try {
			sendings = t_system_message_sending.find("").fetch(Constants.JOB_STATION_AMOUNT);
		} catch (Exception e) {
			Logger.error("定时任务发送系统站内信" + e.getMessage());
			
			return ;
		}
		
		int len = sendings == null ? 0 : sendings.size();
		
		if(len == 0)
			return ;
		
		StationLetter letter = null;
		
		for(t_system_message_sending sending : sendings) {
			 letter = new StationLetter();
			 
			 letter.senderSupervisorId = 1;
			 letter.receiverUserId = sending.receiver_user_id;
			 letter.title = sending.title;
			 letter.content = sending.body;
			 
			 letter.sendToUserBySupervisor(error);
		}
		
		long maxSendId = sendings.get(len - 1).id;
		JPA.em()
		.createNativeQuery("delete from t_system_message_sending where id <= ?")
		.setParameter(1, maxSendId)
		.executeUpdate();
	}
	
	/**
	 * 获得站内信的模板信息
	 * @param id
	 * @param error
	 * @return
	 */
	public static TemplateStation getStationTemplate(long id, ErrorInfo error) {
		error.clear();
		TemplateStation station = (TemplateStation) Cache.get("STATION_"+id);
		t_message_station_templates stationTemplate = null;
		
		if(station == null) {
			try{
				stationTemplate = t_message_station_templates.findById(id);
			}catch(Exception e) {
				Logger.error("查询站内信模板时：%s", e.getMessage());
				error.code = -1;
				error.msg = "查询站内信模板失败";
				
				return null;
			}
			
			if(error.code < 0) {
				return null;
			}
			
			station = new TemplateStation();
			
			station.setInfo(stationTemplate);
			
			Cache.set("STATION_"+stationTemplate.id, station);
			
			return station;
		}
		
		return station;
	}

	/**
	 * 赋值信息
	 */
	public void setInfo(t_message_station_templates station) {
		this._id = station.id;
		this.time = station.time;
		this.scenarios = station.scenarios;
		this.title = station.title;
		this.content = station.content;
		this.size = station.size;
		this.status = station.status;
		this.type = station.type;
		this.replacedContent = replaceKeywords(this.content);
	}
}
