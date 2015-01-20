package org.nonpolar.commons.nuona.handler;

import java.net.SocketAddress;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * @author shanguoming 2015年1月14日 下午4:47:22
 * @version V1.0
 * @modify: {原因} by shanguoming 2015年1月14日 下午4:47:22
 */
public class NuoNaClientHandler extends ChannelOutboundHandlerAdapter {
	
	@Override
	public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
		System.out.println("client : bind");
		super.bind(ctx, localAddress, promise);
	}
	
	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
		System.out.println("client : connect");
		super.connect(ctx, remoteAddress, localAddress, promise);
	}
	
	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		System.out.println("client : disconnect");
		super.disconnect(ctx, promise);
	}
	
	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		System.out.println("client : close");
		super.close(ctx, promise);
	}
	
	@Override
	public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		System.out.println("client : deregister");
		super.deregister(ctx, promise);
	}
	
	@Override
	public void read(ChannelHandlerContext ctx) throws Exception {
		System.out.println("client : read");
		super.read(ctx);
	}
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		System.out.println("client : write");
		super.write(ctx, msg, promise);
	}
	
	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception {
		System.out.println("client : flush");
		super.flush(ctx);
	}
}
