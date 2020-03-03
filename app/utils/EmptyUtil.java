package utils;

import java.math.BigDecimal;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

/**
 * 空值处理
 * 
 * @ClassName EmptyUtil
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年7月20日 上午11:02:33
 * @version 1.0.0
 */
public class EmptyUtil {
	/**
	 * 如果为空转为""
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param obj
	 * @return
	 * @author: zj
	 */
	public static String obj2Str(Object obj) {
		return obj == null ? "" : obj.toString();
	}

	/**
	 * 针对数值，金额类的判空转换
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param obj
	 * @return BigDecimal(0)
	 * @author: zj
	 */
	public static BigDecimal obj20(Object obj) {
		return obj == null ? new BigDecimal(0) : new BigDecimal(obj.toString());
	}
}
