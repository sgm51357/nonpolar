package com.hikvision.ga.commons.jetty.bootstrap.jetty;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hikvision.ga.commons.jetty.bootstrap.PropertyMap;

public class JettyServer {
	
	private static final Logger log = LoggerFactory.getLogger(JettyServer.class);
	private final ClassLoader classLoader;
	private final Map<String, String> properties;
	private final String[] args;
	private JettyMainThread thread;
	
	public JettyServer(ClassLoader classLoader, Map<String, String> properties, String[] args) {
		if (classLoader == null) {
			throw new NullPointerException();
		}
		this.classLoader = classLoader;
		if (properties == null) {
			throw new NullPointerException();
		}
		this.properties = properties;
		if (args == null) {
			throw new NullPointerException();
		}
		this.args = args;
	}
	
	private Exception propagateThrowable(Throwable e) throws Exception {
		if (e instanceof RuntimeException) {
			throw ((RuntimeException)e);
		}
		if (e instanceof Exception) {
			throw ((Exception)e);
		}
		if (e instanceof Error) {
			throw ((Error)e);
		}
		throw new Error(e);
	}
	
	public synchronized void start() throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(this.classLoader);
		try {
			AtomicReference<Throwable> exception = new AtomicReference<Throwable>();
			AccessController.doPrivileged(new PrivilegedAction<AtomicReference<Throwable>>() {
				
				public AtomicReference<Throwable> run() {
					try {
						JettyServer.this.doStart();
					} catch (Exception e) {
						// this.val$exception.set(e);
					}
					return null;
				}
			});
			Throwable e = (Throwable)exception.get();
			if (e != null) {
				log.error("Start failed", e);
				throw propagateThrowable(e);
			}
		} finally {
			Thread.currentThread().setContextClassLoader(cl);
		}
	}
	
	private void doStart() throws Exception {
		if (this.thread != null) {
			throw new IllegalStateException("Already started");
		}
		log.info("Starting");
		List<LifeCycle> components = new ArrayList<LifeCycle>();
		PropertyMap props = new PropertyMap();
		props.putAll(this.properties);
		XmlConfiguration last = null;
		for (String arg : this.args) {
			URL url = Resource.newResource(arg).getURL();
			if (url.getFile().toLowerCase(Locale.ENGLISH).endsWith(".properties")) {
				log.info("Loading properties: {}", url);
				props.load(url);
			} else {
				log.info("Applying configuration: {}", url);
				XmlConfiguration configuration = new XmlConfiguration(url);
				if (last != null) {
					configuration.getIdMap().putAll(last.getIdMap());
				}
				if (!(props.isEmpty())) {
					configuration.getProperties().putAll(props);
				}
				Object component = configuration.configure();
				if (component instanceof LifeCycle) {
					components.add((LifeCycle)component);
				}
				last = configuration;
			}
		}
		if (components.isEmpty()) {
			throw new Exception("Failed to configure any components");
		}
		this.thread = new JettyMainThread(components);
		this.thread.setContextClassLoader(this.classLoader);
		this.thread.startComponents();
		log.info("Started");
	}
	
	public synchronized void stop() throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(this.classLoader);
		try {
			AtomicReference<Throwable> exception = new AtomicReference<Throwable>();
			AccessController.doPrivileged(new PrivilegedAction<AtomicReference<Throwable>>() {
				
				public AtomicReference<Throwable> run() {
					try {
						JettyServer.this.doStop();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			});
			Throwable e = (Throwable)exception.get();
			if (e != null) {
				log.error("Stop failed", e);
				throw propagateThrowable(e);
			}
		} finally {
			Thread.currentThread().setContextClassLoader(cl);
		}
	}
	
	private void doStop() throws Exception {
		if (this.thread == null) {
			throw new IllegalStateException("Not started");
		}
		log.info("Stopping");
		this.thread.stopComponents();
		this.thread = null;
		log.info("Stopped");
	}
	
	private static class JettyMainThread extends Thread {
		
		private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger(1);
		private final List<LifeCycle> components;
		private final CountDownLatch started;
		private final CountDownLatch stopped;
		private volatile Exception exception;
		
		public JettyMainThread(List<LifeCycle> components) {
			super("jetty-main-" + INSTANCE_COUNTER.getAndIncrement());
			this.components = components;
			this.started = new CountDownLatch(1);
			this.stopped = new CountDownLatch(1);
		}
		
		public void run() {
			try {
				Server server = null;
				try {
					for (LifeCycle component : this.components) {
						if (component instanceof Server) {
							server = (Server)component;
						}
						if (!(component.isRunning())) {
							JettyServer.log.info("Starting: {}", component);
							component.start();
						}
					}
				} catch (Exception e) {
					this.exception = e;
				} finally {
					this.started.countDown();
				}
				if (server != null) {
					JettyServer.log.info("Running");
					server.join();
				}
			} catch (InterruptedException e) {
			} finally {
				this.stopped.countDown();
			}
		}
		
		public void startComponents() throws Exception {
			start();
			this.started.await();
			if (this.exception != null) throw this.exception;
		}
		
		public void stopComponents() throws Exception {
			Collections.reverse(this.components);
			for (LifeCycle component : this.components) {
				if (component.isRunning()) {
					JettyServer.log.info("Stopping: {}", component);
					component.stop();
				}
			}
			this.components.clear();
			this.stopped.await();
		}
	}
}
