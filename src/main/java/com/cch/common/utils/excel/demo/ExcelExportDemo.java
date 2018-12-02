package com.cch.common.utils.excel.demo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cch.common.utils.date.DateUtils;
import com.cch.common.utils.entities.BeanOne;
import com.cch.common.utils.excel.ExcelExport;



public class ExcelExportDemo {
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		long t1 = System.currentTimeMillis();
		System.out.println("start");
		List<BeanOne> list = new ArrayList<>();
		for (int i = 0; i < 3000; i++) {
			BeanOne bean = new BeanOne();
			bean.setName("name"+i);
			//bean.setPass("pass"+i);
			bean.setAge(i);
			bean.setDate(new Date());
			bean.setPrice(123.456d + i);
			bean.setFlag(false);
			list.add(bean);
		}
		//String fileName = "export_demo" + DateUtils.getDate("yyyyMMdd") + ".xlsx";
		String fileName = "E:\\export_demo" + DateUtils.getDate("yyyyMMdd") + ".xlsx";
		try(ExcelExport ee = new ExcelExport(null, BeanOne.class)){
			//ee.setDataList(list).write(response, fileName);
			ee.getWorkbook().getSheetAt(0).getRow(0).getCell(0).setCellValue("Client");
			ee.setDataList(list).writeFile(fileName);
		}
		System.out.println("end");
		long t2 = System.currentTimeMillis();
		System.out.println("time : "+(t2-t1)/1000);
	}

}
