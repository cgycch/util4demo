package com.cch.common.utils.refect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.DateUtil;

import com.cch.common.utils.base.ObjectUtils;
import com.cch.common.utils.date.DateUtils;

/**
 * Reflection tool class. Provides tool functions such as calling getter /
 * setter methods, accessing private variables, calling private methods, getting
 * generic types of Class, real classes that have been AOP-enabled.
 */
@SuppressWarnings("rawtypes")
public class ReflectUtils {

	private static final String SETTER_PREFIX = "set";

	private static final String GETTER_PREFIX = "get";

	private static final String CGLIB_CLASS_SEPARATOR = "$$";

	private static Logger logger = Logger.getLogger(ReflectUtils.class);

	/**
	 * Invoke the Getter method. Support for multilevel, such as object name. object
	 * name. method.
	 */
	public static Object invokeGetter(Object obj, String propertyName) {
		Object object = obj;
		for (String name : StringUtils.split(propertyName, ".")) {
			String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(name);
			object = invokeMethod(object, getterMethodName, new Class[] {}, new Object[] {});
		}
		return object;
	}

	/**
	 * Invoke the Setter method, only matching the name of the method. Support for
	 * multilevel, such as object name.object name.method.
	 */
	public static void invokeSetter(Object obj, String propertyName, Object value) {
		Object object = obj;
		String[] names = StringUtils.split(propertyName, ".");
		for (int i = 0; i < names.length; i++) {
			if (i < names.length - 1) {
				String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(names[i]);
				object = invokeMethod(object, getterMethodName, new Class[] {}, new Object[] {});
			} else {
				String setterMethodName = SETTER_PREFIX + StringUtils.capitalize(names[i]);
				invokeMethodByName(object, setterMethodName, new Object[] { value });
			}
		}
	}

	/**
	 * Read object attribute values directly, ignore private/protected modifier, do not pass getter function.
	 */
	public static Object getFieldValue(final Object obj, final String fieldName) {
		Field field = getAccessibleField(obj, fieldName);
		if (field == null) {
			logger.warn("in [" + obj.getClass() + "] ，could not found field [" + fieldName + "]");
			return null;
		}
		Object result = null;
		try {
			result = field.get(obj);
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage());
		}
		return result;
	}

	/**
	 *Set object attribute value directly, ignore private/protected modifier, do not pass setter function.
	 */
	public static void setFieldValue(final Object obj, final String fieldName, final Object value) {
		Field field = getAccessibleField(obj, fieldName);
		if (field == null) {
			logger.warn("in [" + obj.getClass() + "] ，could not found field [" + fieldName + "]");
			return;
		}
		try {
			field.set(obj, value);
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Call the object method directly, ignoring the private/protected modifier.
	 * In the case of one-time calls, otherwise the getAccessibleMethod () function 
	 * should be used to get the method and call it repeatedly. 
	 * The method name + parameter type should be matched.
	 */
	public static Object invokeMethod(final Object obj, final String methodName, final Class<?>[] parameterTypes,
			final Object[] args) {
		if (obj == null || methodName == null) {
			return null;
		}
		Method method = getAccessibleMethod(obj, methodName, parameterTypes);
		if (method == null) {
			logger.warn("in [" + obj.getClass() + "] ，could not found method [" + methodName + "]");
			return null;
		}
		try {
			return method.invoke(obj, args);
		} catch (Exception e) {
			String msg = "method: " + method + ", obj: " + obj + ", args: " + args + "";
			throw convertReflectionExceptionToUnchecked(msg, e);
		}
	}

	/**
	 * Call the object method directly, ignoring the private/protected modifier.
	 * For one-time calls, otherwise you should use the getAccessibleMethodByName () function to get the Method 
	 * and call it repeatedly.
	 * Only match the function name, if there are multiple namesake functions, call the first one.
	 */
	public static Object invokeMethodByName(final Object obj, final String methodName, final Object[] args) {
		Method method = getAccessibleMethodByName(obj, methodName, args.length);
		if (method == null) {
			logger.warn("in [" + obj.getClass() + "] ，could not found method [" + methodName + "] ");
			return null;
		}
		try {
			// Type conversion (converting the parameter data type to the target method parameter type)
			Class<?>[] cs = method.getParameterTypes();
			for (int i = 0; i < cs.length; i++) {
				if (args[i] != null && !args[i].getClass().equals(cs[i])) {
					if (cs[i] == String.class) {
						args[i] = ObjectUtils.toString(args[i]);
						if (StringUtils.endsWith((String) args[i], ".0")) {
							args[i] = StringUtils.substringBefore((String) args[i], ".0");
						}
					} else if (cs[i] == Integer.class) {
						args[i] = ObjectUtils.toInteger(args[i]);
					} else if (cs[i] == Long.class) {
						args[i] = ObjectUtils.toLong(args[i]);
					} else if (cs[i] == Double.class) {
						args[i] = ObjectUtils.toDouble(args[i]);
					} else if (cs[i] == Float.class) {
						args[i] = ObjectUtils.toFloat(args[i]);
					} else if (cs[i] == Date.class) {
						if (args[i] instanceof String) {
							args[i] = DateUtils.parseDate(args[i]);
						} else {
							// POI Excel Date format conversion
							args[i] = DateUtil.getJavaDate((Double) args[i]);
						}
					}
				}
			}
			return method.invoke(obj, args);
		} catch (Exception e) {
			String msg = "method: " + method + ", obj: " + obj + ", args: " + args + "";
			throw convertReflectionExceptionToUnchecked(msg, e);
		}
	}

	/**
	 * Cyclically transitions up, gets the object's Declared Field, and forces it to be accessible.
	 * If the upward transition to Object is still not found, returns null.
	 */
	public static Field getAccessibleField(final Object obj, final String fieldName) {
		if (obj == null) {
			return null;
		}
		Validate.notBlank(fieldName, "fieldName can't be blank");
		for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
			try {
				Field field = superClass.getDeclaredField(fieldName);
				makeAccessible(field);
				return field;
			} catch (NoSuchFieldException e) {
				//Field is not in the current class definition, and continues upward transformation.
				continue;
			}
		}
		return null;
	}

	/**
	 * Loop up transition, get the Declared Method of the object, and force it to be accessible.
	 * If the object can't be found, return null. Match function name + parameter type.
	 * For cases where a method needs to be called many times.
 	 * Use this function to get Method first and then invoke Method. invoke (Object obj, Object... args)
	 */
	public static Method getAccessibleMethod(final Object obj, final String methodName, final Class<?>... parameterTypes) {
		if (obj == null) {
			return null;
		}
		Validate.notBlank(methodName, "methodName can't be blank");
		for (Class<?> searchType = obj.getClass(); searchType != Object.class; searchType = searchType.getSuperclass()) {
			try {
				Method method = searchType.getDeclaredMethod(methodName, parameterTypes);
				makeAccessible(method);
				return method;
			} catch (NoSuchMethodException e) {
				// Method is not in the current class definition, and continues upward transformation.
				continue;
			}
		}
		return null;
	}

	/**
	 * Loop up transition, get the Declared Method of the object, and force it to be accessible.
	 * If the object can't be found, return null. Match function name + parameter type.
	 * For cases where a method needs to be called many times.
 	 * Use this function to get Method first and then invoke Method. invoke (Object obj, Object... args)
	 */
	public static Method getAccessibleMethodByName(final Object obj, final String methodName, int argsNum) {
		if (obj == null) {
			return null;
		}
		Validate.notBlank(methodName, "methodName can't be blank");
		for (Class<?> searchType = obj.getClass(); searchType != Object.class; searchType = searchType
				.getSuperclass()) {
			Method[] methods = searchType.getDeclaredMethods();
			for (Method method : methods) {
				if (method.getName().equals(methodName) && method.getParameterTypes().length == argsNum) {
					makeAccessible(method);
					return method;
				}
			}
		}
		return null;
	}

	/**
	 * Change the private / protected method to public, 
	 * try not to invoke the actually changed statements, 
	 * and avoid JDK's SecurityManager complaints.
	 */
	public static void makeAccessible(Method method) {
		if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
				&& !method.isAccessible()) {
			method.setAccessible(true);
		}
	}

	/**
	 * Change the private / protected method to public, 
	 * try not to invoke the actually changed statements, 
	 * and avoid JDK's SecurityManager complaints.
	 */
	public static void makeAccessible(Field field) {
		if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
				|| Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
			field.setAccessible(true);
		}
	}

	/**
	 * Get the type of the generic parameter declared in the Class definition by reflection, 
	 * noting that the generic must be defined at the parent class and return Object.class if not found. 
	 * eg. public UserDao extends HibernateDao<User>
	 * @param clazz
	 *            The class to introspect
	 * @return the first generic declaration, or Object.class if cannot be
	 *         determined
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getClassGenricType(final Class clazz) {
		return getClassGenricType(clazz, 0);
	}

	/**
	 * Get the type of the generic parameter declared in the Class definition by reflection, 
	 * noting that the generic must be defined at the parent class and return Object.class if not found. 
	 * public UserDao extends HibernateDao<User,Long>
	 * 
	 * @param clazz
	 *            clazz The class to introspect
	 * @param index
	 *            the Index of the generic ddeclaration,start from 0.
	 * @return the index generic declaration, or Object.class if cannot be
	 *         determined
	 */
	public static Class getClassGenricType(final Class clazz, final int index) {

		Type genType = clazz.getGenericSuperclass();

		if (!(genType instanceof ParameterizedType)) {
			logger.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
			return Object.class;
		}

		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

		if (index >= params.length || index < 0) {
			logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
					+ params.length);
			return Object.class;
		}
		if (!(params[index] instanceof Class)) {
			logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
			return Object.class;
		}

		return (Class) params[index];
	}

	public static Class<?> getUserClass(Object instance) {
		if (instance == null) {
			throw new RuntimeException("Instance must not be null");
		}
		Class clazz = instance.getClass();
		if (clazz != null && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass != null && !Object.class.equals(superClass)) {
				return superClass;
			}
		}
		return clazz;

	}

	/**
	 * Convert checked exception to unchecked exception. when reflected.
	 */
	public static RuntimeException convertReflectionExceptionToUnchecked(String msg, Exception e) {
		if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException
				|| e instanceof NoSuchMethodException) {
			return new IllegalArgumentException(msg, e);
		} else if (e instanceof InvocationTargetException) {
			return new RuntimeException(msg, ((InvocationTargetException) e).getTargetException());
		}
		return new RuntimeException(msg, e);
	}
}
