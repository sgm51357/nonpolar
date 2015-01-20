package com.hikvision.ga.commons.datasync.in;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.csvreader.CsvReader;
import com.hikvision.ga.commons.datasync.ResSyncBase;
import com.hikvision.ga.commons.datasync.common.DataSyncException;
import com.hikvision.ga.commons.datasync.common.ResultType;
import com.hikvision.ga.commons.datasync.common.UpdateRecoder;
import com.hikvision.ga.commons.datasync.convert.DataConvert;
import com.hikvision.ga.commons.datasync.convert.DataDTO;
import com.hikvision.ga.commons.datasync.utils.DataSyncTools;

/**
 * csv数据输入同步处理类
 * @author fangzhibin 2015年1月7日 下午2:22:08
 * @version V1.0   
 * @modify: {原因} by fangzhibin 2015年1月7日 下午2:22:08
 */
public class CvsResSyncInSysHandler extends ResSyncBase {
	
	private final Logger log = LoggerFactory.getLogger("data-sync:CvsResSyncInSysHandler");
	private IValueConvert<String> defaultValueConvert = new DefaultInValueConvert();
	
	/**
	 * 执行资源同步接口
	 * @author shanguoming 2014年7月21日 上午11:25:43
	 * @param isForce 是否强制重新同步
	 * @return
	 * @throws DataSyncException
	 * @throws Exception
	 */
	public void execute(DataSyncInEvent event) throws DataSyncException {
		log.info("-------------------开始[{}]任务,是否强制[{}]-----------------------", event.getCode(), event.isForce());
		event.setStartTime(System.currentTimeMillis());
		Map<String, UpdateRecoder> remoteUpdateRecodes = null;
		try {
			remoteUpdateRecodes = event.getRemoteUpdateRecoder(event.getUpdateRecoderFileName());
		} catch (IOException e) {
			log.info("{}：读远程更新记录文件[{}]时IO异常", event.getCode(), event.getRemoteFileUrl(event.getUpdateRecoderFileName()));
			event.callback(ResultType.REMOTE_UPDATERECODER_IO_ERROR, null);
			return;
		}
		if (DataSyncTools.isEmpty(remoteUpdateRecodes)) {
			log.error("{}：没有获得[{}]上的资源更新记录", event.getCode(), event.getRemoteFileUrl(event.getUpdateRecoderFileName()));
			event.callback(ResultType.REMOTE_UPDATERECODER_NO_UPDATE, null);
			return;
		}
		event.cleanFile(event.getLocalPath());
		Map<String, UpdateRecoder> localUpdateRecodes = null;
		try {
			localUpdateRecodes = getLocalUpdateRecoder(event.getLocalPath(), event.getUpdateRecoderFileName(), event.getCharset());
		} catch (IOException e) {
			log.info("{}：无法获得本地的更新记录信息[{}],判定为是第一次更新", event.getCode(), event.getLocalPath());
		}
		for (DataDTO data : event.getDatas().values()) {
			long start = System.currentTimeMillis();
			UpdateRecoder remoteUpdateRecode = remoteUpdateRecodes.get(data.getTypeCode());
			if (remoteUpdateRecode == null) {
				log.info("{}:远程没有资源类型为[{}]的增量或全量更新记录", event.getCode(), data.getTypeCode());
				continue;
			}
			if (!remoteUpdateRecode.isValidate()) {
				log.error("{}:远程typecode=[{}]更新记录的格式错误", event.getCode(), data.getTypeCode());
				continue;
			}
			UpdateRecoder localUpdateRecode = null;
			if (localUpdateRecodes != null) {
				localUpdateRecode = localUpdateRecodes.get(data.getTypeCode());
			}
			log.info("{}:typecode=[{}]的本地更新记录文件信息: {}", (null == localUpdateRecode)?null:Arrays.toString(localUpdateRecode.getRecoders()));
			log.info("{}:typecode=[{}]的远程更新记录文件信息: {}", Arrays.toString(remoteUpdateRecode.getRecoders()));
			boolean isFull = isFullUpdate(remoteUpdateRecode, localUpdateRecode) || event.isForce();
			if (isFull) {// 全量更新
				log.info("-------------------开始[{}]任务的全量同步-----------------------", event.getCode());
				FileInputStream fis = null;
				CsvReader csv = null;
				String localFile = null;
				try {
					if (localUpdateRecodes != null) {
						localUpdateRecodes.remove(data.getTypeCode());
						localUpdateRecode = null;
					}
					localFile = event.downloadFullFile(remoteUpdateRecode.getSyncDir(), remoteUpdateRecode.getFullFileName());
					if (DataSyncTools.isNotBlank(localFile)) {
						fis = new FileInputStream(localFile);
						csv = new CsvReader(fis, ',', event.getCharset());
						List<String[]> values = new ArrayList<String[]>();
						long fullAllUniqueStart = System.currentTimeMillis();
						List<String> indexCodeList = event.getAllUnique(data);
						int uniqueSize = 0;
						Set<String> uniqueList = new HashSet<String>();
						if (!DataSyncTools.isEmpty(indexCodeList)) {// 避免大数据时，从list删除indexcode的性能问题，转成set
							uniqueSize = indexCodeList.size();
							uniqueList.addAll(indexCodeList);
						}
						long fullStart = System.currentTimeMillis();
						long count = 0;
						long batchSize = 0;
						String[] valueArr = null;
						while (csv.readRecord()) {
							valueArr = csv.getValues();
							if (null == valueArr) {
								log.warn("{}:读取[{}]全量文件时，读到空数据", event.getCode(), data.getTypeCode());
								continue;
							}
							if (count == 0l) {
								log.info("{}:全量同步首行有效数据:", event.getCode(), Arrays.toString(valueArr));
							}
							values.add(valueArr);
							if (!DataSyncTools.isEmpty(values) && values.size() % event.getLimit() == 0) {
								uniqueList.removeAll(toDBFull(data, values, event));
								values.clear();
								batchSize++;
							}
							count++;
						}
						log.info("{}:全量同步尾行数据:", event.getCode(), Arrays.toString(valueArr));
						if (!values.isEmpty()) {
							uniqueList.removeAll(toDBFull(data, values, event));
							batchSize++;
						}
						long fullDeleteStart = System.currentTimeMillis();
						event.deleteByIndexCodesForSync(new ArrayList<String>(uniqueList), data.getClazz(), data.getTypeCode(), data.getParams());
						long fullEnd = System.currentTimeMillis();
						log.info("{}:全量同步[{}],共读取csv记录{}条,分{}批次入库,唯一键集合中还剩余{}条需要从数据库删除,查询唯一键集合耗时：{}ms,解析入库耗时：{}ms,删除耗时：ms,总耗时：{}ms", event.getCode(), data.getTypeCode(), count, batchSize, uniqueSize,
						        (fullStart - fullAllUniqueStart), (fullDeleteStart - fullStart), (fullEnd - fullDeleteStart), (fullEnd - fullAllUniqueStart));
					}
				} catch (IOException e) {
					log.error("{}:读取资源文件[{}]IO异常", event.getCode(), localFile, e);
					event.callback(ResultType.READ_RESOURCE_FILE_EXCEPTION, new DataSyncException(e));
					return;
				} finally {
					if (csv != null) {
						csv.close();
					}
					IOUtils.closeQuietly(fis);
				}
				log.info("-------------------结束[{}]任务的全量同步-----------------------", event.getCode());
			}
			if (isIncreUpdate(remoteUpdateRecode, localUpdateRecode) || isFull) {// 增量更新
				log.info("-------------------开始[{}]任务的增量同步-----------------------", event.getCode());
				FileInputStream fis = null;
				CsvReader csv = null;
				String localFile = null;
				int rowId = 0;
				try {
					// 本地上次更新行号
					rowId = DataSyncTools.toInt(localUpdateRecode == null?null:localUpdateRecode.getIncreLastUpdateRowId());
					if (isFull) {// 如果进行了全量同步或者本地行号要大于远程行号，则增量从0开始重新读取
						rowId = 0;
					}
					localFile = event.downloadIncreFile(remoteUpdateRecode.getSyncDir(), remoteUpdateRecode.getIncreFileName(), rowId);
					if (DataSyncTools.isNotBlank(localFile)) {
						fis = new FileInputStream(localFile);
						csv = new CsvReader(fis, ',', event.getCharset());
						LinkedHashMap<String, String[]> addOrModValues = new LinkedHashMap<String, String[]>();
						List<String> delIndexCodes = new ArrayList<String>();
						long remoteRowId = 0;
						int num = 0;// 记录增量有效值的起始位置，ncg默认从0开始
						long count = 0;
						long increStart = System.currentTimeMillis();
						int size = data.getFields().size();
						int uniqueNum = 0;
						for (int j = 0; j < size; j++) {// 获取唯一键
							DataConvert.Data.Fields.Field field = data.getFields().get(j);
							if (field.isUnique() != null && field.isUnique().booleanValue()) {
								uniqueNum = j;
								break;
							}
						}
						String[] valueArr = null;
						while (csv.readRecord()) {
							valueArr = csv.getValues();
							if (null == valueArr || valueArr.length <= 2) {
								log.warn("{}:读取[{}]增量文件时，读到空数据", event.getCode(), data.getTypeCode());
								continue;
							}
							// 对ncg的增量做兼容
							String operate = valueArr[valueArr.length - 2];
							remoteRowId = DataSyncTools.toLong(valueArr[valueArr.length - 1]);
							// 当操作类型不是[ADD,MOD,DEL,OFF]中的任意值 或者最后一行不是数字
							if (!((DataSyncTools.equalsIgnoreCase(operate, "ADD") || DataSyncTools.equalsIgnoreCase(operate, "MOD") || DataSyncTools.equalsIgnoreCase(operate, "OFF") || DataSyncTools.equalsIgnoreCase(
							        operate, "DEL")) && DataSyncTools.isNumber(valueArr[valueArr.length - 1]))) {
								num = 2;// SDK的从2开始是有效值起始位置，0,1位是格式固定占位符
								operate = valueArr[1];// SDK的操作类型占位符
								remoteRowId = DataSyncTools.toLong(valueArr[0]);
							}
							if (remoteRowId <= rowId) {// 过滤已更新的数据
								log.warn("{}:发现{}行已经被更新过，可能是高读写并发导致行号不一致", event.getCode(), remoteRowId);
								continue;
							}
							if (count == 0l) {
								log.info("{}:本次增量同步首行有效数据:", event.getCode(), Arrays.toString(valueArr));
							}
							String uniqueKey = valueArr[uniqueNum + num];
							if (DataSyncTools.isNotBlank(uniqueKey)) {
								count++;
								if (DataSyncTools.equalsIgnoreCase(operate, "ADD") || DataSyncTools.equalsIgnoreCase(operate, "MOD") || DataSyncTools.equalsIgnoreCase(operate, "OFF")) {
									if (!DataSyncTools.isEmpty(delIndexCodes)) {
										log.info("{}:增量同步[{}]时遇到保存会更新操作,先处理之前的批量删除操作,删除数据{}条", event.getCode(), data.getClazz(), delIndexCodes.size());
										event.deleteByIndexCodesForSync(delIndexCodes, data.getClazz(), data.getTypeCode(), data.getParams());
										delIndexCodes.clear();
									}
									addOrModValues.put(uniqueKey, valueArr);
								} else if (DataSyncTools.equalsIgnoreCase(operate, "DEL")) {
									if (!addOrModValues.isEmpty()) {
										toDBIncre(data, addOrModValues, num, event);
										addOrModValues.clear();
									}
									delIndexCodes.add(uniqueKey);
								}
								if (!addOrModValues.isEmpty() && addOrModValues.size() % 100 == 0) {
									toDBIncre(data, addOrModValues, num, event);
									addOrModValues.clear();
								}
								if (!DataSyncTools.isEmpty(delIndexCodes) && delIndexCodes.size() % 100 == 0) {
									log.info("{}:增量同步[{}]时遇到批量处理限制数100,批量删除数据", event.getCode(), data.getClazz());
									event.deleteByIndexCodesForSync(delIndexCodes, data.getClazz(), data.getTypeCode(), data.getParams());
									delIndexCodes.clear();
								}
							}
						}
						log.info("{}:本次增量同步尾行数据:", event.getCode(), Arrays.toString(valueArr));
						if (!addOrModValues.isEmpty()) {
							toDBIncre(data, addOrModValues, num, event);
							addOrModValues.clear();
						}
						if (!DataSyncTools.isEmpty(delIndexCodes)) {
							log.info("{}:增量同步[{}]删除左后一批批量数据{}条", event.getCode(), data.getClazz(), delIndexCodes);
							event.deleteByIndexCodesForSync(delIndexCodes, data.getClazz(), data.getTypeCode(), data.getParams());
							delIndexCodes.clear();
						}
						remoteUpdateRecode.setIncreLastUpdateRowId(remoteRowId + "");
						long increEnd = System.currentTimeMillis();
						log.info("{}:增量更新[{}]完成,从第{}条开始更新,共更新{}条,耗时:{}ms", event.getCode(), data.getTypeCode(), (rowId + 1), count, (increEnd - increStart));
					}
				} catch (IOException e) {
					log.error("{}:读取资源文件异常[{}] io error", event.getCode(), localFile, e);
					event.callback(ResultType.READ_RESOURCE_FILE_EXCEPTION, new DataSyncException(e));
					return;
				} finally {
					if (csv != null) {
						csv.close();
					}
					IOUtils.closeQuietly(fis);
				}
				log.info("-------------------结束[{}]任务的增量同步-----------------------", event.getCode());
			}
			List<String[]> updateRecoders = new ArrayList<String[]>();
			updateRecoders.add(UpdateRecoder.getHeaders());
			for (UpdateRecoder updateRecoder : remoteUpdateRecodes.values()) {
				updateRecoders.add(updateRecoder.getRecoders());
			}
			if (updateRecoders.size() > 1) {
				event.updateRecoder(updateRecoders, event.getUpdateRecoderFileName(), event.getLocalPath());
			}
			event.endSyncByTypeCode(data.getTypeCode(), data.getClazz());
			long end = System.currentTimeMillis();
			log.debug("{}:[{}]资源同步执行时间：{}ms", event.getCode(), data.getTypeCode(), (end - start));
		}
		event.endSync(event.getNotifys());
		log.info("-------------------结束[{}]任务-----------------------", event.getCode());
		event.callback(ResultType.SUCCESS, null);
	}
	
	/**
	 * 判断是否需要全量更新
	 * @author shanguoming 2014年7月22日 上午10:14:16
	 * @param remoteUpdateRecoder
	 * @param localUpdateRecoder
	 * @return
	 */
	private boolean isFullUpdate(UpdateRecoder remoteUpdateRecoder, UpdateRecoder localUpdateRecoder) {
		String remoteLastUpdateTime = remoteUpdateRecoder == null?null:remoteUpdateRecoder.getFullLastUpdateTime();
		String localLastUpdateTime = localUpdateRecoder == null?null:localUpdateRecoder.getFullLastUpdateTime();
		return (DataSyncTools.isNotBlank(remoteLastUpdateTime) && !DataSyncTools.equals(remoteLastUpdateTime, localLastUpdateTime));
	}
	
	/**
	 * 判断是否需要增量更新
	 * @author shanguoming 2014年7月22日 上午10:14:16
	 * @param remoteUpdateRecoder
	 * @param localUpdateRecoder
	 * @return
	 */
	private boolean isIncreUpdate(UpdateRecoder remoteUpdateRecoder, UpdateRecoder localUpdateRecoder) {
		String remoteLastRowId = remoteUpdateRecoder == null?null:remoteUpdateRecoder.getIncreLastUpdateRowId();
		String localLastRowId = localUpdateRecoder == null?null:localUpdateRecoder.getIncreLastUpdateRowId();
		return (remoteLastRowId == null?0l:DataSyncTools.toLong(remoteLastRowId)) > (localLastRowId == null?0l:DataSyncTools.toLong(localLastRowId));
	}
	
	/**
	 * class转Object
	 * @author shanguoming 2015年1月5日 下午2:42:28
	 * @param className
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月5日 下午2:42:28
	 */
	@SuppressWarnings("unchecked")
	private IValueConvert<String> classToObj(String className) {
		if (DataSyncTools.isNotBlank(className)) {
			try {
				Class<?> clazz = Class.forName(className);
				return (IValueConvert<String>)clazz.newInstance();
			} catch (Exception e) {
				log.error("对象className={}实例化成对象时异常", className, e);
			}
		}
		return defaultValueConvert;
	}
	
	/**
	 * 全量数据写入数据库
	 * @author shanguoming 2014年7月21日 上午11:28:15
	 * @param data
	 * @param values
	 * @return
	 * @throws Exception
	 */
	private List<String> toDBFull(DataDTO data, List<String[]> values, DataSyncInEvent event) throws DataSyncException {
		List<String> indexCodes = new ArrayList<String>();
		LinkedHashMap<String, Map<String, Object>> beansMap = new LinkedHashMap<String, Map<String, Object>>();
		int size = data.getFields().size();
		String key = null;
		String uniqueKey = null;
		for (String[] value : values) {
			int length = value.length;
			Map<String, Object> beanMap = new HashMap<String, Object>();
			boolean exclude = false;
			for (int i = 0; i < size; i++) {
				DataConvert.Data.Fields.Field field = data.getFields().get(i);
				if (i >= length) {
					log.warn("{}: 同步[{}]时字段溢出，溢出字段[{}]", event.getCode(), data.getTypeCode(), field.getTarget());
					break;
				}
				if (DataSyncTools.isNotBlank(field.getTarget())) {
					String v = value[i];
					IValueConvert<String> valueConvert = classToObj(field.getConvert());
					// FIXME 1.排除不同步的原始值
					if (exclude = valueConvert.valueExclude(data.getValueExcludes().get(field.getTarget()), v)) {
						break;
					}
					// FIXME 2.null字符串转换成null对象
					v = valueConvert.nullConvert(v);
					// FIXME 3.原始值映射转换
					v = valueConvert.mapping(field.getMapping(), v);
					// FIXME 4.假设字段为null的话，如果field上配置了default，则不管数据库中是否存在该数据，都会进行默认值赋值。不同于defaultValue
					if (DataSyncTools.isBlank(v) && DataSyncTools.isNotBlank(field.getDefault())) {
						if (DataSyncTools.isNotBlank(field.getDefault())) {
							v = field.getDefault();
						} else if (DataSyncTools.isNotBlank(data.getDefaultValues().get(field.getTarget()))) {
							v = data.getDefaultValues().get(field.getTarget());
						}
					}
					// FIXME 5.自定义转换
					v = valueConvert.convert(data.getTypeCode(), field, v);
					if (field.isUnique() != null && field.isUnique().booleanValue()) {
						key = v;
						uniqueKey = field.getTarget();
						indexCodes.add(v);
					}
					beanMap.put(field.getTarget(), v);
				}
			}
			if (exclude) {
				log.warn("{}:typeCode=[{}]中唯一键为{}的值被配置忽略同步", event.getCode(), data.getTypeCode(), key);
				continue;
			}
			if (DataSyncTools.isNotBlank(key) && !beanMap.isEmpty()) {
				// FIXME 6.用户自定义值得转换,已经是否忽略同步
				if (event.customValue(beanMap, data.getClazz(), data.getTypeCode())) {
					beansMap.put(key, beanMap);
				} else {
					log.warn("{}:typeCode=[{}]中唯一键为{}的值被自定义忽略同步", event.getCode(), data.getTypeCode(), key);
				}
			}
		}
		List<?> list = null;
		if (!beansMap.isEmpty()) {
			list = event.findByIndexCodes(indexCodes, data.getParams(), data.getClazz(), data.getTypeCode());
			merge(beansMap, list, uniqueKey, data, event);
			save(beansMap, data, event);
		}
		return indexCodes;
	}
	
	/**
	 * 增量数据写入数据库
	 * @author shanguoming 2014年7月21日 上午11:28:37
	 * @param data
	 * @param values
	 * @return
	 * @throws Exception
	 */
	private List<String> toDBIncre(DataDTO data, LinkedHashMap<String, String[]> values, int num, DataSyncInEvent event) throws DataSyncException {
		List<String> indexCodes = new ArrayList<String>();
		LinkedHashMap<String, Map<String, Object>> beansMap = new LinkedHashMap<String, Map<String, Object>>();
		int size = data.getFields().size();
		String key = null;
		String uniqueKey = null;
		for (Iterator<String[]> it = values.values().iterator(); it.hasNext();) {
			String[] value = it.next();
			int length = value.length;
			Map<String, Object> beanMap = new HashMap<String, Object>();
			boolean exclude = false;
			for (int i = 0; i < size; i++) {
				DataConvert.Data.Fields.Field field = data.getFields().get(i);
				if (i + 2 >= length) {
					log.warn("{}: 同步[{}]时字段溢出，溢出字段[{}]", event.getCode(), data.getTypeCode(), field.getTarget());
					break;
				}
				String v = value[i + num];
				if (DataSyncTools.isNotBlank(field.getTarget())) {
					IValueConvert<String> valueConvert = classToObj(field.getConvert());
					// FIXME 1.排除不同步的原始值
					if (exclude = valueConvert.valueExclude(data.getValueExcludes().get(field.getTarget()), v)) {
						break;
					}
					// FIXME 2.null字符串转换成null对象
					v = valueConvert.nullConvert(v);
					// FIXME 3.原始值映射转换
					v = valueConvert.mapping(field.getMapping(), v);
					// FIXME 4.假设字段为null的话，如果field上配置了default，则不管数据库中是否存在该数据，都会进行默认值赋值。不同于defaultValue
					if (DataSyncTools.isBlank(v) && DataSyncTools.isNotBlank(field.getDefault())) {
						if (DataSyncTools.isNotBlank(field.getDefault())) {
							v = field.getDefault();
						} else if (DataSyncTools.isNotBlank(data.getDefaultValues().get(field.getTarget()))) {
							v = data.getDefaultValues().get(field.getTarget());
						}
					}
					// FIXME 5.自定义转换
					v = valueConvert.convert(data.getTypeCode(), field, v);
					if (field.isUnique() != null && field.isUnique().booleanValue()) {
						key = v;
						uniqueKey = field.getTarget();
						indexCodes.add(v);
					}
					beanMap.put(field.getTarget(), v);
				}
			}
			if (exclude) {
				continue;
			}
			if (DataSyncTools.isNotBlank(key) && !beanMap.isEmpty()) {
				// FIXME 6.用户自定义值得转换,已经是否忽略同步
				if (event.customValue(beanMap, data.getClazz(), data.getTypeCode())) {
					beansMap.put(key, beanMap);
				} else {
					log.warn("{}:typeCode=[{}]中唯一键为{}的值被自定义忽略同步", event.getCode(), data.getTypeCode(), key);
				}
			}
		}
		List<?> list = null;
		if (!beansMap.isEmpty()) {
			merge(beansMap, list, uniqueKey, data, event);
			save(beansMap, data, event);
		}
		return indexCodes;
	}
	
	/**
	 * 更新或保存数据
	 * @author shanguoming 2014年6月11日 下午5:31:59
	 * @param beansMap
	 * @param list
	 * @param className
	 * @param uniqueKey
	 * @param excludeFields
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected void merge(Map<String, Map<String, Object>> beansMap, List<?> list, String uniqueKey, DataDTO data, DataSyncInEvent event) throws DataSyncException {
		log.info("{}:开始分批更新数据，typeCode=[{}]", event.getCode(), data.getTypeCode());
		long start = System.currentTimeMillis();
		int size = 0;
		int updateSize = 0;
		int updateExcludeSize = 0;
		try {
			if (null != list && !list.isEmpty()) {
				size = list.size();
				List<Object> updateList = new ArrayList<Object>();
				for (Object obj : list) {
					boolean isUpdate = false;
					Map<String, Object> oldMap = null;
					try {
						if (DataSyncTools.equals(obj.getClass().getName(), Map.class.getName())) {
							oldMap = (Map<String, Object>)obj;
						} else {
							oldMap = DataSyncTools.convertToMap(obj);
						}
						if (oldMap.get(uniqueKey) != null) {
							String oldKey = oldMap.get(uniqueKey).toString();
							if (DataSyncTools.isNotBlank(oldKey)) {
								Map<String, Object> newMap = beansMap.get(oldKey);
								for (String key : newMap.keySet()) {
									String excludeField = data.getUpdateExcludes().get(key);
									Object newV = newMap.get(key);
									String compare = newV == null?"":newV.toString();
									boolean excludeFlag = false;
									if (!DataSyncTools.equalsIgnoreCase(excludeField, "update")) {
										if (DataSyncTools.equalsIgnoreCase(excludeField, "force")) {
											excludeFlag = true;
										} else if (DataSyncTools.isBlank(compare)) {
											excludeFlag = true;
										} else if (DataSyncTools.equalsIgnoreCase(excludeField, "zero") && DataSyncTools.isNumber(compare) && (DataSyncTools.toDouble(compare) == DataSyncTools.toDouble(""))) {
											excludeFlag = true;
										}
									}
									if (!excludeFlag) {
										Object oldObj = oldMap.get(key);
										if (oldObj == null || !DataSyncTools.equalsIgnoreCase(excludeField, "conflict") || DataSyncTools.isBlank(oldObj.toString())
										        || DataSyncTools.equalsIgnoreCase(excludeField, "forceUpdate")) {
											if (DataSyncTools.isNumber(compare)) {
												if (oldObj == null || (DataSyncTools.toDouble(compare) != DataSyncTools.toDouble(oldObj.toString()))) {
													log.debug("resSync merge method typeCode is {}, the key is {}, value from {} to {}", data.getTypeCode(), key, compare, newV);
													oldMap.put(key, newV);
													isUpdate = true;
												}
											} else {
												String oldV = oldMap.get(key) == null?"":oldMap.get(key).toString();
												if (!DataSyncTools.equals(compare, oldV)) {
													log.debug("resSync merge method typeCode is {}, the key is {}, value from {} to {}", data.getTypeCode(), key, compare, newV);
													oldMap.put(key, newV);
													isUpdate = true;
												}
											}
										}
									} else {
										updateExcludeSize++;
									}
								}
							}
							beansMap.remove(oldKey);
						}
					} catch (Exception e) {
						log.error("{}:同步更新数据入库时，对象[{}]转map异常", event.getCode(), obj.getClass().getName(), e);
						throw new DataSyncException(e);
					}
					if (isUpdate) {
						if (DataSyncTools.equals(data.getClazz().getName(), Map.class.getName())) {
							updateList.add(oldMap);
						} else {
							try {
								updateList.add(DataSyncTools.convertToBean(data.getClazz(), oldMap));
							} catch (Exception e) {
								log.error("{}:同步更新数据入库时，map转[{}]对象异常", event.getCode(), data.getClazz().getName(), e);
								throw new DataSyncException(e);
							}
						}
					}
				}
				if (!updateList.isEmpty()) {
					updateSize = updateList.size();
					event.batchUpdate(updateList, data.getClazz(), data.getTypeCode());
				}
			}
		} finally {
			long end = System.currentTimeMillis();
			log.info("{}:分批更新[{}]数据,从数据库中查询的{}条，最后更新{}条，忽略更新{}条，耗时:{}ms", event.getCode(), data.getTypeCode(), size, updateSize, updateExcludeSize, (end - start));
		}
	}
	
	/**
	 * 保存数据
	 * @author shanguoming 2014年6月11日 下午5:31:38
	 * @param beansMap
	 * @param clazz
	 * @param syncFileName
	 * @throws Exception
	 */
	protected void save(Map<String, Map<String, Object>> beansMap, DataDTO data, DataSyncInEvent event) throws DataSyncException {
		log.info("{}:开始分批保存数据，typeCode=[{}]", event.getCode(), data.getTypeCode());
		long start = System.currentTimeMillis();
		int size = 0;
		try {
			List<Object> saveList = new ArrayList<Object>();
			for (Map<String, Object> map : beansMap.values()) {
				if (DataSyncTools.equals(data.getClazz().getName(), Map.class.getName())) {
					saveList.add(map);
				} else {
					try {
						saveList.add(DataSyncTools.convertToBean(data.getClazz(), map));
					} catch (Exception e) {
						log.error("{}:同步保存数据入库时，map转[{}]对象异常", event.getCode(), data.getClazz().getName(), e);
						throw new DataSyncException(e);
					}
				}
				size++;
			}
			if (!saveList.isEmpty()) {
				event.batchSave(saveList, data.getClazz(), data.getTypeCode());
			}
		} finally {
			long end = System.currentTimeMillis();
			log.info("{}:分批保存[{}]数据{}条，耗时:{}ms", event.getCode(), data.getTypeCode(), size, (end - start));
		}
	}
}
