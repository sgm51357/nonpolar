package free.solely.common.microbus;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author shanguoming 2014年11月14日 下午2:31:03
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年11月14日 下午2:31:03
 */
public class MicrobusBeanPostProcessor implements BeanPostProcessor {
	
	/**
	 * @author shanguoming 2014年11月14日 下午2:31:04
	 * @param bean
	 * @param beanName
	 * @return
	 * @throws BeansException
	 * @modify: {原因} by shanguoming 2014年11月14日 下午2:31:04
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		System.out.println("对象" + beanName + "开始实例化");
		// if (null != bean && null != bean.getClass().getAnnotation(MicroNamespace.class)) {
		// System.out.println(bean.getClass());
		// }
		return bean;
	}
	
	/**
	 * @author shanguoming 2014年11月14日 下午2:31:04
	 * @param bean
	 * @param beanName
	 * @return
	 * @throws BeansException
	 * @modify: {原因} by shanguoming 2014年11月14日 下午2:31:04
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		System.out.println("对象" + beanName + "实例化完成");
		// if (null != bean && null != bean.getClass().getAnnotation(MicroNamespace.class)) {
		// System.out.println(bean.getClass());
		// }
		return bean;
	}
}
