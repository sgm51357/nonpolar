package com.hikvision.ga.commons.datasync.in;

import java.util.List;
import java.util.Map;
import com.hikvision.ga.commons.datasync.IDataSyncSysService;

/**
 * @author shanguoming 2014年12月31日 下午3:35:07
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年12月31日 下午3:35:07
 */
public interface IDataSyncInSysService extends IDataSyncSysService{

	/**
	 * 清除增量记录del操作的数据
	 * @author shanguoming 2014年7月7日 下午4:18:26
	 * @param indexCodes 被del操作的indexCode集合
	 * @param clazz 同步对象
	 * @param typeCode 数据编码
	 * @param params 预设参数
	 */
	public void deleteByIndexCodesForSync(List<String> indexCodes, Class<?> clazz, String typeCode, Map<String, String> params);
	
	/**
	 * 实现批量保存的接口
	 * @author shanguoming 2014年7月7日 上午9:50:33
	 * @param saveList
	 * @param clazz
	 * @param typeCode
	 * @return
	 */
	int batchSave(List<Object> saveList, Class<?> clazz, String typeCode);
	
	/**
	 * 实现批量更新接口
	 * @author shanguoming 2014年7月7日 上午9:57:36
	 * @param updateList
	 * @param clazz
	 * @param typeCode
	 * @return
	 */
	int batchUpdate(List<Object> updateList, Class<?> clazz, String typeCode);
}
