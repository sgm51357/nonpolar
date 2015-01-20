package com.hikvision.ga.commons.jetty.bootstrap.monitor.commands;

import com.hikvision.ga.commons.jetty.bootstrap.ShutdownHelper;
import com.hikvision.ga.commons.jetty.bootstrap.monitor.CommandMonitorThread;

public class ExitCommand implements CommandMonitorThread.Command {
	
	public static final String NAME = "EXIT";
	
	public String getId() {
		return "EXIT";
	}
	
	public boolean execute() {
		ShutdownHelper.exit(666);
		throw new Error("Unreachable statement");
	}
}
