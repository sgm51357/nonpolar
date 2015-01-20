package com.ivms6.core.scheduler;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * <p>
 * GUID 生成类
 * </p>
 * @author shanguoming 2012-9-24 下午2:09:31
 * @version V1.0
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2012-9-24
 * @modify by reason:{方法名}:{原因}
 */
class GUIDGenerator {
	
	/**
	 * 机器描述
	 */
	private static String MACHINE_DESCRIPTOR = getMachineDescriptor();
	
	/**
	 * 生成48位的GUID
	 * @author shanguoming 2012-9-24 下午2:10:15
	 * @return GUID字符串
	 */
	public static String generate() {
		StringBuffer id = new StringBuffer();
		encode(id, MACHINE_DESCRIPTOR);
		encode(id, Runtime.getRuntime());
		encode(id, Thread.currentThread());
		encode(id, System.currentTimeMillis());
		encode(id, getRandomInt());
		return id.toString();
	}
	
	/**
	 * 获取计算机信息
	 * @author shanguoming 2012-9-24 下午2:11:25
	 * @return
	 */
	private static String getMachineDescriptor() {
		StringBuffer descriptor = new StringBuffer();
		descriptor.append(System.getProperty("os.name"));
		descriptor.append("::");
		descriptor.append(System.getProperty("os.arch"));
		descriptor.append("::");
		descriptor.append(System.getProperty("os.version"));
		descriptor.append("::");
		descriptor.append(System.getProperty("user.name"));
		descriptor.append("::");
		StringBuffer b = buildNetworkInterfaceDescriptor();
		if (b != null) {
			descriptor.append(b);
		} else {
			InetAddress addr;
			try {
				addr = InetAddress.getLocalHost();
				descriptor.append(addr.getHostAddress());
			} catch (UnknownHostException e) {
				;
			}
		}
		return descriptor.toString();
	}
	
	/**
	 * 构建网络接口描述
	 * @author shanguoming 2012-9-24 下午2:11:53
	 * @return
	 */
	private static StringBuffer buildNetworkInterfaceDescriptor() {
		Enumeration<NetworkInterface> e1;
		try {
			e1 = NetworkInterface.getNetworkInterfaces();
		} catch (Throwable t) {
			// not available
			return null;
		}
		StringBuffer b = new StringBuffer();
		while (e1.hasMoreElements()) {
			NetworkInterface ni = (NetworkInterface)e1.nextElement();
			StringBuffer b1 = getMACAddressDescriptor(ni);
			StringBuffer b2 = getInetAddressDescriptor(ni);
			StringBuffer b3 = new StringBuffer();
			if (b1 != null) {
				b3.append(b1);
			}
			if (b2 != null) {
				if (b3.length() > 0) {
					b3.append('=');
				}
				b3.append(b2);
			}
			if (b3.length() > 0) {
				if (b.length() > 0) {
					b.append(';');
				}
				b.append(b3);
			}
		}
		return b;
	}
	
	/**
	 * 获取mac地址描述
	 * @author shanguoming 2012-9-24 下午2:12:08
	 * @param ni 网络接口
	 * @return
	 */
	private static StringBuffer getMACAddressDescriptor(NetworkInterface ni) {
		byte[] haddr;
		try {
			haddr = ni.getHardwareAddress();
		} catch (Throwable t) {
			// not available.
			haddr = null;
		}
		StringBuffer b = new StringBuffer();
		if (haddr != null) {
			for (int i = 0; i < haddr.length; i++) {
				if (b.length() > 0) {
					b.append("-");
				}
				String hex = Integer.toHexString(0xff & haddr[i]);
				if (hex.length() == 1) {
					b.append('0');
				}
				b.append(hex);
			}
		}
		return b;
	}
	
	/**
	 * 获取网络地址描述
	 * @author shanguoming 2012-9-24 下午2:13:11
	 * @param ni 网络接口
	 * @return
	 */
	private static StringBuffer getInetAddressDescriptor(NetworkInterface ni) {
		StringBuffer b = new StringBuffer();
		Enumeration<InetAddress> e2 = ni.getInetAddresses();
		while (e2.hasMoreElements()) {
			InetAddress addr = (InetAddress)e2.nextElement();
			if (b.length() > 0) {
				b.append(',');
			}
			b.append(addr.getHostAddress());
		}
		return b;
	}
	
	/**
	 * 随机整数
	 * @author shanguoming 2012-9-24 下午2:13:26
	 * @return
	 */
	private static int getRandomInt() {
		return (int)Math.round((Math.random() * Integer.MAX_VALUE));
	}
	
	/**
	 * 把对象的hashcode编码，并添加到字符串的缓冲区
	 * @author shanguoming 2012-9-24 下午2:16:14
	 * @param b 字符串缓冲区
	 * @param obj 对象
	 */
	private static void encode(StringBuffer b, Object obj) {
		encode(b, obj.hashCode());
	}
	
	/**
	 * 对整数编码，并添加到字符串缓冲区
	 * @author shanguoming 2012-9-24 下午2:14:55
	 * @param b 字符串缓冲区
	 * @param value 整数
	 */
	private static void encode(StringBuffer b, int value) {
		String hex = Integer.toHexString(value);
		int hexSize = hex.length();
		for (int i = 8; i > hexSize; i--) {
			b.append('0');
		}
		b.append(hex);
	}
	
	/**
	 * 把长整型编码，并添加到字符串缓冲区
	 * @author shanguoming 2012-9-24 下午2:16:51
	 * @param b 字符串缓冲区
	 * @param value 长整型
	 */
	private static void encode(StringBuffer b, long value) {
		String hex = Long.toHexString(value);
		int hexSize = hex.length();
		for (int i = 16; i > hexSize; i--) {
			b.append('0');
		}
		b.append(hex);
	}
}
