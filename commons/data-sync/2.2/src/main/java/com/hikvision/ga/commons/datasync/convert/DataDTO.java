package com.hikvision.ga.commons.datasync.convert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataDTO {
	
	/**
	 * 属性映射
	 */
	private List<DataConvert.Data.Fields.Field> fields = new ArrayList<DataConvert.Data.Fields.Field>();
	/**
	 * 预设参数
	 */
	private Map<String, String> params = new LinkedHashMap<String, String>();
	/**
	 * 属性默认值
	 */
	private Map<String, String> defaultValues = new LinkedHashMap<String, String>();
	/**
	 * 属性更新忽略
	 */
	private Map<String, String> updateExcludes = new LinkedHashMap<String, String>();
	/**
	 * 值更新忽略
	 */
	private Map<String, Set<String>> valueExcludes = new LinkedHashMap<String, Set<String>>();
	/**
	 * 同步类型
	 */
	private String typeCode;
	/**
	 * 映射对象
	 */
	private Class<?> clazz;
	/**
	 * 增量文件名
	 */
	private String fullName;
	/**
	 * 全量文件名
	 */
	private String increName;
	
	/**
	 * 属性映射
	 * @author shanguoming 2014年7月24日 下午3:01:43
	 * @return
	 */
	public List<DataConvert.Data.Fields.Field> getFields() {
		return fields;
	}
	
	public void setFields(List<DataConvert.Data.Fields.Field> fields) {
		if (fields != null && !fields.isEmpty()) {
			this.fields.addAll(fields);
		}
	}
	
	/**
	 * 预设参数
	 * @author shanguoming 2014年7月24日 下午3:01:51
	 * @return
	 */
	public Map<String, String> getParams() {
		return params;
	}
	
	public void putParam(String target, String value) {
		this.params.put(target, value);
	}
	
	/**
	 * 属性默认值
	 * @author shanguoming 2014年7月24日 下午3:01:58
	 * @return
	 */
	public Map<String, String> getDefaultValues() {
		return defaultValues;
	}
	
	public void putDefaultValue(String target, String value) {
		this.defaultValues.put(target, value);
	}
	
	/**
	 * 属性更新忽略
	 * @author shanguoming 2014年7月24日 下午3:02:13
	 * @return
	 */
	public Map<String, String> getUpdateExcludes() {
		return updateExcludes;
	}
	
	public void putUpdateExclude(String target, String model) {
		this.updateExcludes.put(target, model);
	}
	
	/**
	 * 值更新忽略
	 * @author shanguoming 2014年7月24日 下午3:02:24
	 * @return
	 */
	public Map<String, Set<String>> getValueExcludes() {
		return valueExcludes;
	}
	
	public void putValueExclude(String target, String value) {
		Set<String> set = this.valueExcludes.get(target);
		if (set == null) {
			set = new HashSet<String>();
			this.valueExcludes.put(target, set);
		}
		if (value == null || "null".equalsIgnoreCase(value)) {
			set.add(null);
		} else {
			set.add(value);
		}
	}
	
	/**
	 * 同步类型
	 * @author shanguoming 2014年7月24日 下午3:02:28
	 * @return
	 */
	public String getTypeCode() {
		return typeCode;
	}
	
	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}
	
	/**
	 * 映射对象
	 * @author shanguoming 2014年7月24日 下午3:02:33
	 * @return
	 */
	public Class<?> getClazz() {
		return clazz;
	}
	
	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	/**
	 * 全量文件名
	 * @author shanguoming 2014年7月25日 下午12:37:18
	 * @return
	 */
	public String getFullName() {
		return fullName;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	/**
	 * 增量文件名
	 * @author shanguoming 2014年7月25日 下午12:37:24
	 * @return
	 */
	public String getIncreName() {
		return increName;
	}
	
	public void setIncreName(String increName) {
		this.increName = increName;
	}
}
