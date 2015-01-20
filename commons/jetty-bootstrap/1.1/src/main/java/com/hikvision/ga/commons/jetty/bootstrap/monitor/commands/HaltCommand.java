package com.hikvision.ga.commons.jetty.bootstrap.monitor.commands;

import com.hikvision.ga.commons.jetty.bootstrap.ShutdownHelper;
import com.hikvision.ga.commons.jetty.bootstrap.monitor.CommandMonitorThread;

public class HaltCommand implements CommandMonitorThread.Command {
	
	public static final String NAME = "HALT";
	
	public String getId() {
		return "HALT";
	}
	
	public boolean execute() {
		ShutdownHelper.halt(666);
		throw new Error("Unreachable statement");
	}
}
