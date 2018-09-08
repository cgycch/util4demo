package com.cch.common.utils.excel;

import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Streaming version of XSSFWorkbook implementing the "BigGridDemo" strategy.
 * This allows to write very large files without running out of memory 
 * as only a configurable portion of the rows are kept in memory at any one time.
 */
public class SXSSFWorkBookDemo {
	public static void main(String[] args) throws Throwable {
		try (SXSSFWorkbook wb = new SXSSFWorkbook(100);){
			// keep 100 rows in memory, exceeding rows will be flushed to disk
			Sheet sh = wb.createSheet();
			for (int rownum = 0; rownum < 1000; rownum++) {
				Row row = sh.createRow(rownum);
				for (int cellnum = 0; cellnum < 10; cellnum++) {
					Cell cell = row.createCell(cellnum);
					String address = new CellReference(cell).formatAsString();
					cell.setCellValue(address);
				}
			}
			for (int rownum = 0; rownum < 900; rownum++) {
				if(sh.getRow(rownum) != null) {
					System.err.println("accessible row <900 are flushed and not accessible");
				}
			}
			for (int rownum = 900; rownum < 1000; rownum++) {
				if(sh.getRow(rownum) != null) {
					System.err.println("last 100 rows is in memory");
				}
			}
			FileOutputStream out = new FileOutputStream("/temp/sxssf.xlsx");
			wb.write(out);
			out.close();
			wb.dispose();
		} catch (Exception e) {
			System.out.println("Exception: "+e.getMessage());
		}
	}

}
