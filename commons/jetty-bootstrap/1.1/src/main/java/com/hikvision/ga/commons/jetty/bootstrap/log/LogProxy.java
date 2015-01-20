package com.hikvision.ga.commons.jetty.bootstrap.log;

public class LogProxy {
	
	public void debug(String message, Object[] args) {
	}
	
	public void info(String message, Object[] args) {
	}
	
	public void error(String message, Object[] args) {
	}
	
	public void error(String message, Throwable e) {
	}
	
	public void warn(String message, Object[] args) {
	}
	
	public static LogProxy getLogger(Class<?> clazz) {
		try {
			LogProxy.class.getClassLoader().loadClass("org.slf4j.Logger");
			return new Slf4jLogProxy(clazz);
		} catch (ClassNotFoundException e) {
		}
		return new SystemOutLogProxy(clazz);
	}
}
