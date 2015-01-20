package com.hikvision.ga.commons.jetty.bootstrap;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import org.codehaus.plexus.interpolation.EnvarBasedValueSource;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationBuilder {
	
	private static final Logger log = LoggerFactory.getLogger(ConfigurationBuilder.class);
	private final PropertyMap properties;
	
	public ConfigurationBuilder() {
		this.properties = new PropertyMap();
	}
	
	public ConfigurationBuilder properties(Map<String, String> props) {
		if (props == null) {
			throw new NullPointerException();
		}
		if (log.isDebugEnabled()) {
			log.debug("Adding properties:");
			for (Map.Entry<String, String> entry : props.entrySet()) {
				log.debug("  {}='{}'", entry.getKey(), entry.getValue());
			}
		}
		this.properties.putAll(props);
		return this;
	}
	
	public ConfigurationBuilder properties(URL url) throws IOException {
		if (url == null) {
			throw new NullPointerException();
		}
		log.debug("Reading properties from: {}", url);
		PropertyMap props = new PropertyMap();
		props.load(url);
		return properties(props);
	}
	
	private URL getResource(String name) {
		URL url = null;
		try {
			url = new URL("file:" + this.properties.get("basedir") + name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return url;
	}
	
	public ConfigurationBuilder properties(String resource, boolean required) throws IOException {
		URL url = getResource(resource);
		if (url == null) {
			if (required) {
				throw new IllegalStateException("Missing required resource: " + resource);
			}
			return this;
		}
		return properties(url);
	}
	
	public ConfigurationBuilder defaults() throws IOException {
		URL url = ConfigurationBuilder.class.getResource("default.properties");
		if (url == null) {
			throw new IllegalStateException("Missing required resource: " + "default.properties");
		}
		return properties(url);
	}
	
	public ConfigurationBuilder set(String name, String value) {
		if (name == null) {
			throw new NullPointerException();
		}
		if (value == null) {
			throw new NullPointerException();
		}
		log.debug("Set: {}={}", name, value);
		this.properties.put(name, value);
		return this;
	}
	
	public ConfigurationBuilder custom(Customizer customizer) throws Exception {
		if (customizer == null) {
			throw new NullPointerException();
		}
		log.debug("Customizing: {}", customizer);
		customizer.apply(this);
		return this;
	}
	
	public ConfigurationBuilder override(Map<String, String> overrides) {
		if (overrides == null) {
			throw new NullPointerException();
		}
		for (Map.Entry<String, String> entry : overrides.entrySet()) {
			String name = (String)entry.getKey();
			if (this.properties.containsKey(name)) {
				String value = (String)entry.getValue();
				log.debug("Override: {}={}", name, value);
				this.properties.put(name, value);
			}
		}
		return this;
	}
	
	public ConfigurationBuilder override(Properties overrides) {
		return override(new PropertyMap(overrides));
	}
	
	private void interpolate() throws Exception {
		Interpolator interpolator = new StringSearchInterpolator();
		interpolator.addValueSource(new MapBasedValueSource(this.properties));
		interpolator.addValueSource(new MapBasedValueSource(System.getProperties()));
		interpolator.addValueSource(new EnvarBasedValueSource());
		for (Map.Entry<String, String> entry : this.properties.entrySet())
			this.properties.put(entry.getKey(), interpolator.interpolate((String)entry.getValue()));
	}
	
	public Map<String, String> build() throws Exception {
		if (this.properties.isEmpty()) {
			throw new IllegalStateException("Not configured");
		}
		interpolate();
		PropertyMap props = new PropertyMap(this.properties);
		log.info("Properties:");
		for (String key : props.keys()) {
			log.info("  {}='{}'", key, props.get(key));
		}
		return props;
	}
	
	public static abstract interface Customizer {
		
		public abstract void apply(ConfigurationBuilder paramConfigurationBuilder) throws Exception;
	}
}
