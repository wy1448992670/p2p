package services.file;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import models.file.t_file;
import models.file.t_file_relation;
import models.file.t_file_relation_dict;

public class FileService {

	/**
	 * 文件关系
	 * 1.users身份证照片实名认证
	 * 2.users活体认证
	 * 3.users用户风控资料
	 * 4.credit_apply
	 * 5.increase_credit_apply
	 * 6.borrow_apply
	 * 7.users.user_type in (2,3)企业用户资料
	 * 8.orgnization合作机构资料
	 * 9.credit_apply第三方报告
	 */
	/**
	 * 通过relation_type得到对应的需要上传的relation_dict列表
	 * @param relation_type
	 * @return
	 */
	public static List<t_file_relation_dict> getFileDictByRelationType(Integer relation_type) {
		List<t_file_relation_dict> fileDictList=t_file_relation_dict.find("relation_type=? order by sequence", relation_type).fetch();
		if(fileDictList.size()==0) {
			throw new RuntimeException("Not find file update relation");
		}
		Collections.sort(fileDictList); //按sequence排序
		return fileDictList;
	}
	/**
	 * 通过relation_type,relation_id得到对应的relation_dict列表,
	 * 用于更新文件使用,其中每个relation_dict包含已上传的file_relation列表,存放已上传的文件
	 * @param relation_type
	 * @param relation_id
	 * @return
	 */
	public static List<t_file_relation_dict> getFileUpdateByRelation(Integer relation_type,Long relation_id) {
		List<t_file_relation_dict> fileDictList=getFileDictByRelationType(relation_type);
		List<t_file_relation> fileRelationList=t_file_relation.find("relation_type=? and relation_id=? order by file_dict_sequence", relation_type,relation_id).fetch();
		List<t_file> fileList=t_file.find("id in (select file_id from t_file_relation "
				+ "where relation_type=? and relation_id=?)", relation_type,relation_id).fetch();
		FileService.installFileRelation(fileRelationList,fileList);
		FileService.installFileRelationDict(fileDictList,fileRelationList);
		return fileDictList;
	}
	
	public static int getSequenceByRelationTypeAndFileDict(Integer relation_type,Integer file_dict_id) {
		List<t_file_relation_dict> fileRelationList=FileService.getFileDictByRelationType(relation_type);
		for (t_file_relation_dict t_file_relation_dict : fileRelationList) {
			if(t_file_relation_dict.file_dict_id==file_dict_id) {
				return t_file_relation_dict.sequence;
			}
		}
		return -1;
	}
	
	/**
	 * 通过relation_type,relation_id得到对应file_relation列表,存放已上传的文件
	 * 用于显示已上传文件
	 * @param relation_type
	 * @param relation_id
	 * @return
	 */
	public static List<t_file_relation> getFileShowByRelation(Integer relation_type,Long relation_id) {
		List<t_file_relation> fileRelationList=t_file_relation.find("relation_type=? and relation_id=? order by file_dict_sequence", relation_type,relation_id).fetch();
		Collections.sort(fileRelationList);
		List<t_file> fileList=t_file.find("id in (select file_id from t_file_relation "
				+ "where relation_type=? and relation_id=?)", relation_type,relation_id).fetch();
		FileService.installFileRelation(fileRelationList,fileList);
		return fileRelationList;
	}
	
	//是否满足必填文件要求
	public static boolean meetRelationRequired(List<t_file_relation_dict> fileDictList)  {
		for(t_file_relation_dict file_relation_dict:fileDictList) {
			if(file_relation_dict.required_num==0) {/** 至少文件数量,0不必填 */
				continue;
			}
			if(file_relation_dict.file_relation_list==null) {
				throw new RuntimeException("file_relation_dict is not install!");
			}
			if(file_relation_dict.file_relation_list.size()<file_relation_dict.required_num) {
				return false;
			}
		}
		return true;
	}
	
	//(有必选条件&&满足必填文件要求) || (没有必选条件&&上传过文件) || 文件集合为空
	public static boolean meetRelationRequiredOrNoRequiredOrEmpty(List<t_file_relation_dict> fileDictList)  {
		//满足必填文件要求:默认为true,当有一条不满足必填要求时为flase
		boolean meetRelationRequired=true;
		//有必填文件要求:默认为fales,当有一条有必选要求时,为true
		boolean haveRequired=false;
		//上传过文件:默认false,当有一个文件上传过时,为true;
		boolean haveUpdate=false;
		//文件集合为空:默认为true,当文件列表不为空时,为false
		boolean isEmpty=true;
		
		for(t_file_relation_dict file_relation_dict:fileDictList) {
			isEmpty=false;
			if(file_relation_dict.required_num>0) {/** 至少文件数量,0不必填 */
				haveRequired=true;
			}
			if(file_relation_dict.file_relation_list==null) {
				throw new RuntimeException("file_relation_dict is not install!");
			}
			if(file_relation_dict.file_relation_list.size()>0) {
				haveUpdate=true;
			}
			if(file_relation_dict.file_relation_list.size()<file_relation_dict.required_num) {
				meetRelationRequired=false;
			}
		}
		
		return (haveRequired && meetRelationRequired) ||( !haveRequired && haveUpdate)|| isEmpty;
	}
	
	public static boolean meetRelationRequired(Integer relation_type,Long relation_id) {
		return FileService.meetRelationRequired(FileService.getFileUpdateByRelation(relation_type,relation_id));
	}
	
	//是否有可选文件可以上传
	public static boolean haveRelationOptional(List<t_file_relation_dict> fileDictList)  {
		for(t_file_relation_dict file_relation_dict:fileDictList) {
			if(file_relation_dict.max_num==-1) {/** 最多文件数量,-1不限数量 */
				return true;
			}
			if(file_relation_dict.file_relation_list==null) {
				throw new RuntimeException("file_relation_dict is not install!");
			}
			if(file_relation_dict.file_relation_list.size()<file_relation_dict.max_num) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 私有文件装配方法
	 */
	private static void installFileRelationDict(List<t_file_relation_dict> fileDictList,List<t_file_relation> fileRelationList) {
		for(t_file_relation_dict file_relation_dict:fileDictList) {
			file_relation_dict.file_relation_list=new ArrayList<t_file_relation>();
			for(t_file_relation file_relation:fileRelationList) {
				if(file_relation_dict.file_dict_id==file_relation.file.file_dict_id) {
					file_relation_dict.file_relation_list.add(file_relation);
				}
			}
		}
	}
	
	/**
	 * 私有文件装配方法
	 */
	private static void installFileRelation(List<t_file_relation> fileRelationList,List<t_file> fileList) {
		for(t_file_relation file_relation:fileRelationList) {
			file_relation.file=null;
			for(t_file file:fileList) {
				if(file_relation.file_id==file.id) {
					file_relation.file=file;
					break;
				}
			}
		}
	}
}
