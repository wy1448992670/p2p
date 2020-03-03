package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 登陆拦截标志
 *
 * @author hys
 * @createDate  2015年9月10日 上午9:19:39
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LoginCheck {
	
	/**
	 * 是否是ajax请求
	 * 
	 * @return
	 */
	boolean value() default false;
	
	/**
	 * 已被拦截标志
	 */
	public static final String TOKEN = "UnLogin";
}