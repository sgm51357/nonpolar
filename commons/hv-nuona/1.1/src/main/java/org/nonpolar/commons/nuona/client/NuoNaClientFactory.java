package org.nonpolar.commons.nuona.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.nonpolar.commons.nuona.codec.NuoNaDecoder;
import org.nonpolar.commons.nuona.codec.NuoNaEncoder;
import org.nonpolar.commons.nuona.codec.NuoNaProtocol;
import org.nonpolar.commons.nuona.handler.NuoNaClientHandler;

/**
 * @author shanguoming 2015年1月14日 下午4:55:06
 * @version V1.0
 * @modify: {原因} by shanguoming 2015年1月14日 下午4:55:06
 */
public class NuoNaClientFactory {
	
	private static final NuoNaClientFactory factory = new NuoNaClientFactory();
	private Map<String, Channel> channels = new HashMap<String, Channel>();
	public Bootstrap bootstrap = null;
	
	private NuoNaClientFactory() {
		bootstrap = new Bootstrap();
		// 指定channel类型
		bootstrap.channel(NioSocketChannel.class);
		// 指定Handler
		bootstrap.handler(new ChannelInitializer<Channel>() {
			
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("decoder", new NuoNaDecoder());
				pipeline.addLast("encoder", new NuoNaEncoder());
				pipeline.addLast("handler", new NuoNaClientHandler());
			}
		});
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		// 指定EventLoopGroup
		bootstrap.group(new NioEventLoopGroup());
	}
	
	public static NuoNaClientFactory getInstance() {
		return factory;
	}
	
	public synchronized Channel getChannel(String host, int port) {
		try {
			Channel channel = channels.get(host + ":" + port);
			if (null == channel && null != bootstrap) {
				channel = bootstrap.connect(new InetSocketAddress(host, port)).sync().channel();
				if (null != channel) {
					channels.put(host + ":" + port, channel);
				}
			}
			return channel;
		} catch (InterruptedException e) {
		}
		return null;
	}
	
	public static void main(String[] args) throws InterruptedException {
		Channel channel = NuoNaClientFactory.getInstance().getChannel("127.0.0.1", 8000);
		for (int i = 0; i < 1; i++) {
			NuoNaProtocol protocol = new NuoNaProtocol();
			int[] bs = {10};
//			protocol.setBf("sldfkiesll".getBytes());
			channel.write(protocol);
			channel.flush();
			Thread.sleep(1000);
		}
	}
}
