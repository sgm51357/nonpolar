package org.nonpolar.commons.nuona.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author shanguoming 2015年1月13日 下午5:35:50
 * @version V1.0
 * @modify: {原因} by shanguoming 2015年1月13日 下午5:35:50
 */
public class NuoNaDecoder extends ByteToMessageDecoder {
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < 38) {
			return;
		}
		readStart(in);
		if (in.isReadable()) {
			int dataLength = in.readInt();
			if (dataLength > 0) {
				if (in.readableBytes() < (dataLength + 4)) {
					in.resetReaderIndex();
					return;
				} else {
					int seq = in.readInt();
					int err = in.readInt();
					byte ver = in.readByte();
					byte flg = in.readByte();
					byte zip = in.readByte();
					byte it = in.readByte();
					byte sec = in.readByte();
					byte bt = in.readByte();
					if (in.readByte() == ';') {
						byte[] body = new byte[dataLength - 22];
						in.readBytes(body);
						byte[] end = new byte[4];
						in.readBytes(end);
						if (end(end)) {
							int length = body.length;
							int i = 0;
							int pre = 0;
							int count = 0;
							for (i = 0; i < length; i++) {
								if (count == 5) {
									break;
								}
								byte b = body[i];
								if (b == ';') {
									if (pre < i) {
										switch (count) {
											case 0:
												String idx = new String(Arrays.copyOfRange(body, pre, i), CharsetUtil.UTF_8);
												break;
											case 1:
												String t = new String(Arrays.copyOfRange(body, pre, i), CharsetUtil.UTF_8);
											case 2:
												byte[] ext = Arrays.copyOfRange(body, pre, i);
											case 3:
												byte[] ser = Arrays.copyOfRange(body, pre, i);
											case 4:
												String bs = new String(Arrays.copyOfRange(body, pre, i), CharsetUtil.UTF_8);
												break;
										}
									}
									count++;
									pre = i + 1;
									continue;
								}
							}
							if (pre < length - 1) {
								byte[] bf = Arrays.copyOfRange(body, pre, length - 1);
							}
						}
					}
				}
			}
		}
		// int dataLength = start(in);
		// if (dataLength > 0) {
		// if (in.readableBytes() < (dataLength + 4)) {
		// return;
		// } else {
		// readStart(in);
		// byte[] body = new byte[dataLength];
		// in.readBytes(body);
		// byte[] end = new byte[4];
		// in.readBytes(end);
		// if (end(end)) {
		// LinkedHashMap<String, byte[]> map = decode(body);
		// // 默认协议格式验证
		// if (validateProtocolDefault(map)) {
		// NuoNaProtocol protocol = new NuoNaProtocol();
		// protocol.setLen(Integer.parseInt(new String(map.remove("len"), CharsetUtil.UTF_8)));
		// protocol.setVer(Integer.parseInt(new String(map.remove("ver"), CharsetUtil.UTF_8)));
		// protocol.setErr(Integer.parseInt(new String(map.remove("err"), CharsetUtil.UTF_8)));
		// protocol.setSeq(Integer.parseInt(new String(map.remove("seq"), CharsetUtil.UTF_8)));
		// protocol.setFlg(Integer.parseInt(new String(map.remove("flg"), CharsetUtil.UTF_8)));
		// protocol.setZip(Integer.parseInt(new String(map.remove("zip"), CharsetUtil.UTF_8)));
		// protocol.setIdx(new String(map.remove("idx"), CharsetUtil.UTF_8));
		// protocol.setIt(Integer.parseInt(new String(map.remove("it"), CharsetUtil.UTF_8)));
		// protocol.setT(new String(map.remove("t"), CharsetUtil.UTF_8));
		// protocol.setSec(Integer.parseInt(new String(map.remove("sec"), CharsetUtil.UTF_8)));
		// protocol.setSer(map.remove("ser"));
		// protocol.setBt(Integer.parseInt(new String(map.remove("bt"), CharsetUtil.UTF_8)));
		// String bs = new String(map.remove("bs"), CharsetUtil.UTF_8);
		// if (null != bs) {
		// String[] lenArr = bs.split(",");
		// if (null != lenArr && lenArr.length > 0) {
		// int length = lenArr.length;
		// int[] bsArr = new int[length];
		// for (int i = 0; i < length; i++) {
		// try {
		// bsArr[i] = Integer.parseInt(lenArr[i]);
		// } catch (NumberFormatException e) {
		// // TODO: 日志打印
		// in.resetReaderIndex();
		// return;
		// }
		// }
		// protocol.setBs(bsArr);
		// }
		// }
		// protocol.setBf(map.remove("bf"));
		// protocol.setExt(map);
		// // 扩展协议格式验证
		// if (validateProtocolExt(protocol)) {
		// in.markReaderIndex();
		// out.add(protocol);
		// } else {
		// in.resetReaderIndex();
		// }
		// } else {
		// in.resetReaderIndex();
		// }
		// } else {
		// in.resetReaderIndex();
		// }
		// }
		// } else {
		// in.resetReaderIndex();
		// }
	}
	
	private void readStart(ByteBuf in) {
		byte[] starts = "start;".getBytes();
		int count = 0;
		boolean reset = true;
		while (in.isReadable()) {
			if (count == 0) {
				reset = false;
				// 标记流读取位置
				in.markReaderIndex();
			}
			if (reset) {
				count--;
				continue;
			}
			byte b = in.readByte();
			// 匹配是否start
			if (count < 6 && starts[count] == b) {
				count++;
			} else {
				in.resetReaderIndex();
				reset = true;
			}
			if (count == 6) {
				break;
			}
		}
	}
	
	/**
	 * 匹配起始位置
	 * @author shanguoming 2015年1月16日 上午10:15:41
	 * @param in
	 * @modify: {原因} by shanguoming 2015年1月16日 上午10:15:41
	 */
	private int start(ByteBuf in) {
		byte[] starts = "start;".getBytes();
		int count = 0;
		int sum = 0;
		while (in.isReadable()) {
			if (count == 0) {
				// 标记流读取位置
				in.markReaderIndex();
			}
			byte b = in.readByte();
			// 匹配是否start
			if (count < 6 && starts[count] == b) {
				count++;
			} else if (count > 0) {// 匹配失败时，当前
				if (b != 's') {
					sum += count;
					count = 0;
				} else {
					sum += count - 1;
					count = 1;
				}
			}
			if (count == 6) {
				break;
			}
		}
		List<Byte> len = new ArrayList<Byte>();
		count = 0;
		while (in.isReadable()) {
			if (count > 14) {
				in.resetReaderIndex();
				if (sum > 0) {
					in.readBytes(new byte[sum]);
				}
				return 0;
			}
			byte b = in.readByte();
			if (b == ';') {
				break;
			}
			count++;
			len.add(b);
		}
		if (!len.isEmpty()) {
			int size = len.size();
			byte[] lens = new byte[size];
			for (int i = 0; i < size; i++) {
				Byte b = len.get(i);
				if (null != b) {
					lens[i] = b;
				}
			}
			String l = new String(lens, CharsetUtil.UTF_8);
			if (null != l && l.length() > 0) {
				String[] arr = l.split(":");
				if (null != arr && arr.length == 2) {
					if ("len".equals(arr[0])) {
						try {
							in.resetReaderIndex();
							if (sum > 0) {
								in.readBytes(new byte[sum]);
							}
							return Integer.parseInt(arr[1]);
						} catch (NumberFormatException e) {
							// TODO 日志输出
						}
					}
				}
			}
		}
		in.resetReaderIndex();
		if (sum > 0) {
			in.readBytes(new byte[sum]);
		}
		return 0;
	}
	
	/**
	 * 匹配结束位置
	 * @author shanguoming 2015年1月16日 上午10:15:41
	 * @param in
	 * @modify: {原因} by shanguoming 2015年1月16日 上午10:15:41
	 */
	private boolean end(byte[] end) {
		if ("end;".equals(new String(end, CharsetUtil.UTF_8))) {
			return true;
		}
		return false;
	}
	
	/**
	 * 协议转成map
	 * @author shanguoming 2015年1月16日 上午11:30:47
	 * @param in
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月16日 上午11:30:47
	 */
	private LinkedHashMap<String, byte[]> decode(byte[] body) {
		LinkedHashMap<String, byte[]> map = new LinkedHashMap<String, byte[]>();
		String key = "";
		int length = body.length;
		int i = 0;
		int pre = 0;
		for (i = 0; i < length; i++) {
			byte b = body[i];
			if (b == ':') {
				key = new String(Arrays.copyOfRange(body, pre, i), CharsetUtil.UTF_8);
				pre = i + 1;
				continue;
			}
			if (null != key && key.equals("bf")) {
				break;
			}
			if (b == ';') {
				if (key.trim().length() != 0) {
					map.put(key, Arrays.copyOfRange(body, pre, i));
					pre = i + 1;
					key = "";
				}
			}
		}
		if (pre < length - 1) {
			byte[] bf = Arrays.copyOfRange(body, pre, length - 1);
			map.put(key, bf);
		}
		return map;
	}
	
	// TODO 默认协议格式验证
	private boolean validateProtocolDefault(LinkedHashMap<String, byte[]> map) {
		return true;
	}
	
	// TODO 协议扩展格式验证
	private boolean validateProtocolExt(NuoNaProtocol protocol) {
		return true;
	}
}
