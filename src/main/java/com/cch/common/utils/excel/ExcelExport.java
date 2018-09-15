package com.cch.common.utils.excel;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cch.common.utils.base.ObjectUtil;
import com.cch.common.utils.codec.EncodeUtil;
import com.cch.common.utils.excel.annotation.ExcelField;
import com.cch.common.utils.excel.annotation.ExcelField.Align;
import com.cch.common.utils.excel.annotation.ExcelField.Type;
import com.cch.common.utils.excel.annotation.ExcelFields;
import com.cch.common.utils.list.ListUtil;
import com.cch.common.utils.refect.ReflectUtils;
import com.cch.common.utils.set.SetUtil;
import com.cch.common.utils.string.StringUtil;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 
Export Excel file (export XLSX format, support large volume export @see org. apache. poi. ss. Spreadsheet Version)
 */
public class ExcelExport implements Closeable{
	
	private static Logger log = LoggerFactory.getLogger(ExcelExport.class);
			

	private Workbook wb;
	private Sheet sheet;
	private Map<String, CellStyle> styles;
	/**
	 * Current line number
	 */
	private int rownum;
	
	/**
	 * Annotation list（Object[]{ ExcelField, Field/Method }）
	 */
	private List<Object[]> annotationList;
	
	/**
	 * Used to clean cache
	 */
	private Set<Class<?>> fieldTypes = SetUtil.newHashSet();
	
	/**
	 * @param title 
	 * @param cls The entity object gets its title through annotation.ExportField.
	 * @param type Export type (1: export data)
	 */
	public ExcelExport(String title, Class<?> cls){
		this(title, cls, Type.EXPORT);
	}
	
	/**
	 * @param title 
	 * @param cls The entity object gets its title through annotation.ExportField.
	 * @param type Export type (1: export,2:import)
	 * @param groups 
	 */
	public ExcelExport(String title, Class<?> cls, Type type, String... groups){
		this(null, null, title, cls, type, groups);
	}
	
	/**
	 * @param sheetName
	 * @param title 
	 * @param cls The entity object gets its title through annotation.ExportField.
	 * @param type Export type (1: export,2:import)
	 * @param groups
	 */
	public ExcelExport(String sheetName, String title, Class<?> cls, Type type, String... groups){
		this(null, sheetName, title, cls, type, groups);
	}
	
	/**
	 * @param wb 
	 * @param sheetName 
	 * @param title 
	 * @param cls The entity object gets its title through annotation.ExportField.
	 * @param type Export type (1: export,2:import)
	 * @param groups 
	 */
	public ExcelExport(Workbook wb, String sheetName, String title, Class<?> cls, Type type, String... groups){
		if (wb != null){
			this.wb = wb;
		}else{
			this.wb = createWorkbook();
		}
		this.createSheet(sheetName, title, cls, type, groups);
	}
	
	/**
	 * @param title 
	 * @param headerList
	 */
	public ExcelExport(String title, List<String> headerList, List<Integer> headerWidthList) {
		this(null, null, title, headerList, headerWidthList);
	}
	
	/**
	 * @param sheetName 
	 * @param title 
	 * @param headerList
	 */
	public ExcelExport(String sheetName, String title, List<String> headerList, List<Integer> headerWidthList) {
		this(null, sheetName, title, headerList, headerWidthList);
	}
	
	/**
	 * @param wb 
	 * @param sheetName
	 * @param title
	 * @param headerList
	 */
	public ExcelExport(Workbook wb, String sheetName, String title, List<String> headerList, List<Integer> headerWidthList) {
		if (wb != null){
			this.wb = wb;
		}else{
			this.wb = createWorkbook();
		}
		this.createSheet(sheetName, title, headerList, headerWidthList);
	}
	
	/**
	 * Create a workbook
	 */
	public Workbook createWorkbook(){
		return new SXSSFWorkbook(500);
	}

	/**
	 * Get current workbook
	 */
	public Workbook getWorkbook() {
		return wb;
	}
	
	/**
	 * Create worksheet
	 * @param sheetName
	 * @param title
	 * @param cls The entity object gets its title through annotation.ExportField.
	 * @param type Export type (1: export data; 2: export template)
	 * @param groups 
	 */
	public void createSheet(String sheetName, String title, Class<?> cls, Type type, String... groups){
		this.annotationList = ListUtil.newArrayList();
		// Get annotation field
		Field[] fs = cls.getDeclaredFields();
		for (Field f : fs){
			ExcelFields efs = f.getAnnotation(ExcelFields.class);
			if (efs != null && efs.value() != null){
				for (ExcelField ef : efs.value()){
					addAnnotation(annotationList, ef, f, type, groups);
				}
			}
			ExcelField ef = f.getAnnotation(ExcelField.class);
			addAnnotation(annotationList, ef, f, type, groups);
		}
		// Get annotation method
		Method[] ms = cls.getDeclaredMethods();
		for (Method m : ms){
			ExcelFields efs = m.getAnnotation(ExcelFields.class);
			if (efs != null && efs.value() != null){
				for (ExcelField ef : efs.value()){
					addAnnotation(annotationList, ef, m, type, groups);
				}
			}
			ExcelField ef = m.getAnnotation(ExcelField.class);
			addAnnotation(annotationList, ef, m, type, groups);
		}
		// Field sorting
		Collections.sort(annotationList, new Comparator<Object[]>() {
			@Override
			public int compare(Object[] o1, Object[] o2) {
				return new Integer(((ExcelField)o1[0]).sort()).compareTo(
						new Integer(((ExcelField)o2[0]).sort()));
			};
		});
		// Initialize
		List<String> headerList = ListUtil.newArrayList();
		List<Integer> headerWidthList = ListUtil.newArrayList();
		for (Object[] os : annotationList){
			ExcelField ef = (ExcelField)os[0];
			String headerTitle = ef.title();
			// If it is exported, remove the annotation.
			if (type == Type.EXPORT){
				String[] ss = StringUtil.split(headerTitle, "**", 2);
				if (ss.length == 2){
					headerTitle = ss[0];
				}
			}
			headerList.add(headerTitle);
			headerWidthList.add(ef.width());
		}
		//Create worksheet
		this.createSheet(sheetName, title, headerList, headerWidthList);
	}
	
	/**
	 * 添加到 annotationList
	 */
	private void addAnnotation(List<Object[]> annotationList, ExcelField ef, Object fOrM, Type type, String... groups){
//		if (ef != null && (ef.type()==0 || ef.type()==type)){
 		if (ef != null && (ef.type() == Type.ALL || ef.type() == type)){
			if (groups != null && groups.length > 0){
				boolean inGroup = false;
				for (String g : groups){
					if (inGroup){
						break;
					}
					for (String efg : ef.groups()){
						if (StringUtil.equals(g, efg)){
							inGroup = true;
							annotationList.add(new Object[]{ef, fOrM});
							break;
						}
					}
				}
			}else{
				annotationList.add(new Object[]{ef, fOrM});
			}
		}
	}
	
	/**
	 * Create worksheet
	 * @param sheetName
	 * @param title
	 * @param headerList
	 * @param headerWidthList
	 */
	public void createSheet(String sheetName, String title, List<String> headerList, List<Integer> headerWidthList) {
		this.sheet = wb.createSheet(StringUtil.defaultString(sheetName, StringUtil.defaultString(title, "Sheet1")));
		this.styles = createStyles(wb);
		this.rownum = 0;
		// Create title
		if (StringUtil.isNotBlank(title)){
			Row titleRow = sheet.createRow(rownum++);
			titleRow.setHeightInPoints(30);
			Cell titleCell = titleRow.createCell(0);
			titleCell.setCellStyle(styles.get("title"));
			titleCell.setCellValue(title);
			sheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(),
					titleRow.getRowNum(), titleRow.getRowNum(), headerList.size()-1));
		}
		// Create header
		if (headerList == null){
			throw new ExcelException("headerList counld not be null!");
		}
		Row headerRow = sheet.createRow(rownum++);
		headerRow.setHeightInPoints(16);
		for (int i = 0; i < headerList.size(); i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellStyle(styles.get("header"));
			String[] ss = StringUtil.split(headerList.get(i), "**", 2);
			if (ss.length==2){
				cell.setCellValue(ss[0]);
				Comment comment = this.sheet.createDrawingPatriarch().createCellComment(
						new XSSFClientAnchor(0, 0, 0, 0, (short) 3, 3, (short) 5, 6));
				comment.setRow(cell.getRowIndex());
				comment.setColumn(cell.getColumnIndex());
				comment.setString(new XSSFRichTextString(ss[1]));
				cell.setCellComment(comment);
			}else{
				cell.setCellValue(headerList.get(i));
			}
//			sheet.autoSizeColumn(i);
		}
		boolean isDefWidth = (headerWidthList != null && headerWidthList.size() == headerList.size());
		for (int i = 0; i < headerList.size(); i++) {
			int colWidth = -1;
			if (isDefWidth){
				colWidth = headerWidthList.get(i);
			}
			if (colWidth == -1){
				colWidth = sheet.getColumnWidth(i)*2;
				colWidth = colWidth < 3000 ? 3000 : colWidth;
			}
			if (colWidth == 0){
				sheet.setColumnHidden(i, true);
			}else{
				sheet.setColumnWidth(i, colWidth);  
			}
		}
		log.debug("Create sheet {} success.", sheetName);
	}
	
	/**
	 * Create table base style(title,header,data)
	 * @param wb
	 * @return
	 */
	private Map<String, CellStyle> createStyles(Workbook wb) {
		Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
		
		CellStyle style = wb.createCellStyle();

		Font titleFont = wb.createFont();
		titleFont.setFontName("Arial");
		titleFont.setFontHeightInPoints((short) 16);
		style.setFont(titleFont);
		styles.put("title", style);

		style = wb.createCellStyle();
		style.setBorderRight(BorderStyle.THIN);
		style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setBorderTop(BorderStyle.THIN);
		style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setBorderBottom(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		Font dataFont = wb.createFont();
		dataFont.setFontName("Arial");
		dataFont.setFontHeightInPoints((short) 10);
		style.setFont(dataFont);
		styles.put("data", style);
		
		style = wb.createCellStyle();
		style.cloneStyleFrom(styles.get("data"));
		style.setAlignment(HorizontalAlignment.LEFT);
		styles.put("data1", style);

		style = wb.createCellStyle();
		style.cloneStyleFrom(styles.get("data"));
		style.setAlignment(HorizontalAlignment.CENTER);
		styles.put("data2", style);

		style = wb.createCellStyle();
		style.cloneStyleFrom(styles.get("data"));
		style.setAlignment(HorizontalAlignment.RIGHT);
		styles.put("data3", style);
		
		style = wb.createCellStyle();
		style.cloneStyleFrom(styles.get("data"));
		style.setAlignment(HorizontalAlignment.CENTER);
		styles.put("header", style);
		
		style = wb.createCellStyle();
		style.cloneStyleFrom(styles.get("data"));
//		style.setWrapText(true);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		Font headerFont = wb.createFont();
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short) 10);
		headerFont.setBold(true);
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		style.setFont(headerFont);
		styles.put("header", style);
		
		return styles;
	}

	public Row addRow(){
		return sheet.createRow(rownum++);
	}
	
	public Cell addCell(Row row, int column, Object val){
		return this.addCell(row, column, val, Align.AUTO, Class.class, null);
	}
	
	/**
	 * Add a cell
	 * @param row 
	 * @param column
	 * @param val 
	 * @param align Alignment (1: left; 2: Center; 3: right).
	 * @param dataFormat Display style (for example: 0.00,yyyy-MM-dd...)
	 * @return Cell
	 */
	public Cell addCell(Row row, int column, Object val, Align align, Class<?> fieldType, String dataFormat){
		Cell cell = row.createCell(column);
		String defaultDataFormat = "@";
		try {
			if(val == null){
				cell.setCellValue("");
			}else if(fieldType != Class.class){
				fieldTypes.add(fieldType);// save first, clean up cache after completion.
				cell.setCellValue((String)fieldType.getMethod("setValue", Object.class).invoke(null, val));
				try{
					defaultDataFormat = (String)fieldType.getMethod("getDataFormat").invoke(null);
				} catch (Exception ex) {
					defaultDataFormat = "@";
				}
			}else{
				if(val instanceof String) {
					cell.setCellValue((String) val);
				}else if(val instanceof Integer) {
					cell.setCellValue((Integer) val);
					defaultDataFormat = "0";
				}else if(val instanceof Long) {
					cell.setCellValue((Long) val);
					defaultDataFormat = "0";
				}else if(val instanceof Double) {
					cell.setCellValue((Double) val);
					defaultDataFormat = "0.00";
				}else if(val instanceof Float) {
					cell.setCellValue((Float) val);
					defaultDataFormat = "0.00";
				}else if(val instanceof Date) {
					cell.setCellValue((Date) val);
					defaultDataFormat = "yyyy-MM-dd HH:mm";
				}else {
					// If you don't specify fieldType, look for the corresponding transformation class(class name + Type) by type.
					Class<?> fieldType2 = Class.forName(this.getClass().getName().replaceAll(this.getClass().getSimpleName(), 
							"fieldtype."+val.getClass().getSimpleName()+"Type"));
					fieldTypes.add(fieldType2); // save first, clean up cache after completion.
					cell.setCellValue((String)fieldType2.getMethod("setValue", Object.class).invoke(null, val));
				}
			}
//			if (val != null){
				CellStyle style = styles.get("data_column_"+column);
				if (style == null){
					style = wb.createCellStyle();
					style.cloneStyleFrom(styles.get("data"+(align.value()>=1&&align.value()<=3?align.value():"")));
					if (dataFormat != null){
						defaultDataFormat = dataFormat;
					}
			        style.setDataFormat(wb.createDataFormat().getFormat(defaultDataFormat));
					styles.put("data_column_" + column, style);
				}
				cell.setCellStyle(style);
//			}
		} catch (Exception ex) {
			log.info("Set cell value ["+row.getRowNum()+","+column+"] error: " + ex.toString());
			cell.setCellValue(ObjectUtil.toString(val));
		}
		return cell;
	}

	/**
	 * Add data (add data through annotation.ExportField)
	 * @return list
	 */
	public <E> ExcelExport setDataList(List<E> list){
		for (E e : list){
			int colunm = 0;
			Row row = this.addRow();
			StringBuilder sb = new StringBuilder();
			for (Object[] os : annotationList){
				ExcelField ef = (ExcelField)os[0];
				Object val = null;
				// Get entity value
				try{
					if (StringUtil.isNotBlank(ef.attrName())){
						val = ReflectUtils.invokeGetter(e, ef.attrName());
					}else{
						if (os[1] instanceof Field){
							val = ReflectUtils.invokeGetter(e, ((Field)os[1]).getName());
						}else if (os[1] instanceof Method){
							val = ReflectUtils.invokeMethod(e, ((Method)os[1]).getName(), new Class[] {}, new Object[] {});
						}
					}
					// If is dict, get dict label
					if (StringUtil.isNotBlank(ef.dictType())){
						Class<?> dictUtils = Class.forName("com.cch.modules.sys.utils.DictUtils");
						val = dictUtils.getMethod("getDictLabel", String.class, String.class,
									String.class).invoke(null, ef.dictType(), val==null?"":val.toString(), "");
						//val = DictUtils.getDictLabel(val==null?"":val.toString(), ef.dictType(), "");
					}
				}catch(Exception ex) {
					// Failure to ignore
					log.info(ex.toString());
					val = "";
				}
				String dataFormat = ef.dataFormat();
				try {
					// Get formatting parameters for Json formatting annotations
					JsonFormat jf = e.getClass().getMethod("get"+StringUtil.capitalize(ef.attrName())).getAnnotation(JsonFormat.class);
					if (jf != null && jf.pattern() != null){
						dataFormat = jf.pattern();
					}
				} catch (Exception e1) {
					// If fail,use the default.
				}
				this.addCell(row, colunm++, val, ef.align(), ef.fieldType(), dataFormat);
				sb.append(val + ", ");
			}
			log.debug("Write success: ["+row.getRowNum()+"] "+sb.toString());
		}
		return this;
	}
	
	/**
	 * Output data stream
	 * @param os
	 */
	public ExcelExport write(OutputStream os){
		try{
			wb.write(os);
		}catch(IOException ex){
			log.error(ex.getMessage(), ex);
		}
		return this;
	}
	
	/**
	 * Output to client
	 * @param fileName Output file name
	 */
	public ExcelExport write(HttpServletResponse response, String fileName){
		response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename="+EncodeUtil.encodeUrl(fileName));
		try {
			write(response.getOutputStream());
		} catch (IOException ex) {
			log.error(ex.getMessage(), ex);
		}
		return this;
	}
	
	/**
	 * output to a file
	 * @param fileName Output file name
	 */
	public ExcelExport writeFile(String name) throws FileNotFoundException, IOException{
		FileOutputStream os = new FileOutputStream(name);
		this.write(os);
		return this;
	}
	
	@Override
	public void close() {
		if (wb instanceof SXSSFWorkbook){
			((SXSSFWorkbook)wb).dispose();
		}
		Iterator<Class<?>> it = fieldTypes.iterator();
		while(it.hasNext()){
			Class<?> clazz = it.next();
			try {
				clazz.getMethod("clearCache").invoke(null);
			} catch (Exception e) {
				// Error reporting may not be achieved.
			}
		}
	}
	
	/**
	 * Export test
	 */
	public static void main(String[] args) throws Throwable {
		long begin =System.currentTimeMillis();
		System.out.println("Export beggin: "+begin);
		//Initialization header
		List<String> headerList = ListUtil.newArrayList();
		for (int i = 1; i <= 10; i++) {
			headerList.add("header"+i);
		}

		//Initialization data set
		List<String> rowList = ListUtil.newArrayList();
		for (int i = 1; i <= headerList.size(); i++) {
			rowList.add("data"+i);
		}
		List<List<String>> dataList = ListUtil.newArrayList();
		for (int i = 1; i <=100; i++) {
			dataList.add(rowList);
		}
		
		// Create a Sheet table and import data
		ExcelExport ee = new ExcelExport("sheet1", "title1", headerList, null);
		for (int i = 0; i < dataList.size(); i++) {
			Row row = ee.addRow();
			for (int j = 0; j < dataList.get(i).size(); j++) {
				ee.addCell(row, j, dataList.get(i).get(j));
			}
		}
		
		// Create a Sheet table and import data
		ee.createSheet("sheet2", "title2", headerList, null);
		for (int i = 0; i < dataList.size(); i++) {
			Row row = ee.addRow();
			for (int j = 0; j < dataList.get(i).size(); j++) {
				ee.addCell(row, j, dataList.get(i).get(j)+"2");
			}
		}
		
		// output to a file
		ee.writeFile("target/export.xlsx");
		ee.close();
		
		log.debug("Export success.");
		long time =System.currentTimeMillis();
		System.out.println("Export success."+ time);
		System.out.println("time ： "+ (time - begin)/(1000));
		
	}

}
