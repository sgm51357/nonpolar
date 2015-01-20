package com.hikvision.ga.commons.datasync;

import java.util.List;
import java.util.Map;
import com.hikvision.ga.commons.datasync.common.DataSyncException;
import com.hikvision.ga.commons.datasync.common.ResultType;
import com.hikvision.ga.commons.datasync.convert.DataConvert.Notifys.Notify;

/**
 * @author shanguoming 2015年1月6日 下午3:38:27
 * @version V1.0
 * @modify: {原因} by shanguoming 2015年1月6日 下午3:38:27
 */
public interface IDataSyncSysService {
	
	/**
	 * 查询全部唯一键集合
	 * @author shanguoming 2015年1月6日 下午2:20:14
	 * @param clazz
	 * @param typeCode
	 * @param params
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月6日 下午2:20:14
	 */
	List<String> getAllUnique(Class<?> clazz, String typeCode, Map<String, String> params);
	
	/**
	 * 根据indexCode集查询对应的资源map集合
	 * @author shanguoming 2014年7月5日 下午2:09:29
	 * @param indexCodes 资源indexCode集
	 * @param params 预设参数
	 * @param clazz 资源类型
	 * @param typeCode 配置文件中的资源code
	 * @return
	 */
	List<?> findByIndexCodes(List<String> indexCodes, Map<String, String> params, Class<?> clazz, String typeCode);
	
	/**
	 * 用户自定义的值转换，如果返回false，则表示直接忽略当前map的同步值
	 * @author shanguoming 2015年1月5日 下午4:06:20
	 * @param map 经过数据同步sdk转换的map值
	 * @param clazz 数据对应的对象
	 * @param typeCode 数据的类型
	 * @return true：该map对象有效，false：该map对象无效
	 * @modify: {原因} by shanguoming 2015年1月5日 下午4:06:20
	 */
	boolean customValue(Map<String, Object> map, Class<?> clazz, String typeCode);
	
	/**
	 * 一种资源类型正常同步完成后回调的事件
	 * @author shanguoming 2015年1月6日 下午2:21:52
	 * @param event
	 * @param typeCode
	 * @param clazz
	 * @modify: {原因} by shanguoming 2015年1月6日 下午2:21:52
	 */
	void endSyncByTypeCode(DataSyncEvent event, String typeCode, Class<?> clazz, Object... args);
	
	/**
	 * 全部资源类型正常同步完成后回调的事件
	 * @author shanguoming 2015年1月6日 下午2:26:29
	 * @param event
	 * @param notifys
	 * @param args
	 * @modify: {原因} by shanguoming 2015年1月6日 下午2:26:29
	 */
	void endSync(DataSyncEvent event, List<Notify> notifys, Object... args);
	
	/**
	 * 同步执行结束的回调对象，可能有异常返回
	 * @author shanguoming 2015年1月6日 下午2:27:12
	 * @param event
	 * @param resultType
	 * @param e
	 * @param args
	 * @modify: {原因} by shanguoming 2015年1月6日 下午2:27:12
	 */
	void callback(DataSyncEvent event, ResultType resultType, DataSyncException e, Object... args);
}
