package com.hikvision.ga.commons.jetty.bootstrap;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.eclipse.jetty.xml.XmlParser;
import org.eclipse.jetty.xml.XmlParser.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.bridge.SLF4JBridgeHandler;
import com.hikvision.ga.commons.jetty.bootstrap.jetty.JettyServer;
import com.hikvision.ga.commons.jetty.bootstrap.monitor.CommandMonitorThread;
import com.hikvision.ga.commons.jetty.bootstrap.monitor.KeepAliveThread;
import com.hikvision.ga.commons.jetty.bootstrap.monitor.commands.ExitCommand;
import com.hikvision.ga.commons.jetty.bootstrap.monitor.commands.HaltCommand;
import com.hikvision.ga.commons.jetty.bootstrap.monitor.commands.PingCommand;
import com.hikvision.ga.commons.jetty.bootstrap.monitor.commands.StopApplicationCommand;

public class Launcher {
	
	public static final String COMMAND_MONITOR_PORT = CommandMonitorThread.class.getName() + ".port";
	public static final String SYSTEM_USERID = "*SYSTEM";
	private static final String FIVE_SECONDS = "5000";
	private static final String ONE_SECOND = "1000";
	private final JettyServer server;
	
	public Launcher(ClassLoader classLoader, Map<String, String> overrides, String[] args) throws Exception {
		Logger log = LoggerFactory.getLogger(Launcher.class);
		if (args == null) {
			throw new NullPointerException();
		}
		if (args.length == 0) {
			throw new IllegalArgumentException("Missing args");
		}
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		ClassLoader cl = (classLoader == null)?super.getClass().getClassLoader():classLoader;
		log.info("jetty basedir: {}", new File(".").getCanonicalPath());
		ConfigurationBuilder builder = new ConfigurationBuilder().defaults().set("basedir", new File(".").getCanonicalPath()).custom(new EnvironmentVariables())
		        .override(System.getProperties());
		if (overrides != null) {
			builder.properties(overrides);
		}
		Map<String, String> props = builder.build();
		XmlParser xmlParser = new XmlParser();
		Node node = xmlParser.parse("./config.xml");
		Node n = node.get("webapps");
		String port = n.getAttribute("port");
		props.put("application-port", port);
		System.getProperties().putAll(props);
		ConfigurationHolder.set(props);
		log.info("Java: {}, {}, {}, {}", new Object[] {System.getProperty("java.version"), System.getProperty("java.vm.name"), System.getProperty("java.vm.vendor"), System.getProperty("java.vm.version")});
		log.info("OS: {}, {}, {}", new Object[] {System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch")});
		log.info("User: {}, {}, {}", new Object[] {System.getProperty("user.name"), System.getProperty("user.language"), System.getProperty("user.home")});
		log.info("CWD: {}", System.getProperty("user.dir"));
		File tmpdir = TemporaryDirectory.get();
		log.info("TMP: {}", tmpdir);
		this.server = new JettyServer(cl, props, args);
	}
	
	public void start() throws Exception {
		maybeEnableCommandMonitor();
		maybeEnableShutdownIfNotAlive();
		this.server.start();
	}
	
	private String getProperty(String name, String defaultValue) {
		String value = System.getProperty(name, System.getenv(name));
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}
	
	private void maybeEnableCommandMonitor() throws IOException {
		String port = getProperty(COMMAND_MONITOR_PORT, null);
		if (port != null) new CommandMonitorThread(Integer.parseInt(port), new CommandMonitorThread.Command[] {new StopApplicationCommand(new Runnable() {
			
			public void run() {
				Launcher.this.commandStop();
			}
		}), new PingCommand(), new ExitCommand(), new HaltCommand()}).start();
	}
	
	private void maybeEnableShutdownIfNotAlive() throws IOException {
		String port = getProperty(KeepAliveThread.KEEP_ALIVE_PORT, null);
		if (port != null) {
			String pingInterval = getProperty(KeepAliveThread.KEEP_ALIVE_PING_INTERVAL, FIVE_SECONDS);
			String timeout = getProperty(KeepAliveThread.KEEP_ALIVE_TIMEOUT, ONE_SECOND);
			new KeepAliveThread("127.0.0.1", Integer.parseInt(port), Integer.parseInt(pingInterval), Integer.parseInt(timeout)).start();
		}
	}
	
	public void commandStop() {
		ShutdownHelper.exit(0);
	}
	
	public void stop() throws Exception {
		this.server.stop();
	}
	
	public static void main(String[] args) throws Exception {
		MDC.put("userId", "*SYSTEM");
		new Launcher(null, null, args).start();
	}
}
