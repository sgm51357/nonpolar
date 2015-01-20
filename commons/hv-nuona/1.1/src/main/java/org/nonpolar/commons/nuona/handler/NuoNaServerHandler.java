package org.nonpolar.commons.nuona.handler;

import org.nonpolar.commons.nuona.codec.NuoNaProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author shanguoming 2015年1月13日 下午5:34:12
 * @version V1.0
 * @modify: {原因} by shanguoming 2015年1月13日 下午5:34:12
 */
public class NuoNaServerHandler extends ChannelInboundHandlerAdapter {
	
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		System.out.println("server : channelRegistered");
		super.channelRegistered(ctx);
	}
	
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		System.out.println("server : channelUnregistered");
		super.channelUnregistered(ctx);
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("server : channelActive");
		super.channelActive(ctx);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("server : channelInactive");
		super.channelInactive(ctx);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(msg instanceof NuoNaProtocol){
			
		}
		super.channelRead(ctx, msg);
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		System.out.println("server : channelReadComplete");
		super.channelReadComplete(ctx);
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		System.out.println("server : userEventTriggered");
		super.userEventTriggered(ctx, evt);
	}
	
	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		System.out.println("server : channelWritabilityChanged");
		super.channelWritabilityChanged(ctx);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("server : exceptionCaught");
		super.exceptionCaught(ctx, cause);
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext channelhandlercontext) throws Exception {
		System.out.println("server : handlerAdded");
		super.handlerAdded(channelhandlercontext);
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext channelhandlercontext) throws Exception {
		System.out.println("server : handlerRemoved");
		super.handlerRemoved(channelhandlercontext);
	}
	
	@Override
	public boolean isSharable() {
		System.out.println("server : isSharable");
		return super.isSharable();
	}
}
