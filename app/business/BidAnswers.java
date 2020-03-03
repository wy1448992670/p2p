package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Query;
import constants.Constants;
import constants.UserEvent;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import models.t_bid_answers;

public class BidAnswers implements Serializable{
	
	public long id;
	private long _id;
	public long questionId;  //标问题ID
	public Date time;  //回答时间
	public String content;  //回答内容
	public int readCount; //阅读次数
	
	public long getId(){
		return _id;
	}
	
	/**
	 * 回答
	 * @param questionId 提问ID
	 * @param content 内容
	 * @param error 信息值
	 * @return ? > 0 : success; ? < 0 : fail
	 */
	public void createAnswers(ErrorInfo error) {
		error.clear();

		t_bid_answers bidAnswers = new t_bid_answers();

		bidAnswers.bid_question_id = this.questionId;
		bidAnswers.content = this.content;
		bidAnswers.time = new Date();
		bidAnswers.read_count = 0;
		
		try {
			bidAnswers.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标提问回答->回答:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "回复失败!";
			
			return;
		}
		
		String hql = "update from t_bid_questions set is_answer = ? where id = ?";
		
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, Constants.ENABLE);
		query.setParameter(2, this.questionId);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标提问回答->修改问题为已回答:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "回复失败!";
		
			return;
		}
		
		if(rows == 0){
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "回复失败!";
			
			return;
		}
		
		/* 添加事件 */
		DealDetail.userEvent(User.currUser().id, UserEvent.ANSWERS_TO_QUESTION, "回答借款提问", error);
		
		if(error.code < 0){
			JPA.setRollbackOnly();
			error.msg = "回复失败!";
			
			return;
		}
		
		error.code = 0;
	}

	/**
	 * 查询针对某个提问的所有回答
	 * @param questionId 问题ID
	 * @return List<BidAnswers>
	 */
	public static List<BidAnswers> queryAnswers(long questionId, ErrorInfo error){
		error.clear();
		
		List<BidAnswers> answers = new ArrayList<BidAnswers>();
		List<t_bid_answers> tanswers = null;
		
		try {
			tanswers = t_bid_answers.find("bid_question_id = ?", questionId).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标提问回答->查询针对某个提问的所有回答:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "加载问题失败!";
			
			return null;
		}
		
		if(null == tanswers)
			return answers;
		
		BidAnswers answer = null;
		
		/* 填充,时间、内容、阅读次数 */
		for (t_bid_answers tanswer : tanswers) {
			answer = new BidAnswers();
			
			answer._id = tanswer.id;
			answer.time = tanswer.time;
			answer.content = tanswer.content;
			answer.readCount = tanswer.read_count;
			
			answers.add(answer);
		}
		
		error.code = 0;
		
		return answers;
	}
	
	/**
	 * 修改答案的阅读次数
	 * @param answerId 答案ID
	 * @param count 次数
	 * @param error 错误信息
	 * @return -1:失败     >0:成功; 
	 */
	public static int editAnswerReadCount(long answerId, ErrorInfo error) {
		error.clear();
		
		String hql = "update t_bid_answers a set a.read_count = a.read_count + 1 where a.id = ? ";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, answerId);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			Logger.error("标提问回答->查询针对某个提问的所有回答:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "加载问题失败!";
			
			return -1;
		}
		
		if(rows < 1) {
			JPA.setRollbackOnly();
			error.code = -2;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		return error.code;
   }
	
	/**
	 * 删除标提问对应的问题
	 * @param id ID
	 * @return 
	 */
	public static int delete(long questionId) {
		String hql = "delete from t_bid_answers where bid_question_id = ?";
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, questionId);
		
		try {
			return query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标提问回答->删除问题:" + e.getMessage());

			return -1;
		}
	}
}
