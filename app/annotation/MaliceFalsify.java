package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 恶意篡改
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-8-8 下午05:35:12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MaliceFalsify {
	int type(); // 说明是被动，还是主动
	boolean isAjax(); // 是否异步
}
