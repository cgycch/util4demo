package com.cch.common.utils.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.cch.common.utils.string.StringUtil;

public abstract class AbstractExcelFileGenerator<T> implements ExcelFileGenerator<T>{
	
	public abstract void init(Class<? extends T> clszz);
	
	public abstract void fillHeader(Row row, List<String> headerList);

	public abstract void fillDataRow(Row row, T data);

	public void extend(List<T> dataList, String filePath, String fileName, String sheetName) {
		// do some extend if you need
	}

	SXSSFWorkbook wb = null;
	Sheet sheet = null;
	Map<String, CellStyle> styleMap = null;
	List<String> headerList = null;
	int rowIdx = 0;

	@SuppressWarnings("unchecked")
	public File generateXLSX(List<T> dataList, String filePath, String fileName, String sheetName)
			throws ExcelException {
		if (StringUtil.isEmpty(filePath) || StringUtil.isEmpty(fileName)) {
			throw new ExcelException("filePath and fileName could not be empty! ");
		}
		if (!StringUtil.isCorrectFileName(fileName)) {
			fileName = StringUtil.renovateFileName(fileName, "");
		}
		if (StringUtil.isEmpty(sheetName)) {
			sheetName = fileName;
		}
		wb = new SXSSFWorkbook();
		sheet = wb.createSheet(sheetName);
		styleMap = new HashMap<>();
		headerList = new ArrayList<>();
		rowIdx = 0;
		if (dataList != null && dataList.size() > 0) {
			init((Class<? extends T>) dataList.get(0).getClass());
			fillHeader(sheet.createRow(rowIdx++), headerList);
		}
		for (T data : dataList) {
			fillDataRow(sheet.createRow(rowIdx++), data);
		}
		extend(dataList, filePath, fileName, sheetName);
		try {
			String excelFileName = filePath + File.separatorChar + fileName;
			File file = new File(excelFileName);
			if(!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			FileOutputStream fileOut = new FileOutputStream(file);
			wb.write(fileOut);
			fileOut.flush();
			fileOut.close();
			wb.close();
			return file;
		} catch (Exception e) {
			throw new ExcelException(e.getMessage());
		}
	}
}
