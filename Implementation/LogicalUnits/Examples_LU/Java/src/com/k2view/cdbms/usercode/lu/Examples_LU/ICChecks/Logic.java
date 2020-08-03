/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.Examples_LU.ICChecks;

import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;

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

import static com.k2view.cdbms.lut.FunctionDef.functionContext;
import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.lu.Examples_LU.Globals.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends UserCode {


	public static void fnExecuteICChecks() throws Exception {
		java.text.DateFormat clsDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		java.util.Date currentTime = new java.util.Date();
		String writeTime = clsDateFormat.format(currentTime);
		String icCheckTblName = "k2view_" + getLuType().luName + ".ic_checks";
		Object[] params = null;
		String sqlInsert = "insert into " + icCheckTblName + " (entity_id, ic_check_name, ic_check_time, ic_check_msg, ic_check_runtime_in_sec) values (?, ?, ?, ?, ?)";
		boolean tblCre = true;
		Map <String, Map <String,String>> icChecksTrn = null;
		icChecksTrn = getTranslationsData("trnIcChecks");
		if(icChecksTrn == null){
			log.error("fnExecuteIcChecks ERROR: trnIcChecks wan't found in project please check!");
		}
		boolean rejEntAtEnd = false;
		String icMsg = null;
		for(java.util.Map.Entry<String, Map <String,String>> trnVals : icChecksTrn.entrySet()){
			long startTime = System.nanoTime();
			Map <String,String> trnVal = (Map <String,String>) trnVals.getValue();
			if(trnVal.get("active") != null && trnVal.get("active").equalsIgnoreCase("false"))continue;
			String icTitle = trnVal.get("title");
			icMsg = "Entity " + getInstanceID() + " Failed During IC Check " + trnVals.getKey() + " Ic Check Title:" + icTitle;
			boolean icPass = false;
			String operator = trnVal.get("operator");
			int value = Integer.valueOf(trnVal.get("value"));
			String action = trnVal.get("action");
			if(!inDebugMode() && trnVal.get("write_result_to_table") != null && trnVal.get("write_result_to_table").equalsIgnoreCase("true") && tblCre){
				if(trnVal.get("interface_name") == null)throw new Exception("fnExecuteIcChecks - Interface name is null and write_result_to_table is Enable!"); 
				db(trnVal.get("interface_name")).execute("create table if not exists " + icCheckTblName + " (entity_id text, ic_check_name text, ic_check_time text, ic_check_msg text, ic_check_runtime_in_sec text, PRIMARY KEY (entity_id,ic_check_name,ic_check_time))");
				tblCre = false;
			}
			int sqlRes = 0;
			try{
				sqlRes = Integer.valueOf(ludb().fetch(trnVal.get("sql")).firstValue().toString());
			}catch(SQLException e){
				log.error("fnExecuteIcChecks ERROR:", e);
			}
			
			if( operator.equals("=")  && sqlRes == value )icPass = true;
			if( operator.equals("!=") && sqlRes != value )icPass = true;
			if( operator.equals(">")  && sqlRes > value  )icPass = true;
			if( operator.equals(">=") && sqlRes >= value )icPass = true;
			if( operator.equals("<")  && sqlRes < value  )icPass = true;
			if( operator.equals("<=") && sqlRes <= value )icPass = true;
		
			if(!icPass){
				if(!inDebugMode() && trnVal.get("write_result_to_table") != null && trnVal.get("write_result_to_table").equalsIgnoreCase("true")){
					if(trnVal.get("interface_name") == null)throw new Exception("fnExecuteIcChecks - Interface name is null and write_result_to_table is Enable!"); 
					long totalIcCheckTime = System.nanoTime() - startTime;
					params = new Object[]{getInstanceID(), trnVals.getKey(), writeTime, icMsg, (totalIcCheckTime/ 1000000000.0)};
					DBExecute(trnVal.get("interface_name"), sqlInsert, params);
				}
				if(action.equalsIgnoreCase("Reject_Entity")){
					rejectInstance(icMsg);
					break;
				}else if(action.equalsIgnoreCase("Report_To_Log")){
					if(inDebugMode())reportUserMessage(icMsg);
					log.warn(icMsg);
					break;
				}else if(action.equalsIgnoreCase("Report_To_Log_And_Execute_Activity")){
					if(inDebugMode())reportUserMessage(icMsg);
					log.warn(icMsg);
					FunctionDef method = (FunctionDef) LUTypeFactoryImpl.getInstance().getTypeByName(getLuType().luName).ludbFunctions.get(trnVal.get("functionName"));
					if (method == null) {
						throw new NoSuchMethodException(String.format("user function '%s' was not found", trnVal.get("functionName")));
					} else {
						try {
							method.invoke(null, functionContext());
						} catch (ReflectiveOperationException | InterruptedException e) {
							log.error("colValidationManager: Failed to invoke user function!", e);
							if (inDebugMode())reportUserMessage("colValidationManager: Failed to invoke user function!, Exception Details:" + e.getMessage());
						}
					}
					break;
				}else if(action.equalsIgnoreCase("Execute_Activity")){
					FunctionDef method = (FunctionDef) LUTypeFactoryImpl.getInstance().getTypeByName(getLuType().luName).ludbFunctions.get(trnVal.get("functionName"));
					if (method == null) {
						throw new NoSuchMethodException(String.format("user function '%s' was not found", trnVal.get("functionName")));
					} else {
						try {
							method.invoke(null, functionContext());
						} catch (ReflectiveOperationException | InterruptedException e) {
							log.error("colValidationManager: Failed to invoke user function!", e);
							if (inDebugMode())reportUserMessage("colValidationManager: Failed to invoke user function!, Exception Details:" + e.getMessage());
						}
					}
					break;
				}else if(action.equalsIgnoreCase("Reject_Entity_At_End_Of_IC")){
					if(inDebugMode())reportUserMessage(icMsg);
					log.warn(icMsg);
					rejEntAtEnd = true;
				}
			}
		};
		if(rejEntAtEnd)rejectInstance(icMsg);
	}

	
	
	
	
}
