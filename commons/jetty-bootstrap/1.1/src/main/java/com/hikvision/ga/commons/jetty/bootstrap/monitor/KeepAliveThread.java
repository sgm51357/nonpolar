package com.hikvision.ga.commons.jetty.bootstrap.monitor;

import java.io.IOException;
import java.net.ConnectException;
import com.hikvision.ga.commons.jetty.bootstrap.ShutdownHelper;

public class KeepAliveThread extends Thread {
	
	public static final String KEEP_ALIVE_PORT = KeepAliveThread.class.getName() + ".port";
	public static final String KEEP_ALIVE_PING_INTERVAL = KeepAliveThread.class.getName() + ".pingInterval";
	public static final String KEEP_ALIVE_TIMEOUT = KeepAliveThread.class.getName() + ".timeout";
	private final CommandMonitorTalker talker;
	private final int interval;
	private final int timeout;
	private final Runnable task;
	private volatile boolean running;
	
	public KeepAliveThread(String host, int port, int interval, int timeout, Runnable task) throws IOException {
		setDaemon(true);
		setName(super.getClass().getName());
		this.talker = new CommandMonitorTalker(host, port);
		this.interval = interval;
		this.timeout = timeout;
		this.task = task;
		this.running = true;
	}
	
	public KeepAliveThread(String host, int port, int interval, int timeout) throws IOException {
		this(host, port, interval, timeout, new Runnable() {
			
			public void run() {
				ShutdownHelper.halt(666);
			}
		});
	}
	
	public void run() {
		while (this.running)
			try {
				try {
					ping();
					sleep(this.interval);
				} catch (InterruptedException e) {
					ping();
				}
			} catch (ConnectException e) {
				stopRunning();
				executeTask();
			}
	}
	
	private void ping() throws ConnectException {
		try {
			this.talker.send("PING", this.timeout);
		} catch (ConnectException e) {
			throw e;
		} catch (Exception e) {
		}
	}
	
	void executeTask() {
		this.task.run();
	}
	
	public void stopRunning() {
		this.running = false;
	}
}
