package com.cch.common.utils.excel.demo;

import java.io.File;
import java.util.List;

import com.cch.common.utils.excel.ExcelException;

public interface ExcelFileGenerator<T> {
	public File generateXLSX(List<T> dataList, String filePath, String fileName, String sheetName)throws ExcelException;

}
