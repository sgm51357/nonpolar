package com.hikvision.ga.commons.jetty.bootstrap.jsw;

import org.slf4j.MDC;
import org.tanukisoftware.wrapper.WrapperManager;
import org.tanukisoftware.wrapper.WrapperUser;
import com.hikvision.ga.commons.jetty.bootstrap.Launcher;
import com.hikvision.ga.commons.jetty.bootstrap.ShutdownHelper;

public class JswLauncher extends WrapperListenerSupport {
	
	private Launcher launcher;
	
	protected Integer doStart(String[] args) throws Exception {
		if (WrapperManager.isControlledByNativeWrapper()) {
			String username = "*UNKNOWN";
			WrapperUser user = WrapperManager.getUser(false);
			if (user != null) {
				username = user.getUser();
			} else {
				this.log.warn("Failed to query native user details, local environment may have problems using JSW functionality");
			}
			this.log.info("JVM ID: {}, JVM PID: {}, Wrapper PID: {}, User: {}",
			        new Object[] {Integer.valueOf(WrapperManager.getJVMId()), Integer.valueOf(WrapperManager.getJavaPID()), Integer.valueOf(WrapperManager.getWrapperPID()), username});
		}
		this.launcher = new Launcher(null, null, args) {
			
			public void commandStop() {
				WrapperManager.stopAndReturn(0);
			}
		};
		this.launcher.start();
		byte[] buf = new byte[1024];
		while (System.in.read(buf) != -1) {
			System.out.println(new String(buf));
		}
		return null;
	}
	
	protected int doStop(int code) throws Exception {
		this.launcher.stop();
		return code;
	}
	
	protected void doControlEvent(int code) {
		if ((202 == code) && (WrapperManager.isLaunchedAsService())) {
			this.log.debug("Launched as a service; ignoring event: {}", Integer.valueOf(code));
		} else {
			this.log.debug("Stopping");
			WrapperManager.stop(0);
			throw new Error("unreachable");
		}
	}
	
	public static void main(String[] args) throws Exception {
		MDC.put("userId", "*SYSTEM");
		ShutdownHelper.setDelegate(new JswShutdownDelegate());
		String[] arg = {"./conf/jetty.xml","./conf/jetty-requestlog.xml"};
		WrapperManager.start(new JswLauncher(), arg);
	}
}
