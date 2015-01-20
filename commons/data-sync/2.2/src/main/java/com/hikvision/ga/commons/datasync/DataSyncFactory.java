package com.hikvision.ga.commons.datasync;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hikvision.ga.commons.datasync.common.DataSyncException;
import com.hikvision.ga.commons.datasync.common.ResultType;
import com.hikvision.ga.commons.datasync.in.CvsResSyncInSysHandler;
import com.hikvision.ga.commons.datasync.in.DataSyncInEvent;
import com.hikvision.ga.commons.datasync.out.CvsResSyncOutSysHandler;
import com.hikvision.ga.commons.datasync.out.DataSyncOutEvent;
import com.hikvision.ga.commons.datasync.utils.DataSyncTools;

/**
 * 数据同步工厂类
 * @author shanguoming 2014年12月31日 下午3:37:46
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年12月31日 下午3:37:46
 */
public class DataSyncFactory {
	
	private static final Logger log = LoggerFactory.getLogger("data-sync:DataSyncFactory");
	private static final DataSyncFactory factory = new DataSyncFactory();
	private static BlockingQueue<DataSyncInEvent> inQueue = new LinkedBlockingQueue<DataSyncInEvent>(100);
	private static CopyOnWriteArraySet<String> inCodes = new CopyOnWriteArraySet<String>();
	private static BlockingQueue<String> outQueue = new LinkedBlockingQueue<String>(100);
	private static ConcurrentMap<String, DataSyncOutEvent> outMap = new ConcurrentHashMap<String, DataSyncOutEvent>();
	
	/**
	 * 初始化数据同步工厂，并启动2个定时器用于同步和输出数据
	 * 创建一个新的实例DataSyncFactory.
	 */
	private DataSyncFactory() {
		SyncInTimerDaemon inTimer = new SyncInTimerDaemon();
		inTimer.start();
		SyncOutTimerDaemon outTimer = new SyncOutTimerDaemon();
		outTimer.start();
	}
	
	public static DataSyncFactory getInstance() {
		return factory;
	}
	
	/**
	 * 同步数据
	 * @author shanguoming 2015年1月4日 上午10:03:11
	 * @param event 数据同步事件
	 * @return -1:放入队列失败，0:队列已经存在，1:放入队列成功
	 * @modify: {原因} by shanguoming 2015年1月4日 上午10:03:11
	 */
	public int syncInSys(DataSyncInEvent event) {
		synchronized (this) {
			if (!inCodes.contains(event.getCode() + "_" + event.getPath())) {
				return inQueue.offer(event)?1:-1;
			}
			return 0;
		}
	}
	
	/**
	 * 生成数据
	 * @author shanguoming 2015年1月4日 上午10:03:17
	 * @param event 数据生成事件
	 * @return -1:放入队列失败，0:队列已经存在，1:放入队列成功
	 * @modify: {原因} by shanguoming 2015年1月4日 上午10:03:17
	 */
	public int syncOutSys(DataSyncOutEvent event) {
		synchronized (this) {
			String code = event.getCode() + "_full";
			DataSyncOutEvent e = outMap.get(code);
			if (null != e) {
				if (!DataSyncTools.isEmpty(event.getIncreBeans())) {
					e.mergeIncreBeans(event.getIncreBeans());
				}
			} else if (event.isFull()) {
				outMap.put(code, event);
				return outQueue.offer(code)?1:-1;
			} else {
				code = event.getCode() + "_incre";
				e = outMap.get(code);
				if (null != e) {
					if (!DataSyncTools.isEmpty(event.getIncreBeans())) {
						e.mergeIncreBeans(event.getIncreBeans());
					}
				} else {
					outMap.put(code, event);
					return outQueue.offer(code)?1:-1;
				}
			}
			return 0;
		}
	}
	
	/**
	 * 消费同步事件
	 * @author shanguoming 2015年1月4日 上午10:04:25
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月4日 上午10:04:25
	 */
	private DataSyncInEvent consumeInEvent() {
		synchronized (this) {
			DataSyncInEvent event = inQueue.poll();
			if (null != event) {
				inCodes.remove(event.getCode() + "_" + event.getPath());
			}
			return event;
		}
	}
	
	/**
	 * 消费生成事件
	 * @author shanguoming 2015年1月4日 上午10:04:38
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月4日 上午10:04:38
	 */
	private DataSyncOutEvent consumeOutEvent() {
		synchronized (this) {
			String code = outQueue.poll();
			if (DataSyncTools.isNotBlank(code)) {
				return outMap.remove(code);
			}
			return null;
		}
	}
	
	/**
	 * 数据同步定时器
	 * @author shanguoming 2015年1月4日 上午10:06:53
	 * @version V1.0
	 * @modify: {原因} by shanguoming 2015年1月4日 上午10:06:53
	 */
	class SyncInTimerDaemon extends Thread {
		
		public void run() {
			for (;;) {
				consume();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.warn("");
				}
			}
		}
		
		/**
		 * 消费同步队列
		 * @author shanguoming 2015年1月4日 上午10:07:55
		 * @modify: {原因} by shanguoming 2015年1月4日 上午10:07:55
		 */
		private void consume() {
			if (!inQueue.isEmpty()) {
				DataSyncInEvent event = consumeInEvent();
				if (null != event) {
					CvsResSyncInSysHandler handler = new CvsResSyncInSysHandler();
					try {
						handler.execute(event);
					} catch (DataSyncException e) {
						event.callback(ResultType.EXCEPTION, e);
					}
				}
			}
		}
	}
	
	/**
	 * 数据生成定时器
	 * @author shanguoming 2015年1月4日 上午10:07:00
	 * @version V1.0
	 * @modify: {原因} by shanguoming 2015年1月4日 上午10:07:00
	 */
	class SyncOutTimerDaemon extends Thread {
		
		public void run() {
			for (;;) {
				consume();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.warn("");
				}
			}
		}
		
		/**
		 * 消费生成队列
		 * @author shanguoming 2015年1月4日 上午10:08:08
		 * @modify: {原因} by shanguoming 2015年1月4日 上午10:08:08
		 */
		private void consume() {
			if (!outQueue.isEmpty()) {
				DataSyncOutEvent event = consumeOutEvent();
				if (null != event) {
					CvsResSyncOutSysHandler handler = new CvsResSyncOutSysHandler();
					try {
						handler.execute(event);
					} catch (DataSyncException e) {
						event.callback(ResultType.EXCEPTION, e);
					}
				}
			}
		}
	}
}
