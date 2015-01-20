package com.hikvision.ga.commons.datasync.out;

import java.util.List;
import java.util.Map;
import com.hikvision.ga.commons.datasync.DataSyncEvent;
import com.hikvision.ga.commons.datasync.common.DataSyncException;
import com.hikvision.ga.commons.datasync.common.ResultType;
import com.hikvision.ga.commons.datasync.convert.DataConvert.Notifys.Notify;


/**
 * @author shanguoming 2015年1月8日 下午3:05:15
 * @version V1.0   
 * @modify: {原因} by shanguoming 2015年1月8日 下午3:05:15
 */
public class DataSyncOutSysServiceImpl implements IDataSyncOutSysService {
	
	/**
	 * @author shanguoming 2015年1月8日 下午3:05:15
	 * @param clazz
	 * @param typeCode
	 * @param params
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月8日 下午3:05:15
	 */
	@Override
	public List<String> getAllUnique(Class<?> clazz, String typeCode, Map<String, String> params) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @author shanguoming 2015年1月8日 下午3:05:15
	 * @param indexCodes
	 * @param params
	 * @param clazz
	 * @param typeCode
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月8日 下午3:05:15
	 */
	@Override
	public List<?> findByIndexCodes(List<String> indexCodes, Map<String, String> params, Class<?> clazz, String typeCode) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @author shanguoming 2015年1月8日 下午3:05:15
	 * @param map
	 * @param clazz
	 * @param typeCode
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月8日 下午3:05:15
	 */
	@Override
	public boolean customValue(Map<String, Object> map, Class<?> clazz, String typeCode) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * @author shanguoming 2015年1月8日 下午3:05:15
	 * @param event
	 * @param typeCode
	 * @param clazz
	 * @param args
	 * @modify: {原因} by shanguoming 2015年1月8日 下午3:05:15
	 */
	@Override
	public void endSyncByTypeCode(DataSyncEvent event, String typeCode, Class<?> clazz, Object... args) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * @author shanguoming 2015年1月8日 下午3:05:15
	 * @param event
	 * @param notifys
	 * @param args
	 * @modify: {原因} by shanguoming 2015年1月8日 下午3:05:15
	 */
	@Override
	public void endSync(DataSyncEvent event, List<Notify> notifys, Object... args) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * @author shanguoming 2015年1月8日 下午3:05:15
	 * @param event
	 * @param resultType
	 * @param e
	 * @param args
	 * @modify: {原因} by shanguoming 2015年1月8日 下午3:05:15
	 */
	@Override
	public void callback(DataSyncEvent event, ResultType resultType, DataSyncException e, Object... args) {
		// TODO Auto-generated method stub
	}
}
