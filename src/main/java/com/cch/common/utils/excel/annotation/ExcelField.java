package com.cch.common.utils.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel annotation definition
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelField {

	/**
	 * Export field name (by default invokes the "get" method of the current field. 
	 * If you specify that the export field is an object, 
	 * please fill in the "object name. object property",
	 * for example: "user. name", "department. name")
	 */
	String attrName() default "";
	
	/**
	 * Export field titles (to add annotations,
	 * please separate them with "**", Title ** annotations, valid only for export templates)
	 */
	String title();
	
	/**
	 * Field type (0: export & import; 1: export only; 2: import only)
	 */
	Type type() default Type.ALL;
	public enum Type {
		ALL(0),EXPORT(1),IMPORT(2);
		private final int value;
		Type(int value) { this.value = value; }
		public int value() { return this.value; }
	}

	/**
	 * Export field alignment (0: automatic; 1: left; 2: Center; 3: right).
	 */
	Align align() default Align.AUTO;
	public enum Align {
		AUTO(0),LEFT(1),CENTER(2),RIGHT(3);
		private final int value;
		Align(int value) { this.value = value; }
		public int value() { return this.value; }
	}
	
	/**
	 * Specify the width of the derived column (in character width of 1/256, 
	 * if you want to display 5 characters, you can set 5 * 256, 1 character for 2 characters)
	 */
	int width() default -1;
	
	/**
	 * Export field sort (asc)
	 */
	int sort() default 0;
	
	/**
	 * When importing, specify column index (starting from 0) to use 
	 * when specifying the column in Excel.
	 */
	int column() default -1;

	/**
	 * If it is a dictionary type, please set the type value of the dictionary.
	 */
	String dictType() default "";
	
	/**
	 * Reflection type,
	 * <br>MoneyType.class: Amount type conversion (reserved two bits),
	 * <br>DateTimeType.class: Date time type yyyy-MM-dd HH:mm:ss
	 */
	Class<?> fieldType() default Class.class;
	
	/**
	 * schemes (for example: 0.00, YYYY-MM-dd...)
	 */
	String dataFormat() default "@";
	
	/**
	 * Field attribution group (import and export for each business) imp, exp
	 */
	String[] groups() default {};
}
