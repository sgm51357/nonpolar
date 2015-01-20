package com.ivms6.core.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 任务执行者
 * </p>
 * @author shanguoming 2012-9-24 下午4:33:28
 * @version V1.0
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2012-9-24
 * @modify by reason:{方法名}:{原因}
 */
final class TaskExecutor implements Runnable {
	
	private Logger log = LoggerFactory.getLogger("hv-scheduler.TaskExecutor");
	
	/**
	 * 任务
	 */
	private Task task;
	private Scheduler scheduler;
	private ITaskEvent taskEvent;
	private transient volatile Thread inThread;
	/**
	 * 对象锁
	 */
	private Object lock = new Object();
	
	TaskExecutor(Scheduler scheduler, Task task) {
		this.scheduler = scheduler;
		this.task = task;
	}
	
	/**
	 * 获取任务
	 * @author shanguoming 2012-9-24 下午4:37:17
	 * @return
	 */
	public Task getTask() {
		return task;
	}
	
	public void run() {
		synchronized (lock) {
			this.inThread = Thread.currentThread();
			try {
				task.execute(taskEvent);
			} finally {
				scheduler.removeExecutor(this);
				this.inThread = null;
			}
		}
	}
	
	public void start() {
		Thread t = new Thread(this);
		t.setName("[Task-" + task.getId() + "]");
		t.start();
	}
	
	/**
	 * 等待任务执行完成。
	 * @throws InterruptedException
	 */
	public void join() throws InterruptedException {
		while (inThread != null) {
			synchronized (lock) {
				log.info("[Task-{}]执行完成。。。，任务",task.getId());
			}
		}
	}
	
	public void setTaskEvent(ITaskEvent taskEvent) {
		this.taskEvent = taskEvent;
	}
}
