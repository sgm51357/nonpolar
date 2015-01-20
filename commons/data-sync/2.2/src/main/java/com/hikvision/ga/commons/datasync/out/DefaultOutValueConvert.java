package com.hikvision.ga.commons.datasync.out;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.hikvision.ga.commons.datasync.convert.DataConvert.Data.Fields.Field;
import com.hikvision.ga.commons.datasync.in.IValueConvert;
import com.hikvision.ga.commons.datasync.utils.DataSyncTools;

/**
 * @author shanguoming 2015年1月6日 下午5:55:56
 * @version V1.0
 * @modify: {原因} by shanguoming 2015年1月6日 下午5:55:56
 */
public class DefaultOutValueConvert implements IValueConvert<Object> {
	
	/**
	 * @author shanguoming 2015年1月6日 下午5:55:56
	 * @param valueExcludes
	 * @param val
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月6日 下午5:55:56
	 */
	@Override
	public boolean valueExclude(Set<String> valueExcludes, Object val) {
		return (!DataSyncTools.isEmpty(valueExcludes) && valueExcludes.contains(val));
	}
	
	/**
	 * @author shanguoming 2015年1月6日 下午5:55:56
	 * @param val
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月6日 下午5:55:56
	 */
	@Override
	public Object nullConvert(Object val) {
		return val;
	}
	
	/**
	 * @author shanguoming 2015年1月6日 下午5:55:56
	 * @param mapping
	 * @param val
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月6日 下午5:55:56
	 */
	@Override
	public Object mapping(String mapping, Object val) {
		if (null != val && DataSyncTools.isNotBlank(mapping)) {
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
	
	/**
	 * @author shanguoming 2015年1月6日 下午5:55:56
	 * @param typeCode
	 * @param field
	 * @param val
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月6日 下午5:55:56
	 */
	@Override
	public Object convert(String typeCode, Field field, Object val) {
		return val;
	}
}
