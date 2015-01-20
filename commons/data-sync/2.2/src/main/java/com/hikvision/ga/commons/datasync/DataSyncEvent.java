package com.hikvision.ga.commons.datasync;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.csvreader.CsvWriter;
import com.hikvision.ga.commons.datasync.common.DataSyncException;
import com.hikvision.ga.commons.datasync.common.ResultType;
import com.hikvision.ga.commons.datasync.convert.DataConvert;
import com.hikvision.ga.commons.datasync.convert.DataConvert.Notifys.Notify;
import com.hikvision.ga.commons.datasync.convert.DataDTO;
import com.hikvision.ga.commons.datasync.utils.DataSyncTools;

/**
 * 数据同步事件
 * @author shanguoming 2014年12月31日 下午4:53:30
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年12月31日 下午4:53:30
 */
public abstract class DataSyncEvent {
	
	private final Logger log = LoggerFactory.getLogger("data-sync:DataSyncEvent");
	private String path;
	private String code;
	private String templateFile;
	private DataConvert dataConvert;
	private int cleanHistory = 7;
	private String updateRecoder;
	private String relativePath;
	private String fileSuffix;
	private Charset charset;
	private int limit = 20000;
	private Map<String, DataDTO> datas = new LinkedHashMap<String, DataDTO>();
	private List<Notify> notifys = null;
	private long startTime = 0;
	private long endTime = 0;
	
	/**
	 * 数据同步和生成的构造函数
	 * 创建一个新的实例DataSyncEvent.
	 * @param path 路径（远程使用：http开头，本地使用：file开头）
	 * @param code 编号
	 * @param templateFile 模板文件
	 */
	public DataSyncEvent(String path, String code, String templateFile) {
		this.path = path;
		this.code = code;
		this.templateFile = templateFile;
	}
	
	/**
	 * 验证事件的有效性
	 * @author shanguoming 2015年1月4日 上午10:13:39
	 * @throws DataSyncException
	 * @modify: {原因} by shanguoming 2015年1月4日 上午10:13:39
	 */
	protected abstract void validate() throws DataSyncException;
	
	/**
	 * 获取资源模板转成的java对象
	 * @author shanguoming 2014年6月11日 下午4:01:49
	 * @return
	 * @throws JAXBException
	 * @throws IOException
	 */
	protected void initDataConvert() throws DataSyncException {
		if (dataConvert == null) {
			log.info("{}:读取资源模板文件，路径[{}]", code, templateFile);
			String xml;
			InputStream in = null;
			try {
				in = DataSyncTools.getInputStreamForPath(templateFile);
				xml = IOUtils.toString(in, "UTF-8");
			} catch (IOException e) {
				log.error("{}：读取资源模板[{}]IO异常", code, templateFile, e);
				throw new DataSyncException(e, ResultType.READ_TEMPLATE_EXCEPTION);
			} finally {
				IOUtils.closeQuietly(in);
			}
			try {
				dataConvert = DataSyncTools.converyToJavaBean(xml, DataConvert.class);
			} catch (JAXBException e) {
				log.error("{}：解析资源模板[{}]异常", code, templateFile, e);
				throw new DataSyncException(e, ResultType.PARSE_TEMPLATE_EXCEPTION);
			}
			if (dataConvert == null) {
				log.error("{}：解析资源模板[{}]后dataConvert is null ", code, templateFile);
				throw new DataSyncException("dataConvert is null", ResultType.PARSE_TEMPLATE_EXCEPTION);
			}
			if (dataConvert.getCleanHistory() != null) {
				cleanHistory = dataConvert.getCleanHistory().intValue();
			}
			fileSuffix = DataSyncTools.defaultIfBlank(dataConvert.getFileSuffix(), "csv");
			updateRecoder = DataSyncTools.defaultIfBlank(dataConvert.getUpdateRecoder(), "updateRecoder");
			relativePath = dataConvert.getRelativePath();
			try {
				charset = Charset.forName(DataSyncTools.defaultIfBlank(dataConvert.getEncoding(), "UTF-8"));
			} catch (Exception e) {
				log.warn("{}：生成资源文件编码格式[{}]异常,设置服务默认编码[{}]", code, dataConvert.getEncoding(), Charset.defaultCharset().name());
			}
			if (dataConvert != null && !DataSyncTools.isEmpty(dataConvert.getData())) {
				int size = dataConvert.getData().size();
				for (int i = 0; i < size; i++) {
					DataConvert.Data data = dataConvert.getData().get(i);
					if (data != null) {
						if (DataSyncTools.isNotBlank(data.getTypeCode()) || DataSyncTools.isNotBlank(data.getClazz()) || (data.getFields() != null && !DataSyncTools.isEmpty(data.getFields().getField()))) {
							DataDTO dataDTO = new DataDTO();
							try {
								dataDTO.setClazz(Class.forName(data.getClazz()));
							} catch (ClassNotFoundException e) {
								log.error("{}：生成[{}]资源时，检测到模板文件中第[{}]个data标签中class值[{}]为无效对象值", code, templateFile, (i + 1), data.getClazz());
								continue;
							}
							dataDTO.setTypeCode(data.getTypeCode());
							if (data.getFields() != null) {
								dataDTO.setFields(data.getFields().getField());
							}
							if (data.getParams() != null) {
								if (data.getParams().getParam() != null && !data.getParams().getParam().isEmpty()) {
									for (DataConvert.Data.Params.Param param : data.getParams().getParam()) {
										if (param != null && param.getTarget() != null && !param.getTarget().trim().equals("")) {
											dataDTO.putParam(param.getTarget(), param.getContent());
										}
									}
								}
							}
							if (data.getUpdateExcludes() != null) {
								if (data.getUpdateExcludes().getUpdateExclude() != null && !data.getUpdateExcludes().getUpdateExclude().isEmpty()) {
									for (DataConvert.Data.UpdateExcludes.UpdateExclude updateExclude : data.getUpdateExcludes().getUpdateExclude()) {
										dataDTO.putUpdateExclude(updateExclude.getTarget(), updateExclude.getModel());
									}
								}
							}
							if (data.getDefaultValues() != null) {
								if (data.getDefaultValues().getDefaultValue() != null && !data.getDefaultValues().getDefaultValue().isEmpty()) {
									for (DataConvert.Data.DefaultValues.DefaultValue defaultValue : data.getDefaultValues().getDefaultValue()) {
										dataDTO.putDefaultValue(defaultValue.getTarget(), defaultValue.getContent());
									}
								}
							}
							if (data.getValueExcludes() != null) {
								if (data.getValueExcludes().getValueExclude() != null && !data.getValueExcludes().getValueExclude().isEmpty()) {
									for (DataConvert.Data.ValueExcludes.ValueExclude valueExclude : data.getValueExcludes().getValueExclude()) {
										dataDTO.putValueExclude(valueExclude.getTarget(), valueExclude.getContent());
									}
								}
							}
							dataDTO.setIncreName(data.getIncreName());
							dataDTO.setFullName(data.getFullName());
							datas.put(data.getTypeCode(), dataDTO);
						} else {
							if (DataSyncTools.isBlank(data.getTypeCode())) {
								log.info("{}：生成[{}]资源时，检测到模板文件中第[{}]个data标签中缺少typeCode值", code, templateFile, (i + 1));
							}
							if (DataSyncTools.isBlank(data.getClazz())) {
								log.info("{}：生成[{}]资源时，检测到模板文件中第[{}]个data标签中缺少class值", code, templateFile, (i + 1));
							}
							if (data.getFields() == null || DataSyncTools.isEmpty(data.getFields().getField())) {
								log.info("{}：生成[{}]资源时，检测到模板文件中第[{}]个data标签中缺少fields或field标签", code, templateFile, (i + 1));
							}
						}
					}
					if (dataConvert.getNotifys() != null) {
						notifys = dataConvert.getNotifys().getNotify();
					}
				}
				if (datas.isEmpty()) {
					throw new DataSyncException("模板文件[" + templateFile + "]主要的data标签定义缺少或者不完整", ResultType.TEMPLATE_FILE_STRUCTURE_MISS_DATA);
				}
			}
		}
	}
	
	/**
	 * 更新本地记录文件
	 * @author shanguoming 2014年7月7日 下午5:13:44
	 * @param values
	 * @param updateRecoderFileName
	 * @param charset
	 * @throws IOException
	 */
	public void updateRecoder(List<String[]> values, String updateRecoderFileName, String path) throws DataSyncException {
		CsvWriter cw = null;
		FileWriterWithEncoding fw = null;
		String localFile = path + updateRecoderFileName;
		try {
			File local = new File(localFile);
			// 判断本地文件是否存在，存在则表示无需再下载
			if (!local.exists()) {
				local.createNewFile();
			}
			fw = new FileWriterWithEncoding(localFile, getCharset(), false);
			cw = new CsvWriter(fw, ',');
			if (!DataSyncTools.isEmpty(values)) {
				Set<String> syncDirs = new HashSet<String>();
				for (String[] vals : values) {
					cw.writeRecord(vals);
					syncDirs.add(vals[1]);
				}
			}
			cw.flush();
		} catch (IOException e) {
			log.error("{}:更新本地[{}]记录文件时IO异常", getCode(), localFile, e);
			throw new DataSyncException(e);
		} finally {
			if (cw != null) {
				cw.close();
			}
			IOUtils.closeQuietly(fw);
		}
	}
	
	public Charset getCharset() {
		if (charset == null) {
			charset = Charset.defaultCharset();
		}
		return charset;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		if (DataSyncTools.isNotBlank(path)) {
			if (path.endsWith("/")) {
				this.path = path.substring(0, path.length() - 1);
			} else {
				this.path = path;
			}
		}
	}
	
	/**
	 * 递归删除文件
	 * @author shanguoming 2014年7月7日 下午3:32:25
	 * @param file
	 * @param code 编号
	 */
	private void cleanFile(File file) {
		try {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				if (null != files && files.length > 0) {
					for (File f : files) {
						if (f.exists()) {
							cleanFile(f);
						}
					}
				}
				file.delete();
			} else {
				file.delete();
			}
		} catch (Exception e) {
			log.error("{}:删除文件或目录[{}]异常", code, file.getName(), e);
		}
	}
	
	/**
	 * 清理本地同步文件
	 * @author shanguoming 2015年1月4日 下午2:37:13
	 * @param localPath
	 * @param code
	 * @param cleanHistory
	 * @modify: {原因} by shanguoming 2015年1月4日 下午2:37:13
	 */
	public void cleanFile(String localPath) {
		File local = new File(localPath);
		if (local.exists()) {
			File[] childs = local.listFiles();
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.DATE, -cleanHistory);
			String now = DataSyncTools.getCurrentDateStr(DataSyncTools.C_DATA_PATTON_YYYYMMDD) + "000000";
			if (childs != null && childs.length > 0) {
				for (File childFile : childs) {
					if (childFile.exists() && now.compareTo(childFile.getName()) < 0) {
						cleanFile(childFile);
					}
				}
			}
		}
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getTemplateFile() {
		return templateFile;
	}
	
	public void setTemplateFile(String templateFile) {
		this.templateFile = templateFile;
	}
	
	public DataConvert getDataConvert() {
		return dataConvert;
	}
	
	public int getCleanHistory() {
		return cleanHistory;
	}
	
	public String getUpdateRecoder() {
		return updateRecoder;
	}
	
	public String getRelativePath() {
		return relativePath;
	}
	
	public String getFileSuffix() {
		return fileSuffix;
	}
	
	public String getUpdateRecoderFileName() {
		return updateRecoder + "." + fileSuffix;
	}
	
	public int getLimit() {
		return limit;
	}
	
	public Map<String, DataDTO> getDatas() {
		return datas;
	}
	
	public List<Notify> getNotifys() {
		return notifys;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	public long getEndTime() {
		return endTime;
	}
	
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	/**
	 * 获取数据的所有唯一键值
	 * @author shanguoming 2014年7月10日 上午10:53:04
	 * @param data 资源对象类型
	 * @return
	 * @throws DataSyncException
	 */
	protected abstract List<String> getAllUnique(DataDTO data) throws DataSyncException;
	
	/**
	 * 根据indexCode集查询对应的资源map集合
	 * @author shanguoming 2014年7月5日 下午2:09:29
	 * @param indexCodes 资源indexCode集
	 * @param params 预设参数
	 * @param clazz 资源类型
	 * @param typeCode 配置文件中的资源code
	 * @return
	 */
	protected abstract List<?> findByIndexCodes(List<String> indexCodes, Map<String, String> params, Class<?> clazz, String typeCode) throws DataSyncException;
	
	/**
	 * 用户自定义的值转换，如果返回false，则表示直接忽略当前map的同步值
	 * @author shanguoming 2015年1月5日 下午4:06:20
	 * @param map 经过数据同步sdk转换的map值
	 * @param clazz 数据对应的对象
	 * @param typeCode 数据的类型
	 * @return true：该map对象有效，false：该map对象无效
	 * @modify: {原因} by shanguoming 2015年1月5日 下午4:06:20
	 */
	protected abstract boolean customValue(Map<String, Object> map, Class<?> clazz, String typeCode) throws DataSyncException;
	
	/**
	 * 一种资源类型正常同步完成后回调的事件
	 * @author shanguoming 2015年1月6日 下午2:21:52
	 * @param typeCode
	 * @param clazz
	 * @param args
	 * @modify: {原因} by shanguoming 2015年1月6日 下午2:21:52
	 */
	protected abstract void endSyncByTypeCode(String typeCode, Class<?> clazz, Object... args);
	
	/**
	 * 全部资源类型正常同步完成后回调的事件
	 * @author shanguoming 2015年1月6日 下午2:26:29
	 * @param event
	 * @param notifys
	 * @param args
	 * @modify: {原因} by shanguoming 2015年1月6日 下午2:26:29
	 */
	protected abstract void endSync(List<Notify> notifys, Object... args);
	
	/**
	 * 同步执行结束的回调对象，可能有异常返回
	 * @author shanguoming 2015年1月6日 下午2:27:12
	 * @param event
	 * @param resultType
	 * @param e
	 * @param args
	 * @modify: {原因} by shanguoming 2015年1月6日 下午2:27:12
	 */
	public abstract void callback(ResultType resultType, DataSyncException e, Object... args);
}
