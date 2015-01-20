package com.hikvision.ga.commons.jetty.bootstrap.monitor.commands;

import com.hikvision.ga.commons.jetty.bootstrap.log.LogProxy;
import com.hikvision.ga.commons.jetty.bootstrap.monitor.CommandMonitorThread;

public class StopMonitorCommand implements CommandMonitorThread.Command {
	
	private static final LogProxy log = LogProxy.getLogger(StopMonitorCommand.class);
	public static final String NAME = "STOP_MONITOR";
	
	public String getId() {
		return "STOP_MONITOR";
	}
	
	public boolean execute() {
		log.debug("Requesting monitor stop", new Object[0]);
		return true;
	}
}
