package free.solely.common.microbus;

import java.util.Date;

/**
 * @author shanguoming 2014年11月26日 下午6:50:31
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年11月26日 下午6:50:31
 */
public class ConsumeException extends Exception {
	
	private Date date;
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 1L;
	
	public ConsumeException(Throwable e) {
		super(e);
		date = new Date();
	}
	
	public Date getDate() {
		return date;
	}
}
