package com.hikvision.ga.commons.jetty.bootstrap.monitor.commands;

import com.hikvision.ga.commons.jetty.bootstrap.log.LogProxy;
import com.hikvision.ga.commons.jetty.bootstrap.monitor.CommandMonitorThread;

public class PingCommand implements CommandMonitorThread.Command {
	
	private static final LogProxy log = LogProxy.getLogger(PingCommand.class);
	public static final String NAME = "PING";
	
	public String getId() {
		return "PING";
	}
	
	public boolean execute() {
		log.debug("Pinged", new Object[0]);
		return false;
	}
}
