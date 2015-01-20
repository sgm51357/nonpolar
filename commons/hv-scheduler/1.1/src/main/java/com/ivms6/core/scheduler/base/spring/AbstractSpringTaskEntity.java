package com.ivms6.core.scheduler.base.spring;

import org.springframework.beans.factory.annotation.Autowired;
import com.ivms6.core.scheduler.SchedulerFactory;
import com.ivms6.core.scheduler.base.AbstractTaskEntity;

/**
 * @author shanguoming 2014年12月24日 下午3:57:02
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年12月24日 下午3:57:02
 */
public abstract class AbstractSpringTaskEntity extends AbstractTaskEntity {
	
	@Autowired
	public void setSchedulerFactory(SchedulerFactory schedulerFactory) {
		super.setSchedulerFactory(schedulerFactory);
	}
}
