package com.cch.common.utils.excel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.crypt.temp.AesZipFileZipEntrySource;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {
	private ExcelUtil() {
	}

	/**
	 * decrypt excel Stream
	 * 
	 * @param inputStream
	 * @param pwd
	 * @return
	 * @throws Exception
	 */
	public static InputStream decrypt(final InputStream inputStream, final String pwd) throws Exception {
		try {
			POIFSFileSystem fs = new POIFSFileSystem(inputStream);
			EncryptionInfo info = new EncryptionInfo(fs);
			Decryptor d = Decryptor.getInstance(info);
			if (!d.verifyPassword(pwd)) {
				throw new RuntimeException("incorrect password");
			}
			return d.getDataStream(fs);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
	/**
	 * 
	 * @param inputStream
	 * @param filename
	 * @param pwd
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static void encrypt(final InputStream inputStream, final String filename, final String pwd)
			throws InvalidFormatException, IOException, GeneralSecurityException {
		POIFSFileSystem fs = null;
		FileOutputStream fos = null;
		OPCPackage opc = null;
		try {
			fs = new POIFSFileSystem();
			EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
			Encryptor enc = Encryptor.getInstance(info);
			enc.confirmPassword(pwd);
			opc = OPCPackage.open(inputStream);
			opc.save(enc.getDataStream(fs));
			fos = new FileOutputStream(filename);
			fs.writeFilesystem(fos);
		} finally {
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(opc);
			IOUtils.closeQuietly(fs);
			IOUtils.closeQuietly(inputStream);
		}
	}

	/**
	 * workbook.getNumberOfSheets()
	 * 
	 * @param inputStream
	 * @throws Exception
	 */
	public static void printSheetCount(final InputStream inputStream) throws Exception {
		AesZipFileZipEntrySource source = AesZipFileZipEntrySource.createZipEntrySource(inputStream);
		try {
			OPCPackage pkg = OPCPackage.open(source);
			try {
				XSSFWorkbook workbook = new XSSFWorkbook(pkg);
				try {
					System.out.println("sheet count: " + workbook.getNumberOfSheets());
				} finally {
					IOUtils.closeQuietly(workbook);
				}
			} finally {
				IOUtils.closeQuietly(pkg);
			}
		} finally {
			IOUtils.closeQuietly(source);
		}
	}
	/**
	 * 
	 * @param strSource
	 * @param strDestination
	 * @throws Exception
	 */
	public static void XLSX2CSV(String strSource, String strDestination) throws Exception{
         new ExcelToCSV().convertExcelToCSV(strSource, strDestination);
	}


}
