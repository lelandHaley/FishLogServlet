package com.fishlog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

	//private static String url = "jdbc:mysql://127.0.0.1:8889/Fishlog"; 
	private static String url="jdbc:mysql://localhost:8889/Fishlog";
	private String driver="org.gjt.mm.mysql.Driver";
	private static String username="root";
	private static String password= "root";
	private static Connection con = null; 

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	/**
	 * This method gets the database connection
	 * @return Connection : return this connection
	 * @throws SQLException
	 */
	public static Connection getConnection() throws SQLException{
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		con = DriverManager.getConnection(url, username, password);
		return con;
	}

	/**
	 * This method is used to close connection to the database
	 * @param rs	: the result set of after executing the sql statement
	 * @param stmt	: the statement that using to retrieve data from database
	 * @param conn	: the connection that use to connect the database
	 */
	public static void releaseConn(ResultSet rs, Statement stmt, Connection conn) {
		if (rs != null && stmt != null && conn != null) {
			try {
				rs.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}


