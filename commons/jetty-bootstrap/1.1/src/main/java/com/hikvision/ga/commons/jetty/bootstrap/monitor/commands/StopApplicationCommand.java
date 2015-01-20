package com.hikvision.ga.commons.jetty.bootstrap.monitor.commands;

import com.hikvision.ga.commons.jetty.bootstrap.log.LogProxy;
import com.hikvision.ga.commons.jetty.bootstrap.monitor.CommandMonitorThread;

public class StopApplicationCommand implements CommandMonitorThread.Command {
	
	private static final LogProxy log = LogProxy.getLogger(StopApplicationCommand.class);
	public static final String NAME = "STOP";
	private final Runnable shutdown;
	
	public StopApplicationCommand(Runnable shutdown) {
		if (shutdown == null) {
			throw new NullPointerException();
		}
		this.shutdown = shutdown;
	}
	
	public String getId() {
		return "STOP";
	}
	
	public boolean execute() {
		log.debug("Requesting application stop", new Object[0]);
		this.shutdown.run();
		return false;
	}
}
