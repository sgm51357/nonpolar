package com.hikvision.ga.commons.jetty.bootstrap;

public class ShutdownHelper {
	
	private static ShutdownDelegate delegate = new JavaShutdownDelegate();
	
	public static ShutdownDelegate getDelegate() {
		if (delegate == null) {
			throw new IllegalStateException();
		}
		return delegate;
	}
	
	public static void setDelegate(ShutdownDelegate delegate) {
		if (delegate == null) {
			throw new NullPointerException();
		}
		ShutdownHelper.delegate = delegate;
	}
	
	public static void exit(int code) {
		getDelegate().doExit(code);
	}
	
	public static void halt(int code) {
		getDelegate().doHalt(code);
	}
	
	public static class JavaShutdownDelegate implements ShutdownHelper.ShutdownDelegate {
		
		public void doExit(int code) {
			System.exit(code);
		}
		
		public void doHalt(int code) {
			Runtime.getRuntime().halt(code);
		}
	}
	
	public static abstract interface ShutdownDelegate {
		
		public abstract void doExit(int paramInt);
		
		public abstract void doHalt(int paramInt);
	}
}
