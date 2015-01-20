package com.hikvision.ga.commons.jetty.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PropertyMap extends HashMap<String, String> {
	
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 1L;
	
	public PropertyMap() {
	}
	
	public PropertyMap(Map<String, String> map) {
		super(map);
	}
	
	public PropertyMap(Properties properties) {
		putAll(properties);
	}
	
	public void putAll(Properties props) {
		for (Iterator<?> i$ = props.keySet().iterator(); i$.hasNext();) {
			Object key = i$.next();
			put(key.toString(), String.valueOf(props.get(key)));
		}
	}
	
	public String get(String key, String defaultValue) {
		String value = (String)super.get(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}
	
	public void load(InputStream input) throws IOException {
		Properties p = new Properties();
		p.load(input);
		putAll(p);
	}
	
	public void load(URL url) throws IOException {
		InputStream input = url.openStream();
		Throwable localThrowable2 = null;
		try {
			load(input);
		} catch (Throwable localThrowable1) {
		} finally {
			if (input != null) if (localThrowable2 != null) try {
				input.close();
			} catch (Throwable x2) {
				localThrowable2.addSuppressed(x2);
			}
			else input.close();
		}
	}
	
	public List<String> keys() {
		List<String> keys = new ArrayList<String>(keySet());
		Collections.sort(keys);
		return Collections.unmodifiableList(keys);
	}
}
