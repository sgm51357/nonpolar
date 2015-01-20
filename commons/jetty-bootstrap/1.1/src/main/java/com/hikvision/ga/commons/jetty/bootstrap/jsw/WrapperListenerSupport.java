package com.hikvision.ga.commons.jetty.bootstrap.jsw;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tanukisoftware.wrapper.WrapperListener;

public abstract class WrapperListenerSupport implements WrapperListener {
	
	protected final Logger log;
	
	public WrapperListenerSupport() {
		this.log = LoggerFactory.getLogger(super.getClass());
	}
	
	public Integer start(String[] args) {
		this.log.info("Starting with arguments: {}", Arrays.asList(args));
		try {
			return doStart(args);
		} catch (Exception e) {
			this.log.error("Failed to start", e);
		}
		return Integer.valueOf(1);
	}
	
	protected abstract Integer doStart(String[] paramArrayOfString) throws Exception;
	
	public int stop(int code) {
		this.log.info("Stopping with code: {}", Integer.valueOf(code));
		try {
			return doStop(code);
		} catch (Exception e) {
			this.log.error("Failed to stop cleanly", e);
		}
		return 1;
	}
	
	protected abstract int doStop(int paramInt) throws Exception;
	
	public void controlEvent(int code) {
		this.log.info("Received control event: {}", Integer.valueOf(code));
		try {
			doControlEvent(code);
		} catch (Exception e) {
			this.log.error("Failed to handle control event[{}]", Integer.valueOf(code), e);
		}
	}
	
	protected abstract void doControlEvent(int paramInt) throws Exception;
}
