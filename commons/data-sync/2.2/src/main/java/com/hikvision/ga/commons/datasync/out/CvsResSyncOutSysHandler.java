package com.hikvision.ga.commons.datasync.out;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.csvreader.CsvWriter;
import com.hikvision.ga.commons.datasync.ResSyncBase;
import com.hikvision.ga.commons.datasync.common.DataSyncException;
import com.hikvision.ga.commons.datasync.common.IncreBean;
import com.hikvision.ga.commons.datasync.common.OperateType;
import com.hikvision.ga.commons.datasync.common.ResultType;
import com.hikvision.ga.commons.datasync.common.UpdateRecoder;
import com.hikvision.ga.commons.datasync.convert.DataConvert;
import com.hikvision.ga.commons.datasync.convert.DataDTO;
import com.hikvision.ga.commons.datasync.in.IValueConvert;
import com.hikvision.ga.commons.datasync.utils.DataSyncTools;

/**
 * csv数据输出同步处理类
 * @author shanguoming 2015年1月6日 下午2:39:35
 * @version V1.0
 * @modify: {原因} by shanguoming 2015年1月6日 下午2:39:35
 */
public class CvsResSyncOutSysHandler extends ResSyncBase {
	
	private final Logger log = LoggerFactory.getLogger("data-sync:CvsResSyncOutSysHandler");
	private IValueConvert<Object> defaultValueConvert = new DefaultOutValueConvert();
	
	public void execute(DataSyncOutEvent event) throws DataSyncException {
		log.info("-------------------开始[{}]任务,是否全量[{}]-----------------------", event.getCode(), event.isFull());
		event.setStartTime(System.currentTimeMillis());
		try {
			if (event.isFull()) {
				log.info("{}:全量生成资源文件开始", event.getCode());
				Map<String, String> typeCodes = new HashMap<String, String>();
				String syncDirName = DataSyncTools.getCurrentDateStr(DataSyncTools.C_DATA_PATTON_YYYYMMDDHHMMSS);
				File syncRootDir = new File(event.getPath() + syncDirName + "/");
				if (!syncRootDir.exists()) {
					syncRootDir.mkdirs();
				}
				for (DataDTO data : event.getDatas().values()) {
					long fullAllUniqueStart = System.currentTimeMillis();
					List<String> uniqueList = event.getAllUnique(data);
					long fullStart = System.currentTimeMillis();
					CsvWriter cw = null;
					int limit = 0;
					long count = 0;
					int size = 0;
					try {
						if (!DataSyncTools.isEmpty(uniqueList)) {
							size = uniqueList.size();
							String fileName = event.getPath() + syncDirName + "/" + data.getTypeCode() + "_FULL.csv";
							log.info("{}:全量生成[{}]数据{}条,路径：", event.getCode(), data.getTypeCode(), size, fileName);
							cw = createFullSyncWriterStream(data, event, fileName);
							typeCodes.put(data.getTypeCode(), data.getTypeCode() + "_FULL");
							limit = size <= event.getLimit()?size:event.getLimit();
							int start = 0;
							List<String> uniques = uniqueList.subList(start, limit);
							while (!DataSyncTools.isEmpty(uniques)) {
								List<?> list = event.findByIndexCodes(uniques, data.getParams(), data.getClazz(), data.getTypeCode());
								fullToFile(cw, list, data, event);
								start = start + limit;
								if (start > size) {
									start = size;
								}
								int end = start + limit;
								if (end > size) {
									end = size;
								}
								uniques = uniqueList.subList(start, end);
								count++;
							}
						}
						event.endSyncByTypeCode(data.getTypeCode(), data.getClazz());
					} finally {
						if (cw != null) {
							cw.close();
						}
						long fullEnd = System.currentTimeMillis();
						log.info("{}:全量生成[{}], 共写入csv记录{}条,分{}批次每批{}条写入,查询唯一键集合耗时：{}ms写文件耗时：{}ms,总耗时：{}ms", event.getCode(), data.getTypeCode(), size, count, limit, (fullStart - fullAllUniqueStart),
						        (fullEnd - fullStart), (fullEnd - fullAllUniqueStart));
					}
				}
				Map<String, UpdateRecoder> localUpdateRecodes = null;
				try {
					localUpdateRecodes = getLocalUpdateRecoder(event.getPath(), event.getUpdateRecoderFileName(), event.getCharset());
				} catch (IOException e) {
					log.warn("{}：无法获得本地的更新记录信息[{}],判定为是第一次更新", event.getCode(), event.getPath());
				}
				List<String[]> updateRecoders = new ArrayList<String[]>();
				String[] updateRecoderHeaders = UpdateRecoder.getHeaders();
				updateRecoders.add(updateRecoderHeaders);
				if (localUpdateRecodes == null) {
					localUpdateRecodes = new LinkedHashMap<String, UpdateRecoder>();
				}
				for (Entry<String, String> entry : typeCodes.entrySet()) {
					UpdateRecoder localUpdateRecoder = localUpdateRecodes.get(entry.getKey());
					if (localUpdateRecoder == null) {
						localUpdateRecoder = new UpdateRecoder();
					}
					localUpdateRecoder.setSyncDir(syncDirName);
					localUpdateRecoder.setTypeCode(entry.getKey());
					localUpdateRecoder.setFullFileName(entry.getValue());
					localUpdateRecoder.setFullLastUpdateTime(DataSyncTools.getCurrentDateStr(DataSyncTools.C_TIME_PATTON_DETAIL));
					localUpdateRecoder.setIncreFileName("");
					localUpdateRecoder.setIncreLastUpdateRowId("");
					updateRecoders.add(localUpdateRecoder.getRecoders());
				}
				event.updateRecoder(updateRecoders, event.getUpdateRecoderFileName(), event.getPath());
			}
			if (null != event.getIncreBeans() && !event.getIncreBeans().isEmpty()) {
				log.info("{}:增量生成资源文件开始", event.getCode());
				long increStart = System.currentTimeMillis();
				for (Entry<String, List<IncreBean>> entry : event.getIncreBeans().entrySet()) {
					DataDTO data = event.getDatas().get(entry.getKey());
					if (null == data) {
						log.warn("{}:增加生成资源文件时,传入的数据类型[{}]不对,无法找到对应的结构体", event.getCode(), entry.getKey());
						continue;
					}
					String syncDirName = "";
					FileWriterWithEncoding fw = null;
					CsvWriter cw = null;
					Map<String, UpdateRecoder> localUpdateRecodes = null;
					try {
						localUpdateRecodes = getLocalUpdateRecoder(event.getPath(), event.getUpdateRecoderFileName(), event.getCharset());
					} catch (IOException e) {
						log.warn("{}：无法获得本地的更新记录信息[{}],判定为是第一次更新", event.getCode(), event.getPath());
					}
					if (localUpdateRecodes == null) {
						localUpdateRecodes = new LinkedHashMap<String, UpdateRecoder>();
					}
					UpdateRecoder localUpdateRecoder = null;
					int lastRowId = 0;
					if (!localUpdateRecodes.isEmpty()) {
						localUpdateRecoder = localUpdateRecodes.get(entry.getKey());
						if (localUpdateRecoder != null) {
							lastRowId = DataSyncTools.isBlank(localUpdateRecoder.getIncreLastUpdateRowId())?0:Integer.parseInt(localUpdateRecoder.getIncreLastUpdateRowId());
							syncDirName = localUpdateRecoder.getSyncDir();
						} else {
							for (UpdateRecoder ur : localUpdateRecodes.values()) {
								syncDirName = ur.getSyncDir();
								break;
							}
						}
					} else {
						syncDirName = DataSyncTools.getCurrentDateStr(DataSyncTools.C_DATA_PATTON_YYYYMMDDHHMMSS);
						File syncRootDir = new File(event.getPath() + syncDirName + "/");
						if (!syncRootDir.exists()) {
							syncRootDir.mkdirs();
						}
					}
					File syncRootDir = new File(event.getPath() + syncDirName + "/");
					if (!syncRootDir.exists()) {
						syncRootDir.mkdirs();
					}
					int startId = (lastRowId + 1);
					try {
						if (entry.getValue() != null && !entry.getValue().isEmpty()) {
							String fileName = event.getPath() + syncDirName + "/" + data.getTypeCode() + "_INCRE.csv";
							fw = new FileWriterWithEncoding(fileName, event.getCharset(), true);
							cw = createIncreSyncWriterStream(data, fileName, fw);
							lastRowId += increToFile(cw, entry.getValue(), data, event, lastRowId);
						}
					} finally {
						if (cw != null) {
							cw.close();
						}
						IOUtils.closeQuietly(fw);
						long increEnd = System.currentTimeMillis();
						log.info("{}:增量生成[{}]数据文件,从第{}行开始,更新{}条,更新到第{}行,耗时：{}ms", event.getCode(), entry.getKey(), startId, entry.getValue().size(), lastRowId, (increEnd - increStart));
					}
					List<String[]> updateRecoders = new ArrayList<String[]>();
					updateRecoders.add(UpdateRecoder.getHeaders());
					if (lastRowId > 0) {
						if (localUpdateRecoder == null) {
							localUpdateRecoder = new UpdateRecoder();
							localUpdateRecoder.setSyncDir(syncDirName);
							localUpdateRecoder.setTypeCode(entry.getKey());
							localUpdateRecoder.setIncreFileName(data.getTypeCode() + "_INCRE");
							localUpdateRecoder.setIncreLastUpdateRowId(lastRowId + "");
							localUpdateRecodes.put(data.getTypeCode(), localUpdateRecoder);
						} else {
							localUpdateRecoder.setIncreFileName(data.getTypeCode() + "_INCRE");
							localUpdateRecoder.setIncreLastUpdateRowId(lastRowId + "");
						}
					}
					if (DataSyncTools.isEmpty(localUpdateRecodes)) {
						for (UpdateRecoder updateRecoder : localUpdateRecodes.values()) {
							if (DataSyncTools.equals(updateRecoder.getTypeCode(), localUpdateRecoder.getTypeCode())) {
								updateRecoder = localUpdateRecoder;
							}
							updateRecoders.add(updateRecoder.getRecoders());
						}
					} else {
						updateRecoders.add(localUpdateRecoder.getRecoders());
					}
					event.updateRecoder(updateRecoders, event.getUpdateRecoderFileName(), event.getPath());
					event.endSyncByTypeCode(data.getTypeCode(), data.getClazz());
				}
			}
			event.endSync(event.getNotifys());
			event.callback(ResultType.SUCCESS, null);
		} catch (Exception e) {
			log.error("{}:资源生成出现异常", event.getCode(), e);
			throw new DataSyncException(e);
		}
	}
	
	/**
	 * 创建全量写文件的csv写对象
	 * @author shanguoming 2015年1月6日 下午4:32:15
	 * @param data
	 * @param event
	 * @param fileName
	 * @return
	 * @throws IOException
	 * @modify: {原因} by shanguoming 2015年1月6日 下午4:32:15
	 */
	private CsvWriter createFullSyncWriterStream(DataDTO data, DataSyncOutEvent event, String fileName) throws IOException {
		File local = new File(fileName);
		if (!local.exists()) {
			local.createNewFile();
		}
		CsvWriter cw = new CsvWriter(fileName, ',', event.getCharset());
		int size = data.getFields().size();
		String[] headers = new String[size];
		for (int i = 0; i < size; i++) {
			DataConvert.Data.Fields.Field field = data.getFields().get(i);
			// 默认已属性名作为列名，亦可自定义
			headers[i] = DataSyncTools.defaultIfBlank(field.getContent(), field.getTarget());
		}
		cw.writeRecord(headers);
		cw.flush();
		return cw;
	}
	
	/**
	 * 创建增量写文件的csv对象
	 * @author shanguoming 2015年1月6日 下午5:29:57
	 * @param data
	 * @param fileName
	 * @param fw
	 * @return
	 * @throws IOException
	 * @modify: {原因} by shanguoming 2015年1月6日 下午5:29:57
	 */
	private CsvWriter createIncreSyncWriterStream(DataDTO data, String fileName, FileWriterWithEncoding fw) throws IOException {
		File local = new File(fileName);
		// 判断本地文件是否存在，存在则表示无需再下载
		boolean newFlag = false;
		if (!local.exists()) {
			local.createNewFile();
			newFlag = true;
		}
		CsvWriter cw = new CsvWriter(fw, ',');
		if (newFlag) {
			int size = data.getFields().size();
			String[] headers = new String[size + 2];
			headers[0] = "rowId";
			headers[1] = "syncOperate";
			for (int i = 0; i < size; i++) {
				DataConvert.Data.Fields.Field field = data.getFields().get(i);
				headers[i + 2] = DataSyncTools.defaultIfBlank(field.getContent(), field.getTarget());
			}
			cw.writeRecord(headers);
			cw.flush();
		}
		return cw;
	}
	
	/**
	 * class转Object
	 * @author shanguoming 2015年1月5日 下午2:42:28
	 * @param className
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月5日 下午2:42:28
	 */
	@SuppressWarnings("unchecked")
	private IValueConvert<Object> classToObj(String className) {
		if (DataSyncTools.isNotBlank(className)) {
			try {
				Class<?> clazz = Class.forName(className);
				return (IValueConvert<Object>)clazz.newInstance();
			} catch (Exception e) {
				log.error("对象className={}实例化成对象时异常", className, e);
			}
		}
		return defaultValueConvert;
	}
	
	private String mapToStr(Map<String, Object> map) {
		if (null != map && !map.isEmpty()) {
			StringBuilder str = new StringBuilder("{");
			for (Entry<String, Object> entry : map.entrySet()) {
				str.append("[");
				str.append(entry.getKey());
				str.append(":");
				str.append(entry.getValue().toString());
				str.append("]");
			}
			str.append("}");
			return str.toString();
		}
		return "";
	}
	
	/**
	 * 全量写文件
	 * @author shanguoming 2015年1月6日 下午5:30:09
	 * @param cw
	 * @param list
	 * @param data
	 * @param event
	 * @throws Exception
	 * @modify: {原因} by shanguoming 2015年1月6日 下午5:30:09
	 */
	@SuppressWarnings("unchecked")
	private void fullToFile(CsvWriter cw, List<?> list, DataDTO data, DataSyncOutEvent event) throws Exception {
		if (cw != null && !DataSyncTools.isEmpty(list)) {
			for (Object obj : list) {
				Map<String, Object> map = null;
				if (DataSyncTools.equals(data.getClazz().getName(), Map.class.getName())) {
					map = (Map<String, Object>)obj;
				} else {
					map = DataSyncTools.convertToMap(obj);
				}
				if (!event.customValue(map, data.getClazz(), data.getTypeCode())) {
					log.warn("{}:typeCode=[{}]中的{}数据被自定义忽略同步", event.getCode(), data.getTypeCode(), mapToStr(map));
					continue;
				}
				int size = data.getFields().size();
				String[] csv = new String[size];
				boolean exclude = false;
				for (int i = 0; i < size; i++) {
					DataConvert.Data.Fields.Field field = data.getFields().get(i);
					if (DataSyncTools.isNotBlank(field.getTarget())) {
						IValueConvert<Object> valueConvert = classToObj(field.getConvert());
						Object v = map.get(field.getTarget());
						// FIXME 1.排除不同步的原始值
						if (exclude = valueConvert.valueExclude(data.getValueExcludes().get(field.getTarget()), v)) {
							break;
						}
						// FIXME 2.null字符串转换成null对象
						v = valueConvert.nullConvert(v);
						// FIXME 3.原始值映射转换
						v = valueConvert.mapping(field.getMapping(), v);
						// FIXME 4.假设字段为null的话，如果field上配置了default，则不管数据库中是否存在该数据，都会进行默认值赋值。不同于defaultValue
						if ((null == v || DataSyncTools.isBlank(v.toString()))) {
							if (DataSyncTools.isNotBlank(field.getDefault())) {
								v = field.getDefault();
							} else if (DataSyncTools.isNotBlank(data.getDefaultValues().get(field.getTarget()))) {
								v = data.getDefaultValues().get(field.getTarget());
							}
						}
						// FIXME 5.自定义转换
						v = valueConvert.convert(data.getTypeCode(), field, v);
						if (v != null) {
							csv[i] = v.toString();
						}
					}
				}
				if (exclude) {
					log.warn("{}:typeCode=[{}]中的{}数据被配置忽略同步", event.getCode(), data.getTypeCode(), mapToStr(map));
					continue;
				}
				cw.writeRecord(csv);
			}
			cw.flush();
		}
	}
	
	/**
	 * 增量写文件
	 * @author shanguoming 2015年1月6日 下午5:30:15
	 * @param cw
	 * @param list
	 * @param data
	 * @param event
	 * @param lastRowId
	 * @return
	 * @throws Exception
	 * @modify: {原因} by shanguoming 2015年1月6日 下午5:30:15
	 */
	@SuppressWarnings("unchecked")
	private int increToFile(CsvWriter cw, List<IncreBean> list, DataDTO data, DataSyncOutEvent event, int lastRowId) throws Exception {
		int rowId = 0;
		if (cw != null && !DataSyncTools.isEmpty(list)) {
			for (IncreBean increBean : list) {
				if (increBean.getBean() instanceof String && increBean.getOperateType() == OperateType.DEL) {
					String indexCode = (String)increBean.getBean();
					boolean flag = false;
					for (DataConvert.Data.Fields.Field field : data.getFields()) {
						if (field.isUnique() == null?false:field.isUnique().booleanValue()) {
							Set<String> vs = data.getValueExcludes().get(field.getTarget());
							if (!DataSyncTools.isEmpty(vs) && vs.contains(indexCode)) {
								flag = true;
								break;
							}
						}
					}
					if (flag) {
						continue;
					}
					rowId++;
					cw.writeRecord(convertIncreResToCsv(indexCode, data.getFields(), lastRowId + rowId, OperateType.DEL));
				} else {
					Map<String, Object> map = null;
					if (DataSyncTools.equals(data.getClazz().getName(), Map.class.getName())) {
						map = (Map<String, Object>)increBean.getBean();
					} else {
						map = DataSyncTools.convertToMap(increBean.getBean());
					}
					if (!event.customValue(map, data.getClazz(), data.getTypeCode())) {
						log.warn("{}:typeCode=[{}]中的{}数据被自定义忽略同步", event.getCode(), data.getTypeCode(), mapToStr(map));
						continue;
					}
					int size = data.getFields().size();
					String[] csv = new String[size];
					boolean exclude = false;
					for (int i = 0; i < size; i++) {
						DataConvert.Data.Fields.Field field = data.getFields().get(i);
						if (DataSyncTools.isNotBlank(field.getTarget())) {
							IValueConvert<Object> valueConvert = classToObj(field.getConvert());
							Object v = map.get(field.getTarget());
							// FIXME 1.排除不同步的原始值
							if (exclude = valueConvert.valueExclude(data.getValueExcludes().get(field.getTarget()), v)) {
								break;
							}
							// FIXME 2.null字符串转换成null对象
							v = valueConvert.nullConvert(v);
							// FIXME 3.原始值映射转换
							v = valueConvert.mapping(field.getMapping(), v);
							// FIXME 4.假设字段为null的话，如果field上配置了default，则不管数据库中是否存在该数据，都会进行默认值赋值。不同于defaultValue
							if ((null == v || DataSyncTools.isBlank(v.toString()))) {
								if (DataSyncTools.isNotBlank(field.getDefault())) {
									v = field.getDefault();
								} else if (DataSyncTools.isNotBlank(data.getDefaultValues().get(field.getTarget()))) {
									v = data.getDefaultValues().get(field.getTarget());
								}
							}
							// FIXME 5.自定义转换
							v = valueConvert.convert(data.getTypeCode(), field, v);
							if (v != null) {
								csv[i] = v.toString();
							}
						}
					}
					if (exclude) {
						log.warn("{}:typeCode=[{}]中的{}数据被配置忽略同步", event.getCode(), data.getTypeCode(), mapToStr(map));
						continue;
					}
					rowId++;
					cw.writeRecord(convertIncreResToCsv(map, data.getFields(), lastRowId + rowId, increBean.getOperateType()));
				}
			}
			cw.flush();
		}
		return rowId;
	}
	
	private String[] convertIncreResToCsv(Map<String, Object> map, List<DataConvert.Data.Fields.Field> fields, int lastRowId, OperateType operateType) {
		int size = fields.size();
		String[] csv = new String[size + 2];
		csv[0] = lastRowId + "";
		csv[1] = operateType.name();
		for (int i = 0; i < size; i++) {
			DataConvert.Data.Fields.Field field = fields.get(i);
			if (DataSyncTools.isNotBlank(field.getTarget())) {
				Object val = map.get(field.getTarget());
				if (val != null) {
					csv[i + 2] = val.toString();
				}
			}
		}
		return csv;
	}
	
	private String[] convertIncreResToCsv(String indexCode, List<DataConvert.Data.Fields.Field> fields, int lastRowId, OperateType operateType) {
		int size = fields.size();
		String[] csv = new String[size + 2];
		csv[0] = lastRowId + "";
		csv[1] = operateType.name();
		for (int i = 0; i < size; i++) {
			DataConvert.Data.Fields.Field field = fields.get(i);
			if (field.isUnique() == null?false:field.isUnique().booleanValue()) {
				csv[i + 2] = indexCode;
				break;
			}
		}
		return csv;
	}
}
