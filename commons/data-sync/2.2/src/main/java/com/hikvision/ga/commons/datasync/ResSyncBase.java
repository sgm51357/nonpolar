package com.hikvision.ga.commons.datasync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import com.csvreader.CsvReader;
import com.hikvision.ga.commons.datasync.common.UpdateRecoder;
import com.hikvision.ga.commons.datasync.utils.DataSyncTools;

/**
 * 
 * @author fangzhibin 2015年1月7日 下午2:18:46
 * @version V1.0   
 * @modify: {原因} by fangzhibin 2015年1月7日 下午2:18:46
 */
public abstract class ResSyncBase {
	
	/**
	 * 获取本地的更新记录文件
	 * @author shanguoming 2014年6月11日 下午4:00:59
	 * @param updateRecoderFile
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	protected Map<String, UpdateRecoder> getLocalUpdateRecoder(String localPath, String updateRecoderFile, Charset charset) throws IOException {
		Reader reader = null;
		BufferedReader br = null;
		String path = localPath + updateRecoderFile;
		CsvReader csv = null;
		try {
			reader = new InputStreamReader(DataSyncTools.getInputStreamForPath(path), charset);
			br = new BufferedReader(reader);
			csv = new CsvReader(br, ',');
			if (csv.readHeaders()) {
				String[] headers = csv.getHeaders();
				if (null != headers && headers.length > 0) {
					Map<String, UpdateRecoder> updateRecoders = new LinkedHashMap<String, UpdateRecoder>();
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
					return updateRecoders;
				}
			}
		} finally {
			if (null != csv) {
				csv.close();
			}
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(reader);
		}
		return null;
	}
}
