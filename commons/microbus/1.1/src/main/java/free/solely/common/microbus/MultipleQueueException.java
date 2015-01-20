package free.solely.common.microbus;

/**
 * @author shanguoming 2014年11月24日 下午3:42:07
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年11月24日 下午3:42:07
 */
public class MultipleQueueException extends Exception {
	
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 1L;
	
	public MultipleQueueException(String message) {
		super(message);
	}
}
