package com.hikvision.ga.commons.datasync.out;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hikvision.ga.commons.datasync.DataSyncEvent;
import com.hikvision.ga.commons.datasync.common.DataSyncException;
import com.hikvision.ga.commons.datasync.common.IncreBean;
import com.hikvision.ga.commons.datasync.common.ResultType;
import com.hikvision.ga.commons.datasync.convert.DataConvert.Notifys.Notify;
import com.hikvision.ga.commons.datasync.convert.DataDTO;
import com.hikvision.ga.commons.datasync.utils.DataSyncTools;

/**
 * 数据输出同步事件
 * @author shanguoming 2014年12月31日 下午3:36:42
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年12月31日 下午3:36:42
 */
public class DataSyncOutEvent extends DataSyncEvent {
	
	private final Logger log = LoggerFactory.getLogger("data-sync:DataSyncOutEvent");
	/**
	 * 是否全量
	 */
	private boolean full = false;
	private Map<String, List<IncreBean>> increBeans;
	private IDataSyncOutSysService dataSyncOutSysService;
	
	/**
	 * 数据同步和生成的构造函数
	 * 创建一个新的实例DataSyncEvent.
	 * @param path 路径以file开头
	 * @param code 编号
	 * @param templateFile 模板文件
	 */
	public DataSyncOutEvent(String path, String code, String templateFile, IDataSyncOutSysService dataSyncOutSysService) throws DataSyncException {
		super(path, code, templateFile);
		this.dataSyncOutSysService = dataSyncOutSysService;
		validate();
	}
	
	protected void validate() throws DataSyncException {
		if (DataSyncTools.isBlank(super.getPath())) {
			throw new DataSyncException("path不能为空", ResultType.EMPTY_PATH);
		}
		if (DataSyncTools.isBlank(super.getCode())) {
			throw new DataSyncException("code不能为空", ResultType.NOT_DEFINITION_VALUE);
		}
		if (DataSyncTools.isBlank(super.getTemplateFile())) {
			throw new DataSyncException("template不能为空", ResultType.EMPTY_TEMPLATE_FILE);
		}
		if (this.dataSyncOutSysService == null) {
			throw new DataSyncException("dataSyncOutSysService不能为null", ResultType.NULL_INTERFACE);
		}
		initDataConvert();
	}
	
	public IDataSyncOutSysService getDataSyncOutSysService() {
		return dataSyncOutSysService;
	}
	
	public void setDataSyncOutSysService(IDataSyncOutSysService dataSyncOutSysService) {
		this.dataSyncOutSysService = dataSyncOutSysService;
	}
	
	@Override
	protected List<String> getAllUnique(DataDTO data) throws DataSyncException {
		if (dataSyncOutSysService != null) {
			try {
				return dataSyncOutSysService.getAllUnique(data.getClazz(), data.getTypeCode(), data.getParams());
			} catch (Exception e) {
				log.error("调用数据同步入库的结果回调方法时异常", e);
				throw new DataSyncException(e);
			}
		}
		return null;
	}
	
	@Override
	protected List<?> findByIndexCodes(List<String> indexCodes, Map<String, String> params, Class<?> clazz, String typeCode) throws DataSyncException {
		if (dataSyncOutSysService != null) {
			try {
				return dataSyncOutSysService.findByIndexCodes(indexCodes, params, clazz, typeCode);
			} catch (Exception e) {
				log.error("{}: 完成资源同步的方法异常", super.getCode(), e);
				throw new DataSyncException(e);
			}
		}
		return null;
	}
	
	@Override
	protected boolean customValue(Map<String, Object> map, Class<?> clazz, String typeCode) throws DataSyncException {
		if (dataSyncOutSysService != null) {
			try {
				return dataSyncOutSysService.customValue(map, clazz, typeCode);
			} catch (Exception e) {
				log.error("{}: 完成资源同步的方法异常", super.getCode(), e);
				throw new DataSyncException(e);
			}
		}
		return true;
	}
	
	@Override
	protected void endSyncByTypeCode(String typeCode, Class<?> clazz, Object... args) {
		if (dataSyncOutSysService != null) {
			try {
				dataSyncOutSysService.endSyncByTypeCode(this, typeCode, clazz);
			} catch (Exception e) {
				log.error("调用数据同步入库的结果回调方法时异常", e);
			}
		}
	}
	
	@Override
	protected void endSync(List<Notify> notifys, Object... args) {
		if (dataSyncOutSysService != null) {
			try {
				dataSyncOutSysService.endSync(this, notifys, args);
			} catch (Exception e) {
				log.error("{}: 完成资源同步的方法异常", super.getCode(), e);
			}
		}
	}
	
	@Override
	public void callback(ResultType resultType, DataSyncException e, Object... args) {
		super.setEndTime(System.currentTimeMillis());
		if (dataSyncOutSysService != null) {
			try {
				dataSyncOutSysService.callback(this, resultType, e, args);
			} catch (Exception ex) {
				log.error("调用数据同步入库的结果回调方法时异常", ex);
			}
		}
	}
	
	public boolean isFull() {
		return full;
	}
	
	public void setFull(boolean full) {
		this.full = full;
	}
	
	public Map<String, List<IncreBean>> getIncreBeans() {
		return increBeans;
	}
	
	public void setIncreBeans(Map<String, List<IncreBean>> increBeans) {
		this.increBeans = increBeans;
	}
	
	public void mergeIncreBeans(Map<String, List<IncreBean>> increBeans) {
		if (null == this.increBeans) {
			this.increBeans = new HashMap<String, List<IncreBean>>();
		}
		if (!DataSyncTools.isEmpty(increBeans)) {
			for (Entry<String, List<IncreBean>> entry : increBeans.entrySet()) {
				List<IncreBean> list = this.increBeans.get(entry.getKey());
				if (null == list) {
					list = new ArrayList<IncreBean>();
					this.increBeans.put(entry.getKey(), list);
				}
				list.addAll(entry.getValue());
			}
		}
	}
}
