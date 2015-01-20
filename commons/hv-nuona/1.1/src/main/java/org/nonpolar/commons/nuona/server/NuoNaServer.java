package org.nonpolar.commons.nuona.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetSocketAddress;
import org.nonpolar.commons.nuona.codec.NuoNaDecoder;
import org.nonpolar.commons.nuona.codec.NuoNaEncoder;
import org.nonpolar.commons.nuona.handler.NuoNaServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shanguoming 2015年1月13日 下午5:31:09
 * @version V1.0
 * @modify: {原因} by shanguoming 2015年1月13日 下午5:31:09
 */
public class NuoNaServer {
	
	private final Logger log = LoggerFactory.getLogger(NuoNaServer.class);
	
	private int port;
	private ServerBootstrap bootstrap = null;
	/*
	 * NioEventLoopGroup实际上就是个线程池,
	 * NioEventLoopGroup在后台启动了n个NioEventLoop来处理Channel事件,
	 * 每一个NioEventLoop负责处理m个Channel,
	 * NioEventLoopGroup从NioEventLoop数组里挨个取出NioEventLoop来处理Channel
	 */
	/** 用于分配处理业务线程的线程组个数 */
	private EventLoopGroup bossGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
	/** 业务出现线程大小 */
	private EventLoopGroup workerGroup = new NioEventLoopGroup(4);
	
	public NuoNaServer(int port) {
		this.port = port;
	}
	
	public void start() {
		if (null == bootstrap) {
			try {
				bootstrap = new ServerBootstrap();
				bootstrap.group(bossGroup, workerGroup);
				bootstrap.channel(NioServerSocketChannel.class);
				bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
					
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pipeline = ch.pipeline();
						pipeline.addLast("decoder", new NuoNaDecoder());
						pipeline.addLast("encoder", new NuoNaEncoder());
						pipeline.addLast(new NuoNaServerHandler());
					}
				});
				bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
				// 开放端口供客户端访问。
				bootstrap.bind(new InetSocketAddress("0.0.0.0",port)).sync();
			} catch (InterruptedException e) {
				log.error("",e);
				workerGroup.shutdownGracefully();
				bossGroup.shutdownGracefully();
			}
		}
	}
	
	public void stop() {
		if (null != bootstrap) {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
			bootstrap = null;
		}
	}
	
	public int getPort() {
		return port;
	}
	
	public static void main(String[] args) {
		NuoNaServer server = new NuoNaServer(8000);
		server.start();
	}
}
