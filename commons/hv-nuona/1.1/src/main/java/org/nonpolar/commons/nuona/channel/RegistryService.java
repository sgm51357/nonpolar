package org.nonpolar.commons.nuona.channel;

import java.util.HashSet;
import java.util.Set;

/**
 * @author shanguoming 2014年12月25日 上午11:52:31
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年12月25日 上午11:52:31
 */
public class RegistryService {
	
	/**
	 * 服务命名空间，默认为(类名.方法名)
	 */
	private String namespace;
	private NChannel serviceNode;
	/**
	 * 注册服务状态
	 */
	private int status;
	
	public String getNamespace() {
		return namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public NChannel getServiceNode() {
		return serviceNode;
	}
	
	public void setServiceNode(NChannel serviceNode) {
		this.serviceNode = serviceNode;
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RegistryService) {
			RegistryService compare = (RegistryService)obj;
			if (null != namespace && !"".equals(namespace.trim()) && null != compare.getNamespace() && namespace.equals(compare.getNamespace())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		if (null != namespace) {
			return namespace.hashCode();
		}
		return super.hashCode();
	}
	
	public static void main(String[] args) {
		Set<RegistryService> registryServices = new HashSet<RegistryService>();
		for (int i = 0; i < 10; i++) {
			RegistryService rs = new RegistryService();
			rs.setNamespace("demo");
			rs.setStatus(i);
			registryServices.add(rs);
		}
		System.out.println(registryServices.size());
	}
}
