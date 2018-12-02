package com.cch.common.utils.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cch.common.utils.excel.demo.ExcelFileSizeLimitDemo.FILE_UNIT;
import com.cch.common.utils.file.ZipUtil;

public class ZipFileLimitSizeDemo {
	static String  filePath = "C:\\Users\\cch\\Desktop\\demo";
	static String  zipPath = "C:\\Users\\cch\\Desktop\\outdemo\\test.zip";
	
	public static void main(String[] args) {
		File file = new File(filePath);
		System.out.println(file.getAbsolutePath());
		FILE_UNIT kb = FILE_UNIT.KB;
		System.out.println("UNIT: KB");
		//option one: files limit size before zip
		long maxSize = (long) (1000 * 1024);//1000KB
		optionOneTest(file, zipPath, maxSize, kb);
		
		//option two: files limit size after zip
		//long maxZipSize = (long) (300 * 1024);//300KB
		//optionTwoTest(file, zipPath, maxZipSize, kb);
		System.out.println("end");
	}
	
	public static void optionTwoTest(File source, String target, long maxZipSize, FILE_UNIT unit) {
		List<File> sourceFiles = Arrays.asList(source.listFiles());
		System.out.println("maxZipSize: "+getFileSize(maxZipSize, unit));
		ZipUtil.zipFile(target, sourceFiles);
		File targetFile = new File(target);
		long targetSize = targetFile.length();
		System.out.println("targetFile size : "+ getFileSize(targetSize, unit));
		if(targetSize <= maxZipSize) {
			System.out.println(" bu xu yao zai sheng cheng la ");
		}else {
			int fileNums = (int) (targetSize / maxZipSize);
			if (targetSize % maxZipSize > 0) {
				++ fileNums;
			}
			System.out.println("===== so file num will be===="+ fileNums);
			long allSize = 0;
			for (File file : sourceFiles) {
				allSize += file.length();
			}
			System.out.println("sourceFiles size: " + getFileSize(allSize, unit));
			long baseSize = allSize/fileNums;
			System.out.println(" so base size is: "+ getFileSize(baseSize, unit));
			targetFile.delete();
			String tempPath = target.substring(0, target.lastIndexOf("."))+"_"+fileNums+"-";
			long tempSize = 0;
			int idx = 1;
			List<File> tempList = new ArrayList<>();
			for (File tempFile : sourceFiles) {
				tempSize += tempFile.length();
				if(tempSize >= baseSize) {
					ZipUtil.zipFile(tempPath + (idx++) + ".zip", tempList);
					tempList.clear();
					tempSize = 0;
				}else {
					tempList.add(tempFile);
				}
			}
			if(!tempList.isEmpty()) {
				ZipUtil.zipFile(tempPath + idx + ".zip", tempList);
			}
		}
	}
	
	public static void optionOneTest(File source, String target, long maxSize, FILE_UNIT unit) {
		List<File> sourceFiles = Arrays.asList(source.listFiles());
		long allSize = 0;
		for (File file : sourceFiles) {
			allSize += file.length();
		}
		System.out.println("sourceFiles size: " + getFileSize(allSize, unit));
		System.out.println("maxSize: " + getFileSize(maxSize, unit));
		int fileNums = (int) (allSize / maxSize);
		if (allSize % maxSize > 0) {
			++fileNums;
		}
		System.out.println("===== so file num will be===="+ fileNums);
		if(fileNums == 1) {
			ZipUtil.zipFile(target, sourceFiles);
		}else {
			String tempPath = target.substring(0, target.lastIndexOf("."))+"_"+fileNums+"-";
			long tempSize = 0;
			int idx = 1;
			List<File> tempList = new ArrayList<>();
			for (File tempFile : sourceFiles) {
				tempSize += tempFile.length();
				if(tempSize >= maxSize) {
					ZipUtil.zipFile(tempPath + (idx++) + ".zip", tempList);
					tempList.clear();
					tempSize = 0;
				}else {
					tempList.add(tempFile);
				}
			}
			if(!tempList.isEmpty()) {
				ZipUtil.zipFile(tempPath + idx + ".zip", tempList);
			}
		}
	}
	
	public static double getFileSize(long size, FILE_UNIT unit) {
		double fileSize = 0.00;
		if (size > 0) {
			fileSize = (size / Math.pow(1024, unit.ordinal()));
			fileSize = (double) Math.round(fileSize * 100) / 100;
		}
		return fileSize;
	}
}

