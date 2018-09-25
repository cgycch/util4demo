package com.cch.common.utils.excel;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.cch.common.utils.base.MethodCallback;
import com.cch.common.utils.base.ObjectUtil;
import com.cch.common.utils.date.DateUtils;
import com.cch.common.utils.excel.annotation.ExcelField;
import com.cch.common.utils.excel.annotation.ExcelField.Type;
import com.cch.common.utils.excel.annotation.ExcelFields;
import com.cch.common.utils.list.ListUtil;
import com.cch.common.utils.refect.ReflectUtils;
import com.cch.common.utils.set.SetUtil;
import com.cch.common.utils.string.StringUtil;

/**
 * Import Excel file (support "XLS" and "XLSX" format)
 */
public class ExcelImport implements Closeable {
	
	private static Logger log = LoggerFactory.getLogger(ExcelImport.class);
	private Workbook wb;
	private Sheet sheet;
	private int headerNum;
	
	/**
	 * Used to clean cache
	 */
	private Set<Class<?>> fieldTypes = SetUtil.newHashSet();
	
	/**
	 * @param path Import the file object and read the first worksheet.
	 * @throws InvalidFormatException 
	 * @throws OException 
	 */
	public ExcelImport(File file) throws InvalidFormatException, IOException {
		this(file, 0, 0);
	}
	
	/**
	 * @param path Import the file object and read the first worksheet.
	 * @param headerNum Header row number, data line number = header line number +1
	 * @throws InvalidFormatException 
	 * @throws IOException 
	 */
	public ExcelImport(File file, int headerNum) 
			throws InvalidFormatException, IOException {
		this(file, headerNum, 0);
	}

	/**
	 * @param path Import the file object
	 * @param headerNum headerNum Header row number, data line number = header line number +1
	 * @param sheetIndexOrName The number or name of the worksheet, starts at 0.
	 * @throws InvalidFormatException 
	 * @throws IOException 
	 */
	public ExcelImport(File file, int headerNum, Object sheetIndexOrName) 
			throws InvalidFormatException, IOException {
		this(file.getName(), new FileInputStream(file), headerNum, sheetIndexOrName);
	}
	
	/**
	 * @param file Import the file object
	 * @param headerNum Header row number, data line number = header line number +1
	 * @param sheetIndexOrName The number or name of the worksheet, starts at 0.
	 * @throws InvalidFormatException 
	 * @throws IOException 
	 */
	public ExcelImport(MultipartFile multipartFile, int headerNum, Object sheetIndexOrName) 
			throws InvalidFormatException, IOException {
		this(multipartFile.getOriginalFilename(), multipartFile.getInputStream(), headerNum, sheetIndexOrName);
	}

	/**
	 * @param path Importing file objects
	 * @param headerNum Header row number, data line number = header line number +1
	 * @param sheetIndexOrName Worksheet number or name
	 * @throws InvalidFormatException 
	 * @throws IOException 
	 */
	public ExcelImport(String fileName, InputStream is, int headerNum, Object sheetIndexOrName) 
			throws InvalidFormatException, IOException {
		if (StringUtil.isBlank(fileName)){
			throw new ExcelException("Import document is empty!");
		}else if(fileName.toLowerCase().endsWith("xls")){    
			this.wb = new HSSFWorkbook(is);    
        }else if(fileName.toLowerCase().endsWith("xlsx")){  
        	this.wb = new XSSFWorkbook(is);
        }else{  
        	throw new ExcelException("The format of the document is incorrect.");
        }
		this.setSheet(sheetIndexOrName, headerNum);
		log.debug("Initialize success.");
	}
	
	/**
	 * 添加到 annotationList
	 */
	private void addAnnotation(List<Object[]> annotationList, ExcelField ef, Object fOrM, Type type, String... groups){
//		if (ef != null && (ef.type()==0 || ef.type()==type)){
		if (ef != null && (ef.type() == Type.ALL || ef.type() == type)){
			if (groups!=null && groups.length>0){
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


	public Workbook getWorkbook() {
		return wb;
	}
	
	/**
	 * Sets the number of rows in the current worksheet and heading.
	 * @author ThinkGem
	 */
	public void setSheet(Object sheetIndexOrName, int headerNum) {
		if (sheetIndexOrName instanceof Integer || sheetIndexOrName instanceof Long){
			this.sheet = this.wb.getSheetAt(ObjectUtil.toInteger(sheetIndexOrName));
		}else{
			this.sheet = this.wb.getSheet(ObjectUtil.toString(sheetIndexOrName));
		}
		if (this.sheet == null){
			throw new ExcelException("没有找到‘"+sheetIndexOrName+"’工作表!");
		}
		this.headerNum = headerNum;
	}

	/**
	 * @param rownum
	 * @return Returns the Row object, if the blank line returns null
	 */
	public Row getRow(int rownum){
		Row row = this.sheet.getRow(rownum);
		if (row == null){
			return null;
		}
		// Verify that it is a blank line. If the blank line returns null?
		short cellNum = 0;
		short emptyNum = 0;
		Iterator<Cell> it = row.cellIterator();
		while (it.hasNext()) {
			cellNum++;
			Cell cell = it.next();
			if (StringUtil.isBlank(cell.toString())) {
				emptyNum++;
			}
		}
		if (cellNum == emptyNum) {
			return null;
		}
		return row;
	}

	/**
	 * Get data line number
	 * @return
	 */
	public int getDataRowNum(){
		return headerNum;
	}
	
	/**
	 * Get the last data line number.
	 * @return
	 */
	public int getLastDataRowNum(){
		//return this.sheet.getLastRowNum() + headerNum;
		return this.sheet.getLastRowNum() + 1;
	}
	
	/**
	 * Get the last column number
	 * @return
	 */
	public int getLastCellNum(){
		Row row = this.getRow(headerNum);
		return row == null ? 0 : row.getLastCellNum();
	}
	
	/**
	 * 获取单元格值
	 * @param row 获取的行
	 * @param column 获取单元格列号
	 * @return 单元格值
	 */
	public Object getCellValue(Row row, int column){
		if (row == null){
			return row;
		}
		Object val = "";
		try{
			Cell cell = row.getCell(column);
			if (cell != null){
				if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
					val = cell.getNumericCellValue();
					if (HSSFDateUtil.isCellDateFormatted(cell)) {
						val = DateUtil.getJavaDate((Double) val); // POI Excel 日期格式转换
					}else{
						if ((Double) val % 1 > 0){
							val = new DecimalFormat("0.00").format(val);
						}else{
							val = new DecimalFormat("0").format(val);
						}
					}
				}else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
					val = cell.getStringCellValue();
				}else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA){
					try {
						val = cell.getStringCellValue();
					} catch (Exception e) {
						FormulaEvaluator evaluator = cell.getSheet().getWorkbook()
								.getCreationHelper().createFormulaEvaluator();
						evaluator.evaluateFormulaCell(cell);
						CellValue cellValue = evaluator.evaluate(cell);
						switch (cellValue.getCellType()) {
						case Cell.CELL_TYPE_NUMERIC:
							val = cellValue.getNumberValue();
							break;
						case Cell.CELL_TYPE_STRING:
							val = cellValue.getStringValue();
							break;
						case Cell.CELL_TYPE_BOOLEAN:
							val = cellValue.getBooleanValue();
							break;
						case Cell.CELL_TYPE_ERROR:
							val = ErrorEval.getText(cellValue.getErrorValue());
							break;
						default:
							val = cell.getCellFormula();
						}
					}
				}else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN){
					val = cell.getBooleanCellValue();
				}else if (cell.getCellType() == Cell.CELL_TYPE_ERROR){
					val = cell.getErrorCellValue();
				}
			}
		}catch (Exception e) {
			return val;
		}
		return val;
	}
	
	/**
	 * 获取导入数据列表
	 * @param cls 导入对象类型
	 * @param groups 导入分组
	 */
	public <E> List<E> getDataList(Class<E> cls, String... groups) throws InstantiationException, IllegalAccessException{
		return getDataList(cls, false, groups);
	}
	
	/**
	 * 获取导入数据列表
	 * @param cls 导入对象类型
	 * @param isThrowException 遇见错误是否抛出异常
	 * @param groups 导入分组
	 */
	public <E> List<E> getDataList(Class<E> cls, final boolean isThrowException, String... groups) throws InstantiationException, IllegalAccessException{
		return getDataList(cls, new MethodCallback() {
			@Override
			public Object execute(Object... params) {
				if (isThrowException){
					Exception ex = (Exception)params[0];
					int rowNum = (int)params[1];
					int columnNum = (int)params[2];
					throw new ExcelException("Get cell value ["+rowNum+","+columnNum+"]", ex);
				}
				return null;
			}
		}, groups);
	}
	/**
	 * 获取导入数据列表
	 * @param cls 导入对象类型
	 * @param isThrowException 遇见错误是否抛出异常
	 * @param groups 导入分组
	 */
	public <E> List<E> getDataList(Class<E> cls, MethodCallback exceptionCallback, String... groups) throws InstantiationException, IllegalAccessException{
		List<Object[]> annotationList = ListUtil.newArrayList();
		// Get annotation field 
		Field[] fs = cls.getDeclaredFields();
		for (Field f : fs){
			ExcelFields efs = f.getAnnotation(ExcelFields.class);
			if (efs != null && efs.value() != null){
				for (ExcelField ef : efs.value()){
					addAnnotation(annotationList, ef, f, Type.IMPORT, groups);
				}
			}
			ExcelField ef = f.getAnnotation(ExcelField.class);
			addAnnotation(annotationList, ef, f, Type.IMPORT, groups);
		}
		// Get annotation method
		Method[] ms = cls.getDeclaredMethods();
		for (Method m : ms){
			ExcelFields efs = m.getAnnotation(ExcelFields.class);
			if (efs != null && efs.value() != null){
				for (ExcelField ef : efs.value()){
					addAnnotation(annotationList, ef, m, Type.IMPORT, groups);
				}
			}
			ExcelField ef = m.getAnnotation(ExcelField.class);
			addAnnotation(annotationList, ef, m, Type.IMPORT, groups);
		}
		// Field sorting
		Collections.sort(annotationList, new Comparator<Object[]>() {
			@Override
			public int compare(Object[] o1, Object[] o2) {
				return new Integer(((ExcelField)o1[0]).sort()).compareTo(
						new Integer(((ExcelField)o2[0]).sort()));
			};
		});
		//log.debug("Import column count:"+annotationList.size());
		// Get excel data
		List<E> dataList = ListUtil.newArrayList();
		for (int i = this.getDataRowNum(); i < this.getLastDataRowNum(); i++) {
			E e = (E)cls.newInstance();
			Row row = this.getRow(i);
			if (row == null){
				continue;
			}
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < annotationList.size(); j++){//Object[] os : annotationList){
				Object[] os = annotationList.get(j);
				ExcelField ef = (ExcelField)os[0];
				int column = (ef.column() != -1) ? ef.column() : j;
				Object val = this.getCellValue(row, column);
				if (val != null){
					// If is dict type, get dict value
					if (StringUtil.isNotBlank(ef.dictType())){
						try{
							Class<?> dictUtils = Class.forName("com.cch.modules.sys.utils.DictUtils");
							val = dictUtils.getMethod("getDictValue", String.class, String.class,
										String.class).invoke(null, ef.dictType(), val.toString(), "");
						} catch (Exception ex) {
							log.info("Get cell value ["+i+","+column+"] error: " + ex.toString());
							val = null;
						}
						//val = DictUtils.getDictValue(val.toString(), ef.dictType(), "");
						//log.debug("Dictionary type value: ["+i+","+colunm+"] " + val);
					}
					// Get param type and type cast
					Class<?> valType = Class.class;
					if (os[1] instanceof Field){
						valType = ((Field)os[1]).getType();
					}else if (os[1] instanceof Method){
						Method method = ((Method)os[1]);
						if ("get".equals(method.getName().substring(0, 3))){
							valType = method.getReturnType();
						}else if("set".equals(method.getName().substring(0, 3))){
							valType = ((Method)os[1]).getParameterTypes()[0];
						}
					}
					//log.debug("Import value type: ["+i+","+column+"] " + valType);
					try {
						if (StringUtil.isNotBlank(ef.attrName())){
							if (ef.fieldType() != Class.class){
								fieldTypes.add(ef.fieldType()); // 先存起来，方便完成后清理缓存
								val = ef.fieldType().getMethod("getValue", String.class).invoke(null, val);
							}
						}else{
							if (val != null){
								if (valType == String.class){
									String s = String.valueOf(val.toString());
									if(StringUtil.endsWith(s, ".0")){
										val = StringUtil.substringBefore(s, ".0");
									}else{
										val = String.valueOf(val.toString());
									}
								}else if (valType == Integer.class){
									val = Double.valueOf(val.toString()).intValue();
								}else if (valType == Long.class){
									val = Double.valueOf(val.toString()).longValue();
								}else if (valType == Double.class){
									val = Double.valueOf(val.toString());
								}else if (valType == Float.class){
									val = Float.valueOf(val.toString());
								}else if (valType == Date.class){
									if (val instanceof String){
										val = DateUtils.parseDate(val);
									}else if (val instanceof Double){
										val = DateUtil.getJavaDate((Double)val); // POI Excel 日期格式转换
									}
								}else{
									if (ef.fieldType() != Class.class){
										fieldTypes.add(ef.fieldType()); // 先存起来，方便完成后清理缓存
										val = ef.fieldType().getMethod("getValue", String.class).invoke(null, val.toString());
									}else{
										// 如果没有指定 fieldType，切自行根据类型查找相应的转换类（com.cch.common.utils.excel.fieldtype.值的类名+Type）
										Class<?> fieldType2 = Class.forName(this.getClass().getName().replaceAll(this.getClass().getSimpleName(), 
												"fieldtype."+valType.getSimpleName()+"Type"));
										fieldTypes.add(fieldType2); // 先存起来，方便完成后清理缓存
										val = fieldType2.getMethod("getValue", String.class).invoke(null, val.toString());
									}
								}
							}
						}
					} catch (Exception ex) {
						log.info("Get cell value ["+i+","+column+"] error: " + ex.toString());
						val = null;
						// 参数：Exception ex, int rowNum, int columnNum
						exceptionCallback.execute(ex, i, column);
					}
					// set entity value
					if (StringUtil.isNotBlank(ef.attrName())){
						ReflectUtils.invokeSetter(e, ef.attrName(), val);
					}else{
						if (os[1] instanceof Field){
							ReflectUtils.invokeSetter(e, ((Field)os[1]).getName(), val);
						}else if (os[1] instanceof Method){
							String mthodName = ((Method)os[1]).getName();
							if ("get".equals(mthodName.substring(0, 3))){
								mthodName = "set"+StringUtil.substringAfter(mthodName, "get");
							}
							ReflectUtils.invokeMethod(e, mthodName, new Class[] {valType}, new Object[] {val});
						}
					}
				}
				sb.append(val+", ");
			}
			dataList.add(e);
			log.debug("Read success: ["+i+"] "+sb.toString());
		}
		return dataList;
	}
	
	@Override
	public void close() {
		Iterator<Class<?>> it = fieldTypes.iterator();
		while(it.hasNext()){
			Class<?> clazz = it.next();
			try {
				clazz.getMethod("clearCache").invoke(null);
			} catch (Exception e) {
				// 报错忽略，有可能没实现此方法
			}
		}
	}

	/**
	 * 导入测试
	 */
	public static void main(String[] args) throws Throwable {
		System.out.println("start at "+DateUtils.formatDateTime(new Date()));
		ExcelImport ei = new ExcelImport(new File("target/export.xlsx"));
		for (int i = ei.getDataRowNum(); i < ei.getLastDataRowNum(); i++) {
			Row row = ei.getRow(i);
			if (row == null){
				continue;
			}
			for (int j = 0; j < row.getLastCellNum(); j++) {
				Object val = ei.getCellValue(row, j);
				System.out.print(val+", ");
			}
			System.out.println();
		}
		ei.close();
		System.out.println("end at "+DateUtils.formatDateTime(new Date()));
	}

}
