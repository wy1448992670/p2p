package models.file;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

@Entity
public class t_file extends Model {
	private static final long serialVersionUID = 1L;
	
	@Access(AccessType.PROPERTY)
	public Integer file_dict_id;
	@Transient
	public t_file_dict file_dict;
	@Transient
	public String file_dict_name;
	
	
	/** 文件地址 */
	public String real_path;
	
	/** 脱敏文件地址 */
	public String desensitization_path;
	
	/** 缓存地址 */
	public String cache_path;
	
	public Date create_time;
	
	/** 过期时间,通过是否过期判断是否能复用 */
	public Date expiry_time;
	
	/** 1.user 2.supervisor */
	public int uploader_type;
	
	public long uploader_id;

	public void setFile_dict_id(Integer file_dict_id) {
		this.file_dict = new t_file_dict().getEnumById(file_dict_id.longValue());
		if (this.file_dict == null) {
			throw new IllegalArgumentException("错误的file_dict！");
		}
		this.file_dict_name = this.file_dict.name;
		this.file_dict_id = file_dict_id;
	}
	
	@Override
	public String toString() {
		return "t_file [id=" + id + ", file_dict_id=" + file_dict_id + ", real_path=" + real_path + ", desensitization_path="
				+ desensitization_path + ", cache_path=" + cache_path + ", create_time=" + create_time
				+ ", expiry_time=" + expiry_time + ", uploader_type=" + uploader_type + ", uploader_id=" + uploader_id
				+ "]";
	}

}