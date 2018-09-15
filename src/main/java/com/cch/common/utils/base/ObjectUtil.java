package com.cch.common.utils.base;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.nustaq.serialization.FSTConfiguration;
import org.springframework.beans.BeanUtils;
import org.springframework.core.NamedThreadLocal;

public class ObjectUtil extends ObjectUtils {

	/**
	 * Convert to Double type
	 */
	public static Double toDouble(final Object val) {
		if (val == null) {
			return 0D;
		}
		try {
			return NumberUtils.toDouble(StringUtils.trim(val.toString()));
		} catch (Exception e) {
			return 0D;
		}
	}

	/**
	 * Convert to Float type
	 */
	public static Float toFloat(final Object val) {
		return toDouble(val).floatValue();
	}

	/**
	 * Convert to Integer type
	 */
	public static Long toLong(final Object val) {
		return toDouble(val).longValue();
	}

	/**
	 * 转换为Integer类型
	 */
	public static Integer toInteger(final Object val) {
		return toLong(val).intValue();
	}

	/**
	 * Convert to Boolean type 'true', 'on', 'y', 't', 'yes' or '1' (case insensitive) will return true. Otherwise, false is returned.
	 */
	public static Boolean toBoolean(final Object val) {
		if (val == null) {
			return false;
		}
		return BooleanUtils.toBoolean(val.toString()) || "1".equals(val.toString());
	}

	/**
	 * Convert to string
	 * @param obj
	 * @return
	 */
	public static String toString(final Object obj) {
		return toString(obj, StringUtils.EMPTY);
	}

	/**
	 * If the object is empty, the defaultVal value is used.
	 * @param obj
	 * @param defaultVal
	 * @return
	 */
	public static String toString(final Object obj, final String defaultVal) {
		return obj == null ? defaultVal : obj.toString();
	}

	/**
	 * Empty object to empty string（"" to "" ; null to "" ; "null" to "" ; "NULL" to "" ; "Null" to ""）
	 * @param val Value to be converted
	 * @return 
	 */
	public static String toStringIgnoreNull(final Object val) {
		return ObjectUtil.toStringIgnoreNull(val, StringUtils.EMPTY);
	}

	/**
	 * Empty object to empty string （"" to defaultVal ; null to defaultVal ; "null" to defaultVal ; "NULL" to defaultVal ; "Null" to defaultVal）
	 * @param val Value to be converted
	 * @param defaultVal 
	 * @return 
	 */
	public static String toStringIgnoreNull(final Object val, String defaultVal) {
		String str = ObjectUtil.toString(val);
		return !"".equals(str) && !"null".equals(str.trim().toLowerCase()) ? str : defaultVal;
	}
	
	/**
	 * Copy an object (but the sub object cannot be copied).
	 * @param source
	 * @param ignoreProperties
	 */
	public static Object copyBean(Object source, String... ignoreProperties){
		if (source == null){
			return null;
		}
    	Object target = BeanUtils.instantiateClass(source.getClass());
	    BeanUtils.copyProperties(source, target, ignoreProperties);
	    return target;
	}

	/**
	 * Annotation to object replication, only copy the matching method.
	 * @param annotation
	 * @param object
	 */
	public static void annotationToObject(Object annotation, Object object) {
		if (annotation != null && object != null) {
			Class<?> annotationClass = annotation.getClass();
			Class<?> objectClass = object.getClass();
			for (Method m : objectClass.getMethods()) {
				if (StringUtils.startsWith(m.getName(), "set")) {
					try {
						String s = StringUtils.uncapitalize(StringUtils.substring(m.getName(), 3));
						Object obj = annotationClass.getMethod(s).invoke(annotation);
						if (obj != null && !"".equals(obj.toString())) {
							m.invoke(object, obj);
						}
					} catch (Exception e) {
						// Ignore all settings failed.
					}
				}
			}
		}
	}
	
	/**
	 * serialize an object
	 * @param object
	 * @return
	 */
	public static byte[] serialize(Object object) {
		if (object == null) {
			return null;
		}
		byte[] bytes = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(object);
			bytes = baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bytes;
	}

	/**
	 * unserialize object
	 * @param bytes
	 * @return
	 */
	public static Object unserialize(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		Object object = null;
		try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
				ObjectInputStream ois = new ObjectInputStream(bais)) {
			object = ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return object;
	}
	
	//FST serialization configuration objects
	private static ThreadLocal<FSTConfiguration> fst = new NamedThreadLocal<FSTConfiguration>("FSTConfiguration") {
		public FSTConfiguration initialValue() {
			return FSTConfiguration.createDefaultConfiguration();
		}
	};

	/**
	 * FST serialize Object
	 * @param object
	 * @return
	 */
	public static byte[] serializeFst(Object object) {
		if (object == null){
			return null;
		}
		return fst.get().asByteArray(object);
	}

	/**
	 * FST deserialize an object
	 * @param bytes
	 * @return
	 */
	public static Object unserializeFst(byte[] bytes) {
		if (bytes == null){
			return null;
		}
		return fst.get().asObject(bytes);
	}
	
	/**
	 * Clone an object (complete copy)
	 * @param source
	 */
	public static Object cloneBean(Object source){
		if (source == null){
			return null;
		}
    	byte[] bytes = ObjectUtil.serializeFst(source);
    	Object target = ObjectUtil.unserializeFst(bytes);
	    return target;
	}
	
}

