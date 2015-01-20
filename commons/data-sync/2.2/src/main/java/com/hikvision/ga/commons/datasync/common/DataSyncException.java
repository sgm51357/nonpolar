package com.hikvision.ga.commons.datasync.common;

/**
 * @author shanguoming 2015年1月4日 上午10:24:17
 * @version V1.0
 * @modify: {原因} by shanguoming 2015年1月4日 上午10:24:17
 */
public class DataSyncException extends Exception {
	
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 1L;
	private ResultType errorType;
	
	public DataSyncException(ResultType errorType) {
		this.errorType = errorType;
	}
	
	public DataSyncException(String msg, ResultType errorType) {
		super(msg);
		this.errorType = errorType;
	}
	
	public DataSyncException(Throwable e) {
		super(e);
	}
	
	public DataSyncException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public DataSyncException(Throwable e, ResultType errorType) {
		super(e);
		this.errorType = errorType;
	}
	
	public DataSyncException(String msg, Throwable e, ResultType errorType) {
		super(msg, e);
		this.errorType = errorType;
	}
	
	public ResultType getErrorType() {
		return errorType;
	}
	
	public void setErrorType(ResultType errorType) {
		this.errorType = errorType;
	}
}
