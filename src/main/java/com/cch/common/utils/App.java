package com.cch.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.cch.common.utils.excel.annotation.ExcelField;
import com.cch.common.utils.excel.demo.ExcelUtil;

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
		// Field sorting
    	List<Object[]> annotationList = new ArrayList<Object[]>();
		Collections.sort(annotationList, new Comparator<Object[]>() {
			@Override
			public int compare(Object[] o1, Object[] o2) {
				return new Integer(((ExcelField)o1[0]).sort()).compareTo(
						new Integer(((ExcelField)o2[0]).sort()));
			};
		});
		annotationList.sort((Object[] o1, Object[] o2) -> {
			return ((ExcelField) o1[0]).sort()-((ExcelField) o2[0]).sort();
		});
    }
}
