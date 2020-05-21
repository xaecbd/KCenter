/**
 * 
 */
package org.nesc.ec.bigdata.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class SqliteUtil{	
	private final static Logger LOG = LoggerFactory.getLogger(SqliteUtil.class);
	protected static Connection conn = null;
	public static void connect(String dbUrl) {
		if (conn != null) {
			return;
		}
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(dbUrl);
		} catch (ClassNotFoundException | SQLException e) {
			LOG.error("Sqlite connect error ", e);
		}
	}

	public static boolean tableExists(String tableName) {
		ResultSet rs = null;
		try (Statement stmt = conn.createStatement();) {
			String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "';";
			LOG.info("Checking if Table '{}' exists", tableName);
			rs = stmt.executeQuery(sql);
			boolean exists = false;
			while (rs.next()) {
				if (rs.getString("name").equals(tableName)) {
					exists = true;
					break;
				}
			}
			return exists;
		} catch (SQLException ex) {
			LOG.error("Sqlite check exists table error ", ex);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					LOG.error("", e);
				}
			}
		}
		return false;
	}


	public static void createTable(String sql) {
		try (Statement stmt = conn.createStatement();) {
			LOG.info("Creating Table: {}", sql);
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			LOG.error("" , e);
		}
	}

	public static void close() {
		try {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (SQLException e) {
			LOG.error("", e);
		}
	}

	public static List<String> loadSql(String fileName){
		List<String> sqlList = new ArrayList<>();
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(new File(fileName)));
			String str = "";
			while ((str=input.readLine())!=null) {
				sqlList.add(str);
			}
		} catch (Exception e) {		
		}finally {
			try {
				if(null!=input) {
					input.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOG.error("", e);
			}
		}
		return sqlList;

	}

	public static void create(String dbPath, String sqlPath) throws Exception {
		if(null==dbPath || "".equals(dbPath)) {
			return;
		}
		SqliteUtil.connect(dbPath);
        List<String> sqlList = loadSql(sqlPath);
        sqlList.forEach((sql)->{
        	SqliteUtil.createTable(sql);        	
        });
        SqliteUtil.close();		
	}

}
