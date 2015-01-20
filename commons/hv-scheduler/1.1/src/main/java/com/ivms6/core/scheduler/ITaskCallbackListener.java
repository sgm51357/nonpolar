package com.ivms6.core.scheduler;

/**
 * @author shanguoming 2014年12月22日 下午4:43:37
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年12月22日 下午4:43:37
 */
public interface ITaskCallbackListener {
	
	/**
	 * 任务结束时，执行task回调的接口
	 * @author shanguoming 2014年12月22日 下午4:45:42
	 * @param context
	 * @modify: {原因} by shanguoming 2014年12月22日 下午4:45:42
	 */
	void callback(Object result);
}
