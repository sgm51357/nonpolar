package com.hikvision.ga.commons.datasync.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据同步工具类
 * @author shanguoming 2015年1月4日 下午3:29:25
 * @version V1.0
 * @modify: {原因} by shanguoming 2015年1月4日 下午3:29:25
 */
public class DataSyncTools {
	
	private static final Logger log = LoggerFactory.getLogger("data-sync:DataSyncTools");
	/**
	 * Resource path prefix that specifies to load from a classpath location, value is <b>{@code classpath:}</b>
	 */
	public static final String CLASSPATH_PREFIX = "classpath:";
	/**
	 * Resource path prefix that specifies to load from a url location, value is <b>{@code url:}</b>
	 */
	public static final String URL_PREFIX = "url:";
	/**
	 * Resource path prefix that specifies to load from a file location, value is <b>{@code file:}</b>
	 */
	public static final String FILE_PREFIX = "file:";
	public static final String C_TIME_PATTON_DETAIL = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String C_TIME_PATTON_DEFAULT = "yyyy-MM-dd HH:mm:ss";
	public static final String C_DATE_PATTON_DEFAULT = "yyyy-MM-dd";
	public static final String C_DATA_PATTON_YYYYMMDD = "yyyyMMdd";
	public static final String C_DATA_PATTON_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	
	// FIXME------------------------------字符串-----------------------------------
	/**
	 * 判断字符串是否非null且非空字符串
	 * @author shanguoming 2015年1月7日 上午9:55:09
	 * @param str
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月7日 上午9:55:09
	 */
	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}
	
	/**
	 * 判断字符串为null或空字符串
	 * @author shanguoming 2015年1月7日 上午9:55:29
	 * @param str
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月7日 上午9:55:29
	 */
	public static boolean isBlank(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(str.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 当字符串为空字符串或者null时，返回设置的默认值
	 * @author shanguoming 2015年1月7日 上午9:58:21
	 * @param str
	 * @param defaultStr
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月7日 上午9:58:21
	 */
	public static String defaultIfBlank(String str, String defaultStr) {
		return isBlank(str)?defaultStr:str;
	}
	
	/**
	 * 字符串比较，忽略大小写
	 * @author shanguoming 2015年1月7日 上午10:05:04
	 * @param str1
	 * @param str2
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月7日 上午10:05:04
	 */
	public static boolean equalsIgnoreCase(String str1, String str2) {
		return str1 == null?str2 == null:str1.equalsIgnoreCase(str2);
	}
	
	/**
	 * 字符串比较，区分大小写
	 * @author shanguoming 2015年1月7日 上午10:08:28
	 * @param str1
	 * @param str2
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月7日 上午10:08:28
	 */
	public static boolean equals(String str1, String str2) {
		return str1 == null?str2 == null:str1.equals(str2);
	}
	
	// FIXME------------------------------集合-----------------------------------
	/**
	 * 判断集合对象是否为空，或者Map对象中没有数据
	 * @author shanguoming 2014年12月31日 上午9:59:52
	 * @param coll
	 * @return
	 * @modify: {原因} by shanguoming 2014年12月31日 上午9:59:52
	 */
	public static boolean isEmpty(Collection<?> coll) {
		return (coll == null || coll.isEmpty());
	}
	
	/**
	 * 判断Map对象是否为空，或者Map对象中没有数据
	 * @author shanguoming 2014年12月31日 上午9:59:44
	 * @param map
	 * @return
	 * @modify: {原因} by shanguoming 2014年12月31日 上午9:59:44
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return (map == null || map.isEmpty());
	}
	
	// FIXME------------------------------数字-----------------------------------
	/**
	 * 判断两Integer值是否相等，双方都为空值时也认为相等
	 * @param int1 数值1
	 * @param int2 数值2
	 * @return 是否匹配
	 */
	public static boolean isEqual(Integer int1, Integer int2) {
		if (int1 == null && int2 == null) {
			return true;
		}
		if (int1 != null && int2 != null) {
			if (int1.intValue() == int2.intValue()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 比较Long值是否相等,同为null也表示相等
	 * @author shanguoming 2012-4-23 下午4:07:16
	 * @param long1
	 * @param long2
	 * @return
	 */
	public static boolean isEqual(Long long1, Long long2) {
		if (long1 == null && long2 == null) {
			return true;
		} else if (long1 != null && long2 != null) {
			if (long1.longValue() == long2.longValue()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 如果Short类型等于null则返回0,否则返回本身的short值
	 * @author shanguoming 2011-11-18 上午11:40:42
	 * @param num 需判断的数值
	 * @return short值
	 */
	public static short null2zero(Short s) {
		short num = 0;
		if (s != null) {
			num = s.shortValue();
		}
		return num;
	}
	
	/**
	 * 如果Integer类型等于null则返回0,否则返回本身的int值
	 * @author shanguoming 2011-11-18 上午11:40:42
	 * @param integer 需判断的数值
	 * @return int值
	 */
	public static int null2zero(Integer integer) {
		int num = 0;
		if (integer != null) {
			num = integer.intValue();
		}
		return num;
	}
	
	/**
	 * 如果Long类型等于null则返回0,否则返回本身的long值
	 * @author shanguoming 2011-11-18 上午11:40:42
	 * @param num 需判断的数值
	 * @return int值
	 */
	public static long null2zero(Long l) {
		long num = 0;
		if (l != null) {
			num = l.longValue();
		}
		return num;
	}
	
	/**
	 * 字符串转int，非int字符串统一转成0
	 * @author shanguoming 2015年1月7日 上午10:11:32
	 * @param str
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月7日 上午10:11:32
	 */
	public static int toInt(String str) {
		return toInt(str, 0);
	}
	
	/**
	 * 字符串转int，非int字符串统一转成defaultValue
	 * @author shanguoming 2015年1月7日 上午10:11:32
	 * @param str
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月7日 上午10:11:32
	 */
	public static int toInt(String str, int defaultValue) {
		if (str == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}
	
	/**
	 * 字符串转long，非long字符串统一转成0
	 * @author shanguoming 2015年1月7日 上午10:11:32
	 * @param str
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月7日 上午10:11:32
	 */
	public static long toLong(String str) {
		return toLong(str, 0L);
	}
	
	/**
	 * 字符串转long，非long字符串统一转成defaultValue
	 * @author shanguoming 2015年1月7日 上午10:11:32
	 * @param str
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月7日 上午10:11:32
	 */
	public static long toLong(String str, long defaultValue) {
		if (str == null) {
			return defaultValue;
		}
		try {
			return Long.parseLong(str);
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}
	
	/**
	 * 字符串转float，非float字符串统一转成0
	 * @author shanguoming 2015年1月7日 上午10:11:32
	 * @param str
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月7日 上午10:11:32
	 */
	public static float toFloat(String str) {
		return toFloat(str, 0.0f);
	}
	
	/**
	 * 字符串转float，非float字符串统一转成defaultValue
	 * @author shanguoming 2015年1月7日 上午10:11:32
	 * @param str
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月7日 上午10:11:32
	 */
	public static float toFloat(String str, float defaultValue) {
		if (str == null) {
			return defaultValue;
		}
		try {
			return Float.parseFloat(str);
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}
	
	/**
	 * 字符串转double，非double字符串统一转成0
	 * @author shanguoming 2015年1月7日 上午10:11:32
	 * @param str
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月7日 上午10:11:32
	 */
	public static double toDouble(String str) {
		return toDouble(str, 0.0d);
	}
	
	/**
	 * 字符串转double，非double字符串统一转成defaultValue
	 * @author shanguoming 2015年1月7日 上午10:11:32
	 * @param str
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月7日 上午10:11:32
	 */
	public static double toDouble(String str, double defaultValue) {
		if (str == null) {
			return defaultValue;
		}
		try {
			return Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}
	
	/**
	 * 判断字符串是否数字
	 * @author shanguoming 2015年1月7日 上午10:13:36
	 * @param str
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月7日 上午10:13:36
	 */
	public static boolean isNumber(String str) {
		if (isBlank(str)) {
			return false;
		}
		char[] chars = str.toCharArray();
		int sz = chars.length;
		boolean hasExp = false;
		boolean hasDecPoint = false;
		boolean allowSigns = false;
		boolean foundDigit = false;
		// deal with any possible sign up front
		int start = (chars[0] == '-')?1:0;
		if (sz > start + 1) {
			if (chars[start] == '0' && chars[start + 1] == 'x') {
				int i = start + 2;
				if (i == sz) {
					return false; // str == "0x"
				}
				// checking hex (it can't be anything else)
				for (; i < chars.length; i++) {
					if ((chars[i] < '0' || chars[i] > '9') && (chars[i] < 'a' || chars[i] > 'f') && (chars[i] < 'A' || chars[i] > 'F')) {
						return false;
					}
				}
				return true;
			}
		}
		sz--; // don't want to loop to the last char, check it afterwords
		      // for type qualifiers
		int i = start;
		// loop to the next to last char or to the last char if we need another digit to
		// make a valid number (e.g. chars[0..5] = "1234E")
		while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
			if (chars[i] >= '0' && chars[i] <= '9') {
				foundDigit = true;
				allowSigns = false;
			} else if (chars[i] == '.') {
				if (hasDecPoint || hasExp) {
					// two decimal points or dec in exponent
					return false;
				}
				hasDecPoint = true;
			} else if (chars[i] == 'e' || chars[i] == 'E') {
				// we've already taken care of hex.
				if (hasExp) {
					// two E's
					return false;
				}
				if (!foundDigit) {
					return false;
				}
				hasExp = true;
				allowSigns = true;
			} else if (chars[i] == '+' || chars[i] == '-') {
				if (!allowSigns) {
					return false;
				}
				allowSigns = false;
				foundDigit = false; // we need a digit after the E
			} else {
				return false;
			}
			i++;
		}
		if (i < chars.length) {
			if (chars[i] >= '0' && chars[i] <= '9') {
				// no type qualifier, OK
				return true;
			}
			if (chars[i] == 'e' || chars[i] == 'E') {
				// can't have an E at the last byte
				return false;
			}
			if (chars[i] == '.') {
				if (hasDecPoint || hasExp) {
					// two decimal points or dec in exponent
					return false;
				}
				// single trailing decimal point after non-exponent is ok
				return foundDigit;
			}
			if (!allowSigns && (chars[i] == 'd' || chars[i] == 'D' || chars[i] == 'f' || chars[i] == 'F')) {
				return foundDigit;
			}
			if (chars[i] == 'l' || chars[i] == 'L') {
				// not allowing L with an exponent
				return foundDigit && !hasExp;
			}
			// last character is illegal
			return false;
		}
		// allowSigns is true iff the val ends in 'E'
		// found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
		return !allowSigns && foundDigit;
	}
	
	// FIXME------------------------------xml转bean-----------------------------------
	/**
	 * xml转换成JavaBean
	 * @author shanguoming 2014年6月5日 下午3:49:26
	 * @param xml
	 * @param clazz
	 * @return
	 * @throws JAXBException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T converyToJavaBean(String xml, Class<T> clazz) throws JAXBException {
		T t = null;
		JAXBContext context = JAXBContext.newInstance(clazz);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		t = (T)unmarshaller.unmarshal(new StringReader(xml));
		return t;
	}
	
	// FIXME------------------------------map与bean之间转换-----------------------------------
	/**
	 * bean对象转map对象,如果bean为null则返回一个空的map
	 * <p>
	 * <li>BeanUtil.convertToMap(null)=new HashMap<String, Object>();</li>
	 * </p>
	 * @author shanguoming 2014年5月10日 下午2:07:30
	 * @param bean 含可读属性的对象（即，属性有public的getXXX()方法）
	 * @return HashMap
	 * @throws Exception
	 */
	public static Map<String, Object> convertToMap(Object bean) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		if (bean != null) {
			Class<?> type = bean.getClass();
			BeanInfo beanInfo = null;;
			try {
				beanInfo = Introspector.getBeanInfo(type);
			} catch (IntrospectionException e) {
				log.warn("convertToMap：[{}]转map时根据对象类型无法获取类属性异常", type.getName());
				throw e;
			}
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
			for (int i = 0; i < propertyDescriptors.length; i++) {
				PropertyDescriptor descriptor = propertyDescriptors[i];
				if (descriptor == null) {
					continue;
				}
				Method readMethod = descriptor.getReadMethod();
				if (readMethod == null) {
					continue;
				}
				Object result = null;
				try {
					result = readMethod.invoke(bean, new Object[0]);
				} catch (IllegalAccessException e) {
					log.warn("convertToMap：[{}]转map时调用[{}]方法访问权限异常", type.getName(), readMethod.getName());
					throw e;
				} catch (IllegalArgumentException e) {
					log.warn("convertToMap：[{}]转map时调用[{}]方法参数异常", type.getName(), readMethod.getName());
					throw e;
				} catch (InvocationTargetException e) {
					log.warn("convertToMap：[{}]转map时调用[{}]方法内部异常", type.getName(), readMethod.getName());
					throw e;
				}
				String propertyName = descriptor.getName();
				if (result != null) {
					returnMap.put(propertyName, result);
				} else {
					returnMap.put(propertyName, null);
				}
			}
		}
		return returnMap;
	}
	
	/**
	 * map转bean对象
	 * <p>
	 * <li>BeanUtil.convertToBean(null,map)=null</li>
	 * <li>BeanUtil.convertToBean(null,null)=null</li>
	 * <li>BeanUtil.convertToBean(Class<?>,null)=new bean()</li>
	 * <li>BeanUtil.convertToBean(Class<?>,map)=new bean()</li>
	 * </p>
	 * @author shanguoming 2014年7月4日 上午9:13:16
	 * @param type
	 * @param map 支持简单类型的相互转换，
	 * @return 如果type为null则返回null
	 * @throws Exception
	 */
	public static <T> T convertToBean(Class<T> type, Map<String, Object> map) throws Exception {
		T obj = null;
		if (type != null) {
			BeanInfo beanInfo = null;;
			try {
				// 获取类属性
				beanInfo = Introspector.getBeanInfo(type);
			} catch (IntrospectionException e) {
				log.warn("convertToBean：map转[{}]时根据对象类型无法获取类属性异常", type.getName());
				throw e;
			}
			try {
				// 创建 JavaBean 对象
				obj = type.newInstance();
			} catch (InstantiationException e) {
				log.warn("convertToBean：map转[{}]时根据对象类型实例化异常", type.getName());
				throw e;
			} catch (IllegalAccessException e) {
				log.warn("convertToBean：map转[{}]时根据对象类型实例化未找到默认构造函数异常", type.getName());
				throw e;
			}
			if (map == null || map.isEmpty()) {
				return obj;
			}
			// 给 JavaBean 对象的属性赋值
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
			for (int i = 0; i < propertyDescriptors.length; i++) {
				PropertyDescriptor descriptor = propertyDescriptors[i];
				String propertyName = descriptor.getName();
				if (map.containsKey(propertyName)) {
					Class<?> propertyType = descriptor.getPropertyType();
					Object value = map.get(propertyName);
					Object[] args = new Object[1];
					String v = value == null?null:value.toString();
					if (equals(propertyType.getSimpleName(), "int")) {
						args[0] = isBlank(v)?0:Integer.parseInt(v);
					} else if (equals(propertyType.getSimpleName(), "double")) {
						args[0] = isBlank(v)?0.0d:Double.parseDouble(v);
					} else if (equals(propertyType.getSimpleName(), "float")) {
						args[0] = isBlank(v)?0.0f:Float.parseFloat(v);
					} else if (equals(propertyType.getSimpleName(), "short")) {
						args[0] = isBlank(v)?(short)0:Short.parseShort(v);
					} else if (equals(propertyType.getSimpleName(), "long")) {
						args[0] = isBlank(v)?0l:Long.parseLong(v);
					} else if (equals(propertyType.getSimpleName(), "byte")) {
						args[0] = isBlank(v)?(byte)0:Byte.parseByte(v);
					} else if (equals(propertyType.getSimpleName(), "char")) {
						args[0] = v.charAt(0);
					} else if (equals(propertyType.getSimpleName(), "boolean")) {
						args[0] = isBlank(v)?false:Boolean.parseBoolean(v);
					} else if (value != null) {
						if (equals(value.getClass().getName(), descriptor.getPropertyType().getName())) {
							args[0] = value;
						} else {
							if (equals(propertyType.getName(), Integer.class.getName())) {
								if (isNotBlank(v)) {
									args[0] = Integer.decode(v);
								} else {
									args[0] = null;
								}
							} else if (equals(propertyType.getName(), Double.class.getName())) {
								if (isNotBlank(v)) {
									args[0] = Double.valueOf(v);
								} else {
									args[0] = null;
								}
							} else if (equals(propertyType.getName(), Float.class.getName())) {
								if (isNotBlank(v)) {
									args[0] = Float.valueOf(v);
								} else {
									args[0] = null;
								}
							} else if (equals(propertyType.getName(), Short.class.getName())) {
								if (isNotBlank(v)) {
									args[0] = Short.parseShort(v);
								} else {
									args[0] = null;
								}
							} else if (equals(propertyType.getName(), Long.class.getName())) {
								if (isNotBlank(v)) {
									args[0] = Long.valueOf(v);
								} else {
									args[0] = null;
								}
							} else if (equals(propertyType.getName(), Byte.class.getName())) {
								if (isNotBlank(v)) {
									args[0] = Byte.parseByte(v);
								} else {
									args[0] = null;
								}
							} else if (equals(propertyType.getName(), Character.class.getName())) {
								if (isNotBlank(v)) {
									args[0] = v.charAt(0);;
								} else {
									args[0] = null;
								}
							} else if (equals(propertyType.getName(), Boolean.class.getName())) {
								if (isNotBlank(v)) {
									args[0] = Boolean.parseBoolean(v);
								} else {
									args[0] = null;
								}
							} else if (equals(propertyType.getName(), String.class.getName())) {
								args[0] = defaultIfBlank(value.toString(), "");
							} else {
								Constructor<?> constructor = descriptor.getPropertyType().getConstructor(propertyType);
								args[0] = constructor.newInstance(value);
							}
						}
					} else {
						args[0] = null;
					}
					Method method = descriptor.getWriteMethod();
					if (method != null) {
						try {
							// 属性赋值
							method.invoke(obj, args);
						} catch (IllegalAccessException e) {
							log.warn("map转[{}]时调用[{}]方法访问权限异常", type.getName(), method.getName());
							throw e;
						} catch (IllegalArgumentException e) {
							log.warn("map转[{}]时调用[{}]方法参数异常", type.getName(), method.getName());
							throw e;
						} catch (InvocationTargetException e) {
							log.warn("map转[{}]时调用[{}]方法内部异常", type.getName(), method.getName());
							throw e;
						}
					}
				}
			}
		}
		return obj;
	}
	
	// FIXME------------------------------array操作-----------------------------------
	/**
	 * 获取指定位置的Object
	 * @author shanguoming 2014年7月4日 下午3:05:53
	 * @param array
	 * @param index
	 * @return
	 */
	public static Object get(final Object[] array, int index) {
		if (array != null && array.length > index) {
			return array[index];
		}
		return null;
	}
	
	/**
	 * 获取指定位置的String
	 * @author shanguoming 2014年7月4日 下午3:05:53
	 * @param array
	 * @param index
	 * @return
	 */
	public static String get(final String[] array, int index) {
		if (array != null && array.length > index) {
			return array[index];
		}
		return null;
	}

	// FIXME------------------------------array操作-----------------------------------
	/**
	 * 根据文件地址，读取文件流
	 * @author shanguoming 2015年1月7日 上午10:24:50
	 * @param resourcePath
	 * @return
	 * @throws IOException
	 * @modify: {原因} by shanguoming 2015年1月7日 上午10:24:50
	 */
	public static InputStream getInputStreamForPath(String resourcePath) throws IOException {
		InputStream is;
		if (resourcePath.startsWith(CLASSPATH_PREFIX)) {
			is = loadFromClassPath(stripPrefix(resourcePath));
		} else if (resourcePath.startsWith(URL_PREFIX)) {
			is = loadFromUrl(stripPrefix(resourcePath));
		} else if (resourcePath.startsWith(FILE_PREFIX)) {
			is = loadFromFile(stripPrefix(resourcePath));
		} else {
			is = loadFromFile(resourcePath);
		}
		if (is == null) {
			throw new IOException("Resource [" + resourcePath + "] could not be found.");
		}
		return is;
	}
	
	private static InputStream loadFromFile(String path) throws IOException {
		log.debug("Opening file [{}]...", path);
		return new FileInputStream(path);
	}
	
	private static InputStream loadFromUrl(String urlPath) throws IOException {
		log.debug("Opening url {}", urlPath);
		URL url = new URL(urlPath);
		return url.openStream();
	}
	
	private static InputStream loadFromClassPath(String path) {
		log.debug("Opening resource from class path [{}]", path);
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream is = loader.getResourceAsStream(path);
		if (is == null) {
			log.trace("Resource [{}] was not found via the thread context ClassLoader. " + "  Trying the current ClassLoader...", path);
			loader = DataSyncTools.class.getClassLoader();
			is = loader.getResourceAsStream(path);
		}
		if (is == null) {
			log.trace("Resource [{}] was not found via the current class loader.  Trying the " + "system/application ClassLoader...", path);
			loader = ClassLoader.getSystemClassLoader();
			is = loader.getResourceAsStream(path);
		}
		if (is == null) {
			log.trace("Resource [{}] was not found via the thread context, current, " + "or system/application ClassLoaders.  All heuristics have been exhausted.  Returning null.", path);
		}
		return is;
	}
	
	private static String stripPrefix(String resourcePath) {
		return resourcePath.substring(resourcePath.indexOf(":") + 1);
	}
	
	// FIXME------------------------------日期字符串转换操作-----------------------------------

	/**
	 * 根据日期输出格式要求，输出当前时间的字符串
	 * @author shanguoming 2015年1月7日 上午10:44:04
	 * @param strFormat
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月7日 上午10:44:04
	 */
	public static String getCurrentDateStr(String strFormat) {
		Calendar cal = Calendar.getInstance();
		Date currDate = cal.getTime();
		return format(currDate, strFormat);
	}
	
	/**
	 * 将Date类型的日期转换为系统参数定义的格式的字符串。
	 * @param aTs_Datetime
	 * @param as_Pattern
	 * @return
	 */
	public static String format(Date aTs_Datetime, String as_Pattern) {
		if (aTs_Datetime == null || as_Pattern == null) return null;
		SimpleDateFormat dateFromat = new SimpleDateFormat();
		dateFromat.applyPattern(as_Pattern);
		return dateFromat.format(aTs_Datetime);
	}
}
