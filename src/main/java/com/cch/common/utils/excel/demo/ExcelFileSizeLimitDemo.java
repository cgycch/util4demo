package com.cch.common.utils.excel.demo;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cch.common.utils.date.DateUtils;
import com.cch.common.utils.entities.BeanOne;
import com.cch.common.utils.excel.ExcelExport;

public class ExcelFileSizeLimitDemo {

	public static void main(String[] args) {

		String fileName = "E:\\export_demo2_" + DateUtils.getDate("yyyyMMdd") + ".xlsx";
		List<BeanOne> dataList = getDataList(10000);

		// option one : base on record number
		// int maxRows = 100;
		// optionOneTest(fileName, dataList, maxRows);

		// oprion two : base on file size
		int maxSize = 100;
		optionTwoTest(fileName, dataList, maxSize, FILE_UNIT.KB);
	}

	public static <T> void optionOneTest(String fileName, List<T> dataList, int maxRows) {
		if (dataList == null || dataList.isEmpty()) {
			System.out.println("data is null");
			return;
		}
		createFileLimitRows(fileName, dataList, maxRows);
	}

	public static <T> void optionTwoTest(String fileName, List<T> dataList, int maxSize, FILE_UNIT unit) {
		if (dataList == null || dataList.isEmpty()) {
			System.out.println("data is null");
			return;
		}
		createFileLimitSize(fileName, dataList, maxSize, unit);
	}

	public enum FILE_UNIT {
		B, KB, MB, GB, TB
	}

	public static String readableFileSize(long size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	public static String readableFileSize(long size, FILE_UNIT unit) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digit = unit.ordinal();
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digit)) + " " + units[digit];
	}

	public static double readableFileSize2Double(long size, FILE_UNIT unit) {
		double fileSize = 0.00;
		if (size > 0) {
			fileSize = (size / Math.pow(1024, unit.ordinal()));
			// fileSize = new BigDecimal(fileSize).setScale(2,
			// BigDecimal.ROUND_HALF_UP).doubleValue();
			fileSize = (double) Math.round(fileSize * 100) / 100;
		}
		return fileSize;
	}

	/**
	 * @decription Create a file with record
	 * @param fileName
	 * @param dataList
	 * @return
	 */
	public static <T> File createFile(String fileName, List<T> dataList) {
		File file = null;
		try (ExcelExport excelExport = new ExcelExport(null, BeanOne.class)) {
			excelExport.setDataList(dataList);
			excelExport.writeFile(fileName);
			file = new File(fileName);
			System.out.println("Success create file: " + fileName);
		} catch (Exception e) {
			System.out.println("Create file became error: " + e.getMessage());
		}
		return file;
	}

	/**
	 * @decription Create a file with a record limit and a maximum value of maxRows
	 * @param fileName
	 * @param dataList
	 * @param maxRows
	 * @return
	 */
	public static <T> List<File> createFileLimitRows(String fileName, List<T> dataList, int maxRows) {
		List<File> fileList = new ArrayList<>();
		try (ExcelExport excelExport = new ExcelExport(null, BeanOne.class)) {
			int dataSize = dataList.size();
			maxRows = (maxRows == 0) ? dataSize : maxRows;
			int fileNums = dataSize / maxRows;
			if (dataSize % maxRows > 0) {
				++fileNums;
			}
			if (fileNums == 1) {
				createFile(fileName, dataList);
			} else {
				int fromIndex = 0;
				int toIndex = maxRows;
				int dot = fileName.lastIndexOf(".");
				String baseName = fileName.substring(0, dot);
				String extName = fileName.substring(dot);
				for (int i = 0; i < fileNums; i++) {
					List<T> list = dataList.subList(fromIndex, toIndex);
					fromIndex = toIndex;
					toIndex = fromIndex + maxRows;
					toIndex = (toIndex > dataSize) ? dataSize : toIndex;
					StringBuilder nameSb = new StringBuilder(baseName);
					nameSb.append("_").append(fileNums).append("-").append(i + 1).append(extName);
					File file = createFile(nameSb.toString(), list);
					fileList.add(file);
				}
			}
		} catch (Exception e) {
			System.out.println("createFileLimitRows() became error: " + e);
		}
		return fileList;
	}

	/**
	 * @decription Create a size-limited file with a maximum value of fileSize
	 * @param fileName
	 * @param dataList
	 * @param fileSize
	 * @return
	 */
	public static <T> List<File> createFileLimitSize(String fileName, List<T> dataList, double maxSize,
			FILE_UNIT unit) {
		List<File> fileList = new ArrayList<>();
		try (ExcelExport excelExport = new ExcelExport(null, BeanOne.class)) {
			File file = createFile(fileName, dataList);
			double fileSize = readableFileSize2Double(file.length(), unit);
			int fileNum = (int) Math.ceil(fileSize / maxSize);
			if (fileNum <= 1) {
				fileList.add(file);
			} else {
				int maxRows = dataList.size() / fileNum;
				file.delete();
				createFileLimitRows(fileName, dataList, maxRows);
			}
		} catch (Exception e) {
			System.out.println("createFileLimitSize() became error: " + e);
		}
		return fileList;
	}

	/**
	 * @decription prepare file datas with limited size
	 * @param size
	 * @return
	 */
	public static List<BeanOne> getDataList(int size) {
		List<BeanOne> dataList = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			BeanOne bean = new BeanOne();
			bean.setName("name");
			bean.setPass("pass");
			bean.setAge(i);
			bean.setPrice(i + 0.123);
			bean.setDate(new Date());
			bean.setFlag(true);
			dataList.add(bean);
		}
		return dataList;
	}

}
