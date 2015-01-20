package com.hikvision.ga.commons.datasync.in;

import java.util.List;
import java.util.Map;
import com.hikvision.ga.commons.datasync.DataSyncEvent;
import com.hikvision.ga.commons.datasync.common.DataSyncException;
import com.hikvision.ga.commons.datasync.common.ResultType;
import com.hikvision.ga.commons.datasync.convert.DataConvert.Notifys.Notify;
import com.hikvision.ga.commons.datasync.utils.DataSyncTools;


/**
 * @author shanguoming 2015年1月8日 下午3:04:50
 * @version V1.0   
 * @modify: {原因} by shanguoming 2015年1月8日 下午3:04:50
 */
public class DataSyncInSysServiceImpl implements IDataSyncInSysService {
	
	/**
	 * @author shanguoming 2015年1月8日 下午3:04:50
	 * @param clazz
	 * @param typeCode
	 * @param params
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月8日 下午3:04:50
	 */
	@Override
	public List<String> getAllUnique(Class<?> clazz, String typeCode, Map<String, String> params) {
		if(DataSyncTools.equals(typeCode, "")){
			
		}
		return null;
	}
	
	/**
	 * @author shanguoming 2015年1月8日 下午3:04:50
	 * @param indexCodes
	 * @param params
	 * @param clazz
	 * @param typeCode
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月8日 下午3:04:50
	 */
	@Override
	public List<?> findByIndexCodes(List<String> indexCodes, Map<String, String> params, Class<?> clazz, String typeCode) {
		System.out.println("findByIndexCodes");
		return null;
	}
	
	/**
	 * @author shanguoming 2015年1月8日 下午3:04:50
	 * @param map
	 * @param clazz
	 * @param typeCode
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月8日 下午3:04:50
	 */
	@Override
	public boolean customValue(Map<String, Object> map, Class<?> clazz, String typeCode) {
		System.out.println("customValue");
		return false;
	}
	
	/**
	 * @author shanguoming 2015年1月8日 下午3:04:50
	 * @param event
	 * @param typeCode
	 * @param clazz
	 * @param args
	 * @modify: {原因} by shanguoming 2015年1月8日 下午3:04:50
	 */
	@Override
	public void endSyncByTypeCode(DataSyncEvent event, String typeCode, Class<?> clazz, Object... args) {
		System.out.println("endSyncByTypeCode");
	}
	
	/**
	 * @author shanguoming 2015年1月8日 下午3:04:50
	 * @param event
	 * @param notifys
	 * @param args
	 * @modify: {原因} by shanguoming 2015年1月8日 下午3:04:50
	 */
	@Override
	public void endSync(DataSyncEvent event, List<Notify> notifys, Object... args) {
		System.out.println("endSync");
	}
	
	/**
	 * @author shanguoming 2015年1月8日 下午3:04:50
	 * @param event
	 * @param resultType
	 * @param e
	 * @param args
	 * @modify: {原因} by shanguoming 2015年1月8日 下午3:04:50
	 */
	@Override
	public void callback(DataSyncEvent event, ResultType resultType, DataSyncException e, Object... args) {
		System.out.println("callback");
	}
	
	/**
	 * @author shanguoming 2015年1月8日 下午3:04:50
	 * @param indexCodes
	 * @param clazz
	 * @param typeCode
	 * @param params
	 * @modify: {原因} by shanguoming 2015年1月8日 下午3:04:50
	 */
	@Override
	public void deleteByIndexCodesForSync(List<String> indexCodes, Class<?> clazz, String typeCode, Map<String, String> params) {
		System.out.println("deleteByIndexCodesForSync");
	}
	
	/**
	 * @author shanguoming 2015年1月8日 下午3:04:50
	 * @param saveList
	 * @param clazz
	 * @param typeCode
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月8日 下午3:04:50
	 */
	@Override
	public int batchSave(List<Object> saveList, Class<?> clazz, String typeCode) {
		System.out.println("batchSave");
		return 0;
	}
	
	/**
	 * @author shanguoming 2015年1月8日 下午3:04:50
	 * @param updateList
	 * @param clazz
	 * @param typeCode
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月8日 下午3:04:50
	 */
	@Override
	public int batchUpdate(List<Object> updateList, Class<?> clazz, String typeCode) {
		System.out.println("batchUpdate");
		return 0;
	}
}
