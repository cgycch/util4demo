package com.cch.common.utils.string;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class StringUtil extends StringUtils {

	private StringUtil() {
	}

	private static final String REG_FILE_NAME = "[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$";
	private static final String REG_FILE_NAME_SPETICAL_CHAR = "[\\s\\\\/:\\*\\?\\\"<>\\|]";

	/**
	 * <b>file name check</b> <br>
	 * not start with blank and not include char( ? * : " < > \ / |)
	 * 
	 * @param fileName
	 * @return
	 */
	public static Boolean isCorrectFileName(String fileName) {
		return fileName.matches(REG_FILE_NAME);
	}

	/**
	 * <b>renovate FileName</b> <br>
	 * replace char( ? * : " < > \ / |) with other
	 * 
	 * @param fileName
	 * @return
	 */
	public static String renovateFileName(String fileName, String replacement) {
		Pattern pattern = Pattern.compile(REG_FILE_NAME_SPETICAL_CHAR);
		Matcher matcher = pattern.matcher(fileName);
		fileName = matcher.replaceAll(replacement); 
		return fileName;
	}

}
