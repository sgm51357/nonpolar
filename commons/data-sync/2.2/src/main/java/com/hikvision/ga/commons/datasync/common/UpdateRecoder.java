package com.hikvision.ga.commons.datasync.common;

import java.util.ArrayList;
import java.util.List;
import com.hikvision.ga.commons.datasync.utils.DataSyncTools;

/**
 * 
 * @author fangzhibin 2015年1月7日 下午2:21:21
 * @version V1.0   
 * @modify: {原因} by fangzhibin 2015年1月7日 下午2:21:21
 */
public class UpdateRecoder {
	
	/**
	 * 资源类型
	 */
	private String typeCode;
	/**
	 * 同步目录
	 */
	private String syncDir;
	/**
	 * 全量文件名
	 */
	private String fullFileName;
	/**
	 * 全量最后更新时间
	 */
	private String fullLastUpdateTime;
	/**
	 * 增量文件名
	 */
	private String increFileName;
	/**
	 * 增量最后更新记录行
	 */
	private String increLastUpdateRowId;
	
	public String getTypeCode() {
		return typeCode;
	}
	
	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}
	
	public String getSyncDir() {
		return syncDir;
	}
	
	public void setSyncDir(String syncDir) {
		this.syncDir = syncDir;
	}
	
	/**
	 * 获取头信息
	 * @author shanguoming 2014年7月8日 下午3:54:17
	 * @param col1
	 * @param col2
	 * @return
	 */
	public static String[] getHeaders() {
		List<String> heads = new ArrayList<String>();
		heads.add("typeCode");
		heads.add("syncDir");
		heads.add("fullFileName");
		heads.add("fullLastUpdateTime");
		heads.add("increFileName");
		heads.add("increLastUpdateRowId");
		return heads.toArray(new String[heads.size()]);
	}

	 public boolean isValidate(){
		 if((DataSyncTools.isNotBlank(fullLastUpdateTime) && DataSyncTools.isNotBlank(fullFileName)) || (DataSyncTools.isNotBlank(increLastUpdateRowId) && DataSyncTools.isNotBlank(increFileName))){
			 return true;
		 }
		 return false;
	 }
	public String[] getRecoders() {
		List<String> recoders = new ArrayList<String>();
		recoders.add(DataSyncTools.defaultIfBlank(typeCode, ""));
		recoders.add(DataSyncTools.defaultIfBlank(syncDir, ""));
		recoders.add(DataSyncTools.defaultIfBlank(fullFileName, ""));
		recoders.add(DataSyncTools.defaultIfBlank(fullLastUpdateTime, ""));
		recoders.add(DataSyncTools.defaultIfBlank(increFileName, ""));
		recoders.add(DataSyncTools.defaultIfBlank(increLastUpdateRowId, ""));
		return recoders.toArray(new String[recoders.size()]);
	}
	
	public String getFullFileName() {
		return fullFileName;
	}
	
	public void setFullFileName(String fullFileName) {
		this.fullFileName = fullFileName;
	}
	
	public String getFullLastUpdateTime() {
		return fullLastUpdateTime;
	}
	
	public void setFullLastUpdateTime(String fullLastUpdateTime) {
		this.fullLastUpdateTime = fullLastUpdateTime;
	}
	
	public String getIncreFileName() {
		return increFileName;
	}
	
	public void setIncreFileName(String increFileName) {
		this.increFileName = increFileName;
	}
	
	public String getIncreLastUpdateRowId() {
		return increLastUpdateRowId;
	}
	
	public void setIncreLastUpdateRowId(String increLastUpdateRowId) {
		this.increLastUpdateRowId = increLastUpdateRowId;
	}
}
