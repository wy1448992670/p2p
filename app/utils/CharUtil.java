package utils;

import org.apache.commons.lang.StringUtils;

public class CharUtil {
    /**
     * 是否是中文字符
     * @param strName
     * @return
     */
    private static boolean isChinese(char c) {
    	int v = (int)c; 
    	
    	return (v >= 19968 && v <= 171941); 
    }
 
    /**
     * 是否全是中文
     * @param strName
     * @return
     */
    public static boolean isChinese(String str) {
    	if (StringUtils.isBlank(str)) {
			return false;
		}
    	
        char[] ch = str.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (!isChinese(c)) {
                return false;
            }
        }
        
        return true;
    }
}
