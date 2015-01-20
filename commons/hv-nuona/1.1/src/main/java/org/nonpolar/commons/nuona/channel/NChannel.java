package org.nonpolar.commons.nuona.channel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author shanguoming 2014年12月26日 下午1:54:16
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年12月26日 下午1:54:16
 */
public class NChannel {
	
	/**
	 * 通道ID
	 */
	private String id;
	/**
	 * 通道类型
	 */
	private String type;
	/**
	 * 通道IP
	 */
	private String ip;
	/**
	 * 通道端口
	 */
	private int port;
	/**
	 * 通道状态
	 */
	private int status;
	/**
	 * 注册服务信息
	 */
	private Set<RegistryService> services = new HashSet<RegistryService>();
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public Set<RegistryService> getServices() {
		return services;
	}
	
	public void addService(RegistryService registryService) {
		if (null != registryService && null != registryService.getNamespace() && !"".equals(registryService.getNamespace().trim())) {
			registryService.setServiceNode(this);
			this.services.add(registryService);
			List<RegistryService> registryServices = NChannelFactory.registryServices.get(registryService.getNamespace().trim());
			if (null == registryServices) {
				registryServices = new CopyOnWriteArrayList<RegistryService>();
				NChannelFactory.registryServices.put(registryService.getNamespace().trim(), registryServices);
			}
			registryServices.add(registryService);
		}
	}
}
