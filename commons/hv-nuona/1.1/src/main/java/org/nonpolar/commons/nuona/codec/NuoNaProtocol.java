package org.nonpolar.commons.nuona.codec;

import io.netty.util.CharsetUtil;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author shanguoming 2015年1月16日 上午10:53:53
 * @version V1.0
 * @modify: {原因} by shanguoming 2015年1月16日 上午10:53:53
 */
public class NuoNaProtocol {
	
	/**
	 * 消息序号
	 */
	private int seq;
	/**
	 * 通信或者协议解析错误码
	 * <p>0:正确</p>
	 */
	private int err = 0;
	/**
	 * 协议类型，默认为0，如果ext有值，则不能为0
	 */
	private byte ver = 0;
	/**
	 * 请求或者响应状态
	 * <p>
	 * 1:请求</br>
	 * 2:响应
	 * </p>
	 */
	private byte flg;
	/**
	 * 压缩格式
	 * <p>
	 * 0:不压缩
	 * </p>
	 */
	private byte zip = 0;
	/**
	 * 消息发送者编号
	 */
	private String idx;
	/**
	 * 发送者身份类型
	 * <p>
	 * 1:服务</br>
	 * 2:客户端
	 * </p>
	 */
	private byte it;
	/**
	 * 令牌
	 */
	private String t;
	/**
	 * bf数据的加密类型
	 * <p>
	 * 0:不加密</br>
	 * 1:base64加密
	 * </p>
	 */
	private byte sec = 0;
	/**
	 * 服务
	 */
	private byte[] ser;
	/**
	 * bf数据类型
	 * <p>
	 * 0:PROTOSTUFF</br>
	 * 1:JSON</br>
	 * 2:XML
	 * </p>
	 */
	private byte bt = 0;
	/**
	 * 扩展属性，默认协议不能含扩展
	 */
	private LinkedHashMap<String, byte[]> ext = null;
	/**
	 * 数据载体
	 */
	private List<byte[]> bf = null;
	
	public byte getVer() {
		return ver;
	}
	
	public void setVer(byte ver) {
		this.ver = ver;
	}
	
	public int getErr() {
		return err;
	}
	
	public void setErr(int err) {
		this.err = err;
	}
	
	public int getSeq() {
		return seq;
	}
	
	public void setSeq(int seq) {
		this.seq = seq;
	}
	
	public byte getFlg() {
		return flg;
	}
	
	public void setFlg(byte flg) {
		this.flg = flg;
	}
	
	public byte getZip() {
		return zip;
	}
	
	public void setZip(byte zip) {
		this.zip = zip;
	}
	
	public String getIdx() {
		return idx;
	}
	
	public void setIdx(String idx) {
		this.idx = idx;
	}
	
	public byte getIt() {
		return it;
	}
	
	public void setIt(byte it) {
		this.it = it;
	}
	
	public String getT() {
		return t;
	}
	
	public void setT(String t) {
		this.t = t;
	}
	
	public byte getSec() {
		return sec;
	}
	
	public void setSec(byte sec) {
		this.sec = sec;
	}
	
	public byte[] getSer() {
		return ser;
	}
	
	public void setSer(byte[] ser) {
		this.ser = ser;
	}
	
	public byte getBt() {
		return bt;
	}
	
	public void setBt(byte bt) {
		this.bt = bt;
	}
	
	public LinkedHashMap<String, byte[]> getExt() {
		return ext;
	}
	
	public void setExt(LinkedHashMap<String, byte[]> ext) {
		this.ext = ext;
	}
	
	public List<byte[]> getBf() {
		return bf;
	}
	
	public void setBf(List<byte[]> bf) {
		this.bf = bf;
	}
	
	/**
	 * 默认协议
	 * <h1>协议格式</h1>
	 * <p>
	 * start;</br>
	 * len:长度(数字);//去掉start;end;的长度</br>
	 * ver:版本号(数字);</br>
	 * seq:序号(数字);</br>
	 * flg:请求标识(数字);</br>
	 * zip:压缩类型(数字);</br>
	 * idx:用户名或编码(字符串);</br>
	 * it:身份类型(数字);</br>
	 * t:令牌(字符串);</br>
	 * sec:加密类型(数字);</br>
	 * bt:数据类型(数字);</br>
	 * err:错误码(数字);</br>
	 * bs:长度(数字),长度(数字)...;//总和与bf的长度匹配</br>
	 * 扩展位(key或val不能包含关键字[:;]),格式:key:val;key:val;...
	 * ser:服务名;
	 * bf:数据流;</br>
	 * end;
	 * </p>
	 * @author shanguoming 2015年1月15日 下午1:30:22
	 * @modify: {原因} by shanguoming 2015年1月15日 下午1:30:22
	 */
	// public byte[] getBytes() {
	// StringBuilder head = new StringBuilder();
	// head.append("start;");
	// head.append("len:").append(";");
	// head.append("ver:").append(getVer()).append(";");
	// head.append("seq:").append(getSeq()).append(";");
	// head.append("flg:").append(getFlg()).append(";");
	// head.append("zip:").append(getZip()).append(";");
	// head.append("idx:");
	// if (null != getIdx()) {
	// head.append(getIdx());
	// }
	// head.append(";");
	// head.append("it:").append(getIt()).append(";");
	// head.append("t:");
	// if (null != getT()) {
	// head.append(getT());
	// }
	// head.append(";");
	// head.append("sec:").append(getSec()).append(";");
	// head.append("ser:");
	// if (null != getSer()) {
	// head.append(getSer());
	// }
	// head.append(";");
	// head.append("bt:").append(getBt()).append(";");
	// head.append("err:").append(getErr()).append(";");
	// head.append("bs:");
	// if (null != getBs()) {
	// String bs = Arrays.toString(getBs());
	// head.append(bs.substring(1, bs.length() - 1));
	// }
	// head.append(";");
	// if (null != getExt() && !getExt().isEmpty()) {
	// for (Iterator<String> it = getExt().keySet().iterator(); it.hasNext();) {
	// String key = it.next();
	// byte[] val = getExt().get(key);
	// head.append(key).append(":");
	// if (null != val) {
	// head.append(val);
	// }
	// head.append(";");
	// }
	// }
	// head.append("bf:");
	// String end = ";end;";
	// int bf_len = 0;
	// if (null != getBf()) {
	// bf_len = getBf().length;
	// }
	// int len = (head.length() + bf_len - 6);
	// String l = len + "";
	// len += l.length() + 1;
	// head.insert(10, len);
	// byte[] buf = new byte[len + 5 + end.length()];
	// byte[] heads = head.toString().getBytes(CharsetUtil.UTF_8);
	// System.arraycopy(heads, 0, buf, 0, heads.length);
	// if (bf_len > 0) {
	// System.arraycopy(getBf(), 0, buf, heads.length, getBf().length);
	// }
	// byte[] ends = end.getBytes(CharsetUtil.UTF_8);
	// System.arraycopy(ends, 0, buf, (heads.length + bf_len), ends.length);
	// return buf;
	// }
	/**
	 * 默认协议
	 * <h1>协议格式</h1>
	 * <p>
	 * start;</br>
	 * len:长度(数字);</br>
	 * seq:序号(数字);</br>
	 * err:错误码(数字);</br>
	 * ver:版本号(数字);</br>
	 * flg:请求标识(数字);</br>
	 * zip:压缩类型(数字);</br>
	 * it:身份类型(数字);</br>
	 * sec:加密类型(数字);</br>
	 * bt:数据类型(数字);</br>
	 * idx:用户名或编码(字符串);</br>
	 * t:令牌(字符串);</br>
	 * 扩展位(key或val不能包含关键字[:;]),格式:key:val;key:val;...
	 * ser:服务名;
	 * bs:长度(数字),长度(数字)...;</br>
	 * bf:数据流;</br>
	 * end;
	 * </p>
	 * @author shanguoming 2015年1月15日 下午1:30:22
	 * @modify: {原因} by shanguoming 2015年1月15日 下午1:30:22
	 */
	public byte[] toBytes() {
		ByteArrayOutputStream baos = null;
		DataOutputStream dos = null;
		try {
			baos = new ByteArrayOutputStream();
			dos = new DataOutputStream(baos);
			byte[] fh = ";".getBytes(CharsetUtil.UTF_8);
			dos.writeInt(getSeq());
			dos.writeInt(getErr());
			dos.writeByte(getVer());
			dos.writeByte(getFlg());
			dos.writeByte(getZip());
			dos.writeByte(getIt());
			dos.writeByte(getSec());
			dos.writeByte(getBt());
			dos.write(fh);
			if (null != getIdx()) {
				dos.write(getIdx().getBytes(CharsetUtil.UTF_8));
			}
			dos.write(fh);
			if (null != getT()) {
				dos.write(getT().getBytes(CharsetUtil.UTF_8));
			}
			dos.write(fh);
			if (null != getExt() && !getExt().isEmpty()) {
				for (Iterator<String> it = getExt().keySet().iterator(); it.hasNext();) {
					String key = it.next();
					if (null != key && !key.trim().equals("")) {
						byte[] val = getExt().get(key);
						dos.write(key.getBytes(CharsetUtil.UTF_8));
						dos.write(":".getBytes(CharsetUtil.UTF_8));
						if (null != val) {
							dos.write(val);
						}
						if (it.hasNext()) {
							dos.write(",".getBytes(CharsetUtil.UTF_8));
						}
					}
				}
			}
			dos.write(fh);
			if (null != getSer()) {
				dos.write(getSer());
			}
			dos.write(fh);
			if (null != getBf()) {
				int size = getBf().size();
				StringBuilder bs = new StringBuilder();
				for (int i = 0; i < size; i++) {
					byte[] b = getBf().get(i);
					if (null != b) {
						bs.append(b.length);
					} else {
						bs.append("0");
					}
					if (i < size - 1) {
						bs.append(",");
					}
				}
				dos.write(fh);
				for (int i = 0; i < size; i++) {
					byte[] b = getBf().get(i);
					if (null != b) {
						dos.write(b);
					}
				}
				dos.write(fh);
			} else {
				dos.write(fh);
				dos.write(fh);
			}
			byte[] bytes = baos.toByteArray();
			baos.reset();
			dos.write("start;".getBytes(CharsetUtil.UTF_8));
			dos.writeInt(bytes.length);
			dos.write(bytes);
			dos.write(";end;".getBytes(CharsetUtil.UTF_8));
			return baos.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (null != dos) {
				try {
					dos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (null != baos) {
				try {
					baos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		NuoNaProtocol p = new NuoNaProtocol();
		p.setBt((byte)2);
		p.setErr(11);
		p.setFlg((byte)2);
		p.setIt((byte)1);
		p.setSec((byte)2);
		byte[] bs = p.toBytes();
		for (int i = 0; i < bs.length; i++) {
			byte b = bs[i];
			if (b == ';') {
				System.out.println(i);
			}
		}
		System.out.println(new String(bs));
	}
}
