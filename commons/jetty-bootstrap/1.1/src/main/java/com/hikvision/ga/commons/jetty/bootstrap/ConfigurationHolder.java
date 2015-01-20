package com.hikvision.ga.commons.jetty.bootstrap;

import java.util.Map;

public class ConfigurationHolder {
	
	private static final InheritableThreadLocal<Map<String, String>> reference = new InheritableThreadLocal<Map<String, String>>();
	
	public static void set(Map<String, String> properties) {
		reference.set(properties);
	}
	
	public static Map<String, String> get() {
		return reference.get();
	}
	
	public static void unset() {
		reference.remove();
	}
}
