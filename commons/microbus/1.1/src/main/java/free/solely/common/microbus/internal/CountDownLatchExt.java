package free.solely.common.microbus.internal;

import java.util.concurrent.CountDownLatch;

/**
 * @author shanguoming 2014年11月27日 上午11:14:53
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年11月27日 上午11:14:53
 */
public class CountDownLatchExt extends CountDownLatch {
	
	private String uuid;
	
	/**
	 * 创建一个新的实例CountDownLatchExt.
	 * @param count
	 */
	public CountDownLatchExt(int count) {
		super(count);
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getUuid() {
		return uuid;
	}
}
