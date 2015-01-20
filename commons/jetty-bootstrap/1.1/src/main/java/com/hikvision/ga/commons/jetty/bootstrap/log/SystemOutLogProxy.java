package com.hikvision.ga.commons.jetty.bootstrap.log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SystemOutLogProxy extends LogProxy {
	
	private Class<?> clazz;
	
	public SystemOutLogProxy(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	public void debug(String message, Object[] args) {
		message("DEBUG", message, args);
	}
	
	public void info(String message, Object[] args) {
		message("INFO", message, args);
	}
	
	public void error(String message, Throwable e) {
		error(message, new Object[0]);
		e.printStackTrace(System.out);
	}
	
	public void error(String message, Object[] args) {
		message("ERROR", message, args);
	}
	
	public void warn(String message, Object[] args) {
		message("WARN", message, args);
	}
	
	private void message(String level, String message, Object[] args) {
		String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		System.out.println(timestamp + " [" + level + "] " + this.clazz.getSimpleName() + " - " + String.format(message.replace("{}", "%s"), args));
	}
}
