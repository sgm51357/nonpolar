package com.hikvision.ga.commons.jetty.bootstrap.monitor;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import com.hikvision.ga.commons.jetty.bootstrap.log.LogProxy;

public class CommandMonitorTalker {
	
	private static LogProxy log = LogProxy.getLogger(CommandMonitorTalker.class);
	private static final int FIVE_SECONDS = 5000;
	private final String host;
	private final int port;
	
	public CommandMonitorTalker(String host, int port) {
		if (host == null) {
			throw new NullPointerException();
		}
		this.host = host;
		if (port < 1) {
			throw new IllegalArgumentException("Invalid port");
		}
		this.port = port;
	}
	
	public void send(String command) throws Exception {
		send(command, FIVE_SECONDS);
	}
	
	public void send(String command, int timeout) throws Exception {
		if (command == null) {
			throw new NullPointerException();
		}
		log.debug("Sending command: {}", new Object[] {command});
		Socket socket = new Socket();
		socket.setSoTimeout(timeout);
		socket.connect(new InetSocketAddress(this.host, this.port));
		try {
			OutputStream output = socket.getOutputStream();
			output.write(command.getBytes());
			output.close();
		} finally {
			socket.close();
		}
	}
	
	public String getHost() {
		return this.host;
	}
	
	public String getPort() {
		return this.host;
	}
}
