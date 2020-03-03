package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Query;
import models.t_product_audit_items;
import models.t_product_audit_items_log;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;

/**
 * 产品资料
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-7-18 下午05:29:00
 */
public class ProductAuditItem implements Serializable{
	public long id;
	private long _id;
	public boolean getPai;
	public long productId;
	public Date time;
	public boolean type;
	public String mark;

	public AuditItem auditItem;
	public long auditItemId;
	private long _auditItemId;

	/**
	 * 获取ID
	 */
	public long getId() {
		return this._id;
	}

	public String getMark() {
		return this.mark;
	}

	/**
	 * 获取资料ID
	 */
	public long getAuditItemId() {
		return _auditItemId;
	}

	/**
	 * 填充资料库对象
	 */
	public void setAuditItemId(long auditItemId) {
		this._auditItemId = auditItemId;

		if (null == this.auditItem) {
			this.auditItem = new AuditItem();
			this.auditItem.getPai = this.getPai;
			this.auditItem.id = auditItemId;
		}
	}

	/**
	 * 填充自己
	 */
	public void setId(long id) {
		t_product_audit_items_log item = null;

		try {
			item = t_product_audit_items_log.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品资料->填充自己" + e.getMessage());
			this._id = -1;

			return;
		}

		this._id = item.id;

		if (this.getPai) {
			this.auditItemId = item.audit_item_id;

			return;
		}

		this.productId = item.product_id;
		this.auditItemId = item.audit_item_id;
		this.time = item.time;
		this.type = item.type;

		this._id = item.id;
		this.mark = item.mark;
		this.productId = item.product_id;
		this.auditItemId = item.audit_item_id;
		this.time = item.time;
		this.type = item.type;
	}

	/**
	 * 根据mark查询备份表数据
	 * @param mark 唯一标示
	 * @param flag 是否全部查询
	 * @param type 可选/必选
	 * @return List<ProductAuditItem>
	 */
	public static List<ProductAuditItem> queryAuditByProductMark(String mark,
			boolean isAll, boolean isNeed) {
		List<ProductAuditItem> pais = new ArrayList<ProductAuditItem>();
		List<Long> ids = null;
		String hql = "select id from t_product_audit_items_log where mark = ?";

		if (!isAll) {
			hql += " and type = " + isNeed;
		}
		
		try {
			ids = t_product_audit_items_log.find(hql, mark).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->查询产品 全部/必须/可选 审核的科目:" + e.getMessage());

			return pais;
		}

		if (null == ids || ids.size() == 0) 
			return pais;
		
		ProductAuditItem item = null;

		for (Long id : ids) {
			item = new ProductAuditItem();
			item.getPai = true;
			item.id = id;

			pais.add(item);
		}

		return pais;
	}

	/**
	 * 查询资料关联到的产品名称
	 * @param aid 资料ID
	 * @param error 信息值
	 * @return List<String>
	 */
	public static List<String> queryProductName(long auditItemId) {
		String hql = "select p.small_image_filename from t_product_audit_items pai, t_products p "
				   + "where pai.product_id = p.id and pai.audit_item_id = ? group by pai.product_id";

		try {
			return t_product_audit_items_log.find(hql, auditItemId).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->查询资料关联到的产品名称:" + e.getMessage());

			return null;
		}
	}

	/**
	 * 查询产品对应的审核资料数量(包含 必选/可选)
	 * @param pid 产品ID
	 * @param error 信息值
	 * @return ? > 0 : success; ? < 0 : fail
	 */
	public static int queryAuditCount(long pid, ErrorInfo error) {
		error.clear();

		try {
			return (int) t_product_audit_items.count("product_id= ? ", pid);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->查询产品对应的审核资料数量:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "查询数量失败!";

			return -1;
		}
	}

	/**
	 * 根据产品ID，删除对应的所有资料
	 * @param pid 产品ID
	 * @return ? > 0 : success; ? < 0 : fail
	 */
	public static int deleteProductAudit(long pid) {
		String hql = "delete from t_product_audit_items where product_id=?";

		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, pid);

		try {
			return query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->根据产品ID,删除对应的所有资料" + e.getMessage());

			return -1;
		}
	}

	/**
	 * 根据产品ID,资料ID，添加产品对应的资料
	 * @param pid 产品ID
	 * @param aid 资料ID
	 * @return ? > 0 : success; ? < 0 : fail
	 */
	public static int createroductAuditItem(long pid, long aid, boolean type,
			String mark) {
		t_product_audit_items item = new t_product_audit_items();

		item.product_id = pid;
		item.time = new Date();
		item.audit_item_id = aid;
		item.type = type;
		item.mark = mark;

		try {
			item.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->添加产品对应的资料" + e.getMessage());

			return -1;
		}

		if (item.id < 1)
			return -2;

		return createroductAuditItemLog(item);
	}

	/**
	 * 添加备份表
	 */
	private static int createroductAuditItemLog(t_product_audit_items item) {
		t_product_audit_items_log log = new t_product_audit_items_log();

		log.product_id = item.product_id;
		log.time = item.time;
		log.audit_item_id = item.audit_item_id;
		log.type = item.type;
		log.mark = item.mark;

		try {
			log.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料-> 添加备份标:" + e.getMessage());

			return -1;
		}

		return log.id < 1 ? -2 : 1;
	}
}
