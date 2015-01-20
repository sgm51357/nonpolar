package org.nonpolar.commons.nuona.codec;


/**
 * @author shanguoming 2015年1月15日 下午1:28:54
 * @version V1.0
 * @modify: {原因} by shanguoming 2015年1月15日 下午1:28:54
 */
public class NuoNaProtocolFactory {
	
	private static final NuoNaProtocolFactory factory = new NuoNaProtocolFactory();
	
	private NuoNaProtocolFactory() {
	}
	
	public static NuoNaProtocolFactory getInstance() {
		return factory;
	}
}
