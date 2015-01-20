package org.nonpolar.commons.nuona.core;

import io.protostuff.WireFormat.FieldType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hamcrest.Description;
import org.nonpolar.commons.nuona.proto.annotation.ProtoBuf;

/**
 * Utility class for generate protobuf IDL content from @{@link Description}
 * @author xiemalin
 * @since 1.0.1
 */
public class ProtobufIDLGenerator {
	
	private static Map<String, String> baseMap = new HashMap<String, String>();
	static {
		baseMap.put("double", "double");
		baseMap.put("float", "float");
		baseMap.put("int", "sint32");
		baseMap.put("long", "sint64");
		baseMap.put("boolean", "bool");
		baseMap.put("string", "string");
		baseMap.put("byte", "bytes");
	}
	
	/**
	 * get IDL content from class.
	 * @param cls
	 *        target protobuf class to parse
	 * @return protobuf IDL content in string
	 */
	public static String getIDL(Class<?> cls) {
		StringBuilder code = new StringBuilder();
		Set<Class<?>> cachedTypes = new HashSet<Class<?>>();
		// define package
		code.append("package ").append(cls.getPackage().getName()).append(";\n");
		// define outer name class
		code.append("option java_outer_classname = \"").append(cls.getSimpleName()).append("$$Pro\";\n");
		cachedTypes.add(cls);
		generateIDL(code, cls, cachedTypes);
		return code.toString();
	}
	
	/**
	 * @param code
	 * @param cls
	 * @return sub message class list
	 */
	private static void generateIDL(StringBuilder code, Class<?> cls, Set<Class<?>> cachedTypes) {
		Set<Class<?>> subTypes = new HashSet<Class<?>>();
		if (null != cls) {
			code.append("message ").append(cls.getSimpleName()).append("{\n");
			do {
				List<Field> fields = findMatchedFields(cls, ProtoBuf.class);
				Set<Integer> orders = new HashSet<Integer>();
				for (Field field : fields) {
					ProtoBuf protobuf = field.getAnnotation(ProtoBuf.class);
					if (null != protobuf) {
						String fieldType = "";
						code.append("// ").append(protobuf.description()).append("\n");
						if (isBaseType(field)) {
							fieldType = baseMap.get(field.getType().getSimpleName().toLowerCase());
							code.append(getFieldRequired(protobuf.required())).append(" ").append(fieldType).append(" ").append(field.getName()).append("=").append(protobuf.order()).append(";\n");
						} else if (isListType(field)) {
							if (protobuf.order() > 0 && !orders.contains(protobuf.order())) {
								code.append(getFieldRequired(protobuf.required())).append(" ").append(fieldType).append(" ").append(field.getName()).append("=").append(protobuf.order()).append(";\n");
								orders.add(protobuf.order());
							} else {
								if (protobuf.order() <= 0) {
									// TODO 日志
								} else {
									// TODO 日志
								}
							}
						} else if (isArrayType(field)) {
						} else if (isEnumType(field)) {
						}
						if (FieldType.MESSAGE == FieldType.MESSAGE) {
							if (isListType(field)) {
								Type type = field.getGenericType();
								if (type instanceof ParameterizedType) {
									ParameterizedType ptype = (ParameterizedType)type;
									Type[] actualTypeArguments = ptype.getActualTypeArguments();
									if (actualTypeArguments != null && actualTypeArguments.length > 0) {
										Type targetType = actualTypeArguments[0];
										if (targetType instanceof Class) {
											Class<?> c = (Class<?>)targetType;
											if (!cachedTypes.contains(c)) {
												cachedTypes.add(c);
												subTypes.add(c);
											}
											code.append("repeated ").append(c.getSimpleName()).append(" ").append(field.getName()).append("=").append(protobuf.order()).append(";\n");
										}
									}
								}
							} else {
								Class<?> c = field.getType();
								code.append(getFieldRequired(protobuf.required())).append(" ").append(c.getSimpleName()).append(" ").append(field.getName()).append("=").append(protobuf.order()).append(";\n");
								if (!cachedTypes.contains(c)) {
									cachedTypes.add(c);
									subTypes.add(c);
								}
							}
						} else {
							code.append(getFieldRequired(protobuf.required())).append(" ").append(fieldType).append(" ").append(field.getName()).append("=").append(protobuf.order()).append(";\n");
						}
					}
				}
				cls = cls.getSuperclass();
			} while (null != cls && cls != Object.class);
		}
		code.append("}\n");
		if (subTypes.isEmpty()) {
			return;
		}
		for (Class<?> subType : subTypes) {
			generateIDL(code, subType, cachedTypes);
		}
	}
	
	/**
	 * @param protobuf
	 * @return
	 */
	private static String getFieldRequired(boolean required) {
		if (required) {
			return "required";
		}
		return "optional";
	}
	
	private static List<Field> findMatchedFields(Class<?> targetClass, Class<ProtoBuf> ann) {
		List<Field> ret = new ArrayList<Field>();
		if (targetClass == null) {
			return ret;
		}
		do {
			Field[] fields = targetClass.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				if (null != fields[i].getAnnotation(ann)) {
					ret.add(fields[i]);
				}
			}
			targetClass = targetClass.getSuperclass();
		} while (null != targetClass && targetClass != Object.class);
		return ret;
	}
	
	/**
	 * 是否list类型
	 * @author shanguoming 2015年1月12日 下午6:38:07
	 * @param field
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月12日 下午6:38:07
	 */
	private static boolean isListType(Field field) {
		Class<?> cls = field.getType();
		if (List.class.isAssignableFrom(cls)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否数组类型
	 * @author shanguoming 2015年1月12日 下午6:38:16
	 * @param field
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月12日 下午6:38:16
	 */
	private static boolean isArrayType(Field field) {
		Class<?> cls = field.getType();
		if (cls.getSimpleName().endsWith("[]")) {
			return true;
		}
		return false;
	}
	
	/**
	 * 死否枚举类型
	 * @author shanguoming 2015年1月12日 下午6:38:35
	 * @param field
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月12日 下午6:38:35
	 */
	private static boolean isEnumType(Field field) {
		Class<?> cls = field.getType();
		if (Enum.class.isAssignableFrom(cls)) {
			return true;
		}
		return false;
	}
	
	private static boolean isBaseType(Field field) {
		Class<?> cls = field.getType();
		if (baseMap.keySet().contains(cls.getSimpleName().toLowerCase())) {
			return true;
		}
		return false;
	}
	
	public static void main(String[] args) {
		System.out.println(FieldType.class.isAssignableFrom(Enum.class));
	}
}
