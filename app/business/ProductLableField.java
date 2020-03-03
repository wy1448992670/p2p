package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;
import models.t_product_lable_fields;
import play.Logger;
import play.db.jpa.JPA;

/**
 * 产品标签字段
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-7-8 下午08:45:01
 */
public class ProductLableField implements Serializable{
	public long id;
	//public long _id;
	public long lableId;
	public String name;
	public String content;
	public int type;
	public String description;
	
	//public ProductLable lable;
	
	//public long getId() {
	//	return _id;
	//}
	
	
//	public void setId(long id) {
//		t_product_lable_fields lables = null;
//
//		try {
//			lables = t_product_lable_fields.findById(id);
//		} catch (Exception e) {
//			Logger.error("产品标签字段->填充自己:" + e.getMessage());
//			this._id = -1;
//			
//			return;
//		}
//		
//		if (null == lables) {
//			this._id = -1;
//			
//			return;
//		}
//
//		this._id = lables.id;
//		this.lableId = lables.lable_id;
//		this.name = lables.name;
//		this.content = lables.content;
//		this.type = lables.type;
//		this.description = lables.description;
//	}

	/**
	 * 根据标签ID查询对应的字段集合
	 * @param lableId 标签ID
	 * @return List<ProductLableField>
	 */
	public static List<ProductLableField> queryFieldByLableId(long lableId){
		
		List<t_product_lable_fields> tfields = null;
		List<ProductLableField> fields = new ArrayList<ProductLableField>();
		
		try {
			tfields = t_product_lable_fields.find(" lable_id = ?", lableId).fetch();
		} catch (Exception e) {
			Logger.error("产品标签字段->添加/编辑标签字段:" + e.getMessage());
			
			return null;
		}
		
		if(null == tfields) return fields;
		
		ProductLableField field = null;
		
		for (t_product_lable_fields tfield : tfields) {
			field = new ProductLableField();
			
			field.id = tfield.id;
			field.lableId = tfield.lable_id;
			field.name = tfield.name;
			field.content = tfield.content;
			field.type = tfield.type;
			//field.description = tfielf.description;
			
			fields.add(field);
		}
		
		return fields;
	}
	
	/**
	 * 添加标签字段
	 * @param error 信息值
	 * @return ? > 0 : success; ? < 0 : fail
	 */
	public void create() {
		t_product_lable_fields field = new t_product_lable_fields();

		this.id = addOrEditField(field);
	}

	/**
	 * 编辑标签字段
	 * @param error 信息值
	 * @return ? > 0 : success; ? < 0 : fail
	 */
	public void editField(long id) {
		t_product_lable_fields field = null;

		try {
			field = t_product_lable_fields.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品标签字段->编辑标签字段:" + e.getMessage());

			return;
		}
		
		this.id = this.addOrEditField(field);
		
		if(this.id < 1) {
			JPA.setRollbackOnly();
		}
	}

	/**
	 * 添加/编辑标签字段
	 */
	private long addOrEditField(t_product_lable_fields field) {
		field.lable_id = this.lableId;
		field.name = this.name;
		field.content = this.content;
		field.type = this.type; // 0单行输入框 1多行输入框
		//field.description = this.description;

		try {
			field.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品标签字段->添加/编辑标签字段:" + e.getMessage());

			return -1;
		}

		return field.id;
	}
	
	/**
	 * 编辑字段
	 */
	public static int editField(long id, String name, String content){
		String hql = "update from t_product_lable_fields set name = ? , content = ? where id = ?";
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, name);
		query.setParameter(2, content);
		query.setParameter(3, id);
		
		try {
			return query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品标签字段->编辑字段:" + e.getMessage());

			return -1;
		}
	}
	
	/**
	 * 修改字段内容
	 * @param id ID
	 * @return
	 */
	public int editContent(long id) {
		String hql = "update from t_product_lable_fields set content = ? where id = ?";
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, this.content);
		query.setParameter(2, id);

		try {
			return query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品标签字段->编辑字段:" + e.getMessage());

			return -1;
		}
	}
	
	/**
	 * 根据标签ID删除字段
	 * @param lableId 标签ID
	 * @return
	 */
	public static int deleteFieldByLableId(long lableId){
		String hql = "delete from t_product_lable_fields where lable_id = ?";
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, lableId);
		
		try {
			return query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品标签字段->删除标签字段:" + e.getMessage());
			
			return -1;
		}
	}
	
	/**
	 * 根据ID删除字段
	 * @param id ID
	 * @return
	 */
	public static int deleteFieldById(long id){
		String hql = "delete from t_product_lable_fields where id = ?";
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, id);
		
		try {
			return query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品标签字段->删除标签字段:" + e.getMessage());
			
			return -1;
		}
	}
}
