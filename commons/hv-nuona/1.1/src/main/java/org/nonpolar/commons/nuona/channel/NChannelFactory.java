package org.nonpolar.commons.nuona.channel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author shanguoming 2014年12月26日 下午1:54:08
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年12月26日 下午1:54:08
 */
public class NChannelFactory {
	
	public static List<NChannel> channels = new CopyOnWriteArrayList<NChannel>();
	public static Map<String, List<RegistryService>> registryServices = new ConcurrentHashMap<String, List<RegistryService>>();
}
