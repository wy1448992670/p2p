package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.PageBean;
import constants.Constants;
import constants.SupervisorEvent;
import models.t_dict_secret_questions;

public class SecretQuestion implements Serializable{
	
	/**
	 * 下面的字段用于管理员添加或修改安全问题
	 */
	public long id;

	public String name;  
	public String type;  
	public int useCount;
	public String status;
	public boolean isUse;
	
	/**
	 * 管理员添加安全问题
	 * @param id 管理员id
	 * @param info
	 * @return
	 */
	public int addSafeQuestion(ErrorInfo error) {
		error.clear();
		
		if(isQuestionExist(this.name, this.type)) {
			error.code = -1;
			error.msg = "安全问题已存在！";
			
			return error.code;
		}
		
		t_dict_secret_questions question = new t_dict_secret_questions();
		
		question.name = this.name;
		question.type = this.type;
		question.is_use = Constants.TRUE;
		
		try {
			question.save();
		}catch (Exception e) {
			e.printStackTrace();
			Logger.error("管理员添加安全问题，保存安全问题时："+e.getMessage());
			error.code = -1;
			error.msg = "保存安全问题失败";

			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ADD_SECRET_QUESTION, "添加安全问题", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "安全问题添加成功！";
		
		return 0;
		
	}
	
	/**
	 * 判断该安全问题是否已经存在
	 * @param name
	 * @param type
	 * @return
	 */
	public static boolean isQuestionExist(String name, String type) {
		long count = -1;
		
		try {
			count = t_dict_secret_questions.count("name = ?  and type = ?", name, type);
		}catch (Exception e) {
			e.printStackTrace();
			Logger.error("管理员添加安全问题，保存安全问题时："+e.getMessage());

			return true;
		}
		
		if(count > 0) {
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * 修改安全问题的状态
	 * @param supervisorId
	 * @param id
	 * @param status
	 * @param info
	 * @return
	 */
	public static int updateStatus(long id, boolean status, ErrorInfo error) {
		error.clear();
		
		String sql = "update t_dict_secret_questions set is_use = ? where id = ?";
		EntityManager em = JPA.em();
		Query query = em.createQuery(sql).setParameter(1, !status).setParameter(2, id);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		}catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.error("修改安全问题的状态，更新安全问题状态时："+e.getMessage());
			error.code = -1;
			error.msg = "保存安全问题失败";

			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.UPDATE_QUESTION_STATUS, 
				status ? "启用安全问题" : "暂停安全问题", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "状态更新成功！";
		
		return 0;
	}
	
	/**
	 * 根据条件查询安全问题
	 * @param superviosrId
	 * @param name
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static PageBean<SecretQuestion> query(String name, int currPage, int pageSize) {
		
		if(currPage <= 0) {
			currPage = 1;
		}
		
		if(pageSize <= 0) {
			currPage = Constants.PAGE_SIZE;
		}
		
		PageBean<SecretQuestion> page = new PageBean<SecretQuestion>();
		page.currPage = currPage;
		page.pageSize = pageSize;
		
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		List<Object> values = new ArrayList<Object>();
		StringBuffer conditions = new StringBuffer("1=1 ");
		conditionMap.put("name", name);
		
		if(!StringUtils.isBlank(name)) {
			conditions.append("and name like ?");
			values.add("%" + name + "%");
		}
		
		List<t_dict_secret_questions> questions = null;
		List<SecretQuestion> secretQuestions = new ArrayList<SecretQuestion>();
		
		try {
			page.totalCount = (int) t_dict_secret_questions.count(conditions.toString(), values.toArray());
			questions = t_dict_secret_questions.find(conditions.toString(), values.toArray()).fetch(page.currPage,page.pageSize);
		}catch (Exception e) {
			e.printStackTrace();
			Logger.error("根据条件查询安全问题，查询安全问题时："+e.getMessage());

			return null;
		}
		
		
		SecretQuestion secretQuestion = null;
		
		for(t_dict_secret_questions question : questions) {
			secretQuestion = new SecretQuestion();
			
			secretQuestion.id = question.id;
			secretQuestion.name = question.name;
			secretQuestion.type = question.type;
			secretQuestion.useCount = question.use_count;
			secretQuestion.isUse = question.is_use;
			
			secretQuestions.add(secretQuestion);
		}
		
		page.conditions = conditionMap;
		page.page = secretQuestions;
		
		return page;
	}
	
	/**
	 * 查询使用中的安全问题，供前台使用
	 * @return
	 */
	public static List<SecretQuestion> queryUserQuestion() {
		
		List<t_dict_secret_questions> questions = null;
		List<SecretQuestion> secretQuestions = new ArrayList<SecretQuestion>();
		
		try {
			questions = t_dict_secret_questions.find("is_use = ?", true).fetch();
		}catch (Exception e) {
			e.printStackTrace();
			Logger.error("根据条件查询安全问题，查询安全问题时："+e.getMessage());
			
			return null;
		}
		
		if(questions == null || questions.size() == 0) {

			return secretQuestions;
		}
		
		SecretQuestion secretQuestion = null;
		
		for(t_dict_secret_questions question : questions) {
			secretQuestion = new SecretQuestion();
			
			secretQuestion.id = question.id;
			secretQuestion.name = question.name;
			secretQuestion.type = question.type;
			secretQuestion.useCount = question.use_count;
			secretQuestion.isUse = question.is_use;
			
			secretQuestions.add(secretQuestion);
		}
		
		return secretQuestions;
	}
	
	/**
	 * 根据id查询问题内容
	 * @param id
	 * @param error
	 * @return
	 */
	public static String queryQuestionById(long id, ErrorInfo error) {
		error.clear();
		String sql = "select name from t_dict_secret_questions where id = ?";
		try {
			return t_dict_secret_questions.find(sql, id).first();
		}catch (Exception e) {
			e.printStackTrace();
			Logger.error("根据条件查询安全问题，查询安全问题时："+e.getMessage());
			
			error.code = -1;
			error.msg = "查询问题失败";
			
			return null;
		}
	}
}
