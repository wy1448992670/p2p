package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import models.t_messages_pact_templates;

public class TemplatePact implements Serializable {
	public int _id; // id 用int类型，谢谢 
	public int id;
	public Date time;
	public String title;
	public String content;
	public boolean is_use;
	
	public int getId() {
		return this._id;
	}

	public void setId(int id) {
		t_messages_pact_templates pact = null;
		
		try {
			pact = t_messages_pact_templates.findById(id);
		} catch (Exception e) {
			this._id = -1;
			
			return;
		}
		
		if(null == pact){
			this._id = -1;
			
			return;
		}
		
		this._id = pact.id;
		this.time = pact.time;
		this.title = pact.title;
		this.content = pact.content;
		this.is_use = pact.is_use;
		
	}
	
	public TemplatePact(){
		
	}
	
	
	/**
	 * 以下是业务逻辑方法
	 */
	
	
	
	/**
	 * 编辑协议模板
	 */
	public static void updatePact(TemplatePact pact,ErrorInfo error){
		
		t_messages_pact_templates pactTemplate = null;
		
		try {
			pactTemplate = t_messages_pact_templates.findById(pact.id);
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "查询对应协议模板异常";
			error.code = -1;
			return;
		}
		
		if(null != pact){
			pactTemplate.time = new Date();
			pactTemplate.title = pact.title;
			pactTemplate.content = pact.content;
			pactTemplate.is_use = pact.is_use;
		}
		
		try {
			pactTemplate.save();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "保存对应协议模板异常";
			error.code = -1;
			return;
		}
		
		error.msg = "保存对应协议模板成功";
		error.code = 1;
		return;
	}
	
	
	/**
	 * 改变对应协议模板启用或暂用
	 */
	public static void editStatus(int pactId, boolean status, ErrorInfo error){
		EntityManager em = JPA.em();
		int rows = 0;
		String sql = "update t_messages_pact_templates set is_use = ? where id = ? ";
		
		try {
			 rows = em.createQuery(sql).setParameter(1, status).setParameter(2, pactId).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "更新对应协议模板状态异常";
			error.code = -1;
			return;
		}
		
		if(rows == 0){
			error.msg = "更新对应协议模板状态异常";
			error.code = -1;
			return;
		}
		
		
		error.msg = "保存对应协议模板成功";
		error.code = 1;
		return;
	}
	
	
	/**
	 * 查询系统协议模板列表
	 * @return
	 */
	public static List<TemplatePact> queryAllPacts(ErrorInfo error){
		
		List<TemplatePact> pacts = new ArrayList<TemplatePact>();
		List<Integer> ids = new ArrayList<Integer>();
		
		String sql = "select id from t_messages_pact_templates";
		try {
			ids = t_messages_pact_templates.find(sql).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "查询平台协议模板列表异常";
			error.code = -1;
			
			return null;
		}
		
		if(ids.size() > 0){
			TemplatePact template = null;
			for(Integer id : ids){
				template = new TemplatePact();
				template.id = id;
				pacts.add(template);
			}
		}
		
		error.msg = "查询成功";
		error.code = 1;
		return pacts;
	}
	
	
	
}
