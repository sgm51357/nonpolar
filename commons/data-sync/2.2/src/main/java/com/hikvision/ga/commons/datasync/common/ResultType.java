package com.hikvision.ga.commons.datasync.common;

/**
 * 
 * @author fangzhibin 2015年1月7日 下午2:21:03
 * @version V1.0   
 * @modify: {原因} by fangzhibin 2015年1月7日 下午2:21:03
 */
public enum ResultType {
	/**
	 * 成功
	 */
	SUCCESS("成功"),
	/**
	 * 未定义CODE
	 */
	NOT_DEFINITION_VALUE("未定义CODE"),
	/**
	 * path为空
	 */
	EMPTY_PATH("path为空"),
	/**
	 * 无效path
	 */
	INVALID_PATH("无效path"),
	/**
	 * templateFile为空
	 */
	EMPTY_TEMPLATE_FILE("templateFile为空"),
	/**
	 * 接口为null
	 */
	NULL_INTERFACE("接口为null"),
	/**
	 * 读取模板文件异常
	 */
	READ_TEMPLATE_EXCEPTION("读取模板文件异常"),
	/**
	 * 解析模板文件异常
	 */
	PARSE_TEMPLATE_EXCEPTION("解析模板文件异常"),
	/**
	 *  读远程更新记录文件时IO异常
	 */
	REMOTE_UPDATERECODER_IO_ERROR("读远程更新记录文件时IO异常"),
	/**
	 * 远程更新记录文件无更新记录
	 */
	REMOTE_UPDATERECODER_NO_UPDATE("远程更新记录文件无更新记录"),
	/**
	 * 远程更新记录文件格式错误
	 */
	REMOTE_UPDATERECODER_FORMAT_ERROR("远程更新记录文件格式错误"),
	/**
	 * 无法找到本地资源文件
	 */
	LOCAL_RESOURCE_FILE_NOT_FIND("无法找到本地资源文件"),
	/**
	 * 读取资源文件异常
	 */
	READ_RESOURCE_FILE_EXCEPTION("读取资源文件异常"),
	/**
	 * 资源生成正在运行
	 */
	RES_BUILD_RUNING("资源生成正在运行"),
	/**
	 * 资源同步正在运行
	 */
	RES_SYNC_RUNING("资源同步正在运行"),
	/**
	 * 模板文件结构错误
	 */
	TEMPLATE_FILE_STRUCTURE_ERROR("模板文件结构错误"),
	/**
	 * 异常
	 */
	EXCEPTION("异常"),
	/**
	 * 模板文件结构缺少data
	 */
	TEMPLATE_FILE_STRUCTURE_MISS_DATA("模板文件结构缺少data");
	
	private String msg;
	
	private ResultType(String msg){
		this.msg = msg;
	}

	
    public String getMsg() {
    	return msg;
    }
	
	
}
