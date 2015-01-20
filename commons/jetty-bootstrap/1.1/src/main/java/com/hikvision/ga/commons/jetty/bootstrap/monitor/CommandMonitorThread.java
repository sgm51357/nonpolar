package com.hikvision.ga.commons.jetty.bootstrap.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import com.hikvision.ga.commons.jetty.bootstrap.log.LogProxy;

public class CommandMonitorThread extends Thread {
	
	public static final String LOCALHOST = "127.0.0.1";
	private static final LogProxy log = LogProxy.getLogger(CommandMonitorThread.class);
	private final ServerSocket socket;
	private final Map<String, Command> commands = new HashMap<String, Command>();
	
	public CommandMonitorThread(int port, Command[] commands) throws IOException {
		setDaemon(true);
		if (commands != null) {
			for (Command command : commands) {
				this.commands.put(command.getId(), command);
			}
		}
		setDaemon(true);
		setName("Bootstrap Command Monitor");
		this.socket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
	}
	
	public void run() {
		log.debug("Listening for commands: {}", new Object[] {this.socket});
		boolean running = true;
		while (running) {
			try {
				Socket client = this.socket.accept();
				log.debug("Accepted client: {}", new Object[] {client});
				BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
				String commandId = reader.readLine();
				log.debug("Read command: {}", new Object[] {commandId});
				client.close();
				if (commandId == null) {
					commandId = "PING";
				}
				Command command = (Command)this.commands.get(commandId);
				if (command == null) {
					log.error("Unknown command: {}", new Object[] {commandId});
				} else running = !(command.execute());
			} catch (Exception e) {
				log.error("Failed", e);
			}
		}
		try {
			this.socket.close();
		} catch (IOException e) {
		}
		log.debug("Stopped", new Object[0]);
	}
	
	public int getPort() {
		return this.socket.getLocalPort();
	}
	
	public static abstract interface Command {
		
		public abstract String getId();
		
		public abstract boolean execute();
	}
}
