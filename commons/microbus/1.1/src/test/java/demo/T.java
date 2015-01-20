package demo;

import java.util.concurrent.TimeUnit;
import free.solely.common.microbus.Subscriber;
import free.solely.common.microbus.annotation.Queue;
import free.solely.common.microbus.annotation.Topic;
import free.solely.common.microbus.internal.MultipleAsyncDispatcher;
import free.solely.common.microbus.internal.SingleAsyncDispatcher;
import free.solely.common.microbus.internal.SyncDispatcher;

/**
 * @author shanguoming 2014年11月26日 下午7:18:06
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年11月26日 下午7:18:06
 */
public class T {
	
	private SyncDispatcher syncDispatcher = new SyncDispatcher();
	private MultipleAsyncDispatcher multipleAsyncDispatcher = new MultipleAsyncDispatcher();
	private SingleAsyncDispatcher singleAsyncDispatcher = new SingleAsyncDispatcher();
	
	/**
	 * 创建一个新的实例T.
	 */
	public T() {
		try {
			syncDispatcher.subscribe(this);
			multipleAsyncDispatcher.subscribe(this);
			singleAsyncDispatcher.subscribe(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Topic(name = "test1")
	public void test() {
		System.out.println("---1");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Queue(name = "test2")
	public void test(String a) {
		System.out.println("---" + a);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Queue(name = "test")
	public String test(String a, String b) {
		System.out.println(a + "---" + b);
		return a + b;
	}
	
	public Subscriber sync(String name, Object... args) {
		return syncDispatcher.queue(name, args);
	}
	
	public Subscriber multipleAsyncQueue(String name, Object... args) {
		return multipleAsyncDispatcher.queue(name, args);
	}
	
	public Subscriber singleAsyncQueue(String name, Object... args) {
		return singleAsyncDispatcher.queue(name, args);
	}
	
	public static void main(String[] args) throws Exception {
		T t = new T();
		 Subscriber multiple = t.multipleAsyncQueue("test", "a","b");
		// Object o1 = multiple.getResult(10, TimeUnit.SECONDS);
		// System.out.println("multiple------"+o1);
//		for (int i = 0; i < 10; i++) {
//			long start = System.currentTimeMillis();
//			Subscriber single = t.singleAsyncQueue("test", "a", "" + i);
//			Object o2 = null;
//			if (i % 2 == 0) {//不能异步和同步交错使用
//				o2 = single.getResult(10, TimeUnit.SECONDS);
//			}
//			System.out.println("single------" + o2 + "-----" + (System.currentTimeMillis() - start));
//		}
	}
}
