package com.hikvision.ga.commons.datasync.in;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.hikvision.ga.commons.datasync.DataSyncEvent;
import com.hikvision.ga.commons.datasync.common.DataSyncException;
import com.hikvision.ga.commons.datasync.common.ResultType;
import com.hikvision.ga.commons.datasync.common.UpdateRecoder;
import com.hikvision.ga.commons.datasync.convert.DataConvert.Notifys.Notify;
import com.hikvision.ga.commons.datasync.convert.DataDTO;
import com.hikvision.ga.commons.datasync.utils.DataSyncTools;

/**
 * 数据输入同步事件
 * @author shanguoming 2014年12月31日 下午3:36:31
 * @version V1.0
 * @modify: {原因} by shanguoming 2014年12月31日 下午3:36:31
 */
public class DataSyncInEvent extends DataSyncEvent {
	
	private final Logger log = LoggerFactory.getLogger("data-sync:DataSyncInEvent");
	/**
	 * 是否强制
	 */
	private boolean force;
	/**
	 * http地址的正则表达式，用于验证path是否符合要求
	 */
	private Pattern url = Pattern.compile("(http://|file:///)(.*?)(:)");
	private IDataSyncInSysService dataSyncInSysService;
	
	/**
	 * 数据同步和生成的构造函数
	 * 创建一个新的实例DataSyncEvent.
	 * @param path 路径，以http开头
	 * @param code 编号
	 * @param templateFile 模板文件
	 */
	public DataSyncInEvent(String path, String code, String templateFile, IDataSyncInSysService dataSyncInSysService) throws DataSyncException {
		super(path, code, templateFile);
		this.dataSyncInSysService = dataSyncInSysService;
		validate();
	}
	
	protected void validate() throws DataSyncException {
		if (DataSyncTools.isBlank(super.getPath())) {
			throw new DataSyncException("path不能为空", ResultType.EMPTY_PATH);
		}
		Matcher matcher = url.matcher(super.getPath());
		String host = null;
		while (matcher.find()) {
			host = matcher.group(2);
		}
		if (DataSyncTools.isBlank(host)) {
			throw new DataSyncException("path=[" + super.getPath() + "]不是合法http地址", ResultType.INVALID_PATH);
		}
		if ("127.0.0.1".equals(host)) {
			throw new DataSyncException("path不能使用127.0.0.1的本地地址", ResultType.INVALID_PATH);
		}
		if ("localhost".equalsIgnoreCase(host)) {
			throw new DataSyncException("path不能使用localhost的本地地址", ResultType.INVALID_PATH);
		}
		if (DataSyncTools.isBlank(super.getCode())) {
			throw new DataSyncException("code不能为空", ResultType.NOT_DEFINITION_VALUE);
		}
		if (DataSyncTools.isBlank(super.getTemplateFile())) {
			throw new DataSyncException("template不能为空", ResultType.EMPTY_TEMPLATE_FILE);
		}
		if (this.dataSyncInSysService == null) {
			throw new DataSyncException("dataSyncInSysService不能为null", ResultType.NULL_INTERFACE);
		}
		initDataConvert();
	}
	
	/**
	 * 获取远程的最后更新时间
	 * @author shanguoming 2014年6月11日 下午4:00:59
	 * @param updateRecoderFile 更新记录文件名
	 * @param encoding 文件编码格式
	 * @return
	 * @throws IOException
	 */
	protected Map<String, UpdateRecoder> getRemoteUpdateRecoder(String updateRecoderFile) throws IOException {
		InputStream in = null;
		HttpURLConnection httpUrlConnection = null;
		String path = getRemoteFileUrl(updateRecoderFile);
		CsvReader csv = null;
		try {
			URL url = new URL(path);
			if (path.startsWith("http://")) {
				httpUrlConnection = (HttpURLConnection)url.openConnection();
				httpUrlConnection.setDoInput(true);
				httpUrlConnection.setUseCaches(false);
				httpUrlConnection.setConnectTimeout(2000);
				httpUrlConnection.setReadTimeout(5000);
				httpUrlConnection.connect();
				in = httpUrlConnection.getInputStream();
			} else if (path.startsWith("file:///")) {
				in = url.openStream();
			}
			csv = new CsvReader(in, ',', super.getCharset());
			if (csv.readHeaders()) {
				String[] headers = csv.getHeaders();
				if (null != headers && headers.length > 0) {
					Map<String, UpdateRecoder> updateRecoders = new LinkedHashMap<String, UpdateRecoder>();
					if (headers.length == 1) {// 当header.length==1是任务是ncg同步，对ncg同步做特别处理，转成和SDK标准格式相同的更新记录信息
						for (DataDTO dataDTO : super.getDatas().values()) {
							UpdateRecoder updateRecoder = new UpdateRecoder();
							updateRecoder.setTypeCode(dataDTO.getTypeCode());
							updateRecoder.setSyncDir("");
							updateRecoder.setFullFileName(dataDTO.getFullName());
							updateRecoder.setFullLastUpdateTime(headers[0]);
							updateRecoder.setIncreFileName(dataDTO.getIncreName());
							updateRecoder.setIncreLastUpdateRowId(0 + "");
							String increPath = getRemoteFileUrl(dataDTO.getIncreName() + "." + super.getFileSuffix());
							InputStream increIn = null;
							CsvReader increCsv = null;
							HttpURLConnection increHttpUrlConnection = null;
							try {
								URL increUrl = new URL(increPath);
								increHttpUrlConnection = (HttpURLConnection)increUrl.openConnection();
								increHttpUrlConnection.setDoInput(true);
								increHttpUrlConnection.setUseCaches(false);
								increHttpUrlConnection.setConnectTimeout(2000);
								increHttpUrlConnection.setReadTimeout(5000);
								increHttpUrlConnection.connect();
								increIn = increHttpUrlConnection.getInputStream();
								increCsv = new CsvReader(increIn, ',', super.getCharset());
								if (increCsv.readHeaders()) {
									if (null != increCsv.getHeaders() && increCsv.getHeaders().length > 0) {
										int i = 0;
										while (increCsv.readRecord()) {
											i++;
										}
										updateRecoder.setIncreLastUpdateRowId(i + "");
									}
								}
							} catch (FileNotFoundException e) {
								log.error("{}：无法找到资源更新记录文件[{}]", super.getCode(), increPath);
							} catch (IOException e) {
								log.error("{}：读取资源更新记录文件{}{}IO异常", super.getCode(), increPath, e);
							} finally {
								if (increCsv != null) {
									increCsv.close();
								}
								IOUtils.closeQuietly(increIn);
								if (increHttpUrlConnection != null) {
									increHttpUrlConnection.disconnect();
								}
							}
							updateRecoders.put(updateRecoder.getTypeCode(), updateRecoder);
						}
					} else {
						while (csv.readRecord()) {
							String[] values = csv.getValues();
							UpdateRecoder updateRecoder = new UpdateRecoder();
							updateRecoder.setTypeCode(DataSyncTools.get(values, 0));
							updateRecoder.setSyncDir(DataSyncTools.get(values, 1));
							updateRecoder.setFullFileName(DataSyncTools.get(values, 2));
							updateRecoder.setFullLastUpdateTime(DataSyncTools.get(values, 3));
							updateRecoder.setIncreFileName(DataSyncTools.get(values, 4));
							updateRecoder.setIncreLastUpdateRowId(DataSyncTools.get(values, 5));
							updateRecoders.put(updateRecoder.getTypeCode(), updateRecoder);
						}
					}
					return updateRecoders;
				}
			}
		} catch (FileNotFoundException e) {
			log.error("{}：无法找到资源更新记录文件[{}]", super.getCode(), path);
			throw e;
		} catch (IOException e) {
			log.error("{}：读取资源更新记录文件{}{}IO异常", super.getCode(), path, e);
			throw e;
		} finally {
			if (csv != null) {
				csv.close();
			}
			IOUtils.closeQuietly(in);
			if (httpUrlConnection != null) {
				httpUrlConnection.disconnect();
			}
		}
		return null;
	}
	
	/**
	 * 远程文件同步到本地
	 * @author shanguoming 2014年7月7日 下午2:17:45
	 * @param syncDir 同步目录
	 * @param fileName 文件名
	 * @param syncType 同步类型(full：全量，incre：增量)
	 * @param increRowId 增量行号
	 * @param charset 文件编码类型
	 * @return
	 */
	protected String downloadFullFile(String syncDir, String fileName) {
		if (DataSyncTools.isBlank(fileName)) {
			return null;
		}
		InputStream in = null;
		HttpURLConnection httpUrlConnection = null;
		FileWriterWithEncoding fw = null;
		String remoteFileUrl = getRemoteFileUrl((DataSyncTools.isNotBlank(syncDir)?(syncDir + "/"):"") + fileName + "." + super.getFileSuffix());
		String localPath = getLocalPath() + (DataSyncTools.isNotBlank(syncDir)?(syncDir + "/"):"");
		String localFile = localPath + fileName + "." + super.getFileSuffix();
		try {
			File localDir = new File(localPath);
			if (!localDir.exists()) {
				localDir.mkdirs();
			}
			File local = new File(localFile);
			// 判断本地文件是否存在，存在则表示无需再下载
			if (!local.exists()) {
				local.createNewFile();
			}
			// 开始读取远程文件信息写入本地
			URL url = new URL(remoteFileUrl);
			if (remoteFileUrl.startsWith("http://")) {
				httpUrlConnection = (HttpURLConnection)url.openConnection();
				httpUrlConnection.setDoInput(true);
				httpUrlConnection.setUseCaches(false);
				httpUrlConnection.setConnectTimeout(2000);
				httpUrlConnection.setReadTimeout(5000);
				httpUrlConnection.connect();
				in = httpUrlConnection.getInputStream();
			} else if (remoteFileUrl.startsWith("file:///")) {
				in = url.openStream();
			}
			fw = new FileWriterWithEncoding(localFile, super.getCharset(), false);
			IOUtils.copy(in, fw, super.getCharset());
		} catch (MalformedURLException e) {
			log.error("{}：下载[{}]文件时URL异常", super.getCode(), remoteFileUrl);
			return null;
		} catch (IOException e) {
			log.error("{}:下载[{}]文件时IO异常", super.getCode(), remoteFileUrl, e);
			return null;
		} finally {
			IOUtils.closeQuietly(fw);
			IOUtils.closeQuietly(in);
			if (httpUrlConnection != null) {
				httpUrlConnection.disconnect();
			}
		}
		return localFile;
	}
	
	protected String downloadIncreFile(String syncDir, String fileName, int increRowId) {
		if (DataSyncTools.isBlank(fileName)) {
			return null;
		}
		CsvWriter cw = null;
		CsvReader cr = null;
		InputStream in = null;
		HttpURLConnection httpUrlConnection = null;
		FileWriterWithEncoding fw = null;
		String remoteFileUrl = getRemoteFileUrl((DataSyncTools.isNotBlank(syncDir)?(syncDir + "/"):"") + fileName + "." + super.getFileSuffix());
		String localPath = getLocalPath() + (DataSyncTools.isNotBlank(syncDir)?(syncDir + "/"):"");
		String localFile = localPath + fileName + "." + super.getFileSuffix();
		try {
			File localDir = new File(localPath);
			if (!localDir.exists()) {
				localDir.mkdirs();
			}
			File local = new File(localFile);
			boolean newFile = false;
			if (!local.exists()) {
				newFile = local.createNewFile();
			} else if (increRowId <= 0) {
				local.delete();
				newFile = local.createNewFile();
			}
			// 开始读取远程文件信息写入本地
			URL url = new URL(remoteFileUrl);
			if (remoteFileUrl.startsWith("http://")) {
				httpUrlConnection = (HttpURLConnection)url.openConnection();
				httpUrlConnection.setDoInput(true);
				httpUrlConnection.setUseCaches(false);
				httpUrlConnection.setConnectTimeout(2000);
				httpUrlConnection.setReadTimeout(5000);
				httpUrlConnection.connect();
				in = httpUrlConnection.getInputStream();
			} else if (remoteFileUrl.startsWith("file:///")) {
				in = url.openStream();
			}
			cr = new CsvReader(in, ',', super.getCharset());
			// 如果是增量同步，则设置文件为续写模式
			fw = new FileWriterWithEncoding(localFile, super.getCharset(), true);
			cw = new CsvWriter(fw, ',');
			if (cr.readHeaders()) {
				log.info("{}：远程增量资源文件[{}]的头信息[{}]", super.getCode(), fileName, Arrays.toString(cr.getHeaders()));
				// 全量同步或者新建增量文件需要写文件头
				if (newFile) {
					cw.writeRecord(cr.getHeaders());
				}
			}
			int i = 0;
			while (cr.readRecord()) {
				i++;
				if (increRowId >= i) {
					continue;
				}
				cw.writeRecord(cr.getValues());
				if (i % 2000 == 0) {
					cw.flush();
				}
			}
			cw.flush();
		} catch (MalformedURLException e) {
			log.error("{}：下载[{}]文件时URL异常", super.getCode(), remoteFileUrl);
			return null;
		} catch (IOException e) {
			log.error("{}:下载[{}]文件时IO异常", super.getCode(), remoteFileUrl, e);
			return null;
		} finally {
			if (cw != null) {
				cw.close();
			}
			IOUtils.closeQuietly(fw);
			if (cr != null) {
				cr.close();
			}
			IOUtils.closeQuietly(in);
			if (httpUrlConnection != null) {
				httpUrlConnection.disconnect();
			}
		}
		return localFile;
	}
	
	@Override
	protected List<String> getAllUnique(DataDTO data) throws DataSyncException {
		if (dataSyncInSysService != null) {
			try {
				return dataSyncInSysService.getAllUnique(data.getClazz(), data.getTypeCode(), data.getParams());
			} catch (Exception e) {
				log.error("调用数据同步入库的结果回调方法时异常", e);
				throw new DataSyncException(e);
			}
		}
		return null;
	}
	
	@Override
	protected List<?> findByIndexCodes(List<String> indexCodes, Map<String, String> params, Class<?> clazz, String typeCode) throws DataSyncException {
		if (dataSyncInSysService != null) {
			try {
				return dataSyncInSysService.findByIndexCodes(indexCodes, params, clazz, typeCode);
			} catch (Exception e) {
				log.error("{}: 完成资源同步的方法异常", super.getCode(), e);
				throw new DataSyncException(e);
			}
		}
		return null;
	}
	
	@Override
	protected boolean customValue(Map<String, Object> map, Class<?> clazz, String typeCode) throws DataSyncException {
		if (dataSyncInSysService != null) {
			try {
				return dataSyncInSysService.customValue(map, clazz, typeCode);
			} catch (Exception e) {
				log.error("{}: 完成资源同步的方法异常", super.getCode(), e);
				throw new DataSyncException(e);
			}
		}
		//
		/*protected boolean customValue(Map<String, Object> map, Class<?> clazz, String typeCode) {
			// 判断是否是组织树的顶层节点同步
			if ("CONTROL_UNIT".equals(typeCode) && "0".equals(map.get("indexCode"))) {
				Object name = map.get("name");
				map.clear();
				map.put("name", name);
			}
			return true;
		}*/
		return true;
	}
	
	@Override
	protected void endSyncByTypeCode(String typeCode, Class<?> clazz, Object... args) {
		if (dataSyncInSysService != null) {
			try {
				dataSyncInSysService.endSyncByTypeCode(this, typeCode, clazz);
			} catch (Exception e) {
				log.error("调用数据同步入库的结果回调方法时异常", e);
			}
		}
	}
	
	@Override
	protected void endSync(List<Notify> notifys, Object... args) {
		if (dataSyncInSysService != null) {
			try {
				dataSyncInSysService.endSync(this, notifys, args);
			} catch (Exception e) {
				log.error("{}: 完成资源同步的方法异常", super.getCode(), e);
			}
		}
	}
	
	@Override
	public void callback(ResultType resultType, DataSyncException e, Object... args) {
		super.setEndTime(System.currentTimeMillis());
		if (dataSyncInSysService != null) {
			try {
				dataSyncInSysService.callback(this, resultType, e, args);
			} catch (Exception ex) {
				log.error("调用数据同步入库的结果回调方法时异常", ex);
			}
		}
	}
	
	protected void deleteByIndexCodesForSync(List<String> indexCodes, Class<?> clazz, String typeCode, Map<String, String> params) throws DataSyncException {
		if (dataSyncInSysService != null) {
			try {
				dataSyncInSysService.deleteByIndexCodesForSync(indexCodes, clazz, typeCode, params);
			} catch (Exception e) {
				log.error("调用数据同步入库的结果回调方法时异常", e);
				throw new DataSyncException(e);
			}
		}
	}
	
	protected int batchUpdate(List<Object> updateList, Class<?> clazz, String typeCode) throws DataSyncException {
		if (dataSyncInSysService != null) {
			try {
				return dataSyncInSysService.batchUpdate(updateList, clazz, typeCode);
			} catch (Exception e) {
				log.error("{}: 完成资源同步的方法异常", super.getCode(), e);
				throw new DataSyncException(e);
			}
		}
		return 0;
	}
	
	protected int batchSave(List<Object> saveList, Class<?> clazz, String typeCode) throws DataSyncException {
		if (dataSyncInSysService != null) {
			try {
				return dataSyncInSysService.batchSave(saveList, clazz, typeCode);
			} catch (Exception e) {
				log.error("{}: 完成资源同步的方法异常", super.getCode(), e);
				throw new DataSyncException(e);
			}
		}
		return 0;
	}
	
	/**
	 * 获取远程的文件URL
	 * @author shanguoming 2014年7月7日 上午10:18:44
	 * @param fileName 文件名
	 * @return
	 */
	public String getRemoteFileUrl(String fileName) {
		String remoteUrl = getRemoteUrl();
		if (null != remoteUrl) {
			remoteUrl = remoteUrl.replace("\\", "/");
			if (!remoteUrl.endsWith("/") && !remoteUrl.endsWith("=")) {
				remoteUrl += "/";
			}
			remoteUrl += DataSyncTools.defaultIfBlank(fileName, "");
			if (remoteUrl.startsWith("http")) {
				if (remoteUrl.indexOf("?") > 0) {
					remoteUrl += "&" + UUID.randomUUID().toString();
				} else {
					remoteUrl += "?" + UUID.randomUUID().toString();
				}
			}
		}
		return remoteUrl;
	}
	
	/**
	 * 获取远程文件的根目录
	 * @author shanguoming 2014年7月7日 上午10:21:22
	 * @param relativePath 相对目录
	 * @return
	 */
	public String getRemoteUrl() {
		return DataSyncTools.defaultIfBlank(super.getPath(), "") + DataSyncTools.defaultIfBlank(super.getRelativePath(), "");
	}
	
	public boolean isForce() {
		return force;
	}
	
	public void setForce(boolean force) {
		this.force = force;
	}
	
	public String getLocalPath() {
		String localDir = this.getClass().getResource("/").getPath() + "sync-in/";
		localDir = localDir.replace("\\", "/");
		return localDir;
	}
	
	public IDataSyncInSysService getDataSyncInSysService() {
		return dataSyncInSysService;
	}
	
	public void setDataSyncInSysService(IDataSyncInSysService dataSyncInSysService) {
		this.dataSyncInSysService = dataSyncInSysService;
	}
}
