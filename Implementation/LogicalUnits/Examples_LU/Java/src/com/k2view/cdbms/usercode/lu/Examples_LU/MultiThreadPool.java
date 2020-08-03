package com.k2view.cdbms.usercode.lu.Examples_LU;

import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;
import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiThreadPool {
	protected static Logger log = LoggerFactory.getLogger(MultiThreadPool.class.getName());
	private static MultiThreadPool instance = new MultiThreadPool();
	public java.util.concurrent.ExecutorService executor = null;
    
	private MultiThreadPool(){}
	
	//GET INSTANCE METHOD\\
	public static MultiThreadPool getInstance(){
		return instance;
	}
	
	//Shutdown executor when done
	public void shutDown(){
		this.executor.shutdown();
	}
	
	//return executer and initalize it if it was shutdown
	public java.util.concurrent.ExecutorService getExec(int numOfThreads){
		if(this.executor == null || this.executor.isShutdown())this.executor = java.util.concurrent.Executors.newFixedThreadPool(numOfThreads);
		return this.executor;
	}

}
