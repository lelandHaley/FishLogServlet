package com.fishlog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBAccess {

	private Connection con;
	
	



	public synchronized boolean insertRecord(String recordName, String recordLat, String recordLon, String recordLure, String recordWeather, String recordSpecies) {
//		try {
//			con = DBConnection.getConnection();
//			Statement s = con.createStatement();
//			String statement = "Select * from users where username = '" + username + "' and password = '" + password + "'";
//			System.out.println(statement);
//			ResultSet r = s.executeQuery(statement);
//			if(r.next())
//				return true;
//			return false;
//		} catch (SQLException e) {
//			System.out.println("failed query for does user: " + username + "exist");
//			return false;
//		}
		
		
		String sqlStatement ="insert into Records(name, lat, lon, lure, weather, species) values (?,?,?,?,?,?)";
		boolean flag = false;
		ResultSet r = null;
        PreparedStatement myStmt = null;
		try {
        	con = DBConnection.getConnection();
			Statement s = con.createStatement();
            myStmt = con.prepareStatement(sqlStatement);
            myStmt.setString(1, recordName);
            myStmt.setFloat(2, Float.valueOf(recordLat));
            myStmt.setFloat(3, Float.valueOf(recordLon));
            myStmt.setString(4, recordLure);
            myStmt.setString(5, recordWeather);
            myStmt.setString(6, recordSpecies);
            flag = (myStmt.executeUpdate()>=0);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	DBConnection.releaseConn(r, myStmt, con);
        }
		return flag;
	}
}
