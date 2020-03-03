package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;
import models.t_product_lables;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;

/**
 * 产品标签
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-7-8 下午08:43:46
 */
public class ProductLable implements Serializable{
	public long id; // 标签ID
	//private long _id; // 标签ID
	public long productId; // 产品ID
	public String name; // 标签名称
	//public String description; // 标签描述

	public Product product; // 产品
	public List<ProductLableField> fields; // 字段集合
	public ProductLableField [] fieldsArray; // 字段集合
	
	//public long getId() {
	//	return _id;
	//}

//	public void setId(long id) {
//		t_product_lables lables = null;
//
//		try {
//			lables = t_product_lables.findById(id);
//		} catch (Exception e) {
//			Logger.error("产品标签->填充自己:" + e.getMessage());
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
//		this.productId = lables.product_id; // 产品ID
//		this.name = lables.name; // 标签名称
//		//this.description = lables.description; // 标签描述
//	}

	/**
	 * 根据产品ID查询标签
	 * @param productId 产品ID
	 * @return List<ProductLable>
	 */
	public static List<ProductLable> queryProductLableByProductId(long productId){
		
		List<t_product_lables> tlables = null;
		List<ProductLable> lables = new ArrayList<ProductLable>();
		
		try {
			tlables = t_product_lables.find(" product_id = ?", productId).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品标签->编辑标签:" + e.getMessage());
			
			return null;
		}
		
		if(null == tlables) {
			return lables;
		}
		
		ProductLable lable = null;
		
		for (t_product_lables tlable : tlables) {
			lable = new ProductLable();
			
			lable.id = tlable.id;
			lable.productId = tlable.product_id;
			lable.name = tlable.name;
			//lable.description = tlable.description;
			
			lables.add(lable);
		}
		
		return lables;
	}
	
	/**
	 * 检查名称是否存在
	 * @param name 名称
	 * @return true : 存在,false : 不存在
	 */
	public static boolean checkName(String name){
		String hql = "select name from t_product_lables where name = ?";

		try {
			name = t_product_lables.find(hql, name.trim()).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品标签->根据name查询name:" + e.getMessage());
			
			return true;
		}
		
		if(null == name) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 字段集合
	 * @return
	 */
	public List<ProductLableField> getFields() {
		if(null == this.fields) 
			this.fields = ProductLableField.queryFieldByLableId(this.id);
		
		return this.fields;
	}

	/**
	 * 添加标签
	 * @param error 信息值
	 * @return ? > 0 : success; ? < 0 : fail
	 */
	public void create() {
		t_product_lables lable = new t_product_lables();
		
		this.id = this.addOrEditLable(lable);
	}

	/**
	 * 编辑标签
	 * @param error 信息值
	 * @return ? > 0 : success; ? < 0 : fail
	 */
	public void editLable(long id) {
		t_product_lables lable = null;

		try {
			lable = t_product_lables.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品标签->编辑标签:" + e.getMessage());

			return;
		}
		
		this.id = this.addOrEditLable(lable);
	}

	/**
	 * 添加/编辑标签
	 */
	private long addOrEditLable(t_product_lables lable) {
		lable.product_id = this.productId;
		lable.name = this.name;
		//lable.description = this.description;

		try {
			lable.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品标签->添加/编辑标签:" + e.getMessage());

			return -1;
		}

		return lable.id;
	}
	
//	/**
//	 * 更新标签的产品ID
//	 * @param productId 产品ID
//	 * @return
//	 */
//	public static int editLableProductId(long productId, long id){
//		String hql = "update t_product_lables set product_id = ? where id = ?";
//		Query query = JPA.em().createQuery(hql);
//		query.setParameter(1, productId);
//		query.setParameter(2, id);
//		
//		try {
//			return query.executeUpdate();
//		} catch (Exception e) {
//			Logger.error("产品标签->更新标签的产品ID:" + e.getMessage());
//
//			return -1;
//		}
//	}
	
	/**
	 * 删除标签
	 */
	public static void deleteLableById(long id, ErrorInfo error) {
		error.clear();
		/* 删除字段 */
		int code = ProductLableField.deleteFieldByLableId(id);

		if (code < 0)
			return;

		Query query = JPA.em().createQuery("delete from t_product_lables where id = ?");
		query.setParameter(1, id);

		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品标签->删除标签:" + e.getMessage());

			return;
		}
		
		if(rows == 0){
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "删除失败!";
			
			return;
		}
		
		error.code = 0;
	}
	
	/**
	 * 删除没有管理产品ID的垃圾数据
	 */
	public static int deleteLable() {
		List<Long> ids = null;
		String hql = "select id from t_product_lables where product_id = 0";
		
		try {
			ids = t_product_lables.find(hql).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品标签->删除标签,查询没有关联产品ID的集合:" + e.getMessage());

			return -1;
		}
		
		int code = -1;
		
		if(null != ids){
			for (Long lableId : ids) {
				code = ProductLableField.deleteFieldByLableId(lableId);
				
				if(code < 0) return -2;
			}
		}
			
		Query query = JPA.em().createQuery("delete from t_product_lables where product_id = 0");

		try {
			return query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品标签->删除没有关联产品ID的标签:" + e.getMessage());

			return -3;
		}
	}
}
