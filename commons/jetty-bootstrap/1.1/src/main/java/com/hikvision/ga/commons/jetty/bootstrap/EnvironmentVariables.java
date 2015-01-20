package com.hikvision.ga.commons.jetty.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvironmentVariables implements ConfigurationBuilder.Customizer {
	
	private static final Logger log = LoggerFactory.getLogger(EnvironmentVariables.class);
	
	public void apply(ConfigurationBuilder builder) throws Exception {
		maybeSetLegacy(builder, "application-port", "PLEXUS_APPLICATION_PORT");
		maybeSet(builder, "application-port", "SPORE_APPLICATION_PORT");
	}
	
	private boolean maybeSet(ConfigurationBuilder builder, String property, String env) {
		String value = System.getenv(env);
		if (value != null) {
			log.debug("Environment variable: {}={}", env, value);
			builder.set(property, value);
			return true;
		}
		return false;
	}
	
	private void maybeSetLegacy(ConfigurationBuilder builder, String property, String env) {
		if (maybeSet(builder, property, env)) log.warn("Detected legacy environment variable: {}", env);
	}
}
