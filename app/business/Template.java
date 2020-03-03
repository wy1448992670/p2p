package business;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang.StringUtils;

/**
 * 模板
 * @author lzp
 * @version 6.0
 * @created 2014-6-27
 */
public class Template implements Serializable{
	
	public static class Keyword {
		public static final String TIME = "【time】";
		public static final String PLATFORM_NAME = "【platform_name】";
		public static final String PLATFORM_TELEPHONE = "【platform_telephone】";
	}
	
	/**
	 * 替换content中的关键字
	 * @return
	 */
	public static String replaceKeywords(String content) {
		if (StringUtils.isBlank(content)) {
			return content;
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		content = content.replace(Keyword.TIME, (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
		content = content.replace(Keyword.PLATFORM_NAME, backstageSet.platformName);
		content = content.replace(Keyword.PLATFORM_TELEPHONE, backstageSet.platformTelephone);
		
		return content;
	}
	
}
