package free.solely.common.microbus.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shanguoming 2014年11月24日 上午11:29:05
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年11月24日 上午11:29:05
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface Topic {
	
	/**
	 * 主题名称
	 * @author shanguoming 2014年11月26日 下午4:30:09
	 * @return
	 * @modify: {原因} by shanguoming 2014年11月26日 下午4:30:09
	 */
	String name();
	
	/**
	 * 队列最大值,只对单异步调度有效
	 * @author shanguoming 2014年11月27日 上午9:25:09
	 * @return
	 * @modify: {原因} by shanguoming 2014年11月27日 上午9:25:09
	 */
	int max() default 100;
}
