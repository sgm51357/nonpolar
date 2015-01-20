package com.ivms6.core.scheduler;

import java.util.List;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerFactory {
	
	private static final Logger log = LoggerFactory.getLogger("hv-scheduler.SchedulerFactory");
	private Scheduler scheduler = new Scheduler();
	
	/**
	 * 设置任务执行的线程池
	 * @param executor
	 */
	public void setExecutor(Executor executor) {
		scheduler.setExecutor(executor);
	}
	
	/**
	 * 重设调度任务
	 * @author shanguoming 2012-11-12 下午4:36:48
	 */
	public void reschedule(String id, String schedulingPattern) {
		scheduler.reschedule(id, schedulingPattern);
		log.info("重设任务[{}]的调度模式为[{}]", id, schedulingPattern);
	}
	
	/**
	 * 执行任务，如果ITaskEvent不为null，且任务正在运行时，可以被放入任务队列，等待当前执行完后，被执行。任务队列上限5个，避免任务堆积
	 * @author shanguoming 2014年12月23日 下午6:07:38
	 * @param taskId
	 * @param event
	 * @modify: {原因} by shanguoming 2014年12月23日 下午6:07:38
	 */
	public void executeTask(String taskId, ITaskEvent event) {
		scheduler.executor(taskId, event);
		log.info("非定时执行任务[{}]，任务事件为[{}]", taskId, event.getClass().getName());
	}
	
	/**
	 * 删除指定ID的任务
	 * @author shanguoming 2012-11-13 上午11:49:15
	 * @param id
	 */
	public void deschedule(String id) {
		scheduler.deschedule(id);
		log.info("删除调度器内的任务[{}]", id);
	}
	
	/**
	 * 添加任务
	 * @author shanguoming 2012-11-13 上午10:29:04
	 * @param task 任务对象
	 */
	public void addTask(ITaskEntity... tasks) throws InvalidPatternException {
		if (tasks != null && tasks.length > 0) {
			for (ITaskEntity taskEntity : tasks) {
				scheduler.addSchedule(taskEntity);
			}
		}
	}
	
	/**
	 * 更新任务
	 * @author shanguoming 2014年12月22日 下午3:08:36
	 * @param tasks
	 * @modify: {原因} by shanguoming 2014年12月22日 下午3:08:36
	 */
	public void updateTask(ITaskEntity... tasks) {
		if (tasks != null && tasks.length > 0) {
			for (ITaskEntity taskEntity : tasks) {
				scheduler.updateSchedule(taskEntity);
			}
		}
	}
	
	/**
	 * 启动任务调度
	 * @author shanguoming 2014年12月24日 下午4:57:37
	 * @param tasks
	 * @return
	 * @modify: {原因} by shanguoming 2014年12月24日 下午4:57:37
	 */
	public boolean start(ITaskEntity... tasks) {
		this.addTask(tasks);
		return start();
	}
	
	/**
	 * 启动任务调度，如果已经启动则失败(false)
	 * @author shanguoming 2012-11-13 上午10:50:43
	 * @return 是否启动成功
	 */
	public boolean start() {
		if (!scheduler.isStarted()) {
			log.info("任务调度器。。。。。。。开始启动");
			scheduler.start();
			log.info("任务调度器。。。。。。。启动");
			return true;
		}
		return false;
	}
	
	/**
	 * 重启任务调度
	 * @author shanguoming 2012-11-13 下午2:02:00
	 * @return 是否启动成功
	 */
	public void restart() {
		log.info("任务调度器。。。。。。。开始重启");
		stop();
		start();
	}
	
	/**
	 * 停止任务调度
	 * @author shanguoming 2012-11-12 下午4:37:06
	 * @return 是否停止任务成功(成功：true or 失败：false)
	 */
	public boolean stop() {
		if (scheduler.isStarted()) {
			try {
				log.info("任务调度器。。。。。。。开始停止");
				scheduler.stop();
				log.info("任务调度器。。。。。。。停止");
			} catch (IllegalStateException e) {
				log.error("任务调度非法状态的异常", e);
				return false;
			} catch (InterruptedException e) {
				log.error("任务调度正在执行的线程被中断异常", e);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 获取调度器任务列表当前执行的上下文
	 * @author shanguoming 2014年12月24日 下午4:57:51
	 * @return
	 * @modify: {原因} by shanguoming 2014年12月24日 下午4:57:51
	 */
	public List<TaskContext> getTaskContexts() {
		return scheduler.getTaskContexts();
	}
	
	/**
	 * 获取调度器任务的执行对象
	 * @author shanguoming 2014年12月24日 下午4:58:18
	 * @param taskId
	 * @return
	 * @modify: {原因} by shanguoming 2014年12月24日 下午4:58:18
	 */
	public ITaskEntity getTaskEntity(String taskId) {
		return scheduler.getTask(taskId);
	}
}
