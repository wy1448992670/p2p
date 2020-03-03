package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 是否支持债权转让，不支持则跳转相应的页面
 *
 * @author hys
 * @createDate  2015年9月9日 上午11:50:50
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DebtCheck {
	int value();
}