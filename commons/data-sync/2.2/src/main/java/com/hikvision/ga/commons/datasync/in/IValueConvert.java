package com.hikvision.ga.commons.datasync.in;

import java.util.Set;
import com.hikvision.ga.commons.datasync.convert.DataConvert.Data.Fields.Field;

/**
 * @author shanguoming 2015年1月5日 上午11:12:25
 * @version V1.0
 * @modify: {原因} by shanguoming 2015年1月5日 上午11:12:25
 */
public interface IValueConvert<T> {
	
	/**
	 * 对同步的原始值进行同步过滤，
	 * @author shanguoming 2015年1月5日 下午2:29:02
	 * @param valueExcludes 过滤条件集合
	 * @param val 原值
	 * @return true：过滤，false：不过滤
	 * @modify: {原因} by shanguoming 2015年1月5日 下午2:29:02
	 */
	boolean valueExclude(Set<String> valueExcludes, T val);
	
	/**
	 * null的字符串转换，如果非null字符串则返回原值
	 * @author shanguoming 2015年1月5日 上午11:23:27
	 * @param val 原值
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月5日 上午11:23:27
	 */
	T nullConvert(T val);
	
	/**
	 * 映射转换
	 * @author shanguoming 2015年1月5日 下午3:46:37
	 * @param mapping 映射关系(默认是逗号进行分组，分号进行key:value)，例：key:value,key:value,key:value
	 * @param val
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月5日 下午3:46:37
	 */
	T mapping(String mapping, T val);
	
	/**
	 * 其他数据转换
	 * @author shanguoming 2015年1月5日 下午4:29:54
	 * @param typeCode
	 * @param field
	 * @param val
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月5日 下午4:29:54
	 */
	T convert(String typeCode, Field field, T val);
}
