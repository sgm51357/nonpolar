package com.hikvision.ga.commons.jetty.bootstrap.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jLogProxy extends LogProxy {
	
	private Logger log;
	
	public Slf4jLogProxy(Logger log) {
		this.log = LoggerFactory.getLogger(super.getClass());
		this.log = log;
	}
	
	public Slf4jLogProxy(Class<?> clazz) {
		this(LoggerFactory.getLogger(clazz));
	}
	
	public void debug(String message, Object[] args) {
		this.log.debug(message, args);
	}
	
	public void info(String message, Object[] args) {
		this.log.info(message, args);
	}
	
	public void error(String message, Object[] args) {
		this.log.error(message, args);
	}
	
	public void error(String message, Throwable e) {
		this.log.error(message, e);
	}
	
	public void warn(String message, Object[] args) {
		this.log.warn(message, args);
	}
}
