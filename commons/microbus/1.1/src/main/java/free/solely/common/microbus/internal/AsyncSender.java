package free.solely.common.microbus.internal;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author shanguoming 2014年11月27日 上午8:44:15
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年11月27日 上午8:44:15
 */
class AsyncSender extends Sender {
	
	private Executor executor = null;
	private int max = 100;
	private Boolean loop = false;
	private Queue<Object> queues = null;
	
	public AsyncSender(int max) {
		queues = new LinkedList<Object>();
		this.max = max;
	}
	
	public void push(Object... args) {
		if (null != getSubscribers() && !getSubscribers().isEmpty()) {
			int size = queues.size();
			if (size == max) {
				throw new IllegalStateException("Queue full");
			}
			queues.add(args);
			if (!getLoop()) {
				setLoop(true);
				getExecutor().execute(new Run(this));
			}
		}
	}
	
	protected Object getQueue() {
		return queues.poll();
	}
	
	protected synchronized Boolean setLoop(Boolean loop) {
		this.loop = loop;
		return this.loop;
	}
	
	protected synchronized Boolean getLoop() {
		return loop;
	}
	
	class Run implements Runnable {
		
		private AsyncSender sender;
		
		public Run(AsyncSender sender) {
			this.sender = sender;
		}
		
		/**
		 * @author shanguoming 2014年11月27日 上午8:59:56
		 * @modify: {原因} by shanguoming 2014年11月27日 上午8:59:56
		 */
		@Override
		public void run() {
			while (true) {
				Object o = sender.queues.poll();
				setChanged();
				notifyObservers(o);
				if (!sender.setLoop(!(sender.queues.size() == 0))) {
					break;
				}
			}
		}
	}
	
	/**
	 * 如果executor为null则返回默认线程池（Executors.newCachedThreadPool()）
	 * @author shanguoming 2014年11月24日 下午2:53:05
	 * @return
	 * @modify: {原因} by shanguoming 2014年11月24日 下午2:53:05
	 */
	private Executor getExecutor() {
		if (executor == null) {
			executor = Executors.newSingleThreadExecutor();
		}
		return executor;
	}
}
