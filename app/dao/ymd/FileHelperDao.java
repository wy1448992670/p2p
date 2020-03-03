/******************************************************************
 *
 *    Java Lib For Android, Powered By Shenzhen Jiuzhou.
 *
 *    Copyright (c) 2001-2014 Digital Telemedia Co.,Ltd
 *    http://www.d-telemedia.com/
 *
 *    Package:     dao.ymd
 *
 *    Filename:    FileHelperDao.java
 *
 *    Description: TODO(用一句话描述该文件做什么)
 *
 *    Copyright:   Copyright (c) 2001-2014
 *
 *    Company:     Digital Telemedia Co.,Ltd
 *
 *    @author:     zj
 *
 *    @version:    1.0.0
 *
 *    Create at:   2018年12月25日 下午3:29:20
 *
 *    Revision:
 *
 *    2018年12月25日 下午3:29:20
 *        - first revision
 *
 *****************************************************************/
package dao.ymd;

import java.util.Date;

import javax.persistence.Query;

import models.file.t_file;
import models.file.t_file_relation;
import models.file.t_file_relation_dict;
import play.db.jpa.JPA;
import utils.DateUtil;
import utils.JPAUtil;

/**
 * @ClassName FileHelperDao
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年12月25日 下午3:29:20
 * @version 1.0.0
 */
public class FileHelperDao {
	/**
	 * 文件有效期 默认6个月
	 */
	static int MONTH = 6;

	/**
	 * @Description 保存文件
	 * @param realPath
	 * @param userId
	 * @param fileDictId 外键： t_file_dict 表的主键
	 * @author: zj
	 */
	public static t_file saveFile(int fileDictId, String realPath, int uploaderType, long userId) {
		t_file file = new t_file();
		file.file_dict_id = fileDictId;
		file.real_path = realPath;
		file.uploader_type = uploaderType;
		file.uploader_id = userId;
		file.expiry_time=DateUtil.addMonth(new Date(), MONTH);
		file.create_time = new Date();
		return file.save();
	}

	/**
	 * @Description 保存 t_file_relation_dict 表记录
	 * @param relationType
	 * @param fileDictId   流程节点 1.实名认证 2.活体认证3.银行卡认证 4,运营商认证 5.基础信息-补充资料 <br>
	 *                     6.下单--资料上传 7.提额--补充资料 8.合作机构创建
	 * @param requiredNum  至少文件数量
	 * @param maxNum       最多文件数量
	 * @param sequence     在业务表中的顺序
	 * @author: zj
	 */
	public static void saveFileRelationDict(int relationType, int fileDictId, int requiredNum, int maxNum, int sequence,
			boolean isVisible, boolean canRewrite) {
		t_file_relation_dict file_relation_dict = new t_file_relation_dict();
		file_relation_dict.relation_type = relationType;
		file_relation_dict.file_dict_id = fileDictId;
		file_relation_dict.required_num = requiredNum;
		file_relation_dict.max_num = maxNum;
		file_relation_dict.sequence = sequence;
		file_relation_dict.is_visible = isVisible;
		file_relation_dict.can_rewrite = canRewrite;
		file_relation_dict.save();
	}

	/**
	 * @Description 新增 t_file_relation 表记录
	 * @param relationType t_file_relation_dict 表中的 relation_type 字段值
	 * @param relationId   业务表外键 根据 relationType 不同，可以存放亿用户id 申请表id 等等
	 * @param fileId       t_file 主键 对应着 具体文件的路径信息记录
	 * @param isVisible    客户端,公开信息中是否显示 1 显示 2 不显示
	 * @author: zj
	 */
	public static t_file_relation saveFileRelation(int relationType, long relationId, long fileId, boolean isVisible,
			int fileDictSequence, boolean canRewrite, boolean isTemp) {
		t_file_relation file_relation = new t_file_relation();
		file_relation.relation_type = relationType;
		file_relation.relation_id = relationId;
		file_relation.file_id = fileId;
		file_relation.is_visible = isVisible;
		file_relation.file_dict_sequence = fileDictSequence;
		file_relation.can_rewrite = canRewrite;
		file_relation.is_temp = isTemp;
		file_relation.save();
		return file_relation;
	}

	/**
	 * @Description 删除实名认证时，上传的身份证信息
	 * @param relationId 用户id
	 * @author: zj
	 */
	public static void delFileRelationById(long file_relation_id) {
		String sqlString = " delete  from t_file_relation where id=? ";
		Query query = JPA.em().createQuery(sqlString);
		query.setParameter(1, file_relation_id);
		query.executeUpdate();
	}

	/**
	 * @param fileDictId   图片类型id t_file_dict 表的主键
	 * @param uploaderType 上传人类型 1 普通用户 2 管理员
	 * @param relationType 业务类型
	 *                     1.users身份证照片实名认证2.users活体认证3.users用户风控资料4.credit_apply5.increase_credit_apply6.borrow_apply7.users.user_type
	 *                     in (2,3)企业用户资料8.orgnization合作机构资料
	 * @param relationId   业务类型id 可以是 userId applyId 等
	 * @param isVisible    是否可见 true false
	 * @param canRewrite   是否重写
	 * @param isTemp       是否缓存
	 * @param requiredNum  最少数量
	 * @param maxNum       最多数量
	 * @param sequence     排序
	 * @return 文件入库返回的主键id
	 * @author: zj
	 * @Description app 或者 pc 端 文件选择完毕，一起提交 通用方法
	 */
	public static long saveImageInfo(int fileDictId, String realPath, int uploaderType, int relationType,
			long relationId, boolean isVisible, boolean canRewrite, boolean isTemp, int requiredNum, int maxNum,
			int sequence) {
		t_file file = FileHelperDao.saveFile(fileDictId, realPath, uploaderType, relationId);
		t_file_relation fileRelation=FileHelperDao.saveFileRelation(relationType, relationId, file.id, isVisible, sequence, canRewrite, isTemp);
		// FileHelperDao.saveFileRelationDict(relationType, fileDictId, requiredNum,
		// maxNum, sequence, isVisible,
		// canRewrite);
		return fileRelation.id;
	}

	/**
	 * @Description 删除 t_file_relation 表
	 * @param relationType 节点类型
	 * @param relationId   业务表主键
	 * @param fileId       文件id
	 * @author: zj
	 */
	@Deprecated
	public static void delFileRelation(int relationType, long relationId, long fileId) {
		String sqlString = " delete from t_file_relation where relation_type=? and relation_id=? and file_id=? ";
		t_file_relation.delete(sqlString, relationType, relationId, fileId);
	}
	
	/**
	 * @Description 删除 t_file_relation 表
	 * @param relationType 节点类型
	 * @param relationId   业务表主键
	 * @param fileId       文件id
	 * @author: zj
	 */
	public static void delFileRelationByFileDict(int relationType, long relationId, long fileDictId) {
		String sqlString = " delete t_file_relation " + 
				" from t_file_relation " + 
				", t_file " + 
				" where  t_file.id=t_file_relation.file_id and relation_type=? and relation_id=? and t_file.file_dict_id=? ";
		//t_file_relation.delete(sqlString, relationType, relationId, fileDictId);
		Query query=JPAUtil.createNativeQuery(sqlString,  relationType, relationId, fileDictId);
		int rows =query.executeUpdate();
	}
	
	
}
