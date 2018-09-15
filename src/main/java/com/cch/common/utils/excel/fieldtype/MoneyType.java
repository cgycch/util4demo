package com.cch.common.utils.excel.fieldtype;

import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * Amount type conversion (reserved two bits)
 * @example fieldType = MoneyType.class
 */
public class MoneyType {

	/**
	 * Get object value (import)
	 */
	public static Object getValue(String val) {
		return val == null ? "" : val.replaceAll(",", "");
	}

	/**
	 * Get object value (export)
	 */
	public static String setValue(Object val) {
		NumberFormat nf = new DecimalFormat(",##0.00"); 
		return val == null ? "" : nf.format(val);
	}
	
	/**
	 * Get object value format (export)
	 */
	public static String getDataFormat() {
		return "0.00";
	}
	
	/**
	 * Clean up caching
	 */
	public static void clearCache(){
		
	}
	
}
