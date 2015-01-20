package com.hikvision.ga.commons.datasync.in;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.hikvision.ga.commons.datasync.convert.DataConvert.Data.Fields.Field;
import com.hikvision.ga.commons.datasync.utils.DataSyncTools;

/**
 * @author shanguoming 2015年1月5日 上午11:12:53
 * @version V1.0
 * @modify: {原因} by shanguoming 2015年1月5日 上午11:12:53
 */
public class DefaultInValueConvert implements IValueConvert<String> {
	
	@Override
	public boolean valueExclude(Set<String> valueExcludes, String val) {
		return (!DataSyncTools.isEmpty(valueExcludes) && valueExcludes.contains(val));
	}
	
	@Override
	public String nullConvert(String val) {
		if (DataSyncTools.equalsIgnoreCase(val, "null")) {
			return null;
		}
		return val;
	}
	
	@Override
	public String mapping(String mapping, String val) {
		if (DataSyncTools.isNotBlank(val) && DataSyncTools.isNotBlank(mapping)) {
			Map<String, String> map = new HashMap<String, String>();
			String[] valArrs = mapping.split(",");
			if (valArrs != null && valArrs.length > 0) {
				for (String valArr : valArrs) {
					if (DataSyncTools.isNotBlank(valArr)) {
						String[] vals = valArr.split(":");
						if (vals != null && vals.length == 2) {
							// 单引号代码空字符串
							if (DataSyncTools.equals(vals[1], "''")) {
								map.put(vals[0], "");
							} else {
								map.put(vals[0], vals[1]);
							}
						}
					}
				}
				if (map.get(val) != null) {
					return map.get(val);
				}
			}
		}
		return val;
	}
	
	@Override
	public String convert(String typeCode, Field field, String val) {
		return val;
	}
}
