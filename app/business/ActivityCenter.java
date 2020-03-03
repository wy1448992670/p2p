package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;



import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;

import models.t_activity_center;

public class ActivityCenter implements Serializable{
	public long id;
	public String title;
	public int order;
	public String url;
	public String first_image_url;
	public String info_image_url;
	public String rule_image_url;
	public String location;
	public String resolution;
	public String file_size;
	public String file_format;
	
	
	
	/**
	 * 查询活动中心
	 * @param title
	 * @return
	 */
	public static List<t_activity_center> queryActivityCenter(String title){
		
		List<t_activity_center> list = new ArrayList<t_activity_center>();
		
		try {
			if(StringUtils.isNotBlank(title)){
				list = t_activity_center.find(" title like ? order by _order desc","%"+title+"%").fetch();
			}else{
				list = t_activity_center.find(" order by _order desc").fetch();
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
			Logger.info("查询活动中心出错", e);
			
		}
		return list;
	}
	
	/**
	 * 删除活动
	 * @param id
	 */
	public static void deleteActivityCenter(long id){
		t_activity_center.delete(" id = ? ", id);
	}
	
	public int saveActivity(ErrorInfo error){
		t_activity_center t = new t_activity_center();
		t.title = this.title;
		t.location = this.location;
		t._order = this.order;
		t.url = this.url;
		t.first_image_url = this.first_image_url;
		t.info_image_url = this.info_image_url;
		t.rule_image_url = this.rule_image_url;
		t.resolution = this.resolution ;
		t.file_size = this.file_size;
		t.file_format = this.file_format;
		try {
			t.save();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "添加活动失败";
			return 0;
		}
		error.code = 0;
		error.msg = "添加活动成功";
		return 1;
	}
	
	public static t_activity_center queryActivityCenter(long id){
		t_activity_center t = t_activity_center.findById(id);
		return t;
	}
	
	public int eidtActivityCenter(ErrorInfo error){
		
		String sql = "update t_activity_center set title = ?,_order = ?,url = ?,first_image_url = ?,info_image_url = ?,rule_image_url = ? where id = ?";
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, this.title).setParameter(2, this.order).setParameter(3, this.url)
		.setParameter(4, this.first_image_url).setParameter(5, this.info_image_url).setParameter(6, this.rule_image_url).setParameter(7, this.id);
		
		try {
			query.executeUpdate();
		} catch (Exception e) {
			
			e.printStackTrace();
			Logger.info("编辑活动内容失败", e.toString());
			error.code = -1;
			error.msg = "编辑活动内容失败";
		}
		error.code = 0;
		error.msg = "编辑活动内容成功";
		return 0;
	}
}
