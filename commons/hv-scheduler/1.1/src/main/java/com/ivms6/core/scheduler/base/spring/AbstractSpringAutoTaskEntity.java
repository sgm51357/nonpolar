package com.ivms6.core.scheduler.base.spring;

import org.springframework.beans.factory.BeanNameAware;

/**
 * @author shanguoming 2015年1月15日 下午3:49:46
 * @version V1.0
 * @modify: {原因} by shanguoming 2015年1月15日 下午3:49:46
 */
public abstract class AbstractSpringAutoTaskEntity extends AbstractSpringTaskEntity implements BeanNameAware {
	
	@Override
	public void setBeanName(String name) {
		setTaskId(name);
		if (null != super.getSchedulerFactory()) {
			super.getSchedulerFactory().updateTask(this);
		}
	}
}
