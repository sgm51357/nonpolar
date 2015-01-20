package com.hikvision.ga.commons.datasync.common;

/**
 * 
 * @author fangzhibin 2015年1月7日 下午2:20:51
 * @version V1.0   
 * @modify: {原因} by fangzhibin 2015年1月7日 下午2:20:51
 */
public class IncreBean {
	
	private Object bean;
	private OperateType operateType;
	
	public Object getBean() {
		return bean;
	}
	
	public void setBean(Object bean) {
		this.bean = bean;
	}
	
	public OperateType getOperateType() {
		return operateType;
	}
	
	public void setOperateType(OperateType operateType) {
		this.operateType = operateType;
	}
}
