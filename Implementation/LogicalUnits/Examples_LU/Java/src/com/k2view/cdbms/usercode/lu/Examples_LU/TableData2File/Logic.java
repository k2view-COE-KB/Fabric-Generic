/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.Examples_LU.TableData2File;

import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.shared.Globals;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import com.k2view.cdbms.usercode.lu.Examples_LU.*;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.lu.Examples_LU.Globals.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends UserCode {


	public static void fnWriTblData2File() throws Exception {
		String DB;
		java.text.DateFormat dateFormat;
		java.util.Date date = new java.util.Date();
		PrintWriter printWriter = null;
		FileOutputStream fos = null;
		
		java.util.Map<String, Map<String, String>> rsFromTrn = getTranslationsData("trnWriTbl2File");
		for (java.util.Map.Entry<String, Map<String, String>> rsFromTrnInfo : rsFromTrn.entrySet()) {
		    Map<String, String> rowInfo = rsFromTrnInfo.getValue();
		
		    if (rowInfo.get("active") == null || rowInfo.get("active").equalsIgnoreCase("false")) continue;
		
		    if (rowInfo.get("db_name") == null || rowInfo.get("db_name").equals("")) {
		        DB = "ludb";
		    } else {
		        DB = rowInfo.get("db_name");
		    }
		
		    boolean appnd = false;
		    if (rowInfo.get("append") != null) {
		        appnd = Boolean.parseBoolean(rowInfo.get("append"));
		    }
		
		    try {
		        String fileName = rowInfo.get("file_name");
		        Pattern r = Pattern.compile("\\{.*}");
		        Matcher m = r.matcher(fileName);
		        if (m.find()) {
		            String dateFormatFromFile = m.group(0);
		            dateFormatFromFile = dateFormatFromFile.replace("{", "").replace("}", "");
		            dateFormat = new java.text.SimpleDateFormat(dateFormatFromFile);
		            fileName = rowInfo.get("file_full_path") + "/" + rowInfo.get("file_name").replaceAll("\\{.*}", dateFormat.format(date)) + "." + rowInfo.get("file_type");
		        } else {
		            fileName = rowInfo.get("file_full_path") + "/" + rowInfo.get("file_name") + "." + rowInfo.get("file_type");
		        }
		
		        if (!rowInfo.get("file_type").equalsIgnoreCase("xls")) {
		            printWriter = new PrintWriter(new FileOutputStream(new File(fileName), appnd));
		        } else {
		            fos = new FileOutputStream(fileName, appnd);
		        }
		    } catch (FileNotFoundException e) {
		        if (inDebugMode()) reportUserMessage("fnWriLuData2File Exception:" + e.getMessage());
		        throw e;
		    }
		
		    StringBuilder sql = new StringBuilder();
		    if (rowInfo.get("table_name") != null && !rowInfo.get("table_name").equals("") && rowInfo.get("columns_list") != null && !rowInfo.get("columns_list").equals("")) {
		        sql.append("Select " + rowInfo.get("columns_list") + " from " + rowInfo.get("table_name"));
		    } else {
		        sql.append(rowInfo.get("sql"));
		    }
		
		    try {
		        String sepChar = null;
		        if (rowInfo.get("file_type").equalsIgnoreCase("csv")) {
		            sepChar = ",";
		        } else {
		            sepChar = rowInfo.get("delimiter");
		        }
		
		        int rowCnt = 0;
		        HSSFWorkbook workBook = new HSSFWorkbook();
		        HSSFSheet tblShet = workBook.createSheet("Table Data");
		        Db.Rows rs = db(DB).fetch(sql.toString());
		
		        String prefix = "";
		        if (rowInfo.get("headers") != null && rowInfo.get("headers").equalsIgnoreCase("true")) {
		            if (rowInfo.get("file_type").equalsIgnoreCase("xls")) {
		                HSSFRow headerRow = tblShet.createRow(rowCnt);
		                for (int i = 0; i < rs.getColumnNames().size(); i++) {
		                    headerRow.createCell(i).setCellValue(rs.getColumnNames().get(i + 1));
		                }
		                rowCnt++;
		            } else {
		                StringBuilder header = new StringBuilder();
		                for (int i = 0; i < rs.getColumnNames().size(); i++) {
		                    header.append(prefix + rs.getColumnNames().get(i + 1));
		                    prefix = sepChar;
		                }
		                printWriter.println(header.toString());
		            }
		        }
		
		        for (Db.Row row : rs) {
		            if (rowInfo.get("file_type").equalsIgnoreCase("xls")) {
		                HSSFRow tblRow = tblShet.createRow(rowCnt);
		                for (int i = 0; i < row.size(); i++) {
		                    if ((row.get(i + 1) + "").matches("[0-9]+|-[0-9]+")) {
		                        String val = row.get((i + 1)) + "";
		                        if (val.length() > 10) {
		                            tblRow.createCell(i).setCellValue(Long.parseLong((row.get((i + 1)) + "")));
		                        } else {
		                            tblRow.createCell(i).setCellValue(Integer.valueOf((row.get((i + 1)) + "")));
		                        }
		                    } else {
		                        tblRow.createCell(i).setCellValue(row.get((i + 1)) + "");
		                    }
		                }
		                rowCnt++;
		            } else {
		                StringBuilder rowSB = new StringBuilder();
		                prefix = "";
		                for (int i = 0; i < row.size(); i++) {
		                    if ((row.get(i + 1) + "").matches("[0-9]+|-[0-9]+")) {
		                        String val = row.get((i + 1)) + "";
		                        if (val.length() > 10) {
		                            rowSB.append(prefix + Long.parseLong((row.get((i + 1)) + "")));
		                        } else {
		                            rowSB.append(prefix + Integer.valueOf((row.get((i + 1)) + "")));
		                        }
		                    } else {
		                        String val = row.get((i + 1)) + "";
		                        if (val.contains("\n")) {
		                            val = val.replace("\n", " ");
		                            rowSB.append(prefix + val);
		                        } else {
		
		                        }
		                    }
		                    prefix = sepChar;
		                }
		                printWriter.println(row.toString());
		            }
		        }
		        if (!rowInfo.get("file_type").equalsIgnoreCase("xls")) {
		            printWriter.close();
		            printWriter.flush();
		        } else {
		            workBook.write(fos);
		            fos.flush();
		        }
		    } finally {
		        if (fos != null) fos.close();
		    }
		}
		
	}


}
