package org.nonpolar.commons.nuona.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author shanguoming 2015年1月13日 下午5:36:43
 * @version V1.0
 * @modify: {原因} by shanguoming 2015年1月13日 下午5:36:43
 */
public class NuoNaEncoder extends MessageToByteEncoder<NuoNaProtocol> {
	
	public NuoNaEncoder() {
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, NuoNaProtocol protocal, ByteBuf out) throws Exception {
		if (null != protocal) {
			byte[] b = protocal.toBytes();
			if (null != b) {
				out.writeBytes(b); // 消息体中包含我们要发送的数据
			}
		}
	}
}
