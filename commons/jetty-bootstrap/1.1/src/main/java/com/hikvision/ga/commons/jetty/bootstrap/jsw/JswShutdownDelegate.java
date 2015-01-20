package com.hikvision.ga.commons.jetty.bootstrap.jsw;

import org.tanukisoftware.wrapper.WrapperManager;
import com.hikvision.ga.commons.jetty.bootstrap.ShutdownHelper;

public class JswShutdownDelegate implements ShutdownHelper.ShutdownDelegate {
	
	public void doExit(int code) {
		WrapperManager.stop(code);
	}
	
	public void doHalt(int code) {
		WrapperManager.stopImmediate(code);
	}
}
