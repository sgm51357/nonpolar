package free.solely.common.microbus.internal;

import java.lang.reflect.Method;
import java.util.Observable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import free.solely.common.microbus.Subscriber;

abstract class AsyncSubscriber extends Subscriber {
	
	private Logger log = LoggerFactory.getLogger(AsyncSubscriber.class);
	protected CountDownLatch sync;
	
	public AsyncSubscriber(Observable observable) {
		super(observable);
	}
	
	/**
	 * @author shanguoming 2014年11月27日 上午9:40:39
	 * @param timeout
	 * @param time
	 * @return
	 * @modify: {原因} by shanguoming 2014年11月27日 上午9:40:39
	 */
	@Override
	public Object getResult(long timeout, TimeUnit time) {
		try {
			createSync();
			synchronized (sync) {
				sync.await(timeout, time);
			}
		} catch (InterruptedException e) {
			log.error("线程等待异常", e);
			throw new RuntimeException(e);
		} finally {
			sync = null;
		}
		return super.getResult();
	}
	
	/**
	 * @author shanguoming 2014年11月27日 上午10:00:09
	 * @param sub
	 * @param method
	 * @param args
	 * @modify: {原因} by shanguoming 2014年11月27日 上午10:00:09
	 */
	@Override
	protected void execute(Object sub, Method method, Object args) {
		createSync();
		super.execute(sub, method, args);
		if (null != sync) {
			sync.countDown();
		}
	}
	
	private synchronized void createSync() {
		if (null == sync) {
			sync = new CountDownLatch(1);
		}
	}
	
	public void cleanLatch() {
		sync = null;
	}
}
