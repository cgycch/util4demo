package com.cch.common.utils.date;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * Date tool class, extend from org.apache.commons.lang.time.DateUtils
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

	private static String[] parsePatterns = { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH",
			"yyyy-MM", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM/dd HH", "yyyy/MM",
			"yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM.dd HH", "yyyy.MM", "yyyy年MM月dd日",
			"yyyy年MM月dd日 HH时mm分ss秒", "yyyy年MM月dd日 HH时mm分", "yyyy年MM月dd日 HH时", "yyyy年MM月", "yyyy" };

	/**
	 * Get date string, conversion format (yyyy-MM-dd)
	 */
	public static String formatDate(Date date) {
		return formatDate(date, "yyyy-MM-dd");
	}

	/**
	 * Get the date string default format (yyyy-MM-dd), pattern can be:
	 * "yyyy-MM-dd", "HH:mm:ss", "E".
	 */
	public static String formatDate(long dateTime, String pattern) {
		return formatDate(new Date(dateTime), pattern);
	}

	/**
	 * Get the date string default format (yyyy-MM-dd), pattern can be:
	 * "yyyy-MM-dd", "HH:mm:ss", "E".
	 */
	public static String formatDate(Date date, String pattern) {
		String formatDate = null;
		if (date != null) {
			if (StringUtils.isBlank(pattern)) {
				pattern = "yyyy-MM-dd";
			}
			formatDate = FastDateFormat.getInstance(pattern).format(date);
		}
		return formatDate;
	}

	/**
	 * Get the date time string, （yyyy-MM-dd HH:mm:ss）
	 */
	public static String formatDateTime(Date date) {
		return formatDate(date, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * Gets the current date string format (yyyy-MM-dd).
	 */
	public static String getDate() {
		return getDate("yyyy-MM-dd");
	}

	/**
	 * Gets the current date string format (yyyy-MM-dd) pattern that can be:
	 * "yyyy-MM-dd", "HH:mm:ss", "E".
	 */
	public static String getDate(String pattern) {
		return FastDateFormat.getInstance(pattern).format(new Date());
	}

	/**
	 * get a date string before/after current time
	 * 
	 * @param pattern
	 *            farmat（yyyy-MM-dd） pattern can be："yyyy-MM-dd" "HH:mm:ss" "E"
	 * @param amont
	 *            offect，negative is before
	 * @param type
	 *            Calendar type(Calendar.HOUR、Calendar.MINUTE、Calendar.SECOND)
	 * @return
	 */
	public static String getDate(String pattern, int amont, int type) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(type, amont);
		return FastDateFormat.getInstance(pattern).format(calendar.getTime());
	}

	/**
	 * Get the current time string format（HH:mm:ss）
	 */
	public static String getTime() {
		return formatDate(new Date(), "HH:mm:ss");
	}

	/**
	 * Gets the current date and time string（yyyy-MM-dd HH:mm:ss）
	 */
	public static String getDateTime() {
		return formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * Gets the current year string（yyyy）
	 */
	public static String getYear() {
		return formatDate(new Date(), "yyyy");
	}

	/**
	 * Gets the current month string format（MM）
	 */
	public static String getMonth() {
		return formatDate(new Date(), "MM");
	}

	/**
	 * Get the string of the day（dd）
	 */
	public static String getDay() {
		return formatDate(new Date(), "dd");
	}

	/**
	 * Get the current week string format (E) weeks.
	 */
	public static String getWeek() {
		return formatDate(new Date(), "E");
	}

	/**
	 * Date string is converted to date. format, see to DateUtils#parsePatterns
	 */
	public static Date parseDate(Object str) {
		if (str == null) {
			return null;
		}
		try {
			return parseDate(str.toString(), parsePatterns);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * Get past days
	 * 
	 * @param date
	 * @return
	 */
	public static long pastDays(Date date) {
		long t = System.currentTimeMillis() - date.getTime();
		return t / (24 * 60 * 60 * 1000);
	}

	/**
	 * Getting past hours
	 * 
	 * @param date
	 * @return
	 */
	public static long pastHour(Date date) {
		long t = System.currentTimeMillis() - date.getTime();
		return t / (60 * 60 * 1000);
	}

	/**
	 * Get past minutes
	 * 
	 * @param date
	 * @return
	 */
	public static long pastMinutes(Date date) {
		long t = System.currentTimeMillis() - date.getTime();
		return t / (60 * 1000);
	}

	/**
	 * Get the number of days between two dates
	 * 
	 * @param before
	 * @param after
	 * @return
	 */
	public static double getDistanceOfTwoDate(Date before, Date after) {
		long beforeTime = before.getTime();
		long afterTime = after.getTime();
		return (afterTime - beforeTime) / (1000 * 60 * 60 * 24);
	}

	/**
	 * How many days are there in a month
	 * 
	 * @param date
	 * @return
	 */
	public static int getMonthHasDays(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Which date is the date of the year?
	 * 
	 * @param date
	 * @return
	 */
	public static int getWeekOfYear(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.WEEK_OF_YEAR);
	}

	/**
	 * Get the start time of a day (e.g. 2018-09-12 00:00:00.000).
	 * 
	 * @param date
	 * @return
	 */
	public static Date getOfDayFirst(Date date) {
		if (date == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * Get the last day of the day.（如：2018-09-12 23:59:59.999）
	 * 
	 * @param date
	 * @return
	 */
	public static Date getOfDayLast(Date date) {
		if (date == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	/**
	 * Get server startup time
	 * 
	 * @param date
	 * @return
	 */
	public static Date getServerStartDate() {
		long time = ManagementFactory.getRuntimeMXBean().getStartTime();
		return new Date(time);
	}

	/**
	 * Format to date range string
	 * 
	 * @param beginDate
	 *            2018-09-01
	 * @param endDate
	 *            2018-09-30
	 * @return 2018-09-01 ~ 2018-09-30
	 */
	public static String formatDateBetweenString(Date beginDate, Date endDate) {
		String begin = DateUtils.formatDate(beginDate);
		String end = DateUtils.formatDate(endDate);
		if (StringUtils.isNoneBlank(begin, end)) {
			return begin + " ~ " + end;
		}
		return null;
	}

	public static void main(String[] args) throws ParseException {

	}

}
