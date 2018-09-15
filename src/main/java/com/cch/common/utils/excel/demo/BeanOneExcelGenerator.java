package com.cch.common.utils.excel.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;

import com.cch.common.utils.entities.BeanOne;

public class BeanOneExcelGenerator extends AbstractExcelFileGenerator<BeanOne> {

	@Override
	public void init(Class<? extends BeanOne> clszz) {
		styleMap.put("myStyle", wb.createCellStyle());
		headerList.add("name");
		headerList.add("pass");
		headerList.add("age");
		headerList.add("price");
		headerList.add("flag");
		headerList.add("date");
	}

	@Override
	public void fillHeader(Row row, List<String> headerList) {
		int column = 0;
		for (String header : headerList) {
			row.createCell(column++).setCellValue(header);
		}
	}

	@Override
	public void fillDataRow(Row row, BeanOne data) {
		int column = 0;
		row.createCell(column++).setCellValue(data.getName());
		row.createCell(column++).setCellValue(data.getPass());
		row.createCell(column++).setCellValue(data.getAge());
		row.createCell(column++).setCellValue(data.getPrice());
		row.createCell(column++).setCellValue(data.getFlag());
		row.createCell(column).setCellValue(data.getDate());
	}
	@Override
	public void extend(List<BeanOne> dataList, String filePath, String fileName, String sheetName) {
		System.out.println("here is my extend");
	}
	
	public static void main(String[] args) {
		ExcelFileGenerator<BeanOne> generator = new BeanOneExcelGenerator();
    	List<BeanOne> dataList = new ArrayList<>();
    	BeanOne bean = new BeanOne();
    	dataList.add(bean);
    	File file = generator.generateXLSX(dataList, "D:\\output", "beanOne.xlsx", "sheet1");
    	System.out.println(file.getAbsolutePath());
	}

}
