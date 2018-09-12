package com.cch.common.utils;

import com.cch.common.utils.excel.ExcelUtil;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	try {
			ExcelUtil.XLSX2CSV("D:\\output\\123.xlsx", "D:\\output");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
