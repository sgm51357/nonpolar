package com.ivms6.core.scheduler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * </p>
 * @author shanguoming 2012-9-24 下午4:52:08
 * @version V1.0
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: Jiyi 2014-3-27
 * @modify by reason: 记录任务执行的时间、次数、异常等基本情况
 */
final class Task implements TaskContext {
	
	private static final Logger log = LoggerFactory.getLogger("hv-scheduler.Task");
	/**
	 * 任务对象
	 */
	private ITaskEntity taskEntity;
	/**
	 * 本次触发时间
	 */
	long currentFireTime;
	/**
	 * 运行的执行时间
	 */
	long currentFirePeriod;
	/**
	 * 任务的运行异常
	 */
	private Throwable currentException;
	/**
	 * 状态，是否在运行
	 */
	private volatile boolean isRuning = false;
	/**
	 * 等待队列
	 */
	private BlockingQueue<ITaskEvent> queues = new LinkedBlockingQueue<ITaskEvent>(5);
	/**
	 * 计数器
	 */
	private AtomicInteger count = new AtomicInteger();
	
	/**
	 * 构造
	 * @param taskEntity
	 *        任务
	 * @throws InvalidPatternException
	 */
	public Task(ITaskEntity taskEntity) throws InvalidPatternException {
		if (taskEntity == null) {
			throw new NullPointerException("Can not input a null task entity to hv-scheduler!");
		}
		this.taskEntity = taskEntity;
		if (taskEntity.getTaskId() == null || taskEntity.getTaskId().trim().equals("")) {
			taskEntity.setTaskId(GUIDGenerator.generate());
		}
		this.pattern = new SchedulingPattern(taskEntity.getSchedulingPattern());
		if (taskEntity instanceof ITaskContextListener) {
			((ITaskContextListener)taskEntity).setTaskContext(this);
		}
	}
	
	private SchedulingPattern pattern;
	
	/**
	 * 获得任务调度计划
	 * @return 解析后的计划
	 * @see SchedulingPattern
	 */
	public SchedulingPattern getSchedulingPattern() {
		return pattern;
	}
	
	/**
	 * 更新任务调度计划
	 * @param schedulingPattern 任务执行计划表达式
	 */
	public void setSchedulingPattern(String schedulingPattern) throws InvalidPatternException {
		taskEntity.setSchedulingPattern(schedulingPattern);
		this.pattern = new SchedulingPattern(schedulingPattern);
	}
	
	/**
	 * 获取任务对象
	 * @author shanguoming 2012-9-24 下午4:04:05
	 * @return
	 * @revised 2014-3-27
	 *          Jiyi，该方法有危害性，获得TaskEntity后可以变更ID,执行计划，但Task中的pattern不会更新。因此要注意不可随意修改pattern
	 */
	public ITaskEntity getTaskEntity() {
		return taskEntity;
	}
	
	/**
	 * 执行任务
	 */
	public void execute(ITaskEvent taskEvent) {
		if (!isRuning) {
			isRuning = true;
			try {
				run(taskEvent);
				while (true) {
					ITaskEvent t = queues.poll();
					if (t != null) {
						currentFireTime = System.currentTimeMillis();
						log.info("执行[{}]中排队的任务，事件类型：{}", taskEntity.getTaskId(), t.getClass());
						run(t);
					} else {
						break;
					}
				}
			} finally {
				isRuning = false;
			}
		} else if (null != taskEvent) {
			boolean flag = queues.offer(taskEvent);
			if (flag) {
				log.warn("任务[{}]正在运行，把事件[{}]放入队列", taskEntity.getTaskId(), taskEvent.getClass().getName());
			} else {
				log.warn("任务[{}]正在运行，队列已满，事件[{}]无法放入队列，队列上限5", taskEntity.getTaskId(), taskEvent.getClass().getName());
			}
		} else {
			log.warn("任务[{}]正在运行，不支持空ITaskEvent事件放入队列", taskEntity.getTaskId());
		}
	}
	
	private void run(ITaskEvent taskEvent) {
		count.incrementAndGet(); // 计数器增加1
		long start = System.currentTimeMillis();
		Object result = null;
		try {
			result = taskEntity.run(taskEvent);
			currentException = null;
		} catch (Throwable e) { // 捕捉并记录执行的错误
			currentException = e;
			log.error("task error,task id=" + getId(), e);
		} finally {
			currentFirePeriod = System.currentTimeMillis() - start;// 计算本次执行时间
			if (taskEntity instanceof ITaskCallbackListener) {
				try {
					((ITaskCallbackListener)taskEntity).callback(result);
				} catch (Exception e) {
					log.error("执行回调任务[{}]的回调方法异常", taskEntity.getTaskId(), e);
				}
			}
			log.info("任务[{}]执行完成", taskEntity.getTaskId());
		}
	}
	
	/**
	 * 获取任务ID
	 * @return
	 */
	public String getId() {
		return taskEntity.getTaskId();
	}
	
	/**
	 * 任务是否运行中
	 * @author shanguoming 2012-10-10 下午2:28:00
	 * @return
	 */
	public boolean isRuning() {
		return isRuning;
	}
	
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("Task[");
		b.append("taskEntity=");
		b.append(taskEntity);
		b.append("]");
		return b.toString();
	}
	
	@Override
	public long getCurrentFireTime() {
		return currentFireTime;
	}
	
	@Override
	public int getExecuteTimes() {
		return count.get();
	}
	
	@Override
	public long getCurrentPeriod() {
		return currentFirePeriod;
	}
	
	@Override
	public boolean isSuccess() {
		return currentException == null;
	}
	
	@Override
	public Throwable getCurrentException() {
		return currentException;
	}
}
