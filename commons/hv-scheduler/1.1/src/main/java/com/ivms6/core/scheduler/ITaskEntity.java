package com.ivms6.core.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ITaskEntity {
	
	public Logger log = LoggerFactory.getLogger("hv-scheduler.ITaskEntity");
	
	/**
	 * 设置调度工厂
	 * @author shanguoming 2014年12月23日 上午11:35:23
	 * @param schedulerFactory
	 * @modify: {原因} by shanguoming 2014年12月23日 上午11:35:23
	 */
	void setSchedulerFactory(SchedulerFactory schedulerFactory);
	
	/**
	 * 获取任务调度模式
	 * @author shanguoming 2014年12月23日 上午11:41:28
	 * @return
	 * @modify: {原因} by shanguoming 2014年12月23日 上午11:41:28
	 */
	String getSchedulingPattern();
	
	/**
	 * 获取任务ID
	 * @author shanguoming 2014年12月23日 上午11:41:42
	 * @return
	 * @modify: {原因} by shanguoming 2014年12月23日 上午11:41:42
	 */
	String getTaskId();
	
	/**
	 * schedulingPattern '秒 分 时 日 月 周'
	 * @author shanguoming 2012-11-12 下午5:11:23
	 * @param schedulingPattern
	 */
	void setSchedulingPattern(String schedulingPattern);
	
	/**
	 * 设置任务的ID
	 * @author shanguoming 2012-11-13 上午11:14:26
	 * @param taskId
	 */
	void setTaskId(String taskId);
	
	/**
	 * 执行任务
	 * @author shanguoming 2014年12月23日 上午11:41:49
	 * @param 任务事件对象
	 * @modify: {原因} by shanguoming 2014年12月23日 上午11:41:49
	 */
	abstract Object run(ITaskEvent taskEvent) throws Exception;
}
