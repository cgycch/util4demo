package com.cch.common.utils.excel;

import java.io.File;
import java.util.List;

public interface ExcelFileGenerator<T> {
	public File generateXLSX(List<T> dataList, String filePath, String fileName, String sheetName)throws ExcelException;

}
