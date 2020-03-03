package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.shove.Convert;

import constants.SupervisorEvent;
import models.t_content_advertisements_links;
import models.t_content_advertisements_partner;
import models.t_user_msg;
import models.v_user_msg;
import models.v_user_success_invest_bids;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.PageBean;
/**
 * 
* Description:用户心声
* @author xinsw
* @date 2017年4月11日
 */
public class UserMsg implements Serializable{
	
	public static PageBean<v_user_msg> queryUserMsg(int currPage, int pageSize, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		List<v_user_msg> page = new ArrayList<v_user_msg>();
		String sqlCount = "select count(*) from t_user_msg m JOIN t_users u on m.user_id = u.id ";
		String sqlPage = "select m.*,u.photo,u.name from t_user_msg m JOIN t_users u on m.user_id = u.id order by id desc";
		EntityManager em = JPA.em();
		PageBean<v_user_msg> bean = new PageBean<v_user_msg>();
		try {
			Query query = em.createNativeQuery(sqlPage.toString(),v_user_msg.class);
            Query queryCount = em.createNativeQuery(sqlCount.toString());
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            page = query.getResultList();
            bean.totalCount = Convert.strToInt(queryCount.getResultList().get(0)+"",0);
            bean.setTotalNum(bean.totalCount);
		} catch (Exception e) {
			Logger.error("查询用户心声:" + e.getMessage());
			error.code = -1;
			error.msg = "加载用户心声失败!";
			e.printStackTrace();
			return null;
		}
		
		
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.page = page;
		bean.conditions = null;
		
		error.code = 0;

		return bean;
	}
	
	public static int addMsg(t_user_msg msg,ErrorInfo error){
		error.clear();
		
		try {
			msg.ins_dt = new Date();
			msg.save();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("添加留言心声时：" + e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致添加留言心声失败！";
			
			return error.code;
		}

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;

		}
		
		
		error.code = 0;
		error.msg = "留言心声添加成功！";

		return 0;
	}
}
