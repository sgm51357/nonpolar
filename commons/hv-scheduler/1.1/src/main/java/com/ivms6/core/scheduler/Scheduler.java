package com.ivms6.core.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 任务管理器
 * </p>
 * @author shanguoming 2012-9-21 上午10:38:09
 * @version V1.0
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2012-9-21
 * @modify by reason:{方法名}:{原因}
 */
public class Scheduler {
	
	private static final Logger log = LoggerFactory.getLogger("hv-scheduler.Scheduler");
	/**
	 * 任务的唯一编码
	 */
	private String guid = GUIDGenerator.generate();
	/**
	 * 世界时区
	 */
	private TimeZone timezone = null;
	/**
	 * 状态标志，如果true表示任务调度运行
	 */
	private boolean started = false;
	/**
	 * 内存任务集
	 */
	private MemoryTaskCollector memoryTaskCollector = new MemoryTaskCollector();
	/**
	 * 时间线程
	 */
	private TimerDaemon timer = null;
	/**
	 * 当前运行任务
	 */
	private ArrayList<TaskExecutor> executors = null;
	/**
	 * 线程池,默认使用缓存线程池{Executors.newCachedThreadPool()}
	 */
	private Executor executor = Executors.newCachedThreadPool();
	/**
	 * 内部锁，用于同步status-aware操作
	 */
	private Object lock = new Object();
	
	/**
	 * 创建任务调度和初始化
	 */
	public Scheduler() {
	}
	
	/**
	 * 构造
	 * @param executor 线程池
	 * @see Executor
	 */
	public Scheduler(Executor executor) {
		this.executor = executor;
	}
	
	/**
	 * 设置任务执行所使用的线程池
	 * @param executor
	 */
	public void setExecutor(Executor executor) {
		this.executor = executor;
	}
	
	/**
	 * 获取当前任务的guid编码
	 * @author shanguoming 2012-9-21 上午11:31:37
	 * @return guid编码
	 */
	public Object getGuid() {
		return guid;
	}
	
	/**
	 * 设置世界时区
	 * @author shanguoming 2012-9-21 上午11:32:44
	 * @param timezone 世界时区
	 */
	public void setTimeZone(TimeZone timezone) {
		this.timezone = timezone;
	}
	
	/**
	 * 获取设置的世界时区
	 * @return 世界时区
	 */
	public TimeZone getTimeZone() {
		return timezone != null?timezone:TimeZone.getDefault();
	}
	
	/**
	 * 判断任务调度是否启动
	 * @return true or false.
	 */
	public boolean isStarted() {
		synchronized (lock) {
			return started;
		}
	}
	
	/**
	 * 增设调度任务
	 * @author shanguoming 2012-9-24 下午3:59:48
	 * @param schedulingPattern
	 *        '秒 分 时 日 月 周'
	 * @param task
	 *        任务对象
	 * @return
	 * @throws InvalidPatternException
	 */
	public String addSchedule(ITaskEntity task) throws InvalidPatternException {
		if (null != task) {
			return addSchedule(new Task(task));
		}
		return null;
	}
	
	/**
	 * 添加调度任务
	 * @author shanguoming 2012-12-6 下午3:38:37
	 * @param schedulingPattern
	 * @param task
	 * @return
	 */
	private String addSchedule(Task task) {
		return memoryTaskCollector.add(task);
	}
	
	/**
	 * 更新调度任务
	 * @author shanguoming 2014年12月22日 下午2:39:20
	 * @param task
	 * @modify: {原因} by shanguoming 2014年12月22日 下午2:39:20
	 */
	public void updateSchedule(ITaskEntity task) throws InvalidPatternException {
		if (null != task) {
			memoryTaskCollector.update(new Task(task));
		}
	}
	
	/**
	 * 根据任务ID改变任务调度模式
	 * @author shanguoming 2012-9-24 下午3:13:36
	 * @param id
	 * @param schedulingPattern
	 *        调度模式
	 * @throws InvalidPatternException
	 */
	public void reschedule(String id, String schedulingPattern) throws InvalidPatternException {
		memoryTaskCollector.updatePattern(id, schedulingPattern);
	}
	
	/**
	 * 根据任务ID删除任务
	 * @author shanguoming 2012-9-24 下午3:15:26
	 * @param id
	 */
	public void deschedule(String id) {
		memoryTaskCollector.remove(id);
	}
	
	/**
	 * 根据GUID，获取任务对象
	 * @author shanguoming 2012-9-24 下午3:15:47
	 * @param id
	 * @return 任务对象
	 * @revised jiyi 2014-4-10 com.ivms6.core.scheduler.Task这个类受保护外界不可见，所以还是改为直接返回TaskEntity.
	 */
	public ITaskEntity getTask(String id) {
		Task task = memoryTaskCollector.getTask(id);
		if (task == null) return null;
		return task.getTaskEntity();
	}
	
	/**
	 * 启动任务调度
	 * @author shanguoming 2012-9-24 下午3:20:59
	 * @throws IllegalStateException
	 */
	public void start() throws IllegalStateException {
		synchronized (lock) {
			if (started) {
				throw new IllegalStateException("Scheduler already started");
			}
			executors = new ArrayList<TaskExecutor>();
			timer = new TimerDaemon(this);
			timer.start();
			started = true;
		}
	}
	
	/**
	 * 停止任务调度
	 * @author shanguoming 2012-9-24 下午3:21:12
	 * @throws IllegalStateException
	 * @throws InterruptedException
	 */
	public void stop() throws IllegalStateException, InterruptedException {
		synchronized (lock) {
			if (!started) {
				throw new IllegalStateException("Scheduler not started");
			}
			if (timer != null) {
				timer.interrupt();
			}
			timer = null;
			started = false;
			for (TaskExecutor executor : executors) {
				executor.join();
			}
			executors.clear();
		}
	}
	
	/**
	 * 任务轮询检测
	 * @author shanguoming 2012-9-24 下午3:21:38
	 * @param referenceTimeInMillis
	 * @return
	 */
	void spawnLauncher(long referenceTimeInMillis) {
		if (memoryTaskCollector.isEmpty() || !started) {
			return;
		}
		for (Task task : memoryTaskCollector.getTasks()) {
			try {
				SchedulingPattern pattern = task.getSchedulingPattern();
				if (null != pattern && pattern.match(getTimeZone(), referenceTimeInMillis)) {
					if (task.isRuning()) {
						log.info("任务{}:正在运行......", task.getId());
					} else {
						log.info("任务{}:满足[{}]表达式要求，任务开始执行。。。。。。", task.getId(), pattern.toString());
						spawnExecutor(task, referenceTimeInMillis);
					}
				}
			} catch (Exception e) {
				log.error("任务{}执行异常:", task.getId(), e);
			}
		}
	}
	
	/**
	 * 执行给定的任务
	 * @author shanguoming 2012-9-24 下午3:22:20
	 * @param task
	 *        任务对象
	 * @return
	 */
	void spawnExecutor(Task task, long referenceTimeInMillis) {
		if (task == null) return;
		task.currentFireTime = referenceTimeInMillis;
		TaskExecutor e = new TaskExecutor(this, task);
		if (executors != null) {
			synchronized (executors) {
				executors.add(e);
			}
			// 在线程池中执行
			executor.execute(e);
		} else {
			// 直接在新线程中运行
			e.start();
		}
	}
	
	public void executor(String taskId, ITaskEvent event) {
		Task task = memoryTaskCollector.getTask(taskId);
		if (null != task) {
			if (!task.isRuning()) {
				task.currentFireTime = System.currentTimeMillis();
				TaskExecutor e = new TaskExecutor(this, task);
				e.setTaskEvent(event);
				if (executors != null) {
					synchronized (executors) {
						executors.add(e);
					}
					// 在线程池中执行
					executor.execute(e);
				} else {
					// 直接在新线程中运行
					e.start();
				}
			} else {
				task.execute(event);
			}
		}
	}
	
	/**
	 * 删除执行完成的任务调度者
	 * @author shanguoming 2012-10-10 下午2:33:35
	 * @param executor
	 */
	void removeExecutor(TaskExecutor executor) {
		if (executors != null) {
			synchronized (executors) {
				executors.remove(executor);
			}
		}
	}
	
	/**
	 * 得到所有的任务上下文
	 * @return
	 */
	public List<TaskContext> getTaskContexts() {
		Task[] tasks = memoryTaskCollector.getTasks().toArray(new Task[memoryTaskCollector.size()]);
		return Arrays.<TaskContext> asList(tasks);
	}
}
