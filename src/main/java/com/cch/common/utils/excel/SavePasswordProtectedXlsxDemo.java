package com.cch.common.utils.excel;
import org.apache.poi.examples.util.TempFileUtils;
import org.apache.poi.poifs.crypt.temp.EncryptedTempData;
import org.apache.poi.poifs.crypt.temp.SXSSFWorkbookWithCustomZipEntrySource;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;

/**
 * An example that outputs a simple generated workbook that is password protected.
 * The example highlights how to do this in streaming way.
 * <p><ul>
 * <li>The example demonstrates that all temp files are removed.
 * <li><code>SXSSFWorkbookWithCustomZipEntrySource</code> extends SXSSFWorkbook to ensure temp files are encrypted.
 * </ul><p>
 */
public class SavePasswordProtectedXlsxDemo {

    public static void main(String[] args) throws Exception {
        TempFileUtils.checkTempFiles();
        String filename = "D:\\passwordProtected.xlsx";
        String password = "123456";
        SXSSFWorkbookWithCustomZipEntrySource wb = new SXSSFWorkbookWithCustomZipEntrySource();
        try {
            for(int i = 0; i < 10; i++) {
                SXSSFSheet sheet = wb.createSheet("Sheet" + i);
                for(int r = 0; r < 1000; r++) {
                    SXSSFRow row = sheet.createRow(r);
                    for(int c = 0; c < 100; c++) {
                        SXSSFCell cell = row.createCell(c);
                        cell.setCellValue("abcd");
                    }
                }
                System.out.println("sheet can get row 0 :"+(sheet.getRow(0)!=null));
            }
            EncryptedTempData tempData = new EncryptedTempData();
            try {
                wb.write(tempData.getOutputStream());
                ExcelUtil.encrypt(tempData.getInputStream(), filename, password);
                System.out.println("Saved " + filename);
            } finally {
                tempData.dispose();
            }
        } finally {
            wb.close();
            wb.dispose();
        }
        TempFileUtils.checkTempFiles();
    }

}
